package main.java.com.powerball;

import java.util.Collections;
import java.util.List;

public class PowerBallTicket {
	private List<Integer> whiteBalls;
	private int powerBall;
	
	public PowerBallTicket(List<Integer> whiteBalls, int powerBall) {
		this.whiteBalls = whiteBalls;
		this.powerBall = powerBall;
	}
	
	public List<Integer> getWhiteBalls() {
		return whiteBalls;
	}
	public void setWhiteBalls(List<Integer> whiteBalls) {
		this.whiteBalls = whiteBalls;
	}
	public int getPowerBall() {
		return powerBall;
	}
	public void setPowerBall(int powerBall) {
		this.powerBall = powerBall;
	}

	@Override
	public String toString() {
		Collections.sort(whiteBalls);
		StringBuilder sb = new StringBuilder();
		for (int i : whiteBalls)
			sb.append(i + " ");
		
		sb.append(powerBall + "P");
		
		return sb.toString();
	}
}
