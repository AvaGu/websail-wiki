package edu.northwestern.websail.el.client.avagu;

import java.io.IOException;
import java.util.ArrayList;

import edu.northwestern.websail.el.adapter.wikidoc.model.FeatureDoc;
import edu.northwestern.websail.el.adapter.wikidoc.model.FeatureSetDoc;
import edu.northwestern.websail.el.client.api.WebSAILWikifierAPIAdapter;
import edu.northwestern.websail.el.models.FeatureValue;

public class CategoryFeature {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		WebSAILWikifierAPIAdapter adapter = new WebSAILWikifierAPIAdapter();
		System.out.println(adapter.getMentions(true, true).get(0).getGold());
		System.out.println(adapter.getPage(6886).getInternalLinks().get(0)
				.getTarget().getId());
		System.out.println(adapter.getMentionFeature("testKey", "nor")
				.getFeatureDocs().get(0).getFeatureValue("nor.testFeature1")
				.getValue());
		
		FeatureSetDoc fsd = new FeatureSetDoc();
		fsd.setMentionKey("testKey");
		ArrayList<FeatureDoc> fds = new ArrayList<FeatureDoc>();
		
		FeatureDoc fd1 = new FeatureDoc();
		fd1.setConceptId(1);
		fd1.setFeatureValue("nor.testFeature1", new FeatureValue(-100.0));
		fd1.setFeatureValue("nor.testFeature2", new FeatureValue(0.0));
		fds.add(fd1);

		FeatureDoc fd2 = new FeatureDoc();
		fd2.setConceptId(2);
		fd2.setFeatureValue("nor.testFeature1", new FeatureValue(20.0));
		fd2.setFeatureValue("nor.testFeature2", new FeatureValue(-40.0));
		fds.add(fd2);
		
		fsd.setFeatureDocs(fds);
		
		adapter.uploadMentionFeature(fsd);
		
		
		
		
	}

}
