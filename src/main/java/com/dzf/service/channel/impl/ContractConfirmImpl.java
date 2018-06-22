package com.dzf.service.channel.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dzf.dao.bs.SingleObjectBO;
import com.dzf.dao.jdbc.framework.SQLParameter;
import com.dzf.dao.jdbc.framework.processor.ArrayListProcessor;
import com.dzf.dao.jdbc.framework.processor.BeanListProcessor;
import com.dzf.dao.multbs.MultBodyObjectBO;
import com.dzf.model.channel.ChnBalanceVO;
import com.dzf.model.channel.ChnDetailVO;
import com.dzf.model.channel.PackageDefVO;
import com.dzf.model.channel.contract.ContractConfrimVO;
import com.dzf.model.channel.contract.RejectHistoryHVO;
import com.dzf.model.channel.contract.RejectHistoryVO;
import com.dzf.model.channel.sale.RejectreasonVO;
import com.dzf.model.demp.contract.ContractDocVO;
import com.dzf.model.pub.CommonUtil;
import com.dzf.model.pub.IStatusConstant;
import com.dzf.model.pub.QryParamVO;
import com.dzf.model.pub.QrySqlSpmVO;
import com.dzf.model.sys.sys_power.CorpVO;
import com.dzf.model.sys.sys_power.UserVO;
import com.dzf.pub.BusinessException;
import com.dzf.pub.DZFWarpException;
import com.dzf.pub.Logger;
import com.dzf.pub.StringUtil;
import com.dzf.pub.cache.CorpCache;
import com.dzf.pub.cache.UserCache;
import com.dzf.pub.image.ImageCommonPath;
import com.dzf.pub.lang.DZFBoolean;
import com.dzf.pub.lang.DZFDate;
import com.dzf.pub.lang.DZFDateTime;
import com.dzf.pub.lang.DZFDouble;
import com.dzf.pub.lock.LockUtil;
import com.dzf.pub.util.SafeCompute;
import com.dzf.pub.util.SqlUtil;
import com.dzf.pub.util.ToolsUtil;
import com.dzf.service.channel.IContractConfirm;
import com.dzf.service.pub.IPubService;
import com.dzf.service.pushappmsg.IPushAppMessage;
import com.dzf.spring.SpringUtils;

@Service("contractconfser")
public class ContractConfirmImpl implements IContractConfirm {
	
	@Autowired
	private SingleObjectBO singleObjectBO;
	
	@Autowired
	private MultBodyObjectBO multBodyObjectBO;
	
	@Autowired
	private IPubService pubser;

	@Override
	public Integer queryTotalRow(QryParamVO paramvo) throws DZFWarpException {
		QrySqlSpmVO sqpvo =  getQryCondition(paramvo, "listqry");
		return multBodyObjectBO.queryDataTotal(ContractConfrimVO.class,sqpvo.getSql(), sqpvo.getSpm());
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ContractConfrimVO> query(QryParamVO paramvo) throws DZFWarpException {
		QrySqlSpmVO sqpvo =  getQryCondition(paramvo, "listqry");
		List<ContractConfrimVO> list = (List<ContractConfrimVO>) multBodyObjectBO.queryDataPage(ContractConfrimVO.class, 
					sqpvo.getSql(), sqpvo.getSpm(), paramvo.getPage(), paramvo.getRows(), null);
		if(list != null && list.size() > 0){
			setShowData(list, "listqry",paramvo.getAreaname());
		}
		return list;
	}
	
	/**
	 * 设置显示内容
	 * @param list
	 * @param showtype   1、listqry：列表查询；2、audit：审核查询；3、info：明细查询；
	 * @throws DZFWarpException
	 */
	private void setShowData(List<ContractConfrimVO> list, String showtype,String areaname) throws DZFWarpException {
		UserVO uservo = null;
		CorpVO corpvo = null;
		String statusname = "";
		String manager = "";
		Map<String,String> marmap = pubser.getManagerMap();//渠道经理
		Map<Integer, String> areaMap = null;//大区集合
		if("listqry".equals(showtype)){
			areaMap = pubser.getAreaMap(areaname,3);
		}
		for(ContractConfrimVO confvo : list){
			confvo.setTstamp(confvo.getCheckts());//校验时间戳，5:待审批；7：已驳回；取原合同，剩余情况取历史合同
			if("listqry".equals(showtype)){
				if(confvo.getVstatus() != null && confvo.getVstatus() == IStatusConstant.IDEDUCTSTATUS_1){//已审核
					//合同代账费 = 合同总金额 - 合同账本费
					confvo.setNaccountmny(SafeCompute.sub(confvo.getNtotalmny(), confvo.getNbookmny()));
					confvo.setNdeductmny(CommonUtil.getDZFDouble(confvo.getNdeductmny()));//预付款扣款
					confvo.setNdedrebamny(CommonUtil.getDZFDouble(confvo.getNdedrebamny()));//返点扣款
				}else if(confvo.getVstatus() != null && confvo.getVstatus() == IStatusConstant.IDEDUCTSTATUS_9){//已终止
//					confvo.setNtotalmny(confvo.getNchangetotalmny());//合同总金额 = 变更后合同总金额
					confvo.setNdedsummny(confvo.getNchangesummny());//扣款总金额 = 变更后扣款总金额
					confvo.setNdeductmny(CommonUtil.getDZFDouble(confvo.getNchangededutmny()));//预付款扣款 = 变更后预付款扣款
					confvo.setNdedrebamny(CommonUtil.getDZFDouble(confvo.getNchangerebatmny()));//返点扣款 = 变更后返点扣款
					//合同代账费 = 变更后合同总金额 - 合同账本费
					confvo.setNaccountmny(SafeCompute.sub(confvo.getNtotalmny(), confvo.getNbookmny()));
				}else if(confvo.getVstatus() != null && confvo.getVstatus() == IStatusConstant.IDEDUCTSTATUS_10){//已作废
					confvo.setNtotalmny(DZFDouble.ZERO_DBL);//合同总金额
					confvo.setNdedsummny(DZFDouble.ZERO_DBL);//扣款总金额
					confvo.setNdeductmny(DZFDouble.ZERO_DBL);//预付款扣款 
					confvo.setNdedrebamny(DZFDouble.ZERO_DBL);//返点扣款
					confvo.setNbookmny(DZFDouble.ZERO_DBL);//合同账本费
					confvo.setNaccountmny(DZFDouble.ZERO_DBL);//合同代账费
				}else{//待提交、已驳回
					//合同代账费 = 合同总金额 - 合同账本费
					confvo.setNaccountmny(SafeCompute.sub(confvo.getNtotalmny(), confvo.getNbookmny()));
				}
				if(areaMap != null && !areaMap.isEmpty()){
					String area = areaMap.get(confvo.getVprovince());
					if(!StringUtil.isEmpty(area)){
						confvo.setAreaname(area);//大区
					}
				}
			}
			uservo = UserCache.getInstance().get(confvo.getVadviser(), null);
			if (uservo != null) {
				confvo.setVadviser(uservo.getUser_name());// 销售顾问
			}
			uservo = UserCache.getInstance().get(confvo.getVoperator(), null);
			if (uservo != null) {
				confvo.setVopername(uservo.getUser_name());// 经办人（审核人）姓名
			}
			corpvo = CorpCache.getInstance().get(null, confvo.getPk_corp());
			if (corpvo != null) {
				confvo.setVarea(corpvo.getCitycounty());//地区
				confvo.setCorpname(corpvo.getUnitname());//加盟商
			}
			corpvo = CorpCache.getInstance().get(null, confvo.getPk_corpk());
			if(corpvo != null){
				confvo.setCorpkname(corpvo.getUnitname());//客户名称
			}
			if(marmap != null && !marmap.isEmpty()){
				manager = marmap.get(confvo.getPk_corp());
				if(!StringUtil.isEmpty(manager)){
					uservo = UserCache.getInstance().get(manager, null);
					if (uservo != null) {
						confvo.setVmanagername(uservo.getUser_name());//渠道经理
					}
				}
			}
			
			if("audit".equals(showtype)){
				confvo.setIscanedit(DZFBoolean.FALSE);
				if(confvo.getChanneltype() != null && confvo.getChanneltype() == 1){
					confvo.setCorptype("普通加盟商");
					if(confvo.getIsncust() != null && confvo.getIsncust().booleanValue()){
						confvo.setIdeductpropor(0);
					}else{
						confvo.setIdeductpropor(10);
					}
				}else if(confvo.getChanneltype() != null && confvo.getChanneltype() == 2){
					confvo.setCorptype("金牌加盟商");
					if(confvo.getIsncust() != null && confvo.getIsncust().booleanValue()){
						confvo.setIdeductpropor(0);
					}else{
						confvo.setIdeductpropor(5);
						confvo.setIscanedit(DZFBoolean.TRUE);
					}
				}else{
					if(confvo.getIsncust() != null && confvo.getIsncust().booleanValue()){
						confvo.setIdeductpropor(0);
					}else{
						confvo.setIdeductpropor(10);
					}
				}
			}
			if("info".equals(showtype)){
				//0：待提交；5:待审批： 1：审核通过； 7：已驳回；8：服务到期；9：已终止；10：已作废；
				switch(confvo.getVdeductstatus()){
				case 0:
					statusname = "待提交";
					break;
				case 5:
					statusname = "待审核";
					break;
				case 1:
					statusname = "已审核";
					break;
				case 7:
					statusname = "已驳回";
					break;
				case 8:
					statusname = "服务到期";
					break;
				case 9:
					statusname = "已终止";
					break;
				case 10:
					statusname = "已作废";
					break;
				}
				confvo.setVstatusname(statusname);
			}
		}
	}
	
	/**
	 * 获取查询条件
	 * @param paramvo
	 * @param qrytype   listqry:列表查询；audit：审核查询；info：详情查询；"":合同变更或其他；
	 * @return
	 * @throws DZFWarpException
	 */
	private QrySqlSpmVO getQryCondition(QryParamVO paramvo, String qrytype) throws DZFWarpException {
		QrySqlSpmVO qryvo = new QrySqlSpmVO();
		StringBuffer sql = new StringBuffer();
		SQLParameter spm = new SQLParameter();
		sql.append("SELECT t.pk_contract,  \n") ;
		sql.append("       t.pk_corp,  \n") ; 
		sql.append("       t.pk_corpk,  \n") ; 
		sql.append("       t.vdeductstatus,  \n") ; 
		sql.append("       t.vstatus,  \n") ; 
		sql.append("       CASE  \n") ; 
		sql.append("             WHEN  t.vstatus = 5 OR t.vstatus = 7 THEN  \n") ; 
		sql.append("              t.tstamp  \n") ; 
		sql.append("             ELSE  \n") ; 
		sql.append("              cn.tstamp  \n") ; 
		sql.append("           END AS checkts,  \n") ;//原合同、历史合同时间戳
		if("audit".equals(qrytype)){
			sql.append("       ba.channeltype,  \n") ; //加盟商类型
		}
		if("listqry".equals(qrytype)){
			sql.append("       ba.vprovince,  \n") ; //省份
		}
		sql.append("       t.vcontcode,  \n") ; 
		sql.append("       t.pk_packagedef,  \n") ; 
		sql.append("       t.isncust,  \n") ; 
		sql.append("       t.isnconfirm,  \n") ; 
		sql.append("       t.chargedeptname,  \n") ; 
		sql.append("       t.busitypemin,  \n") ;//业务小类
		sql.append("       bs.vbusitypename,  \n") ; 
		sql.append("       t.ntotalmny,  \n") ; 
		sql.append("       t.nbookmny,  \n") ; 
		sql.append("       t.nmservicemny,  \n") ; 
		sql.append("       t.icontractcycle,  \n") ; 
		sql.append("       t.ireceivcycle,  \n") ; 
		sql.append("       t.dsubmitime,  \n") ; 
		sql.append("       t.vbeginperiod,  \n") ; 
		sql.append("       t.vendperiod,  \n") ; 
		sql.append("       t.dbegindate,  \n") ; 
		sql.append("       t.denddate,  \n") ; 
		sql.append("       t.vchangeperiod,  \n") ; 
		sql.append("       t.patchstatus,  \n") ; 
		sql.append("       t.vconfreason,  \n") ; 
		sql.append("       t.dsigndate,  \n") ; 
		sql.append("       t.vadviser,  \n") ; 
		sql.append("       cn.pk_confrim,  \n") ; 
		sql.append("       cn.tstamp,  \n") ; 
		sql.append("       cn.deductdata,  \n") ; 
		sql.append("       cn.deductime,  \n") ; 
//		sql.append("       cn.vconmemo,  \n") ; 
		sql.append("       cn.voperator,  \n") ; 
		sql.append("       cn.ndedsummny,  \n") ; 
		sql.append("       cn.ndeductmny,  \n") ; 
		sql.append("       cn.ndedrebamny,  \n") ; 
		sql.append("       cn.ideductpropor,  \n") ; 
		sql.append("       cn.ichangetype,  \n") ; 
		sql.append("       cn.vchangeraeson,  \n") ; 
		sql.append("       cn.vchangememo,  \n") ; 
		sql.append("       cn.vstopperiod,  \n") ; 
		sql.append("       cn.nreturnmny,  \n") ; 
		sql.append("       cn.nretdedmny,  \n") ; 
		sql.append("       cn.nretrebmny,  \n") ; 
		sql.append("       t.nchangetotalmny,  \n") ; //原合同金额
		sql.append("       cn.nchangesummny,  \n") ; 
		sql.append("       cn.nchangededutmny,  \n") ; 
		sql.append("       cn.nchangerebatmny,  \n") ; 
		sql.append("       cn.vchanger,  \n") ; 
		sql.append("       cn.dchangetime,  \n") ; 
		sql.append("       substr(dchangetime, 0, 10) AS dchangedate,  \n") ; 
		sql.append("       cn.nsubtotalmny,  \n") ; 
		sql.append("       cn.nsubdedsummny,  \n") ; 
		sql.append("       cn.nsubdeductmny,  \n") ; 
		sql.append("       cn.nsubdedrebamny  \n") ; 
		
		sql.append("  FROM ynt_contract t  \n") ; 
		sql.append("  LEFT JOIN cn_contract cn ON t.pk_contract = cn.pk_contract  \n") ; 
		sql.append("  LEFT JOIN ynt_busitype bs ON t.busitypemin = bs.pk_busitype  \n") ; 
		if(!"info".equals(qrytype)){
			sql.append("  LEFT JOIN bd_account ba ON t.pk_corp = ba.pk_corp \n") ;
		}
		sql.append(" WHERE nvl(t.dr, 0) = 0  \n") ; 
		sql.append("   AND nvl(cn.dr, 0) = 0  \n") ; 
		sql.append("   AND nvl(bs.dr, 0) = 0  \n") ; 
		if(!"info".equals(qrytype)){
			sql.append("   AND nvl(ba.dr, 0) = 0  \n") ; 
		}
		sql.append("   AND nvl(t.isflag, 'N') = 'Y'  \n") ; 
		sql.append("   AND nvl(t.icosttype, 0) = 0  \n") ; 
		sql.append("   AND t.icontracttype = 2  \n") ; //加盟商合同
		sql.append("   AND nvl(t.vstatus,0) != 0  \n") ; //未提交合同不查询
		if(paramvo.getVdeductstatus() != null && paramvo.getVdeductstatus() != -1){
			if(paramvo.getVdeductstatus() != null && paramvo.getVdeductstatus() == IStatusConstant.IDEDUCTSTATUS_8){
				sql.append(" AND t.vendperiod < ? ");
				DZFDate date = new DZFDate();
				spm.addParam(date.getYear()+"-"+date.getStrMonth());
			}else{
				sql.append(" AND t.vdeductstatus = ? \n") ;
				spm.addParam(paramvo.getVdeductstatus());
			}
		}
		if(paramvo.getBegdate() != null){
			sql.append("   AND t.dsubmitime >= ? \n") ; 
			spm.addParam(paramvo.getBegdate() + " 00:00:00");
		}
		if(paramvo.getEnddate() != null){
			sql.append("   AND t.dsubmitime <= ? \n") ; 
			spm.addParam(paramvo.getEnddate() + " 23:59:59");
		}
		if(!StringUtil.isEmpty(paramvo.getBeginperiod())){
			sql.append("   AND cn.deductdata >= ? \n") ; 
			spm.addParam(paramvo.getBeginperiod());
		}
		if(!StringUtil.isEmpty(paramvo.getEndperiod())){
			sql.append("   AND cn.deductdata <= ? \n") ; 
			spm.addParam(paramvo.getEndperiod());
		}
		if(paramvo.getIsncust() != null){
			sql.append(" AND nvl(t.isncust,'N') = ? \n") ; 
			spm.addParam(paramvo.getIsncust());
		}
		if("info".equals(qrytype) || "audit".equals(qrytype) ){
			if(!StringUtil.isEmpty(paramvo.getPk_bill())){
				sql.append("   AND t.pk_contract = ? \n") ; //原合同合同主键
				spm.addParam(paramvo.getPk_bill());
			}
		}else{
			if(!StringUtil.isEmpty(paramvo.getPk_bill())){
				sql.append("   AND cn.pk_confrim = ? \n") ; //历史合同主键
				spm.addParam(paramvo.getPk_bill());
			}
		}
		if(paramvo.getQrytype() != null && paramvo.getQrytype() == 1){
			sql.append(" AND nvl(t.patchstatus, 0) != 2 \n") ;
		}else if(paramvo.getQrytype() != null && paramvo.getQrytype() == 2){
			sql.append(" AND nvl(t.patchstatus, 0) = 2 \n") ;
		}
		if(paramvo.getCorptype() != null){
			if(paramvo.getCorptype() == 1){
			    sql.append(" AND t.chargedeptname = ? \n") ; 
	            spm.addParam("小规模纳税人");
			}else if(paramvo.getCorptype() == 2){
			    sql.append(" AND t.chargedeptname = ? \n") ; 
	            spm.addParam("一般纳税人");
			}
		}
		if(!StringUtil.isEmpty(paramvo.getPk_corp())){
		    String[] strs = paramvo.getPk_corp().split(",");
		    String inSql = SqlUtil.buildSqlConditionForIn(strs);
		    sql.append(" AND t.pk_corp in (").append(inSql).append(")");
		}
		if(!StringUtil.isEmpty(paramvo.getPk_corpk())){
			sql.append(" AND t.pk_corpk = ? \n") ; 
			spm.addParam(paramvo.getPk_corpk());
		}
		if(!StringUtil.isEmpty(paramvo.getVqrysql())){
			sql.append(paramvo.getVqrysql());
		}
		sql.append(" ORDER BY t.ts desc \n");
		qryvo.setSql(sql.toString());
		qryvo.setSpm(spm);
		return qryvo;
	}
	
	/**
	 * 获取渠道经理查询条件
	 * @param cuserid
	 * @return
	 * @throws DZFWarpException
	 */
	@Override
	public String getQrySql(String cuserid) throws DZFWarpException {
		StringBuffer sql = new StringBuffer();
		String[] corps = pubser.getManagerCorp(cuserid);
		if(corps != null && corps.length > 0){
			String where = SqlUtil.buildSqlForIn(" t.pk_corp", corps);
			sql.append(" AND ").append(where);
		}else{
			sql.append(" AND t.pk_corp is null \n") ; 
		}
		return sql.toString();
	}
	
	/**
	 * 查询套餐属性
	 * @return
	 */
	@Override
	public Map<String, String> queryPackageMap() throws DZFWarpException {
		Map<String, String> map = new HashMap<String, String>();
		String sql = " nvl(dr,0) = 0 ";
		PackageDefVO[] packVOs = (PackageDefVO[]) singleObjectBO.queryByCondition(PackageDefVO.class, sql, null);
		if(packVOs != null && packVOs.length > 0){
			for(PackageDefVO vo : packVOs){
				map.put(vo.getPk_packagedef(), vo.getVtaxpayertype());
			}
		}
		return map;
	}
	
	/**
	 * 通过主键查询合同数据
	 * @param pk_contract
	 * @return
	 * @throws DZFWarpException
	 */
	@SuppressWarnings("unchecked")
	private ContractConfrimVO queryContractById(String pk_contract) throws DZFWarpException{
		QryParamVO paramvo = new QryParamVO();
		paramvo.setPk_bill(pk_contract);
		QrySqlSpmVO qryvo =  getQryCondition(paramvo, "audit");
		List<ContractConfrimVO> list = (List<ContractConfrimVO>) singleObjectBO.executeQuery(qryvo.getSql(),
				qryvo.getSpm(), new BeanListProcessor(ContractConfrimVO.class));
		if (list != null && list.size() > 0) {
			setShowData(list, "audit",null);
			return list.get(0);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ContractConfrimVO queryDebitData(ContractConfrimVO paramvo) throws DZFWarpException {
		//1、查询合同信息：
		ContractConfrimVO retvo = queryContractById(paramvo.getPk_contract());
		//2、查询预付款和返点信息：
		SQLParameter spm = new SQLParameter();
		String sql = " nvl(dr,0) = 0 AND pk_corp = ? AND ipaytype in (?,?) ";//只查询预付款和返点款
		spm.addParam(paramvo.getPk_corp());
		spm.addParam(IStatusConstant.IPAYTYPE_2);
		spm.addParam(IStatusConstant.IPAYTYPE_3);
		ChnBalanceVO[] balVOs = (ChnBalanceVO[]) singleObjectBO.queryByCondition(ChnBalanceVO.class, sql, spm);
		if(balVOs != null && balVOs.length > 0){
			for(ChnBalanceVO balvo : balVOs){
				if(balvo.getIpaytype() != null && balvo.getIpaytype() == IStatusConstant.IPAYTYPE_2){
					retvo.setNbalance(SafeCompute.sub(balvo.getNpaymny(), balvo.getNusedmny()));
				}else if(balvo.getIpaytype() != null && balvo.getIpaytype() == IStatusConstant.IPAYTYPE_3){
					retvo.setNrebatebalance(SafeCompute.sub(balvo.getNpaymny(), balvo.getNusedmny()));
				}
			}
		}
		//3、查询套餐信息：
		PackageDefVO packvo = (PackageDefVO) singleObjectBO.queryByPrimaryKey(PackageDefVO.class, paramvo.getPk_packagedef());
		if(packvo != null){
			if(packvo.getIspromotion() != null && packvo.getIspromotion().booleanValue()){
				Integer publishnum = packvo.getIpublishnum() == null ? 0 : packvo.getIpublishnum();
				Integer usenum = packvo.getIusenum() == null ? 0 : packvo.getIusenum();
				int num = publishnum - usenum;
				String vmome = packvo.getVmemo() == null ? "" :packvo.getVmemo();
				retvo.setVsalespromot("促销活动： "+ vmome + "    剩余名额" + num + "个");
			}
		}
		//4、查询驳回历史
		if(!StringUtil.isEmpty(retvo.getVconfreason())){
			RejectHistoryHVO[] rejeVOs = qryRejectHistory(retvo.getPk_contract());
			if(rejeVOs != null && rejeVOs.length > 0){
				retvo.setChildren(rejeVOs);
			}
		}
		return retvo;
	}
	
	/**
	 * 查询驳回历史
	 * @param pk_contract
	 * @return
	 * @throws DZFWarpException
	 */
	private RejectHistoryHVO[] qryRejectHistory(String pk_contract) throws DZFWarpException {
		String sql = " nvl(dr,0) = 0 AND pk_contract = ? ORDER BY ts DESC";
		SQLParameter spm = new SQLParameter();
		spm.addParam(pk_contract);
		return (RejectHistoryHVO[]) singleObjectBO.queryByCondition(RejectHistoryHVO.class, sql, spm);
	}

	@Override
	public ContractConfrimVO updateDeductData(ContractConfrimVO datavo, Integer opertype, String cuserid, 
			String pk_corp) throws DZFWarpException {
	    String uuid = UUID.randomUUID().toString();
		try {
			LockUtil.getInstance().tryLockKey(datavo.getTableName(), datavo.getPk_contract(),uuid, 120);
			String errmsg = "";//错误信息
			ChnBalanceVO[] balVOs = null;//余额信息
			PackageDefVO packvo = null;//套餐信息
			if(IStatusConstant.IDEDUCTYPE_1 == opertype){//扣款
				
				System.out.println("审核前:"+ new DZFDateTime());
				
				//1、审核前校验
				Map<String,Object> checkmap = CheckBeforeAudit(datavo,"single");
				if(checkmap != null && !checkmap.isEmpty()){
					errmsg = (String) checkmap.get("errmsg");
					balVOs = (ChnBalanceVO[]) checkmap.get("balance");
					packvo = (PackageDefVO) checkmap.get("package");
				}
				if(!StringUtil.isEmpty(errmsg)){
					setNullValue(datavo);
					throw new BusinessException(errmsg);
				}
				System.out.println("单个审核1:"+ new DZFDateTime());
				
				//2、计算扣款金额
				if(datavo.getIdeductpropor() != 0){//扣款比例如果为0，则不计算扣款金额
					countDedMny(datavo, balVOs);
				}
				System.out.println("单个审核2:"+ new DZFDateTime());
				
				//3、预付款扣款、返点扣款分项校验：
				if(datavo.getIdeductpropor() != 0){
					errmsg = checkBalance(datavo, cuserid, balVOs);
					if(!StringUtil.isEmpty(errmsg)){
						setNullValue(datavo);
						throw new BusinessException(errmsg);
					}
				}
				System.out.println("单个审核3:"+ new DZFDateTime());
				//4、生成合同审核数据
				datavo = saveContConfrim(datavo, cuserid);
				System.out.println("单个审核4:"+ new DZFDateTime());
				//5、回写付款余额
				if(datavo.getIdeductpropor() != 0){//扣款比例如果为0，则不回写余额
					updateBalanceMny(datavo, cuserid, balVOs);
				}
				System.out.println("单个审核5:"+ new DZFDateTime());
				//6、更新原合同加盟合同状态、驳回原因
				updateContract(datavo, opertype, cuserid, pk_corp);
				System.out.println("单个审核6:"+ new DZFDateTime());
				//7、回写套餐促销活动名额(补提交的合同不回写套餐数量)
				if(datavo.getPatchstatus() == null || (datavo.getPatchstatus() != null && datavo.getPatchstatus() != 2)){
					updateSerPackage(packvo);
				}
				System.out.println("单个审核7:"+ new DZFDateTime());
				//8、回写我的客户纳税人性质 、是否为存量客户
				updateCorp(datavo);
				System.out.println("单个审核8:"+ new DZFDateTime());
				//9、发送消息
				saveAuditMsg(datavo, 1, pk_corp, cuserid);
				System.out.println("单个审核9:"+ new DZFDateTime());
			}else if(IStatusConstant.IDEDUCTYPE_2 == opertype){//驳回
				errmsg = checkBeforeReject(datavo);
				if(!StringUtil.isEmpty(errmsg)){
					throw new BusinessException(errmsg);
				}
				updateContract(datavo, opertype, cuserid, pk_corp);
				datavo.setVdeductstatus(IStatusConstant.IDEDUCTSTATUS_7);//已驳回
				datavo.setVstatus(IStatusConstant.IDEDUCTSTATUS_7);//已驳回
				setNullValue(datavo);
				//发送消息
				saveAuditMsg(datavo, 2, pk_corp, cuserid);
			}
		} finally {
			LockUtil.getInstance().unLock_Key(datavo.getTableName(), datavo.getPk_contract(),uuid);
		}
		
		return datavo;
	}
	
	/**
	 * 更新我的客户“纳税人性质、是否存量客户”
	 * @param paramvo
	 * @param packmap
	 * @throws DZFWarpException
	 */
	private void updateCorp(ContractConfrimVO datavo) throws DZFWarpException{
		CorpVO corpvo = CorpCache.getInstance().get(null, datavo.getPk_corpk());
		List<String> uplist = new ArrayList<String>();
		if(corpvo != null){
			//1、审核时，直接从合同取“纳税人性质”更新我的客户“纳税人性质”
			corpvo.setChargedeptname(datavo.getChargedeptname());
			uplist.add("chargedeptname");
			//2、审核时，如果我的客户“是否存量客户”为空，且合同“是否存量客户”不为空，则更新我的客户“是否存量客户”为是
			if(datavo.getIsncust() != null && datavo.getIsncust().booleanValue()){
				corpvo.setIsncust(datavo.getIsncust());
				uplist.add("isncust");
			}
			singleObjectBO.update(corpvo, uplist.toArray(new String[0]));
			CorpCache.getInstance().remove(datavo.getPk_corpk());
		}
	}
	
	/**
	 * 置空值
	 * @param datavo
	 */
	private void setNullValue(ContractConfrimVO datavo) throws DZFWarpException{
		datavo.setDeductdata(null);//扣款日期
		datavo.setIdeductpropor(null);//扣款比例
		datavo.setVoperator(null);//经办人
		datavo.setVopername(null);//经办人姓名
		datavo.setNdedsummny(null);//扣款总金额
		datavo.setNdeductmny(null);//预付款扣款
		datavo.setNdedrebamny(null);//返点扣款
	}

	/**
	 * 更新套餐使用个数
	 * @param packvo
	 * @throws DZFWarpException
	 */
	private void updateSerPackage(PackageDefVO packvo) throws DZFWarpException{
		if(packvo != null){
		    String uuid = UUID.randomUUID().toString();
			try {
				LockUtil.getInstance().tryLockKey(packvo.getTableName(), packvo.getPk_packagedef(),uuid, 30);
				Integer num = packvo.getIusenum() == null ? 0 : packvo.getIusenum();
				Integer pulishnum = packvo.getIpublishnum() == null ? 0 : packvo.getIpublishnum();
				if(packvo.getIspromotion() != null && packvo.getIspromotion().booleanValue()){
					if(num.compareTo(pulishnum) == 0){
						throw new BusinessException("套餐发布个数已经用完");
					}
				}
				packvo.setIusenum(num + 1);
				singleObjectBO.update(packvo, new String[]{"iusenum"});
			} finally {
				LockUtil.getInstance().unLock_Key(packvo.getTableName(), packvo.getPk_packagedef(),uuid);
			}
		}
	}
	
	/**
	 * 保存合同审核信息
	 * @param datavo
	 * @param cuserid
	 * @param balVOs
	 * @return
	 * @throws DZFWarpException
	 */
	private ContractConfrimVO saveContConfrim(ContractConfrimVO datavo, String cuserid)	throws DZFWarpException {
		//保存前赋默认值
		datavo.setVdeductstatus(IStatusConstant.IDEDUCTSTATUS_1);
		datavo.setVstatus(IStatusConstant.IDEDUCTSTATUS_1);
		datavo.setTstamp(new DZFDateTime());// 时间戳
		datavo.setDeductdata(new DZFDate());// 扣款日期
		datavo.setDeductime(new DZFDateTime());// 扣款时间
		datavo.setDr(0);
//		paramvo.setCoperatorid(cuserid);
		UserVO uservo = UserCache.getInstance().get(datavo.getVoperator(), null);
		if (uservo != null) {
			datavo.setVopername(uservo.getUser_name());
		}
		//清空会计公司名称和客户名称
		datavo.setCorpname(null);
		datavo.setCorpkname(null);
		return (ContractConfrimVO) singleObjectBO.saveObject("000001", datavo);
	}
	
	/**
	 * 预付款余额、返点余额生成前校验
	 * @param paramvo
	 * @param cuserid
	 * @param balVOs
	 * @return
	 * @throws DZFWarpException
	 */
	private String checkBalance(ContractConfrimVO paramvo, String cuserid, ChnBalanceVO[] balVOs) throws DZFWarpException {
		//先校验，再进行数据存储，否则会造成数据存储错误
		Map<String,ChnBalanceVO> map = new HashMap<String,ChnBalanceVO>();
		String corpname = "";
		CorpVO corpvo = CorpCache.getInstance().get(null, paramvo.getPk_corp());
		if(corpvo != null){
			corpname = corpvo.getUnitname();
		}
		for(ChnBalanceVO balvo : balVOs){
			if(balvo.getIpaytype() != null && balvo.getIpaytype() == IStatusConstant.IPAYTYPE_2){
				map.put("pay", balvo);
			}else if(balvo.getIpaytype() != null && balvo.getIpaytype() == IStatusConstant.IPAYTYPE_3){
				map.put("reb", balvo);
			}
		}
		if(paramvo != null && CommonUtil.getDZFDouble(paramvo.getNdedrebamny()).compareTo(DZFDouble.ZERO_DBL) != 0){//返点款扣款
			ChnBalanceVO balancevo = map.get("reb");
			if(balancevo == null){
				return "加盟商："+corpname+"余额表信息为空；";
			}
			DZFDouble balance = SafeCompute.sub(balancevo.getNpaymny(), balancevo.getNusedmny());
			if(balance.compareTo(paramvo.getNdedrebamny()) < 0){
				return "加盟商："+corpname+"返点余额不足；";
			}
		}
		if(paramvo != null && CommonUtil.getDZFDouble(paramvo.getNdeductmny()).compareTo(DZFDouble.ZERO_DBL) != 0){//预付款扣款
			ChnBalanceVO balancevo = map.get("pay");
			if(balancevo == null){
				return "加盟商："+corpname+"余额表信息为空；";
			}
			DZFDouble balance = SafeCompute.sub(balancevo.getNpaymny(), balancevo.getNusedmny());
			if(balance.compareTo(paramvo.getNdeductmny()) < 0){
				return "加盟商："+corpname+"预付款余额不足；";
			}
		}
		return "";
	}
	
	/**
	 * 计算扣款总金额
	 * @param confrimvo
	 * @param paramvo
	 */
	private void countDedSumMny(ContractConfrimVO confrimvo, ContractConfrimVO paramvo){
		confrimvo.setIdeductpropor(paramvo.getIdeductpropor());//扣款比例
		confrimvo.setVoperator(paramvo.getVoperator());//经办人
		confrimvo.setVconfreason(paramvo.getVconfreason());//驳回原因
		//合同扣款基数 = 合同总金额 - 账本费
		DZFDouble countmny = SafeCompute.sub(confrimvo.getNtotalmny(), confrimvo.getNbookmny());
		DZFDouble ndedsummny = countmny.multiply(confrimvo.getIdeductpropor()).div(100);
		confrimvo.setNdedsummny(ndedsummny.setScale(2, DZFDouble.ROUND_HALF_UP));//批量审核扣款总金额精度控制，直接四舍五入保留两位小数
	}
	
	/**
	 * 计算扣款分项金额（预付款扣款、返点扣款）
	 * @param datavo
	 * @param balVOs
	 * @throws DZFWarpException
	 */
	private void countDedMny(ContractConfrimVO datavo,ChnBalanceVO[] balVOs) throws DZFWarpException {
		DZFDouble ndedsummny = datavo.getNdedsummny();//扣款总金额
		DZFDouble balance = DZFDouble.ZERO_DBL;
		DZFDouble balasum = DZFDouble.ZERO_DBL;
		DZFDouble paybalance = DZFDouble.ZERO_DBL;//预付款余额
		DZFDouble rebbalance = DZFDouble.ZERO_DBL;//返点款余额
		for(ChnBalanceVO balvo : balVOs){
			balance = SafeCompute.sub(balvo.getNpaymny(), balvo.getNusedmny());
			balasum = SafeCompute.add(balasum, balance);
			if(balvo.getIpaytype() != null && balvo.getIpaytype() == IStatusConstant.IPAYTYPE_2){
				paybalance = balance;
			}else if(balvo.getIpaytype() != null && balvo.getIpaytype() == IStatusConstant.IPAYTYPE_3){
				rebbalance = balance;
			}
		}
		//1、预付款余额>=扣款总金额，全扣预付款
		if(paybalance.compareTo(ndedsummny) >= 0){
			datavo.setNdeductmny(ndedsummny);//预付款扣款金额
			datavo.setNdedrebamny(DZFDouble.ZERO_DBL);//返点单扣款金额
		}else if(paybalance.compareTo(DZFDouble.ZERO_DBL) > 0 
				&& paybalance.compareTo(ndedsummny) < 0){
			//2、0<预付款余额<扣款总金额，先扣预付款，再扣返点余额
			datavo.setNdeductmny(paybalance);//预付款扣款金额
			datavo.setNdedrebamny(SafeCompute.sub(ndedsummny, paybalance));//返点款扣款金额
		}else if(paybalance.compareTo(DZFDouble.ZERO_DBL) == 0 
				&& rebbalance.compareTo(ndedsummny) > 0){
			//3、预付款余额=0，则全扣返点余额
			datavo.setNdeductmny(DZFDouble.ZERO_DBL);//预付款扣款金额
			datavo.setNdedrebamny(ndedsummny);//返点款扣款金额
		}
	}
	
	/**
	 * 更新原合同信息
	 * @param contvo
	 * @param paramvo
	 * @throws DZFWarpException
	 */
	private void updateContract(ContractConfrimVO paramvo, Integer opertype, String cuserid, String pk_corp) throws DZFWarpException {
		StringBuffer sql = new StringBuffer();
		SQLParameter spm = new SQLParameter();
		sql.append(" UPDATE ynt_contract set vdeductstatus = ? , vstatus = ?, vconfreason = ? ,");
		sql.append(" tstamp = ? WHERE nvl(dr,0) = 0 AND pk_corp = ? AND pk_contract = ? ");
		if(IStatusConstant.IDEDUCTYPE_1 == opertype){//扣款
			spm.addParam(IStatusConstant.IDEDUCTSTATUS_1);
			spm.addParam(IStatusConstant.IDEDUCTSTATUS_1);
		}else if(IStatusConstant.IDEDUCTYPE_2 == opertype){//驳回
			spm.addParam(IStatusConstant.IDEDUCTSTATUS_7);
			spm.addParam(IStatusConstant.IDEDUCTSTATUS_7);
		}
		if(StringUtil.isEmpty(paramvo.getVconfreason())){
			spm.addParam("");
		}else{
			spm.addParam(paramvo.getVconfreason());
		}
		spm.addParam(new DZFDateTime());
		spm.addParam(paramvo.getPk_corp());
		spm.addParam(paramvo.getPk_contract());
		singleObjectBO.executeUpdate(sql.toString(), spm);
		if(IStatusConstant.IDEDUCTYPE_2 == opertype){
			saveRejectHistory(paramvo, cuserid, pk_corp);
		}else if(IStatusConstant.IDEDUCTYPE_1 == opertype){
			updateRejectHistory(paramvo, pk_corp);
		}
	}
	
	/**
	 * 删除驳回历史
	 * @param paramvo
	 * @param pk_corp
	 * @throws DZFWarpException
	 */
	private void updateRejectHistory(ContractConfrimVO paramvo, String pk_corp) throws DZFWarpException {
		StringBuffer sql = new StringBuffer();
		SQLParameter spm = new SQLParameter();
		sql.append("DELETE FROM cn_rejecthistory  \n") ;
		sql.append(" WHERE nvl(dr, 0) = 0  \n") ; 
		sql.append("   AND pk_contract = ?  \n") ; 
		sql.append("   AND pk_corp = ?  \n");
		spm.addParam(paramvo.getPk_contract());
		spm.addParam(paramvo.getPk_corp());
		singleObjectBO.executeUpdate(sql.toString(), spm);
		sql = new StringBuffer();
		spm = new SQLParameter();
		sql.append("DELETE FROM cn_rejecthistory_h  \n") ;
		sql.append(" WHERE nvl(dr, 0) = 0  \n") ; 
		sql.append("   AND pk_contract = ?  \n") ; 
		sql.append("   AND pk_corp = ?  \n");
		spm.addParam(paramvo.getPk_contract());
		spm.addParam(paramvo.getPk_corp());
		singleObjectBO.executeUpdate(sql.toString(), spm);
	}
	
	/**
	 * 保存驳回原因
	 * @param paramvo
	 * @param cuserid
	 * @throws DZFWarpException
	 */
	private void saveRejectHistory(ContractConfrimVO paramvo, String cuserid, String pk_corp) throws DZFWarpException {
		String vconfreasonid = paramvo.getVconfreasonid();
		String[] rejectids = vconfreasonid.split(",");
		List<RejectHistoryVO> list = new ArrayList<RejectHistoryVO>();
		
		//存储驳回原因历史及明细：
		RejectHistoryHVO rejehvo = new RejectHistoryHVO();
		rejehvo.setPk_contract(paramvo.getPk_contract());
		rejehvo.setPk_corp(paramvo.getPk_corp());
		rejehvo.setVreason(paramvo.getVconfreason());
		rejehvo.setDr(0);
		rejehvo.setCoperatorid(cuserid);
		rejehvo.setDoperatedate(new DZFDate());
		
		RejectHistoryVO hisvo = null;
		RejectreasonVO revo = null;
		Map<String,RejectreasonVO> remap = qryRejectMap();
		for(String id : rejectids){
			if(remap != null && !remap.isEmpty()){
				revo = remap.get(id);
				if(revo != null){
					hisvo = new RejectHistoryVO();
					hisvo.setPk_source(revo.getPk_rejectreason());
					hisvo.setPk_contract(paramvo.getPk_contract());
					hisvo.setPk_corp(paramvo.getPk_corp());
					hisvo.setVreason(revo.getVreason());
					hisvo.setVsuggest(revo.getVsuggest());
					hisvo.setDr(0);
					hisvo.setCoperatorid(cuserid);
					hisvo.setDoperatedate(new DZFDate());
					list.add(hisvo);
				}
			}
		}
		
		if(list != null && list.size() > 0){
			rejehvo.setChildren(list.toArray(new RejectHistoryVO[0]));
			singleObjectBO.saveObject(pk_corp, rejehvo);
		}
	}
	
	/**
	 * 获取驳回原因
	 * @return
	 * @throws DZFWarpException
	 */
	private Map<String,RejectreasonVO> qryRejectMap() throws DZFWarpException {
		Map<String,RejectreasonVO> map = new HashMap<String,RejectreasonVO>();
		String sql = " nvl(dr,0) = 0 ";
		RejectreasonVO[] rejeVOs = (RejectreasonVO[]) singleObjectBO.queryByCondition(RejectreasonVO.class, sql, null);
		if(rejeVOs != null && rejeVOs.length > 0){
			for(RejectreasonVO rvo : rejeVOs){
				map.put(rvo.getPk_rejectreason(), rvo);
			}
		}
		return map;
	}
	
	/**
	 * 合同审核前状态校验
	 * @throws DZFWarpException
	 */
	@SuppressWarnings("unchecked")
	private String checkBeforeReject(ContractConfrimVO confrimvo) throws DZFWarpException {
		StringBuffer sql = new StringBuffer();
		SQLParameter spm = new SQLParameter();
		sql.append("SELECT tstamp,vdeductstatus \n") ;
		sql.append("  FROM ynt_contract t \n") ; 
		sql.append(" WHERE nvl(t.dr, 0) = 0 \n") ; 
		sql.append("   AND t.pk_contract = ? ");
		spm.addParam(confrimvo.getPk_contract());
		ArrayList<Object> result = (ArrayList<Object>) singleObjectBO.executeQuery(sql.toString(), spm, new ArrayListProcessor());
		if(result != null && result.size() > 0){
			Object[] obj = (Object[]) result.get(0); 
			DZFDateTime tstamp = new DZFDateTime(String.valueOf(obj[0]));
			if(tstamp != null && tstamp.compareTo(confrimvo.getTstamp()) != 0){
				return "合同号："+confrimvo.getVcontcode()+"数据发生变化，请刷新界面后，再次尝试";
			}
			if(IStatusConstant.IDEDUCTSTATUS_5 != Integer.parseInt(String.valueOf(obj[1]))){
				return "合同状态不为待审核";
			}
		}else{
			return "合同数据错误";
		}
		return "";
	}
	
	/**
	 * 更新余额信息
	 * @param datavo
	 * @param cuserid
	 * @throws DZFWarpException
	 */
	private void updateBalanceMny(ContractConfrimVO datavo, String cuserid, ChnBalanceVO[] balVOs) throws DZFWarpException {
		if(balVOs != null && balVOs.length > 0){
			Map<String,ChnBalanceVO> map = new HashMap<String,ChnBalanceVO>();
			for(ChnBalanceVO balvo : balVOs){
				if(balvo.getIpaytype() != null && balvo.getIpaytype() == IStatusConstant.IPAYTYPE_2){
					map.put("pay", balvo);
				}else if(balvo.getIpaytype() != null && balvo.getIpaytype() == IStatusConstant.IPAYTYPE_3){
					map.put("reb", balvo);
				}
			}
			CorpVO corpvo = CorpCache.getInstance().get(null, datavo.getPk_corpk());
			if(corpvo != null){
				datavo.setCorpkname(corpvo.getUnitname());
			}
			if(datavo != null && CommonUtil.getDZFDouble(datavo.getNdeductmny()).compareTo(DZFDouble.ZERO_DBL) != 0){//预付款扣款
				ChnBalanceVO balancevo = map.get("pay");
				balancevo.setNusedmny(SafeCompute.add(balancevo.getNusedmny(), datavo.getNdeductmny()));
				singleObjectBO.update(balancevo, new String[]{"nusedmny"});
				
				ChnDetailVO detvo = new ChnDetailVO();
				detvo.setPk_corp(datavo.getPk_corp());
				detvo.setNusedmny(datavo.getNdeductmny());
				detvo.setIpaytype(IStatusConstant.IPAYTYPE_2);//预付款
				detvo.setPk_bill(datavo.getPk_confrim());
				if(datavo.getIsncust() != null && datavo.getIsncust().booleanValue()){
					detvo.setVmemo("存量客户："+datavo.getCorpkname()+"、"+datavo.getVcontcode());
				}else{
					detvo.setVmemo(datavo.getCorpkname()+"、"+datavo.getVcontcode());
				}
				detvo.setCoperatorid(cuserid);
				detvo.setDoperatedate(new DZFDate());
				detvo.setDr(0);
				detvo.setIopertype(IStatusConstant.IDETAILTYPE_2);//合同扣款
				detvo.setNtotalmny(datavo.getNtotalmny());//合同金额
				detvo.setIdeductpropor(datavo.getIdeductpropor());//扣款比例
				singleObjectBO.saveObject("000001", detvo);
			}
			if(datavo != null && CommonUtil.getDZFDouble(datavo.getNdedrebamny()).compareTo(DZFDouble.ZERO_DBL) != 0){//返点款扣款
				ChnBalanceVO balancevo = map.get("reb");
				balancevo.setNusedmny(SafeCompute.add(balancevo.getNusedmny(), datavo.getNdedrebamny()));
				singleObjectBO.update(balancevo, new String[]{"nusedmny"});
				
				ChnDetailVO detvo = new ChnDetailVO();
				detvo.setPk_corp(datavo.getPk_corp());
				detvo.setNusedmny(datavo.getNdedrebamny());
				detvo.setIpaytype(IStatusConstant.IPAYTYPE_3);//返点款
				detvo.setPk_bill(datavo.getPk_confrim());
				if(datavo.getIsncust()!=null && datavo.getIsncust().booleanValue()){
					detvo.setVmemo("存量客户："+datavo.getCorpkname()+"、"+datavo.getVcontcode());
				}else{
					detvo.setVmemo(datavo.getCorpkname()+"、"+datavo.getVcontcode());
				}
				detvo.setCoperatorid(cuserid);
				detvo.setDoperatedate(new DZFDate());
				detvo.setDr(0);
				detvo.setIopertype(IStatusConstant.IDETAILTYPE_2);//合同扣款
				detvo.setNtotalmny(datavo.getNtotalmny());//合同金额
				detvo.setIdeductpropor(datavo.getIdeductpropor());//扣款比例
				singleObjectBO.saveObject("000001", detvo);
			}
		}
	}

	/**
	 * 批量审核-审核单个数据
	 * @param confrimvo 合同数据
	 * @param paramvo  参数数据
	 * @param opertype
	 * @param cuserid
	 * @param packmap
	 * @return
	 * @throws DZFWarpException
	 */
	@Override
	public ContractConfrimVO updateBathDeductData(ContractConfrimVO confrimvo, ContractConfrimVO paramvo,
			Integer opertype, String cuserid, Map<String, String> packmap, String pk_corp) throws DZFWarpException {
		if(StringUtil.isEmpty(confrimvo.getTableName()) || StringUtil.isEmpty(confrimvo.getPk_contract())){
			confrimvo.setVerrmsg("数据错误");
			return confrimvo;
		}
		if(confrimvo.getIsncust() != null && confrimvo.getIsncust().booleanValue()){
			paramvo.setIdeductpropor(0);//存量客户的扣款比例默认为0%
		}
		String uuid = UUID.randomUUID().toString();
		try {
			LockUtil.getInstance().tryLockKey(confrimvo.getTableName(), confrimvo.getPk_contract(),uuid, 120);
			String errmsg = "";
			ChnBalanceVO[] balVOs = null;//余额信息
			PackageDefVO packvo = null;//套餐信息
			if(IStatusConstant.IDEDUCTYPE_1 == opertype){//扣款
				System.out.println("批量审核前:"+ new DZFDateTime());
				//1、计算扣款总金额：
				countDedSumMny(confrimvo, paramvo);
				Map<String,Object> checkmap =  CheckBeforeAudit(confrimvo, "batch");
				if(checkmap != null && !checkmap.isEmpty()){
					errmsg = (String) checkmap.get("errmsg");
					balVOs = (ChnBalanceVO[]) checkmap.get("balance");
					packvo = (PackageDefVO) checkmap.get("package");
				}
				if(!StringUtil.isEmpty(errmsg)){
					confrimvo.setVerrmsg(errmsg);
					setNullValue(confrimvo);
					return confrimvo;
				}
				System.out.println("批量审核1:"+ new DZFDateTime());
				//2、计算扣款分项金额：
				if(paramvo.getIdeductpropor() != 0){
					countDedMny(confrimvo, balVOs);
				}
				System.out.println("批量审核2:"+ new DZFDateTime());
				//3、生成合同审核数据
				confrimvo = saveContConfrim(confrimvo, cuserid);
				if(!StringUtil.isEmpty(confrimvo.getVerrmsg())){
					setNullValue(confrimvo);
					return confrimvo;
				}
				System.out.println("批量审核3:"+ new DZFDateTime());
				//4、回写付款余额
				if(paramvo.getIdeductpropor() != 0){//扣款比例如果为0，则不回写余额
					updateBalanceMny(confrimvo, cuserid, balVOs);
				}
				System.out.println("批量审核4:"+ new DZFDateTime());
				//5、更新合同加盟合同状态、驳回原因
				updateContract(confrimvo, opertype, cuserid, pk_corp);
				System.out.println("批量审核5:"+ new DZFDateTime());
				//6、回写套餐促销活动名额
				updateSerPackage(packvo);
				System.out.println("批量审核6:"+ new DZFDateTime());
				//7、回写我的客户“纳税人性质  、是否存量客户”
				updateCorp(confrimvo);
				System.out.println("批量审核7:"+ new DZFDateTime());
				//8、发送消息
				saveAuditMsg(confrimvo, 1, pk_corp, cuserid);
				System.out.println("批量审核8:"+ new DZFDateTime());
			}else if(IStatusConstant.IDEDUCTYPE_2 == opertype){//驳回
				errmsg = checkBeforeReject(confrimvo);
				if(!StringUtil.isEmpty(errmsg)){
					confrimvo.setVerrmsg(errmsg);
					setNullValue(confrimvo);
					return confrimvo;
				}
				confrimvo.setVconfreason(paramvo.getVconfreason());
				confrimvo.setVconfreasonid(paramvo.getVconfreasonid());
				updateContract(confrimvo, opertype, cuserid, pk_corp);
				confrimvo.setVdeductstatus(IStatusConstant.IDEDUCTSTATUS_7);//已驳回
				confrimvo.setVstatus(IStatusConstant.IDEDUCTSTATUS_7);//已驳回
				setNullValue(confrimvo);
				//发送消息
				saveAuditMsg(confrimvo, 2, pk_corp, cuserid);
			}
		} finally {
			LockUtil.getInstance().unLock_Key(confrimvo.getTableName(), confrimvo.getPk_contract(),uuid);
		}

		return confrimvo;
	}
	
	/**
	 * 审核前校验
	 * @param confrimvo
	 * @param checktype    single：单个合同审核
	 * @return
	 * @throws DZFWarpException
	 */
	@SuppressWarnings("unchecked")
	private Map<String,Object> CheckBeforeAudit(ContractConfrimVO confrimvo, String checktype) throws DZFWarpException{
		Map<String,Object> map = new HashMap<String,Object>();
		String errmsg = "";
		String corpname = "";
		CorpVO corpvo = CorpCache.getInstance().get(null, confrimvo.getPk_corp());
		if(corpvo != null){
			corpname = corpvo.getUnitname();
		}
		// 1、审核数据状态校验
		StringBuffer sql = new StringBuffer();
		SQLParameter spm = new SQLParameter();
		sql.append("SELECT tstamp,vdeductstatus \n");
		sql.append("  FROM ynt_contract t \n");
		sql.append(" WHERE nvl(t.dr, 0) = 0 \n");
		sql.append("   AND t.pk_contract = ? ");
		spm.addParam(confrimvo.getPk_contract());
		ArrayList<Object> result = (ArrayList<Object>) singleObjectBO.executeQuery(sql.toString(), spm,
				new ArrayListProcessor());
		if (result != null && result.size() > 0) {
			Object[] obj = (Object[]) result.get(0);
			DZFDateTime tstamp = new DZFDateTime(String.valueOf(obj[0]));
			if (tstamp != null && tstamp.compareTo(confrimvo.getTstamp()) != 0) {
				errmsg = "合同号：" + confrimvo.getVcontcode() + "数据发生变化，请刷新界面后，再次尝试；";
				map.put("errmsg", errmsg);
				return map;
			}
			if (IStatusConstant.IDEDUCTSTATUS_5 != Integer.parseInt(String.valueOf(obj[1]))) {
				errmsg = "合同号：" + confrimvo.getVcontcode() + "状态不为待审核；";
				map.put("errmsg", errmsg);
				return map;
			}
		} else {
			errmsg = "合同号：" + confrimvo.getVcontcode() + "数据错误；";
			map.put("errmsg", errmsg);
			return map;
		}

		// 2、预付款余额校验（扣款比例不为0的情况进行校验）
		if(confrimvo.getIdeductpropor() != 0){
			//2.1、合同扣款金额计算校验：
			if("single".equals(checktype)){
				//合同扣款基数 = 合同总金额 - 账本费
				DZFDouble countmny = SafeCompute.sub(confrimvo.getNtotalmny(), confrimvo.getNbookmny());
				DZFDouble ndedsummny = countmny.multiply(confrimvo.getIdeductpropor()).div(100);
				ndedsummny = ndedsummny.setScale(2, DZFDouble.ROUND_HALF_UP);//预付款扣款金额精度控制，直接四舍五入保留两位小数
				if(ndedsummny.compareTo(confrimvo.getNdedsummny()) != 0){
					errmsg = "合同号：" + confrimvo.getVcontcode() + "扣款金额计算错误；";
					map.put("errmsg", errmsg);
					return map;
				}
			}
			
			//2.2、余额校验：
			//扣款金额 <= 预付款余额 + 返点余额
			String yesql = " nvl(dr,0) = 0 and pk_corp = ? and ipaytype in (?,?) ";
			SQLParameter yespm = new SQLParameter();
			yespm.addParam(confrimvo.getPk_corp());
			yespm.addParam(IStatusConstant.IPAYTYPE_2);
			yespm.addParam(IStatusConstant.IPAYTYPE_3);
			ChnBalanceVO[] balVOs = (ChnBalanceVO[]) singleObjectBO.queryByCondition(ChnBalanceVO.class, yesql, yespm);
			if (balVOs != null && balVOs.length > 0) {
				DZFDouble balance = DZFDouble.ZERO_DBL;
				DZFDouble balasum = DZFDouble.ZERO_DBL;
				for(ChnBalanceVO balvo : balVOs){
					balance = SafeCompute.sub(balvo.getNpaymny(), balvo.getNusedmny());
					balasum = SafeCompute.add(balasum, balance);
				}
				if (balasum.compareTo(confrimvo.getNdedsummny()) < 0) {//与扣款总结做比较
					errmsg = "合同号：" + confrimvo.getVcontcode() + "扣款金额大于预付款余额与返点余额之和；";
					map.put("errmsg", errmsg);
					return map;
				}
				
				map.put("balance", balVOs);
			} else {
				errmsg = "合同号：" + confrimvo.getVcontcode() + "扣款金额大于预付款余额与返点余额之和；";
				map.put("errmsg", errmsg);
				return map;
			}
			
			
		}

		// 3、套餐发布个数校验
		PackageDefVO packvo = (PackageDefVO) singleObjectBO.queryByPrimaryKey(PackageDefVO.class,
				confrimvo.getPk_packagedef());
		if(packvo.getIspromotion() != null && packvo.getIspromotion().booleanValue()){
			Integer num = packvo.getIusenum() == null ? 0 : packvo.getIusenum();
			Integer pulishnum = packvo.getIpublishnum() == null ? 0 : packvo.getIpublishnum();
			if (num.compareTo(pulishnum) == 0) {
				errmsg = "合同号：" + confrimvo.getVcontcode() + "对应套餐发布个数已经用完；";
				map.put("errmsg", errmsg);
				return map;
			}
			map.put("package", packvo);
		}
		return map;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ContractDocVO[] getAttatches(ContractDocVO qvo) throws DZFWarpException {
		if (qvo == null) {
			return null;
		}
		if (StringUtil.isEmpty(qvo.getPk_contract()) && StringUtil.isEmpty(qvo.getPk_contract_doc())) {
			throw new BusinessException("获取附件参数有误");
		}
		StringBuffer condition = new StringBuffer();
		condition.append(" nvl(dr,0) = 0 and pk_corp = ?");
		SQLParameter params = new SQLParameter();
		params.addParam(qvo.getPk_corp());
		if (!StringUtil.isEmpty(qvo.getPk_contract())) {
			condition.append(" and  pk_contract = ? ");
			params.addParam(qvo.getPk_contract());
		}
		if (!StringUtil.isEmpty(qvo.getPk_contract_doc())) {
			condition.append(" and pk_contract_doc = ? ");
			params.addParam(qvo.getPk_contract_doc());
		}
		String orderBy = " ts desc ";
		List<ContractDocVO> resvos = (List<ContractDocVO>) singleObjectBO.retrieveByClause(ContractDocVO.class,
				condition.toString(), orderBy, params);
		if (resvos != null) {
			return resvos.toArray(new ContractDocVO[0]);
		}
		return null;
	}

	@Override
	public ContractConfrimVO saveChange(ContractConfrimVO datavo, String cuserid, File[] files, String[] filenames)
			throws DZFWarpException {
		if(StringUtil.isEmpty(datavo.getTableName()) || StringUtil.isEmpty(datavo.getPk_contract())){
			datavo.setVerrmsg("数据错误");
			return datavo;
		}
		String uuid = UUID.randomUUID().toString();
		try {
			LockUtil.getInstance().tryLockKey(datavo.getTableName(), datavo.getPk_contract(),uuid, 120);
			//1、变更前校验：
			checkBeforeChange(datavo);
			//2、相关字段赋值：
			if (datavo.getIchangetype() == IStatusConstant.ICONCHANGETYPE_1) {
				datavo.setVdeductstatus(IStatusConstant.IDEDUCTSTATUS_9);
				datavo.setVstatus(IStatusConstant.IDEDUCTSTATUS_9);
				datavo.setIchangetype(IStatusConstant.ICONCHANGETYPE_1);
				datavo.setVchangeraeson("C端客户终止，变更合同");
			} else if (datavo.getIchangetype() == IStatusConstant.ICONCHANGETYPE_2) {
				datavo.setVdeductstatus(IStatusConstant.IDEDUCTSTATUS_10);
				datavo.setVstatus(IStatusConstant.IDEDUCTSTATUS_10);
				datavo.setIchangetype(IStatusConstant.ICONCHANGETYPE_2);
				datavo.setVchangeraeson("合同作废");
				datavo.setIdeductpropor(0);
			}
			datavo.setPatchstatus(3);//加盟合同类型（null正常合同；1被补提交的合同；2补提交的合同；3变更合同）
			datavo.setVchanger(cuserid);
			datavo.setDchangetime(new DZFDateTime());
			datavo.setTstamp(new DZFDateTime());
			//3、退款（预付款退款、返点款退款）相关字段计算：
			if(CommonUtil.getDZFDouble(datavo.getNreturnmny()).compareTo(DZFDouble.ZERO_DBL) > 0){
				if(CommonUtil.getDZFDouble(datavo.getNdedrebamny()).compareTo(DZFDouble.ZERO_DBL) > 0){//返点扣款>0
					if(CommonUtil.getDZFDouble(datavo.getNreturnmny()).compareTo(datavo.getNdedrebamny()) <= 0){
						//3.1、0<退款总金额<=返点扣款，全部退返点
						datavo.setNretrebmny(datavo.getNreturnmny());//返点退款
						datavo.setNretdedmny(DZFDouble.ZERO_DBL);//预付款退款
					}else if(CommonUtil.getDZFDouble(datavo.getNreturnmny()).compareTo(datavo.getNdedrebamny()) > 0){
						//3.2、0<返点扣款<退款总金额，先退返点，再退预付款
						datavo.setNretrebmny(datavo.getNdedrebamny());//返点退款 = 返点扣款
						datavo.setNretdedmny(SafeCompute.sub(datavo.getNreturnmny(), datavo.getNretrebmny()));//预付款退款 = 退款总金额 - 返点退款
					}
				}else if(CommonUtil.getDZFDouble(datavo.getNdedrebamny()).compareTo(DZFDouble.ZERO_DBL) == 0){//返点扣款=0
					//3.3、返点扣款=0，全部退预付款
					datavo.setNretrebmny(DZFDouble.ZERO_DBL);//返点退款
					datavo.setNretdedmny(datavo.getNreturnmny());//预付款退款 = 退款总金额
				}
			}
			//4、变更后扣款（变更后预付款扣款、变更后返点扣款）相关字段计算：
			datavo.setNchangededutmny(SafeCompute.sub(datavo.getNdeductmny(), datavo.getNretdedmny()));//变更后预付款扣款 = 预付款扣款 - 预付款退款
			datavo.setNchangerebatmny(SafeCompute.sub(datavo.getNdedrebamny(), datavo.getNretrebmny()));//变更后返点扣款 = 返点扣款 - 返点退款
			
			//5、变更差额数据处理：
			//5.1、变更后合同总金额差额  = 原合同总金额 - 变更后合同金额
			DZFDouble nsubtotalmny = SafeCompute.sub(datavo.getNtotalmny(), datavo.getNchangetotalmny());
			datavo.setNsubtotalmny(CommonUtil.getDZFDouble(nsubtotalmny).multiply(-1));
			
			//5.2、变更后扣款总金额差额 = 原扣款总金额 - 变更后扣款总金额
			DZFDouble nsubdedsummny = SafeCompute.sub(datavo.getNdedsummny(), datavo.getNchangesummny());
			datavo.setNsubdedsummny(CommonUtil.getDZFDouble(nsubdedsummny).multiply(-1));
			
			//5.3、变更后预付款扣款差额 = 原预付款扣款金额 - 变更后预付款扣款金额
			DZFDouble nsubdeductmny = SafeCompute.sub(datavo.getNdeductmny(), datavo.getNchangededutmny());
			datavo.setNsubdeductmny(CommonUtil.getDZFDouble(nsubdeductmny).multiply(-1));
			
			//5.4、变更后返点款扣款差额 = 原返点扣款金额 - 变更后返点扣款金额
			DZFDouble nsubdedrebamny = SafeCompute.sub(datavo.getNdedrebamny(), datavo.getNchangerebatmny());
			datavo.setNsubdedrebamny(CommonUtil.getDZFDouble(nsubdedrebamny).multiply(-1));
			
			// 6、更新合同历史数据
			String[] str = new String[] { "vdeductstatus", "vstatus","ichangetype", "vchangeraeson", "vchangememo",
					"vstopperiod", "vchanger","dchangetime","tstamp",
					"nreturnmny", "nretrebmny", "nretdedmny", 
					"nchangetotalmny", "nchangesummny", "nchangededutmny", "nchangerebatmny",
					"nsubtotalmny", "nsubdedsummny", "nsubdeductmny", "nsubdedrebamny", "ideductpropor" };
			singleObjectBO.update(datavo, str);
			
			// 7.1、更新原合同主表数据的合同结束日期、合同结束期间、合同周期、合同总金额
			StringBuffer sql = new StringBuffer();
			SQLParameter spm = new SQLParameter();
			sql.append("update ynt_contract \n") ;
			sql.append("   set vdeductstatus = ?, vstatus = ?, patchstatus = 3, tstamp = ? \n") ; 
			if (datavo.getIchangetype() == IStatusConstant.ICONCHANGETYPE_1) {
				spm.addParam(IStatusConstant.IDEDUCTSTATUS_9);
				spm.addParam(IStatusConstant.IDEDUCTSTATUS_9);
			}else if(datavo.getIchangetype() == IStatusConstant.ICONCHANGETYPE_2){
				spm.addParam(IStatusConstant.IDEDUCTSTATUS_10);
				spm.addParam(IStatusConstant.IDEDUCTSTATUS_10);
			}
			spm.addParam(new DZFDateTime());
			if(datavo.getIchangetype() == IStatusConstant.ICONCHANGETYPE_1){
				String vbeginperiod = datavo.getVbeginperiod();//合同开始期间
				String vstopperiod = datavo.getVstopperiod();//合同终止期间
				Integer changenum = ToolsUtil.getCyclenum(vbeginperiod, vstopperiod);
				sql.append(" , icontractcycle = ? , ntotalmny = ? \n") ;
				spm.addParam(changenum);
				spm.addParam(datavo.getNchangetotalmny());
				DZFDate denddate = null;
				String vendperiod = "";
				try {
					String venddate = ToolsUtil.getDateAfterNum(datavo.getDbegindate(), changenum);
					if(!StringUtil.isEmpty(venddate)){
						denddate = new DZFDate(venddate);
						vendperiod = denddate.getYear() + "-" + denddate.getStrMonth();
						sql.append(" , denddate = ? , vendperiod = ? \n") ;
						spm.addParam(denddate);
						spm.addParam(vendperiod);
						datavo.setDenddate(denddate);
						datavo.setVendperiod(vendperiod);
					}
				} catch (ParseException e) {
					throw new BusinessException("获取变更后结束日期失败");
				};
			}
			sql.append(" where nvl(dr, 0) = 0 \n") ; 
			sql.append("   and pk_corp = ? \n") ; 
			sql.append("   and pk_contract = ? \n");

			spm.addParam(datavo.getPk_corp());
			spm.addParam(datavo.getPk_contract());
			singleObjectBO.executeUpdate(sql.toString(), spm);
			//7.2、如果是合同终止，更新合同子表“代理记账费”的应收金额、未收金额、应收期间；更新合同子表“ 账本费”的应收期间
			if(datavo.getIchangetype() == IStatusConstant.ICONCHANGETYPE_1){
				//7.2.1 代理记账费
				String vreceivmonth = datavo.getVbeginperiod() + "至" + datavo.getVendperiod();
				sql = new StringBuffer();
				spm = new SQLParameter();
				sql.append("UPDATE ynt_contract_b \n") ;
				sql.append("   SET nreceivemny = ? , wsreceivemny = ? , vreceivmonth = ? \n") ; 
				sql.append(" WHERE nvl(dr, 0) = 0 \n") ; 
				sql.append("   AND pk_corp = ? \n") ; 
				sql.append("   AND pk_contract = ? \n") ; 
				sql.append("   AND icosttype = 1 \n");
				spm.addParam(SafeCompute.sub(datavo.getNchangetotalmny(), datavo.getNbookmny()));
				spm.addParam(SafeCompute.sub(datavo.getNchangetotalmny(), datavo.getNbookmny()));
				spm.addParam(vreceivmonth);
				spm.addParam(datavo.getPk_corp());
				spm.addParam(datavo.getPk_contract());
				singleObjectBO.executeUpdate(sql.toString(), spm);
				//7.2.1 账本费
				sql = new StringBuffer();
				spm = new SQLParameter();
				sql.append("UPDATE ynt_contract_b \n") ;
				sql.append("   SET vreceivmonth = ? \n") ; 
				sql.append(" WHERE nvl(dr, 0) = 0 \n") ; 
				sql.append("   AND pk_corp = ? \n") ; 
				sql.append("   AND pk_contract = ? \n") ; 
				sql.append("   AND icosttype = 0 \n");
				spm.addParam(vreceivmonth);
				spm.addParam(datavo.getPk_corp());
				spm.addParam(datavo.getPk_contract());
				singleObjectBO.executeUpdate(sql.toString(), spm);
			}
			
			//8、上传变更合同附件
			saveContDocVO(datavo, files, filenames, cuserid);
			//9、更新合同变更后余额及余额明细表
			StringBuffer vmemo = new StringBuffer();
			if (datavo.getIchangetype() == IStatusConstant.ICONCHANGETYPE_1) {
				vmemo.append("合同终止：");
			}else if(datavo.getIchangetype() == IStatusConstant.ICONCHANGETYPE_2){
				vmemo.append("合同终止：");

			}
			CorpVO corpvo = CorpCache.getInstance().get(null, datavo.getPk_corpk());
			if(corpvo != null){
				datavo.setCorpkname(corpvo.getUnitname());
				vmemo.append(corpvo.getUnitname()).append("、");
			}
			vmemo.append(datavo.getVcontcode());
			updateChangeBalMny(datavo, cuserid, vmemo.toString());
		} finally {
			LockUtil.getInstance().unLock_Key(datavo.getTableName(), datavo.getPk_contract(),uuid);
		}
		return datavo;
	}
	
	/**
	 * 合同变更前校验
	 * @param paramvo
	 * @throws DZFWarpException
	 */
	@SuppressWarnings("unchecked")
	private void checkBeforeChange(ContractConfrimVO paramvo) throws DZFWarpException {
		ContractConfrimVO oldvo = null;
//		ContractConfrimVO oldvo = (ContractConfrimVO) singleObjectBO.queryByPrimaryKey(ContractConfrimVO.class,
//				paramvo.getPk_confrim());
		QryParamVO pavo = new QryParamVO();
		pavo.setPk_bill(paramvo.getPk_confrim());
		pavo.setPk_corp(paramvo.getPk_corp());
		QrySqlSpmVO qryvo =  getQryCondition(pavo, "");
		List<ContractConfrimVO> list = (List<ContractConfrimVO>) singleObjectBO.executeQuery(qryvo.getSql(),
				qryvo.getSpm(), new BeanListProcessor(ContractConfrimVO.class));
		if (list != null && list.size() > 0) {
			oldvo = list.get(0);
		}
		if(oldvo == null){
			throw new BusinessException("变更合同信息错误");
		}
		if(oldvo.getVdeductstatus() != null && oldvo.getVdeductstatus() != IStatusConstant.IDEDUCTSTATUS_1){
			throw new BusinessException("合同状态不为审核通过");
		}
		if(oldvo.getPatchstatus() != null && oldvo.getPatchstatus() == 1){
			throw new BusinessException("该合同已被补提单，不允许变更");
		}
		if(oldvo.getPatchstatus() != null && oldvo.getPatchstatus() == 2){
			throw new BusinessException("该合同为补提单，不允许变更");
		}
		if(oldvo.getPatchstatus() != null && oldvo.getPatchstatus() == 3){
			throw new BusinessException("该合同已变更，不允许再次变更");
		}
		if(paramvo.getIchangetype() == 1){
			//再次计算退回扣款相关金额并进行校验
			DZFDouble ntotalmny = oldvo.getNtotalmny();// 原合同总金额
			String vbeginperiod = oldvo.getVbeginperiod();//合同开始期间
			String vstopperiod = paramvo.getVstopperiod();//合同终止期间
			Integer changenum = ToolsUtil.getCyclenum(vbeginperiod, vstopperiod);
			Integer ireceivcycle = CommonUtil.getInteger(oldvo.getIreceivcycle());//原收款周期
			DZFDouble ndedsummny = oldvo.getNdedsummny();//原扣款总金额
			//退回扣款总金额算法：原扣款总金额-{（原扣款总金额/原收款期间）*（原开始期间到终止期间的期数）}
			DZFDouble midmny = CommonUtil.getDZFDouble(ndedsummny).div(
					CommonUtil.getDZFDouble(ireceivcycle)).multiply(CommonUtil.getDZFDouble(changenum));
			DZFDouble nreturnmny = SafeCompute.sub(ndedsummny, midmny);//退回扣款总金额
			if(nreturnmny.compareTo(DZFDouble.ZERO_DBL) < 0){
				nreturnmny = DZFDouble.ZERO_DBL;
			}
			//变更后合同金额 = 原月代账费  *（原开始期间到终止期间的期数）+ 账本费
			DZFDouble nmservicemny = oldvo.getNmservicemny(); // 每月服务费
			DZFDouble nbookmny = oldvo.getNbookmny(); // 账本费
			DZFDouble nchangetotalmny = DZFDouble.ZERO_DBL;
			if(nreturnmny.compareTo(DZFDouble.ZERO_DBL) == 0){
				nchangetotalmny = ntotalmny;
			}else{
				nchangetotalmny = SafeCompute.multiply(nmservicemny, 
						CommonUtil.getDZFDouble(changenum)).add(CommonUtil.getDZFDouble(nbookmny));
			}
			//变更后扣款总金额 = 原扣款总金额 - 退回扣款总金额
			DZFDouble nchangesummny = SafeCompute.sub(ndedsummny, nreturnmny);
			
			if(nreturnmny.setScale(2, DZFDouble.ROUND_HALF_UP).compareTo(paramvo.getNreturnmny()) != 0){
				throw new BusinessException("退回扣款金额计算错误");
			}
			if(nchangetotalmny.setScale(2, DZFDouble.ROUND_HALF_UP).compareTo(paramvo.getNchangetotalmny()) != 0){
				throw new BusinessException("变更后合同金额计算错误");
			}
			if(nchangesummny.setScale(2, DZFDouble.ROUND_HALF_UP).compareTo(paramvo.getNchangesummny()) != 0){
				throw new BusinessException("变更后扣款金额计算错误");
			}
		}
	}
	
	/**
	 * 上传变更合同附件
	 * @param vo
	 * @param files
	 * @param filenames
	 * @param cuserid
	 * @throws DZFWarpException
	 */
	private void saveContDocVO(ContractConfrimVO vo, File[] files, String[] filenames, String cuserid)
			throws DZFWarpException {
		CorpVO corpvo = CorpCache.getInstance().get(null, vo.getPk_corp());
		if (corpvo == null) {
			throw new BusinessException("会计公司信息错误");
		}
		if (files != null && files.length > 0) {
			String uploadPath = ImageCommonPath.getContractFilePath(corpvo.getInnercode(), vo.getPk_contract(), null);
			ArrayList<ContractDocVO> list = new ArrayList<>();
			ContractDocVO docvo = null;
			for (int i = 0; i < files.length; i++) {
				String fname = System.nanoTime() + filenames[i].substring(filenames[i].indexOf("."));
				String filepath = uploadPath + File.separator + fname;
				docvo = new ContractDocVO();
				docvo.setPk_corp(vo.getPk_corp());
				docvo.setPk_contract(vo.getPk_contract());
				docvo.setCoperatorid(cuserid);
				docvo.setDoperatedate(new DZFDate());
				docvo.setDocOwner(cuserid);
				docvo.setDocTime(new DZFDateTime());
				docvo.setDocName(filenames[i]);
				docvo.setDocTemp(fname);
				docvo.setVfilepath(filepath);
				docvo.setDr(0);
				docvo.setIdoctype(3);
				list.add(docvo);
				InputStream is = null;
				OutputStream os = null;
				try {
					File ff = new File(filepath);
					if (!ff.getParentFile().exists()) {
						ff.getParentFile().mkdirs();
					}
					is = new FileInputStream(files[i]);
					os = new FileOutputStream(filepath);
					IOUtils.copy(is, os);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (is != null) {
						try {
							is.close();
						} catch (IOException e) {
						    Logger.error(this, e.getMessage(),e);
						}
					}
					if (os != null) {
						try {
							os.close();
						} catch (IOException e) {
						    Logger.error(this, e.getMessage(),e);
						}
					}
				}
			}
			singleObjectBO.insertVOArr(vo.getPk_corp(), list.toArray(new ContractDocVO[0]));
		}
	}
	
	/**
	 * 更新合同变更后余额信息
	 * @param paramvo
	 * @param cuserid
	 * @throws DZFWarpException
	 */
	private void updateChangeBalMny(ContractConfrimVO paramvo, String cuserid, String vmemo) throws DZFWarpException {
		String yesql = " nvl(dr,0) = 0 and pk_corp = ? and ipaytype in (?,?) ";
		SQLParameter yespm = new SQLParameter();
		yespm.addParam(paramvo.getPk_corp());
		yespm.addParam(IStatusConstant.IPAYTYPE_2);
		yespm.addParam(IStatusConstant.IPAYTYPE_3);
		ChnBalanceVO[] balVOs = (ChnBalanceVO[]) singleObjectBO.queryByCondition(ChnBalanceVO.class, yesql, yespm);
		if(balVOs != null && balVOs.length > 0){
			Map<String,ChnBalanceVO> map = new HashMap<String,ChnBalanceVO>();
			for(ChnBalanceVO balvo : balVOs){
				if(balvo.getIpaytype() != null && balvo.getIpaytype() == IStatusConstant.IPAYTYPE_2){
					map.put("pay", balvo);
				}else if(balvo.getIpaytype() != null && balvo.getIpaytype() == IStatusConstant.IPAYTYPE_3){
					map.put("reb", balvo);
				}
			}
			if(paramvo != null && CommonUtil.getDZFDouble(paramvo.getNretdedmny()).compareTo(DZFDouble.ZERO_DBL) != 0){//预付款退款
				ChnBalanceVO balancevo = map.get("pay");
				if(balancevo == null){
					throw new BusinessException("余额表信息不能为空");
				}
				balancevo.setNusedmny(SafeCompute.sub(balancevo.getNusedmny(), paramvo.getNretdedmny()));
				singleObjectBO.update(balancevo, new String[]{"nusedmny"});
				
				ChnDetailVO detvo = new ChnDetailVO();
				detvo.setPk_corp(paramvo.getPk_corp());
				detvo.setNusedmny(SafeCompute.multiply(paramvo.getNretdedmny(),new DZFDouble(-1)));
				detvo.setIpaytype(IStatusConstant.IPAYTYPE_2);//预付款
				detvo.setPk_bill(paramvo.getPk_confrim());
				detvo.setVmemo(vmemo);
				detvo.setCoperatorid(cuserid);
				detvo.setDoperatedate(new DZFDate());
				detvo.setDr(0);
				detvo.setIopertype(IStatusConstant.IDETAILTYPE_2);//合同扣款
				detvo.setNtotalmny(paramvo.getNtotalmny());//合同金额
				detvo.setIdeductpropor(paramvo.getIdeductpropor());//扣款比例
				singleObjectBO.saveObject("000001", detvo);
			}
			if(paramvo != null && CommonUtil.getDZFDouble(paramvo.getNretrebmny()).compareTo(DZFDouble.ZERO_DBL) != 0){//返点款退款
				ChnBalanceVO balancevo = map.get("reb");
				if(balancevo == null){
					throw new BusinessException("余额表信息不能为空");
				}
				balancevo.setNusedmny(SafeCompute.sub(balancevo.getNusedmny(), paramvo.getNretrebmny()));
				singleObjectBO.update(balancevo, new String[]{"nusedmny"});
				
				ChnDetailVO detvo = new ChnDetailVO();
				detvo.setPk_corp(paramvo.getPk_corp());
				detvo.setNusedmny(SafeCompute.multiply(paramvo.getNretrebmny(),new DZFDouble(-1)));
				detvo.setIpaytype(IStatusConstant.IPAYTYPE_3);//返点款
				detvo.setPk_bill(paramvo.getPk_confrim());
				detvo.setVmemo(vmemo);
				detvo.setCoperatorid(cuserid);
				detvo.setDoperatedate(new DZFDate());
				detvo.setDr(0);
				detvo.setIopertype(IStatusConstant.IDETAILTYPE_2);//合同扣款
				detvo.setNtotalmny(paramvo.getNtotalmny());//合同金额
				detvo.setIdeductpropor(paramvo.getIdeductpropor());//扣款比例
				singleObjectBO.saveObject("000001", detvo);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public ContractConfrimVO queryInfoById(ContractConfrimVO paramvo) throws DZFWarpException {
		ContractConfrimVO confvo = null;
		QryParamVO pavo = new QryParamVO();
		pavo.setPk_bill(paramvo.getPk_contract());
		pavo.setPk_corp(paramvo.getPk_corp());
		QrySqlSpmVO qryvo =  getQryCondition(pavo, "info");
		List<ContractConfrimVO> list = (List<ContractConfrimVO>) singleObjectBO.executeQuery(qryvo.getSql(),
				qryvo.getSpm(), new BeanListProcessor(ContractConfrimVO.class));
		if (list != null && list.size() > 0) {
			setShowData(list, "info",null);
			confvo = list.get(0);
		}
		if(confvo != null && !StringUtil.isEmpty(confvo.getVconfreason())){
			RejectHistoryHVO[] rejeVOs = qryRejectHistory(confvo.getPk_contract());
			if(rejeVOs != null && rejeVOs.length > 0){
				confvo.setChildren(rejeVOs);
			}
		}
		return confvo;
	}
	
	/**
	 * 发送合同审核消息
	 * @param contvo
	 * @param sendtype
	 * @param loginpk
	 * @param userid
	 * @throws DZFWarpException
	 */
	private void saveAuditMsg(ContractConfrimVO contvo, Integer sendtype, String loginpk, String userid) throws DZFWarpException {
		IPushAppMessage pushMsgService = (IPushAppMessage) SpringUtils.getBean("pushappmessageimpl");
		pushMsgService.saveConAuditMsg(contvo, sendtype, loginpk, userid);
	}
}
