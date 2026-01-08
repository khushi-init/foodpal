package commons;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

import static org.apache.commons.lang3.builder.ToStringStyle.MULTI_LINE_STYLE;

@Entity
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(unique = true)
    private String name;

    @Column
    private int totalServings = 0;

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

    /**
     * JPA required no argument constructor
     */
    public  Recipe () {
    }

    /**
     * constructor for a new recipe
     * @param name  name of recipe
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

    public int getTotalServings() {
        return totalServings;
    }

    public void setTotalServings(int totalServings) {
        this.totalServings = totalServings;
    }

    /**
     * Add an amount of servings to the total amount.
     * @param servings - The servings to add to the total servings amount.
     */
    public void addServings(int servings) {
        this.totalServings += servings;
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
        output.append("## Ingredients\n");
        output.append("| Name | Amount |\n");
        output.append("|------|--------|\n");

        // Add all ingredients to the table.
        for (RecipeIngredient i : ingredients) {
            output.append("| ")
                    .append(i.getIngredient().getName())
                    .append(" | ")
                    .append(i.getQuantity())
                    .append(" |\n");
        }

        // List of all preparation steps.
        output.append("\n## Preparation Steps\n");
        for (String step : preparationSteps) {
            output.append("* ").append(step).append("\n");
        }

        output.append("\n")
                .append("*This recipe has been served ")
                .append(totalServings)
                .append(" time(s)*\n");

        return output.toString();
    }
}
