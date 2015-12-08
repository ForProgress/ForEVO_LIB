package fp.forevo.proxy;

import fp.forevo.manager.MasterScript;
import fp.forevo.manager.TestObjectManager;

public class Image extends TestObject {

	public Image(MasterScript ms, TestObjectManager tom, Window window, String testObjectName) {
		super(ms, tom, window, tom.getXTestObject(window.getXWindow().getName(), testObjectName), tom.getAbsoluteResPath());
	}
}
