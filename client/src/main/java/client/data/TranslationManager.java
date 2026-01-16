package client.data;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class TranslationManager {

    private final ObjectProperty<ResourceBundle> bundle = new SimpleObjectProperty<>();
    private Locale currentLocale;

    public TranslationManager() {
        setLanguage(Locale.ENGLISH);
    }

    public void setLanguage(Locale locale) {
        this.currentLocale = locale;
        this.bundle.set(ResourceBundle.getBundle("i18n.messages", locale));
    }

    public String tr(String key, Object... args) {
        if (bundle.get() == null) return key;
        try {
            String value = bundle.get().getString(key);
            return MessageFormat.format(value, args);
        } catch (Exception e) {
            return key;
        }
    }

    public ObjectProperty<ResourceBundle> bundleProperty() {
        return bundle;
    }

    public Locale getCurrentLocale() {
        return currentLocale;
    }
}