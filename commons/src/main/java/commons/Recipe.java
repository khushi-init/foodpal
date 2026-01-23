package commons;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

import static commons.FormalUnit.*;
import static org.apache.commons.lang3.builder.ToStringStyle.MULTI_LINE_STYLE;

@Entity
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(unique = true)
    private String name;

    @Column
    private Integer totalServings = 0;

    @Column
    private String language = "en";

    // One Recipe has MANY RecipeIngredients (the join entity)
    @OneToMany(
            mappedBy = "recipe",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonManagedReference
    private List<RecipeIngredient> ingredients;

    @ElementCollection
    private List<String> preparationSteps;

    private static final double thousand = 1000.0;
    private static final double hundered= 100.0;
    private static final double fifteen = 15.0;


    /**
     * JPA required no argument constructor
     */
    public  Recipe () {
    }

    /**
     * constructor for a new recipe
     * @param name  name of recipe
     * @param totalServings the amount of total servings
     * @param ingredients list of ingredients
     * @param preparationSteps list of preparation steps
     */
    public Recipe (String name, int totalServings, List<RecipeIngredient> ingredients,
                   List<String> preparationSteps) {
        this.name = name;
        this.totalServings = totalServings;
        this.ingredients = ingredients;
        this.preparationSteps = preparationSteps;
    }

    /**
     * constructor for a new recipe
     * @param name  name of recipe
     * @param ingredients list of ingredients
     * @param preparationSteps list of preparation steps
     */
    public Recipe (String name, List<RecipeIngredient> ingredients,
                   List<String> preparationSteps) {
        this.name = name;
        this.ingredients = ingredients;
        this.preparationSteps = preparationSteps;
    }

    // GETTERS AND SETTERS MANDATORY
    // for frameworks (JPA/Jackson) to read and write object data from/to the database and JSON

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getTotalServings() {
        return totalServings;
    }

    public void setTotalServings(Integer totalServings) {
        if (totalServings != null && totalServings >= 0) {
            this.totalServings = totalServings;
        }
    }

    public List<RecipeIngredient> getIngredients() {
        return ingredients;
    }

    public List<String> getPreparationSteps() {
        return preparationSteps;
    }

    public void setIngredients(List<RecipeIngredient> ingredients) {
        this.ingredients = ingredients;
    }

    public void setPreparationSteps(List<String> preparationSteps) {
        this.preparationSteps = preparationSteps;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, MULTI_LINE_STYLE);
    }

    /**
     * Turn the recipe into a markdown format.
     * @return the string with the markdown format.
     */
    public String toMarkdown() {
        StringBuilder output = new StringBuilder();

        // Header 1 with the name of the recipe
        output.append("# ").append(name).append("\n\n");
        // List of ingredients in a table
        output.append("## Ingredients\n")
                .append("| Name | Amount |\n")
                .append("|------|--------|\n");

        // Add all ingredients to the table.
        for (RecipeIngredient i : ingredients) {
            output.append("| ")
                    .append(i.getIngredient().getName())
                    .append(" | ");

            String amountText;

            try {
                if (i.getUnit() == null) {
                    amountText = String.valueOf(i.getQuantity());
                } else {
                    Unit unit = i.getUnit().toUnit();
                    if (unit == null) {
                        amountText = "N/A";
                    } else {
                        amountText = i.getQuantity() + " "
                                + unit.getDisplayName();
                    }
                }
            } catch (Exception e) {
                amountText = "N/A";
            }

            output.append(amountText)
                    .append(" |\n");
        }

        // List of all preparation steps.
        output.append("\n## Preparation Steps\n");
        for (String step : preparationSteps) {
            output.append("* ")
                    .append(step)
                    .append("\n");
        }

        output.append("\n")
                .append("*This recipe gives you a total of ")
                .append(totalServings)
                .append(" serving(s)*\n");

        return output.toString();
    }

    public double convertToGrams(double quantity, Unit unit) {
        return switch (unit) {
            // Weight
            case G -> quantity;
            case MG -> quantity / thousand;
            case KG -> quantity * thousand;

            // Volume (Assuming 1ml = 1g)
            case ML -> quantity;
            case L -> quantity * thousand;


            default -> 0.0;
        };
    }

    /**
     * Calculates the kcal per 100g for the supplied recipe considering its nutritional value
     * @param recipe The recipe from which the nutritional value will be taken
     * @return The kcal/100g of that tecipe
     */
    public double calculateRecipeKcalPer100g(Recipe recipe) {
        double totalWeightInGrams = 0;
        double totalCalories = 0;

        for (RecipeIngredient ri : recipe.getIngredients()) {
            if (ri.getUnit() == null) {
                continue;
            }
            Unit unit = ri.getUnit().toUnit();
            if (unit != null) {
                double weightGrams = convertToGrams(ri.getQuantity(), unit);

                double kcalPer100g = ri.getIngredient().getNutritionalValue().kcal100g();
                totalCalories += (weightGrams * kcalPer100g) / hundered;
                totalWeightInGrams += weightGrams;
            }
        }

        if (totalWeightInGrams <= 0) {
            return 0;
        }
        return (totalCalories / totalWeightInGrams) * hundered;
    }

    /**
     * Calculates the total calories for the entire recipe based on all ingredients.
     * @return total kcal
     */
    public double calculateTotalKcal() {
        double totalCalories = 0;

        for (RecipeIngredient ri : this.ingredients) {
            if (ri.getUnit() == null) continue;

            Unit unit = ri.getUnit().toUnit();
            if (unit != null) {
                // Convert current ingredient quantity to grams
                double weightGrams = convertToGrams(ri.getQuantity(), unit);

                // Get kcal per 100g from the NutritionalValue object
                double kcalPer100g = ri.getIngredient().getNutritionalValue().kcal100g();

                // Add to total: (Weight / 100) * kcal
                totalCalories += (weightGrams * kcalPer100g) / hundered;
            }
        }
        return totalCalories;
    }
}
