package NC12.LupusInCampus.controller;

import NC12.LupusInCampus.model.dto.game.GameActionDTO;
import NC12.LupusInCampus.model.dto.lobby.LobbyUpdate;
import NC12.LupusInCampus.model.dto.game.ChatMessage;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class WebSocketGameController {
            
    private final SimpMessagingTemplate messagingTemplate;

    WebSocketGameController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
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

}
