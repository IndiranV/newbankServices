package org.in.com.service.impl;

import java.util.List;

import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dao.ImageDetailsDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.CashbookTransactionDTO;
import org.in.com.dto.ImageDetailsDTO;
import org.in.com.dto.PaymentReceiptDTO;
import org.in.com.dto.enumeration.ImageCategoryEM;
import org.in.com.service.CashbookTransactionService;
import org.in.com.service.ImageDetailsService;
import org.in.com.service.PaymentTransactionService;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ImageDetailsServiceImpl implements ImageDetailsService {

	@Autowired
	PaymentTransactionService paymentTransactionService;
	@Autowired
	CashbookTransactionService cashbookTransactionService;

	public void updateImageDetails(AuthDTO authDTO, List<ImageDetailsDTO> imageDetailsList, String referenceCode, ImageCategoryEM imageCategory) {
		ImageDetailsDAO imageDetailsDAO = new ImageDetailsDAO();
		imageDetailsDAO.updateImageDetails(authDTO, imageDetailsList);

		String imageDetailsIds = getImageDetailsIds(imageDetailsList);
		if (imageCategory.getId() == ImageCategoryEM.PAYMENT_RECEIPT.getId()) {
			paymentTransactionService.updatePaymentReceiptImageDetails(authDTO, referenceCode, imageDetailsIds);
		}
		else if (imageCategory.getId() == ImageCategoryEM.CASHBOOK.getId()) {
			cashbookTransactionService.updateCashbookTransactionImageDetails(authDTO, referenceCode, imageDetailsIds);
		}
	}

	public List<ImageDetailsDTO> getImageDetails(AuthDTO authDTO, String referenceCode, ImageCategoryEM imageCategory) {
		ImageDetailsDAO imageDAO = new ImageDetailsDAO();
		List<ImageDetailsDTO> imageDetailsList = null;

		if (imageCategory.getId() == ImageCategoryEM.PAYMENT_RECEIPT.getId()) {
			PaymentReceiptDTO paymentReceiptDTO = new PaymentReceiptDTO();
			paymentReceiptDTO.setCode(referenceCode);

			paymentTransactionService.getPaymentReceipt(authDTO, paymentReceiptDTO);
			if (paymentReceiptDTO.getId() != Numeric.ZERO_INT) {
				imageDetailsList = paymentReceiptDTO.getImageDetails();
			}
		}
		else {
			CashbookTransactionDTO cashbookTransactionDTO = new CashbookTransactionDTO();
			cashbookTransactionDTO.setCode(referenceCode);
			cashbookTransactionService.getCashbookTransaction(authDTO, cashbookTransactionDTO);
			if (StringUtil.isNotNull(cashbookTransactionDTO.getImages())) {
				imageDetailsList = cashbookTransactionDTO.getImages();
			}
		}
		if (imageDetailsList != null && !imageDetailsList.isEmpty()) {
			imageDAO.getImageDetails(authDTO, imageDetailsList);
		}
		return imageDetailsList;
	}

	private String getImageDetailsIds(List<ImageDetailsDTO> imageDetailsList) {
		StringBuilder imageDetailsIds = new StringBuilder();
		for (ImageDetailsDTO imageDetailsDTO : imageDetailsList) {
			if (imageDetailsDTO.getId() == Numeric.ZERO_INT) {
				continue;
			}
			if (imageDetailsIds.length() > Numeric.ZERO_INT) {
				imageDetailsIds.append(Text.COMMA);
			}
			imageDetailsIds.append(imageDetailsDTO.getId());
		}
		return StringUtil.isNotNull(imageDetailsIds.toString()) ? imageDetailsIds.toString() : Text.NA;
	}

}
