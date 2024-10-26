package com.hackathon.bankingapp.mappers;

import com.hackathon.bankingapp.dto.MessageDto;
import org.springframework.stereotype.Component;

@Component
public class MessageMapper {
    public MessageDto toMessageDto(String message) {
        return new MessageDto(message);
    }
}
