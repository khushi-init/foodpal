package commons;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NutritionalValueTest {
    
    //reusable NOT MAGIC NUMBERS
    private final double z = 0.0;
    private final double one = 1.0;
    private final double four = 4.0;
    private final double nine = 9.0;
    private final double ten = 10.0;
    private final double twenty = 20.0;
    private final double thirty = 30.0;
    private final double hundred = 100.0;

    // helper to reduce repetition
    private NutritionalValue nv(double f, double p, double c) {
        return new NutritionalValue(f, p, c);
    }

    // compute expected kcal
    private double kcal(double f, double p, double c) {
        final double kf = 9.0;
        final double kp = 4.0;
        final double kc = 4.0;
        return f * kf + p * kp + c * kc;
    }

    @Test
    void nutriScoreIsCorrect() {
        assertEquals(kcal(one, one, one), nv(one, one, one).kcal100g());
        assertEquals(kcal(ten, z, z), nv(ten, z, z).kcal100g());
        assertEquals(kcal(z, ten, z), nv(z, ten, z).kcal100g());
        assertEquals(kcal(z, z, ten), nv(z, z, ten).kcal100g());
    }

    @Test
    void nutriScorePointsAllZero() {
        assertEquals(z, nv(z, z, z).nutriScorePoints());
    }

    @Test
    void nutriScorePointsKnownExample() {
        final double expected = 3.0;
        assertEquals(expected, nv(nine, four, four).nutriScorePoints());
    }

    @Test
    void nutriScorePointsOutOfUpperBounds() {
        final double expected = 15.0;
        assertEquals(expected, nv(hundred, hundred, hundred).nutriScorePoints());
    }

    @Test
    void increasingFatDoesNotDecreaseScore() {
        assertTrue(nv(thirty, z, z).nutriScorePoints() >= nv(one, z, z).nutriScorePoints());
    }

    @Test
    void increasingCarbsDoesNotDecreaseScore() {
        assertTrue(nv(z, z, thirty).nutriScorePoints() >= nv(z, z, one).nutriScorePoints());
    }

    @Test
    void increasingProteinDoesNotIncreaseScore() {
        assertTrue(nv(ten, twenty, ten).nutriScorePoints() <= nv(ten, z, ten).nutriScorePoints());
    }
}
