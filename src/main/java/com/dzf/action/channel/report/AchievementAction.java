package com.dzf.action.channel.report;

import org.apache.log4j.Logger;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;

import com.dzf.action.pub.BaseAction;
import com.dzf.model.channel.report.AchievementVO;
import com.dzf.model.pub.Json;
import com.dzf.model.pub.QryParamVO;
import com.dzf.pub.DzfTypeUtils;
import com.dzf.service.channel.report.IAchievementService;

/**
 * 业绩分析
 * @author zy
 *
 */
@ParentPackage("basePackage")
@Namespace("/report")
@Action(value = "achievementrep")
public class AchievementAction extends BaseAction<AchievementVO> {

	private static final long serialVersionUID = -1847049518952345198L;
	
	private Logger log = Logger.getLogger(this.getClass());
	
	@Autowired
	private IAchievementService achieveser;

	/**
	 * 查询业绩环比
	 */
	public void queryLine() {
		Json json = new Json();
		try {
			QryParamVO paramvo = new QryParamVO();
			paramvo = (QryParamVO)DzfTypeUtils.cast(getRequest(), paramvo);
			paramvo.setCuserid(getLoginUserid());//登陆人员id
			AchievementVO linevo = achieveser.queryLine(paramvo);
			json.setRows(linevo);
			json.setSuccess(true);
			json.setMsg("操作成功");
		} catch (Exception e) {
			printErrorLog(json, log, e, "操作失败");
		}
		writeJson(json);
	}
	
	/**
	 * 查询业绩同比
	 */
	public void queryChart() {
		Json json = new Json();
		try {
			QryParamVO paramvo = new QryParamVO();
			paramvo = (QryParamVO)DzfTypeUtils.cast(getRequest(), paramvo);
			paramvo.setCuserid(getLoginUserid());//登陆人员id
			AchievementVO linevo = achieveser.queryChart(paramvo);
			json.setRows(linevo);
			json.setSuccess(true);
			json.setMsg("操作成功");
		} catch (Exception e) {
			printErrorLog(json, log, e, "操作失败");
		}
		writeJson(json);
	}
	
}