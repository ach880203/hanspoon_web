package com.project.hanspoon.shop.product.dto;

import com.project.hanspoon.shop.product.entity.ProductImageType;
import lombok.*;

/**
 * ProductImageResponseDto
 * - “상품 이미지” 정보를 클라이언트(프론트/앱)로 내려주기 위한 응답 DTO
 * - ProductImage 엔티티를 그대로 노출하지 않고,
 *   화면에 필요한 최소 정보만 추려서 제공하는 목적
 *
 * 보통 상세 페이지에서 사용:
 * - 이미지 목록 렌더링 (imgUrl)
 * - 파일명 표시가 필요하면 originalName 활용
 * - 대표 이미지 표시/정렬을 위해 repYn 활용
 */
@Getter
// 응답 DTO는 보통 읽기 전용이므로 Getter만 제공
@NoArgsConstructor
// Jackson/프레임워크 처리, 또는 테스트에서 객체 생성 편의를 위해 기본 생성자 제공
@AllArgsConstructor
// 모든 필드를 받는 생성자: 매핑/테스트 용이
@Builder
// 빌더 패턴: 필요한 필드만 선택적으로 조립 가능 (서비스/매퍼에서 쓰기 편함)
public class ProductImageResponseDto {

    /**
     * 이미지 PK
     * - product_image_id 값을 내려주는 식별자
     * - 프론트에서 key로 쓰거나, 삭제/수정 요청 시 식별자로 활용 가능
     */
    private Long id;

    /**
     * 원본 파일명
     * - 사용자가 업로드한 실제 파일명
     * - UI에서 다운로드명/표시명으로 쓰거나, 관리 페이지에서 확인 용도
     *
     * ※ 보안/개인정보 측면에서 원본 파일명 노출이 불필요하면 제외해도 됨(리뷰 참고)
     */
    private String originalName;

    /**
     * 노출 URL
     * - 화면에서 <img src="...">로 바로 사용 가능한 접근 경로
     * - 예) "/images/uuid.jpg" 또는 CDN URL
     */
    private String imgUrl;

    /**
     * 대표 이미지 여부
     * - true: 대표(썸네일) 이미지
     * - false: 일반 이미지
     *
     * 프론트에서:
     * - 대표 먼저 정렬하거나
     * - “대표 배지” 표시 등의 UI 처리 가능
     */
    private boolean repYn;

    private ProductImageType imageType;
}
