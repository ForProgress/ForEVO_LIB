package fp.forevo.manager;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.sikuli.basics.Settings;

import autoitx4java.AutoItX;

import com.jacob.com.LibraryLoader;

import fp.forevo.proxy.Browser;
import fp.forevo.proxy.Button;
import fp.forevo.proxy.ComboBox;
import fp.forevo.proxy.Element;
import fp.forevo.proxy.Image;
import fp.forevo.proxy.TextBox;
import fp.forevo.proxy.Window;

public class MasterScript {
	
	public static final int TIMEOUT = 20;		// How log the script has to wait for application under test
	public static AutoItX autoIt = null;		// Main object to work using AutoIt libraries
	public static WebDriver browser = null;		// Main object to work using Selenium WebDriver
	public static String tag = null;			// Tag for searching images using Sikuli libraries
	protected static boolean debugMode = false; // Running mode. Hightlight object before action on it
	protected static DataManager data = null;		// Data Manager object
	public static Logger log = null;		// Logger for test scripts
	protected static Conf conf = new Conf();			// Configuration object
	public static String baseUrl = null;
	
	public MasterScript() {
		conf = new Conf();		
		initalizeAutoIt();	
		initalizeBrowserDrivers();
		autoIt = new AutoItX();
		Settings.ActionLogs = false;	// Sikuli logging level
		data = new DataManager();
		log = new Logger();
		baseUrl = "";
		
	}	
	
	protected String getProjectPath(Class<?> mapClass) {
		String path = mapClass.getProtectionDomain().getCodeSource().getLocation().getPath();
		path = path.replace("/bin/", "");
		path = path.substring(1);
		return path;
	}
	
	public static void runApp(String app) {
		autoIt.run(app);
	}
	
	public static void setDebugMode(boolean debugMode) {
		MasterScript.debugMode = debugMode;
	}
	
	public static boolean isDebugMode() {
		return MasterScript.debugMode;
	}
	
	public void initalizeAutoIt() {
		String jacobDllVersionToUse = System.getProperty("sun.arch.data.model").contains("32") ? "jacob-1.18-M2-x86.dll" : "jacob-1.18-M2-x64.dll";
		File dllFile = new File(conf.getLibPath(), jacobDllVersionToUse);
		System.setProperty(LibraryLoader.JACOB_DLL_PATH, dllFile.getAbsolutePath());
	}
	
	public void initalizeBrowserDrivers(){
		// initialize CHROME driver
		File file = new File(conf.getLibPath()+"/webdriver","chromedriver.exe");		
	    System.setProperty("webdriver.chrome.driver", file.getAbsolutePath());
	    
	    // initialize Internet Explorer driver
	    file = new File(conf.getLibPath()+"/webdriver","IEDriverServer.exe");
	    System.setProperty("webdriver.ie.driver", file.getAbsolutePath());
	    
	    // initialize Opera driver
	    file = new File(conf.getLibPath()+"/webdriver","operadriver.exe");
	    System.setProperty("webdriver.opera.driver", file.getAbsolutePath());
	}
	
	public String runBrowser(Browser browserName) {
		try{
			if (browserName.equals(Browser.Firefox)) {
				browser = new FirefoxDriver();
			} else if (browserName.equals(Browser.Chrome)) {
				browser = new ChromeDriver();
			} else if (browserName.equals(Browser.InternetExplorer)) {
				browser = new InternetExplorerDriver();
			} else if (browserName.equals(Browser.Opera)) {
				browser = new OperaDriver();
			} else if (browserName.equals(Browser.Safari)) {
				browser = new SafariDriver();
			}		
			
			browser.manage().timeouts().implicitlyWait(TIMEOUT, TimeUnit.SECONDS);
			return browser.getWindowHandle();
		}catch(Exception e){
			return e.getMessage();
		}
	}
	
	public void runBrowser(Browser browserName, String url) {
		runBrowser(browserName);
		browser.get(url);
	}
	
	public static void sleep(double sec) {
		try {
			Thread.sleep((long) (sec * 1000));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	
	
	
	public static String getTag() {
		return MasterScript.tag;
	}
	
	public static void setTag(String tag) {
		MasterScript.tag = tag;
	}
	
	public static String decrypt(String encryptedString) {
		return encryptedString;
	}
	
	/**
	 * Pobranie rekordu danych z bazy frameworka. Jeœli dany proces nie mia³ przypisanych danych, to pobieramy dane na podstawie klucza.
	 * Jeœli ma ju¿ dane to pobieramy dane poprzez id_test_data.
	 * @param tag - etykieta danych
	 * @return
	 */
	public DataSet initTestData(Tag tag) {
		DataSet newData = null;
		
		// Jeœli dany proces nie mia³ przypisanych danych, to pobieramy dane na podstawie klucza.
		// Jeœli ma ju¿ dane to pobieramy dane poprzez id	
		if (TestSettings.getIdTestData() < 0) {
			System.out.println("Identyfikator rekordu danych nie przekazany - pobranie rekordu z kluczem " + tag.toString());
			newData = data.dbGetTestData(tag, true);
			if (newData != null) {		
				// Przekazujemy informacjê o id_test_data dla pozosta³ych skryptów				
				TestSettings.setIdTestData(newData.getIdTestData());
			}
		} else {
			System.out.println("Pobieramy dane dla identyfikatora " + TestSettings.getIdTestData());
			newData = data.dbGetTestData(TestSettings.getIdTestData());
			if (newData != null)
				// Jeœli porzedni skrypt nie wykona³ siê poprawnie to klucz bêdzie inny ni¿ tego oczekujemy, wtedy musimy pobraæ inne dane.
				if (!newData.getTag().equals(tag.toString())) {
					System.out.println("Porzucenie danych o identyfikatorze " + newData.getIdTestData() + ". Dane nie s¹ gotowe do wykorzystania na tym etapie. Przyczyn¹ mo¿e byæ niepoprawne wykonanie poprzedniego etapu.");
					newData = data.dbGetTestData(tag, true);
					if (newData != null) {
						TestSettings.setIdTestData(newData.getIdTestData());
					} else {
						TestSettings.setIdTestData(-1);
					}
				}
		}
		if (newData == null)
			System.out.println("Brak danych testowych");
		else
			System.out.println("id_test_data: " + newData.getIdTestData() + ", klucz: " + newData.getTag().toString() + ", dane: " + newData.getData());
		
		return newData;
	}
	
	public Window getWindow(TestObjectManager tom, String windowName) {
		Window window = new Window(this, tom, windowName);
		if (window.getXWindow() == null) {
			log.error(windowName ,": object does not exist in xml map file!");
		}
		return window;
	}
	
	public Button getButton(TestObjectManager tom, Window window, String testObjectName) {
		Button button = new Button(this, tom, window, testObjectName);
		if (!button.isNotNull()) {
			log.error(testObjectName , ": object does not exist in xml map file!");
		}
		return button;
	}
	
	public Element getElement(TestObjectManager tom, Window window, String testObjectName){
		Element element = new Element(this, tom, window, testObjectName);
		if (!element.isNotNull()) {
			log.error(testObjectName , ": object does not exist in xml map file!");
		}
		return element;
	}
	
	public TextBox getTextBox(TestObjectManager tom, Window window, String testObjectName) {
		TextBox textBox = new TextBox(this, tom, window, testObjectName);
		if (!textBox.isNotNull()) {
			log.error(testObjectName , ": object does not exist in xml map file!");
		}
		return textBox;
	}
	
	public Image getImage(TestObjectManager tom, Window window, String testObjectName) {
		Image image = new Image(this, tom, window, testObjectName);
		if (!image.isNotNull()) {
			log.error(testObjectName , ": object does not exist in xml map file!");
		}
		return image;
	}
	
	public ComboBox getComboBox(TestObjectManager tom, Window window, String testObjectName){
		ComboBox combobox = new ComboBox(this, tom, window, testObjectName);
		if(!combobox.isNotNull()){
			log.error(testObjectName, ": object does not exist in xml map file");
		}
		return combobox;
	}
	
	
	
}
