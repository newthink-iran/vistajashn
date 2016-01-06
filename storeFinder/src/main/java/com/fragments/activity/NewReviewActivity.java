package com.fragments.activity;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.asynctask.MGAsyncTask;
import com.asynctask.MGAsyncTask.OnMGAsyncTaskListener;
import com.config.Config;
import com.dataparser.DataParser;
import com.models.ResponseReview;
import com.models.Review;
import com.models.Store;
import com.projects.storefinder.R;
import com.usersession.UserAccessSession;
import com.usersession.UserSession;
import com.utilities.MGUtilities;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.SpannedString;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import info.semsamot.actionbarrtlizer.ActionBarRtlizer;
import info.semsamot.actionbarrtlizer.RtlizeEverything;

public class NewReviewActivity extends FragmentActivity {

	private Store store;
	private ResponseReview response;
	MGAsyncTask task;
	
	@SuppressLint("DefaultLocale") 
	@Override
	public void onCreate(Bundle savedInstanceState) {
/*        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }*/
		super.onCreate(savedInstanceState);
		
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
		setContentView(R.layout.fragment_review_new);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        this.getActionBar().setIcon(R.drawable.header_logo);
        this.getActionBar().setTitle("");
        
		store = (Store) this.getIntent().getSerializableExtra("store");
		
		
		final EditText txtReview = (EditText) findViewById(R.id.txtReview);
		final TextView tvMaxCharCount = (TextView) findViewById(R.id.tvMaxCharCount);
		String charsLeft = String.format("%d %s", 
				Config.MAX_CHARS_REVIEWS, 
				MGUtilities.getStringFromResource(NewReviewActivity.this, R.string.chars_left));
		
		tvMaxCharCount.setText(charsLeft);
		
		InputFilter filter = new InputFilter() { 
        
			@Override
            public CharSequence filter(CharSequence source, int start, int end, 
                            Spanned dest, int dstart, int dend) { 
            	
//	            for (int i = start; i < end; i++) { 
//	                if (!Character.isLetterOrDigit(source.charAt(i))) { 
//	                        return ""; 
//	                } 
//	            } 
				
				if(source.length() >= Config.MAX_CHARS_REVIEWS)
					return "";
				
				String charsLeft = String.format("%d %s", 
						Config.MAX_CHARS_REVIEWS - txtReview.getText().toString().length(), 
						MGUtilities.getStringFromResource(NewReviewActivity.this, R.string.chars_left));
				
				tvMaxCharCount.setText(charsLeft);
	            
	            return source; 
            }
		};
		
		txtReview.setFilters(new InputFilter[] { filter } ); 
	}
	
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // toggle nav drawer on selecting action bar app icon/title
        
        // Handle action bar actions click
        switch (item.getItemId()) {
	        case R.id.menuPostReview:
	        	postReview();
	            return true;
	            
	        default:
	        	finish();	
	            return super.onOptionsItemSelected(item);
        }
    }
 
    
    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new_review, menu);
        ActionBarRtlizer rtlizer = new ActionBarRtlizer(this);
        ViewGroup homeView = (ViewGroup) rtlizer.getHomeView();

        RtlizeEverything.rtlize(rtlizer.getActionBarView());

        if (rtlizer.getHomeViewContainer() instanceof ViewGroup) {
            RtlizeEverything.rtlize((ViewGroup) rtlizer.getHomeViewContainer());
        }

        RtlizeEverything.rtlize(homeView);
      //  rtlizer.flipActionBarUpIconIfAvailable(homeView);
        ImageView upIcon = (ImageView) homeView.getChildAt(0);
        upIcon.setRotationY(180);
        return true;
    }
    
    
    @Override
    public boolean onPrepareOptionsMenu(android.view.Menu menu) {
        // if nav drawer is opened, hide the action items
        
        return super.onPrepareOptionsMenu(menu);
    }
    
    
    
	public void postReview() {
		
		if(!MGUtilities.hasConnection(NewReviewActivity.this)) {
			
			MGUtilities.showAlertView(
					NewReviewActivity.this, 
					R.string.network_error, 
					R.string.no_network_connection);
			
			return;
		}
		
		EditText txtReview = (EditText) findViewById(R.id.txtReview);
		String reviewStr = txtReview.getText().toString().trim();
		if(reviewStr.length() == 0) {
			MGUtilities.showAlertView(
					this, 
					R.string.empty_error, 
					R.string.empty_error_details);
			return;
		}
		
        task = new MGAsyncTask(NewReviewActivity.this);
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
				reloadDataToReview();
			}
			
			@Override
			public void onAsyncTaskDoInBackground(MGAsyncTask asyncTask) {
				// TODO Auto-generated method stub
				syncReview();
			}
		});
        task.execute();
	}
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void syncReview() {
		
		EditText txtReview = (EditText) findViewById(R.id.txtReview);
		String reviewStr = txtReview.getText().toString().trim();
//		reviewStr = reviewStr.replace("\"", "");
//		reviewStr = reviewStr.replace("'", "");
		
		UserAccessSession userAccess = UserAccessSession.getInstance(NewReviewActivity.this);
		UserSession userSession = userAccess.getUserSession();
		
		try {
//			String reviewString = URLEncoder.encode(txtReview.getText().toString(), "UTF-8");
			SpannedString span = new SpannedString(reviewStr);
			String reviewString1 = Html.toHtml(span);
			String reviewString = Html.escapeHtml(span);
			
			Log.e("toHtml", reviewString1);
			Log.e("escapeHtml", reviewString);
			
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("store_id", String.valueOf(store.getStore_id()) ));
			params.add(new BasicNameValuePair("review", reviewString ));
			params.add(new BasicNameValuePair("user_id", String.valueOf(userSession.getUser_id()) ));
			params.add(new BasicNameValuePair("login_hash", userSession.getLogin_hash() ));

	        response = DataParser.getJSONFromUrlReview(Config.POST_REVIEW_URL, params);
	        
	        if(response != null) {
	        	
	        	if(response.getReturn_count() < response.getTotal_row_count()) {
	        		
	                if(response.getReviews() != null) {
	                	Review review = new Review();
	                	review.setReview_id(-1);
	                	response.getReviews().add(0, review);
	                }
	            }
	        }
	        
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public void reloadDataToReview() {
			
		Intent returnIntent = new Intent();
//		returnIntent.putExtra("response",response);
//		returnIntent.putExtra("store",store);
		setResult(RESULT_OK, returnIntent);
		finish();
		
	}
	
	@Override
    public void onDestroy()  {
        super.onDestroy();
        
        if(task != null)
        	task.cancel(true);
	}
}
