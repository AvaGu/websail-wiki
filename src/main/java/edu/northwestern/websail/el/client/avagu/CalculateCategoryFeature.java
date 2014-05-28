package edu.northwestern.websail.el.client.avagu;
//An Ava Code

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.client.ClientProtocolException;

import edu.northwestern.websail.el.adapter.wikidoc.model.CandidateDoc;
import edu.northwestern.websail.el.adapter.wikidoc.model.FeatureDoc;
import edu.northwestern.websail.el.adapter.wikidoc.model.FeatureSetDoc;
import edu.northwestern.websail.el.client.api.WebSAILWikifierAPIAdapter;
import edu.northwestern.websail.el.client.api.model.ExtendedMentionDoc;
import edu.northwestern.websail.el.models.FeatureValue;
import edu.northwestern.websail.wikiparser.parsers.model.WikiExtractedPage;
import edu.northwestern.websail.wikiparser.parsers.model.element.WikiLink;

public class CalculateCategoryFeature {
	public static WebSAILWikifierAPIAdapter adapter = new WebSAILWikifierAPIAdapter();
	public static final int FINDBEST_UPLOADSUCESS = 0;
	public static final int FINDBEST_UPLOADFAILED = 1;
	public static final int NOBEST_UPLOADSUCESS = 2;
	public static final int NOBEST_UPLOADFAILED = 3;
	public static final String featurename = "ava.category";
	private static int match = 0;
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
	public static void learnCategoryInternalLinks(ExtendedMentionDoc mention, Set<String> knowledge) throws IOException{
		int sourceId = Integer.parseInt(mention.getArticle().getSourceId());
		WikiExtractedPage wkpage = adapter.getPage(sourceId);
		ArrayList<WikiLink> internalLinks = wkpage.getInternalLinks();
		for (WikiLink il:internalLinks){
			WikiExtractedPage ilpage = adapter.getPage(il.getTarget().getId());
			if (ilpage == null){
				System.out.println("Error: cannot find page for a internal link : " + il.getTarget().getId() + " : " + il.getTarget().getId());
				continue;
			}
			ArrayList<WikiLink> wls = ilpage.getCategoryLinks();
			for (WikiLink wl:wls){
				String category = wl.getSurface();
				knowledge.add(category);
			}
		}
		
	}
	public static void learnCategoryOnlyOriginalPage(ExtendedMentionDoc mention, Set<String> knowledge) throws IOException{
		int sourceId = Integer.parseInt(mention.getArticle().getSourceId());
		WikiExtractedPage wkpage = adapter.getPage(sourceId);
		ArrayList<WikiLink> categories = wkpage.getCategoryLinks();
		for (WikiLink wl:categories){
				String category = wl.getSurface();
				knowledge.add(category);
		}
		
	}
	public static int calculateCategory(ExtendedMentionDoc mention, Set<String> knowledge, boolean withMatchRate) throws Exception{
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
			ArrayList<WikiLink> wls = candPage.getCategoryLinks();
			int total = wls.size();
			int count = 0;
			for (WikiLink wl:wls){
				String category = wl.getSurface();
				if (knowledge.contains(category)){
					count ++;
				}
			}
			if (total == 0){
				System.out.println("Error: candidate has no category");
				value = 0;
			}
			else{
				value = (double)count / (double) total;
			}
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
				int categoryBestId = categories.get(0).getConcept().getTitleId();
				int goldId = mention.getGold().getTitleId();
				if (categoryBestId == goldId){
					match ++;
				}
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
	
		System.out.println(uploadResult);
		if (max_index == -1){
			//no best
			if (uploadResult == true){
				return CalculateCategoryFeature.NOBEST_UPLOADSUCESS;
			}
			else{
				return CalculateCategoryFeature.NOBEST_UPLOADFAILED;
			}
		}
		else{
			//has best
			if (uploadResult == true){
				return CalculateCategoryFeature.FINDBEST_UPLOADSUCESS;
			}
			else{
				return CalculateCategoryFeature.FINDBEST_UPLOADFAILED;
			}
		}
	}
	public static void processMention(ExtendedMentionDoc mention, boolean withMatchRate) throws Exception{
		Set<String> knowledgeInternalLinks = new HashSet<String>();
		Set<String> knowledgeOnlyPages = new HashSet<String>();
		Set<String> wholeKnowledge = new HashSet<String>();
		
//		learnCategoryOnlyOriginalPage(mention, wholeKnowledge);
//		learnCategoryInternalLinks(mention, wholeKnowledge);
//		int firstResult = calculateCategory(mention, wholeKnowledge, withMatchRate);
		
//		learnCategoryOnlyOriginalPage(mention, knowledgeOnlyPages);
		
		learnCategoryInternalLinks(mention, knowledgeInternalLinks);
		int firstResult = calculateCategory(mention, knowledgeInternalLinks, withMatchRate);
//		int firstResult = calculateCategory(mention, knowledgeOnlyPages, withMatchRate);
//		if (firstResult == CalculateCategoryFeature.FINDBEST_UPLOADSUCESS){
//			System.out.println("Upload ambiguous mention successful");
//		}
//		else if (firstResult == CalculateCategoryFeature.FINDBEST_UPLOADFAILED){
//			System.out.println("Error: upload ambiguous mention failed");
//		}
//		else{
//			learnCategoryInternalLinks(mention, knowledgeInternalLinks);
//			System.out.println("Reprocess mention with larger knowledge");
//			int secondResult = calculateCategory(mention, knowledgeInternalLinks, withMatchRate);
//			if (secondResult == CalculateCategoryFeature.FINDBEST_UPLOADSUCESS || secondResult == CalculateCategoryFeature.NOBEST_UPLOADSUCESS){
//				System.out.println("Upload ambiguous mention successful");
//			}
//			else{
//				System.out.println("Error: upload ambiguous mention failed");
//			}
//			
//		}
		
	}
	
	
	
	public static void processMentions(ArrayList<ExtendedMentionDoc> mentions, boolean withMatchRate) throws Exception{
		match = 0;
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
				processMention(md, withMatchRate);
			}
			System.out.println("");
			count ++;
		}
	}
	public static void testKeys(ArrayList<ExtendedMentionDoc> mentions){
		for (ExtendedMentionDoc md:mentions){
			System.out.println(md.getKey());
		}
	}
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("Step 1: Getting all mentions");
		ArrayList<ExtendedMentionDoc> mentions = adapter.getMentions(true, false);// true false   and false false
		System.out.println("Found " + mentions.size() + " mentions\n");
		boolean withMatchRate = true;
//		testKeys(mentions);
		processMentions(mentions, withMatchRate);
		
		if (withMatchRate == true){
			System.out.println("Match rate : " +match + " / " + mentions.size());
		}
	}

}
