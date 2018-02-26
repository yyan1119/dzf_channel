package com.dzf.service.channel.chn_set.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dzf.dao.bs.SingleObjectBO;
import com.dzf.dao.jdbc.framework.SQLParameter;
import com.dzf.dao.jdbc.framework.processor.BeanListProcessor;
import com.dzf.dao.jdbc.framework.processor.BeanProcessor;
import com.dzf.dao.jdbc.framework.processor.ColumnProcessor;
import com.dzf.dao.multbs.MultBodyObjectBO;
import com.dzf.model.channel.sale.ChnAreaBVO;
import com.dzf.model.channel.sale.ChnAreaVO;
import com.dzf.model.pub.ComboBoxVO;
import com.dzf.model.sys.sys_power.CorpVO;
import com.dzf.model.sys.sys_power.UserVO;
import com.dzf.pub.BusinessException;
import com.dzf.pub.DZFWarpException;
import com.dzf.pub.StringUtil;
import com.dzf.pub.SuperVO;
import com.dzf.pub.cache.CorpCache;
import com.dzf.pub.cache.UserCache;
import com.dzf.service.channel.chn_set.IChnAreaService;

@Service("chn_area")
public class ChnAreaServiceImpl implements IChnAreaService {
	
	@Autowired
	private SingleObjectBO singleObjectBO = null;
	
	@Autowired
	private MultBodyObjectBO multBodyObjectBO = null;

	@SuppressWarnings("unchecked")
	@Override
	public ChnAreaVO[] query(ChnAreaVO qvo) throws DZFWarpException {
		StringBuffer sql = new StringBuffer();
		SQLParameter sp = new SQLParameter();
		sql.append("select * from cn_chnarea where nvl(dr,0) = 0");
		sql.append(" and pk_corp=? order by areacode " );   
		sp.addParam(qvo.getPk_corp());
		List<ChnAreaVO> vos =(List<ChnAreaVO>) singleObjectBO.executeQuery(sql.toString(), sp,new BeanListProcessor(ChnAreaVO.class));
		ChnAreaBVO[] bvos = queryBy1ID(null);
		Map<String, StringBuffer> map = new HashMap<String, StringBuffer>();
		if(bvos!=null&&bvos.length>0){
			for (ChnAreaBVO chnAreaBVO : bvos) {
				if(map.containsKey(chnAreaBVO.getPk_chnarea())){
					StringBuffer sf=map.get(chnAreaBVO.getPk_chnarea()).append(chnAreaBVO.getVprovname()).append(",");
					map.put(chnAreaBVO.getPk_chnarea(), sf);
				}else{
					StringBuffer sf=new StringBuffer();
					sf.append(chnAreaBVO.getVprovname()).append(",");
					map.put(chnAreaBVO.getPk_chnarea(), sf);
				}
			}
		}
		if(vos != null){
			UserVO uvo = null;
			for (ChnAreaVO chnAreaVO : vos) {
				uvo=UserCache.getInstance().get(chnAreaVO.getUserid(), null);
				if(uvo != null){
					chnAreaVO.setUsername(uvo.getUser_name());
				}
				StringBuffer isf = map.get(chnAreaVO.getPk_chnarea());
				if(isf!=null){
					chnAreaVO.setVprovnames(isf.toString().substring(0,isf.toString().length()-1));
				}
			}
		}
		return vos.toArray(new ChnAreaVO[0]);
	}
	

	@Override
	public ChnAreaVO save(ChnAreaVO vo) throws DZFWarpException {
		vo.setAreacode(vo.getAreacode().replaceAll(" ", ""));
		vo.setAreaname(vo.getAreaname().replaceAll(" ", ""));
		if(!checkIsUnique(vo) ){
			throw new BusinessException("大区编码或名称重复,请重新输入");
		}
		if(!StringUtil.isEmpty(vo.getPk_chnarea())){
			ChnAreaVO oldvo =(ChnAreaVO) singleObjectBO.queryByPrimaryKey(ChnAreaVO.class, vo.getPk_chnarea());
			vo.setDoperatedate(oldvo.getDoperatedate());
			vo.setCoperatorid(oldvo.getCoperatorid());
			vo.setTs(oldvo.getTs());
			SQLParameter sp = new SQLParameter();
			StringBuffer sql = new StringBuffer();
			sp.addParam(vo.getPk_chnarea());
			sql.append("update cn_chnarea_b set dr = 1 where pk_chnarea=? and nvl(dr,0)=0  ");
			singleObjectBO.executeUpdate(sql.toString(), sp);
		}
		vo=(ChnAreaVO) multBodyObjectBO.saveMultBObject(vo.getPk_corp(), vo);
		if(!checkCorpIsOnly() ){
			throw new BusinessException("加盟商重复,请重新输入");
		}
		SuperVO[] bvos = (SuperVO[])vo.getTableVO("cn_chnarea_b");
		vo.setChildren(bvos);
		return vo;
	}
	
	/**
	 * 校验(负责地区+渠道经理)是否重复
	 * @param vo
	 * @return
	 */
	private boolean checkCorpIsOnly() {
		boolean ret = false;
		StringBuffer sql = new StringBuffer();
		sql.append(" select count(1) as count from ( ");
		sql.append(" select count(1)  from cn_chnarea_b where pk_corp!=null ");
		sql.append(" group by pk_corp having count(1)>1) ");
		String res = singleObjectBO.executeQuery(sql.toString(), null, new ColumnProcessor("count")).toString();
		int num = Integer.valueOf(res);
		if(num <= 0)
			ret = true;
		return ret;
	}
	
	/**
	 * 校验大区步骤,大区编码
	 * @param vo
	 * @return
	 */
	private boolean checkIsUnique(ChnAreaVO vo) {
		boolean ret = false;
		StringBuffer sql = new StringBuffer();
		SQLParameter sp = new SQLParameter();
		sql.append("select count(1) as count from cn_chnarea");
		sql.append(" where pk_corp=? and nvl(dr,0) = 0 ");
		sql.append(" and (areaname = ? or areacode = ?)");
		sp.addParam(vo.getPk_corp());
		sp.addParam(vo.getAreaname());
		sp.addParam(vo.getAreacode());
		if(!StringUtil.isEmpty(vo.getPk_chnarea())){
			sql.append(" and pk_chnarea != ?");
			sp.addParam(vo.getPk_chnarea());
		}
		String res = singleObjectBO.executeQuery(sql.toString(), sp, new ColumnProcessor("count")).toString();
		int num = Integer.valueOf(res);
		if(num <= 0)
			ret = true;
		return ret;
	}
	
	/**
	 * 通过主表主键查询主从表数据
	 */
	@Override
	public ChnAreaVO queryByPrimaryKey(String pk) throws DZFWarpException {
		ChnAreaVO hvo = (ChnAreaVO)singleObjectBO.queryVOByID(pk, ChnAreaVO.class);
		UserVO user = UserCache.getInstance().get(hvo.getUserid(), null);
		if(user != null){
			hvo.setUsername(user.getUser_name());
		}
		if(hvo != null){
			ChnAreaBVO[] bvos = queryBy1ID(pk);
			hvo.setChildren(bvos); 
		}
		return hvo;
	}
	
	private ChnAreaBVO[] queryBy1ID(String pk) throws DZFWarpException {
		StringBuffer corpsql = new StringBuffer();
		SQLParameter sp = new SQLParameter();
		corpsql.append("SELECT pk_chnarea,pk_corp,vprovince,vprovname,ischarge,userid,vmemo,ts");
		corpsql.append(" FROM cn_chnarea_b where nvl(dr,0)= 0");
		if(pk!=null){
			corpsql.append(" and pk_chnarea = ? ");
			sp.addParam(pk);
		}
		corpsql.append(" order by ts desc");
		Map<String, ChnAreaBVO > map = new HashMap<String,ChnAreaBVO>();
		List<ChnAreaBVO> b1vos = (List<ChnAreaBVO>) singleObjectBO.executeQuery(corpsql.toString(),sp,new BeanListProcessor(ChnAreaBVO.class));
		int i=0;
		CorpVO cvo =null;
		UserVO user =null;
		for (ChnAreaBVO chnAreaBVO : b1vos) {
			user = UserCache.getInstance().get(chnAreaBVO.getUserid(), null);
			if(user != null){
				chnAreaBVO.setUsername(user.getUser_name());
			}
			cvo=CorpCache.getInstance().get(null, chnAreaBVO.getPk_corp());
			if(!StringUtil.isEmpty(chnAreaBVO.getUserid())){
				String id=chnAreaBVO.getVprovince()+chnAreaBVO.getUserid();
				if(map.containsKey(id)){
					ChnAreaBVO vo = map.get(id);
					if(!StringUtil.isEmpty(chnAreaBVO.getPk_corp())){
						vo.setPk_corp(vo.getPk_corp()+","+chnAreaBVO.getPk_corp());
						vo.setCorpname(vo.getCorpname()+","+cvo.getUnitname());
					}
					map.put(id,vo);
				}else{
					if(cvo!=null){
						chnAreaBVO.setCorpname(cvo.getUnitname());
					}
					map.put(id,chnAreaBVO);
				}
			}else{
				if(cvo!=null){
					chnAreaBVO.setCorpname(cvo.getUnitname());
				}
				map.put(i+"",chnAreaBVO);
				i++;
			}
		}
		Collection<ChnAreaBVO> vos = map.values();
		List<ChnAreaBVO> list= new ArrayList<ChnAreaBVO>(vos);
		return list.toArray(new ChnAreaBVO[0]);
	}
	
	@Override
	public void delete(String pk,String pk_corp) throws DZFWarpException {
		if (!StringUtil.isEmpty(pk)) {
			SQLParameter sp = new SQLParameter();
			StringBuffer main_sql = new StringBuffer();
			StringBuffer depe_sql = new StringBuffer();
			sp.addParam(pk);
			depe_sql.append("update cn_chnarea_b set dr = 1 where pk_chnarea= ? and nvl(dr,0)=0");
			singleObjectBO.executeUpdate(depe_sql.toString(), sp);
			sp.addParam(pk_corp);
			main_sql.append("update cn_chnarea set dr = 1 where pk_chnarea = ? and pk_corp =? and nvl(dr,0)=0");
			singleObjectBO.executeUpdate(main_sql.toString(), sp);
		}else{
			throw new BusinessException("删除失败");
		}
	}

	@Override
	public String queryManager(String pk_corp) throws DZFWarpException {
		String sql="select vdeptuserid userid from cn_leaderset where nvl(dr,0)=0 and pk_corp=? ";
		SQLParameter sp = new SQLParameter();
		sp.addParam(pk_corp);
		ChnAreaBVO vo =(ChnAreaBVO) singleObjectBO.executeQuery(sql, sp, new BeanProcessor(ChnAreaBVO.class));
		String username=null;
		if(vo!=null&&!StringUtil.isEmpty(vo.getUserid())){
			UserVO uvo=UserCache.getInstance().get(vo.getUserid(), null);
			if(uvo!=null){
				username=uvo.getUser_name();
			}
		}
		return username;
	}
	
	@Override
	public ArrayList queryComboxArea(String pk_area) throws DZFWarpException {
		StringBuffer sql = new StringBuffer();
		SQLParameter sp = new SQLParameter();
		sql.append("select region_id as id, region_name as name\n");
		sql.append("  from ynt_area\n");
		sql.append(" where nvl(dr, 0) = 0\n");
		sql.append("   and parenter_id = 1 and region_id not in  ");
		sql.append("    (select vprovince from cn_chnarea_b where nvl(dr,0)=0 ");
		if(!StringUtil.isEmpty(pk_area)){
			sql.append(" and pk_chnarea != ?");
			sp.addParam(pk_area);
		}
		sql.append("   ) order by region_id asc ");
		ArrayList list = (ArrayList) singleObjectBO.executeQuery(sql.toString(), sp,new BeanListProcessor(ComboBoxVO.class));
		return list;
	}
	
}
