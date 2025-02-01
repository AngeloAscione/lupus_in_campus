package NC12.LupusInCampus.controller;

import NC12.LupusInCampus.model.DevicePk;
import NC12.LupusInCampus.model.dao.DeviceDAO;
import NC12.LupusInCampus.model.Device;
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

    @PostMapping("/save-token")
    public ResponseEntity<?> saveToken(@RequestParam String playerID, @RequestParam String deviceToken) {

        //insert token in DB
        Device device = new Device();
        device.setDeviceToken(deviceToken);
        device.setPlayerID(Integer.parseInt(playerID));
        deviceDAO.save(device);

        return ResponseEntity.ok().body("Token salvato");
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendNotification(@RequestParam String receivingPlayerID, @RequestParam String message) {

        DevicePk devicePk = new DevicePk();
        devicePk.setPlayerID(Integer.parseInt(receivingPlayerID));
        List<Device> devices = deviceDAO.findDevicesByDevicePk(devicePk);

        if (devices.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Nessun dispositivo trovato per questo utente.");
        }

        // create payload
        Map<String, Object> payload = createPayload(devices, message);

        // send notification
        ResponseEntity<String> response = sendHttpToPushy(payload);

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
