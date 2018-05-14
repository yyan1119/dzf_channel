package com.dzf.service.channel.report.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dzf.dao.bs.SingleObjectBO;
import com.dzf.dao.jdbc.framework.SQLParameter;
import com.dzf.dao.jdbc.framework.processor.BeanListProcessor;
import com.dzf.model.channel.report.AchievementVO;
import com.dzf.model.channel.report.ContQryVO;
import com.dzf.model.channel.report.ManagerVO;
import com.dzf.model.channel.sale.ChnAreaBVO;
import com.dzf.model.pub.CommonUtil;
import com.dzf.model.pub.IStatusConstant;
import com.dzf.model.pub.QryParamVO;
import com.dzf.model.sys.sys_power.AccountVO;
import com.dzf.pub.BusinessException;
import com.dzf.pub.DZFWarpException;
import com.dzf.pub.StringUtil;
import com.dzf.pub.lang.DZFDouble;
import com.dzf.pub.util.SafeCompute;
import com.dzf.pub.util.SqlUtil;
import com.dzf.pub.util.ToolsUtil;
import com.dzf.service.channel.report.IAchievementService;

@Service("achievementser")
public class AchievementServiceImpl implements IAchievementService {
	
	@Autowired
	private SingleObjectBO singleObjectBO;

	@Override
	public AchievementVO queryLine(QryParamVO paramvo) throws DZFWarpException {
		Map<Integer, String> powmap = qryUserPower(paramvo);
		if(powmap != null && !powmap.isEmpty() && !StringUtil.isEmpty(powmap.get(4))){
			return null;
		}
		return queryLineDate(paramvo, powmap);
	}
	
	/**
	 * 查询线状图数据
	 * @param paramvo
	 * @param powmap
	 * @return
	 * @throws DZFWarpException
	 */
	private AchievementVO queryLineDate(QryParamVO paramvo, Map<Integer, String> powmap) throws DZFWarpException {
		if(paramvo.getQrytype() != null && paramvo.getQrytype() == 1){//按照月度查询
			return qryLineMonthData(paramvo, powmap);
		}
		
		return null;
	}
	
	/**
	 * 查询线状图-月数据
	 * @return
	 * @throws DZFWarpException
	 */
	private AchievementVO qryLineMonthData(QryParamVO paramvo, Map<Integer, String> powmap) throws DZFWarpException {
		AchievementVO retvo = new AchievementVO();
		List<DZFDouble> first = new ArrayList<DZFDouble>();
		List<DZFDouble> second = new ArrayList<DZFDouble>();
		List<String> showdate = ToolsUtil.getPeriodsBp(paramvo.getBeginperiod(), paramvo.getEndperiod());
		String preperiod = ToolsUtil.getPreviousMonth(paramvo.getBeginperiod());
		List<String> qrylist = new ArrayList<String>();
		qrylist.add(preperiod);
		qrylist.addAll(showdate);
		if(qrylist != null && qrylist.size() > 0){
			String qrysql = SqlUtil.buildSqlForIn("SUBSTR(t.deductdata, 1, 7)", qrylist.toArray(new String[0]));
			List<ContQryVO> zlist = qryPositiveData(powmap, qrysql);//扣款金额
			List<ContQryVO> flist = qryNegativeData(powmap, qrysql);//退款金额
			Map<String,DZFDouble> kkmap = new HashMap<String,DZFDouble>();
			Map<String,DZFDouble> jemap = new HashMap<String,DZFDouble>();
			for(ContQryVO zvo : zlist){
				kkmap.put(zvo.getVperiod(), zvo.getNdedsummny());
				jemap.put(zvo.getVperiod(), zvo.getNaccountmny());
			}
			for(ContQryVO fvo : flist){
				kkmap.put(fvo.getVperiod(), SafeCompute.add(kkmap.get(fvo.getVperiod()), fvo.getNdedsummny()));
				jemap.put(fvo.getVperiod(), SafeCompute.add(kkmap.get(fvo.getVperiod()), fvo.getNaccountmny()));
			}
			DZFDouble submny = DZFDouble.ZERO_DBL;
			for(int i = 0; i < showdate.size(); i++){
				if(CommonUtil.getDZFDouble(kkmap.get(showdate.get(i))).compareTo(DZFDouble.ZERO_DBL) == 0){
					first.add(DZFDouble.ZERO_DBL);
				}else{
					if(i == 0){
						if(CommonUtil.getDZFDouble(kkmap.get(preperiod)).compareTo(DZFDouble.ZERO_DBL) == 0){
							first.add(new DZFDouble(100.00));
						}
					}else{
						//(当月金额 - 上一月金额) / 上一月金额
						if(CommonUtil.getDZFDouble(kkmap.get(showdate.get(i-1))).compareTo(DZFDouble.ZERO_DBL) == 0){
							first.add(new DZFDouble(100.00));
						}else{
							submny = SafeCompute.sub(kkmap.get(showdate.get(i)), kkmap.get(showdate.get(i-1)));
							first.add(submny.div(kkmap.get(showdate.get(i-1))).multiply(100));
						}
					}
				}
				
				if(CommonUtil.getDZFDouble(jemap.get(showdate.get(i))).compareTo(DZFDouble.ZERO_DBL) == 0){
					second.add(DZFDouble.ZERO_DBL);
				}else{
					if(i == 0){
						if(CommonUtil.getDZFDouble(jemap.get(preperiod)).compareTo(DZFDouble.ZERO_DBL) == 0){
							second.add(new DZFDouble(100.00));
						}
					}else{
						//(当月金额 - 上一月金额) / 上一月金额
						if(CommonUtil.getDZFDouble(jemap.get(showdate.get(i-1))).compareTo(DZFDouble.ZERO_DBL) == 0){
							second.add(new DZFDouble(100.00));
						}else{
							submny = SafeCompute.sub(jemap.get(showdate.get(i)), jemap.get(showdate.get(i-1)));
							second.add(submny.div(jemap.get(showdate.get(i-1))).multiply(100));
						}
					}
				}
			}
			retvo.setShowdate(showdate);
			retvo.setFirst(first);
			retvo.setSecond(second);
		}else{
			throw new BusinessException("月度查询条件不能为空");
		}
		return retvo;
	}
	
	/**
	 * 查询正数据
	 * @param powmap
	 * @param qrysql
	 * @return
	 * @throws DZFWarpException
	 */
	@SuppressWarnings("unchecked")
	private List<ContQryVO> qryPositiveData(Map<Integer, String> powmap, String qrysql) throws DZFWarpException {
		StringBuffer sql = new StringBuffer();
		SQLParameter spm = new SQLParameter();
		sql.append("SELECT SUBSTR(t.deductdata, 1, 7) AS period,  \n");
		sql.append("       SUM(nvl(t.ndedsummny, 0)) AS ndedsummny,  \n");
		sql.append("       SUM(nvl(t.ntotalmny, 0) - nvl(t.nbookmny, 0)) AS naccountmny  \n");
		sql.append("  FROM cn_contract t  \n");
		if (powmap != null && !powmap.isEmpty() && (!StringUtil.isEmpty(powmap.get(2)) || !StringUtil.isEmpty(powmap.get(3)))) {
			sql.append("  LEFT JOIN bd_account acc ON t.pk_corp = acc.pk_corp  \n");
		}
		sql.append(" WHERE nvl(t.dr, 0) = 0  \n");
		if(powmap != null && !powmap.isEmpty() && (!StringUtil.isEmpty(powmap.get(2)) || !StringUtil.isEmpty(powmap.get(3)))){
			sql.append("   AND nvl(acc.dr, 0) = 0  \n");
			if(!StringUtil.isEmpty(powmap.get(2))){
				sql.append(" AND ").append(powmap.get(2));
			}else if(!StringUtil.isEmpty(powmap.get(3))){
				sql.append(" AND ").append(powmap.get(3));
			}
		}
		sql.append("   AND nvl(t.isncust, 'N') = 'N'  \n");
		sql.append("   AND t.vdeductstatus in (?, ?, ?)  \n");
		spm.addParam(IStatusConstant.IDEDUCTSTATUS_1);
		spm.addParam(IStatusConstant.IDEDUCTSTATUS_9);
		spm.addParam(IStatusConstant.IDEDUCTSTATUS_10);
		if(StringUtil.isEmpty(qrysql)){
			sql.append(" AND ").append(qrysql);
		}
		sql.append(" GROUP BY SUBSTR(t.deductdata, 1, 7) \n");
		sql.append(" ORDER BY SUBSTR(t.deductdata, 1, 7) \n");
		return (List<ContQryVO>) singleObjectBO.executeQuery(sql.toString(), spm,
				new BeanListProcessor(ContQryVO.class));
	}
	
	/**
	 * 查询赋数据
	 * @param powmap
	 * @param qrylist
	 * @return
	 * @throws DZFWarpException
	 */
	@SuppressWarnings("unchecked")
	private List<ContQryVO> qryNegativeData(Map<Integer, String> powmap, String qrysql) throws DZFWarpException {
		StringBuffer sql = new StringBuffer();
		SQLParameter spm = new SQLParameter();
		sql.append("SELECT SUBSTR(t.dchangetime, 1, 7) AS period,  \n") ;
		sql.append("       SUM(nvl(t.nsubdedsummny, 0)) AS ndedsummny,  \n") ; 
		sql.append("       SUM(nvl(t.nsubtotalmny, 0) - nvl(t.nbookmny, 0)) AS naccountmny  \n") ; 
		sql.append("  FROM cn_contract t  \n") ; 
		if (powmap != null && !powmap.isEmpty() && (!StringUtil.isEmpty(powmap.get(2)) || !StringUtil.isEmpty(powmap.get(3)))) {
			sql.append("  LEFT JOIN bd_account acc ON t.pk_corp = acc.pk_corp  \n");
		}
		sql.append(" WHERE nvl(t.dr, 0) = 0  \n") ; 
		if(powmap != null && !powmap.isEmpty() && (!StringUtil.isEmpty(powmap.get(2)) || !StringUtil.isEmpty(powmap.get(3)))){
			sql.append("   AND nvl(acc.dr, 0) = 0  \n");
			if(!StringUtil.isEmpty(powmap.get(2))){
				sql.append(" AND ").append(powmap.get(2));
			}else if(!StringUtil.isEmpty(powmap.get(3))){
				sql.append(" AND ").append(powmap.get(3));
			}
		}
		sql.append("   AND nvl(t.isncust, 'N') = 'N'  \n") ; 
		sql.append("   AND t.vdeductstatus in (?, ?)  \n") ; 
		spm.addParam(IStatusConstant.IDEDUCTSTATUS_9);
		spm.addParam(IStatusConstant.IDEDUCTSTATUS_10);
		if(StringUtil.isEmpty(qrysql)){
			sql.append(" AND ").append(qrysql);
		}
		sql.append(" GROUP BY SUBSTR(t.dchangetime, 1, 7)  \n") ; 
		sql.append(" ORDER BY SUBSTR(t.dchangetime, 1, 7) \n");
		return (List<ContQryVO>) singleObjectBO.executeQuery(sql.toString(), spm,
				new BeanListProcessor(ContQryVO.class));
	}

	@Override
	public AchievementVO queryChart(QryParamVO paramvo) throws DZFWarpException {
		
		return null;
	}
	
	/**
	 * 查询用户角色权限  
	 * 1、区域总经理；2、区域经理；3、渠道经理或渠道负责人（有可能一个渠道经理是多个地区的负责人，同时是另外多个地区的非负责人）；4、非销售人员；
	 * @param paramvo
	 * @return
	 * @throws DZFWarpException
	 */
	@SuppressWarnings("unchecked")
	private Map<Integer, String> qryUserPower(QryParamVO paramvo) throws DZFWarpException {
		Map<Integer, String> pmap = new HashMap<Integer, String>();
		StringBuffer sql = new StringBuffer();
		SQLParameter spm = new SQLParameter();
		List<String> pklist = new ArrayList<String>();
		// 1、区域总经理；
		sql.append("SELECT t.pk_leaderset  \n");
		sql.append("  FROM cn_leaderset t  \n");
		sql.append(" WHERE nvl(t.dr, 0) = 0  \n");
		sql.append("   AND (t.vdeptuserid = ? OR t.vcomuserid = ? OR t.vgroupuserid = ? ) \n");
		spm.addParam(paramvo.getCuserid());
		spm.addParam(paramvo.getCuserid());
		spm.addParam(paramvo.getCuserid());
		List<ManagerVO> list = (List<ManagerVO>) singleObjectBO.executeQuery(sql.toString(), spm,
				new BeanListProcessor(ManagerVO.class));
		if (list != null && list.size() > 0) {
			pmap.put(1, "区域总经理");
			return pmap;
		}
		// 2、区域经理
		sql = new StringBuffer();
		spm = new SQLParameter();
		sql.append("SELECT b.vprovince  \n");
		sql.append("  FROM cn_chnarea a  \n");
		sql.append("  LEFT JOIN cn_chnarea_b b ON a.pk_chnarea = b.pk_chnarea  \n");
		sql.append(" WHERE nvl(a.dr, 0) = 0  \n");
		sql.append("   AND nvl(b.dr, 0) = 0  \n");
		sql.append("   AND a.userid = ? \n");
		spm.addParam(paramvo.getCuserid());
		List<ChnAreaBVO> plist = (List<ChnAreaBVO>) singleObjectBO.executeQuery(sql.toString(), spm,
				new BeanListProcessor(ChnAreaBVO.class));
		if (plist != null && plist.size() > 0) {
			pklist = new ArrayList<String>();
			for (ChnAreaBVO bvo : plist) {
				if (bvo.getVprovince() != null) {
					pklist.add(String.valueOf(bvo.getVprovince()));
				}
			}
			if (pklist != null && pklist.size() > 0) {
				String prosql = SqlUtil.buildSqlForIn("acc.vprovince", pklist.toArray(new String[0]));
				pmap.put(2, prosql);
				return pmap;
			}
		}
		// 3、渠道经理或渠道负责人
		pklist = new ArrayList<String>();
		sql = new StringBuffer();
		spm = new SQLParameter();
		// 3.1、区域负责人
		sql.append("SELECT t.pk_corp  \n");
		sql.append("  FROM cn_chnarea_b b  \n");
		sql.append("  LEFT JOIN bd_account t ON b.vprovince = t.vprovince  \n");
		sql.append(" WHERE nvl(b.dr, 0) = 0  \n");
		sql.append("   AND nvl(t.dr, 0) = 0  \n");
		sql.append("   AND nvl(t.ischannel, 'N') = 'Y'  \n");
		sql.append("   AND nvl(b.ischarge, 'N') = 'Y'  \n");
		sql.append("   AND b.userid = ? \n");
		spm.addParam(paramvo.getCuserid());
		List<AccountVO> clist = (List<AccountVO>) singleObjectBO.executeQuery(sql.toString(), spm,
				new BeanListProcessor(AccountVO.class));
		if(clist != null && clist.size() > 0){
			for(AccountVO acvo : clist){
				pklist.add(acvo.getPk_corp());
			}
		}
		sql = new StringBuffer();
		spm = new SQLParameter();
		sql.append("SELECT b.pk_corp  \n") ;
		sql.append("  FROM cn_chnarea_b b  \n") ; 
		sql.append(" WHERE nvl(b.dr, 0) = 0  \n") ; 
		sql.append("   AND nvl(b.ischarge, 'N') = 'N'  \n") ; 
		sql.append("   AND b.userid = ? \n");
		spm.addParam(paramvo.getCuserid());
		List<AccountVO> klist = (List<AccountVO>) singleObjectBO.executeQuery(sql.toString(), spm,
				new BeanListProcessor(AccountVO.class));
		if(klist != null && klist.size() > 0){
			for(AccountVO acvo : klist){
				pklist.add(acvo.getPk_corp());
			}
		}
		if(pklist != null && pklist.size() > 0){
			String corpsql = SqlUtil.buildSqlForIn("acc.pk_corp", pklist.toArray(new String[0]));
			pmap.put(3, corpsql);
			return pmap;
		}
		pmap.put(4, "非销售人员");
		return pmap;
	}

}