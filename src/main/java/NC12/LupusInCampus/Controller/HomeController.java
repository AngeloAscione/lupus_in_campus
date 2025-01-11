package NC12.LupusInCampus.Controller;

import NC12.LupusInCampus.Model.DAO.GiocatoreDAO;
import NC12.LupusInCampus.Model.Giocatore;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("controller/home")
public class HomeController {

    private final GiocatoreDAO giocatoreDAO;

    @Autowired
    public HomeController(GiocatoreDAO giocatoreDAO) {
        this.giocatoreDAO = giocatoreDAO;
    }

    @GetMapping("/")
    public ResponseEntity<?> home(HttpSession session){

        Giocatore giocatore = (Giocatore) session.getAttribute("giocatore");

        if (giocatore == null)
            return ResponseEntity.badRequest().body("Giocatore nella sessione non trovato");

        Giocatore informazioniGiocatore = giocatoreDAO.findGIocatoreById(giocatore.getId());

        if (informazioniGiocatore == null)
            return ResponseEntity.badRequest().body("Giocatore non trovato");


        return ResponseEntity.ok(informazioniGiocatore);
    }
}
