package com.alphabet.authsystem.entity;


public class OrgEntity {
	
	private String orgId;//组织ID
	private String orgKey;//组织编码
	private String orgName;//组织名称
	private String parentId;//父节点ID
	private String orgTypeId;//组织类型（单位�?�部门）
	private String orgClassify;//组织分类ID
	private String comments;//备注
	private String budgetunit;//预算单位
	private String showOrder;
	public String getShowOrder() {
		return showOrder;
	}
	public void setShowOrder(String showOrder) {
		this.showOrder = showOrder;
	}
	public String getOrgId() {
		return orgId;
	}
	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}
	public String getOrgKey() {
		return orgKey;
	}
	public void setOrgKey(String orgKey) {
		this.orgKey = orgKey;
	}
	public String getOrgName() {
		return orgName;
	}
	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}
	public String getParentId() {
		return parentId;
	}
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
	public String getOrgTypeId() {
		return orgTypeId;
	}
	public void setOrgTypeId(String orgTypeId) {
		this.orgTypeId = orgTypeId;
	}
	public String getOrgClassify() {
		return orgClassify;
	}
	public void setOrgClassify(String orgClassify) {
		this.orgClassify = orgClassify;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	public String getBudgetunit() {
		return budgetunit;
	}
	public void setBudgetunit(String budgetunit) {
		this.budgetunit = budgetunit;
	}
	
}
