package com.cinema.movie_booking.service.impl;

import com.cinema.movie_booking.dto.ChatRequestDTO;
import com.cinema.movie_booking.dto.SeatStatusDTO;
import com.cinema.movie_booking.entity.Movie;
import com.cinema.movie_booking.entity.Showtime;
import com.cinema.movie_booking.repository.MovieRepository;
import com.cinema.movie_booking.service.ChatbotService;
import com.cinema.movie_booking.service.ShowtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatbotServiceImpl implements ChatbotService {

    private final MovieRepository movieRepository;
    private final ShowtimeService showtimeService;

    @Value("${gemini.api.key:AIzaSyCuwAxoXh6-Rnp2ZiDnZTn5EKxxMqNT7-Y}")
    private String geminiApiKey;

    @Override
    public String getChatbotResponse(ChatRequestDTO request) {
        String systemContext = buildSystemContext();

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + geminiApiKey;

        // Build request body for Gemini API
        Map<String, Object> requestBody = new HashMap<>();

        // System Instruction
        Map<String, Object> systemInstruction = new HashMap<>();
        Map<String, Object> systemParts = new HashMap<>();
        systemParts.put("text", systemContext);
        systemInstruction.put("parts", systemParts);
        requestBody.put("system_instruction", systemInstruction);

        // Contents (History + current message)
        List<Map<String, Object>> contents = new ArrayList<>();
        
        if (request.getHistory() != null) {
            // Lọc bỏ message chào ban đầu của hệ thống
            for (int i = 1; i < request.getHistory().size(); i++) {
                ChatRequestDTO.ChatMessage m = request.getHistory().get(i);
                Map<String, Object> messageContent = new HashMap<>();
                messageContent.put("role", m.getRole());
                List<Map<String, String>> partsList = new ArrayList<>();
                Map<String, String> partText = new HashMap<>();
                partText.put("text", m.getText());
                partsList.add(partText);
                messageContent.put("parts", partsList);
                contents.add(messageContent);
            }
        }

        // Current message
        Map<String, Object> currentMessage = new HashMap<>();
        currentMessage.put("role", "user");
        List<Map<String, String>> currentPartsList = new ArrayList<>();
        Map<String, String> currentPartText = new HashMap<>();
        currentPartText.put("text", request.getMessage());
        currentPartsList.add(currentPartText);
        currentMessage.put("parts", currentPartsList);
        contents.add(currentMessage);

        requestBody.put("contents", contents);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                    return (String) parts.get(0).get("text");
                }
            }
            return "Xin lỗi, không nhận được câu trả lời từ AI.";
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi kết nối với Gemini API: " + e.getMessage());
        }
    }

    private String buildSystemContext() {
        List<Movie> movies = movieRepository.findAll();
        List<Showtime> showtimes = showtimeService.getAllShowtimes();

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy HH:mm");
        String currentDateTime = now.format(formatter);

        StringBuilder sb = new StringBuilder();
        sb.append("Bạn là trợ lý ảo tư vấn của rạp chiếu phim. Hãy trả lời ngắn gọn, tự nhiên, thân thiện và lịch sự bằng tiếng Việt.\n");
        sb.append("THỜI GIAN HIỆN TẠI: Hôm nay là ").append(currentDateTime).append(". (Hãy dùng thông tin này để biết ngày/giờ thực tế khi khách hỏi về 'hôm nay', 'ngày mai', 'sắp chiếu').\n\n");
        
        sb.append("DỮ LIỆU RẠP PHIM HIỆN TẠI:\n");
        sb.append("- Phim đang chiếu: ");
        String moviesStr = movies.stream()
            .map(m -> "\"" + m.getTitle() + "\" (" + m.getDuration() + " phút)")
            .collect(Collectors.joining(", "));
        sb.append(moviesStr).append(".\n");

        sb.append("- Lịch chiếu: ");
        String showtimesStr = showtimes.stream()
            .filter(s -> s.getStartTime().isAfter(now)) // Chỉ lấy suất chiếu từ thời điểm hiện tại trở đi
            .map(s -> {
                // Tính số lượng ghế trống
                long availableSeats = showtimeService.getSeatsByShowtimeId(s.getId()).stream()
                    .filter(seat -> "AVAILABLE".equals(seat.getStatus()))
                    .count();
                return "Phim \"" + s.getMovie().getTitle() + "\" chiếu lúc " + s.getStartTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + ", giá " + s.getPrice() + "đ (còn trống " + availableSeats + " ghế)";
            })
            .collect(Collectors.joining("; "));
        sb.append(showtimesStr).append(".\n\n");

        sb.append("HƯỚNG DẪN TRẢ LỜI:\n");
        sb.append("1. Dựa vào 'Lịch chiếu' và 'THỜI GIAN HIỆN TẠI', nếu khách hỏi 'hôm nay có phim gì', hãy đối chiếu ngày tháng để chỉ kể tên các phim có suất chiếu trong ngày hôm nay.\n");
        sb.append("2. Báo giá vé chính xác dựa vào Lịch chiếu. Trả lời số ghế trống nếu được hỏi.\n");
        sb.append("3. Tuyệt đối chỉ trả lời các thông tin xoay quanh rạp phim, phim, suất chiếu, ghế trống, vé. Từ chối lịch sự các chủ đề khác.\n");
        sb.append("4. LƯU Ý QUAN TRỌNG: Hãy trả lời bằng VĂN BẢN THƯỜNG (PLAIN TEXT). KHÔNG sử dụng định dạng Markdown (như dấu ** để in đậm, hay dấu * ở đầu dòng). Hãy dùng dấu gạch ngang (-) để liệt kê.");

        return sb.toString();
    }
}
