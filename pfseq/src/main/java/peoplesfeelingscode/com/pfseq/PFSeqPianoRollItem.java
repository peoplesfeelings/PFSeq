package peoplesfeelingscode.com.pfseq;

import android.util.Log;

import static peoplesfeelingscode.com.pfseq.PFSeq.LOG_TAG;
import static peoplesfeelingscode.com.pfseq.PFSeqConfig.REPEATING;
import static peoplesfeelingscode.com.pfseq.PFSeqConfig.TIME_SIG_UPPER;

public class PFSeqPianoRollItem {
    private boolean enabled;
    private PFSeq seq;
    private PFSeqClip clip;
    private String name;
    private PFSeqTimeOffset timeOffset; // relative to begginning of bar

    public PFSeqPianoRollItem(PFSeq seq, PFSeqClip clip, String name, PFSeqTimeOffset timeOffset) {
        this.seq = seq;
        setClip(clip);
        this.name = name;
        this.timeOffset = timeOffset;
    }

    public long soonestNanoAfter(long nano) {
        // may return a nanotime that is not actually after, but before, if pfseq config is set to non-repeating

        double nanosPerBeat = seq.getNanosecondsPerBeat().doubleValue();
        int timeSigBeatsPerBar = seq.getConfig().getInt(TIME_SIG_UPPER);
        int beatsSinceTempoStart = seq.beatsSinceTempoStart(nano);
        long currentBeatNanotime = seq.beatStartNanotime(beatsSinceTempoStart);
        int itemBeatOfBar = timeOffset.getBeatOfBar();
        long offsetFromBeatNano = (long) ( timeOffset.getPercent() * nanosPerBeat );

        int currentBeatOfBar = -1;
        boolean isRepeating = seq.getConfig().getBool(REPEATING);
        if (isRepeating) {
            currentBeatOfBar = beatsSinceTempoStart % timeSigBeatsPerBar;
        } else {
            currentBeatOfBar = beatsSinceTempoStart;
        }

        // how many beats in the future the item is
        int beatsOut = -1;
        if (isRepeating) {
            // if itemBeatOfBar equals currentBeatOfBar then we need to see if the next nano is in this bar or the next bar
            beatsOut = (timeSigBeatsPerBar + itemBeatOfBar - currentBeatOfBar) % timeSigBeatsPerBar;
            if (currentBeatNanotime + (beatsOut * nanosPerBeat) + offsetFromBeatNano < nano) {
                beatsOut += timeSigBeatsPerBar;
            }
        } else {
            // may be negative
            beatsOut = itemBeatOfBar - beatsSinceTempoStart;
        }

//        Log.d(LOG_TAG, name + "\nbeatsSinceTempoStart:" + beatsSinceTempoStart + "\ntimeOffset.getPercent(): " + timeOffset.getPercent() + "\nbeatsOut: " + beatsOut + "\noffsetFromBeatNano: " + offsetFromBeatNano);
        return currentBeatNanotime + (long) (beatsOut * nanosPerBeat) + offsetFromBeatNano;
    }
    public int lengthInFrames() {
        return getClip().getPcm().length / 2;
    }

    // accessors
    public PFSeqClip getClip() {
        return clip;
    }
    public void setClip(PFSeqClip clip) {
        this.clip = clip;
        if (clip.isLoadedSuccessfully()) {
            setEnabled(true);
        } else {
            Log.d(LOG_TAG, "clip not loaded successfully. pr item disabled");
            setEnabled(false);
        }
    }
    public PFSeqTimeOffset getTimeOffset() {
        return timeOffset;
    }
    public String getName() {
        return name;
    }
    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
