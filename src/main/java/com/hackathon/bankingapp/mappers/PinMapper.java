package com.hackathon.bankingapp.mappers;

import com.hackathon.bankingapp.dto.PinResponseDto;
import org.springframework.stereotype.Component;

@Component
public class PinMapper {

    public PinResponseDto toPinResponseDto(String message) {
        return new PinResponseDto(message);
    }

}
