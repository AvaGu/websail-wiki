package edu.northwestern.websail.el.client.api.model.feature;

import java.util.ArrayList;
import java.util.HashMap;

import org.jongo.marshall.jackson.oid.ObjectId;
/**
 * this will be deleted soon
 * @author NorThanapon
 *
 */
public class FeatureMapSet {
	@ObjectId
	protected String _id; //for jongo
	private String mentionKey; // ref in mongodb;
	private ArrayList<FeatureMap> featureDocs;
	private long ts;
	public FeatureMapSet(){
		this.featureDocs = new ArrayList<FeatureMap>();
		setTS(System.currentTimeMillis());
	}

	public String getKey() {
		return _id;
	}

	public String getMentionKey() {
		return mentionKey;
	}

	public HashMap<Integer, FeatureMap> getFeatureMap() {
		HashMap<Integer, FeatureMap> featureMap = new HashMap<Integer, FeatureMap>();
		
		for(FeatureMap fd : featureDocs) {
			featureMap.put(fd.getConceptId(), fd);
		}
		
		return featureMap;
	}

	public void setKey(String key) {
		this._id = key;
	}

	public void setMentionKey(String mentionKey) {
		this.mentionKey = mentionKey;
	}

	public ArrayList<FeatureMap> getFeatureDocs() {
		return featureDocs;
	}

	public void setFeatureDocs(ArrayList<FeatureMap> featureDocs) {
		this.featureDocs = featureDocs;
	}

	public long getTS() {
		return ts;
	}

	public void setTS(long ts) {
		this.ts = ts;
	}

//	public void setFeatureMap(HashMap<Integer, FeatureDoc> featureMap) {
//		this.featureMap = featureMap;
//	}
	
	
	
}
