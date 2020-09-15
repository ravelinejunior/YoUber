package br.com.ravelineUber.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import br.com.ravelineUber.R;
import br.com.ravelineUber.model.Driver;

public class Common {

    public static final String DRIVER_INFO_REFERENCE = "DriverInfo";
    public static final String DRIVERS_LOCATION_REFERENCES = "DriversLocation";
    public static final String TOKEN_REFERENCE = "Token";
    public static final String NOTI_TITLE = "title";
    public static final String NOTI_CONTENT = "body";
    public static Driver currentUser;

    public static String buildWelcomeMessage() {

        if (currentUser != null)
            return new StringBuilder("Welcome ")
                    .append(Common.currentUser.getFirstName())
                    .append(" ")
                    .append(Common.currentUser.getLastName()).toString();
        else return "";
    }


    public static void showNotification(Context context, int id, String title, String body, Intent i) {
        PendingIntent pendingIntent = null;
        if (i != null) {
            pendingIntent = PendingIntent.getActivity(context, id, i, PendingIntent.FLAG_UPDATE_CURRENT);
            String NOTIFICATION_CHANNEL_ID = "raveline_new_uber";
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel = new NotificationChannel(
                        NOTIFICATION_CHANNEL_ID, "YoUber Raveline", NotificationManager.IMPORTANCE_HIGH );
                notificationChannel.setDescription("YoUber Drive ");
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.RED);
                notificationChannel.setVibrationPattern(new long[]{0,1000,500,1000});
                notificationChannel.enableVibration(true);

                notificationManager.createNotificationChannel(notificationChannel);
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context,NOTIFICATION_CHANNEL_ID);
            builder.setContentTitle(title)
                    .setAutoCancel(false)
                    .setAllowSystemGeneratedContextualActions(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setSmallIcon(R.drawable.ic_car_black)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_car_black));
            if(pendingIntent != null)
                builder.setContentIntent(pendingIntent);

            Notification notification = builder.build();
            notificationManager.notify(id,notification);

        }
    }
}
