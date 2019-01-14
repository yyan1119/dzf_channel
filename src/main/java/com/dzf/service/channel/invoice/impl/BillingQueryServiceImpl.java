package com.dzf.service.channel.invoice.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dzf.dao.bs.SingleObjectBO;
import com.dzf.dao.jdbc.framework.SQLParameter;
import com.dzf.dao.jdbc.framework.processor.BeanListProcessor;
import com.dzf.dao.jdbc.framework.processor.ColumnProcessor;
import com.dzf.model.channel.ChInvoiceVO;
import com.dzf.model.channel.invoice.BillingInvoiceVO;
import com.dzf.model.pub.CommonUtil;
import com.dzf.model.pub.IStatusConstant;
import com.dzf.model.sys.sys_power.AccountVO;
import com.dzf.pub.BusinessException;
import com.dzf.pub.DZFWarpException;
import com.dzf.pub.QueryDeCodeUtils;
import com.dzf.pub.StringUtil;
import com.dzf.pub.WiseRunException;
import com.dzf.pub.jm.CodeUtils1;
import com.dzf.pub.lang.DZFDate;
import com.dzf.pub.lang.DZFDouble;
import com.dzf.pub.lock.LockUtil;
import com.dzf.pub.util.SafeCompute;
import com.dzf.pub.util.SqlUtil;
import com.dzf.service.channel.invoice.IBillingQueryService;
import com.dzf.service.pub.IPubService;

@Service("billingQueryServiceImpl")
public class BillingQueryServiceImpl implements IBillingQueryService {

	@Autowired
	private SingleObjectBO singleObjectBO;

	@Autowired
	private IPubService pubService;

	private final static String tablename = "cn_invoice";

	@SuppressWarnings("unchecked")
	@Override
	public List<BillingInvoiceVO> query(BillingInvoiceVO paramvo) throws DZFWarpException {
		StringBuffer sql = new StringBuffer();
		SQLParameter spm = new SQLParameter();
		sql.append("select ba.pk_corp, \n");
		sql.append("       ba.innercode as corpcode, \n");
		sql.append("       ba.unitname as corpname, \n");
		sql.append("       ba.vprovince, \n");
		sql.append("       ba.citycounty as vprovname, \n");
		sql.append("       sum(nvl(detail.nusedmny, 0)) as debittotalmny \n");
		sql.append("  from bd_account ba \n");
		sql.append("  left join cn_detail detail on ba.pk_corp = detail.pk_corp \n");
		sql.append("                            and nvl(detail.dr, 0) = 0 \n");
		sql.append("                            and detail.iopertype in (2, 5) \n");
		sql.append("                            and detail.ipaytype = 2 \n");

		if (!StringUtil.isEmpty(paramvo.getBdate())) {
			sql.append(" and detail.doperatedate <= ?");
			spm.addParam(paramvo.getBdate());
		}
		sql.append(" where ba.ischannel = 'Y'  ");
		if (null != paramvo.getCorps() && paramvo.getCorps().length > 0) {
			String corpIdS = SqlUtil.buildSqlConditionForIn(paramvo.getCorps());
			sql.append(" and ba.pk_corp  in (" + corpIdS + ")");
		}

		String condition = pubService.makeCondition(paramvo.getCuserid(), paramvo.getAreaname(),
				IStatusConstant.IYUNYING);
		if (condition != null && !condition.equals("alldata")) {
			sql.append(condition);
		} else if (condition == null) {
			return new ArrayList<BillingInvoiceVO>();
		}
		sql.append(" group by ba.pk_corp, \n");
		sql.append("          ba.innercode, \n");
		sql.append("          ba.unitname, \n");
		sql.append("          ba.vprovince, \n");
		sql.append("          ba.citycounty \n");

		List<BillingInvoiceVO> list = (List<BillingInvoiceVO>) singleObjectBO.executeQuery(sql.toString(), spm,
				new BeanListProcessor(BillingInvoiceVO.class));
		HashMap<String, BillingInvoiceVO> map = queryInvoiceMny(paramvo);
		Map<Integer, String> areaMap = pubService.getAreaMap(paramvo.getAreaname(), 3);
		if (list != null && list.size() > 0) {
			List<BillingInvoiceVO> retlist = new ArrayList<BillingInvoiceVO>();
			QueryDeCodeUtils.decKeyUtils(new String[] { "corpname" }, list, 2);
			for (BillingInvoiceVO bvo : list) {
				if (areaMap != null && !areaMap.isEmpty()) {
					String area = areaMap.get(bvo.getVprovince());
					if (!StringUtil.isEmpty(area)) {
						bvo.setAreaname(area);
					}
				}
				BillingInvoiceVO binvo = map.get(bvo.getPk_corp());
				if (binvo != null) {
					bvo.setBilltotalmny(CommonUtil.getDZFDouble(binvo.getBilltotalmny()));
					bvo.setNoticketmny(SafeCompute.sub(bvo.getDebittotalmny(), bvo.getBilltotalmny()));
				}
				if (!StringUtil.isEmpty(paramvo.getCorpname())) {
					if (bvo.getCorpcode().indexOf(paramvo.getCorpname()) != -1
							|| bvo.getCorpname().indexOf(paramvo.getCorpname()) != -1) {
						retlist.add(bvo);
					}
				}
			}
			if (!StringUtil.isEmpty(paramvo.getCorpname())) {
				return retlist;
			}
		}
		return list;
	}

	/**
	 * 查询已开票金额
	 * 
	 * @param vo
	 */
	@SuppressWarnings("unchecked")
	private HashMap<String, BillingInvoiceVO> queryInvoiceMny(BillingInvoiceVO vo) {
		StringBuffer sql = new StringBuffer();
		SQLParameter spm = new SQLParameter();
		sql.append("select a.pk_corp, sum(nvl(invoice.invprice, 0)) as billtotalmny \n");
		sql.append("  from bd_account a \n");
		sql.append("  left join cn_invoice invoice on invoice.pk_corp = a.pk_corp \n");
		sql.append("                              and nvl(invoice.dr, 0) = 0 \n");
		sql.append("                              and (invoice.invstatus = 2 or \n");
		sql.append("                                  invoice.invstatus = 1) \n");
		sql.append("                              and invoice.apptime <= ? \n");
		spm.addParam(new DZFDate());
		sql.append(" where a.ischannel = 'Y' \n");
		if (null != vo.getCorps() && vo.getCorps().length > 0) {
			String corpIdS = SqlUtil.buildSqlConditionForIn(vo.getCorps());
			sql.append(" and a.pk_corp  in (" + corpIdS + ")");
		}
		sql.append(" group by a.pk_corp \n");
		List<BillingInvoiceVO> list = (List<BillingInvoiceVO>) singleObjectBO.executeQuery(sql.toString(), spm,
				new BeanListProcessor(BillingInvoiceVO.class));
		HashMap<String, BillingInvoiceVO> map = new HashMap<>();
		if (list != null && list.size() > 0) {
			for (BillingInvoiceVO bvo : list) {
				map.put(bvo.getPk_corp(), bvo);
			}
		}
		return map;

	}

	@Override
	public void insertBilling(BillingInvoiceVO vo) throws DZFWarpException {
		String uuid = UUID.randomUUID().toString();
		try {
			LockUtil.getInstance().tryLockKey(tablename, vo.getPk_corp(), uuid, 60);
			if (vo.getNoticketmny().compareTo(DZFDouble.ZERO_DBL) <= 0) {
				throw new BusinessException("未开票金额必须大于0");
			}
			DZFDouble invmny = queryInvoiceMny(vo.getPk_corp());
			DZFDouble umny = CommonUtil.getDZFDouble(vo.getDebittotalmny());
			DZFDouble invprice = new DZFDouble(vo.getNoticketmny());
			if (invprice.compareTo(umny.sub(invmny)) > 0) {
				StringBuffer msg = new StringBuffer();
				msg.append("你本次要开票的金额").append(invprice.setScale(2, DZFDouble.ROUND_HALF_UP)).append("元大于可开票金额")
						.append(umny.sub(invmny).setScale(2, DZFDouble.ROUND_HALF_UP)).append("元，请刷新数据。");
				throw new BusinessException(msg.toString());
			}
			AccountVO avo = (AccountVO) singleObjectBO.queryByPrimaryKey(AccountVO.class, vo.getPk_corp());
			if (StringUtil.isEmpty(avo.getTaxcode())) {
				throw new BusinessException("开票信息【税号】为空。");
			}
			ChInvoiceVO cvo = new ChInvoiceVO();
			cvo.setPk_corp(vo.getPk_corp());
			cvo.setCorpname(vo.getCorpname());
			cvo.setInvnature(0);// 发票性质
			cvo.setTaxnum(avo.getTaxcode());// 税号
			cvo.setInvprice(vo.getNoticketmny());// 开票金额
			cvo.setInvtype(avo.getInvtype() == null ? 2 : avo.getInvtype());// 发票类型
			cvo.setCorpaddr(avo.getPostaddr());// 公司地址
			cvo.setInvphone(CodeUtils1.deCode(avo.getPhone1()));
			cvo.setBankcode(avo.getVbankcode());// 开户账户
			cvo.setBankname(avo.getVbankname());// 开户行
			cvo.setEmail(avo.getEmail1());// 邮箱
			cvo.setApptime(new DZFDate().toString());// 申请日期
			cvo.setInvstatus(1);// 状态
			cvo.setIpaytype(0);
			cvo.setInvcorp(2);
			cvo.setRusername(avo.getLinkman2());
			singleObjectBO.saveObject(vo.getPk_corp(), cvo);
		} catch (Exception e) {
			if (e instanceof BusinessException)
				throw new BusinessException(e.getMessage());
			else
				throw new WiseRunException(e);
		} finally {
			LockUtil.getInstance().unLock_Key(tablename, vo.getPk_corp(), uuid);
		}
	}

	/**
	 * 查询已开票金额
	 * 
	 * @param vo
	 */
	private DZFDouble queryInvoiceMny(String pk_corp) {
		StringBuffer sql = new StringBuffer();
		SQLParameter spm = new SQLParameter();
		sql.append("select sum(nvl(invprice, 0)) as billtotalmny \n");
		sql.append("  from cn_invoice \n");
		sql.append(" where (invstatus = 2 or invstatus = 1) \n");
		sql.append("   and apptime <= ? \n");
		sql.append("   and pk_corp = ? \n");
		sql.append("   and nvl(dr, 0) = 0 \n");
		spm.addParam(new DZFDate());
		spm.addParam(pk_corp);
		sql.append(" group by pk_corp ");
		Object obj = singleObjectBO.executeQuery(sql.toString(), spm, new ColumnProcessor());
		return obj == null ? DZFDouble.ZERO_DBL : new DZFDouble(obj.toString());
	}

}
