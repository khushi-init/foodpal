package commons;

public record NutritionalValue(
        double fat100g,
        double protein100g,
        double carbs100g
) {
    private static final int kcalPerGramFat = 9;
    private static final int kcalPerGramProtein = 4;
    private static final int kcalPerGramCarbs = 4;

    /**
     * Calculates the estimated calories per 100g
     * @return kcal per 100g
     */
    public double kcal100g(){
        return (this.fat100g*kcalPerGramFat)
                +(this.protein100g*kcalPerGramProtein)
                +(this.carbs100g*kcalPerGramCarbs);
    }

}
