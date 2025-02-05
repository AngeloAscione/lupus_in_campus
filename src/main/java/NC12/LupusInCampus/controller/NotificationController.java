package NC12.LupusInCampus.controller;

import NC12.LupusInCampus.dto.notification.SaveTokenRequest;
import NC12.LupusInCampus.dto.notification.SendNotificationRequest;
import NC12.LupusInCampus.model.DevicePk;
import NC12.LupusInCampus.model.Player;
import NC12.LupusInCampus.model.dao.DeviceDAO;
import NC12.LupusInCampus.model.Device;
import NC12.LupusInCampus.model.enums.ErrorMessages;
import NC12.LupusInCampus.utils.LoggerUtil;
import NC12.LupusInCampus.utils.Session;
import NC12.LupusInCampus.utils.clientServerComunication.MessageResponse;
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

    private String pushyApiKey;

    private static final String PUSHY_API_URL = "https://api.pushy.me/push?api_key=";

    private final DeviceDAO deviceDAO;

    @Autowired
    public NotificationController(DeviceDAO deviceDAO) {this.deviceDAO = deviceDAO;}

    @PutMapping("/save-token")
    public ResponseEntity<?> saveToken(@RequestBody SaveTokenRequest saveTokenRequest) {
        LoggerUtil.logInfo("-> Ricevuta richiesta save-token");
        Device device = new Device();
        device.setDeviceToken(saveTokenRequest.getToken());
        device.setPlayerID(saveTokenRequest.getPlayerId());
        deviceDAO.save(device);

        LoggerUtil.logInfo("<- Risposta save-token");
        return ResponseEntity.ok().body("Token salvato");
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendNotification(@RequestBody SendNotificationRequest sendNotificationRequest, HttpSession session) {

        LoggerUtil.logInfo("-> Ricevuta richiesta send notification");

        if (!Session.sessionIsActive(session)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new MessageResponse(
                        ErrorMessages.PLAYER_NOT_IN_SESSION.getCode(),
                        ErrorMessages.PLAYER_NOT_IN_SESSION.getMessage()
                )
        );

        DevicePk devicePk = new DevicePk();
        devicePk.setPlayerID(sendNotificationRequest.getReceiverId());
        List<Device> devices = deviceDAO.findDevicesByDevicePk(devicePk);

        if (devices.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Nessun dispositivo trovato per questo utente.");
        }

        // create payload
        Player p = (Player) session.getAttribute("player");
        String message = "Hai un messaggio da " + p.getNickname() + "\n";
        Map<String, Object> payload = createPayload(devices, message + sendNotificationRequest.getMessage());

        // send notification
        ResponseEntity<String> response = sendHttpToPushy(payload);

        LoggerUtil.logInfo("<- Risposta send notification");
        return ResponseEntity.ok(response.getBody());
    }

    public Map<String, Object> createPayload(List<Device> devices, String message) {
        Map<String, Object> payload = new HashMap<>();

        // list of device
        List<String> tokens = devices.stream()
                .map(Device::getDeviceToken)
                .toList();

        payload.put("to", tokens);
        payload.put("data", Map.of("message", message));

        return payload;
    }

    public ResponseEntity<String> sendHttpToPushy(Map<String, Object> payload) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        String url = PUSHY_API_URL + pushyApiKey;

        return restTemplate.postForEntity(url, request, String.class);
    }
}
