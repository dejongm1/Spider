package com.mcd.spider.main.util;

import com.mcd.spider.main.entities.record.ArrestRecord;
import com.mcd.spider.main.entities.record.Record;
import com.mcd.spider.main.entities.record.State;
import com.mcd.spider.main.entities.site.Site;
import com.mcd.spider.main.exception.ExcelOutputException;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

public class ExcelWriter {

	public static final Logger logger = Logger.getLogger(ExcelWriter.class);

	private String docName;
	private WritableWorkbook workbook;
	private State state;
	private Record record;
	private static final String OUTPUT_DIR = "output/";
    private static final String BACKUP_DIR = "backup/";
	private File oldBook;
	private File newBook;
	private Workbook currentWorkbook;
	private WritableWorkbook copyWorkbook;
	private WritableWorkbook backupWorkbook;
	private Calendar workbookCreateDate;

	public ExcelWriter(State state, Record record, Site site) {
	    Calendar date = Calendar.getInstance();
		this.workbookCreateDate = date;
		this.docName = state.getName() 
		+ "_" + (date.get(Calendar.MONTH)+1)
		+ "-" + date.get(Calendar.DAY_OF_MONTH)
		+ "-" + date.get(Calendar.YEAR) + "_"
		+ record.getClass().getSimpleName() + "_"
        + site.getName() + ".xls";
		this.state = state;
		this.record = record;
	}

	public String getDocName() {
		return docName;
	}
	public WritableWorkbook getWorkbook() {
		return workbook;
	}
	public void setWorkbook(WritableWorkbook workbook) {
		this.workbook = workbook;
	}
	public State getState() {
		return state;
	}
	public File getOldBook() {
		return oldBook;
	}
	public void setOldBook(File oldBook) {
		this.oldBook = oldBook;
	}
	public File getNewBook() {
		return newBook;
	}
	public void setNewBook(File newBook) {
		this.newBook = newBook;
	}
    public Workbook getCurrentWorkbook() {
        return currentWorkbook;
    }
    public WritableWorkbook getBackupWorkbook() {
        return backupWorkbook;
    }
	public Calendar getWorkbookCreateDate() {return this.workbookCreateDate; }
	public void setCurrentWorkbook(Workbook currentWorkbook) {
		this.currentWorkbook = currentWorkbook;
	}
	public WritableWorkbook getCopyWorkbook() {
		return copyWorkbook;
	}
	public void setCopyWorkbook(WritableWorkbook copyWorkbook) {
		this.copyWorkbook = copyWorkbook;
	}

	public Set<String> getPreviousIds() throws ExcelOutputException {
	    Set<String> ids = new HashSet<>();
	    try {
	        //check name as well to make sure it's the right state/site
            Workbook workbook = Workbook.getWorkbook(findMostRecentWorkbook());

            if (workbook!=null) {
                Sheet worksheet = workbook.getSheet(0);
                Cell[] column = worksheet.getColumn(ArrestRecord.RecordColumnEnum.ID_COLUMN.index());
                for (Cell cell : column) {
                    ids.add(cell.getContents());
                }
                ids.remove(ArrestRecord.RecordColumnEnum.ID_COLUMN.title());
            }
        } catch (IOException | BiffException e) {
            throw new ExcelOutputException(this, "getPreviousIds");
        }
        return ids;
    }

	public void createSpreadhseet() {
		WritableWorkbook newWorkbook = null;
		try {
			//currently overwrites previous workbook - need something different?
			newWorkbook = Workbook.createWorkbook(new File(OUTPUT_DIR + docName));

			WritableSheet excelSheet = newWorkbook.createSheet(state.getName(), 0);

			//create columns based on Record.getFieldsToOutput()
			int columnNumber = 0;
			for (Field recordField : record.getFieldsToOutput()) {
				//********extract to createLabelMethod????
				Label columnLabel = new Label(columnNumber, 0, recordField.getName().toUpperCase());
				excelSheet.addCell(columnLabel);
				columnNumber++;
			}
			newWorkbook.write();
            workbook = newWorkbook;//this only works if I create one spreadsheet per ExcelWriter
		} catch (IOException | WriteException e) {
			logger.error(e.getMessage());
		} finally {
        	if (newWorkbook != null) {
        		try {
        			newWorkbook.close();
        		} catch (IOException e) {
        			logger.error(e.getMessage());
        		} catch (WriteException e) {
        			logger.error(e.getMessage());
        		}
        	}
        }
	}

	public void saveRecordsToWorkbook(List<Record> records) {
	    //TODO this is adding to excel, not overwriting
		try {
			int rowNumber = 0;
			for (Record currentRecord : records) {
				currentRecord.addToExcelSheet(workbook, rowNumber);
				rowNumber++;
			}
		} catch (IllegalAccessException e) {
			logger.error("Error trying to save data to workbook", e);
		}
	}

    public void addRecordToWorkbook(Record record) {
        try {
            createWorkbookCopy();

            int rowNumber = copyWorkbook.getSheet(0).getRows();

            record.addToExcelSheet(copyWorkbook, rowNumber);

            replaceOldBookWithNew();
        } catch (IOException | WriteException | IllegalAccessException | BiffException  e) {
            logger.error("Error trying to save record to workbook", e);
        }
    }

	public void findPossibleDuplicates() {
		//use name
	}

	public boolean removeColumnsFromSpreadsheet(int[] args) {
		boolean successful = false;
		try {
			createWorkbookCopy();
			
			WritableSheet sheet = copyWorkbook.getSheet(0);
			
			for (int c=0;c<args.length;c++) {
				sheet.removeColumn(args[c]);
			}

			replaceOldBookWithNew();
		} catch (IOException | WriteException | BiffException e) {
			logger.error("Error trying to remove ID column from workbook", e);
		}
		return successful;
	}

    public void backupWorkbook() throws ExcelOutputException {
	    try {
            oldBook = findMostRecentWorkbook();
            //newBook = new File(OUTPUT_DIR + docName + "_" + workbookCreateDate.get(Calendar.HOUR_OF_DAY) + ":" + workbookCreateDate.get(Calendar.MINUTE) + ".xls");
            newBook = new File(OUTPUT_DIR + BACKUP_DIR + docName.substring(0, docName.length()-4) + "_backup.xls");
            currentWorkbook = Workbook.getWorkbook(oldBook);
            backupWorkbook = Workbook.createWorkbook(newBook, currentWorkbook);
            backupWorkbook.write();
            backupWorkbook.close();
        } catch (IOException | BiffException | WriteException e) {
            throw new ExcelOutputException(this, "backupWorkbook");
        }
    }

	private void createWorkbookCopy() throws BiffException, IOException {
		oldBook = new File(OUTPUT_DIR + docName);
		newBook = new File(OUTPUT_DIR + "temp_copy.xls");
		currentWorkbook = Workbook.getWorkbook(oldBook);
		copyWorkbook = Workbook.createWorkbook(newBook, currentWorkbook);
	}
	
	private void replaceOldBookWithNew() throws IOException, WriteException {
		copyWorkbook.write();
		copyWorkbook.close();
		currentWorkbook.close();

		if (oldBook.delete()) {
			newBook.renameTo(new File(OUTPUT_DIR + docName));
		} else {
			//making sure we don't lose data or override good data
			newBook.renameTo(new File(OUTPUT_DIR + docName + System.currentTimeMillis()));
		}
	}

	private File findMostRecentWorkbook() {
	    //TODO need to ignore directories
        File[] files = new File(OUTPUT_DIR).listFiles();
        if (files.length == 0) {
            return null;
        }
        Arrays.sort(files, new Comparator<File>() {
            public int compare(File o1, File o2) {
                return new Long(o2.lastModified()).compareTo(o1.lastModified());
            }});
        return files[0];
    }
}
