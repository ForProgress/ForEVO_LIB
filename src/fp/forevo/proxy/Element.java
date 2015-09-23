package fp.forevo.proxy;

import fp.forevo.manager.MasterScript;
import fp.forevo.manager.TestObjectManager;

public class Element extends TestObject {

	public Element(MasterScript ms, TestObjectManager tom, Window window, String testObjectName) {
		super(ms, window, tom.getXTestObject(window.getXWindow().getName(), testObjectName), tom.getAbsoluteResPath());
	}
	

}
