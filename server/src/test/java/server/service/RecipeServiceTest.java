package server.service;

import commons.Recipe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.database.IngredientRepository;
import server.database.RecipeRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RecipeServiceTest {

    private RecipeService sut;
    private RecipeRepository mockRecipeRepo;
    private IngredientRepository mockIngredientRepo;

    @BeforeEach
    public void setUp() {
        mockRecipeRepo = mock(RecipeRepository.class);
        mockIngredientRepo = mock(IngredientRepository.class);
        sut = new RecipeService(mockRecipeRepo, mockIngredientRepo);
    }

    @Test
    public void getAllRecipesTest() {
        when(mockRecipeRepo.findAll()).thenReturn(List.of(new Recipe()));

        List<Recipe> result = sut.getAllRecipes();

        assertEquals(1, result.size());
    }

    @Test
    public void getRecipeByIdTest() {
        when(mockRecipeRepo.findById(1L)).thenReturn(Optional.of(new Recipe()));

        Optional<Recipe> result = sut.getRecipeById(1L);

        assertTrue(result.isPresent());
    }

    @Test
    public void getRecipeMarkdownBytesTest() {
        Recipe r = new Recipe("Test", new ArrayList<>(), new ArrayList<>());

        when(mockRecipeRepo.findById(1L)).thenReturn(Optional.of(r));

        Optional<byte[]> result = sut.getRecipeMarkdownBytes(1L);

        assertTrue(result.isPresent());
    }

    @Test
    public void createRecipeTest() {
        Recipe incoming = new Recipe("New Recipe", null, null);
        when(mockRecipeRepo.save(any(Recipe.class))).thenReturn(incoming);

        Recipe saved = sut.createRecipe(incoming);

        assertEquals("New Recipe", saved.getName());
    }

    @Test
    public void updateRecipeTest() {
        Recipe existing = new Recipe();
        when(mockRecipeRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(mockRecipeRepo.save(any(Recipe.class))).thenReturn(existing);

        Optional<Recipe> result = sut.updateRecipe(1L, new Recipe("Updated", null, null));

        assertTrue(result.isPresent());
    }

    @Test
    public void deleteRecipeTest() {
        when(mockRecipeRepo.existsById(1L)).thenReturn(true);

        boolean deleted = sut.deleteRecipe(1L);

        assertTrue(deleted);
    }

    @Test
    public void removeIngredientFromRecipeTest() {
        // ARRANGE: Create a recipe with an initialized empty list
        Recipe recipe = new Recipe();
        recipe.setIngredients(new ArrayList<>());

        when(mockRecipeRepo.findById(1L)).thenReturn(Optional.of(recipe));

        // ACT
        boolean result = sut.removeIngredientFromRecipe(1L, 5L);

        // ASSERT: Should be false because the list was empty
        assertFalse(result);
    }
}
