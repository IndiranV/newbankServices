package org.in.com.controller.web.io;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExportReportIO extends BaseIO {
	public List<NamespaceIO> namespace;
	public String reportName;
	public String frequency;
	public String reportCode;
	public int filterDateTypeFlag;
}
