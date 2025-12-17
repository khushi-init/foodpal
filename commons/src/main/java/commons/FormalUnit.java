package commons;

public enum FormalUnit implements Unit{
    // Weight Units
    GRAM("g"),
    MILLIGRAM("mg"),
    KILOGRAM("kg"),

    // Volume Units
    MILLILITER("ml"),
    LITER("l"),
    TABLESPOON("tbsp");
    private String displayName;

    /**
     * Constructor for FormalUnit
     * @param displayName name of formal uni
     */
    FormalUnit(String displayName){
        this.displayName = displayName;
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public boolean isScalable() {
        return true;
    }
}