package edu.northwestern.websail.el.client.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import edu.northwestern.websail.el.adapter.wikidoc.model.FeatureDoc;
import edu.northwestern.websail.el.adapter.wikidoc.model.FeatureSetDoc;
import edu.northwestern.websail.el.client.api.model.ExtendedMentionDoc;
import edu.northwestern.websail.el.client.api.model.Response;
import edu.northwestern.websail.el.models.FeatureValue;
import edu.northwestern.websail.wikiparser.parsers.model.WikiExtractedPage;

public class WebSAILWikifierAPIAdapter {

	public static final Logger logger = Logger
			.getLogger(WebSAILWikifierAPIAdapter.class.getName());
	private static final String serviceRoot = "http://websail-fe.cs.northwestern.edu:8080/wikifier";

	public WebSAILWikifierAPIAdapter() {

	}

	public String get(String api) throws IOException {
		String endPoint = serviceRoot + "/" + api;
		URL u = new URL(endPoint);
		HttpURLConnection uc = (HttpURLConnection) u.openConnection();
		logger.fine("Requesting data from " + u);
		uc.connect();
		int responseCode = uc.getResponseCode();
		if (200 == responseCode) {
			logger.fine("OK");
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
				logger.severe("Unexpected error while reading requested data.");
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
			logger.severe("Cannot get data from " + u + " with error status "
					+ responseCode);
			return null;
		}
	}

	public String post(String api, Object obj) throws ClientProtocolException,
			IOException {
		String endPoint = serviceRoot + "/" + api;
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpPost postRequest = new HttpPost(endPoint);

		Gson gson = new Gson();
		String data = gson.toJson(obj);
		StringEntity input = new StringEntity(data);
		input.setContentType("application/json");
		postRequest.setEntity(input);

		HttpResponse response = httpClient.execute(postRequest);

		if (response.getStatusLine().getStatusCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : "
					+ response.getStatusLine().getStatusCode());
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(
				(response.getEntity().getContent())));

		String output;
		String res = "";
		while ((output = br.readLine()) != null) {
			res += output;
		}

		httpClient.close();
		return res;
	}

	// public String post(String api, String data) throws IOException {
	// String endPoint = serviceRoot + "/" + api;
	// URL u = new URL(endPoint);
	// HttpURLConnection connection = (HttpURLConnection) u.openConnection();
	// logger.fine("Posting data from " + u);
	// connection.setDoOutput(true);
	// connection.setDoInput(true);
	// connection.setInstanceFollowRedirects(false);
	// connection.setRequestMethod("POST");
	// connection.setRequestProperty("Content-Type",
	// "application/json; charset=UTF-8;");
	// connection.setRequestProperty("charset", "utf-8");
	// connection.setRequestProperty("Content-Length",
	// "" + Integer.toString(data.getBytes().length));
	// connection.setUseCaches(false);
	// DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
	// wr.writeBytes(data);
	// wr.flush();
	// wr.close();
	//
	// int responseCode = connection.getResponseCode();
	// if (200 == responseCode) {
	// logger.fine("OK");
	// BufferedReader br = null;
	// StringBuilder sb = new StringBuilder();
	// String line;
	// try {
	// br = new BufferedReader(new InputStreamReader(
	// connection.getInputStream()));
	// while ((line = br.readLine()) != null) {
	// sb.append(line);
	// }
	// } catch (IOException e) {
	// logger.severe("Unexpected error while reading requested data.");
	// e.printStackTrace();
	// } finally {
	// if (br != null) {
	// try {
	// br.close();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }
	// }
	// connection.disconnect();
	// return sb.toString();
	// } else {
	// logger.severe("Cannot get data from " + u + " with error status "
	// + responseCode);
	// connection.disconnect();
	// return null;
	// }
	//
	// }

	private InputStream getInputStream(String api) throws IOException {
		String endPoint = serviceRoot + "/" + api;
		URL u = new URL(endPoint);
		HttpURLConnection uc = (HttpURLConnection) u.openConnection();
		logger.fine("Requesting data from " + u);
		uc.connect();
		int responseCode = uc.getResponseCode();
		if (200 == responseCode) {
			logger.fine("OK");
			return uc.getInputStream();
		} else {
			logger.severe("Cannot get data from " + u + " with error status "
					+ responseCode);
			return null;
		}
	}

	public WikiExtractedPage getPage(int titleId) throws IOException {
		InputStream json = this.getInputStream("resource/article/" + titleId);
		Gson gson = new Gson();
		Type type = new TypeToken<Response<WikiExtractedPage>>() {
		}.getType();
		BufferedReader br = new BufferedReader(new InputStreamReader(json));
		Response<WikiExtractedPage> r = gson.fromJson(br, type);
		br.close();
		return r.getResponse();
	}

	public ArrayList<ExtendedMentionDoc> getMentions(boolean isExample,
			boolean isSmall) throws IOException {
		String api = "resource/";
		if (isSmall)
			api += "small/";
		if (isExample)
			api += "examples";
		else
			api += "targets";

		InputStream json = this.getInputStream(api);
		Gson gson = new Gson();
		Type type = new TypeToken<Response<ArrayList<ExtendedMentionDoc>>>() {
		}.getType();
		BufferedReader br = new BufferedReader(new InputStreamReader(json));
		Response<ArrayList<ExtendedMentionDoc>> response = gson.fromJson(br,
				type);
		br.close();
		return response.getResponse();
	}

	public FeatureSetDoc getMentionFeature(String mentionKey, String groupName)
			throws IOException {
		String api = "feature/get/" + mentionKey + "/" + groupName;
		InputStream json = this.getInputStream(api);
		Gson gson = new Gson();
		Type type = new TypeToken<Response<FeatureSetDoc>>() {
		}.getType();
		BufferedReader br = new BufferedReader(new InputStreamReader(json));
		Response<FeatureSetDoc> response = gson.fromJson(br, type);
		br.close();
		return response.getResponse();
	}

	public boolean uploadMentionFeature(FeatureSetDoc fms)
			throws ClientProtocolException, IOException {
		String res = this.post("feature/post", fms);
		return res
				.equalsIgnoreCase("{\"status\":200,\"response\":\"ok\",\"message\":\"OK\"}");

	}

	public static void main(String[] args) throws Exception {
		WebSAILWikifierAPIAdapter adapter = new WebSAILWikifierAPIAdapter();
		System.out.println(adapter.getMentions(true, true).get(0).getGold());
		System.out.println(adapter.getPage(6886).getInternalLinks().get(0).getSurface() + "->" +adapter.getPage(6886).getInternalLinks().get(0)
				.getTarget().getId());
		System.out.println(adapter.getMentionFeature("testKey", "nor")
				.getFeatureDocs().get(0).getFeatureValue("nor.testFeature1")
				.getValue());
		
		FeatureSetDoc fsd = new FeatureSetDoc();
		fsd.setMentionKey("testKey");
		ArrayList<FeatureDoc> fds = new ArrayList<FeatureDoc>();
		
		FeatureDoc fd1 = new FeatureDoc();
		fd1.setConceptId(1);
		fd1.setFeatureValue("nor.testFeature1", new FeatureValue(-500.0));
		fd1.setFeatureValue("nor.testFeature2", new FeatureValue(0.0));
		fds.add(fd1);

		FeatureDoc fd2 = new FeatureDoc();
		fd2.setConceptId(2);
		fd2.setFeatureValue("nor.testFeature1", new FeatureValue(20.0));
		fd2.setFeatureValue("nor.testFeature2", new FeatureValue(-40.0));
		fds.add(fd2);
		
		fsd.setFeatureDocs(fds);
		
		adapter.uploadMentionFeature(fsd);
		
		// FeatureMapSet fs = new FeatureMapSet();
		// fs.setMentionKey("testKey");
		// FeatureMap fm1 = new FeatureMap(1);
		// FeatureMap fm2 = new FeatureMap(2);
		// FeatureMap fm3 = new FeatureMap(3);
		// fm1.setFeatureValue("nor.test4", 100.0);
		// fm2.setFeatureValue("nor.test4", 10.0);
		// fm3.setFeatureValue("nor.test4", 10.0);
		// fs.getFeatureDocs().add(fm1);
		// fs.getFeatureDocs().add(fm2);
		// fs.getFeatureDocs().add(fm3);
		// Gson gson = new Gson();
		// String data = gson.toJson(fs);
		// System.out.println(data);
		// System.out.println(adapter.uploadMentionFeature(fs));
	}

}
