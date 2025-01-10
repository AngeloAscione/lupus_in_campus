package NC12.LupusInCampus.Model;

import jakarta.persistence.*;



@Entity
@Table(name = "giocatore")
public class Giocatore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "nickname", unique=true, nullable=false)
    private String nickname;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "pass", nullable = false)
    private String password;

    public Giocatore(){}

    public int getId() {
        return id;
    }

    public String getNickname() {
        return nickname;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "Giocatore{" +
                "id=" + id +
                ", nickname='" + nickname + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
