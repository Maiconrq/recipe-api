package com.recime.recipe_api.specification;
import com.recime.recipe_api.model.Recipe;
import org.springframework.data.jpa.domain.Specification;

public class RecipeSpecifications {

    public static Specification<Recipe> hasVegetarian(Boolean vegetarian) {
        return (root, query, criteriaBuilder) -> {
            if (vegetarian == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("vegetarian"), vegetarian);
        };
    }

    public static Specification<Recipe> hasServings(Integer servings) {
        return (root, query, criteriaBuilder) -> {
            if (servings == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("servings"), servings);
        };
    }

}
