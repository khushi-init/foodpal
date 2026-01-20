package client.data;

import client.popups.ServerDisconnectPopupCtrl;
import client.scenes.ErrorCtrl;
import client.utils.StrictPopupService;
import com.google.inject.Inject;
import javafx.application.Platform;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.messaging.converter.JacksonJsonMessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class WebSocketManager {

    private final int subscriptionTime = 10;
    private ErrorCtrl errorCtrl;
    private StompSession session;
    private final String url = "ws://localhost:8080/autoupdates-websocket";

    private final StrictPopupService<ServerDisconnectPopupCtrl> serverDisconnectPopup;

    private final Map<String, Subsubscription<?>> activeSubscriptions = new ConcurrentHashMap<>();

    @Inject
    private WebSocketManager(ErrorCtrl errors, StrictPopupService<ServerDisconnectPopupCtrl> serverDisconnectPopup){
        connect();
        this.errorCtrl = errors;
        this.serverDisconnectPopup = serverDisconnectPopup;
    }

    // /!\ WARNING /!\ THE DOCUMENTATION USED TO WRITE THE FOLLOWING CODE WAS VERY LIMITED. I KINDA KNOW WHAT THIS DOES.
    // AI WAS ALSO USED TO SUMMARIZE DOCUMENTATION AND GENERATE EXAMPLES
    // THIS CODE IS A TICKING TIME BOMB AND MUST BE THOROUGHLY CHECKED AND TESTED

    /**
     * Subscribes to a websocket update topic and adds it to activeSubscriptions
     * @param <T> The type of updatePackets we're managing
     * @param topic The socket path
     * @param type The Class type to convert from JSON into
     * @param onMessage The callback function to run when data arrives
     */
    public <T> void subscribe(String topic, Class<T> type, Consumer<T> onMessage) {
        Subsubscription<T> details = new Subsubscription<>(
                type, onMessage, new AtomicReference<>()
        );
        activeSubscriptions.put(topic, details);

        // Perform connection instantly if connected
        if (session != null && session.isConnected()) {
            details.stompSubscription().set(performSubscription(topic, details));
        }
    }

    /**
     * Given a path, performs a websocket subscription and returns a StompSession.Subscription object
     * @param topic - Topic path
     * @param details - The Subsubscription containing the desired subscription details
     * @return - A Subscription object, should the connection be successful
     * @param <T> - The type of updatePackets used in the Subsubscription
     */
    public <T> StompSession.Subscription performSubscription(String topic, Subsubscription<T> details) {
        System.out.println("Subscribed to " + topic);
        return session.subscribe(topic, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return details.type();
            }

            @Override
            public void handleFrame(StompHeaders headers, @Nullable Object payload) {
                Platform.runLater(() -> details.handler().accept(details.type().cast(payload)));
            }
        });
    }

    /**
     * Given a path, unsubscribes from that topic and removes the subscription from the active connection list
     * @param topic - Topic to unsubscribe from
     */
    public void unsubscribe(String topic) {
        Subsubscription<?> details = activeSubscriptions.remove(topic);

        if (details != null) {
            StompSession.Subscription sub = details.stompSubscription().get();
            if (sub != null && session != null && session.isConnected()) {
                sub.unsubscribe();
                System.out.println("Unsubscribed from " + topic);
            }
        }
    }

    private void connect() {
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new JacksonJsonMessageConverter());

        stompClient.connectAsync(url,new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(@NonNull StompSession sesh, @NonNull StompHeaders headers) {
                session = sesh;
                System.out.println("Socket Connection Established!");
                activeSubscriptions.forEach((topic, details) -> {
                    details.stompSubscription().set(performSubscription(topic, details));
                });
                serverDisconnectPopup.hideStrictPopup();
            }

            @Override
            public void handleException(@NonNull StompSession sesh, StompCommand command,
                                        @NonNull StompHeaders headers, byte @NonNull [] p, @NonNull Throwable t) {
                errorCtrl.showGenericError(Arrays.toString(t.getStackTrace()));
            }

            @Override
            public void handleTransportError(StompSession sesh, Throwable e) {
                // TODO We need to limit the amount of reconnections and resubscribe
                System.out.println("Connection lost. Retrying in 5 seconds...");
                if (!serverDisconnectPopup.isShowing()) {
                    serverDisconnectPopup.showStrictPopup(ServerDisconnectPopupCtrl.class, "client", "modules", "ServerDisconnectPopup.fxml");
                }
                // Schedule a reconnect
                Executors.newSingleThreadScheduledExecutor()
                        .schedule(() -> connect(), subscriptionTime/2, TimeUnit.SECONDS);
            }
        });
    }
}
