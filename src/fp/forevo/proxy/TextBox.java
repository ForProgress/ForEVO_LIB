package fp.forevo.proxy;

import org.sikuli.script.FindFailed;
import org.sikuli.script.Match;
import org.sikuli.script.Pattern;
import org.sikuli.script.Region;

import fp.forevo.manager.MasterScript;
import fp.forevo.manager.TestObjectManager;
import fp.forevo.xml.map.XImage;

public class TextBox extends TestObject {

	public TextBox(MasterScript ms, TestObjectManager tom, Window window, String testObjectName) {
		super(ms, window, tom.getXTestObject(window.getXWindow().getName(), testObjectName), tom.getAbsoluteResPath());
	}
	
	public boolean checkText(String text) {
		return true;
	}
	
	public void setText(String text) {	
		
		//ms.log.info(xTestObject.getName() + ".setText(" + text + ");");
		
		if (MasterScript.isDebugMode()) highlight();
		
		switch (xTestObject.getDriverName()) {
		case WEB_DRIVER:
			MasterScript.browser.findElement(by()).sendKeys(text);
			break;
		case AUTO_IT:
			MasterScript.autoIt.ControlSetText(window.getXWindow().getTarget(), "", xTestObject.getTarget(), text);
			break;
		case SIKULI:
			XImage xImage = getImage();
			Pattern pattern = getPattern(xImage);
			try {
				Region region = window.getRegion();
				if (xImage.isImgRecognition()) {
					region.type(pattern, text);
				} else {
					Match m = region.findText(text);
					Region txtArea = new Region(m.x + xImage.getOffsetX() + m.w/2, m.y + xImage.getOffsetY() + m.h/2, 1, 1);
					txtArea.click();
					window.getRegion().type(text);
				}
			} catch (FindFailed e) {
				e.printStackTrace();
			}
			break;
		default:
			System.err.println("Unsupported tool name " + xTestObject.getDriverName());
		}		
	}
	

}
