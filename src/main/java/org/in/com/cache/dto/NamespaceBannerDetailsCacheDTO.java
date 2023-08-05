package org.in.com.cache.dto;

public class NamespaceBannerDetailsCacheDTO {
	private String code;
	private String mediaTypeCode;
	private String url;
	private String redirectUrl;
	private String alternateText;
	private int sequence;
	private int activeFlag;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
	public String getMediaTypeCode() {
		return mediaTypeCode;
	}

	public void setMediaTypeCode(String mediaTypeCode) {
		this.mediaTypeCode = mediaTypeCode;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getRedirectUrl() {
		return redirectUrl;
	}

	public void setRedirectUrl(String redirectUrl) {
		this.redirectUrl = redirectUrl;
	}

	public String getAlternateText() {
		return alternateText;
	}

	public void setAlternateText(String alternateText) {
		this.alternateText = alternateText;
	}

	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	public int getActiveFlag() {
		return activeFlag;
	}

	public void setActiveFlag(int activeFlag) {
		this.activeFlag = activeFlag;
	}

}
