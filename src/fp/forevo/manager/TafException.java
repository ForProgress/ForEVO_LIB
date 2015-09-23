package fp.forevo.manager;

import java.awt.image.BufferedImage;


public class TafException extends Exception {
	Logger log=new Logger();
	
	BufferedImage image;
	
    public TafException(String message, BufferedImage image ) {
    	super(message);
    	StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
		StackTraceElement e = stacktrace[2];
		String methodName = e.getMethodName();
    	this.image=image;
    	log.error(methodName, message, image);    
    }
    public TafException(String action,String message, BufferedImage image ) {
    	super(message);
    	this.image=image;
    	log.error(action, message, image);    
    }
    public TafException(String message ) {
    	super(message);
    	StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
		StackTraceElement e = stacktrace[2];
		String methodName = e.getMethodName();
    	log.error(methodName, message);    
    }
    public TafException(String action, String message ) {
    	super(message);
    	log.error(action, message);     
    }
    
}