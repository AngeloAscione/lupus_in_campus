package NC12.LupusInCampus.controller;

import NC12.LupusInCampus.model.dto.notification.SaveTokenRequest;
import NC12.LupusInCampus.model.dto.notification.SendNotificationRequest;
import NC12.LupusInCampus.model.DevicePk;
import NC12.LupusInCampus.model.Player;
import NC12.LupusInCampus.model.dao.DeviceDAO;
import NC12.LupusInCampus.model.Device;
import NC12.LupusInCampus.model.enums.ErrorMessages;
import NC12.LupusInCampus.model.enums.SuccessMessages;
import NC12.LupusInCampus.service.RequestService;
import NC12.LupusInCampus.utils.Session;
import NC12.LupusInCampus.utils.clientServerComunication.MessagesResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("controller/notification")
public class NotificationController {

    private final MessagesResponse messagesResponse;
    @Value("${pushy.api.key}")
    private String pushyApiKey;

    private static final String PUSHY_API_URL = "https://api.pushy.me/push?api_key=";

    private final DeviceDAO deviceDAO;

    @Autowired
    public NotificationController(DeviceDAO deviceDAO, MessagesResponse messagesResponse) {this.deviceDAO = deviceDAO;
        this.messagesResponse = messagesResponse;
    }

    @PutMapping("/save-token")
    public ResponseEntity<?> saveToken(@RequestBody SaveTokenRequest saveTokenRequest, HttpSession session, HttpServletRequest request) {

        String endpoint = RequestService.getEndpoint(request);

        if (!Session.sessionIsActive(session))
            return messagesResponse.createResponse(endpoint, ErrorMessages.PLAYER_NOT_IN_SESSION);

        //insert token in DB
        Device device = new Device();
        device.setDeviceToken(saveTokenRequest.getToken());
        device.setPlayerID(saveTokenRequest.getPlayerId());
        deviceDAO.save(device);

        return messagesResponse.createResponse(endpoint, SuccessMessages.TOKEN_SAVED);
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendNotification(@RequestBody SendNotificationRequest sendNotificationRequest, HttpSession session, HttpServletRequest request) {

        String endpoint = RequestService.getEndpoint(request);

        /*
        if (!Session.sessionIsActive(session))
            return messagesResponse.createResponse(endpoint, ErrorMessages.PLAYER_NOT_IN_SESSION);*/

        DevicePk devicePk = new DevicePk();
        devicePk.setPlayerID(sendNotificationRequest.getReceiverId());
        List<Device> devices = deviceDAO.findDevicesByDevicePkPlayerID(devicePk.getPlayerID());

        if (devices.isEmpty())
            return messagesResponse.createResponse(endpoint, ErrorMessages.DEVICES_NOT_FOUND);

        // create payload
        Player p = (Player) session.getAttribute("player");
        String message = "Hai un messaggio da " + p.getNickname() + "\n";
        Map<String, Object> payload = createPayload(devices, message + sendNotificationRequest.getMessage());

        // send notification
        ResponseEntity<?> response;
        try {
            response = sendHttpToPushy(payload);
        }catch (Exception e){
            return messagesResponse.createResponse(endpoint, ErrorMessages.ERROR_SEND_NOTIFICATION, e);
        }

        return messagesResponse.createResponse(endpoint, SuccessMessages.NOTIFICATION_SENT, response.getBody());
    }

    public Map<String, Object> createPayload(List<Device> devices, String message) {
        Map<String, Object> payload = new HashMap<>();

        // list of device
        List<String> tokens = devices.stream()
                .map(Device::getDeviceToken)
                .toList();

        payload.put("data", Map.of("message", message));
        payload.put("to", tokens);

        return payload;
    }

    public ResponseEntity<?> sendHttpToPushy(Map<String, Object> payload) {

        // Prepare list of target device tokens
        List<String> deviceTokens = (List<String>) payload.get("to");

        // Convert to String[] array
        String[] to = deviceTokens.toArray(new String[0]);

        // Estrai i dati dal payload (ad esempio il messaggio)
        Map<String, String> payloadOut = new HashMap<>();
        if (payload.containsKey("data")) {
            Map<String, String> data = (Map<String, String>) payload.get("data");
            if (data != null && data.containsKey("message")) {
                // Aggiungi il messaggio al payloadOut
                payloadOut.put("message", data.get("message"));
            }
        }

        // Aggiungi il campo "to" (destinatari) al payload
        payloadOut.put("to", String.join(",", to));  // Unisci i destinatari in un'unica stringa separata da virgola

        // Log della struttura finale del payload
        System.out.println("Payload finale: " + payloadOut);

        // Creazione degli headers per la richiesta HTTP
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + pushyApiKey);  // Usa l'API key per l'autenticazione

        // Creazione dell'entità HTTP con payload e headers
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(payloadOut, headers);

        // URL di Pushy con la tua API key
        String url = PUSHY_API_URL + pushyApiKey;

        // Creazione di RestTemplate per inviare la richiesta
        RestTemplate restTemplate = new RestTemplate();

        // Restituisci la risposta (può essere un JSON con il risultato dell'invio della notifica)
        return restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);



        /*System.out.println("dentro - payload: "+payload);
        RestTemplate restTemplate = new RestTemplate();
        System.out.println("dopo rest");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + pushyApiKey);
        System.out.println("dopo headers");

        Map<String, Object> request = new HttpEntity<>(payload, headers);
        System.out.println("dopo request - request: "+request);

        String url = PUSHY_API_URL + pushyApiKey;

        System.out.println(url);
        System.out.println(request);

        return restTemplate.postForEntity(url, request, ResponseEntity.class);*/

    }
}
