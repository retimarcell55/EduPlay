package eduplay.module.games.bash;

import eduplay.module.ApplicationModule;

public interface BashInterface extends ApplicationModule{

		void myTrust(boolean myTrust);

		boolean isMyTurn();

		int getMyActualThrowValue();

		int getComputerLastAnnounceValue();

		void announce(int myThrow);

		int getMyLastAnnounceValue();

		void whyNotValid(int justSixSide, int tooSmall, int wrongOrder);

}
