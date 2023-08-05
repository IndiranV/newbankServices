/**
 *
 */
package org.in.com.utils;

import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.in.com.config.ApplicationConfig;
import org.in.com.constants.Constants;
import org.in.com.dto.NotificationTemplateConfigDTO;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.StringTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;

public class TemplateUtils {

	public Configuration getSMSConfiguration() throws Exception {
		Configuration cfg = new Configuration();
		ClassTemplateLoader ftl1 = new ClassTemplateLoader(this.getClass(), "/templates/sms");
		List<TemplateLoader> arraylist = new ArrayList<TemplateLoader>();
		arraylist.add(ftl1);
		if (Constants.SERVER_ENV_LINUX.equals(ApplicationConfig.getServerEnv())) {
			FileTemplateLoader ftl2 = new FileTemplateLoader(new File("/usr/tomcat/templates/sms"));
			arraylist.add(ftl2);
		}
		MultiTemplateLoader mtl = new MultiTemplateLoader(arraylist.toArray(new TemplateLoader[arraylist.size()]));
		cfg.setTemplateLoader(mtl);
		return cfg;
	}

	public Configuration getEmailConfiguration() throws Exception {

		Configuration cfg = new Configuration();
		ClassTemplateLoader ftl1 = new ClassTemplateLoader(this.getClass(), "/templates/email");
		List<TemplateLoader> arraylist = new ArrayList<TemplateLoader>();
		arraylist.add(ftl1);
		if (Constants.SERVER_ENV_LINUX.equals(ApplicationConfig.getServerEnv())) {
			FileTemplateLoader ftl2 = new FileTemplateLoader(new File("/usr/tomcat/templates/email"));
			arraylist.add(ftl2);
		}
		MultiTemplateLoader mtl = new MultiTemplateLoader(arraylist.toArray(new TemplateLoader[arraylist.size()]));
		cfg.setTemplateLoader(mtl);
		return cfg;
	}

	public String processFileContent(String ftlName, Object dataModel) throws Exception {
		Configuration cfg = getSMSConfiguration();
		Template template = cfg.getTemplate(ftlName + ".ftl");
		Writer writer = new StringWriter();
		template.process(dataModel, writer);
		String content = writer.toString();
		return content;
	}

	public String processDynamicContent(NotificationTemplateConfigDTO templateConfig, Object dataModel) throws Exception {
		Configuration cfg = new Configuration();
		StringTemplateLoader stringLoader = new StringTemplateLoader();
		String templateName = templateConfig.getCode();
		stringLoader.putTemplate(templateConfig.getCode(), templateConfig.getContent());
		cfg.setTemplateLoader(stringLoader);
		Template template = cfg.getTemplate(templateName, "UTF-8");
		Writer writer = new StringWriter();
		template.process(dataModel, writer);
		String content = writer.toString();
		return content;
	}

	public String processEmailContent(String ftlName, Map<String, Object> dataModel) throws Exception {
		Configuration cfg = getEmailConfiguration();
		Template template = cfg.getTemplate(ftlName + ".ftl");
		Writer writer = new StringWriter();
		template.process(dataModel, writer);
		String content = writer.toString();
		return content;
	}

	public String processEmailSubject(String subject) throws Exception {
		Map<String, String> subjectMap = new HashMap<String, String>();
		return processEmailSubject(subject, subjectMap);
	}

	public String processEmailSubject(String subject, Map<String, String> subjectMap) throws Exception {
		Configuration cfg = getEmailConfiguration();
		subjectMap.put("subject", subject);
		Template template = cfg.getTemplate("subjects.ftl");
		Writer writer = new StringWriter();
		template.process(subjectMap, writer);
		return writer.toString();
	}
	
	public static TemplateUtils getInstance() {
		return new TemplateUtils();
	}
}
