package com.dzf.service.channel.report.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dzf.dao.bs.SingleObjectBO;
import com.dzf.dao.jdbc.framework.SQLParameter;
import com.dzf.dao.jdbc.framework.processor.BeanListProcessor;
import com.dzf.model.channel.report.ManagerVO;
import com.dzf.model.sys.sys_power.CorpVO;
import com.dzf.model.sys.sys_power.UserVO;
import com.dzf.pub.DZFWarpException;
import com.dzf.pub.IDefaultValue;
import com.dzf.pub.StringUtil;
import com.dzf.pub.cache.CorpCache;
import com.dzf.pub.cache.UserCache;
import com.dzf.pub.lang.DZFDouble;
import com.dzf.pub.util.SqlUtil;
import com.dzf.service.channel.report.IManagerService;

@Service("mana_manager")
public class ManagerServiceImpl implements IManagerService {
	
	@Autowired
	private SingleObjectBO singleObjectBO = null;
	
	@Override
	public List<ManagerVO> query(ManagerVO qvo,Integer type) throws DZFWarpException {
		ArrayList<ManagerVO> list=new ArrayList<ManagerVO>();
		if(type==3){//渠道经理
			if(!checkIsLeader(qvo)){
				return null;
			}
		}
		List<String> vprovinces=new ArrayList<>();
		List<ManagerVO> qryCharge= qryCharge(qvo,type);			//查询  是  省/市负责人相关的数据
		LinkedHashMap<String, ManagerVO> map=new LinkedHashMap<>();
		for (ManagerVO managerVO : qryCharge) {
			Boolean flg=false;//判断查询框的渠道经理的过滤
			if(StringUtil.isEmpty(qvo.getCuserid()) || qvo.getCuserid().equals(managerVO.getCuserid())){
				flg=true;
			}
			if(!map.containsKey(managerVO.getPk_corp()) && flg){
				map.put(managerVO.getPk_corp(), managerVO);
			}
			if(!vprovinces.contains(String.valueOf(managerVO.getVprovince()))){
				vprovinces.add(managerVO.getVprovince().toString());
			}else{
				if(!StringUtil.isEmpty(managerVO.getCuserid())){
					map.put(managerVO.getPk_corp(),managerVO);
				}
			}
		}
		List<ManagerVO> qryNotCharge = qryNotCharge(qvo,type,vprovinces);//查询  非  省/市负责人相关的数据
		for (ManagerVO managerVO : qryNotCharge) {
			Boolean flg=false;//判断查询框的渠道经理的过滤
			if(StringUtil.isEmpty(qvo.getCuserid()) || qvo.getCuserid().equals(managerVO.getCuserid())){
				flg=true;
			}
			if(!map.containsKey(managerVO.getPk_corp()) && flg){
				map.put(managerVO.getPk_corp(), managerVO);
			}else{
				if(!StringUtil.isEmpty(managerVO.getCuserid())){
					map.put(managerVO.getPk_corp(),managerVO);
				}
			}
		}
		if(!map.isEmpty()){
			list=new ArrayList<>(map.values());
			Collections.sort(list, new Comparator<ManagerVO>() {
				@Override
				public int compare(ManagerVO o1, ManagerVO o2) {
					return -o1.getInnercode().compareTo(o2.getInnercode());
				}
			});
		}
		if(list!=null && list.size()>0){
			list=queryCommon(qvo,list);
		}
		return list;
	}
	
	private List<ManagerVO> qryCharge(ManagerVO qvo,Integer type) {
		StringBuffer sql = new StringBuffer();
		SQLParameter sp = new SQLParameter();
		sql.append("select p.pk_corp ,a.areaname,a.userid ,b.vprovname,b.vprovince,p.innercode,");
		sql.append(" (case when b.pk_corp is null then null when b.pk_corp!=p.pk_corp then null else b.userid end) cuserid ");
		sql.append(" from bd_corp p right join cn_chnarea_b b on  p.vprovince=b.vprovince  " );   
		sql.append(" left join cn_chnarea a on b.pk_chnarea=a.pk_chnarea " );   
		sql.append(" where nvl(b.dr,0)=0 and nvl(p.dr,0)=0 and nvl(a.dr,0)=0 and b.type=1" );
	    sql.append(" and nvl(p.ischannel,'N')='Y' and nvl(p.isaccountcorp,'N') = 'Y' and p.fathercorp = ? " );
	    sql.append(" and nvl(b.ischarge,'N')='Y' " );
	    sp.addParam(IDefaultValue.DefaultGroup);
	    if(type==1){
	    	sql.append("  and b.userid=? ");
	    	sp.addParam(qvo.getUserid());
	    }
	    if (type == 2) {// 区域总经理
			sql.append(" and a.userid=? ");
			sp.addParam(qvo.getUserid());
		}
		if (!StringUtil.isEmpty(qvo.getAreaname())) {
			sql.append(" and a.areaname=? "); // 大区
			sp.addParam(qvo.getAreaname());
		}
		if (qvo.getVprovince() != null && qvo.getVprovince() != -1) {
			sql.append(" and b.vprovince=? ");// 省市
			sp.addParam(qvo.getVprovince());
		}
		if (!StringUtil.isEmpty(qvo.getCuserid())) {
			sql.append(" and b.userid=? ");// 渠道经理
			sp.addParam(qvo.getCuserid());
		}
	    List<ManagerVO> list =(List<ManagerVO>) singleObjectBO.executeQuery(sql.toString(), sp,new BeanListProcessor(ManagerVO.class));
	    return list;
	}

	private List<ManagerVO> qryNotCharge(ManagerVO qvo,Integer type,List<String> vprovinces) {
		StringBuffer sql = new StringBuffer();
		SQLParameter sp=new SQLParameter();
		sql.append("select p.pk_corp ,a.areaname,a.userid,b.userid cuserid,b.vprovname,b.vprovince,p.innercode");
		sql.append(" from bd_corp p right join cn_chnarea_b b on  p.pk_corp=b.pk_corp " );   
		sql.append(" left join cn_chnarea a on b.pk_chnarea=a.pk_chnarea " );   
		sql.append(" where nvl(b.dr,0)=0 and nvl(p.dr,0)=0 and nvl(a.dr,0)=0 and b.type=1" );
	    sql.append(" and nvl(p.ischannel,'N')='Y' and nvl(p.isaccountcorp,'N') = 'Y' and p.fathercorp = ? " );
	    sql.append(" and nvl(b.ischarge,'N')='N' " );
	    sp.addParam(IDefaultValue.DefaultGroup);
	    if(type==1){
	    	if(vprovinces!=null && vprovinces.size()>0){
				sql.append("  and (b.userid=? or ");
				sql.append(SqlUtil.buildSqlForIn("b.vprovince",vprovinces.toArray(new String[vprovinces.size()])));
				sql.append(" )");
			}else{
				sql.append("  and b.userid=? ");
			}
	    	sp.addParam(qvo.getUserid());
	    }
	    if (type == 2) {// 区域总经理
	    	if(vprovinces!=null && vprovinces.size()>0){
				sql.append("  and (a.userid=? or ");
				sql.append(SqlUtil.buildSqlForIn("b.vprovince",vprovinces.toArray(new String[vprovinces.size()])));
				sql.append(" )");
			}else{
				sql.append(" and a.userid=? ");
			}
			sp.addParam(qvo.getUserid());
		}
		if (!StringUtil.isEmpty(qvo.getAreaname())) {
			sql.append(" and a.areaname=? "); // 大区
			sp.addParam(qvo.getAreaname());
		}
		if (qvo.getVprovince() != null && qvo.getVprovince() != -1) {
			sql.append(" and b.vprovince=? ");// 省市
			sp.addParam(qvo.getVprovince());
		}
		if (!StringUtil.isEmpty(qvo.getCuserid())) {
			sql.append(" and b.userid=? ");// 渠道经理
			sp.addParam(qvo.getCuserid());
		}
	    List<ManagerVO> vos =(List<ManagerVO>) singleObjectBO.executeQuery(sql.toString(), sp,new BeanListProcessor(ManagerVO.class));
		return vos;
	}

	private ArrayList<ManagerVO> queryCommon(ManagerVO qvo,List<ManagerVO> vos) {
		CorpVO cvo = null;
		UserVO uvo = null;
		Map<String, ManagerVO> map = new HashMap<String, ManagerVO>();
		ArrayList<String> pk_corps =new ArrayList<>();
		for (ManagerVO managerVO : vos) {
			cvo = CorpCache.getInstance().get(null, managerVO.getPk_corp());
			if(cvo!=null){
				managerVO.setCorpname(cvo.getUnitname());
			}
			uvo = UserCache.getInstance().get(managerVO.getUserid(), null);
			if(uvo!=null){
				managerVO.setUsername(uvo.getUser_name());
			}
			if(!StringUtil.isEmpty(managerVO.getCuserid())){
				uvo = UserCache.getInstance().get(managerVO.getCuserid(), null);
				if(uvo!=null){
					managerVO.setCusername(uvo.getUser_name());
				}
			}
			managerVO.setNdeductmny(DZFDouble.ZERO_DBL);
			managerVO.setNdedrebamny(DZFDouble.ZERO_DBL);
			managerVO.setNum(0);
			managerVO.setBondmny(DZFDouble.ZERO_DBL);
			managerVO.setNtotalmny(DZFDouble.ZERO_DBL);
			managerVO.setOutmny(DZFDouble.ZERO_DBL);
			managerVO.setPredeposit(DZFDouble.ZERO_DBL);
			if(!StringUtil.isEmpty(qvo.getCorpname())){
				if(cvo.getUnitname().indexOf(qvo.getCorpname())>=0){
					pk_corps.add(managerVO.getPk_corp());
					map.put(managerVO.getPk_corp(), managerVO);
				}
			}else{
				pk_corps.add(managerVO.getPk_corp());
				map.put(managerVO.getPk_corp(), managerVO);
			}
		}
		if(pk_corps!=null&&pk_corps.size()>0){
			String[] pks=pk_corps.toArray(new String[0]);
			StringBuffer buf=new StringBuffer();//保证金
			buf.append(" select npaymny as bondmny,pk_corp from cn_balance where nvl(dr,0) = 0 and ipaytype=1 and ");
			buf.append(SqlUtil.buildSqlForIn("pk_corp ",pks));
			List<ManagerVO> list1 =(List<ManagerVO>)singleObjectBO.executeQuery(buf.toString(), null, new BeanListProcessor(ManagerVO.class));
			
			buf=new StringBuffer();//预存款
			buf.append("  select  sum(d.npaymny) as predeposit,d.pk_corp from cn_detail d left join cn_paybill p on d.pk_bill=p.pk_paybill where p.vstatus=3  and");
			buf.append(SqlUtil.buildSqlForIn("d.pk_corp ",pks));
			buf.append("  and  p.dpaydate>=? and  p.dpaydate<=?  and nvl(d.dr,0)=0 and nvl(p.dr,0)=0 and p.ipaytype=2");
			buf.append("  group by d.pk_corp");
			SQLParameter spm=new SQLParameter();
			spm.addParam(qvo.getDbegindate());
			spm.addParam(qvo.getDenddate());
			List<ManagerVO> list2 =(List<ManagerVO>)singleObjectBO.executeQuery(buf.toString(), spm, new BeanListProcessor(ManagerVO.class));
			
			spm.addParam(qvo.getDbegindate());
			spm.addParam(qvo.getDenddate());
			spm.addParam(qvo.getDbegindate());
			spm.addParam(qvo.getDenddate());
			spm.addParam(qvo.getDbegindate());
			spm.addParam(qvo.getDenddate());
			spm.addParam(qvo.getDbegindate());
			spm.addParam(qvo.getDenddate());
			spm.addParam(qvo.getDbegindate());
			spm.addParam(qvo.getDenddate());
			spm.addParam(qvo.getDbegindate());
			spm.addParam(qvo.getDenddate());
			spm.addParam(qvo.getDbegindate());
			spm.addParam(qvo.getDenddate());
			spm.addParam(qvo.getDbegindate());
			spm.addParam(qvo.getDenddate());
			spm.addParam(qvo.getDbegindate());
			spm.addParam(qvo.getDenddate());
			
			buf=new StringBuffer();//提单量,合同总金额,扣款金额(预付款,返点款)
			buf.append("  select pk_corp,");
			buf.append("  sum(decode((sign(to_date(deductdata,'yyyy-MM-dd')-to_date(?,'yyyy-MM-dd'))*");
			buf.append("  sign(to_date(deductdata,'yyyy-MM-dd')-to_date(?,'yyyy-MM-dd'))),1,0,1))+");
			buf.append("  sum(decode(vdeductstatus,10," );
			buf.append("  decode((sign(to_date(substr(dchangetime,0,10),'yyyy-MM-dd')-to_date(?,'yyyy-MM-dd'))*");
			buf.append("  sign(to_date(substr(dchangetime,0,10),'yyyy-MM-dd')-to_date(?,'yyyy-MM-dd'))),1,0,-1),");
			buf.append("  decode((sign(to_date(substr(dchangetime,0,10),'yyyy-MM-dd')-to_date(?,'yyyy-MM-dd'))*");
			buf.append("  sign(to_date(substr(dchangetime,0,10),'yyyy-MM-dd')-to_date(?,'yyyy-MM-dd'))),1,0,0)");
			buf.append("  ))as num,");
			
			buf.append("  sum(decode((sign(to_date(deductdata,'yyyy-MM-dd')-to_date(?,'yyyy-MM-dd'))*");
			buf.append("  sign(to_date(deductdata,'yyyy-MM-dd')-to_date(?,'yyyy-MM-dd'))),1,0,nvl(ntotalmny,0)-nvl(nbookmny,0)))+");
			buf.append("  sum(decode(vdeductstatus,10," );
			buf.append("  decode((sign(to_date(substr(dchangetime,0,10),'yyyy-MM-dd')-to_date(?,'yyyy-MM-dd'))*");
			buf.append("  sign(to_date(substr(dchangetime,0,10),'yyyy-MM-dd')-to_date(?,'yyyy-MM-dd'))),1,0,nvl(nsubtotalmny,0)+nvl(nbookmny,0)),");
			buf.append("  decode((sign(to_date(substr(dchangetime,0,10),'yyyy-MM-dd')-to_date(?,'yyyy-MM-dd'))*");
			buf.append("  sign(to_date(substr(dchangetime,0,10),'yyyy-MM-dd')-to_date(?,'yyyy-MM-dd'))),1,0,nvl(nsubtotalmny,0))");
			buf.append("  ))as ntotalmny,");
			
			buf.append("  sum(decode((sign(to_date(deductdata,'yyyy-MM-dd')-to_date(?,'yyyy-MM-dd'))*");
			buf.append("  sign(to_date(deductdata,'yyyy-MM-dd')-to_date(?,'yyyy-MM-dd'))),1,0,nvl(ndeductmny,0)))+");
			buf.append("  sum(decode((sign(to_date(substr(dchangetime,0,10),'yyyy-MM-dd')-to_date(?,'yyyy-MM-dd'))*");
			buf.append("  sign(to_date(substr(dchangetime,0,10),'yyyy-MM-dd')-to_date(?,'yyyy-MM-dd'))),1,0,nvl(nsubdeductmny,0)))as ndeductmny,");
			
			buf.append("  sum(decode((sign(to_date(deductdata,'yyyy-MM-dd')-to_date(?,'yyyy-MM-dd'))*");
			buf.append("  sign(to_date(deductdata,'yyyy-MM-dd')-to_date(?,'yyyy-MM-dd'))),1,0,nvl(ndedrebamny,0)))+");
			buf.append("  sum(decode((sign(to_date(substr(dchangetime,0,10),'yyyy-MM-dd')-to_date(?,'yyyy-MM-dd'))*");
			buf.append("  sign(to_date(substr(dchangetime,0,10),'yyyy-MM-dd')-to_date(?,'yyyy-MM-dd'))),1,0,nvl(nsubdedrebamny,0)))as ndedrebamny");
			
			buf.append("  from cn_contract where nvl(isncust,'N')='N' and nvl(dr,0) = 0 and (vdeductstatus=1 or vdeductstatus=9 or vdeductstatus=10) and ");
			buf.append(SqlUtil.buildSqlForIn("pk_corp ",pks));
			buf.append("  group by pk_corp");
			List<ManagerVO> list3 =(List<ManagerVO>)singleObjectBO.executeQuery(buf.toString(), spm, new BeanListProcessor(ManagerVO.class));
			
		    buf=new StringBuffer();//预存款余额
			buf.append(" select (nvl(npaymny,0)-nvl(nusedmny,0)) as outmny,pk_corp from cn_balance where nvl(dr,0) = 0 and ipaytype=2 and ");
			buf.append(SqlUtil.buildSqlForIn("pk_corp ",pks));
			List<ManagerVO> list4 =(List<ManagerVO>)singleObjectBO.executeQuery(buf.toString(), null, new BeanListProcessor(ManagerVO.class));
			
			buf=new StringBuffer();//小规模及一般纳税人的数量
			buf.append(" select count(pk_corp) num,chargedeptname corpname, fathercorp pk_corp from bd_corp  ");
			buf.append(" where nvl(dr,0)=0 and nvl(isseal,'N')='N' and nvl(isaccountcorp,'N')='N' and chargedeptname is not null and ");
			buf.append(SqlUtil.buildSqlForIn("fathercorp ",pks));
			buf.append(" group by fathercorp,chargedeptname  ");
			List<ManagerVO> list5 =(List<ManagerVO>)singleObjectBO.executeQuery(buf.toString(), null, new BeanListProcessor(ManagerVO.class));
			
		     if(list1!=null&&list1.size()>0){//保证金
		    	 for (ManagerVO managerVO : list1) {
					ManagerVO vo = map.get(managerVO.getPk_corp());
					vo.setBondmny(managerVO.getBondmny());
					map.put(managerVO.getPk_corp(),vo);
				}
		     }
		     if(list2!=null&&list2.size()>0){//预存款
		    	 for (ManagerVO managerVO : list2) {
						ManagerVO vo = map.get(managerVO.getPk_corp());
						vo.setPredeposit(managerVO.getPredeposit());
						vo.setOutmny(managerVO.getPredeposit()); //预存款余额金额s
						map.put(managerVO.getPk_corp(),vo);
					}
		     } 
		     if(list3!=null&&list3.size()>0){//提单量, 合同总金额,扣款金额(预付款,返点款)
		    	 for (ManagerVO managerVO : list3) {
						ManagerVO vo = map.get(managerVO.getPk_corp());
						vo.setNum(managerVO.getNum());
						vo.setNtotalmny(managerVO.getNtotalmny());
						vo.setNdeductmny(managerVO.getNdeductmny());
						vo.setNdedrebamny(managerVO.getNdedrebamny());
						map.put(managerVO.getPk_corp(),vo);
					}
		     }
		     if(list4!=null&&list4.size()>0){//预存款余额
		    	 for (ManagerVO managerVO : list4) {
					ManagerVO vo = map.get(managerVO.getPk_corp());
					vo.setOutmny(managerVO.getOutmny());
					map.put(managerVO.getPk_corp(),vo);
				}
		     }
		     if(list5!=null&&list5.size()>0){//小规模及一般纳税人的数量
		    	 for (ManagerVO managerVO : list5) {
					ManagerVO vo = map.get(managerVO.getPk_corp());
					if("小规模纳税人".equals(managerVO.getCorpname())){
						vo.setXgmNum(managerVO.getNum());
					}else{
						vo.setYbrNum(managerVO.getNum());
					}
					map.put(managerVO.getPk_corp(),vo);
				}
		     }
		}
		 Collection<ManagerVO> manas = map.values();
		 ArrayList<ManagerVO> list= new ArrayList<ManagerVO>(manas);
		return list;
	}
	
	private boolean checkIsLeader(ManagerVO qvo) {
		String sql="select vdeptuserid corpname,vcomuserid username,vgroupuserid cusername from cn_leaderset where nvl(dr,0)=0";
		List<ManagerVO> list =(List<ManagerVO>)singleObjectBO.executeQuery(sql, null, new BeanListProcessor(ManagerVO.class));
		if(list!=null&&list.size()>0){
			ManagerVO vo=list.get(0);
			if(qvo.getUserid().equals(vo.getCusername())||qvo.getUserid().equals(vo.getCorpname())||qvo.getUserid().equals(vo.getUsername())){
				return true;
			}
		}
		return false;
	}

	@Override
	public List<ManagerVO> queryDetail(ManagerVO qvo) throws DZFWarpException {//补提单的合同
		StringBuffer sql = new StringBuffer();
		SQLParameter sp=new SQLParameter();
		sp.addParam(qvo.getDbegindate());
		sp.addParam(qvo.getDenddate());
		sp.addParam(qvo.getPk_corp());
		sql.append(" select 1 as num,pk_confrim as pk_corp ,deductdata as denddate, ");
		sql.append(" nvl(ntotalmny,0)-nvl(nbookmny,0) as ntotalmny, " );   
		sql.append(" nvl(ndeductmny,0) as ndeductmny,nvl(ndedrebamny,0) as ndedrebamny from cn_contract " );   
		sql.append(" where nvl(isncust,'N')='N' and nvl(dr,0) = 0 and (vdeductstatus=1 or vdeductstatus=9 or vdeductstatus=10) and " );
		sql.append(" deductdata>=? and deductdata<=? and pk_corp=? " );
		List<ManagerVO> qryYSH =(List<ManagerVO>)singleObjectBO.executeQuery(sql.toString(), sp, new BeanListProcessor(ManagerVO.class));
		
	    sql = new StringBuffer();
	    sql.append(" select 0 as num,pk_confrim as pk_corp,substr(dchangetime,0,10)as denddate, ");
		sql.append(" nvl(nsubtotalmny,0) as ntotalmny,nvl(nsubdeductmny,0) as ndeductmny , " );   
		sql.append(" nvl(nsubdedrebamny,0) as ndedrebamny from cn_contract " );   
		sql.append(" where nvl(isncust,'N')='N' and nvl(dr,0) = 0 and vdeductstatus=9  and" );
		sql.append(" substr(dchangetime,0,10)>=? and substr(dchangetime,0,10)<=? and pk_corp=?" );
		List<ManagerVO> qryYZZ =(List<ManagerVO>)singleObjectBO.executeQuery(sql.toString(), sp, new BeanListProcessor(ManagerVO.class));
		
		sql = new StringBuffer();
	    sql.append(" select -1 as num,pk_confrim as pk_corp,substr(dchangetime,0,10)as denddate, ");
		sql.append(" nvl(nsubtotalmny,0)+nvl(nbookmny,0) as ntotalmny,nvl(nsubdeductmny,0) as ndeductmny , " );   
		sql.append(" nvl(nsubdedrebamny,0) as ndedrebamny from cn_contract " );   
		sql.append(" where nvl(isncust,'N')='N' and nvl(dr,0) = 0  and vdeductstatus=10 and" );
		sql.append(" substr(dchangetime,0,10)>=? and substr(dchangetime,0,10)<=? and pk_corp=?" );
		List<ManagerVO> qryYZF =(List<ManagerVO>)singleObjectBO.executeQuery(sql.toString(), sp, new BeanListProcessor(ManagerVO.class));
			
		ArrayList<ManagerVO> vos=new ArrayList<>();
		if(qryYSH!=null && qryYSH.size()>0){
			vos.addAll(qryYSH);
		}
		if(qryYZZ!=null && qryYZZ.size()>0){
			vos.addAll(qryYZZ);
		}
		if(qryYZF!=null && qryYZF.size()>0){
			vos.addAll(qryYZF);
		}
		Collections.sort(vos, new Comparator<ManagerVO>() {
			@Override
			public int compare(ManagerVO o1, ManagerVO o2) {
				return o1.getDenddate().compareTo(o2.getDenddate());
			}
		});
		return vos;
	}
	
}
