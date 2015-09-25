package fp.forevo.manager;

import java.util.HashMap;
import java.util.Map;

public class ParamProvider {
	private Map<String, String> map = null; 
	
	public ParamProvider() {
		map = new HashMap<String, String>();
	}
	
	/**
	 * Konstruktor do wykorzystania przy wyci¹ganiu danych z bazy. Wtedy data jest stringiem
	 * @param data
	 */
	public ParamProvider(String data) {
		map = new HashMap<String, String>();
		String[] table = data.split(";");
		for (String item : table) {
			String[] keyValue = item.split(":"); 
			map.put(keyValue[0], keyValue[1]);
		}	
	}
	
	public ParamProvider(String [] data) {
		map = new HashMap<String, String>();
		for (int i = 0; i < data.length; i++) {
			String [] param = data[i].split(":");
			map.put(param[0], param[1]);
		}
	}
	
	public void addParam(String paramName, String paramValue) {
		map.put(paramName, paramValue);
	}
	
	public String getParam(String paramName) {
		return map.get(paramName);
	}
	
	public String getAllParams() {
		String result = "";
		for(String pName : map.keySet())
			result += pName + ":" + map.get(pName) + ";";

		return result.substring(0, result.length() - 1);
	}
}
