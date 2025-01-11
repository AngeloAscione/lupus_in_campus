package NC12.LupusInCampus.Controller;

import NC12.LupusInCampus.Model.DAO.GiocatoreDAO;
import NC12.LupusInCampus.Model.Giocatore;
import NC12.LupusInCampus.Model.Utils.Validator;
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

    @PostMapping
    public ResponseEntity<?> aggiungiGiocatore(
            @RequestParam String nickname, @RequestParam String email, @RequestParam String password) {

        List<String> errori = validaGiocatore(nickname, email, password);

        if (!errori.isEmpty()) {
            return ResponseEntity.badRequest().body(errori);
        }

        Giocatore giocatore = new Giocatore();
        giocatore.setNickname(nickname);
        giocatore.setEmail(email);
        giocatore.setPassword(password);

        // Altrimenti, salva il giocatore e restituiscilo
        Giocatore nuovoGiocatore = giocatoreDAO.save(giocatore);
        return ResponseEntity.ok(nuovoGiocatore);
    }

    @PostMapping("/delete")
    public ResponseEntity<?> eliminaGiocatore(@RequestParam String id) {
        
        if (id.isEmpty() || id.isBlank()){
            return ResponseEntity.badRequest().body("ID vuoto");
        }

        int id_giocatore = Integer.parseInt(id);
        Giocatore giocatore = giocatoreDAO.findGIocatoreById(id_giocatore);

        if (giocatore != null) {
            giocatoreDAO.delete(giocatore);
            return ResponseEntity.ok("Giocatore eliminato con successo");
        } else {
            return ResponseEntity.badRequest().body("Giocatore non trovato");
        }
    }

    public List<String> validaGiocatore(String nickname, String email, String password){
        List<String> errori = new ArrayList<>();
        
        if (email.isBlank() || email.isEmpty()){
            errori.add("Il campo email non può essere vuoto");
        } else if (!Validator.isEmailValid(email)) {
            errori.add("Email non corrisponde al formato");
        }else if (giocatoreDAO.findGiocatoreByEmail(email) != null) {
            errori.add("Email già in uso");
        }

        if (password.isBlank() || password.isEmpty()){
            errori.add("Il campo password non può essere vuoto");
        }

        if (nickname.isBlank() || nickname.isEmpty()){
            errori.add("Il campo nickname non può essere vuoto");
        } else if (giocatoreDAO.findGiocatoreByNickname(nickname) != null) {
            errori.add("Nickname già in uso");
        }

        return errori;
    }
}
