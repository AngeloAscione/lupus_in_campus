package NC12.LupusInCampus.controller;

import NC12.LupusInCampus.model.dto.game.GameUpdateMessage;
import NC12.LupusInCampus.model.dto.lobby.LobbyUpdate;
import NC12.LupusInCampus.model.dto.game.ChatMessage;
import NC12.LupusInCampus.utils.LoggerUtil;
import com.google.gson.JsonObject;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

@Controller
public class WebSocketGameController {
            
    private static SimpMessagingTemplate messagingTemplate = null;

    private static MessageQueueManager messageQueueManager = new MessageQueueManager();

    WebSocketGameController(SimpMessagingTemplate template) {
        messagingTemplate = template;
    }

    // Handle messages sent to a specific lobby chat
    @MessageMapping("/chat/{lobbyCode}")
    public void sendMessageToLobbyChat(@DestinationVariable String lobbyCode, ChatMessage message) {
        String topic = "/topic/chat/" + lobbyCode; // Dynamic topic
        messagingTemplate.convertAndSend(topic, message);
    }

    // Handle lobby-specific announcements
    @MessageMapping("/lobby/{lobbyCode}")
    public void sendLobbyUpdate(@DestinationVariable String lobbyCode, LobbyUpdate update) {
        String topic = "/topic/lobby/" + lobbyCode; // Dynamic topic
        messagingTemplate.convertAndSend(topic, update);
    }

    public static void assignRolesToPlayers(String nickname, GameUpdateMessage message) {
        String topic = "/topic/lobby/" + message.getLobbyCode() + "/" + nickname; // Dynamic topic
        messagingTemplate.convertAndSend(topic, message);
    }

    public static void notifyGameUpdate(GameUpdateMessage message) {
        String lobbyCode = message.getLobbyCode();
        messagingTemplate.convertAndSend("/topic/lobby/"+lobbyCode, message);
    }


    public static void addToQueue(String playerName, GameUpdateMessage message) {
        messageQueueManager.sendMessageWithRetry(playerName, message);
    }

    @MessageMapping("/ack")
    public void receiveAcknowledgment(String nickname) {
        LoggerUtil.logInfo("Received acknowledgment from " + nickname);
        messageQueueManager.acknowledgeMessage(nickname);
    }


    public static class MessageQueueManager {

        // Map to track messages waiting for acknowledgment
        private final Map<String, GameUpdateMessage> pendingMessages = new ConcurrentHashMap<>();
        // Executor for retrying messages
        private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        public void sendMessageWithRetry(String nickname, GameUpdateMessage message) {
            pendingMessages.putIfAbsent(nickname, message);

            sendNextMessage(nickname);
        }

        private void sendNextMessage(String playerName) {
            GameUpdateMessage message = pendingMessages.get(playerName);
            if (message == null) return;

            LoggerUtil.logInfo("Sending next message to lobby " + message.getLobbyCode() + " for " + playerName);

            assignRolesToPlayers(playerName, message);

            // Schedule a retry if no acknowledgment is received
            scheduler.schedule(() -> checkAcknowledgment(playerName), 2, TimeUnit.SECONDS);
        }

        public void acknowledgeMessage(String playerName) {

            JsonObject obj = new JsonObject();

            pendingMessages.remove(playerName);// Remove the acknowledged message
            LoggerUtil.logInfo("Acknowledging next message to lobby for " + playerName);
        }

        private void checkAcknowledgment(String playerName) {
            GameUpdateMessage message = pendingMessages.get(playerName);
            if (message != null) {
                LoggerUtil.logInfo("Sending the message again for" + playerName);
                sendNextMessage(playerName);
            }
        }
    }

}
