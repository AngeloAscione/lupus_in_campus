package NC12.LupusInCampus.utils.clientServerComunication;

import NC12.LupusInCampus.model.enums.ErrorMessages;
import NC12.LupusInCampus.utils.LoggerUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.ArrayList;
import java.util.List;

@Component
@RequestScope
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

    public ResponseEntity<String> createResponse(String from, Object... messages){

        // check if there are errors
        boolean hasError = false;

        // add messages
        for (Object message : messages) {
            if (message instanceof ErrorMessages) {
                hasError = true;
            }
            this.addMessage(message);
        }

        // print log
        if (hasError)
            LoggerUtil.logError("<- Risposta alla richiesta " +from, new Exception(this.toString()));
        else
            LoggerUtil.logInfo("<- Risposta alla richiesta " + from + ":\n" + this.toString());

        //return response
        return ResponseEntity.status(HttpStatus.OK).body(this.toString());
    }

    @Override
    public String toString() {
        return "{\"MessagesResponse\":{" +
                "\"messages\":" + messages +
                "}}";
    }
}
