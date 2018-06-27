package com.dzf.model.channel.report;

import com.dzf.pub.SuperVO;
import com.dzf.pub.Field.FieldAlias;

/**
 * 数据权限vo(目前只是用在数据运营管理报表的三个节点)
 *
 */
@SuppressWarnings({ "rawtypes", "serial" })
public class DataVO extends SuperVO {
	
	@FieldAlias("aname")
    public String areaname;//大区名称
	
	@FieldAlias("uid")
	public String userid; // 用户主键（大区总经理）
    
    @FieldAlias("uname")
    public String username; // 用户名称（大区总经理）
    
	@FieldAlias("provname")
	public String vprovname;// 省市名称
//
//	@FieldAlias("ovince")
//	public Integer vprovince;// 省市
	
	@FieldAlias("cuid")
	public String cuserid; // 用户主键（培训师）
	
    @FieldAlias("cuname")
    public String cusername; // 用户名称（培训师）
    
    @FieldAlias("incode")
    public String innercode;//加盟商编码
	
	@FieldAlias("corpid")
	public String pk_corp;//会计公司主键
	
	@FieldAlias("corpnm")
	public String corpname;//加盟商名称

	public String getAreaname() {
		return areaname;
	}

	public void setAreaname(String areaname) {
		this.areaname = areaname;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getVprovname() {
		return vprovname;
	}

	public void setVprovname(String vprovname) {
		this.vprovname = vprovname;
	}

	public String getCorpname() {
		return corpname;
	}

	public void setCorpname(String corpname) {
		this.corpname = corpname;
	}

	public String getCuserid() {
		return cuserid;
	}

	public String getInnercode() {
		return innercode;
	}

	public void setInnercode(String innercode) {
		this.innercode = innercode;
	}

	public String getPk_corp() {
		return pk_corp;
	}

	public void setPk_corp(String pk_corp) {
		this.pk_corp = pk_corp;
	}

	public void setCuserid(String cuserid) {
		this.cuserid = cuserid;
	}

	public String getCusername() {
		return cusername;
	}

	public void setCusername(String cusername) {
		this.cusername = cusername;
	}

	@Override
	public String getPKFieldName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getParentPKFieldName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTableName() {
		// TODO Auto-generated method stub
		return null;
	}

}