package com.redhat.fabric8analytics.eclipse.core.data;

/**
 * Stores 3scale data. 
 * 
 * @author ljelinko
 *
 */
public class ThreeScaleData {

	private String prod;
	
	private String stage;
	
	private String userKey;

	public ThreeScaleData() {
		// empty constructor
	}

	public ThreeScaleData(String prod, String stage, String userKey) {
		super();
		this.prod = prod;
		this.stage = stage;
		this.userKey = userKey;
	}

	public void setProd(String prod) {
		this.prod = prod;
	}

	public void setStage(String stage) {
		this.stage = stage;
	}

	public void setUserKey(String userKey) {
		this.userKey = userKey;
	}
	
	public String getProd() {
		return prod;
	}

	public String getStage() {
		return stage;
	}

	public String getUserKey() {
		return userKey;
	}
}
