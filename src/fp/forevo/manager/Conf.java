package fp.forevo.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Conf {

	// Database configuration
	private boolean dbLog = false;
	private boolean robotLog = true;
	private String dbPath = null;
	private String dbUser = null;
	private String dbPassword = null;
	private Properties prop;	
	private String processName = "brak";
	private int idTestData = -1;

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
	}
	
	public String getProcessName() {
		return processName;
	}
	
	public void setProcessName(String processName) {
		this.processName = processName;
	}

	public boolean isDbLog() {
		return dbLog;
	}

	public void setDbLog(boolean enabled) {
		dbLog = enabled;
	}
	
	public boolean isRobotLog() {
		return robotLog;
	}
	
	public void setRobotLog(boolean enabled) {
		robotLog = enabled;
	}
	
	public int getIdTestData() {
		return idTestData;
	}
	
	public void setIdTestData(int id) {
		this.idTestData = id;
	}
	
/*
	public String getClassPath() {
		return Conf.class.getProtectionDomain().getCodeSource().getLocation().getPath();
	}
*/
	public String getDbPath() {
		return dbPath;
	}

	public String getDbUser() {
		return dbUser;
	}

	public String getDbPassword() {
		return dbPassword;
	}

	public String getLibPath() {
		return System.getenv("FP_TAF_PATH") + "\\drivers";
	}

	public String getConfFile() {
		return System.getenv("FP_TAF_PATH") + "\\conf.properties";
	}
}
