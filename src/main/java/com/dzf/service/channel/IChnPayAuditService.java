package com.dzf.service.channel;

import java.util.List;

import com.dzf.model.channel.ChnPayBillVO;
import com.dzf.model.pub.QryParamVO;
import com.dzf.pub.DZFWarpException;

public interface IChnPayAuditService {

	/**
	 * 查询数据行数
	 * @param paramvo
	 * @return
	 * @throws DZFWarpException
	 */
	public Integer queryTotalRow(QryParamVO paramvo) throws DZFWarpException;
	
	/**
	 * 查询数据
	 * @param paramvo
	 * @return
	 * @throws DZFWarpException
	 */
	public List<ChnPayBillVO> query(QryParamVO paramvo) throws DZFWarpException;
	
	/**
	 * 确认审批、取消审批、驳回 单条数据操作
	 * @param billvo
	 * @param opertype
	 * @param cuserid
	 * @param vreason
	 * @return
	 * @throws DZFWarpException
	 */
	public ChnPayBillVO updateOperate(ChnPayBillVO billvo, Integer opertype, String cuserid,String vreason) throws DZFWarpException;
}
