package fp.forevo.manager;

/**
 * Klasa przeznaczona do obs³ugi danych pobieranych z bazy. 
 * Obiekt tej klasy przechowuje informacje na temat idetyfikatora, etykiety oraz danych testowych.
 * @author kslysz (ForProgress)
 */
public class DataSet {
	
	private int idTestData = 0;	// identyfikator pobierany z pola test_data.id_test_data
	private String tag = null;  // etykieta pobierana z pola test_data.tag
	private String data = null;	// dane testowe pobierane z pola test_data.data
	
	/**
	 * Konstruktor klasy.
	 * @param idTestData - identyfikator
	 * @param tag - etykieta danych
	 * @param data - dane testowe
	 */
	public DataSet(int idTestData, String tag, String data) {
		this.idTestData = idTestData;
		this.tag = tag;
		this.data = data;
	}
	
	public int getIdTestData() {
		return idTestData;
	}
	
	public void setIdTestData(int idTestData) {
		this.idTestData = idTestData;
	}
	
	public String getTag() {
		return tag.toString();
	}
	
	public void setTag(Tag tag) {
		this.tag = tag.toString();
	}
	
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}	
	
	public void printData() {
		System.out.println("IdTestData: " + idTestData + ", Key: " + tag + ", Data: " + data);
	}
}
