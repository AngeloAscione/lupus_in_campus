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

    @MessageMapping("/joinLobby") // Client invia messaggio qui
    @SendTo("/topic/lobby") // Messaggio viene broadcastato a tutti i client connessi
    public GameActionDTO joinLobby(GameActionDTO message) {
        return new GameActionDTO(message.getPlayer(), "si è unito alla lobby!");
    }

    @MessageMapping("/startGame")
    @SendTo("/topic/game") // Notifica tutti che la partita è iniziata
    public GameActionDTO startGame(GameActionDTO message) {
        return new GameActionDTO(message.getPlayer(), "ha avviato la partita!");
    }

    @MessageMapping("/gameAction")
    @SendTo("/topic/game") // Invia azioni a tutti i giocatori
    public GameActionDTO handleGameAction(GameActionDTO action) {
        return new GameActionDTO(action.getPlayer(), action.getAction());
    }

    @MessageMapping("/test") // Client invia un messaggio qui
    @SendTo("/topic/responses") // Risposta inviata a tutti i client connessi
    public String handleTestMessage(String message) {
        return "Server ha ricevuto: " + message;
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

    @MessageMapping("/lobby/{lobbyCode}/join")
    public void notifyPlayersInLobby(@DestinationVariable String lobbyCode, LobbyUpdate lobbyUpdate) {
        String topic = "/topic/lobby/" + lobbyCode; // Dynamic topic for this lobby
        System.out.println("New player joined lobby " + lobbyCode + ": " + lobbyUpdate.getPlayer());

        // Broadcast message to all players in the lobby
        messagingTemplate.convertAndSend(topic, lobbyUpdate);
    }

}
