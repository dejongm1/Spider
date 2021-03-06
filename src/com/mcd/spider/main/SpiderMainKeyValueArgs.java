package com.mcd.spider.main;

import com.mcd.spider.engine.SpiderEngine;
import com.mcd.spider.entities.audit.AuditParameters;
import com.mcd.spider.entities.record.State;
import com.mcd.spider.entities.record.filter.RecordFilter.RecordFilterEnum;
import com.mcd.spider.exception.ExcelOutputException;
import com.mcd.spider.exception.SpiderException;
import com.mcd.spider.exception.StateNotReadyException;
import com.mcd.spider.util.MainInputUtil;
import com.mcd.spider.util.SpiderConstants;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Michael De Jong
 *
 */

public class SpiderMainKeyValueArgs {

    private static final Logger logger = Logger.getLogger(SpiderMain.class);
    private static MainInputUtil mainInputUtil;
    private static SpiderEngine engine;

    private static String prompt;

    private SpiderMainKeyValueArgs(){}

    public static void  main(String[] args) throws IOException {
        logger.info("*******************************************************************************************");
        logger.info("*********************************                        **********************************");
        logger.info("*********************************     Spider started     **********************************");
        logger.info("*********************************                        **********************************");
        logger.info("*******************************************************************************************");
        mainInputUtil = new MainInputUtil();
        engine = new SpiderEngine();

        //create map of args and skip prompt on success. on failure show prompt
        Map<String,String> argsMap = new HashMap<>();
        try {
            for (String arg : args) {
                String[] argPair = arg.split("=");
                argsMap.put(argPair[0], argPair[1]);
            }
        } catch (Exception e) {
            prompt = "One of your arguments (below) wasn't formatted properly. Please follow the prompts to continue. \n"
                        + args.toString() + "\n"
                        + prompt;
        }

        if (prompt==null) {
            prompt = SpiderConstants.PROMPT;
        }
        String scrapeTypeChoice = "";

        if (args.length==0) {
            scrapeTypeChoice = (String) mainInputUtil.getInput(prompt, 3, "");
        } else if (args.length>=1 && args.length==argsMap.size()) {
            scrapeTypeChoice = args[0];
        }

        //reset prompt
        prompt = SpiderConstants.PROMPT;
        try {
            if (scrapeTypeChoice.toLowerCase().contains("frequen")
                    || scrapeTypeChoice.toLowerCase().contains("words")
                    || scrapeTypeChoice.equals("1")) {
                getPopularWords(args);
            } else if (scrapeTypeChoice.toLowerCase().contains("text")
                    || scrapeTypeChoice.toLowerCase().contains("scrap")
                    || scrapeTypeChoice.equals("2")) {
                getTextBySelector(args);
            } else if (scrapeTypeChoice.toLowerCase().contains("search")
                    || scrapeTypeChoice.toLowerCase().contains("term")
                    || scrapeTypeChoice.equals("3")) {
                getSearchTerms(args);
            } else if (scrapeTypeChoice.toLowerCase().contains("arrest")
                    || scrapeTypeChoice.toLowerCase().contains("record")
                    || scrapeTypeChoice.equals("4")) {
                getArrestRecords(args);
//            } else if (scrapeTypeChoice.toLowerCase().contains("connect")
//                    || scrapeTypeChoice.toLowerCase().contains("test")
//                    || scrapeTypeChoice.equals("5")) {
//                testConnectionGetter(args);
            } else if (scrapeTypeChoice.toLowerCase().contains("seo")
                    || scrapeTypeChoice.toLowerCase().contains("audit")
                    || scrapeTypeChoice.equals("5")) {
                if (args.length!=0) {
                    args[0] = ""; //remove first scrapeTypechoice and continue
                }
                getSEOAudit(String.join(" ", args));
            } else if (scrapeTypeChoice.toLowerCase().contains("backdoor")
                    || scrapeTypeChoice.toLowerCase().contains("debug")
                    || scrapeTypeChoice.equals("99")) {
                backdoor(args);
            } else if (mainInputUtil.quitting(scrapeTypeChoice)) {
                System.exit(0);
            } else if (scrapeTypeChoice.toLowerCase().contains("help")) {
                help(scrapeTypeChoice);
            } else {
                prompt = SpiderConstants.UNKNOWN_COMMAND + SpiderConstants.PROMPT;
                main(new String[] {});
            }
        } catch (IOException ioe) {
            System.err.println("Dunno what you did but I don't like it. I quit.");
            System.exit(0);
        } catch (ExcelOutputException ebe) {
            prompt = "Error with excel output: " + ebe.getMethodName() + ". Please ensure it's not open and try again. \n" + prompt;
            main(new String[] {});
        } catch (NullPointerException npe) {
            prompt = "I didn't understand this parameter, please try again. Type \"quit\" if you changed your mind. \n" + prompt;
            main(new String[] {});
        } catch (StateNotReadyException snre) {
            logger.error(snre.getState().getName() + " has not been set up \n" );
            prompt = snre.getState().getName() + " is not ready for scraping yet. Please try another\n" + prompt;
            main(new String[] {"4"});
        } catch (Exception e) {
            logger.error("Exception caught but not handled", e);
        }
    }

    private static void getPopularWords(String[] args) throws IOException {
        String url = args.length>=2?mainInputUtil.convertToUrl(args[1]):(String) mainInputUtil.getInput("URL: ", 3, SpiderConstants.URL_VALIDATION);
        int numberOfWords = args.length>=3?mainInputUtil.convertToNumber(args[2]):(int) mainInputUtil.getInput("Number of words: ", 3, SpiderConstants.NUMBER_VALIDATION);

        String confirmationPrompt = "You chose \n" +
                "URL: " + url + "\n" +
                "Number of words: " + numberOfWords + "\n" +
                "   Is that correct?";
        if ((boolean) mainInputUtil.getInput(confirmationPrompt, 2, SpiderConstants.BOOLEAN_VALIDATION)) {
            engine.getPopularWords(url, numberOfWords);
        } else {
            main(new String[] {args[0]});
        }
    }

    private static void getTextBySelector(String[] args) throws IOException {
        String url = args.length>=2?mainInputUtil.convertToUrl(args[1]):(String) mainInputUtil.getInput("URL: ", 3, SpiderConstants.URL_VALIDATION);
        String selector = args.length>=3?args[2]:(String) mainInputUtil.getInput("Selector(s): ", 1, SpiderConstants.NO_VALIDATION);

        String confirmationPrompt = "You chose \n" +
                "URL: " + url + "\n" +
                "Selector(s): " + selector + "\n" +
                "   Is that correct?";
        if ((boolean) mainInputUtil.getInput(confirmationPrompt, 2, SpiderConstants.BOOLEAN_VALIDATION)) {
            engine.getTextBySelector(url, selector);
        } else {
            main(new String[] {args[0]});
        }
    }

    private static void getSearchTerms(String[] args) throws IOException {
        String url = args.length>=2?mainInputUtil.convertToUrl(args[1]):(String) mainInputUtil.getInput("URL: ", 3, SpiderConstants.URL_VALIDATION);
        String words = args.length>=3?args[2]:(String) mainInputUtil.getInput("Words: ", 1, SpiderConstants.NO_VALIDATION);
        int flexibility = 0; //(int) mainInputUtil.getInput("Flexibility of search (1-3): ", 1, SpiderConstants.NUMBER_VALIDATION);

        String confirmationPrompt = "You chose \n" +
                "URL: " + url + "\n" +
                "Words: " + words + "\n" +
                "Flexibility: " + flexibility + "\n" +
                "   Is that correct?";
        if ((boolean) mainInputUtil.getInput(confirmationPrompt, 2, SpiderConstants.BOOLEAN_VALIDATION)) {
            engine.search(url, words, flexibility);
        } else {
            main(new String[] {args[0]});
        }
    }

    @SuppressWarnings("unchecked")
    private static void getArrestRecords(String[] args) throws IOException, SpiderException {
        List<State> states = args.length>=2?mainInputUtil.convertToStates(args[1]):(List<State>) mainInputUtil.getInput("State(s) or \"All\": ", 3, SpiderConstants.STATE_VALIDATION);
        RecordFilterEnum filter = args.length>=3?mainInputUtil.convertToFilter(args[2]):(RecordFilterEnum) mainInputUtil.getInput("Filter: ", 3, SpiderConstants.FILTER_VALIDATION);
        long maxNumberOfResults = args.length>=4?mainInputUtil.convertToNumber(args[3]):(int) mainInputUtil.getInput("Maximum Number of Records: ", 3, SpiderConstants.NUMBER_VALIDATION);
        boolean retrieveMissedRecords = args.length>=5?mainInputUtil.convertToBoolean(args[4]):(boolean) mainInputUtil.getInput("Start from Last Crawled: ", 3, SpiderConstants.BOOLEAN_VALIDATION);

        StringBuilder stateSB = new StringBuilder();
        for (int s = 0;s<states.size();s++) {
            stateSB.append(states.get(s).getName());
            stateSB.append(s+1<states.size()?", ":"");
        }
        String confirmationPrompt = "You chose \n" +
                "State(s): " + stateSB.toString() + "\n" +
                "Filter: " + filter.filterName() + "\n" +
                "Max Number of Records: " + maxNumberOfResults + "\n" +
                "Start from Last Crawled: " + retrieveMissedRecords + "\n" +
                "   Is that correct?";
        if ((boolean) mainInputUtil.getInput(confirmationPrompt, 2, SpiderConstants.BOOLEAN_VALIDATION)) {
            engine.getArrestRecordsByState(states, maxNumberOfResults, filter, retrieveMissedRecords);
        } else {
            main(new String[] {args[0]});
        }
    }

    @SuppressWarnings("unchecked")
    private static void backdoor(String[] args) throws IOException, SpiderException {
        List<State> states = args.length>=2?mainInputUtil.convertToStates(args[1]):(List<State>) mainInputUtil.getInput("State(s) or \"All\": ", 3, SpiderConstants.STATE_VALIDATION);
        RecordFilterEnum filter = args.length>=3?mainInputUtil.convertToFilter(args[2]):(RecordFilterEnum) mainInputUtil.getInput("Filter: ", 3, SpiderConstants.FILTER_VALIDATION);
        long maxNumberOfResults = args.length>=4?mainInputUtil.convertToNumber(args[3]):(int) mainInputUtil.getInput("Maximum Number of Records: ", 3, SpiderConstants.NUMBER_VALIDATION);
        boolean retrieveMissedRecords = args.length>=5?mainInputUtil.convertToBoolean(args[4]):(boolean) mainInputUtil.getInput("Start from Last Crawled: ", 3, SpiderConstants.BOOLEAN_VALIDATION);

        StringBuilder stateSB = new StringBuilder();
        for (int s = 0;s<states.size();s++) {
            stateSB.append(states.get(s).getName());
            stateSB.append(s+1<states.size()?", ":"");
        }
        String confirmationPrompt = "You chose \n" +
                "State(s): " + stateSB.toString() + "\n" +
                "Filter: " + filter.filterName() + "\n" +
                "Max Number of Records: " + maxNumberOfResults + "\n" +
                "Start from Last Crawled: " + retrieveMissedRecords + "\n" +
                "   Is that correct?";
        if ((boolean) mainInputUtil.getInput(confirmationPrompt, 2, SpiderConstants.BOOLEAN_VALIDATION)) {
            engine.getArrestRecordsThroughTheBackDoor(states, maxNumberOfResults, filter, retrieveMissedRecords);
        } else {
            main(new String[] {args[0]});
        }
    }

    private static void getSEOAudit(String argString) throws IOException {
        //levels deep
        //output type or location??
        AuditParameters parameters = new AuditParameters(argString);
        engine.performSEOAudit(parameters);
    }

    private static void help(String argString) throws IOException {
        String[] args = argString.split(" ");
        String helpType = args.length>=2?args[1]:"";
        //display help and receive input
        if (helpType.toLowerCase().contains("frequen")
                || helpType.toLowerCase().contains("words")
                || helpType.equals("1")) {
            prompt = SpiderConstants.HELP_MESSAGE_1 + prompt;
            main(new String[] {});
        } else if (helpType.toLowerCase().contains("text")
                || helpType.toLowerCase().contains("scrap")
                || helpType.equals("2")) {
            prompt = SpiderConstants.HELP_MESSAGE_2 + prompt;
            main(new String[] {});
        } else if (helpType.toLowerCase().contains("search")
                || helpType.toLowerCase().contains("term")
                || helpType.equals("3")) {
            prompt = SpiderConstants.HELP_MESSAGE_3 + prompt;
            main(new String[] {});
        } else if (helpType.toLowerCase().contains("arrest")
                || helpType.toLowerCase().contains("record")
                || helpType.equals("4")) {
            prompt = SpiderConstants.HELP_MESSAGE_4 + prompt;
            main(new String[] {});
        } else if (helpType.toLowerCase().contains("seo")
                || helpType.toLowerCase().contains("audit")
                || helpType.equals("5")) {
            prompt = SpiderConstants.HELP_MESSAGE_5 + prompt;
            main(new String[] {});
        } else {
            prompt = SpiderConstants.HELP_MESSAGE_ALL + prompt;
            main(new String[] {});
        }
    }
}
