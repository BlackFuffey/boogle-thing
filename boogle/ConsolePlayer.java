package boogle;

import java.util.Scanner;

public class ConsolePlayer implements Player {
    private String prompt;
    private Scanner console = new Scanner(System.in);

    public ConsolePlayer(String prompt) {
        this.prompt = prompt;
    }

    public String nextMove(String[] prevMoves) {
        System.out.println("\nTo skip your turn, enter '-skip'");
        System.out.print(prompt);

        String input = console.nextLine();

        if (input.equalsIgnoreCase("-skip"))
            return null;

        return input;
    }
}
