package fp.forevo.proxy;

public enum Browser {

	Firefox("Firefox"),
    Chrome("Chrome"),
    InternetExplorer("InternetExplorer"),
    Opera("Opera"),
    Safari("Safari");
    
    private final String browserName;

    private Browser(final String browserName) {
        this.browserName = browserName;
    }
    
    @Override
    public String toString() {
        return browserName;
    }
	
}
