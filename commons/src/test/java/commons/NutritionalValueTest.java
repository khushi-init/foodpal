package commons;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NutritionalValueTest {

    @Test
    void NutriScoreCalculatesCorrectlyTest() {
        var nv = new NutritionalValue(1, 1, 1);
        assertEquals(17.0, nv.kcal100g());

        assertEquals(90.0, new NutritionalValue(10, 0, 0).kcal100g());
        assertEquals(40.0, new NutritionalValue(0, 10, 0).kcal100g());
        assertEquals(40.0, new NutritionalValue(0, 0, 10).kcal100g());
    }

    @Test
    void nutriScorePointsAllZeroTest() {
        var nv = new NutritionalValue(0, 0, 0);
        assertEquals(0.0, nv.nutriScorePoints());
    }

    @Test
    void nutriScorePointsInBoundsTest() {
        //values are in bounds
        var nv = new NutritionalValue(9, 4, 4);
        assertEquals(3.0, nv.nutriScorePoints());
    }

    @Test
    void nutriScorePointsAboveBoundsTest() {
        //values are above bounds
        var nv = new NutritionalValue(100, 100, 100);
        assertEquals(15.0, nv.nutriScorePoints());
    }

    @Test
    void increasingFatDoesNotDecreaseScore() {
        //fat should not reduce score
        var lowFat = new NutritionalValue(1, 0, 0);
        var highFat = new NutritionalValue(30, 0, 0);

        assertTrue(highFat.nutriScorePoints() >= lowFat.nutriScorePoints());
    }

    @Test
    void increasingProteinDoesNotIncreaseScore() {
        //protein should increase or keep score
        var base = new NutritionalValue(10, 0, 10);
        var moreProtein = new NutritionalValue(10, 20, 10);

        assertTrue(moreProtein.nutriScorePoints() <= base.nutriScorePoints());
    }

    @Test
    void increasingCarbsDoesNotDecreaseScore() {
        //carbs should not reduce score
        var lowCarbs = new NutritionalValue(0, 0, 1);
        var highCarbs = new NutritionalValue(0, 0, 30);

        assertTrue(highCarbs.nutriScorePoints() >= lowCarbs.nutriScorePoints());
    }
}
