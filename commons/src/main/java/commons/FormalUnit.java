package commons;

public enum FormalUnit implements Unit{
    // Weight Units
    G("g"),
    MG("mg"),
    KG("kg"),

    // Volume Units
    ML("ml"),
    L("l");
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