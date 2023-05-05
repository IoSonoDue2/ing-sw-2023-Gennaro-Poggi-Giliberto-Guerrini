package Server.Model.GameItems;

public enum PointsTile {
    TWO_1, TWO_2, FOUR_1, FOUR_2,
    SIX_1, SIX_2, EIGHT_1, EIGHT_2,
    MATCH_ENDED;

    public static PointsTile randomPointsTile() {
        return values()[(int) (Math.random() * values().length)];
    }

    /**
     * This method is used fromn the CLI to obtain a printable represention of this object
     * @return the char matrix that represents a "drawing" of this object
     */
    public char[][] getCLIRepresentation(){
        char[][] res = new char[4][9];

        for(int i=0; i<4; i++){
            for(int j=0; j<9; j++){
                res[i][j] = ' ';
            }
        }
        res[0][0] = '+';
        res[0][8] = '+';
        res[3][0] = '+';
        res[3][8] = '+';
        for(int j=1;j<8;j++){
            res[0][j] = '-';
            res[3][j] = '-';
        }
        res[1][0] = '|';
        res[2][0] = '|';

        res[1][8] = '|';
        res[2][8] = '|';

        res[2][2] = 'p';
        res[2][3] = 'o';
        res[2][4] = 'i';
        res[2][5] = 'n';
        res[2][6] = 't';
        res[2][7] = 's';

        if(this == TWO_1 || this == TWO_2){
            res[1][4] = '2';
        }else if(this == FOUR_1 || this == FOUR_2){
            res[1][4] = '4';
        }else if(this == SIX_1 || this == SIX_2){
            res[1][4] = '6';
        }else if(this == EIGHT_1 || this == EIGHT_2){
            res[1][4] = '8';
        }else{
            res[1][4] = '?';
        }

        return res;
    }
}
