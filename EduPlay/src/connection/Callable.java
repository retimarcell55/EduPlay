package connection;

import module.ApplicationModule;

public interface Callable {
	
	public void yourTurn();
	
	public <T extends ApplicationModule> void initialize(T app);
	
}
