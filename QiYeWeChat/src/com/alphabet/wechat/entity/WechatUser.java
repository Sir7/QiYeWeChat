package com.alphabet.wechat.entity;

import java.util.List;
/**
 * 企业微信 用户VO
 */
public class WechatUser {
	
	private String userid;		//成员UserID。对应管理端的帐号，企业内必须唯�?
	private String name;		//成员名称
	private String english_name;//英文�?
	private String mobile;		//手机号码。企业内必须唯一
	private List<Integer> department;//成员�?属部门id列表,不超�?20�?
	//private List<Integer> order;//如果有的话，数量必须和department�?致，数�?�越大排序越前面
	private String position;	//职位信息
	private String gender;		//性别�?1表示男�?�，2表示女�??
	private String email;		//邮箱
	private String telephone;	//邮箱
	private Integer isleader;	//标识是否为上�?
	private String avatar_mediaid;//成员头像的mediaid
	private Object extattr;		//扩展属�??
	private Integer hide_mobile;//隐藏手机号�??1表示隐藏手机号，0表示不隐藏手机号
	
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEnglish_name() {
		return english_name;
	}
	public void setEnglish_name(String english_name) {
		this.english_name = english_name;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public List<Integer> getDepartment() {
		return department;
	}
	public void setDepartment(List<Integer> department) {
		this.department = department;
	}
//	public List<Integer> getOrder() {
//		return order;
//	}
//	public void setOrder(List<Integer> order) {
//		this.order = order;
//	}
	public String getPosition() {
		return position;
	}
	public void setPosition(String position) {
		this.position = position;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public Integer getIsleader() {
		return isleader;
	}
	public void setIsleader(Integer isleader) {
		this.isleader = isleader;
	}
	public String getAvatar_mediaid() {
		return avatar_mediaid;
	}
	public void setAvatar_mediaid(String avatar_mediaid) {
		this.avatar_mediaid = avatar_mediaid;
	}
	public String getTelephone() {
		return telephone;
	}
	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}
	public Object getExtattr() {
		return extattr;
	}
	public void setExtattr(Object extattr) {
		this.extattr = extattr;
	}
	public Integer getHide_mobile() {
		return hide_mobile;
	}
	public void setHide_mobile(Integer hide_mobile) {
		this.hide_mobile = hide_mobile;
	}
	
}
