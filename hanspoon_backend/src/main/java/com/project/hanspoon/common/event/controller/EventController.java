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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping({ "", "/list" })
    public ResponseEntity<ApiResponse<PageResponse<EventDto>>> list(
            @RequestParam(value = "activeOnly", defaultValue = "false") boolean activeOnly,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<EventDto> events = activeOnly ? eventService.getActiveEventList(pageable)
                : eventService.getEventList(pageable);

        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(events)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventDto>> view(@PathVariable("id") Long eventId) {
        return ResponseEntity.ok(ApiResponse.ok(eventService.getEvent(eventId)));
    }
}
