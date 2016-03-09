package com.fragments;

import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.db.DbHelper;
import com.db.Queries;
import com.models.Setting;
import com.projects.edalatshop.MainActivity;
import com.projects.edalatshop.R;
import com.utilities.MGUtilities;

import java.util.ArrayList;

public class AboutUsFragment1 extends Fragment implements OnClickListener{

	private View viewInflate;

	private Queries q;
	private SQLiteDatabase db;

	//setting in json
	private ArrayList<Setting> settings;
	//end of setting in json

	public AboutUsFragment1() { }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		viewInflate = inflater.inflate(R.layout.fragment_about_us1, null);
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
		
		/*Button btnContactUs = (Button) viewInflate.findViewById(R.id.btnContactUs);
		btnContactUs.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				email();
			}
		});*/

		ImageView call_me = (ImageView) viewInflate.findViewById(R.id.imgViewCall);
		call_me.setOnClickListener(this);

		ImageView sms_me = (ImageView) viewInflate.findViewById(R.id.imgViewSMS);
		sms_me.setOnClickListener(this);

		ImageView route_me = (ImageView) viewInflate.findViewById(R.id.imgViewRoute);
		route_me.setOnClickListener(this);

		MainActivity main = (MainActivity) this.getActivity();
		DbHelper dbHelper = new DbHelper(main);
		q = new Queries(db, dbHelper);
		String about_content = "";

		try{
			//setting in json
			settings = q.getSettings();
			//end of setting in json

			about_content = settings.get(0).getAbout1();
			about_content =  about_content.replace("&lt;", "<");
			about_content =  about_content.replace("&gt;", ">");
		}

		catch (Exception e){
		}


		TextView textView2 = (TextView) viewInflate.findViewById(R.id.textView2);
		textView2.setText(Html.fromHtml(about_content));
	}

	@Override
	public void onClick(View v) {

		switch(v.getId()){
			case R.id.imgViewCall:
				call();
				break;

			case R.id.imgViewSMS:
				sms();
				break;

			case R.id.imgViewRoute:
				route();
				break;
		}
	}

	private void call() {

		String phone = "09133074281";

		if( phone == null || phone.length() == 0 ) {
			MGUtilities.showAlertView(
					getActivity(),
					R.string.action_error,
					R.string.cannot_proceed);
			return;
		}

		PackageManager pm = getActivity().getBaseContext().getPackageManager();
		boolean canCall = pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);

		if(!canCall) {
			MGUtilities.showAlertView(
					getActivity(),
					R.string.action_error,
					R.string.cannot_proceed);
			return;
		}

		String phoneNo = phone.replaceAll("[^0-9]", "");

		String uri = "tel:" + phoneNo;
		Intent intent = new Intent(Intent.ACTION_CALL);
		intent.setData(Uri.parse(uri));
		this.startActivity(intent);
	}

	private void route() {

		double lat_val = 32.678958;
		double long_val = 51.604459;

		if(lat_val == 0 || long_val == 0) {
			MGUtilities.showAlertView(
					getActivity(),
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
				lat_val,
				long_val) ;

//		String geo = String.format("http://maps.google.com/maps?f=d&daddr=%s&dirflg=d",
//				store.getStore_address()) ;

		Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(geo));
//		Uri.parse("geo:55.74274,37.56577?q=55.74274,37.56577 (name)"));
		intent.setComponent(new ComponentName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity"));
		this.startActivity(intent);
	}

	private void sms() {

		String sms = "09133074281";

		if( sms == null || sms.length() == 0 ) {
			MGUtilities.showAlertView(
					getActivity(),
					R.string.action_error,
					R.string.handset_not_supported);
			return;
		}

		PackageManager pm = getActivity().getBaseContext().getPackageManager();
		boolean canSMS = pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);

		if(!canSMS) {
			MGUtilities.showAlertView(
					getActivity(),
					R.string.action_error,
					R.string.handset_not_supported);
			return;
		}

		String smsNo = sms.replaceAll("[^0-9]", "");

		Intent smsIntent = new Intent(Intent.ACTION_VIEW);
		smsIntent.setType("vnd.android-dir/mms-sms");
		smsIntent.putExtra("address", smsNo);

		smsIntent.putExtra("sms_body",
				MGUtilities.getStringFromResource(getActivity(), R.string.sms_body));

		this.startActivity(smsIntent);
	}
	
	/*private void email() {
		
		Intent emailIntent = new Intent(Intent.ACTION_SEND);
		emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{ Config.ABOUT_US_EMAIL } );	
		
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, 
				MGUtilities.getStringFromResource(getActivity(), R.string.email_subject_company) );
		
		emailIntent.putExtra(Intent.EXTRA_TEXT, 
				MGUtilities.getStringFromResource(getActivity(), R.string.email_body_company) );
		emailIntent.setType("message/rfc822");
		
		getActivity().startActivity(Intent.createChooser(emailIntent, 
				MGUtilities.getStringFromResource(getActivity(), R.string.choose_email_client)) );
	}*/

}
