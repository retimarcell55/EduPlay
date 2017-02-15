package eduplay.module.games.bash;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import eduplay.connection.*;

public class Bash implements BashInterface {

    private ArrayList<String> exercises = new ArrayList();
    private String selectedExercise;

    private boolean isRuntimeError;

    private final int POINTS_TO_WIN = 1000;
    private int turnCount;
    private Callable player;

    private enum turnState {
        PlayerTurn, ComputerTurn
    }

    private turnState actualTurnState;
    private Random rnd = new Random();
    private boolean isFirstTurn;


    private int playerPoints;
    private int playerActualThrowValue;
    private int playerActualAnnounceValue;
    private boolean isPlayerTrust;

    private int tooSmall;
    private int justSixSide;
    private int wrongOrder;

    private int computerPoints;
    private int computerActualThrowValue;
    private int computerActualAnnounceValue;
    private boolean isComputerTrust;

    private int comp_tooSmall;
    private int comp_justSixSide;
    private int comp_wrongOrder;

    private List<Integer> throwingOccurrenceFrom61To65;
    private final int COMPUTER_AI_TRESHOLD = (int) (POINTS_TO_WIN * 0.15);
    private int courageFactor;
    private int playerLiedCount;
    private int playerNotLiedCount;
    private int playerTrustedCount;
    private int playerNotTrustedCount;

    public Bash() {
        exercises.add("Bash - t dobott az ellenfél? <elágazás>");
        exercises.add("Érvényes volt a bemondás? <elágazás>");
        exercises.add("Mit mondott a gép legtöbbször? <tömbkezelés>");
        exercises.add("Játék a gép ellen");
        selectedExercise = exercises.get(0);
    }

    @Override
    public String getName() {

        return "Bash - Kockajáték";
    }

    @Override
    public String getRules() {
        return "A Bash egy két kockával játszható körökre osztott szerencsejáték. A játék célja, " +
                "hogy a játékos legyőzze a számítógépet, azzal, hogy előbb gyűjt össze 100 pontot" +
                "." +
                " Ehhez körgyőzelmeket kell aratni. \n"
                + "A játszmát véletlenszerű játékos kezdi, azaz ő dobhat először a kockákkal. (6 " +
                "oldalú kocka, minden oldalnak ugyanannyi esélye van) Miután dobott, megnézi és " +
                "bemond egy dobásértéket. \n"
                + "Az érvényes bemondások erősségi sorrendben gyengébbtől az erősebbig: 31,32,41," +
                "42,43,51,52,53,54,61,62,63,64,65,11,22,33,44,55,66,21 \n"
                + "Mint látható mindig a nagyobb számértéket kell a kisebb elé helyezni, így " +
                "például a 23 nem érvényes bemondás. \n"
                + "Ha ugyanolyan szám szerepel a kockákon akkor azt a dobást bash - nak nevezzük," +
                " innen ered a játék neve is. (Például a 22 az egy kettes bash). \n"
                + "A 21 a lehető legerősebb bemondható érték. \n"
                + "A játékosok tehát minden körben dobnak és be kell mondaniuk egy érvényes dobás" +
                " értéket. A bemondott dobás értéknek mindenféleképp felül kell múlnia az " +
                "előzőleg, az ellenfél által bemondott dobást. \n"
                + "Ez természetesen nem mindig adódik az adott kör kockadobásából, tehát sokszor " +
                "hazudni kell, a játékos nem mondhatja be azt az értéket amit tényleg dobott. \n"
                + "Ha az ellenfél gyanakszik a dobás hihetőségét illetően, akkor megteheti, hogy " +
                "nem hiszi el. Ekkor meg kell mutatni mi volt a dobás. \n"
                + "A játék tehát körökből áll, valaki dob, és bemond egy számot. Az ellenfél ha " +
                "azt nem hiszi el, akkor meg kell mutatni a dobást és a körnek vége. Ha elhiszi " +
                "akkor az ellenfél dob, és megpróbálja űberelni az előző bemondást. \n"
                + "A kör első dobásánál természetesen nem kell űberelni semmit, mert még nem volt" +
                " előző bemondás. \n"
                + "A 21 - es bemondást nem lehet űberelni, viszont ezesetben az újabb 21 - es " +
                "dobás elegendően erős bemondás \n"
                + "Egy kör addig tart, amíg valaki hazugnak nem ítéli a másik dobását, vagy ha " +
                "valaki véletlenül kisebb számot mond be, mint az előző bemondás. \n"
                + "Pontot szerezni lehet azzal, hogy a játékos hazugnak nevezi ellenfelét és " +
                "igaza van, vagy az ellenfél hazugnak nevezi és nincs igaza, illetve, ha az " +
                "ellenfél véletlenül kisebbet mond mint az előző bemondás. \n"
                + " Ekkor a nyertes kap egy pontot, ez egy körgyőzelem. Ha nincs még meg a 3 " +
                "pontja valamely játékosnak, akkor új kör indul.\n"
                + "Továbbá ha valaki tévesen vádolja meg a másikat, akkor a következő kör első " +
                "dobása nem az övé, hanem a másik játékosé lesz újból. \n"
                + "\n"
                + "A játék default játékosa véletlenszerűen hiszi el az elenfél bemondásait, és " +
                "mindig azt a számot mondja be, amit ténylegesen dobott. \n"
                + "Az első dobás alatt a másik játékosnak még nem volt bemondása, tehát az erre " +
                "irányuló függvény lekérdezése 0 - t fog adni eredményül. \n"
                + "A játék legelején a kezdő játékos kiválasztása véletlenszerű. A program leáll," +
                " ha valaki eléri a 1000 győzelmet. \n";
    }

    @Override
    public String getApi() {
        String api = "";
        if (selectedExercise == "Bash - t dobott az ellenfél? <elágazás>") {
            api = "void myTrust(boolean b) -> Ha az ellenfél dobott az adott körben, akkor egy " +
                    "boolean értékkel megadhatod, hogy elhiszed-e a dobását vagy sem \n"
                    + "int getComputerLastAnnounceValue() -> A számítógép legutóbbi bemondását kérheted le " +
                    "ezzel a függvénnyel \n"
                    + "void print(String s) -> A képernyődre irathatsz ki ezzel a függvénnyel \n"
                    + "\n"
                    + "Ne feledd, a függvényeket az app tagváltozón keresztül éred el! (pl : app" +
                    ".isMyTurn()) \n"
                    + "A saját függvényeidet természetesen az app változó nélkül éred el (pl. " +
                    "this.MyFunc()) \n";
        }
        if (selectedExercise == "Érvényes volt a bemondás? <elágazás>") {
            api = "void myTrust(boolean b) -> Ha az ellenfél dobott az adott körben, akkor egy " +
                    "boolean értékkel megadhatod, hogy elhiszed-e a dobását vagy sem \n"
                    + "void whyNotValid(int justSixSide, int tooSmall, int wrongOrder) -> A " +
                    "függvény segítségével juttathatjuk el a programnak, hogy melyik " +
                    "érvénytelenségből mennyi volt\n"
                    + "Például ha beírjuk hogy whyNotValid(0,2,3) akkor azt közöljük, hogy 2 túl " +
                    "kicsi és 3 olyan dobás volt, ahol rossz sorrendben voltak a számok.\n"
                    + "int getComputerLastAnnounceValue() -> A számítógép legutóbbi bemondását kérheted le " +
                    "ezzel a függvénnyel \n"
                    + "void print(String s) -> A képernyődre irathatsz ki ezzel a függvénnyel \n"
                    + "\n"
                    + "Ne feledd, a függvényeket az app tagváltozón keresztül éred el! (pl : app" +
                    ".isMyTurn()) \n"
                    + "A saját függvényeidet természetesen az app változó nélkül éred el (pl. " +
                    "this.MyFunc()) \n";
        }
        if (selectedExercise == "Mit mondott a gép legtöbbször? <tömbkezelés>") {
            api = "void announce(int number) -> Ezzel mondhatod meg az adott körben, hogy szerinted " +
                    "eddig mi volt a legtöbbet dobott érték\n"
                    + "int getComputerLastAnnounceValue() -> A számítógép legutóbbi bemondását kérheted le " +
                    "ezzel a függvénnyel \n"
                    + "void print(String s) -> A képernyődre irathatsz ki ezzel a függvénnyel \n"
                    + "\n"
                    + "Ne feledd, a függvényeket az app tagváltozón keresztül éred el! (pl : app" +
                    ".isMyTurn()) \n"
                    + "A saját függvényeidet természetesen az app változó nélkül éred el (pl. " +
                    "this.MyFunc()) \n";
        }
        if (selectedExercise == "Játék a gép ellen") {
            api = "void announce(int number) -> Ha te dobtál az adott körben, akkor a függvénnyel " +
                    "mondhatod be a dobásod értékét, ami vagy igaz, vagy nem \n"
                    + "void myTrust(boolean b) -> Ha az ellenfél dobott az adott körben, akkor " +
                    "egy boolean értékkel megadhatod, hogy elhiszed-e a dobását vagy sem \n"
                    + "boolean isMyTurn() -> Egy logikai értéket ad vissza, hogy az adott körben " +
                    "te dobsz-e vagy sem \n"
                    + "int getMyActualThrowValue() -> Az adott dobásod értékét kérheted le a függvény " +
                    "segítségével \n"
                    + "int getComputerLastAnnounceValue() -> A számítógép legutóbbi bemondását kérheted le " +
                    "ezzel a függvénnyel \n"
                    + "void print(String s) -> A képernyődre irathatsz ki ezzel a függvénnyel \n"
                    + "int getMyLastAnnounceValue() -> A te legutóbbi bemondásodat kérdezheted le a " +
                    "függvénnyel \n"
                    + "\n" + "Ne feledd, a függvényeket az app tagváltozón keresztül éred el! (pl" +
                    " : app.isMyTurn()) \n"
                    + "A saját függvényeidet természetesen az app változó nélkül éred el (pl. " +
                    "this.MyFunc()) \n";
        }
        return api;
    }

    @Override
    public String getDescription() {
        String description = "";
        if (selectedExercise == "Bash - t dobott az ellenfél? <elágazás>") {
            description = "A feladat célja, hogy az ellenfél dobásáról eldöntsük bash - e vagy " +
                    "sem.\n"
                    + "Az ellenfél minden körben dobni fog véletlenszerűen és mindig igazat mond," +
                    " \n"
                    + "a játékos csak szemlélő és értékelheti a számítógép dobásait.\n"
                    + "A számítógép aktuális dobását a getComputerLastAnnounceValue függvénnyel kérhetjük " +
                    "le,\n"
                    + "A myTrust függvény használatával pedig eldönthetjük egy dobásról, hogy " +
                    "bash - e (true),\n"
                    + "vagy nem(false), tehát ha azt írjuk, hogy app.myTrust(true) akkor minden " +
                    "dobásról\n"
                    + "az fogjuk gondolni, hogy bash. Ez természetesen rossz taktika.\n"
                    + "A előre beállított ágens véletlenszerűen gondolja egy dobásról, hogy bash " +
                    "- e vagy sem.\n"
                    + "Vajon 100 dobás mindegyikéről el tudjuk dönteni, hogy bash - e?\n";
        }
        if (selectedExercise == "Érvényes volt a bemondás? <elágazás>") {
            description = "A feladat célja, hogy az ellenfél dobásáról eldöntsük érvényes-e vagy " +
                    "sem.\n"
                    + "A számítógép 1 - től 99 - ig fog egész számokat mondani véletlenszerűen, " +
                    "hogy teszteljen,\n"
                    + "a játékos csak szemlélő és értékelheti a számítógép bemondásait.\n"
                    + "A számítógép aktuális bemondását a getComputerLastAnnounceValue függvénnyel kérhetjük" +
                    " le,\n"
                    + "A myTrust függvény használatával pedig eldönthetjük egy bemondásról, hogy " +
                    "érvényes - e (true),\n"
                    + "vagy nem(false), tehát ha azt írjuk, hogy app.myTrust(true) akkor minden " +
                    "dobásról\n"
                    + "az fogjuk gondolni, hogy érvényes. Ez természetesen rossz taktika.\n"
                    + "A előre beállított ágens véletlenszerűen gondolja egy dobásról, hogy " +
                    "érvényes vagy sem.\n"
                    + "Egy dobás érvényessége természetesen a játékszabályoknak megfelelően " +
                    "történik,\n"
                    + "(Például a 76 nem érvényes míg a 21 igen stb)\n"
                    + "Az érvényesség ellenőrzése érdekében számolni kell az érvénytelenségeket\n"
                    + "3 fajta érvénytelenség létezik prioritásis orrendben a legmagasabbtól a " +
                    "legalacsonyabbig:\n"
                    + "Van benne olyan szám, ami nincs a kocka oldalán, túl kicsi a szám, " +
                    "nincsenek jó sorrendben a számok.\n"
                    + "A prioritás miatt például a 9-es bemondás az első érvénytelenségi " +
                    "kategóriába esik, nem abba, hogy túl kicsi.\n"
                    + "A megszámlált eseteket a whyNotValid függvény segítségével juttathatjuk el" +
                    " a rendszernek.\n"
                    + "Vajon 100 dobás mindegyikéről el tudjuk dönteni, hogy érvényes?\n";
        }
        if (selectedExercise == "Mit mondott a gép legtöbbször? <tömbkezelés>") {
            description = "Az ellenfél minden körben elvégez egy dobást és azt is mondja be.\n"
                    + "A feladat, hogy minden körben megállapítsd, hogy az eddigi véletlenszerű " +
                    "dobások alatt\n"
                    + "mi volt az a dobásérték ami a legtöbbet szerepelt. Ezt kell beadni az " +
                    "APInak.\n"
                    + "Ha aktuálisan nincs konkrét maximum akkor 0 - t kell mondanunk!\n"
                    + "Például ha 3 körös a játék és a dobások: 32,11,32 akkor a bemondásaink: " +
                    "32,0,32\n"
                    + "Először a 32 dobásból volt a legtöbb, majd második körben nem volt maximum" +
                    " mert 1db 32 -es és 1db 11- es volt,\n"
                    + "majd megint egy 32 -es következett, amiből összesen így kettő lett."
                    + "Az aktuális bemondásunkat a announce függvény segítségével juttathatjuk el a " +
                    "gépnek\n"
                    + "A gép aktuális bemondását a getComputerLastAnnounceValue függvénnyel kérhetjük le.\n"
                    + "Az előre beállított ágens véletlenszerű értéket mond minden körben.\n"
                    + "Vajon 100 dobás mindegyike után meg tudjuk állapítani melyikből volt eddig" +
                    " a legtöbb?";
        }
        if (selectedExercise == "Játék a gép ellen") {
            description = "Ha megcsináltad az eddigi feladatokat, akkor nem okozhat nehézséget a " +
                    "számítógép legyőzése.\n"
                    + "Használhatsz függvényeket, és az egész API lehetőségeit\n" + "Ki nyer " +
                    "először 1000 játszmát?";
        }
        return description;
    }

    @Override
    public String getHelp() {
        String help = "";
        if (selectedExercise == "Bash - t dobott az ellenfél? <elágazás>") {
            help = "Valószínűleg az Integer dobásértéket át kell konvertálni String - be.\n"
                    + "Úgy már lehet vizsgálni az egyes karakterek értékét!\n"
                    + "Ha esetleg nem akarsz konvertálni, akkor bizony csak a matematikára " +
                    "hagyatkozhatunk!";
        }
        if (selectedExercise == "Érvényes volt a bemondás? <elágazás>") {
            help = "Egy kockának csak 6 oldala van.\n" + "A dobás egyjegyű vagy kétjegyű?\n";
        }
        if (selectedExercise == "Mit mondott a gép legtöbbször? <tömbkezelés>") {
            help = "A feladathoz érdemes globális változót használni, amit a függvényeink mellett" +
                    " deklarálhatunk!\n"
                    + "Használjatjuk a java.util könyvtár kollekcióit, például HashMap - ot, ami " +
                    "kulcs,érték párokat tárol!";
        }
        if (selectedExercise == "Játék a gép ellen") {
            help = "Nincs elérhető segítség.";
        }
        return help;
    }

    private void initializeExercise() {
        turnCount = 0;
        playerPoints = 0;
        computerPoints = 0;
        playerActualThrowValue = 0;
        computerActualThrowValue = 0;
        playerActualAnnounceValue = 0;
        computerActualAnnounceValue = 0;
        isPlayerTrust = false;
        isFirstTurn = true;
        isComputerTrust = false;
        throwingOccurrenceFrom61To65 = new ArrayList<Integer>(Arrays.asList(0, 0, 0, 0, 0));
        courageFactor = 0;
        playerLiedCount = 0;
        playerNotLiedCount = 0;
        playerTrustedCount = 0;
        playerNotTrustedCount = 0;
        isRuntimeError = false;

        tooSmall = 0;
        justSixSide = 0;
        wrongOrder = 0;
        comp_tooSmall = 0;
        comp_justSixSide = 0;
        comp_wrongOrder = 0;

        if (randomInt(0, 1) == 0) {
            actualTurnState = turnState.PlayerTurn;
        } else {
            actualTurnState = turnState.ComputerTurn;
        }

        Coordinator.appWindow.clearMessage();
        Coordinator.appWindow.outputMessage("" + throwingOccurrenceFrom61To65.size());
    }

    // A játékot lebonyolító függvény
    @Override
    public void playSelectedExercise() {

        initializeExercise();

        this.player = Coordinator.player;
        player.initializeSelectedModule(this);

        if (selectedExercise == "Bash - t dobott az ellenfél? <elágazás>") {
            int correctCounter = 0;
            int bashCounter = 0;

            for (int i = 1; i <= 100; i++) {

                isPlayerTrust = randomInt(0, 1) != 0;

                int randomThrow = generateRandomThrow();
                computerActualAnnounceValue = randomThrow;

                try {
                    player.playerTurn();
                } catch (Exception ex) {
                    Coordinator.appWindow.outputMessage(ex.toString());
                    break;
                }

                Coordinator.appWindow.outputMessage("A dobás: " + randomThrow);

                if (isPlayerTrust) {
                    Coordinator.appWindow.outputMessage("A dobásra az mondod, hogy bash!");
                } else {
                    Coordinator.appWindow.outputMessage("A dobásra az mondod, hogy nem bash!");
                }

                if ((isPlayerTrust == true && isItBash(randomThrow))) {
                    Coordinator.appWindow.outputMessage("Bash volt, igazad volt!");
                    correctCounter++;
                    bashCounter++;
                } else if ((isPlayerTrust == false && !isItBash(randomThrow))) {
                    Coordinator.appWindow.outputMessage("Nem volt bash, igazad volt!");
                    correctCounter++;
                } else if (isItBash(randomThrow)) {
                    bashCounter++;
                    Coordinator.appWindow.outputMessage("Bash volt, tévedtél!");
                } else {
                    Coordinator.appWindow.outputMessage("Nem volt bash, tévedtél!");
                }
                Coordinator.appWindow.outputMessage("");
            }
            if (isRuntimeError) {
                return;
            }
            Coordinator.appWindow.outputMessage("");
            Coordinator.appWindow.outputMessage(
                    "A 100 dobásból " + bashCounter + " volt bash és neked " + correctCounter + "" +
                            " helyes találatod volt!");
            if (correctCounter == 100) {
                Coordinator.appWindow.outputMessage("A feladat kifogástalanul meg lett oldva!");
            } else if (correctCounter > 50) {
                Coordinator.appWindow.outputMessage("Valami eset talán nem lett lekezelve");
            } else {
                Coordinator.appWindow.outputMessage("Nem igazán működik az algoritmus");
            }
        }
        if (selectedExercise == "Érvényes volt a bemondás? <elágazás>") {
            int correctCounter = 0;
            int validCounter = 0;
            for (int i = 1; i <= 100; i++) {

                isPlayerTrust = randomInt(0, 1) != 0;

                int randomInt = randomInt(1, 99);
                computerActualAnnounceValue = randomInt;

                try {
                    player.playerTurn();
                } catch (Exception ex) {
                    Coordinator.appWindow.outputMessage(ex.toString());
                    break;
                }

                Coordinator.appWindow.outputMessage("A dobás: " + randomInt);

                if (isPlayerTrust) {
                    Coordinator.appWindow.outputMessage("A dobásra az mondod, hogy érvényes!");
                } else {
                    Coordinator.appWindow.outputMessage("A dobásra az mondod, hogy nem érvényes!");
                }

                if (isValidThrowValue(randomInt)) {
                    if (isPlayerTrust) {
                        Coordinator.appWindow.outputMessage("Érvényes volt, igazad volt!");
                        correctCounter++;
                        validCounter++;
                    } else {
                        validCounter++;
                        Coordinator.appWindow.outputMessage("Érvényes volt, tévedtél!");
                    }
                } else {
                    if (isPlayerTrust) {
                        Coordinator.appWindow.outputMessage("Nem volt érvényes, tévedtél!");
                    } else {
                        Coordinator.appWindow.outputMessage("Nem volt érvényes, igazad volt!");
                        correctCounter++;
                    }
                }
                Coordinator.appWindow.outputMessage("");
            }
            if (isRuntimeError) {
                return;
            }
            Coordinator.appWindow.outputMessage("");
            Coordinator.appWindow.outputMessage(
                    "A 100 dobásból " + validCounter + " volt érvényes és neked " +
                            correctCounter + " helyes találatod volt!");
            Coordinator.appWindow.outputMessage("A rossz dobások oka előfordulásaik számával:");
            Coordinator.appWindow.outputMessage("A kockának csak 6 oldala van: " +
                    comp_justSixSide);
            Coordinator.appWindow.outputMessage("Túl kicsi a dobás: " + comp_tooSmall);
            Coordinator.appWindow.outputMessage("Nem jó sorrendben vannak a számok: " +
                    comp_wrongOrder);
            Coordinator.appWindow.outputMessage("Az általad vélt rossz dobások oka előfordulásaik" +
                    " számával:");
            Coordinator.appWindow.outputMessage("A kockának csak 6 oldala van: " + justSixSide);
            Coordinator.appWindow.outputMessage("Túl kicsi a dobás: " + tooSmall);
            Coordinator.appWindow.outputMessage("Nem jó sorrendben vannak a számok: " + wrongOrder);
            if (correctCounter == 100 && wrongOrder == comp_wrongOrder && justSixSide ==
                    comp_justSixSide
                    && tooSmall == comp_tooSmall) {
                Coordinator.appWindow.outputMessage("A feladat kifogástalanul meg lett oldva!");
            } else if (correctCounter > 50) {
                Coordinator.appWindow.outputMessage("Valami eset talán nem lett lekezelve");
            } else {
                Coordinator.appWindow.outputMessage("Nem igazán működik az algoritmus");
            }

        }
        if (selectedExercise == "Mit mondott a gép legtöbbször? <tömbkezelés>") {
            int correctCounter = 0;
            HashMap<Integer, Integer> throwAndOccurenceMap = new HashMap<Integer, Integer>();
            for (int i = 1; i <= 100; i++) {

                int randomThrow = generateRandomThrow();

                if (throwAndOccurenceMap.containsKey(randomThrow)) {
                    throwAndOccurenceMap.put(randomThrow, throwAndOccurenceMap.get(randomThrow) +
                            1);
                } else {
                    throwAndOccurenceMap.put(randomThrow, 1);
                }

                int maxThrowKey = 0;
                int maxThrowValue = 0;
                int maxThrowValueCount = 0;
                for (HashMap.Entry<Integer, Integer> entry : throwAndOccurenceMap.entrySet()) {
                    if (entry.getValue() > maxThrowValue) {
                        maxThrowKey = entry.getKey();
                        maxThrowValue = entry.getValue();
                    }
                }
                for (HashMap.Entry<Integer, Integer> entry : throwAndOccurenceMap.entrySet()) {
                    if (entry.getValue() == maxThrowValue) {
                        maxThrowValueCount++;
                    }
                }
                if (maxThrowValueCount > 1) {
                    maxThrowKey = 0;
                }

                playerActualAnnounceValue = generateRandomThrow();

                try {
                    player.playerTurn();
                } catch (Exception ex) {
                    Coordinator.appWindow.outputMessage(ex.toString());
                    break;
                }

                Coordinator.appWindow.outputMessage("A dobás: " + randomThrow);
                Coordinator.appWindow.outputMessage("Eddigi dobások és darabszámuk:");

                String output = "";
                for (HashMap.Entry<Integer, Integer> entry : throwAndOccurenceMap.entrySet()) {
                    output = output + entry.getKey() + " -> " + entry.getValue() + " db  ";
                }
                Coordinator.appWindow.outputMessage(output);

                if (playerActualAnnounceValue == 0) {
                    Coordinator.appWindow
                            .outputMessage("Azt mondod, hogy jelenleg több maximum is létezik a " +
                                    "dobásértékek között!");
                } else {
                    Coordinator.appWindow
                            .outputMessage("Azt mondod, hogy a legtöbbször dobott érték a: " +
                                    playerActualAnnounceValue);
                }

                if (maxThrowKey == 0 && playerActualAnnounceValue == 0) {
                    Coordinator.appWindow
                            .outputMessage("Igazad volt, tényleg nincs jelenleg maximum a " +
                                    "dobásértékek között!");
                    correctCounter++;
                } else if (maxThrowKey == 0 && playerActualAnnounceValue != 0) {
                    Coordinator.appWindow
                            .outputMessage("Nem volt igazad, jelenleg nincs maximumérték a " +
                                    "dobásértékek között!");
                } else if (maxThrowKey == playerActualAnnounceValue) {
                    Coordinator.appWindow.outputMessage("Igazad volt, a jelenleg legtöbbször " +
                            "dobott érték: " + maxThrowKey);
                    correctCounter++;
                } else {
                    Coordinator.appWindow.outputMessage("Nem volt igazad, a legtöbbször dobott " +
                            "érték: " + maxThrowKey);
                }
                Coordinator.appWindow.outputMessage("");

            }
            if (isRuntimeError) {
                return;
            }
            Coordinator.appWindow.outputMessage("");
            Coordinator.appWindow.outputMessage("A feladat során " + correctCounter + " jó " +
                    "bemondásod volt a 100-ból!");

            if (correctCounter == 100) {
                Coordinator.appWindow.outputMessage("A feladat kifogástalanul meg lett oldva!");
            } else if (correctCounter > 50) {
                Coordinator.appWindow.outputMessage("Valami eset talán nem lett lekezelve");
            } else {
                Coordinator.appWindow.outputMessage("Nem igazán működik az algoritmus");
            }

        }
        if (selectedExercise == "Játék a gép ellen") {

            while (playerPoints < POINTS_TO_WIN && computerPoints < POINTS_TO_WIN) {
                turnCount++;
                boolean isWinner = false;
                playerActualThrowValue = 0;
                computerActualThrowValue = 0;
                playerActualAnnounceValue = 0;
                computerActualAnnounceValue = 0;
                isFirstTurn = true;

                Coordinator.appWindow.outputMessage("<JÁTÉK> " + turnCount + ". kör!");
                Coordinator.appWindow
                        .outputMessage("<JÁTLÉK> Állás: Játékos -> " + playerPoints + " Gép -> "
                                + computerPoints);

                if (actualTurnState == turnState.PlayerTurn) {
                    Coordinator.appWindow.outputMessage("<JÁTÉK> A játékos kezd!");
                } else {
                    Coordinator.appWindow.outputMessage("<JÁTÉK> A számítógép kezd!");
                }
                Coordinator.appWindow.outputMessage("");

                while (!isWinner) {

                    // Default Ágens
                    isPlayerTrust = randomInt(0, 1) != 0;

                    isComputerTrust = false;

                    int actual = generateRandomThrow();

                    switch (actualTurnState) {
                        case PlayerTurn:
                            playerActualThrowValue = actual;
                            // Default Ágens
                            playerActualAnnounceValue = playerActualThrowValue;
                            Coordinator.appWindow.outputMessage("<JÁTÉK> A dobásod: " +
                                    playerActualThrowValue);
                            break;

                        case ComputerTurn:
                            computerActualThrowValue = actual;
                            intelligence();
                            Coordinator.appWindow.outputMessage("<JÁTÉK> A gép " +
                                    computerActualAnnounceValue + " -t mond!");
                            break;

                    }

                    // Futás idejű hibák kezelése
                    try {
                        player.playerTurn();
                    } catch (Exception ex) {
                        Coordinator.appWindow.outputMessage(ex.toString());
                        isRuntimeError = true;
                        break;
                    }

                    switch (actualTurnState) {
                        case PlayerTurn:
                            intelligence();
                            if (!isComputerTrust) {
                                Coordinator.appWindow.outputMessage("<JÁTÉK> A gép szerint " +
                                        "hazudsz!");
                                Coordinator.appWindow.outputMessage(" <JÁTÉK> Dobásod: " +
                                        playerActualThrowValue
                                        + " , és amit mondtál: " + playerActualAnnounceValue);
                                if (playerActualAnnounceValue != playerActualThrowValue) {
                                    playerLiedCount++;
                                    Coordinator.appWindow.outputMessage("<JÁTÉK> Hazudtál!");
                                    isWinner = true;
                                    computerPoints++;
                                    actualTurnState = turnState.ComputerTurn;
                                } else {
                                    playerLiedCount++;
                                    Coordinator.appWindow.outputMessage("<JÁTÉK> Nem Hazudtál!");
                                    isWinner = true;
                                    playerPoints++;
                                    actualTurnState = turnState.PlayerTurn;
                                }
                            } else {
                                if (isFirstTurn) {
                                    actualTurnState = turnState.ComputerTurn;
                                } else if (whatIsStronger(playerActualAnnounceValue,
                                        computerActualAnnounceValue) != 0) {
                                    Coordinator.appWindow
                                            .outputMessage("<JÁTÉK> Nem mondtál nagyobbat, mint " +
                                                    "ami az ellenfélnek volt!");
                                    isWinner = true;
                                    computerPoints++;
                                    actualTurnState = turnState.ComputerTurn;
                                } else {
                                    actualTurnState = turnState.ComputerTurn;
                                }

                            }
                            isFirstTurn = false;
                            break;

                        case ComputerTurn:
                            if (!isPlayerTrust) {
                                playerNotTrustedCount++;
                                Coordinator.appWindow.outputMessage("<JÁTÉK> A gép szerinted " +
                                        "hazudik!");
                                Coordinator.appWindow.outputMessage("<JÁTÉK> A gép dobása: " +
                                        computerActualThrowValue
                                        + " , és amit mondtál: " + computerActualAnnounceValue);
                                if (computerActualAnnounceValue != computerActualThrowValue) {
                                    Coordinator.appWindow.outputMessage("<JÁTÉK> Igazad volt!");
                                    isWinner = true;
                                    playerPoints++;
                                    actualTurnState = turnState.PlayerTurn;
                                } else {
                                    Coordinator.appWindow.outputMessage("<JÁTÉK> Tévedtél!");
                                    isWinner = true;
                                    computerPoints++;
                                    actualTurnState = turnState.ComputerTurn;
                                }
                            } else {
                                playerTrustedCount++;
                                Coordinator.appWindow.outputMessage("<JÁTÉK> A gép szerinted " +
                                        "igazat mond!");
                                if (isFirstTurn) {
                                    actualTurnState = turnState.PlayerTurn;
                                } else if (whatIsStronger(computerActualAnnounceValue,
                                        playerActualAnnounceValue) != 0) {
                                    Coordinator.appWindow.outputMessage(
                                            "<JÁTÉK> Az ellenfél nem mondott nagyobbat, mint ami " +
                                                    "neked volt!");
                                    isWinner = true;
                                    playerPoints++;
                                    actualTurnState = turnState.PlayerTurn;
                                } else {
                                    actualTurnState = turnState.PlayerTurn;
                                }

                            }
                            isFirstTurn = false;
                            break;

                    }
                }
                if (isRuntimeError) {
                    break;
                }
                if (playerPoints != POINTS_TO_WIN && computerPoints != POINTS_TO_WIN) {
                    Coordinator.appWindow.outputMessage("<JÁTÉK> Új kör!");
                }
                Coordinator.appWindow.outputMessage("");
                Coordinator.appWindow
                        .outputMessage
                                ("--------------------------------------------------------------------");
                Coordinator.appWindow.outputMessage("");
            }
            Coordinator.appWindow
                    .outputMessage("<JÁTÉK> Állás: Játékos -> " + playerPoints + " Gép -> " +
                            computerPoints);
            if (playerPoints == POINTS_TO_WIN) {
                Coordinator.appWindow.outputMessage("NYERT A JÁTÉKOS!");
            } else if (computerPoints == POINTS_TO_WIN) {
                Coordinator.appWindow.outputMessage("NYERT A GÉP!");
            } else if (isRuntimeError) {
                Coordinator.appWindow.outputMessage("FUTÁS IDEJŰ HIBA TÖRTÉNT, A JÁTÉK LEÁLL!");
            }
        }
    }

    // A gép intelligenciája a megfelelő körök szerint
    private void intelligence() {

		/*if(turnCount%((int)(POINTS_TO_WIN*0.15)) == 0) {
			if(playerTrustedCount > playerNotTrustedCount && computerPoints > playerPoints) {
				if(courageFactor<3) {
					courageFactor++;
				}
			} else if(playerTrustedCount > playerNotTrustedCount && computerPoints >
			playerPoints) {
				if(courageFactor>-3) {
					courageFactor--;
				}
			} if(playerLiedCount > playerNotLiedCount && computerPoints > playerPoints) {
				if(courageFactor<3) {
					courageFactor++;
				}
			} else if(playerLiedCount > playerNotLiedCount && computerPoints > playerPoints) {
				if(courageFactor>-3) {
					courageFactor--;
				}
			}
		}*/

        switch (actualTurnState) {
            case PlayerTurn:

                switch (playerActualAnnounceValue) {
                    case 61:
                        throwingOccurrenceFrom61To65.set(0, throwingOccurrenceFrom61To65.get(0) +
                                1);
                        if (throwingOccurrenceFrom61To65.get(0) >= COMPUTER_AI_TRESHOLD) {
                            Coordinator.appWindow.outputMessage("<GÉP> Nem hiszem el!");
                            isComputerTrust = false;
                            return;
                        }
                        break;
                    case 62:
                        throwingOccurrenceFrom61To65.set(1, throwingOccurrenceFrom61To65.get(1) +
                                1);
                        if (throwingOccurrenceFrom61To65.get(1) >= COMPUTER_AI_TRESHOLD) {
                            Coordinator.appWindow.outputMessage("<GÉP> Nem hiszem el!");
                            isComputerTrust = false;
                            return;
                        }
                        break;
                    case 63:
                        throwingOccurrenceFrom61To65.set(2, throwingOccurrenceFrom61To65.get(2) +
                                1);
                        if (throwingOccurrenceFrom61To65.get(2) >= COMPUTER_AI_TRESHOLD) {
                            Coordinator.appWindow.outputMessage("<GÉP> Nem hiszem el!");
                            isComputerTrust = false;
                            return;
                        }
                        break;
                    case 64:
                        throwingOccurrenceFrom61To65.set(3, throwingOccurrenceFrom61To65.get(3) +
                                1);
                        if (throwingOccurrenceFrom61To65.get(3) >= COMPUTER_AI_TRESHOLD) {
                            Coordinator.appWindow.outputMessage("<GÉP> Nem hiszem el!");
                            isComputerTrust = false;
                            return;
                        }
                        break;
                    case 65:
                        throwingOccurrenceFrom61To65.set(4, throwingOccurrenceFrom61To65.get(4) +
                                1);
                        if (throwingOccurrenceFrom61To65.get(4) >= COMPUTER_AI_TRESHOLD) {
                            Coordinator.appWindow.outputMessage("<GÉP> Nem hiszem el!");
                            isComputerTrust = false;
                            return;
                        }
                        break;
                }

                if (playerActualAnnounceValue == 21) {
                    Coordinator.appWindow.outputMessage("<GÉP> A 21 - et nem hiszem el!");
                    isComputerTrust = false;
                } else if (whatIsStronger(playerActualAnnounceValue, computerActualAnnounceValue)
                        != 0 && !isFirstTurn) {
                    Coordinator.appWindow.outputMessage("<GÉP> Elhiszem, mert nem mondasz " +
                            "nagyobbat mint én!");
                    isComputerTrust = true;
                } else if (isItBash(playerActualAnnounceValue)) {
                    int chance = rnd.nextInt(10) + 1;
                    if (playerActualAnnounceValue == 11) {
                        if (chance > (6 - courageFactor)) {
                            Coordinator.appWindow.outputMessage("<GÉP> Elhiszem a bash - t!");
                            isComputerTrust = true;
                        } else {
                            Coordinator.appWindow.outputMessage("<GÉP> Nem hiszem el a bash - t!");
                            isComputerTrust = false;
                        }
                    } else if (playerActualAnnounceValue == 22) {
                        if (chance > (8 - courageFactor)) {
                            Coordinator.appWindow.outputMessage("<GÉP> Elhiszem a bash - t!");
                            isComputerTrust = true;
                        } else {
                            Coordinator.appWindow.outputMessage("<GÉP> Nem hiszem el a bash - t!");
                            isComputerTrust = false;
                        }
                    } else if (playerActualAnnounceValue == 33) {
                        if (chance > (9 - courageFactor)) {
                            Coordinator.appWindow.outputMessage("<GÉP> Elhiszem a bash - t!");
                            isComputerTrust = true;
                        } else {
                            Coordinator.appWindow.outputMessage("<GÉP> Nem hiszem el a bash - t!");
                            isComputerTrust = false;
                        }
                    } else {
                        Coordinator.appWindow.outputMessage("<GÉP> Nem hiszem el a bash - t!");
                        isComputerTrust = false;
                    }
                } else if (isFirstTurn) {
                    int chance = rnd.nextInt(10) + 1;
                    if (chance < (8 + courageFactor)) {
                        Coordinator.appWindow.outputMessage("<GÉP> Elhiszem");
                        isComputerTrust = true;
                    } else {
                        if (playerActualAnnounceValue == 32 || playerActualAnnounceValue == 41 ||
                                playerActualAnnounceValue == 42 || playerActualAnnounceValue == 43
                                || playerActualAnnounceValue == 51 || playerActualAnnounceValue
                                == 52 || playerActualAnnounceValue == 53) {
                            Coordinator.appWindow.outputMessage("<GÉP> Elhiszem");
                            isComputerTrust = true;
                        } else {
                            Coordinator.appWindow.outputMessage("<GÉP> Nem hiszem el ezt az első " +
                                    "dobást!");
                            isComputerTrust = false;
                        }
                    }
                } else if (Integer.toString(playerActualAnnounceValue).charAt(0) == 6) {
                    int chance = rnd.nextInt(10) + 1;
                    if (chance > (7 - courageFactor)) {
                        Coordinator.appWindow.outputMessage("<GÉP> Nem hiszem el, ez nekem túl " +
                                "sok!");
                        isComputerTrust = false;
                    } else {
                        Coordinator.appWindow.outputMessage("<GÉP> Elhiszem");
                        isComputerTrust = true;
                    }
                } else if (playerActualAnnounceValue == nextBiggerThrow(computerActualAnnounceValue)) {
                    int chance = rnd.nextInt(10) + 1;
                    if (chance < (6 - courageFactor)) {
                        Coordinator.appWindow.outputMessage("<GÉP> Nem hiszem el!");
                        isComputerTrust = false;
                    } else {
                        Coordinator.appWindow.outputMessage("<GÉP> Elhiszem!");
                        isComputerTrust = true;
                    }
                } else if (playerActualAnnounceValue == nextBiggerThrow(nextBiggerThrow
                        (computerActualAnnounceValue))) {
                    int chance = rnd.nextInt(10) + 1;
                    if (chance < (4 - courageFactor)) {
                        Coordinator.appWindow.outputMessage("<GÉP> Nem hiszem el!");
                        isComputerTrust = false;
                    } else {
                        Coordinator.appWindow.outputMessage("<GÉP> Elhiszem!");
                        isComputerTrust = true;
                    }
                } else {
                    Coordinator.appWindow.outputMessage("<GÉP> Elhiszem!");
                    isComputerTrust = true;
                }
                break;
            case ComputerTurn:
                isComputerTrust = true;
                if (whatIsStronger(computerActualThrowValue, playerActualAnnounceValue) == 0) {
                    if (isFirstTurn) {
                        int chance = rnd.nextInt(10) + 1;
                        if (chance > (8 - courageFactor) && !isItBash(computerActualThrowValue)) {
                            chance = rnd.nextInt(3) + 1;
                            int tmp = computerActualThrowValue;
                            for (int i = 0; i < chance; i++) {
                                tmp = nextBiggerThrow(tmp);
                            }
                            computerActualAnnounceValue = tmp;
                        } else {
                            computerActualAnnounceValue = computerActualThrowValue;
                        }
                    } else {
                        computerActualAnnounceValue = computerActualThrowValue;
                    }
                } else {
                    int chance = rnd.nextInt(4) + 1;
                    int tmp = computerActualThrowValue;
                    while (whatIsStronger(tmp, playerActualAnnounceValue) == 1) {
                        tmp = nextBiggerThrow(tmp);
                    }
                    for (int i = 0; i < chance; i++) {
                        tmp = nextBiggerThrow(tmp);
                    }
                    computerActualAnnounceValue = tmp;
                }
                break;
        }

    }

    public int whatIsStronger(int firstThrow, int secondThrow) {
        if (firstThrow == secondThrow) {
            return 2;
        } else if ((firstThrow > secondThrow && !isItBash(firstThrow) && !isItBash(secondThrow)
                && secondThrow != 21) || (firstThrow > secondThrow && isItBash(firstThrow) &&
                isItBash(secondThrow))
                || (firstThrow == 21) || (firstThrow < secondThrow && isItBash(firstThrow) &&
                !isItBash(secondThrow))
                || (firstThrow > secondThrow && isItBash(firstThrow) && !isItBash(secondThrow) &&
                secondThrow != 21)) {
            return 0;
        } else {
            return 1;
        }
    }

    private boolean isItBash(int x) {
        if (Integer.toString(x).length() != 2) {
            return false;
        } else return Integer.toString(x).charAt(0) == Integer.toString(x).charAt(1);
    }

    private boolean isValidThrowValue(int number) {
        if (Integer.toString(number).contains("0") || Integer.toString(number).contains("7")
                || Integer.toString(number).contains("8") || Integer.toString(number).contains
                ("9")) {
            comp_justSixSide++;
            Coordinator.appWindow.outputMessage("A kockának csak 6 oldala van!");
            return false;
        } else if (Integer.toString(number).length() != 2) {
            comp_tooSmall++;
            Coordinator.appWindow.outputMessage("Nem megfelelő hosszú a szám! A beadott szám: " +
                    number);
            return false;
        } else if (Character.getNumericValue(Integer.toString(number).charAt(0)) < Character
                .getNumericValue(Integer.toString(number).charAt(1))) {
            comp_wrongOrder++;
            Coordinator.appWindow.outputMessage("Nagyobb számot kell előre írni!");
            return false;
        } else {
            return true;
        }
    }

    private int nextBiggerThrow(int number) {

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

    private int randomInt(int from, int to) {
        int chance = rnd.nextInt(to - from + 1);
        chance = chance + from;
        return chance;
    }

    private int generateRandomThrow() {

        int firstDigit = randomInt(1, 6);
        int secondDigit = randomInt(1, 6);
        int actualThrow = 0;

        if (firstDigit > secondDigit) {
            actualThrow = Integer.parseInt(Integer.toString(firstDigit) + Integer.toString(secondDigit));

        } else {
            actualThrow = Integer.parseInt(Integer.toString(secondDigit) + Integer.toString(firstDigit));
        }

        return actualThrow;
    }


    @Override
    public void announce(int myThrow) {
        if (selectedExercise == "Bash - t dobott az ellenfél? <elágazás>") {

        }
        if (selectedExercise == "Érvényes volt a bemondás? <elágazás>") {

        }
        if (selectedExercise == "Mit mondott a gép legtöbbször? <tömbkezelés>") {
            playerActualAnnounceValue = myThrow;
        }
        if (selectedExercise == "Játék a gép ellen") {
            if (actualTurnState == turnState.ComputerTurn) {
                Coordinator.appWindow
                        .outputMessage("<JÁTÉK> Most ne számot mondj , hanem az ellenfél dobását " +
                                "kritizáld!");
            } else {
                if (isValidThrowValue(myThrow)) {
                    playerActualAnnounceValue = myThrow;
                    Coordinator.appWindow.outputMessage("<JÁTÉK> A bemondásod: " +
                            playerActualAnnounceValue);

                }
            }
        }

    }

    @Override
    public void myTrust(boolean myTrust) {
        if (selectedExercise == "Bash - t dobott az ellenfél? <elágazás>") {
            isPlayerTrust = myTrust;
        }
        if (selectedExercise == "Érvényes volt a bemondás? <elágazás>") {
            isPlayerTrust = myTrust;
        }
        if (selectedExercise == "Játék a gép ellen") {
            if (actualTurnState == turnState.PlayerTurn) {
                Coordinator.appWindow.outputMessage("<JÁTÉK> Most csak az ellenfél dobását " +
                        "kritizálhatod!");
            } else {
                isPlayerTrust = myTrust;
            }
        }
    }

    @Override
    public boolean isMyTurn() {
        if (selectedExercise == "Bash - t dobott az ellenfél? <elágazás>") {

        }
        if (selectedExercise == "Érvényes volt a bemondás? <elágazás>") {

        }
        if (selectedExercise == "Játék a gép ellen") {
            if (actualTurnState == turnState.PlayerTurn) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getMyActualThrowValue() {
        int tmp = 0;
        if (selectedExercise == "Bash - t dobott az ellenfél? <elágazás>") {

        }
        if (selectedExercise == "Érvényes volt a bemondás? <elágazás>") {

        }
        if (selectedExercise == "Játék a gép ellen") {
            tmp = playerActualThrowValue;
        }
        return tmp;
    }

    @Override
    public int getComputerLastAnnounceValue() {
        int tmp = 0;
        if (selectedExercise == "Bash - t dobott az ellenfél? <elágazás>") {
            tmp = computerActualAnnounceValue;
        }
        if (selectedExercise == "Érvényes volt a bemondás? <elágazás>") {
            tmp = computerActualAnnounceValue;
        }
        if (selectedExercise == "Mit mondott a gép legtöbbször? <tömbkezelés>") {
            tmp = computerActualAnnounceValue;
        }
        if (selectedExercise == "Játék a gép ellen") {
            tmp = computerActualAnnounceValue;
        }
        return tmp;
    }

    @Override
    public void print(String s) {
        Coordinator.appWindow.outputMessage("<PLAYER> " + s);

    }

    @Override
    public int getMyLastAnnounceValue() {
        int tmp = 0;
        if (selectedExercise == "Bash - t dobott az ellenfél? <elágazás>") {

        }
        if (selectedExercise == "Érvényes volt a bemondás? <elágazás>") {

        }
        if (selectedExercise == "Játék a gép ellen") {
            tmp = playerActualAnnounceValue;
        }
        return tmp;
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

    @Override
    public void whyNotValid(int justSixSide, int tooSmall, int wrongOrder) {
        if (selectedExercise == "Érvényes volt a bemondás? <elágazás>") {

            this.justSixSide = justSixSide;
            this.tooSmall = tooSmall;
            this.wrongOrder = wrongOrder;
        }

    }

}
