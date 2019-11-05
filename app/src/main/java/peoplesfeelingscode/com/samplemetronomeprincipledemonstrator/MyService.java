package peoplesfeelingscode.com.samplemetronomeprincipledemonstrator;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import peoplesfeelingscode.com.pfseq.PFSeq;

public class MyService extends PFSeq {

    @Override
    public Notification getNotification() {
        Context context = getApplicationContext();
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(context, 0, intent, 0);

        Notification notification = null;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
            String channelId = "the channel id";
            NotificationChannel mChannel = new NotificationChannel(channelId, "the public channel name", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.createNotificationChannel(mChannel);

            notification =
                    new Notification.Builder(context, channelId)
                            .setContentTitle("notif title")
                            .setContentText("notif content text")
                            .setSmallIcon(R.drawable.notif_icon)
                            .setContentIntent(pendingIntent)
                            .setTicker("notif ticker text")
                            .build();
        } else {
            notification =
                    new Notification.Builder(context)
                            .setPriority(Notification.PRIORITY_HIGH)
                            .setContentTitle("notif title")
                            .setContentText("notif content text")
                            .setSmallIcon(R.drawable.notif_icon)
                            .setContentIntent(pendingIntent)
                            .setTicker("notif ticker text")
                            .build();
        }

        return notification;
    }
}
