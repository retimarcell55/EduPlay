package connection;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import gui.ApplicationWindow;
import gui.MainWindow;
import module.ApplicationModule;
import module.games.bash.Bash;
import module.games.escape.Escape;

public class Coordinator {

	public static final String FILE_SOURCE = "C:/EduPlay";

	static MainWindow mainWindow;
	public static ApplicationWindow appWindow;
	public static ApplicationModule appModule;
	public static Callable player;


	public static Map<String, String> modules = new HashMap<String, String>();
	public static String selectedModule = null;

	public static void main(String[] args) {

		modules.put("Bash - Kockajáték", "BashInterface");
		modules.put("Fuss amíg tudsz", "EscapeInterface");
		File sourceDirectory = new File(FILE_SOURCE);
		if (!sourceDirectory.exists()) {
			new File(FILE_SOURCE).mkdir();
		}

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					mainWindow = new MainWindow();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public static void openApplication(String selected) {

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					appWindow = new ApplicationWindow();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		if (selected == "Bash - Kockajáték") {
			appModule = new Bash();
			selectedModule = "Bash - Kockajáték";
		} else if (selected == "Fuss amíg tudsz") {
			appModule = new Escape();
			selectedModule = "Fuss amíg tudsz";
		}
	}

	public static void compile(String playerTurnCode)
			throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {

		String playerDeclarationCode = "";
		File declarationFile = new File(Coordinator.FILE_SOURCE + "/Helper.txt");
		if (declarationFile.exists()) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(Coordinator.FILE_SOURCE + "/Helper.txt"));
				if (br.readLine() != null) {
					playerDeclarationCode = new Scanner(new File(Coordinator.FILE_SOURCE + "/Helper.txt")).useDelimiter("\\Z")
							.next();
				}
			} catch (FileNotFoundException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
		}

		String playerClassSourceCode = "import module.ApplicationModule;\n " + "import module.games."
				+ modules.get(selectedModule).replace("Interface", "").toLowerCase() + "." + modules.get(selectedModule)
				+ ";\n " + "import connection.Callable; " + "import java.util.*;\n "
				+ "public class Player implements Callable {\n  " + "private " + modules.get(selectedModule) + " app;\n "
				+ playerDeclarationCode + "\n@Override\n " + "public void playerTurn() {\n " + playerTurnCode + "\n}\n " + "@Override\n "
				+ "public <T extends ApplicationModule> void initializeSelectedModule( T app) {\n this.app = ("
				+ modules.get(selectedModule) + ")app; \n}\n}";

		OutputStream errorOutput = new OutputStream() {
			private StringBuilder sb = new StringBuilder();

			@Override
			public void write(int b) throws IOException {
				this.sb.append((char) b);
			}

			@Override
			public String toString() {
				return this.sb.toString();
			}
		};

		File root = new File(FILE_SOURCE);

		File sourceFile = new File(root, "Player.java");
		Files.write(sourceFile.toPath(), playerClassSourceCode.getBytes());

		JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();

		javaCompiler.run(null, null, errorOutput, sourceFile.getPath());
		if (errorOutput.toString().length() != 0) {
			appWindow.clearMessage();
			appWindow.outputMessage(errorOutput.toString());
		} else if (playerClassSourceCode.contains("System.in") || playerClassSourceCode.contains("System.out") || playerDeclarationCode.contains("import")) {
			appWindow.clearMessage();
			appWindow.outputMessage("System.in ,System.out illetve import tiltott karakterláncolatok! Képernyőre kiírás: app.print(String)");
		} else if (playerClassSourceCode.contains("app.playSelectedExercise(")) {

		} else {

			URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] { root.toURI().toURL() });
			Class<?> cls = Class.forName("Player", true, classLoader);
			Object instance = cls.newInstance();
			player = (Callable) instance;
			appModule.playSelectedExercise();
		}

	}

	public static void returnFromApplicationWindow() {
		mainWindow = new MainWindow();
	}

}
