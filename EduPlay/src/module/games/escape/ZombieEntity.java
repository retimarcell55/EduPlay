package module.games.escape;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

import connection.Coordinator;

public class ZombieEntity extends Entity {

	public ZombieEntity(String name, int positionRow, int positionColumn) {
		super(name, positionRow, positionColumn);

	}

	Random rnd;

	public void intelligence(char[][] gameBoard, int playerRow, int playerColumn) {

		int boardHeight = gameBoard.length;
		int boardWidth = gameBoard[0].length;

		List<Position> possibleNeighbours = getNeighbour(new Position(this.positionRow, this.positionColumn), boardHeight,
				boardWidth);
		for (Position position : possibleNeighbours) {
			if (gameBoard[position.row][position.column] == 'p') {
				this.positionRow = position.row;
				this.positionColumn = position.column;
				return;
			}
		}

		int[][] distanceMatrix = new int[boardHeight][boardWidth];
		
		PriorityQueue<Position> positionQueue = new PriorityQueue<Position>(boardWidth * boardHeight,
				new Comparator<Position>() {

					@Override
					public int compare(Position o1, Position o2) {
						if (distanceMatrix[o1.row][o1.column] < distanceMatrix[o2.row][o2.column])
							return -1;
						else if (distanceMatrix[o1.row][o1.column] > distanceMatrix[o2.row][o2.column])
							return 1;
						else
							return 0;
					}
				});
		
		distanceMatrix[playerRow][playerColumn] = 0;
		positionQueue.offer(new Position(playerRow, playerColumn));

		while (!positionQueue.isEmpty()) {

			Position current = positionQueue.poll();
			List<Position> neighbours = getNeighbour(current, boardHeight, boardWidth);

			for (Position neighbour : neighbours) {
				if (!(gameBoard[neighbour.row][neighbour.column] == 'z') && !(gameBoard[neighbour.row][neighbour.column] == 'e')
						&& !(gameBoard[neighbour.row][neighbour.column] == 'p')
						&& !(gameBoard[neighbour.row][neighbour.column] == 'w')
						&& distanceMatrix[neighbour.row][neighbour.column] == 0) {

					distanceMatrix[neighbour.row][neighbour.column] = distanceMatrix[current.row][current.column] + 1;
					positionQueue.offer(neighbour);
				}
				
				if (neighbour.row == this.positionRow && neighbour.column == this.positionColumn) {

					int distance = distanceMatrix[current.row][current.column];
					rnd = new Random();

					List<Position> possibleSteps = new ArrayList<Position>();
					for (Position position : possibleNeighbours) {
						if (distanceMatrix[position.row][position.column] == distance) {
							possibleSteps.add(position);
						}
					}
					
					int whichStepToChoose = rnd.nextInt(possibleSteps.size());
					Coordinator.appWindow.outputMessage(getName() + " lépése: (" + getPositionRow() + "," + getPositionColumn()
							+ ") -> (" + possibleSteps.get(whichStepToChoose).row + "," + possibleSteps.get(whichStepToChoose).column +")");
					this.positionRow = possibleSteps.get(whichStepToChoose).row;
					this.positionColumn = possibleSteps.get(whichStepToChoose).column;

					positionQueue.clear();
					break;
				}

			}

		}

	}
}
