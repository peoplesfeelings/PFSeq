package peoplesfeelingscode.com.pfseq;

/*
This class defines a relative time position that is stored as a percent of a beat or fraction of the duration of a musical bar.
 */

public class PFSeqTimeOffset {
    public static final int MODE_FRACTIONAL = 0;
    public static final int MODE_PERCENT = 1;

    private int beatOfBar; // 0-based
    private int mode; // one of the PFSeqTimeOffset.MODE_ constants

    // only used if mode is PERCENT:
    private double percent; // between 0 and 1, inclusive

    // only used if mode is FRACTIONAL:
    private int binaryDivisions; // how many binary divisions you need in a beat. 1 or binary number like 2, 4, 8, 16, etc
    private int binaryPos; // 0-based. 0 for on-beat
    private boolean isTriplet;
    private int tripletPos; // 0-based. 0 for on the binary position. 1 for a third of the way between that and the next binary position, etc

    // private constructor. use static make method
    private PFSeqTimeOffset(int beatOfBar, int mode, double percentPos, int binaryDivisions, int binaryPos, boolean isTriplet, int tripletPos) {
        this.beatOfBar = beatOfBar;
        this.mode = mode;
        this.percent = percentPos;
        this.binaryDivisions = binaryDivisions;
        this.binaryPos = binaryPos;
        this.isTriplet = isTriplet;
        this.tripletPos = tripletPos;
    }

    public static PFSeqTimeOffset make(int beatOfBar, int mode, double percentPos, int binaryDivisions, int binaryPos, boolean isTriplet, int tripletPos) {
        if (mode == PFSeqTimeOffset.MODE_FRACTIONAL) {
            if ( !(binaryDivisions == 1 || binaryDivisions % 2 == 0 )
                    || binaryPos < 0
                    || binaryPos >= binaryDivisions) {
                return null;
            }
            if (isTriplet && (tripletPos > 2 || tripletPos < 0) ) {
                return null;
            }
        }
        if (mode == PFSeqTimeOffset.MODE_PERCENT) {
            if (percentPos < 0 || percentPos > 1) {
                return null;
            }
        }

        PFSeqTimeOffset timeOffset = new PFSeqTimeOffset( beatOfBar,  mode,  percentPos,  binaryDivisions,  binaryPos,  isTriplet,  tripletPos);

        return timeOffset;
    }

    public int getBeatOfBar() {
        return beatOfBar;
    }

    public int getMode() {
        return mode;
    }

    /*
    get the time position as a percent (of the duration of the beat)
     */
    public double getPercent() {
        if (getMode() == PFSeqTimeOffset.MODE_PERCENT) {
            return percent;
        } else {
            double theReturn = ((double) getBinaryPos()) / getBinaryDivisions();
            if (isTriplet()) {
                return theReturn + ( 1 / getBinaryDivisions() / 3 * getTripletPos() );
            } else {
                return theReturn;
            }
        }
    }

    public int getBinaryDivisions() {
        return binaryDivisions;
    }

    public int getBinaryPos() {
        return binaryPos;
    }

    public boolean isTriplet() {
        return isTriplet;
    }

    public int getTripletPos() {
        return tripletPos;
    }


}
