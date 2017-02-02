package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;

import connection.Coordinator;

public class MainWindow {

	private JFrame frame;
	private JComboBox<String> modules;
	private JButton more;
	private JButton start;
	private JButton exit;
	private JButton about;
	
	private Set<String> modulenames;
	
	public MainWindow() {
		initialize();
	}

	private void initialize() {
		
		modulenames = Coordinator.modules.keySet();
		
		frame = new JFrame();
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setBounds(dim.width/2-600/2, dim.height/2-300/2, 600, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("EduPlay");
		frame.setResizable(false);
		
		modules = new JComboBox(modulenames.toArray());
		modules.setSelectedIndex(0);
		modules.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		frame.getContentPane().add(modules,BorderLayout.PAGE_START);
		
		more = new JButton("Többet a modulról");
		more.setPreferredSize(new Dimension(150,150));
		frame.getContentPane().add(more,BorderLayout.LINE_START);
		
		start = new JButton("Indítás");
		start.setBackground(Color.CYAN);
		start.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				if(modules.getSelectedItem().toString() != "Válassz modult!") {
					frame.dispose();
					Coordinator.openApplication(modules.getSelectedItem().toString());
				}
				
			}
					
		});
		frame.getContentPane().add(start,BorderLayout.CENTER);
		
		about = new JButton("A Programról");
		about.setPreferredSize(new Dimension(150,150));
		frame.getContentPane().add(about,BorderLayout.LINE_END);
		
		exit = new JButton("Kilépés");
		exit.setBackground(Color.RED);
		exit.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		exit.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
				
			}
					
		});
		frame.getContentPane().add(exit,BorderLayout.PAGE_END);
		
		frame.setVisible(true);
	}

}
