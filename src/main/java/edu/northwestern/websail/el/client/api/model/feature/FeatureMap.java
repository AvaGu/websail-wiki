package edu.northwestern.websail.el.client.api.model.feature;

import java.util.HashMap;

/**
 * this will be deleted soon
 * @author NorThanapon
 *
 */
public class FeatureMap {
	private int conceptId;
	private HashMap<String, HashMap<String, Double>> featureGroups;
	
	public FeatureMap(){
		this.featureGroups = new HashMap<String, HashMap<String, Double>>();
	}
	
	public FeatureMap(int conceptId){
		this();
		this.conceptId = conceptId;
		
	}
	
	public int getConceptId() {
		return conceptId;
	}
	public HashMap<String, HashMap<String, Double>> getFeatureGroups() {
		return featureGroups;
	}
	public void setConceptId(int conceptId) {
		this.conceptId = conceptId;
	}
	public void setFeatureGroups(
			HashMap<String, HashMap<String, Double>> featureGroups) {
		this.featureGroups = featureGroups;
	}
	
	public Double getFeatureValue(String featureName) {
		String[] parts = featureName.split("\\.");
		String group = parts[0];
		String key = parts[1];
		return this.featureGroups.get(group).get(key);
	}

	public Double setFeatureValue(String featureName, Double value)
			throws Exception {
		String[] parts = featureName.split("\\.");
		String group = parts[0];
		String key = parts[1];
		if (parts.length != 2) {
			throw new Exception(
					"Feature name should be in a format of <group>.<key>");
		}
		if (!this.featureGroups.containsKey(group)) {
			this.featureGroups.put(group, new HashMap<String, Double>());
		}
		Double old = this.featureGroups.get(group).get(key);
		this.featureGroups.get(group).put(key, value);
		return old;
	}
}

