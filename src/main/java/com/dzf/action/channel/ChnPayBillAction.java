package com.dzf.action.channel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.dispatcher.multipart.MultiPartRequestWrapper;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.dzf.action.pub.BaseAction;
import com.dzf.model.channel.ChnPayBillVO;
import com.dzf.model.pub.Grid;
import com.dzf.model.pub.Json;
import com.dzf.model.sys.sys_power.CorpVO;
import com.dzf.pub.BusinessException;
import com.dzf.pub.DzfTypeUtils;
import com.dzf.pub.StringUtil;
import com.dzf.pub.Field.FieldMapping;
import com.dzf.pub.lang.DZFDate;
import com.dzf.pub.util.JSONConvtoJAVA;
import com.dzf.pub.util.QueryUtil;
import com.dzf.service.channel.IChnPayService;

/**
 * 渠道-付款单
 * 
 */
@ParentPackage("basePackage")
@Namespace("/chnpay")
@Action(value = "chnpaybill")
public class ChnPayBillAction extends BaseAction<ChnPayBillVO> {

	private static final long serialVersionUID = -5179718423895024141L;
	
	@Autowired
	private IChnPayService chnpay;

	private Logger log = Logger.getLogger(this.getClass());

	public void query() {
		Grid grid = new Grid();
		try {
			ChnPayBillVO paramvo = new ChnPayBillVO();
		    paramvo = (ChnPayBillVO) DzfTypeUtils.cast(getRequest(), paramvo);
			int page = paramvo == null ?1: paramvo.getPage();
			int rows = paramvo ==null? 100000: paramvo.getRows();
			ChnPayBillVO[]  vos = chnpay.query(paramvo, getLoginUserInfo());
			int len = vos==null?0:vos.length;
			if (vos != null && vos.length > 0) {
				vos = (ChnPayBillVO[]) QueryUtil.getPagedVOs(vos,page,rows);
				grid.setRows(Arrays.asList(vos));
				grid.setTotal((long) (len));
				grid.setMsg("查询成功!");
			} else {
				grid.setRows(new ArrayList<ChnPayBillVO>());
				grid.setTotal(0L);
				grid.setMsg("查询数据为空!");
			}
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
				CorpVO accvo = getLoginCorpInfo();
				File[] files = ((MultiPartRequestWrapper) getRequest()).getFiles("pFile");
				String[] filenames = ((MultiPartRequestWrapper) getRequest()).getFileNames("pFile");
				data=setDefault(data);
	 			data= chnpay.save(data,accvo,getLoginUserInfo().getCuserid(),files,filenames);
				json.setRows(Arrays.asList(data));
				json.setSuccess(true);	
				json.setMsg("保存成功!");
			} catch (Exception e) {
				printErrorLog(json, log, e, "保存失败");
			}
		}else {
			json.setSuccess(false);
			json.setMsg("保存失败");
		}
		writeJson(json);
	}
	
	//新增，默认录入人与日期
	private ChnPayBillVO setDefault(ChnPayBillVO chn){
		if(StringUtil.isEmpty(chn.getPk_paybill())){
			if(chn.getDoperatedate()==null){
				chn.setDoperatedate(new DZFDate());
			}
			chn.setCoperatorid(getLoginUserInfo().getCuserid());
			chn.setSystype(2);
		}
		return chn;
	}
	
	public void updateStatus() {
		Json json = new Json();
		try {
			String chns = getRequest().getParameter("chns");
			String temp = getRequest().getParameter("stat");
			chns = chns.replace("}{", "},{");
			chns = "[" + chns + "]";
			JSONArray array = (JSONArray) JSON.parseArray(chns);
			Map<String, String> map = FieldMapping.getFieldMapping(new ChnPayBillVO());
			ChnPayBillVO[] cons = DzfTypeUtils.cast(array,  map, ChnPayBillVO[].class,
					JSONConvtoJAVA.getParserConfig());
			if (cons.length == 0 || StringUtil.isEmpty(temp)) {
				throw new BusinessException("参数错误");
			}
			int stat = Integer.parseInt(temp);
			if(stat==2){
				cons[0].setSubmitid(getLoginUserid());//提单人
			}
			HashMap<String, Object> cmap = chnpay.updateStatusMult(cons, stat);
			if(cmap.get("stat").equals("1")){
				json.setStatus(-1);
			}
			String errmsg = (String)cmap.get("errmsg");
			json.setSuccess(true);
			json.setMsg(errmsg);
		} catch (Exception e) {
			printErrorLog(json, log, e, "操作失败");
		}
		writeJson(json);
	}

	/**
	 * 查询单个数据
	 */
	public void queryByID(){
		Json json = new Json();
		try {
			ChnPayBillVO chn = new ChnPayBillVO();
			chn = (ChnPayBillVO) DzfTypeUtils.cast(getRequest(), chn);
			if(StringUtil.isEmpty(chn.getPk_paybill())){
				throw new BusinessException("主键为空");
			}
			if (StringUtil.isEmpty(chn.getPk_corp())){
				chn.setPk_corp(getLogincorppk());
			}
			ChnPayBillVO recust = chnpay.queryByID(chn.getPk_paybill());
			json.setSuccess(true);
			json.setRows(recust);
			json.setMsg("查询成功!");
		} catch (Exception e) {
			printErrorLog(json, log, e, "查询失败");
		}
		writeJson(json);
	}

	public void delete() {
		Json json = new Json();
		try {
			String pk_corp = getLogincorppk();
			String cids=getRequest().getParameter("bids");
//			if (StringUtil.isEmpty(pk_corp)) {
//				pk_corp = getLoginCorpInfo().getPk_corp();
//			}
//			CheckUtil.checkPower(pk_corp, IMsgConstant.ACTION_SELF, getLoginUserInfo());
			chnpay.delete(getLogincorppk(),cids);
			json.setSuccess(true);
			json.setRows(data);
			json.setMsg("删除成功!");
		} catch (Exception e) {
			printErrorLog(json, log, e, "删除失败");
		}
		writeJson(json);
	}
	
	
	/****
	 * 获取附件显示图片
	 */
	public void getAttachImage(){
		InputStream is = null;
		OutputStream os = null;
		try {
			ChnPayBillVO vo=new ChnPayBillVO();
			vo = (ChnPayBillVO) DzfTypeUtils.cast(getRequest(), vo);
			if(StringUtil.isEmpty(vo.getPk_paybill())){
				throw new BusinessException("主键为空");
			}
			if (StringUtil.isEmpty(vo.getPk_corp())){
				vo.setPk_corp(getLogincorppk());
			}
			vo = chnpay.queryByID(vo.getPk_paybill());
			boolean isexists = true;
			if(vo==null){
				isexists = false;
			}
			 String fpath = vo.getVfilepath();
			 File afile = new File(fpath);
			 if(!afile.exists()){
				 isexists = false;
			 }
			 if(isexists){
				 String path = getRequest().getSession().getServletContext().getRealPath("/");
				 String typeiconpath = path + "images" + File.separator + "typeicon" + File.separator;
				 os = getResponse().getOutputStream();
				 is = new FileInputStream(afile);
				 IOUtils.copy(is, os);
			 }
		} catch (Exception e) {
			
		}finally{
		    try {
                if(os != null){
                    os.close();
                }
                if(is!=null){
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
		}
	}
	
	public void deleteImageFile(){
		Json json = new Json();
		try {
			ChnPayBillVO paramvo = new ChnPayBillVO();
			paramvo = (ChnPayBillVO) DzfTypeUtils.cast(getRequest(), paramvo);
			if (StringUtil.isEmpty(paramvo.getPk_corp())){
				paramvo.setPk_corp(getLogincorppk());
			}
			if(!StringUtil.isEmpty(paramvo.getPk_paybill())){
				chnpay.deleteImageFile(paramvo);
			}
			json.setSuccess(true);
			json.setMsg("删除附件成功");
			json.setRows(0);
		} catch (Exception e) {
			printErrorLog(json, log, e, "删除附件失败");
		}
		writeJson(json);
	}
	
}