package NC12.LupusInCampus.Model.Utils.ClientServerComunication;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

public class WebClientNotification {
    private static final WebClient.Builder urlBase = WebClient.builder();
    private static final WebClient webClient = urlBase.baseUrl("http://localhost:8080/").build();

    public WebClientNotification(){}

    public static ResponseEntity<?> sendNotificationWebClient(String idFriend, String message){

        ResponseEntity<?> response = webClient.post()
                .uri("controller/notification/send")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("receivingPlayerID", idFriend)
                        .with("message", message))
                .retrieve()
                .toEntity(Object.class)
                .block();


        assert response != null;
        return  ResponseEntity
                .status(response.getStatusCode())
                .headers(response.getHeaders())
                .body(response.getBody());
    }
}
