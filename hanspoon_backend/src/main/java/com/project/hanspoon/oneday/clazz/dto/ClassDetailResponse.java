package com.project.hanspoon.oneday.clazz.dto;

import com.project.hanspoon.oneday.clazz.domain.Level;
import com.project.hanspoon.oneday.clazz.domain.RecipeCategory;
import com.project.hanspoon.oneday.clazz.domain.RunType;
import com.project.hanspoon.oneday.clazz.entity.ClassProduct;
import java.util.List;

public record ClassDetailResponse(
        Long id,
        String title,
        String description,
        String detailDescription,
        String detailImageData,
        List<String> detailImageDataList,
        Level level,
        RunType runType,
        RecipeCategory category,
        Long instructorId,
        String locationAddress,
        Double locationLat,
        Double locationLng,
        String instructorName,
        String instructorBio,
        String instructorSpecialty,
        String instructorCareer,
        String instructorProfileImageData
) {
    public static ClassDetailResponse from(ClassProduct p) {
        Long instructorId = (p.getInstructor() != null) ? p.getInstructor().getId() : null;
        String instructorName = (p.getInstructor() != null && p.getInstructor().getUser() != null)
                ? p.getInstructor().getUser().getUserName()
                : null;
        String instructorBio = (p.getInstructor() != null) ? p.getInstructor().getBio() : null;
        String instructorSpecialty = (p.getInstructor() != null) ? p.getInstructor().getSpecialty() : null;
        String instructorCareer = (p.getInstructor() != null) ? p.getInstructor().getCareer() : null;
        String instructorProfileImageData = (p.getInstructor() != null) ? p.getInstructor().getProfileImageData() : null;

        return new ClassDetailResponse(
                p.getId(),
                p.getTitle(),
                p.getDescription(),
                p.getDetailDescription(),
                p.getDetailImageData(),
                p.getDetailImages().stream().map(img -> img.getImageData()).toList(),
                p.getLevel(),
                p.getRunType(),
                p.getCategory(),
                instructorId,
                p.getLocationAddress(),
                p.getLocationLat(),
                p.getLocationLng(),
                instructorName,
                instructorBio,
                instructorSpecialty,
                instructorCareer,
                instructorProfileImageData
        );
    }
}
