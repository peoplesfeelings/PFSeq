package peoplesfeelingscode.com.pfseq;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import static peoplesfeelingscode.com.pfseq.PFSeq.LOG_TAG;
import static peoplesfeelingscode.com.pfseq.PFSeqMessage.ERROR_MSG_PREFIX;
import static peoplesfeelingscode.com.pfseq.PFSeqMessage.MESSAGE_TYPE_ERROR;

/*
to be extended by any activity that will interact with the PFSeq service
 */

public abstract class PFSeqActivity extends AppCompatActivity {

    private PFSeq service;
    private Class serviceClass;
    private boolean bound;
    private ServiceConnection serviceConnection;

    /*
    stuff that requires a reference to the service should happen here.
    for example, setting up UI listeners that call service methods.
     */
    public abstract void onConnect();

    /*
    display message from service to user, if appopriate for app.
    this precision sequencer may impose limitations to users with regard to audio files properties,
    so relaying error information can help users.
     */
    public abstract void receiveMessage(PFSeqMessage message);

    /*
    the service to start. return YourServiceThatExtendsPFSeq.class
     */
    public abstract Class getServiceClass();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        serviceClass = getServiceClass();
        setUpServiceConnection();

        Log.d(LOG_TAG, "activity created");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d(LOG_TAG, "activity destroyed");
    }

    @Override
    protected void onStart() {
        super.onStart();
        doBindService();
        Log.d(LOG_TAG, "activity on start");
    }

    @Override
    protected void onStop() {
        super.onStop();
        doUnbindService();
        Log.d(LOG_TAG, "activity on stop");
    }

    public PFSeq getService() {
        return service;
    }

    private void setUpServiceConnection() {
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName className, IBinder iBinder) {
                Log.d(LOG_TAG, "service connected");
                bound = true;
                PFSeq.PFSeqBinder binder = (PFSeq.PFSeqBinder) iBinder;
                service = binder.getService();
                service.setCallbacks(PFSeqActivity.this);

                if (service.getStashedMessage() != null) {
                    receiveMessage(service.getStashedMessage());
                    service.setStashedMessage(null);
                }

                onConnect();
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
                Log.d(LOG_TAG, "service disconnected");
                service = null;
                bound = false;
            }
        };
    }

    private boolean doBindService() {
        if (serviceClass == null) {
            receiveMessage(new PFSeqMessage(MESSAGE_TYPE_ERROR, ERROR_MSG_PREFIX + "getServiceClass() returned null. PFSeqActivity base class needs to return Class object form the base class of PFSeq."));
            return false;
        }

        Context context = getApplicationContext();
        if (!serviceRunning()) {
            startService(new Intent(getApplicationContext(), serviceClass));
        }

        if (!bound) {
            Intent intent = new Intent(context, serviceClass);
            bindService(intent, serviceConnection, Context.BIND_ABOVE_CLIENT);
        }

        return true;
    }

    private boolean doUnbindService() {
        if (bound) {
            service.setCallbacks(null);
            unbindService(serviceConnection);
            bound = false;
            Log.d(LOG_TAG, "service unbound");
        }
        return true;
    }

    public boolean serviceRunning() {
        ActivityManager manager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public boolean isBound() {
        return bound;
    }
}
