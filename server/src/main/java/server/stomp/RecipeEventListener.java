package server.stomp;

import commons.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class RecipeEventListener {
    private final SimpMessagingTemplate messaging;

    /**
     * The Recipe event listener sends updates via socket when informed by the RecipeService
     * @param messaging - The messaging template for socket communication
     */
    public RecipeEventListener(SimpMessagingTemplate messaging) {
        this.messaging = messaging;
    }

    /**
     * If a recipe name update event is published and the database transaction succeeds, the update is forwarded to the socket.
     * Note: Using TitleUpdate for both event publishing and socket communication is not perfect, but should be fine withing the
     * scope of the project
     * @param update - The title-update object
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishRecipeNameChange(TitleUpdate update) {
        messaging.convertAndSend("/updates/title", update);
    }

    /**
     * Sends an updated recipe to all clients subscribed to that specific recipe.
     * @param update The updated recipe
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishRecipeChange(RecipeUpdate update){
        System.out.println("Sending updated recipe with id " + update.id());
        messaging.convertAndSend("/updates/recipe/" + Long.toString(update.recipe().getId()), update);
    }

    /**
     * Sends a new recipe to all clients subscribed to recipe additions
     * @param update RecipeAddition containing the new recipe
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishRecipeAddition(RecipeAddition update){
        System.out.println("Sending added recipe with id " + update.recipe().getId());
        messaging.convertAndSend("/updates/recipe-addition", update);
    }

    /**
     * Sends the id of a deleted recipe to all clients subscribed to recipe deletions
     * @param update RecipeDeletion containing the ID of the deleted recipe
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishRecipeDeletion(RecipeDeletion update){
        System.out.println("Sending deleted recipe with id " + update.id());
        messaging.convertAndSend("/updates/recipe-deletion", update);
    }
}
