package com.example.mdm.brainstorm;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;


import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.util.EntityUtils;
import org.apache.james.mime4j.message.SingleBody;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {
    ImageButton start;
    ImageButton stop;
    ImageButton play;
    ImageButton submit;
    ImageButton left;
    ImageButton right;
    ImageView sendTo;
    int counter=0;
    Button mainMenu;
    private MediaRecorder myRecorder;
    private String outputFile = null;
    private String value = null;
    private boolean isRecording = false;
    private static String audioFilePath;
    MediaPlayer mediaPlayer = new MediaPlayer();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start = (ImageButton)findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                startRecord(v);
            }
        });

        sendTo = (ImageView)findViewById(R.id.imageView2);
        stop = (ImageButton)findViewById(R.id.stop);
        stop.setEnabled(false);
        stop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                stopRecord(v);
            }
        });

        play = (ImageButton)findViewById(R.id.play);
        play.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                try {
                    playRecord(v);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        left = (ImageButton)findViewById(R.id.left);
        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                    left(v);

            }
        });

        right = (ImageButton)findViewById(R.id.right);
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                left(v);

            }
        });

        submit = (ImageButton)findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                try {
                    submit(v);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

//        mainMenu = (Button)findViewById(R.id.MainMenu);
//        mainMenu.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                // TODO Auto-generated method stub
//                mainMenu(v);
//            }
//        });


        audioFilePath =  Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/myaudio.3gp";

    }//end onCreate

    private void left(View v) {
        if(counter == 0) {
            sendTo.setImageResource(R.drawable.group);
            counter = 1;
        }
        if (counter==1) {
            sendTo.setImageResource(R.drawable.single);
            counter = 0;
        }

    }

    public void startRecord(View view){




        myRecorder = new MediaRecorder();
        myRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        myRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        myRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        myRecorder.setOutputFile(audioFilePath);


        try {
            myRecorder.prepare();
            myRecorder.start();
        } catch (IllegalStateException e) {
            // start:it is called before prepare()
            // prepare: it is called after start() or before setOutputFormat()
            e.printStackTrace();
        } catch (IOException e) {
            // prepare() fails
            e.printStackTrace();
        }

        start.setEnabled(false);
        stop.setEnabled(true);

        Toast.makeText(getApplicationContext(), "Start recording...",
                Toast.LENGTH_SHORT).show();

       }//end else

    public void playRecord(View view) throws IOException {


        start.setEnabled(true);
        stop.setEnabled(true);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setDataSource(audioFilePath);
        mediaPlayer.prepare();
        mediaPlayer.start();
        isRecording = true;


    }//end else

    public void stopRecord(View view){

        stop.setEnabled(false);
        play.setEnabled(true);


            start.setEnabled(false);
            myRecorder.stop();
            myRecorder.release();
            myRecorder = null;
            isRecording = false;

        Toast.makeText(getApplicationContext(), "Done recording...",
                Toast.LENGTH_SHORT).show();

    }//end stopRecord

    public void submit(View view) throws IOException {
        Log.d("Attempting to post", "Attempting to post");
        new MyAsyncTask().execute(audioFilePath);
        Toast.makeText(getApplicationContext(), "Sending...",
                Toast.LENGTH_SHORT).show();






    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Double> {

        @Override
        protected Double doInBackground(String... params) {
            // TODO Auto-generated method stub
            postData(params[0]);
            return null;
        }



        public void postData(String valueIWantToSend) {
            // Create a new HttpClient and Post Header
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://mydea.io:8080/api/ideas");
            httppost.setHeader("Authorization", "6969");

            try {
                // Add your data
                //List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                //MultipartEntity entity = new MultipartEntity();
                //FileEntity fe = new FileEntity(new File(audioFilePath), "/");
                FileBody fileBody = new FileBody(new File(audioFilePath));

                MultipartEntity entity = new MultipartEntity();
                entity.addPart("value", fileBody);
                entity.addPart("ext", new StringBody("3gp"));
                httppost.setEntity(entity);

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                String responseStr = EntityUtils.toString(response.getEntity());
                Log.d("HERE:", responseStr);

            } catch (ClientProtocolException e) {
                Log.d("client", "protocol exception");
            } catch (IOException e) {
                Log.d("IOEXCEPTION", "YOU MORON");
            }
        }

    }
}

