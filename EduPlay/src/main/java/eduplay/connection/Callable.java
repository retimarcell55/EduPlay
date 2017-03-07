package eduplay.connection;

import eduplay.module.ApplicationModule;

public interface Callable {
	
	void playerAction();
	
	<T extends ApplicationModule> void initializeSelectedModule(T app);
	
}
