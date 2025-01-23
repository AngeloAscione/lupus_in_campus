package NC12.LupusInCampus.Model.Utils.ComunicazioneClientServer;

public class MessageResponse {
    private int status; // status HTTP
    private String statusStr; // status HTTP String
    private String message; // string by SuccessMessages
    private Object body; // message or data json

    // Builders
    public MessageResponse(int status, String statusStr, Object body) {
        this.status = status;
        this.statusStr = statusStr;
        this.body = body;
    }

    public MessageResponse(int status, String statusStr,  Object body, String message) {
        this.status = status;
        this.statusStr = statusStr;
        this.message = message;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }
}
