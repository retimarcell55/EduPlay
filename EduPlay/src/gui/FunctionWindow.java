package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.LayoutManager;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
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
import javax.swing.JTextArea;

import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import connection.Coordinator;

import java.awt.Component;

import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.*;

public class FunctionWindow {

	private JFrame frame;
	
	private JPanel panelf;
	
	private JLabel funcwrite;
	private RSyntaxTextArea funcwritearea;
	private JButton save;
	JScrollPane scrollw;

	public FunctionWindow() {
		initialize();
	}

	private void initialize() {
		frame = new JFrame();
		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.PAGE_AXIS));
		
		panelf = new JPanel(new BorderLayout(0,0));
		
		funcwrite = new JLabel("F�ggv�nyek �r�sa:");
		panelf.add(funcwrite,BorderLayout.NORTH);
		
		funcwritearea = new RSyntaxTextArea();
		funcwritearea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
		funcwritearea.setCodeFoldingEnabled(true);
		
		File f = new File(Coordinator.filesource + "/Helper.txt");
		if(f.exists()) {
			String content = "";
			try {
				BufferedReader br = new BufferedReader(new FileReader(Coordinator.filesource + "/Helper.txt"));     
				if (br.readLine() != null) {
					content = new Scanner(new File(Coordinator.filesource + "/Helper.txt")).useDelimiter("\\Z").next();
				}
			} catch (Exception e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			funcwritearea.setText(content);
		}
		
		scrollw = new JScrollPane(funcwritearea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
		JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		panelf.add(scrollw,BorderLayout.CENTER);
		
		save = new JButton("F�ggv�nyek ment�se");
		save.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				BufferedWriter writer;
				try {
					writer = new BufferedWriter(new FileWriter(Coordinator.filesource + "/Helper.txt", false));
					funcwritearea.write(writer);
					writer.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				
			}
			
		});
		panelf.add(save,BorderLayout.SOUTH);
		
		frame.getContentPane().add(panelf);
		
		frame.setTitle("F�ggv�nyek");
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setBounds(dim.width/2-700/2, dim.height/2-400/2, 700, 400);
		
		frame.setVisible(true);
	}

}
