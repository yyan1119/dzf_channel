<%@ page language="java" pageEncoding="UTF-8"%>
<%@page import="com.dzf.pub.UpdateGradeVersion"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<title>物料处理</title>
<jsp:include page="../../inc/easyui.jsp"></jsp:include>
<link href=<%UpdateGradeVersion.outversion(out, "../../css/index.css");%> rel="stylesheet">
<script src=<%UpdateGradeVersion.outversion(out, "../../js/easyuiext.js");%> charset="UTF-8" type="text/javascript"></script>
<script src=<%UpdateGradeVersion.outversion(out, "../../js/channel/matmanage/mathandle.js");%> charset="UTF-8" type="text/javascript"></script>
<script src=<%UpdateGradeVersion.outversion(out,"../../js/impexcel.js");%> charset="UTF-8" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/jslib/jquery-easyui-1.4.3/datagrid-detailview.js" charset="UTF-8" type="text/javascript"></script>
<style>
#mat_add div.panel.datagrid {
	margin-left: 132px;
	margin-top: -21px;
}
#infoform div.panel.datagrid {
	margin-left: 132px;
	margin-top: -21px;
}
.selecticon {
    background: url(../../img/add_lan.png) no-repeat;
} 
</style>	
</head>
<body>
	<!-- 列表界面 begin -->
	
	<div id="listPanel" class="wrapper" style="width: 100%;overflow:hidden; height: 100%;">
		<div class="mod-toolbar-top">
			<div class="mod-toolbar-content">
				<div class="left mod-crumb">
					<div class="h30 h30-arrow" id="cxjs">
						<label class="mr5">查询：</label>
						<strong id="jqj"></strong>
						<span class="arrow-date"></span>
					</div>
				</div>
				<div class="left mod-crumb">
					<span class="cur"></span>
				</div>
				<div class="left mod-crumb">
					<div style="margin:6px 0px 0px 10px;float:right;font-size:14px;">
						<a href="javascript:void(0)"  style="font-size:14;color:blue;" onclick="load(3)">待发货</a>
					</div>
				</div>
				<div class="right">
					<a href="javascript:void(0)" class="ui-btn ui-btn-xz" data-options="plain:true" onclick="send()">发货</a>
					<a href="javascript:void(0)" class="ui-btn ui-btn-xz" data-options="plain:true" onclick="edit()">修改</a>
					<a href="javascript:void(0)" class="ui-btn ui-btn-xz" data-options="plain:true" onclick="onImport()">导入</a>
					<a href="javascript:void(0)" class="ui-btn ui-btn-xz" data-options="plain:true" onclick="doExport()">导出</a>
				</div>
			</div>
		</div>
		
		<!-- 查询对话框 begin -->
		<div class="qijian_box" id="qrydialog" style="display:none; width:450px; height:263px">
			<s class="s" style="left: 25px;"><i class="i"></i> </s>
			<h3>
				<span>查询</span><a class="panel-tool-close" href="javascript:closeCx()"></a>
			</h3>
			<div class="time_col time_colp10">
				<input id="rq" type="radio"  name="seledate" checked="true" value="lr" />
				<label style="text-align:right; width: 70px;">录入日期：</label> 
				<input id="begdate" name="begdate" type="text" class="easyui-datebox" 
						 data-options="width:137,height:27" />  - 
				<input id="enddate" name="enddate" type="text" class="easyui-datebox" 
						data-options="width:137,height:27" />
			</div>
			<div class="time_col time_colp10">
				<input id="qj" type="radio" name="seledate" value="app" />
				<label style="text-align:right; width: 70px;">申请日期：</label> 
				<input id="bperiod" name="bperiod" type="text" class="easyui-datebox" 
						 data-options="width:137,height:27" />  - 
				<input id="eperiod" name="eperiod" type="text" class="easyui-datebox" 
						data-options="width:137,height:27" />
			</div>
			<div class="time_col time_colp10">
				<label style="width:85px;text-align:right">加盟商：</label>
				<input id="qcorpname" class="easyui-textbox" style="width:286px;height:28px;"/>
			</div>
			<div class="time_col time_colp10">
				<label style="width:85px;text-align:right">单据状态：</label>
				<!-- 单据状态：全部、待审核、待发货、已发货、已驳回 -->
				<select id="status" class="easyui-combobox" data-options="panelHeight:'auto'" style="width:282px;height:28px;">
					<!-- <option value="1">待审核</option> -->
					<option value="0">全部</option>
					<option value="2">待发货</option>
					<option value="3">已发货</option>
					<!-- <option value="4">已驳回</option> -->
				</select>
			</div>
			<div class="time_col time_colp10">
				<label style="width:85px;text-align:right">渠道经理：</label>
				<input id="uid" class="easyui-combobox" editable="false" style="width:286px;height:28px;text-align:left"
			       data-options="required:false,valueField:'id',textField:'name',multiple:false,panelHeight:200" /> 
			</div>
			<p>
				<a class="ui-btn save_input" onclick="clearParams()">清除</a>
				<a class="ui-btn save_input" onclick="load(1)">确定</a>
				<a class="ui-btn cancel_input" onclick="closeCx()">取消</a>
			</p>
		</div>
		<!-- 查询对话框end -->
		
		<div id="dataGrid" class="grid-wrap">
			<table id="grid"></table>
		</div>
		
		<!-- 新增对话框  begin-->
		<div id="cbDialog" class="easyui-dialog" style="height:500px;width:680px;overflow:auto;padding-top:18px;"
			data-options="closed:true,buttons:'#dlg-buttons'" modal=true>
			<form id="mat_add" method="post">
				<input id="matbillid" name="matbillid" type="hidden">
				<input id="updatets" name="updatets" type="hidden">
				<input id="type" name="type" type="hidden">
				<input id="debegdate" name="debegdate" type="hidden">
				<input id="deenddate" name="deenddate" type="hidden">
				<div class="time_col time_colp10">
						<label style="text-align:right; width: 124px;">合同编号：</label> 
						<input id="code" name="code" class="easyui-textbox" style="width:168px;height:28px;"/>
			            <span class="hid">
			            <label style="text-align:right; width: 85px;">状态：</label> 
						<input id="stat" class="easyui-textbox" style="width:163px;height:28px;"/>
			            </span>
			    </div>
			    <div class="time_col time_colp10">
						<label style="width:124px;text-align:right"><i class="bisu">*</i>加盟商：</label> 
						<input id="corpnm" name="corpname" class="easyui-textbox" style="width:431px;height:28px;" data-options="required:true"/>
						<input id="fcorp" name="fcorp" type="hidden">
				</div>
				<div class="time_col time_colp10">
						<label style="width:124px;text-align:right"><i class="bisu">*</i>所在地区：</label> 
						<input id="pname" name="pname" class="easyui-combobox" style="width:141px;height:28px;"
				           editable="false" data-options="valueField:'vprovince', textField:'pname', panelHeight:'200',prompt:'请选择省'" /> 
				        <input id="vprovince" name="vprovince" type="hidden">
				        <input id="cityname" name="cityname" class="easyui-combobox" style="width:141px;height:28px;"
				           editable="false" data-options="valueField:'vcity', textField:'cityname', panelHeight:'200',prompt:'请选择市'" />
				        <input id="vcity" name="vcity" type="hidden">
				        <input id="countryname" name="countryname" class="easyui-combobox" style="width:141px;height:28px;"
				           editable="false" data-options="valueField:'varea', textField:'countryname', panelHeight:'200',prompt:'请选择区/县'" />
				         <input id="varea" name="varea" type="hidden">
				</div>
				<div class="time_col time_colp10">
				       <label style="text-align:right; width: 124px;"><i class="bisu">*</i>详细地址：</label> 
				       <!-- <textarea id="address" name="address" class="easyui-validatebox" style="width: 432px;height:51px;"  data-options="required:true"></textarea> -->
			           <input id="address" name="address" class="easyui-textbox" style="width:431px;height:28px;" data-options="required:true" />
				</div>
				<div class="time_col time_colp10">
					   <label style="width:124px;text-align:right"><i class="bisu">*</i>收货人：</label> 
					   <input id="receiver" name="receiver" class="easyui-textbox" style="width:167px;height:28px;" data-options="required:true"/>
					   <label style="width:85px;text-align:right"><i class="bisu">*</i>联系电话：</label> 
					   <input id="phone" name="phone" class="easyui-textbox" style="width:167px;height:28px;" data-options="required:true"/>
				</div>
				<div class="time_col time_colp10">
						<label style="width:124px;text-align:right"><i class="bisu">*</i>物料选择：</label>
					    <div id="cardGrid" style="width:432px;height:70px;
							margin-left:94px;margin-top:-21px;">
						</div>
				</div>
               	<div class="time_col time_colp10">
						<label style="text-align:right; width: 124px;">备注：</label>
						<!-- <textarea id="memo" name="memo" class="easyui-validatebox" style="width: 432px;height:51px;"  placeholder="最多可输入200个字"></textarea> -->
				        <input id="memo" name="memo" class="easyui-textbox" style="width:431px;height:28px;"  data-options="prompt:'最多可输入200个字'"/>
				</div>
				<div class="time_col time_colp10">
					   <label style="width:124px;text-align:right">申请人：</label> 
					   <input id="applyname" name="applyname" class="easyui-textbox" style="width:168px;height:28px;"/>
					   <label style="width:87px;text-align:right">申请时间：</label> 
					   <input id="adate" name="adate" type="text" class="easyui-datebox" 
						 data-options="width:163,height:27" />  
				</div>
				<div class="time_col time_colp10">
						<label style="width:124px;text-align:right">审核人：</label>
						<input id="audname" name="audname" class="easyui-textbox" style="width:168px;height:28px;"/>
						<label style="width:87px;text-align:right">审核时间：</label>
						<input id="audate" name="audate" type="text" class="easyui-datebox"
							   data-options="width:163,height:27" />
					</div>
				<div class="time_col time_colp10">
					 	<label style="text-align:right; width: 124px;">快递公司：</label>
					    <input id="logid" name="logid" class="easyui-combobox" style="width:166px;height:28px;"
				           editable="false" data-options="valueField:'logid', textField:'logname', panelHeight:'200'" />
			            <label style="text-align:right; width: 85px;">金额：</label> 
						<input id="fcost" name="fcost" class="easyui-numberbox" style="width:166px;height:28px;" />
				</div>
				<div class="time_col time_colp10">
					   <label style="width:124px;text-align:right">快递单号：</label> 
					   <input id="fcode" name="fcode" class="easyui-textbox" style="width:166px;height:28px;" />
					   <label style="width:85px;text-align:right">发货时间：</label> 
					   <input id="dedate" name="dedate" type="text" class="easyui-datebox" 
						   data-options="width:166,height:27" />  
				</div>
				<div class="time_col time_colp10">
					  <label style="width:124px;text-align:right">发货人：</label> 
				      <input id="dename" name="dename" class="easyui-textbox" style="width:166px;height:28px;"/>
				</div>
				  
				<div style="float:right;margin-top:2px;margin-right:293px;">
				    <span class="sid">
				    <a href="javascript:void(0)" class="ui-btn ui-btn-xz" onclick="onSave()">保存</a> 
					<a href="javascript:void(0)" class="ui-btn ui-btn-xz" onclick="onCancel()">取消</a>
				    </span>
				</div>
			</form>
		</div>
		<!-- 新增对话框  end-->
		
		
		<!-- 详情对话框  begin-->
		<div id="infoDialog" class="easyui-dialog" style="height:500px;width:680px;overflow:auto;padding-top:18px;"
			data-options="closed:true,buttons:'#dlg-buttons'" modal=true>
			<form id="infoform" method="post">
				<input id="imatbillid" name="matbillid" type="hidden">
				<input id="iupdatets" name="updatets" type="hidden">
				<input id="itype" name="type" type="hidden">
				<input id="idebegdate" name="debegdate" type="hidden">
				<input id="ideenddate" name="deenddate" type="hidden">
				<div class="time_col time_colp10">
						<label style="text-align:right; width: 124px;">合同编号：</label> 
						<input id="icode" name="code" class="easyui-textbox" style="width:168px;height:28px;"
							data-options="readonly:true"/>
			            <label style="text-align:right; width: 85px;">状态：</label> 
						<input id="istat" class="easyui-textbox" style="width:163px;height:28px;"
							data-options="readonly:true" />
			    </div>
			    <div class="time_col time_colp10">
						<label style="width:124px;text-align:right"><i class="bisu">*</i>加盟商：</label> 
						<input id="icorpnm" name="corpname" class="easyui-textbox" style="width:431px;height:28px;" 
							data-options="readonly:true" />
						<input id="ifcorp" name="fcorp" type="hidden">
				</div>
				<div class="time_col time_colp10">
						<label style="width:124px;text-align:right"><i class="bisu">*</i>所在地区：</label> 
						<input id="ipname" name="pname" class="easyui-combobox" style="width:141px;height:28px;"
				           editable="false" data-options="required:true,readonly:true,valueField:'vprovince', textField:'pname', panelHeight:'200',prompt:'请选择省'" /> 
				        <input id="ivprovince" name="vprovince" type="hidden">
				        <input id="icityname" name="cityname" class="easyui-combobox" style="width:141px;height:28px;"
				           editable="false" data-options="required:true,readonly:true,valueField:'vcity', textField:'cityname', panelHeight:'200',prompt:'请选择市'" />
				        <input id="ivcity" name="vcity" type="hidden">
				        <input id="icountryname" name="countryname" class="easyui-combobox" style="width:141px;height:28px;"
				           editable="false" data-options="required:true,readonly:true,valueField:'varea', textField:'countryname', panelHeight:'200',prompt:'请选择区/县'" />
				         <input id="ivarea" name="varea" type="hidden">
				</div>
				<div class="time_col time_colp10">
				       <label style="text-align:right; width: 124px;"><i class="bisu">*</i>详细地址：</label> 
			           <input id="iaddress" name="address" class="easyui-textbox" style="width:431px;height:28px;" 
			           	data-options="required:true" />
				</div>
				<div class="time_col time_colp10">
					   <label style="width:124px;text-align:right"><i class="bisu">*</i>收货人：</label> 
					   <input id="ireceiver" name="receiver" class="easyui-textbox" style="width:167px;height:28px;" 
					   	data-options="required:true"/>
					   <label style="width:85px;text-align:right"><i class="bisu">*</i>联系电话：</label> 
					   <input id="iphone" name="phone" class="easyui-textbox" style="width:167px;height:28px;" 
					   	data-options="required:true"/>
				</div>
				<div class="time_col time_colp10">
						<label style="width:124px;text-align:right"><i class="bisu">*</i>物料选择：</label>
					    <div id="icardGrid" style="width:432px;height:70px;display:inline-block;"></div>
				</div>
               	<div class="time_col time_colp10">
						<label style="text-align:right; width: 124px;">备注：</label>
				        <input id="imemo" name="memo" class="easyui-textbox" style="width:431px;height:28px;"  
				         data-options="readonly:true"/>
				</div>
				<div class="time_col time_colp10">
					   <label style="width:124px;text-align:right">申请人：</label> 
					   <input id="iapplyname" name="applyname" class="easyui-textbox" style="width:168px;height:28px;"
					   		data-options="readonly:true"/>
					   <label style="width:87px;text-align:right">申请时间：</label> 
					   <input id="iadate" name="adate" type="text" class="easyui-datebox" style="width:168px;height:27px;"
						 data-options="readonly:true" />  
				</div>
				<div class="time_col time_colp10">
						<label style="width:124px;text-align:right">审核人：</label>
						<input id="iaudname" name="audname" class="easyui-textbox" style="width:168px;height:28px;"
						data-options="readonly:true"/>
						<label style="width:87px;text-align:right">审核时间：</label>
						<input id="iaudate" name="audate" type="text" class="easyui-datebox"
					    data-options="width:163,height:27,readonly:true" />
				</div>
				<div class="time_col time_colp10">
					 	<label style="text-align:right; width: 124px;">快递公司：</label>
			             <input id="ilogid" name="logid" class="easyui-combobox" style="width:166px;height:28px;"
				           editable="false" data-options="valueField:'logid', textField:'logname', panelHeight:'200',readonly:true" />
				           
			            <label style="text-align:right; width: 85px;">金额：</label> 
						<input id="ifcost" name="fcost" class="easyui-numberbox" style="width:166px;height:28px;" 
							data-options="readonly:true"/>
				</div>
				<div class="time_col time_colp10">
					   <label style="width:124px;text-align:right">快递单号：</label> 
					   <input id="ifcode" name="fcode" class="easyui-textbox" style="width:166px;height:28px;" data-options="readonly:true"/>
					   <label style="width:85px;text-align:right">发货时间：</label> 
					   <input id="idedate" name="dedate" type="text" class="easyui-datebox" style="width:166px;height:27px;" 
						   data-options="readonly:true" />  
				</div>
				<div class="time_col time_colp10">
					  <label style="width:124px;text-align:right">发货人：</label> 
				      <input id="idename" name="dename" class="easyui-textbox" style="width:166px;height:28px;"
				      	data-options="readonly:true"/>
				</div>
				<div style="float:right;margin-top:2px;margin-right:293px;">
				    <span class="eid">
				    <a href="javascript:void(0)" class="ui-btn ui-btn-xz" onclick="editSave()">保存</a> 
					<a href="javascript:void(0)" class="ui-btn ui-btn-xz" onclick="onCancel()">取消</a>
				    </span>
				</div>
			</form>
		</div>
		<!-- 详情对话框  end-->
		
		<!-- 附件上传begin -->
		<div id="impDlg" class="easyui-dialog" style="overflow: auto;" data-options="closed:true">
		<form id="impForm" method="post" enctype="multipart/form-data" style="text-align:center;font-size:14px;">
			<p>
			 	<p class="selecticon" style="cursor:pointer;border:1px solid #A5A5A5;width:78px;height:78px;margin:20px 175px 10px 175px;background-position:26px 26px;"
			 		onclick="$('#impfile').trigger('click')">
			 	</p>
			 	<p class="fileicon" style="margin:20px 175px 10px 175px;">
			 		<img src="../../img/fileicon.png">
			 	</p>
			</p>
			<p style="color:#333;">
		 		<input id="impfile" type="file" accept= "application/vnd.ms-excel, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" name="file" style="display:none"
		 			onchange="onFileSelected()">
				<span id="impfileName" class="notfeil">未选择任何文件</span>
			</p>
			<p class="clickdown" style="color:#999;">
				<span >支持标准模板导入,没有导入模板
					<a href="<%=request.getContextPath()%>/exceltemplet/mathistroryimp.xlsx" id="downloadtemp" 
					style="color:#2c9dd8;font-size:14px;text-decoration:none;">点此下载</a>
				</span>
			</p>
		</form>
		<div id="imp_msg" ></div>
	</div>
	<div id="imp-buttons" style="display:none;">
		<a href="javascript:void(0)" id="confirmBtn" class="easyui-linkbutton c6" onclick="onFileConfirm()" style="width:90px">上传</a> 
		<a href="javascript:void(0)" class="easyui-linkbutton" onclick="javascript:$('#impDlg').dialog('close')" style="width:90px">取消</a>
	</div>
	<!-- 附件上传end -->
		
	</div>
	<!-- 列表界面 end -->
	
</body>
</html>