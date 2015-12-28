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
	
	private final int INFO = 1;
	private final int PASSED = 2;
	private final int WARNING = 3;
	private final int FAILED = 4;	
	
	private final String IMG_FORMAT = "png"; // possible values: gif, png, jpg
	
	/**
	 * For database logging only!
	 * Create new log (new id_run from run table) in database. 
	 * This method should be executed for every new test process.
	 * @param processName
	 */
	public void startTest(String testName) {
		// Tworzymy nowy run_id i od tego momentu logujemy pod ten id
		
		if (Conf.isDbLog()) {
			int idTest = getTestId(testName);
			
			if (idTest == -1)
				idTest = createNewTest(testName);
				
			int idRun = getNewRunId(idTest);
			
			Conf.setTestName(testName);
			Conf.setRunId(idRun);
			Conf.setTestId(idTest);
			Conf.setTestStatus(PASSED);
			
			info("Started test " + testName);
			info("Run id: " + idRun);
		}		
	}
	
	/**
	 * Report information in report with PASSED status
	 * @param message - message to report
	 */
	public void pass(String message) {
		report(PASSED, message, null);
	}
	
	/**
	 * Report information and image in report with PASSED status
	 * @param message - message to report
	 * @param image - image to report
	 */
	public void pass(String message, BufferedImage image) {
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
	public void fail(String message) {
		report(FAILED, message, null);
	}
	
	/**
	 * Report information and image in report with FAILED status
	 * @param message - message to report
	 * @param image - image to report
	 */
	public void fail(String message, BufferedImage image) {
		report(FAILED, message, image);
	}
	
	/**
	 * Report message into log
	 * @param status - (PASSED, FAILED, WARNING, INFO)
	 * @param message - message to log 
	 * @param image - test object parent window image, created by testObject.capture() method 
	 */
	private void report(int status, String message, BufferedImage image) {
		// update test status
		Conf.setTestStatus(status);
		
		// get method name
		StackTraceElement[] thread = Thread.currentThread().getStackTrace();
		String methodName = thread[3].getMethodName();
		
		// report message
		if (Conf.isDbLog()) 
			logDatabaseMessage(methodName, status, message, image);
		
		if (Conf.isRobotLog()) 
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
				absoluteImagePath = System.getProperty("user.dir") + "/results/temp/img/" + System.currentTimeMillis() + "." + IMG_FORMAT;
			} else {			
				new File(System.getProperty("user.dir") + "/img/").mkdirs();
				absoluteImagePath = System.getProperty("user.dir") + "/img/" + System.currentTimeMillis() + "." + IMG_FORMAT;
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
			System.out.println("|PASS|" + currentDate() + "|" + methodName + " " + message);
			break;
		case FAILED:
			System.err.println("|FAIL|" + currentDate()  + "|" + methodName + " " + message);
			break;
		case WARNING:
			System.out.println("|WARN|" + currentDate()  + "|" + methodName + " " + message);
			break;
		default:
			System.out.println("|INFO|" + currentDate()  + "|" + methodName + " " + message);
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
					.prepareStatement("insert into steps (id_test, id_run, id_status, title, details, time, id_img) VALUES (?, ?, ?, ?, ?, now(), ?)");
			pStat.setInt(1, Conf.getTestId());
			pStat.setInt(2, Conf.getRunId());
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
			dbConnection = DriverManager.getConnection(Conf.getDbPath(), Conf.getDbUser(), Conf.getDbPassword());
			if (!dbConnection.isValid(10)) {
				Conf.setDbLog(false);
				return false;
			} else
				return true;
		}
		return true;
	}

	/**
	 * Method returns test identyficator
	 * @param testName
	 * @return
	 */
	private int getTestId(String testName) {
		try {
			connect();
			Statement statement = dbConnection.createStatement();
			ResultSet resultSet = statement.executeQuery("select id_test from tests where name = '" + testName + "'");

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
	 * @param testName - the process name
	 * @return process identyficator
	 */
	private int createNewTest(String testName) {
		int newTestId = -1;
		try {
			connect();
			PreparedStatement pStat = dbConnection.prepareStatement("insert into tests (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
			pStat.setString(1, testName);
			pStat.execute();

			ResultSet rs = pStat.getGeneratedKeys();
			if (rs.next()) {
				newTestId = rs.getInt(1);
			}
			pStat.close();
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return newTestId;
	}
	
	private int getNewRunId(int idTest) {
		int newRunId = -1;
		try {
			connect();			
			
			PreparedStatement pStat = dbConnection.prepareStatement("insert into runs (id_test, id_status, time_start, time_end, user_name) VALUES (?, ?, now(), now(), ?)",
					Statement.RETURN_GENERATED_KEYS);
			pStat.setInt(1, idTest);
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
			pStat.setInt(1, Conf.getTestStatus());
			pStat.setInt(2, Conf.getRunId());
			pStat.execute();
			pStat.close();
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		} 
	}

	private String currentDate() {
		SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date date = new Date(System.currentTimeMillis());
		return formater.format(date);
	}	
}
