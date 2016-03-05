package com.config;

import com.models.Menu;
import com.models.Menu.HeaderType;
import com.projects.arzansara.R;


public class UIConfig {

	public static int[] ITEM_ENTRIES = {R.string.manual_login, R.string.social_login};
	public static int THEME_BLACK_COLOR = R.color.theme_black_color;
	
	public static float BORDER_WIDTH = 5.0f;
	
	public static Menu MENU_HOME = new Menu(R.string.home, R.drawable.ic_home, R.drawable.ic_home, HeaderType.HeaderType_CATEGORY);
	public static Menu MENU_CATEGORIES = new Menu(R.string.categories, R.drawable.ic_categories, R.drawable.ic_categories, HeaderType.HeaderType_CATEGORY);
	public static Menu MENU_STARRED = new Menu(R.string.starred, R.drawable.ic_starred, R.drawable.ic_starred, HeaderType.HeaderType_CATEGORY);
	public static Menu MENU_FEATURED = new Menu(R.string.featured, R.drawable.ic_featured, R.drawable.ic_featured, HeaderType.HeaderType_CATEGORY);
	public static Menu MENU_MAP = new Menu(R.string.map, R.drawable.ic_map, R.drawable.ic_map, HeaderType.HeaderType_CATEGORY);
	public static Menu MENU_SEARCH = new Menu(R.string.search, R.drawable.ic_search, R.drawable.ic_search, HeaderType.HeaderType_CATEGORY);
	public static Menu MENU_NEWS = new Menu(R.string.news, R.drawable.ic_news, R.drawable.ic_news, HeaderType.HeaderType_CATEGORY);
	public static Menu MENU_DISCOUNTS = new Menu(R.string.discounts, R.drawable.ic_news, R.drawable.ic_news, HeaderType.HeaderType_CATEGORY);
	public static Menu MENU_WEATHER = new Menu(R.string.weather, R.drawable.ic_weather, R.drawable.ic_weather, HeaderType.HeaderType_CATEGORY);
	public static Menu MENU_ABOUT_US = new Menu(R.string.about_us, R.drawable.ic_about, R.drawable.ic_about, HeaderType.HeaderType_CATEGORY);
	public static Menu MENU_ABOUT_US1 = new Menu(R.string.about_us1, R.drawable.ic_about, R.drawable.ic_about, HeaderType.HeaderType_CATEGORY);
	public static Menu MENU_ABOUT_US2 = new Menu(R.string.about_us2, R.drawable.ic_about, R.drawable.ic_about, HeaderType.HeaderType_CATEGORY);
	public static Menu MENU_ABOUT_US3 = new Menu(R.string.about_us3, R.drawable.ic_about, R.drawable.ic_about, HeaderType.HeaderType_CATEGORY);
	public static Menu MENU_TERMS_CONDITION = new Menu(R.string.terms_condition, R.drawable.ic_tc, R.drawable.ic_tc, HeaderType.HeaderType_CATEGORY);
	public static Menu MENU_ANIMATION = new Menu(R.string.animation, R.drawable.ic_settings, R.drawable.ic_settings, HeaderType.HeaderType_CATEGORY);
	public static Menu MENU_REGISTER = new Menu(R.string.register, R.drawable.ic_register, R.drawable.ic_register, HeaderType.HeaderType_CATEGORY);
	public static Menu MENU_LOGIN = new Menu(R.string.login, R.drawable.ic_login, R.drawable.ic_login, HeaderType.HeaderType_CATEGORY);
	public static Menu MENU_EXIT = new Menu(R.string.exit, R.drawable.ic_login, R.drawable.ic_login, HeaderType.HeaderType_CATEGORY);
	
	public static Menu MENU_PROFILE = new Menu(R.string.profile, R.drawable.ic_register, R.drawable.ic_register, HeaderType.HeaderType_CATEGORY);
	
	
	public static Menu HEADER_CATEGORIES = new Menu(R.string.header_categories, -1, -1, HeaderType.HeaderType_LABEL);
	public static Menu HEADER_EXTRAS = new Menu(R.string.header_extras, -1,-1, HeaderType.HeaderType_LABEL);
	public static Menu HEADER_USER = new Menu(R.string.header_users, -1, -1, HeaderType.HeaderType_LABEL);
	
	public static Menu[] MENUS_NOT_LOGGED = {
		
	//	HEADER_CATEGORIES,
	//	MENU_HOME,
	//	MENU_NEWS,
	//	MENU_DISCOUNTS,
	//	MENU_ABOUT_US1,
	//	MENU_ABOUT_US2,
	//	MENU_ABOUT_US3,
	//	MENU_CATEGORIES,
	//	MENU_STARRED,
	//	MENU_FEATURED,
	//	MENU_MAP,
	//	MENU_SEARCH,
	//	MENU_NEWS,
	//	MENU_WEATHER,
		
	//	HEADER_EXTRAS,
	//	MENU_ABOUT_US,
	//	MENU_TERMS_CONDITION,
		
	//	HEADER_USER,
	//	MENU_REGISTER,
	//	MENU_LOGIN,
		MENU_EXIT
	};
	
	public static Menu[] MENUS_LOGGED = {
		
	//	HEADER_CATEGORIES,
		MENU_HOME,
		MENU_CATEGORIES,
		MENU_STARRED,
	//	MENU_FEATURED,
	//	MENU_MAP,
		MENU_SEARCH,
		MENU_NEWS,
	//	MENU_WEATHER,
		
	//	HEADER_EXTRAS,
	//	MENU_ABOUT_US,
	//	MENU_TERMS_CONDITION,
		
	//	HEADER_USER,
	//	MENU_PROFILE
	};
	
	public static int SLIDER_PLACEHOLDER = R.drawable.slider_placeholder;
	
	public static int PLACEHOLDER_REVIEW_THUMB = R.drawable.review_thumb_placeholder;

}
