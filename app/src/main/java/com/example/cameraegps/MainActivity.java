package com.example.cameraegps;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    String currentPhotoPath;
    static final int REQUEST_TAKE_PHOTO = 1;
    File photoFile = null;
    Double latPoint;
    Double lngPoint;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Button cameraBt = findViewById(R.id.cameraBt);
        cameraBt.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                dispatchTakePictureIntentFullSize();
            }
        });
        Button gpsBt = findViewById(R.id.permitirGps);
        gpsBt.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                askForPermission();
            }
        });

    }
    private void askForPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }else {
            configService();
            showDialog();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    configService();
                } else {
                    Toast.makeText(this, "No permission for GPS acessing", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }
    public void configService(){try {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                latPoint = location.getLatitude();
                lngPoint = location.getLongitude();
                Log.i("Latitude ",String.valueOf(latPoint));
            }public void onStatusChanged(String provider, int status, Bundle extras) {

            }public void onProviderEnabled(String provider) {

            }public void onProviderDisabled(String provider) {

            }
        };
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }catch(SecurityException ex){
        Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();}
    }
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg",storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
    private void dispatchTakePictureIntentFullSize() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            photoFile = null;
            try {
                photoFile = createImageFile();
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider",  photoFile );
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                }
            } catch (IOException ex) { }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode,data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if(photoFile !=null){
                Bitmap imageBitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                ImageView iv = findViewById(R.id.imageViewHolder);
                TextView latView = findViewById(R.id.idLat);
                latView.setText("Latitude: "+latPoint);
                TextView lngView = findViewById(R.id.idLng);
                lngView.setText("Longitute: "+lngPoint);
                iv.setImageBitmap(imageBitmap);
                Log.i("Sucesso:", "A imagem foi salva com sucesso");
                try {
                    String b64 = encodeTobase64(imageBitmap);
                    //Log.i("BASE64: ",b64);
                    ArrayList<String> novoJson = new ArrayList<String>();
                    novoJson.add(String.valueOf(latPoint));
                    novoJson.add(String.valueOf(lngPoint));
                    novoJson.add(b64);
                    JsonArray jsonArray = new Gson().toJsonTree(novoJson).getAsJsonArray();
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.add("latPoint", jsonArray.get(0));
                    jsonObject.add("lngPoint", jsonArray.get(1));
                    jsonObject.add("Imagem", jsonArray.get(2));
                    DoRequest r = new DoRequest();
                    r.execute(jsonObject.toString());
                    createImageFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
    public  String encodeTobase64(Bitmap image) {
        Bitmap immagex = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immagex.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);
        return imageEncoded;
    }
    private class DoRequest extends AsyncTask<String, String, String> {

        protected String doInBackground(String... strings) {
            String jsonString = strings[0];
            System.out.println(jsonString);
            MediaType JSON
                    = MediaType.parse("application/json; charset=utf-8");
            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(JSON, jsonString);

            Request newRequest = new Request.Builder()
                    .url("http://192.168.0.100:8080/Cardapio/CameraEGPS")
                    .post(body)
                    .build();
            try (Response response = client.newCall(newRequest).execute()) {
                return jsonString;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        protected void onPostExecute(String result) {

        }
    }
    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Status do GPS")
                .setMessage("A permissão para uso do GPS já foi concedida")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // OK button clicked
                        // Perform any desired actions here
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Cancel button clicked
                        // Perform any desired actions here
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
