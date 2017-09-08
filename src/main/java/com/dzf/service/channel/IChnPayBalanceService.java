package com.dzf.service.channel;

import java.util.List;

import com.dzf.model.channel.ChnBalanceVO;
import com.dzf.model.channel.ChnDetailVO;
import com.dzf.model.pub.QryParamVO;
import com.dzf.pub.DZFWarpException;

public interface IChnPayBalanceService {

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
	public List<ChnBalanceVO> query(QryParamVO paramvo) throws DZFWarpException;
	
	/**
	 * 查询明细数据
	 * @param paramvo
	 * @return
	 * @throws DZFWarpException
	 */
	public List<ChnDetailVO> queryDetail(QryParamVO paramvo) throws DZFWarpException;
}
