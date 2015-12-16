package fp.forevo.manager;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FileUtils;

import fp.forevo.proxy.Image;
import fp.forevo.xml.map.ObjectFactory;
import fp.forevo.xml.map.XDriverName;
import fp.forevo.xml.map.XImage;
import fp.forevo.xml.map.XTestObject;
import fp.forevo.xml.map.XTestObjectMap;
import fp.forevo.xml.map.XWindow;
import fp.forevo.xml.project.XProject;
import fp.forevo.xml.project.XTag;

public class TestObjectManager {
	
	private XTestObjectMap testObjectMap = null;
	private String projectPath = null;			// np. C:\Workspace\ExampleAutomationProject\
	private String mapName = null;				// np. HomePage
	private boolean changed = false;
	private List<XTag> tagList = null;			// Tag list defined for project
	
	/**
	 * Konstruktor klasy wykorzystywany przez Test Object Manager
	 * @param scriptClassFile - Plik klasy java np. C:\Workspace\ExampleAutomationProject\src\myappl\modules\HomePage.java
	 * @param parentClass - klasa po której ma dziedziczyc klasa mapy. Domyslnie jest "fp.forevo.manager.MasterScript"
	 */
	public TestObjectManager(File scriptClassFile, String parentClass) {
		this.projectPath = scriptClassFile.getAbsolutePath().replace("\\", "/").split("/src")[0].replace("/", "\\");
		ObjectFactory factory = new ObjectFactory();
		testObjectMap = factory.createXTestObjectMap();
		testObjectMap.setScriptClassPath("src" + scriptClassFile.getAbsolutePath().split("src")[1]);
		testObjectMap.setParentClassName(parentClass);
		setMapName(scriptClassFile);
	}
	
	/**
	 * Konstruktor klasy wykorzystywany w skryptach
	 * @param projectPath - Œcie¿ka do projektu np. C:\Workspace\ExampleAutomationProject
	 * @param mapFile
	 */
	public TestObjectManager(String projectPath, String mapPath) {
		this.projectPath = projectPath;
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(XTestObjectMap.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			testObjectMap = (XTestObjectMap) unmarshaller.unmarshal(new File(projectPath + "\\" + mapPath));
			setMapName(mapPath);			
			this.changed = false;
			
			// Wczytanie listy tagów
			JAXBContext projectJaxbContext = JAXBContext.newInstance(XProject.class);
			unmarshaller = projectJaxbContext.createUnmarshaller();
			XProject project = (XProject) unmarshaller.unmarshal(new File(projectPath + "\\project.taf"));	
			tagList = project.getTags();
			
		} catch (JAXBException e) {
			e.printStackTrace();
		}			
	}
	
	public List<XTag> getTagList() {
		return tagList;
	}
	
	public String getProjectPath() {
		return projectPath.replace("/", "\\");
	}
	
	public XTestObjectMap getTestObjectMap() {
		return testObjectMap;
	}
	
	/**
	 * Ustawia nazwe mapy
	 * @param classPath
	 */
	private void setMapName(String classPath) {
		String [] temp = classPath.replace("\\", "/").split("/");
		this.mapName = temp[temp.length - 1].split(".map")[0];
	}
	
	private void setMapName(File scriptClassFile) {
		String [] temp = scriptClassFile.getAbsolutePath().replace("\\", "/").split("/");
		this.mapName = temp[temp.length - 1].split(".java")[0];
	}
	
	/**
	 * @return relative path for map directory 
	 */
	public String getResPath() {
		int i = testObjectMap.getScriptClassPath().lastIndexOf("\\");
		String res = testObjectMap.getScriptClassPath().substring(0, i).replace("\\", ".");
		if (res.equals("src"))
			res = "res";
		else
			res = res.replace("src.", "res\\");
		String name = testObjectMap.getScriptClassPath().substring(i).replace(".java", "").replace("\\", "");
		return res + "\\" + name;
	}
	
	public String getAbsoluteResPath() {
		return getProjectPath() + "\\" + getResPath();
	}
	
	public String getMapPath() {
		return getResPath() + "\\" + getMapName() + ".map";
	}
	
	public String getScriptClassPath() {
		return testObjectMap.getScriptClassPath();
	}
	
	public String getMapClassPath() {
		return getMapPackagePath() + "\\" + getMapName() + "Map.java";
	}
	
	public String getScriptPackagePath() {
		int pos = getScriptClassPath().lastIndexOf("\\");		
		return getScriptClassPath().substring(0, pos);
	}
	
	public String getScriptPackageName() {
		String [] tab = getScriptClassPath().split("src");
		int i = tab[1].lastIndexOf("\\");
		if (i == 0) return null;
		String temp1 = tab[1].substring(1, i).replace("\\", ".");
		
		return temp1;
	}
	
	public String getMapPackageName() {
		return getScriptPackageName() + ".maps";
	}
	
	public String getMapPackagePath() {
		return getScriptPackagePath() + "\\maps";
	}
	
	public String getMapName() {
		return mapName;
	}
	
	public void saveTestObjectMap() {
		JAXBContext jaxbContext;
		String filePath = getAbsoluteResPath() + "\\" + getMapName() + ".map";
		try {
			jaxbContext = JAXBContext.newInstance(XTestObjectMap.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.marshal(testObjectMap, new File(filePath));
			setChanged(false);			
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
	
	public XWindow getXWindow(String name) {
		for (XWindow w : testObjectMap.getWindow()) {
			if (w.getName().equals(name)) {
				return w;
			}
		}
		return null;
	}
	
	public XTestObject getXTestObject(String parentName, String testObjectName) {
		XWindow w = getXWindow(parentName);
		for (XTestObject testObject : w.getTestObject()) {
			if (testObject.getName().equals(testObjectName))
				return testObject;
		}
		return null;
	}
		
	public boolean isChanged() {
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}
	
	public void updateScriptClass() {
		try {
			File file = new File(getProjectPath() + "\\" + getScriptClassPath());
			String name = file.getName().replace(".java", "");
			List<String> lines = FileUtils.readLines(file);
			boolean imp = false;
			for (int i = 0; i < lines.size(); i++) {
				String line = lines.get(i);
				if (line.startsWith("package") && !imp) {
					imp = true;
					lines.set(i, line + "\n\nimport " + getMapPackageName() + "." + getMapName() + "Map;\n");
				}
				if (line.contains("public class")) {
					line = "public class " + name + " extends " + name + "Map {";
					lines.set(i, line);
				}
			}
			
			// Jesli skrypt nie jest w pakiecie to musimy w inny sposob dodac importowanie skryptu mapy
			if (imp == false) {
				lines.set(0, "import maps." + getMapName() + "Map;\n\n" + lines.get(0));
			}
			FileUtils.writeLines(file, lines);
		} catch (IOException e) {
			e.printStackTrace();
		}		 
    }   
	
	public void generateMapClass() {
		try {
			File file = new File(getProjectPath() + "\\" + getMapClassPath());
	        BufferedWriter output = new BufferedWriter(new FileWriter(file));
	        
			if (getScriptPackageName() != null) 
				output.write("package " + getMapPackageName() + ";\n\n");
			else 
				output.write("package maps;\n\n");
			//output.write("import java.io.File;\n");
			output.write("import " + getTestObjectMap().getParentClassName() + ";\n");
			output.write("import fp.forevo.manager.TestObjectManager;\n");
			output.write("import fp.forevo.proxy.*;\n\n");
			output.write("/**\n");
			output.write(" * Class generated automaticaly. Do not edit from text editor. Change it using Test Object Manager.\n");
			output.write(" * @author Test Object Manager by ForProgress\n");
			output.write(" * @see www.forprogress.com.pl\n");
			output.write(" * @since " + currentTime() + "\n");
			output.write(" */\n");
			
			output.write("public class " + getMapName() + "Map");
			
			String [] parentClassName = getTestObjectMap().getParentClassName().replace(".", ",").split(",");
			if (getTestObjectMap().getParentClassName() != null) output.write(" extends " + parentClassName[parentClassName.length - 1] + " {\n");
			output.write("\n");
			output.write("\tprivate TestObjectManager tomgr = new TestObjectManager(getProjectPath(this.getClass()), \"" + getMapPath().replace("\\", "/") + "\");\n");
			output.write("\n");	
			for (XWindow window : getTestObjectMap().getWindow()) {
				output.write("\t/**\n\t * <b>Description:</b> " + formatDescription(getEmptyIfNull(window.getDescription())) + "\n\t */\n");
				output.write("\tprotected Window " + window.getName() + " = getWindow(tomgr, \"" + window.getName() + "\");\n");
				output.write("\n");			
				for (XTestObject testObject : window.getTestObject()) {
					String name = testObject.getName();
					String className = testObject.getClassName().value();
					output.write("\t/**\n");
					output.write("\t * <b>Description:</b> " + formatDescription(getEmptyIfNull(testObject.getDescription())) + "<br/><br/>\n");
					output.write("\t * <b>Driver:</b> " + getEmptyIfNull(testObject.getDriverName().value()) + "<br/>\n");
					output.write("\t * <b>Class:</b> " + getEmptyIfNull(testObject.getClassName().value()) + "<br/>\n");
					if (testObject.getDriverName().equals(XDriverName.SIKULI)) {
						if (testObject.getImage().size() > 0) {
							output.write("\t * <br/>\n");
							for (XImage img : testObject.getImage()) {
								if (img.getFileName() != null) output.write("\t * <b>File:</b> " + img.getFileName() + "<br/>\n");
								if (img.getSimilarity() != null) 
									output.write("\t * <b>Similarity:</b> " + img.getSimilarity() + "<br/>\n");
								int ox = img.getOffsetX() != null ? img.getOffsetX() : 0;
								int oy = img.getOffsetY() != null ? img.getOffsetY() : 0;
							
								output.write("\t * <b>Offset: </b> " + ox + ":" + oy + "<br/>\n");
								output.write("\t * <b>Tags: </b> " + getEmptyIfNull(img.getTagUids()) + "<br/>\n"); //TODO: uidy zamienic na nazwy tagow
								if (img.isImgRecognition()) {
									//Image png = new Image(tomApp.getShell().getDisplay(), img.getPath());
									//Rectangle pngRec = png.getBounds();
									if (img.getFileName().toLowerCase().endsWith(".png")) {
										String imgPath = getAbsoluteResPath() + "\\" + img.getFileName();
										String imgRelativePath = "{@docRoot}\\..\\" + getResPath() + "\\" + img.getFileName();
										imgRelativePath = imgRelativePath.replace("\\", "/");
										Rectangle pngRec = getImageRectangle(imgPath);
										int w = pngRec.width;
										int h = pngRec.height;	
										int offsetX = w/2 - 5;
										int offsetY = h/2 - 5;											
										
										if (img.getOffsetX() != null) offsetX = w/2 + img.getOffsetX() - 5;
										if (img.getOffsetY() != null) offsetY = h/2 + img.getOffsetY() - 5;
																				
										// get shift
										int shiftX =0, shiftY=0, shiftW=0, shiftH = 0;
										if (img.getShift()!=null){
											String strRectangle = img.getShift();
											strRectangle = strRectangle.substring(11, strRectangle.length() - 1);
											String [] array = strRectangle.split(",");
											
											shiftX = Integer.parseInt(array[0].trim());
											shiftY = Integer.parseInt(array[1].trim());
											shiftW = Integer.parseInt(array[2].trim());
											shiftH = Integer.parseInt(array[3].trim());											
											
										}
										
										// calculate size form and offset
										int sizeXMainRectangle =(offsetX<0) ? w + Math.abs(offsetX) : w;
										int sizeYMainRectangle = (offsetY<0) ? h + Math.abs(offsetY) : h;
										
										int sizeXShiftRectangle = (offsetX<0) ? shiftW+shiftX+Math.abs(offsetX) : shiftW+shiftX;										
										int sizeYShiftRectangle = (offsetY<0) ? shiftH+shiftY+Math.abs(offsetY) : shiftH+shiftY;		
										
										int formX = (sizeXMainRectangle > sizeXShiftRectangle) ? sizeXMainRectangle : sizeXShiftRectangle;
										int formY = (sizeYMainRectangle > sizeYShiftRectangle) ? sizeYMainRectangle : sizeYShiftRectangle;
																				
										if(offsetX>formX && offsetY<formY){
											formX = offsetX+10;
										}else if(offsetX<formX && offsetY>formY){
											formY = offsetY+10;
										}
										
										// set margin if point offset is negative
										int marginXMain = (offsetX<0) ? Math.abs(offsetX) : 0;
										int marginYMain = (offsetY<0) ? Math.abs(offsetY) : 0;
										
										shiftX = (offsetX<0) ? shiftX + Math.abs(offsetX) : shiftX;
										shiftY = (offsetY<0) ? shiftY + Math.abs(offsetY) : shiftY;
										
										output.write("\t * <div style=\"overflow:visible;width:"+formX+"px;height:"+formY+"px;border:1px solid #e3e3c8;\">\n");
										output.write("\t * <div style=\"border:1px solid #ff00ff;width:"+shiftW+"px;height:"+shiftH+"px;margin-top:"+shiftY+"px;margin-left:"+shiftX+"px;position:absolute;z-index:2\"></div>\n");
										output.write("\t * <div style=\"background: url('" + imgRelativePath + "') no-repeat;width:" + w + "px;height:" + h + "px;margin-left:"+marginXMain+"px;margin-top:"+marginYMain+"px;z-index:1;border:1px solid #0000ff;\">\n");
										output.write("\t * <img style=\"margin-left:" + offsetX + "px;margin-top:" + offsetY + "px;position:absolute;z-index:3;\" src=\"" + getPointPath()  + "\"></div>\n");
										output.write("\t * </div>\n");
										output.write("\t * <br/>\n");									
									
									}
								} else {
									output.write("\t * <b>OCR:</b> " + img.getOcrText() + "<br/>\n");
								}	
							}
						}
					} else
						output.write("\t * <b>Target:</b> " + testObject.getTarget() + "<br/>\n");
					output.write("\t */\n");
					output.write("\tprotected " + className + " " + name + " = get" + className + "(tomgr, " + window.getName() + ", \"" + name + "\");\n");
					
					output.write("\n");
				}
			}
			output.write("}\n");
			
			output.close();
		} catch ( IOException e ) {
            e.printStackTrace();
        }
	}
	
	private Rectangle getImageRectangle(String imagePath) {
		BufferedImage readImage = null;

		try {
		    readImage = ImageIO.read(new File(imagePath));
		    return new Rectangle(readImage.getWidth(), readImage.getHeight());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private String currentTime() {
		SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");    
		Date date = new Date(System.currentTimeMillis());
		return formater.format(date);
	}
	
	private String getEmptyIfNull(String str) {
		return str != null ? str : "";
	}
	
	private String formatDescription(String str) {
		str = str.replace("\n", "\n\t * ");
		return str;
	}
	
	private String getPointPath() {
		//return System.getenv("FP_TAF_PATH") + "\\point.png";
		return "{@docRoot}/../res/point.png";
		
	}

}
