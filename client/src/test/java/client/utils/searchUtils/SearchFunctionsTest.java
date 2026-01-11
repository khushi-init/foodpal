package client.utils.searchUtils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commons.Ingredient;
import commons.Recipe;
import commons.RecipeIngredient;

public class SearchFunctionsTest {
    AtomicProposition aProp1;
    AtomicProposition aProp2;
    CombinedProposition cProp1;
    CombinedProposition cProp2;
    Recipe recipe;

    @BeforeEach
    public void setup(){
        recipe = new Recipe("oddlyspecificname", null, List.of("step1", "step2", "step3"));
        Ingredient ingredient1 = new Ingredient("testname", null);
        RecipeIngredient ri1 = new RecipeIngredient(recipe, ingredient1, 0.0);
        Ingredient ingredient2 = new Ingredient("testname2", null);
        RecipeIngredient ri2 = new RecipeIngredient(recipe, ingredient2, 1.0);
        recipe.setIngredients(List.of(ri1, ri2));
        aProp1 = new AtomicProposition(SearchFunctions.HASNAME, new ArrayList<String>(List.of("oddlyspecific")));
        aProp2 = new AtomicProposition(SearchFunctions.HASING, new ArrayList<String>(List.of("testname2")));
        cProp1 = new CombinedProposition(SearchFunctions.AND, new ArrayList<Proposition>(List.of(aProp1, aProp2)));

        cProp2 = new CombinedProposition(SearchFunctions.AND, new ArrayList<Proposition>(List.of(aProp1, cProp1)));
    }

    @Test
    public void mapToPropositionListTest(){
        AtomicProposition prop = new AtomicProposition(null, null);
        ArrayList<Object> obj = new ArrayList<>(List.of((Object) prop));
        ArrayList<Proposition> props = new ArrayList<Proposition>(List.of(prop));
        assertEquals(props, SearchFunctions.mapToPropositionList(obj));
    }

    @Test
    public void evaluateANDTest(){
        assertTrue(cProp1.evaluate(recipe));
        assertTrue(cProp2.evaluate(recipe));
        aProp1.getArguments().set(0, "othername");
        assertFalse(cProp1.evaluate(recipe));
        assertFalse(cProp2.evaluate(recipe));
    }

    @Test
    public void evaluateORTest(){
        cProp1.setFunction(SearchFunctions.OR);
        cProp2.setFunction(SearchFunctions.OR);
        assertTrue(cProp1.evaluate(recipe));
        assertTrue(cProp2.evaluate(recipe));
        aProp1.getArguments().set(0, "othername");
        assertTrue(cProp1.evaluate(recipe));
        assertTrue(cProp2.evaluate(recipe));
        aProp2.getArguments().set(0, "othername");
        assertFalse(cProp1.evaluate(recipe));
        assertFalse(cProp2.evaluate(recipe));
    }

    @Test
    public void evaluateMAXINGTest(){
        aProp1.setFunction(SearchFunctions.MAXING);
        aProp1.setArguments(new ArrayList<String>(List.of("testname2", "0.0")));
        assertFalse(aProp1.evaluate(recipe));
        aProp1.getArguments().set(1, "1.0");
        assertTrue(aProp1.evaluate(recipe));
        aProp1.getArguments().set(1, "2.0");
        assertTrue(aProp1.evaluate(recipe));
    }

    @Test
    public void evaluateMININGTest(){
        aProp1.setFunction(SearchFunctions.MINING);
        aProp1.setArguments(new ArrayList<String>(List.of("testname2", "0.0")));
        assertTrue(aProp1.evaluate(recipe));
        aProp1.getArguments().set(1, "1.0");
        assertTrue(aProp1.evaluate(recipe));
        aProp1.getArguments().set(1, "2.0");
        assertFalse(aProp1.evaluate(recipe));
    }

    @Test
    public void evaluateMAXSTEPSTest(){
        aProp1.setFunction(SearchFunctions.MAXSTEPS);
        aProp1.getArguments().set(0, "4");
        assertTrue(aProp1.evaluate(recipe));
        aProp1.getArguments().set(0, "3");
        assertTrue(aProp1.evaluate(recipe));
        aProp1.getArguments().set(0, "2");
        assertFalse(aProp1.evaluate(recipe));
    }

    @Test
    public void evaluateMINSTEPSTest(){
        aProp1.setFunction(SearchFunctions.MINSTEPS);
        aProp1.getArguments().set(0, "4");
        assertFalse(aProp1.evaluate(recipe));
        aProp1.getArguments().set(0, "3");
        assertTrue(aProp1.evaluate(recipe));
        aProp1.getArguments().set(0, "2");
        assertTrue(aProp1.evaluate(recipe));
    }

    @Test
    public void evaluateNOTNAMETest(){
        aProp1.setFunction(SearchFunctions.NOTNAME);
        aProp2.setFunction(SearchFunctions.NOTNAME);
        assertTrue(aProp2.evaluate(recipe));
        assertFalse(aProp1.evaluate(recipe));
    }

    @Test
    public void evaluateNOTINGTest(){
        aProp1.setFunction(SearchFunctions.NOTING);
        aProp2.setFunction(SearchFunctions.NOTING);
        assertFalse(aProp2.evaluate(recipe));
        assertTrue(aProp1.evaluate(recipe));
    }

    @Test
    public void evaluateNOTSTEPTest(){
        aProp1.setFunction(SearchFunctions.NOTSTEP);
        aProp1.getArguments().set(0, "step1");
        assertFalse(aProp1.evaluate(recipe));
        aProp1.getArguments().set(0, "step123");
        assertTrue(aProp1.evaluate(recipe));
    }

}
