package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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
import javax.swing.JTextField;
import javax.swing.JTextPane;

import java.awt.GridLayout;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.awt.event.ActionEvent;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import connection.Coordinator;

import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.*;


public class ApplicationWindow {

	private JFrame frame;
	private JButton compile;
	private JPanel inputpanel;
	private JPanel outputpanel;
	private JPanel settingspanel;
	private JPanel panel;
	private RSyntaxTextArea input;
	private JTextArea output;
	private JLabel inputlabel;
	private JLabel outputlabel;
	private JButton rules;
	private JButton API;
	private JButton exit;
	
	JMenuBar menubar;
	JMenu exercisemenu;
	JButton description;
	JButton help;
	JLabel actualexercisename;
	JButton function;
	ArrayList<String> exercises;
	ArrayList<JMenuItem> exercisemenuitems;
	
	JScrollPane scrolli;
	JScrollPane scrollo;

	public ApplicationWindow() {
		initialize();
	}

	private void initialize() {
		frame = new JFrame();
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setBounds(dim.width/2-1000/2, dim.height/2-800/2, 1000, 800);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle(Coordinator.appmodule.getName());
		frame.setResizable(true);
		
		menubar = new JMenuBar();
		exercisemenu = new JMenu("Feladatok");
		
		exercises = Coordinator.appmodule.getExercises();
		exercisemenuitems = new ArrayList<JMenuItem>();
		
		for(String item : exercises) {
			JMenuItem tmp = new JMenuItem(item); 
			tmp.addActionListener(new ActionListener() {
			    public void actionPerformed(ActionEvent ev) {
			    	Coordinator.appmodule.setExercise(tmp.getText().toString());
			    	actualexercisename.setText("   Aktuális feladat: " + Coordinator.appmodule.getactualExercise().toString());
			    }
			});
			exercisemenuitems.add(tmp);
			
			exercisemenu.add(tmp);
		}
		
		description = new JButton("Aktuális feladat leírása");
		description.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(frame, Coordinator.appmodule.getDescription(), "Feladatleírás",
						JOptionPane.INFORMATION_MESSAGE);
			}
			
		});
		
		help = new JButton("Segítség");
		help.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(frame, Coordinator.appmodule.getHelp(), "Segítség",
						JOptionPane.INFORMATION_MESSAGE);
			}
			
		});
		
		actualexercisename = new JLabel("Aktuális feladat: " + Coordinator.appmodule.getactualExercise().toString());
		actualexercisename.setBorder(new EmptyBorder(0, 0, 0, 100));
		
		function = new JButton("Függvények");
		function.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						try {
							FunctionWindow fw = new FunctionWindow();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
				
			}
			
		});

		menubar.add(exercisemenu);
		menubar.add(description);
		menubar.add(help);
		menubar.add(function);
		menubar.add(Box.createHorizontalGlue());
		menubar.add(actualexercisename);
		frame.setJMenuBar(menubar);

		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

		inputpanel = new JPanel(new BorderLayout());
		inputpanel.setPreferredSize(new Dimension(1000, 300));

		inputlabel = new JLabel("Bemenet:");
		inputpanel.add(inputlabel, BorderLayout.PAGE_START);
		
		CompletionProvider provider = createCompletionProvider();
		AutoCompletion ac = new AutoCompletion(provider);
		
		input = new RSyntaxTextArea();
		input.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
		input.setCodeFoldingEnabled(true);
		
		ac.install(input);
		
		scrolli = new JScrollPane(input, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		inputpanel.add(scrolli, BorderLayout.CENTER);

		compile = new JButton("Fordítás");
		compile.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
					
				BufferedWriter writer;
				try {
					if((!input.getText().trim().equals(""))) {
						writer = new BufferedWriter(new FileWriter(Coordinator.filesource + "/Player.txt", false));
						input.write(writer);
						writer.close();
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				try {
					Coordinator.compile(input.getText().toString());
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
		inputpanel.add(compile, BorderLayout.PAGE_END);

		panel.add(inputpanel);

		outputpanel = new JPanel(new BorderLayout());
		outputpanel.setPreferredSize(new Dimension(1000, 300));

		outputlabel = new JLabel("Kimenet:");
		outputpanel.add(outputlabel, BorderLayout.PAGE_START);

		output = new JTextArea();
		output.setEditable(false);
		scrollo = new JScrollPane(output, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		outputpanel.add(scrollo, BorderLayout.CENTER);

		panel.add(outputpanel);

		settingspanel = new JPanel(new BorderLayout());

		rules = new JButton("Szabályok");
		rules.setPreferredSize(new Dimension(250, 50));
		rules.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(frame, Coordinator.appmodule.getRules(), "Szabályok",
						JOptionPane.INFORMATION_MESSAGE);
			}

		});
		settingspanel.add(rules, BorderLayout.LINE_START);

		API = new JButton("API");
		API.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(frame, Coordinator.appmodule.getAPI(), "API",
						JOptionPane.INFORMATION_MESSAGE);
			}

		});
		settingspanel.add(API, BorderLayout.CENTER);

		exit = new JButton("Kilépés");
		exit.setPreferredSize(new Dimension(250, 50));
		exit.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				frame.dispose();
				Coordinator.returning();
			}

		});
		settingspanel.add(exit, BorderLayout.LINE_END);

		panel.add(settingspanel);

		frame.getContentPane().add(panel, BorderLayout.CENTER);

		frame.setVisible(true);
		
	}

	public void outputMessage(String message) {
		output.append(message + "\n");
	}

	public void clearMessage() {
		output.setText("");
	}
	
	private CompletionProvider createCompletionProvider() {
	      DefaultCompletionProvider provider = new DefaultCompletionProvider();

	      provider.addCompletion(new BasicCompletion(provider, "if() {}"));
	      provider.addCompletion(new BasicCompletion(provider, "else if() {}"));
	      provider.addCompletion(new BasicCompletion(provider, "while(true) {}"));
	      provider.addCompletion(new BasicCompletion(provider, "else {}"));
	      provider.addCompletion(new BasicCompletion(provider, "int"));
	      provider.addCompletion(new BasicCompletion(provider, "String"));
	      provider.addCompletion(new BasicCompletion(provider, "for(int i = 0 ; i < length; i++) {}"));
	      provider.addCompletion(new BasicCompletion(provider, "String"));

	      return provider;

	   }
	
}
