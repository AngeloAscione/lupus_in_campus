package NC12.LupusInCampus.Model.Utils.ComunicazioneClientServer;

import java.util.HashMap;
import java.util.Map;

public class MessageResponse {
    private int status;
    private String statusStr;
    private Object body; // Permette di adattarsi a dati generici

    // Costruttore
    public MessageResponse(int status, String statusStr, Object body) {
        this.status = status;
        this.statusStr = statusStr;
        this.body = body;
    }

    // Getter e setter
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getStatusStr() {
        return statusStr;
    }

    public void setStatusStr(String statusStr) {
        this.statusStr = statusStr;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    // Metodo statico per creare una risposta
    public static MessageResponse createResponse(int status, String statusStr, Object body) {
        return new MessageResponse(status, statusStr, body);
    }

    // Metodo opzionale per una rappresentazione JSON-friendly
    public Map<String, Object> toMap() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", status);
        response.put("statusStr", statusStr);
        response.put("body", body);
        return response;
    }
}
