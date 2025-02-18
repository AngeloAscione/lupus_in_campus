package NC12.LupusInCampus.service;


import NC12.LupusInCampus.model.Player;
import org.springframework.stereotype.Component;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ListPlayersLobbiesService {
    //private static final Map<Integer, List<Player>> lobbyLists = new ConcurrentHashMap<>();ù
    private static final Map<Integer, List<Player>> lobbyLists = new HashMap<>();


    public List<Player> getListPlayers(int id) {
        return lobbyLists.get(id);
    }

    public void addLobbyCode(int code){
        lobbyLists.putIfAbsent(code, new ArrayList<>());
    }

    public void removeLobbyCode(int code){
        lobbyLists.remove(code);
    }

    public void addPlayer(Player player, int lobbyCode) {
        lobbyLists.get(lobbyCode).add(player);
    }

    public void removePlayer(Player player, int lobbyCode) {
        lobbyLists.get(lobbyCode).remove(player);
    }

    public boolean containsCode(int code){
        return lobbyLists.containsKey(code);
    }

    public boolean containsPlayer(int code, Player player){
        return lobbyLists.get(code).contains(player);
    }

    public int getListSize(){
        return lobbyLists.size();
    }

}
