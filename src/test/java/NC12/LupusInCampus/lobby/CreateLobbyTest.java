package NC12.LupusInCampus.lobby;

import NC12.LupusInCampus.controller.LobbyController;
import NC12.LupusInCampus.model.Lobby;
import NC12.LupusInCampus.model.Player;
import NC12.LupusInCampus.model.dao.LobbyDAO;
import NC12.LupusInCampus.model.dao.LobbyInvitationDAO;
import NC12.LupusInCampus.model.enums.ErrorMessages;
import NC12.LupusInCampus.model.enums.SuccessMessages;
import NC12.LupusInCampus.service.ListPlayersLobbiesService;
import NC12.LupusInCampus.utils.Session;
import NC12.LupusInCampus.utils.clientServerComunication.MessagesResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import okhttp3.internal.platform.Platform;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class CreateLobbyTest {

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
        when(request.getRequestURI()).thenReturn("/controller/lobby/create-lobby");
    }

    @Test
    void testPlayerAlreadyJoinInALobby(){// TC_3.1_1

        Player player = new Player();
        player.setId(1);
        player.setNickname("franco");

        when(Session.sessionIsActive(session)).thenReturn(true);
        when(session.getAttribute("player")).thenReturn(player);
        when(lobbyLists.containsPlayerSomewhere(player)).thenReturn(true);

        when(messagesResponse.createResponse(anyString(), eq(ErrorMessages.LOBBY_NOT_CREATE)))
                .thenReturn(ResponseEntity.ok().body(ErrorMessages.LOBBY_NOT_CREATE.getMessage()));

        ResponseEntity<?> response = lobbyController.createLobby(Map.of("tipo", "Pubblica"), session, request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(ErrorMessages.LOBBY_NOT_CREATE.getMessage(), response.getBody());

        verify(lobbyDAO, never()).save(any(Lobby.class));
    }

    @Test
    void testWrongTypeLobby(){ // TC_3.2_1
        Player player = new Player();
        player.setId(1);
        player.setNickname("franco");

        when(Session.sessionIsActive(session)).thenReturn(true);
        when(session.getAttribute("player")).thenReturn(player);
        when(lobbyLists.containsPlayerSomewhere(player)).thenReturn(false);


        when(messagesResponse.createResponse(anyString(), eq(ErrorMessages.LOBBY_TYPE_NOT_SUPPORTED)))
                .thenReturn(ResponseEntity.ok().body(ErrorMessages.LOBBY_TYPE_NOT_SUPPORTED.getMessage()));

        ResponseEntity<?> response = lobbyController.createLobby(Map.of("tipo", "InvalidType"), session, request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(ErrorMessages.LOBBY_TYPE_NOT_SUPPORTED.getMessage(), response.getBody());

        verify(lobbyDAO, never()).save(any(Lobby.class));
    }

    @Test
    void testSuccessCreateLobby(){ //TC_3.3
        Player player = new Player();
        player.setId(1);
        player.setNickname("franco");

        when(Session.sessionIsActive(session)).thenReturn(true);
        when(session.getAttribute("player")).thenReturn(player);
        when(lobbyLists.containsPlayerSomewhere(player)).thenReturn(false);


        when(messagesResponse.createResponse(anyString(), eq(SuccessMessages.LOBBY_CREATED), any(Lobby.class)))
                .thenReturn(ResponseEntity.ok().body(SuccessMessages.LOBBY_CREATED.getMessage()));

        ResponseEntity<?> response = lobbyController.createLobby(Map.of("tipo", "Pubblica"), session, request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(SuccessMessages.LOBBY_CREATED.getMessage(), response.getBody());

        verify(lobbyDAO, times(1)).save(any(Lobby.class));
        verify(lobbyLists, times(1)).addLobbyCode(anyInt());
        verify(lobbyLists, times(1)).addPlayer(eq(player), anyInt());

    }
}
