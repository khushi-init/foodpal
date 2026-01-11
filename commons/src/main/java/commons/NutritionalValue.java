package commons;

public record NutritionalValue(
        double fat100g,
        double protein100g,
        double carbs100g
) {
    private static final int kcalPerGramFat = 9;
    private static final int kcalPerGramProtein = 4;
    private static final int kcalPerGramCarbs = 4;

    // NutriScore constants
    private static final double[] kcalBounds = {80, 160, 240, 320, 400, 480, 560, 640, 720};
    private static final double[] carbsBounds = {2, 5, 10, 15, 20};
    private static final double[] fatsBounds = {3, 8, 15, 25, 35};
    private static final double[] proteinsBounds = {3, 6, 10, 15};

    /**
     * Calculates the estimated calories per 100g
     * @return kcal per 100g
     */
    public double kcal100g(){
        return (this.fat100g*kcalPerGramFat)
                +(this.protein100g*kcalPerGramProtein)
                +(this.carbs100g*kcalPerGramCarbs);
    }

    /**
     * Calculates the NutriScore points based on nutritional values
     * @return the total points for NutriScore calculation
     */
    public int nutriScorePoints() {
        return pointsKcal(kcal100g()) +
               pointsCarbs(carbs100g) +
               pointsFats(fat100g) -
               pointsProtein(protein100g);
    }

    private int pointsKcal(double kcal) {
        return pointsFromUpperBounds(kcal, kcalBounds);
    }

    private int pointsCarbs(double carbs) {
        return pointsFromUpperBounds(carbs, carbsBounds);
    }

    private int pointsFats(double fat) {
        return pointsFromUpperBounds(fat, fatsBounds);
    }

    private int pointsProtein(double protein) {
        return pointsFromUpperBounds(protein, proteinsBounds);
    }

    private int pointsFromUpperBounds(double value, double[] upperBounds) {
        for (int i = 0; i < upperBounds.length; i++) {
            if (value <= upperBounds[i]) return i;
        }
        return upperBounds.length;
    }
}
