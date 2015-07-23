package questionsearch;

import javax.xml.parsers.*;

import org.xml.sax.*;

import java.util.*;
import java.io.*;

import  org.apache.commons.lang3.StringUtils;

/**
 * This class searches and matches keywords from the question analysis 
 * portion of the Ephyra pipeline to questions asked in the Piazza 
 * data. 
 * 
 * @author Alexander Wang
 */
public class PiazzaSearch{
	/** Piazza files and questions */
	private static String[] relatedQuestions = new String[3];
	private static String[] questions;
	
	/** Lecture files */
	private static String relatedLecture = null;
	private static ArrayList<String> lectures = new ArrayList<String>();

	/**
	 * Initializes piazza and lectures to be used by SAX
	 */
	public PiazzaSearch() {
		//Preparing Lectures
		File folder = new File("data/lectures");
		File[] lectfiles = folder.listFiles();
		for(File f : lectfiles){
			convertXML(f);
		}
		
		//Preparing piazza data
		folder = new File("data/piazza");
		File[] piafiles = folder.listFiles();
		for(File f : piafiles){
			convertPiazza(f);
		}
	}
	
	/**
	 * Searches for most related past piazza questions
	 */
	public void SearchPiazza(String[] keywords){
		int[] ranks = new int[relatedQuestions.length];
		int rank, lowest = 0;
		for(String q : questions){
			rank = 0;
			for(String kw : keywords){
				if(q.toLowerCase().contains(kw.toLowerCase())){
					rank += StringUtils.countMatches(q.toLowerCase(), kw.toLowerCase());
				}
			}
			if(rank > ranks[lowest]){
//				System.out.println("Before = " + ranks[0] + " " + ranks[1] + " " + ranks[2] + "| Lowest = " + lowest);
				ranks[lowest] = rank;
//				System.out.println("After = " + ranks[0] + " " + ranks[1] + " " + ranks[2]);
				relatedQuestions[lowest] = q;
				int min = 100000000;
				for(int i = 0; i < ranks.length; ++i){
					if(min > ranks[i]){
						min = ranks[i];
						lowest = i;
					}
				}
//				System.out.println("New Lowest at index " + lowest + " | Value = " + ranks[lowest]);
			}
		}
		for(int i = 0; i < relatedQuestions.length; ++i){
			relatedQuestions[i] = relatedQuestions[i].substring(0, relatedQuestions[i].indexOf("|"));
		}
	}
	
	/**
	 * Uses SAX to convert the piazza data into easy to use strings
	 */
	private void convertPiazza(File f){
		SAXParserFactory factory = SAXParserFactory.newInstance();
		InputStream xml;
		try {
			xml = new FileInputStream(f);
			SAXParser saxParse = factory.newSAXParser();
			
			SAXPiazza handler = new SAXPiazza();
			saxParse.parse(xml, handler);
			questions = new String[handler.data.size()];
			handler.data.toArray(questions);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Look through lecture files for most relevant lecture to query
	 */
	public void SearchLectures(String[] keywords){
		int rating = 0, sub;
		for(String lec : lectures){
			sub = 0;
			for(String kw : keywords){
				if(lec.toLowerCase().contains(kw.toLowerCase())){
					sub += StringUtils.countMatches(lec.toLowerCase(), kw.toLowerCase());
				}
			}
			
			if(sub > rating){
				relatedLecture = lec;
				rating = sub;
			}
		}
		relatedLecture = relatedLecture.substring(0, relatedLecture.indexOf("."));
	}
	
	/**
	 * Converts lecture xmls into strings
	 */
	private void convertXML(File f){
		String line = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader(f));
			StringBuilder sb = new StringBuilder();
			while((line = br.readLine()) != null){
				sb.append(line.trim());
			}
			line = sb.toString();
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		line = f.getName() + "|" + line;
		lectures.add(line);
	}
	
	
	public void testPrint() throws FileNotFoundException, UnsupportedEncodingException{
		PrintWriter writer = new PrintWriter("piazza.txt", "UTF-8");
		for(String q : questions){
			writer.println(q);
		}
		writer.close();
		
		PrintWriter wroter = new PrintWriter("lectures.txt", "UTF-8");
		for(String q : lectures){
			wroter.println(q);
		}
		wroter.close();
	}
	
	public String getRelatedLec(){
		return relatedLecture;
	}
	
	public String[] getRelatedPosts(){
		return relatedQuestions;
	}
}
