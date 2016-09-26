package fp.forevo.proxy;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import fp.forevo.manager.MasterScript;
import fp.forevo.manager.TestObjectManager;

public class ComboBox extends TestObject {

	public ComboBox(MasterScript ms, TestObjectManager tom, Window window, String testObjectName) {
		super(ms, tom, window, tom.getXTestObject(window.getXWindow().getName(), testObjectName), tom.getAbsoluteResPath());
	}
	
	public void selectByValue(String value) {
		WebElement element = MasterScript.browser.findElement(by());
		
		Select select = new Select(element);
		select.selectByValue(value);
	}
	
	public void selectByVisibleText(String text) {
		WebElement element = MasterScript.browser.findElement(by());
		
		Select select = new Select(element);
		select.selectByVisibleText(text);
	}

	public void clickElementFromListByTag(String value, String tag) {
		try {
			WebElement select = MasterScript.browser.findElement(by());

			List<WebElement> options = select.findElements(By.tagName(tag));

			for (WebElement option : options) {
				if ((option.getText()).contains(value))
					option.click();
			}
		} catch (StaleElementReferenceException e) {
			WebElement select = MasterScript.browser.findElement(by());
			List<WebElement> options = select.findElements(By.tagName(tag));
			for (WebElement option : options) {
				if (value.contains(option.getText()))
					option.click();
			}
		}

	}
}
