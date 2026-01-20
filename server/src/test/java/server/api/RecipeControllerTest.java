package server.api;

import commons.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import server.service.RecipeService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.http.HttpStatus.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RecipeControllerTest {

    @Mock
    private RecipeService mockRecipeService;

    private Recipe testRecipe;

    private final Long id = 1L;
    private final Long id2 = 2L;
    private final Long fakeId = 99L;

    private final int negativeServingsAmount = -2;
    private final int nutritionalValue = 10;
    private final double ingredientQuantity = 10.0;

    @InjectMocks
    private RecipeController sut;

    @BeforeEach
    public void setUp(){
        testRecipe = new Recipe("Mock recipe", null, null);
        testRecipe.setId(id);
    }

    @Test
    public void getAllRecipesTest(){
        // ARRANGE: Tell the mock to return a list containing one recipe
        List<Recipe> expectedList = List.of(testRecipe);
        when(mockRecipeService.getAllRecipes()).thenReturn(expectedList);

        // ACT
        List<Recipe> actualList = sut.findAllRecipes();

        // ASSERT: Check that the returned list matches the one the mock gave
        assertEquals(expectedList, actualList);
        // Check that the method was called on the fake database (Verification)
        verify(mockRecipeService, times(1)).getAllRecipes();
    }

    @Test
    public void getByIdCorrectTest() {
        // ARRANGE: Tell the mock that when findById(1L) is called, return the testRecipe
        when(mockRecipeService.getRecipeById(id)).thenReturn(Optional.of(testRecipe));

        // ACT: Call the controller method
        ResponseEntity<Recipe> response = sut.findRecipeById(id);

        // ASSERT: Check HTTP status (200 OK)
        assertEquals(OK, response.getStatusCode());
        verify(mockRecipeService, times(1)).getRecipeById(id);
    }


    @Test
    public void getByIdNotFoundTest() {
        // ARRANGE: Tell the mock that when findById(99L) is called, return an empty Optional
        when(mockRecipeService.getRecipeById(fakeId)).thenReturn(Optional.empty());

        // ACT: Call the controller method with a non-existent ID
        ResponseEntity<Recipe> response = sut.findRecipeById(fakeId);

        // ASSERT: Check HTTP status (404 Not Found)
        assertEquals(NOT_FOUND, response.getStatusCode());
        verify(mockRecipeService, times(1)).getRecipeById(fakeId);
    }

    @Test
    public void downloadRecipeNotFoundTest() {
        // ARRANGE
        when(mockRecipeService.getRecipeById(fakeId)).thenReturn(Optional.empty());

        // ACT
        ResponseEntity<byte[]> response = sut.downloadRecipe(fakeId);

        // ASSERT
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(mockRecipeService, times(1)).getRecipeById(fakeId);
    }

    @Test
    public void downloadRecipeFoundTest() {
        // ARRANGE
        Long id = 1L;

        Recipe recipe = new Recipe();
        recipe.setName("TestRecipe");
        recipe.setIngredients(List.of());
        recipe.setPreparationSteps(List.of("Do this"));

        when(mockRecipeService.getRecipeById(id)).thenReturn(Optional.of(recipe));

        // ACT
        ResponseEntity<byte[]> response = sut.downloadRecipe(id);

        // ASSERT
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("attachment; filename=recipe-TestRecipe.md",
                response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
    }

    @Test
    public void createRecipeCorrectTest() {
        // ARRANGE: Input recipe with no ID
        Recipe inputRecipe = new Recipe("New Cake", null, null);

        // ARRANGE: The mock must simulate the database assigning an ID upon save
        Recipe savedRecipe = new Recipe("New Cake",  null, null);
        savedRecipe.setId(id2);
        when(mockRecipeService.createRecipe(inputRecipe)).thenReturn(savedRecipe);

        // ACT
        ResponseEntity<Recipe> response = sut.createRecipe(inputRecipe);

        // ASSERT: Check HTTP status (201 Created)
        assertEquals(CREATED, response.getStatusCode());
        verify(mockRecipeService, times(1)).createRecipe(inputRecipe);
    }

    @Test
    public void createRecipeEmptyNameTest() {
        // ARRANGE: Create a recipe where the name is empty
        Recipe badRecipe = new Recipe("",  null, null);

        // ACT
        ResponseEntity<Recipe> response = sut.createRecipe(badRecipe);

        // ASSERT: Check HTTP status (400 Bad Request)
        assertEquals(BAD_REQUEST, response.getStatusCode());

        // ASSERT: Verify the save method was never called due to validation failure
        verify(mockRecipeService, never()).createRecipe(badRecipe);
    }

    @Test
    public void createRecipeNullNameTest() {
        // ARRANGE: Create a recipe where name field is null
        Recipe badRecipe = new Recipe(null,  null, null);

        // ACT
        ResponseEntity<Recipe> response = sut.createRecipe(badRecipe);

        // ASSERT: Check HTTP status (400 Bad Request)
        assertEquals(BAD_REQUEST, response.getStatusCode());

        // ASSERT: Verify the save method was never called due to validation failure
        verify(mockRecipeService, never()).createRecipe(badRecipe);
    }

    @Test
    public void createRecipeNegativeServingsTest() {
        // ARRANGE: Create a recipe with an invalid amount of servings
        Recipe badRecipe = new Recipe("",  negativeServingsAmount, null, null);

        // ACT
        ResponseEntity<Recipe> response = sut.createRecipe(badRecipe);

        // ASSERT: Check HTTP status (400 Bad Request)
        assertEquals(BAD_REQUEST, response.getStatusCode());

        // ASSERT: Verify the save method was never called due to validation failure
        verify(mockRecipeService, never()).createRecipe(badRecipe);
    }

    @Test
    public void changeRecipeCorrectTest() {
        Recipe updated = new Recipe("Updated Recipe Name", new ArrayList<>(), new ArrayList<>());
        updated.setId(id);

        // The service now returns an Optional<Recipe> in our refactored PutMapping
        when(mockRecipeService.updateRecipe(eq(id), any(Recipe.class))).thenReturn(Optional.of(updated));

        ResponseEntity<Recipe> response = sut.changeRecipe(id, updated);

        assertEquals(OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
    }

    @Test
    public void changeRecipeInvalidIDTest() {
        Recipe updated = new Recipe("Updated Recipe Name", new ArrayList<>(), new ArrayList<>());
        updated.setId(id);

        // Recipe with this id does not exist
        when(mockRecipeService.updateRecipe(eq(fakeId), any(Recipe.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<Recipe> response = sut.changeRecipe(fakeId, updated);

        assertEquals(NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void changeRecipeNullNameTest() {
        // Arrange
        Recipe updated = new Recipe(null, new ArrayList<>(), new ArrayList<>());
        updated.setId(id);

        // Act
        ResponseEntity<Recipe> response = sut.changeRecipe(id, updated);

        assertEquals(BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void changeRecipeEmptyNameTest() {
        // Arrange
        Recipe updated = new Recipe("", new ArrayList<>(), new ArrayList<>());
        updated.setId(id);

        // Act
        ResponseEntity<Recipe> response = sut.changeRecipe(id, updated);

        assertEquals(BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void changeRecipeInvalidServingsTest() {
        // Arrange
        Recipe updated = new Recipe(null, negativeServingsAmount, new ArrayList<>(), new ArrayList<>());
        updated.setId(id);

        // Act
        ResponseEntity<Recipe> response = sut.changeRecipe(id, updated);

        assertEquals(BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void changeRecipeInvalidIngredientTest() {
        // Arrange
        Ingredient ingredient = new Ingredient(
                "",
                new NutritionalValue(nutritionalValue, nutritionalValue, nutritionalValue)
        );
        RecipeIngredient recipeIngredient = new RecipeIngredient(
                null,
                ingredient,
                ingredientQuantity,
                new RecipeIngredientUnit(UnitType.FORMAL, "g", "g")
        );

        List<RecipeIngredient> recipeIngredients = List.of(recipeIngredient);

        Recipe updated = new Recipe("Updated", recipeIngredients, new ArrayList<>());
        updated.setId(id);

        // Act
        ResponseEntity<Recipe> response = sut.changeRecipe(id, updated);

        // Assert
        assertEquals(BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void deleteRecipeCorrectTest() {
        // ARRANGE: Tell the mock that the recipe with ID 1 exists
        when(mockRecipeService.deleteRecipe(id)).thenReturn(true);

        // ACT
        ResponseEntity<?> response = sut.deleteRecipeById(id);

        // ASSERT: Check HTTP status (204 No Content)
        assertEquals(NO_CONTENT, response.getStatusCode());

        verify(mockRecipeService, times(1)).deleteRecipe(id);
    }

    @Test
    public void deleteRecipeNotFoundTest() {
        // ARRANGE: Tell the mock that the recipe with ID 99 does not exist
        when(mockRecipeService.deleteRecipe(fakeId)).thenReturn(false);

        // ACT
        ResponseEntity<?> response = sut.deleteRecipeById(fakeId);

        // ASSERT: Check HTTP status (404 Not Found)
        assertEquals(NOT_FOUND, response.getStatusCode());

        verify(mockRecipeService, times(1)).deleteRecipe(fakeId);
    }

    @Test
    public void deleteIngredientFromRecipeCorrectTest() {
        //when service reports successful removal
        when(mockRecipeService.removeIngredientFromRecipe(id, id2)).thenReturn(true);

        ResponseEntity<Void> response = sut.deleteIngredientFromRecipe(id, id2);

        assertEquals(NO_CONTENT, response.getStatusCode());
        verify(mockRecipeService, times(1)).removeIngredientFromRecipe(id, id2);
    }

    @Test
    public void deleteIngredientFromRecipeNotFoundTest() {
        //when service reports failure (recipe or ingredient not found)
        when(mockRecipeService.removeIngredientFromRecipe(id, fakeId)).thenReturn(false);

        ResponseEntity<Void> response = sut.deleteIngredientFromRecipe(id, fakeId);

        assertEquals(NOT_FOUND, response.getStatusCode());
        verify(mockRecipeService, times(1)).removeIngredientFromRecipe(id, fakeId);
    }

}