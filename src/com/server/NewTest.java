package com.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorker;
import com.itextpdf.tool.xml.exceptions.RuntimeWorkerException;
import com.itextpdf.tool.xml.XMLWorkerFontProvider;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.itextpdf.tool.xml.css.StyleAttrCSSResolver;
import com.itextpdf.tool.xml.html.CssAppliersImpl;
import com.itextpdf.tool.xml.html.Tags;
import com.itextpdf.tool.xml.parser.XMLParser;
import com.itextpdf.tool.xml.pipeline.css.CssResolverPipeline;
import com.itextpdf.tool.xml.pipeline.end.PdfWriterPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipelineContext;
import com.server.Printer;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.Files;

import javax.print.DocFlavor;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.AttributeSet;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;

public class NewTest {
	public static final String HTML = "c:\\Temp1\\output.html";
	public String PDF = "c:\\Temp1\\";
	public static final String FONTFORMAT = "c:\\Temp1\\font.ttf";
	public static final String CSSSTYLE = "c:\\Temp1\\css1.css";

	public static void main(String[] args) throws Exception {
		NewTest server = new NewTest();
		server.run();
	}

	private void run() {
		ServerSocket socket = null;
		try {
			socket = new ServerSocket(7655);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Server is  running");

		while (true) {
			try {
				Socket 	sock = socket.accept();
				InputStreamReader IR = new InputStreamReader(sock.getInputStream());
				new PrintStream(sock.getOutputStream());
				BufferedReader bufferedReader = new BufferedReader(IR);

				while (bufferedReader.readLine().length() != 0) {

				}

				StringBuilder payload = new StringBuilder();

				while (bufferedReader.ready()) {
					payload.append((char) bufferedReader.read());
				}

				BufferedWriter out= new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
				ObjectMapper mapper = new ObjectMapper();
				if (payload.length() > 1) {
					Printer printer = (Printer) mapper.readValue(payload.toString(), Printer.class);
					FileWriter fWriter = new FileWriter(HTML);
					BufferedWriter writer = new BufferedWriter(fWriter);
					writer.write(printer.content);
					writer.close();
					String fileName = PDF + printer.uid + ".pdf";

					try {
						printPdf(fileName);
						print(printer.nameOfPrinter, printer.noOfCopies, fileName);

						out.write("HTTP/1.0 200 OK\r\n");
						out.write("Access-Control-Allow-Origin: *\r\n");
						out.write("Access-Control-Allow-Headers: Authorization\r\n");
						out.write("Access-Control-Allow-Headers: Content-type\r\n");
						out.write("Content-type: text/plain\r\n");
						out.write("s");
						out.close();
						sock.close();
						File file = new File(fileName);
						Files.deleteIfExists(file.toPath());
					} catch (IOException | RuntimeWorkerException
							arg11) {
						arg11.printStackTrace();
						out.write("HTTP/1.0 500 ERROR\r\n");
						out.write("\r\n");
						out.write("<TITLE>Print Utility</TITLE>");
						out.write(arg11.toString());
						out.close();
						sock.close();
					}
				} else {
					out.write("HTTP/1.0 200 OK\r\n");
					out.write("Access-Control-Allow-Origin: *\r\n");
					out.write("Access-Control-Allow-Headers: Authorization\r\n");
					out.write("Access-Control-Allow-Headers: Content-type\r\n");
					out.write("s");
					out.close();
					sock.close();
				}
			} catch (IOException arg11) {
			
			}

		}
	}

	private static void printPdf(String pdfName)  {
		File file = new File(pdfName);
		file.getParentFile().mkdirs();
		Rectangle pagesize = new Rectangle(135.0F, 590.0F);
		Document document = new Document(pagesize, 0F, 0F, 0F, 15F);
		PdfWriter writer;
		try {
			writer = PdfWriter.getInstance(document, new FileOutputStream(file));
			document.open();
			StyleAttrCSSResolver cssResolver = new StyleAttrCSSResolver();
			cssResolver.addCss(XMLWorkerHelper.getCSS(new FileInputStream(CSSSTYLE)));
			XMLWorkerFontProvider fontProvider = new XMLWorkerFontProvider("?");
			fontProvider.register(FONTFORMAT);
			CssAppliersImpl cssAppliers = new CssAppliersImpl(fontProvider);
			HtmlPipelineContext htmlContext = new HtmlPipelineContext(cssAppliers);
			htmlContext.setTagFactory(Tags.getHtmlTagProcessorFactory());
			PdfWriterPipeline pdf = new PdfWriterPipeline(document, writer);
			HtmlPipeline html = new HtmlPipeline(htmlContext, pdf);
			CssResolverPipeline css = new CssResolverPipeline(cssResolver, html);
			XMLWorker worker = new XMLWorker(css, true);
			XMLParser p = new XMLParser(worker);
			p.parse(new FileInputStream(HTML), Charset.forName("UTF-8"));
			document.close();
		} catch (IOException | DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	}

	private static void print(String nameOFPrinter, int noOFCopies, String pdf)
		 {
		File file = new File(pdf);
		PDDocument document;
		try {
			document = PDDocument.load(file);
			PrintService myPrintService = findPrintService(nameOFPrinter);
			PrinterJob job = PrinterJob.getPrinterJob();
			job.setPageable(new PDFPageable(document));

			if (myPrintService == null) {
				myPrintService = PrintServiceLookup.lookupDefaultPrintService();
			}

			job.setPrintService(myPrintService);
			job.setCopies(noOFCopies);
			job.print();
			document.close();
		} catch (IOException |PrinterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	}

	private static PrintService findPrintService(String printerName) {
		PrintService[] printServices = PrintServiceLookup.lookupPrintServices((DocFlavor) null, (AttributeSet) null);
		PrintService[] arg4 = printServices;
		int arg3 = printServices.length;

		for (int arg2 = 0; arg2 < arg3; ++arg2) {
			PrintService printService = arg4[arg2];
			if (printService.getName().trim().equals(printerName)) {
				return printService;
			}
		}

		return null;
	}
}