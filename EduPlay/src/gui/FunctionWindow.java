package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import connection.Coordinator;

import org.fife.ui.rsyntaxtextarea.*;

public class FunctionWindow {

	private JFrame mainFrame;
	
	private JPanel mainPanel;
	
	private JLabel functionLabel;
	private RSyntaxTextArea inputTextArea;
	private JButton saveButton;
	JScrollPane mainScrollPane;

	public FunctionWindow() {
		initialize();
	}

	private void initialize() {
		mainFrame = new JFrame();
		mainFrame.getContentPane().setLayout(new BoxLayout(mainFrame.getContentPane(), BoxLayout.PAGE_AXIS));
		
		mainPanel = new JPanel(new BorderLayout(0,0));
		
		functionLabel = new JLabel("F�ggv�nyek �r�sa:");
		mainPanel.add(functionLabel,BorderLayout.NORTH);
		
		inputTextArea = new RSyntaxTextArea();
		inputTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
		inputTextArea.setCodeFoldingEnabled(true);
		
		File playerDeclarationFile = new File(Coordinator.FILE_SOURCE + "/Helper.txt");
		if(playerDeclarationFile.exists()) {
			String content = "";
			try {
				BufferedReader br = new BufferedReader(new FileReader(Coordinator.FILE_SOURCE + "/Helper.txt"));
				if (br.readLine() != null) {
					content = new Scanner(new File(Coordinator.FILE_SOURCE + "/Helper.txt")).useDelimiter("\\Z").next();
				}
			} catch (Exception e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			inputTextArea.setText(content);
		}
		
		mainScrollPane = new JScrollPane(inputTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
		JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		mainPanel.add(mainScrollPane,BorderLayout.CENTER);
		
		saveButton = new JButton("F�ggv�nyek ment�se");
		saveButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				BufferedWriter writer;
				try {
					writer = new BufferedWriter(new FileWriter(Coordinator.FILE_SOURCE + "/Helper.txt", false));
					inputTextArea.write(writer);
					writer.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				
			}
			
		});
		mainPanel.add(saveButton,BorderLayout.SOUTH);
		
		mainFrame.getContentPane().add(mainPanel);
		
		mainFrame.setTitle("F�ggv�nyek");
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		mainFrame.setBounds(dimension.width/2-700/2, dimension.height/2-400/2, 700, 400);
		
		mainFrame.setVisible(true);
	}

}
