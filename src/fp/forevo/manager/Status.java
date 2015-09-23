package fp.forevo.manager;

/**
 * Enumeracja reprezentuj�ca statusy testu.
 * @author kslysz
 */
public enum Status {
	PASSED(1),		// Brak b��d�w w skrypcie
	WARNING(3),	// B��dy w skrypcie pozwalaj�ce kontynuowa� test
	FAILED(2)	,	// B��dy w skrypcie kt�re blokuj� wykonanie pozosta�ych akcji
	INFO(4);		// Informacja
	
	private int id_status;

	private Status(int id_status) {
		this.id_status = id_status;
	}

	public int getId() {
		return id_status;
	}
	
}
