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
import com.helpers.DateTimeHelper;
import com.models.Favorite;
import com.models.News;
import com.models.Photo;
import com.models.Rating;
import com.models.ResponseRating;
import com.models.ResponseStore;
import com.models.Store;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.projects.arzansara.MainActivity;
import com.projects.arzansara.R;
import com.refreshlayout.SwipeRefreshActivity;
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
        ImageView contBtn = (ImageView) findViewById(R.id.cont);
        contBtn.setOnClickListener(this);

        /*ImageView buttonFavorite = (ImageView) findViewById(R.id.ButtonFavorite);
        buttonFavorite.setOnClickListener(this);
        Favorite fave = q.getFavoriteByStoreId(news.getNews_id());
        if(fave != null)
            buttonFavorite.setImageResource(R.drawable.button_call);
        else
            buttonFavorite.setImageResource(R.drawable.button_fav);*/

        ImageView comments = (ImageView) findViewById(R.id.comments);
        comments.setOnClickListener(this);

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


        //String strDesc = news.getNews_content().replace("\n", "[{~}]");
        //Spanned details = Html.fromHtml(strDesc);
        //details = Html.fromHtml(details.toString());
        //strDesc = details.toString().replace("[{~}]", "\n");
        String address = news.getNews_content();
        address = address.replace("&amp;lt;", "<");
        address = address.replace("&amp;gt;", ">");

        tvDetails.setText(Html.fromHtml(address));


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

            /*case R.id.ButtonFavorite:
                checkFave(v);
                break;*/

            case R.id.comments:
                Intent i = new Intent(this, ReviewActivity.class);
                i.putExtra("news", news);
                startActivity(i);
                break;

        }
    }


    private void checkFave(View view) {
        Favorite fave = q.getFavoriteByStoreId(news.getNews_id());
        if(fave != null) {
            q.deleteFavorite(news.getNews_id());
            ((Button) view).setText("افزودن به علاقه مندی ها");

        }
        else {
            fave = new Favorite();
            fave.setStore_id(news.getNews_id());
            q.insertFavorite(fave);
            ((Button) view).setText("حذف از علاقه مندی ها");
        }
    }


    private void website() {
        /*if(news.getNews_url() == null || news.getNews_url().length() == 0) {
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
                MGUtilities.getStringFromResource(this, R.string.choose_browser)));*/

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        String strDesc = news.getNews_content().replace("\n", "[{~}]");
        Spanned details = Html.fromHtml(strDesc);
        details = Html.fromHtml(details.toString());
        strDesc = details.toString().replace("[{~}]", "\n");
        String from = getString(R.string.from);
        String sendText = from + "\n\n" + Html.fromHtml(news.getNews_title()).toString() + "\n" + strDesc;
        sendIntent.putExtra(Intent.EXTRA_TEXT, sendText);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, getString(R.string.action_share)));
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
    }

    @Override
    public void onStart()  {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }


}
