package edu.northwestern.websail.el.client.avagu;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;

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
	private static int ambiguousMentions= 0;
	private static int uniqueMentions = 0;
	private static int nocandiMentions = 0;
	private static int uniqueMentionMatch = 0;
	private static int ambiguousMentionMatch = 0;
	
	private static int caredMentions = 0;
	public static final String featurename = "ava.localsentence";

	private static StanfordNLPTokenizer tokenizer = new StanfordNLPTokenizer();

	static {

		CharArraySet cas = StopAnalyzer.ENGLISH_STOP_WORDS_SET;
		CharArraySet skips = new CharArraySet(Version.LUCENE_40, 0, true);
		ArrayList<String> prepositions = new ArrayList<String>(Arrays.asList("a",
																			"abaft",
																			"aboard",
																			"absent",
																			"across",
																			"afore",
																			"after",
																			"against",
																			"along",
																			"alongside",
																			"amid",
																			"amidst",
																			"among",
																			"amongst",
																			"an",
																			"anenst",
																			"apropos",
																			"apud",
																			"around",
																			"as",
																			"aside",
																			"astride",
																			"at",
																			"athwart",
																			"atop",
																			"barring",
																			"before",
																			"beneath",
																			"beside",
																			"besides",
																			"between",
																			"beyond",
																			"but",
																			"by",
																			"circa",
																			"concerning",
																			"despite",
																			"down",
																			"during",
																			"except",
																			"excluding",
																			"failing",
																			"following",
																			"for",
																			"forenenst",
																			"from",
																			"given",
																			"in",
																			"including",
																			"inside",
																			"into",
																			"like",
																			"mid",
																			"midst",
																			"minus",
																			"modulo",
																			"near",
																			"next",
																			"notwithstanding",
																			"o'",
																			"of",
																			"off",
																			"on",
																			"onto",
																			"opposite",
																			"out",
																			"outside",
																			"over",
																			"pace",
																			"past",
																			"per",
																			"plus",
																			"pro",
																			"qua",
																			"regarding",
																			"round",
																			"sans",
																			"since",
																			"than",
																			"through",
																			"throughout",
																			"till",
																			"times",
																			"to",
																			"toward",
																			"towards",
																			"under",
																			"underneath",
																			"unlike",
																			"until",
																			"unto",
																			"up",
																			"upon",
																			"versus",
																			"vs",
																			"via",
																			"with",
																			"without",
																			"worth"
				));
		ArrayList<String> conjunctions =new ArrayList<String>(Arrays.asList("after",
																			"although",
																			"as",
																			"because",
																			"before",
																			"if",
																			"lest",
																			"once",
																			"since",
																			"than",
																			"that",
																			"though",
																			"till",
																			"unless",
																			"until",
																			"when",
																			"whenever",
																			"where",
																			"wherever",
																			"while"
				));
		ArrayList<String> pronouns =new ArrayList<String>(Arrays.asList("I",
																		"me",
																		"you",
																		"she",
																		"her",
																		"he",
																		"him",
																		"it",
																		"they",
																		"them",
																		"that",
																		"which",
																		"who",
																		"whom",
																		"whose",
																		"whichever",
																		"whoever",
																		"whomever",
																		"this",
																		"these",
																		"that",
																		"those",
																		"anybody",
																		"anyone",
																		"anything",
																		"each",
																		"either",
																		"everybody",
																		"everyone",
																		"everything",
																		"neither",
																		"nobody",
																		"nothing",
																		"somebody",
																		"someone",
																		"something",
																		"both",
																		"few",
																		"many",
																		"several",
																		"all",
																		"any",
																		"most",
																		"none",
																		"some",
																		"myself",
																		"ourselves",
																		"yourself",
																		"yourselves",
																		"himeself",
																		"herself",
																		"itset",
																		"themselves",
																		"my",
																		"your",
																		"his",
																		"her",
																		"its",
																		"our",
																		"their",
																		"mine",
																		"yours",
																		"hers",
																		"ours",
																		"yours",
																		"theirs"
																		
				));
		
		ArrayList<String> skiplist = new ArrayList<String>(
			    Arrays.asList(",",
			    			".", 
			    			"?",
			    			"!",
			    			";",
			    			"who",
			    			"where",
			    			"what",
			    			"when",
			    			"while",
			    			"how",
			    			"which",
			    			"-lrb-",
			    			"-rrb-",
			    			"-lcb-",
			    			"-rcb-",
			    			"-lsb-",
			    			"-rsb-",
			    			"--",
			    			"my",
			    			"mine",
			    			"me",
			    			"I",
			    			"you",
			    			"your",
			    			"yours",
			    			"him",
			    			"his",
			    			"he",
			    			"she",
			    			"her",
			    			"hers",
			    			"it",
			    			"its",
			    			"they",
			    			"them",
			    			"their",
			    			"theirs",
			    			"it's",
			    			"``",
			    			"from",
			    			"to",
			    			"also",
			    			"and",
			    			"before",
			    			"after",
			    			"in",
			    			"on",
			    			"by",
			    			"more",
			    			"less",
			    			"often",
			    			"other",
			    			"many",
			    			"much",
			    			"because",
			    			"so",
			    			"therefore",
			    			"however",
			    			"''",
			    			"'",
			    			"should",
			    			"must",
			    			"can"
			    			)
			    			);
		skips.addAll(conjunctions);
		skips.addAll(pronouns);
		skips.addAll(prepositions);
		skips.addAll(skiplist);
		skips.addAll(cas);
		// cas.addAll(skips);
		tokenizer.setToLower(true);
		tokenizer.setStopwords(skips);
	}

	public static boolean uploadUniqueMention(ExtendedMentionDoc mention, boolean withMatchRate)
			throws Exception {
		ArrayList<CandidateDoc> candidates = mention.getCandidates();
		int numCand = candidates.size();
		if (numCand != 1) {
			return false;
		}
		
		if (withMatchRate == true){
			if (candidates.get(0).getConcept().getTitleId() == mention.getGold().getTitleId()){
				uniqueMentionMatch ++;
			}
		}

		FeatureSetDoc fsd = new FeatureSetDoc();
		fsd.setMentionKey(mention.getKey());
		ArrayList<FeatureDoc> fds = new ArrayList<FeatureDoc>();
		FeatureDoc fd1 = new FeatureDoc();
		fd1.setConceptId(candidates.get(0).getConcept().getTitleId());

		fd1.setFeatureValue(featurename, new FeatureValue(1.0));

		fds.add(fd1);
		fsd.setFeatureDocs(fds);

		try {
			return adapter.uploadMentionFeature(fsd);
		} catch (Exception e) {
			return false;
		}

	}

	public static String getLocalSentence(ExtendedMentionDoc mention)
			throws Exception {
		WikiExtractedPage page = adapter.getPage(Integer.parseInt(mention
				.getArticle().getSourceId()));
		String localSentence;
		String plaintext = page.getPlainText();
		int startOffset = mention.getStartOffset();
		int endOffset = mention.getEndOffset();

		int localStart = 0;
		int localEnd = 0;
		boolean firstTime = true;
		int i;
		for (i = startOffset; i >= 0; i--) {
			if (plaintext.charAt(i) == '\n'){
				localStart = i + 1;
			}
			if (plaintext.charAt(i) == '.') {
				if (firstTime == true) {
					firstTime = false;
					continue;
				} else {
					localStart = i + 1;
					break;
				}
			}
			
		}
		if (i == -1) {
			localStart = 0;
		}
		firstTime = true;
		for (i = endOffset; i < plaintext.length(); i++) {
			if (plaintext.charAt(i) == '\n'){
				localEnd = i;
			}
			if (plaintext.charAt(i) == '.') {
				if (firstTime == true) {
					firstTime = false;
					continue;
				} else {
					localEnd = i;
					break;
				}
			}
		}
		if (i == plaintext.length()) {
			localEnd = plaintext.length();
		}
		localSentence = plaintext.substring(localStart, localEnd);

		return localSentence;
	}

	public static HashMap<String, Integer> tokenizeParagraph(String content)
			throws IOException {
		HashMap<String, Integer> map = new HashMap<String, Integer>();

		tokenizer.initialize(content);
		for (Token t : tokenizer.getAllTokens()) {
			String word = t.getText().trim();
			// if (skips.contains(word)){
			// continue;
			// }
			// else{
			if (map.containsKey(word)) {
				int newvalue = map.get(word) + 1;
				map.put(word, 1);
			} else {
				map.put(word, 1);
			}
			// }
		}
		return map;

	}

	public static boolean calculateLocalSentence(ExtendedMentionDoc mention,
			HashMap<String, Integer> localMap, boolean withMatchRate)
			throws Exception {
		ArrayList<CandidateDoc> categories = mention.getCandidates();
		FeatureSetDoc fsd = new FeatureSetDoc();
		fsd.setMentionKey(mention.getKey());
		ArrayList<FeatureDoc> fds = new ArrayList<FeatureDoc>();
		double max = 0;
		int max_index = -1;
		int index = 0;
		for (CandidateDoc cd : categories) {
			double value = 0;

			int candId = cd.getConcept().getTitleId();
			WikiExtractedPage candPage = adapter.getPage(candId);
			ArrayList<WikiSection> candSections = candPage.getSections();
			int glossEndOffset = candPage.getPlainText().length();
			if (candSections.size() > 0) {
				glossEndOffset = candPage.getSections().get(0).getOffset();
			}
			String candisection = candPage.getPlainText().substring(0,
					glossEndOffset);
			HashMap<String, Integer> sectionMap = tokenizeParagraph(candisection);
			value = 0;
			double up = 0;
			double down = 0;
			double downleft = 0;
			double downright = 0;
			for (String key : localMap.keySet()) {
				if (sectionMap.containsKey(key)) {
					up += localMap.get(key) * sectionMap.get(key);
				}
				downleft += localMap.get(key) * localMap.get(key);
			}// for
			for (String key : sectionMap.keySet()) {
				downright += sectionMap.get(key) * sectionMap.get(key);
			}// for
			down = Math.sqrt(downleft) * Math.sqrt(downright);
			// down = Math.sqrt(down);
			if (down != 0) {
				value = up / down;
				
			}
			value = up;
			
			if (withMatchRate == true){
			boolean correct = cd.getConcept().getTitleId() == mention.getGold()
					.getTitleId();
			String mark = "";
			if (correct == true){
				mark = "Golden";
			}
//			System.out.println("Value: " + value + ", " + mark + "("+cd.getConcept() + " vs " + mention.getGold()+")");
			System.out.println("Value: " + value + ", " + mark + "("+cd.getConcept() + ")");
			}

			FeatureDoc fd = new FeatureDoc();
			fd.setConceptId(cd.getConcept().getTitleId());
			fd.setFeatureValue(featurename, new FeatureValue(value));
			fds.add(fd);

			if (value > max) {
				max = value;
				max_index = index;
			}
			index++;
		}

		if (withMatchRate == true) {
			if (max_index != -1) {
				int categoryBestId = categories.get(max_index).getConcept()
						.getTitleId();
				int goldId = mention.getGold().getTitleId();
				if (categoryBestId == goldId) {
					ambiguousMentionMatch++;
					System.out.println("Right case");
//					System.out.println("Right Value:  " + max + ".");
				} else {
					
					System.out.println("\nWrong case Error analysis:");
					System.out.println("Mention (" + mention.getSurface() + ") Local sentences map:");
					System.out.println(localMap);
					System.out.println();
					System.out.println("Gold: " + mention.getGold().getTitle());
					
//					WikiExtractedPage tmpPage = adapter.getPage(mention
//							.getGold().getTitleId());
//					String section = tmpPage.getPlainText().substring(0,
//							tmpPage.getSections().get(0).getOffset());
//					HashMap<String, Integer> sectionMap = tokenizeParagraph(section);
//					System.out.println("Section map:");
//					System.out.println(sectionMap);

					System.out.println("Pick up : "+ categories.get(max_index).getConcept().getTitle());
//					tmpPage = adapter.getPage(categories.get(max_index)
//							.getConcept().getTitleId());
//					System.out.println("Section map:");
//					section = tmpPage.getPlainText().substring(0,
//							tmpPage.getSections().get(0).getOffset());
//					sectionMap = tokenizeParagraph(section);
//					System.out.println(sectionMap);
					System.out.println("Wrong candidate value:" + max + ".");

				}
//				System.out
//						.println("---------------------------------------------------");
//				System.out.println("Matched : " + match);
//				System.out
//						.println("---------------------------------------------------");
			}
		}

		fsd.setFeatureDocs(fds);
		boolean uploadResult = true;

		try {
			uploadResult = adapter.uploadMentionFeature(fsd);
		} catch (Exception e) {
			uploadResult = false;
		}
		return uploadResult;
		// System.out.println(uploadResult);
		// if (max_index == -1){
		// //no best
		// if (uploadResult == true){
		// return CalculateCategoryFeature.NOBEST_UPLOADSUCESS;
		// }
		// else{
		// return CalculateCategoryFeature.NOBEST_UPLOADFAILED;
		// }
		// }
		// else{
		// //has best
		// if (uploadResult == true){
		// return CalculateCategoryFeature.FINDBEST_UPLOADSUCESS;
		// }
		// else{
		// return CalculateCategoryFeature.FINDBEST_UPLOADFAILED;
		// }
		// }
	}

	public static void processMention(ExtendedMentionDoc mention,
			boolean withMatchRate) throws Exception {
		String localSentence = getLocalSentence(mention);
		HashMap<String, Integer> localSentenceMap = tokenizeParagraph(localSentence);
		calculateLocalSentence(mention, localSentenceMap, withMatchRate);

	}

	public static void processMentions(ArrayList<ExtendedMentionDoc> mentions,
			boolean withMatchRate) throws Exception {
		ambiguousMentionMatch = 0;
		uniqueMentionMatch = 0;
		
		ambiguousMentions = 0;
		uniqueMentions = 0;
		nocandiMentions = 0;
		
		int mentionNum = mentions.size();
		int count = 1;

		for (ExtendedMentionDoc md : mentions) {
			System.out.println("\n-----------------  Process " + count + " / " + mentionNum + " : "
					+ md.getSurface() + " : " + md.getKey() + " -------------------");
			ArrayList<CandidateDoc> candidates = md.getCandidates();
			int numCand = candidates.size();
			if (numCand == 0) {
				nocandiMentions ++;
				System.out.println("Error: no candidates found");
			} else if (numCand == 1) {
				uniqueMentions ++;
				boolean result = uploadUniqueMention(md, withMatchRate);
				if (result == true) {
					System.out.println("Upload unique mention successful");
				} else {
					System.out.println("Error: upload unique mention failed");
				}
			} else {
				ambiguousMentions ++;
				processMention(md, withMatchRate);
			}
			System.out.println("-------------------------------------------------------");
			count++;
		}
	}

	public static void main(String[] args) throws Exception {
//		int caseNo = 0;
//		 int caseNo = 1;
		 int caseNo = 2;

		// TODO Auto-generated method stub
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date startdate = new Date();
		System.out.println("Program start!");

		System.out.println("Step 1: Getting all mentions");
		ArrayList<ExtendedMentionDoc> mentions;
		boolean withMatchRate;
		if (caseNo == 0) {
			mentions = adapter.getMentions(true, true);// true false and false
														// false
			withMatchRate = true;
		} else if (caseNo == 1) {
			mentions = adapter.getMentions(true, false);// true false and false
														// false
			withMatchRate = true;
		} else {
			withMatchRate = false;
			mentions = adapter.getMentions(false, false);// true false and false
															// false
		}
		System.out.println("Found " + mentions.size() + " mentions\n");

		processMentions(mentions, withMatchRate);

		if (withMatchRate == true) {
			System.out.println("\n\nOverview :");
			System.out.println("Unique Candidate Mentions Accuracy :");
			System.out.println(uniqueMentionMatch + " / " + uniqueMentions);
			System.out.println("Ambiguous Mentions Accuracy :");
			System.out.println(ambiguousMentionMatch + " / " + ambiguousMentions);
			System.out.println("No candidates mentions :");
			System.out.println(nocandiMentions);
			
			
			System.out.println("\n\n Overall accuracy");
			System.out.println((uniqueMentionMatch + ambiguousMentionMatch) + " / " + mentions.size());
			
			
		}

		System.out.println("\nProgram start @");
		System.out.println(dateFormat.format(startdate));
		Date date = new Date();
		System.out.println("Program end@");
		System.out.println(dateFormat.format(date));

	}// main

}
