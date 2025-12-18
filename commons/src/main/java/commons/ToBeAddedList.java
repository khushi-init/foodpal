package commons;

import java.util.ArrayList;
import java.util.List;

public class ToBeAddedList {

    private final List<ToBeAddedIngredient> ingredients = new ArrayList<>();

    public List<ToBeAddedIngredient> getIngredients() {
        return ingredients;
    }

    public void addIngredient(String text) {
        ingredients.add(new ToBeAddedIngredient(text));
    }

    public void removeIngredient(int index) {
        ingredients.remove(index);
    }

    public void updateIngredient(int index, String newText) {
        ingredients.get(index).setText(newText);
    }

    public boolean isEmpty() {
        return ingredients.isEmpty();
    }

    public void clear() {
        ingredients.clear();
    }
}

