package org.in.com.controller.web;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.in.com.aggregator.aws.S3Service;
import org.in.com.config.ApplicationConfig;
import org.in.com.controller.web.io.ExportReportIO;
import org.in.com.controller.web.io.NamespaceIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.ExportReportDTO;
import org.in.com.dto.ExportReportDetailsDTO;
import org.in.com.dto.NamespaceDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.ExportReportService;
import org.in.com.service.NamespaceService;
import org.in.com.service.ReportQueryService;
import org.in.com.utils.BitsEnDecrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Controller
@RequestMapping("/{authtoken}/export/report")
public class ExportReportController extends BaseController {

	@Autowired
	ExportReportService exportReportService;
	@Autowired
	NamespaceService namespaceService;
	@Autowired
	ReportQueryService reportQueryService;
	@Autowired
	S3Service s3Service;

	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<ExportReportIO> updateExportReport(@PathVariable("authtoken") String authtoken, @RequestBody ExportReportIO exportReportIO) throws Exception {
		ExportReportIO reportIO = new ExportReportIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			ExportReportDTO reportDTO = new ExportReportDTO();
			reportDTO.setCode(exportReportIO.getCode());
			List<NamespaceDTO> namespaceList = new ArrayList<>();
			if (exportReportIO.getNamespace() != null) {
				for (NamespaceIO namespace : exportReportIO.getNamespace()) {
					NamespaceDTO namespaceDTO = new NamespaceDTO();
					namespaceDTO.setCode(namespace.getCode());
					namespaceList.add(namespaceDTO);
				}
			}
			reportDTO.setNamespace(namespaceList);
			reportDTO.setReportName(exportReportIO.getReportName());
			reportDTO.setReportCode(exportReportIO.getReportCode());
			reportDTO.setFrequency(exportReportIO.getFrequency());
			reportDTO.setFilterDateTypeFlag(exportReportIO.getFilterDateTypeFlag());
			reportDTO.setActiveFlag(exportReportIO.getActiveFlag());
			exportReportService.updateExportReport(authDTO, reportDTO);
			if (reportDTO.getCode() != null) {
				reportIO.setCode(reportDTO.getCode());
				reportIO.setActiveFlag(reportDTO.getActiveFlag());
			}
		}
		return ResponseIO.success(reportIO);
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<ExportReportIO>> getAllExportReport(@PathVariable("authtoken") String authtoken) throws Exception {
		List<ExportReportIO> reportList = new ArrayList<>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<ExportReportDTO> exportReportList = exportReportService.getAllExportReport(authDTO);
			for (ExportReportDTO reportDTO : exportReportList) {
				ExportReportIO reportIO = new ExportReportIO();
				reportIO.setCode(reportDTO.getCode());
				List<NamespaceIO> namespaceList = new ArrayList<>();
				for (NamespaceDTO namespace : reportDTO.getNamespace()) {
					NamespaceIO namespaceIO = new NamespaceIO();
					namespaceIO.setCode(namespace.getCode());
					namespaceIO.setName(namespace.getName());
					namespaceList.add(namespaceIO);
				}
				reportIO.setNamespace(namespaceList);
				reportIO.setReportName(reportDTO.getReportName());
				reportIO.setReportCode(reportDTO.getReportCode());
				reportIO.setFrequency(reportDTO.getFrequency());
				reportIO.setFilterDateTypeFlag(reportDTO.getFilterDateTypeFlag());
				reportIO.setActiveFlag(reportDTO.getActiveFlag());
				reportList.add(reportIO);
			}
		}
		return ResponseIO.success(reportList);
	}

	@RequestMapping(value = "/retrieve", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<String> retrieveReport(@PathVariable("authtoken") String authtoken, @RequestParam(required = true) String reportCode, String status) throws Exception {
		JsonObject json = null;
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		ExportReportDetailsDTO detailsDTO = reportQueryService.getReportDetailsByStatus(authDTO, status, reportCode);
		String data = BitsEnDecrypt.getDecoder(detailsDTO.getEncryptData());
		json = JsonParser.parseString(data).getAsJsonObject();

		if (!json.get("zoneCode").getAsString().equals(ApplicationConfig.getServerZoneCode())) {
			throw new ServiceException(ErrorCode.INVALID_CREDENTIALS);
		}
		URL url = s3Service.generatePresignedURL(authDTO, json);
		return ResponseIO.success(url.toString());
	}

}
