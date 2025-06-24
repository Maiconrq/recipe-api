package com.recime.recipe_api.controller;

import com.recime.recipe_api.dto.RecipeCreateDTO;
import com.recime.recipe_api.dto.RecipeResponseDTO;
import com.recime.recipe_api.dto.RecipeUpdateDTO;
import com.recime.recipe_api.service.RecipeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @PostMapping
    public ResponseEntity<RecipeResponseDTO> createRecipe(@Valid @RequestBody RecipeCreateDTO dto) {
        RecipeResponseDTO createdRecipe = recipeService.createRecipe(dto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdRecipe.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdRecipe);
    }

    @GetMapping
    public ResponseEntity<List<RecipeResponseDTO>> getRecipes(
            @RequestParam(required = false) Boolean vegetarian,
            @RequestParam(required = false) Integer servings) {

        List<RecipeResponseDTO> recipes = recipeService.getRecipesByFilters(vegetarian, servings);
        return ResponseEntity.ok(recipes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeResponseDTO> getRecipeById(@PathVariable Long id) {
        RecipeResponseDTO dto = recipeService.getRecipeById(id);
        return ResponseEntity.ok(dto);
    }


    @PutMapping("/{id}")
    public ResponseEntity<RecipeResponseDTO> updateRecipe(@PathVariable Long id,
                                                          @Valid @RequestBody RecipeUpdateDTO dto) {
        RecipeResponseDTO updatedRecipe = recipeService.updateRecipe(id, dto);
        return ResponseEntity.ok(updatedRecipe);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable Long id) {
        recipeService.deleteRecipe(id);
        return ResponseEntity.noContent().build();
    }
}
