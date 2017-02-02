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

	static MainWindow window;
	public static ApplicationWindow appwindow;
	public static ApplicationModule appmodule;
	public static Callable player;
	public static String filesource = "C:/EduPlay";

	public static Map<String, String> modules = new HashMap<String, String>();
	public static String selectedmodule = null;

	public static void main(String[] args) {

		modules.put("Bash - Kockajáték", "BashInterface");
		modules.put("Fuss amíg tudsz", "EscapeInterface");
		File f = new File(filesource);
		if (!f.exists()) {
			new File(filesource).mkdir();
		}

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					window = new MainWindow();
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
					appwindow = new ApplicationWindow();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		if (selected == "Bash - Kockajáték") {
			appmodule = new Bash();
			selectedmodule = "Bash - Kockajáték";
		} else if (selected == "Fuss amíg tudsz") {
			appmodule = new Escape();
			selectedmodule = "Fuss amíg tudsz";
		}
	}

	public static void compile(String code)
			throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {

		String helpercode = "";
		File f = new File(Coordinator.filesource + "/Helper.txt");
		if (f.exists()) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(Coordinator.filesource + "/Helper.txt"));
				if (br.readLine() != null) {
					helpercode = new Scanner(new File(Coordinator.filesource + "/Helper.txt")).useDelimiter("\\Z")
							.next();
				}
			} catch (FileNotFoundException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
		}

		String source = "import module.ApplicationModule;\n " + "import module.games."
				+ modules.get(selectedmodule).replace("Interface", "").toLowerCase() + "." + modules.get(selectedmodule)
				+ ";\n " + "import connection.Callable; " + "import java.util.*;\n "
				+ "public class Player implements Callable {\n  " + "private " + modules.get(selectedmodule) + " app;\n "
				+ helpercode + "\n@Override\n " + "public void yourTurn() {\n " + code + "\n}\n " + "@Override\n "
				+ "public <T extends ApplicationModule> void initialize( T app) {\n this.app = ("
				+ modules.get(selectedmodule) + ")app; \n}\n}";

		OutputStream erroroutput = new OutputStream() {
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

		File root = new File(filesource);

		File sourceFile = new File(root, "Player.java");
		Files.write(sourceFile.toPath(), source.getBytes());

		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

		compiler.run(null, null, erroroutput, sourceFile.getPath());
		if (erroroutput.toString().length() != 0) {
			appwindow.clearMessage();
			appwindow.outputMessage(erroroutput.toString());
		} else if (source.contains("System.in") || source.contains("System.out") || helpercode.contains("import")) {
			appwindow.clearMessage();
			appwindow.outputMessage("System.in ,System.out illetve import tiltott karakterláncolatok! Képernyőre kiírás: app.print(String)");
		} else if (source.contains("app.play(")) {

		} else {

			URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] { root.toURI().toURL() });
			Class<?> cls = Class.forName("Player", true, classLoader);
			Object instance = cls.newInstance();
			player = (Callable) instance;
			appmodule.play();
		}

	}

	public static void returning() {
		window = new MainWindow();
	}

}
