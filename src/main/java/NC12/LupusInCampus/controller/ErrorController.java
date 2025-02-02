package NC12.LupusInCampus.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ErrorController {

    @GetMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute("javax.servlet.error.status_code");
        Object errorMessage = request.getAttribute("javax.servlet.error.message");

        model.addAttribute("status", status != null ? status.toString() : "N/A");
        model.addAttribute("error", errorMessage != null ? errorMessage.toString() : "Errore sconosciuto");

        return "error";
    }
}
