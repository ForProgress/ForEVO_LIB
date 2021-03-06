package fp.forevo.manager;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.sikuli.basics.Settings;

import com.jacob.com.LibraryLoader;

import autoitx4java.AutoItX;
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
	protected static DataManager data = null;	// Data Manager object
	public static Logger log = null;			// Logger for test scripts
	public static String baseUrl = null;
	
	public MasterScript() {
		new Conf();							// read configuration
		initalizeAutoIt();	
		initalizeBrowserDrivers();
		autoIt = new AutoItX();
		Settings.ActionLogs = false;		// Sikuli logging level
		Settings.OcrTextRead = true;		// Mo�liwosc czytania tekstu z regionu
		Settings.OcrTextSearch = true;		// Mo�liwosc wyszukiwania polozenia tekstu na regionie
		data = new DataManager();
		log = new Logger();
		baseUrl = "";
		
	}	
	
	public void loadProjectTags(File projectPath) {
		
	}
	
	protected String getProjectPath(Class<?> repoClass) {
		String path = repoClass.getProtectionDomain().getCodeSource().getLocation().getPath();
		//System.out.println("Path: " + path);
		if(path.endsWith(".jar")) {  // get resource from JAR file
			
		} else { // get resource from IDE			
			path = path.replace("/bin/", "");
			path = path.substring(1);
		}		
		
		return path;
	}
	
	/**
	 * Run application using AutoIt.
	 * eg. run("calc.exe");
	 * @param app
	 */
	public static void runApp(String app) {
		autoIt.run(app);
	}
	
	public void initalizeAutoIt() {
		String jacobDllVersionToUse = System.getProperty("sun.arch.data.model").contains("32") ? "jacob-1.18-M2-x86.dll" : "jacob-1.18-M2-x64.dll";
		File dllFile = new File(Conf.getLibPath(), jacobDllVersionToUse);
		System.setProperty(LibraryLoader.JACOB_DLL_PATH, dllFile.getAbsolutePath());
	}
	
	public void initalizeBrowserDrivers(){
		// initialize CHROME driver
		File file = new File(Conf.getLibPath()+"/webdriver","chromedriver.exe");		
	    System.setProperty("webdriver.chrome.driver", file.getAbsolutePath());
	    
	    // initialize Internet Explorer driver
	    file = new File(Conf.getLibPath()+"/webdriver","IEDriverServer.exe");
	    System.setProperty("webdriver.ie.driver", file.getAbsolutePath());
	    
	    // initialize Opera driver
	    file = new File(Conf.getLibPath()+"/webdriver","operadriver.exe");
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
	 * Pobranie rekordu danych z bazy frameworka. Je�li dany proces nie mia� przypisanych danych, to pobieramy dane na podstawie klucza.
	 * Je�li ma ju� dane to pobieramy dane poprzez id_test_data.
	 * @param tag - etykieta danych
	 * @return
	 */
	public DataSet initTestData(Tag tag) {
		DataSet newData = null;
		// Je�li dany proces nie mia� przypisanych danych, to pobieramy dane na podstawie klucza.
		// Je�li ma ju� dane to pobieramy dane poprzez id	
		if (Conf.getIdTestData() < 0) {
			System.out.println("Identyfikator rekordu danych nie przekazany - pobranie rekordu z kluczem " + tag.toString());
			newData = data.dbGetTestData(tag, true);
			if (newData != null) {		
				// Przekazujemy informacj� o id_test_data dla pozosta�ych skrypt�w				
				Conf.setIdTestData(newData.getIdTestData());
			}
		} else {
			System.out.println("Pobieramy dane dla identyfikatora " + Conf.getIdTestData());
			newData = data.dbGetTestData(Conf.getIdTestData());
			if (newData != null)
				// Je�li porzedni skrypt nie wykona� si� poprawnie to klucz b�dzie inny ni� tego oczekujemy, wtedy musimy pobra� inne dane.
				if (!newData.getTag().equals(tag.toString())) {
					System.out.println("Porzucenie danych o identyfikatorze " + newData.getIdTestData() + ". Dane nie s� gotowe do wykorzystania na tym etapie. Przyczyn� mo�e by� niepoprawne wykonanie poprzedniego etapu.");
					newData = data.dbGetTestData(tag, true);
					if (newData != null) {
						Conf.setIdTestData(newData.getIdTestData());
					} else {
						Conf.setIdTestData(-1);
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
		if (window.getXWindow() == null)
			log.fail(windowName + ": object does not exist in xml repo file!");
		return window;
	}
	
	public Button getButton(TestObjectManager tom, Window window, String testObjectName) {
		
		Button button = new Button(this, tom, window, testObjectName);

		if (!button.isNotNull())
			log.fail(testObjectName + ": object does not exist in xml repo file!");

		return button;
	}
	
	public Element getElement(TestObjectManager tom, Window window, String testObjectName){
		Element element = new Element(this, tom, window, testObjectName);
		if (!element.isNotNull())
			log.fail(testObjectName + ": object does not exist in xml repo file!");
		return element;
	}
	
	public TextBox getTextBox(TestObjectManager tom, Window window, String testObjectName) {
		TextBox textBox = new TextBox(this, tom, window, testObjectName);
		if (!textBox.isNotNull())
			log.fail(testObjectName + ": object does not exist in xml repo file!");
		return textBox;
	}
	
	public Image getImage(TestObjectManager tom, Window window, String testObjectName) {
		Image image = new Image(this, tom, window, testObjectName);
		if (!image.isNotNull())
			log.fail(testObjectName + ": object does not exist in xml repo file!");
		return image;
	}
	
	public ComboBox getComboBox(TestObjectManager tom, Window window, String testObjectName){
		ComboBox combobox = new ComboBox(this, tom, window, testObjectName);
		if(!combobox.isNotNull())
			log.fail(testObjectName + ": object does not exist in xml repo file!");
		return combobox;
	}
	
	
	
}
