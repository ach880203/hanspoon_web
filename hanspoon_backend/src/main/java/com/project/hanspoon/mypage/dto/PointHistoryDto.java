package com.project.hanspoon.mypage.dto;

import com.project.hanspoon.common.user.constant.PointType;
import com.project.hanspoon.common.user.entity.PointHistory;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointHistoryDto {
    private Long id;
    private int amount;
    private PointType type;
    private String description;
    private LocalDateTime createdAt;

    public static PointHistoryDto fromEntity(PointHistory entity) {
        return PointHistoryDto.builder()
                .id(entity.getId())
                .amount(entity.getAmount())
                .type(entity.getType())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
