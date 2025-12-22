package com.cooking.dto;

import com.cooking.entity.Message;
import com.cooking.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {
    private UserResponse otherUser;
    private MessageResponse lastMessage;
    private Long unreadCount;
    private LocalDateTime lastMessageTime;

    public static ConversationResponse from(Message message, User currentUser, Long unreadCount) {
        User otherUser = message.getSender().getId().equals(currentUser.getId()) 
            ? message.getReceiver() 
            : message.getSender();
        
        return new ConversationResponse(
            UserResponse.from(otherUser),
            MessageResponse.from(message),
            unreadCount,
            message.getCreatedAt()
        );
    }
}

