package com.totalnihon.ubs.connect4;

import java.util.InputMismatchException;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.totalnihon.ubs.connect4.infrastructure.exception.ColumnIsFullException;
import com.totalnihon.ubs.connect4.infrastructure.exception.ColumnOutOfBoudariesException;

@SpringBootApplication
public class Connect4 implements CommandLineRunner {
	@Autowired 
	GameEngine ge;
	
	final public static String WRONG_INPUT = "Wrong Input";
	
	public static void main(String args[])  {
		ConfigurableApplicationContext ctx = SpringApplication.run(Connect4.class, args);
		ctx.close();
	}
	
	@Override
	public void run(String... arg0) throws Exception {
		int column;	
		Scanner scanner = new Scanner(System.in);

		ge.printBoard();
		
		do {
		    //  prompt for the user's name
		    System.out.print((ge.getTurn()? GameEngine.PLAYERA:GameEngine.PLAYERB) +  "[" + (ge.getTurn()? GameEngine.RED:GameEngine.YELLOW) + "] - choose column (1-7): ");
	
		    // get their input as a String
		    try {
		    	column = scanner.nextInt();
				ge.play(ge.getTurn(), column);
			    ge.printBoard();
		    } catch(InputMismatchException ime) {
		    	System.out.println(WRONG_INPUT);
		    } catch (ColumnIsFullException | ColumnOutOfBoudariesException e) {
				System.out.println(e.getMessage());
			} 		    		    
		} while(!ge.isGameFinished());
	
		scanner.close();
		System.out.println(ge.getWinner());
	}
}
