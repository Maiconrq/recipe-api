package com.recime.recipe_api.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeUpdateDTO {

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotEmpty
    private List<String> ingredients;

    @NotBlank
    private String instructions;

    private boolean vegetarian;

    @Min(1)
    private Integer servings;
}
