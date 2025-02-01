package NC12.LupusInCampus.Model.DAO;

import NC12.LupusInCampus.Model.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendRequestDAO extends JpaRepository<FriendRequest, Integer> {
    boolean existsFriendRequestByReceiverIdAndSenderId(int receiverId, int senderId);
}
