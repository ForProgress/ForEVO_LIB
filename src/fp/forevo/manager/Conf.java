package fp.forevo.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Conf {

	// Database configuration
	private static boolean dbLog = false;
	private static boolean robotLog = true;
	private static String dbPath = null;
	private static String dbUser = null;
	private static String dbPassword = null;

	public Conf() {			
		try {
			Properties prop = new Properties();
			FileInputStream fis;
			fis = new FileInputStream(new File(getConfFile()));
			prop.load(fis);
			fis.close();
			
			// Database configuration
			dbLog = Boolean.parseBoolean(prop.getProperty("dbLog"));
			robotLog = Boolean.parseBoolean(prop.getProperty("robotLog"));
			dbPath = prop.getProperty("dbPath");
			dbUser = prop.getProperty("dbUser");
			dbPassword = prop.getProperty("dbPassword");

		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	public static String getTestName() {
		return System.getProperty("FOREVO_TEST_NAME");
	}
	
	public static void setTestName(String processName) {
		System.setProperty("FOREVO_TEST_NAME", processName);
	}
	
	public static int getTestId() {
		String idProcess = System.getProperty("FOREVO_TEST_ID");
		if (idProcess != null)
			return Integer.parseInt(idProcess);
		else
			return -1;
	}
	
	public static void setTestId(int id) {
		System.setProperty("FOREVO_TEST_ID", "" + id);
	}
	
	public static int getIdTestData() {
		String idTestDataStr = System.getProperty("FOREVO_DATA_ID");
		if (idTestDataStr != null)
			return Integer.parseInt(idTestDataStr);
		else
			return -1;
	}
	
	public static void setIdTestData(int id) {
		System.setProperty("FOREVO_DATA_ID", "" + id);
	}
	
	public static int getRunId() {
		String idTestRun = System.getProperty("FOREVO_RUN_ID");
		if (idTestRun != null)
			return Integer.parseInt(idTestRun);
		else
			return -1;
	}
	
	public static void setRunId(int id) {
		System.setProperty("FOREVO_RUN_ID", "" + id);
	}
	
	public static int getTestStatus() {
		String idTestStatus = System.getProperty("FOREVO_TEST_STATUS_ID");
		if (idTestStatus != null)
			return Integer.parseInt(idTestStatus);
		else
			return 2; // PASSED
	}
	
	public static void setTestStatus(int status) {
		int currTestStatus = getTestStatus();
		if (currTestStatus < status)
			System.setProperty("FOREVO_TEST_STATUS_ID", "" + status);
	}

	public static boolean isDbLog() {
		return dbLog;
	}

	public static void setDbLog(boolean enabled) {
		dbLog = enabled;
	}
	
	public static boolean isRobotLog() {
		return robotLog;
	}
	
	public static void setRobotLog(boolean enabled) {
		robotLog = enabled;
	}
	
	public static String getDbPath() {
		return dbPath;
	}

	public static String getDbUser() {
		return dbUser;
	}

	public static String getDbPassword() {
		return dbPassword;
	}

	public static String getLibPath() {
		return System.getenv("FOREVO") + "\\drivers";
	}

	public static String getConfFile() {
		return System.getenv("FOREVO") + "\\conf.properties";
	}
}
