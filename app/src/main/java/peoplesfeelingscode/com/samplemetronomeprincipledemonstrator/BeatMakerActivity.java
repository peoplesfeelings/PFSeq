package peoplesfeelingscode.com.samplemetronomeprincipledemonstrator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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

import static peoplesfeelingscode.com.pfseq.PFSeqConfig.ID;
import static peoplesfeelingscode.com.pfseq.PFSeqConfig.ONGOING_NOTIF_ID;
import static peoplesfeelingscode.com.pfseq.PFSeqConfig.TIME_SIG_LOWER;
import static peoplesfeelingscode.com.pfseq.PFSeqConfig.TIME_SIG_UPPER;
import static peoplesfeelingscode.com.pfseq.PFSeqMessage.MESSAGE_TYPE_ALERT;
import static peoplesfeelingscode.com.pfseq.PFSeqMessage.MESSAGE_TYPE_ERROR;

public class BeatMakerActivity extends PFSeqActivity {
    final String CONFIG_ID = "beat maker";

    Button mainBtn;
    Button playBtn;
    Button stopBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beat_maker);

        mainBtn = findViewById(R.id.mainBtn);
        playBtn = findViewById(R.id.playBtn);
        stopBtn = findViewById(R.id.stopBtn);

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
    }

    @Override
    public void receiveMessage(PFSeqMessage message) {
        //
    }

    @Override
    public Class getServiceClass() {
        return MyService.class;
    }

    private void setUpListeners() {
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSeq().play();
            }
        });
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSeq().stop();
            }
        });
        mainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(BeatMakerActivity.this, MainActivity.class));
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
        File audFile1;
        File audFile2;

        try {
            audFile1 = File.createTempFile("demo_app_file", "");
            InputStream ins = getResources().openRawResource(R.raw.guitar_hit_5);
            OutputStream out = new FileOutputStream(audFile1);

            byte[] buffer = new byte[1024];
            int read;
            while((read = ins.read(buffer)) != -1){
                out.write(buffer, 0, read);
            }

            audFile2 = File.createTempFile("demo_app_file", "");
            ins = getResources().openRawResource(R.raw.guitar_hit_1_flac);
//            ins = getResources().openRawResource(R.raw.dewip_16bit_stereo_smoother);
            out = new FileOutputStream(audFile2);

            buffer = new byte[1024];
            while((read = ins.read(buffer)) != -1){
                out.write(buffer, 0, read);
            }
        } catch (IOException e) {
            receiveMessage(new PFSeqMessage(MESSAGE_TYPE_ERROR, "error creating file object \n" + e.getStackTrace().toString()));
            return false;
        }

        PFSeqClip clipA1 = new PFSeqClip(seq, audFile1);
        PFSeqClip clipB1 = new PFSeqClip(seq, audFile2);

        PFSeqTrack trackA = new PFSeqTrack(seq, "track a");

        PFSeqTimeOffset timeOffsetA1 = PFSeqTimeOffset.make(0, PFSeqTimeOffset.MODE_FRACTIONAL, 0, 4, 0,false, 0);
        PFSeqPianoRollItem itemA1 = new PFSeqPianoRollItem(seq, clipA1, "item a1", timeOffsetA1);
        trackA.addPianoRollItem(itemA1);

        seq.addTrack(trackA);

        PFSeqTrack trackB = new PFSeqTrack(seq, "track b");

        PFSeqTimeOffset timeOffsetB1 = PFSeqTimeOffset.make(0, PFSeqTimeOffset.MODE_FRACTIONAL, 0, 4, 1,false, 0);
        PFSeqPianoRollItem itemB1 = new PFSeqPianoRollItem(seq, clipB1, "item b1", timeOffsetB1);
        trackB.addPianoRollItem(itemB1);

        PFSeqTimeOffset timeOffsetB2  = PFSeqTimeOffset.make(0, PFSeqTimeOffset.MODE_FRACTIONAL, 0, 4, 2, false, 0);
        PFSeqPianoRollItem itemB2 = new PFSeqPianoRollItem(seq, clipB1, "item b2", timeOffsetB2);
        trackB.addPianoRollItem(itemB2);

        seq.addTrack(trackB);

        return true;
    }
}
