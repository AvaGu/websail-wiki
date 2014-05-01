package edu.northwestern.websail.el.client.avagu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.Gson;

import edu.northwestern.websail.el.adapter.wikidoc.model.CandidateDoc;
import edu.northwestern.websail.el.adapter.wikidoc.model.FeatureDoc;
import edu.northwestern.websail.el.adapter.wikidoc.model.FeatureSetDoc;
import edu.northwestern.websail.el.client.api.WebSAILWikifierAPIAdapter;
import edu.northwestern.websail.el.client.api.model.ExtendedMentionDoc;
import edu.northwestern.websail.el.models.FeatureValue;
import edu.northwestern.websail.el.models.WikiTitle;
import edu.northwestern.websail.wikiparser.parsers.model.element.WikiLink;

public class CategoryFeature {
	public static WebSAILWikifierAPIAdapter adapter = new WebSAILWikifierAPIAdapter();
	public static void processAmbiguousMentions(Set<String>confirmedCategories, ArrayList<ExtendedMentionDoc>ambiguousMentions) throws Exception{
		System.out.println("ProcessAmbiguousMentions");
		int numOfUnsolved = ambiguousMentions.size();
		System.out.println("Now have : " + numOfUnsolved + " ambiguousMentions");
		int count = 0;
		
		//toDoMentions: parameters that should be passed into next recursion
		ArrayList<ExtendedMentionDoc> toDoMentions = new ArrayList<ExtendedMentionDoc>();
		for (ExtendedMentionDoc md:ambiguousMentions){
			count ++;
			System.out.println("ProcessAmbiguousMentions : " + count);
			//for upload use
			FeatureSetDoc fsd = new FeatureSetDoc();
			//????????????????????????????
			String mention = md.getSurface();
			fsd.setMentionKey(mention);
			ArrayList<FeatureDoc> fds = new ArrayList<FeatureDoc>();
			System.out.println("For mention : " + md.getGold().getTitle());
			
			ArrayList<CandidateDoc> candidates = md.getCandidates();
			boolean hasMatched = false;
			double max = 0;
			int maxIndex = -1;
			for (int i = 0; i < candidates.size(); i ++){
				CandidateDoc cd = candidates.get(i);
				FeatureDoc fd1 = new FeatureDoc();
				fd1.setConceptId(cd.getConcept().getTitleId());
				double value = 0;
				//获取当前mention的categories
				ArrayList<WikiLink> wls = adapter.getPage(cd.getConcept().getTitleId()).getCategoryLinks();
				int totalCategories = wls.size();
				int matchCategories = 0;
				for (WikiLink wl:wls){
					if (confirmedCategories.contains(wl.getSurface())){
						matchCategories ++;
					}
				}
				if (matchCategories > 0){
					hasMatched = true;
				}
//				System.out.println("\t "+ matchCategories + "/" + totalCategories + " matched");
				
				if (totalCategories !=0){
					value = (double)matchCategories/(double)totalCategories;
				}
				if (value > max){
					max = value;
					maxIndex = i;
				}
				fd1.setFeatureValue("ava.category", new FeatureValue(value));
				fds.add(fd1);
			}
			fsd.setFeatureDocs(fds);
			Gson gson = new Gson();
			String data = gson.toJson(fsd);
//				System.out.println("Pushing data : \n" + data);
			try{
					adapter.uploadMentionFeature(fsd);
			}
			catch(Exception e){
					System.out.println("error: Push wrong!");
					System.out.println(data);
			}
			if (hasMatched == true){
				//update knowledge
				ArrayList<WikiLink> categories = adapter.getPage(md.getCandidates().get(maxIndex).getConcept().getTitleId()).getCategoryLinks();
				for (WikiLink wl:categories){
					confirmedCategories.add(wl.getSurface());
				}
			}//if hasMatched
			else{
				toDoMentions.add(md);
			}//else
		}//for each mention
		
		if (toDoMentions.size() == ambiguousMentions.size()){
			System.out.println("No more ambiguousMentions could be resloved!");
			System.out.println("We are done!");
		}
		else{
			System.out.println("This time we solved " + (numOfUnsolved - toDoMentions.size()) + " out of " + numOfUnsolved);
			System.out.println("We have more ambiguousMentions to solve");
			processAmbiguousMentions(confirmedCategories, toDoMentions);
		}
		
	}
	public static void generateConfirmedCategories(Set<String> confirmedCategories, ArrayList<ExtendedMentionDoc> uniqueMentions) throws Exception{
		System.out.println("generateConfirmedCategories start");
		int count = 0;
		for (ExtendedMentionDoc m:uniqueMentions){
			count ++;
			System.out.println("generateConfirmedCategories: " + count);
			ArrayList<CandidateDoc> candidates = m.getCandidates();
			for (CandidateDoc c:candidates){
				WikiTitle wt = c.getConcept();
				int cdid = wt.getTitleId();
				ArrayList<WikiLink> wls = adapter.getPage(cdid).getCategoryLinks();
				for (WikiLink wl:wls){
					String categoryTitle = wl.getSurface();
//					int cutPoint = categoryTitle.indexOf(':');
//					categoryTitle = categoryTitle.substring(cutPoint +1).trim();
//					System.out.println(categoryTitle);
					confirmedCategories.add(categoryTitle);
				}
			}
		}
		
	}
	
	
	public static void splitMentions(ArrayList<ExtendedMentionDoc> mentions, ArrayList<ExtendedMentionDoc> uniqueMentions, ArrayList<ExtendedMentionDoc> ambiguousMentions) throws Exception{
		System.out.println("splitMentions start");
		int count = 0;
		for (ExtendedMentionDoc e:mentions){
			count ++;
			System.out.println("splitMentions : " + count);
			WikiTitle wt = e.getGold();
			String title = wt.getTitle();
			int id = wt.getTitleId();
//			System.out.println(title);
//			System.out.println(id);
			ArrayList<CandidateDoc> candidates = e.getCandidates();
//			for (CandidateDoc c:candidates){
//				for (WikiLink wl:this.getPage(c.getConcept().getTitleId()).getCategoryLinks()){
////					System.out.println(wl.getSurface());
////					count ++;
//				}
//			}
			if (candidates.size() == 0){
				System.out.println("error: No candidates found for mention :" + e.getGold().getTitle());
			}
			else if (candidates.size() == 1){
				//No ambiguous ones
				uniqueMentions.add(e);
			}
			else{
				//Ambiguous ones
				ambiguousMentions.add(e);
			}
			
		}//for mentions
		System.out.println(count);
	}
	
	
	
	
	

	public static void main(String[] args) throws Exception {
		
		System.out.println("Step 1: Getting all mentions");
		ArrayList<ExtendedMentionDoc> mentions = adapter.getMentions(true, false);
		System.out.println("Found " + mentions.size() + " mentions\n");
		
		System.out.println("Step 2: Classifying mentions to uniqueMentions and ambiguousMentions");
		ArrayList<ExtendedMentionDoc> uniqueMentions = new ArrayList<ExtendedMentionDoc>();
		ArrayList<ExtendedMentionDoc> ambiguousMentions = new ArrayList<ExtendedMentionDoc>();
		splitMentions(mentions, uniqueMentions, ambiguousMentions);
		System.out.println("Have found " + uniqueMentions.size() + " mentions that has unique page");
		System.out.println("Have found " + ambiguousMentions.size() + " mentions that has ambiguous page\n");
		
		
		System.out.println("Step 3: Generating confirmedCategories");
		//Knowledge set
		Set<String> confirmedCategories = new HashSet<String>();
		generateConfirmedCategories(confirmedCategories, uniqueMentions);
        //generateConfirmedCategories(confirmedCategories, ambiguousMentions);
		System.out.println("Has put " + confirmedCategories.size() + " confirmed categories into our knowledge\n");
	
		processAmbiguousMentions(confirmedCategories, uniqueMentions);
		processAmbiguousMentions(confirmedCategories, ambiguousMentions);
//		// TODO Auto-generated method stub
//		WebSAILWikifierAPIAdapter adapter = new WebSAILWikifierAPIAdapter();
//		System.out.println(adapter.getMentions(true, true).get(0).getGold());
//		System.out.println(adapter.getPage(6886).getInternalLinks().get(0)
//				.getTarget().getId());
//		System.out.println(adapter.getMentionFeature("testKey", "nor")
//				.getFeatureDocs().get(0).getFeatureValue("nor.testFeature1")
//				.getValue());
//		
//		FeatureSetDoc fsd = new FeatureSetDoc();
//		fsd.setMentionKey("testKey");
//		ArrayList<FeatureDoc> fds = new ArrayList<FeatureDoc>();
//		
//		FeatureDoc fd1 = new FeatureDoc();
//		fd1.setConceptId(1);
//		fd1.setFeatureValue("nor.testFeature1", new FeatureValue(-100.0));
//		fd1.setFeatureValue("nor.testFeature2", new FeatureValue(0.0));
//		fds.add(fd1);
//
//		FeatureDoc fd2 = new FeatureDoc();
//		fd2.setConceptId(2);
//		fd2.setFeatureValue("nor.testFeature1", new FeatureValue(20.0));
//		fd2.setFeatureValue("nor.testFeature2", new FeatureValue(-40.0));
//		fds.add(fd2);
//		
//		fsd.setFeatureDocs(fds);
//		
//		adapter.uploadMentionFeature(fsd);
		
		
		
		
	}

}
