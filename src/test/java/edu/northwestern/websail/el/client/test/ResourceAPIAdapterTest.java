package edu.northwestern.websail.el.client.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;

import edu.northwestern.websail.el.client.api.ResourceAPIAdapter;
import edu.northwestern.websail.el.client.api.model.ExtendedMentionDoc;
import edu.northwestern.websail.wikiparser.parsers.model.WikiExtractedPage;

public class ResourceAPIAdapterTest {

	public ResourceAPIAdapterTest(){}
	
	@Test
	public void testGetPage() throws IOException{
		ResourceAPIAdapter api = new ResourceAPIAdapter();
		Integer chicagoTitleId = 6886;
		WikiExtractedPage chicago = api.getPage(chicagoTitleId);
		assertNotNull("Page is not null", chicago);
		assertEquals("Title Id is the same", chicagoTitleId, (Integer)chicago.getTitle().getId());
		assertEquals("Title is Chicago", "Chicago", chicago.getTitle().getTitle());
	}
	
	@Test
	public void testGetExampleMentions() throws IOException{
		ResourceAPIAdapter api = new ResourceAPIAdapter();
		ArrayList<ExtendedMentionDoc> exampleMentions = api.getMentions(true, true);
		assertNotNull("Mention list is not null", exampleMentions);
		for(ExtendedMentionDoc mention: exampleMentions){
			assertNotNull("Each mention is not null", mention);
			assertNotNull("Gold annotation is not null", mention.getGold());
		}
	}
	
	@Test
	public void testGetTestMentions() throws IOException{
		ResourceAPIAdapter api = new ResourceAPIAdapter();
		ArrayList<ExtendedMentionDoc> exampleMentions = api.getMentions(false, true);
		assertNotNull("Mention list is not null", exampleMentions);
		for(ExtendedMentionDoc mention: exampleMentions){
			assertNotNull("Each mention is not null", mention);
			assertTrue("Gold annotation is null", mention.getGold() == null);
		}
	}
}
