package com.recime.recipe_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeResponseDTO {

    private Long id;
    private String title;
    private String description;
    private List<String> ingredients;
    private String instructions;
    private boolean vegetarian;
    private Integer servings;
}
