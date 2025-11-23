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
    public Long id;

    public String name;
    public List<Ingredient> ingredients;

    @ElementCollection
    public List<String> preparationSteps;

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
    public Recipe (String name, List<Ingredient> ingredients, List<String> preparationSteps) {
        this.name = name;
        this.ingredients = ingredients;
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
