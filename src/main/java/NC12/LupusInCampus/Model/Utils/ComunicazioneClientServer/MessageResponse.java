package NC12.LupusInCampus.Model.Utils.ComunicazioneClientServer;

public class MessageResponse {
    private int status; // status HTTP
    private String statusStr; // status HTTP String
    private Object body; // message or data json

    // Builders
    public MessageResponse(int status, String statusStr, Object body) {
        this.status = status;
        this.statusStr = statusStr;
        this.body = body;
    }

    public MessageResponse(int status, String statusStr) {
        this.status = status;
        this.statusStr = statusStr;
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
}
