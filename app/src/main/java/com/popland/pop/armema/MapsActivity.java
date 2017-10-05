package com.popland.pop.armema;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Geocoder;
import android.location.Location;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import android.location.Address;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.wajahatkarim3.easyflipview.EasyFlipView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;



public class MapsActivity extends FragmentActivity implements ConnectionCallbacks, OnConnectionFailedListener,OnMapReadyCallback {

    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    static final int LOCATION_REQUEST_CODE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //handle incompatible version of GP service on devices
        if(googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(googleApiClient!=null && googleApiClient.isConnected())
            googleApiClient.disconnect();
    }

    public void setupMap(){
        //GPS & Wifi settings
        //request user's precise location
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
           return;
        }

        //get user's current location
        mMap.setMyLocationEnabled(true);//show blue dot & button
        LocationAvailability locationAvailability = LocationServices.FusedLocationApi.getLocationAvailability(googleApiClient);
        if(locationAvailability!=null && locationAvailability.isLocationAvailable())
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        LatLng currentLocation;
        if(lastLocation!=null) {
            currentLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 12));
//            String s = getAddressFromLatLng(currentLocation);
//            Toast.makeText(this,s,Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    boolean lock = true;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);//normal & satellite
        mMap.getUiSettings().setMapToolbarEnabled(false);

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                    MarkerOptions options = new MarkerOptions();
                    options.position(latLng);
                    options.alpha(0.7f);
                    options.icon(BitmapDescriptorFactory.defaultMarker(359));
                    mMap.addMarker(options);//zoom out, marker centers on Map
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
                    InfoDialog infoDialog1 = new InfoDialog(MapsActivity.this);
                    infoDialog1.MapLongClick(latLng);
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                InfoDialog infoDialog2 = new InfoDialog(MapsActivity.this);
                infoDialog2.MarkerClick(marker);
                return false;
            }
        });
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
         setupMap();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    ImageView ivImage;
    double lat,lon;
    Dialog dialog;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==999 && resultCode==RESULT_OK){
            Bitmap bmp = (Bitmap) data.getExtras().get("data");
            ivImage.setImageBitmap(bmp);
        }
    }

    class InfoDialog implements View.OnClickListener{
        Context context;
        EditText edtNote;
        TextView tvAddress;
        EasyFlipView flipView;
        ImageButton ibFlip, ibCamera, ibUpdate;
        LinearLayout llColor;
        int color = 359;

        public InfoDialog(Context context){
            this.context = context;
            dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.info_card);
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.gravity = Gravity.BOTTOM;
            dialog.getWindow().setAttributes(lp);

            edtNote = dialog.findViewById(R.id.edtNote);
            tvAddress = dialog.findViewById(R.id.tvAddress);
            flipView = dialog.findViewById(R.id.flipView);
            ibFlip = dialog.findViewById(R.id.ibFlip);
            ibCamera = dialog.findViewById(R.id.ibCamera);
            ivImage = dialog.findViewById(R.id.ivImage);
            llColor = dialog.findViewById(R.id.llColor);
            ibUpdate = dialog.findViewById(R.id.ibUpdate);

            ibUpdate.setOnClickListener(this);
            ibFlip.setOnClickListener(this);
            ibCamera.setOnClickListener(this);
            dialog.show();
        }

        @Override
        public void onClick(View view) {
            switch(view.getId()){
                case R.id.ibUpdate:
                    String text = edtNote.getText().toString();
                    byte[] image = ImageViewtoByteArray(ivImage);
                    SplashActivity.sqlite.update(text,image,color,lat,lon);
                    Cursor c = SplashActivity.sqlite.getData("SELECT * FROM Marker");
                    int len= -2;
                    while(c.moveToNext()){
                        if(c.getBlob(2)==null)
                            len = -1;
                        else
                            len = c.getBlob(2).length;
                        Log.i("record: ",c.getPosition()+"-"+c.getString(1)+"-"+len+"-"+c.getInt(3)+"-"+c.getDouble(4)+"-"+c.getDouble(5));
                    }
                    dialog.cancel();
                    break;
                case R.id.ibFlip:
                    flipView.flipTheView(true);
                    if(flipView.isBackSide()) {
                        ibCamera.setVisibility(View.VISIBLE);

                        //show color panel
                        if(llColor.getChildCount()>0)
                            llColor.removeAllViews();
                        String[] color_code = getResources().getStringArray(R.array.color_code);
                        int[] hue = getResources().getIntArray(R.array.hue);
                        for(int i=0;i<hue.length;i++){
                            TextView tv = new TextView(context);
                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(180,180);
                            tv.setLayoutParams(lp);
                            tv.setBackgroundColor(Color.parseColor(color_code[i]));
                            tv.setTag(hue[i]);
                            tv.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    color = (int)view.getTag();
                                    Toast.makeText(context,color+"",Toast.LENGTH_SHORT).show();
                                }
                            });
                            llColor.addView(tv);
                        }
                    }else
                        ibCamera.setVisibility(View.INVISIBLE);
                    break;
                case R.id.ibCamera:
                    Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(i,999);
                    break;
            }
        }

        public byte[] ImageViewtoByteArray(ImageView iv){
            BitmapDrawable drawable = (BitmapDrawable) iv.getDrawable();
            Bitmap bmp = drawable.getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG,100,baos);
            byte[] bytes = baos.toByteArray();
            return bytes;
        }

        public void MapLongClick(LatLng latLng) {
            lat = latLng.latitude;
            lon = latLng.longitude;
            Log.i("latLonMap ",lat+" , "+lon);
            tvAddress.setText(getAddressFromLatLng(latLng));
            Cursor cursor = SplashActivity.sqlite.getData("SELECT * FROM Marker WHERE latitude LIKE '%" + lat + "%' AND longitude LIKE '%" + lon + "%'");
            if (cursor.getCount() == 0) {
                SplashActivity.sqlite.insert(lat,lon);
                Cursor c = SplashActivity.sqlite.getData("SELECT * FROM Marker");
                int len= -2;

                while(c.moveToNext()){
                    if(c.getBlob(2)==null)
                        len = -1;
                    else
                        len = c.getBlob(2).length;
                    Log.i("record: ",c.getPosition()+"-"+c.getString(1)+"-"+len+"-"+c.getInt(3)+"-"+c.getDouble(4)+"-"+c.getDouble(5));
                }
                Toast.makeText(context,c.getCount()+"",Toast.LENGTH_SHORT).show();
            }
        }

        public void MarkerClick(Marker marker){
            lat = marker.getPosition().latitude;
            lon = marker.getPosition().longitude;
            Log.i("latLonMarker ",lat+" , "+lon);
            Cursor cursor = SplashActivity.sqlite.getData("SELECT * FROM Marker WHERE latitude LIKE '%" + lat + "%' AND longitude LIKE '%" + lon + "%'");
            tvAddress.setText(getAddressFromLatLng(marker.getPosition()));
            while(cursor.moveToNext()) {
               edtNote.setText(cursor.getString(1));

               Bitmap bitmap = BitmapFactory.decodeByteArray(cursor.getBlob(2), 0, cursor.getBlob(2).length);
               ivImage.setImageBitmap(bitmap);
           }
        }

        public String getAddressFromLatLng(LatLng latLng){// GM Geocoding Api enabled
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            String addressString = "";
            List<Address> addresses;
            Address address;

            try {
                addresses = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
                address = addresses.get(0);
                //for(int i=0;i<address.getMaxAddressLineIndex();i++){
                addressString = address.getAddressLine(0);
                Toast.makeText(context,addressString,Toast.LENGTH_SHORT).show();
                Log.i("Addresses: ",addressString);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return addressString;
        }
    }
}
