package boogle.player;

import java.util.*;
import boogle.core.*;

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

    public String nextMove(Gameboard gb, Set<String> wordlist, ArrayList<String> playedWords) {
        System.out.println("\nEnter your word, or '-skip' to skip this turn");
        System.out.print(name + ", make your move: ");

        String input = console.nextLine();

        if (input.equalsIgnoreCase("-skip"))
            return null;

        return input;
    }
}
