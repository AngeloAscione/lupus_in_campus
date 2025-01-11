package NC12.LupusInCampus.Controller;

import NC12.LupusInCampus.Model.DAO.GiocatoreDAO;
import NC12.LupusInCampus.Model.Giocatore;
import NC12.LupusInCampus.Model.Utils.ComunicazioneClientServer.MessageResponse;
import NC12.LupusInCampus.Model.Utils.MessaggiErrore;
import NC12.LupusInCampus.Model.Utils.MessaggiSuccesso;
import NC12.LupusInCampus.Model.Utils.Validator;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("controller/giocatore")
public class GiocatoreController {

    private final GiocatoreDAO giocatoreDAO;

    @Autowired
    public GiocatoreController(GiocatoreDAO giocatoreDAO) {
        this.giocatoreDAO = giocatoreDAO;
    }

    @PostMapping("/registrazione")
    public ResponseEntity<?> registrazioneGiocatore(
            @RequestParam String nickname, @RequestParam String email, @RequestParam String password, HttpSession session) {

        List<String> errori = validaGiocatoreRegistrazione(nickname, email, password);

        if (!errori.isEmpty()) {
            // Invio messaggi di errore della registrazione
            MessageResponse response = new MessageResponse(
                    HttpStatus.UNAUTHORIZED.value(),
                    HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                    errori
            );

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // Se non ci sono errori
        Giocatore giocatore = new Giocatore();
        giocatore.setNickname(nickname);
        giocatore.setEmail(email);
        giocatore.setPassword(password);

        // salva
        Giocatore nuovoGiocatore = giocatoreDAO.save(giocatore);
        session.setAttribute("giocatore", nuovoGiocatore);

        // Invio dati giocatore
        MessageResponse response = new MessageResponse(
                HttpStatus.OK.value(),
                HttpStatus.OK.getReasonPhrase(),
                nuovoGiocatore
        );
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginGiocatore(@RequestParam String email, @RequestParam String password, HttpSession session) {

        List<String> errori = validaGiocatoreLogin(email, password);

        if (!errori.isEmpty()) {
            // Invio messaggi di errore del login
            MessageResponse response = new MessageResponse(
                    HttpStatus.UNAUTHORIZED.value(),
                    HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                    errori
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // Se non ci sono errori
        Giocatore giocatore = giocatoreDAO.findGiocatoreByEmail(email);
        session.setAttribute("giocatore", giocatore);

        // Invio dati giocatore
        MessageResponse response = new MessageResponse(
                HttpStatus.OK.value(),
                HttpStatus.OK.getReasonPhrase(),
                giocatore
        );
        return ResponseEntity.ok().body(response);

    }

    @PostMapping("/delete")
    public ResponseEntity<?> eliminaGiocatore(@RequestParam String id) {

        if (id.isEmpty() || id.isBlank()){

            MessageResponse response = new MessageResponse(
                    HttpStatus.UNAUTHORIZED.value(),
                    HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                    MessaggiErrore.ID_VUOTO.getMessage()
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        int id_giocatore = Integer.parseInt(id);
        Giocatore giocatore = giocatoreDAO.findGIocatoreById(id_giocatore);

        if (giocatore != null) {
            giocatoreDAO.delete(giocatore);

            MessageResponse response = new MessageResponse(
                    HttpStatus.OK.value(),
                    HttpStatus.OK.getReasonPhrase(),
                    MessaggiSuccesso.GIOCATORE_ELIMINATO.getMessage()
            );
            return ResponseEntity.ok().body(response);

        } else {
            MessageResponse response = new MessageResponse(
                    HttpStatus.UNAUTHORIZED.value(),
                    HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                    MessaggiErrore.GIOCATORE_INESISTENTE.getMessage()
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    public List<String> validaGiocatoreRegistrazione(String nickname, String email, String password){
        List<String> errori = new ArrayList<>();

        if (email.isBlank() || email.isEmpty())
            errori.add(MessaggiErrore.CAMPO_EMAIL_VUOTO.getMessage());
        else if (!Validator.isEmailValid(email))
            errori.add(MessaggiErrore.FORMATO_EMAIL.getMessage());
        else if (giocatoreDAO.findGiocatoreByEmail(email) != null)
            errori.add(MessaggiErrore.EMAIL_GIA_USATA.getMessage());


        if (password.isBlank() || password.isEmpty())
            errori.add(MessaggiErrore.CAMPO_PASSWORD_VUOTO.getMessage());


        if (nickname.isBlank() || nickname.isEmpty())
            errori.add(MessaggiErrore.CAMPO_NICKNAME_VUOTO.getMessage());
        else if (giocatoreDAO.findGiocatoreByNickname(nickname) != null)
            errori.add(MessaggiErrore.NICKNAME_GIA_USATO.getMessage());

        return errori;
    }

    public List<String> validaGiocatoreLogin(String email, String password){
        List<String> errori = new ArrayList<>();

        if (email.isBlank() || email.isEmpty())
            errori.add(MessaggiErrore.CAMPO_EMAIL_VUOTO.getMessage());
        else if (!Validator.isEmailValid(email))
            errori.add(MessaggiErrore.FORMATO_EMAIL.getMessage());
        else if (giocatoreDAO.findGiocatoreByEmail(email) == null)
            errori.add(MessaggiErrore.EMAIL_NON_REGISTRATA.getMessage());


        if (password.isBlank() || password.isEmpty())
            errori.add(MessaggiErrore.CAMPO_PASSWORD_VUOTO.getMessage());


        if (errori.isEmpty())
            if (!giocatoreDAO.existsGiocatoreByEmailAndPassword(email, password))
                errori.add(MessaggiErrore.CREDENZIALI_ERRATE.getMessage());

        return errori;
    }
}
