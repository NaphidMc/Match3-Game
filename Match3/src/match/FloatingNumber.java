package match;

import java.util.ArrayList;

public class FloatingNumber {

	public static ArrayList<FloatingNumber> active = new ArrayList<FloatingNumber>();
	private double lifetime = .7d;
	private double x, y;
	private double moveSpeed = 40d;
	public String value = "";
	
	public FloatingNumber(int x, int y, String text) {
		value = text;
		this.x = x;
		this.y = y;
		active.add(this);
	}
	
	public int x() {
		return (int) x;
	}
	
	public int y() {
		return (int) y;
	}
	
	public void update(double delta) {
		lifetime -= delta;
		if(lifetime <= 0) {
			active.remove(this);
		}
		
		y -= moveSpeed * delta;
	}
}
