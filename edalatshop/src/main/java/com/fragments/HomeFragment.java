package com.fragments;

import java.util.ArrayList;

import com.asynctask.MGAsyncTask;
import com.asynctask.MGAsyncTask.OnMGAsyncTaskListener;
import com.config.Config;
import com.config.Constants;
import com.config.UIConfig;
import com.dataparser.DataParser;
import com.db.Queries;
import com.fragments.activity.NewsDetailActivity2;
import com.fragments.activity.StoreActivity;
import com.models.Category;
import com.models.Data;
import com.models.DataNews;
import com.models.Discount;
import com.models.News;
import com.models.Photo;
import com.models.Setting;
import com.models.Store;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.projects.edalatshop.MainActivity;
import com.projects.edalatshop.R;
import com.slider.MGSliderAdapter;
import com.slider.MGSliderAdapter.OnMGSliderAdapterListener;
import com.utilities.MGUtilities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class HomeFragment extends Fragment implements OnItemClickListener, OnClickListener {
	
	private View viewInflate;
	private String type=null,title=null;;
	DisplayImageOptions options;
	ArrayList<Store> storeList;
	ArrayList<News> newsList;
	MGAsyncTask task;
	ProgressDialog progressDialog;
	
	public HomeFragment() { }
	
	@SuppressLint("InflateParams") 
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if(Constants.title!=null&&Constants.type!=null) {
			if (getArguments() != null) {
				Bundle bundle = getArguments();

				if (bundle.containsKey(Constants.KEY_NOTIFY_TYPE))
					type = bundle.getString(Constants.KEY_NOTIFY_TYPE);

				if (bundle.containsKey(Constants.KEY_NOTIFY_TITLE))
					title = bundle.getString(Constants.KEY_NOTIFY_TITLE);
			}
		}
		viewInflate = inflater.inflate(R.layout.fragment_home, null);
		return viewInflate;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {

		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(task != null)
			task.cancel(true);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		super.onViewCreated(view, savedInstanceState);


		options = new DisplayImageOptions.Builder()
			.showImageOnLoading(UIConfig.SLIDER_PLACEHOLDER)
			.showImageForEmptyUri(UIConfig.SLIDER_PLACEHOLDER)
			.showImageOnFail(UIConfig.SLIDER_PLACEHOLDER)
			.cacheInMemory(true)
			.cacheOnDisk(true)
			.considerExifParams(true)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.build();
		
		MainActivity main = (MainActivity) getActivity();
		//main.showSwipeProgress();

		if(main.IsFirst){
			progressDialog = new ProgressDialog(this.getActivity());
			progressDialog.setMessage("لطفا شکیبا باشید...");
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setCancelable(false);
			progressDialog.show();
		}

		main.getDebugKey();
        // my edit //
        //Queries q = main.getQueries();
        //storeList = q.getStoresFeatured();
       // newsList = q.getNews();
        //if(!storeList.isEmpty() || !newsList.isEmpty() )
        //createSlider();
        showList();

        // end my edit //

		Handler h = new Handler();
		h.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				getData();
			}
		}, Config.DELAY_SHOW_ANIMATION);
	}

	public void getData() {
		
		task = new MGAsyncTask(getActivity());
		task.setMGAsyncTaskListener(new OnMGAsyncTaskListener() {
			
			@Override
			public void onAsyncTaskProgressUpdate(MGAsyncTask asyncTask) { }
			
			@Override
			public void onAsyncTaskPreExecute(MGAsyncTask asyncTask) {
				asyncTask.dialog.hide();
			}
			
			@Override
			public void onAsyncTaskPostExecute(MGAsyncTask asyncTask) {
				// TODO Auto-generated method stub
				MainActivity main = (MainActivity) getActivity();
				//Queries q = main.getQueries();
				//storeList = q.getStoresFeatured();
				//newsList = q.getNews();
				
				//createSlider();

				showList();

				//main.hideSwipeProgress();
				if(main.IsFirst){
					main.IsFirst = false;
					progressDialog.hide();
				}


				ImageView btn1 = (ImageView) viewInflate.findViewById(R.id.button1);
				btn1.setEnabled(true);

				ImageView btn2 = (ImageView) viewInflate.findViewById(R.id.button2);
				btn2.setEnabled(true);

				ImageView btn3 = (ImageView) viewInflate.findViewById(R.id.button3);
				btn3.setEnabled(true);

				ImageView btn4 = (ImageView) viewInflate.findViewById(R.id.button4);
				btn4.setEnabled(true);

				ImageView btn5 = (ImageView) viewInflate.findViewById(R.id.button5);
				btn5.setEnabled(true);

				if(type!=null) {
					if(type.equals(Constants.KEY_NEWS)) {
						if(title!=null) {
							onClickNewsFragment(title);
						}
					}

				}
				if(type!=null) {

					if(type.equals(Constants.KEY_DISCOUNT)) {
						if(title!=null) {
							onClickDiscountFragment(title);
						}
					}
				}

			}
			
			@Override
			public void onAsyncTaskDoInBackground(MGAsyncTask asyncTask) {
				// TODO Auto-generated method stub
				MainActivity main = (MainActivity) getActivity();
				if(main == null)
					return;

				if(main.IsFirst){
					try {
						DataParser parser = new DataParser();
						Data data = parser.getData(Config.DATA_JSON_URL);
						DataNews dataNews = parser.getDataNews(Config.DATA_NEWS_URL);

						//MainActivity main = (MainActivity) getActivity();

						if(main == null)
							return;

						Queries q = main.getQueries();

						if(data == null)
							return;

						if(data.getCategories() != null && data.getCategories().size() > 0) {

							q.deleteTable("categories");
							for(Category cat : data.getCategories()) {
								q.insertCategory(cat);
							}
							Log.e("HOME FRAGMENT LOG", "Store count =" + data.getCategories().size());
						}

						if(data.getPhotos() != null && data.getPhotos().size() > 0) {

							q.deleteTable("photos");
							for(Photo photo : data.getPhotos()) {
								q.insertPhoto(photo);
							}
						}

						if(data.getStores() != null && data.getStores().size() > 0) {

							q.deleteTable("stores");
							for(Store store : data.getStores()) {
								q.insertStore(store);
							}
							Log.e("HOME FRAGMENT LOG", "Store count =" + data.getStores().size());
						}

						if(data.getDiscounts() != null && data.getDiscounts().size() > 0) {

							q.deleteTable("discounts");
							for(Discount discount : data.getDiscounts()) {
								q.insertDiscount(discount);
							}
							Log.e("HOME FRAGMENT LOG", "Discount count =" + data.getDiscounts().size());
						}

						if(data.getSettings() != null && data.getSettings().size() > 0) {

							q.deleteTable("settings");
							for(Setting setting : data.getSettings()) {
								q.insertSettings(setting);
							}
							Log.e("HOME FRAGMENT LOG", "Discount count =" + data.getSettings().size());
						}

						if(dataNews.getNews() != null && dataNews.getNews().size() > 0) {

							q.deleteTable("news");
							for(News news : dataNews.getNews()) {
								q.insertNews(news);
							}
							Log.e("HOME FRAGMENT LOG", "Store count =" + dataNews.getNews().size());
						}

					}
					catch(Exception e) {
						e.printStackTrace();
					}
				}

			}
		});
		task.execute();
	
	}
	
	@Override
    public void onDestroyView()  {
        super.onDestroyView();
        if (viewInflate != null) {
            ViewGroup parentViewGroup = (ViewGroup) viewInflate.getParent();
            if (parentViewGroup != null) {
            	
            	//MGSlider slider = (MGSlider) viewInflate.findViewById(R.id.slider);
        		//slider.pauseSliderAnimation();
        		
                parentViewGroup.removeAllViews();
            }
        }
    }
	
	private void showList() {
		
		ListView listView = (ListView) viewInflate.findViewById(R.id.listView);
		listView.setOnItemClickListener(this);
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		ImageView btn1 = (ImageView) viewInflate.findViewById(R.id.button1);
		btn1.setOnClickListener(this);

		ImageView btn2 = (ImageView) viewInflate.findViewById(R.id.button2);
		btn2.setOnClickListener(this);

		ImageView btn3 = (ImageView) viewInflate.findViewById(R.id.button3);
		btn3.setOnClickListener(this);

		ImageView btn4 = (ImageView) viewInflate.findViewById(R.id.button4);
		btn4.setOnClickListener(this);

		ImageView btn5 = (ImageView) viewInflate.findViewById(R.id.button5);
		btn5.setOnClickListener(this);



		/*Button hallBtn = (Button) viewInflate.findViewById(R.id.hallBtn);
		hallBtn.setOnClickListener(this);

		Button flowerBtn = (Button) viewInflate.findViewById(R.id.flowerBtn);
		flowerBtn.setOnClickListener(this);

		Button restaurantBtn = (Button) viewInflate.findViewById(R.id.restaurantBtn);
		restaurantBtn.setOnClickListener(this);

		Button photoBtn = (Button) viewInflate.findViewById(R.id.photoBtn);
		photoBtn.setOnClickListener(this);

		Button beautyBtn = (Button) viewInflate.findViewById(R.id.beautyBtn);
		beautyBtn.setOnClickListener(this);

		Button mesonBtn = (Button) viewInflate.findViewById(R.id.mesonBtn);
		mesonBtn.setOnClickListener(this);

		Button goldBtn = (Button) viewInflate.findViewById(R.id.goldBtn);
		goldBtn.setOnClickListener(this);

		Button giftBtn = (Button) viewInflate.findViewById(R.id.giftBtn);
		giftBtn.setOnClickListener(this);*/

		/*
		MGListAdapter adapter = new MGListAdapter(
				getActivity(), newsList.size(), R.layout.news_entry);
		
		adapter.setOnMGListAdapterAdapterListener(new OnMGListAdapterAdapterListener() {
			
			@Override
			public void OnMGListAdapterAdapterCreated(MGListAdapter adapter, View v,
					int position, ViewGroup viewGroup) {
				// TODO Auto-generated method stub
				
				News news = newsList.get(position);
				
				MGImageView imgViewPhoto = (MGImageView) v.findViewById(R.id.imgViewPhoto);
				imgViewPhoto.setCornerRadius(0.0f);
				imgViewPhoto.setBorderWidth(UIConfig.BORDER_WIDTH);
				imgViewPhoto.setBorderColor(getResources().getColor(UIConfig.THEME_BLACK_COLOR));
				
				if(news.getPhoto_url() != null) {
					MainActivity.getImageLoader().displayImage(news.getPhoto_url(), imgViewPhoto, options);
				}
				else {
					MainActivity.getImageLoader().displayImage(null, imgViewPhoto, options);
				}
				
				imgViewPhoto.setTag(position);
				
				Spanned name = Html.fromHtml(news.getNews_title());
				//Spanned address = Html.fromHtml(news.getNews_content());
				String address = news.getNews_content();
				address = address.replace("&amp;lt;", "<");
				address = address.replace("&amp;gt;", ">");
				
				TextView tvTitle = (TextView) v.findViewById(R.id.tvTitle);
				tvTitle.setText(name);
				
				TextView tvSubtitle = (TextView) v.findViewById(R.id.tvSubtitle);
				tvSubtitle.setText(Html.fromHtml(address));


				/* elyas : farsi date */
				/*Date d = DateTimeHelper.getDateFromTimeStamp(news.getCreated_at());
				String date = Utilities.FarsiDate(d);

				//String date = DateTimeHelper.getStringDateFromTimeStamp(news.getCreated_at(), "MM/dd/yyyy" );
				TextView tvDate = (TextView) v.findViewById(R.id.tvDate);
				tvDate.setText(date);
			}
		});
		listView.setAdapter(adapter); */
	}


	@Override
	public void onItemClick(AdapterView<?> adapterView, View v, int pos, long resId) {
		// TODO Auto-generated method stub
		
		//MGSlider slider = (MGSlider) viewInflate.findViewById(R.id.slider);
		//slider.stopSliderAnimation();
		
		News news = newsList.get(pos);
		//Intent i = new Intent(getActivity(), NewsDetailActivity.class);
		Intent i = new Intent(getActivity(), NewsDetailActivity2.class);
		i.putExtra("news", news);
		getActivity().startActivity(i);
	}
	
	// Create Slider
	private void createSlider() {
		
		if(storeList != null && storeList.size() == 0 && newsList != null && newsList.size() == 0) {
			MGUtilities.showNotifier(this.getActivity(), MainActivity.offsetY, R.string.failed_data);
			return;
		}
		
		final MainActivity main = (MainActivity) getActivity();
		final Queries q = main.getQueries();
		
		//MGSlider slider = (MGSlider) viewInflate.findViewById(R.id.slider);
		//slider.setMaxSliderThumb(storeList.size());
    	MGSliderAdapter adapter = new MGSliderAdapter(
    			R.layout.slider_entry, storeList.size(), storeList.size());
    	
    	adapter.setOnMGSliderAdapterListener(new OnMGSliderAdapterListener() {
			
			@Override
			public void onOnMGSliderAdapterCreated(MGSliderAdapter adapter, View v,
					int position) {
				// TODO Auto-generated method stub
				
				final Store entry = storeList.get(position);
				Photo p = q.getPhotoByStoreId(entry.getStore_id());


				ImageView imageViewSlider = (ImageView) v.findViewById(R.id.imageViewSlider);
				
				if(p != null) {
					MainActivity.getImageLoader().displayImage(p.getPhoto_url(), imageViewSlider, options);
				}
				else {
					imageViewSlider.setImageResource(UIConfig.SLIDER_PLACEHOLDER);
				}
				
				imageViewSlider.setTag(position);
				imageViewSlider.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						/*Intent i = new Intent(getActivity(), DetailActivity.class);
						i.putExtra("store", entry);
						getActivity().startActivity(i);*/
					}
				});
				
				Spanned name = Html.fromHtml(entry.getStore_name());
				//Spanned address = Html.fromHtml(entry.getStore_address());
				String address = entry.getStore_address();
				address = address.replace("&amp;lt;", "<");
				address = address.replace("&amp;gt;", ">");
				
				TextView tvTitle = (TextView) v.findViewById(R.id.tvTitle);
				tvTitle.setText(name);
				
				//TextView tvSubtitle = (TextView) v.findViewById(R.id.tvSubtitle);
				//tvSubtitle.setText(address);
			}
		});
    	
    	/*slider.setOnMGSliderListener(new OnMGSliderListener() {
			
			@Override
			public void onItemThumbSelected(MGSlider slider, ImageView[] buttonPoint,
					ImageView imgView, int pos) { }
			
			@Override
			public void onItemThumbCreated(MGSlider slider, ImageView imgView, int pos) { }
			
			
			@Override
			public void onItemPageScrolled(MGSlider slider, ImageView[] buttonPoint, int pos) { }
			
			@Override
			public void onItemMGSliderToView(MGSlider slider, int pos) { }
			
			@Override
			public void onItemMGSliderViewClick(AdapterView<?> adapterView, View v,
					int pos, long resid) { }

			@Override
			public void onAllItemThumbCreated(MGSlider slider, LinearLayout linearLayout) { }
			
		});
    	
    	slider.setOffscreenPageLimit(storeList.size() - 1);
    	slider.setAdapter(adapter);
    	slider.setActivity(this.getActivity());
    	slider.setSliderAnimation(5000);
    	slider.resumeSliderAnimation();*/
	}
private void onClickNewsFragment(String title){
		Fragment fragment = null;
		FragmentTransaction ft = null;
		this.type=null;
		this.title=null;
		fragment = new NewsFragment();
		Bundle bundle = new Bundle();
		bundle.putString(Constants.KEY_NOTIFY_TITLE, title);
		fragment.setArguments(bundle);
		ft = getFragmentManager().beginTransaction();
		ft.replace(R.id.frame_container, fragment);
		ft.addToBackStack(null);
		ft.commit();
	}
	private void onClickDiscountFragment(String title){
		Fragment fragment = null;
		FragmentTransaction ft = null;
		this.type=null;
		this.title=null;
		fragment = new DiscountFragment();
		Bundle bundle = new Bundle();
		bundle.putString(Constants.KEY_NOTIFY_TITLE, title);
		fragment.setArguments(bundle);
		ft = getFragmentManager().beginTransaction();
		ft.replace(R.id.frame_container, fragment);
		ft.addToBackStack(null);
		ft.commit();
	}



	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
		//MGSlider slider = (MGSlider) viewInflate.findViewById(R.id.slider);
		//slider.stopSliderAnimation();

		MainActivity main = (MainActivity) this.getActivity();
		final Queries q = main.getQueries();
		ArrayList<Category> categoryList;
		categoryList = q.getCategories();
		Intent i = new Intent(getActivity(), StoreActivity.class);
		Category category;

		Fragment fragment = null;
		FragmentTransaction ft = null;

		switch(v.getId()){
			case R.id.button1:
				fragment = new NewsFragment();
				ft = getFragmentManager().beginTransaction();
				ft.replace(R.id.frame_container, fragment);
				ft.addToBackStack(null);
				ft.commit();
				break;
			case R.id.button2:
				fragment = new DiscountFragment();
				ft = getFragmentManager().beginTransaction();
				ft.replace(R.id.frame_container, fragment);
				ft.addToBackStack(null);
				ft.commit();
				break;
			case R.id.button3:
				fragment = new AboutUsFragment1();
				ft = getFragmentManager().beginTransaction();
				ft.replace(R.id.frame_container, fragment);
				ft.addToBackStack(null);
				ft.commit();
				break;
			case R.id.button4:
				fragment = new FavoriteDiscounts();
				ft = getFragmentManager().beginTransaction();
				ft.replace(R.id.frame_container, fragment);
				ft.addToBackStack(null);
				ft.commit();
				break;
			case R.id.button5:
				fragment = new AboutUsFragment();
				ft = getFragmentManager().beginTransaction();
				ft.replace(R.id.frame_container, fragment);
				ft.addToBackStack(null);
				ft.commit();
				break;
		}




		/*switch(v.getId()) {
			case R.id.hallBtn:
				category = categoryList.get(0);
				i.putExtra("category", category);
				getActivity().startActivity(i);
				break;
			case R.id.flowerBtn:
				category = categoryList.get(1);
				i.putExtra("category", category);
				getActivity().startActivity(i);
				break;
			case R.id.restaurantBtn:
				category = categoryList.get(2);
				i.putExtra("category", category);
				getActivity().startActivity(i);
				break;
			case R.id.photoBtn:
				category = categoryList.get(3);
				i.putExtra("category", category);
				getActivity().startActivity(i);
				break;
			case R.id.beautyBtn:
				category = categoryList.get(4);
				i.putExtra("category", category);
				getActivity().startActivity(i);
				break;
			case R.id.mesonBtn:
				category = categoryList.get(5);
				i.putExtra("category", category);
				getActivity().startActivity(i);
				break;
			case R.id.goldBtn:
				category = categoryList.get(6);
				i.putExtra("category", category);
				getActivity().startActivity(i);
				break;
			case R.id.giftBtn:
				category = categoryList.get(7);
				i.putExtra("category", category);
				getActivity().startActivity(i);
				break;
		}*/
	}

	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		/*MGSlider slider = (MGSlider) viewInflate.findViewById(R.id.slider);
		slider.resumeSliderAnimation();*/
	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		/*MGSlider slider = (MGSlider) viewInflate.findViewById(R.id.slider);
		slider.pauseSliderAnimation();*/
	}
	
}
