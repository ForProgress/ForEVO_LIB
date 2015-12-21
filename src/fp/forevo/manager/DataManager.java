package fp.forevo.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

public class DataManager {
	
	public static final String ROBOT_LIBRARY_SCOPE = "GLOBAL";
	private Connection connection = null;
	private int currentRow = 1; // wiersz w aktualnym arkuszu z którego aktualnie korzystamy
	private String currentSheet = null;
	private HSSFWorkbook workbook = null;
	private Conf conf = null;
	
	public DataManager(Conf conf) {
		this.conf = conf;
	}
	
	
	//----------------------------------------- DATABASE ------------------------------------------
		
	/**
	 * Metoda s³u¿¹ca do po³¹czeñ z Oraclowa baz¹ danych
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	private void connect() throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.jdbc.Driver");
		if (connection == null || connection.isClosed()) {
			connection = DriverManager.getConnection(conf.getDbPath(), conf.getDbUser(), conf.getDbPassword());
		}
	}	
		
	/** 
	 * Wybór rekordu z bazy automatu, w którym dane spe³niaj¹ kryteria podane w parametrze data (SELECT * FROM test_data WHERE data like '%data%')
	 * @param data
	 * @return obiekt typu Data lub null jeœli nie znaleziono rekordu spe³niaj¹cego kryteria
	 */
	public DataSet dbGetTestData(String data) {
		
		try {
			connect();	
			DataSet kd = null;
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery("select * from test_data where data like '%" + data + "%'");
			while (resultSet.next()) {
				kd = new DataSet(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3));
			}		
			resultSet.close();
			statement.close();
			return kd;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	/**
	 * Funkcja dodaje dane do tabeli. Tworzony jest nowy wiersz danych
	 * @param tag - etykieta na podstawie którego te dane maja byæ wyszukiwane
	 * @param data - dane
	 * @param processName - nazwa procesu który wygenerowa³ te dane
	 * @return
	 */	
	public void dbAddTestData(Tag tag, String data) {
		try {	
			connect();	
		
			PreparedStatement pStat = connection.prepareStatement("insert into test_data (tags, data, locked, creation_date, user_name, process_name) VALUES (?, ?, 1, now(), ?, ?)");
			pStat.setString(1, tag.toString());
			pStat.setString(2, data);
			pStat.setString(3, System.getProperty("user.name"));
			pStat.setString(4, conf.getProcessName());
			pStat.execute();
			pStat.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Funcja zwraca dane dla podanego idTestData. Zazwyczaj takie bêd¹ zablokowane do wyszukania bo kluczu (Key)
	 * @param idTestData - identyfikator id_test_data
	 * @return
	 */	
	public DataSet dbGetTestData(int idTestData) {
		try {
			connect();	
			DataSet kd = null;
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery("select * from test_data where id_test_data = " + idTestData);
			while (resultSet.next()) {
				kd = new DataSet(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3));
			}		
			resultSet.close();
			statement.close();
			return kd;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Funkcja zwraca pierwszy wolny (status > 0) zestaw danych dla podanego klucza.
	 * @param tag - klucz na podstawie którego szukamy danych
	 * @param lock - czy zablokowaæ dane (nikt inny nie bêdzie móg³ pobraæ tego zestawu danych)
	 * @return
	 */	
	public DataSet dbGetTestData(Tag tag, boolean lock) {
		try {
			connect();	
			DataSet kd = null;
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery("select * from test_data where tags = \"" + tag + "\" limit 1");
			if (resultSet.next()) {
				int idTestData = resultSet.getInt(1);
				kd = new DataSet(idTestData, resultSet.getString(2), resultSet.getString(3));
				PreparedStatement prepStmt = connection.prepareStatement("update test_data set locked = ?, process_name = ? where id_test_data = " + idTestData);
				prepStmt.setInt(1, lock == true ? 1 : 0);
				prepStmt.setString(2, conf.getProcessName());
				prepStmt.execute();
				prepStmt.close();
			}		
			resultSet.close();
			statement.close();
			return kd;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	/*
	/**
	 * Funkcja zwraca pierwszy wolny (status > 0) zestaw danych których klucz pasuje do jednego z przekazanych kluczy.
	 * np. new TestData().dbGetTestData(new Key[]{Key.TEST, Key.KLIENT_POTENCJALNY, Key.KLIENT_UMOWA}, true);
	 * @param tags - klucze na podstawie których szukamy danych
	 * @param lock - czy zablokowaæ dane (nikt inny nie bêdzie móg³ pobraæ tego zestawu danych)
	 * @return
	 *
	public Data dbGetTestData(Tag [] tags, boolean lock) {
		String processName = "";
		String strKeys = "";
		
		//(key = 'TEST' or key = 'KLIENT_POTENCJALNY')
		for (Tag tag : tags) {
			strKeys += "key = '" + tag + "' or ";
		}
		
		// Jeœli skonfigurowana jest nazwa procesu to j¹ wrzucamy do bazy
		if (System.getProperty("process.name") != null)
			processName = System.getProperty("process.name");
		
		try {
			connect();	
			Data kd = null;
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery("select * from test_data where id_test_data = (select min(id_test_data) from test_data where (" + strKeys.substring(0, strKeys.length() - 4) + ") and locked < 1 and test_env = '" + conf.getTestEnv() + "')");
			while (resultSet.next()) {
				int idTestData = resultSet.getInt(1);
				kd = new Data(idTestData, resultSet.getString(2), resultSet.getString(3));
				PreparedStatement prepStmt = connection.prepareStatement("update test_data set locked = ?, process_name = ? where id_test_data = " + idTestData);
				prepStmt.setInt(1, lock == true ? 1 : 0);
				prepStmt.setString(2, processName);
				prepStmt.execute();
				prepStmt.close();
			}		
			resultSet.close();
			statement.close();
			return kd;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	*/
	
	/**
	 * Aktualizacja danych. Funkcja wykorzystywana np. po wykonaniu etapu w procesie.
	 * @param Data - obiekt zawieraj¹cy dane (id_test_data, key, data)
	 * @param lock - czy blokowaæ dane (true jeœli dane maj¹ byæ nadal zablokowane do wyszukiwania po kluczu)
	 */
	public void dbUpdateTestData(DataSet Data, boolean lock) {
		try {
			connect();	
			PreparedStatement pStat = connection.prepareStatement("update test_data set tags = ?, data = ?, locked = ?, update_date = now(), update_user = ?, process_name = ? where id_test_data = " + Data.getIdTestData());
			pStat.setString(1, Data.getTag());
			pStat.setString(2, Data.getData());
			pStat.setInt(3, lock == true ? 1 : 0);
			pStat.setString(4, System.getProperty("user.name"));
			pStat.setString(5,  conf.getProcessName());
			pStat.executeUpdate();
			pStat.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	private void dbLockUnlockData(int idTestData, boolean lock) {
		try {
			connect();	
			PreparedStatement pStat = connection.prepareStatement("update test_data set locked = ?, update_date = now(), update_user = ? where id_test_data = ?");
			pStat.setInt(1, lock == true ? 1 : 0);
			pStat.setString(2, System.getProperty("user.name"));
			pStat.setInt(3, idTestData);			
			pStat.executeUpdate();
			pStat.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void dbLockData(int idTestData) {
		dbLockUnlockData(idTestData, true);
	}
	
	public void dbUnlockData(int idTestData) {
		dbLockUnlockData(idTestData, false);
	}
		
	//----------------------------------------- EXCEL ---------------------------------------------
	
	/**
	 * Funkcja importuje ca³y dokument Excel do lokalnego Workbooka
	 * @param filePath - œcie¿ka wzglêdna do pliku (np. samples/data/Dane1.xls)
	 */
	public void xlsImportWorkbook(String filePath) {
		POIFSFileSystem fileSystem;
		try {
			FileInputStream inputStream = new FileInputStream(filePath);
			fileSystem = new POIFSFileSystem(inputStream);
			workbook = new HSSFWorkbook(fileSystem);
			inputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Funkcja importuje arkusz z zewnêtrznego pliku do lokalnego pliku danych
	 * @param filePath - wzglêdna œcie¿na do pliku (np. /samples/data/Dane1.xls)
	 * @param sheetName - nazwa arkusza który chcemy przekopiowaæ
	 */
	public void xlsImportSheet(String filePath, String sheetName) {
		// Jeœli nie mamy workbooka to tworzymy nowy
		if (workbook == null)
			workbook = new HSSFWorkbook();
		
		// Sprawdzamy czy taki arkusz ju¿ istnieje, jeœli nie to importujemy
		if (workbook.getSheet(sheetName) == null) {		
			POIFSFileSystem fileSystem;
			try {
				FileInputStream inputStream = new FileInputStream(filePath);
				fileSystem = new POIFSFileSystem(inputStream);
				HSSFWorkbook sourceWorkbook = new HSSFWorkbook(fileSystem);
				HSSFSheet sourceSheet = sourceWorkbook.getSheet(sheetName);
				HSSFSheet targetSheet = workbook.createSheet(sheetName);
				ExcelUtil.copySheets(targetSheet, sourceSheet);
				inputStream.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Funkcja importuje arkusz z zewnêtrznego pliku do lokalnego pliku danych
	 * @param sheetMap
	 */
	public void xlsImportSheet(SheetMap sheetMap) {
		xlsImportSheet(sheetMap.getFilePath(), sheetMap.getSheetName());
	}
	
	/**
	 * Funkcja zapisuje lokalny workbook do pliku Excel
	 * @param filePath - œcie¿ka wzglêdna do pliku (np. samples/data/Dane1.xls)
	 */
	public void xlsSaveWorkbook(String filePath) {
	    FileOutputStream fileOut;
		try {
			fileOut = new FileOutputStream(filePath);
			workbook.write(fileOut);
		    fileOut.close();
		} catch ( IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Funkcja zwraca arkusz w postaci obiektu HSSFSheet. Na tym obiekcie mo¿emy wykonaæ niestandardowe czynnoœci.
	 * @param sheetName - nazwa arkusza
	 * @return obiekt klasy HSSFSheet
	 */
	public HSSFSheet xlsGetSheet(String sheetName) {
		return workbook.getSheet(sheetName);
	}
	
	/**
	 * Funkcja ustawia nazwê do aktualnie u¿ywanego arkusza w lokalnym workbooku
	 * @param sheetMap - nazwa arkusza
	 */
	public void xlsSetCurrentSheet(SheetMap sheetMap) {
		this.currentSheet = sheetMap.getSheetName();
	}
	
	/**
	 * Funkcja ustawia nazwê do aktualnie u¿ywanego arkusza w lokalnym workbooku
	 * @param sheetName - nazwa arkusza
	 */
	public void xlsSetCurrentSheet(String sheetName) {
		this.currentSheet = sheetName;
	}
	
	/**
	 * Funkcja zwraca nazwê aktualnie wybranego arkusza
	 * @return
	 */
	public String xlsGetCurrentSheet() {
		if (currentSheet == null)
			System.err.println("Current sheet is not defined! Set current sheet name before this step. Use method 'xlsSetCurrentSheet(\"SheetName\").");
		return currentSheet;
	}
	
	/**
	 * Funkcja zwraca liczbê wierszy w aktualnie u¿ywanym arkuszu
	 */
	public int xlsGetRowCount() {
		return workbook.getSheet(xlsGetCurrentSheet()).getPhysicalNumberOfRows();
	}
	
	/**
	 * Funkcja ustawia wiersz aktualnie u¿ywanego wiersza danych. 
	 * Minimalna wartoœæ to 1. 
	 * Wiersz 0 zarezerwowany jest do nazw kolumn.
	 */
	public void xlsSetCurrentRow(int rowId) {
		if (rowId > 0 & rowId < xlsGetRowCount())
			this.currentRow = rowId;
		else {			
			this.currentRow = 1;
			System.out.println("Incorect row id! Currently used default id 1.");
		}
	}
	
	/**
	 * Funkcja zwraca numer aktualnie u¿ywanego wiersza danych
	 * @return
	 */
	public int xlsGetCurrentRow() {
		return this.currentRow;
	}
	
	/**
	 * Funkcja s³u¿y do importu arkusza oraz ustawienia aktualnie u¿ywanego arkusza i wiersza danych
	 * @param sheetName
	 * @param rowId
	 */
	public void xlsSetCurrentSheetRow(SheetMap sheetMap, int rowId) {
		xlsImportSheet(sheetMap);
		xlsSetCurrentSheet(sheetMap);
		xlsSetCurrentRow(rowId);
	}
	
	/**
	 * Funkcja ustawia wstawia wartoœæ w kolumnie przekazanej parametrem paramName aktualnie u¿ywanego wiersza danych
	 * @param paramName - nazwa kolumny w której chcemy wstawiæ wartoœæ
	 * @param paramValue - wartoœæ któr¹ chcemy wstawiæ w pole
	 */
	public void xlsSetValue(String paramName, String paramValue) {
		HSSFRow row = workbook.getSheet(xlsGetCurrentSheet()).getRow(currentRow);
		int paramId = xlsGetColumnId(paramName);
		if (paramId > 0) {
			if (row.getCell(paramId) == null)
				row.createCell(paramId);
			row.getCell(paramId).setCellValue(paramValue);
		} 
	}
	
	/**
	 * Funkcja zwraca numer kolumny o okreœlonej nazwie
	 * @param columnName
	 * @return
	 */
	private int xlsGetColumnId(String columnName) {
		HSSFRow head = workbook.getSheet(xlsGetCurrentSheet()).getRow(0);
		for (int i = 0; i < head.getPhysicalNumberOfCells(); i++)
			if (head.getCell(i).getStringCellValue().equals(columnName))
				return i;
		System.err.println("Sheet '" + xlsGetCurrentSheet() + "' doesn't have column '" + columnName + "'.");			
		return -1;
	}
	
	/**
	 * Funkcja zwraca wartoœæ z kolumny przekazanej parametrem paramName aktualnie u¿ywanego wiersza danych
	 * @param paramName - nazwa kolumny z której chcemy wczytaæ dane
	 * @return wartoœæ z komórki
	 */
	public String xlsGetValue(String paramName){
		HSSFRow row = workbook.getSheet(xlsGetCurrentSheet()).getRow(currentRow);
		int paramId = xlsGetColumnId(paramName);
		if (paramId >= 0) 
			return xlsGetCellValue(row.getCell(paramId));
		else
			return null;
	}
	
	/**
	 * Funkcja ustawia jako aktywny pierwszy wolny niezablokowany wiersz pliku excel
	 */
	public int xlsSetAsCurrentUnlockedRow(){
		HSSFSheet head = workbook.getSheet(xlsGetCurrentSheet());
		int paramId = xlsGetColumnId("Lock");
		for(Row row : head){
			for(Cell cell : row){
				if(cell.getColumnIndex() == paramId && cell.getCellType() == Cell.CELL_TYPE_NUMERIC && cell.getNumericCellValue() == 0){
					xlsSetCurrentRow(row.getRowNum());
					return row.getRowNum();
				}
			}			
		}
		return 0;
	}
	
	/**
	 * Funkcja numer pierwszego nie wykorzystanego wiersza
	 * @return Numer wiersza lub 0 gdy brak wolnych wierszy
	 */
	public int xlsGetFirstUnlockedRow(){
		HSSFSheet head = workbook.getSheet(xlsGetCurrentSheet());
		int paramId = xlsGetColumnId("Lock");
		for(Row row : head){
			for(Cell cell : row){
				if(cell.getColumnIndex() == paramId && cell.getCellType() == Cell.CELL_TYPE_NUMERIC && cell.getNumericCellValue() == 0){
					return row.getRowNum();
				}
			}			
		}
		return 0;
	}
	
	/**
	 * Funckja sprawdza czy wiersz byl juz uzywany
	 * @param i  - numer wiersz
	 * @return true lub false	 */
	
	public boolean xlsRowIsLocked(int i){
		HSSFSheet head = workbook.getSheet(xlsGetCurrentSheet());
		int paramId = xlsGetColumnId("Lock");
		int cellValue = (int) head.getRow(i).getCell(paramId).getNumericCellValue();
		
		if(cellValue==1){
			return true;
		}else{
			return false;
		}		
	}
		
	/**
	 * Funkcja ustawia aktywny wiersz jako zablokowany
	 */
	public void xlsSetRowAsLocked(){
		xlsSetValue("Lock", "1");	
		
		HSSFRow row = workbook.getSheet(xlsGetCurrentSheet()).getRow(currentRow);
		int paramId = xlsGetColumnId("Lock");
		if (paramId > 0) {
			if (row.getCell(paramId) == null)
				row.createCell(paramId);
			row.getCell(paramId).setCellValue(1);
		}		
	}
	
	/**
	 * Funkcja zwraca wartoœæ komórki przekszta³caj¹c j¹ w String
	 * @param cell - komórka
	 * @return
	 */
	private String xlsGetCellValue(HSSFCell cell) {
		if (cell != null) {
			switch (cell.getCellType()) {
	        case HSSFCell.CELL_TYPE_STRING:
	            return cell.getStringCellValue();
	        case HSSFCell.CELL_TYPE_NUMERIC:
	        	return formatToString(cell.getNumericCellValue());
	        case HSSFCell.CELL_TYPE_BOOLEAN:
	            return "" + cell.getBooleanCellValue();
	        default:
	            return "";
			}
		} else 
			return "";
	}
	
	/**
	 * Funkcja zamienia liczbê typu double w String
	 * @param d
	 * @return
	 */
	private String formatToString(double d){
	    if(d == (long) d)
	        return String.format("%d",(long)d);
	    else
	        return String.format("%s",d);
	}
	
	/**
	 * Funkcja wypisuje ca³¹ zawartoœæa aktualnie u¿ywanego arkusza
	 */
	public void xlsPrintCurrentSheet() {
		HSSFSheet tempWorkbook = workbook.getSheet(xlsGetCurrentSheet());
		for (int i = 0; i < tempWorkbook.getPhysicalNumberOfRows(); i++) {
			System.out.print("|");
			for (int k = 0; k < tempWorkbook.getRow(0).getPhysicalNumberOfCells(); k++) {
				System.out.print(tempWorkbook.getRow(i).getCell(k).getStringCellValue() + " | ");
			}
			System.out.println();
		}
	}
	
	//----------------------------------------- XML ---------------------------------------------
	
	public Object xmlLoadFileByName(String className, String fileName) {
		try {
			return xmlLoadFileByClass(Class.forName(className), fileName);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}	
		return null;
	}
	
	public Object xmlLoadFileByClass(Class<?> className, String fileName) {
		JAXBContext jaxbContext;
		try {
			jaxbContext = JAXBContext.newInstance(className);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			return unmarshaller.unmarshal(new File(fileName));
		} catch (JAXBException e) {
			e.printStackTrace();
		}		
		return null;
	}
	
	public void xmlSaveFileByName(Object obj, String className, String fileName) {
		try {
			xmlSaveFileByClass(obj, Class.forName(className), fileName);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void xmlSaveFileByClass(Object obj, Class<?> className, String fileName) {
		JAXBContext jaxbContext;
		try {
			jaxbContext = JAXBContext.newInstance(className);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.marshal(obj, new File(fileName));	
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
	
	
	
	
}
