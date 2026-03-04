package com.project.hanspoon.recipe.component;

import com.project.hanspoon.recipe.dto.IngredientDto;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecipeParserTest {

    private final RecipeParser parser = new RecipeParser();

    @Test
    void parse_정수소수_포맷으로_치환한다() {
        IngredientDto flour = IngredientDto.builder()
                .name("밀가루")
                .baseAmount(100)
                .unit("g")
                .build();
        IngredientDto milk = IngredientDto.builder()
                .name("우유")
                .baseAmount(33.3)
                .unit("ml")
                .build();

        String parsed = parser.parse(
                "@밀가루 와 @우유",
                Map.of("밀가루", flour, "우유", milk),
                1.0
        );

        assertEquals("밀가루100g 와 우유33.3ml", parsed);
    }

    @Test
    void parse_본문이_null이면_빈문자열을_반환한다() {
        String parsed = parser.parse(null, Map.of(), 1.0);
        assertEquals("", parsed);
    }
}
