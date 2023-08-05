package org.in.com.controller.pg;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class PaymentStatusController extends AbstractController {

	public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

		ModelAndView mav = new ModelAndView("paymentStatus");
		if (request.getParameter("status") != null && request.getParameter("status").toString().equalsIgnoreCase("success"))
			mav.addObject("message", "Payment made sucessfully");
		else
			mav.addObject("message", "Payment failed");
		return mav;
	}

}