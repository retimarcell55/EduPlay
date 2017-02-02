package module;

import java.util.ArrayList;

import connection.Callable;

public interface ApplicationModule {

	public String getName();

	public String getRules();

	public String getAPI();
	
	public String getDescription();
	
	public String getHelp();

	// A játékot lebonyolító függvény
	public void play();
	
	// Kiírathatunk a program output mezőjébe, amit szeretnénk
	public void print(String s);
	
	//----------ezeket nem hívhatja meg a felhasználó, de egyelőre így marad------------
	
	public ArrayList<String> getExercises();
	
	public void setExercise(String ex);
	
	public String getactualExercise();

}
