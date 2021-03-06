package projectetaxi.etaxi_v1;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class DestinationSelectionActivity extends FragmentActivity implements

        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMapLongClickListener {

    private  String currentLat;
    private  String currentLng;
    private  String destinationLat;
    private  String destinationLng;
    private String driverEmail;

    private double doubleCurrentLat;
    private double doubleCurrentLng;
    private double doubleDestinationLat;
    private double doubleDestinationLng;


    public String getCurrentLat() {
        return currentLat;
    }

    public String getCurrentLng() {
        return currentLng;
    }

    public String getDestinationLat() {
        return destinationLat;
    }

    public String getDestinationLng() {
        return destinationLng;
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    ArrayList<LatLng> MarkerPoints;
    Marker mCurrLocationMarker, marker;
    LocationRequest mLocationRequest;
    private GoogleMap mMap;
    TextView tvDistanceDuration;

    static LatLng currentlatLng;

    final String TAG = this.getClass().getName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_destination_selection);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        //tvDistanceDuration = (TextView) findViewById(R.id.tv_distance_time);

        // Initializing
        MarkerPoints = new ArrayList<LatLng>();


        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager()
                        .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        //mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);

                Log.d(TAG, "Set Location----> "+mMap);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

    //  mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        Log.d(TAG, "Set Long Click Location----> "+mMap);

        // Set a listener for marker click.
        mMap.setOnMarkerClickListener(this);
        Log.d(TAG, "Set On Marker Click Location----> "+mMap);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }


    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest, this);

            Log.d(TAG, "Requested Location----> "+mLocationRequest);
        }
    }



    @Override
    public void onConnectionSuspended(int i) {
    }



    @Override
    public void onLocationChanged(Location location) {

        Log.d(TAG, "Changed Location -----> " + location);

        mLastLocation = location;
        if (mCurrLocationMarker != null) {

            Log.d(TAG, "If mLastLocation: " + mLastLocation + " = " + " location: " + location);
            mCurrLocationMarker.remove();
        }


        //Showing Current Location Marker on Map
        LatLng latLng = new LatLng(
                location.getLatitude(),
                location.getLongitude()
        );

        currentlatLng = latLng;


        Log.d(TAG, "Current Location----> " + latLng);
        Log.d(TAG, "Current Latitude----> " + location.getLatitude());
        Log.d(TAG, "Current Longitude----> " + location.getLongitude());

        doubleCurrentLat = location.getLatitude();
        doubleCurrentLng = location.getLongitude();

        currentLat = String.valueOf(location.getLatitude());
        currentLng = String.valueOf(location.getLongitude());


        Bundle bundle = getIntent().getExtras();
        driverEmail = bundle.getString("driverEmail");
        Log.d(TAG, "Booooking Driver Email in Destination Select Activity: " + driverEmail);


        Log.d(TAG, "Passenger Current Latitude----> " + currentLat);
        Log.d(TAG, "Passenger Current Longitude----> " + currentLng);

        Log.d(TAG, "Passenger Class Current Longitude----> " + location.getLatitude());
        Log.d(TAG, "Passenger Class Current Longitude----> " + location.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);

        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        String provider = locationManager.getBestProvider(new Criteria(), true);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

    // TODO: Consider calling
    // ActivityCompat#requestPermissions
    // here to request the missing permissions, and then overriding
    // public void onRequestPermissionsResult(int requestCode, String[]permissions,
    // int[] grantResults)
    // to handle the case where the user grants the permission. See the documentation
    // for ActivityCompat#requestPermissions for more details.
            return;
        }


        Location locations = locationManager.getLastKnownLocation(provider);
        List<String> providerList = locationManager.getAllProviders();

        Log.d(TAG, "Locations----> "+ locations);


        if (null != locations && null != providerList && providerList.size() > 0) {
            double longitude = locations.getLongitude();
            double latitude = locations.getLatitude();
            Geocoder geocoder = new Geocoder(getApplicationContext(),
                    Locale.getDefault());
            try {
                List<Address> listAddresses = geocoder.getFromLocation(latitude,
                        longitude, 1);

                if (null != listAddresses && listAddresses.size() > 0) {
            // Here we are finding , whatever we want our marker to show when clicked
                    String state = listAddresses.get(0).getAdminArea();
                    String country = listAddresses.get(0).getCountryName();
                    String subLocality = listAddresses.get(0).getSubLocality();
                    markerOptions.title("" + latLng + "," + subLocality + "," + state
                            + "," + country);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mMap.addMarker(markerOptions);

    //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
//        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

    //this code stops location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,
                    this);
        }

        // Adding new item to the ArrayList
        MarkerPoints.add(latLng);
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }


    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
// Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
// Show an explanation to the user *asynchronously* -- don't block
// this thread waiting for the user's response! After the user
// sees the explanation, try again to request the permission.
//Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
// No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
// If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
// permission was granted. Do the
// contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    Toast.makeText(this, "permission denied",
                            Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }


    @Override
    public boolean onMarkerClick(final Marker marker) {

//        if(mCurrLocationMarker!=null){
//            mCurrLocationMarker.remove();
//        }

        // Retrieve the data from the marker.
        Integer clickCount = (Integer) marker.getTag();

        // Check if a click count was set, then display the click count.
        if (clickCount != null) {
            clickCount = clickCount + 1;
            marker.setTag(clickCount);

            Log.d(TAG, "---Toast---");

            Toast.makeText(this,
                    marker.getTitle() +
                            " has been clicked " + clickCount + " times.",
                    Toast.LENGTH_SHORT).show();
        }

        // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false;
    }



    @Override
    public void onMapLongClick(final LatLng destlatLng) {

        final String lat = String.valueOf(destlatLng.latitude);
        final String lng = String.valueOf(destlatLng.longitude);

        destinationLat = lat;
        destinationLng = lng;

        Log.d(TAG, "--------Dest Latitude: " + destinationLat);
        Log.d(TAG, "--------Dest Longitude: " + destinationLng);

        Log.d(TAG, "--------Long Clicked lat before if statement: " + destlatLng.latitude);
        Log.d(TAG, "--------Long Clicked lng before if statement: " + destlatLng.longitude);

        doubleDestinationLat = destlatLng.latitude;
        doubleDestinationLng = destlatLng.longitude;

        Log.d(TAG, "Long Clicked latlng: " + destlatLng);

        if(marker!=null){
            marker.remove();
            mMap.clear();


            Log.d(TAG, "--------After if statement, Long Clicked lat: " + destlatLng.latitude);
            Log.d(TAG, "--------After if statement, Long Clicked latlng: " + destlatLng.longitude);
            Log.d(TAG, "After if stmt, Long Clicked latlng: " + destlatLng);
        }

        marker= mMap.addMarker(new MarkerOptions().position(destlatLng).title(destlatLng.toString()));

        // Getting URL to the Google Directions API
        String url = getUrl(currentlatLng, destlatLng);
        Log.d("onMapClick", url.toString());
        FetchUrl FetchUrl = new FetchUrl();

        // Start downloading json data from Google Directions API
        FetchUrl.execute(url);


        final Button btSelectDestination = (Button) findViewById(R.id.btSelectDest);

        btSelectDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){

                destinationLat = lat;
                destinationLng = lng;

                Intent intent= new Intent(DestinationSelectionActivity.this,
                        ReviewBookingActivity.class);
                DestinationSelectionActivity.this.startActivity(intent);

                Log.d(TAG, "/////Src Lat: " + currentLat);
                Log.d(TAG, "/////Src Lng: " + currentLng);
                Log.d(TAG, "/////Dest Lat: " + destinationLat);
                Log.d(TAG, "/////Dest Lng: " + destinationLng);
                Log.d(TAG, "Driver Emaiiiiiil: " + driverEmail);

                Bundle destBundle = new Bundle();
                destBundle.putString("driverEmail", driverEmail);
                destBundle.putString("srcLat", currentLat);
                destBundle.putString("srcLng", currentLng);
                destBundle.putString("destLat", destinationLat);
                destBundle.putString("destLng", destinationLng);
                intent.putExtras(destBundle);
                startActivity(intent);

            }
        });
    }

    private String getUrl(LatLng currentlatLng, LatLng destlatLng) {
        // Origin of route
        String str_origin = "origin=" + currentlatLng.latitude + "," + currentlatLng.longitude;

        // Destination of route
        String str_dest = "destination=" + destlatLng.latitude + "," + destlatLng.longitude;


        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;


        return url;

    }


    @Override
    public void onMapClick(LatLng latLng) {
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

        Log.d(TAG, "/////--Src Lat: " + currentLat);
        Log.d(TAG, "/////--Src Lng: " + currentLng);
        Log.d(TAG, "/////--Dest Lat: " + destinationLat);
        Log.d(TAG, "/////--Dest Lng: " + destinationLng);
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            Log.d("downloadUrl", data.toString());
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask",jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask","Executing routes");
                Log.d("ParserTask", "Booking Distance and Time: "+routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask",e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;
            String distance = "";
            String duration = "";



            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    if(j==0){    // Get distance from the list
                        distance = (String)point.get("distance");
                        continue;
                    }else if(j==1){ // Get duration from the list
                        duration = (String)point.get("duration");
                        continue;
                    }


                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.BLUE);

                Log.d("onPostExecute","onPostExecute lineoptions decoded");

            }

//            tvDistanceDuration.setText("Distance:"+distance + ", Duration:"+duration);


            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null) {
                mMap.addPolyline(lineOptions);
            }
            else {
                Log.d("onPostExecute","without Polylines drawn");
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}