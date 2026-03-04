package com.project.hanspoon.shop.product.dto;

import com.project.hanspoon.shop.constant.ProductCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * ProductRequestDto
 * - 상품 “등록/수정” 요청에서 클라이언트가 보내는 입력값을 담는 DTO
 * - Controller에서 @Valid(또는 @Validated)로 검증을 수행해
 *   잘못된 값이 서비스/DB까지 내려가는 것을 1차적으로 차단한다.
 *
 * 사용 시나리오 예:
 * - 등록(Create): id는 null, 나머지 필드는 필수
 * - 수정(Update): id가 존재(필수일 가능성 높음), 나머지 필드는 수정 대상 값
 */
@Getter
@Setter
// 요청 DTO는 바인딩 과정에서 setter가 편하긴 함(스프링이 필드에 값을 채우기 위해)
// 다만 Builder/Setter 혼용은 실수 여지가 있으니 사용 방식은 팀 규칙으로 통일하는 게 좋음
@NoArgsConstructor
// 스프링 바인딩/역직렬화(Jackson)에서 기본 생성자가 필요할 수 있음
@AllArgsConstructor
// 테스트/매핑에서 편하게 생성 가능
@Builder
// 테스트/서비스 레이어에서 조립하기 편함
public class ProductRequestDto {

    /**
     * 상품 ID
     * - “수정” 시 대상 상품을 식별하기 위한 값
     * - “등록” 시에는 DB에서 생성되므로 null 가능
     *
     * 주의:
     * - 등록/수정이 같은 DTO를 쓰면 id의 필수 여부가 상황에 따라 달라짐
     *   (등록: 없어도 됨, 수정: 있어야 함)
     */
    private Long id;

    /**
     * 카테고리
     * - enum 타입을 그대로 받음
     * - @NotNull: null이면 검증 실패 (카테고리 미선택 방지)
     */
    @NotNull(message = "카테고리는 필수입니다.")
    private ProductCategory category;

    /**
     * 상품명
     * - @NotBlank: null/""/"   " 모두 실패 (공백만 입력 방지)
     * - @Size(max=100): DB 컬럼 length(100)과 동일하게 맞춰 “DB 저장 실패”를 사전에 차단
     */
    @NotBlank(message = "상품명은 필수입니다.")
    @Size(max = 100, message = "상품명은 100자 이하로 입력해주세요.")
    private String name;

    /**
     * 가격
     * - @Min(0): 음수 가격 입력 방지
     *
     * 주의:
     * - int는 기본값이 0이라, 값이 누락되면 “0으로 들어오는” 경우가 생길 수 있음
     *   (JSON 바인딩/폼 전송 방식에 따라 다름)
     */
    @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
    private int price;

    /**
     * 재고
     * - @Min(0): 음수 재고 입력 방지
     * - 0이면 품절 상태로 해석 가능
     *
     * 주의:
     * - price와 동일하게 int 기본값 0 이슈가 있을 수 있음
     */
    @Min(value = 0, message = "재고는 0 이상이어야 합니다.")
    private int stock;

    @Size(max = 50000, message = "상품 상세 내용은 50000자 이하로 입력해주세요.")
    private String detailContent;
}
