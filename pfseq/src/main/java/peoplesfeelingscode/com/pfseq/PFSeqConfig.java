package peoplesfeelingscode.com.pfseq;

/*
BUFFER_SIZE_BYTES - this affects the beginning of the play state. the buffer needs to be filled before
    the audiotrack will play on some devices, so a 1 megabyte buffer can mean a 5 second wait before
    content begins (because you need the audiotimestamp before you can measure precisely and you need
    to play before you get the audiotimestamp and you need a full buffer before it will play). note that
    this is the requested buffer size (the native layer decides) so actual buffer size may differ.
FADE_LENGTH_FRAMES - this is just meant to prevent clipping, when audio clips are abridged. adjust to
    taste. making it too large could cause errors on some devices
FRAMES_TO_LEAVE_BEFORE_NEXT_ITEM - you want this as low as possible without having a second PR item get
    skipped because the first PR item wrote slightly past the nanotime at which the second item would
    have been seen to have been able to start at, in the scenario in which the first item abuts against
    the second item
MAX_CLIP_FRAMES - for memory reasons we can't allow extremely long audio clips. the audio data is
    manipulated in the content-writing loop and it's hard to test on all devices so I'm starting this
    low (10 s) but ultimately we want it to approach an hour.
MIN_MILLIS_AHEAD_TO_WRITE - it's min because you can right a little further out, like if the sample
    extends beyond that poiint in time. this is how far into the future we want to keep the AudioTrack's
    buffer written for.
MIN_WRITABLE_CONTENT_NANO - when calling nextPianoRollItemAfter() to get the itemAfterNext, we need to
    pass it a nanotime that is slightly later than the nano start time of the earlier item.
    this should be long enough that calculations are safe, but short enough that it's unlikely
    a subsequent item would be skipped because it was too close to the earlier one. nanoseconds
POLLING_CONTROL_THREAD_MILLIS -
REPEATING - whether the piano roll loops or plays once
SMALLEST_STOPGAP_SILENCE_MILLIS - used while syncing so we don't bother writing tiny amounts of silence
SYNC_MARGIN_MILLIS - how far before MIN_MILLIS_AHEAD_TO_WRITE is it ok for the buffer to be in order
    for us to say it's ok to take a break from writing to sync the tracks
SYNC_POLLING_SLEEP_MILLIS - used in syncTracks runnable to throttle the while loop
SYNC_TIME_OUT_MILLIS - used in syncTracks runnable to exit runnable and send error to activity if syncing
    does not complete in the specified time
TIME_SIG_LOWER - note value that represents one beat. binary sequence numbers. 4 is quarter note
TIME_SIG_UPPER - how many beats constitute a bar
TIMESTAMP_POLLING_DELAY_MILLIS - how frequently to check if the AudioTimestamp has been returned
    from the native layer. this is also how frequently to write silence, while waiting for nano to be mapped.
    make sure this is less than the equivalent of your buffer size, by some margin
 */

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static peoplesfeelingscode.com.pfseq.PFSeq.LOG_EOL;
import static peoplesfeelingscode.com.pfseq.PFSeq.LOG_TAG;
import static peoplesfeelingscode.com.pfseq.PFSeq.MILLIS_PER_SECOND;

public class PFSeqConfig implements Serializable {
    private static final String KEY_NOT_FOUND = "key not found - ";

    private boolean isValid;

    // keys
    public static final String BUFFER_SIZE_BYTES = "buffer_size_bites";
    public static final String FADE_LENGTH_FRAMES = "fade_length_frames";
    public static final String FRAMES_TO_LEAVE_BEFORE_NEXT_ITEM = "frames_to_leave_before_next_item";
    public static final String MAX_BPM = "max_bpm";
    public static final String MAX_CLIP_FRAMES = "max_clip_frames";
    public static final String MAX_TRACKS = "max_tracks";
    public static final String MIN_BPM = "min_bpm";
    public static final String MIN_MILLIS_AHEAD_TO_WRITE = "min_millis_ahead_to_write";
    public static final String MIN_WRITABLE_CONTENT_NANO = "min_writable_audio_nano";
    public static final String ONGOING_NOTIF_ID = "ongoing_notif_id";
    public static final String POLLING_CONTROL_THREAD_MILLIS = "polling_control_thread_millis";
    public static final String REPEATING = "repeating";
    public static final String RUN_IN_FOREGROUND = "run_in_foreground";
    public static final String SAMPLE_RATE = "sample_rate";
    public static final String SMALLEST_STOPGAP_SILENCE_MILLIS = "smallest_stopgap_silence_millis";
    public static final String SYNC_MARGIN_MILLIS = "sync_margin_millis";
    public static final String SYNC_POLLING_SLEEP_MILLIS = "sync_polling_sleep_millis";
    public static final String SYNC_TIME_OUT_MILLIS = "time_out_millis";
    public static final String TEMPO = "tempo_bpm";
    public static final String TIME_SIG_LOWER = "starting_time_sig_lower_numeral";
    public static final String TIME_SIG_UPPER = "starting_time_sig_upper_numeral";
    public static final String TIMESTAMP_POLLING_DELAY_MILLIS = "timestamp_polling_delay_millis";

    // default config values
    public static final HashMap<String, Integer> INT_DEFAULTS = new HashMap<String, Integer>() {{
        put(BUFFER_SIZE_BYTES, 100000);
        put(FADE_LENGTH_FRAMES, 2000);
        put(FRAMES_TO_LEAVE_BEFORE_NEXT_ITEM, 100);
        put(MAX_BPM, 1000);
        put(MAX_CLIP_FRAMES, 441000);
        put(MAX_TRACKS, 1);
        put(MIN_BPM, 15);
        put(MIN_MILLIS_AHEAD_TO_WRITE, 400);
        put(MIN_WRITABLE_CONTENT_NANO, 5000);
        put(ONGOING_NOTIF_ID, -1);
        put(POLLING_CONTROL_THREAD_MILLIS, 50);
        put(SAMPLE_RATE, 44100);
        put(SMALLEST_STOPGAP_SILENCE_MILLIS, 50);
        put(SYNC_MARGIN_MILLIS, 100);
        put(SYNC_POLLING_SLEEP_MILLIS, 5);
        put(SYNC_TIME_OUT_MILLIS, 4000);
        put(TIME_SIG_LOWER, 4);
        put(TIME_SIG_UPPER, 4);
        put(TIMESTAMP_POLLING_DELAY_MILLIS, 50);
    }};
    public static final HashMap<String, Boolean> BOOL_DEFAULTS = new HashMap<String, Boolean>() {{
        put(REPEATING, true);
        put(RUN_IN_FOREGROUND, true);
    }};
    public static final HashMap<String, Double> DOUBLE_DEFAULTS = new HashMap<String, Double>() {{
        put(TEMPO, 120.0);
    }};
    public static final HashMap<String, String> STRING_DEFAULTS = new HashMap<String, String>() {{
        //
    }};

    private HashMap<String, Integer> _ints;
    private HashMap<String, Boolean> _bools;
    private HashMap<String, Double> _doubles;
    private HashMap<String, String> _strings;

    /*
    pass constructor the values that you want different from the defaults, or null.
     */
    public PFSeqConfig(HashMap<String, Integer> intValues,
                       HashMap<String, Boolean> boolValues,
                       HashMap<String, Double> doubleValues,
                       HashMap<String, String> stringValues) {

        // initialize with defaults
        this._ints = INT_DEFAULTS;
        this._bools = BOOL_DEFAULTS;
        this._doubles = DOUBLE_DEFAULTS;
        this._strings = STRING_DEFAULTS;

        // overwrite defaults with values passed to constructor
        if (intValues != null) {
            for (Map.Entry<String, Integer> entry : intValues.entrySet()) {
                if (_ints.containsKey(entry.getKey())) {
                    _ints.put(entry.getKey(), entry.getValue());
                } else {
                    Log.d(LOG_TAG, KEY_NOT_FOUND + " key: " + entry.getKey());
                }
            }
        }
        if (boolValues != null) {
            for (Map.Entry<String, Boolean> entry : boolValues.entrySet()) {
                if (_bools.containsKey(entry.getKey())) {
                    _bools.put(entry.getKey(), entry.getValue());
                } else {
                    Log.d(LOG_TAG, KEY_NOT_FOUND + " key: " + entry.getKey());
                }
            }
        }
        if (doubleValues != null) {
            for (Map.Entry<String, Double> entry : doubleValues.entrySet()) {
                if (_doubles.containsKey(entry.getKey())) {
                    _doubles.put(entry.getKey(), entry.getValue());
                } else {
                    Log.d(LOG_TAG, KEY_NOT_FOUND + " key: " + entry.getKey());
                }
            }
        }
        if (stringValues != null) {
            for (Map.Entry<String, String> entry : stringValues.entrySet()) {
                if (_strings.containsKey(entry.getKey())) {
                    _strings.put(entry.getKey(), entry.getValue());
                } else {
                    Log.d(LOG_TAG, KEY_NOT_FOUND + " key: " + entry.getKey());
                }
            }
        }

        isValid = validate();

        Log.d(LOG_TAG, "Config values: " + LOG_EOL + printConfig() + LOG_EOL + (isValid ? "valid" : "not valid"));
    }

    private boolean validate() {
        // assumes 16bit 2 channel and 44100
        int bufferSizeBytes = getInt(BUFFER_SIZE_BYTES);
        int bufferSizeFrames = bufferSizeBytes / 4;
        int bufferSizeMillis = (int) (bufferSizeFrames / (44100 / MILLIS_PER_SECOND));
        if (getInt(TIMESTAMP_POLLING_DELAY_MILLIS) >= bufferSizeMillis) {
            Log.d(LOG_TAG, "TIMESTAMP_POLLING_DELAY_MILLIS should be less than equivalent of BUFFER_SIZE_BYTES. TIMESTAMP_POLLING_DELAY_MILLIS: " + getInt(TIMESTAMP_POLLING_DELAY_MILLIS) + " BUFFER_SIZE_BYTES: " + getInt(BUFFER_SIZE_BYTES) + " bufferSizeMillis: " + bufferSizeMillis);
            return false;
        }
        if (getInt(MIN_MILLIS_AHEAD_TO_WRITE) >= bufferSizeMillis) {
            Log.d(LOG_TAG, "MIN_MILLIS_AHEAD_TO_WRITE should be less than equivalent of BUFFER_SIZE_BYTES. MIN_MILLIS_AHEAD_TO_WRITE: " + getInt(MIN_MILLIS_AHEAD_TO_WRITE) + " BUFFER_SIZE_BYTES: " + getInt(BUFFER_SIZE_BYTES) + " bufferSizeMillis: " + bufferSizeMillis);
            return false;
        }
        if (getInt(SAMPLE_RATE) != 44100) {
            Log.d(LOG_TAG, "only sample rate 44100 allowed. sample rate: " + getInt(SAMPLE_RATE));
            return false;
        }
        if (getInt(MIN_BPM) >= getInt(MAX_BPM)) {
            Log.d(LOG_TAG, "min bpm not lower than max bpm. min: " + getInt(MIN_BPM) + " max: " + getInt(MAX_BPM));
            return false;
        }
        if (getInt(MIN_BPM) > getDouble(TEMPO)) {
            Log.d(LOG_TAG, "tempo lower than min bpm. min: " + getInt(MIN_BPM) + " tempo: " + getDouble(TEMPO));
            return false;
        }
        if (getInt(MAX_BPM) < getDouble(TEMPO)) {
            Log.d(LOG_TAG, "tempo greater than max bpm. max: " + getInt(MAX_BPM) + " tempo: " + getDouble(TEMPO));
            return false;
        }
        if (getInt(TIME_SIG_LOWER) <= 0 || getInt(TIME_SIG_LOWER) % 2 != 0) {
            Log.d(LOG_TAG, "failed to create time signature. upper numeral: " + getInt(TIME_SIG_UPPER) + " lower numeral: " + getInt(TIME_SIG_LOWER));
            return false;
        }
        if (getBool(REPEATING) && getInt(TIME_SIG_UPPER) <= 0) {
            Log.d(LOG_TAG, "upper time sig numeral negative: " + getInt(TIME_SIG_UPPER));
            return false;
        }
        if (getInt(ONGOING_NOTIF_ID) < 0 && getBool(RUN_IN_FOREGROUND)) {
            Log.d(LOG_TAG, "ONGOING_NOTIF_ID shouldn't be -1 if foreground is true. ONGOING_NOTIF_ID: " + getInt(ONGOING_NOTIF_ID));
            return false;
        }

        return true;
    }

    public int getInt(String key) {
        if (_ints.get(key) != null) {
            return _ints.get(key);
        } else {
            Log.d(LOG_TAG, KEY_NOT_FOUND + " key: " + key);
            return -1;
        }
    }

    public double getDouble(String key) {
        if (_doubles.get(key) != null) {
            return _doubles.get(key);
        } else {
            Log.d(LOG_TAG, KEY_NOT_FOUND + " key: " + key);
            return -1;
        }
    }

    public boolean getBool(String key) {
        if (_bools.get(key) != null) {
            return _bools.get(key);
        } else {
            Log.d(LOG_TAG, KEY_NOT_FOUND + " key: " + key);
            return false;
        }
    }

    public String getString(String key) {
        if (_strings.get(key) != null) {
            return _strings.get(key);
        } else {
            Log.d(LOG_TAG, KEY_NOT_FOUND + " key: " + key);
            return "";
        }
    }

    public String printConfig() {
        ArrayList<String> lines = new ArrayList<String>();

        for (Map.Entry<String, Integer> entry : _ints.entrySet()) {
            lines.add(entry.getKey() + ": " + entry.getValue() + LOG_EOL);
        }
        for (Map.Entry<String, Double> entry : _doubles.entrySet()) {
            lines.add(entry.getKey() + ": " + entry.getValue() + LOG_EOL);
        }
        for (Map.Entry<String, Boolean> entry : _bools.entrySet()) {
            lines.add(entry.getKey() + ": " + entry.getValue() + LOG_EOL);
        }
        for (Map.Entry<String, String> entry : _strings.entrySet()) {
            lines.add(entry.getKey() + ": " + entry.getValue() + LOG_EOL);
        }

        Collections.sort(lines);

        String theString = "";
        for (String line : lines) {
            theString += line;
        }

        return theString;
    }

    public boolean isValid() {
        return isValid;
    }
}
