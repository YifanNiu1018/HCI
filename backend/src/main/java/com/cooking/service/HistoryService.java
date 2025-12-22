package com.cooking.service;

import com.cooking.dto.HistoryResponse;
import com.cooking.entity.Dish;
import com.cooking.entity.History;
import com.cooking.entity.Note;
import com.cooking.entity.User;
import com.cooking.repository.HistoryRepository;
import com.cooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final HistoryRepository historyRepository;
    private final UserRepository userRepository;

    @Transactional
    public void recordDishView(Long dishId) {
        String username = getCurrentUsername();
        if (username == null) {
            return; // 未登录用户不记录历史
        }

        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return;
        }

        // 检查是否已有该菜品的最近记录（5分钟内）
        List<History> recentHistories = historyRepository.findByUserAndDishId(user, dishId);
        if (!recentHistories.isEmpty()) {
            History recent = recentHistories.get(0);
            // 如果最近5分钟内浏览过，更新浏览时间
            if (recent.getViewedAt().isAfter(java.time.LocalDateTime.now().minusMinutes(5))) {
                recent.setViewedAt(java.time.LocalDateTime.now());
                historyRepository.save(recent);
                return;
            }
        }

        // 创建新记录
        History history = new History();
        history.setUser(user);
        Dish dish = new Dish();
        dish.setId(dishId);
        history.setDish(dish);
        historyRepository.save(history);
    }

    @Transactional
    public void recordNoteView(Long noteId) {
        String username = getCurrentUsername();
        if (username == null) {
            return; // 未登录用户不记录历史
        }

        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return;
        }

        // 检查是否已有该笔记的最近记录（5分钟内）
        List<History> recentHistories = historyRepository.findByUserAndNoteId(user, noteId);
        if (!recentHistories.isEmpty()) {
            History recent = recentHistories.get(0);
            // 如果最近5分钟内浏览过，更新浏览时间
            if (recent.getViewedAt().isAfter(java.time.LocalDateTime.now().minusMinutes(5))) {
                recent.setViewedAt(java.time.LocalDateTime.now());
                historyRepository.save(recent);
                return;
            }
        }

        // 创建新记录
        History history = new History();
        history.setUser(user);
        Note note = new Note();
        note.setId(noteId);
        history.setNote(note);
        historyRepository.save(history);
    }

    public List<HistoryResponse> getUserHistories() {
        String username = getCurrentUsername();
        if (username == null) {
            throw new RuntimeException("请先登录");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        List<History> histories = historyRepository.findByUserOrderByViewedAtDesc(user);
        
        // 只返回最近100条记录
        return histories.stream()
                .limit(100)
                .map(history -> {
                    HistoryResponse response = new HistoryResponse();
                    response.setId(history.getId());
                    response.setViewedAt(history.getViewedAt());
                    
                    if (history.getDish() != null) {
                        com.cooking.entity.Dish dish = history.getDish();
                        response.setDish(com.cooking.dto.DishResponse.from(dish, false));
                        response.setType("dish");
                    } else if (history.getNote() != null) {
                        com.cooking.entity.Note note = history.getNote();
                        response.setNote(com.cooking.dto.NoteResponse.from(note));
                        response.setType("note");
                    }
                    
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void clearHistory() {
        String username = getCurrentUsername();
        if (username == null) {
            throw new RuntimeException("请先登录");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        List<History> histories = historyRepository.findByUserOrderByViewedAtDesc(user);
        historyRepository.deleteAll(histories);
    }

    private String getCurrentUsername() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return null;
        }
    }
}

