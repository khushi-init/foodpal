package commons;

public interface Unit {
    /**
     * method to get display name of unit
     * @return display name of unit
     */
    String getDisplayName();

    /**
     * method to know if the unit is scalable
     * @return true or false whether unit is scalable or not
     */
    boolean isScalable();
}
