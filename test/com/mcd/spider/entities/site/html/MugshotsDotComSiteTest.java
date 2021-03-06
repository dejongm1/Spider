package com.mcd.spider.entities.site.html;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

import org.apache.commons.validator.routines.UrlValidator;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.mcd.spider.entities.record.State;

public class MugshotsDotComSiteTest {

    static final Logger logger = Logger.getLogger(MugshotsDotComSiteTest.class);
    MugshotsDotComSite mockTexasSite = new MugshotsDotComSite(new String[]{State.TX.getName()});
    MugshotsDotComSite mockArizonaSite = new MugshotsDotComSite(new String[]{State.AZ.getAbbreviation()});
    Document mockMainPageDoc;
    Document mockDetailDoc;
    Document mockDetailDocVariation;

    @BeforeClass
    public void setUpClass() throws IOException {
        logger.info("********** Starting Test cases for MugshotsDotComSite *****************");
        System.setProperty("TestingSpider", "true");
        mockMainPageDoc = Jsoup.parse(new File("test/resources/htmls/mainPageDoc_MugshotsDotCom.html"), "UTF-8");
        mockDetailDoc = Jsoup.parse(new File("test/resources/htmls/recordDetailPage_MugshotsDotCom.html"), "UTF-8");
        mockDetailDocVariation = Jsoup.parse(new File("test/resources/htmls/recordDetailPage2_MugshotsDotCom.html"), "UTF-8");
    }

    @AfterClass
    public void tearDownClass() {
        System.setProperty("TestingSpider", "false");
        logger.info("********** Finishing Test cases for MugshotsDotComSite *****************");
    }
    @Test
    public void testConstructor_TexasFullName() {
        Assert.assertEquals(mockTexasSite.getBaseUrl(), "https://mugshots.com/US-Counties/Texas");
    }

    @Test
    public void testConstructor_ArizonaAbbreviation() {
        Assert.assertEquals(mockArizonaSite.getBaseUrl(), "https://mugshots.com/US-Counties/Arizona");
    }

    @Test
    public void testObtainRecordId_Success() {
        String mockUrl = "http://mugshots.com/US-Counties/Arizona/Scott-County-AZ/Bill-Allen-Manfree-Jr.141436546.html";
        Assert.assertEquals(mockArizonaSite.obtainRecordId(mockUrl), "Bill-Allen-Manfree-Jr.141436546");
    }

    @Test
    public void testObtainRecordId_NoRecordIDinUrl() {
        String mockUrl = "http://mugshots.com/Most-Wanted/Arizona/Scott-County-AZ/Bill-Allen-Manfree-Jr.141436546.html";
        Assert.assertNull(mockArizonaSite.obtainRecordId(mockUrl));
    }

    @Test
    public void testGetRecordElements() {
        Elements mockElements = mockArizonaSite.getRecordElements(mockMainPageDoc);
        Assert.assertEquals(mockElements.size(), 120);
        Assert.assertTrue(mockElements.get(0).text().contains("Kev J Vohs"));
    }

    @Test
    public void testGetRecordDetailDocUrl() {
        Element mockRecordElement = new Element("a");
        mockRecordElement.attr("href", "/US-Counties/Arizona/Black-County-AZ/Clyde-Jones-Jr.156388932.html");
        mockRecordElement.attr("class", "image-preview");
        mockRecordElement.html("<div class=\"image\">\n"+
                "\t\t\t\t\t\t\t\n"+
                "\t\t\t\t\t\t\t\t<div class=\"no-image\">No Mugshot<br>Available</div>\n"+
                "\t\t\t\t\t\t\t\n"+
                "\t\t\t\t\t\t</div>\n"+
                "\t\t\t\t\t\t<div class=\"label\">Clyde Jones Jr.</div>");
        Assert.assertEquals(mockArizonaSite.getRecordDetailDocUrl(mockRecordElement), "https://mugshots.com/US-Counties/Arizona/Clyde-Jones-Jr.156388932.html");
    }

    @Test
    public void testGetRecordDetailDocUrl_NotAProperRecordElement() {
        Element mockRecordElement = new Element("a");
        mockRecordElement.attr("href", "/Clyde-Jones-Jr.156388932.html");
        mockRecordElement.attr("class", "image-preview");
        mockRecordElement.html("<div class=\"image\">\n"+
                "\t\t\t\t\t\t\t\n"+
                "\t\t\t\t\t\t\t\t<div class=\"no-image\">No Mugshot<br>Available</div>\n"+
                "\t\t\t\t\t\t\t\n"+
                "\t\t\t\t\t\t</div>\n"+
                "\t\t\t\t\t\t<div class=\"label\">Clyde Jones Jr.</div>");
        Assert.assertEquals(mockArizonaSite.getRecordDetailDocUrl(mockRecordElement), "");
    }

    @Test
    public void testGetRecordDetailDocUrl_UsingGetRecordElements() {
        Elements mockElements = mockArizonaSite.getRecordElements(mockMainPageDoc);
        Assert.assertEquals(mockArizonaSite.getRecordDetailDocUrl(mockElements.get(0)), "https://mugshots.com/US-Counties/Arizona/Kev-J-Vohs.156562647.html");
    }

    @Test
    public void testGetRecordDetailElements() {
        Elements mockRecordDetails = mockTexasSite.getRecordDetailElements(mockDetailDoc);
        Assert.assertEquals(mockRecordDetails.size(), 13);
        Assert.assertEquals(mockRecordDetails.get(2).select("span.value").text(), "Visah,Salim NMN");
        Assert.assertEquals(mockRecordDetails.get(4).select("span.value").text(), "22");
    }

    @Test
    public void testGetRecordDetailElementsVariation() {
        Elements mockRecordDetails = mockTexasSite.getRecordDetailElements(mockDetailDocVariation);
        Assert.assertEquals(mockRecordDetails.size(), 16);
        Assert.assertEquals(mockRecordDetails.get(2).select("span.value").text(), "Brain Clark Stan");
        Assert.assertEquals(mockRecordDetails.get(3).select("span.value").text(), "30");
    }

    @Test
    public void testGenerateResultsPageUrl_BlackHawkCountyIA() {
        MugshotsDotComSite mockIowaSite = new MugshotsDotComSite(new String[]{"Iowa"});
        Assert.assertEquals(mockIowaSite.generateResultsPageUrl("Black Hawk"), "https://mugshots.com/US-Counties/Iowa/Black-Hawk-County-IA");
    }

    @Test
    public void testGenerateResultsPageUrl_TravisCountyTX() {
        MugshotsDotComSite mockIowaSite = new MugshotsDotComSite(new String[]{"TX"});
        Assert.assertEquals(mockIowaSite.generateResultsPageUrl("Travis"), "https://mugshots.com/US-Counties/Texas/Travis-County-TX");
    }

    @Test
    public void testGetMiscSafeUrlsFromDoc_Detail() {
        String[] schemes = {"http","https"};
        UrlValidator urlValidator = new UrlValidator(schemes);
        Map<Object, String> urlMap = mockTexasSite.getMiscSafeUrlsFromDoc(mockDetailDoc, 5);
        for (Map.Entry<Object, String> entry : urlMap.entrySet()) {
            Assert.assertTrue(urlValidator.isValid(entry.getValue()));
        }
        Assert.assertTrue(urlMap.size()>0);
        Assert.assertTrue(urlMap.size()<=5);
    }

    @Test
    public void testGetMiscSafeUrlsFromDocVariation_Detail() {
        String[] schemes = {"http","https"};
        UrlValidator urlValidator = new UrlValidator(schemes);
        Map<Object, String> urlMap = mockTexasSite.getMiscSafeUrlsFromDoc(mockDetailDocVariation, 5);
        for (Map.Entry<Object, String> entry : urlMap.entrySet()) {
            Assert.assertTrue(urlValidator.isValid(entry.getValue()));
        }
        Assert.assertTrue(urlMap.size()>0);
        Assert.assertTrue(urlMap.size()<=5);
    }

    @Test
    public void testGetMiscSafeUrlsFromDoc_Results() {
        String[] schemes = {"http","https"};
        UrlValidator urlValidator = new UrlValidator(schemes);
        Map<Object, String> urlMap = mockArizonaSite.getMiscSafeUrlsFromDoc(mockMainPageDoc, 10);
        for (Map.Entry<Object, String> entry : urlMap.entrySet()) {
            Assert.assertTrue(urlValidator.isValid(entry.getValue()));
            Assert.assertNull(mockArizonaSite.obtainRecordId(entry.getValue()));
        }
        Assert.assertTrue(urlMap.size()>0);
        Assert.assertTrue(urlMap.size()<=10);
    }

    @Test
    public void testIsAResultsDoc() {
        Assert.assertTrue(mockTexasSite.isAResultsDoc((mockMainPageDoc)));
        Assert.assertFalse(mockTexasSite.isAResultsDoc((mockDetailDoc)));
        Assert.assertFalse(mockTexasSite.isAResultsDoc((mockDetailDocVariation)));
    }

    @Test
    public void testIsARecordDetailDoc() {
        Assert.assertTrue(mockTexasSite.isARecordDetailDoc(mockDetailDoc));
        Assert.assertTrue(mockArizonaSite.isARecordDetailDoc(mockDetailDoc));
        Assert.assertTrue(mockTexasSite.isARecordDetailDoc(mockDetailDocVariation));
        Assert.assertTrue(mockArizonaSite.isARecordDetailDoc(mockDetailDocVariation));
    }

    @Test
    public void testObtainDetailUrl() {
    	Assert.assertEquals(mockTexasSite.obtainDetailUrl("Joe-Blow-32132342"), "https://mugshots.com/US-Counties/Texas/Joe-Blow-32132342.html");
    }

    @Test
    public void testGetNextResultsPageUrl_New() {
    	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    	Calendar cal = Calendar.getInstance();
    	String recentDate = dateFormat.format(cal.getTime());
    	String htmlDoc = "<html><body>"
    			+ "<div class=\"pagination\">"
				+ "<a href=\"?from="+recentDate+"+23%3A30%3A42.173479&amp;from=156225870\" class=\"next page\">Next</a>"
				+ "</div>"
				+ "</body></html>";
        Document doc = Jsoup.parse(htmlDoc);

        Assert.assertNotNull(mockTexasSite.getNextResultsPageUrl(doc));
        Assert.assertNotNull(mockArizonaSite.getNextResultsPageUrl(doc));
    }

    @Test
    public void testGetNextResultsPageUrl_None() {
    	String htmlDoc = "<html><body>"
    			+ "<div class=\"pagination\">"
				+ "<a href=\"None\" class=\"next page\">Next</a>"
				+ "</div>"
				+ "</body></html>";
        Document doc = Jsoup.parse(htmlDoc);

        Assert.assertNull(mockTexasSite.getNextResultsPageUrl(doc));
        Assert.assertNull(mockArizonaSite.getNextResultsPageUrl(doc));
    }
    
    @Test
    public void testGetNextResultsPageUrl_Old() {
        Assert.assertNull(mockTexasSite.getNextResultsPageUrl(mockMainPageDoc));
        Assert.assertNull(mockArizonaSite.getNextResultsPageUrl(mockMainPageDoc));
    }
}