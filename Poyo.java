package poyopoyo;

import java.util.ArrayList;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Poyo {
	private Image image;
	protected int posX;
	protected int posY;
	private int selectColor;
	boolean isChecked = false;
	boolean isDeleted = false;
	static ArrayList<Poyo> poyos = new ArrayList<Poyo>();
	static final String[] COLOR = {"red", "blue", "green", "yellow", "purple"};
	boolean isOperated;
	static int adjCount = 0;

	public Poyo(int posX, int posY, boolean isOperated, int selectColor) {
		this.posX = posX;
		this.posY = posY;
		this.isOperated = isOperated;
		this.selectColor = selectColor;
		image = new Image(this.getClass().getResourceAsStream(COLOR[this.selectColor] + ".png"));
		Poyo.poyos.add(this);
	}

	static public void paintPoyos(GraphicsContext gc) {
		synchronized(Poyo.poyos) {
			for(int i = 0; i < Poyo.poyos.size(); i++) {
				Poyo poyo = Poyo.poyos.get(i);
				gc.drawImage(poyo.image, poyo.posX * Game_Poyopoyo.TILESIZE, poyo.posY * Game_Poyopoyo.TILESIZE);
			}
		}
	}

	public boolean moves(int x, int y) {
		int nextX = posX + x;
		int nextY = posY + y;
		Poyo ahead = Poyo.existsAt(nextX, nextY);
		if(ahead instanceof Poyo) {
			return false;
		}
		posX += x;
		posY += y;
		return true;
	}

	static public Poyo existsAt(int x, int y) {
		synchronized (Poyo.poyos) {
			for (int i = 0; i < Poyo.poyos.size(); i++) {
				Poyo poyo = Poyo.poyos.get(i);
				if((poyo.posX == x) && (poyo.posY == y) && poyo.isOperated == false) {
					return poyo;
				}
			}
			return null;
		}
	}

	static public void fall() {
		synchronized (Poyo.poyos) {
			for(int i = 0; i < Poyo.poyos.size(); i++) {
				Poyo poyo = Poyo.poyos.get(i);
				if(poyo.posY < Game_Poyopoyo.NUMTILE_Y-1 && Poyo.existsAt(poyo.posX, poyo.posY+1) == null &&
				   poyo.isOperated == false) {
					poyo.moves(0, 1);
					Poyo.fall();
				}
			}
		}
	}

	static public void checkAdj(Poyo start) {
		adjCount++;
		start.isChecked = true;
		Poyo adjTop = Poyo.existsAt(start.posX, start.posY-1);
		Poyo adjBottom = Poyo.existsAt(start.posX, start.posY+1);
		Poyo adjLeft = Poyo.existsAt(start.posX-1, start.posY);
		Poyo adjRight = Poyo.existsAt(start.posX+1, start.posY);

		if(adjTop instanceof Poyo && adjTop.selectColor == start.selectColor && !adjTop.isChecked) {
			Poyo.checkAdj(adjTop);
		}

		if(adjBottom instanceof Poyo && adjBottom.selectColor == start.selectColor && !adjBottom.isChecked) {
			Poyo.checkAdj(adjBottom);
		}

		if(adjLeft instanceof Poyo && adjLeft.selectColor == start.selectColor && !adjLeft.isChecked) {
			Poyo.checkAdj(adjLeft);
		}

		if(adjRight instanceof Poyo && adjRight.selectColor == start.selectColor && !adjRight.isChecked) {
			Poyo.checkAdj(adjRight);
		}

		if(adjCount > 3 && start.isChecked) {
			start.isDeleted = true;
		}
		start.isChecked = false;
	}

	static public boolean delete() throws InterruptedException {
		boolean result = false;
		ArrayList<Poyo> deletePoyos = new ArrayList<Poyo>();
		synchronized(Poyo.poyos) {
			for(int i = 0; i < Poyo.poyos.size(); i++) {
				Poyo poyo = Poyo.poyos.get(i);
				Poyo.checkAdj(poyo);
				Poyo.adjCount = 0;
				if(poyo.isDeleted == true) {
					Game_Poyopoyo.deletePoyoCount++;
					deletePoyos.add(poyo);
					result = true;
				}
			}
			Poyo.poyos.removeAll(deletePoyos);
			Poyo.fall();
		}
		if(result) Game_Poyopoyo.rensaCount++;
		return result;
	}

}
