package com.dzf.service.channel.dealmanage;

import java.util.List;

import com.dzf.model.channel.dealmanage.GoodsBillVO;
import com.dzf.model.channel.stock.StockOutBVO;
import com.dzf.model.channel.stock.StockOutVO;
import com.dzf.model.pub.ComboBoxVO;
import com.dzf.model.pub.QryParamVO;
import com.dzf.pub.DZFWarpException;

public interface IStockOutService {
	
	
	/**
	 * 查询数据行数
	 * @param paramvo
	 * @return
	 * @throws DZFWarpException
	 */
	public Integer queryTotalRow(QryParamVO pamvo) throws DZFWarpException;
	
	/**
	 * 查询数据
	 * @param paramvo
	 * @return
	 * @throws DZFWarpException
	 */
	public List<StockOutVO> query(QryParamVO pamvo) throws DZFWarpException;
	
	/**
	 * 查询出库单明细
	 * @param gid
	 * @return
	 * @throws DZFWarpException
	 */
	public StockOutVO queryByID(String soutid) throws DZFWarpException;
	
	/**
	 * 查询订单（为了新增出库单）
	 * @param pk_corp
	 * @return
	 * @throws DZFWarpException
	 */
	public List<StockOutBVO> queryOrders(String pk_corp,String bills,String billid)throws DZFWarpException;
	
	/**
	 * 新增保存;修改保存
	 * @param vo
	 * @throws DZFWarpException
	 */
	public void save(StockOutVO vo,List<String> billids) throws DZFWarpException;
	
	/**
	 * 保存+确认出库（订单出库保存）
	 * @param vo
	 * @param billids
	 * @throws DZFWarpException
	 */
	public void saveCommit(StockOutVO vo,List<String> billids) throws DZFWarpException;
	
	/**
	 * 删除出库单
	 * @param vo
	 * @throws DZFWarpException
	 */
	public void delete(StockOutVO vo) throws DZFWarpException;
	
	/**
	 * 确认出库
	 * @param vo
	 * @throws DZFWarpException
	 */
	public void updateCommit(StockOutVO vo) throws DZFWarpException;
	
	
	/**
	 * 取消确认出库
	 * @param vo
	 * @throws DZFWarpException
	 */
	public void updateCancel(StockOutVO vo) throws DZFWarpException;
	
	/**
	 * 确认发货
	 * @param vo
	 * @throws DZFWarpException
	 */
	public void updateDeliver(StockOutVO vo) throws DZFWarpException;
	
	/**
	 * 查询打印数据明细
	 * @param soutid
	 * @return
	 * @throws DZFWarpException
	 */
	public StockOutVO queryForPrint(String soutid) throws DZFWarpException;
	
	/**
	 * 查询有订单的加盟商数据
	 * @return
	 * @throws DZFWarpException
	 */
	public List<ComboBoxVO> queryChannel(String cuserid ) throws DZFWarpException;
	
	/**
	 * 查询物流公司档案
	 * @return
	 * @throws DZFWarpException
	 */
	public List<ComboBoxVO> queryLogist() throws DZFWarpException;
	
	/**
	 * 根据订单编号，获取订单地址
	 * @param code
	 * @return
	 * @throws DZFWarpException
	 */
	public GoodsBillVO qryGoodsBill(String code) throws DZFWarpException;
	
	/**
	 * 获取权限sql语句
	 * @param pamvo
	 * @return
	 * @throws DZFWarpException
	 */
	public String getQrySql(QryParamVO pamvo) throws DZFWarpException ;
	
}
