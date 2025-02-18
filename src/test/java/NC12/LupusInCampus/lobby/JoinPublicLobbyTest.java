package NC12.LupusInCampus.lobby;

import NC12.LupusInCampus.controller.LobbyController;
import NC12.LupusInCampus.model.Lobby;
import NC12.LupusInCampus.model.LobbyInvitation;
import NC12.LupusInCampus.model.LobbyInvitationPk;
import NC12.LupusInCampus.model.Player;
import NC12.LupusInCampus.model.dao.LobbyDAO;
import NC12.LupusInCampus.model.dao.LobbyInvitationDAO;
import NC12.LupusInCampus.model.enums.ErrorMessages;
import NC12.LupusInCampus.model.enums.SuccessMessages;
import NC12.LupusInCampus.service.ListPlayersLobbiesService;
import NC12.LupusInCampus.utils.LoggerUtil;
import NC12.LupusInCampus.utils.Session;
import NC12.LupusInCampus.utils.clientServerComunication.MessagesResponse;
import NC12.LupusInCampus.utils.clientServerComunication.NotificationCaller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JoinPublicLobbyTest{

    @InjectMocks
    private LobbyController lobbyController;
    @Mock
    private LobbyDAO lobbyDAO;
    @Mock
    private ListPlayersLobbiesService lobbyLists;
    @Mock
    private MessagesResponse messagesResponse;
    @Mock
    private HttpSession session;
    @Mock
    private HttpServletRequest request;
    @Mock
    private Lobby lobby;
    @Mock
    private LobbyInvitationDAO lobbyInvitationDAO;


    @BeforeEach
    void setUp(){
        when(request.getRequestURI()).thenReturn("/controller/lobby/join-lobby");
    }

    @Test
    void testPublicLobbiesNotFound(){//TC_2.1_1

        when(Session.sessionIsActive(session)).thenReturn(true);
        when(lobbyDAO.count()).thenReturn(0L); //nessuna lobby attiva
        when(messagesResponse.createResponse(anyString(), eq(ErrorMessages.LOBBY_NOT_FOUND)))
                .thenReturn(ResponseEntity.ok().body(ErrorMessages.LOBBY_NOT_FOUND.getMessage()));

        ResponseEntity<String> response = lobbyController.joinLobby(Map.of("code", 123456), session, request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(ErrorMessages.LOBBY_NOT_FOUND.getMessage(), response.getBody());
        verify(lobbyLists, never()).addPlayer(any(Player.class), eq(123456));
    }

    @Test
    void testChooseLobbyFull() { // TC_2.2_1
        int code = 123456;
        int maxNumPlayer = 18;


        when(Session.sessionIsActive(session)).thenReturn(true);
        when(lobbyDAO.count()).thenReturn(5L); //5 lobby attive

        when(lobbyLists.containsCode(code)).thenReturn(true);
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < maxNumPlayer; i++){
            players.add(new Player());
        }
        when(lobbyLists.getListPlayers(code)).thenReturn(players);

        when(lobbyDAO.findLobbyByCode(code)).thenReturn(lobby);
        when(lobby.getMaxNumPlayer()).thenReturn(maxNumPlayer);

        when(messagesResponse.createResponse(anyString(), eq(ErrorMessages.LIMIT_PLAYER_LOBBY)))
                .thenReturn(ResponseEntity.ok().body(ErrorMessages.LIMIT_PLAYER_LOBBY.getMessage()));


        ResponseEntity<String> response = lobbyController.joinLobby(Map.of("code", code), session, request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(ErrorMessages.LIMIT_PLAYER_LOBBY.getMessage(), response.getBody());
        verify(lobbyLists, never()).addPlayer(any(Player.class), eq(code));

    }

    @Test
    void testChooseLobbyAlreadyStart(){ // TC_2.3_1
        int code = 555555;
        int maxNumPlayer = 18;

        when(Session.sessionIsActive(session)).thenReturn(true);
        when(lobbyDAO.count()).thenReturn(5L); //5 lobby attive

        when(lobbyLists.containsCode(code)).thenReturn(true);
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < maxNumPlayer-4; i++){
            players.add(new Player());
        }
        when(lobbyLists.getListPlayers(code)).thenReturn(players);

        when(lobbyDAO.findLobbyByCode(code)).thenReturn(lobby);
        when(lobby.getMaxNumPlayer()).thenReturn(maxNumPlayer);
        when(lobby.getState()).thenReturn("In corso");

        when(messagesResponse.createResponse(anyString(), eq(ErrorMessages.LOBBY_ALREADY_STARTED)))
                .thenReturn(ResponseEntity.ok().body(ErrorMessages.LOBBY_ALREADY_STARTED.getMessage()));

        ResponseEntity<String> response = lobbyController.joinLobby(Map.of("code", code), session, request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(ErrorMessages.LOBBY_ALREADY_STARTED.getMessage(), response.getBody());
        verify(lobbyLists, never()).addPlayer(any(Player.class), eq(code));

    }

    @Test
    void joinSuccessful(){ //TC_2.4
        //parametri
        int code = 888888;
        int maxNumPlayer = 18;

        //simula il giocatore
        Player playerToJoin = new Player();
        playerToJoin.setId(1);
        playerToJoin.setNickname("playerToJoin");

        //sessione attiva
        when(Session.sessionIsActive(session)).thenReturn(true);

        //lobby attive = 5
        when(lobbyDAO.count()).thenReturn(5L); //5 lobby attive

        //controlli e riempimetno di lobbyList per simulare le lobby
        when(lobbyLists.containsCode(code)).thenReturn(true);
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < maxNumPlayer-4; i++){
            players.add(new Player());
        }
        when(lobbyLists.getListPlayers(code)).thenReturn(players);

        //simulare il get lobby dal DB e settaggio dei parametri
        when(lobbyDAO.findLobbyByCode(code)).thenReturn(lobby);
        when(lobby.getMaxNumPlayer()).thenReturn(maxNumPlayer);
        when(lobby.getState()).thenReturn("Attesa giocatori");

        //prendo l'utente simulato dalla sessione
        when(session.getAttribute("player")).thenReturn(playerToJoin);

        //controllo se è già presente nella lobby
        when(lobbyLists.containsPlayer(code, playerToJoin)).thenReturn(false);

        // Crea un oggetto invito simulato
        LobbyInvitationPk lobbyInvitationPk = new LobbyInvitationPk();
        lobbyInvitationPk.setSendingPlayerId(lobby.getCreatorID());
        lobbyInvitationPk.setInvitedPlayerId(playerToJoin.getId());

        LobbyInvitation invitation = new LobbyInvitation();
        invitation.setLobbyInvitationPk(lobbyInvitationPk);

        // mock per restituire l'invito quando viene cercato
        when(lobbyInvitationDAO.findLobbyInvitationByLobbyInvitationPk(lobbyInvitationPk)).thenReturn(invitation);

        //chiamata metodo per invito
        lobbyController.checkIfExistsLobbyInvitation(lobby, playerToJoin);

        //simulo risposta
        when(messagesResponse.createResponse(anyString(), eq(SuccessMessages.PLAYER_ADDED_LOBBY), eq(players)))
                .thenReturn(ResponseEntity.ok().body(SuccessMessages.PLAYER_ADDED_LOBBY.getMessage()));

        //chimata all'endpoint
        ResponseEntity<String> response = lobbyController.joinLobby(Map.of("code", code), session, request);

        //controlli
        assertEquals(200, response.getStatusCode().value());
        assertEquals(SuccessMessages.PLAYER_ADDED_LOBBY.getMessage(), response.getBody());
        verify(lobbyLists, times(1)).addPlayer(eq(playerToJoin), eq(code));
    }
}