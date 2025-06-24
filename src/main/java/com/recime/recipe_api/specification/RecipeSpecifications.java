package com.recime.recipe_api.specification;

import com.recime.recipe_api.model.Recipe;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class RecipeSpecifications {

    public static Specification<Recipe> hasVegetarian(Boolean vegetarian) {
        return (root, query, criteriaBuilder) -> {
            if (vegetarian == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("vegetarian"), vegetarian);
        };
    }

    public static Specification<Recipe> hasServings(Integer servings) {
        return (root, query, criteriaBuilder) -> {
            if (servings == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("servings"), servings);
        };
    }

    public static Specification<Recipe> hasIngredientsInclude(List<String> includeIngredients) {
        return (root, query, criteriaBuilder) -> {
            if (includeIngredients == null || includeIngredients.isEmpty())
                return criteriaBuilder.conjunction();

            List<Predicate> predicates = new ArrayList<>();
            includeIngredients.forEach(ingredient ->
                    predicates.add(criteriaBuilder.isMember(ingredient, root.<List<String>>get("ingredients")))
            );
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Recipe> hasIngredientsExclude(List<String> excludeIngredients) {
        return (root, query, criteriaBuilder) -> {
            if (excludeIngredients == null || excludeIngredients.isEmpty())
                return criteriaBuilder.conjunction();

            List<Predicate> predicates = new ArrayList<>();
            excludeIngredients.forEach(ingredient ->
                    predicates.add(criteriaBuilder.isNotMember(ingredient, root.<List<String>>get("ingredients")))
            );
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Recipe> hasInstructionContaining(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null || keyword.isBlank()) return criteriaBuilder.conjunction();
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("instructions")),
                    "%" + keyword.toLowerCase() + "%"
            );
        };
    }
}
