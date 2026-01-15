package server.api;

import commons.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import server.database.IngredientRepository;
import server.database.RecipeIngredientRepository;
import server.database.RecipeRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class RecipeIngredientRepositoryTest {

    @Autowired
    RecipeIngredientRepository recipeIngredientRepository;

    @Autowired
    IngredientRepository ingredientRepository;

    @Autowired
    RecipeRepository recipeRepository;

    private final int dummyNutriValue = 0;
    private final double dummyQuantity = 5.0;

    @Test
    void testDeletingRecipeDeletesRecipeIngredient() {
        Ingredient carrot = new Ingredient("Carrot", new NutritionalValue(dummyNutriValue, dummyNutriValue, dummyNutriValue));
        RecipeIngredient ri = new RecipeIngredient(null, carrot, dummyQuantity,
                new RecipeIngredientUnit(UnitType.FORMAL, FormalUnit.GRAM.getDisplayName(), null));
        Recipe dummy = new Recipe("Carrot Stew", List.of(ri), new ArrayList<>());
        ri.setRecipe(dummy);

        Recipe saved = recipeRepository.save(dummy);
        RecipeIngredientKey savedRiKey = recipeIngredientRepository.getIdFromIngredientId(carrot.getId());
        assertNotNull(savedRiKey);
        recipeRepository.deleteById(saved.getId());
        RecipeIngredientKey deletedRiKey = recipeIngredientRepository.getIdFromIngredientId(carrot.getId());
        assertNull(deletedRiKey);
    }
}
