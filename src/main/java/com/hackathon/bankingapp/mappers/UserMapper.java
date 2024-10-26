package com.hackathon.bankingapp.mappers;

import com.hackathon.bankingapp.dto.UserResponseDto;
import com.hackathon.bankingapp.entities.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponseDto mapToResponseDto(User user) {
        return new UserResponseDto(user.getName(), user.getEmail(), user.getPhoneNumber(),
                user.getAddress(), user.getAccountNumber().toString(), user.getHashedPassword());
    }
}
