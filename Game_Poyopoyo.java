package poyopoyo;

import java.util.Random;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Game_Poyopoyo extends Application{
	static final int TILESIZE = 32;
	static final int NUMTILE_X = 6;
	static final int NUMTILE_Y = 13;
	static final int PUZZLE_W = TILESIZE * NUMTILE_X;
	static final int PUZZLE_H = TILESIZE * NUMTILE_Y;
	static OperatedPoyo operatedPoyo;
	static Game_Poyopoyo game;
	static int time = 120;
	static int rensaCount = 0;
	static int rensaMax = 0;
	static int deletePoyoCount = 0;

	private Random r = new Random();
	Poyo[] nextPoyo = new Poyo[2];
	int nextRotateColor = (int) (r.nextDouble() * 5);
	int nextBaseColor = (int) (r.nextDouble() * 5);
	int nextNextRotateColor = (int) (r.nextDouble() * 5);
	int nextNextBaseColor = (int) (r.nextDouble() * 5);

	private Canvas canvas;
	Stage stage;
	Timeline timer;
	private ImageView nextRotate = new ImageView(new Image(this.getClass().
            getResourceAsStream(Poyo.COLOR[nextRotateColor] + ".png")));
	private ImageView nextBase = new ImageView(new Image(this.getClass().
								   getResourceAsStream(Poyo.COLOR[this.nextBaseColor] + ".png")));
	private ImageView nextNextRotate =  new ImageView(new Image(this.getClass().
			   getResourceAsStream(Poyo.COLOR[this.nextNextRotateColor] + ".png")));
	private ImageView nextNextBase = new ImageView(new Image(this.getClass().
			   getResourceAsStream(Poyo.COLOR[this.nextNextBaseColor] + ".png")));

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		Game_Poyopoyo.game = this;
		HBox root = new HBox();
		this.canvas = new Canvas(Game_Poyopoyo.PUZZLE_W, Game_Poyopoyo.PUZZLE_H);
		VBox info = new VBox();

		HBox nextArea = new HBox();

		VBox nextSet = new VBox();
		nextSet.getChildren().addAll(nextRotate, nextBase);
		VBox nextNextSet = new VBox();
		nextNextSet.getChildren().addAll(nextNextRotate, nextNextBase);

		nextArea.getChildren().addAll(nextSet, nextNextSet);

		VBox textArea = new VBox();
		textArea.setAlignment(Pos.TOP_CENTER);
		textArea.setPadding(new Insets(160, 0, 0, 0));
		Label time = new Label(String.valueOf(Game_Poyopoyo.time));
		time.setStyle("-fx-font-size:32px;");
		textArea.getChildren().addAll(time);
		info.getChildren().addAll(nextArea, textArea);
		root.getChildren().addAll(this.canvas, info);

		this.draw();
		Scene scene = new Scene(root);
		scene.setOnKeyPressed(e -> {
			switch(e.getCode()) {
			case A:
				Game_Poyopoyo.operatedPoyo.rotate(true);
				break;

			case D:
				Game_Poyopoyo.operatedPoyo.rotate(false);
				break;

			case LEFT:
				if(OperatedPoyo.basePoyo.posX > 0 && OperatedPoyo.rotatePoyo.posX > 0) {
					operatedPoyo.move(-1, 0);
				}
				break;

			case RIGHT:
				if(OperatedPoyo.basePoyo.posX < Game_Poyopoyo.NUMTILE_X-1 &&
					OperatedPoyo.rotatePoyo.posX < Game_Poyopoyo.NUMTILE_X-1) {
					operatedPoyo.move(1, 0);
				}
				break;

			case DOWN:
				try {
					operatedPoyo.droped();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				break;

			default:
				break;
			}

			Poyo.fall();
			this.draw();
		});

		this.timer = new Timeline(new KeyFrame(Duration.millis(999), e -> {
			Game_Poyopoyo.time--;
			time.setText(String.valueOf(Game_Poyopoyo.time));
			try {
				if(!operatedPoyo.droped() && !Poyo.delete()) {
					if(rensaMax < rensaCount) rensaMax = rensaCount;
					rensaCount = 0;
					pushNextPoyo();
				}
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			this.draw();
			if(Game_Poyopoyo.time <= 0) {
				this.timer.stop();
				this.call(primaryStage);
			}
		}));
		this.timer.setCycleCount(Timeline.INDEFINITE);

		primaryStage.setTitle("poyopoyo");
		primaryStage.setScene(scene);
		primaryStage.show();
		this.timer.play();

		this.stage = primaryStage;
	}

	private void call(Stage stage) {
		VBox root = new VBox();
		root.setAlignment(Pos.TOP_CENTER);
		root.setStyle("-fx-font-size: 16px;");
		Label numRensa = new Label("最大連鎖数:" + rensaMax);
		numRensa.setStyle("-fx-padding: 80, 80;");
		Label numDeletePoyo = new Label("消したぽよの数:" + deletePoyoCount);
		root.getChildren().addAll(numRensa, numDeletePoyo);
		Scene gameOver = new Scene(root, stage.getWidth(), stage.getHeight());
		stage.setScene(gameOver);
		stage.show();
	}

	void draw() {
		if (this.canvas != null) {
			GraphicsContext gc = this.canvas.getGraphicsContext2D();
			gc.clearRect(0, 0, PUZZLE_W, PUZZLE_H);

			gc.setLineWidth(5.0);
				gc.strokeLine(0, 0, 0, TILESIZE * NUMTILE_Y);
				gc.strokeLine(NUMTILE_X * TILESIZE, 0, NUMTILE_X * TILESIZE, TILESIZE * NUMTILE_Y);

				gc.strokeLine(0, 0, TILESIZE * NUMTILE_X, 0);
				gc.strokeLine(0, TILESIZE * NUMTILE_Y, TILESIZE * NUMTILE_X, TILESIZE * NUMTILE_Y);
			Poyo.paintPoyos(gc);
		}
	}

	public void init() throws Exception {
		synchronized(Poyo.poyos) {
			operatedPoyo = new OperatedPoyo((int) (r.nextDouble() * 5),
					                        (int) (r.nextDouble() * 5));
		}
	}

	public void pushNextPoyo() {
		synchronized(Poyo.poyos) {
			operatedPoyo = new OperatedPoyo(nextRotateColor, nextBaseColor);
			nextRotateColor = nextNextRotateColor;
			nextRotate.setImage(new Image(this.getClass().
	                getResourceAsStream(Poyo.COLOR[nextRotateColor] + ".png")));

			nextBaseColor = nextNextBaseColor;
			nextBase.setImage(new Image(this.getClass().
					   getResourceAsStream(Poyo.COLOR[nextBaseColor] + ".png")));

			nextNextRotateColor = (int) (r.nextDouble() * 5);
			nextNextRotate.setImage(new Image(this.getClass().
					   getResourceAsStream(Poyo.COLOR[this.nextNextRotateColor] + ".png")));

			nextNextBaseColor = (int) (r.nextDouble() * 5);
			nextNextBase.setImage(new Image(this.getClass().
					   getResourceAsStream(Poyo.COLOR[this.nextNextBaseColor] + ".png")));
		}
	}


}
