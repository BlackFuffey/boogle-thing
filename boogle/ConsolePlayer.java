package boogle;

import java.util.Scanner;

public class ConsolePlayer implements Player {
    private String prompt;
    private Scanner console;

    public ConsolePlayer(String prompt, Scanner scanner) {
        this.prompt = prompt;
        this.console = scanner;
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
