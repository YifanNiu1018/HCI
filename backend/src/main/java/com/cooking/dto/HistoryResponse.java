package com.cooking.dto;

import com.cooking.entity.History;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoryResponse {
    private Long id;
    private DishResponse dish;
    private NoteResponse note;
    private LocalDateTime viewedAt;
    private String type; // "dish" or "note"

    public static HistoryResponse from(History history) {
        HistoryResponse response = new HistoryResponse();
        response.setId(history.getId());
        response.setViewedAt(history.getViewedAt());
        
        if (history.getDish() != null) {
            response.setDish(DishResponse.from(history.getDish(), false));
            response.setType("dish");
        } else if (history.getNote() != null) {
            response.setNote(NoteResponse.from(history.getNote()));
            response.setType("note");
        }
        
        return response;
    }
}

