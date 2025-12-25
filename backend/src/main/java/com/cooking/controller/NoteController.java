package com.cooking.controller;

import com.cooking.dto.NoteRequest;
import com.cooking.dto.NoteResponse;
import com.cooking.dto.UpdateNoteVisibilityRequest;
import com.cooking.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @PostMapping
    public ResponseEntity<NoteResponse> createNote(@Valid @RequestBody NoteRequest request) {
        try {
            NoteResponse response = noteService.createNote(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/my")
    public ResponseEntity<List<NoteResponse>> getMyNotes() {
        try {
            List<NoteResponse> notes = noteService.getUserNotes();
            return ResponseEntity.ok(notes);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/public")
    public ResponseEntity<List<NoteResponse>> getPublicNotes() {
        List<NoteResponse> notes = noteService.getPublicNotes();
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/public/search")
    public ResponseEntity<List<NoteResponse>> searchPublicNotes(@RequestParam(required = false) String keyword) {
        List<NoteResponse> notes = noteService.searchPublicNotes(keyword);
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoteResponse> getNoteById(@PathVariable Long id) {
        try {
            NoteResponse note = noteService.getNoteById(id);
            return ResponseEntity.ok(note);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/drafts")
    public ResponseEntity<List<NoteResponse>> getDrafts() {
        try {
            List<NoteResponse> drafts = noteService.getDrafts();
            return ResponseEntity.ok(drafts);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/draft")
    public ResponseEntity<NoteResponse> saveDraft(@RequestBody NoteRequest request) {
        try {
            NoteResponse response = noteService.saveDraft(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/draft/{id}")
    public ResponseEntity<NoteResponse> updateDraft(
            @PathVariable Long id,
            @RequestBody NoteRequest request) {
        try {
            NoteResponse response = noteService.updateDraft(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/visibility")
    public ResponseEntity<NoteResponse> updateNoteVisibility(
            @PathVariable Long id,
            @Valid @RequestBody UpdateNoteVisibilityRequest request) {
        try {
            NoteResponse response = noteService.updateNoteVisibility(id, request.getIsPublic());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNote(@PathVariable Long id) {
        try {
            noteService.deleteNote(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/favorite")
    public ResponseEntity<Map<String, Boolean>> toggleFavorite(@PathVariable Long id) {
        try {
            NoteResponse note = noteService.toggleFavorite(id);
            return ResponseEntity.ok(Map.of("isFavorite", note.getIsFavorite()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/following")
    public ResponseEntity<List<NoteResponse>> getFollowingNotes() {
        try {
            List<NoteResponse> notes = noteService.getFollowingNotes();
            return ResponseEntity.ok(notes);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

