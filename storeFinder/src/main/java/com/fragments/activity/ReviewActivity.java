package com.fragments.activity;

import com.adapters.MGListAdapter;
import com.adapters.MGListAdapter.OnMGListAdapterAdapterListener;
import com.asynctask.MGAsyncTask;
import com.asynctask.MGAsyncTask.OnMGAsyncTaskListener;
import com.config.Config;
import com.config.UIConfig;
import com.dataparser.DataParser;
import com.imageview.RoundedImageView;
import com.models.News;
import com.models.ResponseReview;
import com.models.Review;
import com.models.Store;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.projects.storefinder.MainActivity;
import com.projects.storefinder.R;
import com.refreshlayout.SwipeRefreshActivity;
import com.usersession.UserAccessSession;
import com.usersession.UserSession;
import com.utilities.MGUtilities;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import info.semsamot.actionbarrtlizer.ActionBarRtlizer;
import info.semsamot.actionbarrtlizer.RtlizeEverything;

public class ReviewActivity extends SwipeRefreshActivity implements OnItemClickListener{

	private Store store;
	private News news;
	private int reviewCount;
	private ResponseReview response;
	DisplayImageOptions options;
	private int NEW_REVIEW_REQUEST_CODE = 9901;
	MGAsyncTask task;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
    /*    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }*/
		super.onCreate(savedInstanceState);
		
//		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
		setContentView(R.layout.fragment_review);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        this.getActionBar().setIcon(R.drawable.header_logo);
        this.getActionBar().setTitle("");
		
		options = new DisplayImageOptions.Builder()
			.showImageOnLoading(UIConfig.PLACEHOLDER_REVIEW_THUMB)
			.showImageForEmptyUri(UIConfig.PLACEHOLDER_REVIEW_THUMB)
			.showImageOnFail(UIConfig.PLACEHOLDER_REVIEW_THUMB)
			.cacheInMemory(true)
			.cacheOnDisk(true)
			.considerExifParams(true)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.build();

		//store = (Store) this.getIntent().getSerializableExtra("store");
		news = (News) this.getIntent().getSerializableExtra("news");
		reviewCount = Config.MAX_REVIEW_COUNT_PER_LISTING;
		response = (ResponseReview) this.getIntent().getSerializableExtra("response");
		Handler h = new Handler();
		h.postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (response == null) {
					getReviews();
				} else {
					showList();
				}
			}
		}, Config.DELAY_SHOW_ANIMATION);
		
		showSwipeProgress();
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
	        case R.id.menuNewReview:
	        	newReview();
	            return true;
	            
	        default:
	        	finish();	
	            return super.onOptionsItemSelected(item);
        }
    }
 
    
    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_reviews, menu);
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
    
    private void newReview() {
    	//UserAccessSession userAccess = UserAccessSession.getInstance(ReviewActivity.this);
		//UserSession userSession = userAccess.getUserSession();
		
		//if(userSession == null) {
		//	MGUtilities.showAlertView(ReviewActivity.this, R.string.login_error, R.string.login_error_review);
		//	return;
		//}
		
		Intent i = new Intent(this, NewReviewActivity.class);
		//i.putExtra("store", store);
		i.putExtra("news", news);
		startActivityForResult(i, NEW_REVIEW_REQUEST_CODE);
    }
	
	public void getReviews() {
		
		if(!MGUtilities.hasConnection(this)) {
			
			MGUtilities.showAlertView(
					this, 
					R.string.network_error, 
					R.string.no_network_connection);
			hideSwipeProgress();
			return;
		}
		
        task = new MGAsyncTask(ReviewActivity.this);
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
				showList();
				hideSwipeProgress();
			}
			
			@Override
			public void onAsyncTaskDoInBackground(MGAsyncTask asyncTask) {
				// TODO Auto-generated method stub
				parseReviews();
			}
		});
        task.execute();
	}
	
	@SuppressLint("DefaultLocale") 
	public void parseReviews() {
		
		//String reviewUrl = String.format("%s?count=%d&store_id=%s",
		//		Config.REVIEWS_URL, reviewCount, store.getStore_id());
		String reviewUrl = String.format("%s?count=%d&store_id=%s",
				Config.REVIEWS_URL, reviewCount, news.getNews_id());

        response = DataParser.getJSONFromUrlReview(reviewUrl, null);
        if(response != null) {
        	
        	if(response.getReturn_count() < response.getTotal_row_count()) {
                
                if(response.getReviews() != null) {
                	Review review = new Review();
                	review.setReview_id(-1);
                	response.getReviews().add(0, review);
                }
            }
        }
	}
	
	
	private void showList() {
		
		if(response.getReviews() == null)
			return;
		ListView listView = (ListView) findViewById(R.id.listView);
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		listView.setOnItemClickListener(this);
		
		MGListAdapter adapter = new MGListAdapter(
				ReviewActivity.this, response.getReviews().size(), R.layout.review_entry);
		
		adapter.setOnMGListAdapterAdapterListener(new OnMGListAdapterAdapterListener() {
			
			@SuppressLint("DefaultLocale") 
			@Override
			public void OnMGListAdapterAdapterCreated(MGListAdapter adapter, View v,
					int position, ViewGroup viewGroup) {
				// TODO Auto-generated method stub
				
				
				Review review = response.getReviews().get(position);
				
				LinearLayout linearLoadMore = (LinearLayout) v.findViewById(R.id.linearLoadMore);
				LinearLayout linearMain = (LinearLayout) v.findViewById(R.id.linearMain);
				linearLoadMore.setVisibility(View.VISIBLE);
				linearMain.setVisibility(View.VISIBLE);
				if(review.getReview_id() > 0) {
					
					linearLoadMore.setVisibility(View.GONE);
					Spanned details1 = Html.fromHtml(review.getReview());
					Spanned details2 = Html.fromHtml(details1.toString());
					
//					String reviewString = URLDecoder.decode(details2.toString());
					String reviewString = details2.toString();
					Spanned title = Html.fromHtml(review.getFull_name());
					Log.e("Review", reviewString);
					TextView tvTitle = (TextView) v.findViewById(R.id.tvTitle);
					tvTitle.setText(title);
					
					TextView tvDetails = (TextView) v.findViewById(R.id.tvDetails);
					tvDetails.setText(reviewString);
					
					RoundedImageView imgViewPhoto = (RoundedImageView) v.findViewById(R.id.imgViewThumb);
					imgViewPhoto.setCornerRadius(R.dimen.corner_radius_review);
					imgViewPhoto.setBorderWidth(UIConfig.BORDER_WIDTH);
					imgViewPhoto.setBorderColor(getResources().getColor(UIConfig.THEME_BLACK_COLOR));
					
					if(review.getThumb_url() != null) {
						MainActivity.getImageLoader().displayImage(review.getThumb_url(), imgViewPhoto, options);
					}
				}
				else if(review.getReview_id() == -1) {
					
					linearMain.setVisibility(View.GONE);
					
					int remaining = response.getTotal_row_count() - response.getReturn_count();
					String str = String.format("%s %d %s", 
							MGUtilities.getStringFromResource(ReviewActivity.this, R.string.view), 
							remaining, 
							MGUtilities.getStringFromResource(ReviewActivity.this, R.string.comments));
					
					TextView tvTitle = (TextView) v.findViewById(R.id.tvLoadMore);
					tvTitle.setText(str);
					
				}
			}
		});
		listView.setAdapter(adapter);
		adapter.notifyDataSetChanged();
	}
	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

	    if (requestCode == NEW_REVIEW_REQUEST_CODE) {
	    	
	        if(resultCode == Activity.RESULT_OK) {
	        	getReviews();
	        }
	        else if (resultCode == Activity.RESULT_CANCELED) {
	            //Write your code if there's no result
	        }
	    }
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View v, int pos, long resid) {
		// TODO Auto-generated method stub
		
		if(!MGUtilities.hasConnection(this)) {
			
			MGUtilities.showAlertView(
					this, 
					R.string.network_error, 
					R.string.no_network_connection);
			return;
		}
		
		Review review = response.getReviews().get(pos);
		
		if(review.getReview_id() == -1 ) {
			reviewCount += Config.MAX_REVIEW_COUNT_PER_LISTING;
			getReviews();
		}
	}
	

}
