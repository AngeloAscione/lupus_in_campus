package NC12.LupusInCampus.utils.comunicazioneClientServer;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/*
* Instanza di Retrofit
*
* Retrofit facilita la gestione delle richieste HTTP, facendo comunicare client e server
*
* */

public class RetrofitClient {

    private static final String BASE_URL = "http://localhost:8080";

    public static Retrofit getRetrofitInstance() {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())  // Converte json (risposte del server)
                                                                        // in oggetti
                .build();
    }

    /*
    * Implementazione di ApiService per chiamare gli endpoint
    *
    * */
    public static ApiService getApiService() {
        return getRetrofitInstance().create(ApiService.class);  // Crea l'implementazione di ApiService
    }
}
