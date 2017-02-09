package module.games.escape;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

import connection.Coordinator;

public class ZombieEntity extends Entity {

	public ZombieEntity(String name, int posrow, int poscolumn) {
		super(name, posrow, poscolumn);

	}

	Random rnd;

	// Az intelligencia a player helyzetétől függő, minden körben egy blokkot
	// közelít, Lee algoritmussal
	public void intelligence(char board[][], int pr, int pc) {

		int rowlength = board.length;
		int columnlength = board[0].length;

		// Ha a player 1 távolságra van természetesen a zombi rálép!
		List<Position> possibleneighbours = getNeighbour(new Position(this.posrow, this.poscolumn), rowlength,
				columnlength);
		for (Position position : possibleneighbours) {
			if (board[position.row][position.col] == 'p') {
				this.posrow = position.row;
				this.poscolumn = position.col;
				return;
			}
		}

		// mátrix amelyben a távolságértékek lesznek benne (8 szomszédság)
		int[][] distancematrix = new int[rowlength][columnlength];

		// A már vizsgált pozíciók tárolódnak benne
		PriorityQueue<Position> queue = new PriorityQueue<Position>(columnlength * rowlength,
				new Comparator<Position>() {

					@Override
					public int compare(Position o1, Position o2) {
						if (distancematrix[o1.row][o1.col] < distancematrix[o2.row][o2.col])
							return -1;
						else if (distancematrix[o1.row][o1.col] > distancematrix[o2.row][o2.col])
							return 1;
						else
							return 0;
					}
				});

		// Az algoritmus a player helyzetétől indul az aktuális zombi felé
		distancematrix[pr][pc] = 0;
		queue.offer(new Position(pr, pc));

		while (!queue.isEmpty()) {

			Position current = queue.poll();
			List<Position> neighbours = getNeighbour(current, rowlength, columnlength);

			for (Position neighbour : neighbours) {
				// Akadályok felsorolása
				if (!(board[neighbour.row][neighbour.col] == 'z') && !(board[neighbour.row][neighbour.col] == 'e')
						&& !(board[neighbour.row][neighbour.col] == 'p')
						&& !(board[neighbour.row][neighbour.col] == 'w')
						&& distancematrix[neighbour.row][neighbour.col] == 0) {

					distancematrix[neighbour.row][neighbour.col] = distancematrix[current.row][current.col] + 1;
					queue.offer(neighbour);
				}

				// Ha megtalálta az adott zombit, a mellette lévő legnagyobb
				// mezőértékek reprezentálják a legrövidebb út utolsó lehetséges
				// lépéseit,
				// ebből fog választani a zombi véletlenszerűen
				if (neighbour.row == this.posrow && neighbour.col == this.poscolumn) {

					int distance = distancematrix[current.row][current.col];
					rnd = new Random();

					List<Position> possiblesteps = new ArrayList<Position>();
					for (Position position : possibleneighbours) {
						if (distancematrix[position.row][position.col] == distance) {
							possiblesteps.add(position);
						}
					}

					// A lehetséges lépések közűl kiválaszt egyet random (amik a
					// legrövidebb úthoz tartoznak)
					int whichstep = rnd.nextInt(possiblesteps.size());
					Coordinator.appWindow.outputMessage(getName() + " lépése: (" + getPosrow() + "," + getPoscolumn()
							+ ") -> (" + possiblesteps.get(whichstep).row + "," + possiblesteps.get(whichstep).col +")");
					this.posrow = possiblesteps.get(whichstep).row;
					this.poscolumn = possiblesteps.get(whichstep).col;

					queue.clear();
					break;
				}

			}

		}

	}
}
