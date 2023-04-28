package Server.Model.Player;


import Server.Model.GameItems.Bookshelf;
import Server.Model.Cards.PersonalGoalCard;
import Server.Model.GameItems.PointsTile;
import Server.Model.GameItems.TileSpot;
import Server.Model.GameItems.TileType;

import java.util.ArrayList;

/**
 * This class defines the player in game
 * @author Paolo Gennaro
 */
public class Player {
    private final int playerID;
    private final Bookshelf bookshelf;
    private final String playerNickName;
    private PersonalGoalCard personalGoalCard;
    private PlayerStatus playerStatus;
    private final ArrayList<PointsTile> pointsTiles;
    private Player nextPlayer;
    private TileType[] takenTiles;

    public Player(int playerID, String playerNickName){
        this.playerID = playerID;
        this.bookshelf = new Bookshelf();
        this.playerNickName = playerNickName;
        this.pointsTiles = new ArrayList<PointsTile>();
        this.playerStatus = new Connected();
    }

    public void setTakenTiles(TileType[] takenTiles) {
        this.takenTiles = takenTiles;
    }

    public TileType[] getTakenTiles(){
        return this.takenTiles;
    }
    public void clearTakenTiles() {
        this.takenTiles = null;
    }

    /**
     * The Getter for the next player
     * @return is the next player assigned to the player
     */
    public Player getNextPlayer() {
        return nextPlayer;
    }

    /**
     * We set the next player
     * @param nextPlayer is the player that is going to play after
     */
    public void setNextPlayer(Player nextPlayer) {
        this.nextPlayer = nextPlayer;
    }

    /**
     * The getter of the Point Tile array which contains the Point Tiles of the player
     * @return a copy of the pointsTiles arraylist
     */
    public ArrayList<PointsTile> getPointsTiles() {
        return new ArrayList<PointsTile>(this.pointsTiles);
    }

    /**
     * The Getter for the Bookshelf of the player
     * @return is the Bookshelf assigned to the player
     */
    public Bookshelf getBookshelf() {
        return bookshelf;
    }

    /**
     * The Getter for the PersonalGoalCard of the player
     * @return is the PersonalGoalCard assigned to the player
     */
    public PersonalGoalCard getPersonalGoalCard() {
        return personalGoalCard;
    }

    /**
     * We assign the Player his PersonalGoalCard
     * @param personalGoalCard is the PersonalGoalCard we assign to the player
     */
    public void assignPersonalGoalCard(PersonalGoalCard personalGoalCard) {
        this.personalGoalCard = personalGoalCard;
    }

    /**
     * The Getter for the PlayerStatus
     * @return is the playerStatus in which the player is
     */
    public PlayerStatus getPlayerStatus() {
        return playerStatus;
    }

    /**
     * This method is to toggle the Status of the player either if it's connected or disconnected
     */
    public void togglePlayerStatus() {
        if (this.playerStatus instanceof Connected) {
            this.playerStatus = new Disconnected();
        } else {
            this.playerStatus = new Connected();
        }
    }

    /**
     * Method to know if the player is connected or not
     * @return 1 if connected, 0 if not
     */
    public boolean isConnected(){
        return this.playerStatus instanceof Connected;
    }

    /**
     * The Getter for the NickName
     * @return is the playerNickName defined in the Constructor of this class
     */
    public String getPlayerNickName() {
        return playerNickName;
    }

    /**
     * The Getter for the PlayerID
     * @return is the playerID defined in the Constructor of this class
     */
    public int getPlayerID() {
        return playerID;
    }

    /**
     * This method is for assign points to a specific player and store the value
     * @param tile is the PointsTile we will store in to the ArrayList
     */
    public void assignPointTile(PointsTile tile) throws UnsupportedOperationException{
        if(tile.toString().contains("1") && this.pointsTiles.stream().map(Enum::toString).anyMatch(t->t.contains("1"))) throw new UnsupportedOperationException("You can't have 2 tiles of the same CommonGoalCard!");
        if(tile.toString().contains("2") && this.pointsTiles.stream().map(Enum::toString).anyMatch(t->t.contains("2"))) throw new UnsupportedOperationException("You can't have 2 tiles of the same CommonGoalCard!");
        if (this.pointsTiles.contains(PointsTile.MATCH_ENDED)) throw new UnsupportedOperationException("You can't add more tiles after you took the tile for completing the bookshelf first!");
        this.pointsTiles.add(tile);
    }
}