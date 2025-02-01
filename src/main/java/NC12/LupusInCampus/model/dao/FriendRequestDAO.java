package NC12.LupusInCampus.model.dao;

import NC12.LupusInCampus.model.FriendRequest;
import NC12.LupusInCampus.model.FriendRequestPk;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendRequestDAO extends JpaRepository<FriendRequest, FriendRequestPk> {
    boolean existsFriendRequestByFriendRequestPk(FriendRequestPk friendRequestPk);
}
