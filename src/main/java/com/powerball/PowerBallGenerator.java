package main.java.com.powerball;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.apache.commons.collections4.bag.HashBag;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import com.google.gson.Gson;

public class PowerBallGenerator {
  private static final String HISTORIC_URL = "https://www.powerball.com/api/v1/numbers/powerball?_format=json&min=2015-11-01%2000:00:00&max=2019-03-26%2023:59:59";
  
  private static final int NUMBER_OF_WHITE_BALLS = 69;
  private static final int NUMBER_OF_POWER_BALLS = 26;
  
  private static final int WHITE_BALLS_PER_DRAWING = 5;
  private static final int POWER_BALLS_PER_DRAWING = 1;
  
  public static void main(String[] args) {
    System.out.println("How many tickets to generate: ");
    Scanner scanner = new Scanner(System.in);
    int numTickets = scanner.nextInt();
    scanner.close();
    
    PowerBallGenerator generator = new PowerBallGenerator();
    try {
			List<PowerBallTicket> tickets = generator.generateTickets(numTickets);
			for (PowerBallTicket ticket : tickets) {
				System.out.println(ticket);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
  }
  
  public List<PowerBallTicket> generateTickets(int numberOfTickets) throws ClientProtocolException, IOException  {
  		List<PowerBallTicket> tickets = new ArrayList<PowerBallTicket>();
   
  	  HashBag<Integer> whiteBallsBag = new HashBag<Integer>();
    HashBag<Integer> powerBallsBag = new HashBag<Integer>();

    // Get the number of times each ball has been selected and put in bag
    getBallDistributions(whiteBallsBag, powerBallsBag);

    Double[] whiteBallsUpdatedWeight = getNewBallWeight(whiteBallsBag, NUMBER_OF_WHITE_BALLS);
    Double[] powerBallsUpdatedWeight = getNewBallWeight(powerBallsBag, NUMBER_OF_POWER_BALLS);

    for (int x = 0; x < numberOfTickets; x++) {
      List<Integer> chosenWhiteBalls = new ArrayList<Integer>();
      int chosenPowerBall = 1;
      Random random = new Random();
      
      // Get the 5 white balls needed for a ticket.  Use the new weights for each ball.
	    for (int i = 0; i < WHITE_BALLS_PER_DRAWING; i++) {
	    	
	    		double randomDouble = random.nextDouble() * 100;
	    		
	    		double runningTotal = 0.0;
	    		for (int j = 0; j < whiteBallsUpdatedWeight.length; j++) {
	    			runningTotal += whiteBallsUpdatedWeight[j];
	    			
	    			if (runningTotal >= randomDouble && !chosenWhiteBalls.contains(j + 1)) {
	    				chosenWhiteBalls.add(j + 1);
	    				break;
	    			}
	    		}
	    }
	    
	    
	    // Get the power ball needed for a ticket.  Use the new weights for each ball.
	    for (int i = 0; i < POWER_BALLS_PER_DRAWING; i++) {
	    		double randomDouble = random.nextDouble() * 100;
	    		
	    		double runningTotal = 0.0;
	    		for (int j = 0; j < powerBallsUpdatedWeight.length; j++) {
	    			runningTotal += powerBallsUpdatedWeight[j];
	    			
	    			if (runningTotal >= randomDouble) {
	    				chosenPowerBall = j + 1;
	    				break;
	    			}
	    		}
	    }
	    
	    tickets.add(new PowerBallTicket(chosenWhiteBalls, chosenPowerBall));
    }
    
    return tickets;
  }
  
  
  /**
   * Fetch and parse the data from the powerball site, then add the white balls to our white ball bag, and add the power balls to our power ball bag.
   * @param whiteBalls
   * @param powerBalls
   * @throws ClientProtocolException
   * @throws IOException
   */
  private void getBallDistributions(HashBag<Integer> whiteBalls, HashBag<Integer> powerBalls) throws ClientProtocolException, IOException {
	  	HttpClient client = HttpClientBuilder.create().build();
	  	HttpGet request = new HttpGet(HISTORIC_URL);
	  	HttpResponse response = client.execute(request);

	  	BufferedReader rd = new BufferedReader(
	  		new InputStreamReader(response.getEntity().getContent()));

	  	StringBuffer result = new StringBuffer();
	  	String line = "";
	  	while ((line = rd.readLine()) != null) {
	  		result.append(line);
	  	}
	  	
	  	Gson gson = new Gson();
	  	DrawingEntry[] drawingEntries = gson.fromJson(result.toString(), DrawingEntry[].class);
	  	
	  	for (DrawingEntry entry : drawingEntries) {
	  		String[] numbers = entry.field_winning_numbers.split(",");
	  		
	  		for (int i = 0; i < numbers.length - 1; i++) {
	  			whiteBalls.add(Integer.valueOf(numbers[i]));
	  		}
	  		
	  		powerBalls.add(Integer.valueOf(numbers[numbers.length - 1]));	  		
	  	}
  }
  
  /**
   * Normalize the chances of each ball being drawn.  For each number of balls, give a "weight" to each ball.  Balls that are being drawn more than average will get a less weight, and vice versa. 
   * That way, balls that are not being drawn as much, have a higher chance of being selected, but we don't completely eliminate balls that have
   * been selected a lot.
   * @param drawnBallsBag
   * @param numberOfBalls
   * @return A Double[] in which each element represents the new weight for Ball (index + 1)
   */
  private Double[] getNewBallWeight(HashBag<Integer> drawnBallsBag, int numberOfBalls) {
    Double[] ballsUpdatedChance = new Double[numberOfBalls];
    
    // % chance that each ball has of being picked.
    double ballChance = 100.00/numberOfBalls; 
    
    // In a perfect world, each ball would've been drawn the exact number of times.
    double ballPerfectWorldTimesDrawn = (double)(drawnBallsBag.size() * 1.0)/numberOfBalls;
    
    for (int i = 1; i <= numberOfBalls; i++) {
    		int numTimesDrawn = drawnBallsBag.getCount(i);
    	
    		double newChance = ballChance + (ballChance - ((numTimesDrawn/ballPerfectWorldTimesDrawn) * ballChance));
    		ballsUpdatedChance[i-1] = newChance;
    }
      
    return ballsUpdatedChance;
  }  

}
