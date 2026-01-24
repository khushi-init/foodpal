package client.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AppSettings {
    private String url = "http://localhost:8080";
    private List<Long> favoriteIds = new ArrayList<>();
    private Locale savedLocale =  Locale.getDefault();

    /**
     * Empty AppsSettings constructor for JDB
     */
    public AppSettings() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<Long> getFavoriteIds() {
        return favoriteIds;
    }

    public void setFavoriteIds(List<Long> favoriteIds) {
        this.favoriteIds = favoriteIds;
    }

    public Locale getSavedLocale() {
        return savedLocale;
    }
    public void setSavedLocale(Locale savedLocale) {
        this.savedLocale = savedLocale;
    }

}
