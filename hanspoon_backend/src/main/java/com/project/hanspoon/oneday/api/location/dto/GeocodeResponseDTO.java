package com.project.hanspoon.oneday.api.location.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 주소 -> 좌표 변환 응답 DTO
 * - address: 사용자에게 보여줄 주소
 * - lat: 위도
 * - lng: 경도
 */
@Setter
@Getter
public class GeocodeResponseDTO {

    private String address;
    private double lat;
    private double lng;

    public GeocodeResponseDTO() {
        // 기본 생성자(직렬화/역직렬화 대비)
    }

    public GeocodeResponseDTO(String address, double lat, double lng) {
        this.address = address;
        this.lat = lat;
        this.lng = lng;
    }

}
