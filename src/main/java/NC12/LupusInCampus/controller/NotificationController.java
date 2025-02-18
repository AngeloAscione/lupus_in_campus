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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.http.entity.ByteArrayEntity;

import java.net.URI;
import java.nio.charset.StandardCharsets;
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

        return messagesResponse.createResponse(endpoint, SuccessMessages.TOKEN_SAVED, device);
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
        String message = "Hai un messaggio da " + sendNotificationRequest.getNickname() + "\n";
        PushyPushRequest push = createPayload(devices, message + sendNotificationRequest.getMessage());

        // send notification
        String message_res = null;
        try {
            if (sendHttpToPushy(push)){
                message_res = "success";
            }
        }catch (Exception e){
            return messagesResponse.createResponse(endpoint, ErrorMessages.ERROR_SEND_NOTIFICATION, e);
        }

        return messagesResponse.createResponse(endpoint, SuccessMessages.NOTIFICATION_SENT, message_res);
    }

    public PushyPushRequest createPayload(List<Device> devices, String message) {
        Map<String, Object> payload = new HashMap<>();

        // Extract device tokens
        List<String> tokens = devices.stream()
                .map(Device::getDeviceToken)
                .toList();

        String[] to = tokens.toArray(new String[tokens.size()]);

        // Ensure `message` is correctly passed inside the payload
        payload.put("message", message);

        Map<String, Object> notification = new HashMap<>();
        notification.put("badge", 1);
        notification.put("title", "Lupus In Campus");
        notification.put("body", message);

        return new PushyPushRequest(payload, to, notification);
    }


    public boolean sendHttpToPushy(PushyPushRequest pushyPushRequest){

        try {
            ObjectMapper mapper = new ObjectMapper();

            HttpClient client = new DefaultHttpClient();

            // Create POST request
            HttpPost request = new HttpPost(PUSHY_API_URL + pushyApiKey);

            // Set content type to JSON
            request.addHeader("Content-Type", "application/json");

            // Convert post data to JSON
            byte[] json = mapper.writeValueAsBytes(pushyPushRequest);

            // Send post data as byte array
            request.setEntity(new ByteArrayEntity(json));

            // Execute the request
            HttpResponse response = client.execute(request, new BasicHttpContext());

            // Get response JSON as string
            String responseJSON = EntityUtils.toString(response.getEntity());

            // Convert JSON response into HashMap
            Map<String, Object> map = mapper.readValue(responseJSON, Map.class);

            // Got an error?
            if (map.containsKey("error")) {
                // Throw it
                throw new Exception(map.get("error").toString());
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

        return true;

    }


    public static class PushyPushRequest {
        public Object to;
        public Object data;

        public Object notification;

        public PushyPushRequest(Object data, Object to, Object notification) {
            this.to = to;
            this.data = data;
            this.notification = notification;
        }
    }
}
