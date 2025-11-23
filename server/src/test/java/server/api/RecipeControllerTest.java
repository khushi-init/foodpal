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

    @Test
    public void getAllRecipesTest(){
        // ARRANGE: Tell the mock to return a list containing one recipe
        List<Recipe> expectedList = List.of(testRecipe);
        when(mockRepository.findAll()).thenReturn(expectedList);

        // ACT
        List<Recipe> actualList = sut.findAllRecipes();

        // ASSERT: Check that the returned list matches the one the mock gave
        assertEquals(expectedList, actualList);
        // Check that the method was called on the fake database (Verification)
        verify(mockRepository, times(1)).findAll();
    }



}
