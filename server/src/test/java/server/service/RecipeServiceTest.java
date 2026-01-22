package server.service;

import commons.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import server.database.IngredientRepository;
import server.database.RecipeIngredientRepository;
import server.database.RecipeRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RecipeServiceTest {
    @Mock
    private RecipeRepository mockRecipeRepo;

    @Mock
    private IngredientRepository mockIngredientRepo;

    @Mock
    private RecipeIngredientRepository mockRecipeIngredientRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private RecipeService sut;

    private final Long id = 1L;
    private final Long id2 = 5L;


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
        Recipe incoming = new Recipe("New Recipe",  new ArrayList<>(), new ArrayList<>());
        when(mockRecipeRepo.save(any(Recipe.class))).thenReturn(incoming);

        Recipe saved = sut.createRecipe(incoming);

        assertEquals("New Recipe", saved.getName());
    }

    @Test
    public void updateRecipeTest() {
        Recipe existing = new Recipe();
        existing.setIngredients(new ArrayList<>());
        when(mockRecipeRepo.findById(id)).thenReturn(Optional.of(existing));
        when(mockRecipeRepo.save(any(Recipe.class))).thenReturn(existing);

        Optional<Recipe> result = sut.updateRecipe(id, new Recipe("Updated", new ArrayList<>(), new ArrayList<>()));

        assertTrue(result.isPresent());
    }

    /**
     * When a recipe name change occurs, verify that the server publishes the change
     */
    @Test
    public void serverPublishesRecipeNameChange() {
        Recipe existing = new Recipe("Old Name", new ArrayList<>(), new ArrayList<>());
        Recipe incoming = new Recipe("New name", new ArrayList<>(), new ArrayList<>());

        when(mockRecipeRepo.findById(id)).thenReturn(Optional.of(existing));
        when(mockRecipeRepo.save(any(Recipe.class))).thenReturn(existing);

        sut.updateRecipe(id, incoming);

        verify(eventPublisher).publishEvent(any(TitleUpdate.class));
    }

    @Test
    public void testServerWontPublishSameName() {
        Recipe existing = new Recipe("Old Name", new ArrayList<>(), new ArrayList<>());
        Recipe incoming = new Recipe("Old Name", new ArrayList<>(), new ArrayList<>());

        when(mockRecipeRepo.findById(id)).thenReturn(Optional.of(existing));
        when(mockRecipeRepo.save(any(Recipe.class))).thenReturn(existing);

        sut.updateRecipe(id, incoming);

        verify(eventPublisher, times(0)).publishEvent(any(TitleUpdate.class));
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

    @Test
    public void serverPublishesRecipeUpdateOnIngredientRemovalTest(){
        Ingredient ingredient = new Ingredient("Test", new NutritionalValue(0,0,0));
        ingredient.setId(id2);
        Recipe recipe = new Recipe();
        recipe.setIngredients(new ArrayList<>(List.of(new RecipeIngredient(recipe, ingredient, 1.0, new RecipeIngredientUnit()))));
        recipe.getIngredients().get(0).setId(new RecipeIngredientKey());

        when(mockRecipeRepo.findById(id)).thenReturn(Optional.of(recipe));

        sut.removeIngredientFromRecipe(id, id2);
        verify(eventPublisher).publishEvent(any(RecipeUpdate.class));
    }

    @Test
    public void serverPublishesIngredientLinkedRecipesUpdateOnIngredientRemovalTest(){
        Ingredient ingredient = new Ingredient("Test", new NutritionalValue(0,0,0));
        ingredient.setId(id2);
        Recipe recipe = new Recipe();
        recipe.setIngredients(new ArrayList<>(List.of(new RecipeIngredient(recipe, ingredient, 1.0, new RecipeIngredientUnit()))));
        recipe.getIngredients().get(0).setId(new RecipeIngredientKey());

        when(mockRecipeRepo.findById(id)).thenReturn(Optional.of(recipe));

        sut.removeIngredientFromRecipe(id, id2);
        verify(eventPublisher).publishEvent(any(IngredientLinkedRecipesUpdate.class));
    }

    @Test
    public void serverPublishesLinkedRecipesUpdateOnIngredientAdditionTest(){
        Ingredient ingredient = new Ingredient("Test", new NutritionalValue(0,0,0));
        ingredient.setId(id2);
        Recipe recipe = new Recipe();
        recipe.setId(id);
        recipe.setIngredients(new ArrayList<>());

        Recipe recipe2 = new Recipe();
        recipe2.setId(id);
        recipe2.setIngredients(new ArrayList<>(List.of(new RecipeIngredient(recipe2, ingredient, 1.0, new RecipeIngredientUnit()))));
        recipe2.getIngredients().get(0).setId(new RecipeIngredientKey());

        when(mockRecipeRepo.findById(id)).thenReturn(Optional.of(recipe));

        sut.updateRecipe(id, recipe2);
        verify(eventPublisher).publishEvent(any(IngredientLinkedRecipesUpdate.class));
    }

    @Test
    public void serverPublishesRecipeUpdateOnIngredientAdditionTest(){
        Ingredient ingredient = new Ingredient("Test", new NutritionalValue(0,0,0));
        ingredient.setId(id2);
        Recipe recipe = new Recipe();
        recipe.setId(id);
        recipe.setIngredients(new ArrayList<>());

        Recipe recipe2 = new Recipe();
        recipe2.setId(id);
        recipe2.setIngredients(new ArrayList<>(List.of(new RecipeIngredient(recipe2, ingredient, 1.0, new RecipeIngredientUnit()))));
        recipe2.getIngredients().get(0).setId(new RecipeIngredientKey());

        when(mockRecipeRepo.findById(id)).thenReturn(Optional.of(recipe));

        sut.updateRecipe(id, recipe2);
        verify(eventPublisher).publishEvent(any(RecipeUpdate.class));
    }

    @Test
    public void serverPublishesRecipeAdditionTest(){
        Recipe incoming = new Recipe("New Recipe",  new ArrayList<>(), new ArrayList<>());
        when(mockRecipeRepo.save(any(Recipe.class))).thenReturn(incoming);
        sut.createRecipe(incoming);
        verify(eventPublisher).publishEvent(any(RecipeAddition.class));
    }

    @Test
    public void serverPublishesRecipeDeletionTest(){
        when(mockRecipeRepo.existsById(id)).thenReturn(true);
        sut.deleteRecipe(id);
        verify(eventPublisher).publishEvent(any(RecipeDeletion.class));
    }

    @Test 
    public void updateRecipesWithChangedIngredientNoRecipesTest(){
        when(mockRecipeRepo.findAll()).thenReturn(new ArrayList<>());
        sut.updateRecipesWithChangedIngredient(new Ingredient("Test", new NutritionalValue(0,0,0)));
        verify(eventPublisher, times(0)).publishEvent(any(RecipeUpdate.class));
    }

    @Test 
    public void updateRecipesWithChangedIngredientPublishesUpdateTest(){
        Ingredient ingredient = new Ingredient("Test", new NutritionalValue(0,0,0));
        ingredient.setId(id2);

        Recipe recipe = new Recipe();
        recipe.setId(id);
        recipe.setIngredients(new ArrayList<>(List.of(new RecipeIngredient(recipe, ingredient, 1.0, new RecipeIngredientUnit()))));
        recipe.getIngredients().get(0).setId(new RecipeIngredientKey());
        
        when(mockRecipeRepo.findAll()).thenReturn(new ArrayList<Recipe>(List.of(recipe)));
        sut.updateRecipesWithChangedIngredient(ingredient);
        verify(eventPublisher, times(1)).publishEvent(any(RecipeUpdate.class));
    }

    @Test 
    public void updateRecipesWithChangedIngredientPublishesNoUpdateTest(){
        Ingredient ingredient = new Ingredient("Test", new NutritionalValue(0,0,0));
        ingredient.setId(id2);

        Recipe recipe = new Recipe();
        recipe.setId(id);
        recipe.setIngredients(new ArrayList<>(List.of(new RecipeIngredient(recipe, ingredient, 1.0, new RecipeIngredientUnit()))));
        recipe.getIngredients().get(0).setId(new RecipeIngredientKey());
        
        when(mockRecipeRepo.findAll()).thenReturn(new ArrayList<Recipe>(List.of(recipe)));
        sut.updateRecipesWithChangedIngredient(new Ingredient("Test", new NutritionalValue(0,0,0)));
        verify(eventPublisher, times(0)).publishEvent(any(RecipeUpdate.class));
    }

    @Test
    public void testRemoveDeletedIngredients() {
        // ARRANGE
        Recipe existing = new Recipe();
        existing.setIngredients(new ArrayList<>());

        existing.getIngredients().add(new RecipeIngredient(existing, new Ingredient("Salt", null), 1.0, RecipeIngredientUnit.fromUnit(FormalUnit.GRAM)));
        existing.getIngredients().get(0).setId(new RecipeIngredientKey());
        existing.getIngredients().get(0).getIngredient().setId(1L);

        existing.getIngredients().add(new RecipeIngredient(existing, new Ingredient("Pepper", null), 2.0, RecipeIngredientUnit.fromUnit(FormalUnit.GRAM)));
        existing.getIngredients().get(1).setId(new RecipeIngredientKey());
        existing.getIngredients().get(0).getIngredient().setId(2L);

        Recipe incoming = new Recipe("Updated", new ArrayList<>(), new ArrayList<>());
        incoming.getIngredients().add(new RecipeIngredient(incoming, new Ingredient("Salt", null), 1.0, RecipeIngredientUnit.fromUnit(FormalUnit.GRAM)));
        existing.getIngredients().get(1).setId(new RecipeIngredientKey());
        existing.getIngredients().get(0).getIngredient().setId(0L);

        when(mockRecipeRepo.findById(id)).thenReturn(Optional.of(existing));
        when(mockRecipeRepo.save(any(Recipe.class))).thenAnswer(inv -> inv.getArgument(0));

        // ACT
        Optional<Recipe> result = sut.updateRecipe(id, incoming);

        // ASSERT
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getIngredients().size());
        assertEquals("Salt",
                result.get().getIngredients().getFirst().getIngredient().getName());
    }


    @Test
    public void testGetIngredientName() {
        RecipeIngredient ri =
                new RecipeIngredient(null, new Ingredient("   ", null), 1.0, RecipeIngredientUnit.fromUnit(FormalUnit.GRAM));
        String result = sut.getIngredientName(ri);
        assertNull(result);
    }

    @Test
    public void updateRecipeNotFoundTest() {
        //when recipe does not exist
        when(mockRecipeRepo.findById(id)).thenReturn(Optional.empty());

        Optional<Recipe> result = sut.updateRecipe(id, new Recipe("Updated", new ArrayList<>(), new ArrayList<>())
        );

        assertFalse(result.isPresent());
        verify(mockRecipeRepo, never()).save(any(Recipe.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

}
