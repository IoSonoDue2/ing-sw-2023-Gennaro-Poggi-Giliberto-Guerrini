package Server.Model;
public class LivingRoomTileSpot extends TileSpot{
    private int dotsNumber;
    private Match m;
    protected boolean isReal(){
        return dotsNumber >= 0 && dotsNumber <= m.getNumberOfPlayers();
    }

    protected int getDotsNumber(){
        return dotsNumber;
    }

}
