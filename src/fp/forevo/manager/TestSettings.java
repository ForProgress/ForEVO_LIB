package fp.forevo.manager;

public class TestSettings {
		private static Status test_status=Status.PASSED;
		private static String processName = "brak";
		private static int idTestData = -1;
		private static int idBusinessProcess=1;
		private static String businessProcess="";
		private static int idrun=-1;
		private static int iteration=0;
		private static String reportDir="Reports\\All";
		private static String screenShotsDir="Screenshots";
		private static boolean isResultsTemporarily=true;
		private static boolean processStarted=false;
		private static boolean continueOnError=false;

		

		/**Czy wyniki maja byc zapisywane tymczasowo? Np przez robot framework w katalogu tmp.
		 * 
		 * @return 
		 * @true raportow nie da sie przeniesc gdyz zawieraja sciezki bezwzgledne
		 * 
		 * @false raporty mozna dowolnie przenosic. Konieczne jest wywolanie akcji startProcess
		 * 		   ze zdefiniowana sciezka raportow. Tylko wtedy uzywane sa sciezki wzgledne
		 */
		public static boolean isResultsTemporarily() {
			return isResultsTemporarily;
		}

		public static void setResultsTemporarily(boolean isResultsTemporarily) {
			TestSettings.isResultsTemporarily = isResultsTemporarily;
		}

		public static void clearTMPSettings(){
			test_status=Status.PASSED;
			processName = "brak";
			idTestData = -1;
			idBusinessProcess=1;
			businessProcess="";
			idrun=-1;
			processStarted=false;
		}
		
		public static Status getTest_status() {
			return test_status;
		}

		public static void setTest_status(Status test_status) {
			TestSettings.test_status = test_status;
		}
		
		public static int getIdrun() {
			return idrun;
		}

		public static void setIdrun(int newidrun) {
			idrun = newidrun;
		}

		public static int getIdBusinessProcess() {
			return idBusinessProcess;
		}

		public static void setIdBusinessProcess(int newidBusinessProcess) {
			idBusinessProcess = newidBusinessProcess;
		}

		public static String getBusinessProcess() {
			return businessProcess;
		}

		public static void setBusinessProcess(String newbusinessProcess) {
			businessProcess = newbusinessProcess;
		}
		public static void setIdTestData(int newidTestData) {
			idTestData = newidTestData;
		}
		
		public static int getIdTestData() {
			return idTestData;
		}
		
		public static void setProcessName(String newprocessName) {
			processName = newprocessName;
		}
		
		public static String getProcessName() {
			return processName;
		}
		
		
		public static void setReportDir(String reportDir) {
			TestSettings.reportDir = reportDir;
		}

		public static String getReportPath() {
			return System.getProperty("user.dir").replace("\\", "/")+"/"+reportDir;
		}
		public static String getScreenShotsPath() {
			return System.getProperty("user.dir").replace("\\", "/")+"/"+reportDir+"/"+screenShotsDir;
		}
		public static String getReportDir() {
			return reportDir.replace("\\", "/");
		}
		public static String getScreenShotsDir() {
			return screenShotsDir.replace("\\", "/");
		}

		public static int getIteration() {
			return iteration;
		}

		public static void setIteration(int iteration) {
			TestSettings.iteration = iteration;
		}

		public static boolean isProcessStarted() {
			return processStarted;
		}

		public static void setProcessStarted(boolean processStarted) {
			TestSettings.processStarted = processStarted;
		}

		public static boolean isContinueOnError() {
			return continueOnError;
		}

		public static void setContinueOnError(boolean continueOnError) {
			TestSettings.continueOnError = continueOnError;
		}

		
}
