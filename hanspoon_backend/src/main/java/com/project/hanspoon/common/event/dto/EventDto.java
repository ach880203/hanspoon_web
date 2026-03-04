package com.project.hanspoon.common.event.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.hanspoon.common.event.entity.Event;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventDto {

    private Long eventId;
    private String title;
    private String content;
    private String thumbnailUrl;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer viewCount;
    private LocalDateTime createdAt;

    @JsonProperty("isActive")
    private boolean isActive;

    public static EventDto from(Event event) {
        return EventDto.builder()
                .eventId(event.getEventId())
                .title(event.getTitle())
                .content(event.getContent())
                .thumbnailUrl(event.getThumbnailUrl())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .viewCount(event.getViewCount())
                .createdAt(event.getCreatedAt())
                .isActive(event.isActive())
                .build();
    }
}
