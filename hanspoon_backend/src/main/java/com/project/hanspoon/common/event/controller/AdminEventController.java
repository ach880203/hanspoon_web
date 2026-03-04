package com.project.hanspoon.common.event.controller;

import com.project.hanspoon.common.dto.PageResponse;
import com.project.hanspoon.common.event.dto.EventDto;
import com.project.hanspoon.common.event.service.EventService;
import com.project.hanspoon.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/events")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminEventController {

    private final EventService eventService;

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<PageResponse<EventDto>>> list(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<EventDto> events = eventService.getEventList(pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(events)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventDto>> get(@PathVariable("id") Long eventId) {
        return ResponseEntity.ok(ApiResponse.ok(eventService.getEvent(eventId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EventDto>> createEvent(@RequestBody EventDto dto) {
        return ResponseEntity.ok(ApiResponse.ok("이벤트가 등록되었습니다.", eventService.createEvent(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EventDto>> updateEvent(@PathVariable("id") Long eventId,
            @RequestBody EventDto dto) {
        return ResponseEntity.ok(ApiResponse.ok("이벤트가 수정되었습니다.", eventService.updateEvent(eventId, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable("id") Long eventId) {
        eventService.deleteEvent(eventId);
        return ResponseEntity.ok(ApiResponse.ok("이벤트가 삭제되었습니다."));
    }
}
