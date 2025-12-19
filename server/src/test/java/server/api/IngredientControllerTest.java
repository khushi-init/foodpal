package server.api;

import commons.Ingredient;
import commons.NutritionalValue;
import commons.Recipe;
import commons.RecipeIngredient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import server.database.IngredientRepository;
import server.database.RecipeIngredientRepository;
import server.service.IngredientService;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class IngredientControllerTest {

    private IngredientController sut;
    private IngredientService mockIngredientService;
    private Ingredient testIngredient;
    private RecipeIngredient testRI1;
    private RecipeIngredient testRI2;
    private Recipe testRecipe1;
    private Recipe testRecipe2;
    private final double fakeQuantity = 3.0;
    private Long id = 1L;
    private Long id2 = 2L;
    private Long fakeRecipeId1 = 30L;
    private Long fakeRecipeId2 = 31L;
    private Long fakeId = 99L;
    private NutritionalValue defaultNutritionalValue = new NutritionalValue(0, 0, 0);


    @BeforeEach
    public void setUp(){
        // Setup mock repository and controller
        mockIngredientService = mock(IngredientService.class);
        sut = new IngredientController(mockIngredientService);


        // Setup test data
        testIngredient = new Ingredient("Sugar", defaultNutritionalValue);
        testIngredient.setId(id);


        testRI1 = new RecipeIngredient(testRecipe1, testIngredient, fakeQuantity);
        testRecipe1 = new Recipe("Ice soup", List.of(testRI1), List.of());

        testRI2 = new RecipeIngredient(testRecipe2, testIngredient, fakeQuantity);
        testRecipe2 = new Recipe("Ice soup", List.of(testRI2), List.of());

        testRecipe1.setId(fakeRecipeId1);
        testRecipe2.setId(fakeRecipeId2);
    }

    @Test
    public void getAllIngredientsTest(){
        // ARRANGE: Mock repository to return a list
        List<Ingredient> expectedList = List.of(testIngredient);
        when(mockIngredientService.getAllIngredients()).thenReturn(expectedList);

        // ACT
        List<Ingredient> actualList = mockIngredientService.getAllIngredients();

        // ASSERT: Check list content and verify repository call
        assertEquals(expectedList, actualList);
        verify(mockIngredientService, times(1)).getAllIngredients();
    }

    @Test
    public void getByIdCorrect() {
        // ARRANGE: Mock repository to return the ingredient when ID 1 is requested
        when(mockIngredientService.getIngredientByID(id)).thenReturn(Optional.of(testIngredient));

        // ACT
        ResponseEntity<Ingredient> response = sut.getIngredientById(id);

        // ASSERT: Check 200 OK status
        assertEquals(OK, response.getStatusCode());
        verify(mockIngredientService, times(1)).getIngredientByID(id);
    }

    @Test
    public void getByIdNotFound() {
        // ARRANGE: Mock repository to return empty when ID 99 is requested
        when(mockIngredientService.getIngredientByID(fakeId)).thenReturn(Optional.empty());

        // ACT
        ResponseEntity<Ingredient> response = sut.getIngredientById(fakeId);

        // ASSERT: Check 404 Not Found status
        assertEquals(NOT_FOUND, response.getStatusCode());
        verify(mockIngredientService, times(1)).getIngredientByID(fakeId);
    }

    @Test
    public void createIngredientCorrect() {
        // ARRANGE: Mock repository to return the saved object with a new ID
        Ingredient inputIngredient = new Ingredient("Flour", defaultNutritionalValue);
        Ingredient savedIngredient = new Ingredient("Flour",  defaultNutritionalValue);
        savedIngredient.setId(id2);
        when(mockIngredientService.createIngredient(inputIngredient)).thenReturn(Optional.of(savedIngredient));

        // ACT
        ResponseEntity<Ingredient> response = sut.createIngredient(inputIngredient);

        // ASSERT: Check 201 Created status
        assertEquals(CREATED, response.getStatusCode());
        verify(mockIngredientService, times(1)).createIngredient(inputIngredient);
    }

    @Test
    public void createIngredientBadRequest() {
        // ARRANGE: Ingredient that fails validation (empty name)
        Ingredient badIngredient = new Ingredient("",  defaultNutritionalValue);

        // ACT
        ResponseEntity<Ingredient> response = sut.createIngredient(badIngredient);

        // ASSERT: Check 400 Bad Request status
        assertEquals(BAD_REQUEST, response.getStatusCode());

        // ASSERT: Verify save was never called
        verify(mockIngredientService, never()).createIngredient(badIngredient);
    }

    @Test
    public void deleteIngredientCorrect() {
        // ARRANGE: Mock repository to confirm ID 1 exists
        when(mockIngredientService.deleteIngredient(id)).thenReturn(true);

        // ACT
        ResponseEntity<?> response = sut.deleteIngredient(id);

        // ASSERT: Check 204 No Content status and verify delete call
        assertEquals(NO_CONTENT, response.getStatusCode());
        verify(mockIngredientService, times(1)).deleteIngredient(id);
    }

    @Test
    public void deleteIngredientNotFound() {
        // ARRANGE: Mock repository to confirm ID 99 does not exist
        when(mockIngredientService.deleteIngredient(fakeId)).thenReturn(false);

        // ACT
        ResponseEntity<?> response = sut.deleteIngredient(fakeId);

        // ASSERT: Check 404 Not Found status
        assertEquals(NOT_FOUND, response.getStatusCode());

        // ASSERT: Verify delete was not called
        verify(mockIngredientService, never()).deleteIngredient(fakeId);
    }
}
