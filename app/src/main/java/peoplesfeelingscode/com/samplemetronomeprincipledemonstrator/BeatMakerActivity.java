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
import peoplesfeelingscode.com.pfseq.PFSeqLength;
import peoplesfeelingscode.com.pfseq.PFSeqMessage;
import peoplesfeelingscode.com.pfseq.PFSeqPianoRollItem;
import peoplesfeelingscode.com.pfseq.PFSeqTimeOffset;
import peoplesfeelingscode.com.pfseq.PFSeqTrack;

import static peoplesfeelingscode.com.pfseq.PFSeqConfig.ID;
import static peoplesfeelingscode.com.pfseq.PFSeqConfig.ONGOING_NOTIF_ID;
import static peoplesfeelingscode.com.pfseq.PFSeqConfig.TIME_SIG_UPPER;
import static peoplesfeelingscode.com.pfseq.PFSeqLength.MODE_FRACTIONAL;
import static peoplesfeelingscode.com.pfseq.PFSeqMessage.MESSAGE_TYPE_ALERT;
import static peoplesfeelingscode.com.pfseq.PFSeqMessage.MESSAGE_TYPE_ERROR;

public class BeatMakerActivity extends PFSeqActivity {
    final String CONFIG_ID = "beat maker";
    final String TRACK_A = "track a";
    final String TRACK_B = "track b";
    final String ITEM_A1 = "item a1";
    final String ITEM_A2 = "item a2";
    final String ITEM_A3 = "item a3";
    final String ITEM_B1 = "item b1";
    final String ITEM_B2 = "item b2";
    final String ITEM_B3 = "item b3";


    Button mainBtn;
    Button playBtn;
    Button stopBtn;
    Button thingBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beat_maker);

        mainBtn = findViewById(R.id.mainBtn);
        playBtn = findViewById(R.id.playBtn);
        stopBtn = findViewById(R.id.stopBtn);
        thingBtn = findViewById(R.id.thingBtn);

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

        thingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // do thing

                getSeq().getTrack(TRACK_A).getPrItem(ITEM_A1).setVelocity(0.2);
                getSeq().getTrack(TRACK_A).getPrItem(ITEM_A2).setVelocity(0.2);
                getSeq().getTrack(TRACK_A).getPrItem(ITEM_A3).setVelocity(0.2);
            }
        });
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
            put(TIME_SIG_UPPER, 2);
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
        File audFile3;

        try {
            audFile1 = File.createTempFile("demo_app_file", "");
            InputStream ins = getResources().openRawResource(R.raw.guitar_hit_1_flac);
            OutputStream out = new FileOutputStream(audFile1);

            byte[] buffer = new byte[1024];
            int read;
            while((read = ins.read(buffer)) != -1){
                out.write(buffer, 0, read);
            }

            audFile2 = File.createTempFile("demo_app_file", "");
            ins = getResources().openRawResource(R.raw.guitar_hit_5);
            out = new FileOutputStream(audFile2);

            buffer = new byte[1024];
            while((read = ins.read(buffer)) != -1){
                out.write(buffer, 0, read);
            }

            audFile3 = File.createTempFile("demo_app_file", "");
            ins = getResources().openRawResource(R.raw.dewip_16bit_stereo_smoother);
            out = new FileOutputStream(audFile3);

            buffer = new byte[1024];
            while((read = ins.read(buffer)) != -1){
                out.write(buffer, 0, read);
            }
        } catch (IOException e) {
            receiveMessage(new PFSeqMessage(MESSAGE_TYPE_ERROR, "error creating file object \n" + e.getStackTrace().toString()));
            return false;
        }

        PFSeqClip clip1 = new PFSeqClip(seq, audFile1);
        PFSeqClip clip2 = new PFSeqClip(seq, audFile2);
        PFSeqClip clip3 = new PFSeqClip(seq, audFile3);
        PFSeqTimeOffset lengthOffset1 = PFSeqTimeOffset.make(0, PFSeqTimeOffset.MODE_FRACTIONAL, 0, 16, 1, false, 0);
        PFSeqLength length1 = new PFSeqLength(getSeq(), MODE_FRACTIONAL, lengthOffset1, 0);
        PFSeqTimeOffset lengthOffset2 = PFSeqTimeOffset.make(0, PFSeqTimeOffset.MODE_FRACTIONAL, 0, 8, 3, false, 0);
        PFSeqLength length2 = new PFSeqLength(getSeq(), MODE_FRACTIONAL, lengthOffset2, 0);

        PFSeqTrack trackA = new PFSeqTrack(seq, TRACK_A);

        PFSeqTimeOffset timeOffsetA1 = PFSeqTimeOffset.make(0, PFSeqTimeOffset.MODE_FRACTIONAL, 0, 4, 0,false, 0);
        PFSeqPianoRollItem itemA1 = new PFSeqPianoRollItem(seq, clip2,ITEM_A1 , timeOffsetA1);
        itemA1.setLength(length2);
        trackA.addPianoRollItem(itemA1);

        PFSeqTimeOffset timeOffsetA2 = PFSeqTimeOffset.make(0, PFSeqTimeOffset.MODE_FRACTIONAL, 0, 4, 1,false, 0);
        PFSeqPianoRollItem itemA2 = new PFSeqPianoRollItem(seq, clip2, ITEM_A2, timeOffsetA2);
        itemA2.setLength(length2);
        trackA.addPianoRollItem(itemA2);

        PFSeqTimeOffset timeOffsetA3 = PFSeqTimeOffset.make(0, PFSeqTimeOffset.MODE_FRACTIONAL, 0, 4, 2,false, 0);
        PFSeqPianoRollItem itemA3 = new PFSeqPianoRollItem(seq, clip2, ITEM_A3, timeOffsetA3);
        itemA3.setLength(length2);
        trackA.addPianoRollItem(itemA3);

        seq.addTrack(trackA);

        PFSeqTrack trackB = new PFSeqTrack(seq, TRACK_B);

        PFSeqTimeOffset timeOffsetB1 = PFSeqTimeOffset.make(1, PFSeqTimeOffset.MODE_FRACTIONAL, 0, 4, 0,false, 0);
        PFSeqPianoRollItem itemB1 = new PFSeqPianoRollItem(seq, clip2, ITEM_B1, timeOffsetB1);
        itemB1.setLength(length1);
        trackB.addPianoRollItem(itemB1);

        PFSeqTimeOffset timeOffsetB2  = PFSeqTimeOffset.make(1, PFSeqTimeOffset.MODE_FRACTIONAL, 0, 4, 1, false, 0);
        PFSeqPianoRollItem itemB2 = new PFSeqPianoRollItem(seq, clip2, ITEM_B2, timeOffsetB2);
        itemB2.setLength(length1);
        trackB.addPianoRollItem(itemB2);

        PFSeqTimeOffset timeOffsetB3  = PFSeqTimeOffset.make(1, PFSeqTimeOffset.MODE_FRACTIONAL, 0, 4, 2, false, 0);
        PFSeqPianoRollItem itemB3 = new PFSeqPianoRollItem(seq, clip2, ITEM_B3, timeOffsetB3);
        itemB3.setLength(length1);
        trackB.addPianoRollItem(itemB3);

        seq.addTrack(trackB);

        return true;
    }
}
