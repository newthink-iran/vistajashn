package com.fragments.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.TextView;

import com.asynctask.MGAsyncTask;
import com.config.UIConfig;
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
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.helpers.DateTimeHelper;
import com.models.Discount;
import com.models.Favorite;
import com.models.News;
import com.models.Photo;
import com.models.ResponseRating;
import com.models.ResponseStore;
import com.models.Store;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.projects.arzansara.MainActivity;
import com.projects.arzansara.R;
import com.refreshlayout.SwipeRefreshActivity;
import com.social.twitter.TwitterApp;
import com.social.twitter.TwitterApp.TwitterAppListener;
import com.utilities.MGUtilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import info.semsamot.actionbarrtlizer.ActionBarRtlizer;
import info.semsamot.actionbarrtlizer.RtlizeEverything;
import twitter4j.auth.AccessToken;

public class DiscountsActivity extends SwipeRefreshActivity implements OnClickListener {


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

    private Discount discount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
    /*    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }*/
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        setContentView(R.layout.activity_discounts);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        this.getActionBar().setIcon(R.drawable.header_logo);
        this.getActionBar().setTitle("");

        DbHelper dbHelper = new DbHelper(this);
        q = new Queries(db, dbHelper);
        discount = (Discount) this.getIntent().getSerializableExtra("discount");

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
        //statusCallback = new SessionStatusCallback();
        //mTwitter = new TwitterApp(this, twitterAppListener);


        showSwipeProgress();
        updateStore();

    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (task != null)
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

        Log.d("alaki", "987928347");

        ImageView imgViewPhoto = (ImageView) findViewById(R.id.imgViewPhoto2);


        MainActivity.getImageLoader().displayImage(discount.getPhoto_url(), imgViewPhoto, options);

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

        Button buttonFavorite = (Button) findViewById(R.id.ButtonFavorite);
        buttonFavorite.setOnClickListener(this);
        Favorite fave = q.getFavoriteByStoreId(discount.getDiscount_id());
        if (fave != null)
            buttonFavorite.setText("حذف از علاقه مندی ها");
        else
            buttonFavorite.setText("افزودن به علاقه مندی ها");

        /*Button comments = (Button) findViewById(R.id.comments);
        comments.setOnClickListener(this);*/

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


        tvTitle.setText(Html.fromHtml(discount.getDiscount_title()));

        //int diffDate = discount.getCreated_at() + (discount.getExp_date()*24*60*60);
        //String date = DateTimeHelper.getStringDateFromTimeStamp(diffDate, "MM/dd/yyyy");
        // tvRatingBarInfo.setText(date);


        String strDesc = discount.getDiscount_content().replace("\n", "[{~}]");
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
        switch (v.getId()) {

            /*case R.id.imgViewWebsite:
                website();
                break;*/

            case R.id.cont:
                website();
                break;

            case R.id.ButtonFavorite:
                checkFave(v);
                break;

            /*case R.id.comments:
                Intent i = new Intent(this, ReviewActivity.class);
                i.putExtra("news", news);
                startActivity(i);
                break;*/

        }
    }


    private void checkFave(View view) {
        Favorite fave = q.getFavoriteByStoreId(discount.getDiscount_id());
        if (fave != null) {
            q.deleteFavorite(discount.getDiscount_id());
            ((Button) view).setText("افزودن به علاقه مندی ها");

        } else {
            fave = new Favorite();
            fave.setStore_id(discount.getDiscount_id());
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
        String strDesc = discount.getDiscount_content().replace("\n", "[{~}]");
        Spanned details = Html.fromHtml(strDesc);
        details = Html.fromHtml(details.toString());
        strDesc = details.toString().replace("[{~}]", "\n");
        String from = getString(R.string.from);
        String sendText = from + "\n\n" + Html.fromHtml(discount.getDiscount_title()).toString() + "\n" + strDesc;
        sendIntent.putExtra(Intent.EXTRA_TEXT, sendText);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, getString(R.string.action_share)));
    }
}