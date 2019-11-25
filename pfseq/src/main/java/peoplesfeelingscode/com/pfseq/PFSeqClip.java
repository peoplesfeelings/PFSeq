package peoplesfeelingscode.com.pfseq;

import android.media.AudioFormat;
import android.media.MediaFormat;
import android.util.Log;

import java.io.File;

import static peoplesfeelingscode.com.pfseq.PFSeq.LOG_TAG;
import static peoplesfeelingscode.com.pfseq.PFSeq.MICROS_PER_SECOND;
import static peoplesfeelingscode.com.pfseq.PFSeq.NANO_PER_MICROS;
import static peoplesfeelingscode.com.pfseq.PFSeqConfig.MAX_CLIP_FRAMES;
import static peoplesfeelingscode.com.pfseq.PFSeqConfig.MIN_WRITABLE_CONTENT_NANO;

public class PFSeqClip {
    private PFSeq seq;
    private File file;
    private short[] pcm;
    private MediaFormat mediaFormat;
    private boolean loadedSuccessfully;
    private String errorMsg;

    public PFSeqClip(PFSeq seq, File file) {
        this.file = file;
        this.seq = seq;

        this.loadedSuccessfully = load();
    }

    // read PCM data from file
    public boolean load() {
        // make it not null so it's not necessary to check loadedSuccessfully in order to use pcm
        this.pcm = new short[0];

        if (!file.exists() || file.isDirectory()) {
            Log.d(LOG_TAG, "failed to load clip");
            return false;
        }

        try {
            this.mediaFormat = PFSeqAudio.getMediaFormat(file);
        } catch (Exception e) {
            Log.d(LOG_TAG, "failed to get mediaformat of clip. message: " + e.getMessage());
            return false;
        }

        Log.d(LOG_TAG, "file " + file.getName() + " mediaformat: " + this.mediaFormat.toString());

        boolean validated = validate(this.mediaFormat);
        if (!validated) {
            Log.d(LOG_TAG, "file " + file.getName() + " failed validation: " + errorMsg);
            return false;
        }

        try {
            this.pcm = PFSeqAudio.getPcm(file);
        } catch (Exception e) {
            this.errorMsg = "failed to load audio data of file " + file.getName() + ". message: " + e.getMessage();
            Log.d(LOG_TAG, errorMsg);
            return false;
        }

        return true;
    }

    public boolean validate(MediaFormat mediaFormat) {
        // mime
        if (mediaFormat.containsKey(MediaFormat.KEY_MIME)) {
            if (!(mediaFormat.getString(MediaFormat.KEY_MIME).substring(0, 5).equalsIgnoreCase("audio"))) {
                this.errorMsg = "file " + this.file.getName() + " not of audio MIME type. MIME type: " + mediaFormat.getString(MediaFormat.KEY_MIME).substring(0, 5);
                return false;
            }
        } else {
            this.errorMsg = "file " + this.file.getName() + " unknown MIME type";
            return false;
        }

        // channels
        if (mediaFormat.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) {
            if ((mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT) != 2)) {
                this.errorMsg = "file " + this.file.getName() + " channel count is not 2. only channel counts of 2 are supported. count was: " + mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
                return false;
            }
        } else {
            this.errorMsg = "file " + this.file.getName() + " unknown channel count";
            return false;
        }

        // frame rate
        if (mediaFormat.containsKey(MediaFormat.KEY_SAMPLE_RATE)) {
            if ((mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE) != 44100)) {
                this.errorMsg = "file " + this.file.getName() + " sample rate is not 44100. only sample rates of 44100 are supported.";
                return false;
            }
        } else {
            this.errorMsg = "file " + this.file.getName() + " unknown sample rate";
            return false;
        }

        // bit depth
        String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
        // if flac or wav
        if (mediaFormat.containsKey(MediaFormat.KEY_MIME)
                && ( mime.equalsIgnoreCase("audio/raw") || mime.equalsIgnoreCase("audio/flac") ) ) {
            if (mediaFormat.containsKey(MediaFormat.KEY_PCM_ENCODING)) {
                if ((mediaFormat.getInteger(MediaFormat.KEY_PCM_ENCODING) != AudioFormat.ENCODING_PCM_16BIT)) {
                    this.errorMsg = "file " + this.file.getName() + " bit depth is not 16. only bit depths of 16 are supported";
                    return false;
                }
            } else {
                if (mediaFormat.containsKey("bits-per-sample")) {
                    if ((mediaFormat.getInteger("bits-per-sample") != 16)) {
                        this.errorMsg = "file " + this.file.getName() + " bit depth is not 16. only bit depths of 16 are supported";
                        return false;
                    }
                } else {
                    this.errorMsg = "file " + this.file.getName() + " unknown bit depth";
                    return false;
                }
            }
        }

        // length
        if (mediaFormat.containsKey(MediaFormat.KEY_DURATION)) {
            long durationMicro = mediaFormat.getLong(MediaFormat.KEY_DURATION);

            int minLengthMicro = (int) (this.seq.getConfig().getInt(MIN_WRITABLE_CONTENT_NANO) / NANO_PER_MICROS);
            int maxLengthMicro = (int) (this.seq.framesToNano(this.seq.getConfig().getInt(MAX_CLIP_FRAMES)) / NANO_PER_MICROS);

            if (durationMicro > maxLengthMicro) {
                this.errorMsg = "file " + this.file.getName() + " is greater than max length. length was: " + mediaFormat.getInteger(MediaFormat.KEY_DURATION) / MICROS_PER_SECOND + " s";
                return false;
            }
            if (durationMicro < minLengthMicro) {
                this.errorMsg = "file " + this.file.getName() + " is less than min length. length was: " + mediaFormat.getInteger(MediaFormat.KEY_DURATION) / MICROS_PER_SECOND + " s";
                return false;
            }
        } else {
            this.errorMsg = "file " + this.file.getName() + " unknown length";
            return false;
        }


        return true;
    }

    // accessors
    public MediaFormat getMediaFormat() {
        return mediaFormat;
    }
    public short[] getPcm() {
        return pcm;
    }
    public boolean isLoadedSuccessfully() {
        return loadedSuccessfully;
    }
    public String getErrorMsg() {
        return errorMsg;
    }
    public File getFile() {
        return file;
    }
}
