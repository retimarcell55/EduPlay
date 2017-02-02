package module.games.escape;

public abstract class Environment {
	private int posrow;
	private int poscolumn;
	
	public Environment(int posrow, int poscolumn) {
		this.setPosrow(posrow);
		this.setPoscolumn(poscolumn);
	}

	public int getPosrow() {
		return posrow;
	}

	public void setPosrow(int posrow) {
		this.posrow = posrow;
	}

	public int getPoscolumn() {
		return poscolumn;
	}

	public void setPoscolumn(int poscolumn) {
		this.poscolumn = poscolumn;
	}
}
