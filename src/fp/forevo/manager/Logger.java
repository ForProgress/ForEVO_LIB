package fp.forevo.manager;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

/**
 * Support for advanced logging
 */
public class Logger {
	
	private static Connection dbConnection = null;
	private Conf conf = null;
	
	private final int PASSED = 1;
	private final int FAILED = 2;
	private final int WARNING = 3;
	private final int INFO = 4;
	
	private final String IMG_FORMAT = "png"; // possible values: gif, png, jpg
	
	private int currentStatus = PASSED;
	private int idProcess = -1;
	private int idRun = -1;
	
	public Logger(Conf conf) {
		this.conf = conf;
	}
	
	
	/**
	 * For database logging only!
	 * Create new log (new id_run from run table) in database. 
	 * This method should be executed for every new test process.
	 * @param processName
	 */
	public void startTest(String testName) {
		// Tworzymy nowy run_id i od tego momentu logujemy pod ten id
		
		if (conf.isDbLog()) {
			idProcess = getProcessId(testName);
			
			if (idProcess == -1)
				idProcess = createNewProcess(testName);
				
			idRun = getNewRunId(idProcess);
		}		
	}
	
	/**
	 * Report information in report with PASSED status
	 * @param message - message to report
	 */
	public void passed(String message) {
		report(PASSED, message, null);
	}
	
	/**
	 * Report information and image in report with PASSED status
	 * @param message - message to report
	 * @param image - image to report
	 */
	public void passed(String message, BufferedImage image) {
		report(PASSED, message, image);
	}
	
	/**
	 * Report information in report with INFO status
	 * @param message - message to report
	 */
	public void info(String message) {
		report(INFO, message, null);
	}
	
	/**
	 * Report information and image in report with INFO status
	 * @param message - message to report
	 * @param image - image to report
	 */
	public void info(String message, BufferedImage image) {
		report(INFO, message, image);
	}
	
	/**
	 * Report information in report with WARNING status
	 * @param message - message to report
	 */
	public void warning(String message) {
		report(WARNING, message, null);
	}
	
	/**
	 * Report information and image in report with WARNING status
	 * @param message - message to report
	 * @param image - image to report
	 */
	public void warning(String message, BufferedImage image) {
		report(WARNING, message, image);
	}
	
	/**
	 * Report information in report with FAILED status
	 * @param message - message to report
	 */
	public void error(String message) {
		report(FAILED, message, null);
	}
	
	/**
	 * Report information and image in report with FAILED status
	 * @param message - message to report
	 * @param image - image to report
	 */
	public void error(String message, BufferedImage image) {
		report(FAILED, message, image);
	}
	
	/**
	 * Report message into log
	 * @param status - (PASSED, FAILED, WARNING, INFO)
	 * @param message - message to log 
	 * @param image - test object parent window image, created by testObject.capture() method 
	 */
	private void report(int status, String message, BufferedImage image) {
		StackTraceElement[] thread = Thread.currentThread().getStackTrace();
		String methodName = thread[thread.length - 3].getMethodName();
		
		if (conf.isDbLog()) 
			logDatabaseMessage(methodName, status, message, image);
		
		if (conf.isRobotLog()) 
			logLocalMessage(methodName, status, message, image);
	}	
	
	/**
	 * Report message into local log
	 * @param - (PASSED, FAILED, WARNING, INFO)
	 * @param message - message to log 
	 * @param image - test object parent window image, created by testObject.capture() method 
	 */
	private void logLocalMessage(String methodName, int status, String message, BufferedImage image) {
		// Save image into file
		if (image != null) {
			String absoluteImagePath = null;
			
			// kiedy wywolujemy bezposrednio z eclipsa
			if (!System.getProperty("user.dir").contains("results")) {
				new File(System.getProperty("user.dir") + "/results/temp/img/").mkdirs();
				absoluteImagePath = System.getProperty("user.dir") + "/results/temp/img/" + System.currentTimeMillis() + ".jpg";
			} else {			
				absoluteImagePath = System.getProperty("user.dir") + "/img/" + System.currentTimeMillis() + ".jpg";
			}
			
			try {
				File outputFile = new File(absoluteImagePath);
				ImageIO.write(image, IMG_FORMAT, outputFile);
				System.out.println("*HTML*<img src=\"" + absoluteImagePath + "\">");
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}		

		switch (status) {
		case PASSED:
			System.out.println("*PASSED:" + System.currentTimeMillis() + "* " + methodName + " " + message);
			break;
		case FAILED:
			System.err.println("*FAILED:" + System.currentTimeMillis() + "* " + methodName + " " + message);
			break;
		case WARNING:
			System.out.println("*WARNING:" + System.currentTimeMillis() + "* " + methodName + " " + message);
			break;
		default:
			System.out.println("*INFO:" + System.currentTimeMillis() + "* " + methodName + " " + message);
			break;
		}
	}
	
	/**
	 * Report message into database log
	 * @param - (PASSED, FAILED, WARNING, INFO)
	 * @param message - message to log 
	 * @param image - test object parent window image, created by testObject.capture() method 
	 */
	private void logDatabaseMessage(String methodName, int status, String message, BufferedImage image) {
		int idImg = -1;
		try {
			connect();
			
			if (image != null)
				idImg = insertImageIntoDatabase(image);
			
			PreparedStatement pStat = dbConnection
					.prepareStatement("insert into steps (id_process, id_run, id_status, title, details, time, id_img) VALUES (?, ?, ?, ?, ?, now(), ?)");
			pStat.setInt(1, idProcess);
			pStat.setInt(2, idRun);
			pStat.setInt(3, status);
			pStat.setString(4, methodName);
			pStat.setString(5, message);

			if (idImg == -1) {
				pStat.setNull(6, Types.INTEGER);
			} else {
				pStat.setInt(6, idImg);
			}

			pStat.execute();
			pStat.close();
			
			updateTestEnd();
			
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		} 
	}
	
	/**
	 * Method insert image capture into database
	 * @param image	- image object [BufferedImage]
	 * @param imageName - image name
	 * @return imageId - image id
	 */
	public int insertImageIntoDatabase(BufferedImage image) {
		int idImage = -1;
		try {
			java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
			ImageIO.write(image, "png", baos);

			PreparedStatement pStat = dbConnection.prepareStatement("insert into images (img) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
			pStat.setBlob(1, new java.io.ByteArrayInputStream(baos.toByteArray()));
			pStat.execute();

			ResultSet rs = pStat.getGeneratedKeys();
			if (rs.next()) {
				idImage = rs.getInt(1);
			}
			pStat.close();
		} catch (SQLException | IOException e) {
			e.printStackTrace();
		} 
		
		return idImage;
	}
	

	/**
	 * Method for connection with test result database
	 * 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	private boolean connect() throws ClassNotFoundException, SQLException {

		Class.forName("com.mysql.jdbc.Driver");

		if (dbConnection == null || dbConnection.isClosed()) {
			System.out.println("DB connection. Creating new DB connection");

			dbConnection = DriverManager.getConnection(conf.getDbPath(), conf.getDbUser(), conf.getDbPassword());

			if (!dbConnection.isValid(10)) {
				conf.setDbLog(false);
				return false;
			} else
				return true;
		}
		return true;
	}

	/**
	 * Method returns process identyficator
	 * @param processName
	 * @return
	 */
	private int getProcessId(String processName) {
		try {
			connect();
			Statement statement = dbConnection.createStatement();
			ResultSet resultSet = statement.executeQuery("select id_process from process where name = '" + processName + "'");

			if (resultSet.next()) {
				return resultSet.getInt(1);
			}
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		} 
		
		return -1;
	}

	/**
	 * Method create new process in the process table 
	 * @param processName - the process name
	 * @return process identyficator
	 */
	private int createNewProcess(String processName) {
		int newProcessId = -1;
		try {
			connect();
			PreparedStatement pStat = dbConnection.prepareStatement("insert into process (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
			pStat.setString(1, processName);
			pStat.execute();

			ResultSet rs = pStat.getGeneratedKeys();
			if (rs.next()) {
				newProcessId = rs.getInt(1);
			}
			pStat.close();
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return newProcessId;
	}
	
	private int getNewRunId(int idProcess) {
		int newRunId = -1;
		try {
			connect();			
			
			PreparedStatement pStat = dbConnection.prepareStatement("insert into runs (`id_process`,`id_status`, `time_start`, `time_end`, `user_name`) VALUES (?, ?, now(), now(), ?)",
					Statement.RETURN_GENERATED_KEYS);
			pStat.setInt(1, idProcess);
			pStat.setInt(2, PASSED);
			pStat.setString(3, System.getProperty("user.name"));
			pStat.execute();

			ResultSet rs = pStat.getGeneratedKeys();
			if (rs.next()) {
				newRunId = rs.getInt(1);
			}
			
			pStat.close();
			
			
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		} 
		return newRunId;
	}

	/**
	 * In the case of test interruption, test end is reported in every step.
	 */
	private void updateTestEnd() {
		try {
			connect();
			PreparedStatement pStat = dbConnection.prepareStatement("UPDATE runs set `id_status`=?,`time_end`=now() where `id_run` =?");
			pStat.setInt(1, currentStatus);
			pStat.setInt(2, idRun);
			pStat.execute();
			pStat.close();
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		} 
	}

	private String currentDate() {
		SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date(System.currentTimeMillis());
		return formater.format(date);
	}	
}
