package edu.northwestern.websail.el.client.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import edu.northwestern.websail.el.client.api.model.ExtendedMentionDoc;
import edu.northwestern.websail.el.client.api.model.Response;
import edu.northwestern.websail.wikiparser.parsers.model.WikiExtractedPage;

public class ResourceAPIAdapter {

	private static final String serviceRoot = "http://chandra.cs.northwestern.edu:8080/wikifier/resource";

	public ResourceAPIAdapter() {

	}

	public String get(String api) throws IOException {
		String endPoint = serviceRoot + "/" + api;
		URL u = new URL(endPoint);
		HttpURLConnection uc = (HttpURLConnection) u.openConnection();
		uc.connect();
		int responseCode = uc.getResponseCode();
		if (200 == responseCode) {
			BufferedReader br = null;
			StringBuilder sb = new StringBuilder();
			String line;
			try {
				br = new BufferedReader(new InputStreamReader(
						uc.getInputStream()));
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return sb.toString();
		} else {
			return null;
		}
	}
	
	public WikiExtractedPage getPage(int titleId) throws IOException{
		String json = this.get("article/"+titleId);
		Gson gson = new Gson();
		Type type = new TypeToken<Response<WikiExtractedPage>>() {
        }.getType();
        Response<WikiExtractedPage> r = gson.fromJson(json, type);
        return r.getResponse();
	}
	
	public ArrayList<ExtendedMentionDoc> getMentions(boolean isExample, boolean isSmall) throws IOException{
		String api = "";
		if(isSmall) api = "small/";
		if(isExample)
			api += "examples";
		else
			api += "targets";
		
		String json = this.get(api);
		Gson gson = new Gson();
		Type type = new TypeToken<Response<ArrayList<ExtendedMentionDoc>>>() {
        }.getType();
        Response<ArrayList<ExtendedMentionDoc>> response = gson.fromJson(json, type);
        return response.getResponse();
	}
	
	public static void main(String[] args) throws IOException{
		ResourceAPIAdapter adapter = new ResourceAPIAdapter();
		System.out.println(adapter.getMentions(true, true).get(0).getGold());
        System.out.println(adapter.getPage(6886).getTitle());
	}

}
