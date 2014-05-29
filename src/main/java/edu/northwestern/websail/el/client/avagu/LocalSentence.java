package edu.northwestern.websail.el.client.avagu;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;

import edu.northwestern.websail.el.adapter.wikidoc.model.CandidateDoc;
import edu.northwestern.websail.el.adapter.wikidoc.model.FeatureDoc;
import edu.northwestern.websail.el.adapter.wikidoc.model.FeatureSetDoc;
import edu.northwestern.websail.el.client.api.WebSAILWikifierAPIAdapter;
import edu.northwestern.websail.el.client.api.model.ExtendedMentionDoc;
import edu.northwestern.websail.el.models.FeatureValue;
import edu.northwestern.websail.el.models.Token;
import edu.northwestern.websail.el.models.TokenSpan;
import edu.northwestern.websail.el.wikifier.text.tokenizer.StanfordNLPSSplit;
import edu.northwestern.websail.el.wikifier.text.tokenizer.StanfordNLPTokenizer;
import edu.northwestern.websail.wikiparser.parsers.model.WikiExtractedPage;
import edu.northwestern.websail.wikiparser.parsers.model.element.WikiLink;
import edu.northwestern.websail.wikiparser.parsers.model.element.WikiSection;

public class LocalSentence {
	public static WebSAILWikifierAPIAdapter adapter = new WebSAILWikifierAPIAdapter();
	private static int match = 0;
	private static int caredMentions = 0;
	public static final String featurename = "ava.localsentence";
	public static boolean uploadUniqueMention(ExtendedMentionDoc mention) throws Exception{
		ArrayList<CandidateDoc> candidates = mention.getCandidates();
		int numCand = candidates.size();
		if (numCand != 1){
			return false;
		}
		
		
		FeatureSetDoc fsd = new FeatureSetDoc();
		fsd.setMentionKey(mention.getKey());
		ArrayList<FeatureDoc> fds = new ArrayList<FeatureDoc>();
		FeatureDoc fd1 = new FeatureDoc();
		fd1.setConceptId(candidates.get(0).getConcept().getTitleId());
		
		fd1.setFeatureValue(featurename, new FeatureValue(1.0));
		
		fds.add(fd1);
		fsd.setFeatureDocs(fds);
		
		try{
			return adapter.uploadMentionFeature(fsd);
		}
		catch(Exception e){
			return false;
		}
		
		
	}
	public static String getLocalSentence(ExtendedMentionDoc mention) throws Exception{
		WikiExtractedPage page = adapter.getPage(Integer.parseInt(mention.getArticle().getSourceId()));
		String localSentence;
		String plaintext = page.getPlainText();
		int startOffset =mention.getStartOffset();
		int endOffset = mention.getEndOffset();
//		System.out.println("StartOffset : " + startOffset + "  EndOffset : " + endOffset);
		
		int localStart = 0;
		int localEnd = 0;
		boolean firstTime = true;
		int i;
		for (i = startOffset; i >=0 ; i --){
			if (plaintext.charAt(i) == '.'){
				if (firstTime == true){
					firstTime = false;
					continue;
				}
				else{
					localStart = i + 1;
					break;
				}
			}
		}
		if (i == -1){
			localStart = 0;
		}
		firstTime = true;
		for (i = endOffset; i < plaintext.length(); i ++){
			if (plaintext.charAt(i) == '.'){
				if (firstTime == true){
					firstTime = false;
					continue;
				}
				else{
					localEnd = i;
					break;
				}
			}
		}
		if (i == plaintext.length()){
			localEnd = plaintext.length();
		}
//		System.out.println("localStart : " + localStart + "  localEnd : " + localEnd);
		localSentence = plaintext.substring(localStart, localEnd);
		
		return localSentence;
	}
	public static HashMap<String,Integer> tokenizeParagraph(String content) throws IOException{
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		StanfordNLPTokenizer tokenizer = new StanfordNLPTokenizer();
		CharArraySet cas = StopAnalyzer.ENGLISH_STOP_WORDS_SET;
		HashSet<String> skips = new HashSet<String>();
		skips.add(",");
		skips.add(".");
		skips.add("?");
		skips.add("!");
		skips.add(";");
		skips.add("who");
		skips.add("where");
		skips.add("what");
		skips.add("when");
		skips.add("while");
		skips.add("how");
		tokenizer.setStopwords(cas);
	
		tokenizer.initialize(content);
		for(Token t : tokenizer.getAllTokens()){
			String word = t.getText().trim();
			if (skips.contains(word)){
				continue;
			}
			else{
				if (map.containsKey(word)){
					int newvalue = map.get(word) + 1;
					map.put(word, newvalue);
				}
				else{
					map.put(word, 1);
				}
			}
		}
		return map;

	}
	public static boolean calculateLocalSentence(ExtendedMentionDoc mention, HashMap<String, Integer> localMap, boolean withMatchRate) throws Exception{
		ArrayList<CandidateDoc> categories = mention.getCandidates();
		FeatureSetDoc fsd = new FeatureSetDoc();
		fsd.setMentionKey(mention.getKey());
		ArrayList<FeatureDoc> fds = new ArrayList<FeatureDoc>();
		double max = 0;
		int max_index = -1;
		int index = 0;
		for (CandidateDoc cd:categories){
			double value = 0;
			
			int candId = cd.getConcept().getTitleId();
			WikiExtractedPage candPage = adapter.getPage(candId);
			ArrayList<WikiSection> candSections = candPage.getSections();
			if (candSections.size() == 0){
				System.out.println("Error: Candidate has no sections");
				value = 0;
			}
			else{
				String candisection = candPage.getPlainText().substring(0, candPage.getSections().get(0).getOffset());
				HashMap<String, Integer> sectionMap = tokenizeParagraph(candisection);
				value = 0;
				double up = 0;
				double down = 0;
				double downleft = 0;
				double downright = 0;
				for (String key : localMap.keySet()) {
					if (sectionMap.containsKey(key)){
						up += localMap.get(key) * sectionMap.get(key);
						downleft += localMap.get(key) * localMap.get(key);
						downright += sectionMap.get(key) * sectionMap.get(key);
					}
				}//for
				down = downleft * downright;
				down = Math.sqrt(down);
				if (down !=0){
					value = up / down;
				}
			}//else
					
			
			FeatureDoc fd = new FeatureDoc();
			fd.setConceptId(cd.getConcept().getTitleId());
			fd.setFeatureValue(featurename, new FeatureValue(value));
			fds.add(fd);
			
			if (value > max){
				max = value;
				max_index = index;
			}
			index ++;
		}
		
		if (withMatchRate == true){
			if (max_index != -1){
				int categoryBestId = categories.get(max_index).getConcept().getTitleId();
				int goldId = mention.getGold().getTitleId();
				if (categoryBestId == goldId){
					match ++;
				}
				else{
					System.out.println("Wrong case : \n");
					System.out.println("Local map:");
					System.out.println(localMap);
					System.out.println();
					System.out.println("Gold: " + mention.getGold().getTitle());
					WikiExtractedPage tmpPage = adapter.getPage(mention.getGold().getTitleId());
					String section = tmpPage.getPlainText().substring(0, tmpPage.getSections().get(0).getOffset());
					HashMap<String, Integer> sectionMap = tokenizeParagraph(section);
					System.out.println("Section map:");
					System.out.println(sectionMap);
					
					System.out.println();
					System.out.println("Pick up : " + categories.get(max_index).getConcept().getTitle());
					tmpPage = adapter.getPage(categories.get(max_index).getConcept().getTitleId());
					System.out.println("Section map:");
					section = tmpPage.getPlainText().substring(0, tmpPage.getSections().get(0).getOffset());
					sectionMap = tokenizeParagraph(section);
					System.out.println(sectionMap);
					
				}
				System.out.println("---------------------------------------------------");
				System.out.println("Matched : " + match);
				System.out.println("---------------------------------------------------");
			}
		}
		
		fsd.setFeatureDocs(fds);
		boolean uploadResult = true;
		
		try{
			uploadResult = adapter.uploadMentionFeature(fsd);
		}
		catch(Exception e){
			uploadResult = false;
		}
		return uploadResult;
//		System.out.println(uploadResult);
//		if (max_index == -1){
//			//no best
//			if (uploadResult == true){
//				return CalculateCategoryFeature.NOBEST_UPLOADSUCESS;
//			}
//			else{
//				return CalculateCategoryFeature.NOBEST_UPLOADFAILED;
//			}
//		}
//		else{
//			//has best
//			if (uploadResult == true){
//				return CalculateCategoryFeature.FINDBEST_UPLOADSUCESS;
//			}
//			else{
//				return CalculateCategoryFeature.FINDBEST_UPLOADFAILED;
//			}
//		}
	}
	
	public static void processMention(ExtendedMentionDoc mention, boolean withMatchRate) throws Exception{
		String localSentence = getLocalSentence(mention);
		HashMap<String, Integer> localSentenceMap =tokenizeParagraph(localSentence);
		calculateLocalSentence(mention, localSentenceMap, withMatchRate);
		
		
		System.out.println(localSentenceMap);
	}
	public static void processMentions(ArrayList<ExtendedMentionDoc> mentions, boolean withMatchRate) throws Exception{
		match = 0;
		caredMentions = 0;
		int mentionNum = mentions.size();
		int count = 1;
		
		for (ExtendedMentionDoc md:mentions){
			System.out.println("Process " + count + " / " + mentionNum + " : " + md.getSurface() + " : " + md.getKey());
			ArrayList<CandidateDoc> candidates = md.getCandidates();
			int numCand = candidates.size();
			if (numCand == 0){
				System.out.println("Error: no candidates found");
			}
			else if (numCand == 1){
				boolean result = uploadUniqueMention(md);
				if (result == true){
					System.out.println("Upload unique mention successful");
				}
				else{
					System.out.println("Error: upload unique mention failed");
				}
			}
			else{
				caredMentions ++;
				processMention(md, withMatchRate);
			}
			System.out.println("");
			count ++;
		}
	}

	public static void main(String[] args) throws Exception{
		int caseNo = 0;
//		int caseNo = 1;
//		int caseNo = 2;
		
		// TODO Auto-generated method stub
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date startdate = new Date();
		System.out.println("Program start!");
		
		
		System.out.println("Step 1: Getting all mentions");
		ArrayList<ExtendedMentionDoc> mentions; 
		boolean withMatchRate;
		if (caseNo == 0){
			mentions = adapter.getMentions(true, true);// true false   and false false
			withMatchRate = true;
		}
		else if (caseNo == 1){
			mentions = adapter.getMentions(true, false);// true false   and false false
			withMatchRate = true;
		}
		else{
			withMatchRate = false;
			mentions = adapter.getMentions(false, false);// true false   and false false
		}
		System.out.println("Found " + mentions.size() + " mentions\n");
		
		
		processMentions(mentions, withMatchRate);
		
		if (withMatchRate == true){
			System.out.println("Match rate : " + match + " / " + caredMentions);
		}
		
		
		
		System.out.println("Program start @");
		System.out.println(dateFormat.format(startdate));
		Date date = new Date();
		System.out.println("Program end@");
		System.out.println(dateFormat.format(date));
		
	}// main

}
