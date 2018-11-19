package com.dzf.service.channel.dealmanage.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dzf.dao.bs.SingleObjectBO;
import com.dzf.dao.jdbc.framework.SQLParameter;
import com.dzf.dao.multbs.MultBodyObjectBO;
import com.dzf.model.channel.stock.GoodsNumVO;
import com.dzf.model.channel.stock.StockOutVO;
import com.dzf.model.pub.QrySqlSpmVO;
import com.dzf.pub.DZFWarpException;
import com.dzf.pub.StringUtil;
import com.dzf.service.channel.dealmanage.IGoodsNumService;

@Service("numGoods")
public class GoodsNumServiceImpl implements IGoodsNumService {
	
	@Autowired
	private SingleObjectBO singleObjectBO;
	
	@Autowired
	private MultBodyObjectBO multBodyObjectBO;
	
	
	@Override
	public List<GoodsNumVO> query(GoodsNumVO qvo) throws DZFWarpException{
		QrySqlSpmVO sqpvo =  getQrySqlSpm(qvo);
		List<GoodsNumVO> list = (List<GoodsNumVO>) multBodyObjectBO.queryDataPage(GoodsNumVO.class, 
				sqpvo.getSql(), sqpvo.getSpm(), qvo.getPage(), qvo.getRows(), null);
		return list;
	}
	
	@Override
	public Integer queryTotalRow(GoodsNumVO qvo) throws DZFWarpException {
		QrySqlSpmVO sqpvo =  getQrySqlSpm(qvo);
		return multBodyObjectBO.queryDataTotal(StockOutVO.class,sqpvo.getSql(), sqpvo.getSpm());
	}
	
	/**
	 * 获取查询条件
	 * @param pamvo
	 * @return
	 * @throws DZFWarpException
	 */
	private QrySqlSpmVO getQrySqlSpm(GoodsNumVO qvo) throws DZFWarpException {
		QrySqlSpmVO qryvo = new QrySqlSpmVO();
		StringBuffer sql = new StringBuffer();
		SQLParameter spm = new SQLParameter();
		sql.append(" select t.vname, ");
		sql.append(" 		s.invspec,s.invtype, ");
		sql.append("        g.vgoodscode, ");
		sql.append("        g.vgoodsname, ");
		sql.append("        num.istocknum - nvl(num.ioutnum, 0) istocknum, ");
		sql.append("        nvl(num.isellnum, 0)  ilocknum, ");
		sql.append("        num.istocknum  - nvl(num.isellnum, 0) iusenum");
		sql.append("   from cn_stocknum num ");
		sql.append("   left join cn_goods g on num.pk_goods = g.pk_goods ");
		sql.append("                       and num.pk_goods = g.pk_goods ");
		sql.append("   left join cn_goodstype t on g.pk_goodstype = t.pk_goodstype ");
		sql.append("   left join cn_goodsspec s on num.pk_goodsspec = s.pk_goodsspec ");
		sql.append("  where nvl(g.dr, 0) = 0 ");
		sql.append("    and nvl(num.dr, 0) = 0 ");
		sql.append("    and nvl(t.dr, 0) = 0 ");
		sql.append("    and nvl(s.dr, 0) = 0 ");
		if(!StringUtil.isEmpty(qvo.getPk_goodstype())){
			sql.append("and g.pk_goodstype=? ");
			spm.addParam(qvo.getPk_goodstype());
		}
		if(!StringUtil.isEmpty(qvo.getVgoodscode())){
			sql.append("and g.vgoodscode like ? ");
			spm.addParam("%"+qvo.getVgoodscode()+"%");
		}
		if(!StringUtil.isEmpty(qvo.getVgoodsname())){
			sql.append("and g.vgoodsname like ? ");
			spm.addParam("%"+qvo.getVgoodsname()+"%");
		}
		sql.append(" order by t.vname,g.vgoodsname ");
		qryvo.setSql(sql.toString());
		qryvo.setSpm(spm);
		return qryvo;
	}
	

}