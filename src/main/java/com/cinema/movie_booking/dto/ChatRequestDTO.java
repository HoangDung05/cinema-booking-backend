package com.cinema.movie_booking.dto;

import lombok.Data;
import java.util.List;

@Data
public class ChatRequestDTO {
    private String message;
    private List<ChatMessage> history;

    @Data
    public static class ChatMessage {
        private String role;
        private String text;
    }
}
