package commons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ShoppingListTest {

    ShoppingList shoppingList;
    ShoppingList shoppingList2;
    List<String> ingredients;

    @BeforeEach
    public void setUp() {
        ingredients = new ArrayList<>(
                List.of("Box of Cereal", "Sugar (50g)", "Milk (1.5L)")
        );

        shoppingList = new ShoppingList();
        shoppingList2 = new ShoppingList();

        shoppingList.addIngredient("Box of Cereal");
        shoppingList.addIngredient("Sugar (50g)");
        shoppingList.addIngredient("Milk (1.5L)");

        shoppingList2.addIngredient("Box of Cereal");
        shoppingList2.addIngredient("Sugar (50g)");
        shoppingList2.addIngredient("Milk (1.5L)");
    }

    @Test
    public void equalsTest() {
        assertEquals(shoppingList, shoppingList2);
    }

    @Test
    public void notEqualsTest() {
        shoppingList.addIngredient("Francisco (1)");
        assertNotEquals(shoppingList, shoppingList2);
    }

    @Test
    public void hashcodeTest() {
        int hashcode1 = shoppingList.hashCode();
        int hashcode2 = shoppingList2.hashCode();
        assertEquals(hashcode1, hashcode2);
    }

    @Test
    public void resetListTest() {
        shoppingList.resetList();
        assertEquals(List.of(), shoppingList.getIngredients());
    }

    @Test
    public void toMarkdownTest() {
        String actual = shoppingList.toMarkdown();

        assertTrue(actual.startsWith("# Shopping List"));
        assertTrue(actual.contains("## Ingredients"));
        assertTrue(actual.contains("* Box of Cereal"));
        assertTrue(actual.contains("* Sugar (50g)"));
        assertTrue(actual.contains("* Milk (1.5L)"));
    }
}
