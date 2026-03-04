package com.project.hanspoon.recipe.service;

import com.project.hanspoon.common.user.repository.UserRepository;
import com.project.hanspoon.recipe.dto.IngredientDto;
import com.project.hanspoon.recipe.dto.IngredientGroupDto;
import com.project.hanspoon.recipe.dto.InstructionDto;
import com.project.hanspoon.recipe.dto.InstructionGroupDto;
import com.project.hanspoon.recipe.dto.RecipeDetailDto;
import com.project.hanspoon.recipe.dto.RecipeFormDto;
import com.project.hanspoon.recipe.entity.Recipe;
import com.project.hanspoon.recipe.entity.RecipeIngredient;
import com.project.hanspoon.recipe.entity.RecipeIngredientGroup;
import com.project.hanspoon.recipe.entity.RecipeInstruction;
import com.project.hanspoon.recipe.entity.RecipeInstructionGroup;
import com.project.hanspoon.recipe.repository.IngredientGroupRepository;
import com.project.hanspoon.recipe.repository.IngredientRepository;
import com.project.hanspoon.recipe.repository.InstructionGroupRepository;
import com.project.hanspoon.recipe.repository.InstructionRepository;
import com.project.hanspoon.recipe.repository.RecipeRelationRepository;
import com.project.hanspoon.recipe.repository.RecommendationRepository;
import com.project.hanspoon.recipe.repository.RecipeRepository;
import com.project.hanspoon.recipe.repository.RecipeRevRepository;
import com.project.hanspoon.recipe.repository.RecipeWishesRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;
    @Mock
    private IngredientGroupRepository ingredientGroupRepository;
    @Mock
    private IngredientRepository ingredientRepository;
    @Mock
    private InstructionRepository instructionRepository;
    @Mock
    private InstructionGroupRepository instructionGroupRepository;
    @Mock
    private RecipeRelationRepository recipeRelationRepository;
    @Mock
    private RecipeWishesRepository recipeWishesRepository;
    @Mock
    private RecipeRevRepository recipeRevRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RecommendationRepository recommendationRepository;

    private RecipeService 서비스생성() {
        return new RecipeService(
                recipeRepository,
                ingredientGroupRepository,
                ingredientRepository,
                instructionRepository,
                instructionGroupRepository,
                recipeRelationRepository,
                recipeWishesRepository,
                recipeRevRepository,
                userRepository,
                recommendationRepository
        );
    }

    @Test
    void convertToGram_단위별_환산이_정상동작한다() {
        RecipeService service = 서비스생성();

        assertEquals(30.0, service.convertToGram("큰술", 2.0));
        assertEquals(10.0, service.convertToGram("작은술", 2.0));
        assertEquals(400.0, service.convertToGram("컵", 2.0));
        assertEquals(1200.0, service.convertToGram("근", 2.0));
        assertEquals(20.0, service.convertToGram("g", 20.0));
        assertEquals(7.0, service.convertToGram("알수없음", 7.0));
    }

    @Test
    void getRecipeDtl_조리문_원본템플릿을_유지한다() {
        RecipeService service = 서비스생성();

        Recipe recipe = Recipe.builder()
                .id(1L)
                .title("테스트 레시피")
                .recipeIngredientGroup(new ArrayList<>())
                .recipeInstructionGroup(new ArrayList<>())
                .subRecipeRelations(new ArrayList<>())
                .recipeRevs(new ArrayList<>())
                .recipeIngs(new ArrayList<>())
                .build();

        RecipeIngredientGroup ingredientGroup = RecipeIngredientGroup.builder()
                .recipe(recipe)
                .name("재료")
                .sortOrder(1)
                .ingredients(new ArrayList<>())
                .build();

        RecipeIngredient ingredient = RecipeIngredient.builder()
                .recipeIngredientGroup(ingredientGroup)
                .name("밀가루")
                .baseAmount(100)
                .unit("g")
                .main(true)
                .ratio(100)
                .build();
        ingredientGroup.getIngredients().add(ingredient);
        recipe.getRecipeIngredientGroup().add(ingredientGroup);

        RecipeInstructionGroup instructionGroup = RecipeInstructionGroup.builder()
                .recipe(recipe)
                .title("반죽")
                .sortOrder(1)
                .instructions(new ArrayList<>())
                .build();
        RecipeInstruction instruction = RecipeInstruction.builder()
                .recipeInstructionGroup(instructionGroup)
                .stepOrder(1)
                .content("@밀가루 넣기")
                .build();
        instructionGroup.getInstructions().add(instruction);
        recipe.getRecipeInstructionGroup().add(instructionGroup);

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));
        RecipeDetailDto dto = service.getRecipeDtl(1L, null);

        assertEquals("@밀가루 넣기", dto.getInstructionGroup().get(0).getInstructions().get(0).getContent());
        assertFalse(dto.isWished());
    }

    @Test
    void saveIngredientsAndInstructions_main재료합계를_비율기준으로_사용한다() {
        RecipeService service = 서비스생성();
        Recipe recipe = 기본레시피();

        RecipeFormDto form = RecipeFormDto.builder()
                .ingredientGroup(List.of(
                        IngredientGroupDto.builder()
                                .name("반죽")
                                .sortOrder(1)
                                .ingredients(List.of(
                                        IngredientDto.builder().name("밀가루").baseAmount(100).unit("g").main(true).build(),
                                        IngredientDto.builder().name("소금").baseAmount(10).unit("g").main(false).build(),
                                        IngredientDto.builder().name("물").baseAmount(200).unit("g").main(true).build()
                                ))
                                .build()
                ))
                .instructionGroup(List.of(
                        InstructionGroupDto.builder()
                                .title("섞기")
                                .sortOrder(1)
                                .instructions(List.of(
                                        InstructionDto.builder().stepOrder(1).content("섞는다").build()
                                ))
                                .build()
                ))
                .build();

        service.saveIngredientsAndInstructions(recipe, form, null);

        ArgumentCaptor<RecipeIngredient> captor = ArgumentCaptor.forClass(RecipeIngredient.class);
        verify(ingredientRepository, times(3)).save(captor.capture());
        verify(ingredientGroupRepository).deleteByRecipe(recipe);
        verify(instructionGroupRepository).deleteByRecipe(recipe);

        List<RecipeIngredient> 저장재료 = captor.getAllValues();
        assertEquals(33.333, 저장재료.get(0).getRatio(), 0.01);
        assertEquals(3.333, 저장재료.get(1).getRatio(), 0.01);
        assertEquals(66.666, 저장재료.get(2).getRatio(), 0.01);
    }

    @Test
    void saveIngredientsAndInstructions_main재료가_없으면_전체합계를_비율기준으로_사용한다() {
        RecipeService service = 서비스생성();
        Recipe recipe = 기본레시피();

        RecipeFormDto form = RecipeFormDto.builder()
                .ingredientGroup(List.of(
                        IngredientGroupDto.builder()
                                .name("반죽")
                                .sortOrder(1)
                                .ingredients(List.of(
                                        IngredientDto.builder().name("재료A").baseAmount(100).unit("g").main(false).build(),
                                        IngredientDto.builder().name("재료B").baseAmount(50).unit("g").main(false).build()
                                ))
                                .build()
                ))
                .instructionGroup(List.of())
                .build();

        service.saveIngredientsAndInstructions(recipe, form, null);

        ArgumentCaptor<RecipeIngredient> captor = ArgumentCaptor.forClass(RecipeIngredient.class);
        verify(ingredientRepository, times(2)).save(captor.capture());

        List<RecipeIngredient> 저장재료 = captor.getAllValues();
        assertEquals(66.666, 저장재료.get(0).getRatio(), 0.01);
        assertEquals(33.333, 저장재료.get(1).getRatio(), 0.01);
    }

    @Test
    void saveIngredientsAndInstructions_기준합계가_0이면_비율은_0으로_저장한다() {
        RecipeService service = 서비스생성();
        Recipe recipe = 기본레시피();

        RecipeFormDto form = RecipeFormDto.builder()
                .ingredientGroup(List.of(
                        IngredientGroupDto.builder()
                                .name("반죽")
                                .sortOrder(1)
                                .ingredients(List.of(
                                        IngredientDto.builder().name("재료A").baseAmount(0).unit("g").main(true).build(),
                                        IngredientDto.builder().name("재료B").baseAmount(0).unit("g").main(false).build()
                                ))
                                .build()
                ))
                .instructionGroup(List.of())
                .build();

        service.saveIngredientsAndInstructions(recipe, form, null);

        ArgumentCaptor<RecipeIngredient> captor = ArgumentCaptor.forClass(RecipeIngredient.class);
        verify(ingredientRepository, times(2)).save(captor.capture());

        List<RecipeIngredient> 저장재료 = captor.getAllValues();
        assertEquals(0.0, 저장재료.get(0).getRatio(), 0.0);
        assertEquals(0.0, 저장재료.get(1).getRatio(), 0.0);
    }

    private Recipe 기본레시피() {
        return Recipe.builder()
                .id(99L)
                .title("비율 검증")
                .recipeIngredientGroup(new ArrayList<>())
                .recipeInstructionGroup(new ArrayList<>())
                .subRecipeRelations(new ArrayList<>())
                .recipeRevs(new ArrayList<>())
                .recipeIngs(new ArrayList<>())
                .build();
    }
}
