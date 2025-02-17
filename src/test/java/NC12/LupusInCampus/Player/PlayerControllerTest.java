package NC12.LupusInCampus.Player;

import NC12.LupusInCampus.controller.PlayerController;
import NC12.LupusInCampus.model.Player;
import NC12.LupusInCampus.model.dao.PlayerDAO;
import NC12.LupusInCampus.model.dto.player.RegistrationRequest;
import NC12.LupusInCampus.model.enums.ErrorMessages;
import NC12.LupusInCampus.model.enums.SuccessMessages;
import NC12.LupusInCampus.service.PasswordResetService;
import NC12.LupusInCampus.utils.LoggerUtil;
import NC12.LupusInCampus.utils.Validator;
import NC12.LupusInCampus.utils.clientServerComunication.MessagesResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Incubating;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PlayerControllerTest {

    @InjectMocks
    private PlayerController playerController;

    @Mock
    private PlayerDAO playerDAO;

    @Mock
    private MessagesResponse messagesResponse;

    @Mock
    private Validator validator;

    @Mock
    private RegistrationRequest registrationRequest;

    @Mock
    private HttpSession session;

    @Mock
    private HttpServletRequest request;



    @BeforeEach
    void setUp() {
        when(request.getRequestURI()).thenReturn("/controller/player/registration");
    }

    @Test
    void testRegistrationWithSomeEmptyFields(){ // TC_1.1_1 or TC_1.2_1 or TC_1.3_1
        //cambiare il campo vuoto per testare tutto
        String nickname = "";
        String email = "franco@gmail.com";
        String password = "franchinello12";

        //comportamento atteso
        when(registrationRequest.getNickname()).thenReturn(nickname);
        when(messagesResponse.createResponse(anyString(), eq(ErrorMessages.EMPTY_NICKNAME_FIELD)))
                .thenReturn(ResponseEntity.ok().body(ErrorMessages.EMPTY_NICKNAME_FIELD.getMessage()));

        //chiamata all'endpoint
        ResponseEntity<String> response = playerController.playerRegistration(registrationRequest, session, request);

        //verifica se il messaggio d'errore sia quello atteso
        assertEquals(200, response.getStatusCode().value());
        assertEquals(ErrorMessages.EMPTY_NICKNAME_FIELD.getMessage(), response.getBody());

        //verifica che il metodo "save" non venga chiamato (never), perché la registrazione non deve andare a buon fine
        verify(playerDAO, never()).save(any(Player.class));
    }

    @Test
    void testRegistrationWithNicknameAlreadyExists() { // TC_1.1_2
        String nickname = "franck";
        String email = "franco@gmail.com";
        String password = "franchinello12";

        when(registrationRequest.getNickname()).thenReturn(nickname);
        when(playerDAO.findPlayerByNickname(nickname)).thenReturn(new Player());
        when(messagesResponse.createResponse(anyString(), eq(ErrorMessages.NICKNAME_ALREADY_USED)))
                .thenReturn(ResponseEntity.ok().body(ErrorMessages.NICKNAME_ALREADY_USED.getMessage()));

        ResponseEntity<String> response = playerController.playerRegistration(registrationRequest, session, request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(ErrorMessages.NICKNAME_ALREADY_USED.getMessage(), response.getBody());
        verify(playerDAO, never()).save(any(Player.class));
    }

    @Test
    void testRegistrationWithWrongEmailFormat(){ // TC_1.2_2

        String nickname = "frank1";
        String email = "franco@com";
        String password = "franchinello12";

        when(registrationRequest.getNickname()).thenReturn(nickname);
        when(playerDAO.findPlayerByNickname(nickname)).thenReturn(null);
        when(registrationRequest.getEmail()).thenReturn(email);
        when(validator.emailIsValid(email)).thenReturn(false);
        when(messagesResponse.createResponse(anyString(), eq(ErrorMessages.EMAIL_FORMAT)))
                .thenReturn(ResponseEntity.ok().body(ErrorMessages.EMAIL_FORMAT.getMessage()));

        ResponseEntity<String> response = playerController.playerRegistration(registrationRequest, session, request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(ErrorMessages.EMAIL_FORMAT.getMessage(), response.getBody());
        verify(playerDAO, never()).save(any(Player.class));
    }

    @Test
    void testRegistrationWithEmailAlreadyExists(){// TC_1.2_3
        String nickname = "frank1";
        String email = "franco@gmail.com";
        String password = "franchinello12";

        when(registrationRequest.getNickname()).thenReturn(nickname);
        when(playerDAO.findPlayerByNickname(nickname)).thenReturn(null);
        when(registrationRequest.getEmail()).thenReturn(email);
        when(validator.emailIsValid(email)).thenReturn(true);
        when(playerDAO.findPlayerByEmail(email)).thenReturn(new Player());
        when(messagesResponse.createResponse(anyString(), eq(ErrorMessages.EMAIL_ALREADY_USED)))
                .thenReturn(ResponseEntity.ok().body(ErrorMessages.EMAIL_ALREADY_USED.getMessage()));

        ResponseEntity<String> response = playerController.playerRegistration(registrationRequest, session, request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(ErrorMessages.EMAIL_ALREADY_USED.getMessage(), response.getBody());
        verify(playerDAO, never()).save(any(Player.class));
    }


    @Test
    void testSuccessfulRegistration() {
        String nickname = "frank1";
        String email = "franco1@gmail.com";
        String password = "franchinello12";

        Player player = new Player();
        player.setNickname(nickname);
        player.setEmail(email);
        player.setPassword(password);

        //comportamento atteso
        when(registrationRequest.getNickname()).thenReturn(nickname);
        when(playerDAO.findPlayerByNickname(nickname)).thenReturn(null);
        when(registrationRequest.getEmail()).thenReturn(email);
        when(validator.emailIsValid(email)).thenReturn(true);
        when(playerDAO.findPlayerByEmail(email)).thenReturn(null);
        when(registrationRequest.getPassword()).thenReturn(password);
        when(playerDAO.save(any(Player.class))).thenReturn(player);
        when(messagesResponse.createResponse(anyString(), eq(SuccessMessages.REGISTRATION_SUCCESS), any(Player.class)))
                .thenReturn(ResponseEntity.ok().body(SuccessMessages.REGISTRATION_SUCCESS.getMessage()));


        //chiamata all'endpoint
        ResponseEntity<String> response = playerController.playerRegistration(registrationRequest, session, request);

        //verifica
        assertEquals(200, response.getStatusCode().value());
        assertEquals(SuccessMessages.REGISTRATION_SUCCESS.getMessage(), response.getBody());
        verify(playerDAO, times(1)).save(any(Player.class));
        verify(session, times(1)).setAttribute(eq("player"), any(Player.class));
    }
}
