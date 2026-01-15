package server.api;

import commons.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import server.database.IngredientRepository;
import server.database.RecipeRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class IngredientRepositoryTest {

    @Autowired
    private RecipeRepository recipeRepo;

    @Autowired
    private IngredientRepository ingredientRepo;

    private final int dummyNutriValue = 0;
    private final double dummyQuantity = 5.0;

    @Test
    void testSavingRecipesSavesIngredients() {
        Ingredient carrot = new Ingredient("Carrot", new NutritionalValue(dummyNutriValue, dummyNutriValue, dummyNutriValue));
        RecipeIngredient ri = new RecipeIngredient(null, carrot, dummyQuantity,
                new RecipeIngredientUnit(UnitType.FORMAL, FormalUnit.GRAM.getDisplayName(), null));
        Recipe dummy = new Recipe("Carrot Stew", List.of(ri), new ArrayList<>());
        ri.setRecipe(dummy);

        Recipe saved = recipeRepo.save(dummy);
        assertTrue(ingredientRepo.findByName("Carrot").isPresent());
        assertNotNull(ingredientRepo.findByName("Carrot").get().getId());
    }

    @Test
    void testDeletingIngredientGetsDeletedFromRecipe() {

    }
}