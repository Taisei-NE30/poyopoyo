package poyopoyo;

public class OperatedPoyo{
	static Poyo rotatePoyo, basePoyo;
	private int rotatePos = 0; // 動くぷよの相対的な位置。 0:上 1:右 2:下 3:左

	public OperatedPoyo(int selectRotate, int selectBase) {
		rotatePoyo = new Poyo(3, 0, true, selectRotate); //こっちが回転する
		basePoyo = new Poyo(3, 1, true, selectBase); // こっちが始点になる
	}

	public void rotate(boolean isLeft) {
		// dir:回転の方向。 true:左, false:右
		if(isLeft) {
			switch(rotatePos) {
			// 上→左→下→右
			case 0:
				if(rotatePoyo.posX > 0) {
					if(rotatePoyo.moves(-1, 1)) {
						rotatePos = 3;
					}
				}
				break;
			case 3:
				if(rotatePoyo.moves(1, 1)) {
					rotatePos = 2;
				}
				break;
			case 2:
				if(rotatePoyo.posX < Game_Poyopoyo.NUMTILE_X - 1) {
					rotatePoyo.moves(1,  -1);
					rotatePos = 1;
				}

				break;
			case 1:
				rotatePoyo.moves(-1, -1);
				rotatePos = 0;
				break;

			}
		} else {
			// 上→右→下→左
			switch(rotatePos) {
			case 0:
				if(rotatePoyo.posX < Game_Poyopoyo.NUMTILE_X - 1) {
					rotatePoyo.moves(1, 1);
					rotatePos = 1;
				}
				break;
			case 1:
				rotatePoyo.moves(-1, 1);
				rotatePos = 2;
				break;
			case 2:
				if(rotatePoyo.posX > 0) {
					rotatePoyo.moves(-1,  -1);
					rotatePos = 3;
				}
				break;
			case 3:
				rotatePoyo.moves(1, -1);
				rotatePos = 0;
				break;
			}
		}
	}

	public boolean move(int x, int y) {
		return (rotatePoyo.moves(x, y) && basePoyo.moves(x, y));
	}

	public void changeOperatePoyo() {
		OperatedPoyo.basePoyo.isOperated = false;
		OperatedPoyo.rotatePoyo.isOperated = false;
	}

	public boolean droped() throws InterruptedException {
		// 存在判定はmovesでも行っているが、ここではmovesを呼び出さずに判定したいので必要
		if(Poyo.existsAt(OperatedPoyo.basePoyo.posX, OperatedPoyo.basePoyo.posY+1) != null ||
		   Poyo.existsAt(OperatedPoyo.rotatePoyo.posX, OperatedPoyo.rotatePoyo.posY+1) != null ||
		   OperatedPoyo.basePoyo.posY >= Game_Poyopoyo.NUMTILE_Y-1 ||
		   OperatedPoyo.rotatePoyo.posY >= Game_Poyopoyo.NUMTILE_Y-1) {
			this.changeOperatePoyo();
			Poyo.fall();
			return false;
		} else {
			this.move(0, 1);
			return true;
		}
	}


}
