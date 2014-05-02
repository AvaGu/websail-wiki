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
	private static int match = 0;
	public static boolean uploadUniqueMention(ExtendedMentionDoc mention) throws Exception{
		ArrayList<CandidateDoc> candidates = mention.getCandidates();
		int numCand = candidates.size();
		if (numCand != 1){
			return false;
		}
		
		
		FeatureSetDoc fsd = new FeatureSetDoc();
		fsd.setMentionKey(mention.getSurface());
		ArrayList<FeatureDoc> fds = new ArrayList<FeatureDoc>();
		FeatureDoc fd1 = new FeatureDoc();
		fd1.setConceptId(candidates.get(0).getConcept().getTitleId());
		fd1.setFeatureValue("ava.category", new FeatureValue(1.0));
		fds.add(fd1);
		fsd.setFeatureDocs(fds);
		
		try{
			return adapter.uploadMentionFeature(fsd);
		}
		catch(Exception e){
			return false;
		}
		
		
	}
	public static void learnCategory(ExtendedMentionDoc mention, Set<String> knowledge) throws IOException{
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
	public static boolean calculateCategory(ExtendedMentionDoc mention, Set<String> knowledge, boolean withMatchRate) throws Exception{
		ArrayList<CandidateDoc> categories = mention.getCandidates();
		FeatureSetDoc fsd = new FeatureSetDoc();
		fsd.setMentionKey(mention.getSurface());
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
			fd.setFeatureValue("ava.category", new FeatureValue(value));
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
		try{
			return adapter.uploadMentionFeature(fsd);
		}
		catch(Exception e){
			return false;
		}
	}
	public static void processMention(ExtendedMentionDoc mention, boolean withMatchRate) throws Exception{
		Set<String> knowledge = new HashSet<String>();
		learnCategory(mention, knowledge);
		boolean result = calculateCategory(mention, knowledge, withMatchRate);
		if (result == true){
			System.out.println("Upload ambiguous mention successful");
		}
		else{
			System.out.println("Error: upload ambiguous mention failed");
		}
	}
	
	
	
	public static void processMentions(ArrayList<ExtendedMentionDoc> mentions, boolean withMatchRate) throws Exception{
		match = 0;
		int mentionNum = mentions.size();
		int count = 1;
		for (ExtendedMentionDoc md:mentions){
			System.out.println("Process " + count + " / " + mentionNum + " : " + md.getSurface());
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
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("Step 1: Getting all mentions");
		ArrayList<ExtendedMentionDoc> mentions = adapter.getMentions(false, false);// true false   and false false
		System.out.println("Found " + mentions.size() + " mentions\n");
		boolean withMatchRate = false;
		processMentions(mentions, withMatchRate);
		
		if (withMatchRate == true){
			System.out.println("Match rate : " +match + " / " + mentions.size());
		}
	}

}
