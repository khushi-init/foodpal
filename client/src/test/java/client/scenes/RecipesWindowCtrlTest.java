package client.scenes;

import client.utils.ErrorCtrl;
import client.utils.SearchCtrl;
import commons.Recipe;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RecipesWindowCtrlTest {
    private RecipesWindowCtrl window;
    private PrimaryCtrl primary;
    private ErrorCtrl error;

    @BeforeEach
    public void setup() {
        window = new RecipesWindowCtrl(error, new PrimaryCtrl(), new SearchCtrl());
    }

    private String invokeCreateCopyName(String baseName) throws Exception {
        Method m = RecipesWindowCtrl.class.getDeclaredMethod("createCopyName", String.class);
        m.setAccessible(true);
        return (String) m.invoke(window, baseName);
    }

    private void setRecipes(ObservableList<Recipe> recipes) throws Exception {
        Field field = RecipesWindowCtrl.class.getDeclaredField("recipes");
        field.setAccessible(true);
        field.set(window, recipes);
    }



    @Test
    public void createCopyNameTestCreatesBaseNameWithOneWhenNoCopies() throws Exception {
        ObservableList<Recipe> recipes = FXCollections.observableArrayList(
                new Recipe("Soup", null, List.of()),
                new Recipe("Pasta", null, List.of()),
                new Recipe("Salad", null, List.of())
        );
        setRecipes(recipes);
        String copyName = invokeCreateCopyName("Salad");
        assertEquals("Salad(1)", copyName);

    }

    @Test
    public void createCopyNameTestCreatesBaseNameWithTwoWhenOneCopy() throws Exception {
        ObservableList<Recipe> recipes = FXCollections.observableArrayList(
                new Recipe("Soup", null, List.of()),
                new Recipe("Pasta", null, List.of()),
                new Recipe("Salad", null, List.of()),
                new Recipe("Salad(1)", null, List.of())
        );
        setRecipes(recipes);
        String copyName = invokeCreateCopyName("Salad");
        assertEquals("Salad(2)", copyName);
    }

    @Test
    public void createCopyNameTestCreatesBaseNameWithOneOneWhenCopyingACopy() throws Exception {
        ObservableList<Recipe> recipes = FXCollections.observableArrayList(
                new Recipe("Soup", null, List.of()),
                new Recipe("Pasta", null, List.of()),
                new Recipe("Salad", null, List.of())
        );
        setRecipes(recipes);
        String copyName = invokeCreateCopyName("Salad");
        String copyName2 = invokeCreateCopyName(copyName);
        assertEquals("Salad(1)(1)", copyName2);
    }
}
