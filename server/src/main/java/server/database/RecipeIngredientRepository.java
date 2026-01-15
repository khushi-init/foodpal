package server.database;

import commons.RecipeIngredient;
import commons.RecipeIngredientKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, RecipeIngredientKey> {

    /**
     * Deletes all RecipeIngredient associations that reference the given ingredient
     * @param ingredientId The ID of the ingredient whose associations should be deleted
     */
    @Modifying
    @Transactional
    // This custom HQL query deletes all links associated with the given ingredient ID
    @Query("DELETE FROM RecipeIngredient ri WHERE ri.ingredient.id = :ingredientId")
    void deleteByIngredientId(@Param("ingredientId") long ingredientId);

    @Query("SELECT ri.id FROM RecipeIngredient ri WHERE ri.ingredient.id = :ingredientId")
    RecipeIngredientKey getIdFromIngredientId(@Param("ingredientId") long ingredientId);

    @Override
    Optional<RecipeIngredient> findById(RecipeIngredientKey recipeIngredientKey);

    @Query("SELECT COUNT(DISTINCT ri.recipe.id) FROM RecipeIngredient ri WHERE ri.ingredient.id = :ingredientId")
    int getRecipeUsageNumber(@Param("ingredientId") long ingredientId);
}
