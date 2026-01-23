package server.service;

import commons.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import server.database.IngredientRepository;
import server.database.RecipeIngredientRepository;
import server.database.RecipeRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class IngredientServiceTest {

    private IngredientService sut;
    private RecipeRepository mockRecipeRepo;
    private IngredientRepository mockIngredientRepo;
    private RecipeIngredientRepository mockRecipeIngredientRepo;
    private RecipeService mockRecipeService;
    private ApplicationEventPublisher eventPublisher;

    private final Long id = 1L;
    private final Long id2 = 2L;

    @BeforeEach
    public void setUp() {
        mockRecipeRepo = mock(RecipeRepository.class);
        mockIngredientRepo = mock(IngredientRepository.class);
        mockRecipeIngredientRepo = mock(RecipeIngredientRepository.class);
        mockRecipeService = mock(RecipeService.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        
        sut = new IngredientService(mockIngredientRepo, mockRecipeIngredientRepo, mockRecipeService, eventPublisher);
    }

    @Test
    public void getAllIngredientsTest() {
        // ARRANGE
        when(mockIngredientRepo.findAll()).thenReturn(List.of(new Ingredient()));

        // ACT
        List<Ingredient> result = sut.getAllIngredients();

        // ASSERT
        assertEquals(1, result.size());
    }

    @Test
    public void getIngredientByIdTest() {
        // ARRANGE
        when(mockIngredientRepo.findById(id)).thenReturn(Optional.of(new Ingredient()));

        // ACT
        Optional<Ingredient> result = sut.getIngredientByID(id);

        // ASSERT
        assertTrue(result.isPresent());
    }

    @Test
    public void getRecipeUsageNumberTest() {
        // ARRANGE
        final int testid = 5;
        when(mockRecipeIngredientRepo.getRecipeUsageNumber(id)).thenReturn(testid);

        // ACT
        int result = sut.getRecipeUsageNumber(id);

        // ASSERT
        assertEquals(testid, result);
    }

    @Test
    public void createIngredientSuccessTest() {
        // ARRANGE
        Ingredient incoming = new Ingredient("Flour", new NutritionalValue(0,0,0));
        when(mockIngredientRepo.save(incoming)).thenReturn(incoming);

        // ACT
        Optional<Ingredient> saved = sut.createIngredient(incoming);

        // ASSERT
        assertTrue(saved.isPresent());
        assertEquals("Flour", saved.get().getName());
    }

    @Test
    public void createIngredientInvalidNameTest() {
        // ARRANGE: Ingredient with empty name
        Ingredient incoming = new Ingredient("", new NutritionalValue(0,0,0));

        // ACT
        Optional<Ingredient> result = sut.createIngredient(incoming);

        // ASSERT
        assertTrue(result.isEmpty());
        // Verify save was NEVER called because validation failed
        verify(mockIngredientRepo, never()).save(any(Ingredient.class));
    }

    @Test
    public void updateIngredientSuccessTest() {
        // ARRANGE
        Ingredient existing = new Ingredient("Sugar", new NutritionalValue(0,0,0));
        existing.setId(id);

        when(mockIngredientRepo.existsById(id)).thenReturn(true);
        when(mockIngredientRepo.findById(id)).thenReturn(Optional.of(existing));
        when(mockIngredientRepo.save(existing)).thenReturn(existing);

        // ACT
        Optional<Ingredient> result = sut.updateIngredient(existing);
        
        // ASSERT
        assertTrue(result.isPresent());
        assertEquals("Sugar", result.get().getName());
    }

    @Test
    public void updateIngredientNotFoundTest() {
        // ARRANGE
        Ingredient incoming = new Ingredient("Sugar", new NutritionalValue(0,0,0));
        incoming.setId(id);

        when(mockIngredientRepo.existsById(id)).thenReturn(false);

        // ACT
        Optional<Ingredient> result = sut.updateIngredient(incoming);

        // ASSERT
        assertTrue(result.isEmpty());
    }

    @Test
    public void updateIngredientInvalidNameTest() {
        // ARRANGE
        Ingredient incoming = new Ingredient("   ", new NutritionalValue(0,0,0));
        incoming.setId(id);

        // Even if it exists, the name is invalid
        when(mockIngredientRepo.existsById(id)).thenReturn(true);

        // ACT
        Optional<Ingredient> result = sut.updateIngredient(incoming);

        // ASSERT
        assertTrue(result.isEmpty());
        verify(mockIngredientRepo, never()).save(any(Ingredient.class));
    }

    @Test
    public void deleteIngredientSuccessTest() {
        // ARRANGE
        when(mockIngredientRepo.existsById(id)).thenReturn(true);

        // ACT
        boolean deleted = sut.deleteIngredient(id);

        // ASSERT
        assertTrue(deleted);

        // Verify proper order of operations:
        // 1. Links are deleted first
        verify(mockRecipeIngredientRepo).deleteByIngredientId(id);
        // 2. Ingredient is deleted second
        verify(mockIngredientRepo).deleteById(id);
    }

    @Test
    public void deleteIngredientNotFoundTest() {
        // ARRANGE
        when(mockIngredientRepo.existsById(id)).thenReturn(false);

        // ACT
        boolean deleted = sut.deleteIngredient(id);

        // ASSERT
        assertFalse(deleted);
        // Ensure no deletion was attempted
        verify(mockRecipeIngredientRepo, never()).deleteByIngredientId(anyLong());
        verify(mockIngredientRepo, never()).deleteById(anyLong());
    }

    @Test
    public void noPublishOnDeleteIngredientNotFoundTest(){
        when(mockIngredientRepo.existsById(id)).thenReturn(false);
        sut.deleteIngredient(id);
        verify(eventPublisher, times(0)).publishEvent(any(IngredientDeletion.class));
    }

    @Test
    public void publishOnDeleteIngredientTest(){
        when(mockIngredientRepo.existsById(id)).thenReturn(true);
        sut.deleteIngredient(id);
        verify(eventPublisher).publishEvent(any(IngredientDeletion.class));
    }

    @Test
    public void publishRecipeUpdateOnDeleteIngredientTest(){
        when(mockIngredientRepo.existsById(id)).thenReturn(true);
        Ingredient ingredient = new Ingredient("Test", new NutritionalValue(0,0,0));
        ingredient.setId(id);
        Recipe recipe = new Recipe();
        recipe.setId(id2);
        recipe.setIngredients(new ArrayList<>(List.of(new RecipeIngredient(recipe, ingredient, 1.0, new RecipeIngredientUnit()))));
        recipe.getIngredients().get(0).setId(new RecipeIngredientKey());
        when(mockRecipeRepo.findAll()).thenReturn(new ArrayList<>(List.of(recipe)));
        when(mockRecipeRepo.findById(id2)).thenReturn(Optional.of(recipe));
        sut.deleteIngredient(id);
        verify(eventPublisher).publishEvent(any(IngredientDeletion.class));
    }

    @Test
    public void publishIngredientAdditionTest(){
        Ingredient ingredient = new Ingredient("Test", new NutritionalValue(0,0,0));
        ingredient.setId(id);
        when(mockIngredientRepo.save(ingredient)).thenReturn(ingredient);
        sut.createIngredient(ingredient);
        verify(eventPublisher).publishEvent(any(IngredientAddition.class));
    }

    @Test
    public void publishIngredientNameChangeTest(){
        Ingredient ingredient = new Ingredient("Test", new NutritionalValue(0,0,0));
        ingredient.setId(id);
        Ingredient ingredient2 = new Ingredient("Test2", new NutritionalValue(0,0,0));
        ingredient2.setId(id);
        when(mockIngredientRepo.save(ingredient2)).thenReturn(ingredient2);
        when(mockIngredientRepo.findById(id)).thenReturn(Optional.of(ingredient));
        sut.updateIngredient(ingredient2);
        verify(eventPublisher).publishEvent(any(IngredientNameUpdate.class));
    }

    @Test
    public void noPublishIngredientNameChangeTest(){
        Ingredient ingredient = new Ingredient("Test", new NutritionalValue(0,0,0));
        ingredient.setId(id);
        Ingredient ingredient2 = new Ingredient("Test", new NutritionalValue(0,0,0));
        ingredient2.setId(id);
        when(mockIngredientRepo.save(ingredient2)).thenReturn(ingredient2);
        when(mockIngredientRepo.findById(id)).thenReturn(Optional.of(ingredient));
        sut.updateIngredient(ingredient2);
        verify(eventPublisher, times(0)).publishEvent(any(IngredientNameUpdate.class));
    }

    @Test
    public void emptyNameNoPublishIngredientNameChangeTest(){
        Ingredient ingredient = new Ingredient("Test", new NutritionalValue(0,0,0));
        ingredient.setId(id);
        Ingredient ingredient2 = new Ingredient("", new NutritionalValue(0,0,0));
        ingredient2.setId(id);
        when(mockIngredientRepo.save(ingredient2)).thenReturn(ingredient2);
        when(mockIngredientRepo.findById(id)).thenReturn(Optional.of(ingredient));
        sut.updateIngredient(ingredient2);
        verify(eventPublisher, times(0)).publishEvent(any(IngredientNameUpdate.class));
    }

    @Test
    public void publishIngredientUpdateOnNutritionalValueChangeTest(){
        Ingredient ingredient = new Ingredient("Test", new NutritionalValue(0,0,0));
        ingredient.setId(id);
        Ingredient ingredient2 = new Ingredient("Test", new NutritionalValue(1,0,0));
        ingredient2.setId(id);
        when(mockIngredientRepo.save(ingredient2)).thenReturn(ingredient2);
        when(mockIngredientRepo.findById(id)).thenReturn(Optional.of(ingredient));
        sut.updateIngredient(ingredient2);
        verify(eventPublisher).publishEvent(any(IngredientUpdate.class));
    }

    @Test 
    public void publishIngredientUpdateOnNameChangeTest(){
        Ingredient ingredient = new Ingredient("Test", new NutritionalValue(0,0,0));
        ingredient.setId(id);
        Ingredient ingredient2 = new Ingredient("Test2", new NutritionalValue(0,0,0));
        ingredient2.setId(id);
        when(mockIngredientRepo.save(ingredient2)).thenReturn(ingredient2);
        when(mockIngredientRepo.findById(id)).thenReturn(Optional.of(ingredient));
        sut.updateIngredient(ingredient2);
        verify(eventPublisher).publishEvent(any(IngredientUpdate.class));
    }
}