package com.project.hanspoon.oneday.wish.dto;

public record WishToggleResponse(
        Long userId,
        Long classProductId,
        boolean wished
) {

}
