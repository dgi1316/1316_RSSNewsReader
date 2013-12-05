package com.rssnews;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.util.Log;

public class RSSParser {

	private static String TAG_CHANNEL = "channel";
	private static String TAG_TITLE = "title";
	private static String TAG_LINK = "link";
	private static String TAG_DESRIPTION = "description";
	private static String TAG_LANGUAGE = "language";
	private static String TAG_ITEM = "item";
	private static String TAG_PUB_DATE = "pubDate";
	private static String TAG_GUID = "guid";

	public RSSParser() {
	}

	/***
	 * Get RSS feed from url	 * 
	 * @param url - is url of the website
	 * @return RSSFeed class object
	 */
	public RSSFeed getRSSFeed(String url) {
		RSSFeed rssFeed = null;
		String rss_feed_xml = null;

		String rss_url = this.getRSSLinkFromURL(url);
		
		if (rss_url != null) {
		
			rss_feed_xml = this.getXmlFromUrl(rss_url);
			
			if (rss_feed_xml != null) {
				try {
					Document doc = this.getDomElement(rss_feed_xml);
					NodeList nodeList = doc.getElementsByTagName(TAG_CHANNEL);
					Element e = (Element) nodeList.item(0);

					String title = this.getValue(e, TAG_TITLE);
					String link = this.getValue(e, TAG_LINK);
					String description = this.getValue(e, TAG_DESRIPTION);
					String language = this.getValue(e, TAG_LANGUAGE);

					rssFeed = new RSSFeed(title, description, link, rss_url,
							language);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
			}
		} else {
		}
		return rssFeed;
	}

	/**
	 * Getting RSS feed items <item>
	 * 
	 * @param - rss link url of the website
	 * @return - List of RSSItem class objects
	 * */
	public List<RSSItem> getRSSFeedItems(String rss_url) {
		List<RSSItem> itemsList = new ArrayList<RSSItem>();
		String rss_feed_xml;

		rss_feed_xml = this.getXmlFromUrl(rss_url);
		if (rss_feed_xml != null) {
			try {
				Document doc = this.getDomElement(rss_feed_xml);
				NodeList nodeList = doc.getElementsByTagName(TAG_CHANNEL);
				Element e = (Element) nodeList.item(0);
				
				NodeList items = e.getElementsByTagName(TAG_ITEM);
				for (int i = 0; i < items.getLength(); i++) {
					Element e1 = (Element) items.item(i);

					String title = this.getValue(e1, TAG_TITLE);
					String link = this.getValue(e1, TAG_LINK);
					String description = this.getValue(e1, TAG_DESRIPTION);
					String pubdate = this.getValue(e1, TAG_PUB_DATE);
					String guid = this.getValue(e1, TAG_GUID);

					RSSItem rssItem = new RSSItem(title, link, description,
							pubdate, guid);
					itemsList.add(rssItem);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return itemsList;
	}

	/**
	 * Getting RSS feed link from HTML source code
	 * 
	 * @param url - is url of the website
	 * @returns url of rss link of website
	 * */
	public String getRSSLinkFromURL(String url) {
		String rss_url = null;
		try {

			org.jsoup.nodes.Document doc = Jsoup.connect(url).get();
			// finding rss links which are having link[type=application/rss+xml]
			org.jsoup.select.Elements links = doc
					.select("link[type=application/rss+xml]");

			Log.d("No of RSS links found", " " + links.size());

			if (links.size() > 0) {
				rss_url = links.get(0).attr("href").toString();
			} else {
				// finding rss links which are having
				// link[type=application/rss+xml]
				org.jsoup.select.Elements links1 = doc
						.select("link[type=application/atom+xml]");
				if (links1.size() > 0) {
					rss_url = links1.get(0).attr("href").toString();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return rss_url;
	}

	/**
	 * Method to get xml content from url HTTP Get request
	 * */
	public String getXmlFromUrl(String url) {
		String xml = null;

		try {
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(url);

			HttpResponse httpResponse = httpClient.execute(httpGet);
			HttpEntity httpEntity = httpResponse.getEntity();
			xml = EntityUtils.toString(httpEntity);

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return xml;
	}

	/**
	 * Getting XML DOM element
	 * 
	 * @param XML string
	 * */
	public Document getDomElement(String xml) {
		Document doc = null;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {

			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(xml));
			doc = (Document) db.parse(is);

		} catch (ParserConfigurationException e) {
			Log.e("Error: ", e.getMessage());
			return null;
		} catch (SAXException e) {
			Log.e("Error: ", e.getMessage());
			return null;
		} catch (IOException e) {
			Log.e("Error: ", e.getMessage());
			return null;
		}
		return doc;
	}

	/**
	 * Getting node value
	 * 
	 * @param elem element
	 */
	public final String getElementValue(Node elem) {
		Node child;
		if (elem != null) {
			if (elem.hasChildNodes()) {
				for (child = elem.getFirstChild(); child != null; child = child
						.getNextSibling()) {
					if (child.getNodeType() == Node.TEXT_NODE
							|| (child.getNodeType() == Node.CDATA_SECTION_NODE)) {
						return child.getNodeValue();
					}
				}
			}
		}
		return "";
	}

	/**
	 * Getting node value
	 * 
	 * @param Element node
	 * @param key string
	 * */
	public String getValue(Element item, String str) {
		NodeList n = item.getElementsByTagName(str);
		return this.getElementValue(n.item(0));
	}
}
