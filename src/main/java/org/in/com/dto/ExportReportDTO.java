package org.in.com.dto;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExportReportDTO extends BaseDTO<ExportReportDTO> {
	public List<NamespaceDTO> namespace;
	public String reportName;
	public String frequency;
	public String reportCode;
	public int filterDateTypeFlag;
}
