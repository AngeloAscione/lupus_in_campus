package NC12.LupusInCampus.lobby;

import NC12.LupusInCampus.controller.LobbyController;
import NC12.LupusInCampus.model.Lobby;
import NC12.LupusInCampus.model.Player;
import NC12.LupusInCampus.model.dao.LobbyDAO;
import NC12.LupusInCampus.model.dao.LobbyInvitationDAO;
import NC12.LupusInCampus.model.enums.ErrorMessages;
import NC12.LupusInCampus.service.ListPlayersLobbiesService;
import NC12.LupusInCampus.utils.Session;
import NC12.LupusInCampus.utils.clientServerComunication.MessagesResponse;
import NC12.LupusInCampus.utils.clientServerComunication.NotificationCaller;

import NC12.LupusInCampus.utils.Validator;
import NC12.LupusInCampus.utils.clientServerComunication.MessagesResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JoinPublicLobbyTest{
    @Mock
    private  LobbyDAO lobbyDAO;
    @Mock
    private  LobbyInvitationDAO lobbyInvitationDAO;
    @Mock
    private  NotificationCaller notificationCaller;
    @Mock
    private  ListPlayersLobbiesService lobbyLists;
    @Mock
    private  MessagesResponse messagesResponse;

    @Mock
    private HttpSession session;
    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private LobbyController lobbyController;

    @BeforeEach
    void setUp(){
        when(request.getRequestURI()).thenReturn("/controller/lobby/join-lobby");
    }

    @Test
    void testPublicLobbiesNotFound(){//TC_2.1_1
        long num_lobbies = 0;
        Map<String, Integer> params = new HashMap<>();
        params.put("code", 123456);

        when(!Session.sessionIsActive(session)).thenReturn(false);
        when(lobbyDAO.count()).thenReturn(num_lobbies);
        when(messagesResponse.createResponse(anyString(), eq(ErrorMessages.LOBBY_NOT_FOUND)))
               .thenReturn(ResponseEntity.ok().body(ErrorMessages.LOBBY_NOT_FOUND.getMessage()));

        ResponseEntity<String> response = lobbyController.joinLobby(params, session, request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(ErrorMessages.LOBBY_NOT_FOUND.getMessage(), response.getBody());
    }

    @Test
    void testPublicLobbiesFull(){ // TC_2.2_1
        long num_lobbies = 5;
        int code = 123456;

        Map<String, Integer> params = new HashMap<>();
        params.put("code", code);

        // Creazione della lobby
        Lobby lobby = new Lobby();
        lobby.setState("In attesa");
        lobby.setCode(code);
        lobby.setMaxNumPlayer(18);  // Numero massimo di giocatori
        lobby.setNumPlayer(18);     // La lobby è piena
        lobby.setMinNumPlayer(6);
        lobby.setCreationDate(LocalDateTime.now());
        lobby.setCreatorID(12);

        // Mock dei metodi che controllano lo stato della sessione e il numero di lobby
        when(!Session.sessionIsActive(session)).thenReturn(false);  // La sessione non è attiva
        when(lobbyDAO.count()).thenReturn(num_lobbies);  // Ci sono 5 lobby nel sistema
        when(!lobbyLists.containsCode(code)).thenReturn(false);

        // Creazione della lista di 18 giocatori (la lobby è piena)
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < 18; i++) {
            players.add(new Player());
        }
        when(lobbyLists.getListPlayers(code)).thenReturn(players);  // La lobby ha 18 giocatori

        // Mock della creazione della risposta con ErrorMessages.LIMIT_PLAYER_LOBBY
        lenient().when(messagesResponse.createResponse(eq("/controller/lobby/join-lobby"), eq(ErrorMessages.LIMIT_PLAYER_LOBBY)))
                .thenReturn(ResponseEntity.ok().body(ErrorMessages.LIMIT_PLAYER_LOBBY.getMessage()));

        // Chiamata al metodo
        ResponseEntity<String> response = lobbyController.joinLobby(params, session, request);

        // Verifica che la risposta sia corretta
        assertEquals(200, response.getStatusCode().value());  // Lo stato della risposta deve essere 200 (OK)
        assertEquals(ErrorMessages.LIMIT_PLAYER_LOBBY.getMessage(), response.getBody());  // Il messaggio di errore deve essere quello giusto

        // Verifica che il giocatore non venga aggiunto alla lobby (la lobby è piena)
        verify(lobbyLists, never()).addPlayer(any(Player.class), code);
    }


}
