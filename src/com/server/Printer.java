package com.server;

public class Printer {
	int noOfCopies;
	String nameOfPrinter;
	String content;

	public int getNoOfCopies() {
		return noOfCopies;
	}

	public void setNoOfCopies(int noOfCopies) {
		this.noOfCopies = noOfCopies;
	}

	public String getNameOfPrinter() {
		return nameOfPrinter;
	}

	public void setNameOfPrinter(String nameOfPrinter) {
		this.nameOfPrinter = nameOfPrinter;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return "Printer [noOfCopies=" + noOfCopies + ", nameOfPrinter=" + nameOfPrinter + ", content=" + content + "]";
	}

}
