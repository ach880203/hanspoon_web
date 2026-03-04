package com.project.hanspoon.oneday.wish.dto;

import java.time.LocalDateTime;

public record WishItemResponse(
        Long wishId,
        Long classProductId,
        String classTitle,
        LocalDateTime wishedAt
) {
}
