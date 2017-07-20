package com.main.mcd.spider.entities.site;

import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public interface Site {
	
	
	String getBaseUrl(String[] arg);
	String getName();
	Url getUrl();
	Element getRecordElement(Document doc);
	Elements getRecordElements(Document doc);
	String getRecordDetailDocUrl(Element record);
	Map<String,String> getRecordDetailDocUrls(List<Document> resultsPageDocs);
	Elements getRecordDetailElements(Document doc);
	int getTotalPages(Document doc);
	int getTotalRecordCount(Document doc);
	String generateResultsPageUrl(int page/*, int resultsPerPage*/);
//	Map<Integer,Document> getResultsPageDocuments();
//	void setResultsPageDocuments(Map<Integer,Document> resultsPageDocuments);
	int[] getPerRecordSleepRange();
	int getPageNumberFromDoc(Document doc);
	Map<Integer,String> getMiscSafeUrlsFromDoc(Document doc, int pagesToMatch);
	
}
