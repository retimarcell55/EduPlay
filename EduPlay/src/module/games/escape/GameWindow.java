package module.games.escape;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
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

import connection.Coordinator;

public class GameWindow{
	
	private int labelsize = 70;
	private JFrame frame;
	private JLabel[][] board;
	private JPanel panel;
	
	private BufferedImage zombie;
	private BufferedImage player;
	private BufferedImage grass;
	private BufferedImage exit;
	private BufferedImage wall;

	public GameWindow(int row, int column) {
		super();
		initialize(row , column);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize(int row , int column) {
		frame = new JFrame();
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setResizable(false);
		frame.getContentPane().setBackground( Color.red );
		frame.setTitle("Játék ablak");
		
		frame.addWindowListener(new WindowListener() {

			@Override
			public void windowOpened(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowClosing(WindowEvent e) {
				// TODO Auto-generated method stub
				frame = null;
			}

			@Override
			public void windowClosed(WindowEvent e) {
				// TODO Auto-generated method stub
				frame = null;
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
		
		board = new JLabel[row][column];
		
		try {
			grass = ImageIO.read(new File("images/grass.png"));
			player = ImageIO.read(new File("images/player.png"));
			zombie = ImageIO.read(new File("images/zombie.png"));
			exit = ImageIO.read(new File("images/exit.jpg"));
			wall = ImageIO.read(new File("images/wall.jpg"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		panel = new JPanel();
	    panel.setLayout(new GridLayout(row, column));
	    for (int i = 0; i < row; i++) {
	    	for (int j = 0; j < column; j++) {
				JLabel tmp = new JLabel();
				tmp.setPreferredSize(new Dimension(labelsize,labelsize));
				panel.add(tmp);
				board[i][j] = tmp;
			}
		}
		
		frame.add(panel);
		frame.pack();
		Rectangle r = frame.getBounds();
		frame.setBounds(dim.width/2-r.width/2, dim.height/2-r.height/2, r.width, r.height);
		frame.setVisible(true);
	}
	private void clearBoard() {
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[0].length; j++) {
				board[i][j].setIcon(new ImageIcon(grass.getScaledInstance(labelsize, labelsize,Image.SCALE_SMOOTH)));
			}
		}
	}
	
	//Itt a rajzolás, jobb képeket még keresni kell!
	public void drawBoard(char[][] gameboard) {
		
		clearBoard();
		
		for (int i = 0; i < gameboard.length; i++) {
			for (int j = 0; j < gameboard[i].length; j++) {
				BufferedImage img = null;
				if(gameboard[i][j] == 'p') {
					img = player;
				} else if(gameboard[i][j] == 'z') {
					img = zombie;
				} else if(gameboard[i][j] == 'e') {
					img = exit;
				} else if(gameboard[i][j] == 'w') {
					img = wall;
				} else {
					continue;
				}
				Image icon = img.getScaledInstance(labelsize, labelsize,Image.SCALE_SMOOTH);
				board[i][j].setIcon(new ImageIcon(icon));
			}
		}
		frame.revalidate();
	}
	
	public boolean isFrameActive () {
		if (frame == null) {
			return false;
		} else {
			return true;
		}
	}

}
