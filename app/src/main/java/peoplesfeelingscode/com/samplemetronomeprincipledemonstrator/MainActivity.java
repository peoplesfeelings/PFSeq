package peoplesfeelingscode.com.samplemetronomeprincipledemonstrator;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import peoplesfeelingscode.com.pfseq.PFSeq;
import peoplesfeelingscode.com.pfseq.PFSeqActivity;
import peoplesfeelingscode.com.pfseq.PFSeqMessage;

public class MainActivity extends PFSeqActivity {
    final static String DEMO_TAG = PFSeq.LOG_TAG + "demo app**";

    Button unsetupBtn;
    Button metronomeBtn;
    Button beatMakerBtn;

    @Override
    public void onConnect() {
        //
    }

    @Override
    public void receiveMessage(PFSeqMessage message) {
        //
    }

    @Override
    public Class getServiceClass() {
        return MyService.class;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        unsetupBtn = findViewById(R.id.unsetupBtn);
        metronomeBtn = findViewById(R.id.metronomeBtn);
        beatMakerBtn = findViewById(R.id.beatMakerBtn);

        setUpListeners();
    }

    private void setUpListeners() {
        unsetupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isBound()) {
                    getSeq().unSetUpSequencer();
                } else {
                    Log.d(DEMO_TAG, "not bound");
                }
            }
        });

        metronomeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, MetronomeActivity.class));
            }
        });

        beatMakerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //
            }
        });
    }
}

