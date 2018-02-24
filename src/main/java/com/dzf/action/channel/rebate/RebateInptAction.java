package com.dzf.action.channel.rebate;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;

import com.dzf.action.pub.BaseAction;
import com.dzf.model.channel.rebate.ManagerRefVO;
import com.dzf.model.channel.rebate.RebateVO;
import com.dzf.model.pub.Grid;
import com.dzf.model.pub.IStatusConstant;
import com.dzf.model.pub.Json;
import com.dzf.model.pub.QryParamVO;
import com.dzf.model.sys.sys_power.UserVO;
import com.dzf.pub.BusinessException;
import com.dzf.pub.DZFWarpException;
import com.dzf.pub.DzfTypeUtils;
import com.dzf.pub.StringUtil;
import com.dzf.pub.lang.DZFDate;
import com.dzf.pub.lang.DZFDateTime;
import com.dzf.pub.util.QueryUtil;
import com.dzf.service.channel.rebate.IRebateInptService;

/**
 * 返点单录入
 * @author zy
 *
 */
@ParentPackage("basePackage")
@Namespace("/rebate")
@Action(value = "rebateinpt")
public class RebateInptAction extends BaseAction<RebateVO> {

	private static final long serialVersionUID = 9155544110565105231L;
	
	private Logger log = Logger.getLogger(this.getClass());
	
	@Autowired
	private IRebateInptService rebateser;
	
	/**
	 * 查询
	 */
	public void query(){
		Grid grid = new Grid();
		try {
			UserVO uservo = getLoginUserInfo();
			if(uservo != null && !"000001".equals(uservo.getPk_corp()) ){
				throw new BusinessException("登陆用户错误");
			}else if(uservo == null){
				throw new BusinessException("登陆用户错误");
			}
			QryParamVO paramvo = new QryParamVO();
			paramvo = (QryParamVO)DzfTypeUtils.cast(getRequest(), paramvo);
			int page = paramvo == null ? 1: paramvo.getPage();
            int rows = paramvo == null ? 100000: paramvo.getRows();
            List<RebateVO> list = rebateser.query(paramvo);
			if(list != null && list.size() > 0){
				RebateVO[] rebatVOs = (RebateVO[]) QueryUtil.getPagedVOs(list.toArray(new RebateVO[0]), page, rows);
				grid.setRows(Arrays.asList(rebatVOs));
				grid.setTotal((long)(list.size()));
			}else{
			    grid.setRows(list);
			    grid.setTotal(0L);
			}
            grid.setMsg("查询成功");
            grid.setSuccess(true);
		} catch (Exception e) {
			printErrorLog(grid, log, e, "查询失败");
		}
		writeJson(grid);
	}

	/**
	 * 保存
	 */
	public void save() {
		Json json = new Json();
		if (data != null) {
			try {
				UserVO uservo = getLoginUserInfo();
				if(uservo != null && !"000001".equals(uservo.getPk_corp()) ){
					throw new BusinessException("登陆用户错误");
				}else if(uservo == null){
					throw new BusinessException("登陆用户错误");
				}
				setDefaultValue(data);
				data = rebateser.save(data, getLogincorppk());
				json.setRows(data);
				json.setSuccess(true);	
				json.setMsg("保存成功");
			} catch (Exception e) {
				printErrorLog(json, log, e, "保存失败");
			}
		}else {
			json.setSuccess(false);
			json.setMsg("保存失败");
		}
		writeJson(json);
	}
	
	/**
	 * 保存前设置默认值
	 * @throws DZFWarpException
	 */
	private void setDefaultValue(RebateVO data) throws DZFWarpException{
		data.setFathercorp(getLogincorppk());
		data.setCoperatorid(getLoginUserid());
		data.setDoperatedate(new DZFDate());
		data.setTstamp(new DZFDateTime());
		data.setTs(new DZFDateTime());
		data.setDr(0);
		data.setIstatus(IStatusConstant.irebatestatus_0);//待提交
		if(!StringUtil.isEmpty(data.getVbillcode())){
			data.setVbillcode(data.getVbillcode().trim());
		}
		data.setVsperiod(getPeriod(data, true));
		data.setVeperiod(getPeriod(data, false));
	}
	
	/**
	 * 获取季度的起止期间
	 * @param data
	 * @param ptype
	 * @return
	 */
	private String getPeriod(RebateVO data, boolean isbegin){
		if(StringUtil.isEmpty(data.getVyear())){
			throw new BusinessException("所属年不能为空");
		}
		if(data.getIseason() == null){
			throw new BusinessException("所属季度不能为空");
		}
		String month = "";
		switch(data.getIseason()){
			case 1:
				if(isbegin){
					month = "-01";
				}else{
					month = "-03";
				}
				break;
			case 2:
				if(isbegin){
					month = "-04";
				}else{
					month = "-06";
				}
				break;
			case 3:
				if(isbegin){
					month = "-07";
				}else{
					month = "-09";
				}
				break;
			case 4:
				if(isbegin){
					month = "-10";
				}else{
					month = "-12";
				}
				break;
		}
		return data.getVyear()+month;
	}
	
	/**
	 * 渠道经理参照查询
	 */
	public void queryManager(){
		Grid grid = new Grid();
		try {
			UserVO uservo = getLoginUserInfo();
			if(uservo != null && !"000001".equals(uservo.getPk_corp()) ){
				throw new BusinessException("登陆用户错误");
			}else if(uservo == null){
				throw new BusinessException("登陆用户错误");
			}
			QryParamVO paramvo = new QryParamVO();
			paramvo = (QryParamVO)DzfTypeUtils.cast(getRequest(), paramvo);
			int page = paramvo == null ? 1: paramvo.getPage();
            int rows = paramvo == null ? 100000: paramvo.getRows();
			List<ManagerRefVO> list = rebateser.queryManagerRef(paramvo);
			if(list != null && list.size() > 0){
				ManagerRefVO[] refVOs = (ManagerRefVO[]) QueryUtil.getPagedVOs(list.toArray(new ManagerRefVO[0]), page, rows);
				grid.setRows(Arrays.asList(refVOs));
				grid.setTotal((long)(list.size()));
			}else{
			    grid.setRows(list);
			    grid.setTotal(0L);
			}
            grid.setMsg("查询成功");
            grid.setSuccess(true);
		} catch (Exception e) {
			printErrorLog(grid, log, e, "查询失败");
		}
		writeJson(grid);
	}
	
	public void delete() {
		Json json = new Json();
		if (data != null) {
			try {
				UserVO uservo = getLoginUserInfo();
				if(uservo != null && !"000001".equals(uservo.getPk_corp()) ){
					throw new BusinessException("登陆用户错误");
				}else if(uservo == null){
					throw new BusinessException("登陆用户错误");
				}
				rebateser.delete(data);
				json.setSuccess(true);
				json.setMsg("操作成功");
			} catch (Exception e) {
				printErrorLog(json, log, e, "操作失败");
			}
		} else {
			json.setSuccess(false);
			json.setMsg("操作数据为空");
		}
		writeJson(json);
	}
}
