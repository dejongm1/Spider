package com.mcd.spider.engine;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mcd.spider.engine.record.various.ArrestsDotOrgEngine;
import com.mcd.spider.entities.record.ArrestRecord;
import com.mcd.spider.entities.record.Record;
import com.mcd.spider.entities.record.State;
import com.mcd.spider.entities.record.filter.RecordFilter.RecordFilterEnum;
import com.mcd.spider.entities.site.html.ArrestsDotOrgSite;
import com.mcd.spider.entities.site.service.DesMoinesRegisterComSite;
import com.mcd.spider.util.io.RecordIOUtil;

/**
 * 
 * @author u569220
 *
 */

public class SpiderEngineTest {

	private static Logger logger = Logger.getLogger(SpiderEngineTest.class);

	private SpiderEngine engine;
	private State state;
	private File testOutputFileOne = new File("output/testing/ArrestsOrgOutput.xls");
	private File testOutputFileTwo = new File("output/testing/DSMRegComOutput.xls");
	private File testOutputFileOneFiltered = new File("output/testing/ArrestsOrgOutput_Alcohol-related.xls");
	private File testOutputFileTwoFiltered = new File("output/testing/DSMRegComOutput_Alcohol-related.xls");
	private File mockOutputFileOne;
	private File mockOutputFileTwo;
	private File mockOutputFileOneFiltered;
	private File mockOutputFileTwoFiltered;
	RecordIOUtil mainIOUtil;
	RecordIOUtil secondaryIOUtil;


	@BeforeClass
	public void setUpClass() {
		logger.info("********** Starting Test cases for SpiderEngine *****************");
		System.setProperty("TestingSpider", "true");
		engine = new SpiderEngine();
		state = State.getState("IA");
		mainIOUtil = new RecordIOUtil(state, new ArrestRecord(), state.getEngines().get(0).getSite(), true);
		secondaryIOUtil = new RecordIOUtil(state, new ArrestRecord(), new DesMoinesRegisterComSite(new String[]{state.getName()}), true);
	}

	@BeforeMethod
	public void setUpMethod() {
		//create output books
		Assert.assertTrue(testOutputFileOne.exists());
		Assert.assertTrue(testOutputFileTwo.exists());
		Assert.assertTrue(testOutputFileOneFiltered.exists());
		Assert.assertTrue(testOutputFileTwoFiltered.exists());
		//rename these to RecordIOUtil expected names
		mockOutputFileOne = new File(mainIOUtil.getMainDocPath());
		mockOutputFileTwo = new File(secondaryIOUtil.getMainDocPath());
		mockOutputFileOneFiltered = new File(mainIOUtil.getOutputter().getFilteredDocPath(RecordFilterEnum.ALCOHOL));
		mockOutputFileTwoFiltered = new File(secondaryIOUtil.getOutputter().getFilteredDocPath(RecordFilterEnum.ALCOHOL));
		testOutputFileOne.renameTo(mockOutputFileOne);
		testOutputFileTwo.renameTo(mockOutputFileTwo);
		testOutputFileOneFiltered.renameTo(mockOutputFileOneFiltered);
		testOutputFileTwoFiltered.renameTo(mockOutputFileTwoFiltered);

	}

	@AfterMethod
	public void tearDownMethod() {
		//delete output books

		//rename testOutputBooks back
		mockOutputFileOne.renameTo(testOutputFileOne);
		mockOutputFileTwo.renameTo(testOutputFileTwo);
		mockOutputFileOneFiltered.renameTo(testOutputFileOneFiltered);
		mockOutputFileTwoFiltered.renameTo(testOutputFileTwoFiltered);
		Assert.assertTrue(testOutputFileOne.exists());
		Assert.assertTrue(testOutputFileTwo.exists());
		Assert.assertTrue(testOutputFileOneFiltered.exists());
		Assert.assertTrue(testOutputFileTwoFiltered.exists());
	}
	@AfterClass
	public void tearDownClass() {
		System.setProperty("TestingSpider", "false");
		logger.info("********** Finishing Test cases for SpiderEngine *****************");
	}

	@Test
	public void customizeArrestOutputs_MultipleEngines() {
		//all possible outputs created
		engine.customizeArrestOutputs(mainIOUtil, state, RecordFilterEnum.ALCOHOL);

		//verify outputs
		throw new RuntimeException("Test not implemented");
	}

	@Test
	public void customizeArrestOutputs_NoFilter() {
		//only all records merge book created
		//no lexis nexis book
		engine.customizeArrestOutputs(mainIOUtil, state, RecordFilterEnum.NONE);

		//verify outputs
		throw new RuntimeException("Test not implemented");
	}

	@Test
	public void customizeArrestOutputs_OneEngineLexisNexisEligible() {
		//no merging
		//lexis nexis book created
		state = State.OK;
		state.setEngines(Arrays.asList(new ArrestsDotOrgEngine(state.getName())));
		mainIOUtil = new RecordIOUtil(state, new ArrestRecord(), new ArrestsDotOrgSite(new String[]{state.getName()}), true);
		engine.customizeArrestOutputs(mainIOUtil, state, RecordFilterEnum.NONE);

		//verify outputs
		throw new RuntimeException("Test not implemented");
	}
	

	@Test
	public void customizeArrestOutputs_OneEngineLexisNexisEligibleNoneFound() {
		//no merging
		//lexis nexis book not created because no eligible records were found
		state = State.OK;
		state.setEngines(Arrays.asList(new ArrestsDotOrgEngine(state.getName())));
		mainIOUtil = new RecordIOUtil(state, new ArrestRecord(), new ArrestsDotOrgSite(new String[]{state.getName()}), true);
		engine.customizeArrestOutputs(mainIOUtil, state, RecordFilterEnum.NONE);

		//verify outputs
		throw new RuntimeException("Test not implemented");
	}
	
	

	@Test
	public void filterOutLexisNexisEligibleRecords() {
		ArrestRecord record1 = new ArrestRecord();
		record1.setArrestDate(Calendar.getInstance());
		record1.setFirstName("EligibleJohn");
		record1.setMiddleName("Q");
		record1.setLastName("Public");
		record1.setDob(new Date());
		ArrestRecord record2 = new ArrestRecord();
		record2.setArrestDate(Calendar.getInstance());
		record2.setLastName("Nelson");
		record2.setDob(new Date());
		ArrestRecord record3 = new ArrestRecord();
		record3.setArrestDate(Calendar.getInstance());
		record3.setFirstName("EligibleJoe");
		record3.setLastName("Gunny");
		record3.setDob(new Date());
		ArrestRecord record4 = new ArrestRecord();
		record4.setFirstName("Jack");
		record4.setLastName("Sprout");
		record4.setDob(new Date());

		Set<Record> recordSet1 = new HashSet<>();
		recordSet1.add(record1);
		recordSet1.add(record2);
		Set<Record> recordSet2 = new HashSet<>();
		recordSet2.add(record4);
		recordSet2.add(record3);

		List<Set<Record>> recordSetList = new ArrayList<>();
		recordSetList.add(recordSet2);
		recordSetList.add(recordSet1);

		List<Set<Record>> eligibleRecords = engine.filterOutLexisNexisEligibleRecords(recordSetList);
		Assert.assertEquals(eligibleRecords.size(), recordSetList.size());
		Assert.assertEquals(eligibleRecords.get(0).size(), 1);
		Assert.assertEquals(eligibleRecords.get(1).size(), 1);

	}

	@Test
	public void getArrestRecordsByState() {
		throw new RuntimeException("Test not implemented");
	}
}
