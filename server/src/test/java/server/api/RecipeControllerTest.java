package server.api;

import commons.Recipe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import server.database.RecipeRepository;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.springframework.http.HttpStatus.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class RecipeControllerTest {
    private RecipeController sut;
    private RecipeRepository mockRepository;
    private Recipe testRecipe;
    private Long id = 1L;
    private Long id2 = 2L;
    private Long fakeId = 99L;

    @BeforeEach
    public void setUp(){
        // Create mock recipeRepository object
        mockRepository = mock(RecipeRepository.class);
        sut = new RecipeController(mockRepository);
        testRecipe = new Recipe("Mock recipe", null, null);
        testRecipe.setId(id);
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

    @Test
    public void getByIdCorrect() {
        // ARRANGE: Tell the mock that when findById(1L) is called, return the testRecipe
        when(mockRepository.findById(id)).thenReturn(Optional.of(testRecipe));

        // ACT: Call the controller method
        ResponseEntity<Recipe> response = sut.findRecipeById(id);

        // ASSERT: Check HTTP status (200 OK)
        assertEquals(OK, response.getStatusCode());
        verify(mockRepository, times(1)).findById(id);
    }


    @Test
    public void getByIdNotFound() {
        // ARRANGE: Tell the mock that when findById(99L) is called, return an empty Optional
        when(mockRepository.findById(fakeId)).thenReturn(Optional.empty());

        // ACT: Call the controller method with a non-existent ID
        ResponseEntity<Recipe> response = sut.findRecipeById(fakeId);

        // ASSERT: Check HTTP status (404 Not Found)
        assertEquals(NOT_FOUND, response.getStatusCode());
        verify(mockRepository, times(1)).findById(fakeId);
    }

    @Test
    public void downloadRecipeNotFoundTest() {
        // ARRANGE
        when(mockRepository.findById(fakeId)).thenReturn(Optional.empty());

        // ACT
        ResponseEntity<byte[]> response = sut.downloadRecipe(fakeId);

        // ASSERT
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(mockRepository, times(1)).findById(fakeId);
    }

    @Test
    public void downloadRecipe_Found() {
        // ARRANGE
        Long id = 1L;

        Recipe recipe = new Recipe();
        recipe.setName("TestRecipe");
        recipe.setIngredients(List.of());
        recipe.setPreparationSteps(List.of("Do this"));

        when(mockRepository.findById(id)).thenReturn(Optional.of(recipe));

        // ACT
        ResponseEntity<byte[]> response = sut.downloadRecipe(id);

        // ASSERT
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("attachment; filename=recipe-TestRecipe.md",
                response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
    }

    @Test
    public void createRecipeCorrect() {
        // ARRANGE: Input recipe with no ID
        Recipe inputRecipe = new Recipe("New Cake", null, null);

        // ARRANGE: The mock must simulate the database assigning an ID upon save
        Recipe savedRecipe = new Recipe("New Cake",  null, null);
        savedRecipe.setId(id2);
        when(mockRepository.save(inputRecipe)).thenReturn(savedRecipe);

        // ACT
        ResponseEntity<Recipe> response = sut.createRecipe(inputRecipe);

        // ASSERT: Check HTTP status (201 Created)
        assertEquals(CREATED, response.getStatusCode());
        verify(mockRepository, times(1)).save(inputRecipe);
    }

    @Test
    public void createRecipeBadRequest() {
        // ARRANGE: Create a recipe that fails validation (empty name)
        Recipe badRecipe = new Recipe("",  null, null);

        // ACT
        ResponseEntity<Recipe> response = sut.createRecipe(badRecipe);

        // ASSERT: Check HTTP status (400 Bad Request)
        assertEquals(BAD_REQUEST, response.getStatusCode());

        // ASSERT: Verify the save method was never called due to validation failure
        verify(mockRepository, never()).save(badRecipe);
    }

    @Test
    public void deleteRecipeCorrect() {
        // ARRANGE: Tell the mock that the recipe with ID 1 exists
        when(mockRepository.existsById(id)).thenReturn(true);

        // ACT
        ResponseEntity<?> response = sut.deleteRecipeById(id);

        // ASSERT: Check HTTP status (204 No Content)
        assertEquals(NO_CONTENT, response.getStatusCode());

        verify(mockRepository, times(1)).deleteById(id);
    }

    @Test
    public void deleteRecipeNotFound() {
        // ARRANGE: Tell the mock that the recipe with ID 99 does not exist
        when(mockRepository.existsById(fakeId)).thenReturn(false);

        // ACT
        ResponseEntity<?> response = sut.deleteRecipeById(fakeId);

        // ASSERT: Check HTTP status (404 Not Found)
        assertEquals(NOT_FOUND, response.getStatusCode());

        verify(mockRepository, never()).deleteById(fakeId);
    }


}
