package com.server;

import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.Socket;
import java.nio.charset.Charset;

import javax.print.DocFlavor;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.AttributeSet;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorker;
import com.itextpdf.tool.xml.XMLWorkerFontProvider;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.itextpdf.tool.xml.css.StyleAttrCSSResolver;
import com.itextpdf.tool.xml.exceptions.RuntimeWorkerException;
import com.itextpdf.tool.xml.html.CssAppliersImpl;
import com.itextpdf.tool.xml.html.Tags;
import com.itextpdf.tool.xml.parser.XMLParser;
import com.itextpdf.tool.xml.pipeline.css.CssResolverPipeline;
import com.itextpdf.tool.xml.pipeline.end.PdfWriterPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipelineContext;

public class ServerThread extends Thread {
	protected Socket sock;
	public static final String HTML = "test/output.html";
	public static final String PDF = "test/Tested.pdf";
	public static final String FONTFORMAT = "test/font.ttf";
	public static final String CSSSTYLE = "test/css1.css";

	public ServerThread(Socket clientSocket) {
		this.sock = clientSocket;
	}

	public void run() {
		InputStreamReader IR;
		BufferedWriter out = null;
		StringBuilder payload = new StringBuilder();
		try {
			IR = new InputStreamReader(sock.getInputStream());
			BufferedReader bufferedReader = new BufferedReader(IR);
			while (bufferedReader.readLine().length() != 0) {
			}
			while (bufferedReader.ready()) {
				payload.append((char) bufferedReader.read());
			}
			out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ObjectMapper mapper = new ObjectMapper();
		if (payload.length() > 1) {
			Printer printer = null;
			try {
				printer = (Printer) mapper.readValue(payload.toString(), Printer.class);
				FileWriter fWriter = new FileWriter(HTML);
				BufferedWriter writer = new BufferedWriter(fWriter);
				writer.write(printer.content);
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				printPdf();
				print(printer.nameOfPrinter, printer.noOfCopies);
				out.write("HTTP/1.0 200 OK\r\n");
				out.write("Access-Control-Allow-Origin: *\r\n");
				out.write("Access-Control-Allow-Headers: Authorization\r\n");
				out.write("Access-Control-Allow-Headers: Content-type\r\n");
				out.write("Content-type: text/plain\r\n");
				out.write("s");
				out.close();
				sock.close();
			} catch (IOException | RuntimeWorkerException | PrinterException arg11) {
				arg11.printStackTrace();
				try {
					out.write("HTTP/1.0 500 ERROR\r\n");
					out.write("\r\n");
					out.write("<TITLE>Print Utility</TITLE>");
					out.write(arg11.toString());
					out.close();
					sock.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (PrintException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				out.write("HTTP/1.0 200 OK\r\n");
				out.write("Access-Control-Allow-Origin: *\r\n");
				out.write("Access-Control-Allow-Headers: Authorization\r\n");
				out.write("Access-Control-Allow-Headers: Content-type\r\n");
				out.write("s");
				out.close();
				sock.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	private static void printPdf() throws IOException, DocumentException {
		File file = new File(PDF);
		file.getParentFile().mkdirs();
		Rectangle pagesize = new Rectangle(135.0F, 590.0F);
		Document document = new Document(pagesize, 0F, 0F, 0F, 15F);
		PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
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
	}

	private static void print(String nameOFPrinter, int noOFCopies)
			throws PrintException, PrinterException, IOException {
		PDDocument document = PDDocument.load(new File(PDF));
		PrintService myPrintService = findPrintService(nameOFPrinter);
		PrinterJob job = PrinterJob.getPrinterJob();
		job.setPageable(new PDFPageable(document));
		if (myPrintService == null) {
			myPrintService = PrintServiceLookup.lookupDefaultPrintService();
		}

		job.setPrintService(myPrintService);
		job.setCopies(noOFCopies);
		job.print();

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