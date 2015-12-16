package fp.forevo.manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.robotframework.RobotFramework;

public class RobotTestRunner {
	
	private String projectPath = null;
	private String robotJarPath = System.getenv("FP_TAF_PATH") + "/drivers/robotframework-2.8.5.jar";
	
	@SuppressWarnings("rawtypes")
	public RobotTestRunner(Class executor) {
		 projectPath = executor.getProtectionDomain().getCodeSource().getLocation().getPath();
		 projectPath = projectPath.substring(1, projectPath.indexOf("/bin/"));
	}
	
	public void runSuite(String suiteName) {
		String keywordsPath = projectPath + "/bin";
		String projectName = projectPath.split("/")[projectPath.split("/").length - 1];
		String suitePath = projectPath + "/robot/" + projectName + "/" + suiteName;
		String discName = projectPath.substring(0, 2);
		
		String command = "java -cp \"" + keywordsPath + ";" + robotJarPath + "\" org.robotframework.RobotFramework " + suitePath;
		
		final Process process;
		try {
			//System.out.println(command);
			System.out.println("Start execution of " + suiteName + " suite.");
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
