package client.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import client.utils.searchUtils.*;
import commons.Ingredient;
import commons.Recipe;
import commons.RecipeIngredient;

public class SearchCtrlTest {
    Recipe recipe;
    SearchCtrl searchCtrl;
    @BeforeEach
    public void setup(){
        searchCtrl = new SearchCtrl();
        recipe = new Recipe("name", null, List.of("step1", "step2", "step3"));
        Ingredient ingredient1 = new Ingredient("testname", null);
        RecipeIngredient ri1 = new RecipeIngredient(recipe, ingredient1, 0.0, null);
        Ingredient ingredient2 = new Ingredient("testname2", null);
        RecipeIngredient ri2 = new RecipeIngredient(recipe, ingredient2, 1.0, null);
        recipe.setIngredients(List.of(ri1, ri2));
    }

    @Test
    public void performSimpleQueryTest(){
        List<Recipe> recipes = List.of(recipe);
        assertEquals(recipes, searchCtrl.performSimpleQuery("step1", recipes));
        assertEquals(List.of(), searchCtrl.performSimpleQuery("step1123", recipes));
    }

    @Test
    public void performComplexQueryTest(){
        List<Recipe> recipes = List.of(recipe);
        assertEquals(recipes, searchCtrl.performComplexQuery(
                "/AND(/HASING(testname2),/HASSTEP(step1))", 
                recipes)
        );
        assertEquals(List.of(), searchCtrl.performComplexQuery(
                "/MAXSTEPS(1)", 
                recipes)
        );
    }

    @Test
    public void queryTest(){
        List<Recipe> recipes = List.of(recipe);
        assertEquals(recipes, searchCtrl.query("*/MAXSTEPS(1000)", recipes));
        assertEquals(recipes, searchCtrl.query("testname2", recipes));
        assertEquals(List.of(), searchCtrl.query("*/MINSTEPS(1000)", recipes));
        assertEquals(List.of(), searchCtrl.query("asdfdfsksdf", recipes));
    }

    @Test
    public void performComplexQueryTest2(){
        List<Recipe> recipes = List.of(recipe);
        AtomicProposition prop = new AtomicProposition(SearchFunctions.HAS, new ArrayList<String>(List.of("name")));
        assertEquals(recipes, searchCtrl.performComplexQuery(prop, recipes));
        prop.getArguments().set(0, "asdflkjfds");
        assertEquals(List.of(), searchCtrl.performComplexQuery(prop, recipes));
    }
}
