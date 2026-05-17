package com.cinema.movie_booking.controller;

import com.cinema.movie_booking.dto.ChatRequestDTO;
import com.cinema.movie_booking.dto.ChatResponseDTO;
import com.cinema.movie_booking.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping
    public ResponseEntity<ChatResponseDTO> chat(@RequestBody ChatRequestDTO request) {
        try {
            String reply = chatbotService.getChatbotResponse(request);
            return ResponseEntity.ok(new ChatResponseDTO(reply));
        } catch (Exception e) {
            // Log lỗi ra console backend để debug
            System.err.println("[CHATBOT ERROR] " + e.getClass().getSimpleName() + ": " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("[CHATBOT CAUSE] " + e.getCause().getMessage());
            }
            return ResponseEntity.ok(new ChatResponseDTO(
                "Lỗi từ AI: " + e.getMessage()
            ));
        }
    }
}
