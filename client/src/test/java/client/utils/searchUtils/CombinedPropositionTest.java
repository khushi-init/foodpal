package client.utils.searchUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commons.Ingredient;
import commons.Recipe;
import commons.RecipeIngredient;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

public class CombinedPropositionTest {
    CombinedProposition prop;
    AtomicProposition aProp;
    Recipe recipe;
    @BeforeEach
    public void setup(){
        prop = new CombinedProposition(SearchFunctions.AND, null);
        aProp = new AtomicProposition(SearchFunctions.HASNAME, new ArrayList<String>(List.of("name")));
        recipe = new Recipe("name", null, List.of("step1", "step2", "step3"));
        Ingredient ingredient1 = new Ingredient("testname", null);
        RecipeIngredient ri1 = new RecipeIngredient(recipe, ingredient1, 0.0);
        Ingredient ingredient2 = new Ingredient("testname2", null);
        RecipeIngredient ri2 = new RecipeIngredient(recipe, ingredient2, 1.0);
        recipe.setIngredients(List.of(ri1, ri2));
    }

    @Test
    public void noArgumentsTest(){
        prop.setArguments(new ArrayList<Proposition>());
        assertThrows(IllegalArgumentException.class, () -> prop.evaluate(recipe));
        assertFalse(prop.verifyArguments());
    }

    @Test
    public void nullRecipeTest(){
        prop.setArguments(new ArrayList<Proposition>(List.of(aProp)));
        assertThrows(IllegalArgumentException.class, () -> prop.evaluate(null));
    }

    @Test
    public void oneArgumentTest(){
        prop.setArguments(new ArrayList<>(List.of(aProp)));
        assertDoesNotThrow(() -> prop.verifyArguments());
        assertTrue(prop.verifyArguments());
    }

    @Test
    public void twoArgumentsTest(){
        prop.setArguments(new ArrayList<>(List.of(aProp, aProp)));
        assertDoesNotThrow(() -> prop.verifyArguments());
        assertTrue(prop.verifyArguments());
    }

    @Test
    public void equalsTest(){
        CombinedProposition prop2 = new CombinedProposition(prop.getFunction(), prop.getArguments());
        assertEquals(prop2, prop);
        prop2.setFunction(SearchFunctions.OR);
        assertNotEquals(prop2, prop);
    }

    @Test
    public void hashCodeTest(){
        CombinedProposition prop2 = new CombinedProposition(prop.getFunction(), prop.getArguments());
        assertEquals(prop2.hashCode(), prop.hashCode());
        prop2.setFunction(SearchFunctions.OR);
        assertNotEquals(prop2.hashCode(), prop.hashCode());
    }
}
