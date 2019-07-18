package com.example.video111;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

    private TextView messageText;
    private Button uploadButton, btnselectpic, btnselectaudio, btnselectvideo;
    private ImageView imageview;
    private int serverResponseCode = 0;
    private ProgressDialog dialog = null;

    private String upLoadServerUri = null;
    private String filepath = null;
    int FLAG = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uploadButton = (Button) findViewById(R.id.uploadButton);
        messageText = (TextView) findViewById(R.id.messageText);
        btnselectpic = (Button) findViewById(R.id.button_selectpic);
        btnselectaudio = (Button) findViewById(R.id.button_selectaudio);
        btnselectvideo = (Button) findViewById(R.id.button_selectvideo);
        imageview = (ImageView) findViewById(R.id.imageView_pic);

        btnselectpic.setOnClickListener(this);
        btnselectaudio.setOnClickListener(this);
        btnselectvideo.setOnClickListener(this);
        uploadButton.setOnClickListener(this);
        upLoadServerUri = "http://192.168.43.110/testing/upload_to_server.php";
    }

    @Override
    public void onClick(View arg0) {
        if (arg0 == btnselectpic) {
            FLAG = 1;
            Intent intent = new Intent();
            // intent.setType("video/*");
            // intent.setType("audio/*");
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(
                    Intent.createChooser(intent, "Complete action using"), 1);

            if (btnselectpic.isPressed()) {
                Drawable bitmap = getResources().getDrawable(R.drawable.ic_launcher_background);
                imageview.setImageDrawable(bitmap);
            }

        } else if (arg0 == btnselectaudio) {
            FLAG = 1;
            Intent intent = new Intent();
            // intent.setType("video/*");
            intent.setType("audio/*");
            // intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(
                    Intent.createChooser(intent, "Complete action using"), 1);

            if (btnselectaudio.isPressed()) {
                Drawable bitmap = getResources().getDrawable(R.drawable.ic_launcher_background);
                imageview.setImageDrawable(bitmap);
            }

        } else if (arg0 == btnselectvideo) {
            FLAG = 1;
            Intent intent = new Intent();
            intent.setType("video/*");
            // intent.setType("audio/*");
            // intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(
                    Intent.createChooser(intent, "Complete action using"), 1);

            if (btnselectvideo.isPressed()) {
                Drawable bitmap = getResources().getDrawable(R.drawable.ic_launcher_background);
                imageview.setImageDrawable(bitmap);
            }

        } else if (arg0 == uploadButton) {

            if (FLAG == 0) {
                Toast.makeText(
                        MainActivity.this,
                        "Please select atleast one image or audio or video !!!",
                        Toast.LENGTH_LONG).show();
            } else {
                if (filepath != null) {
                    dialog = ProgressDialog.show(MainActivity.this, "",
                            "Uploading file...", true);
                    messageText.setText("uploading started.....");
                    new Thread(new Runnable() {
                        public void run() {
                            uploadFile(filepath);
                        }
                    }).start();
                } else {
                    Toast.makeText(MainActivity.this, "Please try again !!!",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1 && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            filepath = getPath(selectedImageUri);
            Bitmap bitmap = BitmapFactory.decodeFile(filepath);
            imageview.setImageBitmap(bitmap);

            messageText.setText("Uploading file path:" + filepath);

        }
    }

    @SuppressWarnings("deprecation")
    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public int uploadFile(final String sourceFileUri) {

        String fileName = sourceFileUri;

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);

        if (!sourceFile.isFile()) {

            dialog.dismiss();

            Log.e("uploadFile", "Source File not exist :" + filepath);

            runOnUiThread(new Runnable() {
                public void run() {
                    messageText.setText("Source File not exist :" + filepath);
                }
            });

            return 0;

        } else {
            try {
                FileInputStream fileInputStream = new FileInputStream(
                        sourceFile);
                URL url = new URL(upLoadServerUri);
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type",
                        "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + fileName + "\"" + lineEnd);

                dos.writeBytes(lineEnd);

                // create a buffer of maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

                if (serverResponseCode == 200) {

                    runOnUiThread(new Runnable() {
                        public void run() {
                            String msg = "File Upload Completed.\n\n See uploaded file here : \n\n"
                                    + " c:/xampp/htdocs/testing/uploads";
                            messageText.setText(msg);
                            Toast.makeText(MainActivity.this,
                                    "File Upload Complete.", Toast.LENGTH_LONG)
                                    .show();
                           // Toast.makeText(MainActivity.this, serverResponseCode, Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                // close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {

                dialog.dismiss();
                ex.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
                        messageText
                                .setText("MalformedURLException Exception : check script url.");
                        Toast.makeText(MainActivity.this,
                                "MalformedURLException", Toast.LENGTH_SHORT)
                                .show();
                    }
                });

                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {

                dialog.dismiss();
                e.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
                        messageText.setText("Got Exception : see logcat ");
                        Toast.makeText(MainActivity.this,
                                "Got Exception : see logcat ",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                Log.e("Upload file to server Exception",
                        "Exception : " + e.getMessage(), e);
            }
            dialog.dismiss();
            return serverResponseCode;
        }
    }

}
