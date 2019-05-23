package main.java.com.powerball;

/**
 * POJO to represent the JSON data returned from powerball for each drawing. 
 */
public class DrawingEntry {
	public String field_winning_numbers;
	public String field_multiplier;
	public String field_draw_date;

	@Override
	public String toString() {
		return "DrawingEntry [field_winning_numbers=" + field_winning_numbers + ", field_multiplier=" + field_multiplier
				+ ", field_draw_date=" + field_draw_date + "]";
	}
}
