package org.in.com.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.in.com.cache.CacheCentral;
import org.in.com.cache.EhcacheManager;
import org.in.com.cache.dto.TermCacheDTO;
import org.in.com.dao.TermsDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.TermDTO;
import org.in.com.dto.enumeration.TransactionTypeEM;
import org.in.com.service.TermsService;
import org.in.com.service.TripService;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.sf.ehcache.Element;

@Service
public class TermsImpl extends CacheCentral implements TermsService {
	private static String TERMS_KEY = "ALL_TERMS_KEY";

	@Autowired
	TripService tripService;

	// private static final Logger logger =
	// LoggerFactory.getLogger(TermsImpl.class);

	@Override
	public List<TermDTO> get(AuthDTO authDTO, TermDTO dto) {

		TermsDAO termsDAO = new TermsDAO();
		return termsDAO.getAllCancellationTerms(authDTO, dto);

	}

	@Override
	public List<TermDTO> getAll(AuthDTO authDTO) {
		return new ArrayList<TermDTO>();
	}

	@Override
	public TermDTO Update(AuthDTO authDTO, TermDTO termDTO) {
		TermsDAO termsDAO = new TermsDAO();
		termsDAO.getTermsIUD(authDTO, termDTO);
		removeTermDTO(authDTO, termDTO);
		return termDTO;
	}

	private void removeTermDTO(AuthDTO authDTO, TermDTO termDTO) {
		String key = TERMS_KEY + "_" + authDTO.getNamespace().getId();
		EhcacheManager.getTermEhCache().remove(key);
	}

	@Override
	public List<TermDTO> getTermsAndConditions(AuthDTO authDTO, TermDTO termDTO, ScheduleDTO scheduleDTO) {
		List<TermDTO> termList = getAllTermsAndConditions(authDTO);
		for (Iterator<TermDTO> iterator = termList.iterator(); iterator.hasNext();) {
			TermDTO term = iterator.next();
			if (scheduleDTO != null && termDTO.getTagList() != null || termDTO.getTagList() != null && scheduleDTO == null) {
				if (!term.getSchedule().isEmpty() && StringUtil.isNotNull(scheduleDTO.getCode()) && BitsUtil.isScheduleExistsV2(term.getSchedule(), scheduleDTO) == null) {
					iterator.remove();
					continue;
				}
				boolean isExist = BitsUtil.getTagList(term, termDTO);
				if (!isExist) {
					iterator.remove();
					continue;
				}
			}
			if (StringUtil.isNotNull(scheduleDTO.getCode()) && scheduleDTO != null && termDTO.getTagList() == null) {
				if (BitsUtil.isScheduleExistsV2(term.getSchedule(), scheduleDTO) == null) {
					iterator.remove();
					continue;
				}
			}
		}

		return termList;
	}

	private List<TermDTO> getAllTermsAndConditions(AuthDTO authDTO) {
		List<TermDTO> termList = null;
		List<TermCacheDTO> termCacheList = null;
		String key = TERMS_KEY + "_" + authDTO.getNamespace().getId();
		Element termElement = EhcacheManager.getTermEhCache().get(key);
		if (termElement != null) {
			termCacheList = (List<TermCacheDTO>) termElement.getObjectValue();
			termList = bindTermsAndConditionsFromCacheObject(termCacheList);
		}
		else {
			TermsDAO termsDAO = new TermsDAO();
			termList = termsDAO.getAllTermsAndConditions(authDTO);
			termCacheList = bindSpecialDiscountCriteriaToCacheObject(termList);
			EhcacheManager.getTermEhCache().put(new Element(key, termCacheList));
		}
		return termList;
	}

	private List<TermCacheDTO> bindSpecialDiscountCriteriaToCacheObject(List<TermDTO> termList) {
		List<TermCacheDTO> list = new ArrayList<>();
		if (termList != null && !termList.isEmpty()) {
			for (TermDTO term : termList) {
				TermCacheDTO termDTO1 = new TermCacheDTO();
				termDTO1.setCode(term.getCode());
				termDTO1.setName(term.getName());
				termDTO1.setSequenceId(term.getSequenceId());
				List<String> tagList = new ArrayList<String>();
				for (String strTag : term.getTagList()) {
					tagList.add(strTag);
				}
				termDTO1.setTag(tagList);
				termDTO1.setTransactionTypeCode(term.getTransactionType().getCode());
				List<ScheduleDTO> scheduleList = new ArrayList<>();
				if (term.getSchedule() != null && !term.getSchedule().isEmpty()) {
					for (ScheduleDTO schedule : term.getSchedule()) {
						ScheduleDTO dto = new ScheduleDTO();
						dto.setCode(schedule.getCode());
						scheduleList.add(dto);
					}
				}
				termDTO1.setScheduleCode(scheduleList);
				termDTO1.setActiveFlag(term.getActiveFlag());
				list.add(termDTO1);
			}
		}
		return list;
	}

	private List<TermDTO> bindTermsAndConditionsFromCacheObject(List<TermCacheDTO> termCacheList) {
		List<TermDTO> list = new ArrayList<>();
		if (termCacheList != null && !termCacheList.isEmpty()) {
			for (TermCacheDTO termCache : termCacheList) {
				TermDTO term = new TermDTO();
				term.setCode(termCache.getCode());
				term.setName(termCache.getName());
				term.setSequenceId(termCache.getSequenceId());
				List<String> tagList = new ArrayList<String>();
				for (String strTag : termCache.getTag()) {
					tagList.add(strTag);
				}
				term.setTagList(tagList);
				term.setTransactionType(TransactionTypeEM.getTransactionTypeEM(termCache.getTransactionTypeCode()));
				List<ScheduleDTO> scheduleList = new ArrayList<>();
				if (termCache.getScheduleCode() != null && !termCache.getScheduleCode().isEmpty()) {
					for (ScheduleDTO schedule : termCache.getScheduleCode()) {
						ScheduleDTO dto = new ScheduleDTO();
						dto.setCode(schedule.getCode());
						scheduleList.add(dto);
					}
				}
				term.setSchedule(scheduleList);
				term.setActiveFlag(termCache.getActiveFlag());
				list.add(term);
			}
		}
		return list;
	}

	@Override
	public List<TermDTO> getTermsAndConditions(AuthDTO authDTO, String tagValue) {

		List<TermDTO> termList = getAllTermsAndConditions(authDTO);
		for (Iterator<TermDTO> iterator = termList.iterator(); iterator.hasNext();) {
			TermDTO termDTO = iterator.next();
			String tag = BitsUtil.getTagValue(tagValue, termDTO);
			if (StringUtil.isNull(tag)) {
				iterator.remove();
				continue;
			}

		}
		return termList;
	}

}
