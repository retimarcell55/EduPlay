package module.games.bash;

import module.ApplicationModule;

public interface BashInterface extends ApplicationModule{
	
		// Ha az ellenfél dobott, akkor ezzel a függvénnyel döntheted el, hogy
		// igazat mondott - e vagy sem
		public void believe(boolean b);

		// A függvény segítségével lehet lekérdezni, hogy az adott körben a játékos
		// dobott - e vagy sem
		public boolean isMyTurn();

		// Lekérdezhető a player legutóbbi dobásának értéke
		public int actualThrow();

		// Lekérdezhető a computer legutóbbi bemondásának értéke
		public int computersaidThrow();
		
		// Ha a te dobsz, akkor ezzel a függvénnyel mondhatsz be számot
		public void say(int number);

		// Lekérdezhető a player legutóbbi bemondásának értéke
		public int mysaidThrow();
		
		//Csak a validációvizsgálatnál elérhető, meg kell indokolni, hogy miért nem jó egy adott dobás
		//A függvényen keresztül küldjük be a programnak, hogy eddig mennyit találtunk
		public void whyNotValid(int justSixSide, int tooSmall, int wrongOrder);

}
