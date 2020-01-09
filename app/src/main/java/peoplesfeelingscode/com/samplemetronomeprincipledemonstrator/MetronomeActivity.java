package peoplesfeelingscode.com.samplemetronomeprincipledemonstrator;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import peoplesfeelingscode.com.pfseq.PFSeq;
import peoplesfeelingscode.com.pfseq.PFSeqActivity;
import peoplesfeelingscode.com.pfseq.PFSeqClip;
import peoplesfeelingscode.com.pfseq.PFSeqConfig;
import peoplesfeelingscode.com.pfseq.PFSeqMessage;
import peoplesfeelingscode.com.pfseq.PFSeqPianoRollItem;
import peoplesfeelingscode.com.pfseq.PFSeqTimeOffset;
import peoplesfeelingscode.com.pfseq.PFSeqTrack;

import static peoplesfeelingscode.com.pfseq.PFSeq.LOG_TAG;
import static peoplesfeelingscode.com.pfseq.PFSeqConfig.ID;
import static peoplesfeelingscode.com.pfseq.PFSeqConfig.ONGOING_NOTIF_ID;
import static peoplesfeelingscode.com.pfseq.PFSeqConfig.TIME_SIG_LOWER;
import static peoplesfeelingscode.com.pfseq.PFSeqConfig.TIME_SIG_UPPER;
import static peoplesfeelingscode.com.pfseq.PFSeqMessage.ALERT_MSG_PREFIX;
import static peoplesfeelingscode.com.pfseq.PFSeqMessage.ERROR_MSG_PREFIX;
import static peoplesfeelingscode.com.pfseq.PFSeqMessage.MESSAGE_TYPE_ALERT;
import static peoplesfeelingscode.com.pfseq.PFSeqMessage.MESSAGE_TYPE_ERROR;

public class MetronomeActivity extends PFSeqActivity {
    final String CONFIG_ID = "metronome";

    Button playbtn;
    Button stopbtn;
    Button mainBtn;
    SeekBar seekBar;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metronome);

        playbtn = findViewById(R.id.playbtn);
        stopbtn = findViewById(R.id.stopbtn);
        mainBtn = findViewById(R.id.main);
        seekBar = findViewById(R.id.seekBar);
        textView = findViewById(R.id.textView);
        seekBar.setMax(1000);

        setUpListeners();
    }

    @Override
    public void onConnect() {
        if (getSeq().isSetUp()) {
            if (!getSeq().getConfig().getString(ID).equals(CONFIG_ID)) {
                getSeq().unSetUpSequencer();
                boolean success = configureSequecer(getSeq());
                if (success) {
                    setUpTracks(getSeq());
                }
            }
        } else {
            boolean success = configureSequecer(getSeq());
            if (success) {
                setUpTracks(getSeq());
            }
        }

        playbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSeq().play();
            }
        });
        stopbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSeq().stop();
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                double bpm = (i / 1000.0 * 1000) + 15;
                double allowedBpm = getSeq().setBpm(bpm);
                textView.setText(allowedBpm + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public void receiveMessage(PFSeqMessage message) {
        String prefix = "";

        switch (message.getType()) {
            case MESSAGE_TYPE_ALERT: prefix = ALERT_MSG_PREFIX;
                break;
            case MESSAGE_TYPE_ERROR: prefix = ERROR_MSG_PREFIX;
                break;
        }

        Log.d(LOG_TAG, "receiveMessage call - " + prefix + message.getMessage());
    }

    @Override
    public Class getServiceClass() {
        return MyService.class;
    }

    private void setUpListeners() {
        mainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MetronomeActivity.this, MainActivity.class));
            }
        });
    }

    private boolean configureSequecer(PFSeq seq) {
        HashMap<String, Boolean> myConfigBools = new HashMap<String, Boolean>() {{
//            put(RUN_IN_FOREGROUND, false);
//            put(REPEATING, false);
        }};
        HashMap<String, Integer> myConfigInts = new HashMap<String, Integer>() {{
            put(ONGOING_NOTIF_ID, 4346);
            put(TIME_SIG_UPPER, 1);
            put(TIME_SIG_LOWER, 4);
        }};
        HashMap<String, String> myConfigStrings = new HashMap<String, String>() {{
            put(ID, CONFIG_ID);
        }};

        PFSeqConfig exampleConfig = new PFSeqConfig(myConfigInts, myConfigBools, null, myConfigStrings);

        boolean seqSetupSuccess = seq.setUpSequencer(exampleConfig);

        if (!seqSetupSuccess) {
            receiveMessage(new PFSeqMessage(MESSAGE_TYPE_ALERT, "failed to set Up Sequencer"));
            return false;
        }
        return true;
    }

    private boolean setUpTracks(PFSeq seq) {
        File audFile;
        try {
            audFile = File.createTempFile("demo_app_file", "");
            InputStream ins = getResources().openRawResource(R.raw.guitar_hit_5);
//            InputStream ins = getResources().openRawResource(R.raw.guitar_hit_1_flac);
//            InputStream ins = getResources().openRawResource(R.raw.guitr_hit_1_cbr_mp3);
//            InputStream ins = getResources().openRawResource(R.raw.tone_mono_cbr_mp3);
//            InputStream ins = getResources().openRawResource(R.raw.dewip_16bit_stereo_smoother);
            OutputStream out = new FileOutputStream(audFile);

            byte[] buffer = new byte[1024];
            int read;
            while((read = ins.read(buffer)) != -1){
                out.write(buffer, 0, read);
            }
        } catch (IOException e) {
            receiveMessage(new PFSeqMessage(MESSAGE_TYPE_ERROR, "error creating file object \n" + e.getStackTrace().toString()));
            return false;
        }
        PFSeqTrack metronomeTrack = new PFSeqTrack(seq, "metronome");
        PFSeqClip clip = new PFSeqClip(seq, audFile);

        PFSeqTimeOffset timeOffset;

        timeOffset = PFSeqTimeOffset.make(0, PFSeqTimeOffset.MODE_FRACTIONAL, 0, 4, 0,false, 0);
        PFSeqPianoRollItem item1 = new PFSeqPianoRollItem(seq, clip, "item 1", timeOffset);
        metronomeTrack.addPianoRollItem(item1);

        seq.addTrack(metronomeTrack);

        return true;
    }
}