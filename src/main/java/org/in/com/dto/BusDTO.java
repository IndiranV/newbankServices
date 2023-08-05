package org.in.com.dto;

import java.util.HashMap;
import java.util.Map;

import org.in.com.constants.Text;
import org.in.com.dto.enumeration.BusCategoryTypeEM;
import org.in.com.dto.enumeration.BusSeatTypeEM;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BusDTO extends BaseDTO<BusDTO> {
	private String categoryCode;
	private String displayName;
	private int seatCount;
	private BusSeatLayoutDTO busSeatLayoutDTO;

	public int getSeatLayoutCount() {
		if (busSeatLayoutDTO != null && busSeatLayoutDTO.getList() != null && !busSeatLayoutDTO.getList().isEmpty()) {
			return busSeatLayoutDTO.getList().size();
		}
		return seatCount;
	}

	public int getReservableLayoutSeatCount() {
		int reservableLayoutSeatCount = 0;
		if (busSeatLayoutDTO != null && busSeatLayoutDTO.getList() != null && !busSeatLayoutDTO.getList().isEmpty()) {
			for (BusSeatLayoutDTO layoutDTO : busSeatLayoutDTO.getList()) {
				if (layoutDTO.getBusSeatType().isReservation()) {
					reservableLayoutSeatCount++;
				}
			}
		}
		return reservableLayoutSeatCount;
	}

	public Map<String, BusSeatTypeEM> getUniqueBusType() {
		Map<String, BusSeatTypeEM> typeMap = new HashMap<String, BusSeatTypeEM>();
		for (BusSeatLayoutDTO layoutDTO : busSeatLayoutDTO.getList()) {
			typeMap.put(layoutDTO.getBusSeatType().getCode(), layoutDTO.getBusSeatType());
		}
		return typeMap;
	}

	public Map<String, BusSeatTypeEM> getUniqueReservableBusType() {
		Map<String, BusSeatTypeEM> typeMap = new HashMap<String, BusSeatTypeEM>();
		for (BusSeatLayoutDTO layoutDTO : busSeatLayoutDTO.getList()) {
			if (layoutDTO.getBusSeatType().isReservation()) {
				typeMap.put(layoutDTO.getBusSeatType().getCode(), layoutDTO.getBusSeatType());
			}
		}
		return typeMap;
	}

	public Map<String, BusSeatLayoutDTO> getBusSeatLayoutMapFromList() {
		Map<String, BusSeatLayoutDTO> seatMap = new HashMap<String, BusSeatLayoutDTO>();
		if (busSeatLayoutDTO != null && busSeatLayoutDTO.getList() != null) {
			for (BusSeatLayoutDTO seatLayoutDTO : busSeatLayoutDTO.getList()) {
				seatMap.put(seatLayoutDTO.getCode(), seatLayoutDTO);
			}
		}
		return seatMap;
	}

	public boolean checkLayoutCategory(BusCategoryTypeEM categoryType) {
		for (String category : categoryCode.split(Text.ESCAPE_CHARACTERS + Text.VERTICAL_BAR)) {
			if (category.equals(categoryType.getCode())) {
				return true;
			}
		}
		return false;
	}
}
