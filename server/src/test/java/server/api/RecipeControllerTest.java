package server.api;

import commons.Recipe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.database.RecipeRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class RecipeControllerTest {
    private RecipeController sut;
    private RecipeRepository mockRepository;
    private Recipe testRecipe;

    @BeforeEach
    public void setUp(){
        mockRepository = mock(RecipeRepository.class); // creates mock recipeRepository object
        sut = new RecipeController(mockRepository);
        testRecipe = new Recipe("Mock recipe", null, null);
        testRecipe.setId(1L);
    }

    

}
