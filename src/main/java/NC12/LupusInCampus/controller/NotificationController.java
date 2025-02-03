package NC12.LupusInCampus.controller;

import NC12.LupusInCampus.model.Player;
import NC12.LupusInCampus.model.dao.DeviceDAO;
import NC12.LupusInCampus.model.Device;
import NC12.LupusInCampus.model.enums.ErrorMessages;
import NC12.LupusInCampus.model.enums.SuccessMessages;
import NC12.LupusInCampus.utils.Session;
import NC12.LupusInCampus.utils.clientServerComunication.MessageResponse;
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

    @Value("${pushy.api.key}")
    private String pushyApiKey;

    private static final String PUSHY_API_URL = "https://api.pushy.me/push?api_key=";

    private final DeviceDAO deviceDAO;

    @Autowired
    public NotificationController(DeviceDAO deviceDAO) {this.deviceDAO = deviceDAO;}

    @PostMapping("/save-token")
    public ResponseEntity<?> saveToken(HttpSession session, @RequestParam String deviceToken) {
        if (!Session.sessionIsActive(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new MessageResponse(
                            ErrorMessages.PLAYER_NOT_IN_SESSION.getCode(),
                            ErrorMessages.PLAYER_NOT_IN_SESSION.getMessage()
                    )
            );
        }

        Player player = (Player) session.getAttribute("player");

        //insert token in DB
        Device device = new Device();
        device.setDeviceToken(deviceToken);
        device.setPlayerID(player.getId());
        deviceDAO.save(device);

        return ResponseEntity.ok().body("Token salvato");
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendNotification(@RequestBody Map<String, String> request) {
        System.out.println(request);

        String receivingPlayerID = request.get("receivingPlayerID");
        String message = request.get("message");

        System.out.println("player id: "+receivingPlayerID);
        System.out.println("mess: "+message);

        System.out.println("####################### " + pushyApiKey);
        List<Device> devices = deviceDAO.findDevicesByDevicePkPlayerID(Integer.parseInt(receivingPlayerID));

        System.out.println("####################### dopo query");
        if (devices.isEmpty()) {
            System.out.println("####################### device non trovati");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new MessageResponse(-1, "Dispositivi non trovati")
            );
        }

        System.out.println("####################### device trovati");

        // create payload
        Map<String, Object> payload = createPayload(devices, message);
        System.out.println("####################### payload");

        // send notification
        ResponseEntity<?> response;
        try {
            response = sendHttpToPushy(payload);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new MessageResponse(
                            -1,
                            "Errore nell'invio della notifica, prob. perché non combaciano i token"
                    )
            );
        }
        System.out.println("####################### response");


        return ResponseEntity.ok().body(
                new MessageResponse(
                        SuccessMessages.NOTIFICATION_SENT.getCode(),
                        SuccessMessages.NOTIFICATION_SENT.getMessage(),
                        response
                )
        );


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
