package NC12.LupusInCampus.Controller;

import NC12.LupusInCampus.Model.Giocatore;
import NC12.LupusInCampus.Repository.GiocatoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("controller/giocatore")
public class GiocatoreController {

    private final GiocatoreRepository giocatoreRepository;

    @Autowired
    public GiocatoreController(GiocatoreRepository giocatoreRepository) {
        this.giocatoreRepository = giocatoreRepository;
    }

    @PostMapping
    public Giocatore aggiungiGiocatore(
            @RequestParam String nickname, @RequestParam String email, @RequestParam String password) {

        Giocatore giocatore = new Giocatore();

        return giocatoreRepository.save(giocatore);
    }
}
