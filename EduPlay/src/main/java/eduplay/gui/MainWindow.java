package eduplay.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;

import eduplay.connection.Coordinator;

public class MainWindow {

	private JFrame mainFrame;
	private JComboBox<String> modulesComboBox;
	private JButton moreButton;
	private JButton startButton;
	private JButton exitButton;
	private JButton aboutButton;
	
	private Set<String> moduleNames;
	
	public MainWindow() {
		initialize();
	}

	private void initialize() {
		
		moduleNames = Coordinator.modules.keySet();
		
		mainFrame = new JFrame();
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		mainFrame.setBounds(dimension.width/2-600/2, dimension.height/2-300/2, 600, 300);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setTitle("EduPlay");
		mainFrame.setResizable(false);
		
		modulesComboBox = new JComboBox(moduleNames.toArray());
		modulesComboBox.setSelectedIndex(0);
		modulesComboBox.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		mainFrame.getContentPane().add(modulesComboBox,BorderLayout.PAGE_START);
		
		moreButton = new JButton("T�bbet a modulr�l");
		moreButton.setPreferredSize(new Dimension(150,150));
		mainFrame.getContentPane().add(moreButton,BorderLayout.LINE_START);
		
		startButton = new JButton("Indítás");
		startButton.setBackground(Color.CYAN);
		startButton.addActionListener(e -> {

            if(modulesComboBox.getSelectedItem().toString() != "V�lassz modult!") {
                mainFrame.dispose();
                Coordinator.openApplication(modulesComboBox.getSelectedItem().toString());
            }

        });
		mainFrame.getContentPane().add(startButton,BorderLayout.CENTER);
		
		aboutButton = new JButton("A Programr�l");
		aboutButton.setPreferredSize(new Dimension(150,150));
		mainFrame.getContentPane().add(aboutButton,BorderLayout.LINE_END);
		
		exitButton = new JButton("Kil�p�s");
		exitButton.setBackground(Color.RED);
		exitButton.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		exitButton.addActionListener(e -> mainFrame.dispose());
		mainFrame.getContentPane().add(exitButton,BorderLayout.PAGE_END);
		
		mainFrame.setVisible(true);
	}

}
