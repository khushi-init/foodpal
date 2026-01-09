package server.stomp;

import commons.TitleUpdate;
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
}
