package com.cooking.service;

import com.cooking.dto.ConversationResponse;
import com.cooking.dto.MessageRequest;
import com.cooking.dto.MessageResponse;
import com.cooking.entity.Message;
import com.cooking.entity.User;
import com.cooking.repository.MessageRepository;
import com.cooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Transactional
    public MessageResponse sendMessage(MessageRequest request) {
        String username = getCurrentUsername();
        if (username == null) {
            throw new RuntimeException("请先登录");
        }

        User sender = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new RuntimeException("接收者不存在"));

        if (sender.getId().equals(receiver.getId())) {
            throw new RuntimeException("不能给自己发消息");
        }

        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(request.getContent().trim());
        message.setIsRead(false);

        message = messageRepository.save(message);
        return MessageResponse.from(message);
    }

    public List<MessageResponse> getConversation(Long otherUserId) {
        String username = getCurrentUsername();
        if (username == null) {
            throw new RuntimeException("请先登录");
        }

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        List<Message> messages = messageRepository.findConversation(currentUser, otherUser);
        
        // 标记消息为已读
        markMessagesAsRead(currentUser, otherUser);

        return messages.stream()
                .map(MessageResponse::from)
                .collect(Collectors.toList());
    }

    public List<ConversationResponse> getConversations() {
        String username = getCurrentUsername();
        if (username == null) {
            throw new RuntimeException("请先登录");
        }

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        List<Message> lastMessages = messageRepository.findConversations(currentUser);

        return lastMessages.stream()
                .map(message -> {
                    User otherUser = message.getSender().getId().equals(currentUser.getId())
                            ? message.getReceiver()
                            : message.getSender();
                    Long unreadCount = messageRepository.countUnreadMessagesFromUser(otherUser, currentUser);
                    return ConversationResponse.from(message, currentUser, unreadCount);
                })
                .collect(Collectors.toList());
    }

    public Long getUnreadCount() {
        String username = getCurrentUsername();
        if (username == null) {
            return 0L;
        }

        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return 0L;
        }

        return messageRepository.countUnreadMessages(user);
    }

    @Transactional
    public void markMessagesAsRead(User currentUser, User otherUser) {
        // 标记来自otherUser的未读消息为已读
        List<Message> unreadMessages = messageRepository.findConversation(currentUser, otherUser)
                .stream()
                .filter(m -> m.getReceiver().getId().equals(currentUser.getId()) && !m.getIsRead())
                .collect(Collectors.toList());
        
        for (Message message : unreadMessages) {
            message.setIsRead(true);
            messageRepository.save(message);
        }
    }

    private String getCurrentUsername() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            if (username == null || username.equals("anonymousUser")) {
                return null;
            }
            return username;
        } catch (Exception e) {
            return null;
        }
    }
}

