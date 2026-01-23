package client.scenes;

import client.MyFXML;
import client.data.DataManipulator;
import client.data.LocalStorage;
import client.utils.SearchService;
import client.utils.ServerUtils;
import client.data.TranslationManager;
import commons.Recipe;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RecipesWindowCtrlTest {
    private RecipesWindowCtrl window;
    private PrimaryCtrl primary;
    private ErrorCtrl error;
    private LocalStorage storage;
    private DataManipulator dataManipulator;
    private ServerUtils server;
    private MyFXML fxml;
    private TranslationManager tm;

    @BeforeEach
    public void setup() {
        tm = new TranslationManager();
        error = new ErrorCtrl(null, tm);
        server = new ServerUtils(error, new ConfigService(error), tm);
        storage = new LocalStorage(null);
        dataManipulator = new DataManipulator(storage, server, error, tm, new ConfigService(error));
        tm = new TranslationManager();
        window = new RecipesWindowCtrl(null, error, primary, storage, dataManipulator, new SearchService(), server, null, tm);
    }

    private String invokeCreateCopyName(String baseName) throws Exception {
        Method m = RecipesWindowCtrl.class.getDeclaredMethod("createCopyName", String.class);
        m.setAccessible(true);
        return (String) m.invoke(window, baseName);
    }

    private void setRecipes(ObservableList<Recipe> recipes) throws Exception {
        storage.setRecipes(recipes);
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
