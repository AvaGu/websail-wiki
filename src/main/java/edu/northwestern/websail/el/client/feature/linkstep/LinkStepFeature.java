package edu.northwestern.websail.el.client.feature.linkstep;

import java.util.ArrayList;

import edu.northwestern.websail.el.adapter.wikidoc.model.CandidateDoc;
import edu.northwestern.websail.el.adapter.wikidoc.model.FeatureDoc;
import edu.northwestern.websail.el.adapter.wikidoc.model.FeatureSetDoc;
import edu.northwestern.websail.el.client.api.WebSAILWikifierAPIAdapter;
import edu.northwestern.websail.el.client.api.model.ExtendedMentionDoc;
import edu.northwestern.websail.el.models.FeatureValue;
import edu.northwestern.websail.wikiparser.parsers.model.element.WikiLink;

public class LinkStepFeature {

	WebSAILWikifierAPIAdapter adapter;

	public void getLinkNumFromSourceInLinks(
			ArrayList<ExtendedMentionDoc> mentions) throws Exception {
		// System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		WebSAILWikifierAPIAdapter adapter = new WebSAILWikifierAPIAdapter();
		int mentionNum = mentions.size();
		System.out.println("Total mention number: " + mentionNum);
		int[][] candidateMap = new int[100][mentionNum];
		int[][] output = new int[100][mentionNum];

		int mentionNo = 0;

		for (ExtendedMentionDoc e : mentions) {

			String sourceId = e.getArticle().getSourceId();

			ArrayList<CandidateDoc> candidates = e.getCandidates();
			// System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
			int candiNo = 0;
			for (CandidateDoc c : candidates) {

				candidateMap[candiNo][mentionNo] = c.getConcept().getTitleId();

				candiNo++;
			}
			ArrayList<WikiLink> internalLinks = adapter.getPage(
					Integer.parseInt(sourceId)).getInternalLinks();
			// System.out.println("internal links of page: " + source + ", " +
			// sourceId);

			int j = 0;
			while (j < internalLinks.size()) {
				int linkId = internalLinks.get(j).getTarget().getId();
				// if(linkId!=id)
				// {
				ArrayList<WikiLink> firstLevelLink = adapter.getPage(linkId)
						.getInternalLinks();
				int k1 = 0;
				while (k1 < firstLevelLink.size()) {
					int k2 = 0;
					while (k2 < 100) {
						if (firstLevelLink.get(k1).getTarget().getId() == candidateMap[k2][mentionNo]) {
							output[k2][mentionNo]++;

						}
						k2++;
					}
					k1++;

				}

				j++;
			}
			System.out.println("The " + mentionNo + " mention is done!");
			mentionNo++;
		}

		int j = 0;
		for (ExtendedMentionDoc e : mentions) {

			FeatureSetDoc fsd = new FeatureSetDoc();
			fsd.setMentionKey(e.getKey());
			System.out.println("uploading mention " + j + ", key: "
					+ e.getKey());
			ArrayList<FeatureDoc> fds = new ArrayList<FeatureDoc>();
			int i = 0;
			for (CandidateDoc c : e.getCandidates()) {

				double value = (double) output[i][j];

				FeatureDoc fd = new FeatureDoc();
				fd.setConceptId(c.getConcept().getTitleId());
				fd.setFeatureValue("yuxi.link_state", new FeatureValue(value));
				fds.add(fd);

				i++;
			}
			fsd.setFeatureDocs(fds);
			adapter.uploadMentionFeature(fsd);
			System.out
					.println("Mention + " + j + " is uploaded successfully!!");
			j++;
		}
		System.out.println("All done!!!");
	}

	public void splitMentions(ArrayList<ExtendedMentionDoc> mentions,
			ArrayList<ExtendedMentionDoc> uniqueMentions,
			ArrayList<ExtendedMentionDoc> ambiguousMentions) throws Exception {
		System.out.println("splitMentions start");
		int count = 0;
		for (ExtendedMentionDoc e : mentions) {
			count++;

			ArrayList<CandidateDoc> candidates = e.getCandidates();

			if (candidates.size() == 0) {
				System.out.println("error: No candidates found for mention :"
						+ e.getGold().getTitle());
			} else if (candidates.size() == 1) {
				// No ambiguous ones
				uniqueMentions.add(e);
			} else {
				// Ambiguous ones
				ambiguousMentions.add(e);

			}

		}// for mentions
		System.out.println(count);
	}

	public void featureLinkStep() throws Exception {
		// Get all mentions in an article
		System.out.println("Step 1: Get all mentions");
		ArrayList<ExtendedMentionDoc> mentions = adapter.getMentions(false,
				true);
		System.out.println("Found " + mentions.size() + " mentions\n");

		System.out.println("Step 2: Get link numbers from souce page links");

		getLinkNumFromSourceInLinks(mentions);

	}

	public WebSAILWikifierAPIAdapter getAdapter() {
		return adapter;
	}

	public void setAdapter(WebSAILWikifierAPIAdapter adapter) {
		this.adapter = adapter;
	}

	public static void main(String[] args) throws Exception {
		WebSAILWikifierAPIAdapter adapter = new WebSAILWikifierAPIAdapter();

		LinkStepFeature stepfeature = new LinkStepFeature();
		stepfeature.setAdapter(adapter);
		stepfeature.featureLinkStep();
	}

}
