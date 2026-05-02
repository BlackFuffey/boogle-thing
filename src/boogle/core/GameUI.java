import boogle.core.Player;
import boogle.core.Launcher.GameOptions;

public interface GameUI {
    public void initialize();
    public void cleanup();

    public void lobby(GameOptions options);

    public void newTurn(Gameboard board, HashMap<Player, Integer> scoreboard, ArrayList<String> playedWords, Player player);

    public void passive();
    public String active();

    public void confirm();
}
