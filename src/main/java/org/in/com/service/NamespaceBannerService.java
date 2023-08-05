package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.NamespaceBannerDTO;

public interface NamespaceBannerService extends BaseService<NamespaceBannerDTO> {

	public NamespaceBannerDTO getNamespaceBanner(AuthDTO authDTO, NamespaceBannerDTO bannerDTO);

	public NamespaceBannerDTO updateBannerDetails(AuthDTO authDTO, NamespaceBannerDTO bannerDTO);

	public List<NamespaceBannerDTO> getActiveBanner(AuthDTO authDTO);
}
