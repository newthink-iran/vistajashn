package com.fragments.activity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import info.semsamot.actionbarrtlizer.ActionBarRtlizer;
import info.semsamot.actionbarrtlizer.RtlizeEverything;

import com.asynctask.MGAsyncTask;
import com.asynctask.MGAsyncTask.OnMGAsyncTaskListener;
import com.config.Config;
import com.config.UIConfig;
import com.dataparser.DataParser;
import com.db.DbHelper;
import com.db.Queries;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.SnapshotReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.models.Favorite;
import com.models.Photo;
import com.models.Rating;
import com.models.ResponseRating;
import com.models.ResponseStore;
import com.models.Store;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.projects.arzansara.MainActivity;
import com.projects.arzansara.R;
import com.refreshlayout.SwipeRefreshActivity;;
import com.usersession.UserAccessSession;
import com.usersession.UserSession;
import com.utilities.MGUtilities;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.ToggleButton;

public class DetailActivity extends SwipeRefreshActivity implements OnClickListener {

	
	private DisplayImageOptions options;
	private Store store;
	private ArrayList<Photo> photoList;
	private ResponseStore responseStore;
	private ResponseRating responseRating;
	boolean canRate = true;
	private SupportMapFragment mapFragment;
	private GoogleMap googleMap;
	private Queries q;
	private SQLiteDatabase db;
	private boolean isPending = false;
	private Bundle savedInstanceState;
	private boolean isUserCanRate = false;
	MGAsyncTask task;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
/*        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }*/
		super.onCreate(savedInstanceState);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
		setContentView(R.layout.fragment_detail);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        this.getActionBar().setIcon(R.drawable.header_logo);
        this.getActionBar().setTitle("");
		
		DbHelper dbHelper = new DbHelper(this);
		q = new Queries(db, dbHelper);
		
		store = (Store) this.getIntent().getSerializableExtra("store");
		
		options = new DisplayImageOptions.Builder()
			.showImageOnLoading(UIConfig.SLIDER_PLACEHOLDER)
			.showImageForEmptyUri(UIConfig.SLIDER_PLACEHOLDER)
			.showImageOnFail(UIConfig.SLIDER_PLACEHOLDER)
			.cacheInMemory(true)
			.cacheOnDisk(true)
			.considerExifParams(true)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.build();

		
		this.savedInstanceState = savedInstanceState;
        
        
        showSwipeProgress();
        updateStore();
		
		UserAccessSession userAccess = UserAccessSession.getInstance(this);
		UserSession userSession = userAccess.getUserSession();
		if(userSession != null) {
			checkUserCanRate();
			isUserCanRate = true;
		}
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(task != null)
			task.cancel(true);
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // toggle nav drawer on selecting action bar app icon/title
        
        // Handle action bar actions click
        switch (item.getItemId()) {
	        /*case R.id.menuReview:
	        	Intent i = new Intent(this, ReviewActivity.class);
				i.putExtra("store", store);
				startActivity(i);
	            return true;*/
	            
	        default:
	        	finish();	
	            return super.onOptionsItemSelected(item);
        }
    }
 
    
    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        ActionBarRtlizer rtlizer = new ActionBarRtlizer(this);
        ViewGroup homeView = (ViewGroup) rtlizer.getHomeView();

        RtlizeEverything.rtlize(rtlizer.getActionBarView());

        if (rtlizer.getHomeViewContainer() instanceof ViewGroup) {
            RtlizeEverything.rtlize((ViewGroup) rtlizer.getHomeViewContainer());
        }

        RtlizeEverything.rtlize(homeView);
        ImageView upIcon = (ImageView) homeView.getChildAt(0);
        upIcon.setRotationY(180);
//        rtlizer.flipActionBarUpIconIfAvailable(homeView);
        return true;
    }
    
    
    @Override
    public boolean onPrepareOptionsMenu(android.view.Menu menu) {
        // if nav drawer is opened, hide the action items
        
        return super.onPrepareOptionsMenu(menu);
    }
	
	
	@SuppressLint("InflateParams") 
	public void showRatingDialog() {
		
		if(!MGUtilities.hasConnection(this)) {
			MGUtilities.showAlertView(
					this, 
					R.string.network_error, 
					R.string.no_network_connection);
			return;
		}
		
		UserAccessSession userAccess = UserAccessSession.getInstance(this);
		UserSession userSession = userAccess.getUserSession();
		
		if(userSession == null) {
			MGUtilities.showAlertView(
					this, 
					R.string.login_error, 
					R.string.login_error_rating);
			return;
		}
		
		if(!canRate) {
			MGUtilities.showAlertView(
					this, 
					R.string.rating_error, 
					R.string.rating_error_finish);
			return;
		}
		
		if(responseRating != null && responseRating.getStore_rating() != null) {
			
			Rating rating = responseRating.getStore_rating();
			if(rating.getCan_rate() == -1) {
				MGUtilities.showAlertView(
						this, 
						R.string.rating_error, 
						R.string.rating_error_finish);
				return;
			}
		}
		else {
			MGUtilities.showAlertView(
					this, 
					R.string.rating_error, 
					R.string.rating_error_something_wrong);
			
			return;
		}
		
		
		LayoutInflater li = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View v = li.inflate(R.layout.rating_dialog, null);
		
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
	    alert.setTitle(this.getResources().getString(R.string.rate_store));
	    alert.setView(v);
	    alert.setPositiveButton(this.getResources().getString(R.string.rate), 
	    		new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				rateStore(v);
			}
	    });

	    alert.setNegativeButton(this.getResources().getString(R.string.cancel), 
	    		new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
		});
	    alert.create();
	    alert.show();
	}
	
	public void rateStore(final View v) {
		
		if(!MGUtilities.hasConnection(this)) {
			MGUtilities.showAlertView(
					this, 
					R.string.network_error, 
					R.string.no_network_connection);
			return;
		}
		
        task = new MGAsyncTask(this);
        task.setMGAsyncTaskListener(new OnMGAsyncTaskListener() {
			
        	
			@Override
			public void onAsyncTaskProgressUpdate(MGAsyncTask asyncTask) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAsyncTaskPreExecute(MGAsyncTask asyncTask) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAsyncTaskPostExecute(MGAsyncTask asyncTask) {
				// TODO Auto-generated method stub
				updateStore();
			}
			
			@Override
			public void onAsyncTaskDoInBackground(MGAsyncTask asyncTask) {
				// TODO Auto-generated method stub
				
				syncRating(v);
			}
		});
        task.execute();
        
		
	}
	
	public void syncRating(View v) {
		RatingBar ratingBar = (RatingBar)v.findViewById(R.id.ratingBar);
		int rating = (int) ratingBar.getRating();
		
		UserAccessSession userAccess = UserAccessSession.getInstance(this);
		UserSession userSession = userAccess.getUserSession();
		
		
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("rating", String.valueOf(rating) ));
		params.add(new BasicNameValuePair("store_id", String.valueOf(store.getStore_id()) ));
		params.add(new BasicNameValuePair("user_id", String.valueOf(userSession.getUser_id()) ));
		params.add(new BasicNameValuePair("login_hash", userSession.getLogin_hash() ));

        responseStore = DataParser.getJSONFromUrlStore(Config.POST_RATING_URL, params);
        
        if(responseStore != null && responseStore.getStore() != null) {
        	
        	q.updateStore(responseStore.getStore());
        	store = responseStore.getStore();
        	canRate = false;
        }
	}
	
	public void checkUserCanRate() {
		
		if(!MGUtilities.hasConnection(this)) {
			hideSwipeProgress();
			return;
		}
		
        task = new MGAsyncTask(this);
        task.setMGAsyncTaskListener(new OnMGAsyncTaskListener() {
			
        	
			@Override
			public void onAsyncTaskProgressUpdate(MGAsyncTask asyncTask) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAsyncTaskPreExecute(MGAsyncTask asyncTask) {
				// TODO Auto-generated method stub
				asyncTask.dialog.hide();
			}
			
			@Override
			public void onAsyncTaskPostExecute(MGAsyncTask asyncTask) {
				// TODO Auto-generated method stub
				hideSwipeProgress();
			}
			
			@Override
			public void onAsyncTaskDoInBackground(MGAsyncTask asyncTask) {
				// TODO Auto-generated method stub
				
				UserAccessSession userAccess = UserAccessSession.getInstance(DetailActivity.this);
				UserSession userSession = userAccess.getUserSession();
				
				ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("store_id", String.valueOf(store.getStore_id()) ));
				params.add(new BasicNameValuePair("user_id", String.valueOf(userSession.getUser_id()) ));
				params.add(new BasicNameValuePair("login_hash", userSession.getLogin_hash() ));
				
				responseRating = DataParser.getJSONFromUrlRating(Config.GET_RATING_USER_URL, params);
			}
		});
        task.execute();
	}
	
	
//	private void setMap() {
//
//		mapFragment = new SupportMapFragment();
//		FragmentTransaction fragmentTransaction = this.getSupportFragmentManager().beginTransaction();
//        fragmentTransaction.add(R.id.googleMapContainer, mapFragment);
//        fragmentTransaction.commit();
//
//        Handler h = new Handler();
//        h.postDelayed(new Runnable() {
//
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				delayMapCall();
//			}
//		}, 300);
//	}
	
//	private void delayMapCall() {
//
//		if(!isUserCanRate)
//			hideSwipeProgress();
//
//		try {
//
//			googleMap = mapFragment.getMap();
//			googleMap.getUiSettings().setAllGesturesEnabled(false);
//			googleMap.getUiSettings().setMyLocationButtonEnabled(false);
//			googleMap.getUiSettings().setScrollGesturesEnabled(false);
//			googleMap.getUiSettings().setCompassEnabled(false);
//			googleMap.getUiSettings().setZoomControlsEnabled(false);
//
//			MarkerOptions markerOptions = new MarkerOptions();
//
//			markerOptions.title( Html.fromHtml(store.getStore_name()).toString() );
//
//			Spanned storeAddress = Html.fromHtml(store.getStore_address());
//			String address = storeAddress.toString();
//
//			if(storeAddress.length() > 50)
//				address = storeAddress.toString().substring(0,  50) + "...";
//
//			markerOptions.snippet(address);
//
//			markerOptions.position(new LatLng(store.getLat(), store.getLon()));
//			markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin_orange));
//
//			Marker mark = googleMap.addMarker(markerOptions);
//			mark.setInfoWindowAnchor(0.25f, 0);
//			mark.showInfoWindow();
//
//			CameraUpdate zoom = CameraUpdateFactory.zoomTo(Config.MAP_ZOOM_LEVEL);
//	    	googleMap.moveCamera(zoom);
//
//			CameraUpdate center = CameraUpdateFactory.newLatLng(
//					new LatLng(store.getLat() + 0.0035, store.getLon()));
//
//			googleMap.moveCamera(center);
//
//
//			Handler h = new Handler();
//	        h.postDelayed(new Runnable() {
//
//				@Override
//				public void run() {
//					// TODO Auto-generated method stub
//
//					final ImageView imgViewMap = (ImageView) findViewById(R.id.imgViewMap);
//					googleMap.snapshot(new SnapshotReadyCallback() {
//
//						@Override
//						public void onSnapshotReady(Bitmap snapshot) {
//							// TODO Auto-generated method stub
//							imgViewMap.setImageBitmap(snapshot);
//
//							FragmentTransaction fragmentTransaction = DetailActivity.this.getSupportFragmentManager().beginTransaction();
//							fragmentTransaction.remove(mapFragment);
//							fragmentTransaction.commit();
//							hideSwipeProgress();
//						}
//					});
//
//				}
//			}, 1500);
//
//
//
//		}
//		catch(Exception e) {
//			e.printStackTrace();
//		}
//	}
	

	@SuppressLint("DefaultLocale") 
	private void updateStore() {
		
		Photo p = q.getPhotoByStoreId(store.getStore_id());
		photoList = q.getPhotosByStoreId(store.getStore_id());
		
		ImageView imgViewPhoto = (ImageView) findViewById(R.id.imgViewPhoto);
		
		if(p != null)
			MainActivity.getImageLoader().displayImage(p.getPhoto_url(), imgViewPhoto, options);
		
		TextView tvTitle = (TextView) findViewById(R.id.tvTitle);
		TextView tvSubtitle = (TextView) findViewById(R.id.tvSubtitle);
		//RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBar);
		//TextView tvRatingBarInfo = (TextView) findViewById(R.id.tvRatingBarInfo);
		
		//ImageView imgViewGallery = (ImageView) findViewById(R.id.imgViewGallery);
		//imgViewGallery.setOnClickListener(this);
		
		//TextView tvGalleryCount = (TextView) findViewById(R.id.tvGalleryCount);
		//TextView tvGallerydesc = (TextView) findViewById(R.id.tvGallerydesc);
        //tvGallerydesc.setOnClickListener(this);
		
		//Button btnRateIt = (Button) findViewById(R.id.btnRateIt);
		//btnRateIt.setOnClickListener(this);
		
		
		
		TextView tvDetails = (TextView) findViewById(R.id.tvDetails);
		
		ImageView imgViewCall = (ImageView) findViewById(R.id.imgViewCall);
		imgViewCall.setOnClickListener(this);
		
		ImageView imgViewEmail = (ImageView) findViewById(R.id.imgViewEmail);
		imgViewEmail.setOnClickListener(this);
		
		ImageView imgViewRoute = (ImageView) findViewById(R.id.imgViewRoute);
		imgViewRoute.setOnClickListener(this);
		
/*		Button imgViewShareFb = (Button) findViewById(R.id.imgViewShareFb);
		imgViewShareFb.setOnClickListener(this);
		
		Button imgViewShareTwitter = (Button) findViewById(R.id.imgViewShareTwitter);
		imgViewShareTwitter.setOnClickListener(this);*/
		
		ImageView imgViewSMS = (ImageView) findViewById(R.id.imgViewSMS);
		imgViewSMS.setOnClickListener(this);
		
		ImageView imgViewWebsite = (ImageView) findViewById(R.id.imgViewWebsite);
		imgViewWebsite.setOnClickListener(this);
		
		ToggleButton toggleButtonFave = (ToggleButton) findViewById(R.id.toggleButtonFave);
		toggleButtonFave.setOnClickListener(this);
		
		
		imgViewCall.setEnabled(true);
		imgViewRoute.setEnabled(true);
		imgViewEmail.setEnabled(true);
		imgViewSMS.setEnabled(true);
		imgViewWebsite.setEnabled(true);
		
		if( store.getPhone_no() == null || store.getPhone_no().trim().length() == 0 )
			imgViewCall.setEnabled(false);
		
		if(store.getLat() == 0 || store.getLon() == 0)
			imgViewRoute.setEnabled(false);
		
		if(store.getEmail() == null || store.getEmail().trim().length() == 0)
			imgViewEmail.setEnabled(false);
		
		if( store.getSms_no() == null || store.getSms_no().trim().length() == 0 )
			imgViewSMS.setEnabled(false);
		
		if(store.getWebsite() == null || store.getWebsite().trim().length() == 0)
			imgViewWebsite.setEnabled(false);
		

		Favorite fave = q.getFavoriteByStoreId(store.getStore_id());
		toggleButtonFave.setChecked(true);
		
		if(fave == null)
			toggleButtonFave.setChecked(false);
		
		// SETTING VALUES
		float rating = 0;
		
		if(store.getRating_total() > 0 && store.getRating_count() > 0)
			rating = store.getRating_total() / store.getRating_count();
		
		String strRating = String.format("%.2f %s %s %d %s", 
				rating, 
				this.getResources().getString(R.string.rating),
				this.getResources().getString(R.string.average_based_on),
				store.getRating_count(),
				this.getResources().getString(R.string.average_based_on2)
				);
				
		tvTitle.setText(Html.fromHtml(store.getStore_name()));
		tvSubtitle.setText(Html.fromHtml(store.getStore_address()));
		//ratingBar.setRating(rating);
		//tvRatingBarInfo.setText(strRating);

		/* elyas */
		String strDesc = store.getStore_desc();//.replace("\n", "[{~}]");
		strDesc = strDesc.replace("&amp;lt;", "<");
		strDesc = strDesc.replace("&amp;gt;", ">");
		//Spanned details = Html.fromHtml(strDesc);
		//details = Html.fromHtml(details.toString());
		//strDesc = details.toString().replace("[{~}]", "\n");

		tvDetails.setText(Html.fromHtml(strDesc));
		
		//tvDetails.setText(strDesc);
		//tvGalleryCount.setText("" + photoList.size());
		
		ImageView imgViewFeatured = (ImageView) findViewById(R.id.imgViewFeatured);
		imgViewFeatured.setVisibility(View.INVISIBLE);
		
		if(store.getFeatured() ==1)
			imgViewFeatured.setVisibility(View.VISIBLE);
		
		
        Handler h = new Handler();
//		h.postDelayed(new Runnable() {
//
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				setMap();
//			}
//		}, Config.DELAY_SHOW_ANIMATION + 300);
	}
	
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()) {
			/*case R.id.btnRateIt:
				showRatingDialog();
			break;*/
            /*case R.id.tvGallerydesc:
                if(photoList != null && photoList.size() > 0) {

//					Bundle b = new Bundle();
//					b.putSerializable("photoList", photoList);
//
//					ImageViewerFragment fragment = new ImageViewerFragment();
//					fragment.setArguments(b);

//					MainActivity mainActivity = (MainActivity)this;
//					mainActivity.switchContent(fragment, true);

                    Intent i = new Intent(this, ImageViewerActivity.class);
                    i.putExtra("photoList", photoList);
                    startActivity(i);
                }
                else {
                    MGUtilities.showAlertView(
                            this,
                            R.string.action_error,
                            R.string.no_image_to_display);
                }
                break;*/
			/*case R.id.imgViewGallery:
				
				if(photoList != null && photoList.size() > 0) {
					
//					Bundle b = new Bundle();
//					b.putSerializable("photoList", photoList);
//					
//					ImageViewerFragment fragment = new ImageViewerFragment();
//					fragment.setArguments(b);
					
//					MainActivity mainActivity = (MainActivity)this;
//					mainActivity.switchContent(fragment, true);
					
					Intent i = new Intent(this, ImageViewerActivity.class);
					i.putExtra("photoList", photoList);
					startActivity(i);
				}
				else {
					MGUtilities.showAlertView(
							this, 
							R.string.action_error, 
							R.string.no_image_to_display);
				}
				break;*/
				
				
			case R.id.imgViewCall:
				call();
				break;
				
			case R.id.imgViewEmail:
				email();
				break;
				
			case R.id.imgViewRoute:
				route();
				break;
				
/*			case R.id.imgViewShareFb:

				if(isPending)
					return;
				
				shareFB();
		        
				break;
				
			case R.id.imgViewShareTwitter:
				
				if(isPending)
					return;
				
				loginToTwitter();
				
				break;*/
				
			case R.id.imgViewSMS:
				sms();
				break;
				
			case R.id.imgViewWebsite:
				website();
				break;
				
			case R.id.toggleButtonFave:
				checkFave(v);
				break;
		}
	}
	
	private void checkFave(View view) {
		
		
		Favorite fave = q.getFavoriteByStoreId(store.getStore_id());
		
		if(fave != null) {
			q.deleteFavorite(store.getStore_id());
			((ToggleButton) view).setChecked(false);
		}
		else {
			
			fave = new Favorite();
			fave.setStore_id(store.getStore_id());
			q.insertFavorite(fave);
			((ToggleButton) view).setChecked(true);
		}
	}
	
	
	private void call() {
		
		if( store.getPhone_no() == null || store.getPhone_no().length() == 0 ) {
			MGUtilities.showAlertView(
					this, 
					R.string.action_error, 
					R.string.cannot_proceed);
			return;
		}
		
		PackageManager pm = this.getBaseContext().getPackageManager();
	    boolean canCall = pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
	    
	    if(!canCall) {
			MGUtilities.showAlertView(
					this, 
					R.string.action_error, 
					R.string.cannot_proceed);
			return;
		}
		
		String phoneNo = store.getPhone_no().replaceAll("[^0-9]", "");
		
		String uri = "tel:" + phoneNo;
		Intent intent = new Intent(Intent.ACTION_CALL);
		intent.setData(Uri.parse(uri));
		this.startActivity(intent);
	}
	
	private void route() {
		
		if(store.getLat() == 0 || store.getLon() == 0) {
			MGUtilities.showAlertView(
					this, 
					R.string.action_error, 
					R.string.cannot_proceed);
			return;
		}
		
//		String geo = String.format("geo:%f,%f?q=%f,%f", 
//				store.getLat(), 
//				store.getLon(), 
//				store.getLat(), 
//				store.getLon() );
		
		String geo = String.format("http://maps.google.com/maps?f=d&daddr=%s,%s&dirflg=d",
				store.getLat(),
				store.getLon()) ;
		
//		String geo = String.format("http://maps.google.com/maps?f=d&daddr=%s&dirflg=d",
//				store.getStore_address()) ;
		
		Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(geo));		
//		Uri.parse("geo:55.74274,37.56577?q=55.74274,37.56577 (name)"));
		intent.setComponent(new ComponentName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity"));
		this.startActivity(intent);
	}	
	
	private void email() {
		
		if(store.getEmail() == null || store.getEmail().length() == 0) {
			MGUtilities.showAlertView(
					this, 
					R.string.action_error, 
					R.string.cannot_proceed);
			return;
		}
		
		Intent emailIntent = new Intent(Intent.ACTION_SEND);
		emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{ store.getEmail() } );	
		
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, 
				MGUtilities.getStringFromResource(this, R.string.email_subject) );
		
		emailIntent.putExtra(Intent.EXTRA_TEXT, 
				MGUtilities.getStringFromResource(this, R.string.email_body) );
		emailIntent.setType("message/rfc822");
		
		this.startActivity(Intent.createChooser(emailIntent, 
				MGUtilities.getStringFromResource(this, R.string.choose_email_client)) );
	}
	
	private void sms() {
		
		if( store.getSms_no() == null || store.getSms_no().length() == 0 ) {
			MGUtilities.showAlertView(
					this, 
					R.string.action_error, 
					R.string.handset_not_supported);
			return;
		}
		
		PackageManager pm = this.getBaseContext().getPackageManager();
	    boolean canSMS = pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
	    
		if(!canSMS) {
			MGUtilities.showAlertView(
					this, 
					R.string.action_error, 
					R.string.handset_not_supported);
			return;
		}
		
		String smsNo = store.getSms_no().replaceAll("[^0-9]", "");
		
		Intent smsIntent = new Intent(Intent.ACTION_VIEW);
		smsIntent.setType("vnd.android-dir/mms-sms");
		smsIntent.putExtra("address", smsNo);
		
		smsIntent.putExtra("sms_body",
				MGUtilities.getStringFromResource(this, R.string.sms_body) );
		
		this.startActivity(smsIntent);
	}
	
	private void website() {
		
		if(store.getWebsite() == null || store.getWebsite().length() == 0) {
			MGUtilities.showAlertView(
					this, 
					R.string.action_error, 
					R.string.cannot_proceed);
			return;
		}
		
		String strUrl = store.getWebsite();
		if(!strUrl.contains("http")) {
			strUrl = "http://" + strUrl;
		}
		
		Intent webIntent = new Intent(Intent.ACTION_VIEW);
		webIntent.setData(Uri.parse(strUrl));
		this.startActivity(Intent.createChooser(webIntent, 
				MGUtilities.getStringFromResource(this, R.string.choose_browser)));
	}
	
	public InputStream getImage() {
		
		
		Photo p = q.getPhotoByStoreId(store.getStore_id());
		
		ImageView imgViewPhoto = (ImageView) findViewById(R.id.imgViewPhoto);
		
		if(p == null)
			return null;

		BitmapDrawable drawable = (BitmapDrawable)imgViewPhoto.getDrawable();
		Bitmap bitmap = drawable.getBitmap();
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
		bitmap.compress(CompressFormat.PNG, 0 /*ignored for PNG*/, bos); 
		byte[] bitmapdata = bos.toByteArray();
		ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);
		
		return bs;
	}
	 	
}
