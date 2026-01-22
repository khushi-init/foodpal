package server.stomp;

import commons.*;
import server.service.IngredientService;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class IngredientEventListener {
    private final SimpMessagingTemplate messaging;
    private final IngredientService ingredientService;

    /**
     * The Ingredient event listener sends updates via socket when informed by the RecipeService
     * @param messaging - The messaging template for socket communication
     * @param ingredientService - The ingredientService instance of the server
     */
    public IngredientEventListener(SimpMessagingTemplate messaging, IngredientService ingredientService) {
        this.messaging = messaging;
        this.ingredientService = ingredientService;
    }

    /**
     * Sends an updated ingredient name to all clients subsribed to name changes
     * @param update IngredientNameUpdate containing the id and new name of the ingredient
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishIngredientNameChange(IngredientNameUpdate update) {
        System.out.println("Sending changed ingredient name: " + update.name());
        messaging.convertAndSend("/updates/ingredient-name", update);
    }

    /**
     * Sends an updated ingredient to all clients subscribed to that specific ingredient.
     * @param update The updated ingredient
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishIngredientChange(IngredientUpdate update){
        System.out.println("Sending updated ingredient with id " + update.id());
        messaging.convertAndSend("/updates/ingredient/" + Long.toString(update.ingredient().getId()), update);
    }

    /**
     * Sends a new ingredient to all clients subscribed to ingredient additions
     * @param update IngredientAddition containing the new ingredient
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishIngredientAddition(IngredientAddition update){
        System.out.println("Sending added ingredient with id " + update.ingredient().getId());
        messaging.convertAndSend("/updates/ingredient-addition", update);
    }

    /**
     * Sends the id of a deleted ingredient to all clients subscribed to ingredient deletions
     * For mysterious reasons, the ingredient doesn't actually get deleted after first calling deleteIngredient on IngredientService
     * So we call it again in a new transaction to be sure that the ingredient is deleted.
     * @param update IngredientDeletion containing the ID of the deleted ingredient
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publishIngredientDeletion(IngredientDeletion update){
        ingredientService.deleteIngredient(update.id());
        System.out.println("Sending deleted ingredient with id " + update.id());
        messaging.convertAndSend("/updates/ingredient-deletion", update);
    }

    /**
     * Sends the id and new recipe count of an ingredient to all clients subscribed to a change in recipe count
     * @param update IngredientLinkedRecipesUpdate containing the ID and new recipe count of the ingredient
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishIngredientLinkedRecipesUpdate(IngredientLinkedRecipesUpdate update){
        System.out.println("Sending linked recipes change for ingredient "+ update.ingredientId());
        messaging.convertAndSend("/updates/ingredient-linked-recipes/"+update.ingredientId(), update);
    }
}
