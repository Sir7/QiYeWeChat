package com.alphabet.common.entity;

/**
 * @author ces
 */
public class CheckWorkEntity {

	private static final long serialVersionUID = 1L;
	private String employeeid;
	private String employeename;
	private String deptname;
	private String superior;
	private String checkdate;
	private String checktime;
	private String postion;
	private String empnum;
	
	private String isPhone;
	private String pic;
	private String remark;
	
	public String getPostion() {
		return postion;
	}
	public void setPostion(String postion) {
		this.postion = postion;
	}
	
	public String getUseridS() {
		return useridS;
	}
	public void setUseridS(String useridS) {
		this.useridS = useridS;
	}

	private String useridS;
	private String sensorid;
	private String badgenumber;
	
	public String getEmployeeid() {
		return employeeid;
	}
	public void setEmployeeid(String employeeid) {
		this.employeeid = employeeid;
	}
	public String getEmployeename() {
		return employeename;
	}
	public void setEmployeename(String employeename) {
		this.employeename = employeename;
	}
	public String getDeptname() {
		return deptname;
	}
	public void setDeptname(String deptname) {
		this.deptname = deptname;
	}
	public String getSuperior() {
		return superior;
	}
	public void setSuperior(String superior) {
		this.superior = superior;
	}
	public String getCheckdate() {
		return checkdate;
	}
	public void setCheckdate(String checkdate) {
		this.checkdate = checkdate;
	}
	public String getChecktime() {
		return checktime;
	}
	public void setChecktime(String checktime) {
		this.checktime = checktime;
	}
	public String getEmpnum() {
		return empnum;
	}
	public void setEmpnum(String empnum) {
		this.empnum = empnum;
	}
	public String getSensorid() {
		return sensorid;
	}
	public void setSensorid(String sensorid) {
		this.sensorid = sensorid;
	}
	public String getBadgenumber() {
		return badgenumber;
	}
	public void setBadgenumber(String badgenumber) {
		this.badgenumber = badgenumber;
	}
	public String getIsPhone() {
		return isPhone;
	}
	public void setIsPhone(String isPhone) {
		this.isPhone = isPhone;
	}
	public String getPic() {
		return pic;
	}
	public void setPic(String pic) {
		this.pic = pic;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	
}
