package commons;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import static org.apache.commons.lang3.builder.ToStringStyle.MULTI_LINE_STYLE;

@Entity
public class Ingredient {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    /**
     * JPA required no argument constructor
     */
    public Ingredient () {
    }

    /**
     * constructor for creation of new Ingredient
     * @param name  name of ingredient
     */
    public Ingredient (String name) {
        this.name = name;
    }

    // GETTERS AND SETTERS MANDATORY
    // for frameworks (JPA/Jackson) to read and write object data from/to the database and JSON

    public Long getId() {
        return id;
    }
    public void setId(Long id) {}

    public String getName() {
        return name;
    }
    public void setName(String name) {}

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
