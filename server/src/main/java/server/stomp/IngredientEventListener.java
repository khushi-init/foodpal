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
     * The Recipe event listener sends updates via socket when informed by the RecipeService
     * @param messaging - The messaging template for socket communication
     * @param 
     */
    public IngredientEventListener(SimpMessagingTemplate messaging, IngredientService ingredientService) {
        this.messaging = messaging;
        this.ingredientService = ingredientService;
    }

    /**
     * If a recipe name update event is published and the database transaction succeeds, the update is forwarded to the socket.
     * Note: Using TitleUpdate for both event publishing and socket communication is not perfect, but should be fine withing the
     * scope of the project
     * @param update - The title-update object
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishIngredientNameChange(IngredientNameUpdate update) {
        System.out.println("Sending changed ingredient name: " + update.name());
        messaging.convertAndSend("/updates/ingredient-name", update);
    }

    /**
     * Sends an updated recipe to all clients subscribed to that specific recipe.
     * @param update The updated recipe
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishIngredientChange(IngredientUpdate update){
        System.out.println("Sending updated ingredient with id " + update.id());
        messaging.convertAndSend("/updates/ingredient/" + Long.toString(update.ingredient().getId()), update);
    }

    /**
     * Sends a new recipe to all clients subscribed to recipe additions
     * @param update RecipeAddition containing the new recipe
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishIngredientAddition(IngredientAddition update){
        System.out.println("Sending added ingredient with id " + update.ingredient().getId());
        messaging.convertAndSend("/updates/ingredient-addition", update);
    }

    /**
     * Sends the id of a deleted recipe to all clients subscribed to recipe deletions
     * @param update RecipeDeletion containing the ID of the deleted recipe
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publishIngredientDeletion(IngredientDeletion update){
        ingredientService.deleteIngredient(update.id());
        System.out.println("Sending deleted ingredient with id " + update.id());
        messaging.convertAndSend("/updates/ingredient-deletion", update);
    }
}
