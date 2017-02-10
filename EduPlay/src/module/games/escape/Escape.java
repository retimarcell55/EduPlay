package module.games.escape;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

import javax.swing.SwingWorker;

import connection.Callable;
import connection.Coordinator;
import module.games.escape.Entity.Position;

public class Escape implements EscapeInterface {

    private ArrayList<String> exercises = new ArrayList<String>();
    private String selectedExercise;

    private char[][] gameBoard;
    private Callable player;
    private ArrayList<Entity> entities;
    private ArrayList<Environment> environmentObjects;
    private int turnCount;
    private GameWindow gameWindow;
    private int boardWidth;
    private int boardHeight;
    private boolean isPlayerAlreadyDoneSomething;
    private int maximumTurnCount;
    private Random rnd;

    private int zombiePositionRow;
    private int zombiePositionColumn;

    private boolean isRuntimeError;

    public Escape() {
        exercises.add("Hol van a zombi? <ciklus , mátrixkezelés>");
        exercises.add("Juss el a célig a lehető legkevesebb lépésből! <ciklus>");
        exercises.add("Juss el a célig a lehető legkevesebb lépésből! <rekurzió>");
        exercises.add("Juss ki élve - alapok!");
        exercises.add("Test");
        selectedExercise = exercises.get(0);
        rnd = new Random();
    }

    private void initialize() {

        turnCount = 0;
        isRuntimeError = false;
        isPlayerAlreadyDoneSomething = false;
        maximumTurnCount = 50;

        Coordinator.appWindow.clearMessage();
        entities = new ArrayList<Entity>();
        environmentObjects = new ArrayList<Environment>();

        if (selectedExercise == "Hol van a zombi? <ciklus , mátrixkezelés>") {
            boardWidth = rnd.nextInt(9) + 2;
            boardHeight = rnd.nextInt(9) + 2;
        }
        if (selectedExercise == "Juss el a célig a lehető legkevesebb lépésből! <rekurzió>") {
            boardWidth = rnd.nextInt(9) + 2;
            boardHeight = rnd.nextInt(9) + 2;
        }
        if (selectedExercise == "Juss el a célig a lehető legkevesebb lépésből! <ciklus>") {
            boardWidth = rnd.nextInt(9) + 2;
            boardHeight = rnd.nextInt(9) + 2;
        } else if (selectedExercise == "Juss ki élve - alapok!") {
            boardWidth = 3;
            boardHeight = 3;
            PlayerEntity hero = new PlayerEntity("Player", 0, 0);
            hero.equip(0, 0, 1);
            entities.add(hero);
            entities.add(new ZombieEntity("Zombi", 0, 2));
            environmentObjects.add(new Exit(2, 2));

        } else if (selectedExercise == "Test") {
            BufferedReader reader = null;
            PlayerEntity hero = null;
            try {

                reader = new BufferedReader(new FileReader(new File(Coordinator.FILE_SOURCE +
                        "/Test.txt")));

                hero = new PlayerEntity("Player", 0, 0);
                entities.add(hero);

                String firstLine = reader.readLine();

                hero.equip(Integer.parseInt(firstLine.split("\\s+")[0]), Integer.parseInt
								(firstLine.split("\\s+")[1]),
                        Integer.parseInt(firstLine.split("\\s+")[2]));

                String secondLine = reader.readLine();

                boardHeight = Integer.parseInt(secondLine.split("\\s+")[0]);
                boardWidth = Integer.parseInt(secondLine.split("\\s+")[1]);

                int zombieCount = 1;

                for (int i = 0; i < boardHeight; i++) {

                    String actualLine = reader.readLine();
                    for (int j = 0; j < boardWidth; j++) {
                        if (actualLine.split("\\s+")[j].equals("p")) {
                            hero.setPositionRow(i);
                            hero.setPositionColumn(j);
                        } else if (actualLine.split("\\s+")[j].equals("z")) {
                            entities.add(new ZombieEntity("Zombi" + zombieCount, i, j));
                            zombieCount++;
                        } else if (actualLine.split("\\s+")[j].equals("w")) {
                            environmentObjects.add(new Wall(i, j));
                        } else if (actualLine.split("\\s+")[j].equals("e")) {
                            environmentObjects.add(new Exit(i, j));
                        }
                    }
                }

                reader.close();

            } catch (FileNotFoundException e) {
                Coordinator.appWindow.outputMessage(e.getMessage());
            } catch (IOException e) {
                Coordinator.appWindow.outputMessage(e.getMessage());
            }
        }
        gameBoard = new char[boardHeight][boardWidth];
        gameWindow = new GameWindow(boardHeight, boardWidth);
        fillBoard();
        gameWindow.drawBoard(gameBoard);
    }

    @Override
    public String getName() {
        return "Fuss amíg tudsz";
    }

    @Override
    public String getRules() {
        return "A játék célja, hogy az adott pályán eljussunk a célig, azaz ellépkedjünk a " +
				"kijáratot ábrázoló mezőig.\n"
                + "A pályát környezeti elemek és zombik tarkítják, utóbbiaknak célja, hogy a " +
				"játékost elkapják és megegyék.\n"
                + "A játékos egy körben egy mezőt léphet, akár átlósan is, vagy cselekedhet " +
				"(például lő, fejszét használ, sprintel)\n"
                + "vagy várhat és akkor karaktere egy helyben marad. Minden körben csak egy " +
				"dolgot tehetünk!\n"
                + "A játék véget ér, ha a karakter rálép a kijáratot ábrázoló mezőre, ekkor " +
				"megnyertük a játékot\n"
                + "vagy egy zombihoz ér, azaz vagy egy zombi rálép, vagy a játékos lép rá egyre. " +
				"Ekkor a játékos vesztett.\n"
                + "A zombik természetesen megpróbálnak egyre közelebb jutani a játékoshoz, egy " +
				"körben egy mezőt lépnek.\n"
                + "Egyes szinteken használhatunk felszereléseket, amik megkönnyíthetik utunkat a " +
				"célig:\n"
                + "A revolverrel egy egyenes vonalban lőhetünk, akár átlósan is. Ha a lövedék egy" +
				" zombit ér, akkor azt eltünteti a játéktérről.\n"
                + "A fejszével egy mellettünk álló zombit szabadíthatunk meg a fejétől, akár " +
				"átlósan is,\n"
                + "illetve, ha egy fal az utunkban áll, azt lebonthatjuk vele.\n"
                + "Lehetőségünk lesz egyes szinteken a sprint képességünket használni, amellyel " +
				"egyszerre 2 blokkot mozoghatunk.\n"
                + "Az erőforrásaint természetesen korlátozottak, a fegyver töltényt használ,\n"
                + "a fejsze elhasználódik egy ütés után, a sprinteléstől kifáradunk stb. Minden " +
				"eszköz mennyisége lekérhető az API segítségével.\n"
                + "A pályán a játékoson és a zombikon kívül más környezeti elemek is lehetnek, " +
				"mint például más áldozatok,\n"
                + "akiket mondjuk a zombik előbb kapnak el mint minket vagy falak stb. Az " +
				"aktuális feladat leírását mindig tüzetesen vizsgáljuk át!\n"
                + "A pálya egy karakter mátrixban van tárolva, ami ugyancsak lekérhető. A benne " +
				"szereplő főbb szimbólumok:\n"
                + " - : semmi , z : zombi , p : játékos , e : kijárat. , w : fal." + "\n"
                + "Az alapértelmezett ágens minden körben vár és nem csinál semmit!\n";
    }

    @Override
    public String getApi() {
        String tmp = "";
        if (selectedExercise == "Hol van a zombi? <ciklus , mátrixkezelés>") {
            tmp = "char[][] getBoard() -> A pálya karakter mátrixát kérhetjük le a függvénnyel\n"
                    + "void zombieLocation(int row , int column) -> Ezzel a függvénnyel adhatod " +
					"meg, hogy szerinted hol van a zombi az adott körben.\n"
                    + "Ne feledd, a függvényeket az app tagváltozón keresztül éred el! (pl : app" +
					".getBoard()) \n"
                    + "A saját függvényeidet természetesen az app változó nélkül éred el (pl. " +
					"this.MyFunc()) \n" + "\n"
                    + "A pálya 0,0 pontja a bal felső sarokban lévő cella. Vigyázzunk az " +
					"indexelés 0 - val kezdődik!"
                    + "Ha várni szeretnénk, akkor az adott körben nem csináljunk semmit!";
        } else if (selectedExercise == "Juss el a célig a lehető legkevesebb lépésből! <ciklus>"
                || selectedExercise == "Juss el a célig a lehető legkevesebb lépésből! " +
				"<rekurzió>") {
            tmp = "char[][] getBoard() -> A pálya karakter mátrixát kérhetjük le a függvénnyel\n"
                    + "void moveToCell(int sor, oszlop) -> A karakterünk mozgatása egy cellával " +
					"arrébb\n"
                    + "int getPositionRow() -> Az aktuális pozíciónk első koordinátája, hogy " +
					"hanyadik sorban vagyunk\n"
                    + "int getPositionColumn() ->  Az aktuális pozíciónk második koordinátája, " +
					"hogy hanyadik oszlopban vagyunk\n"
                    + "Ne feledd, a függvényeket az app tagváltozón keresztül éred el! (pl : app" +
					".getBoard()) \n"
                    + "A saját függvényeidet természetesen az app változó nélkül éred el (pl. " +
					"this.MyFunc()) \n" + "\n"
                    + "A pálya 0,0 pontja a bal felső sarokban lévő cella. Vigyázzunk az " +
					"indexelés 0 - val kezdődik!"
                    + "Ha várni szeretnénk, akkor az adott körben nem csináljunk semmit!";
        } else if (selectedExercise == "Juss ki élve - alapok!") {
            tmp = "void moveToCell(int sor, oszlop) -> A karakterünk mozgatása egy cellával " +
					"arrébb\n"
                    + "int getPositionRow() -> Az aktuális pozíciónk első koordinátája, hogy " +
					"hanyadik sorban vagyunk\n"
                    + "int getPositionColumn() ->  Az aktuális pozíciónk második koordinátája, " +
					"hogy hanyadik oszlopban vagyunk\n"
                    + "char[][] getBoard() -> A pálya karakter mátrixát kérhetjük le a " +
					"függvénnyel\n"
                    + "void axeAttack(String direction) -> Fejszecsapás egy megadott irányba, " +
					"lehetséges irányok:\n"
                    + "UP,DOWN,RIGHT,LEFT,UPLEFT,UPRIGHT,DOWNLEFT,DOWNRIGHT. Felhasználásra " +
					"példa,ütés fölfele: app.axeAttack(\"UP\")\n"
                    + "void shoot(String direction) -> Lövés egy megadott irányba, lehetséges " +
					"irányok:\n"
                    + "UP,DOWN,RIGHT,LEFT,UPLEFT,UPRIGHT,DOWNLEFT,DOWNRIGHT. Felhasználásra " +
					"példa,lövés jobbra: app.shoot(\"RIGHT\")\n"
                    + "int getAxe() -> Lekérheted vele hány darab fejszéd van még\n"
                    + "int getSprint() -> Lekérheted, hogy hányszor tudod még a futást alkalmazni\n"
                    + "int getAmmo() -> Lekérheted, hogy a fegyveredben hány töltény van még\n"
                    + "Ne feledd, a függvényeket az app tagváltozón keresztül éred el! (pl : app" +
					".getBoard()) \n"
                    + "A saját függvényeidet természetesen az app változó nélkül éred el (pl. " +
					"this.MyFunc()) \n" + "\n"
                    + "A pálya 0,0 pontja a bal felső sarokban lévő cella. Vigyázzunk az " +
					"indexelés 0 - val kezdődik!"
                    + "Ha várni szeretnénk, akkor az adott körben nem csináljunk semmit!";
        } else if (selectedExercise == "Test") {
            tmp = "void moveToCell(int sor, oszlop) -> A karakterünk mozgatása egy cellával " +
					"arrébb\n"
                    + "int getPositionRow() -> Az aktuális pozíciónk első koordinátája, hogy " +
					"hanyadik sorban vagyunk\n"
                    + "int getPositionColumn() ->  Az aktuális pozíciónk második koordinátája, " +
					"hogy hanyadik oszlopban vagyunk\n"
                    + "char[][] getBoard() -> A pálya karakter mátrixát kérhetjük le a " +
					"függvénnyel\n"
                    + "void axeAttack(String direction) -> Fejszecsapás egy megadott irányba, " +
					"lehetséges irányok:\n"
                    + "UP,DOWN,RIGHT,LEFT,UPLEFT,UPRIGHT,DOWNLEFT,DOWNRIGHT. Felhasználásra " +
					"példa,ütés fölfele: app.axeAttack(\"UP\")\n"
                    + "void shoot(String direction) -> Lövés egy megadott irányba, lehetséges " +
					"irányok:\n"
                    + "UP,DOWN,RIGHT,LEFT,UPLEFT,UPRIGHT,DOWNLEFT,DOWNRIGHT. Felhasználásra " +
					"példa,lövés jobbra: app.shoot(\"RIGHT\")\n"
                    + "int getAxe() -> Lekérheted vele hány darab fejszéd van még\n"
                    + "int getSprint() -> Lekérheted, hogy hányszor tudod még a futást alkalmazni\n"
                    + "int getAmmo() -> Lekérheted, hogy a fegyveredben hány töltény van még\n"
                    + "void sprintToCell(int row, int column) -> Ha van még sprint lehetőséged, " +
					"akkor akár 2 mező távolságra is léphetsz vele\n"
                    + "Ne feledd, a függvényeket az app tagváltozón keresztül éred el! (pl : app" +
					".getBoard()) \n"
                    + "A saját függvényeidet természetesen az app változó nélkül éred el (pl. " +
					"this.MyFunc()) \n" + "\n"
                    + "A pálya 0,0 pontja a bal felső sarokban lévő cella. Vigyázzunk az " +
					"indexelés 0 - val kezdődik!"
                    + "Ha várni szeretnénk, akkor az adott körben nem csináljunk semmit!";
        }
        return tmp;
    }

    @Override
    public String getDescription() {
        String tmp = "";
        if (selectedExercise == "Hol van a zombi? <ciklus , mátrixkezelés>") {
            tmp = "A feladat lényege, hogy minden körben megállapítsuk, melyik cellában van a " +
					"zombi!\n"
                    + "Minden körben valahova le lesz rakva egy zombi a pályára, a pálya mérete " +
					"véletlenszerű\n"
                    + "Minden körben találjuk meg a zombit és a zombiPosition(sor,oszlop) " +
					"függvénnyel küldhetjük el koordinátáit a rendszernek\n"
                    + "Az alapértelmezett ágens mindig a (0,0) helyet fogja tippelni.\n"
                    + "Vajon minden körben megtaláljuk a zombi helyét?";
        }
        if (selectedExercise == "Juss el a célig a lehető legkevesebb lépésből! <ciklus>") {
            tmp = "A feladat lényege, hogy a lehető legkevesebb lépésből eljussunk a kijárathoz.\n"
                    + "A játék elindítása után egy véletlenszerű méretű pálya generálódik,\n"
                    + "amin véletlenszerűen van elhelyezve a játékos illetve a menekülést jelentő" +
					" kijárat.\n"
                    + "Pont annyi körünk van elérni a kijáratig, amennyi kör alatt a legkevesebb " +
					"lépésből eljutnánk oda.\n"
                    + "Más akadály apályán nem lesz elhelyezve!.\n" + "Vajon ki tudunk jutni " +
					"időben?\n";
        } else if (selectedExercise == "Juss el a célig a lehető legkevesebb lépésből! " +
				"<rekurzió>") {
            tmp = "A feladat lényege, hogy a lehető legkevesebb lépésből eljussunk a kijárathoz.\n"
                    + "A játék elindítása után egy véletlenszerű méretű pálya generálódik,\n"
                    + "amin véletlenszerűen van elhelyezve a játékos illetve a menekülést jelentő" +
					" kijárat.\n"
                    + "A pályán véletlenszerű mennyiségű fal lesz elhelyezve, amin nem tudunk " +
					"átmenni.\n"
                    + "A feladat mindig úgy generálódik, hogy megoldható legyen!\n"
                    + "Pont annyi körünk van elérni a kijáratig, amennyi kör alatt a legkevesebb " +
					"lépésből eljutnánk oda.\n"
                    + "Vajon ki tudunk jutni időben?\n";
        } else if (selectedExercise == "Juss ki élve - alapok!") {
            tmp = "A gyakorló pályán, ami 3x3 - as, egy darab zombi lesz lerakva a jobb felső " +
					"sarokba.\n"
                    + "A kijárat a jobb alsó sarokban lesz. A felszerelésed 1 fejsze!\n"
                    + "Juss el a kijáratig , anélkül, hogy a zombi megenne! Vigyázz a zombi " +
					"mindig közelíteni fog!\n";
        }
        return tmp;
    }

    @Override
    public String getHelp() {
        String tmp = "";
        if (selectedExercise == "Hol van a zombi? <ciklus , mátrixkezelés>") {
            tmp = "A pálya mérete és a zombi helye nem statikus.\n "
                    + "Legegyszerűbb ha lekérjük a pálya mátrixát és végiglépkedünk minden mezőn," +
					" amíg meg nem találjuk a zombit.";
        }
        if (selectedExercise == "Juss el a célig a lehető legkevesebb lépésből! <ciklus>") {
            tmp = "A pálya mérete és a zombi helye nem statikus.\n "
                    + "Első sorban kérjük le a pálya mátrixát és keressük meg rajta, hogy hol van" +
					" a kijárat! ('e')\n"
                    + "Találjuk ki az algoritmust ami mindig a lehető legrövidebb utat adja!\n"
                    + "Az átlós lépés egy oszlop és egy sor lépését váltja ki egyetlen kör " +
					"alatt!\n";
        }
        if (selectedExercise == "Juss el a célig a lehető legkevesebb lépésből! <rekurzió>") {
            tmp = "A pálya mérete és a zombi helye nem statikus.\n "
                    + "Első sorban kérjük le a pálya mátrixát és keressük meg rajta, hogy hol van" +
					" a kijárat! ('e')\n"
                    + "Találjuk ki az algoritmust ami mindig a lehető legrövidebb utat adja!\n"
                    + "Szükségünk lesz egy rekurzív függvény megírására, ami legrövidebb utat " +
					"számol!\n"
                    + "Gondoljunk a pályára mint gráf csúcsokra. Ahol fal van, ott nincs él a " +
					"gráf csúcsai között!\n"
                    + "Az átlós lépés egy oszlop és egy sor lépését váltja ki egyetlen kör " +
					"alatt!\n";
        } else if (selectedExercise == "Juss ki élve - alapok!") {
            tmp = "Nem mindig az a legjobb megoldás, ha folyamatosan a cél felé haladunk, " +
					"gondoljuk át a helyzetet nyugalomban!";
        }
        return tmp;
    }

    @Override
    public void playSelectedExercise() {

        initialize();

        this.player = Coordinator.player;
        player.initializeSelectedModule(this);

        new SwingWorker() {

            @Override
            protected Object doInBackground() throws Exception {

                if (selectedExercise == "Hol van a zombi? <ciklus , mátrixkezelés>") {
                    gameLoopExercise1();
                } else if (selectedExercise == "Juss el a célig a lehető legkevesebb lépésből! " +
						"<ciklus>"
                        || selectedExercise == "Juss el a célig a lehető legkevesebb lépésből! " +
						"<rekurzió>") {
                    gameLoopExercise2And3();
                } else {
                    gameLoop();
                }
                return null;
            }

        }.execute();
        if (isRuntimeError) {
            Coordinator.appWindow.outputMessage("FUTÁS IDEJŰ HIBA TÖRTÉNT, A JÁTÉK LEÁLL!");
            return;
        }

    }

    private void intelligence() {
        for (Entity entity : entities) {
            fillBoard();
            if (entity instanceof ZombieEntity) {
                ((ZombieEntity) entity).intelligence(gameBoard, entities.get(0).getPositionRow(),
                        entities.get(0).getPositionColumn());
            }
        }
    }

    @Override
    public void print(String s) {
        Coordinator.appWindow.outputMessage("<PLAYER> " + s);

    }

    @Override
    public ArrayList<String> getExercises() {
        return exercises;
    }

    @Override
    public void setExercise(String exercise) {
        for (String item : exercises) {
            if (item == exercise) {
                selectedExercise = item;
            }
        }

    }

    @Override
    public String getSelectedExercise() {
        return selectedExercise;
    }

    private void fillBoard() {

        for (int i = 0; i < gameBoard.length; i++) {
            for (int j = 0; j < gameBoard[i].length; j++) {
                gameBoard[i][j] = '-';
            }
        }

        for (Entity entity : entities) {
            if (entity instanceof PlayerEntity) {
                gameBoard[entity.getPositionRow()][entity.getPositionColumn()] = 'p';
            } else if (entity instanceof ZombieEntity) {
                gameBoard[entity.getPositionRow()][entity.getPositionColumn()] = 'z';
            }
        }

        for (Environment env : environmentObjects) {
            if (env instanceof Exit) {
                gameBoard[env.getPositionRow()][env.getPositionColumn()] = 'e';
            }
            if (env instanceof Wall) {
                gameBoard[env.getPositionRow()][env.getPositionColumn()] = 'w';
            }
        }
    }

    // Két entitás egymára lépett-e
    private <T extends Entity> boolean isEntityCollideWithObject(T entity1, T entity2) {
        if ((entity1.getPositionRow() == entity2.getPositionRow()) && (entity1.getPositionColumn() == entity2.getPositionColumn())) {
            return true;
        } else {
            return false;
        }
    }

    private <E extends Entity, T extends Environment> boolean isEntityCollideWithObject(E  entity, T environment) {
        if ((entity.getPositionRow() == environment.getPositionRow()) && (entity.getPositionColumn() == environment.getPositionColumn())) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isExitReached() {
        if (isEntityCollideWithObject(entities.get(0), environmentObjects.get(0))) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isPlayerEaten() {
        for (int i = 1; i < entities.size(); i++) {
            if (isEntityCollideWithObject(entities.get(0), entities.get(i))) {
                return true;
            }
        }
        return false;
    }

    private boolean isOneCellFromPosition(Entity entity, int row, int column) {
        List<Position> neighbours = Entity.getNeighbour(entity.new Position(entity.getPositionRow(),
						entity.getPositionColumn()),
                gameBoard.length, gameBoard[0].length);
        for (Position position : neighbours) {
            if (position.row == row && position.column == column) {
                return true;
            }
        }
        return false;
    }

    private boolean isTwoCellFromPosition(Entity entity, int row, int column) {
        if (Math.max(Math.abs(row - entity.getPositionRow()), Math.abs(column - entity.getPositionColumn())
		) == 1
                || Math.max(Math.abs(row - entity.getPositionRow()), Math.abs(column - entity
				.getPositionColumn())) == 2) {
            return true;
        }
        return false;
    }

    @Override
    public void moveToCell(int row, int column) {
        if (selectedExercise == "Hol van a zombi? <ciklus , mátrixkezelés>") {

        } else {
            if (isPlayerAlreadyDoneSomething) {
                Coordinator.appWindow.outputMessage("<JÁTÉK> A karaktered ebben a körben már " +
						"cselekedett!");
                return;
            }

            if (!isOnTheBoard(row, column)) {
                Coordinator.appWindow.outputMessage("<JÁTÉK> A lépés kimutat a játéktérből!");
            } else if (!isOneCellFromPosition(entities.get(0), row, column)) {
                Coordinator.appWindow.outputMessage("<JÁTÉK> A lépés nem egy mezőre volt az " +
						"aktuális pozíciódtól!");
            } else if (gameBoard[row][column] == 'w') {
                Coordinator.appWindow.outputMessage("Falon nem tudsz átmenni!");
            } else {
                isPlayerAlreadyDoneSomething = true;
                Coordinator.appWindow
                        .outputMessage(entities.get(0).getName() + " lépése: (" + entities.get(0)
								.getPositionRow() + ","
                                + entities.get(0).getPositionColumn() + ") -> (" + row + "," + column
								+ ")");
                entities.get(0).setPositionRow(row);
                entities.get(0).setPositionColumn(column);
            }
        }
    }

    @Override
    public void sprintToCell(int row, int column) {

        if (isPlayerAlreadyDoneSomething) {
            Coordinator.appWindow.outputMessage("<JÁTÉK> A karaktered ebben a körben már " +
					"cselekedett!");
            return;
        }

        PlayerEntity hero = (PlayerEntity) entities.get(0);

        if (hero.getSprint() <= 0) {
            Coordinator.appWindow.outputMessage("<JÁTÉK> A karaktered nem tud többet sprintelni!");
            return;
        }

        if (!isOnTheBoard(row, column)) {
            Coordinator.appWindow.outputMessage("<JÁTÉK> A lépés kimutat a játéktérből!");
        } else if (!isTwoCellFromPosition(entities.get(0), row, column)) {
            Coordinator.appWindow.outputMessage("<JÁTÉK> A lépés nem egy mezőre volt az aktuális " +
					"pozíciódtól!");
        } else if (gameBoard[row][column] == 'w') {
            Coordinator.appWindow.outputMessage("<JÁTÉK> Falon nem tudsz átmenni!");
        } else {
            isPlayerAlreadyDoneSomething = true;
            hero.setSprint(hero.getSprint() - 1);
            Coordinator.appWindow.outputMessage("<JÁTÉK> elhasználtál egy sprintelési " +
					"lehetőséget!");
            Coordinator.appWindow.outputMessage(entities.get(0).getName() + " lépése: (" +
					entities.get(0).getPositionRow()
                    + "," + entities.get(0).getPositionColumn() + ") -> (" + row + "," + column + ")");
            entities.get(0).setPositionRow(row);
            entities.get(0).setPositionColumn(column);
        }
    }

    public void gameLoop() {
        boolean isGameEnd = false;

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        while (!isGameEnd && turnCount != maximumTurnCount) {
            isPlayerAlreadyDoneSomething = false;
            if (!gameWindow.isFrameActive()) {
                break;
            }

            turnCount++;
            Coordinator.appWindow.outputMessage("<JÁTÉK> " + turnCount + ". kör");
            try {
                player.playerTurn();
            } catch (Exception ex) {
                Coordinator.appWindow.outputMessage(ex.toString());
                isRuntimeError = true;
                break;
            }
            fillBoard();

            intelligence();

            fillBoard();
            gameWindow.drawBoard(gameBoard);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            Coordinator.appWindow.outputMessage("");

            if (isPlayerEaten()) {
                Coordinator.appWindow.outputMessage("<JÁTÉK> A játékost egy zombi megharapta! " +
						"VÉGE A JÁTÉKNAK");
                break;
            } else if (isExitReached()) {
                Coordinator.appWindow.outputMessage("<JÁTÉK> A játékos eljutott a kijárathoz! " +
						"MEGMENEKÜLTÉL");
                break;
            }
        }
    }

    // Hol van a zombi? <ciklus , mátrixkezelés>
    public void gameLoopExercise1() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        int correctGuess = 0;

        while (turnCount != 10) {
            entities = new ArrayList<Entity>();
            entities.add(new ZombieEntity("Zombi", rnd.nextInt(boardHeight), rnd.nextInt
					(boardWidth)));

            zombiePositionRow = 0;
            zombiePositionColumn = 0;
            turnCount++;

            fillBoard();
            gameWindow.drawBoard(gameBoard);

            Coordinator.appWindow.outputMessage("<JÁTÉK> " + turnCount + ". kör");
            try {
                player.playerTurn();
            } catch (Exception ex) {
                Coordinator.appWindow.outputMessage(ex.toString());
                isRuntimeError = true;
                break;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            Coordinator.appWindow.outputMessage(
                    "<JÁTÉK> A zombi szerinted a (" + zombiePositionRow + "," +
							zombiePositionColumn + ") helyen tartózkodik!");
            Coordinator.appWindow.outputMessage("<JÁTÉK> A zombi a (" + entities.get(0).getPositionRow
					() + ","
                    + entities.get(0).getPositionColumn() + ") helyen tartózkodik!");
            if (zombiePositionRow == entities.get(0).getPositionRow() && zombiePositionColumn ==
					entities.get(0).getPositionColumn()) {
                Coordinator.appWindow.outputMessage("<JÁTÉK> Igazad volt!");
                correctGuess++;
            } else {
                Coordinator.appWindow.outputMessage("<JÁTÉK> Tévedtél!");
            }

            Coordinator.appWindow.outputMessage("");
        }
        Coordinator.appWindow.outputMessage(
                "<JÁTÉK> " + 10 + " körből " + correctGuess + " alkalommal találtad el , hogy hol van " +
						"a zombi!");
        if (correctGuess == 10) {
            Coordinator.appWindow.outputMessage("<JÁTÉK> MINDENT ELTALÁLTÁL!");
        } else {
            Coordinator.appWindow.outputMessage("<JÁTÉK> VALAMI PROBLÉMA VAN MÉG AZ " +
					"ALGORITMUSSAL!");
        }

    }
    
    public void gameLoopExercise2And3() {

        environmentObjects.add(new Exit(rnd.nextInt(boardHeight), rnd.nextInt(boardWidth)));

        while (true) {
            int rndRow = rnd.nextInt(boardHeight);
            int rndColumn = rnd.nextInt(boardWidth);

            if (!(rndRow == environmentObjects.get(0).getPositionRow() && rndColumn == environmentObjects.get(0)
					.getPositionColumn())) {
                entities.add(new PlayerEntity("Player", rndRow, rndColumn));
                break;
            }
        }

        if (selectedExercise == "Juss el a célig a lehető legkevesebb lépésből! <rekurzió>") {
            int maxWallCount = boardWidth * boardHeight / 2 - 1;
            boolean isPossible = false;

            while (!isPossible) {
                while (maxWallCount != 0) {
                    int rndRow = rnd.nextInt(boardHeight);
                    int rndColumn = rnd.nextInt(boardWidth);

                    if (!(rndRow == entities.get(0).getPositionRow() && rndColumn == entities.get(0).getPositionColumn()
					)) {

                        boolean good = true;
                        for (Environment env : environmentObjects) {
                            if (rndRow == env.getPositionRow() && rndColumn == env.getPositionColumn()) {
                                good = false;
                                break;
                            }
                        }

                        if (good) {
                            environmentObjects.add(new Wall(rndRow, rndColumn));
                            maxWallCount--;
                        }
                    }
                }
                fillBoard();
                if (getLeeDistance(entities.get(0).getPositionRow(), entities.get(0).getPositionColumn(),
                        environmentObjects.get(0).getPositionRow(), environmentObjects.get(0)
								.getPositionColumn()) == 0) {

                    environmentObjects = new ArrayList<Environment>();
                    entities = new ArrayList<Entity>();

                    environmentObjects.add(new Exit(rnd.nextInt(boardHeight), rnd.nextInt
							(boardWidth)));

                    while (true) {
                        int rndRow = rnd.nextInt(boardHeight);
                        int rndColumn = rnd.nextInt(boardWidth);

                        if (!(rndRow == environmentObjects.get(0).getPositionRow()
                                && rndColumn == environmentObjects.get(0).getPositionColumn())) {
                            entities.add(new PlayerEntity("Player", rndRow, rndColumn));
                            break;
                        }
                    }
                } else {
                    isPossible = true;
                }
            }

        }
        fillBoard();
        int distance = 0;
        if (selectedExercise == "Juss el a célig a lehető legkevesebb lépésből! <ciklus>") {
            distance = Math.max(Math.abs(environmentObjects.get(0).getPositionRow() - entities.get(0)
							.getPositionRow()),
                    Math.abs(environmentObjects.get(0).getPositionColumn() - entities.get(0)
							.getPositionColumn()));
        } else if (selectedExercise == "Juss el a célig a lehető legkevesebb lépésből! " +
				"<rekurzió>") {
            distance = getLeeDistance(entities.get(0).getPositionRow(), entities.get(0).getPositionColumn(),
                    environmentObjects.get(0).getPositionRow(), environmentObjects.get(0).getPositionColumn
							());
        }

        gameWindow.drawBoard(gameBoard);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        while (turnCount != distance) {
            turnCount++;
            isPlayerAlreadyDoneSomething = false;
            Coordinator.appWindow.outputMessage("<JÁTÉK> " + turnCount + ". kör");
            try {
                player.playerTurn();
            } catch (Exception ex) {
                Coordinator.appWindow.outputMessage(ex.toString());
                isRuntimeError = true;
                break;
            }

            fillBoard();
            gameWindow.drawBoard(gameBoard);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            Coordinator.appWindow.outputMessage("");
        }
        if (environmentObjects.get(0).getPositionRow() == entities.get(0).getPositionRow()
                && environmentObjects.get(0).getPositionColumn() == entities.get(0).getPositionColumn()) {
            Coordinator.appWindow.outputMessage(
                    "<JÁTÉK> ELJUTOTTÁL A CÉLIG! MEGMENEKÜLTÉL! A legkevesebb lépésszám a célig: " +
							"" + distance);
        } else {
            Coordinator.appWindow.outputMessage(
                    "<JÁTÉK> NEM JUTOTTÁL EL A CÉLIG! VESZTETTÉL! A legkevesebb lépésszám a " +
							"célig: " + distance);
        }
    }

    @Override
    public int getPositionRow() {
        return entities.get(0).getPositionRow();
    }

    @Override
    public int getPositionColumn() {
        return entities.get(0).getPositionColumn();
    }

    @Override
    public char[][] getBoard() {
        return gameBoard;
    }

    // Az adott pont a pályán belül van - e
    private boolean isOnTheBoard(int row, int col) {
        if (row >= 0 && row < boardHeight && col >= 0 && col < boardWidth) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void axeAttack(String direction) {
        if (isPlayerAlreadyDoneSomething) {
            Coordinator.appWindow.outputMessage("<JÁTÉK> A karaktered ebben a körben már " +
					"cselekedett!");
            return;
        }

        PlayerEntity hero = (PlayerEntity) entities.get(0);
        int axeRow = hero.getPositionRow();
        int axeColumn = hero.getPositionColumn();

        if (hero.getAxe() <= 0) {
            Coordinator.appWindow.outputMessage("<JÁTÉK> A karakterednek nincs több használható " +
					"fejszéje!");
            return;
        }

        if (direction == "UP") {
            axeRow = axeRow - 1;

        } else if (direction == "DOWN") {
            axeRow = axeRow + 1;

        } else if (direction == "RIGHT") {
            axeColumn = axeColumn + 1;

        } else if (direction == "LEFT") {
            axeColumn = axeColumn - 1;

        } else if (direction == "UPRIGHT") {
            axeRow = axeRow - 1;
            axeColumn = axeColumn + 1;

        } else if (direction == "UPLEFT") {
            axeRow = axeRow - 1;
            axeColumn = axeColumn - 1;

        } else if (direction == "DOWNRIGHT") {
            axeRow = axeRow + 1;
            axeColumn = axeColumn + 1;

        } else if (direction == "DOWNLEFT") {
            axeRow = axeRow + 1;
            axeColumn = axeColumn - 1;

        } else {
            Coordinator.appWindow.outputMessage("<JÁTÉK> Fejszecsapásod iránya nem volt értelmezhető!");
            return;
        }

        if (isOnTheBoard(axeRow, axeColumn)) {
            isPlayerAlreadyDoneSomething = true;
            if (gameBoard[axeRow][axeColumn] == 'z') {
                Coordinator.appWindow
                        .outputMessage("<JÁTÉK> Fejszecsapásod teli találat! Egy zombival kevesebb! Amit megadtál: ("
                                + (axeRow) + "," + axeColumn + ")");
                Coordinator.appWindow.outputMessage("Fejszéid száma egyel csökkent!");
                for (Entity entity : entities) {
                    if (entity.getPositionRow() == axeRow && entity.getPositionColumn() == axeColumn) {
                        entities.remove(entity);
                        break;
                    }
                }
                hero.setAxe(hero.getAxe() - 1);
            } else if (gameBoard[axeRow][axeColumn] == 'w') {
                Coordinator.appWindow.outputMessage(
                        "<JÁTÉK> Fejszecsapásod egy falat talált el! Amit megadtál: (" + (axeRow) + "," + axeColumn + ")");
                Coordinator.appWindow.outputMessage("Fejszéid száma egyel csökkent!");
                for (Environment env : environmentObjects) {
                    if (env.getPositionRow() == axeRow && env.getPositionColumn() == axeColumn) {
                        environmentObjects.remove(env);
                        break;
                    }
                }
                hero.setAxe(hero.getAxe() - 1);
            } else {
                Coordinator.appWindow.outputMessage(
                        "<JÁTÉK> Fejszecsapásod semmit sem talált el! Amit megadtál: (" + (axeRow) + "," + axeColumn + ")");
            }
        } else {
            Coordinator.appWindow.outputMessage(
                    "<JÁTÉK> Fejszecsapásod kimutat a játéktérből! Amit megadtál: (" + (axeRow) + "," + axeColumn + ")");
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
    public void zombieLocation(int row, int column) {
        zombiePositionRow = row;
        zombiePositionColumn = column;

    }

    @Override
    public void shoot(String direction) {
        if (isPlayerAlreadyDoneSomething) {
            Coordinator.appWindow.outputMessage("<JÁTÉK> A karaktered ebben a körben már cselekedett!");
            return;
        }

        PlayerEntity hero = (PlayerEntity) entities.get(0);
        int bulletRow = hero.getPositionRow();
        int bulletColumn = hero.getPositionColumn();

        if (hero.getAmmo() <= 0) {
            Coordinator.appWindow.outputMessage("<JÁTÉK> A karakterednek nincs több tölténye a fegyverhez!");
            return;
        }

        if (direction == "UP") {
            bulletRow = bulletRow - 1;

        } else if (direction == "DOWN") {
            bulletRow = bulletRow + 1;

        } else if (direction == "RIGHT") {
            bulletColumn = bulletColumn + 1;

        } else if (direction == "LEFT") {
            bulletColumn = bulletColumn - 1;

        } else if (direction == "UPRIGHT") {
            bulletRow = bulletRow - 1;
            bulletColumn = bulletColumn + 1;

        } else if (direction == "UPLEFT") {
            bulletRow = bulletRow - 1;
            bulletColumn = bulletColumn - 1;

        } else if (direction == "DOWNRIGHT") {
            bulletRow = bulletRow + 1;
            bulletColumn = bulletColumn + 1;

        } else if (direction == "DOWNLEFT") {
            bulletRow = bulletRow + 1;
            bulletColumn = bulletColumn - 1;

        } else {
            Coordinator.appWindow.outputMessage("<JÁTÉK> A lövés iránya nem volt értelmezhető!");
            return;
        }

        isPlayerAlreadyDoneSomething = true;
        int rowDifference = bulletRow - hero.getPositionRow();
        int columnDifference = bulletColumn - hero.getPositionColumn();
        int i = bulletRow;
        int j = bulletColumn;
        hero.setAmmo(hero.getAmmo() - 1);
        Coordinator.appWindow.outputMessage("<JÁTÉK> Töltényeid száma egyel csökkent!");
        while (true) {

            if (!isOnTheBoard(i, j)) {
                Coordinator.appWindow.outputMessage("<JÁTÉK> A lövésed semmit sem talált el!");
                break;
            } else if (gameBoard[i][j] == 'z') {
                Coordinator.appWindow.outputMessage(
                        "<JÁTÉK> Lövésed teli találat! Egy zombival kevesebb! A találat helye: (" + i + "," + j + ")");

                for (Entity entity : entities) {
                    if (entity.getPositionRow() == i && entity.getPositionColumn() == j) {
                        entities.remove(entity);
                        break;
                    }
                }
                break;
            } else if (gameBoard[i][j] == 'w') {
                Coordinator.appWindow
                        .outputMessage("<JÁTÉK> Lövésed egy falat talált el! A találat helye: (" + i + "," + j + ")");
                break;
            } else {
                i = i + rowDifference;
                j = j + columnDifference;
            }
        }

    }

    private int getLeeDistance(int fromX, int fromY, int toX, int toY) {
        int[][] distanceMatrix = new int[boardHeight][boardWidth];

        PriorityQueue<Position> queue = new PriorityQueue<Position>(boardHeight * boardWidth,
                new Comparator<Position>() {

                    @Override
                    public int compare(Position o1, Position o2) {
                        if (distanceMatrix[o1.row][o1.column] < distanceMatrix[o2.row][o2.column])
                            return -1;
                        else if (distanceMatrix[o1.row][o1.column] > distanceMatrix[o2.row][o2.column])
                            return 1;
                        else
                            return 0;
                    }
                });

        queue.offer(entities.get(0).new Position(fromX, fromY));
        distanceMatrix[fromX][fromY] = 0;

        while (!queue.isEmpty()) {

            Position current = queue.poll();
            List<Position> neighbours = Entity.getNeighbour(current, boardHeight, boardWidth);

            for (Position neighbour : neighbours) {

                if (!(gameBoard[neighbour.row][neighbour.column] == 'z')
                        && !(gameBoard[neighbour.row][neighbour.column] == 'e')
                        && !(gameBoard[neighbour.row][neighbour.column] == 'p')
                        && !(gameBoard[neighbour.row][neighbour.column] == 'w')
                        && distanceMatrix[neighbour.row][neighbour.column] == 0) {

                    distanceMatrix[neighbour.row][neighbour.column] = distanceMatrix[current.row][current.column] + 1;
                    queue.offer(neighbour);
                }

                if (neighbour.row == toX && neighbour.column == toY) {
                    return distanceMatrix[current.row][current.column] + 1;
                }

            }

        }
        return 0;
    }
}
