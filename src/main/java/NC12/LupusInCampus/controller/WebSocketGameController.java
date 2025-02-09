package NC12.LupusInCampus.controller;

import NC12.LupusInCampus.model.dto.game.GameActionDTO;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketGameController {

    @MessageMapping("/joinLobby") // Client invia messaggio qui
    @SendTo("/notifyAll/lobby") // Messaggio viene broadcastato a tutti i client connessi
    public GameActionDTO joinLobby(GameActionDTO message) {
        return new GameActionDTO(message.getPlayer(), "si è unito alla lobby!");
    }

    @MessageMapping("/startGame")
    @SendTo("/notifyAll/game") // Notifica tutti che la partita è iniziata
    public GameActionDTO startGame(GameActionDTO message) {
        return new GameActionDTO(message.getPlayer(), "ha avviato la partita!");
    }

    @MessageMapping("/gameAction")
    @SendTo("/notifyAll/game") // Invia azioni a tutti i giocatori
    public GameActionDTO handleGameAction(GameActionDTO action) {
        return new GameActionDTO(action.getPlayer(), action.getAction());
    }
}
