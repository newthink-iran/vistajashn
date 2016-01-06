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
import twitter4j.auth.AccessToken;

import com.asynctask.MGAsyncTask;
import com.asynctask.MGAsyncTask.OnMGAsyncTaskListener;
import com.config.Config;
import com.config.UIConfig;
import com.dataparser.DataParser;
import com.db.DbHelper;
import com.db.Queries;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.LoggingBehavior;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.SnapshotReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.helpers.DateTimeHelper;
import com.models.Favorite;
import com.models.News;
import com.models.Photo;
import com.models.Rating;
import com.models.ResponseRating;
import com.models.ResponseStore;
import com.models.Store;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.projects.storefinder.MainActivity;
import com.projects.storefinder.R;
import com.refreshlayout.SwipeRefreshActivity;
import com.social.twitter.TwitterApp;
import com.social.twitter.TwitterApp.TwitterAppListener;
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

public class NewsDetailActivity2 extends SwipeRefreshActivity implements OnClickListener {


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
    private Session.StatusCallback statusCallback;
    private TwitterApp mTwitter;
    private boolean isPending = false;
    private Bundle savedInstanceState;
    private boolean isUserCanRate = false;
    MGAsyncTask task;

    private News news;
    @Override
    public void onCreate(Bundle savedInstanceState) {
    /*    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }*/
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        setContentView(R.layout.activity_news_detail_activity2);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        this.getActionBar().setIcon(R.drawable.header_logo);
        this.getActionBar().setTitle("");

        DbHelper dbHelper = new DbHelper(this);
        q = new Queries(db, dbHelper);

        news = (News) this.getIntent().getSerializableExtra("news");


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
        statusCallback = new SessionStatusCallback();
        mTwitter = new TwitterApp(this, twitterAppListener);


        showSwipeProgress();
        updateStore();

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
                Intent i = new Intent(this, NewReviewActivity.class);
                i.putExtra("new", news);
                startActivity(i);
                return true;*/

            default:
                finish();
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        //getMenuInflater().inflate(R.menu.menu_details, menu);
        getMenuInflater().inflate(R.menu.menu_default, menu);
        ActionBarRtlizer rtlizer = new ActionBarRtlizer(this);
        ViewGroup homeView = (ViewGroup) rtlizer.getHomeView();

        RtlizeEverything.rtlize(rtlizer.getActionBarView());

        if (rtlizer.getHomeViewContainer() instanceof ViewGroup) {
            RtlizeEverything.rtlize((ViewGroup) rtlizer.getHomeViewContainer());
        }

        RtlizeEverything.rtlize(homeView);
       // rtlizer.flipActionBarUpIconIfAvailable(homeView);
        ImageView upIcon = (ImageView) homeView.getChildAt(0);
        upIcon.setRotationY(180);
        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(android.view.Menu menu) {
        // if nav drawer is opened, hide the action items

        return super.onPrepareOptionsMenu(menu);
    }



    @SuppressLint("DefaultLocale")
    private void updateStore() {

        ImageView imgViewPhoto = (ImageView) findViewById(R.id.imgViewPhoto2);


        MainActivity.getImageLoader().displayImage(news.getPhoto_url(), imgViewPhoto, options);

        TextView tvTitle = (TextView) findViewById(R.id.tvTitle2);
//        TextView tvSubtitle = (TextView) findViewById(R.id.tvSubtitle2);
        //RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBar2);
        //TextView tvRatingBarInfo = (TextView) findViewById(R.id.tvRatingBarInfo2);
//
//        ImageView imgViewGallery = (ImageView) findViewById(R.id.imgViewGallery2);
//        imgViewGallery.setOnClickListener(this);
//
//        TextView tvGalleryCount = (TextView) findViewById(R.id.tvGalleryCount2);
//
//
        Button contBtn = (Button) findViewById(R.id.cont);
        contBtn.setOnClickListener(this);



        TextView tvDetails = (TextView) findViewById(R.id.tvDetails2);

/*        ImageView imgViewCall = (ImageView) findViewById(R.id.imgViewCall2);
        imgViewCall.setOnClickListener(this);

        ImageView imgViewEmail = (ImageView) findViewById(R.id.imgViewEmail2);
        imgViewEmail.setOnClickListener(this);

        ImageView imgViewRoute = (ImageView) findViewById(R.id.imgViewRoute2);
        imgViewRoute.setOnClickListener(this);*/

       /* Button imgViewShareFb = (Button) findViewById(R.id.imgViewShareFb2);
        imgViewShareFb.setOnClickListener(this);*/

        /*Button imgViewShareTwitter = (Button) findViewById(R.id.imgViewShareTwitter2);
        imgViewShareTwitter.setOnClickListener(this);*/
/*
        ImageView imgViewSMS = (ImageView) findViewById(R.id.imgViewSMS2);
        imgViewSMS.setOnClickListener(this);

        ImageView imgViewWebsite = (ImageView) findViewById(R.id.imgViewWebsite2);
        imgViewWebsite.setOnClickListener(this);

        ToggleButton toggleButtonFave = (ToggleButton) findViewById(R.id.toggleButtonFave2);
        toggleButtonFave.setOnClickListener(this);


        imgViewCall.setEnabled(false);
        imgViewRoute.setEnabled(false);
        imgViewEmail.setEnabled(false);*/
/*        imgViewSMS.setEnabled(false);
        imgViewWebsite.setEnabled(true);*/

/*        if(news.getNews_url() == null || news.getNews_url().trim().length() == 0)
            imgViewWebsite.setEnabled(false);*/



        // SETTING VALUES
        float rating = 0;



        tvTitle.setText(Html.fromHtml(news.getNews_title()));
        String date = DateTimeHelper.getStringDateFromTimeStamp(news.getCreated_at(), "MM/dd/yyyy");
       // tvRatingBarInfo.setText(date);


        String strDesc = news.getNews_content().replace("\n", "[{~}]");
        Spanned details = Html.fromHtml(strDesc);
        details = Html.fromHtml(details.toString());
        strDesc = details.toString().replace("[{~}]", "\n");

        tvDetails.setText(strDesc);


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

            case R.id.imgViewWebsite:
                website();
                break;

            case R.id.cont:
                website();
                break;

        }
    }


    private void website() {

        if(news.getNews_url() == null || news.getNews_url().length() == 0) {
            MGUtilities.showAlertView(
                    this,
                    R.string.action_error,
                    R.string.cannot_proceed);
            return;
        }

        String strUrl = news.getNews_url();
        if(!strUrl.contains("http")) {
            strUrl = "http://" + strUrl;
        }

        Intent webIntent = new Intent(Intent.ACTION_VIEW);
        webIntent.setData(Uri.parse(strUrl));
        this.startActivity(Intent.createChooser(webIntent,
                MGUtilities.getStringFromResource(this, R.string.choose_browser)));
    }

    private void shareFB() {

        if(isLoggedInToFacebook()) {

            Photo p = photoList != null && photoList.size() > 0 ? photoList.get(0) : null;

            if (FacebookDialog.canPresentShareDialog(this,
                    FacebookDialog.ShareDialogFeature.SHARE_DIALOG)) {
                // Publish the post using the Share Dialog
                FacebookDialog shareDialog = null;

                if(p != null) {
                    shareDialog =
                            new FacebookDialog.ShareDialogBuilder(this)

                                    .setLink(store.getWebsite())
                                    .setPicture(p.getThumb_url())
                                    .build();
                }
                else {

                    shareDialog =
                            new FacebookDialog.ShareDialogBuilder(this)

                                    .setLink(store.getWebsite())
                                    .build();
                }

                shareDialog.present();
            } else {
                // Fallback. For example, publish the post using the Feed Dialog
                Bundle params = new Bundle();
                params.putString("link", store.getWebsite());

                if(p != null)
                    params.putString("picture", p.getThumb_url());

                WebDialog feedDialog = (
                        new WebDialog.FeedDialogBuilder(this,
                                Session.getActiveSession(),
                                params))
                        .setOnCompleteListener(new OnCompleteListener() {

                            @Override
                            public void onComplete(Bundle values,
                                                   FacebookException error) {
                                // TODO Auto-generated method stub

                                if (error == null) {
                                    // When the story is posted, echo the success
                                    // and the post Id.
                                    final String postId = values.getString("post_id");
                                    if (postId != null) {
                                        // publish successful

                                    } else {
                                        // User clicked the Cancel button
                                        MGUtilities.showAlertView(
                                                NewsDetailActivity2.this,
                                                R.string.publish_error,
                                                R.string.publish_cancelled);
                                    }
                                } else if (error instanceof FacebookOperationCanceledException) {
                                    // User clicked the "x" button

                                    MGUtilities.showAlertView(
                                            NewsDetailActivity2.this,
                                            R.string.publish_error,
                                            R.string.publish_cancelled);
                                } else {
                                    MGUtilities.showAlertView(
                                            NewsDetailActivity2.this,
                                            R.string.network_error,
                                            R.string.problems_encountered_facebook);
                                }
                            }

                        })
                        .build();
                feedDialog.show();
            }
        }
        else {
            isPending = true;
            loginToFacebook(savedInstanceState);
        }
    }


    @SuppressLint("InflateParams")
    private void postToTwitter() {

        isPending = false;

        LayoutInflater inflate = (LayoutInflater)
                this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View view = inflate.inflate(R.layout.twitter_dialog, null);

        // create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setView(view);
        builder.setTitle("Twitter Status");
        builder.setCancelable(false);

        final EditText txtStatus = (EditText) view.findViewById(R.id.txtStatus);
        txtStatus.setText("");

        // set dialog button
        builder.setPositiveButton("Tweet!", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                String tweet = txtStatus.getText().toString().trim();

                InputStream is = getImage();

                if(is == null)
                    mTwitter.updateStatus(tweet);
                else
                    mTwitter.updateStatusWithLogo(is, tweet);

            }
        })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        // show dialog
        AlertDialog alert = builder.create();
        alert.show();
    }

    public InputStream getImage() {


        Photo p = q.getPhotoByStoreId(store.getStore_id());

        ImageView imgViewPhoto = (ImageView) findViewById(R.id.imgViewPhoto2);

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






    // FACEBOOK
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Session session = Session.getActiveSession();
        Session.saveSession(session, outState);
    }

    @Override
    public void onStart()  {
        super.onStart();

        if(Session.getActiveSession() != null)
            Session.getActiveSession().addCallback(statusCallback);
    }

    @Override
    public void onStop() {
        super.onStop();

        if(Session.getActiveSession() != null)
            Session.getActiveSession().removeCallback(statusCallback);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }

    // ###############################################################################################
    // FACEBOOK INTEGRATION METHODS
    // ###############################################################################################
    public void loginToFacebook(Bundle savedInstanceState) {
        Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);

        Session session = Session.getActiveSession();

        if (session == null) {

            session = new Session(this);
            Session.setActiveSession(session);
        }

        if (!session.isOpened() && session.isClosed()) {
            session.openForRead(new Session.OpenRequest(this)
                    .setPermissions(Arrays.asList("public_profile", "email"))
                    .setCallback(statusCallback));
        } else {
            Session.openActiveSession(this, true, statusCallback);
            updateView();
        }
    }

    private void updateView() {
        Session session = Session.getActiveSession();
        if (session.isOpened()) {
//	            URL_PREFIX_FRIENDS + session.getAccessToken();
            isPending = false;
            shareFB();
        }
    }


    private class SessionStatusCallback implements Session.StatusCallback {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            updateView();
        }
    }

    // ###############################################################################################
    // TWITTER INTEGRATION METHODS
    // ###############################################################################################
    public void loginToTwitter() {
        if (mTwitter.hasAccessToken() == true) {
            try {
                postToTwitter();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {

            isPending = true;
            mTwitter.loginToTwitter();
        }
    }

    TwitterAppListener twitterAppListener = new TwitterAppListener() {

        @Override
        public void onError(String value)  {
            // TODO Auto-generated method stub
            Log.e("TWITTER ERROR**", value);
        }

        @Override
        public void onComplete(AccessToken accessToken) {
            // TODO Auto-generated method stub
            NewsDetailActivity2.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    postToTwitter();
                }
            });
        }
    };

    public boolean isLoggedInToFacebook() {
        Session session = Session.getActiveSession();
        return (session != null && session.isOpened());
    }




}
