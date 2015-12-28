package fp.forevo.manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RobotTestRunner {
	
	private String projectPath = null;
	private List<String> libs = new ArrayList<String>();
	
	@SuppressWarnings("rawtypes")
	public RobotTestRunner(Class executor) {
		 projectPath = executor.getProtectionDomain().getCodeSource().getLocation().getPath();
		 projectPath = projectPath.substring(1, projectPath.indexOf("/bin/"));
		 libs.add(System.getenv("FOREVO") + "/drivers/robotframework-2.8.5.jar");
		 libs.add(System.getenv("FOREVO") + "/lib/ForEVO_LIB.jar");
		 libs.add(System.getenv("FOREVO") + "/lib/ForEvo_XML.jar");
		 libs.add(System.getenv("FOREVO") + "/drivers/sikulixapi.jar");
		 libs.add(System.getenv("FOREVO") + "/drivers/jacob.jar");
		 libs.add(System.getenv("FOREVO") + "/drivers/AutoItX4Java.jar");
		 libs.add(System.getenv("FOREVO") + "/drivers/selenium-java-2.45.0.jar");
		 libs.add(System.getenv("FOREVO") + "/drivers/mysql-connector-java-5.1.35-bin.jar");
		 libs.add(System.getenv("FOREVO") + "/drivers/selenium-server-standalone-2.46.0.jar");
	}
	
	public void runSuite(String suiteName) {
		String keywordsPath = projectPath + "/bin";
		String projectName = projectPath.split("/")[projectPath.split("/").length - 1];
		String suitePath = projectPath + "/robot/" + projectName + "/" + suiteName;
		
		String command = "java -cp \"" + keywordsPath;
		for (String lib : libs)
			command += ";" + lib; 
		command += "\" org.robotframework.RobotFramework " + suitePath;
		
		final Process process;
		try {
			System.out.println(command);
			//System.out.println("Start execution of " + suiteName + " suite.");
			File workingDir = new File(projectPath + "/results/" + suiteName + " " + getSuffix());
			workingDir.mkdirs();
			process = Runtime.getRuntime().exec(command, null, workingDir);
			
			new Thread(new Runnable() {
			    public void run() {
			     BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
			     String line = null; 

			     try {
			        while ((line = input.readLine()) != null)
			            System.out.println(line);
			     } catch (IOException e) {
			            e.printStackTrace();
			     }
			    }
			}).start();
			
			process.waitFor();
			
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private String getSuffix() {
		SimpleDateFormat ft = new SimpleDateFormat ("YY.MM.dd HH.mm.ss");
	    return ft.format(new Date());
	}

}
