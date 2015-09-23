package fp.forevo.proxy;

import org.sikuli.script.FindFailed;
import org.sikuli.script.Pattern;

import fp.forevo.manager.MasterScript;
import fp.forevo.manager.TestObjectManager;

public class TextBox extends TestObject {

	public TextBox(MasterScript ms, TestObjectManager tom, Window window, String testObjectName) {
		super(ms, window, tom.getXTestObject(window.getXWindow().getName(), testObjectName), tom.getAbsoluteResPath());
	}
	
	public boolean checkText(String text) {
		return true;
	}
	
	public void setText(String text) {	
		
		ms.log.info(xTestObject.getName() + ".setText(" + text + ");");
		
		if (MasterScript.isDebugMode()) highlight();
		
		switch (xTestObject.getDriverName()) {
		case WEB_DRIVER:
			MasterScript.browser.findElement(by()).sendKeys(text);
			break;
		case AUTO_IT:
			MasterScript.autoIt.ControlSetText(window.getXWindow().getTarget(), "", xTestObject.getTarget(), text);
			break;
		case SIKULI:
			Pattern pattern = getPattern();
			if (MasterScript.isDebugMode()) sikuliHighlight(pattern);
			try {
				window.getRegion().type(pattern, text);
			} catch (FindFailed e) {
			}
			break;
		default:
			System.err.println("Unsupported tool name " + xTestObject.getDriverName());
		}		
	}
	

}
