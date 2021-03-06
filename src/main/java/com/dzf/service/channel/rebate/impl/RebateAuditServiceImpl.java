package com.dzf.service.channel.rebate.impl;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dzf.dao.bs.SingleObjectBO;
import com.dzf.dao.jdbc.framework.SQLParameter;
import com.dzf.model.channel.ChnBalanceVO;
import com.dzf.model.channel.ChnDetailVO;
import com.dzf.model.channel.rebate.RebateVO;
import com.dzf.model.pub.IStatusConstant;
import com.dzf.model.pub.WorkflowVO;
import com.dzf.model.sys.sys_power.UserVO;
import com.dzf.pub.BusinessException;
import com.dzf.pub.DZFWarpException;
import com.dzf.pub.StringUtil;
import com.dzf.pub.WiseRunException;
import com.dzf.pub.lang.DZFBoolean;
import com.dzf.pub.lang.DZFDate;
import com.dzf.pub.lang.DZFDateTime;
import com.dzf.pub.lock.LockUtil;
import com.dzf.service.channel.rebate.IRebateAuditService;
import com.dzf.service.channel.rebate.IRebateInputService;
import com.dzf.service.sys.sys_power.IUserService;

@Service("rebateauditser")
public class RebateAuditServiceImpl implements IRebateAuditService {
	
	@Autowired
	private IRebateInputService rebateser;
	
	@Autowired
	private SingleObjectBO singleObjectBO;
	@Autowired
	private IUserService userServiceImpl;

	@Override
	public RebateVO updateAudit(RebateVO data, String pk_corp, Integer opertype) throws DZFWarpException {
		// 时间校验
		String errmsg = rebateser.checkData(data);
		if (!StringUtil.isEmpty(errmsg)) {
			throw new BusinessException(errmsg);
		}
		if (opertype != null && opertype == IStatusConstant.IREBATEOPERTYPE_1) {//驳回修改
			if (data.getIstatus() != null && 
					data.getIstatus() != IStatusConstant.IREBATESTATUS_2) {
				throw new BusinessException("返点单号" + data.getVbillcode() + "不为待审批态");
			}
			if (StringUtil.isEmpty(data.getVapprovenote())) {
				throw new BusinessException("驳回说明不能为空");
			}
			data.setIstatus(IStatusConstant.IREBATESTATUS_4);
		}else if(opertype != null && opertype == IStatusConstant.IREBATEOPERTYPE_4){//审核通过
			if (data.getIstatus() != null && 
					data.getIstatus() != IStatusConstant.IREBATESTATUS_2) {
				throw new BusinessException("返点单号" + data.getVbillcode() + "不为待审批态");
			}
			data.setIstatus(IStatusConstant.IREBATESTATUS_3);
		}
		data.setTapprovetime(new DZFDateTime());
		data.setTstamp(new DZFDateTime());
		String uuid = UUID.randomUUID().toString();
		try {
			LockUtil.getInstance().tryLockKey(data.getTableName(), data.getPk_rebate(),uuid, 15);
			//1、更新相关确认信息
			singleObjectBO.update(data,
					new String[] { "istatus", "vapprovenote", "vapproveid", "tapprovetime", "tstamp" });
			//2、记录审批历史
			WorkflowVO flowvo = getFlowInfo(data, pk_corp, opertype);
			if(flowvo != null){
				singleObjectBO.saveObject(pk_corp, flowvo);
			}
			if(opertype != null && opertype == IStatusConstant.IREBATEOPERTYPE_4){
				//3、只有审核通过才记录存储返点单金额
				saveRebateMny(data, pk_corp);
			}
		} catch (Exception e) {
			if (e instanceof BusinessException)
				throw new BusinessException(e.getMessage());
			else
				throw new WiseRunException(e);
		} finally {
			LockUtil.getInstance().unLock_Key(data.getTableName(), data.getPk_rebate(),uuid);
		}
		return data;
	}
	
	/**
	 * 获取审批历史
	 * @param data
	 * @param pk_corp
	 * @param opertype
	 * @return
	 * @throws DZFWarpException
	 */
	private WorkflowVO getFlowInfo(RebateVO data, String pk_corp, Integer opertype) throws DZFWarpException{
		UserVO uservo = null;
		WorkflowVO flowvo = new WorkflowVO();
		flowvo.setPk_corp(pk_corp);
		flowvo.setPk_corpk(data.getPk_corp());
		flowvo.setPk_bill(data.getPk_rebate());
		flowvo.setVcode(data.getVbillcode());
		flowvo.setIsdeal(DZFBoolean.TRUE);
		String userid = data.getVapproveid();//审批人
		flowvo.setSenderman(userid);
		uservo = userServiceImpl.queryUserJmVOByID(userid);
		flowvo.setDsenddate(new DZFDate());
		flowvo.setDsendtime(new DZFDateTime());
		flowvo.setDealman(userid);
		flowvo.setDdealdate(new DZFDate());
		flowvo.setDdealtime(new DZFDateTime());
		if(uservo != null){
			flowvo.setSendmanname(uservo.getUser_name());
			flowvo.setDealmanname(uservo.getUser_name());
		}
		flowvo.setCoperatorid(userid);
		flowvo.setDoperatedate(new DZFDate());
		flowvo.setDr(0);
		flowvo.setVbilltype(IStatusConstant.IBILLTYPE_FD02);//返点单审批
		flowvo.setVapprovenote(data.getVapprovenote());//审批说明or驳回说明
		if (opertype != null && opertype == IStatusConstant.IREBATEOPERTYPE_1) {
			flowvo.setVstatusnote("驳回修改");
		}else if(opertype != null && opertype == IStatusConstant.IREBATEOPERTYPE_4){
			flowvo.setVstatusnote("审批通过");
		}
		return flowvo;
	}
	
	/**
	 * 存储返点单相关金额
	 * @param data
	 * @param pk_corp
	 * @throws DZFWarpException
	 */
	private void saveRebateMny(RebateVO data, String pk_corp) throws DZFWarpException {
		if(data.getNrebatemny() == null){
			throw new BusinessException("返点金额不能为空");
		}
		ChnDetailVO detvo = new ChnDetailVO();
		detvo.setPk_corp(data.getPk_corp());
		detvo.setNpaymny(data.getNrebatemny());//返点金额
		detvo.setIpaytype(IStatusConstant.IPAYTYPE_3);//返点
		detvo.setVmemo("返点");
		detvo.setPk_bill(data.getPk_rebate());
		detvo.setCoperatorid(data.getVapproveid());
		detvo.setDoperatedate(new DZFDate());
		detvo.setDr(0);
		detvo.setIopertype(IStatusConstant.IDETAILTYPE_3);//返点增加
		ChnBalanceVO balvo = null;
		String sql = " nvl(dr,0) = 0 and pk_corp = ? and ipaytype = ? ";
		SQLParameter spm = new SQLParameter();
		spm.addParam(data.getPk_corp());
		spm.addParam(IStatusConstant.IPAYTYPE_3);
		ChnBalanceVO[] balVOs = (ChnBalanceVO[]) singleObjectBO.queryByCondition(ChnBalanceVO.class, sql, spm);
		if(balVOs != null && balVOs.length > 0){
//			balvo = balVOs[0];
//			balvo.setNpaymny(SafeCompute.add(balvo.getNpaymny(), data.getNrebatemny()));
//			singleObjectBO.update(balvo, new String[]{"npaymny"});
			
			String uid = UUID.randomUUID().toString();
			try {
				LockUtil.getInstance().tryLockKey("cn_balance",
						data.getPk_corp() + "" + IStatusConstant.IPAYTYPE_3, uid, 120);
				StringBuffer usql = new StringBuffer();
				spm = new SQLParameter();
				usql.append("UPDATE cn_balance l    ");
				usql.append("   SET l.npaymny = nvl(l.npaymny,0) + ?    ");
				spm.addParam(data.getNrebatemny());
				usql.append(" WHERE nvl(l.dr,0) = 0 AND l.ipaytype = ?    ");
				spm.addParam(IStatusConstant.IPAYTYPE_3);
				usql.append("   AND l.pk_corp = ?    ");
				spm.addParam(data.getPk_corp());
				int res = singleObjectBO.executeUpdate(usql.toString(), spm);
				if(res != 1){
					throw new BusinessException("余额表金额更新错误");
				}
			} catch (Exception e) {
				if (e instanceof BusinessException)
					throw new BusinessException(e.getMessage());
				else
					throw new WiseRunException(e);
			} finally {
				LockUtil.getInstance().unLock_Key("cn_balance",
						data.getPk_corp() + "" + IStatusConstant.IPAYTYPE_3, uid);
			}
			
		}else{
			balvo = new ChnBalanceVO();
			balvo.setPk_corp(data.getPk_corp());
			balvo.setNpaymny(data.getNrebatemny());
			balvo.setIpaytype(IStatusConstant.IPAYTYPE_3);
			balvo.setVmemo(data.getVyear()+"年"+data.getIseason()+"季度返点");
			balvo.setCoperatorid(data.getVapproveid());
			balvo.setDoperatedate(new DZFDate());
			balvo.setDr(0);
			singleObjectBO.saveObject("000001", balvo);
		}
		singleObjectBO.saveObject("000001", detvo);
	}

}
