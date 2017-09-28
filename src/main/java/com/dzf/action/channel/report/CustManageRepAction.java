package com.dzf.action.channel.report;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;

import com.dzf.action.pub.BaseAction;
import com.dzf.model.channel.report.CustCountVO;
import com.dzf.model.channel.report.CustManageRepVO;
import com.dzf.model.pub.Grid;
import com.dzf.model.pub.QryParamVO;
import com.dzf.pub.DzfTypeUtils;
import com.dzf.pub.StringUtil;
import com.dzf.pub.lang.DZFDate;
import com.dzf.pub.util.QueryUtil;
import com.dzf.service.channel.report.ICustManageRep;

/**
 * 加盟商客户管理
 * @author zy
 *
 */
@ParentPackage("basePackage")
@Namespace("/report")
@Action(value = "custmanagerep")
public class CustManageRepAction extends BaseAction<CustManageRepVO> {

	private static final long serialVersionUID = 2114395045979123542L;
	
	private Logger log = Logger.getLogger(this.getClass());
	
	@Autowired
	private ICustManageRep custServ;

	/**
	 * 查询
	 */
	public void query(){
		Grid grid = new Grid();
		try {
			QryParamVO paramvo = new QryParamVO();
			paramvo = (QryParamVO)DzfTypeUtils.cast(getRequest(), paramvo);
			if(StringUtil.isEmpty(paramvo.getPk_corp())){
				paramvo.setPk_corp(getLogincorppk());
			}
			paramvo.setBegdate(new DZFDate());
			List<CustManageRepVO> list = custServ.query(paramvo);
			int page = paramvo == null ? 1 : paramvo.getPage();
			int rows = paramvo ==null ? 10000 : paramvo.getRows();
		    int len = list == null ? 0 : list.size();
		    if (len > 0) {
				grid.setTotal((long) (len));
				grid.setRows(Arrays.asList(QueryUtil.getPagedVOs(list.toArray(new CustManageRepVO[0]), page, rows)));
				grid.setSuccess(true);
				grid.setMsg("查询成功");
			}else{
				grid.setTotal(Long.valueOf(0));
				grid.setRows(list);
				grid.setSuccess(true);
				grid.setMsg("查询结果为空");
			}
		} catch (Exception e) {
			printErrorLog(grid, log, e, "查询失败");
		}
		writeJson(grid);
	}
	
	/**
	 * 行业查询
	 */
	public void queryIndustry(){
		Grid grid = new Grid();
		try {
			QryParamVO paramvo = new QryParamVO();
			paramvo = (QryParamVO)DzfTypeUtils.cast(getRequest(), paramvo);
			if(StringUtil.isEmpty(paramvo.getPk_corp())){
				paramvo.setPk_corp(getLogincorppk());
			}
			List<CustCountVO> list = custServ.queryIndustry(paramvo);
		    grid.setRows(list);
			grid.setSuccess(true);
			grid.setMsg("查询成功");
		} catch (Exception e) {
			printErrorLog(grid, log, e, "查询失败");
		}
		writeJson(grid);
	}

}
