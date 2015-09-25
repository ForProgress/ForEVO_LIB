package fp.forevo.manager;

public enum Tag {
	
	ETAP1("ETAP1"),
	ETAP2("ETAP2"),		
	ETAP3("ETAP3"),	
	ETAP4("ETAP4"),	
	ETAP5("ETAP5"),	
	ETAP6("ETAP6"),	
	ETAP7("ETAP7"),	
	ETAP8("ETAP8"),    
    ;
    private final String text;

    private Tag(final String text) {
        this.text = text;
    }
    
    @Override
    public String toString() {
        return text;
    }	
}
