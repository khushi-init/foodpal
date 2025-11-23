package commons;

import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

import static org.apache.commons.lang3.builder.ToStringStyle.MULTI_LINE_STYLE;

@Entity
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    // One Recipe has MANY RecipeIngredients (the join entity)
    @OneToMany(
            mappedBy = "recipe",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
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
    public Recipe (String name, List<RecipeIngredient> ingredients, List<String> preparationSteps) {
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
    

}
