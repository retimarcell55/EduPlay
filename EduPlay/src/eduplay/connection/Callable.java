package eduplay.connection;

import eduplay.module.ApplicationModule;

public interface Callable {
	
	void playerTurn();
	
	<T extends ApplicationModule> void initializeSelectedModule(T app);
	
}
