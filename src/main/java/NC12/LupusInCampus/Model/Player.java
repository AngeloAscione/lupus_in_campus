package NC12.LupusInCampus.Model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "giocatore")
public class Player {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "nickname", unique=true, nullable=false)
    private String nickname;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "pass", nullable = false)
    private String password;

    @Transient
    private List<Player> friendsList;

    @Transient
    private String role;


    public Player(){
        this.friendsList = new ArrayList<>();
    }

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

    public List<Player> getFriendsList() {
        return friendsList;
    }

    public void setFriendsList(List<Player> friendsList) {
        this.friendsList = friendsList;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "Player{" +
                "id=" + id +
                ", nickname='" + nickname + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", friendsList=" + friendsList +
                ", role='" + role + '\'' +
                '}';
    }
}
