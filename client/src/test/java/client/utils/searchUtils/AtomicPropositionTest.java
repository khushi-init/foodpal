package client.utils.searchUtils;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commons.Ingredient;
import commons.Recipe;
import commons.RecipeIngredient;

import static org.junit.jupiter.api.Assertions.*;

public class AtomicPropositionTest {
    AtomicProposition prop;
    Recipe recipe;
    @BeforeEach
    public void setup(){
        prop = new AtomicProposition(SearchFunctions.HASNAME, new ArrayList<String>(List.of()));
        recipe = new Recipe("test", null, List.of("step1", "step2"));
        Ingredient ingredient1 = new Ingredient("testname", null);
        RecipeIngredient ri1 = new RecipeIngredient(recipe, ingredient1, 0.0);
        Ingredient ingredient2 = new Ingredient("testname2", null);
        RecipeIngredient ri2 = new RecipeIngredient(recipe, ingredient2, 1.0);
        recipe.setIngredients(List.of(ri1, ri2));
    }

    @Test
    public void nullRecipeTest(){
        prop.setArguments(new ArrayList<String>(List.of("")));
        assertThrows(IllegalArgumentException.class, () -> prop.evaluate(null));
    }

    @Test
    public void noArgumentsTest(){
        assertThrows(IllegalArgumentException.class, () -> prop.evaluate(recipe));
    }

    @Test
    public void tooManyArgumentsTest(){
        prop.setArguments(new ArrayList<String>(List.of("1", "2", "3")));
        assertThrows(IllegalArgumentException.class, () -> prop.evaluate(recipe));
    }

    @Test
    public void wrongArgumentTypeTest(){
        prop.setFunction(SearchFunctions.MAXING);
        prop.setArguments(new ArrayList<String>(List.of("name", "this should be convertable to a double")));
        assertThrows(IllegalArgumentException.class, () -> prop.evaluate(recipe));
    }

    @Test
    public void noExceptionTest(){
        prop.setArguments(new ArrayList<String>(List.of("test")));
        assertDoesNotThrow( () -> prop.evaluate(recipe));
        prop.setArguments(new ArrayList<String>(List.of("test", "0.5")));
        prop.setFunction(SearchFunctions.MINING);
        assertDoesNotThrow( () -> prop.evaluate(recipe));
    }

    
}
