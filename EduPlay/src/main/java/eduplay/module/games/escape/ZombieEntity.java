package eduplay.module.games.escape;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

import eduplay.connection.Coordinator;

public class ZombieEntity extends Entity {

	public ZombieEntity(String name, Position position) {
		super(name, position);

	}

	Random rnd;

	public void nextMove(char[][] gameBoard, int playerRow, int playerColumn) {

		int boardHeight = gameBoard.length;
		int boardWidth = gameBoard[0].length;

		List<Position> possibleNeighbours = Position.getNeighbour(new Position(getPositionRow(),
						getPositionColumn()), boardHeight,
				boardWidth);
		for (Position position : possibleNeighbours) {
			if (gameBoard[position.row][position.column] == 'p') {
				setPositionRow(position.row);
				setPositionColumn( position.column);
				return;
			}
		}

		int[][] distanceMatrix = new int[boardHeight][boardWidth];
		
		PriorityQueue<Position> positionQueue = new PriorityQueue<>(boardWidth * boardHeight,
				(o1, o2) -> {
                    if (distanceMatrix[o1.row][o1.column] < distanceMatrix[o2.row][o2.column])
                        return -1;
                    else if (distanceMatrix[o1.row][o1.column] > distanceMatrix[o2.row][o2.column])
                        return 1;
                    else
                        return 0;
                });
		
		distanceMatrix[playerRow][playerColumn] = 0;
		positionQueue.offer(new Position(playerRow, playerColumn));

		while (!positionQueue.isEmpty()) {

			Position current = positionQueue.poll();
			List<Position> neighbours = Position.getNeighbour(current, boardHeight, boardWidth);

			for (Position neighbour : neighbours) {
				if (!(gameBoard[neighbour.row][neighbour.column] == 'z') && !(gameBoard[neighbour.row][neighbour.column] == 'e')
						&& !(gameBoard[neighbour.row][neighbour.column] == 'p')
						&& !(gameBoard[neighbour.row][neighbour.column] == 'w')
						&& distanceMatrix[neighbour.row][neighbour.column] == 0) {

					distanceMatrix[neighbour.row][neighbour.column] = distanceMatrix[current.row][current.column] + 1;
					positionQueue.offer(neighbour);
				}
				
				if (neighbour.row == getPositionRow() && neighbour.column == getPositionColumn()) {

					int distance = distanceMatrix[current.row][current.column];
					rnd = new Random();

					List<Position> possibleSteps = new ArrayList<>();
					for (Position position : possibleNeighbours) {
						if (distanceMatrix[position.row][position.column] == distance) {
							possibleSteps.add(position);
						}
					}
					
					int whichStepToChoose = rnd.nextInt(possibleSteps.size());
					Coordinator.appWindow.outputMessage(getName() + " lépése: (" + getPositionRow() + "," + getPositionColumn()
							+ ") -> (" + possibleSteps.get(whichStepToChoose).row + "," + possibleSteps.get(whichStepToChoose).column +")");
					setPositionRow(possibleSteps.get(whichStepToChoose).row);
					setPositionColumn(possibleSteps.get(whichStepToChoose).column);
					positionQueue.clear();
					break;
				}

			}

		}

	}
}
