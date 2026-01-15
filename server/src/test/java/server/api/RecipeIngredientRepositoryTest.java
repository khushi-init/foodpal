package server.api;

import commons.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import server.database.IngredientRepository;
import server.database.RecipeIngredientRepository;
import server.database.RecipeRepository;
import server.service.IngredientService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(IngredientService.class)
public class RecipeIngredientRepositoryTest {

    @Autowired
    RecipeIngredientRepository recipeIngredientRepository;

    @Autowired
    IngredientRepository ingredientRepository;

    @Autowired
    RecipeRepository recipeRepository;

    @Autowired
    IngredientService ingredientService;

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
    void testDeletingRecipeDeletesRecipeIngredient() {
        Recipe saved = recipeRepository.save(dummyRecipe);
        RecipeIngredientKey savedRiKey = recipeIngredientRepository.getIdFromIngredientId(dummyIngredient.getId());
        assertNotNull(savedRiKey);
        recipeRepository.deleteById(saved.getId());
        RecipeIngredientKey deletedRiKey = recipeIngredientRepository.getIdFromIngredientId(dummyIngredient.getId());
        assertNull(deletedRiKey);
    }

    // Weird that for ingredient deletions we rely on the service
    @Test
    void testDeletingIngredientDeletesRecipeIngredient() {
        Recipe saved = recipeRepository.save(dummyRecipe);
        ingredientService.deleteIngredient(dummyIngredient.getId());
        RecipeIngredientKey deletedRiKey = recipeIngredientRepository.getIdFromIngredientId(dummyIngredient.getId());
        assertNull(deletedRiKey);
    }
}
