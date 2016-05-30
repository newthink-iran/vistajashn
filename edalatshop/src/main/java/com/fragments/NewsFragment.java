package com.fragments;

import java.util.ArrayList;
import java.util.Date;

import com.adapters.MGListAdapter;
import com.adapters.MGListAdapter.OnMGListAdapterAdapterListener;
import com.amplitude.api.Amplitude;
import com.config.Config;
import com.config.Constants;
import com.config.UIConfig;
import com.db.Queries;
import com.fragments.activity.DiscountsActivity;
import com.fragments.activity.NewsDetailActivity2;
import com.helpers.DateTimeHelper;
import com.imageview.MGImageView;
import com.models.Discount;
import com.models.News;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.utilities.MGUtilities;
import com.projects.edalatshop.MainActivity;
import com.projects.edalatshop.R;

import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.fragments.activity.Utilities;

public class NewsFragment extends Fragment implements OnItemClickListener, OnClickListener{
	
	private View viewInflate;
	private ArrayList<News> arrayData;
	DisplayImageOptions options;
	private String title=null;
	public NewsFragment() { }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if(getArguments()!=null) {
			Bundle bundle = getArguments();
			if (bundle.containsKey(Constants.KEY_NOTIFY_TITLE))
				title = bundle.getString(Constants.KEY_NOTIFY_TITLE);
		}
		viewInflate = inflater.inflate(R.layout.fragment_news, null);
		return viewInflate;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);	
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onViewCreated(view, savedInstanceState);
		
		MainActivity main = (MainActivity) this.getActivity();
		final Queries q = main.getQueries();

		view.setFocusableInTouchMode(true);
		view.requestFocus();
		view.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {

				if (keyCode == KeyEvent.KEYCODE_BACK) {
					android.support.v4.app.FragmentManager fm = getFragmentManager();
					if (fm.getBackStackEntryCount() > 0){
						getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
						return true;
					}
					return false;
				} else {
					return false;
				}
			}
		});
		
		options = new DisplayImageOptions.Builder()
			.showImageOnLoading(UIConfig.SLIDER_PLACEHOLDER)
			.showImageForEmptyUri(UIConfig.SLIDER_PLACEHOLDER)
			.showImageOnFail(UIConfig.SLIDER_PLACEHOLDER)
			.cacheInMemory(true)
			.cacheOnDisk(true)
			.considerExifParams(true)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.build();
		
		Handler h = new Handler();
		h.postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				arrayData = q.getNews();
				showList();
			}
		}, Config.DELAY_SHOW_ANIMATION);
		
		main.showSwipeProgress();
	}
	
	private void showList() {
		
		MainActivity main = (MainActivity) this.getActivity();
		main.hideSwipeProgress();
		
		if(arrayData != null && arrayData.size() == 0) {
			MGUtilities.showNotifier(this.getActivity(), MainActivity.offsetY);
			return;
		}
		
		ListView listView = (ListView) viewInflate.findViewById(R.id.listView);
		listView.setOnItemClickListener(this);
		
		MGListAdapter adapter = new MGListAdapter(
				getActivity(), arrayData.size(), R.layout.news_entry);
		
		adapter.setOnMGListAdapterAdapterListener(new OnMGListAdapterAdapterListener() {
			
			@Override
			public void OnMGListAdapterAdapterCreated(MGListAdapter adapter, View v,
					int position, ViewGroup viewGroup) {
				// TODO Auto-generated method stub
				
				News news = arrayData.get(position);
				
				MGImageView imgViewPhoto = (MGImageView) v.findViewById(R.id.imgViewPhoto);
				imgViewPhoto.setCornerRadius(0.0f);
				imgViewPhoto.setBorderWidth(UIConfig.BORDER_WIDTH);
				imgViewPhoto.setBorderColor(getResources().getColor(UIConfig.THEME_BLACK_COLOR));
				
				if(news.getPhoto_url() != null) {
					MainActivity.getImageLoader().displayImage(news.getPhoto_url(), imgViewPhoto, options);
				}
				else {
					imgViewPhoto.setImageResource(UIConfig.SLIDER_PLACEHOLDER);
				}
				
				imgViewPhoto.setTag(position);
				
				Spanned name = Html.fromHtml(news.getNews_title());
				//Spanned address = Html.fromHtml(news.getNews_content());
				String address = news.getNews_content();
				address = address.replace("&amp;lt;", "<");
				address = address.replace("&amp;gt;", ">");

				
				TextView tvTitle = (TextView) v.findViewById(R.id.tvTitle);
				tvTitle.setText(name);
				
				//TextView tvSubtitle = (TextView) v.findViewById(R.id.tvSubtitle);
				//tvSubtitle.setText(Html.fromHtml(address));

				TextView tvViewCount = (TextView) v.findViewById(R.id.tvViewCount);
				tvViewCount.setText("تعداد بازدید: " + String.valueOf(news.getViewCount()));


				/* elyas : farsi date */

				Date d = DateTimeHelper.getDateFromTimeStamp(news.getCreated_at());
				String date = Utilities.FarsiDate(d);

				//String date = DateTimeHelper.getStringDateFromTimeStamp(news.getCreated_at(), "MM/dd/yyyy" );
				TextView tvDate = (TextView) v.findViewById(R.id.tvDate);
				tvDate.setText("تاریخ: " + date);
			}
		});
		listView.setAdapter(adapter);
		adapter.notifyDataSetChanged();
	}
	@Override
	public void onResume() {
		super.onResume();
		if(title!=null){
			new Handler().post(new Runnable() {
				@Override
				public void run() {
					MainActivity main = (MainActivity) NewsFragment.this.getActivity();
					final Queries q = main.getQueries();
					arrayData = q.getNews();
					for(int i=0;i<arrayData.size();i++){
						if(arrayData.get(i).getNews_title().equals(title)){
							Constants.type=null;
							Constants.title=null;
							title=null;
							final News news = arrayData.get(i);
							Intent intent = new Intent(getActivity(), NewsDetailActivity2.class);
							Amplitude.getInstance().logEvent(news.getNews_title());
							intent.putExtra("news", news);
							getActivity().startActivity(intent);
							break;

						}
					}
				}
			});

		}

	}
	@Override
	public void onItemClick(AdapterView<?> adapterView, View v, int pos, long resId) {
		// TODO Auto-generated method stub
		
		News news = arrayData.get(pos);
//		NewsDetailFragment fragment = new NewsDetailFragment();
//		
//		Bundle b = new Bundle();
//		b.putSerializable("news", news);
//		fragment.setArguments(b);
//		
//		MainActivity main = (MainActivity) this.getActivity();
//		main.switchContent(fragment, true);
		
		//Intent i = new Intent(getActivity(), NewsDetailActivity.class);
		Intent i = new Intent(getActivity(), NewsDetailActivity2.class);
		Amplitude.getInstance().logEvent(news.getNews_title());
		i.putExtra("news", news);
		getActivity().startActivity(i);
	}


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}



}
