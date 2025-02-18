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
    private static Map<String, List<Player>> games_players = new HashMap<>();

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
        List<Player> players;
        if (!games_players.containsKey(lobbyCode)) {
            games_players.put(lobbyCode, gameDAO.findPartecipantsByIdGame(game.getId()));
        }
        players = games_players.get(lobbyCode);


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

        GameUpdateMessage gameUpdateMessagecol = null;
        if (!running_games.get(lobbyCode).containsKey(message.getVoted_player())){
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("game_phase", message.getPhase());
            jsonObject.addProperty("message", "Il giocatore votato non è in partita");
            gameUpdateMessagecol = new GameUpdateMessage("RETRY_VOTE", jsonObject.getAsString(), lobbyCode);
            message.setPhase("ERROR");
        }

        switch (message.getPhase()){
            case "werewolves":{
                Boolean result = doProcessWerewolvesVotes(lobbyCode, message);
                if (result != null && !result.booleanValue()) {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("game_phase", message.getPhase());
                    jsonObject.addProperty("message", "Tutti gli studenti fuori corso devono essere d'accordo");
                    gameUpdateMessagecol = new GameUpdateMessage("RETRY_VOTE", jsonObject.getAsString(), lobbyCode);
                } else if (result != null && result.booleanValue()) {
                    gameUpdateMessagecol = new GameUpdateMessage("NEXT_PHASE", "bodyguard", lobbyCode);
                }
                break;
            }
            case "bodyguard":{
                doProcessBodyguardVote(lobbyCode, message);
                gameUpdateMessagecol = new GameUpdateMessage("NEXT_PHASE", "seer", lobbyCode);
                break;
            }
            case "seer":{
                Boolean result = doProcessSeerVote(lobbyCode, message, players);
                String result_message;
                if (result != null && result.booleanValue()) {
                    result_message = "Hai trovato uno studente fuori corso";
                } else {
                    result_message = "Non hai trovato uno studente fuori corso";
                }
                String seerNickname = players.stream().filter(p -> p.getRole().equals("Ricercatore")).findFirst().get().getNickname();
                sendMessageToPlayer(seerNickname, new GameUpdateMessage("SEER_RESULT", result_message, lobbyCode));
                gameUpdateMessagecol = new GameUpdateMessage("NEXT_PHASE", "discussion", lobbyCode);
                break;
            }
            case "discussion":{
                break;
            }
        }


        //TODO:
        // - Gestire il retry dei giocatori lato client
        // - Gestire la notifica al seer lato client
        // - Gestire la votazione e l'espulsione lato server


        // Variabile statica che conta le votazioni, che si resetta quando si conferma il voto e che deve adattarsi al numero dei lupi presenti in gioco
        // Se var == numero voti aspettati, allora voto effettivo quindi conferma
        // altrimenti nulla, aspetta
        // ricorda di creare la variabile atomica o synchronized
        // metodo flip della vita

        // Prima cosa, prendere lista giocatori nella lobby con lobbyCode

        LoggerUtil.logInfo("Ricevuti da client " + lobbyCode + " " + message.toString());
        messagingTemplate.convertAndSend(topic, gameUpdateMessagecol);
    }

    private Boolean doProcessSeerVote(String lobbyCode, GamePhaseResult message, List<Player> players) {
        Player tmp = players.stream().filter(p -> p.getNickname().equals(message.getVoted_player())).findFirst().orElse(null);
        if (tmp == null) {
            return null;
        }

        if (tmp.getRole().equals("Studente fuori corso")) return true;

        return false;
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
                voted_players.get(lobbyCode).clear();
                return false;
            }
        }

        werevolves_vote.put(lobbyCode, werevolves_vote.get(lobbyCode) - 1);
        return null;
    }


    private void doProcessBodyguardVote(String lobbyCode, GamePhaseResult message) {

        if (voted_players.containsKey(lobbyCode)){
            if (voted_players.get(lobbyCode).containsKey(message.getVoted_player())){
                running_games.get(lobbyCode).put(message.getVoted_player(), true);
            }
        }
    }

    public static void sendMessageToPlayer(String nickname, GameUpdateMessage message) {
        String topic = "/topic/lobby/" + message.getLobbyCode() + "/" + nickname; // Dynamic topic
        messagingTemplate.convertAndSend(topic, message);
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
