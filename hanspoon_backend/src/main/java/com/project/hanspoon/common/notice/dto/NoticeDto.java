package com.project.hanspoon.common.notice.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeDto {
    private Long noticeId;
    
    @Size(max = 200)
    private String title;
    
    private String content;
    private Boolean isImportant;
    private Integer viewCount;
    private String createdAt;
}
