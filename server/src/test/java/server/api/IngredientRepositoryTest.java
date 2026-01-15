package server.api;

import commons.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import server.database.IngredientRepository;
import server.database.RecipeRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    private Recipe dummyRecipe;
    private Ingredient dummyIngredient;
    private RecipeIngredient dummyRecipeIngredient;

    @BeforeEach
    void createDummies() {
        dummyIngredient = new Ingredient("Carrot", new NutritionalValue(dummyNutriValue, dummyNutriValue, dummyNutriValue));
        dummyRecipeIngredient = new RecipeIngredient(null, dummyIngredient, dummyQuantity,
                new RecipeIngredientUnit(UnitType.FORMAL, FormalUnit.GRAM.getDisplayName(), null));
        dummyRecipe = new Recipe("Carrot Stew", List.of(dummyRecipeIngredient), new ArrayList<>());
        dummyRecipeIngredient.setRecipe(dummyRecipe);
    }

    @Test
    void testSavingRecipesSavesIngredients() {
        Recipe saved = recipeRepo.save(dummyRecipe);
        assertTrue(ingredientRepo.findByName("Carrot").isPresent());
        assertNotNull(ingredientRepo.findByName("Carrot").get().getId());
    }

    @Test
    void testDeletingRecipePreservesIngredient() {
        Recipe saved = recipeRepo.save(dummyRecipe);
        recipeRepo.deleteById(dummyRecipe.getId());
        Optional<Ingredient> preservedIngredient = ingredientRepo.findByName("Carrot");
        assertTrue(preservedIngredient.isPresent());
    }
}