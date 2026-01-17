package client.data;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class TranslationManager {

    private final ObjectProperty<ResourceBundle> bundle = new SimpleObjectProperty<>();
    private Locale currentLocale;

    /**
     *
     *Constructor for TranslationManager, initializes with default language English
     */
    public TranslationManager() {
        setLanguage(Locale.ENGLISH);
    }

    /**
     *Sets the active language for the UI and loads the corresponding resource bundle.
     *@param locale the locale to switch the application language to
     */
    public void setLanguage(Locale locale) {
        this.currentLocale = locale;
        this.bundle.set(ResourceBundle.getBundle("i18n.messages", locale));
    }

    /**
     *Translates the given key using the active language.
     *If the key is not found, the key itself is returned.
     *@param key the translation key
     *@param args optional arguments for formatted messages
     *@return the translated string or the key if no translation is found
     */
    public String tr(String key, Object... args) {
        if (bundle.get() == null) return key;
        try {
            String value = bundle.get().getString(key);
            return MessageFormat.format(value, args);
        } catch (Exception e) {
            return key;
        }
    }

    /**
     *Returns the resource bundle property used for translations. --> where translates text lives
     *@return the current resource bundle property
     */
    public ObjectProperty<ResourceBundle> bundleProperty() {
        return bundle;
    }

    /**
     *Returns the currently active locale. -->which language is currently set
     *@return the current locale
     */
    public Locale getCurrentLocale() {
        return currentLocale;
    }
}