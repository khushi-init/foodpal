package commons;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import static org.apache.commons.lang3.builder.ToStringStyle.MULTI_LINE_STYLE;

@Entity
public class RecipeIngredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "recipe_id")
    @JsonBackReference
    private Recipe recipe;

    // CascadeTpe.PERSIST --> if you use a new Ingredient when trying to create an instance of RecipeIngredient,
    // it saves the new Ingredient first
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient;

    private Double quantity;

    /**
     * JPA required no argument constructor
     */
    public RecipeIngredient() {

    }

    /**
     * creates a linked usage instance
     * @param recipe the recipe
     * @param ingredient the ingredient
     * @param quantity quantity of the ingredient
     */
    public RecipeIngredient(Recipe recipe, Ingredient ingredient, Double quantity) {
        this.recipe = recipe;
        this.ingredient = ingredient;
        this.quantity = quantity;
    }


    // GETTERS AND SETTERS MANDATORY
    // for frameworks (JPA/Jackson) to read and write object data from/to the database and JSON

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
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

}
