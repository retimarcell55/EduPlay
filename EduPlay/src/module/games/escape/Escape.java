package module.games.escape;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import connection.Callable;
import connection.Coordinator;
import gui.MainWindow;
import module.games.escape.Entity.Position;

public class Escape implements EscapeInterface {

	// A feladatok változói
	private ArrayList<String> exercises = new ArrayList<String>();
	private String actualexercise;

	// A játék változói
	private char[][] gameboard;
	private Callable player;
	private ArrayList<Entity> entities;
	private ArrayList<Environment> environmentobjects;
	int turncount;
	GameWindow gamewindow;
	int boardwidth;
	int boardheight;
	boolean playerturnend;
	int maxturn;
	Random rnd;

	// Hol van a zombi? <ciklus , mátrixkezelés>
	int zombiposrow;
	int zombiposcolumn;

	// ha fordítás közben futás idejű hiba van
	private boolean runtimeerror;

	public Escape() {
		exercises.add("Hol van a zombi? <ciklus , mátrixkezelés>");
		exercises.add("Juss el a célig a lehető legkevesebb lépésből! <ciklus>");
		exercises.add("Juss el a célig a lehető legkevesebb lépésből! <rekurzió>");
		exercises.add("Juss ki élve - alapok!");
		exercises.add("Test");
		actualexercise = exercises.get(0);
		rnd = new Random();
	}

	private void initialize() {

		turncount = 0;
		runtimeerror = false;
		playerturnend = false;
		maxturn = 50;

		Coordinator.appwindow.clearMessage();
		entities = new ArrayList<Entity>();
		environmentobjects = new ArrayList<Environment>();

		if (actualexercise == "Hol van a zombi? <ciklus , mátrixkezelés>") {
			boardwidth = rnd.nextInt(9) + 2;
			boardheight = rnd.nextInt(9) + 2;
		}
		if (actualexercise == "Juss el a célig a lehető legkevesebb lépésből! <rekurzió>") {
			boardwidth = rnd.nextInt(9) + 2;
			boardheight = rnd.nextInt(9) + 2;
		}
		if (actualexercise == "Juss el a célig a lehető legkevesebb lépésből! <ciklus>") {
			boardwidth = rnd.nextInt(9) + 2;
			boardheight = rnd.nextInt(9) + 2;
		} else if (actualexercise == "Juss ki élve - alapok!") {
			boardwidth = 3;
			boardheight = 3;
			PlayerEntity hero = new PlayerEntity("Player", 0, 0);
			hero.equip(0, 0, 1);
			entities.add(hero);
			entities.add(new ZombieEntity("Zombi", 0, 2));
			environmentobjects.add(new Exit(2, 2));

		} else if (actualexercise == "Test") {
			FileReader fileReader = null;
			PlayerEntity hero = null;
			try {
				fileReader = new FileReader(new File(Coordinator.filesource + "/Test.txt"));

				BufferedReader br = new BufferedReader(fileReader);

				hero = new PlayerEntity("Player", 0, 0);
				entities.add(hero);

				// Milyen felszerelése van a hősnek(sprint,ammo,axe)
				String firstline = br.readLine();

				hero.equip(Integer.parseInt(firstline.split("\\s+")[0]), Integer.parseInt(firstline.split("\\s+")[1]),
						Integer.parseInt(firstline.split("\\s+")[2]));

				// Milyen nagy a pálya (sor,oszlop)
				String secondline = br.readLine();

				boardheight = Integer.parseInt(secondline.split("\\s+")[0]);
				boardwidth = Integer.parseInt(secondline.split("\\s+")[1]);
				
				int zombiecount = 1;
				
				//A pálya
				for (int i = 0; i < boardheight; i++) {
					
					String actualline = br.readLine();
					for (int j = 0; j < boardwidth; j++) {
						if(actualline.split("\\s+")[j].equals("p")) {
							hero.setPosrow(i);
							hero.setPoscolumn(j);
						} else if(actualline.split("\\s+")[j].equals("z")) {
							entities.add(new ZombieEntity("Zombi" + zombiecount, i, j));
							zombiecount++;
						} else if(actualline.split("\\s+")[j].equals("w")) {
							environmentobjects.add(new Wall(i, j));
						} else if(actualline.split("\\s+")[j].equals("e")) {
							environmentobjects.add(new Exit(i, j));
						}
					}
				}
				
				br.close();

			} catch (FileNotFoundException e) {
				Coordinator.appwindow.outputMessage(e.getMessage());
			} catch (IOException e) {
				Coordinator.appwindow.outputMessage(e.getMessage());
			}
		}
		gameboard = new char[boardheight][boardwidth];
		gamewindow = new GameWindow(boardheight, boardwidth);
		fillBoard();
		gamewindow.drawBoard(gameboard);
	}

	@Override
	public String getName() {
		return "Fuss amíg tudsz";
	}

	@Override
	public String getRules() {
		return "A játék célja, hogy az adott pályán eljussunk a célig, azaz ellépkedjünk a kijáratot ábrázoló mezőig.\n"
				+ "A pályát környezeti elemek és zombik tarkítják, utóbbiaknak célja, hogy a játékost elkapják és megegyék.\n"
				+ "A játékos egy körben egy mezőt léphet, akár átlósan is, vagy cselekedhet (például lő, fejszét használ, sprintel)\n"
				+ "vagy várhat és akkor karaktere egy helyben marad. Minden körben csak egy dolgot tehetünk!\n"
				+ "A játék véget ér, ha a karakter rálép a kijáratot ábrázoló mezőre, ekkor megnyertük a játékot\n"
				+ "vagy egy zombihoz ér, azaz vagy egy zombi rálép, vagy a játékos lép rá egyre. Ekkor a játékos vesztett.\n"
				+ "A zombik természetesen megpróbálnak egyre közelebb jutani a játékoshoz, egy körben egy mezőt lépnek.\n"
				+ "Egyes szinteken használhatunk felszereléseket, amik megkönnyíthetik utunkat a célig:\n"
				+ "A revolverrel egy egyenes vonalban lőhetünk, akár átlósan is. Ha a lövedék egy zombit ér, akkor azt eltünteti a játéktérről.\n"
				+ "A fejszével egy mellettünk álló zombit szabadíthatunk meg a fejétől, akár átlósan is,\n"
				+ "illetve, ha egy fal az utunkban áll, azt lebonthatjuk vele.\n"
				+ "Lehetőségünk lesz egyes szinteken a sprint képességünket használni, amellyel egyszerre 2 blokkot mozoghatunk.\n"
				+ "Az erőforrásaint természetesen korlátozottak, a fegyver töltényt használ,\n"
				+ "a fejsze elhasználódik egy ütés után, a sprinteléstől kifáradunk stb. Minden eszköz mennyisége lekérhető az API segítségével.\n"
				+ "A pályán a játékoson és a zombikon kívül más környezeti elemek is lehetnek, mint például más áldozatok,\n"
				+ "akiket mondjuk a zombik előbb kapnak el mint minket vagy falak stb. Az aktuális feladat leírását mindig tüzetesen vizsgáljuk át!\n"
				+ "A pálya egy karakter mátrixban van tárolva, ami ugyancsak lekérhető. A benne szereplő főbb szimbólumok:\n"
				+ " - : semmi , z : zombi , p : játékos , e : kijárat. , w : fal." + "\n"
				+ "Az alapértelmezett ágens minden körben vár és nem csinál semmit!\n";
	}

	@Override
	public String getAPI() {
		String tmp = "";
		if (actualexercise == "Hol van a zombi? <ciklus , mátrixkezelés>") {
			tmp = "char[][] getBoard() -> A pálya karakter mátrixát kérhetjük le a függvénnyel\n"
					+ "void zombiLocation(int row , int column) -> Ezzel a függvénnyel adhatod meg, hogy szerinted hol van a zombi az adott körben.\n"
					+ "Ne feledd, a függvényeket az app tagváltozón keresztül éred el! (pl : app.getBoard()) \n"
					+ "A saját függvényeidet természetesen az app változó nélkül éred el (pl. this.MyFunc()) \n" + "\n"
					+ "A pálya 0,0 pontja a bal felső sarokban lévő cella. Vigyázzunk az indexelés 0 - val kezdődik!"
					+ "Ha várni szeretnénk, akkor az adott körben nem csináljunk semmit!";
		} else if (actualexercise == "Juss el a célig a lehető legkevesebb lépésből! <ciklus>"
				|| actualexercise == "Juss el a célig a lehető legkevesebb lépésből! <rekurzió>") {
			tmp = "char[][] getBoard() -> A pálya karakter mátrixát kérhetjük le a függvénnyel\n"
					+ "void moveToCell(int sor, oszlop) -> A karakterünk mozgatása egy cellával arrébb\n"
					+ "int getPositionRow() -> Az aktuális pozíciónk első koordinátája, hogy hanyadik sorban vagyunk\n"
					+ "int getPositionColumn() ->  Az aktuális pozíciónk második koordinátája, hogy hanyadik oszlopban vagyunk\n"
					+ "Ne feledd, a függvényeket az app tagváltozón keresztül éred el! (pl : app.getBoard()) \n"
					+ "A saját függvényeidet természetesen az app változó nélkül éred el (pl. this.MyFunc()) \n" + "\n"
					+ "A pálya 0,0 pontja a bal felső sarokban lévő cella. Vigyázzunk az indexelés 0 - val kezdődik!"
					+ "Ha várni szeretnénk, akkor az adott körben nem csináljunk semmit!";
		} else if (actualexercise == "Juss ki élve - alapok!") {
			tmp = "void moveToCell(int sor, oszlop) -> A karakterünk mozgatása egy cellával arrébb\n"
					+ "int getPositionRow() -> Az aktuális pozíciónk első koordinátája, hogy hanyadik sorban vagyunk\n"
					+ "int getPositionColumn() ->  Az aktuális pozíciónk második koordinátája, hogy hanyadik oszlopban vagyunk\n"
					+ "char[][] getBoard() -> A pálya karakter mátrixát kérhetjük le a függvénnyel\n"
					+ "void axeAttack(String direction) -> Fejszecsapás egy megadott irányba, lehetséges irányok:\n"
					+ "UP,DOWN,RIGHT,LEFT,UPLEFT,UPRIGHT,DOWNLEFT,DOWNRIGHT. Felhasználásra példa,ütés fölfele: app.axeAttack(\"UP\")\n"
					+ "void shoot(String direction) -> Lövés egy megadott irányba, lehetséges irányok:\n"
					+ "UP,DOWN,RIGHT,LEFT,UPLEFT,UPRIGHT,DOWNLEFT,DOWNRIGHT. Felhasználásra példa,lövés jobbra: app.shoot(\"RIGHT\")\n"
					+ "int getAxe() -> Lekérheted vele hány darab fejszéd van még\n"
					+ "int getSprint() -> Lekérheted, hogy hányszor tudod még a futást alkalmazni\n"
					+ "int getAmmo() -> Lekérheted, hogy a fegyveredben hány töltény van még\n"
					+ "Ne feledd, a függvényeket az app tagváltozón keresztül éred el! (pl : app.getBoard()) \n"
					+ "A saját függvényeidet természetesen az app változó nélkül éred el (pl. this.MyFunc()) \n" + "\n"
					+ "A pálya 0,0 pontja a bal felső sarokban lévő cella. Vigyázzunk az indexelés 0 - val kezdődik!"
					+ "Ha várni szeretnénk, akkor az adott körben nem csináljunk semmit!";
		} else if (actualexercise == "Test") {
			tmp = "void moveToCell(int sor, oszlop) -> A karakterünk mozgatása egy cellával arrébb\n"
					+ "int getPositionRow() -> Az aktuális pozíciónk első koordinátája, hogy hanyadik sorban vagyunk\n"
					+ "int getPositionColumn() ->  Az aktuális pozíciónk második koordinátája, hogy hanyadik oszlopban vagyunk\n"
					+ "char[][] getBoard() -> A pálya karakter mátrixát kérhetjük le a függvénnyel\n"
					+ "void axeAttack(String direction) -> Fejszecsapás egy megadott irányba, lehetséges irányok:\n"
					+ "UP,DOWN,RIGHT,LEFT,UPLEFT,UPRIGHT,DOWNLEFT,DOWNRIGHT. Felhasználásra példa,ütés fölfele: app.axeAttack(\"UP\")\n"
					+ "void shoot(String direction) -> Lövés egy megadott irányba, lehetséges irányok:\n"
					+ "UP,DOWN,RIGHT,LEFT,UPLEFT,UPRIGHT,DOWNLEFT,DOWNRIGHT. Felhasználásra példa,lövés jobbra: app.shoot(\"RIGHT\")\n"
					+ "int getAxe() -> Lekérheted vele hány darab fejszéd van még\n"
					+ "int getSprint() -> Lekérheted, hogy hányszor tudod még a futást alkalmazni\n"
					+ "int getAmmo() -> Lekérheted, hogy a fegyveredben hány töltény van még\n"
					+ "void sprintToCell(int row, int column) -> Ha van még sprint lehetőséged, akkor akár 2 mező távolságra is léphetsz vele\n"
					+ "Ne feledd, a függvényeket az app tagváltozón keresztül éred el! (pl : app.getBoard()) \n"
					+ "A saját függvényeidet természetesen az app változó nélkül éred el (pl. this.MyFunc()) \n" + "\n"
					+ "A pálya 0,0 pontja a bal felső sarokban lévő cella. Vigyázzunk az indexelés 0 - val kezdődik!"
					+ "Ha várni szeretnénk, akkor az adott körben nem csináljunk semmit!";
		}
		return tmp;
	}

	@Override
	public String getDescription() {
		String tmp = "";
		if (actualexercise == "Hol van a zombi? <ciklus , mátrixkezelés>") {
			tmp = "A feladat lényege, hogy minden körben megállapítsuk, melyik cellában van a zombi!\n"
					+ "Minden körben valahova le lesz rakva egy zombi a pályára, a pálya mérete véletlenszerű\n"
					+ "Minden körben találjuk meg a zombit és a zombiPosition(sor,oszlop) függvénnyel küldhetjük el koordinátáit a rendszernek\n"
					+ "Az alapértelmezett ágens mindig a (0,0) helyet fogja tippelni.\n"
					+ "Vajon minden körben megtaláljuk a zombi helyét?";
		}
		if (actualexercise == "Juss el a célig a lehető legkevesebb lépésből! <ciklus>") {
			tmp = "A feladat lényege, hogy a lehető legkevesebb lépésből eljussunk a kijárathoz.\n"
					+ "A játék elindítása után egy véletlenszerű méretű pálya generálódik,\n"
					+ "amin véletlenszerűen van elhelyezve a játékos illetve a menekülést jelentő kijárat.\n"
					+ "Pont annyi körünk van elérni a kijáratig, amennyi kör alatt a legkevesebb lépésből eljutnánk oda.\n"
					+ "Más akadály apályán nem lesz elhelyezve!.\n" + "Vajon ki tudunk jutni időben?\n";
		} else if (actualexercise == "Juss el a célig a lehető legkevesebb lépésből! <rekurzió>") {
			tmp = "A feladat lényege, hogy a lehető legkevesebb lépésből eljussunk a kijárathoz.\n"
					+ "A játék elindítása után egy véletlenszerű méretű pálya generálódik,\n"
					+ "amin véletlenszerűen van elhelyezve a játékos illetve a menekülést jelentő kijárat.\n"
					+ "A pályán véletlenszerű mennyiségű fal lesz elhelyezve, amin nem tudunk átmenni.\n"
					+ "A feladat mindig úgy generálódik, hogy megoldható legyen!\n"
					+ "Pont annyi körünk van elérni a kijáratig, amennyi kör alatt a legkevesebb lépésből eljutnánk oda.\n"
					+ "Vajon ki tudunk jutni időben?\n";
		} else if (actualexercise == "Juss ki élve - alapok!") {
			tmp = "A gyakorló pályán, ami 3x3 - as, egy darab zombi lesz lerakva a jobb felső sarokba.\n"
					+ "A kijárat a jobb alsó sarokban lesz. A felszerelésed 1 fejsze!\n"
					+ "Juss el a kijáratig , anélkül, hogy a zombi megenne! Vigyázz a zombi mindig közelíteni fog!\n";
		}
		return tmp;
	}

	@Override
	public String getHelp() {
		String tmp = "";
		if (actualexercise == "Hol van a zombi? <ciklus , mátrixkezelés>") {
			tmp = "A pálya mérete és a zombi helye nem statikus.\n "
					+ "Legegyszerűbb ha lekérjük a pálya mátrixát és végiglépkedünk minden mezőn, amíg meg nem találjuk a zombit.";
		}
		if (actualexercise == "Juss el a célig a lehető legkevesebb lépésből! <ciklus>") {
			tmp = "A pálya mérete és a zombi helye nem statikus.\n "
					+ "Első sorban kérjük le a pálya mátrixát és keressük meg rajta, hogy hol van a kijárat! ('e')\n"
					+ "Találjuk ki az algoritmust ami mindig a lehető legrövidebb utat adja!\n"
					+ "Az átlós lépés egy oszlop és egy sor lépését váltja ki egyetlen kör alatt!\n";
		}
		if (actualexercise == "Juss el a célig a lehető legkevesebb lépésből! <rekurzió>") {
			tmp = "A pálya mérete és a zombi helye nem statikus.\n "
					+ "Első sorban kérjük le a pálya mátrixát és keressük meg rajta, hogy hol van a kijárat! ('e')\n"
					+ "Találjuk ki az algoritmust ami mindig a lehető legrövidebb utat adja!\n"
					+ "Szükségünk lesz egy rekurzív függvény megírására, ami legrövidebb utat számol!\n"
					+ "Gondoljunk a pályára mint gráf csúcsokra. Ahol fal van, ott nincs él a gráf csúcsai között!\n"
					+ "Az átlós lépés egy oszlop és egy sor lépését váltja ki egyetlen kör alatt!\n";
		} else if (actualexercise == "Juss ki élve - alapok!") {
			tmp = "Nem mindig az a legjobb megoldás, ha folyamatosan a cél felé haladunk, gondoljuk át a helyzetet nyugalomban!";
		}
		return tmp;
	}

	@Override
	public void play() {

		initialize();

		this.player = Coordinator.player;
		player.initialize(this);

		// JÁTÉKCIKLUS!
		new SwingWorker() {

			@Override
			protected Object doInBackground() throws Exception {

				if (actualexercise == "Hol van a zombi? <ciklus , mátrixkezelés>") {
					gameLoop1();
				} else if (actualexercise == "Juss el a célig a lehető legkevesebb lépésből! <ciklus>"
						|| actualexercise == "Juss el a célig a lehető legkevesebb lépésből! <rekurzió>") {
					gameLoop2();
				} else {
					gameLoop();
				}
				return null;
			}

		}.execute();
		if (runtimeerror) {
			Coordinator.appwindow.outputMessage("FUTÁS IDEJŰ HIBA TÖRTÉNT, A JÁTÉK LEÁLL!");
			return;
		}

	}

	private void intelligence() {
		for (Entity entity : entities) {
			fillBoard();
			if (entity instanceof ZombieEntity) {
				((ZombieEntity) entity).intelligence(gameboard, entities.get(0).getPosrow(),
						entities.get(0).getPoscolumn());
			}
		}
	}

	@Override
	public void print(String s) {
		Coordinator.appwindow.outputMessage("<PLAYER> " + s);

	}

	@Override
	public ArrayList<String> getExercises() {
		return exercises;
	}

	@Override
	public void setExercise(String ex) {
		for (String item : exercises) {
			if (item == ex) {
				actualexercise = item;
			}
		}

	}

	@Override
	public String getactualExercise() {
		return actualexercise;
	}

	// A gameboard játékteret tölti fel char elemekkel
	private void fillBoard() {

		for (int i = 0; i < gameboard.length; i++) {
			for (int j = 0; j < gameboard[i].length; j++) {
				gameboard[i][j] = '-';
			}
		}

		for (Entity entity : entities) {
			if (entity instanceof PlayerEntity) {
				gameboard[entity.getPosrow()][entity.getPoscolumn()] = 'p';
			} else if (entity instanceof ZombieEntity) {
				gameboard[entity.getPosrow()][entity.getPoscolumn()] = 'z';
			}
		}

		for (Environment env : environmentobjects) {
			if (env instanceof Exit) {
				gameboard[env.getPosrow()][env.getPoscolumn()] = 'e';
			}
			if (env instanceof Wall) {
				gameboard[env.getPosrow()][env.getPoscolumn()] = 'w';
			}
		}
	}

	// Két entitás egymára lépett-e
	private <T extends Entity> boolean isObjectsCollide(T e1, T e2) {
		if ((e1.getPosrow() == e2.getPosrow()) && (e1.getPoscolumn() == e2.getPoscolumn())) {
			return true;
		} else {
			return false;
		}
	}

	// Egy entitás az objektumra lépett-e
	private <E extends Entity, T extends Environment> boolean isObjectsCollide(E e1, T e2) {
		if ((e1.getPosrow() == e2.getPosrow()) && (e1.getPoscolumn() == e2.getPoscolumn())) {
			return true;
		} else {
			return false;
		}
	}

	// A player elérte a kijáratot?
	private boolean isExitReached() {
		if (isObjectsCollide(entities.get(0), environmentobjects.get(0))) {
			return true;
		} else {
			return false;
		}
	}

	// Ha egy zombi a Playerre lép vagy fordítva akkor a zombi megeszi, és a
	// játéknak vége
	private boolean isPlayerEaten() {
		for (int i = 1; i < entities.size(); i++) {
			if (isObjectsCollide(entities.get(0), entities.get(i))) {
				return true;
			}
		}
		return false;
	}

	// Egy lépés ténylegesen egy cellát mozgatott-e
	private boolean isOneMove(Entity entity, int row, int column) {
		List<Position> neighbours = Entity.getNeighbour(entity.new Position(entity.getPosrow(), entity.getPoscolumn()),
				gameboard.length, gameboard[0].length);
		for (Position position : neighbours) {
			if (position.row == row && position.col == column) {
				return true;
			}
		}
		return false;
	}

	// Egy sprint max két cellát mozgatott-e
	private boolean isTwoMove(Entity entity, int row, int column) {
		if (Math.max(Math.abs(row - entity.getPosrow()), Math.abs(column - entity.getPoscolumn())) == 1
				|| Math.max(Math.abs(row - entity.getPosrow()), Math.abs(column - entity.getPoscolumn())) == 2) {
			return true;
		}
		return false;
	}

	@Override
	public void moveToCell(int row, int column) {
		if (actualexercise == "Hol van a zombi? <ciklus , mátrixkezelés>") {

		} else {
			if (playerturnend) {
				Coordinator.appwindow.outputMessage("<JÁTÉK> A karaktered ebben a körben már cselekedett!");
				return;
			}

			if (!isOnTheBoard(row, column)) {
				Coordinator.appwindow.outputMessage("<JÁTÉK> A lépés kimutat a játéktérből!");
			} else if (!isOneMove(entities.get(0), row, column)) {
				Coordinator.appwindow.outputMessage("<JÁTÉK> A lépés nem egy mezőre volt az aktuális pozíciódtól!");
			} else if (gameboard[row][column] == 'w') {
				Coordinator.appwindow.outputMessage("Falon nem tudsz átmenni!");
			} else {
				playerturnend = true;
				Coordinator.appwindow
						.outputMessage(entities.get(0).getName() + " lépése: (" + entities.get(0).getPosrow() + ","
								+ entities.get(0).getPoscolumn() + ") -> (" + row + "," + column + ")");
				entities.get(0).setPosrow(row);
				entities.get(0).setPoscolumn(column);
			}
		}
	}

	@Override
	public void sprintToCell(int row, int column) {

		if (playerturnend) {
			Coordinator.appwindow.outputMessage("<JÁTÉK> A karaktered ebben a körben már cselekedett!");
			return;
		}

		PlayerEntity hero = (PlayerEntity) entities.get(0);

		if (hero.getSprint() <= 0) {
			Coordinator.appwindow.outputMessage("<JÁTÉK> A karaktered nem tud többet sprintelni!");
			return;
		}

		if (!isOnTheBoard(row, column)) {
			Coordinator.appwindow.outputMessage("<JÁTÉK> A lépés kimutat a játéktérből!");
		} else if (!isTwoMove(entities.get(0), row, column)) {
			Coordinator.appwindow.outputMessage("<JÁTÉK> A lépés nem egy mezőre volt az aktuális pozíciódtól!");
		} else if (gameboard[row][column] == 'w') {
			Coordinator.appwindow.outputMessage("<JÁTÉK> Falon nem tudsz átmenni!");
		} else {
			playerturnend = true;
			hero.setSprint(hero.getSprint() - 1);
			Coordinator.appwindow.outputMessage("<JÁTÉK> elhasználtál egy sprintelési lehetőséget!");
			Coordinator.appwindow.outputMessage(entities.get(0).getName() + " lépése: (" + entities.get(0).getPosrow()
					+ "," + entities.get(0).getPoscolumn() + ") -> (" + row + "," + column + ")");
			entities.get(0).setPosrow(row);
			entities.get(0).setPoscolumn(column);
		}
	}

	// Juss ki élve - alapok!
	public void gameLoop() {
		boolean end = false;

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		while (!end && turncount != maxturn) {
			playerturnend = false;
			if (!gamewindow.isFrameActive()) {
				break;
			}

			turncount++;
			Coordinator.appwindow.outputMessage("<JÁTÉK> " + turncount + ". kör");
			try {
				player.yourTurn();
			} catch (Exception ex) {
				Coordinator.appwindow.outputMessage(ex.toString());
				runtimeerror = true;
				break;
			}
			fillBoard();

			intelligence();

			fillBoard();
			gamewindow.drawBoard(gameboard);

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Coordinator.appwindow.outputMessage("");

			if (isPlayerEaten()) {
				Coordinator.appwindow.outputMessage("<JÁTÉK> A játékost egy zombi megharapta! VÉGE A JÁTÉKNAK");
				break;
			} else if (isExitReached()) {
				Coordinator.appwindow.outputMessage("<JÁTÉK> A játékos eljutott a kijárathoz! MEGMENEKÜLTÉL");
				break;
			}
		}
	}

	// Hol van a zombi? <ciklus , mátrixkezelés>
	public void gameLoop1() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int correct = 0;

		while (turncount != 10) {
			entities = new ArrayList<Entity>();
			entities.add(new ZombieEntity("Zombi", rnd.nextInt(boardheight), rnd.nextInt(boardwidth)));

			zombiposrow = 0;
			zombiposcolumn = 0;
			turncount++;

			fillBoard();
			gamewindow.drawBoard(gameboard);

			Coordinator.appwindow.outputMessage("<JÁTÉK> " + turncount + ". kör");
			try {
				player.yourTurn();
			} catch (Exception ex) {
				Coordinator.appwindow.outputMessage(ex.toString());
				runtimeerror = true;
				break;
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Coordinator.appwindow.outputMessage(
					"<JÁTÉK> A zombi szerinted a (" + zombiposrow + "," + zombiposcolumn + ") helyen tartózkodik!");
			Coordinator.appwindow.outputMessage("<JÁTÉK> A zombi a (" + entities.get(0).getPosrow() + ","
					+ entities.get(0).getPoscolumn() + ") helyen tartózkodik!");
			if (zombiposrow == entities.get(0).getPosrow() && zombiposcolumn == entities.get(0).getPoscolumn()) {
				Coordinator.appwindow.outputMessage("<JÁTÉK> Igazad volt!");
				correct++;
			} else {
				Coordinator.appwindow.outputMessage("<JÁTÉK> Tévedtél!");
			}

			Coordinator.appwindow.outputMessage("");
		}
		Coordinator.appwindow.outputMessage(
				"<JÁTÉK> " + 10 + " körből " + correct + " alkalommal találtad el , hogy hol van a zombi!");
		if (correct == 10) {
			Coordinator.appwindow.outputMessage("<JÁTÉK> MINDENT ELTALÁLTÁL!");
		} else {
			Coordinator.appwindow.outputMessage("<JÁTÉK> VALAMI PROBLÉMA VAN MÉG AZ ALGORITMUSSAL!");
		}

	}

	// "Juss el a célig a lehető legkevesebb lépésből! <ciklus>" / "Juss el a
	// célig a lehető legkevesebb lépésből! <rekurzió>"
	public void gameLoop2() {

		environmentobjects.add(new Exit(rnd.nextInt(boardheight), rnd.nextInt(boardwidth)));

		while (true) {
			int r = rnd.nextInt(boardheight);
			int c = rnd.nextInt(boardwidth);

			if (!(r == environmentobjects.get(0).getPosrow() && c == environmentobjects.get(0).getPoscolumn())) {
				entities.add(new PlayerEntity("Player", r, c));
				break;
			}
		}

		if (actualexercise == "Juss el a célig a lehető legkevesebb lépésből! <rekurzió>") {
			int maxwall = boardwidth * boardheight / 2 - 1;
			boolean beatable = false;

			while (!beatable) {
				while (maxwall != 0) {
					int r = rnd.nextInt(boardheight);
					int c = rnd.nextInt(boardwidth);

					if (!(r == entities.get(0).getPosrow() && c == entities.get(0).getPoscolumn())) {

						boolean good = true;
						for (Environment env : environmentobjects) {
							if (r == env.getPosrow() && c == env.getPoscolumn()) {
								good = false;
								break;
							}
						}

						if (good) {
							environmentobjects.add(new Wall(r, c));
							maxwall--;
						}
					}
				}
				fillBoard();
				if (leeDistance(entities.get(0).getPosrow(), entities.get(0).getPoscolumn(),
						environmentobjects.get(0).getPosrow(), environmentobjects.get(0).getPoscolumn()) == 0) {

					environmentobjects = new ArrayList<Environment>();
					entities = new ArrayList<Entity>();

					environmentobjects.add(new Exit(rnd.nextInt(boardheight), rnd.nextInt(boardwidth)));

					while (true) {
						int r = rnd.nextInt(boardheight);
						int c = rnd.nextInt(boardwidth);

						if (!(r == environmentobjects.get(0).getPosrow()
								&& c == environmentobjects.get(0).getPoscolumn())) {
							entities.add(new PlayerEntity("Player", r, c));
							break;
						}
					}
				} else {
					beatable = true;
				}
			}

		}
		fillBoard();
		int distance = 0;
		if (actualexercise == "Juss el a célig a lehető legkevesebb lépésből! <ciklus>") {
			distance = Math.max(Math.abs(environmentobjects.get(0).getPosrow() - entities.get(0).getPosrow()),
					Math.abs(environmentobjects.get(0).getPoscolumn() - entities.get(0).getPoscolumn()));
		} else if (actualexercise == "Juss el a célig a lehető legkevesebb lépésből! <rekurzió>") {
			distance = leeDistance(entities.get(0).getPosrow(), entities.get(0).getPoscolumn(),
					environmentobjects.get(0).getPosrow(), environmentobjects.get(0).getPoscolumn());
		}

		gamewindow.drawBoard(gameboard);

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		while (turncount != distance) {
			turncount++;
			playerturnend = false;
			Coordinator.appwindow.outputMessage("<JÁTÉK> " + turncount + ". kör");
			try {
				player.yourTurn();
			} catch (Exception ex) {
				Coordinator.appwindow.outputMessage(ex.toString());
				runtimeerror = true;
				break;
			}

			fillBoard();
			gamewindow.drawBoard(gameboard);

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Coordinator.appwindow.outputMessage("");
		}
		if (environmentobjects.get(0).getPosrow() == entities.get(0).getPosrow()
				&& environmentobjects.get(0).getPoscolumn() == entities.get(0).getPoscolumn()) {
			Coordinator.appwindow.outputMessage(
					"<JÁTÉK> ELJUTOTTÁL A CÉLIG! MEGMENEKÜLTÉL! A legkevesebb lépésszám a célig: " + distance);
		} else {
			Coordinator.appwindow.outputMessage(
					"<JÁTÉK> NEM JUTOTTÁL EL A CÉLIG! VESZTETTÉL! A legkevesebb lépésszám a célig: " + distance);
		}
	}

	@Override
	public int getPositionRow() {
		return entities.get(0).getPosrow();
	}

	@Override
	public int getPositionColumn() {
		return entities.get(0).getPoscolumn();
	}

	@Override
	public char[][] getBoard() {
		return gameboard;
	}

	// Az adott pont a pályán belül van - e
	private boolean isOnTheBoard(int row, int col) {
		if (row >= 0 && row < boardheight && col >= 0 && col < boardwidth) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void axeAttack(String direction) {
		if (playerturnend) {
			Coordinator.appwindow.outputMessage("<JÁTÉK> A karaktered ebben a körben már cselekedett!");
			return;
		}

		PlayerEntity hero = (PlayerEntity) entities.get(0);
		int row = hero.getPosrow();
		int col = hero.getPoscolumn();

		if (hero.getAxe() <= 0) {
			Coordinator.appwindow.outputMessage("<JÁTÉK> A karakterednek nincs több használható fejszéje!");
			return;
		}

		if (direction == "UP") {
			row = row - 1;

		} else if (direction == "DOWN") {
			row = row + 1;

		} else if (direction == "RIGHT") {
			col = col + 1;

		} else if (direction == "LEFT") {
			col = col - 1;

		} else if (direction == "UPRIGHT") {
			row = row - 1;
			col = col + 1;

		} else if (direction == "UPLEFT") {
			row = row - 1;
			col = col - 1;

		} else if (direction == "DOWNRIGHT") {
			row = row + 1;
			col = col + 1;

		} else if (direction == "DOWNLEFT") {
			row = row + 1;
			col = col - 1;

		} else {
			Coordinator.appwindow.outputMessage("<JÁTÉK> Fejszecsapásod iránya nem volt értelmezhető!");
			return;
		}

		if (isOnTheBoard(row, col)) {
			playerturnend = true;
			if (gameboard[row][col] == 'z') {
				Coordinator.appwindow
						.outputMessage("<JÁTÉK> Fejszecsapásod teli találat! Egy zombival kevesebb! Amit megadtál: ("
								+ (row) + "," + col + ")");
				Coordinator.appwindow.outputMessage("Fejszéid száma egyel csökkent!");
				for (Entity entity : entities) {
					if (entity.getPosrow() == row && entity.getPoscolumn() == col) {
						entities.remove(entity);
						break;
					}
				}
				hero.setAxe(hero.getAxe() - 1);
			} else if (gameboard[row][col] == 'w') {
				Coordinator.appwindow.outputMessage(
						"<JÁTÉK> Fejszecsapásod egy falat talált el! Amit megadtál: (" + (row) + "," + col + ")");
				Coordinator.appwindow.outputMessage("Fejszéid száma egyel csökkent!");
				for (Environment env : environmentobjects) {
					if (env.getPosrow() == row && env.getPoscolumn() == col) {
						environmentobjects.remove(env);
						break;
					}
				}
				hero.setAxe(hero.getAxe() - 1);
			} else {
				Coordinator.appwindow.outputMessage(
						"<JÁTÉK> Fejszecsapásod semmit sem talált el! Amit megadtál: (" + (row) + "," + col + ")");
			}
		} else {
			Coordinator.appwindow.outputMessage(
					"<JÁTÉK> Fejszecsapásod kimutat a játéktérből! Amit megadtál: (" + (row) + "," + col + ")");
		}

	}

	@Override
	public int getSprint() {
		return ((PlayerEntity) entities.get(0)).getSprint();
	}

	@Override
	public int getAmmo() {
		return ((PlayerEntity) entities.get(0)).getAmmo();
	}

	@Override
	public int getAxe() {
		return ((PlayerEntity) entities.get(0)).getAxe();
	}

	@Override
	public void zombiLocation(int row, int column) {
		zombiposrow = row;
		zombiposcolumn = column;

	}

	@Override
	public void shoot(String direction) {
		if (playerturnend) {
			Coordinator.appwindow.outputMessage("<JÁTÉK> A karaktered ebben a körben már cselekedett!");
			return;
		}

		PlayerEntity hero = (PlayerEntity) entities.get(0);
		int row = hero.getPosrow();
		int col = hero.getPoscolumn();

		if (hero.getAmmo() <= 0) {
			Coordinator.appwindow.outputMessage("<JÁTÉK> A karakterednek nincs több tölténye a fegyverhez!");
			return;
		}

		if (direction == "UP") {
			row = row - 1;

		} else if (direction == "DOWN") {
			row = row + 1;

		} else if (direction == "RIGHT") {
			col = col + 1;

		} else if (direction == "LEFT") {
			col = col - 1;

		} else if (direction == "UPRIGHT") {
			row = row - 1;
			col = col + 1;

		} else if (direction == "UPLEFT") {
			row = row - 1;
			col = col - 1;

		} else if (direction == "DOWNRIGHT") {
			row = row + 1;
			col = col + 1;

		} else if (direction == "DOWNLEFT") {
			row = row + 1;
			col = col - 1;

		} else {
			Coordinator.appwindow.outputMessage("<JÁTÉK> A lövés iránya nem volt értelmezhető!");
			return;
		}

		playerturnend = true;
		int rowdiff = row - hero.getPosrow();
		int columndiff = col - hero.getPoscolumn();
		int i = row;
		int j = col;
		hero.setAmmo(hero.getAmmo() - 1);
		Coordinator.appwindow.outputMessage("<JÁTÉK> Töltényeid száma egyel csökkent!");
		while (true) {

			if (!isOnTheBoard(i, j)) {
				Coordinator.appwindow.outputMessage("<JÁTÉK> A lövésed semmit sem talált el!");
				break;
			} else if (gameboard[i][j] == 'z') {
				Coordinator.appwindow.outputMessage(
						"<JÁTÉK> Lövésed teli találat! Egy zombival kevesebb! A találat helye: (" + i + "," + j + ")");

				for (Entity entity : entities) {
					if (entity.getPosrow() == i && entity.getPoscolumn() == j) {
						entities.remove(entity);
						break;
					}
				}
				break;
			} else if (gameboard[i][j] == 'w') {
				Coordinator.appwindow
						.outputMessage("<JÁTÉK> Lövésed egy falat talált el! A találat helye: (" + i + "," + j + ")");
				break;
			} else {
				i = i + rowdiff;
				j = j + columndiff;
			}
		}

	}

	// Két pont közötti legrövidebb távolságot adja ki Lee algoritmus
	// segítségével, ha 0-t ad nincs megfelelő út
	private int leeDistance(int fromx, int fromy, int tox, int toy) {
		int[][] grid = new int[boardheight][boardwidth];

		PriorityQueue<Position> queue = new PriorityQueue<Position>(boardheight * boardwidth,
				new Comparator<Position>() {

					@Override
					public int compare(Position o1, Position o2) {
						if (grid[o1.row][o1.col] < grid[o2.row][o2.col])
							return -1;
						else if (grid[o1.row][o1.col] > grid[o2.row][o2.col])
							return 1;
						else
							return 0;
					}
				});

		queue.offer(entities.get(0).new Position(fromx, fromy));
		grid[fromx][fromy] = 0;

		while (!queue.isEmpty()) {

			Position current = queue.poll();
			List<Position> neighbours = Entity.getNeighbour(current, boardheight, boardwidth);

			for (Position neighbour : neighbours) {

				if (!(gameboard[neighbour.row][neighbour.col] == 'z')
						&& !(gameboard[neighbour.row][neighbour.col] == 'e')
						&& !(gameboard[neighbour.row][neighbour.col] == 'p')
						&& !(gameboard[neighbour.row][neighbour.col] == 'w')
						&& grid[neighbour.row][neighbour.col] == 0) {

					grid[neighbour.row][neighbour.col] = grid[current.row][current.col] + 1;
					queue.offer(neighbour);
				}

				if (neighbour.row == tox && neighbour.col == toy) {
					return grid[current.row][current.col] + 1;
				}

			}

		}
		return 0;
	}
}
