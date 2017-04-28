package eduplay.module;

import java.util.ArrayList;

public interface ApplicationModule {

	String getName();

	String getRules();

	String getApi();
	
	String getDescription();
	
	String getHelp();

	void playSelectedExercise();

	void print(String s);
	
	ArrayList<String> getExercises();
	
	void setExercise(String exercise);
	
	String getSelectedExercise();

}
