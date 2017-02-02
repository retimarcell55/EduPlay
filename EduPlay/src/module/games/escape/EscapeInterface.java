package module.games.escape;

import module.ApplicationModule;

public interface EscapeInterface extends ApplicationModule {
	
	//Lépés megadása, maximum egy mező távolságra (indexek 1-gyel kezdődnek)
	public void moveToCell(int row , int column);
	
	//Sprintelés lehetősége egyik cellából a másikba
	public void sprintToCell(int row , int column);
	
	//A player épp melyik sorban van (indexek 1-gyel kezdődnek)
	public int getPositionRow();
	
	//A playe épp melyik oszlopban van (indexek 1-gyel kezdődnek)
	public int getPositionColumn();
	
	//A teljes pálya képe visszakapható char mátrix formájában
	public char[][] getBoard();
	
	//támadás a fejszével, lehetséges irányok: UP,DOWN,RIGHT,LEFT,UPLEFT,UPRIGHT,DOWNLEFT,DOWNRIGHT
	//csak akkor támadhat, ha van még fejszéje, ha zombit talál vagy falat elhasználódik!
	public void axeAttack(String direction);
	
	//lövés a fegyverrel, lehetséges irányok: UP,DOWN,RIGHT,LEFT,UPLEFT,UPRIGHT,DOWNLEFT,DOWNRIGHT
	//csak akkor lőhet, ha van még lőszere.
	public void shoot(String direction);
	
	//Hányszor tudunk futni
	public int getSprint();
	
	//Hány lőszerünk van
	public int getAmmo();
	
	//Hány fejszénk van
	public int getAxe();
	
	//"Hol van a zombi? <ciklus , mátrixkezelés>" - hez
	public void zombiLocation(int row , int column);

}
