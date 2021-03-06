<%@page language="java" pageEncoding="UTF-8"%>
<%@page import="com.dzf.pub.UpdateGradeVersion"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<title>快递统计表</title>
<jsp:include page="../../inc/easyui.jsp"></jsp:include>
<link href=<%UpdateGradeVersion.outversion(out, "../../css/index.css");%> rel="stylesheet">
<script src=<%UpdateGradeVersion.outversion(out,"../../js/easyuiext.js");%> charset="UTF-8" type="text/javascript"></script>
<script src=<%UpdateGradeVersion.outversion(out,"../../js/channel/report/logisticReport.js");%> charset="UTF-8" type="text/javascript"></script>
</head>
<body>

		<div class="wrapper">
		<div id="listPanel" class="wrapper" style="width: 100%;overflow:hidden; height: 100%;">
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
						<a href="javascript:void(0)" class="ui-btn ui-btn-xz" onclick="doExport()">导出</a>
					</div>
				</div>
			</div>
			
			<div class="qijian_box" id="qrydialog" style="display: none; width: 450px; height: 260px">
				<s class="s" style="left: 25px;"><i class="i"></i> </s>
				<h3>
					<span>查询</span><a class="panel-tool-close" href="javascript:closeCx()"></a>
				</h3>
				<div class="time_col time_colp10">
					<label style="width: 80px;text-align:right">发货日期：</label>
					<font>
						<input id="begdate" name="begdate"  class="easyui-datebox" style="width:130px;height:27px;" />
					</font>
					<font>-</font>
					<font>
						<input id="enddate"  name="enddate"  class="easyui-datebox" style="width:130px;height:27px;"/>
					</font>
				</div>
				<div class="time_col time_colp10">
					<label style="width:80px;text-align:right">查询类型：</label>
					<input type="radio" id="mater" value="mater" name="log" checked style="width:20px;height:28px;text-align:left;margin-left:20px;">
					<label style="width:100px;text-align:left" for='mater'>物料快递</label> 
                    <input type="radio" id="goods" value="goods" name="log" style="width:20px;height:28px;text-align:left;margin-left:20px;">
                    <label style="width:100px;text-align:left" for='goods'>商品快递</label> 
				</div>
				<div class="time_col time_colp10">
					<label style="width:80px;text-align:right">加盟商：</label>
					<input id="corpnm" class="easyui-textbox" style="width:290px;height:28px;" />
					<input id="corpid" type="hidden">
				</div>
				<div class="time_col time_colp10">
					<label style="width:80px;text-align:right">单号：</label>
					<input id="ucode" class="easyui-textbox" style="width:290px;height:28px;" />
				</div>
				<div class="time_col time_colp10">
					<label style="width:80px;text-align:right">大区：</label>
					<input id="aname"  name="aname" class="easyui-combobox" style="width: 150px; height: 28px;" 
						data-options="required:false,valueField:'name',textField:'name',panelHeight:100" editable="false" />
				</div>
				<p>
					<a class="ui-btn save_input" onclick="clearParams()">清除</a>
					<a class="ui-btn save_input" onclick="reloadData()">确定</a>
					<a class="ui-btn cancel_input" onclick="closeCx()">取消</a>
				</p>
			</div>
			
			<div class="mod-inner">
				<div id="dataGrid" class="grid-wrap">
					<table id="grid"></table>
				</div>
			</div> 
			
			<!-- 加盟商参照对话框及按钮 begin -->
			<div id="chnDlg"></div>
			<div id="chnBtn" style="display:none;">
				<a href="javascript:void(0)" class="easyui-linkbutton c6"  onclick="selectCorps()" style="width:90px">确认</a> 
				<a href="javascript:void(0)" class="easyui-linkbutton" 
					onclick="javascript:$('#chnDlg').dialog('close');" style="width:90px">取消</a>
			</div>
			<!-- 加盟商参照对话框及按钮 end -->
	 	</div>	
	</div>
</body>
</html>
