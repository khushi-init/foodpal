package client.utils.searchUtils;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commons.Ingredient;
import commons.Recipe;
import commons.RecipeIngredient;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The purpose of this class is to check if an AtomicProposition throws the right exceptions, not to check if evaluating it on a recipe results in the right boolean value.
 * The latter is checked in the SearchFunctionsTest class.
 */
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
        assertFalse(prop.verifyArguments(SearchFunctions.MINING, new ArrayList<String>(List.of())));
    }

    @Test
    public void tooManyArgumentsTest(){
        prop.setArguments(new ArrayList<String>(List.of("1", "2", "3")));
        assertThrows(IllegalArgumentException.class, () -> prop.evaluate(recipe));
        assertFalse(prop.verifyArguments(SearchFunctions.HASING, new ArrayList<String>(List.of("1", "2", "3"))));
    }

    @Test
    public void wrongArgumentTypeTest(){
        prop.setFunction(SearchFunctions.MAXING);
        prop.setArguments(new ArrayList<String>(List.of("name", "this should be convertible to a double")));
        assertThrows(IllegalArgumentException.class, () -> prop.evaluate(recipe));
        assertFalse(prop.verifyArguments(SearchFunctions.HASING, new ArrayList<String>(List.of("name", "not a double"))));
    }

    @Test
    public void noExceptionTest(){
        prop.setArguments(new ArrayList<String>(List.of("test")));
        assertDoesNotThrow( () -> prop.evaluate(recipe));
        assertTrue(prop.verifyArguments(SearchFunctions.HAS, prop.getArguments()));
        prop.setArguments(new ArrayList<String>(List.of("test", "0.5")));
        prop.setFunction(SearchFunctions.MINING);
        assertDoesNotThrow( () -> prop.evaluate(recipe));
        assertTrue(prop.verifyArguments(SearchFunctions.MINING, prop.getArguments()));
    }

    
}
