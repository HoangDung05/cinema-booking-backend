package com.cinema.movie_booking.service;

import com.cinema.movie_booking.dto.ChatRequestDTO;

public interface ChatbotService {
    String getChatbotResponse(ChatRequestDTO request);
}
