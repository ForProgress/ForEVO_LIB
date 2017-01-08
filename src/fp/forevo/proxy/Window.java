package fp.forevo.proxy;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.image.BufferedImage;

import org.openqa.selenium.By;
import org.openqa.selenium.remote.SessionNotFoundException;
import org.sikuli.script.Region;

import fp.forevo.manager.MasterScript;
import fp.forevo.manager.TafException;
import fp.forevo.manager.TestObjectManager;
import fp.forevo.xml.repo.XWindow;

public class Window {

	private XWindow xWindow = null;

	public Window(MasterScript ms, TestObjectManager tom, String windowName) {
		xWindow = tom.getXWindow(windowName);
	}

	/** Get XML object representation */
	public XWindow getXWindow() {
		return xWindow;
	}

	/** Close window */
	public void close() {
		MasterScript.autoIt.winClose(getXWindow().getTarget());
	}

	/** Get Sikuli region from autoIt window */
	public Region getRegion() {
		int x, y, w, h;
		switch (getXWindow().getDriverName()) {
		case WEB_DRIVER:
			x = MasterScript.browser.manage().window().getPosition().x;
			y = MasterScript.browser.manage().window().getPosition().y;
			w = MasterScript.browser.manage().window().getSize().width;
			h = MasterScript.browser.manage().window().getSize().height;
			return new Region(x, y, w, h);
		case AUTO_IT:
			x = MasterScript.autoIt.winGetPosX(xWindow.getTarget());
			y = MasterScript.autoIt.winGetPosY(xWindow.getTarget());
			w = MasterScript.autoIt.winGetPosWidth(xWindow.getTarget());
			h = MasterScript.autoIt.winGetPosHeight(xWindow.getTarget());
			return new Region(x, y, w, h);
		default:
			break;
		}
		return null;
	}

	/** Set title for application window */
	public void setTitle(String title) {
		MasterScript.autoIt.winSetTitle(xWindow.getTarget(), "", title);
		xWindow.setTarget(title);
	}

	/**
	 * Verify Title
	 * 
	 * @param title
	 * @return true if page contains specified title or false otherwise
	 */
	public boolean verifyTitle(String title) {
		String currentTitle = MasterScript.browser.getTitle();
		if (currentTitle.equals(title)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Assert Title
	 * 
	 * @param title
	 * @return true if page contains specified title or throw exception
	 *         otherwise
	 */
	public boolean assertTitle(String title) throws TafException {
		String currentTitle = MasterScript.browser.getTitle();
		if (currentTitle.equals(title)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Assert Text
	 * 
	 * @param Expected
	 *            Text
	 * @return true if page contains specified text or throw exception otherwise
	 */
	public boolean assertText(String expectedText) throws TafException {
		boolean status = MasterScript.browser.findElement(By.cssSelector("body")).getText().contains(expectedText);

		if (status == true) {
			return status;
		} else {
			return status;
		}
	}

	/**
	 * Verify Text
	 * 
	 * @param Expected
	 *            Text
	 * @return true if page contains specified text or false otherwise
	 */
	public boolean verifyText(String expectedText) throws TafException {
		boolean status = MasterScript.browser.findElement(By.cssSelector("body")).getText().contains(expectedText);

		if (status == true) {
			return status;
		} else {
			return status;
		}
	}

	/**
	 * Wait maximum TIMEOUT for window activation. TIMEOUT is defined in
	 * MasterScript
	 */
	public void activate() {
		activate(MasterScript.TIMEOUT);
	}

	/** Wait maximum timeout (sec) for window activation */
	public void activate(int timeout) {
		switch (getXWindow().getDriverName()) {
		case WEB_DRIVER:
			// String handle = ms.browser.getWindowHandle();
			// ms.browser.switchTo().window(handle);
			// Nie wiem jak wyciagnac okno przegladarki na wierzch...
			break;
		case AUTO_IT:
			MasterScript.autoIt.winActivate(getXWindow().getTarget());
			MasterScript.autoIt.winWaitActive(getXWindow().getTarget(), "", timeout);
			break;
		default:
			break;
		}
	}

	/** Wait until app is not opened and activated */
	public void waitIfNotActive() {
		switch (getXWindow().getDriverName()) {
		case WEB_DRIVER:
			// Nie wiem jak wyciagnac okno przegladarki na wierzch...
			break;
		case AUTO_IT:
			MasterScript.autoIt.winWaitActive(getXWindow().getTarget(), "", MasterScript.TIMEOUT);
			break;
		default:
			break;
		}
	}

	public void highlight() {
		getRegion().highlight(1);
	}

	public boolean exist() {
		switch (getXWindow().getDriverName()) {
		case WEB_DRIVER:
			if (MasterScript.browser == null) {
				return false;
			} else {
				try {
					MasterScript.browser.getTitle();
					return true;
				} catch (SessionNotFoundException e) {
					return false;
				}
			}
		case AUTO_IT:
			return MasterScript.autoIt.winExists(xWindow.getTarget());
		default:
			return false;
		}
	}

	public BufferedImage screenShot() {

		Robot robot;
		try {
			robot = new Robot();
			BufferedImage bi = robot.createScreenCapture(getRegion().getRect());
			return bi;
		} catch (AWTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}
}
