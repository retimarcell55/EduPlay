package eduplay.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import eduplay.connection.Coordinator;

public class MainWindow {

	private JFrame mainFrame;
	private JComboBox<String> modulesComboBox;
	private JButton moreButton;
	private JButton startButton;
	private JButton aboutButton;
	private JLabel moduleLabel;
	
	private Set<String> moduleNames;
	
	public MainWindow() {
		initialize();
	}

	private void initialize() {
		
		moduleNames = Coordinator.modules.keySet();
		
		mainFrame = new JFrame();
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		mainFrame.setBounds(dimension.width/2-600/2, dimension.height/2-300/2, 400, 200);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setTitle("EduPlay");
		mainFrame.setResizable(false);

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout(10,10));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        Container topContainer = new Container();
        topContainer.setLayout(new BorderLayout(10,10));

		moduleLabel = new JLabel("Válassz modult!");

		topContainer.add(moduleLabel,BorderLayout.PAGE_START);

		modulesComboBox = new JComboBox(moduleNames.toArray());
		modulesComboBox.setSelectedIndex(0);
        topContainer.add(modulesComboBox,BorderLayout.CENTER);

        mainPanel.add(topContainer,BorderLayout.NORTH);

        JPanel middlePanel = new JPanel();
        middlePanel.setLayout(new BorderLayout(10,10));

		startButton = new JButton("Modul Indítása");
		startButton.setBackground(Color.CYAN);
		startButton.addActionListener(e -> {

            if(modulesComboBox.getSelectedItem().toString() != "Válassz modult!") {
                mainFrame.dispose();
                Coordinator.openApplication(modulesComboBox.getSelectedItem().toString());
            }

        });

        startButton.setPreferredSize(new Dimension(175,30));
        middlePanel.add(startButton, BorderLayout.LINE_START);

        moreButton = new JButton("Többet a modulról");
        moreButton.setPreferredSize(new Dimension(175,30));
        middlePanel.add(moreButton,BorderLayout.LINE_END);

        mainPanel.add(middlePanel,BorderLayout.CENTER);

        Container bottomContainer = new Container();
        bottomContainer.setLayout(new BorderLayout(10,10));
		
		aboutButton = new JButton("Többet az EduPlay-ről");
        aboutButton.setPreferredSize(new Dimension(175,40));
        bottomContainer.add(aboutButton,BorderLayout.LINE_END);

        mainPanel.add(bottomContainer,BorderLayout.SOUTH);

        mainFrame.getContentPane().add(mainPanel);

		mainFrame.setVisible(true);
	}

}
