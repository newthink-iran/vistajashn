package com.fragments;

import android.app.FragmentManager;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.config.Config;
import com.db.DbHelper;
import com.db.Queries;
import com.models.Setting;
import com.projects.edalatshop.MainActivity;
import com.projects.edalatshop.R;
import com.utilities.MGUtilities;

import java.util.ArrayList;

public class AboutUsFragment2 extends Fragment implements OnClickListener{

	private View viewInflate;

	private Queries q;
	private SQLiteDatabase db;

	//setting in json
	private ArrayList<Setting> settings;
	//end of setting in json

	public AboutUsFragment2() { }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		viewInflate = inflater.inflate(R.layout.fragment_about_us2, null);
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
					if (fm.getBackStackEntryCount() > 0) {
						getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
						return true;
					}
					return false;
				} else {
					return false;
				}
			}
		});

		
		Button btnContactUs = (Button) viewInflate.findViewById(R.id.btnContactUs);
		btnContactUs.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				email();
			}
		});

		MainActivity main = (MainActivity) this.getActivity();
		DbHelper dbHelper = new DbHelper(main);
		q = new Queries(db, dbHelper);

		//setting in json
		settings = q.getSettings();
		//end of setting in json

		String about_content = settings.get(0).getAbout2();
		about_content =  about_content.replace("&lt;", "<");
		about_content =  about_content.replace("&gt;", ">");

		TextView textView2 = (TextView) viewInflate.findViewById(R.id.textView2);
		textView2.setText(Html.fromHtml(about_content));
	}

	@Override
	public void onClick(View v) { }
	
	private void email() {
		
		Intent emailIntent = new Intent(Intent.ACTION_SEND);
		emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{ Config.ABOUT_US_EMAIL } );	
		
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, 
				MGUtilities.getStringFromResource(getActivity(), R.string.email_subject_company) );
		
		emailIntent.putExtra(Intent.EXTRA_TEXT, 
				MGUtilities.getStringFromResource(getActivity(), R.string.email_body_company) );
		emailIntent.setType("message/rfc822");
		
		getActivity().startActivity(Intent.createChooser(emailIntent, 
				MGUtilities.getStringFromResource(getActivity(), R.string.choose_email_client)) );
	}
}
