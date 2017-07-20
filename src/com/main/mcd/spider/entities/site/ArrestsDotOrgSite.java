package com.main.mcd.spider.entities.site;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ArrestsDotOrgSite implements Site {

	private static final Url url = new Url("http://", "arrests.org", new String[]{});
	private static final String name = "Arrests.org";
	private String baseUrl;
	private int pages;
	private int totalRecordCount;
	private static final int[] perRecordSleepRange = new int[]{5,15};
	private Map<Integer,Document> resultsPageDocuments;

	public ArrestsDotOrgSite() {}
	
	@Override
	public Url getUrl() {
		return url;
	}
	@Override
	public String getName() {
		return name;
	}
	@Override
	public int[] getPerRecordSleepRange() {
		return perRecordSleepRange;
	}
	@Override
	public String generateResultsPageUrl(int page/*, int resultsPerPage*/) {
		String builtUrl = baseUrl;
		builtUrl += "/?page="+page;
		/*if (resultsPerPage % 14 == 0) {
			builtUrl += "&results="+resultsPerPage;
		} else {*/
			builtUrl += "&results=56";
		/*}*/
		return builtUrl;
	}
//	@Override
//	public void setResultsPageDocuments(Map<Integer,Document> resultsPageDocuments) {
//		this.resultsPageDocuments = resultsPageDocuments;
//	}
//	@Override
//	public Map<Integer,Document> getResultsPageDocuments() {
//		return this.resultsPageDocuments;
//	}
	@Override
	public String getBaseUrl(String[] args) {
		if (baseUrl==null) {
			Url url = getUrl();
//			String resultsPerPage = args[1];
//			String pageNumber = args[2];
			String builtUrl = url.getProtocol() + (args[0]!=null?args[0]+".":"") + url.getDomain();
//			builtUrl += "/?page="+(pageNumber!=null?pageNumber:"1");
//			builtUrl += "&results="+(resultsPerPage!=null?resultsPerPage:"14");
			baseUrl =  builtUrl.toLowerCase();
		}
		return baseUrl;
	}
	@Override
	public Element getRecordElement(Document doc) {
		//need to return a specific record?
		return null;
	}
	@Override
	public Elements getRecordElements(Document doc) {
		return doc.select(".search-results .profile-card .title a");
	}
	@Override
	public String getRecordDetailDocUrl(Element record) {
		String pdLink = record.attr("href");
		pdLink = pdLink.replace("?d=1", "");
		return getBaseUrl(new String[]{})+pdLink;
	}
	@Override
	public Map<String,String> getRecordDetailDocUrls(List<Document> resultsPageDocs) {
		//TODO
		return null;
	}
	@Override
	public Elements getRecordDetailElements(Document doc) {
		return doc.select(".content-box.profile.profile-full h3, .info .section-content div, .section-content.charges");
	}
	@Override
	public int getTotalPages(Document doc) {
		if (pages==0) {
			Elements pageCountElements = doc.select(".content-box .pager :nth-last-child(2)");
			try {
				pages = Integer.parseInt(pageCountElements.get(0).text());
			} catch (NumberFormatException nfe) {
				pages = 0;
			}
		}
		return pages;
	}
	@Override
	public int getTotalRecordCount(Document doc) {
		if (totalRecordCount==0) {
			int recordsPerPage = 12;//default
			Elements recordsPerDropdown = doc.select(".content-box .pager-options  option[selected=\"selected\"]");
			for (Element recordsPer : recordsPerDropdown) {
				try {
					recordsPerPage = Integer.parseInt(recordsPer.text());
				} catch (NumberFormatException nfe) {
				}
			}
			int pages = getTotalPages(doc);
			totalRecordCount = recordsPerPage * pages;
		}
		return totalRecordCount;
	}
	@Override
	public int getPageNumberFromDoc(Document doc) {
		String url = doc.baseUri();
		int pageNumber = Character.getNumericValue(url.charAt(url.indexOf('&')-1));
		return pageNumber;
	}
	@Override
	public Map<Integer,String> getMiscSafeUrlsFromDoc(Document doc, int pagesToMatch) {
		Elements links = doc.select("a[href]");
		Collections.shuffle(links);
		//get one misc page per results page
		//double the size of the list and only fill the second half
		Map<Integer,String> safeUrls = new HashMap<>();
		for (int u=pagesToMatch+1;u<=pagesToMatch*2;u++) {
			Element link = links.get(u);
			//(ignore rel=stylesheet, include /ABC.php, '/ABC/', '/', '#', '/Arrests/ABC')
			if (!link.hasAttr("rel")
					&& (link.attr("href").endsWith(".php")
						|| link.attr("href").startsWith("/Arrests/")
						//|| link.attr("href").equals("#")
						|| link.attr("href").matches("/[a-zA-Z]+/")
						|| link.attr("href").equals("/"))) {
				safeUrls.put(u,getBaseUrl(null) + link.attr("href"));
			}
		}
		return safeUrls;
	}
}
