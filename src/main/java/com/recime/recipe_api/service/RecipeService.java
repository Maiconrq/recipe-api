package com.recime.recipe_api.service;

import com.recime.recipe_api.dto.RecipeCreateDTO;
import com.recime.recipe_api.dto.RecipeResponseDTO;
import com.recime.recipe_api.dto.RecipeUpdateDTO;
import com.recime.recipe_api.exception.RecipeNotFoundException;
import com.recime.recipe_api.model.Recipe;
import com.recime.recipe_api.repository.RecipeRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.recime.recipe_api.specification.RecipeSpecifications.hasVegetarian;
import static com.recime.recipe_api.specification.RecipeSpecifications.hasServings;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;

    public RecipeService(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    public RecipeResponseDTO createRecipe(RecipeCreateDTO dto) {
        Recipe recipe = Recipe.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .ingredients(dto.getIngredients())
                .instructions(dto.getInstructions())
                .vegetarian(dto.isVegetarian())
                .servings(dto.getServings())
                .build();

        Recipe savedRecipe = recipeRepository.save(recipe);

        return mapToResponseDTO(savedRecipe);
    }

    public List<RecipeResponseDTO> getRecipesByFilters(Boolean vegetarian, Integer servings) {
        Specification<Recipe> spec = null;

        if (vegetarian != null) {
            spec = hasVegetarian(vegetarian);
        }

        if (servings != null) {
            spec = (spec == null) ? hasServings(servings) : spec.and(hasServings(servings));
        }

        return recipeRepository.findAll(spec).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public RecipeResponseDTO getRecipeById(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RecipeNotFoundException(id));

        return mapToResponseDTO(recipe);
    }

    public RecipeResponseDTO updateRecipe(Long id, RecipeUpdateDTO dto) {
        Recipe existingRecipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RecipeNotFoundException(id));

        existingRecipe.setTitle(dto.getTitle());
        existingRecipe.setDescription(dto.getDescription());
        existingRecipe.setIngredients(dto.getIngredients());
        existingRecipe.setInstructions(dto.getInstructions());
        existingRecipe.setVegetarian(dto.isVegetarian());
        existingRecipe.setServings(dto.getServings());

        Recipe updatedRecipe = recipeRepository.save(existingRecipe);

        return mapToResponseDTO(updatedRecipe);
    }

    public void deleteRecipe(Long id) {
        if (!recipeRepository.existsById(id)) {
            throw new EmptyResultDataAccessException("Recipe not found", 1);
        }
        recipeRepository.deleteById(id);
    }


    private RecipeResponseDTO mapToResponseDTO(Recipe recipe) {
        return RecipeResponseDTO.builder()
                .id(recipe.getId())
                .title(recipe.getTitle())
                .description(recipe.getDescription())
                .ingredients(recipe.getIngredients())
                .instructions(recipe.getInstructions())
                .vegetarian(recipe.isVegetarian())
                .servings(recipe.getServings())
                .build();
    }
}
