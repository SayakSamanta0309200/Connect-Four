package com.internshala.connectfour;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Controller implements Initializable
{
	public static final int COLUMNS=7;
	public static final int ROWS=6;
	public static final int CIRCLE_DIAMETER=70;
	public static final String discColor1="#24303E";
	public static final String discColor2="#4CAA88";


	private static String PLAYER_ONE= "Player One";
	private static String PLAYER_TWO= "Player Two";

	private boolean isPlayerOneTurn=true;

	private Disc[][] insertedDiscsArray=new Disc[ROWS][COLUMNS]; //for structural Changes

	@FXML
	public GridPane rootGridpane;
	@FXML
	public Pane insertedDiscsPane;
	@FXML
	public Label playerNameLabel;

	private boolean isAllowedToInsert=true; //Flag to avoid same color disc being added

	public void createPlayground()
	{

		Shape rectangleWithHoles=createGameStructuralGrid();
		rootGridpane.add(rectangleWithHoles,0,1);

		ArrayList<Rectangle> rectangleList=createClickableColumns();
		for(Rectangle rectangle: rectangleList)
		{
			rootGridpane.add(rectangle,0,1);
		}
	}

	private Shape createGameStructuralGrid()
	{
		Shape rectangleWithHoles=new Rectangle((COLUMNS+1) * CIRCLE_DIAMETER, (ROWS+1) * CIRCLE_DIAMETER);

		for(int row=0;row<ROWS;row++)
		{
			for(int col=0;col<COLUMNS;col++)
			{
				Circle circle=new Circle();
				circle.setRadius(CIRCLE_DIAMETER/2);
				circle.setCenterX(CIRCLE_DIAMETER/2);
				circle.setCenterY(CIRCLE_DIAMETER/2);
				circle.setSmooth(true);

				circle.setTranslateX(col * (CIRCLE_DIAMETER+5)+CIRCLE_DIAMETER/4);
				circle.setTranslateY(row * (CIRCLE_DIAMETER+5)+CIRCLE_DIAMETER/4);

				rectangleWithHoles=Shape.subtract(rectangleWithHoles,circle);
			}
		}

		rectangleWithHoles.setFill(Color.WHITE);
		return rectangleWithHoles;
	}
	private ArrayList<Rectangle> createClickableColumns()
	{
		ArrayList<Rectangle> rectangleList=new ArrayList<>();
		for(int col=0;col<COLUMNS;col++)
		{
			Rectangle rectangle=new Rectangle(CIRCLE_DIAMETER,(ROWS+1) * CIRCLE_DIAMETER);
			rectangle.setFill(Color.TRANSPARENT);
			rectangle.setTranslateX(col * (CIRCLE_DIAMETER+5)+CIRCLE_DIAMETER/4);

			rectangle.setOnMouseEntered(mouseEvent -> rectangle.setFill(Color.valueOf(":#eeeeee26")));
			rectangle.setOnMouseExited(mouseEvent -> rectangle.setFill(Color.TRANSPARENT));

			final int column=col;
			rectangle.setOnMouseClicked(mouseEvent -> {
				if(isAllowedToInsert) {
					isAllowedToInsert=false; //when a disc is being dropped no more disc is being inserted
					insertDisc(new Disc(isPlayerOneTurn), column);
				}
			});
			rectangleList.add(rectangle);
		}

		return rectangleList;
	}
	private void insertDisc(Disc disc,int column) //to insert disc at a specific position
	{
		int row=ROWS-1;
		while(row>=0) //checking which row is empty
		{
			if(getDiscIfPresent(row,column)==null)
				break;

			row--;
		}
		if(row<0) //if it is full, we cannot insert anymore discs
			return;
		insertedDiscsArray[row][column]=disc;     //placing the disc on the array
		insertedDiscsPane.getChildren().add(disc); //for visual changes added disc to disc pane
		disc.setTranslateX(column * (CIRCLE_DIAMETER+5)+CIRCLE_DIAMETER/4); //translate animation along x axis

		int currentRow=row;
		TranslateTransition translateTransition=new TranslateTransition(Duration.seconds(0.5),disc);
		translateTransition.setToY(row * (CIRCLE_DIAMETER+5)+CIRCLE_DIAMETER/4); //translate animation along y axis
		translateTransition.setOnFinished(actionEvent -> {

			isAllowedToInsert=true; //Finally, when disc is dropped allow next player to insert disc
			if(gameEnded(currentRow,column)){
				gameOver();
				return;
			}
			//togglign between player one and two
			isPlayerOneTurn=!isPlayerOneTurn;
			playerNameLabel.setText(isPlayerOneTurn? PLAYER_ONE:PLAYER_TWO); //setting text for player one and two
		});
		translateTransition.play();
	}
	private boolean gameEnded(int row,int column){
		List<Point2D> verticalPoints= IntStream.rangeClosed(row-3,row+3) //range of row values
									  .mapToObj(r->new Point2D(r,column)) //0,3 1,3 2,3 3,3 4,3 5,3 -> Point2D
									  .collect(Collectors.toList());

		List<Point2D> horizontalPoints= IntStream.rangeClosed(column-3,column+3) //range of horizontal values
				.mapToObj(col->new Point2D(row,col))
				.collect(Collectors.toList());

		Point2D startPoint1=new Point2D(row-3,column+3);
		List<Point2D> diagonal1Points=IntStream.rangeClosed(0,6)
				.mapToObj(i-> startPoint1.add(i,-i))
				.collect(Collectors.toList());

		Point2D startPoint2=new Point2D(row-3,column-3);
		List<Point2D> diagonal2Points=IntStream.rangeClosed(0,6)
				.mapToObj(i-> startPoint2.add(i,i))
				.collect(Collectors.toList());

		boolean isEnded = checkCombinations(verticalPoints) || checkCombinations(horizontalPoints)
				|| checkCombinations(diagonal1Points)
				|| checkCombinations(diagonal2Points);
		return isEnded;
	}

	private boolean checkCombinations(List<Point2D> points) {
		int chain=0;
		for(Point2D point:points){

			int rowIndexForArray= (int) point.getX();
			int columnIndexForArray= (int) point.getY();

			Disc disc=getDiscIfPresent(rowIndexForArray,columnIndexForArray);

			if(disc != null && disc.isPlayerOneMove==isPlayerOneTurn){ //if the lst inserted disc belongs to the current player
				chain++;
				if(chain==4)
					return true; //for getting four possible combinations
			}else{
				chain=0;
			}
		}
		return false; //for getting no possible combination
	}
	private Disc getDiscIfPresent(int row,int column){  //to prevent array index out of bounds exception
		if(row>=ROWS || row<0 || column >= COLUMNS || column<0)
			return null;
		return insertedDiscsArray[row][column];
	}
	private void gameOver(){
		String winner=isPlayerOneTurn? PLAYER_ONE:PLAYER_TWO;
		System.out.println("Winner is: "+winner);

		Alert alert=new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Connect Four");
		alert.setHeaderText("The winner is "+winner);
		alert.setContentText("Want to play again?");

		ButtonType yesBtn=new ButtonType("Yes");
		ButtonType noBtn=new ButtonType("No, Exit");
		alert.getButtonTypes().setAll(yesBtn,noBtn);

		Platform.runLater(()->{

			Optional<ButtonType> btnClicked = alert.showAndWait();
			if(btnClicked.isPresent() && btnClicked.get() == yesBtn){
				resetGame();
			}else{
				Platform.exit();
				System.exit(0);
			}
		});
	}

	public void resetGame() {
		insertedDiscsPane.getChildren().clear(); //Remove all inserted disc form pane

		for (int row=0;row<insertedDiscsArray.length;row++){ //Structurally, make all elements of insertedDiscsArray[][] to null
			for(int col=0;col<insertedDiscsArray[row].length;col++){
				insertedDiscsArray[row][col]=null;
			}
		}

		isPlayerOneTurn=true; //let player one start the game
		playerNameLabel.setText(PLAYER_ONE);

		createPlayground(); //create fresh playground
	}

	private static class Disc extends Circle
	{
		private final boolean isPlayerOneMove;
		public Disc(boolean isPlayerOneMove)
		{
			this.isPlayerOneMove=isPlayerOneMove;
			setRadius(CIRCLE_DIAMETER/2);
			setFill(isPlayerOneMove? Color.valueOf(discColor1):Color.valueOf(discColor2));
			setCenterX(CIRCLE_DIAMETER/2);
			setCenterY(CIRCLE_DIAMETER/2);
		}
	}
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {

	}
}