package boogle;

import java.util.*;

public class ConsolePlayer implements Player {
    private String name;
    private Scanner console;

    public ConsolePlayer(String name, Scanner scanner) {
        this.name = name;
        this.console = scanner;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String nextMove(Gameboard gb, HashSet<String> wordlist, String[] prevMoves) {
        System.out.println("\nEnter your word, or '-skip' to skip this turn");
        System.out.print(name + ", make your move: ");

        String input = console.nextLine();

        if (input.equalsIgnoreCase("-skip"))
            return null;

        return input;
    }
}
