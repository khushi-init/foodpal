package commons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RecipeTest {
    private Recipe recipe;
    private Ingredient ingredient1;
    private Ingredient ingredient2;
    private RecipeIngredient recipeIngredient1;
    private RecipeIngredient recipeIngredient2;
    private double amount1 = 50.0;
    private double amount2 = 25.0;
    private List<RecipeIngredient> ingredientList;
    private List<String> preparationSteps;
    private NutritionalValue defaultNutritionalValue = new NutritionalValue(0, 0, 0);


    @BeforeEach
    public void setUp() {
        ingredient1 = new Ingredient("Sugar", defaultNutritionalValue);
        ingredient2 = new Ingredient("Butter", defaultNutritionalValue);

        recipeIngredient1 = new RecipeIngredient(recipe, ingredient1, amount1);
        recipeIngredient2 = new RecipeIngredient(recipe, ingredient2, amount2);

        ingredientList = new ArrayList<>(List.of(recipeIngredient1, recipeIngredient2));
        preparationSteps = new ArrayList<>(List.of(
                "Mix the sugar and butter together.",
                "Use the spell 'Pepernoteratus'.",
                "Put it in the oven for 10 minutes."
        ));

        recipe = new Recipe("Pepernoten", ingredientList,preparationSteps);
    }

    @Test
    public void toMarkdownTest() {
        String expected = """
                # Pepernoten
                
                ## Ingredients
                | Name | Amount |
                |------|--------|
                | Sugar | 50.0 |
                | Butter | 25.0 |
                
                ## Preparation Steps
                * Mix the sugar and butter together.
                * Use the spell 'Pepernoteratus'.
                * Put it in the oven for 10 minutes.
                """;

        String result = recipe.toMarkdown();

        assertEquals(expected, result);
    }

}
