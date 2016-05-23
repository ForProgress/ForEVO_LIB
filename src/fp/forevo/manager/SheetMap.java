package fp.forevo.manager;

public enum SheetRepo {
	

	SOURCE("C:/Workspace/XML Data Test/src/test", "Dane.xls", "Arkusz1"),	
	TARGET("C:/Workspace/XML Data Test/src/test", "DaneNowe.xls", "Arkusz1"),

    ;
	private final String filePath;
    private final String fileName;
    private final String sheetName;

    private SheetRepo(final String filePath, final String fileName, final String sheetName) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.sheetName = sheetName;
    }
    
    public String getFilePath() {
    	if (filePath.endsWith("/"))
    		return filePath + fileName;
    	else    			
    		return filePath + "/" + fileName;
    }
    
    public String getSheetName() {
    	return sheetName;
    }
	
}
