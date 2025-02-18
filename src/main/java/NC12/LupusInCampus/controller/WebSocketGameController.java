package NC12.LupusInCampus.controller;

import NC12.LupusInCampus.model.Game;
import NC12.LupusInCampus.model.Player;
import NC12.LupusInCampus.model.dao.GameDAO;
import NC12.LupusInCampus.model.dao.LobbyDAO;
import NC12.LupusInCampus.model.dto.game.GamePhaseResult;
import NC12.LupusInCampus.model.dto.game.GameUpdateMessage;
import NC12.LupusInCampus.model.dto.lobby.LobbyUpdate;
import NC12.LupusInCampus.model.dto.game.ChatMessage;
import NC12.LupusInCampus.roleFactory.RoleAssignmentFactoryProvider;
import NC12.LupusInCampus.utils.LoggerUtil;
import com.google.gson.JsonObject;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.*;
import java.util.concurrent.*;

import static NC12.LupusInCampus.model.enums.PlayerRole.STUDENT_OUT_COURSE;

@Controller
public class WebSocketGameController {
            
    private static SimpMessagingTemplate messagingTemplate = null;

    private static MessageQueueManager messageQueueManager = new MessageQueueManager();

    private static Map<String, Map<String, Boolean>> running_games = new HashMap<>();

    private static Map<String, Integer> werevolves_vote = new HashMap<>();
    private static Map<String, Map<String, Integer>> voted_players = new HashMap<>();

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

    @MessageMapping("/game/{lobbyCode}/phase-result")
    public synchronized void phaseResult(@DestinationVariable String lobbyCode, GamePhaseResult message) {
        String topic = "/topic/lobby/" + lobbyCode;
        LobbyDAO lobbyDAO = LobbyController.getLobbyDAO();
        GameDAO gameDAO = GameController.getGameDAO();
        Game game = gameDAO.findLastCreatedGameByCreatorId(lobbyDAO.findLobbyByCode(Integer.parseInt(lobbyCode)).getCreatorID());
        List<Player> players = gameDAO.findPartecipantsByIdGame(game.getId());

        if (!running_games.containsKey(lobbyCode)){
            running_games.put(lobbyCode, new HashMap<String, Boolean>());
            Map<String, Boolean> player_alive = running_games.get(lobbyCode);
            for (Player player : players) {
                player_alive.put(player.getNickname(), true);
            }
        }

        if (!werevolves_vote.containsKey(lobbyCode)){
            werevolves_vote.put(lobbyCode,
                    RoleAssignmentFactoryProvider.getFactory(players.size()).getRoles(players.size()).stream()
                            .filter(v -> v.equals(STUDENT_OUT_COURSE)).toList().size());

        }

        if (!running_games.get(lobbyCode).containsKey(message.getVoted_player())){
            messagingTemplate.convertAndSend(topic, new GameUpdateMessage("RETRY_VOTE", "Il giocatore votato non è in partita", lobbyCode));
            return;
        }

        switch (message.getPhase()){
            case "werewolves":{
                Boolean result = doProcessWerewolvesVotes(lobbyCode, message);
                if (result != null && !result.booleanValue()) {
                    messagingTemplate.convertAndSend(topic, new GameUpdateMessage("RETRY_VOTE", "Tutti gli studenti fuori corso devono essere d'accordo", lobbyCode));
                } else if (result != null && result.booleanValue()) {
                    messagingTemplate.convertAndSend(topic, new GameUpdateMessage("NEXT_PHASE", "bodyguard", lobbyCode));
                }
                break;
            }
            case "bodyguard":{
                break;
            }
            case "seer":{
                break;
            }
            case "discussion":{
                break;
            }
        }





        // Variabile statica che conta le votazioni, che si resetta quando si conferma il voto e che deve adattarsi al numero dei lupi presenti in gioco
        // Se var == numero voti aspettati, allora voto effettivo quindi conferma
        // altrimenti nulla, aspetta
        // ricorda di creare la variabile atomica o synchronized
        // metodo flip della vita

        // Prima cosa, prendere lista giocatori nella lobby con lobbyCode

        LoggerUtil.logInfo("Ricevuti da client " + lobbyCode + " " + message.toString());
    }

    private Boolean doProcessWerewolvesVotes(String lobbyCode, GamePhaseResult message) {

        boolean flag = false;
        if (werevolves_vote.containsKey(lobbyCode)){
            if (werevolves_vote.get(lobbyCode) == 1)
                flag = true;
        }

        if (!voted_players.containsKey(lobbyCode)){
            voted_players.put(lobbyCode, new HashMap<String, Integer>());
        }

        Map<String, Integer> votes = voted_players.get(lobbyCode);
        if (!votes.containsKey(message.getVoted_player())){
            votes.put(message.getVoted_player(), 1);
        } else {
           Integer i = ((Integer) votes.get(message.getVoted_player()));
           i++;
           votes.put(message.getVoted_player(), i);
        }

        if (flag){
            if (votes.keySet().size() == 1){
                running_games.get(lobbyCode).put(message.getVoted_player(), false);
                return true;
            } else {
                return false;
            }
        }

        werevolves_vote.put(lobbyCode, werevolves_vote.get(lobbyCode) - 1);
        return null;
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
