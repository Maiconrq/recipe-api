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
import org.springframework.data.jpa.domain.Specification;

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
    void should_ReturnFilteredRecipes_When_FiltersApplied() {
        Recipe recipe = Recipe.builder().id(1L).title("Vegetarian").vegetarian(true).servings(2).build();

        when(recipeRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(recipe));

        List<RecipeResponseDTO> result = recipeService.getRecipesByFilters(true, 2);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Vegetarian");
    }
}
