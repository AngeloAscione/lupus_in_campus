package NC12.LupusInCampus.service;

import NC12.LupusInCampus.utils.LoggerUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class RequestService {

    //endpoint + log
    public static String getEndpoint(HttpServletRequest request) {
        String endpoint = request.getRequestURI();
        LoggerUtil.logInfo("-> Ricevuta richiesta da " + request.getRemoteAddr() + " per endpoint: " + endpoint);
        return endpoint;
    }
}
