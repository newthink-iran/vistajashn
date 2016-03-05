package com.fragments;

import android.annotation.SuppressLint;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.adapters.MGListAnimatedAdapter;
import com.adapters.MGListAnimatedAdapter.OnMGListAnimatedAdapter;
import com.config.Config;
import com.config.UIConfig;
import com.db.Queries;
import com.fragments.activity.DetailActivity;
import com.fragments.activity.DiscountsActivity;
import com.fragments.activity.NewsDetailActivity2;
import com.fragments.activity.Utilities;
import com.helpers.DateTimeHelper;
import com.imageview.MGImageView;
import com.models.Discount;
import com.models.News;
import com.models.Photo;
import com.models.Store;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.projects.arzansara.MainActivity;
import com.projects.arzansara.R;
import com.utilities.MGUtilities;

import java.util.ArrayList;
import java.util.Date;

public class FavoriteDiscounts extends Fragment implements OnItemClickListener, OnClickListener{

    private View viewInflate;
    private ArrayList<Discount> arrayData;
    DisplayImageOptions options;

    public FavoriteDiscounts() { }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        viewInflate = inflater.inflate(R.layout.fragment_category, null);
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
                arrayData = q.getDiscountsFavorites();
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

        final Queries q = main.getQueries();

        ListView listView = (ListView) viewInflate.findViewById(R.id.listView);
        listView.setOnItemClickListener(this);

        MGListAnimatedAdapter adapter = new MGListAnimatedAdapter(
                getActivity(), arrayData.size(), R.layout.discount_entry);

        adapter.setOnMGListAnimatedAdapter(new OnMGListAnimatedAdapter() {

            @SuppressLint("DefaultLocale")
            @Override
            public void OnMGListAnimatedAdapterCreated(MGListAnimatedAdapter adapter,
                                                       View v, int position, ViewGroup viewGroup) {
                // TODO Auto-generated method stub
                final Discount discount = arrayData.get(position);
                Photo p = q.getPhotoByStoreId(discount.getDiscount_id());

                MGImageView imgViewPhoto = (MGImageView) v.findViewById(R.id.imgViewPhoto);
                imgViewPhoto.setCornerRadius(0.0f);
                imgViewPhoto.setBorderWidth(UIConfig.BORDER_WIDTH);
                imgViewPhoto.setBorderColor(getResources().getColor(UIConfig.THEME_BLACK_COLOR));
                imgViewPhoto.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        // TODO Auto-generated method stub
                        Intent i = new Intent(getActivity(), DiscountsActivity.class);
                        i.putExtra("discount", discount);
                        getActivity().startActivity(i);
                    }
                });

                if(discount.getPhoto_url() != null) {
                    MainActivity.getImageLoader().displayImage(discount.getPhoto_url(), imgViewPhoto, options);
                }
                else {
                    imgViewPhoto.setImageResource(UIConfig.SLIDER_PLACEHOLDER);
                }

                imgViewPhoto.setTag(position);

                Spanned name = Html.fromHtml(discount.getDiscount_title());
                //Spanned address = Html.fromHtml(news.getNews_content());
                //String address = discount.getDiscount_content();
               // address = address.replace("&amp;lt;", "<");
                //address = address.replace("&amp;gt;", ">");

                TextView tvTitle = (TextView) v.findViewById(R.id.tvTitle);
                tvTitle.setText(name);

               // TextView tvSubtitle = (TextView) v.findViewById(R.id.tvSubtitle);
                //tvSubtitle.setText(Html.fromHtml(address));

                Spanned discountValue = Html.fromHtml(String.valueOf(discount.getDiscount_val()));

                TextView tvDiscountVal = (TextView) v.findViewById(R.id.tvDiscountVal);
                tvDiscountVal.setText(discountValue + "% تخفیف");

				/* elyas : farsi date */

                int diffDate = discount.getCreated_at() + (discount.getExp_date()*24*60*60);
                Date d = DateTimeHelper.getDateFromTimeStamp(diffDate);
                String date = Utilities.FarsiDate(d);

                //String date = DateTimeHelper.getStringDateFromTimeStamp(news.getCreated_at(), "MM/dd/yyyy" );
                TextView tvDate = (TextView) v.findViewById(R.id.tvDate);
                tvDate.setText("تا تاریخ "+ date);


                // SETTING VALUES
				/*float rating = 0;

				if(store.getRating_total() > 0 && store.getRating_count() > 0)
					rating = store.getRating_total() / store.getRating_count();

				String strRating = String.format("%.2f %s %d %s",
						rating,
						getActivity().getResources().getString(R.string.average_based_on),
						store.getRating_count(),
						getActivity().getResources().getString(R.string.rating));*/

                //RatingBar ratingBar = (RatingBar) v.findViewById(R.id.ratingBar);
                //ratingBar.setRating(rating);

                //TextView tvRatingBarInfo = (TextView) v.findViewById(R.id.tvRatingBarInfo);


				/*if(rating > 0)
					tvRatingBarInfo.setText(strRating);
				else
					tvRatingBarInfo.setText(
							getActivity().getResources().getString(R.string.no_rating));
				*/


				/*ImageView imgViewStarred = (ImageView) v.findViewById(R.id.imgViewStarred);
				imgViewStarred.setVisibility(View.VISIBLE);*/

				/* elyas : farsi date */

                //Date d = DateTimeHelper.getDateFromTimeStamp(news.getCreated_at());
                //String date = Utilities.FarsiDate(d);

                //String date = DateTimeHelper.getStringDateFromTimeStamp(news.getCreated_at(), "MM/dd/yyyy" );
                //TextView tvDate = (TextView) v.findViewById(R.id.tvDate);
                //tvDate.setText(date);

                /*ImageView imgViewFeatured = (ImageView) v.findViewById(R.id.imgViewFeatured);
                imgViewFeatured.setVisibility(View.INVISIBLE);

                if(store.getFeatured() ==1)
                    imgViewFeatured.setVisibility(View.VISIBLE);*/

            }
        });

        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View v, int pos, long resId) { }


    @Override
    public void onClick(View v) { }
}
