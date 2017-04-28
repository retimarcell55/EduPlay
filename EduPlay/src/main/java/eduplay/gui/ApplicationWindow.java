package eduplay.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import java.awt.Toolkit;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.awt.event.ActionEvent;
import javax.swing.border.EmptyBorder;

import eduplay.connection.Coordinator;

import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.rsyntaxtextarea.*;


public class ApplicationWindow {

	private JFrame mainFrame;
	private JButton compileButton;
	private JPanel inputPanel;
	private JPanel outputPanel;
	private JPanel settingsPanel;
	private JPanel mainPanel;
	private RSyntaxTextArea inputTextField;
	private JTextArea outputTextField;
	private JLabel inputLabel;
	private JLabel outputLabel;
	private JButton rulesButton;
	private JButton apiButton;
	private JButton exitButton;
	
	JMenuBar mainMenuBar;
	JMenu exerciseMenu;
	JButton descriptionButton;
	JButton helpButton;
	JLabel selectedExerciseLabel;
	JButton functionButton;
	ArrayList<String> exercises;
	ArrayList<JMenuItem> exerciseMenuItems;
	
	JScrollPane inputScrollPane;
	JScrollPane outputScrollPane;

	public ApplicationWindow() {
		initialize();
	}

	private void initialize() {
		mainFrame = new JFrame();
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		mainFrame.setBounds(dimension.width/2-1000/2, dimension.height/2-800/2, 1000, 800);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setTitle(Coordinator.appModule.getName());
		mainFrame.setResizable(true);
		
		mainMenuBar = new JMenuBar();
		exerciseMenu = new JMenu("Feladatok");
		
		exercises = Coordinator.appModule.getExercises();
		exerciseMenuItems = new ArrayList<JMenuItem>();
		
		for(String exercise : exercises) {
			JMenuItem newMenuItem = new JMenuItem(exercise);
			newMenuItem.addActionListener(new ActionListener() {
			    public void actionPerformed(ActionEvent ev) {
			    	Coordinator.appModule.setExercise(newMenuItem.getText().toString());
			    	selectedExerciseLabel.setText("   Aktualis feladat: " + Coordinator.appModule.getSelectedExercise().toString());
			    }
			});
			exerciseMenuItems.add(newMenuItem);
			
			exerciseMenu.add(newMenuItem);
		}
		
		descriptionButton = new JButton("Aktu�lis feladat le�r�sa");
		descriptionButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(mainFrame, Coordinator.appModule.getDescription(), "Feladatle�r�s",
						JOptionPane.INFORMATION_MESSAGE);
			}
			
		});
		
		helpButton = new JButton("Seg�ts�g");
		helpButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(mainFrame, Coordinator.appModule.getHelp(), "Seg�ts�g",
						JOptionPane.INFORMATION_MESSAGE);
			}
			
		});
		
		selectedExerciseLabel = new JLabel("Aktu�lis feladat: " + Coordinator.appModule.getSelectedExercise().toString());
		selectedExerciseLabel.setBorder(new EmptyBorder(0, 0, 0, 100));
		
		functionButton = new JButton("F�ggv�nyek");
		functionButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						try {
							FunctionWindow functionWindow = new FunctionWindow();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
				
			}
			
		});

		mainMenuBar.add(exerciseMenu);
		mainMenuBar.add(descriptionButton);
		mainMenuBar.add(helpButton);
		mainMenuBar.add(functionButton);
		mainMenuBar.add(Box.createHorizontalGlue());
		mainMenuBar.add(selectedExerciseLabel);
		mainFrame.setJMenuBar(mainMenuBar);

		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

		inputPanel = new JPanel(new BorderLayout());
		inputPanel.setPreferredSize(new Dimension(1000, 300));

		inputLabel = new JLabel("Bemenet:");
		inputPanel.add(inputLabel, BorderLayout.PAGE_START);
		
		CompletionProvider provider = createCompletionProvider();
		AutoCompletion ac = new AutoCompletion(provider);
		
		inputTextField = new RSyntaxTextArea();
		inputTextField.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
		inputTextField.setCodeFoldingEnabled(true);
		
		ac.install(inputTextField);
		
		inputScrollPane = new JScrollPane(inputTextField, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		inputPanel.add(inputScrollPane, BorderLayout.CENTER);

		compileButton = new JButton("Ford�t�s");
		compileButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
					
				BufferedWriter writer;
				try {
					if((!inputTextField.getText().trim().equals(""))) {
						writer = new BufferedWriter(new FileWriter(Coordinator.FILE_SOURCE + "/Player.txt", false));
						inputTextField.write(writer);
						writer.close();
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				try {
					Coordinator.compileAndStartExercise();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		});
		inputPanel.add(compileButton, BorderLayout.PAGE_END);

		mainPanel.add(inputPanel);

		outputPanel = new JPanel(new BorderLayout());
		outputPanel.setPreferredSize(new Dimension(1000, 300));

		outputLabel = new JLabel("Kimenet:");
		outputPanel.add(outputLabel, BorderLayout.PAGE_START);

		outputTextField = new JTextArea();
		outputTextField.setEditable(false);
		outputScrollPane = new JScrollPane(outputTextField, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		outputPanel.add(outputScrollPane, BorderLayout.CENTER);

		mainPanel.add(outputPanel);

		settingsPanel = new JPanel(new BorderLayout());

		rulesButton = new JButton("Szab�lyok");
		rulesButton.setPreferredSize(new Dimension(250, 50));
		rulesButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(mainFrame, Coordinator.appModule.getRules(), "Szab�lyok",
						JOptionPane.INFORMATION_MESSAGE);
			}

		});
		settingsPanel.add(rulesButton, BorderLayout.LINE_START);

		apiButton = new JButton("API");
		apiButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(mainFrame, Coordinator.appModule.getApi(), "API",
						JOptionPane.INFORMATION_MESSAGE);
			}

		});
		settingsPanel.add(apiButton, BorderLayout.CENTER);

		exitButton = new JButton("Kil�p�s");
		exitButton.setPreferredSize(new Dimension(250, 50));
		exitButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				mainFrame.dispose();
				Coordinator.returnFromApplicationWindow();
			}

		});
		settingsPanel.add(exitButton, BorderLayout.LINE_END);

		mainPanel.add(settingsPanel);

		mainFrame.getContentPane().add(mainPanel, BorderLayout.CENTER);

		mainFrame.setVisible(true);
		
	}

	public void outputMessage(String message) {
		outputTextField.append(message + "\n");
	}

	public void clearMessage() {
		outputTextField.setText("");
	}

	public String getPlayerActionCode() {
		return inputTextField.getText().toString();
	}
	
	private CompletionProvider createCompletionProvider() {
	      DefaultCompletionProvider completionProvider = new DefaultCompletionProvider();

	      completionProvider.addCompletion(new BasicCompletion(completionProvider, "if() {}"));
	      completionProvider.addCompletion(new BasicCompletion(completionProvider, "else if() {}"));
	      completionProvider.addCompletion(new BasicCompletion(completionProvider, "while(true) {}"));
	      completionProvider.addCompletion(new BasicCompletion(completionProvider, "else {}"));
	      completionProvider.addCompletion(new BasicCompletion(completionProvider, "int"));
	      completionProvider.addCompletion(new BasicCompletion(completionProvider, "String"));
	      completionProvider.addCompletion(new BasicCompletion(completionProvider, "for(int i = 0 ; i < length; i++) {}"));
	      completionProvider.addCompletion(new BasicCompletion(completionProvider, "String"));

	      return completionProvider;

	   }
	
}
