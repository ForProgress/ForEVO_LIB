package fp.forevo.proxy;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.util.Iterator;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.sikuli.script.FindFailed;
import org.sikuli.script.Match;
import org.sikuli.script.Pattern;
import org.sikuli.script.Region;

import fp.forevo.manager.MasterScript;
import fp.forevo.manager.TafException;
import fp.forevo.manager.TestObjectManager;
import fp.forevo.manager.TestSettings;
import fp.forevo.xml.map.XImage;
import fp.forevo.xml.map.XTestObject;
import fp.forevo.xml.project.XTag;

public class TestObject {
	
	protected Window window = null;
	protected XTestObject xTestObject = null;
	protected MasterScript ms = null;
	protected TestObjectManager tom = null;
	public WebDriverWait webDriverWait = null;
	private String resDir = null;
	
	// GLOBAL METHODS

	public TestObject(MasterScript ms, TestObjectManager tom, Window window, XTestObject xTestObject, String resDir) {
		this.window = window;
		this.xTestObject = xTestObject;
		this.ms = ms;	
		this.tom = tom;
		this.resDir = resDir;
	}
	
	public Window getParentWindow() {
		return window;
	}
	
	public String getImagePath() {
		switch (xTestObject.getDriverName()){	
		case SIKULI:
			return getImage().getFileName();
		default:
			return null;		
		}
	}
	
	/**
	 * For <b>Sikuli</b> object only!<br/>
	 * Find all elements on parent window.
	 * @return Iterator&lt;Match&gt; or null
	 */
	public Iterator<Match> findAll() {
		switch (xTestObject.getDriverName()){	
		case SIKULI:
			XImage xImage = getImage();	
			Pattern pattern = getPattern(xImage);
			try {
				if (xImage.isImgRecognition()) {
					return window.getRegion().findAll(pattern);
				} else {
					return window.getRegion().findAllText(xImage.getOcrText());
				}				
			} catch (FindFailed e) {
				e.printStackTrace();
			}
		default:
			return null;		
		}
	}
	
	public boolean isNotNull() {
		return xTestObject != null;
	}
	
	public Region getRegion() {
		switch (xTestObject.getDriverName()){
		case WEB_DRIVER:
			System.err.println("Currently method getRegion() is not supported in WebDriver");
		case AUTO_IT:
			System.err.println("Currently method getRegion() is not supported in AutoIt");		
		case SIKULI:
			XImage xImage = getImage();	
			Pattern pattern = getPattern(xImage);
			try {
				if (xImage.isImgRecognition()) {
					Region objRegion = window.getRegion().find(pattern);
					if (xImage.getShift() != null) {
						Rectangle shift = getRectangle(xImage.getShift());
						return new Region(objRegion.x + shift.x, objRegion.y + shift.y, shift.width, shift.height);
					} else {
						return objRegion;
					}
				} else {
					Match txtArea = window.getRegion().findText(xImage.getOcrText());
					if (xImage.getShift() != null) {
						Rectangle shift = getRectangle(xImage.getShift());
						return new Region(txtArea.x + shift.x, txtArea.y + shift.y, shift.width, shift.height);
					} else {
						return new Region(txtArea.getRect());
					}
				}				
			} catch (FindFailed e) {
				e.printStackTrace();
			}			
		}
		return null;
	}
	
	public void highlight() {
		highlight(1);
	}
	
	public void highlight(int sec) {
		switch (xTestObject.getDriverName()) {
		case WEB_DRIVER: webDriverHighlight(); break;
		case AUTO_IT: autoItHighlight(); break;
		case SIKULI: sikuliHighlight(); break;
		default: 
			return;
		}
	}
	
	/** Highlight webDriver testObject */
	private void webDriverHighlight() {
		WebElement element = MasterScript.browser.findElement(by());
		String border  = element.getCssValue("arguments[0].style.border");
	    if (MasterScript.browser instanceof JavascriptExecutor) {
	        ((JavascriptExecutor) MasterScript.browser).executeScript("arguments[0].style.border='3px solid red'", element);
	        MasterScript.sleep(1);
	        ((JavascriptExecutor) MasterScript.browser).executeScript("arguments[0].style.border='" + border + "'", element);
	    }
	}
	
	/** Highlight autoIt testObject */
	private void autoItHighlight() {
		MasterScript.autoIt.controlFocus(window.getXWindow().getTarget(), "", xTestObject.getTarget());
	}
	
	/** Highlight sikuli testObject 
	 * @throws FindFailed */
	protected void sikuliHighlight() {
		XImage xImage = getImage();
		Region winRegion = window.getRegion();
		if (winRegion != null) {
			try {				
				Match match = null;
				if (xImage.isImgRecognition()) {
					match = winRegion.find(getPattern(xImage));
					match.highlight(1);
					Region point = new Region(match.getTarget().getX(), match.getTarget().getY(), 1, 1);
					point.highlight(1);
				} else {
					match = winRegion.findText(xImage.getOcrText());
					match.highlight(1);
					Region point = new Region(match.getX() + xImage.getOffsetX() + match.getW()/2, match.getY() + xImage.getOffsetY() + match.getH()/2, 1, 1);
					point.highlight(1);
				}				
			} catch (FindFailed e) {
				e.printStackTrace();
			}			
		} 
	}
	
	/** click at the object 
	 * @throws TafException 
	 * @throws FindFailed */
	public void click() throws TafException  {		
		switch (xTestObject.getDriverName()) {
		case WEB_DRIVER:
			waitForVisible();
			//MasterScript.browser.findElement(by()).click();
			WebElement element = MasterScript.browser.findElement(by());
			((JavascriptExecutor) MasterScript.browser).executeScript("arguments[0].scrollIntoView(false);", element);
			element.click();
			
			break;
		case AUTO_IT:
			MasterScript.autoIt.controlClick(window.getXWindow().getTarget(), "", xTestObject.getTarget());
			break;
		case SIKULI:
			XImage xImage = getImage();	
			if (xImage.getOffsetX() == null) xImage.setOffsetX(0);
			if (xImage.getOffsetY() == null) xImage.setOffsetY(0);
			
			Pattern pattern = getPattern(xImage);
			try {
				if (xImage.isImgRecognition()) {
					window.getRegion().click(pattern);
				} else {
					Match txtArea = window.getRegion().findText(xImage.getOcrText());
					Region point = new Region(txtArea.x + xImage.getOffsetX() + txtArea.w/2, txtArea.y + xImage.getOffsetY() + txtArea.h/2, 1, 1);
					point.click();
				}				
			} catch (FindFailed e) {
				e.printStackTrace();
			}
			break;
		default:
			System.err.println("Unsupported tool name " + xTestObject.getDriverName());
		}
	}
	
	public boolean checkIfExist(){
		switch (xTestObject.getDriverName()){
		case WEB_DRIVER:
			if(MasterScript.browser.findElement(by())!=null){
				return true;
			} else { return false; }			
		case AUTO_IT:
			boolean status = MasterScript.autoIt.controlFocus(window.getXWindow().getTarget(), "", xTestObject.getTarget());
			return status;			
		case SIKULI:
			XImage xImage = getImage();
			Pattern pattern = getPattern(xImage);
			if (xImage.isImgRecognition()) {
				return (window.getRegion().exists(pattern) != null);
			} else {
				return sikuli_FindText(window.getRegion(), xImage.getOcrText()) != null;
			}
												
		}
		return false;		
	}
	
	private Match sikuli_FindText(Region region, String text) {
		return sikuli_FindText(region, text, (double) ms.TIMEOUT);
	}
	
	private Match sikuli_FindText(Region region, String text, Double timeout) {
		try {
			return region.findText(text, timeout);
		} catch (FindFailed e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public boolean checkVisibility() {
		return true;
	}
	
	/** Returns Sikuli object pattern */
	protected Pattern getPattern(XImage xImage) {
		Pattern pattern = new Pattern(resDir + "\\" + xImage.getFileName());
		if (xImage.getSimilarity() != null) {
			pattern = pattern.similar(xImage.getSimilarity());
		}
		if (xImage.getOffsetX() != null | xImage.getOffsetY() != null) {
			pattern = pattern.targetOffset(xImage.getOffsetX(), xImage.getOffsetY());
		}
		return pattern;
	}
	
	/**
	 * Funkcja dostaje listê uidow a zwraca listê tagow
	 * @param uids
	 * @return
	 */
	private String getTags(String uids) {
		String [] uidArray = uids.split(";");
		String result = "";
		for (String uid : uidArray) {
			result += getTagName(uid) + ";";
		}
		return result;
	}
	
	/**
	 * Funkcja zwraca nazwê taga na podstawie uida
	 * @param uid
	 * @return
	 */
	public String getTagName(String uid) {
		for (XTag xTag : tom.getTagList()) {
			if (xTag.getUID().equals(uid))
				return xTag.getName();
		}
		return null;
	}
	
	/** Returns Sikuli object xImage */
	public XImage getImage() {
		for (XImage img : xTestObject.getImage()) {
			if (MasterScript.getTag() == null) {
				return img;
			} else if (img.getTagUids() != null && getTags(img.getTagUids()).contains(MasterScript.getTag())) {
				return img;
			}
		}
		
		// If we have one img olny it don't need tag
		if (xTestObject.getImage().size() == 1) {
			return xTestObject.getImage().get(0);
		}
		
		return null;
	}
	
	public boolean waitIfNotExist() throws TafException {
		 return waitIfNotExist(MasterScript.TIMEOUT);
	}
	
	public boolean waitIfNotExist(int maxSec) throws TafException {
		Long start = System.currentTimeMillis();

		while (!exist()) {
			Long current = System.currentTimeMillis();
			if (start + maxSec * 1000 > current) {
				reportError(
						"Obiekt " + xTestObject.getName()
								+ " nie zostal znaleziony w czasie " + maxSec
								+ " sec.", window.screenShot());
				return false;
			}
		}
		return true;
	}

	private void reportError(String message, BufferedImage image) throws TafException {
		StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
		StackTraceElement e = stacktrace[2];
		String methodName = e.getMethodName();
		if (!TestSettings.isContinueOnError()) {
			throw new TafException(methodName, message, window.screenShot());
		} else {
			ms.log.error(methodName, message, image);
		}
	}
	public boolean waitIfExist() throws TafException {
		return waitIfExist(MasterScript.TIMEOUT);
	}
	
	public boolean waitIfExist(int maxSec) throws TafException {
		Long start = System.currentTimeMillis();

		while (!exist()) {
			Long current = System.currentTimeMillis();
			if (start + maxSec * 1000 > current) {
				reportError(
						"Obiekt " + xTestObject.getName()
								+ " nie zniknal znaleziony w czasie " + maxSec
								+ " sec.", window.screenShot());
				return false;
			}
		}
		return true;
	}

	/**
	 * Used by WebDriver
	 * @throws TafException 
	 * 
	 */
	public void waitForPresent() throws TafException{
		waitForPresent(MasterScript.TIMEOUT);
	}
	
	/**
	 * Used by WebDriver
	 * @throws TafException 
	 * 
	 */
	public void waitForPresent(int maxSec) throws TafException{
		
		switch(xTestObject.getDriverName()){
		case WEB_DRIVER:
			this.webDriverWait = new WebDriverWait(this.ms.browser,  this.ms.TIMEOUT);
			webDriverWait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(this.by()));
			break;
		case AUTO_IT:
			waitIfExist(maxSec);
			break;
		case SIKULI:
			waitIfExist(maxSec);
		default:
			break;		
		}
	}
	
	/**
	 * Used by WebDriver
	 * @throws TafException 
	 * 
	 */
	public void waitForNotPresent() throws TafException{
		waitForNotPresent(MasterScript.TIMEOUT);
	}
	
	/**
	 * Used by WebDriver
	 * @throws TafException 
	 * 
	 */
	public void waitForNotPresent(int maxSec) throws TafException{
		switch(xTestObject.getDriverName()){
		case WEB_DRIVER:	
			this.webDriverWait = new WebDriverWait(this.ms.browser,  this.ms.TIMEOUT);
			webDriverWait.until(ExpectedConditions.not(ExpectedConditions.presenceOfElementLocated((this.by()))));
			break;
		case AUTO_IT:
			waitIfExist(maxSec);
			break;
		case SIKULI:
			waitIfExist(maxSec);
		default:
			break;		
		}
	}
	
	public void waitForVisible() throws TafException{
		waitForVisible(MasterScript.TIMEOUT);
	}
	
	public void waitForVisible(int maxSec) throws TafException{
		switch(xTestObject.getDriverName()){
		case WEB_DRIVER:
			this.webDriverWait = new WebDriverWait(this.ms.browser,  this.ms.TIMEOUT);
			webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(this.by()));
			break;
		case AUTO_IT:
			waitIfExist(maxSec);
			break;
		case SIKULI:
			waitIfExist(maxSec);
		default:
			break;		
		}
	}
	
	public void waitForNotVisible() throws TafException{
		waitForVisible(MasterScript.TIMEOUT);
	}
	
	public void waitForNotVisible(int maxSec) throws TafException{
		switch(xTestObject.getDriverName()){
		case WEB_DRIVER:
			this.webDriverWait = new WebDriverWait(this.ms.browser,  this.ms.TIMEOUT);
			webDriverWait.until(ExpectedConditions.not(ExpectedConditions.visibilityOfElementLocated(this.by())));
			break;
		case AUTO_IT:
			waitIfExist(maxSec);
			break;
		case SIKULI:
			waitIfExist(maxSec);
		default:
			break;		
		}
	}
	
	
	/**
	 * Used by WebDriver
	 * 
	 */
	public boolean exist() {
		
		switch (xTestObject.getDriverName()) {
		case WEB_DRIVER:
			if (MasterScript.browser.findElement(by()) != null) return true;
			else return false;
		case AUTO_IT:
			return MasterScript.autoIt.controlEnable(window.getXWindow().getTarget(), "", xTestObject.getTarget());
		case SIKULI:
			XImage xImage = getImage();
			Pattern pattern = getPattern(xImage);
			Region region = window.getRegion();
			region.waitVanish(pattern);
			try {
				region.find(pattern);
				return true;
			} catch (FindFailed e) {
				return false;
			} 			
		default:
			break;
		}
		return false;
	}
	
	protected By by() {
		String target = xTestObject.getTarget();
		if (target.contains("=")) {
			String [] loc = xTestObject.getTarget().split("=");
			switch (loc[0]) {
			case "id": return By.id(loc[1]);
			case "xpath": return By.xpath(loc[1]);
			case "link" : return By.linkText(loc[1]);
			case "class" : return By.className(loc[1]);
			case "css" : return By.cssSelector(loc[1]);
			case "cssSelector": return By.cssSelector(loc[1]);
			case "linkText": return By.linkText(loc[1]);
			case "name": return By.name(loc[1]);
			default:
				System.out.println("Unexpected localizator '" + loc[0] + "' in text object '" + getObjectName() + "'");
				return null;
			}
		} else {
			return By.xpath(target);
		}
		
	}
	
	/**
	 * Used by WebDriver
	 * @return webElement
	 */
	protected WebElement findElement() {
		WebElement element = MasterScript.browser.findElement(by());
		return element;
	}
	
	/**
	 * Returns window and object names
	 * @return for example: winMyApp->btnOK
	 */
	protected String getObjectName() {
		return window.getXWindow().getName() + "->" + xTestObject.getName();
	}
	
	/**
	 * Used by Sikuli
	 * @return window region(X, Y, W, H)
	 */
	protected Region getWindowRegion() {
		return window.getRegion();
	}	
	
	public BufferedImage screenShot(){
		
		Robot robot;
		try {
			robot = new Robot();
			BufferedImage bi = robot.createScreenCapture(getWindowRegion().getRect());
			return bi;
		} catch (AWTException e) {
			e.printStackTrace();
		}
		return null;
				
	}
	public String getText(){
		switch(xTestObject.getDriverName()){
		case WEB_DRIVER:
			 return MasterScript.browser.findElement(by()).getText();
		case AUTO_IT:
			return MasterScript.autoIt.controlGetText(window.getXWindow().getTarget(), "", xTestObject.getTarget());
		case SIKULI:
			XImage xImage = getImage();	
			Pattern pattern = getPattern(xImage);
			try {
				if (xImage.isImgRecognition()) {
					Region objRegion = window.getRegion().find(pattern);
					if (xImage.getShift() != null) {
						Rectangle shift = getRectangle(xImage.getShift());
						Region shiftRegion = new Region(objRegion.x + shift.x, objRegion.y + shift.y, shift.width, shift.height);
						return shiftRegion.text();
					} else {
						return objRegion.text();
					}
				} else {
					Region txtRegion = window.getRegion().findText(xImage.getOcrText());
					if (xImage.getShift() != null) {
						Rectangle shift = getRectangle(xImage.getShift());
						Region shiftRegion = new Region(txtRegion.x + shift.x, txtRegion.y + shift.y, shift.width, shift.height);
						return shiftRegion.text();
					} else {
						return txtRegion.text();
					}
				}				
			} catch (FindFailed e) {
				e.printStackTrace();
			}
			return null;
		default:
			System.err.println("Unsupported tool name " + xTestObject.getDriverName());
			return null;		
		}
	}
	
	private Rectangle getRectangle(String strRectangle) {
		strRectangle = strRectangle.substring(11, strRectangle.length() - 1);
		String [] array = strRectangle.split(",");
		
		int x = Integer.parseInt(array[0].trim());
		int y = Integer.parseInt(array[1].trim());
		int w = Integer.parseInt(array[2].trim());
		int h = Integer.parseInt(array[3].trim());
		return new Rectangle(x, y, w, h);
	}
	
	public boolean assertText(String expectedText) throws TafException{
		String text = getText();
		if(text.equals(expectedText)){
			ms.log.info("Found the correct text: \""+text+"\"");
			return true;
		}else{
			reportError("Invalid text. Expected:\""+expectedText+"\". Found:\""+text+"\"", window.screenShot());
		}
		return false;
	}
}
