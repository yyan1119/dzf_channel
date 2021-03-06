package com.dzf.action.branch.reportmanage;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.dzf.action.channel.expfield.ExpireContractExcelField;
import com.dzf.action.pub.BaseAction;
import com.dzf.model.branch.reportmanage.QueryContractVO;
import com.dzf.model.branch.setup.BranchInstSetupVO;
import com.dzf.model.channel.matmanage.MaterielFileVO;
import com.dzf.model.pub.Grid;
import com.dzf.model.pub.QryParamVO;
import com.dzf.model.sys.sys_power.UserVO;
import com.dzf.pub.BusinessException;
import com.dzf.pub.DZFWarpException;
import com.dzf.pub.DzfTypeUtils;
import com.dzf.pub.StringUtil;
import com.dzf.pub.Field.FieldMapping;
import com.dzf.pub.excel.Excelexport2003;
import com.dzf.pub.util.JSONConvtoJAVA;
import com.dzf.service.branch.reportmanage.IBranchExpireContractService;

/**
 * 到期合同统计
 */
@ParentPackage("basePackage")
@Namespace("/branch")
@Action(value = "expcontract")
public class BranchExpireContractAction extends BaseAction<QueryContractVO>{
	
	private Logger log = Logger.getLogger(this.getClass());
	
	@Autowired
	private IBranchExpireContractService exconser;
	
	/**
	 * 查询列表
	 */
	public void query(){
		Grid grid = new Grid();
		try{
			UserVO uservo = getLoginUserInfo();
			checkUser(uservo);
			QueryContractVO param = new QueryContractVO();
			param = (QueryContractVO) DzfTypeUtils.cast(getRequest(), param);
			
			QryParamVO paramvo = new QryParamVO();
			paramvo = (QryParamVO) DzfTypeUtils.cast(getRequest(), paramvo);
			if(paramvo == null){
				paramvo = new QryParamVO();
			}
			int page = paramvo.getPage();
			int rows = paramvo.getRows();
			
			List<QueryContractVO> list = exconser.query(param,uservo);
			if (list != null && list.size() > 0) {
				List<Integer> ilist = list.get(0).getiList();
				for(int i=ilist.size()-1;i>=0;i--){
					list.remove(ilist.get(i).intValue());
				}
				QueryContractVO[] cvos = getPagedVOs(list.toArray(new QueryContractVO[0]), page, rows);
				grid.setRows(Arrays.asList(cvos));
				grid.setTotal((long) (list.size()));
				grid.setMsg("查询成功");
			}else{
				grid.setRows(new ArrayList<QueryContractVO>());
				grid.setMsg("查询数据为空");
				grid.setTotal(0L);
				
			}
			grid.setSuccess(true);
		}catch (Exception e) {
			grid.setSuccess(false);
			printErrorLog(grid, log, e, "查询失败");
		}
		writeJson(grid);
	}
	
	/**
	 * 登录用户校验
	 * @throws DZFWarpException
	 */
	private void checkUser(UserVO uservo) throws DZFWarpException {
		if(uservo != null && !"000001".equals(uservo.getPk_corp()) ){
			throw new BusinessException("登陆用户错误！");
		}else if(uservo == null){
			throw new BusinessException("请先登录！");
		}
	}

	
	
	private QueryContractVO[] getPagedVOs(QueryContractVO[] cvos, int page, int rows) {
		int beginIndex = rows * (page - 1);
		int endIndex = rows * page;
		if (endIndex >= cvos.length) {// 防止endIndex数组越界
			endIndex = cvos.length;
		}
		cvos = Arrays.copyOfRange(cvos, beginIndex, endIndex);
		return cvos;
	}
	
	
	/**
	 * 查询所有机构名称
	 */
	public void queryComboBox(){
		Grid grid = new Grid();
		try {
			UserVO uservo = getLoginUserInfo();
			List<BranchInstSetupVO> list = exconser.queryComboBox(uservo);
			if (list == null || list.size() == 0) {
				grid.setRows(new ArrayList<MaterielFileVO>());
				grid.setSuccess(true);
				grid.setMsg("查询数据为空");
			} else {
				grid.setRows(list);
				grid.setSuccess(true);
				grid.setMsg("查询成功");
			}
		} catch (Exception e) {
			printErrorLog(grid, log, e, "查询失败");
		}
		writeJson(grid);
	}
	
	
	
	/**
	 * 导出
	 */
	public void exportAuditExcel() {
		String strlist =getRequest().getParameter("strlist");
		String qj = getRequest().getParameter("qj");
		if(StringUtil.isEmpty(strlist)){
			throw new BusinessException("导出数据不能为空!");
		}	
		JSONArray exparray = (JSONArray) JSON.parseArray(strlist);
		Map<String, String> mapping = FieldMapping.getFieldMapping(new QueryContractVO());
		QueryContractVO[] expVOs = DzfTypeUtils.cast(exparray, mapping,QueryContractVO[].class, JSONConvtoJAVA.getParserConfig());
		HttpServletResponse response = getResponse();
		Excelexport2003<QueryContractVO> ex = new Excelexport2003<>();
		ExpireContractExcelField fields = new ExpireContractExcelField();
		fields.setVos(expVOs);
		fields.setQj(qj);
		ServletOutputStream servletOutputStream = null;
		OutputStream toClient = null;
		try {
			response.reset();
			// 设置response的Header
			String filename = fields.getExcelport2003Name();
			String formattedName = URLEncoder.encode(filename, "UTF-8");
	        response.addHeader("Content-Disposition", "attachment;filename=" + filename + ";filename*=UTF-8''" + formattedName);
			servletOutputStream = response.getOutputStream();
			toClient = new BufferedOutputStream(servletOutputStream);
			response.setContentType("applicationnd.ms-excel;charset=gb2312");
			ex.exportExcel(fields, toClient);
		} catch (Exception e) {
			log.error("导出失败",e);
		}  finally {
			if(toClient != null){
				try {
					toClient.close();
				} catch (IOException e) {
					log.error("导出失败",e);
				}
			}
			if(servletOutputStream != null){
				try {
					servletOutputStream.flush();
					servletOutputStream.close();
				} catch (IOException e) {
					log.error("导出失败",e);
				}
			}
		}
	}
	

}
