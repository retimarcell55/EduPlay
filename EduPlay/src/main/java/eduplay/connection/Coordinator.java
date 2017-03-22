package eduplay.connection;

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

import eduplay.gui.ApplicationWindow;
import eduplay.gui.MainWindow;
import eduplay.module.ApplicationModule;
import eduplay.module.games.bash.Bash;
import eduplay.module.games.escape.Escape;

public class Coordinator {

	public static final String FILE_SOURCE = "UserData";

	static MainWindow mainWindow;
	public static ApplicationWindow appWindow;
	public static ApplicationModule appModule;
	public static Callable player;


	public static Map<String, String> modules = new HashMap<>();
	public static String selectedModule = null;

	public static void main(String[] args) {

		initializeModules();
		initializeUserDataDirectory();
		createMainWindow();

	}

	private static void initializeModules() {
		modules.put("Bash - Kockajáték", "BashInterface");
		modules.put("Fuss amíg tudsz", "EscapeInterface");
	}

	private static void initializeUserDataDirectory() {
		File sourceDirectory = new File(FILE_SOURCE);
		if (!sourceDirectory.exists()) {
			new File(FILE_SOURCE).mkdir();
		}
	}

	private static void createMainWindow() {
		EventQueue.invokeLater(() -> {
			try {
				mainWindow = new MainWindow();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
	private static void createApplicationWindow() {
		EventQueue.invokeLater(() -> {
			try {
				appWindow = new ApplicationWindow();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	public static void openApplication(String selected) {

		createApplicationWindow();

		if (selected == "Bash - Kockajáték") {
			appModule = new Bash();
			selectedModule = "Bash - Kockajáték";
		} else if (selected == "Fuss amíg tudsz") {
			appModule = new Escape();
			selectedModule = "Fuss amíg tudsz";
		}
	}

	public static void compileAndStartExercise()
			throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {

		String playerClassSourceCode = getPlayerClassCode();

		OutputStream errorOutput = createOutputStream();

		File root = new File(FILE_SOURCE);

		File sourceFile = new File(root, "Player.java");
		Files.write(sourceFile.toPath(), playerClassSourceCode.getBytes());

		JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();

		javaCompiler.run(null, null, errorOutput, sourceFile.getPath());
		if (errorOutput.toString().length() != 0) {
			appWindow.clearMessage();
			appWindow.outputMessage(errorOutput.toString());
		} else if (playerClassSourceCode.contains("System.in") || playerClassSourceCode.contains
				("System.out") || getPlayerDeclarationCode().contains("import")) {
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

	private static String getPlayerDeclarationCode() {
		String declarationCode = "";
		File declarationFile = new File(Coordinator.FILE_SOURCE + "/Helper.txt");
		if (declarationFile.exists()) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(Coordinator.FILE_SOURCE + "/Helper.txt"));
				if (br.readLine() != null) {
					declarationCode = new Scanner(new File(Coordinator.FILE_SOURCE + "/Helper" +
							".txt")).useDelimiter("\\Z")
							.next();
				}
			} catch (FileNotFoundException e2) {
				Coordinator.appWindow.outputMessage("Nem található a deklarációs kódot tartalmazó" +
						" fájl");
				e2.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return declarationCode;
	}

	private static String getPlayerActionCode() {
		return appWindow.getPlayerActionCode();
	}


	private static String getPlayerClassCode() {

		StringBuilder playerClassSourceCode = new StringBuilder();
		playerClassSourceCode.append("import eduplay.module.ApplicationModule;\n ");
		playerClassSourceCode.append("import eduplay.module.games.");
		playerClassSourceCode.append(modules.get(selectedModule).replace("Interface", "")
				.toLowerCase());
		playerClassSourceCode.append("." + modules.get(selectedModule)
				+ ";\n ");
		playerClassSourceCode.append("import eduplay.connection.Callable; ");
		playerClassSourceCode.append("import java.util.*;\n ");
		playerClassSourceCode.append("public class Player implements Callable {\n  " );
		playerClassSourceCode.append("private " + modules.get(selectedModule) + " app;\n ");
		playerClassSourceCode.append(getPlayerDeclarationCode());
		playerClassSourceCode.append("\n@Override\n public void playerAction() {\n ");
		playerClassSourceCode.append(getPlayerActionCode());
		playerClassSourceCode.append("\n}\n @Override\n ");
		playerClassSourceCode.append("public <T extends ApplicationModule> void " +
				"initializeSelectedModule( T app) {\n this.app = (");
		playerClassSourceCode.append(modules.get(selectedModule) + ")app; \n}\n}");

		return playerClassSourceCode.toString();
	}

	private static OutputStream createOutputStream() {
		return new OutputStream() {
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
	}

	public static void returnFromApplicationWindow() {
		mainWindow = new MainWindow();
	}

}
