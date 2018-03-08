package com.dzf.model.channel.report;

import com.dzf.pub.SuperVO;
import com.dzf.pub.Field.FieldAlias;
import com.dzf.pub.lang.DZFBoolean;
import com.dzf.pub.lang.DZFDate;
import com.dzf.pub.lang.DZFDateTime;
import com.dzf.pub.lang.DZFDouble;

/**
 * 经理VO（渠道，区域，加盟商）
 *
 */
@SuppressWarnings({ "rawtypes", "serial" })
public class ManagerVO extends SuperVO {

	@FieldAlias("bdate")
	private DZFDate dbegindate; // 开始日期

	@FieldAlias("edate")
	private DZFDate denddate; // 结束日期

	@FieldAlias("corpid")
	private String pk_corp; // 渠道商主键

	@FieldAlias("corpnm")
	private String corpname; // 渠道商名称
	
	@FieldAlias("aname")
    private String areaname;//大区名称
    
	@FieldAlias("uid")
    private String userid; // 用户主键（大区总经理）
    
    @FieldAlias("uname")
    private String username; // 用户名称（大区总经理）
    
	@FieldAlias("cuid")
    private String cuserid; // 用户主键（渠道经理）
    
    @FieldAlias("cuname")
    private String cusername; // 用户名称（渠道总经理）
    
	@FieldAlias("provname")
	public String vprovname;// 地区名称
	
	@FieldAlias("ovince")
	public Integer vprovince;// 地区

	@FieldAlias("ntlmny")
	private DZFDouble ntotalmny; // 合同总金额
	
	@FieldAlias("bondmny")
	private DZFDouble bondmny; // 保证金
	
	@FieldAlias("predeposit")
	private DZFDouble predeposit; //预存款
	
	@FieldAlias("ndemny")
	private DZFDouble ndeductmny; // 扣款金额-预付款
	
	@FieldAlias("nderebmny")
	private DZFDouble ndedrebamny;//扣款金额-返点款
	
	@FieldAlias("outmny")
	private DZFDouble outmny; // 预存款余额金额

	@FieldAlias("num")
	private Integer num;// 提单量
	
	private String innercode;//排序用的编码

	public String getInnercode() {
		return innercode;
	}

	public void setInnercode(String innercode) {
		this.innercode = innercode;
	}

	public DZFDate getDbegindate() {
		return dbegindate;
	}

	public void setDbegindate(DZFDate dbegindate) {
		this.dbegindate = dbegindate;
	}

	public DZFDate getDenddate() {
		return denddate;
	}

	public void setDenddate(DZFDate denddate) {
		this.denddate = denddate;
	}

	public DZFDouble getNdeductmny() {
		return ndeductmny;
	}

	public void setNdeductmny(DZFDouble ndeductmny) {
		this.ndeductmny = ndeductmny;
	}

	public String getPk_corp() {
		return pk_corp;
	}

	public void setPk_corp(String pk_corp) {
		this.pk_corp = pk_corp;
	}

	public DZFDouble getNdedrebamny() {
		return ndedrebamny;
	}

	public void setNdedrebamny(DZFDouble ndedrebamny) {
		this.ndedrebamny = ndedrebamny;
	}

	public Integer getVprovince() {
		return vprovince;
	}

	public void setVprovince(Integer vprovince) {
		this.vprovince = vprovince;
	}

	public String getCorpname() {
		return corpname;
	}

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

	public void setVprovname(String vprovname) {
		this.vprovname = vprovname;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getCuserid() {
		return cuserid;
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

	public String getVprovname() {
		return vprovname;
	}

	public void setVprovnames(String vprovname) {
		this.vprovname = vprovname;
	}

	public void setCorpname(String corpname) {
		this.corpname = corpname;
	}

	public DZFDouble getNtotalmny() {
		return ntotalmny;
	}

	public void setNtotalmny(DZFDouble ntotalmny) {
		this.ntotalmny = ntotalmny;
	}

	public DZFDouble getBondmny() {
		return bondmny;
	}

	public void setBondmny(DZFDouble bondmny) {
		this.bondmny = bondmny;
	}

	public DZFDouble getPredeposit() {
		return predeposit;
	}

	public void setPredeposit(DZFDouble predeposit) {
		this.predeposit = predeposit;
	}

	public DZFDouble getOutmny() {
		return outmny;
	}

	public void setOutmny(DZFDouble outmny) {
		this.outmny = outmny;
	}

	public Integer getNum() {
		return num;
	}

	public void setNum(Integer num) {
		this.num = num;
	}

	@Override
	public String getPKFieldName() {
		return null;
	}

	@Override
	public String getParentPKFieldName() {
		return null;
	}

	@Override
	public String getTableName() {
		return null;
	}

}
