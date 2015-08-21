package info.ephyra.questionsearch;

import java.util.ArrayList;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

public class SAXPiazza extends DefaultHandler {
	public ArrayList<String> data = new ArrayList<String>();
	private String sub;
	private boolean inHistory = false;
	private boolean historyContent = false;
	private boolean historySubject = false;
	private boolean recorded = false;
	private boolean nr = false;
	
	@Override
	public void startDocument(){
//		System.out.println("Started parsing");
	}
	
	@Override
	public void endDocument() {
//		System.out.println("Finished parsing");
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes){
		if(qName.equalsIgnoreCase("history") && !recorded){
			inHistory = true;
		} else if (qName.equalsIgnoreCase("nr")){
			nr = true;
		} else if (qName.equalsIgnoreCase("content") && inHistory && !recorded){
			historyContent = true;
		} else if (qName.equalsIgnoreCase("subject") && inHistory){
			historySubject = true;
		}
	}
	
	
	@Override
	public void endElement(String uri, String localName, String qName){
		if(qName.equalsIgnoreCase("content") && historyContent){
			historyContent = false;
			recorded = true;
		}
	}
	
	@Override
	public void characters(char ch[], int start, int length){
		if(historyContent){
			String soob = new String(ch, start, length);
			sub = sub + soob;
		} else if(historySubject){
			String soob = new String(ch, start, length);
			sub = " - " + soob + "|" + sub;
			historySubject = false;
			inHistory = false;
		} else if(nr){
			String soob = new String(ch, start, length);
			sub = "@" + soob + sub;
			nr = false;
			recorded = false;
			data.add(sub);
//			System.out.println(sub);
			sub = "";
		}
	}
}
