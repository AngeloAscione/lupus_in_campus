package NC12.LupusInCampus.utils.clientServerComunication;

import java.util.ArrayList;
import java.util.List;

public class MessagesResponse {
    private List<Object> messages;

    public MessagesResponse() {
        this.messages = new ArrayList<>();
    }

    public void setMessages(List<Object> messages) {
        this.messages = messages;
    }

    public List<Object> getMessages() {
        return messages;
    }

    public void addMessage(Object message) {
        this.messages.add(message);
    }

    @Override
    public String toString() {
        return "MessagesResponse{" +
                "messages=" + messages +
                '}';
    }
}
