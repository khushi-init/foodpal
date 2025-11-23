package server.api;

import commons.Ingredient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import server.database.IngredientRepository;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class IngredientControllerTest {

    private IngredientController sut;
    private IngredientRepository mockRepository;
    private Ingredient testIngredient;

    @BeforeEach
    public void setUp(){
        // Setup mock repository and controller
        mockRepository = mock(IngredientRepository.class);
        sut = new IngredientController(mockRepository);

        // Setup test data
        testIngredient = new Ingredient("Sugar");
        testIngredient.setId(1L);
    }

    @Test
    public void getAllIngredientsTest(){
        // ARRANGE: Mock repository to return a list
        List<Ingredient> expectedList = List.of(testIngredient);
        when(mockRepository.findAll()).thenReturn(expectedList);

        // ACT
        List<Ingredient> actualList = sut.getAllIngredients();

        // ASSERT: Check list content and verify repository call
        assertEquals(expectedList, actualList);
        verify(mockRepository, times(1)).findAll();
    }

    @Test
    public void getById_Correct() {
        // ARRANGE: Mock repository to return the ingredient when ID 1 is requested
        when(mockRepository.findById(1L)).thenReturn(Optional.of(testIngredient));

        // ACT
        ResponseEntity<Ingredient> response = sut.getIngredientById(1L);

        // ASSERT: Check 200 OK status
        assertEquals(OK, response.getStatusCode());
        verify(mockRepository, times(1)).findById(1L);
    }

    @Test
    public void getById_NotFound() {
        // ARRANGE: Mock repository to return empty when ID 99 is requested
        when(mockRepository.findById(99L)).thenReturn(Optional.empty());

        // ACT
        ResponseEntity<Ingredient> response = sut.getIngredientById(99L);

        // ASSERT: Check 404 Not Found status
        assertEquals(NOT_FOUND, response.getStatusCode());
        verify(mockRepository, times(1)).findById(99L);
    }

    @Test
    public void createIngredient_Correct() {
        // ARRANGE: Mock repository to return the saved object with a new ID
        Ingredient inputIngredient = new Ingredient("Flour");
        Ingredient savedIngredient = new Ingredient("Flour");
        savedIngredient.setId(2L);
        when(mockRepository.save(inputIngredient)).thenReturn(savedIngredient);

        // ACT
        ResponseEntity<Ingredient> response = sut.createIngredient(inputIngredient);

        // ASSERT: Check 201 Created status
        assertEquals(CREATED, response.getStatusCode());
        verify(mockRepository, times(1)).save(inputIngredient);
    }

    @Test
    public void createIngredient_BadRequest() {
        // ARRANGE: Ingredient that fails validation (empty name)
        Ingredient badIngredient = new Ingredient("");

        // ACT
        ResponseEntity<Ingredient> response = sut.createIngredient(badIngredient);

        // ASSERT: Check 400 Bad Request status
        assertEquals(BAD_REQUEST, response.getStatusCode());

        // ASSERT: Verify save was never called
        verify(mockRepository, never()).save(badIngredient);
    }

    @Test
    public void deleteIngredient_Correct() {
        // ARRANGE: Mock repository to confirm ID 1 exists
        when(mockRepository.existsById(1L)).thenReturn(true);

        // ACT
        ResponseEntity<?> response = sut.deleteIngredient(1L);

        // ASSERT: Check 204 No Content status and verify delete call
        assertEquals(NO_CONTENT, response.getStatusCode());
        verify(mockRepository, times(1)).deleteById(1L);
    }

    @Test
    public void deleteIngredient_NotFound() {
        // ARRANGE: Mock repository to confirm ID 99 does not exist
        when(mockRepository.existsById(99L)).thenReturn(false);

        // ACT
        ResponseEntity<?> response = sut.deleteIngredient(99L);

        // ASSERT: Check 404 Not Found status
        assertEquals(NOT_FOUND, response.getStatusCode());

        // ASSERT: Verify delete was not called
        verify(mockRepository, never()).deleteById(99L);
    }
}
