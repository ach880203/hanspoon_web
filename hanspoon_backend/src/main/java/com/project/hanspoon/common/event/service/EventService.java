package com.project.hanspoon.common.event.service;

import com.project.hanspoon.common.event.dto.EventDto;
import com.project.hanspoon.common.event.entity.Event;
import com.project.hanspoon.common.event.repository.EventRepository;
import com.project.hanspoon.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;

    public Page<EventDto> getEventList(Pageable pageable) {
        return eventRepository.findAll(pageable).map(EventDto::from);
    }

    public Page<EventDto> getActiveEventList(Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        return eventRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(now, now, pageable)
                .map(EventDto::from);
    }

    @Transactional
    public EventDto getEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException("존재하지 않는 이벤트입니다."));

        event.setViewCount(event.getViewCount() + 1);
        return EventDto.from(event);
    }

    @Transactional
    public EventDto createEvent(EventDto dto) {
        Event event = Event.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .thumbnailUrl(dto.getThumbnailUrl())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .build();

        return EventDto.from(eventRepository.save(event));
    }

    @Transactional
    public EventDto updateEvent(Long eventId, EventDto dto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException("존재하지 않는 이벤트입니다."));

        event.setTitle(dto.getTitle());
        event.setContent(dto.getContent());
        event.setThumbnailUrl(dto.getThumbnailUrl());
        event.setStartDate(dto.getStartDate());
        event.setEndDate(dto.getEndDate());

        return EventDto.from(event);
    }

    @Transactional
    public void deleteEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException("존재하지 않는 이벤트입니다."));
        eventRepository.delete(event);
    }
}
