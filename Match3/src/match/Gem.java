package match;

import java.util.concurrent.ThreadLocalRandom;

public class Gem {

	// Some of these names may be completely false
	public static enum GemType {
		RUBY(0, 0), AMETHYST(1, 0), DIAMOND(2, 0), EMERALD(3, 0), TOPAZ(4, 0), MOONSTONE(0, 1);
		
		int tx, ty;
		GemType(int tx, int ty) {
			this.tx = tx;
			this.ty = ty;
		}
	};
	
	private GemType gemType;
	private double x, y;	  // Pixel position
	private int slotX, slotY; // Position on the board
	private int tx, ty;
	private int targetSlotX, targetSlotY;
	private double gemMoveSpeed = 256;
	public boolean matched = false, visible = true;
	private double invisibleTimer = .2d;
	private double matchTimer = .25d;
	
	/**
	 * Creates a new random gem object
	 */
	public Gem(int slotX, int slotY) {
		this(slotX, slotY, GemType.values()[ThreadLocalRandom.current().nextInt(0, 6)]);
	}
	
	/**
	 * Creates a new gem of a specific types
	 * @param type
	 */
	public Gem(int slotX, int slotY, GemType type) {
		this.gemType = type;
		this.tx = type.tx;
		this.ty = type.ty;
		setSlot(slotX, slotY);
	}
	
	public int x() {
		return (int) x;
	}
	
	public int y() {
		return (int) y;
	}
	
	public int tx() {
		return tx;
	}
	
	public int ty() {
		return ty;
	}
	
	public int getSlotX() {
		return slotX;
	}
	
	public int getSlotY() {
		return slotY;
	}
	
	public GemType getGemType() {
		return gemType;
	}
	
	public void setSlot(int x, int y) {
		this.slotX = x;
		this.slotY = y;
		this.targetSlotX = slotX;
		this.targetSlotY = slotY;
		this.x = x * Engine.spriteSize + Engine.boardOffsetX;
		this.y = y * Engine.spriteSize + Engine.boardOffsetY;
	}
	
	public void setTargetSlot(int x, int y) {
		this.targetSlotX = x;
		this.targetSlotY = y;
	}
	
	public void update(double deltaT) {
		if(matched) {
			matchTimer -= deltaT;
			if(matchTimer <= 0) {
				new FloatingNumber(x() + (int) (Engine.spriteSize/2), y(), (Engine.gem_score * Engine.consecutiveMatchesMultiplier) + "");
				Engine.score += Engine.gem_score * Engine.consecutiveMatchesMultiplier;
				Engine.gems[slotX][slotY] = null;
			}
		}
		
		if(!visible) {
			invisibleTimer -= deltaT;
			if(invisibleTimer <= 0) {
				visible = true;
			}
		}
		
		// Moves gem towards target
		if(targetSlotX != slotX || targetSlotY != slotY) {
			Engine.gemMoving = true;
			
			int targetXPos = targetSlotX * Engine.spriteSize + Engine.boardOffsetX;
			int targetYPos = targetSlotY * Engine.spriteSize + Engine.boardOffsetY;
			
			int steps = 5;
			for(int i = 0; i < steps; i++) {
				double distX = targetXPos - x;
				double distY = targetYPos - y;
			
				if(distX < 0)
					x -= (gemMoveSpeed * deltaT)/steps;
				
				if(distX > 0)
					x += (gemMoveSpeed * deltaT)/steps;
				
				if(distY < 0) 
					y -= (gemMoveSpeed * deltaT)/steps;
				
				if(distY > 0)
					y += (gemMoveSpeed * deltaT)/steps;
				
				if(Math.abs(distX) <= 5 && Math.abs(distY) <= 5) {
					setSlot(targetSlotX, targetSlotY);
				}
			}
		}
	}
}
