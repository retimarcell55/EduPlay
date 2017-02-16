package eduplay.module.games.escape;

import eduplay.module.ApplicationModule;

public interface EscapeInterface extends ApplicationModule {

	void moveToCell(int row , int column);

	void sprintToCell(int row , int column);

	int getPositionRow();

	int getPositionColumn();

	char[][] getBoard();

	void axeAttack(String direction);

	void shoot(String direction);

	int getSprint();

	int getAmmo();

	int getAxe();

	void zombieLocation(int row , int column);

}
