package com.dzf.service.channel.matmanage;

import java.util.List;

import com.dzf.model.channel.matmanage.MatOrderBVO;
import com.dzf.model.channel.matmanage.MatOrderVO;
import com.dzf.model.channel.matmanage.MaterielFileVO;
import com.dzf.model.sys.sys_power.UserVO;

public interface IMatApplyService {

	int queryTotalRow(MatOrderVO pamvo, String stype);

	List<MatOrderVO> query(MatOrderVO pamvo, UserVO uservo, String stype);

	List<MatOrderBVO> queryNumber(MatOrderVO pamvo);

	List<MaterielFileVO> queryMatFile();

	List<MatOrderVO> queryAllProvince();

	List<MatOrderVO> queryCityByProId(Integer pid);

	List<MatOrderVO> queryAreaByCid(Integer cid);

	MatOrderVO showDataByCorp(String corpid);

	String saveApply(MatOrderVO vo, UserVO uservo, MatOrderBVO[] bvos, String type, String stype);

	MatOrderVO queryDataById(String id, UserVO uservo, String type, String stype);

	void delete(MatOrderVO param);

}
