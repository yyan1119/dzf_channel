package com.dzf.service.channel.dealmanage.impl;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dzf.dao.bs.SingleObjectBO;
import com.dzf.dao.jdbc.framework.SQLParameter;
import com.dzf.dao.jdbc.framework.processor.BeanListProcessor;
import com.dzf.dao.jdbc.framework.processor.ColumnListProcessor;
import com.dzf.dao.multbs.MultBodyObjectBO;
import com.dzf.model.channel.dealmanage.GoodsBillSVO;
import com.dzf.model.channel.dealmanage.GoodsBillVO;
import com.dzf.model.channel.stock.StockOutBVO;
import com.dzf.model.channel.stock.StockOutVO;
import com.dzf.model.pub.IStatusConstant;
import com.dzf.model.pub.MaxCodeVO;
import com.dzf.model.pub.QryParamVO;
import com.dzf.model.pub.QrySqlSpmVO;
import com.dzf.model.sys.sys_power.CorpVO;
import com.dzf.model.sys.sys_power.UserVO;
import com.dzf.pub.BusinessException;
import com.dzf.pub.DZFWarpException;
import com.dzf.pub.StringUtil;
import com.dzf.pub.WiseRunException;
import com.dzf.pub.cache.CorpCache;
import com.dzf.pub.cache.UserCache;
import com.dzf.pub.lang.DZFDate;
import com.dzf.pub.lang.DZFDateTime;
import com.dzf.pub.lock.LockUtil;
import com.dzf.pub.util.SqlUtil;
import com.dzf.service.channel.dealmanage.IStockOutService;
import com.dzf.service.pub.IBillCodeService;

@Service("outStock")
public class StockOutServiceImpl implements IStockOutService{
	
	@Autowired
	private SingleObjectBO singleObjectBO;
	
	@Autowired
	private IBillCodeService billCode;
	
	
	@Autowired
	private MultBodyObjectBO multBodyObjectBO= null;
	

	@Override
	public Integer queryTotalRow(QryParamVO pamvo) throws DZFWarpException {
		QrySqlSpmVO sqpvo =  getQrySqlSpm(pamvo);
		return multBodyObjectBO.queryDataTotal(StockOutVO.class,sqpvo.getSql(), sqpvo.getSpm());
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<StockOutVO> query(QryParamVO pamvo) throws DZFWarpException {
		QrySqlSpmVO sqpvo =  getQrySqlSpm(pamvo);
		List<StockOutVO> list = (List<StockOutVO>) multBodyObjectBO.queryDataPage(StockOutVO.class, 
				sqpvo.getSql(), sqpvo.getSpm(), pamvo.getPage(), pamvo.getRows(), null);
		UserVO uvo = null;
		CorpVO cvo = null;
		for (StockOutVO stockOutVO : list) {
			uvo = UserCache.getInstance().get(stockOutVO.getCoperatorid(), null);
			if (uvo != null) {
				stockOutVO.setCoperatname(uvo.getUser_name());
			}
			cvo = CorpCache.getInstance().get(null, stockOutVO.getPk_corp());
			if(cvo !=null){
				stockOutVO.setCorpname(cvo.getUnitname());
			}
		}
		return list;
	}
	
	@Override
	public StockOutVO queryByID(String soutid) throws DZFWarpException {
		StockOutVO retvo = (StockOutVO) singleObjectBO.queryByPrimaryKey(StockOutVO.class, soutid);
		if(retvo == null){
			throw new BusinessException("很抱歉，该出库单已被删除!");
		}
		StringBuffer corpsql = new StringBuffer();
		SQLParameter sp = new SQLParameter();
		corpsql.append("SELECT c.vbillcode,g.vgoodsname,");
		corpsql.append(" g.pk_goodsspec,g.invspec,g.invtype,");
		corpsql.append(" s.nnum,s.nmny");
		corpsql.append("  FROM cn_stockout_b s");
		corpsql.append(" left join cn_goodsbill_b g on s.pk_goodsbill_b = g.pk_goodsbill_b ");
		corpsql.append(" left join cn_goodsbill c on g.pk_goodsbill = c.pk_goodsbill");
		corpsql.append(" where nvl(s.dr,0)= 0 and nvl(g.dr,0)=0 and nvl(c.dr,0)=0 ");
		corpsql.append(" and pk_stockout= ? ");
//		corpsql.append(" order by ts asc,pk_dealstep_b asc");
		sp.addParam(soutid);
		List<StockOutBVO> bvos = (List<StockOutBVO>) singleObjectBO.executeQuery(corpsql.toString(),sp,new BeanListProcessor(StockOutBVO.class));
		retvo.setChildren(bvos.toArray(new StockOutBVO[0]));
		return retvo;
	}
	
	@Override
	public List<StockOutBVO> queryOrders(String pk_corp)throws DZFWarpException {
		StringBuffer sql = new StringBuffer();
		SQLParameter spm = new SQLParameter();
		sql.append("select c.vbillcode, ");
		sql.append("       b.pk_goodsbill_b, ");
		sql.append("       b.pk_goods, ");
		sql.append("       b.pk_goodsspec, ");
		sql.append("       b.invspec, ");
		sql.append("       b.invtype, ");
		sql.append("       b.vgoodsname, ");
		sql.append("       b.amount nnum,");
		sql.append("       b.nprice, ");
		sql.append("       b.ntotalmny ");
		sql.append("  from cn_goodsbill_b b ");
		sql.append("  left join cn_goodsbill c on b.pk_goodsbill = c.pk_goodsbill ");
		sql.append(" where nvl(b.dr, 0) = 0 ");
		sql.append("   and nvl(c.dr, 0) = 0 ");
		sql.append("   and c.vstatus in (1, 2, 3) ");
//		sql.append("   and nvl(b.deamount, 0) = 0 ");
		sql.append("   and b.pk_corp = ? ");
		spm.addParam(pk_corp);
		List<StockOutBVO> vos=(List<StockOutBVO>)singleObjectBO.executeQuery(sql.toString(), spm, new BeanListProcessor(StockOutBVO.class));
		return vos;
	}
	
	@Override
	public void saveNew(StockOutVO vo) throws DZFWarpException {
		vo.setVstatus(0);
		vo.setDoperatedate(new DZFDateTime());
		setDefaultCode(vo);
		multBodyObjectBO.saveMultBObject(vo.getFathercorp(), vo);
	}
	
	@Override
	public void saveEdit(StockOutVO vo) throws DZFWarpException {
		//1、更新主表；
		String[] str ={"pk_corp"};
		if (!StringUtil.isEmpty(vo.getVmemo())) {
			String[] str1 = { "vmemo" };
			str = (String[]) ArrayUtils.addAll(str, str1);
		}
		singleObjectBO.update(vo,str);
		//2、删除子表
		StringBuffer sql = new StringBuffer();
		SQLParameter spm = new SQLParameter();
		sql.append("DELETE FROM cn_stockout_b  \n") ;
		sql.append(" WHERE  pk_stockout = ? \n") ; 
		spm.addParam(vo.getPk_stockout());
		singleObjectBO.executeUpdate(sql.toString(), spm);
		//3、插入子表
		singleObjectBO.insertVOArr(vo.getFathercorp(), vo.getChildren());
	}
	
	@Override
	public void delete(StockOutVO vo) throws DZFWarpException {
		if(vo.getVstatus()!=0){
			throw new BusinessException("只有待确认的出库单可以删除");
		}
		String uuid = UUID.randomUUID().toString();
		try {
			boolean lockKey = LockUtil.getInstance().addLockKey(vo.getTableName(), vo.getPk_stockout(),uuid, 120);
			if(!lockKey){
				String message="单据编号："+vo.getVbillcode()+",其他用户正在操作此数据;<br>";
				throw new BusinessException(message);
			}
			checkData(vo);
			StringBuffer sql = new StringBuffer();
			SQLParameter spm = new SQLParameter();
			sql.append("DELETE FROM cn_stockout  \n") ;
			sql.append(" WHERE  pk_stockout = ? \n") ; 
			spm.addParam(vo.getPk_stockout());
			singleObjectBO.executeUpdate(sql.toString(), spm);
			
			sql = new StringBuffer();
			sql.append("DELETE FROM cn_stockout_b  \n") ;
			sql.append(" WHERE  pk_stockout = ? \n") ; 
			singleObjectBO.executeUpdate(sql.toString(), spm);
			
		}catch (Exception e) {
		    if (e instanceof BusinessException)
		        throw new BusinessException(e.getMessage());
		    else
		        throw new WiseRunException(e);
		} finally {
			LockUtil.getInstance().unLock_Key(vo.getTableName(), vo.getPk_stockout(),uuid);
		}
	}

	@Override
	public void updateCommit(StockOutVO vo) throws DZFWarpException {
		checkStatus(",不能确认出库;",vo);
		String uuid = UUID.randomUUID().toString();
		try {
			boolean lockKey = LockUtil.getInstance().addLockKey(vo.getTableName(), vo.getPk_stockout(),uuid, 120);
			if(!lockKey){
				String message="订单编号："+vo.getVbillcode()+",其他用户正在操作此数据;<br>";
				throw new BusinessException(message);
			}
			checkData(vo);
			//1、更新出库单主表
			vo.setVstatus(1);
			vo.setDconfirmtime(new DZFDateTime());
			singleObjectBO.update(vo, new String[]{"vstatus","vconfirmid","dconfirmtime","logisticsunit","fastcode"});
			//2、更新库存表
			
		}catch (Exception e) {
		    if (e instanceof BusinessException)
		        throw new BusinessException(e.getMessage());
		    else
		        throw new WiseRunException(e);
		} finally {
			LockUtil.getInstance().unLock_Key(vo.getTableName(), vo.getPk_stockout(),uuid);
		}
	}
	

	@Override
	public void updateDeliver(StockOutVO vo) throws DZFWarpException {
		checkStatus(",不能确认发货;",vo);
		String uuid = UUID.randomUUID().toString();
		try {
			boolean lockKey = LockUtil.getInstance().addLockKey(vo.getTableName(), vo.getPk_stockout(),uuid, 120);
			if(!lockKey){
				String message="订单编号："+vo.getVbillcode()+",其他用户正在操作此数据;<br>";
				throw new BusinessException(message);
			}
//			checkData(vo);暂时没用，
			//1、更新出库单主表
			vo.setVstatus(2);
			vo.setDdelivertime(new DZFDateTime());
			singleObjectBO.update(vo, new String[]{"vstatus","vdeliverid","ddelivertime"});
			vo=(StockOutVO)singleObjectBO.queryByPrimaryKey(StockOutVO.class, vo.getPk_stockout());
			
			//2、查询出库单的子表对应的未发货订单
			StringBuffer sql = new StringBuffer();
			SQLParameter spm = new SQLParameter();
			sql.append("select distinct b.pk_goodsbill ");
			sql.append("  from cn_stockout_b sb ");
			sql.append("  left join cn_goodsbill_b gb on sb.pk_goodsbill_b = gb.pk_goodsbill_b ");
			sql.append("  left join cn_goodsbill b on gb.pk_goodsbill = b.pk_goodsbill ");
			sql.append(" where nvl(sb.dr, 0) = 0 ");
			sql.append("   and nvl(gb.dr, 0) = 0 ");
			sql.append("   and nvl(b.dr, 0) = 0 ");
			sql.append("   and sb.pk_stockout = ? ");
			sql.append("   and b.vstatus = 1 ");
			spm.addParam(vo.getPk_stockout());
			List<GoodsBillVO> vos=(List<GoodsBillVO>)singleObjectBO.executeQuery(sql.toString(), spm, new BeanListProcessor(GoodsBillVO.class));
			
			//3、更新订单主表信息；创建订单状态子表；
			updateGoodBills(vo,vos);
			
			//4、批量更新订单商品子表的 deamount字段
			sql = new StringBuffer();
			sql = new StringBuffer(" select sb.pk_goodsbill_b from cn_stockout_b sb ");
			sql = new StringBuffer("  where nvl(sb.dr,0)=0 and sb.pk_stockout=? ");
			List<String> billbs = (List<String>)singleObjectBO.executeQuery(sql.toString(), spm, new ColumnListProcessor("pk_goodsbill_b"));
			if(billbs!=null &&  billbs.size()>0){
				sql = new StringBuffer();
				sql = new StringBuffer(" update cn_goodsbill_b sb set sb.deamount=sb.nnum ");
				sql = new StringBuffer("  where nvl(sb.dr,0)=0 ");
				sql.append(SqlUtil.buildSqlForIn("pk_goodsbill_b",billbs.toArray(new String[billbs.size()])));
				singleObjectBO.executeUpdate(sql.toString(),null);
			}
		}catch (Exception e) {
		    if (e instanceof BusinessException)
		        throw new BusinessException(e.getMessage());
		    else
		        throw new WiseRunException(e);
		} finally {
			LockUtil.getInstance().unLock_Key(vo.getTableName(), vo.getPk_stockout(),uuid);
		}
	}
	
	private void updateGoodBills(StockOutVO vo, List<GoodsBillVO> vos)throws DZFWarpException {
		String uuid = UUID.randomUUID().toString();
		try {
			GoodsBillSVO bsvo =null;
			for (GoodsBillVO goodsBillVO : vos) {
				LockUtil.getInstance().tryLockKey("cn_goodsbill", goodsBillVO.getPk_goodsbill(),uuid, 120);
				goodsBillVO.setVstatus(IStatusConstant.IORDERSTATUS_2);//已发货
				singleObjectBO.update(goodsBillVO, new String[]{"vstatus"});
				
				bsvo = new GoodsBillSVO();
				bsvo.setPk_goodsbill(goodsBillVO.getPk_goodsbill());
				bsvo.setPk_corp(vo.getPk_corp());
				bsvo.setVsaction(IStatusConstant.IORDERACTION_2);
				bsvo.setVstatus(IStatusConstant.IORDERSTATUS_2);
				bsvo.setVsdescribe(IStatusConstant.IORDERDESCRIBE_2);//状态描述
				bsvo.setCoperatorid(vo.getVdeliverid());
				bsvo.setDoperatedate(new DZFDate());
				bsvo.setDoperatetime(new DZFDateTime());
				bsvo.setLogisticsunit(vo.getLogisticsunit());//物流公司
				bsvo.setFastcode(vo.getFastcode());//物流单号
				singleObjectBO.saveObject(bsvo.getPk_corp(), bsvo);
			}
//				singleObjectBO.updateAry(vos.toArray(new GoodsBillVO[vos.size()]), new String[]{"vstatus"});
		}catch (Exception e) {
		    if (e instanceof BusinessException)
		        throw new BusinessException(e.getMessage());
		    else
		        throw new WiseRunException(e);
		} finally {
			for (GoodsBillVO goodsBillVO : vos) {
				LockUtil.getInstance().unLock_Key("cn_goodsbill", goodsBillVO.getPk_goodsbill(),uuid);
			}
		}
	}

	/**
	 * 获取查询条件
	 * @param pamvo
	 * @return
	 * @throws DZFWarpException
	 */
	private QrySqlSpmVO getQrySqlSpm(QryParamVO pamvo) throws DZFWarpException {
		QrySqlSpmVO qryvo = new QrySqlSpmVO();
		StringBuffer sql = new StringBuffer();
		SQLParameter spm = new SQLParameter();
		sql.append("select c.vbillcode,");
		sql.append("       c.pk_stockout,");
		sql.append("       c.pk_corp,");
		sql.append("       c.logisticsunit,");
		sql.append("       c.fastcode,");
		sql.append("       c.vmemo,");
		sql.append("       c.vstatus,");
		sql.append("       c.coperatorid,");
		sql.append("       c.doperatedate,");
		sql.append("       c.dconfirmtime,");
		sql.append("       c.updatets");
		sql.append("  from cn_stockout c");
		sql.append(" where nvl(c.dr,0)=0 ");
		if(pamvo.getBegdate()!=null){
			sql.append("and substr(c.doperatedate,0,10)>=? ");
			spm.addParam(pamvo.getBegdate());
		}
		if(pamvo.getEnddate()!=null){
			sql.append("and substr(c.doperatedate,0,10)<= ? ");
			spm.addParam(pamvo.getEnddate());
		}
		if( pamvo.getQrytype()!=null && pamvo.getQrytype()!=-1){
			sql.append("and c.vstatus=? ");
			spm.addParam(pamvo.getQrytype());
		}
		if(!StringUtil.isEmpty(pamvo.getPk_corp())){
			sql.append("and c.pk_corp=? ");
			spm.addParam(pamvo.getPk_corp());
		}
		if(!StringUtil.isEmpty(pamvo.getUser_code())){
			sql.append("and c.vbillcode like ? ");
			spm.addParam("%"+pamvo.getUser_code()+"%");
		}
		if(!StringUtil.isEmpty(pamvo.getCuserid())){
			sql.append("and c.coperatorid = ? ");
			spm.addParam(pamvo.getCuserid());
		}
		sql.append(" order by c.ts desc ");
		qryvo.setSql(sql.toString());
		qryvo.setSpm(spm);
		return qryvo;
	}
	
	/**
	 * 是否是最新数据
	 * @param qryvo
	 */
	private void checkData(StockOutVO qryvo) throws DZFWarpException{
		StockOutVO vo =(StockOutVO) singleObjectBO.queryByPrimaryKey(StockOutVO.class, qryvo.getPk_stockout());
		if(!vo.getUpdatets().equals(qryvo.getUpdatets())){
			throw new BusinessException("单据编号："+vo.getVbillcode()+",数据已发生变化;<br>");
		}
	}
	
	/**
	 * 校验状态能否操作
	 * @param msg
	 * @param vo
	 * @throws DZFWarpException
	 */
	private void checkStatus(String msg,StockOutVO vo) throws DZFWarpException{
		HashMap<Integer,String> map=new HashMap<>();
		map.put(0, "待确认");
		map.put(1, "待发货");
		map.put(2, "待收货");
		String message="订单编号："+vo.getVbillcode()+"是"+map.get(vo.getVstatus())+msg+"<br>";
		if(msg.equals(",不能确认出库;")){
			if(vo.getVstatus()!=0){
				throw new BusinessException(message);
			}
		}
		if(msg.equals(",不能确认发货;")){
			if(vo.getVstatus()!=1){
				throw new BusinessException(message);
			}
		}
	}
	
	
	/**
	 * 设置默认出库单编码
	 * @param vo
	 */
	private void setDefaultCode(StockOutVO vo) {
		MaxCodeVO mcvo=new MaxCodeVO();
		DZFDate now =new DZFDate();
		mcvo.setTbName(vo.getTableName());
		mcvo.setFieldName("vbillcode");
		mcvo.setPk_corp(vo.getPk_corp());
		mcvo.setBillType("ck"+now.getStrMonth()+now.getStrDay());
		mcvo.setCorpIdField("pk_corp");
		mcvo.setDiflen(3);
		vo.setVbillcode(billCode.getDefaultCode(mcvo));
	}
	
}