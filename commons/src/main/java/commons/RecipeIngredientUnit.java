package commons;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import org.apache.commons.lang3.tuple.Pair;

// The @Embeddable annotation tells JPA/Hibernate that this class
// is not a separate table, but that it's fields should be inserted directly
// into the table of the entity that owns it, in this case: RecipeIngredient
@Embeddable
public class RecipeIngredientUnit {

    // storing the type of the unit
    @Enumerated(EnumType.STRING)
    private UnitType type;

    private String formalUnitName;
    private String informalUnitName;

    private final static int thousand= 1000;

    /**
     * JPA required no argument constructor
     */
    public RecipeIngredientUnit() {
    }

    /**
     * Custom constructor for manual initialization
     * @param type type of unit
     * @param formalUnitName name of formal unit
     * @param informalUnitName name of informal unit
     */
    public RecipeIngredientUnit(UnitType type, String formalUnitName, String informalUnitName) {
        this.type = type;
        this.formalUnitName = formalUnitName;
        this.informalUnitName = informalUnitName;
    }

    public UnitType getType() {
        return type;
    }
    public String getFormalUnitName() {
        return formalUnitName;
    }
    public String getInformalUnitName() {
        return informalUnitName;
    }

    public void setType(UnitType type) {
        this.type = type;
    }
    public void setFormalUnitName(String name) {
        this.formalUnitName = name;
    }
    public void setInformalUnitName(String name) {
        this.informalUnitName = name;
    }

    /**
     * Converts a concrete Unit object (formal or informal) into a
     * persistable RecipeIngredientUnit storage wrapper.
     * @param unit unit to be converted
     * @return RecipeIngredientUnit storage wrapper of the unit
     */
    public static RecipeIngredientUnit fromUnit(Unit unit) {
        RecipeIngredientUnit recipeIngredientUnit = new RecipeIngredientUnit();
        if(unit instanceof FormalUnit formalUnit) {
            recipeIngredientUnit.type = UnitType.FORMAL;
            recipeIngredientUnit.formalUnitName = formalUnit.name();
        } else if (unit instanceof InformalUnit informalunit) {
            recipeIngredientUnit.type = UnitType.INFORMAL;
            recipeIngredientUnit.informalUnitName = informalunit.getDisplayName();
        } else  {
            throw new IllegalArgumentException("Unknown unit type : " + unit.getClass().getName());
        }
        return recipeIngredientUnit;
    }

    /**
     * Reconstructs the original concrete Unit object from the data contained within the storage wrapper
     * @return the original unit object
     */
    public Unit toUnit() {
        if(this.type == UnitType.FORMAL && this.formalUnitName != null) {
            return FormalUnit.valueOf(formalUnitName);
        }
        else if(this.type == UnitType.INFORMAL && this.informalUnitName != null) {
            return new InformalUnit(informalUnitName);
        }
        return null;
    }

    /**
     * Normalizes quantity and unit for display purposes.
     * Note: This uses Pair.of() because Apache Commons Pair is abstract.
     */
    public Pair<Double, String> getNormalizedDisplay(double qty) {
        String unit = "";

        // Get the name to check
        if (this.type == UnitType.FORMAL && this.formalUnitName != null) {
            // Assuming FormalUnit is an enum like GRAM("g")
            unit = FormalUnit.valueOf(formalUnitName).getDisplayName();
        } else if (this.type == UnitType.INFORMAL) {
            unit = informalUnitName;
        }

        if (qty >= thousand && unit != null) {
            switch (unit.toLowerCase()) {
                case "mg": return Pair.of(qty / thousand, "g");
                case "g":  return Pair.of(qty / thousand, "kg");
                case "ml": return Pair.of(qty / thousand, "l");
            }
        }
        return Pair.of(qty, unit);
    }
}
