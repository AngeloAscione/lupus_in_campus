package NC12.LupusInCampus.Model.DAO;

import NC12.LupusInCampus.Model.Utils.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviceDAO extends JpaRepository<Device, Integer> {

    List<Device> findDevicesByPlayerID(int playerID);
}
