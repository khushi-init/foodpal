package client.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AppSettings {
    private String url = "http://localhost:8080";
    private List<Long> favoriteIds = new ArrayList<>();

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
}
