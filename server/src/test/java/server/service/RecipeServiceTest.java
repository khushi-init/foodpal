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
    private Long id = 1L;
    private Long id2 = 5L;

    @BeforeEach
    public void setUp() {
        mockRecipeRepo = mock(RecipeRepository.class);
        mockIngredientRepo = mock(IngredientRepository.class);
        sut = new RecipeService(mockRecipeRepo, mockIngredientRepo, null);
    }

    @Test
    public void getAllRecipesTest() {
        when(mockRecipeRepo.findAll()).thenReturn(List.of(new Recipe()));

        List<Recipe> result = sut.getAllRecipes();

        assertEquals(1, result.size());
    }

    @Test
    public void getRecipeByIdTest() {
        when(mockRecipeRepo.findById(id)).thenReturn(Optional.of(new Recipe()));

        Optional<Recipe> result = sut.getRecipeById(id);

        assertTrue(result.isPresent());
    }

    @Test
    public void getRecipeMarkdownBytesTest() {
        Recipe r = new Recipe("Test", new ArrayList<>(), new ArrayList<>());

        when(mockRecipeRepo.findById(id)).thenReturn(Optional.of(r));

        Optional<byte[]> result = sut.getRecipeMarkdownBytes(id);

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
        when(mockRecipeRepo.findById(id)).thenReturn(Optional.of(existing));
        when(mockRecipeRepo.save(any(Recipe.class))).thenReturn(existing);

        Optional<Recipe> result = sut.updateRecipe(id, new Recipe("Updated", null, null));

        assertTrue(result.isPresent());
    }

    @Test
    public void deleteRecipeTest() {
        when(mockRecipeRepo.existsById(id)).thenReturn(true);

        boolean deleted = sut.deleteRecipe(id);

        assertTrue(deleted);
    }

    @Test
    public void removeIngredientFromRecipeTest() {
        // ARRANGE: Create a recipe with an initialized empty list
        Recipe recipe = new Recipe();
        recipe.setIngredients(new ArrayList<>());

        when(mockRecipeRepo.findById(id)).thenReturn(Optional.of(recipe));

        // ACT
        boolean result = sut.removeIngredientFromRecipe(id, id2);

        // ASSERT: Should be false because the list was empty
        assertFalse(result);
    }
}
