package com.example.hayq.distancecalculation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {
    private GoogleMap mMap;
    private LocationManager locationManager;
    private Marker target1;
    private Marker target2;
    private Polygon polygon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_view);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //ui
        Button reset = findViewById(R.id.reset);
        reset.setOnClickListener(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    private LocationListener locationListener = new LocationListener()
    {
        @Override
        public void onLocationChanged(Location location) {
            setCurrentLocation(location);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
        }

        @Override
        public void onProviderDisabled(String s) {
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Location Permishion
        mapInit();

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                setTargets(latLng);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void mapInit() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.INTERNET
                }, 10);
            } else {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            1000, 0, locationListener);
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 10:
                if( grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults.length > 0)
                {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            1000, 0, locationListener);
                    mMap.setMyLocationEnabled(true);
                }
                break;
            default:
                break;
        }
    }

    private void setCurrentLocation(Location marker) {
        double latitute = marker.getLatitude();
        double longitude = marker.getLongitude();

        mMap.addMarker(new MarkerOptions()
            .position(new LatLng(latitute, longitude))
            .draggable(false));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(latitute, longitude))
                .zoom((float) 17)
                .bearing(30)
                .tilt(16)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        locationManager.removeUpdates(locationListener);
    }

    public void setTargets(LatLng targets) {
        Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(targets)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.target)));

        if( target1 == null ) {
            target1 = marker;
            return;
        }

        if( target2 == null ){
            target2 = marker;
        }else{
            target2.remove();
            target2 = marker;
        }

        PolygonOptions polygonOptions = new PolygonOptions()
                .strokeWidth(4)
                .add(target1.getPosition(), target2.getPosition());

        if( polygon != null )
            polygon.remove();

        polygon = mMap.addPolygon(polygonOptions);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.reset:
                resetTargets();
                break;
        }
    }

    private void resetTargets() {
        if( target1 != null ){
            target1.remove();
            target1 = null;
        }

        if( target2 != null ){
            target2.remove();
            target2 = null;
        }

        if( polygon != null ){
            polygon.remove();
            polygon = null;
        }
    }
}
