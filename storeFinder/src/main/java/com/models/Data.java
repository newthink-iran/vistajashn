package com.models;

import java.util.ArrayList;

public class Data {
	
	private ArrayList<Store> stores;
	private ArrayList<Category> categories;
	private ArrayList<Photo> photos;
	private ArrayList<Discount> discounts;

	public void setStores(ArrayList<Store> s) {
	    stores = s;
	}
	
	public ArrayList<Store> getStores() {
	    return stores;
	}
	
	public void setCategories(ArrayList<Category> s) {
		categories = s;
	}
	
	public ArrayList<Category> getCategories() {
	    return categories;
	}
	
	
	public void setPhotos(ArrayList<Photo> s) {
	    photos = s;
	}
	
	public ArrayList<Photo> getPhotos() {
	    return photos;
	}

	public void setDiscounts(ArrayList<Discount> s) {
		discounts = s;
	}

	public ArrayList<Discount> getDiscounts() {
		return discounts;
	}
}
