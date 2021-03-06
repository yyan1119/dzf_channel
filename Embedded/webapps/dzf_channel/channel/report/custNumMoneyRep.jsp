<%@ page language="java"  pageEncoding="UTF-8"%>
<%@ page import="java.util.Calendar"%>
<%@ page import="java.util.Date"%>
<%@ page import="java.text.SimpleDateFormat"%>
<%@page import="com.dzf.pub.UpdateGradeVersion"%>
<!DOCTYPE html>
<html>
<head>
<title>新增统计</title>
<jsp:include page="../../inc/easyui.jsp"></jsp:include>
<link href=<%UpdateGradeVersion.outversion(out, "../../css/index.css");%> rel="stylesheet">
<script src=<%UpdateGradeVersion.outversion(out, "../../js/easyuiext.js");%> charset="UTF-8" type="text/javascript"></script>
<script src=<%UpdateGradeVersion.outversion(out, "../../js/channel/report/repcommon.js");%> charset="UTF-8" type="text/javascript"></script>
<script src=<%UpdateGradeVersion.outversion(out, "../../js/channel/report/custNumMoneyRep.js");%> charset="UTF-8" type="text/javascript"></script>
</head>
<body>
	<div class="wrapper">
		<div id="List_panel" class="wrapper" style="width: 100%; height: 100%;" data-options="closed:false">
		    <div class="mod-toolbar-top">
		        <div class="mod-toolbar-content">
				    <div class="left mod-crumb">
						<span class="cur"></span>
					</div>
					<div class="left mod-crumb">
						<div class="h30 h30-arrow" id="cxjs">
							<label class="mr5">查询：</label>
							<strong id="jqj"></strong>
							<span class="arrow-date"></span>
						</div>
					</div>
				  	<div class="right">
						<a href="javascript:void(0)" class="ui-btn ui-btn-xz conn"
							data-options=" plain:true" onclick="reloadData()" id="">刷新</a> 
						<a href="javascript:void(0)" class="ui-btn ui-btn-xz" onclick="doExport()">导出</a>
					</div>
		        </div>
		    </div>
		    <div class="qijian_box" id="qrydialog" style="display:none; width:450px; height:330px;">
				<s class="s"><i class="i"></i></s>
				<h3>
					<span>查询</span> <a class="panel-tool-close" href="javascript:closeCx()"></a>
				</h3>
				<div class="time_col time_colp10">
					<input id="tddate" type="radio" name="seledate" style="margin-left:12px;" checked />
					<label style="text-align:right;width: 72px;">查询期间：</label>
					<input name="bdate" type="text" id="bdate" class="easyui-datebox" 
						style="width:116px;height:28px;" data-options="editable:true,validType:'checkdate'"/>
					-
					<input name="edate" type="text" id="edate" class="easyui-datebox" 
						style="width:116px;height:28px;" data-options="editable:true,validType:'checkdate'"/>
				</div>
				<div class="time_col time_colp10">
				    <input id="kkdate" type="radio" name="seledate" style="margin-left:12px;" />
					<label style="text-align:right;width: 72px;">查询月份：</label>
					<input type="text" id="qryperiod" class="easyui-textbox" data-options="editable:false"
						style="width:116px;height:28px;" />
				</div>
				<div class="time_col time_colp10">
					<label style="text-align:right;width: 100px;">大区：</label> 
					<input id="aname"  name="aname" class="easyui-combobox" style="width: 245px; height: 28px;" 
						data-options="required:false,valueField:'name',textField:'name',panelHeight:100" editable="false" />  
				</div>
				<div class="time_col time_colp10">
					<label style="text-align:right;width: 100px;">省（市）：</label> 
					<input id="ovince"  name="ovince" class="easyui-combobox" style="width: 245px; height: 28px;" 
						data-options="required:false,valueField:'id',textField:'name',panelHeight:100" editable="false" />  
				</div>		
				<div class="time_col time_colp10">
					<label style="text-align:right;width:100px;">会计运营经理：</label> 
					<input id="uid" name="uid" class="easyui-combobox" style="width:245px;height:28px;text-align:left"
						data-options="required:false,valueField:'id',textField:'name',panelHeight:100" editable="false" />  
				</div>
				<div class="time_col time_colp10">
					<label style="text-align:right;width:100px">加盟商：</label>
					<input id="channel_select" class="easyui-textbox" style="width:245px;height:28px;" />
					<input id="pk_account" type="hidden">
				</div>
				<div class="time_col time_colp10">
					<label style="text-align:left;width:115px" for='stype'>包含已解约加盟商</label> 
					<input id="stype" type="checkbox" style="width:20px;height:28px;text-align:left;margin-left:2px;"/>
					
					<label style="width:97px;text-align:right">客户类型：</label>
					<select id="isncust" class="easyui-combobox" data-options="panelHeight:'auto'" 
						style="width:100px;height:28px;">
						<option value="">全部</option>
						<option value="N">非存量客户</option>
						<option value="Y">存量客户</option>
					</select>
				</div>
				<p>
					<a class="ui-btn save_input" id="cleanbtn" onclick="clearCondition();">清除</a> 
					<a class="ui-btn save_input" onclick="reloadData()">确定</a>
					<a class="ui-btn cancel_input" onclick="closeCx()">取消</a>
				</p>
			</div>
			<div id="dataGrid" class="grid-wrap">
				<table id="grid"></table>
			</div>
			
			<div id="kj_dialog"></div>
			<div id="kj_buttons" style="display:none;">
				<a href="javascript:void(0)" class="easyui-linkbutton c6"  onclick="selectCorps()" style="width:90px">确认</a> 
				<a href="javascript:void(0)" class="easyui-linkbutton" onclick="javascript:$('#kj_dialog').dialog('close');" style="width:90px">取消</a>
			</div>
			
		</div>
	</div>
</body>
</html>
