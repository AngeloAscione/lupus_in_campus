package NC12.LupusInCampus.service.emails;

import NC12.LupusInCampus.utils.JsonConfigReader;
import com.google.gson.JsonObject;

public class MailProperties {

    private String host;
    private int port;
    private String username;
    private String password;
    private String protocol;
    private boolean auth;
    private boolean sslEnable;
    private boolean starttlsEnable;


    private static MailProperties instance;
    public static MailProperties getInstance() {
        if (instance == null) {
            instance = new MailProperties();
        }
        return instance;
    }

    private MailProperties(){
        try {
            JsonObject o = JsonConfigReader.readFile("mail-config.json");

            this.host = o.get("host").getAsString();
            this.port = o.get("port").getAsInt();
            this.username = o.get("username").getAsString();
            this.password = o.get("password").getAsString();
            this.protocol = o.get("protocol").getAsString();
            this.auth = o.get("auth").getAsBoolean();
            this.sslEnable = o.get("sslEnable").getAsBoolean();
            this.starttlsEnable = o.get("starttlsEnable").getAsBoolean();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public boolean isAuth() {
        return auth;
    }

    public void setAuth(boolean auth) {
        this.auth = auth;
    }

    public boolean isSslEnable() {
        return sslEnable;
    }

    public void setSslEnable(boolean sslEnable) {
        this.sslEnable = sslEnable;
    }

    public boolean isStarttlsEnable() {
        return starttlsEnable;
    }

    public void setStarttlsEnable(boolean starttlsEnable) {
        this.starttlsEnable = starttlsEnable;
    }
}
