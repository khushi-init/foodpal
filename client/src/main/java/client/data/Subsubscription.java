package client.data;

import org.springframework.messaging.simp.stomp.StompSession;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public record Subsubscription<T>(Class<T> type, Consumer<T> handler, AtomicReference<StompSession.Subscription> stompSubscription) {
}
