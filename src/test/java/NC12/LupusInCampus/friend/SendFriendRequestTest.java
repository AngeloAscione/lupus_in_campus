package NC12.LupusInCampus.friend;

import NC12.LupusInCampus.controller.FriendController;
import NC12.LupusInCampus.model.FriendRequest;
import NC12.LupusInCampus.model.FriendRequestPk;
import NC12.LupusInCampus.model.Player;
import NC12.LupusInCampus.model.dao.FriendDAO;
import NC12.LupusInCampus.model.dao.FriendRequestDAO;
import NC12.LupusInCampus.model.dao.PlayerDAO;
import NC12.LupusInCampus.model.enums.ErrorMessages;
import NC12.LupusInCampus.model.enums.SuccessMessages;
import NC12.LupusInCampus.utils.Session;
import NC12.LupusInCampus.utils.clientServerComunication.MessagesResponse;
import NC12.LupusInCampus.utils.clientServerComunication.NotificationCaller;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import reactor.netty.http.server.HttpServerRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SendFriendRequestSearchTest {

    @InjectMocks
    private FriendController friendController;
    @Mock
    private PlayerDAO playerDAO;
    @Mock
    private FriendDAO friendDAO;
    @Mock
    private FriendRequestDAO friendRequestDAO;
    @Mock
    private NotificationCaller notificationCaller;
    @Mock
    private MessagesResponse messagesResponse;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpSession session;
    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp(){
        when(request.getRequestURI()).thenReturn("/controller/friend/send-friend-request");
    }

    @Test
    void testReceiverNotExists(){// TC_4.1_1
        Player receiver = new Player();
        receiver.setNickname("frank");
        receiver.setId(1);

        Player sender = new Player();
        sender.setNickname("sender");
        sender.setId(2);

        when(Session.sessionIsActive(session)).thenReturn(true);
        when(playerDAO.findPlayerById(receiver.getId())).thenReturn(null);


        when(messagesResponse.createResponse(anyString(), eq(ErrorMessages.PLAYER_NOT_FOUND)))
                .thenReturn(ResponseEntity.ok().body(ErrorMessages.PLAYER_NOT_FOUND.getMessage()));

        ResponseEntity<?> response = friendController.sendFriendRequest(Map.of("friendId", String.valueOf(receiver.getId())), session, request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(ErrorMessages.PLAYER_NOT_FOUND.getMessage(), response.getBody());
        verify(notificationCaller, never()).sendNotificationWebClient(String.valueOf(eq(receiver.getId())), eq("Richiesta di amicizia"), eq(sender));
    }

    @Test
    void testReceiverAlreadyFriend(){ // TC_4.2_1
        Player receiver = new Player();
        receiver.setNickname("frank");
        receiver.setId(1);

        Player sender = new Player();
        sender.setNickname("sender");
        sender.setId(2);

        when(Session.sessionIsActive(session)).thenReturn(true);
        when(playerDAO.findPlayerById(receiver.getId())).thenReturn(receiver);

        when(session.getAttribute("player")).thenReturn(sender);

        List<Player> friends = List.of(receiver);
        when(friendDAO.findFriendsByPlayerId(sender.getId())).thenReturn(friends);

        when(messagesResponse.createResponse(anyString(), eq(ErrorMessages.PLAYER_ALREADY_FRIEND)))
                .thenReturn(ResponseEntity.ok().body(ErrorMessages.PLAYER_ALREADY_FRIEND.getMessage()));

        ResponseEntity<?> response = friendController.sendFriendRequest(Map.of("friendId", String.valueOf(receiver.getId())), session, request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(ErrorMessages.PLAYER_ALREADY_FRIEND.getMessage(), response.getBody());
        verify(notificationCaller, never()).sendNotificationWebClient(String.valueOf(eq(receiver.getId())), eq("Richiesta di amicizia"), eq(sender));

    }

    @Test
    void testFriendRequestAlreadySent(){// TC_4.3_1
        Player receiver = new Player();
        receiver.setNickname("frank");
        receiver.setId(1);

        Player sender = new Player();
        sender.setNickname("sender");
        sender.setId(2);

        when(Session.sessionIsActive(session)).thenReturn(true);
        when(playerDAO.findPlayerById(receiver.getId())).thenReturn(receiver);

        when(session.getAttribute("player")).thenReturn(sender);

        List<Player> friends = new ArrayList<>();
        when(friendDAO.findFriendsByPlayerId(sender.getId())).thenReturn(friends);

        FriendRequestPk friendRequestPk = new FriendRequestPk();
        friendRequestPk.setSenderId(sender.getId());
        friendRequestPk.setReceiverId(receiver.getId());

        when(friendRequestDAO.existsFriendRequestByFriendRequestPk(friendRequestPk)).thenReturn(true);

        when(messagesResponse.createResponse(anyString(), eq(ErrorMessages.FRIEND_REQUEST_ALREADY_SENT)))
                .thenReturn(ResponseEntity.ok().body(ErrorMessages.FRIEND_REQUEST_ALREADY_SENT.getMessage()));

        ResponseEntity<?> response = friendController.sendFriendRequest(Map.of("friendId", String.valueOf(receiver.getId())), session, request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(ErrorMessages.FRIEND_REQUEST_ALREADY_SENT.getMessage(), response.getBody());
        verify(notificationCaller, never()).sendNotificationWebClient(String.valueOf(eq(receiver.getId())), eq("Richiesta di amicizia"), eq(sender));

    }

    @Test
    void testFriendRequestSentSuccessfully(){// TC_4.4

        Player receiver = new Player();
        receiver.setNickname("frank");
        receiver.setId(1);

        Player sender = new Player();
        sender.setNickname("sender");
        sender.setId(2);

        when(Session.sessionIsActive(session)).thenReturn(true);
        when(playerDAO.findPlayerById(receiver.getId())).thenReturn(receiver);
        when(session.getAttribute("player")).thenReturn(sender);

        List<Player> friends = new ArrayList<>();
        when(friendDAO.findFriendsByPlayerId(sender.getId())).thenReturn(friends);

        FriendRequestPk friendRequestPk = new FriendRequestPk();
        friendRequestPk.setSenderId(sender.getId());
        friendRequestPk.setReceiverId(receiver.getId());

        when(friendRequestDAO.existsFriendRequestByFriendRequestPk(friendRequestPk)).thenReturn(false);


        ResponseEntity<?> successResponse = ResponseEntity.ok("success");
        Mockito.<ResponseEntity<?>>when(notificationCaller.sendNotificationWebClient(
                eq(String.valueOf(receiver.getId())),
                eq("Richiesta di amicizia"),
                eq(sender)
        )).thenReturn(successResponse);

        ResponseEntity<?> response = friendController.sendFriendRequest(
                Map.of("friendId", String.valueOf(receiver.getId())), session, request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("success", response.getBody());

        verify(notificationCaller, times(1))
                .sendNotificationWebClient(eq(String.valueOf(receiver.getId())), eq("Richiesta di amicizia"), eq(sender));

    }

}
