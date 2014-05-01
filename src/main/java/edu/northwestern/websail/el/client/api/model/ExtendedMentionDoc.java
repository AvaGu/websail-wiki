package edu.northwestern.websail.el.client.api.model;

import java.util.HashMap;
import java.util.List;

import edu.northwestern.websail.el.adapter.wikidoc.model.FeatureDoc;
import edu.northwestern.websail.el.adapter.wikidoc.model.MentionDoc;
import edu.northwestern.websail.el.models.Token;

public class ExtendedMentionDoc extends MentionDoc {
	private String key;
	private HashMap<Integer, FeatureDoc> featureSet;
	private List<Token> contextTokens;
	public ExtendedMentionDoc() {
	}

	public ExtendedMentionDoc(MentionDoc mentionDoc, List<Token> contextTokens, HashMap<Integer, FeatureDoc> featureSet) {
		super(
				mentionDoc.getContext(), 
				mentionDoc.getSurface(), 
				mentionDoc.getLocalStartOffset(), 
				mentionDoc.getLocalEndOffset(), 
				mentionDoc.getStartOffset(),
				mentionDoc.getEndOffset()
		);
		this.featureSet = featureSet;
		this.setContextTokens(contextTokens);
	}

	public HashMap<Integer, FeatureDoc> getFeatureSet() {
		return featureSet;
	}

	public void setFeatureSet(HashMap<Integer, FeatureDoc> featureSet) {
		this.featureSet = featureSet;
	}

	public List<Token> getContextTokens() {
		return contextTokens;
	}

	public void setContextTokens(List<Token> contextTokens) {
		this.contextTokens = contextTokens;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	
	
}