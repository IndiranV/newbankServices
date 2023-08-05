package org.in.com.utils;

import java.io.FileInputStream;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.ServiceUI;
import javax.print.SimpleDoc;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.HashDocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;

// https://stackoverflow.com/questions/23695900/printing-an-html-file-using-java-without-showing-print-dialog-to-the-user
public class Printing {
	public static void main(String args[]) throws Exception {
		// String filename = args[0];
		PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
		DocFlavor flavor = DocFlavor.INPUT_STREAM.PNG;
		PrintService printService[] = PrintServiceLookup.lookupPrintServices(flavor, pras);
		PrintService defaultService = printService[6]; // PrintServiceLookup.lookupDefaultPrintService();
		// PrintService service = ServiceUI.printDialog(null, 200, 200,
		// printService, defaultService, flavor, pras);
		if (defaultService != null) {
			DocPrintJob job = defaultService.createPrintJob();
			FileInputStream fis = new FileInputStream("C:\\Users\\java\\Desktop\\TNEB Oct 2017 Online Payment.pdf");
			DocAttributeSet das = new HashDocAttributeSet();
			Doc doc = new SimpleDoc(fis, flavor, das);
			job.print(doc, pras);
			Thread.sleep(10000);
		}
		System.exit(0);
	}
}
