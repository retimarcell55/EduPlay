package module.games.escape;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class GameWindow{
	
	private final int LABEL_SIZE = 70;
	private JFrame mainFrame;
	private JLabel[][] gameLabelBoard;
	private JPanel mainPanel;
	
	private BufferedImage zombieImage;
	private BufferedImage playerImage;
	private BufferedImage grassImage;
	private BufferedImage exitImage;
	private BufferedImage wallImage;

	public GameWindow(int row, int column) {
		super();
		initialize(row , column);
	}

	private void initialize(int row , int column) {
		mainFrame = new JFrame();
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		mainFrame.setResizable(false);
		mainFrame.getContentPane().setBackground( Color.red );
		mainFrame.setTitle("J�t�k ablak");
		
		mainFrame.addWindowListener(new WindowListener() {

			@Override
			public void windowOpened(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowClosing(WindowEvent e) {
				// TODO Auto-generated method stub
				mainFrame = null;
			}

			@Override
			public void windowClosed(WindowEvent e) {
				// TODO Auto-generated method stub
				mainFrame = null;
			}

			@Override
			public void windowIconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowActivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		gameLabelBoard = new JLabel[row][column];
		
		try {
			grassImage = ImageIO.read(new File("images/grassImage.png"));
			playerImage = ImageIO.read(new File("images/playerImage.png"));
			zombieImage = ImageIO.read(new File("images/zombieImage.png"));
			exitImage = ImageIO.read(new File("images/exitImage.jpg"));
			wallImage = ImageIO.read(new File("images/wallImage.jpg"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		mainPanel = new JPanel();
	    mainPanel.setLayout(new GridLayout(row, column));
	    for (int i = 0; i < row; i++) {
	    	for (int j = 0; j < column; j++) {
				JLabel newLabel = new JLabel();
				newLabel.setPreferredSize(new Dimension(LABEL_SIZE, LABEL_SIZE));
				mainPanel.add(newLabel);
				gameLabelBoard[i][j] = newLabel;
			}
		}
		
		mainFrame.add(mainPanel);
		mainFrame.pack();
		Rectangle rect = mainFrame.getBounds();
		mainFrame.setBounds(dimension.width/2-rect.width/2, dimension.height/2-rect.height/2, rect.width, rect.height);
		mainFrame.setVisible(true);
	}
	private void clearBoard() {
		for (int i = 0; i < gameLabelBoard.length; i++) {
			for (int j = 0; j < gameLabelBoard[0].length; j++) {
				gameLabelBoard[i][j].setIcon(new ImageIcon(grassImage.getScaledInstance(LABEL_SIZE, LABEL_SIZE,Image.SCALE_SMOOTH)));
			}
		}
	}

	public void drawBoard(char[][] gameboard) {
		
		clearBoard();
		
		for (int i = 0; i < gameboard.length; i++) {
			for (int j = 0; j < gameboard[i].length; j++) {
				BufferedImage img = null;
				if(gameboard[i][j] == 'p') {
					img = playerImage;
				} else if(gameboard[i][j] == 'z') {
					img = zombieImage;
				} else if(gameboard[i][j] == 'e') {
					img = exitImage;
				} else if(gameboard[i][j] == 'w') {
					img = wallImage;
				} else {
					continue;
				}
				Image icon = img.getScaledInstance(LABEL_SIZE, LABEL_SIZE,Image.SCALE_SMOOTH);
				gameLabelBoard[i][j].setIcon(new ImageIcon(icon));
			}
		}
		mainFrame.revalidate();
	}
	
	public boolean isFrameActive () {
		if (mainFrame == null) {
			return false;
		} else {
			return true;
		}
	}

}
