package fp.forevo.manager;

/**
 * Enumeracja reprezentuj¹ca statusy testu.
 * @author kslysz
 */
public enum Status {
	PASSED(1),		// Brak b³êdów w skrypcie
	WARNING(3),	// B³êdy w skrypcie pozwalaj¹ce kontynuowaæ test
	FAILED(2)	,	// B³êdy w skrypcie które blokuj¹ wykonanie pozosta³ych akcji
	INFO(4);		// Informacja
	
	private int id_status;

	private Status(int id_status) {
		this.id_status = id_status;
	}

	public int getId() {
		return id_status;
	}
	
}
