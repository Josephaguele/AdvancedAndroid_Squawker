package android.example.com.squawker.fcm;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.example.com.squawker.MainActivity;
import android.example.com.squawker.R;
import android.example.com.squawker.provider.SquawkContract;
import android.example.com.squawker.provider.SquawkProvider;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import static android.example.com.squawker.provider.SquawkContract.COLUMN_AUTHOR;
import static android.example.com.squawker.provider.SquawkContract.COLUMN_AUTHOR_KEY;
import static android.example.com.squawker.provider.SquawkContract.COLUMN_DATE;
import static android.example.com.squawker.provider.SquawkContract.COLUMN_MESSAGE;




//  Service in the fcm package that extends from FirebaseMessagingService.
public class SquawkFirebaseMessagingService extends FirebaseMessagingService {


    private static final String JSON_KEY_AUTHOR =       COLUMN_AUTHOR;
    private static final String JSON_KEY_AUTHOR_KEY =   COLUMN_AUTHOR_KEY;
    private static final String JSON_KEY_MESSAGE =      COLUMN_MESSAGE;
    private static final String JSON_KEY_DATE =         COLUMN_DATE;


    //  (2) As part of the new Service - Override onMessageReceived. This method will
    // be triggered whenever a squawk is received. You can get the data from the squawk
    // message using getData(). When you send a test message, this data will include the
    // following key/value pairs:
    // test: true
    // author: Ex. "TestAccount"
    // authorKey: Ex. "key_test"
    // message: Ex. "Hello world"
    // date: Ex. 1484358455343
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> data;

        data = remoteMessage.getData();

        insertSquawk(data);
        sendNotification(data);
    }


// insert a single squawk into the database
    private void insertSquawk(final Map<String, String> data) {

        // network operations must be performed using a background thread and not on the main thread
        AsyncTask<Void, Void, Void> insertSquawkTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_AUTHOR, data.get(JSON_KEY_AUTHOR));
                values.put(COLUMN_AUTHOR_KEY, data.get(JSON_KEY_AUTHOR_KEY));
                values.put(COLUMN_MESSAGE,data.get(JSON_KEY_MESSAGE));
                values.put(COLUMN_DATE, data.get(JSON_KEY_DATE));
                getContentResolver().insert(SquawkProvider.SquawkMessages.CONTENT_URI,values);
                return null;
            }
            
        };
        
        insertSquawkTask.execute();
    }


    //  (3) As part of the new Service - If there is message data, get the data using
    // the keys and do two things with it :
    // 1. Display a notification with the first 30 character of the message
    // 2. Use the content provider to insert a new message into the local database
    // Hint: You shouldn't be doing content provider operations on the main thread.
    // If you don't know how to make notifications or interact with a content provider
    // look at the notes in the classroom for help.

    private void sendNotification(Map<String,String> data) {

        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // Create pending intent to launch the activity
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        // calling the values as a test sample from the squawker server
        // The data is where the information from the server is stored
        String author = data.get(JSON_KEY_AUTHOR);
        String message = data.get(JSON_KEY_MESSAGE) ;


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.test)
                .setContentTitle(author)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Much longer text that cannot fit one line..."))
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

// notificationId is a unique int for each notification that you must define
        int notificationId = 0;
        notificationManager.notify(notificationId, mBuilder.build());
    }


}
