package com.recime.recipe_api.service;

import com.recime.recipe_api.dto.RecipeCreateDTO;
import com.recime.recipe_api.dto.RecipeResponseDTO;
import com.recime.recipe_api.dto.RecipeUpdateDTO;
import com.recime.recipe_api.exception.RecipeNotFoundException;
import com.recime.recipe_api.model.Recipe;
import com.recime.recipe_api.repository.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @InjectMocks
    private RecipeService recipeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void should_CreateRecipe_When_ValidInput() {
        RecipeCreateDTO dto = RecipeCreateDTO.builder()
                .title("Test Recipe")
                .description("Test Description")
                .ingredients(List.of("Ingredient 1", "Ingredient 2"))
                .instructions("Test Instructions")
                .vegetarian(true)
                .servings(2)
                .build();

        Recipe savedRecipe = Recipe.builder()
                .id(1L)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .ingredients(dto.getIngredients())
                .instructions(dto.getInstructions())
                .vegetarian(dto.isVegetarian())
                .servings(dto.getServings())
                .build();

        when(recipeRepository.save(any(Recipe.class))).thenReturn(savedRecipe);

        RecipeResponseDTO result = recipeService.createRecipe(dto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test Recipe");
        verify(recipeRepository, times(1)).save(any(Recipe.class));
    }

    @Test
    void should_ThrowException_When_TitleIsNull() {
        RecipeCreateDTO dto = RecipeCreateDTO.builder()
                .title(null)
                .description("Description")
                .ingredients(List.of("Ingredient 1"))
                .instructions("Instructions")
                .vegetarian(true)
                .servings(2)
                .build();

        assertThatThrownBy(() -> recipeService.createRecipe(dto))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void should_ThrowException_When_RepositoryFailsOnSave() {
        RecipeCreateDTO dto = RecipeCreateDTO.builder()
                .title("Test Recipe")
                .description("Description")
                .ingredients(List.of("Ingredient"))
                .instructions("Instructions")
                .vegetarian(true)
                .servings(2)
                .build();

        when(recipeRepository.save(any(Recipe.class))).thenThrow(new RuntimeException("Database error"));

        assertThatThrownBy(() -> recipeService.createRecipe(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database error");

        verify(recipeRepository, times(1)).save(any(Recipe.class));
    }
    @Test
    void should_CreateMultipleRecipes_When_ValidInputList() {
        RecipeCreateDTO dto1 = RecipeCreateDTO.builder()
                .title("Recipe 1")
                .description("Desc 1")
                .ingredients(List.of("Ingredient A"))
                .instructions("Instr 1")
                .vegetarian(true)
                .servings(2)
                .build();

        RecipeCreateDTO dto2 = RecipeCreateDTO.builder()
                .title("Recipe 2")
                .description("Desc 2")
                .ingredients(List.of("Ingredient B"))
                .instructions("Instr 2")
                .vegetarian(false)
                .servings(4)
                .build();

        Recipe recipe1 = Recipe.builder().id(1L).title(dto1.getTitle()).description(dto1.getDescription())
                .ingredients(dto1.getIngredients()).instructions(dto1.getInstructions())
                .vegetarian(dto1.isVegetarian()).servings(dto1.getServings()).build();

        Recipe recipe2 = Recipe.builder().id(2L).title(dto2.getTitle()).description(dto2.getDescription())
                .ingredients(dto2.getIngredients()).instructions(dto2.getInstructions())
                .vegetarian(dto2.isVegetarian()).servings(dto2.getServings()).build();

        when(recipeRepository.saveAll(anyList())).thenReturn(List.of(recipe1, recipe2));

        List<RecipeResponseDTO> result = recipeService.createMoreThanOneRecipe(List.of(dto1, dto2));

        assertThat(result).hasSize(2);
        assertThat(result).extracting(RecipeResponseDTO::getTitle)
                .containsExactlyInAnyOrder("Recipe 1", "Recipe 2");
        verify(recipeRepository).saveAll(anyList());
    }

    @Test
    void should_ThrowException_When_BulkInsertFails() {
        RecipeCreateDTO dto = RecipeCreateDTO.builder()
                .title("Recipe 1")
                .description("Desc 1")
                .ingredients(List.of("Ingredient A"))
                .instructions("Instr 1")
                .vegetarian(true)
                .servings(2)
                .build();

        when(recipeRepository.saveAll(anyList()))
                .thenThrow(new RuntimeException("Bulk insert error"));

        assertThatThrownBy(() -> recipeService.createMoreThanOneRecipe(List.of(dto)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Bulk insert error");

        verify(recipeRepository).saveAll(anyList());
    }

    @Test
    void should_ReturnRecipe_When_IdExists() {
        Recipe recipe = Recipe.builder().id(1L).title("Test").build();
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));

        RecipeResponseDTO result = recipeService.getRecipeById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test");
    }

    @Test
    void should_ThrowException_When_IdNotFound() {
        when(recipeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recipeService.getRecipeById(1L))
                .isInstanceOf(RecipeNotFoundException.class)
                .hasMessageContaining("1");
    }

    @Test
    void should_UpdateRecipe_When_IdExists() {
        Recipe existingRecipe = Recipe.builder().id(1L).title("Old").build();
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(existingRecipe));

        RecipeUpdateDTO dto = RecipeUpdateDTO.builder()
                .title("Updated")
                .description("Updated Description")
                .ingredients(List.of("New Ingredient"))
                .instructions("New Instructions")
                .vegetarian(false)
                .servings(3)
                .build();

        when(recipeRepository.save(any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RecipeResponseDTO result = recipeService.updateRecipe(1L, dto);

        assertThat(result.getTitle()).isEqualTo("Updated");
        verify(recipeRepository).save(any(Recipe.class));
    }

    @Test
    void should_ThrowException_When_SaveFails() {
        Recipe existingRecipe = Recipe.builder().id(1L).title("Old").build();
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(existingRecipe));

        RecipeUpdateDTO dto = RecipeUpdateDTO.builder()
                .title("Updated")
                .description("Updated Description")
                .ingredients(List.of("New Ingredient"))
                .instructions("New Instructions")
                .vegetarian(false)
                .servings(3)
                .build();

        when(recipeRepository.save(any(Recipe.class)))
                .thenThrow(new RuntimeException("Database error"));

        assertThatThrownBy(() -> recipeService.updateRecipe(1L, dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database error");

        verify(recipeRepository).findById(1L);
        verify(recipeRepository).save(any(Recipe.class));
    }

    @Test
    void should_UpdateRecipe_When_DTOFieldsAreNull() {
        Recipe existingRecipe = Recipe.builder()
                .id(1L)
                .title("Old Title")
                .description("Old Description")
                .ingredients(List.of("Old Ingredient"))
                .instructions("Old Instructions")
                .vegetarian(true)
                .servings(5)
                .build();

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(existingRecipe));

        RecipeUpdateDTO dto = RecipeUpdateDTO.builder()
                .title(null)
                .description(null)
                .ingredients(null)
                .instructions(null)
                .vegetarian(false)
                .servings(null)
                .build();

        when(recipeRepository.save(any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RecipeResponseDTO result = recipeService.updateRecipe(1L, dto);

        assertThat(result.getTitle()).isNull();
        assertThat(result.getDescription()).isNull();
        assertThat(result.getIngredients()).isNull();
        assertThat(result.getInstructions()).isNull();
        assertThat(result.isVegetarian()).isFalse();
        assertThat(result.getServings()).isNull();

        verify(recipeRepository).findById(1L);
        verify(recipeRepository).save(any(Recipe.class));
    }

    @Test
    void should_DeleteRecipe_When_IdExists() {
        when(recipeRepository.existsById(1L)).thenReturn(true);

        recipeService.deleteRecipe(1L);

        verify(recipeRepository, times(1)).deleteById(1L);
    }

    @Test
    void should_ThrowException_When_DeletingNonExistentRecipe() {
        when(recipeRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> recipeService.deleteRecipe(1L))
                .isInstanceOf(EmptyResultDataAccessException.class)
                .hasMessageContaining("Recipe not found");
    }

    @Test
    void should_PropagateException_When_DeleteFailsInRepository() {
        when(recipeRepository.existsById(1L)).thenReturn(true);
        doThrow(new RuntimeException("Database delete error")).when(recipeRepository).deleteById(1L);

        assertThatThrownBy(() -> recipeService.deleteRecipe(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database delete error");

        verify(recipeRepository).existsById(1L);
        verify(recipeRepository).deleteById(1L);
    }

    @Test
    void should_ReturnFilteredRecipes_When_FiltersApplied() {
        Recipe recipe = Recipe.builder().id(1L).title("Vegetarian").vegetarian(true).servings(2).build();

        when(recipeRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(recipe)));

        Page<RecipeResponseDTO> result = recipeService.getRecipesByFilters(true, 2, null, null, null, PageRequest.of(0, 10));

        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Vegetarian");

    }

    @Test
    void should_ReturnAllRecipes_When_NoFiltersProvided() {
        Recipe recipe1 = Recipe.builder().id(1L).title("Recipe 1").build();
        Recipe recipe2 = Recipe.builder().id(2L).title("Recipe 2").build();

        when(recipeRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(recipe1, recipe2)));

        Page<RecipeResponseDTO> result = recipeService.getRecipesByFilters(null, null, null, null, null, PageRequest.of(0, 10));

        assertThat(result).hasSize(2);
        assertThat(result).extracting(RecipeResponseDTO::getId).containsExactlyInAnyOrder(1L, 2L);
        verify(recipeRepository).findAll(nullable(Specification.class), any(Pageable.class));
    }

    @Test
    void should_ReturnRecipes_When_OnlyVegetarianFilterProvided() {
        Recipe recipe = Recipe.builder().id(1L).title("Veg Recipe").vegetarian(true).build();

        when(recipeRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(recipe)));

        Page<RecipeResponseDTO> result = recipeService.getRecipesByFilters(true, null, null, null, null, PageRequest.of(0, 10));

        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).isVegetarian()).isTrue();
        verify(recipeRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void should_ReturnRecipes_When_OnlyServingsFilterProvided() {
        Recipe recipe = Recipe.builder().id(1L).title("Recipe with 2 servings").servings(2).build();

        when(recipeRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(recipe)));

        Page<RecipeResponseDTO> result = recipeService.getRecipesByFilters(null, 2, null, null, null, PageRequest.of(0, 10));

        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).getServings()).isEqualTo(2);
        verify(recipeRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void should_ReturnEmptyList_When_NoRecipesMatchFilters() {
        Pageable pageable = PageRequest.of(0, 10);

        when(recipeRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        Page<RecipeResponseDTO> result = recipeService.getRecipesByFilters(
                true, 2, null, null, null, pageable
        );

        assertThat(result.getContent()).isEmpty();
        verify(recipeRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void should_ReturnRecipes_When_IncludeIngredientsFilterProvided() {
        Recipe recipe = Recipe.builder()
                .id(1L)
                .title("Tomato Pasta")
                .ingredients(List.of("Tomato", "Pasta", "Salt"))
                .build();

        when(recipeRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(recipe)));

        Page<RecipeResponseDTO> result = recipeService.getRecipesByFilters(null, null, List.of("Tomato"), null, null, PageRequest.of(0, 10));

        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Tomato Pasta");
    }

    @Test
    void should_ExcludeRecipes_When_ExcludeIngredientsFilterProvided() {
        when(recipeRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        Page<RecipeResponseDTO> result = recipeService.getRecipesByFilters(null, null, null, List.of("Rice"), null, PageRequest.of(0, 10));

        assertThat(result).isEmpty();
    }

    @Test
    void should_ReturnRecipes_When_InstructionContainsKeyword() {
        Recipe recipe = Recipe.builder()
                .id(1L)
                .title("Boil Pasta")
                .instructions("Boil water and add pasta.")
                .build();

        when(recipeRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(recipe)));

        Page<RecipeResponseDTO> result = recipeService.getRecipesByFilters(null, null, null, null, "boil", PageRequest.of(0, 10));

        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).getInstructions()).containsIgnoringCase("boil");
    }

    @Test
    void should_ReturnRecipes_When_AllFiltersCombined() {
        Recipe recipe = Recipe.builder()
                .id(1L)
                .title("Veggie Dish")
                .ingredients(List.of("Tomato", "Onion"))
                .instructions("Cook all ingredients slowly.")
                .vegetarian(true)
                .servings(3)
                .build();

        when(recipeRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(recipe)));


        Page<RecipeResponseDTO> result = recipeService.getRecipesByFilters(
                true,
                3,
                List.of("Tomato"),
                List.of("Meat"),
                "cook",
                PageRequest.of(0, 10)
        );

        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Veggie Dish");
    }

}
