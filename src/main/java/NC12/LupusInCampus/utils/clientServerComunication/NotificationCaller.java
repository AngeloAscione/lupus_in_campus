package NC12.LupusInCampus.utils.clientServerComunication;

import NC12.LupusInCampus.model.Player;
import com.google.gson.Gson;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class NotificationCaller {
    private final RestTemplate restTemplate = new RestTemplate();
    private final Gson gson = new Gson();

    public NotificationCaller() {}

    public ResponseEntity<?> sendNotificationWebClient(String idFriend, String message, Player player) {

        String url = "http://localhost:8080/controller/notification/send";

        String jsonBody = gson.toJson(Map.of(
                "receiverId", idFriend,
                "message", message,
                "nickname", player.getNickname()
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, headers);

        return restTemplate.postForEntity(url, requestEntity, ResponseEntity.class);

    }
}
