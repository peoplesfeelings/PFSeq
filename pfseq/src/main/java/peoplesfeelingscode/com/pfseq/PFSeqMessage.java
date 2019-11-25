package peoplesfeelingscode.com.pfseq;

public class PFSeqMessage {
    public  static final String ERROR_MSG_PREFIX = "PFSeq Error - ";
    public  static final String ALERT_MSG_PREFIX = "PFSeq Alert - ";
    public  static final int MESSAGE_TYPE_ERROR = 0; // meant for things that would warrant stopping playback
    public  static final int MESSAGE_TYPE_ALERT = 1; // meant for things that would not warrant stopping playback

    private int type;
    private String message;

    public PFSeqMessage(int type, String message) {
        this.type = type;
        this.message = message;
    }

    public int getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }
}
