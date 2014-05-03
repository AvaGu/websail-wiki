package edu.northwestern.websail.el.client.feature.example;

import java.util.ArrayList;

import edu.northwestern.websail.el.adapter.wikidoc.model.CandidateDoc;
import edu.northwestern.websail.el.adapter.wikidoc.model.FeatureDoc;
import edu.northwestern.websail.el.adapter.wikidoc.model.FeatureSetDoc;
import edu.northwestern.websail.el.adapter.wikidoc.model.MentionDoc;
import edu.northwestern.websail.el.client.api.WebSAILWikifierAPIAdapter;
import edu.northwestern.websail.el.client.api.model.ExtendedMentionDoc;
import edu.northwestern.websail.el.models.FeatureValue;

public class SurfaceDistanceFeatureExtractor {
	private StringDistance strDist;
	private String mentionSurface;
	public SurfaceDistanceFeatureExtractor(StringDistance strDist, String mentionSurface){
		this.strDist = strDist;
		this.mentionSurface = mentionSurface;
	}
	
	private String getNormalizedTitle(CandidateDoc c){
		return c.getConcept().getTitle().replaceAll("_", " ");
	}
	
	public double extractFeature(CandidateDoc c){
		return strDist.computeDistance(mentionSurface, this.getNormalizedTitle(c));
	}
	
	public static void main(String[] args) throws Exception{
		WebSAILWikifierAPIAdapter adapter = new WebSAILWikifierAPIAdapter();
		StringDistance strDist = new LevenshteinDistance();
		ArrayList<ExtendedMentionDoc> exampleMentions = adapter.getMentions(true, false);
		for(ExtendedMentionDoc m : exampleMentions){
			System.out.println(m.getKey());
			FeatureSetDoc fds = getFSD(m, strDist);
			adapter.uploadMentionFeature(fds);
		}
		ArrayList<ExtendedMentionDoc> targetMentions = adapter.getMentions(false, false);
		for(ExtendedMentionDoc m : targetMentions){
			System.out.println(m.getKey());
			FeatureSetDoc fds = getFSD(m, strDist);
			adapter.uploadMentionFeature(fds);
		}
	}
	
	public static FeatureSetDoc getFSD(MentionDoc m, StringDistance strDist) throws Exception{
		SurfaceDistanceFeatureExtractor fe = new SurfaceDistanceFeatureExtractor(strDist, m.getSurface());
		FeatureSetDoc fsd = new FeatureSetDoc();
		fsd.setMentionKey(m.getKey());
		ArrayList<FeatureDoc> fds = new ArrayList<FeatureDoc>();
		for(CandidateDoc c : m.getCandidates()){
			double value = fe.extractFeature(c);
			FeatureDoc fd = new FeatureDoc();
			fd.setConceptId(c.getConcept().getTitleId());
			fd.setFeatureValue("nor.surface_distance", new FeatureValue(value));
			fds.add(fd);
		}
		fsd.setFeatureDocs(fds);
		return fsd;
	}
}
