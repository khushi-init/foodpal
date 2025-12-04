package server.database;

import commons.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IngredientRepository extends JpaRepository<Ingredient,Long> {
    /**
     * Finds an ingredient by its name.
     * @param name the name of the ingredient
     * @return on optional containing the ingredient if found, otherwise empty
     */
    Optional<Ingredient> findByName(String name);
}
