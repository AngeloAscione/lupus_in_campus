package NC12.LupusInCampus.Model.Utils.ComunicazioneClientServer;

import org.springframework.web.ErrorResponse;
import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

/*
* Interfaccia ApiService per definire i metodi delle chiamate HTTP utilizzate
* per comunicare con il backend
* Utilizza Retrofit per semplificare la gestione delle richeste REST
*
* Ogni metodo è un endpoint del backend
*
* */

public interface ApiService {

    // endpoint di registrazione
    @POST("controller/giocatore/registrazione")
    // ritorna nua chiamata (Call) di tipo MessageResponse (in Utils), contiene i dettagli della risposta dal server (errori, classi, ecc..).
    Call<MessageResponse> registrazione(@Query("nickname") String nickname, @Query("email") String email, @Query("password") String password);

    /* endpoint di login */
    @POST("controller/giocatore/login")
    // ritorna nua chiamata (Call) di tipo MessageResponse (in Utils), contiene i dettagli della risposta dal server (errori, classi, ecc..).

    Call<ErrorResponse> login(@Query("email") String email, @Query("password") String password);
}
