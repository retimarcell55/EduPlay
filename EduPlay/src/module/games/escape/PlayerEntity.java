package module.games.escape;

public class PlayerEntity extends Entity {

	private int sprint;
	private int ammo;
	private int axe;

	public PlayerEntity(String name, int posrow, int poscolumn) {
		super(name, posrow, poscolumn);
		setSprint(0);
		setAmmo(0);
		setAxe(0);
	}

	public void equip(int sprint, int ammo, int axe) {
		this.setSprint(sprint);
		this.setAmmo(ammo);
		this.setAxe(axe);
	}

	public int getSprint() {
		return sprint;
	}

	public void setSprint(int sprint) {
		this.sprint = sprint;
	}

	public int getAmmo() {
		return ammo;
	}

	public void setAmmo(int ammo) {
		this.ammo = ammo;
	}


	public int getAxe() {
		return axe;
	}

	public void setAxe(int axe) {
		this.axe = axe;
	}

}
