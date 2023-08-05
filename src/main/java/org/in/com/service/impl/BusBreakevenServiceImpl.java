package org.in.com.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.in.com.aggregator.aws.S3ServiceImpl;
import org.in.com.cache.BusBreakevenCache;
import org.in.com.cache.EhcacheManager;
import org.in.com.constants.Constants;
import org.in.com.dao.BusBreakevenDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusBreakevenSettingsDTO;
import org.in.com.dto.ScheduleBusDTO;
import org.in.com.dto.StateDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.TripDTO;
import org.in.com.service.BusBreakevenService;
import org.in.com.service.BusService;
import org.in.com.utils.BitsEnDecrypt;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hirondelle.date4j.DateTime;
import net.sf.ehcache.Element;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
public class BusBreakevenServiceImpl extends BusBreakevenCache implements BusBreakevenService {
	private static final String TAX = "tax";
	private static final String EXPENSE = "expense";
	private static final String VALUE = "value";

	@Autowired
	BusService busServivce;

	@Override
	public BusBreakevenSettingsDTO getBreakeven(AuthDTO authDTO, BusBreakevenSettingsDTO breakevenSettingsDTO) {
		if (breakevenSettingsDTO.getId() != 0) {
			getBusBreakevenById(authDTO, breakevenSettingsDTO);
		}
		else if (StringUtil.isNotNull(breakevenSettingsDTO.getCode())) {
			getBusBreakeven(authDTO, breakevenSettingsDTO);
		}
		if (breakevenSettingsDTO.getBus() != null && breakevenSettingsDTO.getBus().getId() != 0) {
			breakevenSettingsDTO.setBus(busServivce.getBus(authDTO, breakevenSettingsDTO.getBus()));
		}
		return breakevenSettingsDTO;
	}

	@Override
	public void updateBreakevenSettings(AuthDTO authDTO, BusBreakevenSettingsDTO breakevenSettingsDTO) {
		BusBreakevenDAO dao = new BusBreakevenDAO();
		breakevenSettingsDTO.setBus(busServivce.getBus(authDTO, breakevenSettingsDTO.getBus()));
		dao.updateBreakevenSettings(authDTO, breakevenSettingsDTO);
		// clear cache
		removeBusBreakeven(authDTO, breakevenSettingsDTO);
	}

	@Override
	public BusBreakevenSettingsDTO getBreakevenSettings(AuthDTO authDTO, BusBreakevenSettingsDTO breakevenSettingsDTO) {
		BusBreakevenDAO dao = new BusBreakevenDAO();
		dao.getBreakevenSettingsDetails(authDTO, breakevenSettingsDTO);
		if (breakevenSettingsDTO.getBus() != null && breakevenSettingsDTO.getBus().getId() != 0) {
			breakevenSettingsDTO.setBus(busServivce.getBus(authDTO, breakevenSettingsDTO.getBus()));
		}
		return breakevenSettingsDTO;
	}

	@Override
	public List<BusBreakevenSettingsDTO> getAllBreakevenSettings(AuthDTO authDTO) {
		BusBreakevenDAO dao = new BusBreakevenDAO();
		List<BusBreakevenSettingsDTO> list = dao.getAllBreakevenSettings(authDTO);
		for (BusBreakevenSettingsDTO breakevenSettings : list) {
			if (breakevenSettings.getBus().getId() != 0) {
				breakevenSettings.setBus(busServivce.getBus(authDTO, breakevenSettings.getBus()));
			}
		}
		return list;
	}

	public JSONObject processTripBreakeven(AuthDTO authDTO, ScheduleBusDTO scheduleBus, TripDTO tripDTO, StationDTO fuelStation) {
		JSONObject breakevenJson = new JSONObject();
		try {
			BusBreakevenSettingsDTO breakevenDTO = getBreakeven(authDTO, scheduleBus.getBreakevenSettings());
			if (breakevenDTO.getBreakevenDetails() != null) {
				JSONObject repoBreakeven = breakevenDTO.getBreakevenDetails();

				float mileage = repoBreakeven.has("mileage") ? Float.parseFloat(repoBreakeven.getString("mileage")) : 0;
				float distance = scheduleBus.getDistance();

				// Previous Fuel Date check
				DateTime fuelDate = DateUtil.NOW();
				if (tripDTO.getTripDate().getStartOfDay().lt(fuelDate.getStartOfDay())) {
					fuelDate = tripDTO.getTripDate();
				}
				BigDecimal perLitreCost = getFuelPrice(authDTO, fuelStation.getState(), fuelDate);
				BigDecimal seatCount = StringUtil.getBigDecimalValue(String.valueOf(tripDTO.getBus().getSeatLayoutCount()));

				JSONObject fuelJson = new JSONObject();
				fuelJson.put("city", fuelStation.getName());
				fuelJson.put(VALUE, perLitreCost);

				// calculate Fuel Cost
				BigDecimal totalFuelCost = BigDecimal.ZERO;
				if (distance > 0) {
					float avgDistance = distance / mileage;
					totalFuelCost = totalFuelCost.add(StringUtil.getBigDecimalValue(String.valueOf(avgDistance)).multiply(perLitreCost));
				}
				breakevenJson.put("fuelExpense", totalFuelCost.setScale(2, RoundingMode.HALF_UP));
				breakevenJson.put("fuel", fuelJson);

				// Calculate tax
				BigDecimal totalTax = BigDecimal.ZERO;
				if (repoBreakeven.has(TAX) && !repoBreakeven.getJSONArray(TAX).isEmpty()) {
					for (Object taxObject : repoBreakeven.getJSONArray(TAX)) {
						JSONObject taxJson = (JSONObject) taxObject;

						BigDecimal taxCost = BigDecimal.ZERO;
						BigDecimal value = taxJson.has(VALUE) ? StringUtil.getBigDecimalValue(taxJson.getString(VALUE)) : BigDecimal.ZERO;
						String type = taxJson.getString("type");

						if (type.equals("SEAT")) {
							taxCost = taxCost.add(value.multiply(seatCount));
						}
						else if (type.equals("BOSEAT") && tripDTO.getBookedSeatCount() > 0) {
							taxCost = taxCost.add(value.multiply(new BigDecimal(tripDTO.getBookedSeatCount())));
						}
						else if (type.equals("KM")) {
							taxCost = taxCost.add(value.multiply(StringUtil.getBigDecimalValue(String.valueOf(distance))));
						}
						else if (type.equals("DAY")) {
							taxCost = taxCost.add(value);
						}
						totalTax = totalTax.add(taxCost);
					}
					breakevenJson.put(TAX, totalTax);
				}

				// Calculate Expenses
				BigDecimal totalExpense = BigDecimal.ZERO;
				if (repoBreakeven.has(EXPENSE) && !repoBreakeven.getJSONArray(EXPENSE).isEmpty()) {
					for (Object expenseObject : repoBreakeven.getJSONArray(EXPENSE)) {
						JSONObject expenseJson = (JSONObject) expenseObject;
						BigDecimal expenseCost = BigDecimal.ZERO;
						BigDecimal value = expenseJson.has(VALUE) ? StringUtil.getBigDecimalValue(expenseJson.getString(VALUE)) : BigDecimal.ZERO;
						String type = expenseJson.getString("type");

						if (type.equals("SEAT")) {
							expenseCost = expenseCost.add(value.multiply(seatCount));
						}
						else if (type.equals("BOSEAT") && tripDTO.getBookedSeatCount() > 0) {
							expenseCost = expenseCost.add(value.multiply(new BigDecimal(tripDTO.getBookedSeatCount())));
						}
						else if (type.equals("KM")) {
							expenseCost = expenseCost.add(value.multiply(StringUtil.getBigDecimalValue(String.valueOf(distance))));
						}
						else if (type.equals("DAY")) {
							expenseCost = expenseCost.add(value);
						}
						totalExpense = totalExpense.add(expenseCost);
					}
				}
				breakevenJson.put(EXPENSE, totalExpense.setScale(2, RoundingMode.HALF_UP));

			}
			tripDTO.setBreakeven(breakevenJson);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return breakevenJson;
	}

	public void processBreakevenToTripBreakeven(AuthDTO authDTO, ScheduleBusDTO scheduleBus, TripDTO tripDTO, StationDTO fuelStation) {
		try {
			BusBreakevenSettingsDTO breakevenDTO = getBreakeven(authDTO, scheduleBus.getBreakevenSettings());
			if (breakevenDTO.getBreakevenDetails() != null) {
				String breakevenKey = BitsEnDecrypt.getSHA256Hash(breakevenDTO.getBreakeven().toString());
				breakevenDTO.setBreakevenKey(breakevenKey);

				BusBreakevenDAO breakevenDAO = new BusBreakevenDAO();
				breakevenDAO.updateTripBreakeven(authDTO, breakevenDTO);
			}
			BigDecimal fuelPrice = getFuelPrice(authDTO, fuelStation.getState(), tripDTO.getTripDate());
			breakevenDTO.setFuelPrice(fuelPrice.doubleValue());
			tripDTO.getTripInfo().setTripBreakeven(breakevenDTO);
			tripDTO.getTripInfo().setDistance(scheduleBus.getDistance());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private BigDecimal getFuelPrice(AuthDTO authDTO, StateDTO stateDTO, DateTime fuelDate) {
		BigDecimal fuelPrice = null;
		Map<String, String> stateFuelMap = getAllStateFuelprice(authDTO, fuelDate);
		if (!stateFuelMap.isEmpty() && stateFuelMap.containsKey(stateDTO.getCode())) {
			fuelPrice = StringUtil.getBigDecimalValue(stateFuelMap.get(stateDTO.getCode()));
		}
		else {
			fuelPrice = StringUtil.getBigDecimalValue(Constants.DEFAULT_FUEL_COST);
		}
		return fuelPrice;
	}

	public Map<String, String> getAllStageFuelPrice(AuthDTO authDTO, DateTime fuelDate) {
		Map<String, String> stateFuelMap = getAllStateFuelprice(authDTO, fuelDate);
		return stateFuelMap;
	}

	private Map<String, String> getAllStateFuelprice(AuthDTO authDTO, DateTime fuelDate) {
		Map<String, String> fuelPriceMap = new HashMap<String, String>();
		String key = "fuel-price-" + DateUtil.convertDate(fuelDate);
		Element element = EhcacheManager.getStateFuelEhCache().get(key);
		if (element != null) {
			Map<String, String> priceMap = (Map<String, String>) element.getObjectValue();
			fuelPriceMap.putAll(priceMap);
		}
		else if (element == null) {
			int i = 0;
			while (i < 10) {
				try {
					S3ServiceImpl serviceImpl = new S3ServiceImpl();
					Map<String, String> priceMap = serviceImpl.getAllStateFuelPrice(authDTO, fuelDate);
					fuelPriceMap.putAll(priceMap);
					break;
				}
				catch (Exception e) {
					i++;
					fuelDate = fuelDate.minusDays(1);
				}
			}
			EhcacheManager.getStateFuelEhCache().put(new Element(key, fuelPriceMap));
		}

		return fuelPriceMap;
	}

	@Override
	public JSONArray getBreakevenExpenses(AuthDTO authDTO) {
		JSONArray expenses = new JSONArray();
		BusBreakevenDAO dao = new BusBreakevenDAO();
		List<BusBreakevenSettingsDTO> breakevenSettingsList = dao.getAllBreakevenSettings(authDTO);

		Map<String, JSONObject> expensesMap = new HashMap<>();
		for (BusBreakevenSettingsDTO breakevenSettings : breakevenSettingsList) {
			JSONObject jsonObject = breakevenSettings.getBreakevenDetails();

			for (Object object : jsonObject.getJSONArray("tax")) {
				JSONObject taxJson = (JSONObject) object;
				String key = StringUtil.substring(StringUtil.removeSymbol(taxJson.getString("name").replaceAll(" ", "")), 15).toUpperCase();
				if (expensesMap.get(key) == null) {
					JSONObject expenseJson = new JSONObject();
					expenseJson.put("actionCode", key);
					expenseJson.put("name", taxJson.getString("name"));
					expenses.add(expenseJson);
					expensesMap.put(key, expenseJson);
				}
			}

			for (Object object : jsonObject.getJSONArray("expense")) {
				JSONObject expenseTypeJson = (JSONObject) object;
				String key = StringUtil.substring(StringUtil.removeSymbol(expenseTypeJson.getString("name").replaceAll(" ", "")), 15).toUpperCase();
				if (expensesMap.get(key) == null) {
					JSONObject expenseJson = new JSONObject();
					expenseJson.put("actionCode", key);
					expenseJson.put("name", expenseTypeJson.getString("name"));
					expenses.add(expenseJson);
					expensesMap.put(key, expenseJson);
				}
			}
		}
		return expenses;
	}
}
