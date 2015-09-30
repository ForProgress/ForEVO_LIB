package fp.forevo.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Conf {
	
	// Database configuration
	private static boolean dbLog = false;
	private static String dbPath = null;
	private static String dbUser = null;
	private static String dbPassword = null;
	private static Properties prop;
	private static boolean tafLog=true;
	private static boolean consoleOutput=true;
	private static boolean saveScreenShotsLocally=true;
	
		
	public Conf() {
		try {
			prop = new Properties();
			FileInputStream fis;
			fis = new FileInputStream(new File(getConfFile()));
			prop.load(fis);
			fis.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}		
		
		// Database configuration
		dbLog = Boolean.parseBoolean(prop.getProperty("dbLog"));
		dbPath = prop.getProperty("dbPath");
		dbUser = prop.getProperty("dbUser");
		dbPassword = prop.getProperty("dbPassword");	
		tafLog=Boolean.parseBoolean(prop.getProperty("tafLog"));
		consoleOutput=Boolean.parseBoolean(prop.getProperty("consoleOutput"));
		saveScreenShotsLocally=Boolean.parseBoolean(prop.getProperty("saveScreenShotsLocally"));
	}
	

	public static boolean isDbLog() {
		if(dbLog==true&&!TestSettings.isProcessStarted()){
			dbLog=false;
			System.err.println("Wyniki nie deda zapisywane do bazy danych. Aby poprawnie raportowac wyniki do bazy "
					+ "danych nalezy na poczatku testu wywolac funkcje logStartTest()");
			return false;
		}
		
		return dbLog;
	}
	
	public static void setDbLog(boolean newdbLog) {
		dbLog = newdbLog;
	}
	
	public static String getPath() {
		return Conf.class.getProtectionDomain().getCodeSource().getLocation().getPath();
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
		return System.getenv("FP_TAF_PATH") + "\\drivers";
	}
	
	public static String getConfFile() {
		return System.getenv("FP_TAF_PATH") + "\\conf.properties";
	}

	public static boolean isTafLog() {
		return tafLog;
	}

	public static void setTafLog(boolean tafLog) {
		Conf.tafLog = tafLog;
	}

	public static boolean isConsoleOutput() {
		return consoleOutput;
	}

	public static void setConsoleOutput(boolean consoleOutput) {
		Conf.consoleOutput = consoleOutput;
	}

	public static boolean isSaveScreenShots() {
		return saveScreenShotsLocally;
	}

	public static void setSaveScreenShots(boolean saveScreenShots) {
		Conf.saveScreenShotsLocally = saveScreenShots;
	}
}
