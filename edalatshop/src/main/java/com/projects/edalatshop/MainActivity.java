package com.projects.edalatshop;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;

import info.semsamot.actionbarrtlizer.ActionBarRtlizer;
import info.semsamot.actionbarrtlizer.RtlizeEverything;
import com.adapters.MGListAdapter;
import com.adapters.MGListAdapter.OnMGListAdapterAdapterListener;
import com.amplitude.api.Amplitude;
import com.config.Config;
import com.config.UIConfig;
import com.db.DbHelper;
import com.db.Queries;
import com.fragments.AboutUsFragment1;
import com.fragments.DiscountFragment;
import com.fragments.FavoriteFragment_news;
import com.fragments.HomeFragment;
import com.fragments.MapFragment;
import com.fragments.NewsFragment;
import com.fragments.SplashFragment;
import com.fragments.activity.LoginActivity;
import com.fragments.activity.ProfileActivity;
import com.fragments.activity.RegisterActivity;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdRequest.Builder;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.common.GooglePlayServicesUtil;

import com.onesignal.OneSignal;
import com.projects.edalatshop.R;
import com.location.LocationHelper;
import com.location.LocationHelper.OnLocationListener;
import com.location.LocationUtils;
import com.models.Menu;
import com.models.Menu.HeaderType;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.refreshlayout.SwipeRefreshActivity;
import com.usersession.UserAccessSession;
import com.usersession.UserSession;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.text.Spanned;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;

import android.graphics.Typeface;

import org.json.JSONObject;

public class MainActivity extends SwipeRefreshActivity implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener  {

    private static boolean DrawerFlag = false;
    public static boolean IsFirst = true;
	private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    // nav drawer title
    private CharSequence mDrawerTitle;
    // used to store app title
    private CharSequence mTitle;
    private Menu[] MENUS;
    public static Location location;
	public static List<Address> address;
	public static int offsetY = 0;
	
	private static SQLiteDatabase db;
	private static DbHelper dbHelper;
	private static Queries q;
	protected static ImageLoader imageLoader;
	private static boolean isShownSplash = false;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    boolean mUpdatesRequested = false;
    
    // Handle to SharedPreferences for this app
    SharedPreferences mPrefs;

    // Handle to a SharedPreferences editor
    SharedPreferences.Editor mEditor;
    private Fragment currFragment;
  
    private GetAddressTask getAddressTask;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*  if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }*/

        Amplitude.getInstance().initialize(this, "cb4b1ef78f74bfec992fe6500fd21f4c").enableForegroundTracking(getApplication());

        //OneSignal.enableNotificationsWhenActive(true);

        OneSignal.startInit(this)
                .setNotificationOpenedHandler(new ExampleNotificationOpenedHandler())
                .init();
        //OneSignal.enableNotificationsWhenActive(true);

        super.onCreate(savedInstanceState);
        setDefaultFont(this, "DEFAULT", "BHoma.ttf");
        setDefaultFont(this, "MONOSPACE", "BHoma.ttf");
        setDefaultFont(this, "SERIF", "BHoma.ttf");
        setDefaultFont(this, "SANS_SERIF", "BHoma.ttf");

        setContentView(R.layout.activity_main);
        
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        this.getActionBar().setIcon(R.drawable.header_logo);
        this.getActionBar().setTitle("");
        
        dbHelper = new DbHelper(this);
		q = new Queries(db, dbHelper);
		
		imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(getBaseContext()));

 
        mTitle = mDrawerTitle = "";
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);
 
        mDrawerList.setOnItemClickListener(new SlideMenuClickListener());
 
        updateMenuList();

        IsFirst = true;
        
        // enabling action bar app icon and behaving it as toggle button
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
 
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_menu, //nav menu toggle icon
                R.string.no_name, // nav drawer open - description for accessibility
                R.string.no_name // nav drawer close - description for accessibility
        ) {
            public void onDrawerClosed(View view) {
/*                getActionBar().setTitle(mTitle);
                // calling onPrepareOptionsMenu() to show action bar icons
                invalidateOptionsMenu();*/
                super.onDrawerClosed(view);
                DrawerFlag = false;
                getActionBar().setTitle(mTitle);
            }

            public void onDrawerOpened(View drawerView) {
                /*getActionBar().setTitle(mDrawerTitle);
                // calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu();*/
                super.onDrawerOpened(drawerView);
                DrawerFlag = true;
                getActionBar().setTitle(mDrawerTitle);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
 
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            offsetY = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }
        
        
        if(!isShownSplash) {
        	isShownSplash = true;
        	this.getActionBar().hide();
        	FragmentManager fragmentManager = this.getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.frame_container, new SplashFragment()).commit();
        }
        
        else if (savedInstanceState == null) {
            // on first time display view for first nav item
            displayView(0);
        }
        
        mUpdatesRequested = false;
        
        // Open Shared Preferences
        mPrefs = getSharedPreferences(LocationUtils.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        // Get an editor
        mEditor = mPrefs.edit();
        
        mGoogleApiClient = new GoogleApiClient.Builder(this)
        .addApi(LocationServices.API)
        .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
        .build();
        
        FrameLayout frameAds = (FrameLayout) findViewById(R.id.frameAds);
		frameAds.setVisibility(View.GONE);
    }


    //check user for accepeting quit application
    @Override
    public void onBackPressed() {
        if(currFragment instanceof HomeFragment && DrawerFlag == false){
            doExit();
        }
        else{
            /*if(DrawerFlag == true){
                mDrawerLayout.closeDrawer(mDrawerList);
            }
            else{
                mDrawerLayout.openDrawer(mDrawerList);
            }*/
            showMainView();
        }

    }
    void doExit()
    {
        /*AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setPositiveButton("بله",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        alertDialog.setNegativeButton("خیر",null);
        alertDialog.setMessage("آیا میخواهید خارج شوید؟");
        alertDialog.setTitle(R.string.app_name);
        alertDialog.show();*/
        finish();
    }

    public void showMainView() {
    	getActionBar().show();
    	displayView(0);
    	//showAds();
    }
 
    
    private class SlideMenuClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            // display view for selected nav drawer item
            displayView(position);
        }
    }
 

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // toggle nav drawer on selecting action bar app icon/title
        if(item != null && item.getItemId() == android.R.id.home){
            if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                mDrawerLayout.closeDrawer(Gravity.RIGHT);

            } else {
                mDrawerLayout.openDrawer(Gravity.RIGHT);
            }

             updateMenuList();
            return true;
        }
        return false;


/*        // Handle action bar actions click
        switch (item.getItemId()) {
//	        case R.id.action_settings:
//	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
        }*/
    }
 
    
    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        ActionBarRtlizer rtlizer = new ActionBarRtlizer(this);
        ViewGroup homeView = (ViewGroup) rtlizer.getHomeView();

        RtlizeEverything.rtlize(rtlizer.getActionBarView());

        if (rtlizer.getHomeViewContainer() instanceof ViewGroup) {
            RtlizeEverything.rtlize((ViewGroup) rtlizer.getHomeViewContainer());
        }

        RtlizeEverything.rtlize(homeView);
        //rtlizer.flipActionBarUpIconIfAvailable(homeView);
        ImageView upIcon = (ImageView) homeView.getChildAt(0);
        upIcon.setRotationY(180);
        return true;
    }
    
    
    @Override
    public boolean onPrepareOptionsMenu(android.view.Menu menu) {
        // if nav drawer is opened, hide the action items
//        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
//        menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }
 
    
    private void displayView(int position) {
        
    	// clear back stack
    	FragmentManager fm = this.getSupportFragmentManager();
	    for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {    
	        fm.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE); 
	    }
	    
	    // update the main content by replacing fragments
        Fragment fragment = null;
        switch (position) {
	        
	        case 0:
	            fragment = new HomeFragment();
	            break;
	        case 1:
                fragment = new NewsFragment();;
                break;
            case 2:
                fragment = new DiscountFragment();
                break;
            case 3:
                fragment = new AboutUsFragment1();
                break;
            /*case 4:
                fragment = new AboutUsFragment2();
                break;
            case 5:
                fragment = new AboutUsFragment3();
                break;*/
	        case 4:
	            fragment = new FavoriteFragment_news();
	            break;
	        /*case 4:
	            fragment = new FeaturedFragment();
	            break;*/
	       /* case 5:
	            fragment = new MapFragment();
	            break;*/
	        /*case 6:
	            fragment = new SearchFragment();
	            break;*/
	        /*case 4:
	            fragment = new NewsFragment();
	            break;*/
	        /*case 7:
	            fragment = new WeatherFragment();
	            break;*/
	            
	        /*case 6:
	            fragment = new AboutUsFragment();
	            break;*/
	        /*case 9:
	            fragment = new TermsConditionFragment();
	            break;*/

            case 5:
                doExit();
                break;
	            
	        case 9:
	        	
				UserAccessSession session = UserAccessSession.getInstance(this);
				if(session.getUserSession() == null) {
					Intent i = new Intent(MainActivity.this, RegisterActivity.class);
					startActivity(i);
				}
				else { 
					Intent i = new Intent(this, ProfileActivity.class);
					startActivity(i);
				}
				
				break;
				
			case 10:
				Intent i = new Intent(this, LoginActivity.class);
				this.startActivity(i);
				break;
 
	        default:
	            break;
        }
        
     // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        mDrawerList.setSelection(position);
//        setTitle(navMenuTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
        
       if(currFragment != null && fragment != null) {
    	   boolean result = fragment.getClass().equals( currFragment.getClass());
           if(result)
        	   return;
       }
        
        if (fragment != null) {
        	
        	if(fragment instanceof MapFragment) {
        		currFragment = fragment;
        		Handler h = new Handler();
        		h.postDelayed(new Runnable() {
        			
        			@Override
        			public void run() {
        				// TODO Auto-generated method stub
        				
                        FragmentManager fragmentManager = MainActivity.this.getSupportFragmentManager();
                        fragmentManager.beginTransaction()
                                .replace(R.id.frame_container, currFragment).commit();
        			}
        		}, Config.DELAY_SHOW_ANIMATION + 200);
        	}
        	else {
        		
        		currFragment = fragment;
                FragmentManager fragmentManager = this.getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_container, fragment).commit();
        	}
 
        }
    }
 
    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }
 
    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
       mDrawerToggle.syncState();
    }
 
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);


    }
	
    public void updateMenuList() {
		
		UserAccessSession accessSession = UserAccessSession.getInstance(this);
		UserSession userSession = accessSession.getUserSession();
				
		if(userSession == null) {
			MENUS = UIConfig.MENUS_NOT_LOGGED;
		}
		else {
			MENUS = UIConfig.MENUS_LOGGED;
		}
			
		showList();
	}
    
    public void showList() {
    	
    	MGListAdapter adapter = new MGListAdapter(
				this, MENUS.length, R.layout.menu_entry);
		
		adapter.setOnMGListAdapterAdapterListener(new OnMGListAdapterAdapterListener() {
			
			@Override
			public void OnMGListAdapterAdapterCreated(MGListAdapter adapter, View v,
					int position, ViewGroup viewGroup) {
				// TODO Auto-generated method stub
				
				FrameLayout frameCategory = (FrameLayout) v.findViewById(R.id.frameCategory);
				FrameLayout frameHeader = (FrameLayout) v.findViewById(R.id.frameHeader);
				
				frameCategory.setVisibility(View.GONE);
				frameHeader.setVisibility(View.GONE);
				
				Menu menu = MENUS[position];
				
				if(menu.getHeaderType() == HeaderType.HeaderType_CATEGORY) {
					frameCategory.setVisibility(View.VISIBLE);
					Spanned title = Html.fromHtml(MainActivity.this.getResources().getString(menu.getMenuResTitle()));
					TextView tvTitle = (TextView) v.findViewById(R.id.tvTitle);
					tvTitle.setText(title);
					
					ImageView imgViewIcon = (ImageView) v.findViewById(R.id.imgViewIcon);
					imgViewIcon.setImageResource(menu.getMenuResIconSelected());
				}
				else {
					frameHeader.setVisibility(View.VISIBLE);
					
					Spanned title = Html.fromHtml(MainActivity.this.getResources().getString(menu.getMenuResTitle()));
					TextView tvTitleHeader = (TextView) v.findViewById(R.id.tvTitleHeader);
					tvTitleHeader.setText(title);
				}
			}
		});
		mDrawerList.setAdapter(adapter);
		adapter.notifyDataSetChanged();
    }
    
    
    
    // ====================================================================================
    // ====================================================================================
    // ====================================================================================
    OnLocatonListener mCallbackLocation;
	
	public interface OnLocatonListener {
		
        public void onLocationChanged(Location prevLoc, Location currentLoc);
    }
	
	public void setOnLocatonListener(OnLocatonListener listener) {
		
		try {
			mCallbackLocation = (OnLocatonListener) listener;
        } catch (ClassCastException e)  {
            throw new ClassCastException(this.toString() + " must implement OnLocatonListener");
        }
	}
	
	
	public void getLocation1() {
 		
 		LocationHelper helper = new LocationHelper();
		helper.setOnLocationListener(new OnLocationListener() {
			
			@Override
			public void onLocationUpdated(LocationHelper helper, Location loc, int tag) {
				// TODO Auto-generated method stub
				if(mCallbackLocation != null)
					mCallbackLocation.onLocationChanged(location, loc);
				
				location = loc;
				Log.e("LOCATION FOUND", "LAT = " + loc.getLatitude() +", LON = " + loc.getLongitude());
				
				try {
					address = LocationHelper.getAddress(getApplicationContext(), location);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		helper.getLocation(this);
 	}
	
	public Queries getQueries() {	
		return q;
	}
	
	public static ImageLoader getImageLoader() {
		return imageLoader;
	}

	OnActivityResultListener mCallbackActivityResult;
	private AdView adView;

	
	@Override
    public void onStart()  {
        super.onStart();
        
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        
        // After disconnect() is called, the client is considered "dead".
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(mCallbackActivityResult != null) {
        	mCallbackActivityResult.onActivityResultCallback(this, requestCode, resultCode, data);
        }
    }


     public void getDebugKey() {
 		try {
 	        PackageInfo info = getPackageManager().getPackageInfo(
 	        		getApplicationContext().getPackageName(), 
 	                PackageManager.GET_SIGNATURES);
 	        for (Signature signature : info.signatures) {
 	            MessageDigest md = MessageDigest.getInstance("SHA");
 	            md.update(signature.toByteArray());
 	           Log.e("KeyHash:", "------------------------------------------");
 	            Log.e("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
 	            Log.e("KeyHash:", "------------------------------------------");
 	            }
 	        
 	    } catch (NameNotFoundException e) {
 	    	e.printStackTrace();
 	    } catch (NoSuchAlgorithmException e) {
 	    	e.printStackTrace();
 	    }
 	}
 	
 	
 	public interface OnActivityResultListener {
         public void onActivityResultCallback(
         		Activity activity, int requestCode, int resultCode, Intent data);
     }

 	public void setOnActivityResultListener(OnActivityResultListener listener) {
 		try {
 			mCallbackActivityResult = (OnActivityResultListener) listener;
         } catch (ClassCastException e)  {
             throw new ClassCastException(this.toString() + " must implement OnActivityResultListener");
         }
 	}
 	
 	
 	
 	private boolean servicesConnected() {

        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d(LocationUtils.APPTAG, "Google Play Service available.");

            // Continue
            return true;
        // Google Play services was not available for some reason
        } else {
            // Display an error dialog
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            dialog.show();
            
            return false;
        }
    }
 	
 	
 	@SuppressLint("NewApi")
    public void getAddress(View v) {

        // In Gingerbread and later, use Geocoder.isPresent() to see if a geocoder is available.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && !Geocoder.isPresent()) {
            // No geocoder is present. Issue an error message
            Toast.makeText(this, "No Geocoder available", Toast.LENGTH_LONG).show();
            return;
        }

        if (servicesConnected()) {
            // Get the current location
//            Location currentLocation = mLocationClient.getLastLocation();
            // Turn the indefinite activity indicator on
//            mActivityIndicator.setVisibility(View.VISIBLE);
            
            // Start the background task
//            (new MainActivity3.GetAddressTask(this)).execute(currentLocation);
        }
    }
 	

    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {

                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

                /*
                * Thrown if Google Play services canceled the original
                * PendingIntent
                */

            } catch (IntentSender.SendIntentException e) {

                // Log the error
                e.printStackTrace();
            }
        } else {

            // If no resolution is available, display a dialog to the user with the error.
//            showErrorDialog(connectionResult.getErrorCode());
        }
    }

    /**
     * Report location updates to the UI.
     *
     * @param location The updated location.
     */
    @Override
    public void onLocationChanged(Location loc) {

        // Report to the UI that the location was updated
        
        Log.e("Location LOG", "Location Updated");

        // In the UI, set the latitude and longitude to the value received
//        mLatLng.setText(LocationUtils.getLatLng(this, location));
        
        if(mCallbackLocation != null)
			mCallbackLocation.onLocationChanged(location, loc);
        
        location = loc;
        
        if(address == null) {
        	getAddressTask = new MainActivity.GetAddressTask(this);
        	getAddressTask.execute(location);
        }
    }

    
    @Override
    public void onResume() {
        super.onResume();

        // If the app already has a setting for getting location updates, get it
        if (mPrefs.contains(LocationUtils.KEY_UPDATES_REQUESTED)) {
            mUpdatesRequested = mPrefs.getBoolean(LocationUtils.KEY_UPDATES_REQUESTED, false);
            

        // Otherwise, turn off location updates until requested
        } else {
            mEditor.putBoolean(LocationUtils.KEY_UPDATES_REQUESTED, false);
            mEditor.commit();
        }

    }

    @Override
    public void onPause() {

        // Save the current setting for updates
        mEditor.putBoolean(LocationUtils.KEY_UPDATES_REQUESTED, mUpdatesRequested);
        mEditor.commit();


        super.onPause();
    }

    public void showAds() {
		
		FrameLayout frameAds = (FrameLayout) findViewById(R.id.frameAds);
        if(Config.WILL_SHOW_ADS) {
        	
        	frameAds.setVisibility(View.VISIBLE);
        	
        	// Create an ad.
            if(adView == null) {
            	adView = new AdView(this);
                adView.setAdSize(AdSize.SMART_BANNER);
                adView.setAdUnitId(Config.BANNER_UNIT_ID);
                
                // Add the AdView to the view hierarchy. The view will have no size
                // until the ad is loaded.            
                frameAds.addView(adView);

                // Create an ad request. Check logcat output for the hashed device ID to
                // get test ads on a physical device.
                Builder builder = new AdRequest.Builder();
                
                if(Config.TEST_ADS_USING_EMULATOR)
                	builder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
                    
                if(Config.TEST_ADS_USING_TESTING_DEVICE)
                	builder.addTestDevice(Config.TESTING_DEVICE_HASH);
                    
                AdRequest adRequest = builder.build();
                // Start loading the ad in the background.
                adView.loadAd(adRequest);
            }
        }
        else {
        	frameAds.setVisibility(View.GONE);
        }
	}
    
    
    protected class GetAddressTask extends AsyncTask<Location, Void, String> {

        // Store the context passed to the AsyncTask when the system instantiates it.
        Context localContext;

        // Constructor called by the system to instantiate the task
        public GetAddressTask(Context context) {

            // Required by the semantics of AsyncTask
            super();

            // Set a Context for the background task
            localContext = context;
        }

        /**
         * Get a geocoding service instance, pass latitude and longitude to it, format the returned
         * address, and return the address to the UI thread.
         */
        @Override
        protected String doInBackground(Location... params) {
            /*
             * Get a new geocoding service instance, set for localized addresses. This example uses
             * android.location.Geocoder, but other geocoders that conform to address standards
             * can also be used.
             */
            Geocoder geocoder = new Geocoder(localContext, Locale.getDefault());

            // Get the current location from the input parameter list
            Location location = params[0];

            // Create a list to contain the result address
            List <Address> addresses = null;

            // Try to get an address for the current location. Catch IO or network problems.
            try {

                /*
                 * Call the synchronous getFromLocation() method with the latitude and
                 * longitude of the current location. Return at most 1 address.
                 */
                addresses = geocoder.getFromLocation(location.getLatitude(),
                    location.getLongitude(), 1
                );

                // Catch network or other I/O problems.
                } catch (IOException exception1) {
                    // print the stack trace
                    exception1.printStackTrace();
                    
                // Catch incorrect latitude or longitude values
                } catch (IllegalArgumentException exception2) {
                    exception2.printStackTrace();
                }
            
                // If the reverse geocode returned an address
                if (addresses != null && addresses.size() > 0) {

                    // Get the first address
                    address = addresses;
                    
                    Address address = MainActivity.address.get(0);
        			
        			String locality = address.getLocality();
        			String countryName = address.getCountryName();
        			
        			String addressStr = String.format("%s, %s", locality, countryName);
        			Log.e("Location LOG", addressStr);


                // If there aren't any addresses, post a message
                }
                
                return null;
        }

        /**
         * A method that's called once doInBackground() completes. Set the text of the
         * UI element that displays the address. This method runs on the UI thread.
         */
        @Override
        protected void onPostExecute(String address) {

        }
    }


	@Override
	public void onConnectionSuspended(int cause) {
		// TODO Auto-generated method stub
		Log.i("GoogleApiClient", "GoogleApiClient connection has been suspend");
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		
		// TODO Auto-generated method stub
		mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(100000); // Update location every second elyas!!!! ziadesh kardam

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
	}


    public void setDefaultFont(Context context, String staticTypefaceFieldName, String fontAssetName) {
        final Typeface regular = Typeface.createFromAsset(context.getAssets(),
                fontAssetName);
        replaceFont(staticTypefaceFieldName, regular);
    }

    protected void replaceFont(String staticTypefaceFieldName, final Typeface newTypeface) {
        try {
            final Field staticField = Typeface.class
                    .getDeclaredField(staticTypefaceFieldName);
            staticField.setAccessible(true);
            staticField.set(null, newTypeface);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    private class ExampleNotificationOpenedHandler implements OneSignal.NotificationOpenedHandler {
        @Override
        public void notificationOpened(String message, JSONObject additionalData, boolean isActive) {
            //String messageTitle = "پیام جدید", messageBody = message;
            //displayView(0);
           /* try {
                if (additionalData != null) {
                    //if (additionalData.getString("type").equals("news"))
                        //displayView(1);

                    //else if (additionalData.getString("type").equals("discount"))
                       //displayView(2);

                }
            } catch (JSONException e) {
            }*/

            /*new AlertDialog.Builder(MainActivity.this)
                    .setTitle("salam")
                    .setMessage("salam")
                    .setCancelable(true)
                    .setPositiveButton("OK", null)
                    .create().show();*/
        }
    }
}
