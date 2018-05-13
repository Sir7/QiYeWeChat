package com.alphabet.wechat.entity;
/**
 * 企业微信 部门VO
 */
public class WechatDepartment {
	
	private Integer id;		//部门id
	
	private String name;	//部门名称
	
	private Integer parentid;//父亲部门id。根部门�?1
	
	private Integer order;	//在父部门中的次序值�?�order值大的排序靠前�??

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getParentid() {
		return parentid;
	}

	public void setParentid(Integer parentid) {
		this.parentid = parentid;
	}

	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}
	
}
