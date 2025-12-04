package client.utils;

// This class is for adding cool little methods to make our lives easier. Add anything you like!
public class NeatUtils {

    /**
     * Converts a String to a Double and returns zero if it fails
     * @param text - The string to parse
     * @return - The parsed double or 0 if parsing failed
     */
    public Double parseDoubleOrZero(String text) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
