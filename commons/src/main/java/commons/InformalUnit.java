package commons;

public class InformalUnit implements Unit{
    private String name;

    /**
     * Constructor for InformalUnit
     * @param name name of informal unit
     */
    public InformalUnit(String name) {
        this.name = name;
    }

    @Override
    public String getDisplayName() {
        return this.name;
    }

    @Override
    public boolean isScalable() {
        return false;
    }
}
