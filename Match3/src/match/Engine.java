package match;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.ScalableGame;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.SpriteSheet;

public class Engine extends BasicGame {
	
	private static SpriteSheet spritesheet = null;
	public static int boardOffsetX = 280, boardOffsetY = 80; // Game board offset
	public static int spriteSize = 64;
	public static Gem[][] gems;
	private static int mouseX, mouseY; 					  // Mouse position
	private static Gem selectedGem = null;
	public static boolean gemMoving = false;			  // Holds whether or not a gem is moving, if true then input is not received
	public static int score = 0, highscore = 0;
	public static int gem_score = 5;
	public static int consecutiveMatchesMultiplier = 1;
	public static final double consecutiveMatchTime = 2.0d;
	public static double consecutiveMatchTimer = consecutiveMatchTime;
	public static java.awt.Rectangle bNewGame, bToggleTimer, bPauseGame;
	static boolean mouse_down = false, timerToggledThisGame = false, timerOn = true, gameOver, gamePaused;
	static final double timerMax = 15.0d, matchTimeIncrease = 1d;
	static final int timerWidth = 500, timerHeight = 15, timerX = boardOffsetX + 6, timerY = 55, consecutiveMatchMax = 6;
	static double currentTimer = timerMax;	
	
	public Engine(String title) {
		super(title);
	}
	
	public static void main(String[] args) {
		try {
			ScalableGame sg = new ScalableGame(new Engine("Match3"), 800, 600, true);
			AppGameContainer appgc = new AppGameContainer(sg);
			appgc.setDisplayMode(appgc.getScreenWidth(), appgc.getScreenHeight(), false);
			appgc.setFullscreen(true);
			appgc.start();
		} catch (SlickException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void render(GameContainer gc, Graphics g) throws SlickException {
		g.setColor(Color.gray);
		g.fillRect(0, 0, 800, 600);
		renderBoard(g);
		renderGems(g);
		renderUI(g);
		renderFloatingNumbers(g);
	}
	
	public void renderBoard(Graphics g) {
		spritesheet.startUse();
		for(int i = 0; i < 8; i++) {
			for(int k = 0; k < 8; k ++) {
				java.awt.Rectangle gemRect = new java.awt.Rectangle(i * spriteSize + boardOffsetX, k * spriteSize + boardOffsetY, spriteSize, spriteSize);
				spritesheet.renderInUse(gemRect.x, gemRect.y, (i + k)%2 == 0 ? 1 : 2, 1);
				// Draws a circle in the square the mouse is in (if that square is not selected of course)
				if(gemRect.contains(mouseX, mouseY) && (selectedGem == null || (selectedGem != null && (selectedGem.getSlotX() != i || selectedGem.getSlotY() != k)))) {
					spritesheet.renderInUse(gemRect.x, gemRect.y, 3, 1);
				}
			}
		}
		spritesheet.endUse();
	}
	
	public void renderFloatingNumbers(Graphics g) {
		g.setColor(Color.white);
		for(int i = 0; i < FloatingNumber.active.size(); i++) {
			FloatingNumber number = FloatingNumber.active.get(i);
			g.drawString(number.value, number.x(), number.y());
		}
	}
	
	public void renderUI(Graphics g) {
		g.setColor(Color.white);
		g.drawString("SCORE: " + score, 25, 25);
		g.drawString("HIGHSCORE: " + highscore, 25, 50);
		
		g.setColor(Color.darkGray);
		if(bNewGame.contains(mouseX, mouseY) && mouse_down)
			spritesheet.getSubImage(2, 2).draw(bNewGame.x, bNewGame.y, bNewGame.width, bNewGame.height);
		else if(bNewGame.contains(mouseX, mouseY))
			spritesheet.getSubImage(1, 2).draw(bNewGame.x, bNewGame.y, bNewGame.width, bNewGame.height);
		else 
			spritesheet.getSubImage(0, 2).draw(bNewGame.x, bNewGame.y, bNewGame.width, bNewGame.height);

		if(bToggleTimer.contains(mouseX, mouseY) && mouse_down)
			spritesheet.getSubImage(2, 2).draw(bToggleTimer.x, bToggleTimer.y, bToggleTimer.width, bToggleTimer.height);
		else if(bToggleTimer.contains(mouseX, mouseY))
			spritesheet.getSubImage(1, 2).draw(bToggleTimer.x, bToggleTimer.y, bToggleTimer.width, bToggleTimer.height);
		else 
			spritesheet.getSubImage(0, 2).draw(bToggleTimer.x, bToggleTimer.y, bToggleTimer.width, bToggleTimer.height);
		
		if(bPauseGame.contains(mouseX, mouseY) && mouse_down)
			spritesheet.getSubImage(2, 2).draw(bPauseGame.x, bPauseGame.y, bPauseGame.width, bPauseGame.height);
		else if(bPauseGame.contains(mouseX, mouseY))
			spritesheet.getSubImage(1, 2).draw(bPauseGame.x, bPauseGame.y, bPauseGame.width, bPauseGame.height);
		else 
			spritesheet.getSubImage(0, 2).draw(bPauseGame.x, bPauseGame.y, bPauseGame.width, bPauseGame.height);
		
		g.setColor(Color.white);
		g.drawString("New Game", bNewGame.x + 26, bNewGame.y + 10);
		g.drawString("Toggle Timer", bToggleTimer.x + 9, bToggleTimer.y + 10);
		g.drawString("Pause Game", bPauseGame.x + 16, bPauseGame.y + 10);
		
		if(timerToggledThisGame) {
			g.setColor(Color.red);
			g.drawString("*High score saving disabled*", bNewGame.x, bNewGame.y + 50 + 64);
		}
		
		// Timer
		g.setColor(Color.black);
		g.fillRect(timerX, timerY, timerWidth, timerHeight);
		
		// Sets timer color depending on whether or not it is active
		if(timerOn && !gameOver && !gamePaused) {
			g.setColor(Color.magenta);
		} else {
			g.setColor(Color.darkGray);
		}
		
		// Makes it so the timer bar doesn't go outside of the bounds of the black bar
		if(currentTimer > 0 && currentTimer < timerMax)
			g.fillRect(timerX, timerY, (int) (timerWidth * (currentTimer/timerMax)), timerHeight);
		else if(currentTimer > 0 && currentTimer >= timerMax) 
			g.fillRect(timerX, timerY, (int) (timerWidth), timerHeight);
		
		// Game lost or paused
		if(gameOver || gamePaused) {
			g.setColor(new Color(0, 0, 0, .75f));
			g.fillRect(boardOffsetX, boardOffsetY, 512, 512);
		}
		
		if(gameOver) {
			g.setColor(Color.white);
			g.drawString("Game Over!", boardOffsetX + 216, boardOffsetY + 242);
		}
		
		if(gamePaused && !gameOver) {
			g.setColor(Color.white);
			g.drawString("Paused", boardOffsetX + 216, boardOffsetY + 242);
		}
	}
	
	public void renderGems(Graphics g) {
		spritesheet.startUse();
		for(int i = 0; i < gems.length; i++) {
			for(int k = 0; k < gems[i].length; k++) {
				if(gems[i][k] == null || gems[i][k].visible == false)
					continue;
				
				spritesheet.renderInUse(gems[i][k].x(), gems[i][k].y(), gems[i][k].tx(), gems[i][k].ty());
				// Renders box around selected gem
				if(selectedGem != null && i == selectedGem.getSlotX() && k == selectedGem.getSlotY())
					spritesheet.renderInUse(i * spriteSize + boardOffsetX, k * spriteSize + boardOffsetY, 4, 1);
			}
		}
		spritesheet.endUse();
		
		for(int i = 0; i < gems.length; i++) {
			for(int k = 0; k < gems[i].length; k++) {
				if(gems[i][k] != null && gems[i][k].matched) {
					g.setColor(new Color(0, 0, 0, .25f));
					g.fillRect(i * spriteSize + boardOffsetX, k * spriteSize + boardOffsetY, spriteSize, spriteSize);
				}
			}
		}
	}
	
	@Override
	public void init(GameContainer gc) throws SlickException {
		// Loads the spritesheet
		try {
			spritesheet = new SpriteSheet(new Image("assets/spritesheet.png").getScaledCopy(2.0f), 64, 64);
			spritesheet.setFilter(SpriteSheet.FILTER_NEAREST);
		} catch(Exception e) {
			System.out.println("Could not load spritesheet...exiting");
			System.exit(1);
		}
		
		bNewGame = new java.awt.Rectangle(5, 80, 128, 64);
		bToggleTimer = new java.awt.Rectangle(140, 80, 128, 64);
		bPauseGame = new java.awt.Rectangle(72, 140, 128, 64);
		loadHighScore();
		
		startGame();
	}
	
	public void loadHighScore() {
		File scoreFile = new File("highscore.txt");
		
		if(!scoreFile.exists()) {
			highscore = 0;
		} else {
			try {
				String fileContents = new String(Files.readAllBytes(scoreFile.toPath()));
				if(!fileContents.equals(""))
					highscore = Integer.parseInt(fileContents);
			} catch(Exception e) {
				System.err.println("Could not load highscore!!" + " " + e.getMessage());
			}
		}
	}
	
	public synchronized void saveHighScore() {
		if(score > highscore && !timerToggledThisGame) {
			highscore = score;
			
			File scoreFile = new File("highscore.txt");
			if(scoreFile.exists())
				scoreFile.delete();
			try {
				scoreFile.createNewFile();
				FileWriter fr = new FileWriter(scoreFile);
				BufferedWriter br = new BufferedWriter(fr);
				br.write(score + "");
				br.close();
			} catch (IOException e) {
				System.err.println("Could not create new high score file: " + e.getMessage());
			}
		} else {
			// Do nothing			
		}
	}
	
	public void addTimeToGameTimer(double amount) {
		currentTimer += amount;
		if(currentTimer > timerMax) {
			currentTimer = timerMax;
		}
	}
	
	public void updateGameTimer(double deltaT) {
		currentTimer -= deltaT;
		
		if(currentTimer <= 0) {
			gameOver = true;
			selectedGem = null;
		}
	}
	
	public void exitGame() {
		saveHighScore();
		System.exit(0);
	}
	
	@Override
	public void update(GameContainer gc, int _delta) throws SlickException {
		mouseX = gc.getInput().getMouseX();
		mouseY = gc.getInput().getMouseY();
		
		double deltaT =	_delta/1000d;
		
		// Saves new high score if current score is greater than high score
		saveHighScore();
		
		// Timer update
		if(timerOn && !gameOver && !gamePaused) 
			updateGameTimer(deltaT);
		
		// Updates all gems
		boolean prevGemMoving = gemMoving;
		gemMoving = false;
		for(int i = 0; i < 8; i++) {
			for(int k = 0; k < 8; k++) {
				if(gems[i][k] != null)
					gems[i][k].update(deltaT);
			}
		}
		
		boolean resolveMatches = true;
		// Checks if there are null values below, if so, this gem is moved to them (gravity)
		for(int k = 0; k < 8; k++) {
			for(int i = 7; i >= 0; i--) {
				if(gems[k][i] == null)
					continue; 
				
				if(!gemMoving) {
					for(int j = i; j < 8; j++) {
						if(gems[k][j] == null) {
							gems[k][i].setTargetSlot(k, i + 1);
							gems[k][i + 1] = gems[k][i];
							gems[k][i] = null;
							resolveMatches = false;
							break;
						}
					}
				}
			}
		}
		
		for(int i = 0; i < 8; i++) {
			if(gems[i][0] == null && !gemMoving) {
				gems[i][0] = new Gem(i, 0);
				gems[i][0].visible = false;
			}
		}
		
		// System.out.println(FloatingNumber.active.size());
		for(int i = 0; i < FloatingNumber.active.size(); i++) {
			FloatingNumber.active.get(i).update(deltaT);
		}
		
		if(prevGemMoving == true && gemMoving == false && resolveMatches) {
			resolveMatches();
		}
		
		consecutiveMatchTimer -= deltaT;
		if(consecutiveMatchTimer <= 0) {
			consecutiveMatchesMultiplier = 1;
		}
	}
	
	public void startGame() {
		gems = new Gem[8][8];
		for(int i = 0; i < gems.length; i++){
			for(int k = 0; k < gems[i].length; k++) {
				gems[i][k] = new Gem(i, k);
			}
		}
		selectedGem = null;
		score = 0;
		timerToggledThisGame = false;
		timerOn = true;
		currentTimer = timerMax;
		gameOver = false;
		gamePaused = false;
		
		// Makes sure that the board contains no matches at the start
		// this should probably be replaced by something more efficient
		// but i have a feeling it wont be
		if(boardContainsMatch()) 
			startGame();
	}
	
	public void keyPressed(int key, char c) {
		// Escape exits the game
		if(key == 1) {
			exitGame();
		}
	}
	
	@Override
	public void mousePressed(int button, int x, int y) {
		mouse_down = true;
		
		// Checks button presses
		if(bNewGame.contains(mouseX, mouseY)) {
			startGame();
		}
		
		if(bToggleTimer.contains(mouseX, mouseY) && !gamePaused && !gameOver) {
			timerToggledThisGame = true;
			timerOn = !timerOn;
		}
		
		if(bPauseGame.contains(mouseX, mouseY) && !gameOver) {
			gamePaused = !gamePaused;
			timerOn = !gamePaused;
		}
		
		if(gemMoving || gamePaused || gameOver)
			return;
		
		// Left click
		if(button == 0) {
			// If selected gem is null, whichever slot the mouse is over is the new selected gem
			if(selectedGem == null) {
				selectNewGem();
			} else {
				Gem gem = getGemAtCoordinates(mouseX, mouseY);
				if(gem != null && isValidSwap(selectedGem, gem)) {
					swapGems(selectedGem, gem);
				} else {
					selectedGem = gem;
				}
			}
		}
	}
	
	@Override
	public void mouseReleased(int button, int x, int y) {
		if(button == 0)
			mouse_down = false;
	}
	
	public boolean isValidSwap(Gem gem1, Gem gem2) {
		// Makes sure swaps can only be done one tile apart and not diagnally
		if(!((Math.abs(gem1.getSlotX() - gem2.getSlotX()) == 1 && Math.abs(gem1.getSlotY() - gem2.getSlotY()) == 0) 
		|| (Math.abs(gem1.getSlotY() - gem2.getSlotY()) == 1 && Math.abs(gem1.getSlotX() - gem2.getSlotX()) == 0))) {
			return false;
		}
		
		boolean result = false;
		
		gems[gem1.getSlotX()][gem1.getSlotY()] = gem2;
		gems[gem2.getSlotX()][gem2.getSlotY()] = gem1;
		
		result = boardContainsMatch();
		
		// Puts gems back where they were
		gems[gem2.getSlotX()][gem2.getSlotY()] = gem2;
		gems[gem1.getSlotX()][gem1.getSlotY()] = gem1;
		
		return result;
	}
	
	public void resolveMatches() {
		
		int horizontalMatches = 0;
		int verticalMatches = 0;
		for(int i = 0; i < 8; i++) {
			for(int j = 0; j < 8; j++) {
				if(gems[i][j] == null) 
					continue;
				
				horizontalMatches = 0;
				verticalMatches = 0;
				
				for(int k = 0; k < 8; k++) {
					if(i + k <= 7) {
						if(gems[i + k][j] != null && gems[i + k][j].matched == false && gems[i + k][j].getGemType() == gems[i][j].getGemType()) {
							horizontalMatches++;
							// System.out.println((i + k) + "," + j  + " (" + gems[i + k][j].getGemType() + ") matched with " + i + "," + j + " (" + gems[i][j].getGemType() + ")");
						} else {
							break;
						}
					}
				}
				
				for(int k = 0; k < 8; k++) {
					if(j + k <= 7) {
						if(gems[i][j + k] != null && gems[i][j + k].matched == false && gems[i][j + k].getGemType() == gems[i][j].getGemType()) {
							verticalMatches++;
						} else {
							break;
						}
					}
				}
				
				if(horizontalMatches >= 3) {

					for(int k = 0; k < horizontalMatches; k++) {
						if(gems[i + k][j] != null) {
							gems[i + k][j].matched = true; 
						}
					}
				}
				
				if(verticalMatches >= 3) {
					for(int k = 0; k < verticalMatches; k++) {
						if(gems[i][j + k] != null) {
							gems[i][j + k].matched = true; 
						}
					}
				}
				
				if(verticalMatches >= 3 || horizontalMatches >= 3) {
					consecutiveMatchesMultiplier++;
					if(consecutiveMatchesMultiplier > consecutiveMatchMax) {
						consecutiveMatchesMultiplier = consecutiveMatchMax;
					}
					consecutiveMatchTimer = consecutiveMatchTime;
					if(timerOn && !gamePaused && !gameOver)
						addTimeToGameTimer(matchTimeIncrease);
				}
			}
		}
	}
	
	public void instantlySwapGems(Gem gem1, Gem gem2) {
		gems[gem1.getSlotX()][gem1.getSlotY()] = gem2;
		gems[gem2.getSlotX()][gem2.getSlotY()] = gem1;
		int tempX = gem1.getSlotX();
		int tempY = gem1.getSlotY();
		gem1.setSlot(gem2.getSlotX(), gem2.getSlotY());
		gem2.setSlot(tempX, tempY);
		selectedGem = null;
	}
	
	public void swapGems(Gem gem1, Gem gem2) {
		gems[gem1.getSlotX()][gem1.getSlotY()] = gem2;
		gems[gem2.getSlotX()][gem2.getSlotY()] = gem1;
		int tempX = gem1.getSlotX();
		int tempY = gem1.getSlotY();
		gem1.setTargetSlot(gem2.getSlotX(), gem2.getSlotY());
		gem2.setTargetSlot(tempX, tempY);
		selectedGem = null;
	}
	
	/**
	 * Returns true if there are three or more of the same gem in a row
	 * @return
	 */
	public boolean boardContainsMatch() {
		for(int i = 0; i < 8; i++) {
			for(int k = 0; k < 8; k++) {
				// Checks vertical matches
				try {
					if(i <= 5 && gems[i + 1][k].getGemType() == gems[i][k].getGemType() && gems[i + 2][k].getGemType() == gems[i][k].getGemType()) {
						return true;
					} 
				} catch(NullPointerException e) {
					
				}
				
				// Horizontal matches
				try {
					if(k <= 5 && gems[i][k].getGemType() == gems[i][k + 1].getGemType() && gems[i][k].getGemType() == gems[i][k + 2].getGemType()) {
						return true;
					}
				} catch(NullPointerException e) {
					
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Gets the gem at coordinates (x, y), if there is no gem there null is returned
	 * @param x
	 * @param y
	 * @return
	 */
	public Gem getGemAtCoordinates(int x, int y) {
		for(int i = 0; i < 8; i++) {
			for(int k = 0; k < 8; k++) {
				java.awt.Rectangle rect = new java.awt.Rectangle(i * spriteSize + boardOffsetX, k * spriteSize + boardOffsetY, spriteSize, spriteSize);
				if(rect.contains(x, y))
					return gems[i][k];
			}
		}
		
		return null;
	}
	
	public void selectNewGem() {
		selectedGem = getGemAtCoordinates(mouseX, mouseY);
	}
}
