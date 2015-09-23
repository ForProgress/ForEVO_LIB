package fp.forevo.manager;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

public class Logger {
	private static Connection tafConnection = null;
	
	//private static Conf conf = new Conf();
	

	/**
	 * Metoda s³u¿¹ca do po³¹czeñ z baz¹ danych
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	private boolean connect() throws ClassNotFoundException, SQLException  {
		
			Class.forName("com.mysql.jdbc.Driver");
			
			if (tafConnection == null || tafConnection.isClosed()) {
				System.out.println("DB connection. Creating new DB connection");
				
				tafConnection = DriverManager.getConnection(Conf.getDbPath(),
						Conf.getDbUser(), Conf.getDbPassword());
				
				if(!tafConnection.isValid(10)){
					Conf.setDbLog(false);
				return false;
				}else{
					return true;
				}
				
				
			}
			return true;
		
		
		
	}	
	private int getIterationNumber() {
		if (Conf.isDbLog())
			try {
				
					connect();
				Statement statement = tafConnection.createStatement();
				ResultSet resultSet = statement
						.executeQuery("Select max(iteration) from runs");

				if (resultSet.next()) {
					return resultSet.getInt(1);
				}
			
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return 1;
	}
	private int getNewIterationNumber() {
		if (Conf.isDbLog())
			try {
				
					connect();
				Statement statement = tafConnection.createStatement();
				ResultSet resultSet = statement
						.executeQuery("Select max(iteration)+1 from runs");

				if (resultSet.next()) {
					return resultSet.getInt(1);
				}
			
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return 1;
	}
	
	private int getIDBusinessProcess(String processName) {
		if (Conf.isDbLog())
			try {
				
					connect();
				Statement statement = tafConnection.createStatement();
				ResultSet resultSet = statement
						.executeQuery("select id_process from process where name = '"
								+ processName + "'");

				if (resultSet.next()) {
					return resultSet.getInt(1);
				}
			
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return 0;
	}
	
	/**
	 * Sprawdza czy w bazie danych istnieje wskazany proces biznesowy i tworzy
	 * nowy jesli takiego nie ma. W ustawieniach zapisywane jest id procesu do
	 * ktorego beda raportowane wyniki
	 * 
	 * @param processName
	 */
	public void createBusinessProcess(String processName) {
		TestSettings.setBusinessProcess(processName);
		
		if(TestSettings.getIteration()==0)
		TestSettings.setIteration(getIterationNumber());
		
		
		
		if (Conf.isDbLog())
			try {
				
					if(!connect()){
						Conf.setDbLog(false);
						warning("DB connection", "problem z polaczeniem z baza danych. Skrypt zostanie przelaczony w tryb DBlog=false");
						return;
					}
						
						
						
				int idBusinessProcess = getIDBusinessProcess(processName);
				if (idBusinessProcess == 0) {

					PreparedStatement pStat = tafConnection.prepareStatement(
							"insert into process (name) VALUES (?)",
							Statement.RETURN_GENERATED_KEYS);
					pStat.setString(1, processName);
					pStat.execute();

					ResultSet rs = pStat.getGeneratedKeys();
					if (rs.next()) {
						idBusinessProcess = rs.getInt(1);
					}
					pStat.close();
					// idBusinessProcess=getIDBusinessProcess(processName);

				}
				TestSettings.setIdBusinessProcess(idBusinessProcess);
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	
	
	private void reportTestStart(int idProcess, int id_thread) {
		
		if (Conf.isDbLog())
			try {
				
					connect();
				int last_inserted_id = -1;

				PreparedStatement pStat = tafConnection
						.prepareStatement(
								"insert into runs (`id_process`,`id_status`,`time_start`,`id_thread`,`user_name`,`iteration`) VALUES (?, ?, now(), ?, ?, ?)",
								Statement.RETURN_GENERATED_KEYS);
				pStat.setInt(1, idProcess);
				pStat.setInt(2, Status.WARNING.getId());
				pStat.setInt(3, id_thread);
				pStat.setString(4, System.getProperty("user.name"));
				pStat.setInt(5, TestSettings.getIteration());
				pStat.execute();

				ResultSet rs = pStat.getGeneratedKeys();
				if (rs.next()) {
					last_inserted_id = rs.getInt(1);
				}

				pStat.close();
				TestSettings.setIdrun(last_inserted_id);
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}

	private void reportTestEnd(Status status) {
		if (Conf.isDbLog())
			try {
				
					connect();
				PreparedStatement pStat = tafConnection
						.prepareStatement("UPDATE runs set `id_status`=?,`time_end`=now() where `id_run` =?");

				pStat.setInt(1, status.getId());
				pStat.setInt(2, TestSettings.getIdrun());
				pStat.execute();
				pStat.close();
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}
	
	private void reportStep(int idProcess, int idRun, Status status,
			String title, String details, int id_img) {
		if (Conf.isDbLog())
			try {
				
				
					connect();
				PreparedStatement pStat = tafConnection
						.prepareStatement("insert into steps (id_process, id_run, id_status, title, details, time, idimg) VALUES (?, ?, ?, ?, ?, now(),?)");
				pStat.setInt(1, idProcess);
				pStat.setInt(2, idRun);
				pStat.setInt(3, status.getId());
				pStat.setString(4, title);
				pStat.setString(5, details);

				if (id_img == -1) {
					pStat.setNull(6, Types.INTEGER);
				} else {
					pStat.setInt(6, id_img);
				}

				pStat.execute();

				pStat.close();
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}
	

	/**Zapisuje do bazy danych informacje. Tabela taf.steps
	 * 
	 * @param message
	 */
	public void info(String message) {
		StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
		StackTraceElement e = stacktrace[2];
		String methodName = e.getMethodName();
		info(methodName, message);
	}	
	
	/**Zapisuje w logu przekazany komunikat bledu wraz ze zrzutem ekranu
	 * 
	 * @param action - nazwa akcji
	 * @param message - tresc wiadomosc
	 * @param image - zrzut ekranu
	 */
	public void error(String action, String message, BufferedImage image) {
		String imageName = "img_Error_" + TestSettings.getIdBusinessProcess()
				+ "_" + TestSettings.getIdrun() + "_"
				+ System.currentTimeMillis();
		int idImage = uploadImage(image, imageName);
		logLocally(action, message, Status.FAILED);
		saveImageLocally(image, imageName);
		reportStep(TestSettings.getIdBusinessProcess(),
				TestSettings.getIdrun(), Status.FAILED, action, message,
				idImage);
		TestSettings.setTest_status(Status.FAILED);
	}

	private void logLocally(String action, String message, Status status) {
		if (status == Status.FAILED) {
			System.err.println("*ERROR:" + System.currentTimeMillis() + "* "
					+ action + " " + message);
		} else if (status == Status.WARNING) {
			System.out.println("*WARN:" + System.currentTimeMillis() + "* "
					+ action + " " + message);
		} else
			System.out.println("*INFO:" + System.currentTimeMillis() + "* "
					+ action + " " + message);
		writeToFile(" " + status + " " + action + " " + message);
	}
	
	/**Zapisuje do bazy danych informacje. Tabela taf.steps
	 * 
	 * @param action
	 * @param message
	 */
	public void error(String action, String message) {
		reportStep(TestSettings.getIdBusinessProcess(),
				TestSettings.getIdrun(), Status.FAILED, action, message, -1);

		logLocally(action, message, Status.FAILED);
		// System.err.println(action +" "+ message);
		// writeToFile(" ERROR "+action +" "+ message);
		TestSettings.setTest_status(Status.FAILED);
	}
	
	public void error(String message) {
		StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
		StackTraceElement e = stacktrace[2];
		String methodName = e.getMethodName();
		error(methodName, message);
	}
	/**Zapisuje do bazy danych informacje. Tabela taf.steps
	 * 
	 * @param action
	 * @param message
	 */
	public void warning(String action, String message, BufferedImage image) {
		String imageName = "img_Warning_" + TestSettings.getIdBusinessProcess()
				+ "_" + TestSettings.getIdrun() + "_"
				+ System.currentTimeMillis();
		logLocally(action, message, Status.WARNING);
		saveImageLocally(image, imageName);
		int idImage = uploadImage(image, imageName);
		reportStep(TestSettings.getIdBusinessProcess(),
				TestSettings.getIdrun(), Status.WARNING, action, message,
				idImage);

	}
	
	public void warning(String action, String message) {
		reportStep(TestSettings.getIdBusinessProcess(),
				TestSettings.getIdrun(), Status.WARNING, action, message, -1);
		logLocally(action, message, Status.WARNING);
	}
	public void warning(String message) {
		StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
		StackTraceElement e = stacktrace[2];
		String methodName = e.getMethodName();
		warning(methodName, message);
	}
	
	
	/**Zapisuje do bazy danych informacje. Tabela taf.steps
	 * 
	 * @param action
	 * @param message
	 * @param status
	 */
	public void info(String action, String message) {
		reportStep(TestSettings.getIdBusinessProcess(),
				TestSettings.getIdrun(), Status.INFO, action, message, -1);
		logLocally(action, message, Status.INFO);
	}

	
	public void info(String action, String message, BufferedImage image) {
		String imageName = "img_INFO_" + TestSettings.getIdBusinessProcess()
				+ "_" + TestSettings.getIdrun() + "_"
				+ System.currentTimeMillis();
		int idImage = uploadImage(image, imageName);
		logLocally(action, message, Status.INFO);
		saveImageLocally(image, imageName);
		reportStep(TestSettings.getIdBusinessProcess(),
				TestSettings.getIdrun(), Status.INFO, action, message, idImage);
	}


	private String currentDate() {
		SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date(System.currentTimeMillis());
		return formater.format(date);
	}

	public void saveImageLocally(BufferedImage image, String imageName) {
		if(Conf.isSaveScreenShots()){
		new File(TestSettings.getScreenShotsPath()).mkdirs();
		File outputfile = new File(TestSettings.getScreenShotsPath() + "/"
				+ imageName + ".jpg");
		try {
			ImageIO.write(image, "jpg", outputfile);
			writeToFile(" INFO image saved to: " +outputfile.getAbsolutePath());		
			
			if(TestSettings.isResultsTemporarily()){
				System.out.println("*HTML*<img src=\""
						+ TestSettings.getScreenShotsPath().replace("/", "\\")+"\\"+outputfile.getName() + "\">");
			}else{
				System.out.println("*HTML*<img src=\""
						+ TestSettings.getScreenShotsDir().replace("/", "\\")+"\\"+outputfile.getName() + "\">");
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}}
	}
	
	public int uploadImage(BufferedImage image, String imageName) {
		java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();

		int idImage = 0;
		if (Conf.isDbLog())
			try {
				
					connect();
				ImageIO.write(image, "png", baos);
				connect();

				PreparedStatement pStat = tafConnection.prepareStatement(
						"insert into img_store (img_name, img) VALUES (?, ?)",
						Statement.RETURN_GENERATED_KEYS);
				pStat.setString(1, imageName);
				pStat.setBlob(2,
						new java.io.ByteArrayInputStream(baos.toByteArray()));
				pStat.execute();

				ResultSet rs = pStat.getGeneratedKeys();
				if (rs.next()) {
					idImage = rs.getInt(1);
				}
				pStat.close();

			 }catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		return idImage;
	}
	
	public void startProcess(String processName, String reportPath, boolean startNewIteration){
		TestSettings.setProcessStarted(true);
		if(startNewIteration)
			TestSettings.setIteration(getNewIterationNumber());
		startProcess(processName, reportPath);
	}
	
	public void startProcess(String processName, String reportPath){
		TestSettings.setProcessStarted(true);
		TestSettings.setReportDir(reportPath);
		TestSettings.setResultsTemporarily(false);
		startProcess(processName);	
	}
	
	
	/**Zapisuje do bazy danych informacje o starcie testu oraz ustawia nazwe procesu biznesowego
	 * dla ktorego beda raportowane kolejne wyniki.
	 * 
	 * @param processName
	 */
	public void startProcess(String processName) {
		TestSettings.setProcessStarted(true);
		createBusinessProcess(processName);
		reportTestStart(TestSettings.getIdBusinessProcess(), 1);
		TestSettings.setTest_status(Status.PASSED);
		info("Start testu", processName);
	}

	public void startProcess() {
		TestSettings.setProcessStarted(true);
		String processName = "Brak procesu";
		createBusinessProcess(processName);
		reportTestStart(TestSettings.getIdBusinessProcess(), 1);
		if (!Conf.isDbLog()) {
			info("Logger", "DBlog=false, wyniki beda zapisywane lokalnie");
		}
		TestSettings.setTest_status(Status.PASSED);
		info("Start testu", processName);
	}
	/**Zapisuje do bazy danych informacje o zakonczeniu testu z okreslonym statusem.
	 * 
	 * @param processName
	 * @param status
	 */
	public void endProcess() {
		info("End testu", TestSettings.getBusinessProcess());
		reportTestEnd(TestSettings.getTest_status());
		TestSettings.setProcessStarted(false);
		TestSettings.clearTMPSettings();
	}
	
	private void writeToFile(String message) {
		if(Conf.isTafLog())
		try {

			DateFormat dateFormat = new SimpleDateFormat(
					"[yyyy/MM/dd HH:mm:ss]");
			Date date = new Date();
			String dateString = dateFormat.format(date);

			String dirPath = TestSettings.getReportPath();
			String path = dirPath + "//log_"
					+ TestSettings.getIdBusinessProcess() + "_" + currentDate()
					+ ".log";

			File dir = new File(dirPath);
			if (!dir.exists()) {
				dir.mkdir();
			}

			File file = new File(path);

			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fileWritter = new FileWriter(file.getAbsolutePath(),
					true);
			BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
			bufferWritter.write(dateString + " " + message);
			bufferWritter.newLine();
			bufferWritter.close();
		} catch (IOException e) {
			System.err.println("Zapis do pliku nieudany");
			e.printStackTrace();
		}
	}
}
