package connection;

import module.ApplicationModule;

public interface Callable {
	
	void playerTurn();
	
	<T extends ApplicationModule> void initializeSelectedModule(T app);
	
}
