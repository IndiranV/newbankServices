package org.in.com.service;

import java.util.Collection;
import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusVehicleDTO;
import org.in.com.dto.BusVehicleDriverDTO;
import org.in.com.dto.TabletDTO;

public interface TabletService extends BaseService<TabletDTO> {

	public String generateAuthorizePIN(AuthDTO authDTO, String tabletCode);

	public Collection<TabletDTO> getRegisterPending(AuthDTO authDTO);

	public TabletDTO registerNewTablet(AuthDTO authDTO, TabletDTO tabletDTO);

	public Collection<TabletDTO> getAllTablets(AuthDTO authDTO);

	public void tabletVehicleMapping(AuthDTO authDTO, TabletDTO tabletDTO);

	public TabletDTO getTablet(AuthDTO authDTO, String tabletCode);

	public void deleteTablet(AuthDTO authDTO, TabletDTO tabletDTO);

	public void updateTablet(AuthDTO authDTO, TabletDTO tabletDTO);

	public TabletDTO deRegisterTablet(AuthDTO authDTO, TabletDTO tabletDTO);

	public void generateOTP(AuthDTO authDTO, TabletDTO tabletDTO);

	public void verifyDeviceMobile(AuthDTO authDTO, TabletDTO tabletDTO, int otp);

	public List<TabletDTO> getTablet(AuthDTO authDTO, BusVehicleDTO busVehicle);
	
	public String generateDriverAuthorizePIN(AuthDTO authDTO, BusVehicleDriverDTO busVehicleDriver);
	
	public TabletDTO saveDriverApp(AuthDTO authDTO, TabletDTO tabletDTO);
}
