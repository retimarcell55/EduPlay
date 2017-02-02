package module.games.bash;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import connection.*;
import module.ApplicationModule;

public class Bash implements BashInterface {

	// A feladatok változói
	private ArrayList<String> exercises = new ArrayList();
	private String actualexercise;

	// ha fordítás közben futás idejű hiba van
	private boolean runtimeerror;

	// A játék változói
	private int howmanytowin;
	private int turncount;
	private Callable player;

	private enum State {
		Playerturn, Computerturn
	};

	private State state;
	private Random rnd = new Random();
	private boolean firstturn;

	// A játékos változói
	private int playerpoint;
	private int playeractualthrow;
	private int playersaidthrow;
	private boolean playertrust;
	// Csak validációvizsgálatnál
	private int tooSmall;
	private int justSixSide;
	private int wrongOrder;

	// A gép változói
	private int computerpoint;
	private int computeractualthrow;
	private int computersaidthrow;
	private boolean computertrust;
	// Csak validációvizsgálatnál
	private int comp_tooSmall;
	private int comp_justSixSide;
	private int comp_wrongOrder;

	// Gép MI
	private List<Integer> from61to65;
	private int treshold;
	private int couragefactor;
	private int playerlied;
	private int playernotlied;
	private int playertrusted;
	private int playernottrusted;

	public Bash() {
		exercises.add("Bash - t dobott az ellenfél? <elágazás>");
		exercises.add("Érvényes volt a bemondás? <elágazás>");
		exercises.add("Mit mondott a gép legtöbbször? <tömbkezelés>");
		exercises.add("Játék a gép ellen");
		actualexercise = exercises.get(0);
	}

	// A játék neve kérdezhető le vele
	@Override
	public String getName() {

		return "Bash - Kockajáték";
	}

	// A játék szabályai kérdezhetőek le vele
	@Override
	public String getRules() {
		return "A Bash egy két kockával játszható körökre osztott szerencsejáték. A játék célja, hogy a játékos legyőzze a számítógépet, azzal, hogy előbb gyűjt össze 100 pontot. Ehhez körgyőzelmeket kell aratni. \n"
				+ "A játszmát véletlenszerű játékos kezdi, azaz ő dobhat először a kockákkal. (6 oldalú kocka, minden oldalnak ugyanannyi esélye van) Miután dobott, megnézi és bemond egy dobásértéket. \n"
				+ "Az érvényes bemondások erősségi sorrendben gyengébbtől az erősebbig: 31,32,41,42,43,51,52,53,54,61,62,63,64,65,11,22,33,44,55,66,21 \n"
				+ "Mint látható mindig a nagyobb számértéket kell a kisebb elé helyezni, így például a 23 nem érvényes bemondás. \n"
				+ "Ha ugyanolyan szám szerepel a kockákon akkor azt a dobást bash - nak nevezzük, innen ered a játék neve is. (Például a 22 az egy kettes bash). \n"
				+ "A 21 a lehető legerősebb bemondható érték. \n"
				+ "A játékosok tehát minden körben dobnak és be kell mondaniuk egy érvényes dobás értéket. A bemondott dobás értéknek mindenféleképp felül kell múlnia az előzőleg, az ellenfél által bemondott dobást. \n"
				+ "Ez természetesen nem mindig adódik az adott kör kockadobásából, tehát sokszor hazudni kell, a játékos nem mondhatja be azt az értéket amit tényleg dobott. \n"
				+ "Ha az ellenfél gyanakszik a dobás hihetőségét illetően, akkor megteheti, hogy nem hiszi el. Ekkor meg kell mutatni mi volt a dobás. \n"
				+ "A játék tehát körökből áll, valaki dob, és bemond egy számot. Az ellenfél ha azt nem hiszi el, akkor meg kell mutatni a dobást és a körnek vége. Ha elhiszi akkor az ellenfél dob, és megpróbálja űberelni az előző bemondást. \n"
				+ "A kör első dobásánál természetesen nem kell űberelni semmit, mert még nem volt előző bemondás. \n"
				+ "A 21 - es bemondást nem lehet űberelni, viszont ezesetben az újabb 21 - es dobás elegendően erős bemondás \n"
				+ "Egy kör addig tart, amíg valaki hazugnak nem ítéli a másik dobását, vagy ha valaki véletlenül kisebb számot mond be, mint az előző bemondás. \n"
				+ "Pontot szerezni lehet azzal, hogy a játékos hazugnak nevezi ellenfelét és igaza van, vagy az ellenfél hazugnak nevezi és nincs igaza, illetve, ha az ellenfél véletlenül kisebbet mond mint az előző bemondás. \n"
				+ " Ekkor a nyertes kap egy pontot, ez egy körgyőzelem. Ha nincs még meg a 3 pontja valamely játékosnak, akkor új kör indul.\n"
				+ "Továbbá ha valaki tévesen vádolja meg a másikat, akkor a következő kör első dobása nem az övé, hanem a másik játékosé lesz újból. \n"
				+ "\n"
				+ "A játék default játékosa véletlenszerűen hiszi el az elenfél bemondásait, és mindig azt a számot mondja be, amit ténylegesen dobott. \n"
				+ "Az első dobás alatt a másik játékosnak még nem volt bemondása, tehát az erre irányuló függvény lekérdezése 0 - t fog adni eredményül. \n"
				+ "A játék legelején a kezdő játékos kiválasztása véletlenszerű. A program leáll, ha valaki eléri a 1000 győzelmet. \n";
	}

	// A játék API - ja kérdezhető le vele
	@Override
	public String getAPI() {
		String tmp = "";
		if (actualexercise == "Bash - t dobott az ellenfél? <elágazás>") {
			tmp = "void believe(boolean b) -> Ha az ellenfél dobott az adott körben, akkor egy boolean értékkel megadhatod, hogy elhiszed-e a dobását vagy sem \n"
					+ "int computersaidThrow() -> A számítógép legutóbbi bemondását kérheted le ezzel a függvénnyel \n"
					+ "void print(String s) -> A képernyődre irathatsz ki ezzel a függvénnyel \n" + "\n"
					+ "Ne feledd, a függvényeket az app tagváltozón keresztül éred el! (pl : app.isMyTurn()) \n"
					+ "A saját függvényeidet természetesen az app változó nélkül éred el (pl. this.MyFunc()) \n";
		}
		if (actualexercise == "Érvényes volt a bemondás? <elágazás>") {
			tmp = "void believe(boolean b) -> Ha az ellenfél dobott az adott körben, akkor egy boolean értékkel megadhatod, hogy elhiszed-e a dobását vagy sem \n"
					+ "void whyNotValid(int justSixSide, int tooSmall, int wrongOrder) -> A függvény segítségével juttathatjuk el a programnak, hogy melyik érvénytelenségből mennyi volt\n"
					+ "Például ha beírjuk hogy whyNotValid(0,2,3) akkor azt közöljük, hogy 2 túl kicsi és 3 olyan dobás volt, ahol rossz sorrendben voltak a számok.\n"
					+ "int computersaidThrow() -> A számítógép legutóbbi bemondását kérheted le ezzel a függvénnyel \n"
					+ "void print(String s) -> A képernyődre irathatsz ki ezzel a függvénnyel \n" + "\n"
					+ "Ne feledd, a függvényeket az app tagváltozón keresztül éred el! (pl : app.isMyTurn()) \n"
					+ "A saját függvényeidet természetesen az app változó nélkül éred el (pl. this.MyFunc()) \n";
		}
		if (actualexercise == "Mit mondott a gép legtöbbször? <tömbkezelés>") {
			tmp = "void say(int number) -> Ezzel mondhatod meg az adott körben, hogy szerinted eddig mi volt a legtöbbet dobott érték\n"
					+ "int computersaidThrow() -> A számítógép legutóbbi bemondását kérheted le ezzel a függvénnyel \n"
					+ "void print(String s) -> A képernyődre irathatsz ki ezzel a függvénnyel \n" + "\n"
					+ "Ne feledd, a függvényeket az app tagváltozón keresztül éred el! (pl : app.isMyTurn()) \n"
					+ "A saját függvényeidet természetesen az app változó nélkül éred el (pl. this.MyFunc()) \n";
		}
		if (actualexercise == "Játék a gép ellen") {
			tmp = "void say(int number) -> Ha te dobtál az adott körben, akkor a függvénnyel mondhatod be a dobásod értékét, ami vagy igaz, vagy nem \n"
					+ "void believe(boolean b) -> Ha az ellenfél dobott az adott körben, akkor egy boolean értékkel megadhatod, hogy elhiszed-e a dobását vagy sem \n"
					+ "boolean isMyTurn() -> Egy logikai értéket ad vissza, hogy az adott körben te dobsz-e vagy sem \n"
					+ "int actualThrow() -> Az adott dobásod értékét kérheted le a függvény segítségével \n"
					+ "int computersaidThrow() -> A számítógép legutóbbi bemondását kérheted le ezzel a függvénnyel \n"
					+ "void print(String s) -> A képernyődre irathatsz ki ezzel a függvénnyel \n"
					+ "int mysaidThrow() -> A te legutóbbi bemondásodat kérdezheted le a függvénnyel \n"
					+ "\n" + "Ne feledd, a függvényeket az app tagváltozón keresztül éred el! (pl : app.isMyTurn()) \n"
					+ "A saját függvényeidet természetesen az app változó nélkül éred el (pl. this.MyFunc()) \n";
		}
		return tmp;
	}

	@Override
	public String getDescription() {
		String tmp = "";
		if (actualexercise == "Bash - t dobott az ellenfél? <elágazás>") {
			tmp = "A feladat célja, hogy az ellenfél dobásáról eldöntsük bash - e vagy sem.\n"
					+ "Az ellenfél minden körben dobni fog véletlenszerűen és mindig igazat mond, \n"
					+ "a játékos csak szemlélő és értékelheti a számítógép dobásait.\n"
					+ "A számítógép aktuális dobását a computersaidThrow függvénnyel kérhetjük le,\n"
					+ "A believe függvény használatával pedig eldönthetjük egy dobásról, hogy bash - e (true),\n"
					+ "vagy nem(false), tehát ha azt írjuk, hogy app.believe(true) akkor minden dobásról\n"
					+ "az fogjuk gondolni, hogy bash. Ez természetesen rossz taktika.\n"
					+ "A előre beállított ágens véletlenszerűen gondolja egy dobásról, hogy bash - e vagy sem.\n"
					+ "Vajon 100 dobás mindegyikéről el tudjuk dönteni, hogy bash - e?\n";
		}
		if (actualexercise == "Érvényes volt a bemondás? <elágazás>") {
			tmp = "A feladat célja, hogy az ellenfél dobásáról eldöntsük érvényes-e vagy sem.\n"
					+ "A számítógép 1 - től 99 - ig fog egész számokat mondani véletlenszerűen, hogy teszteljen,\n"
					+ "a játékos csak szemlélő és értékelheti a számítógép bemondásait.\n"
					+ "A számítógép aktuális bemondását a computersaidThrow függvénnyel kérhetjük le,\n"
					+ "A believe függvény használatával pedig eldönthetjük egy bemondásról, hogy érvényes - e (true),\n"
					+ "vagy nem(false), tehát ha azt írjuk, hogy app.believe(true) akkor minden dobásról\n"
					+ "az fogjuk gondolni, hogy érvényes. Ez természetesen rossz taktika.\n"
					+ "A előre beállított ágens véletlenszerűen gondolja egy dobásról, hogy érvényes vagy sem.\n"
					+ "Egy dobás érvényessége természetesen a játékszabályoknak megfelelően történik,\n"
					+ "(Például a 76 nem érvényes míg a 21 igen stb)\n"
					+ "Az érvényesség ellenőrzése érdekében számolni kell az érvénytelenségeket\n"
					+ "3 fajta érvénytelenség létezik prioritásis orrendben a legmagasabbtól a legalacsonyabbig:\n"
					+ "Van benne olyan szám, ami nincs a kocka oldalán, túl kicsi a szám, nincsenek jó sorrendben a számok.\n"
					+ "A prioritás miatt például a 9-es bemondás az első érvénytelenségi kategóriába esik, nem abba, hogy túl kicsi.\n"
					+ "A megszámlált eseteket a whyNotValid függvény segítségével juttathatjuk el a rendszernek.\n"
					+ "Vajon 100 dobás mindegyikéről el tudjuk dönteni, hogy érvényes?\n";
		}
		if (actualexercise == "Mit mondott a gép legtöbbször? <tömbkezelés>") {
			tmp = "Az ellenfél minden körben elvégez egy dobást és azt is mondja be.\n"
					+ "A feladat, hogy minden körben megállapítsd, hogy az eddigi véletlenszerű dobások alatt\n"
					+ "mi volt az a dobásérték ami a legtöbbet szerepelt. Ezt kell beadni az APInak.\n"
					+ "Ha aktuálisan nincs konkrét maximum akkor 0 - t kell mondanunk!\n"
					+ "Például ha 3 körös a játék és a dobások: 32,11,32 akkor a bemondásaink: 32,0,32\n"
					+ "Először a 32 dobásból volt a legtöbb, majd második körben nem volt maximum mert 1db 32 -es és 1db 11- es volt,\n"
					+ "majd megint egy 32 -es következett, amiből összesen így kettő lett."
					+ "Az aktuális bemondásunkat a say függvény segítségével juttathatjuk el a gépnek\n"
					+ "A gép aktuális bemondását a computersaidThrow függvénnyel kérhetjük le.\n"
					+ "Az előre beállított ágens véletlenszerű értéket mond minden körben.\n"
					+ "Vajon 100 dobás mindegyike után meg tudjuk állapítani melyikből volt eddig a legtöbb?";
		}
		if (actualexercise == "Játék a gép ellen") {
			tmp = "Ha megcsináltad az eddigi feladatokat, akkor nem okozhat nehézséget a számítógép legyőzése.\n"
					+ "Használhatsz függvényeket, és az egész API lehetőségeit\n" + "Ki nyer először 1000 játszmát?";
		}
		return tmp;
	}

	@Override
	public String getHelp() {
		String tmp = "";
		if (actualexercise == "Bash - t dobott az ellenfél? <elágazás>") {
			tmp = "Valószínűleg az Integer dobásértéket át kell konvertálni String - be.\n"
					+ "Úgy már lehet vizsgálni az egyes karakterek értékét!\n"
					+ "Ha esetleg nem akarsz konvertálni, akkor bizony csak a matematikára hagyatkozhatunk!";
		}
		if (actualexercise == "Érvényes volt a bemondás? <elágazás>") {
			tmp = "Egy kockának csak 6 oldala van.\n" + "A dobás egyjegyű vagy kétjegyű?\n";
		}
		if (actualexercise == "Mit mondott a gép legtöbbször? <tömbkezelés>") {
			tmp = "A feladathoz érdemes globális változót használni, amit a függvényeink mellett deklarálhatunk!\n"
					+ "Használjatjuk a java.util könyvtár kollekcióit, például HashMap - ot, ami kulcs,érték párokat tárol!";
		}
		if (actualexercise == "Játék a gép ellen") {
			tmp = "Nincs elérhető segítség.";
		}
		return tmp;
	}

	// A játék kezdetét inicializáló függvény
	private void initialize() {
		turncount = 0;
		playerpoint = 0;
		computerpoint = 0;
		playeractualthrow = 0;
		computeractualthrow = 0;
		playersaidthrow = 0;
		computersaidthrow = 0;
		playertrust = false;
		firstturn = true;
		computertrust = false;
		howmanytowin = 1000;
		from61to65 = new ArrayList<Integer>(Arrays.asList(0, 0, 0, 0, 0));
		treshold = (int) (howmanytowin * 0.15);
		couragefactor = 0;
		playerlied = 0;
		playernotlied = 0;
		playertrusted = 0;
		playernottrusted = 0;
		runtimeerror = false;

		tooSmall = 0;
		justSixSide = 0;
		wrongOrder = 0;
		comp_tooSmall = 0;
		comp_justSixSide = 0;
		comp_wrongOrder = 0;

		if (randomInt(0, 1) == 0) {
			state = State.Playerturn;
		} else {
			state = State.Computerturn;
		}

		Coordinator.appwindow.clearMessage();
		Coordinator.appwindow.outputMessage("" + from61to65.size());
	}

	// A játékot lebonyolító függvény
	@Override
	public void play() {

		initialize();

		this.player = Coordinator.player;
		player.initialize(this);

		if (actualexercise == "Bash - t dobott az ellenfél? <elágazás>") {
			int correct = 0;
			int bash = 0;

			for (int i = 1; i <= 100; i++) {

				if (randomInt(0, 1) == 0) {
					playertrust = false;
				} else {
					playertrust = true;
				}

				int tmp = randomThrow();
				computersaidthrow = tmp;

				try {
					player.yourTurn();
				} catch (Exception ex) {
					Coordinator.appwindow.outputMessage(ex.toString());
					break;
				}

				Coordinator.appwindow.outputMessage("A dobás: " + tmp);

				if (playertrust) {
					Coordinator.appwindow.outputMessage("A dobásra az mondod, hogy bash!");
				} else {
					Coordinator.appwindow.outputMessage("A dobásra az mondod, hogy nem bash!");
				}

				if ((playertrust == true && isItBash(tmp))) {
					Coordinator.appwindow.outputMessage("Bash volt, igazad volt!");
					correct++;
					bash++;
				} else if ((playertrust == false && !isItBash(tmp))) {
					Coordinator.appwindow.outputMessage("Nem volt bash, igazad volt!");
					correct++;
				} else if (isItBash(tmp)) {
					bash++;
					Coordinator.appwindow.outputMessage("Bash volt, tévedtél!");
				} else {
					Coordinator.appwindow.outputMessage("Nem volt bash, tévedtél!");
				}
				Coordinator.appwindow.outputMessage("");
			}
			if (runtimeerror) {
				return;
			}
			Coordinator.appwindow.outputMessage("");
			Coordinator.appwindow.outputMessage(
					"A 100 dobásból " + bash + " volt bash és neked " + correct + " helyes találatod volt!");
			if (correct == 100) {
				Coordinator.appwindow.outputMessage("A feladat kifogástalanul meg lett oldva!");
			} else if (correct > 50) {
				Coordinator.appwindow.outputMessage("Valami eset talán nem lett lekezelve");
			} else {
				Coordinator.appwindow.outputMessage("Nem igazán működik az algoritmus");
			}
		}
		if (actualexercise == "Érvényes volt a bemondás? <elágazás>") {
			int correct = 0;
			int valid = 0;
			for (int i = 1; i <= 100; i++) {

				if (randomInt(0, 1) == 0) {
					playertrust = false;
				} else {
					playertrust = true;
				}

				int tmp = randomInt(1, 99);
				computersaidthrow = tmp;

				try {
					player.yourTurn();
				} catch (Exception ex) {
					Coordinator.appwindow.outputMessage(ex.toString());
					break;
				}

				Coordinator.appwindow.outputMessage("A dobás: " + tmp);

				if (playertrust) {
					Coordinator.appwindow.outputMessage("A dobásra az mondod, hogy érvényes!");
				} else {
					Coordinator.appwindow.outputMessage("A dobásra az mondod, hogy nem érvényes!");
				}

				if (validNumber(tmp)) {
					if (playertrust) {
						Coordinator.appwindow.outputMessage("Érvényes volt, igazad volt!");
						correct++;
						valid++;
					} else {
						valid++;
						Coordinator.appwindow.outputMessage("Érvényes volt, tévedtél!");
					}
				} else {
					if (playertrust) {
						Coordinator.appwindow.outputMessage("Nem volt érvényes, tévedtél!");
					} else {
						Coordinator.appwindow.outputMessage("Nem volt érvényes, igazad volt!");
						correct++;
					}
				}
				Coordinator.appwindow.outputMessage("");
			}
			if (runtimeerror) {
				return;
			}
			Coordinator.appwindow.outputMessage("");
			Coordinator.appwindow.outputMessage(
					"A 100 dobásból " + valid + " volt érvényes és neked " + correct + " helyes találatod volt!");
			Coordinator.appwindow.outputMessage("A rossz dobások oka előfordulásaik számával:");
			Coordinator.appwindow.outputMessage("A kockának csak 6 oldala van: " + comp_justSixSide);
			Coordinator.appwindow.outputMessage("Túl kicsi a dobás: " + comp_tooSmall);
			Coordinator.appwindow.outputMessage("Nem jó sorrendben vannak a számok: " + comp_wrongOrder);
			Coordinator.appwindow.outputMessage("Az általad vélt rossz dobások oka előfordulásaik számával:");
			Coordinator.appwindow.outputMessage("A kockának csak 6 oldala van: " + justSixSide);
			Coordinator.appwindow.outputMessage("Túl kicsi a dobás: " + tooSmall);
			Coordinator.appwindow.outputMessage("Nem jó sorrendben vannak a számok: " + wrongOrder);
			if (correct == 100 && wrongOrder == comp_wrongOrder && justSixSide == comp_justSixSide
					&& tooSmall == comp_tooSmall) {
				Coordinator.appwindow.outputMessage("A feladat kifogástalanul meg lett oldva!");
			} else if (correct > 50) {
				Coordinator.appwindow.outputMessage("Valami eset talán nem lett lekezelve");
			} else {
				Coordinator.appwindow.outputMessage("Nem igazán működik az algoritmus");
			}

		}
		if (actualexercise == "Mit mondott a gép legtöbbször? <tömbkezelés>") {
			int correct = 0;
			HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
			for (int i = 1; i <= 100; i++) {

				int rt = randomThrow();

				if (map.containsKey(rt)) {
					map.put(rt, map.get(rt) + 1);
				} else {
					map.put(rt, 1);
				}

				int max = 0;
				int maxv = 0;
				int maxcount = 0;
				for (HashMap.Entry<Integer, Integer> entry : map.entrySet()) {
					if (entry.getValue() > maxv) {
						max = entry.getKey();
						maxv = entry.getValue();
					}
				}
				for (HashMap.Entry<Integer, Integer> entry : map.entrySet()) {
					if (entry.getValue() == maxv) {
						maxcount++;
					}
				}
				if (maxcount > 1) {
					max = 0;
				}

				playersaidthrow = randomThrow();

				try {
					player.yourTurn();
				} catch (Exception ex) {
					Coordinator.appwindow.outputMessage(ex.toString());
					break;
				}

				Coordinator.appwindow.outputMessage("A dobás: " + rt);
				Coordinator.appwindow.outputMessage("Eddigi dobások és darabszámuk:");

				String tmp = "";
				for (HashMap.Entry<Integer, Integer> entry : map.entrySet()) {
					tmp = tmp + entry.getKey() + " -> " + entry.getValue() + " db  ";
				}
				Coordinator.appwindow.outputMessage(tmp);

				if (playersaidthrow == 0) {
					Coordinator.appwindow
							.outputMessage("Azt mondod, hogy jelenleg több maximum is létezik a dobásértékek között!");
				} else {
					Coordinator.appwindow
							.outputMessage("Azt mondod, hogy a legtöbbször dobott érték a: " + playersaidthrow);
				}

				if (max == 0 && playersaidthrow == 0) {
					Coordinator.appwindow
							.outputMessage("Igazad volt, tényleg nincs jelenleg maximum a dobásértékek között!");
					correct++;
				} else if (max == 0 && playersaidthrow != 0) {
					Coordinator.appwindow
							.outputMessage("Nem volt igazad, jelenleg nincs maximumérték a dobásértékek között!");
				} else if (max == playersaidthrow) {
					Coordinator.appwindow.outputMessage("Igazad volt, a jelenleg legtöbbször dobott érték: " + max);
					correct++;
				} else {
					Coordinator.appwindow.outputMessage("Nem volt igazad, a legtöbbször dobott érték: " + max);
				}
				Coordinator.appwindow.outputMessage("");

			}
			if (runtimeerror) {
				return;
			}
			Coordinator.appwindow.outputMessage("");
			Coordinator.appwindow.outputMessage("A feladat során " + correct + " jó bemondásod volt a 100-ból!");

			if (correct == 100) {
				Coordinator.appwindow.outputMessage("A feladat kifogástalanul meg lett oldva!");
			} else if (correct > 50) {
				Coordinator.appwindow.outputMessage("Valami eset talán nem lett lekezelve");
			} else {
				Coordinator.appwindow.outputMessage("Nem igazán működik az algoritmus");
			}

		}
		if (actualexercise == "Játék a gép ellen") {
			// JÁTÉKCIKULS!
			while (playerpoint < howmanytowin && computerpoint < howmanytowin) {
				turncount++;
				boolean win = false;
				playeractualthrow = 0;
				computeractualthrow = 0;
				playersaidthrow = 0;
				computersaidthrow = 0;
				firstturn = true;

				Coordinator.appwindow.outputMessage("<JÁTÉK> " + turncount + ". kör!");
				Coordinator.appwindow
						.outputMessage("<JÁTLÉK> Állás: Játékos -> " + playerpoint + " Gép -> " + computerpoint);

				if (state == State.Playerturn) {
					Coordinator.appwindow.outputMessage("<JÁTÉK> A játékos kezd!");
				} else {
					Coordinator.appwindow.outputMessage("<JÁTÉK> A számítógép kezd!");
				}
				Coordinator.appwindow.outputMessage("");

				while (!win) {

					// Default Ágens
					if (randomInt(0, 1) == 0) {
						playertrust = false;
					} else {
						playertrust = true;
					}

					computertrust = false;

					int actual = randomThrow();

					switch (state) {
					case Playerturn:
						playeractualthrow = actual;
						// Default Ágens
						playersaidthrow = playeractualthrow;
						Coordinator.appwindow.outputMessage("<JÁTÉK> A dobásod: " + playeractualthrow);
						break;

					case Computerturn:
						computeractualthrow = actual;
						intelligence();
						Coordinator.appwindow.outputMessage("<JÁTÉK> A gép " + computersaidthrow + " -t mond!");
						break;

					}

					// Futás idejű hibák kezelése
					try {
						player.yourTurn();
					} catch (Exception ex) {
						Coordinator.appwindow.outputMessage(ex.toString());
						runtimeerror = true;
						break;
					}

					switch (state) {
					case Playerturn:
						intelligence();
						if (!computertrust) {
							Coordinator.appwindow.outputMessage("<JÁTÉK> A gép szerint hazudsz!");
							Coordinator.appwindow.outputMessage(" <JÁTÉK> Dobásod: " + playeractualthrow
									+ " , és amit mondtál: " + playersaidthrow);
							if (playersaidthrow != playeractualthrow) {
								playerlied++;
								Coordinator.appwindow.outputMessage("<JÁTÉK> Hazudtál!");
								win = true;
								computerpoint++;
								state = State.Computerturn;
							} else {
								playerlied ++;
								Coordinator.appwindow.outputMessage("<JÁTÉK> Nem Hazudtál!");
								win = true;
								playerpoint++;
								state = State.Playerturn;
							}
						} else {
							if (firstturn) {
								state = State.Computerturn;
							} else if (whatIsStronger(playersaidthrow, computersaidthrow) != 0) {
								Coordinator.appwindow
										.outputMessage("<JÁTÉK> Nem mondtál nagyobbat, mint ami az ellenfélnek volt!");
								win = true;
								computerpoint++;
								state = State.Computerturn;
							} else {
								state = State.Computerturn;
							}

						}
						firstturn = false;
						break;

					case Computerturn:
						if (!playertrust) {
							playernottrusted++;
							Coordinator.appwindow.outputMessage("<JÁTÉK> A gép szerinted hazudik!");
							Coordinator.appwindow.outputMessage("<JÁTÉK> A gép dobása: " + computeractualthrow
									+ " , és amit mondtál: " + computersaidthrow);
							if (computersaidthrow != computeractualthrow) {
								Coordinator.appwindow.outputMessage("<JÁTÉK> Igazad volt!");
								win = true;
								playerpoint++;
								state = State.Playerturn;
							} else {
								Coordinator.appwindow.outputMessage("<JÁTÉK> Tévedtél!");
								win = true;
								computerpoint++;
								state = State.Computerturn;
							}
						} else {
							playertrusted++;
							Coordinator.appwindow.outputMessage("<JÁTÉK> A gép szerinted igazat mond!");
							if (firstturn) {
								state = State.Playerturn;
							} else if (whatIsStronger(computersaidthrow, playersaidthrow) != 0) {
								Coordinator.appwindow.outputMessage(
										"<JÁTÉK> Az ellenfél nem mondott nagyobbat, mint ami neked volt!");
								win = true;
								playerpoint++;
								state = State.Playerturn;
							} else {
								state = State.Playerturn;
							}

						}
						firstturn = false;
						break;

					}
				}
				if (runtimeerror) {
					break;
				}
				if (playerpoint != howmanytowin && computerpoint != howmanytowin) {
					Coordinator.appwindow.outputMessage("<JÁTÉK> Új kör!");
				}
				Coordinator.appwindow.outputMessage("");
				Coordinator.appwindow
						.outputMessage("--------------------------------------------------------------------");
				Coordinator.appwindow.outputMessage("");
			}
			Coordinator.appwindow
					.outputMessage("<JÁTÉK> Állás: Játékos -> " + playerpoint + " Gép -> " + computerpoint);
			if (playerpoint == howmanytowin) {
				Coordinator.appwindow.outputMessage("NYERT A JÁTÉKOS!");
			} else if (computerpoint == howmanytowin) {
				Coordinator.appwindow.outputMessage("NYERT A GÉP!");
			} else if (runtimeerror) {
				Coordinator.appwindow.outputMessage("FUTÁS IDEJŰ HIBA TÖRTÉNT, A JÁTÉK LEÁLL!");
			}
		}
	}

	// A gép intelligenciája a megfelelő körök szerint
	private void intelligence() {
		
		/*if(turncount%((int)(howmanytowin*0.15)) == 0) {
			if(playertrusted > playernottrusted && computerpoint > playerpoint) {
				if(couragefactor<3) {
					couragefactor++;
				}
			} else if(playertrusted > playernottrusted && computerpoint > playerpoint) {
				if(couragefactor>-3) {
					couragefactor--;
				}
			} if(playerlied > playernotlied && computerpoint > playerpoint) {
				if(couragefactor<3) {
					couragefactor++;
				}
			} else if(playerlied > playernotlied && computerpoint > playerpoint) {
				if(couragefactor>-3) {
					couragefactor--;
				}
			}
		}*/
		
		switch (state) {
		case Playerturn:

			switch (playersaidthrow) {
			case 61:
				from61to65.set(0, from61to65.get(0) + 1);
				if (from61to65.get(0) >= treshold) {
					Coordinator.appwindow.outputMessage("<GÉP> Nem hiszem el!");
					computertrust = false;
					return;
				}
				break;
			case 62:
				from61to65.set(1, from61to65.get(1) + 1);
				if (from61to65.get(1) >= treshold) {
					Coordinator.appwindow.outputMessage("<GÉP> Nem hiszem el!");
					computertrust = false;
					return;
				}
				break;
			case 63:
				from61to65.set(2, from61to65.get(2) + 1);
				if (from61to65.get(2) >= treshold) {
					Coordinator.appwindow.outputMessage("<GÉP> Nem hiszem el!");
					computertrust = false;
					return;
				}
				break;
			case 64:
				from61to65.set(3, from61to65.get(3) + 1);
				if (from61to65.get(3) >= treshold) {
					Coordinator.appwindow.outputMessage("<GÉP> Nem hiszem el!");
					computertrust = false;
					return;
				}
				break;
			case 65:
				from61to65.set(4, from61to65.get(4) + 1);
				if (from61to65.get(4) >= treshold) {
					Coordinator.appwindow.outputMessage("<GÉP> Nem hiszem el!");
					computertrust = false;
					return;
				}
				break;
			}

			if (playersaidthrow == 21) {
				Coordinator.appwindow.outputMessage("<GÉP> A 21 - et nem hiszem el!");
				computertrust = false;
			} else if (whatIsStronger(playersaidthrow, computersaidthrow) != 0 && !firstturn) {
				Coordinator.appwindow.outputMessage("<GÉP> Elhiszem, mert nem mondasz nagyobbat mint én!");
				computertrust = true;
			} else if (isItBash(playersaidthrow)) {
				int chance = rnd.nextInt(10) + 1;
				if (playersaidthrow == 11) {
					if (chance > (6 - couragefactor)) {
						Coordinator.appwindow.outputMessage("<GÉP> Elhiszem a bash - t!");
						computertrust = true;
					} else {
						Coordinator.appwindow.outputMessage("<GÉP> Nem hiszem el a bash - t!");
						computertrust = false;
					}
				} else if (playersaidthrow == 22) {
					if (chance > (8 - couragefactor)) {
						Coordinator.appwindow.outputMessage("<GÉP> Elhiszem a bash - t!");
						computertrust = true;
					} else {
						Coordinator.appwindow.outputMessage("<GÉP> Nem hiszem el a bash - t!");
						computertrust = false;
					}
				} else if (playersaidthrow == 33) {
					if (chance > (9 - couragefactor)) {
						Coordinator.appwindow.outputMessage("<GÉP> Elhiszem a bash - t!");
						computertrust = true;
					} else {
						Coordinator.appwindow.outputMessage("<GÉP> Nem hiszem el a bash - t!");
						computertrust = false;
					}
				} else {
					Coordinator.appwindow.outputMessage("<GÉP> Nem hiszem el a bash - t!");
					computertrust = false;
				}
			} else if (firstturn) {
				int chance = rnd.nextInt(10) + 1;
				if (chance < (8 + couragefactor)) {
					Coordinator.appwindow.outputMessage("<GÉP> Elhiszem");
					computertrust = true;
				} else {
					if (playersaidthrow == 32 || playersaidthrow == 41 || playersaidthrow == 42 || playersaidthrow == 43
							|| playersaidthrow == 51 || playersaidthrow == 52 || playersaidthrow == 53) {
						Coordinator.appwindow.outputMessage("<GÉP> Elhiszem");
						computertrust = true;
					} else {
						Coordinator.appwindow.outputMessage("<GÉP> Nem hiszem el ezt az első dobást!");
						computertrust = false;
					}
				}
			} else if (Integer.toString(playersaidthrow).charAt(0) == 6) {
				int chance = rnd.nextInt(10) + 1;
				if (chance > (7 - couragefactor)) {
					Coordinator.appwindow.outputMessage("<GÉP> Nem hiszem el, ez nekem túl sok!");
					computertrust = false;
				} else {
					Coordinator.appwindow.outputMessage("<GÉP> Elhiszem");
					computertrust = true;
				}
			} else if (playersaidthrow == nextBigger(computersaidthrow)) {
				int chance = rnd.nextInt(10) + 1;
				if (chance < (6 - couragefactor)) {
					Coordinator.appwindow.outputMessage("<GÉP> Nem hiszem el!");
					computertrust = false;
				} else {
					Coordinator.appwindow.outputMessage("<GÉP> Elhiszem!");
					computertrust = true;
				}
			} else if (playersaidthrow == nextBigger(nextBigger(computersaidthrow))) {
				int chance = rnd.nextInt(10) + 1;
				if (chance < (4 - couragefactor)) {
					Coordinator.appwindow.outputMessage("<GÉP> Nem hiszem el!");
					computertrust = false;
				} else {
					Coordinator.appwindow.outputMessage("<GÉP> Elhiszem!");
					computertrust = true;
				}
			} else {
				Coordinator.appwindow.outputMessage("<GÉP> Elhiszem!");
				computertrust = true;
			}
			break;
		case Computerturn:
			computertrust = true;
			if (whatIsStronger(computeractualthrow, playersaidthrow) == 0) {
				if (firstturn) {
					int chance = rnd.nextInt(10) + 1;
					if (chance > (8 - couragefactor) && !isItBash(computeractualthrow)) {
						chance = rnd.nextInt(3) + 1;
						int tmp = computeractualthrow;
						for (int i = 0; i < chance; i++) {
							tmp = nextBigger(tmp);
						}
						computersaidthrow = tmp;
					} else {
						computersaidthrow = computeractualthrow;
					}
				} else {
					computersaidthrow = computeractualthrow;
				}
			} else {
				int chance = rnd.nextInt(4) + 1;
				int tmp = computeractualthrow;
				while (whatIsStronger(tmp, playersaidthrow) == 1) {
					tmp = nextBigger(tmp);
				}
				for (int i = 0; i < chance; i++) {
					tmp = nextBigger(tmp);
				}
				computersaidthrow = tmp;
			}
			break;
		}

	}

	// Ha X erősebb 0 - t , ha Y erősebb 1 - et , ha egyenlőek 2 - t ad vissza
	public int whatIsStronger(int x, int y) {
		if (x == y) {
			return 2;
		} else if ((x > y && !isItBash(x) && !isItBash(y) && y != 21) || (x > y && isItBash(x) && isItBash(y))
				|| (x == 21) || (x < y && isItBash(x) && !isItBash(y))
				|| (x > y && isItBash(x) && !isItBash(y) && y != 21)) {
			return 0;
		} else {
			return 1;
		}
	}

	// A függvény megnézi, hogy az adott dobás bash - e
	private boolean isItBash(int x) {
		if (Integer.toString(x).length() != 2) {
			return false;
		} else if (Integer.toString(x).charAt(0) == Integer.toString(x).charAt(1)) {
			return true;
		} else {
			return false;
		}
	}

	// Megvizsgálja, hogy a bemondott szám megfelel-e a játékszabályoknak
	private boolean validNumber(int number) {
		if (Integer.toString(number).contains("0") || Integer.toString(number).contains("7")
				|| Integer.toString(number).contains("8") || Integer.toString(number).contains("9")) {
			comp_justSixSide++;
			Coordinator.appwindow.outputMessage("A kockának csak 6 oldala van!");
			return false;
		} else if (Integer.toString(number).length() != 2) {
			comp_tooSmall++;
			Coordinator.appwindow.outputMessage("Nem megfelelő hosszú a szám! A beadott szám: " + number);
			return false;
		} else if (Character.getNumericValue(Integer.toString(number).charAt(0)) < Character
				.getNumericValue(Integer.toString(number).charAt(1))) {
			comp_wrongOrder++;
			Coordinator.appwindow.outputMessage("Nagyobb számot kell előre írni!");
			return false;
		} else {
			return true;
		}
	}

	// A beadott számhoz eső legközelebbi erősebb bemondást adja vissza
	private int nextBigger(int number) {

		if (number == 11) {
			return 22;
		} else if (number == 22) {
			return 33;
		} else if (number == 33) {
			return 44;
		} else if (number == 44) {
			return 55;
		} else if (number == 55) {
			return 66;
		} else if (number == 66) {
			return 21;
		} else if (number == 31) {
			return 32;
		} else if (number == 32) {
			return 41;
		} else if (number == 41) {
			return 42;
		} else if (number == 42) {
			return 43;
		} else if (number == 43) {
			return 51;
		} else if (number == 51) {
			return 52;
		} else if (number == 52) {
			return 53;
		} else if (number == 53) {
			return 54;
		} else if (number == 54) {
			return 61;
		} else if (number == 61) {
			return 62;
		} else if (number == 62) {
			return 63;
		} else if (number == 63) {
			return 64;
		} else if (number == 64) {
			return 65;
		} else if (number == 65) {
			return 11;
		} else {
			return 21;
		}
	}

	// Generál egy random számot a megadott intervallumban beleértve a szélső
	// értékeket
	private int randomInt(int from, int to) {
		int chance = rnd.nextInt(to - from + 1);
		chance = chance + from;
		return chance;
	}

	// Generál egy bash szabályoknak megfelelő véletlenszerű dobást
	private int randomThrow() {

		int first = randomInt(1, 6);
		int second = randomInt(1, 6);
		int actual = 0;

		if (first > second) {
			actual = Integer.parseInt(Integer.toString(first) + Integer.toString(second));

		} else {
			actual = Integer.parseInt(Integer.toString(second) + Integer.toString(first));
		}

		return actual;
	}

	// Ha a te dobsz, akkor ezzel a függvénnyel mondhatsz be számot
	@Override
	public void say(int number) {
		if (actualexercise == "Bash - t dobott az ellenfél? <elágazás>") {

		}
		if (actualexercise == "Érvényes volt a bemondás? <elágazás>") {

		}
		if (actualexercise == "Mit mondott a gép legtöbbször? <tömbkezelés>") {
			playersaidthrow = number;
		}
		if (actualexercise == "Játék a gép ellen") {
			if (state == State.Computerturn) {
				Coordinator.appwindow
						.outputMessage("<JÁTÉK> Most ne számot mondj , hanem az ellenfél dobását kritizáld!");
			} else {
				if (validNumber(number)) {
					playersaidthrow = number;
					Coordinator.appwindow.outputMessage("<JÁTÉK> A bemondásod: " + playersaidthrow);

				}
			}
		}

	}

	// Ha az ellenfél dobott, akkor ezzel a függvénnyel döntheted el, hogy
	// igazat mondott - e vagy sem
	@Override
	public void believe(boolean b) {
		if (actualexercise == "Bash - t dobott az ellenfél? <elágazás>") {
			playertrust = b;
		}
		if (actualexercise == "Érvényes volt a bemondás? <elágazás>") {
			playertrust = b;
		}
		if (actualexercise == "Játék a gép ellen") {
			if (state == State.Playerturn) {
				Coordinator.appwindow.outputMessage("<JÁTÉK> Most csak az ellenfél dobását kritizálhatod!");
			} else {
				playertrust = b;
			}
		}
	}

	// A függvény segítségével lehet lekérdezni, hogy az adott körben a játékos
	// dobott - e vagy sem
	@Override
	public boolean isMyTurn() {
		if (actualexercise == "Bash - t dobott az ellenfél? <elágazás>") {

		}
		if (actualexercise == "Érvényes volt a bemondás? <elágazás>") {

		}
		if (actualexercise == "Játék a gép ellen") {
			if (state == State.Playerturn) {
				return true;
			}
		}
		return false;
	}

	// Lekérdezhető a player legutóbbi dobásának értéke
	@Override
	public int actualThrow() {
		int tmp = 0;
		if (actualexercise == "Bash - t dobott az ellenfél? <elágazás>") {

		}
		if (actualexercise == "Érvényes volt a bemondás? <elágazás>") {

		}
		if (actualexercise == "Játék a gép ellen") {
			tmp = playeractualthrow;
		}
		return tmp;
	}

	// Lekérdezhető a computer legutóbbi bemondásának értéke
	@Override
	public int computersaidThrow() {
		int tmp = 0;
		if (actualexercise == "Bash - t dobott az ellenfél? <elágazás>") {
			tmp = computersaidthrow;
		}
		if (actualexercise == "Érvényes volt a bemondás? <elágazás>") {
			tmp = computersaidthrow;
		}
		if (actualexercise == "Mit mondott a gép legtöbbször? <tömbkezelés>") {
			tmp = computersaidthrow;
		}
		if (actualexercise == "Játék a gép ellen") {
			tmp = computersaidthrow;
		}
		return tmp;
	}

	// Kiírathatunk a program output mezőjébe, amit szeretnénk
	@Override
	public void print(String s) {
		Coordinator.appwindow.outputMessage("<PLAYER> " + s);

	}

	// Lekérdezhető a player legutóbbi bemondásának értéke
	@Override
	public int mysaidThrow() {
		int tmp = 0;
		if (actualexercise == "Bash - t dobott az ellenfél? <elágazás>") {

		}
		if (actualexercise == "Érvényes volt a bemondás? <elágazás>") {

		}
		if (actualexercise == "Játék a gép ellen") {
			tmp = playersaidthrow;
		}
		return tmp;
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

	@Override
	public void whyNotValid(int justSixSide, int tooSmall, int wrongOrder) {
		if (actualexercise == "Érvényes volt a bemondás? <elágazás>") {

			this.justSixSide = justSixSide;
			this.tooSmall = tooSmall;
			this.wrongOrder = wrongOrder;
		}

	}

}
