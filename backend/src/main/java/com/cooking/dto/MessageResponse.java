package com.cooking.dto;

import com.cooking.entity.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private Long id;
    private UserResponse sender;
    private UserResponse receiver;
    private String content;
    private Boolean isRead;
    private LocalDateTime createdAt;

    public static MessageResponse from(Message message) {
        return new MessageResponse(
            message.getId(),
            UserResponse.from(message.getSender()),
            UserResponse.from(message.getReceiver()),
            message.getContent(),
            message.getIsRead(),
            message.getCreatedAt()
        );
    }
}

