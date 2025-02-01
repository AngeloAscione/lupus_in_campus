package NC12.LupusInCampus.model.dao;

import NC12.LupusInCampus.model.Device;
import NC12.LupusInCampus.model.DevicePk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviceDAO extends JpaRepository<Device, DevicePk> {
    List<Device> findDevicesByDevicePk(DevicePk devicePk);
}
