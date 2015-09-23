package fp.forevo.proxy;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.image.BufferedImage;

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
import fp.forevo.manager.TestSettings;
import fp.forevo.xml.map.XImage;
import fp.forevo.xml.map.XTestObject;

public class TestObject {
	
	protected Window window = null;
	protected XTestObject xTestObject = null;
	protected MasterScript ms = null;
	public WebDriverWait webDriverWait = null;
	private String resDir = null;
	
	public TestObject(MasterScript ms, Window window, XTestObject xTestObject, String resDir) {
		this.window = window;
		this.xTestObject = xTestObject;
		this.ms = ms;	
		this.resDir = resDir;
	}
	
	public Window getParentWindow() {
		return window;
	}
	
	public boolean isNotNull() {
		return xTestObject != null;
	}
	
	/** Highlight testObject 
	 * @throws FindFailed */
	public boolean highlight() {
		switch (xTestObject.getDriverName()) {
		case WEB_DRIVER: return webDriverHighlight();
		case AUTO_IT: return autoItHighlight();
		case SIKULI: return sikuliHighlight(getPattern());
		default: return false;
		}
	}
	
	/** Highlight webDriver testObject */
	private boolean webDriverHighlight() {
		WebElement element = MasterScript.browser.findElement(by());
		String border  = element.getCssValue("arguments[0].style.border");
	    if (MasterScript.browser instanceof JavascriptExecutor) {
	        ((JavascriptExecutor) MasterScript.browser).executeScript("arguments[0].style.border='3px solid red'", element);
	        MasterScript.sleep(1);
	        ((JavascriptExecutor) MasterScript.browser).executeScript("arguments[0].style.border='" + border + "'", element);
	    }
	    return true;
	}
	
	/** Highlight autoIt testObject */
	private boolean autoItHighlight() {
		MasterScript.autoIt.controlFocus(window.getXWindow().getTarget(), "", xTestObject.getTarget());
		return true;
	}
	
	/** Highlight sikuli testObject 
	 * @throws FindFailed */
	protected boolean sikuliHighlight(Pattern pattern) {
		Region winRegion = window.getRegion();
		if (winRegion != null) {
			Match match;
			try {
				match = winRegion.find(pattern);
				match.highlight(1);
				Region point = new Region(match.getTarget().getX(), match.getTarget().getY(), 1, 1);
				point.highlight(1);
			} catch (FindFailed e) {}			
			return true;
		} else {
			return false;
		}
	}
	
	/** click at the object 
	 * @throws TafException 
	 * @throws FindFailed */
	public void click() throws TafException  {		
		switch (xTestObject.getDriverName()) {
		case WEB_DRIVER:
			if (MasterScript.isDebugMode()) webDriverHighlight();
			
			waitForVisible();
			//MasterScript.browser.findElement(by()).click();
			WebElement element = MasterScript.browser.findElement(by());
			((JavascriptExecutor) MasterScript.browser).executeScript("arguments[0].scrollIntoView(false);", element);
			element.click();
			
			break;
		case AUTO_IT:
			if (MasterScript.isDebugMode()) autoItHighlight();
			MasterScript.autoIt.controlClick(window.getXWindow().getTarget(), "", xTestObject.getTarget());
			break;
		case SIKULI:
			Pattern pattern = getPattern();
			try {
				if (MasterScript.isDebugMode()) sikuliHighlight(pattern);
				window.getRegion().click(pattern);
			} catch (FindFailed e) {}
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
			Pattern pattern = getPattern();		
			Match statusSikuli = window.getRegion().exists(pattern);
			if(statusSikuli!=null){
				return true;
			}else{
				return false;
			}										
		}
		return false;		
	}
	
	public boolean checkVisibility() {
		return true;
	}
	
	/** Returns Sikuli object pattern */
	protected Pattern getPattern() {
		XImage image = getImage();
		Pattern pattern = new Pattern(resDir + "\\" + image.getFileName());
		if (image.getSimilarity() != null) {
			pattern = pattern.similar(image.getSimilarity());
		}
		if (image.getOffsetX() != null | image.getOffsetY() != null) {
			pattern = pattern.targetOffset(image.getOffsetX(), image.getOffsetY());
		}
		return pattern;
	}
	
	/** Returns Sikuli object xImage */
	protected XImage getImage() {
		for (XImage img : xTestObject.getImage()) {
			if (MasterScript.getTag() == null) {
				return img;
			} else if (img.getTag().contains(MasterScript.getTag())) {
				return img;
			}
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
	private void reportError(String message, BufferedImage image ) throws TafException{
		StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
		StackTraceElement e = stacktrace[2];
		String methodName = e.getMethodName();
		if(!TestSettings.isContinueOnError()){
			throw new TafException(methodName,message,window.screenShot());}else{
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
			Pattern pattern = getPattern();
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
		String [] loc = xTestObject.getTarget().split(":");
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
			// TODO Auto-generated catch block
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
			System.err.println("Currently method getText() is not supported in Sikuli");
		default:
			System.err.println("Unsupported tool name " + xTestObject.getDriverName());
			break;		
		}
		return null;
	}
	
	public boolean assertText(String expectedText) throws TafException{
		String text=getText();
		if(text.equals(expectedText)){
			ms.log.info("Found the correct text: \""+text+"\"");
			return true;
		}else{
			reportError("Invalid text. Expected:\""+expectedText+"\". Found:\""+text+"\"", window.screenShot());
		}
		return false;
	}
	
}