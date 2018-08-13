<%@ page language="java" pageEncoding="UTF-8"%>
<%@page import="com.dzf.pub.UpdateGradeVersion"%>
<%@ page import="com.dzf.pub.IGlobalConstants"%>
<%@page import="com.dzf.model.sys.sys_power.UserVO"%>
<%@ page import="com.dzf.pub.cache.UserCache"%>
<%@page import="com.dzf.pub.constant.AdminDateUtil"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<title>商品管理</title>
<jsp:include page="../../inc/easyui.jsp"></jsp:include>
<link href=<%UpdateGradeVersion.outversion(out, "../../css/index.css");%> rel="stylesheet">
<link href=<%UpdateGradeVersion.outversion(out, "../../css/channelorder.css");%> rel="stylesheet">
<script src=<%UpdateGradeVersion.outversion(out, "../../js/easyuiext.js");%> charset="UTF-8" type="text/javascript"></script>
<script src=<%UpdateGradeVersion.outversion(out, "../../js/channel/dealmanage/channelorder.js");%> charset="UTF-8" type="text/javascript"></script>

</head>
<body>
	<div id="List_panel" class="wrapper" data-options="closed:false">
		<div class="mod-toolbar-top">
			<div class="mod-toolbar-content">
				<div class="left mod-crumb">
					<div id="cxjs" class="h30 h30-arrow">
						<label class="mr5">查询：</label>
						<strong id="querydate"></strong>
						<span class="arrow-date"></span>
					</div>
				</div>
				<div class="left mod-crumb">
					<span class="cur"></span>
				</div>
				
				<div class="right">
				<a href="javascript:void(0)" class="ui-btn ui-btn-xz" data-options="plain:true"  onclick="add();">订单详情</a>
					<a href="javascript:void(0)" class="ui-btn ui-btn-xz" data-options="plain:true" onclick="confirm()">确认</a>
					<a href="javascript:void(0)" class="ui-btn ui-btn-xz" data-options="plain:true" onclick="cancel()">取消订单</a>
					<a href="javascript:void(0)" class="ui-btn ui-btn-xz" data-options="plain:true" onclick="sendOut()">商品发货</a>
				</div>
			</div>
		</div>
		
		<!-- 查询对话框 begin -->
		<div class="qijian_box" id="qrydialog" style="display:none; width:450px; height:190px">
			<s class="s" style="left: 25px;"><i class="i"></i> </s>
			<h3>
				<span>查询</span><a class="panel-tool-close" href="javascript:closeCx()"></a>
			</h3>
			<div class="time_col time_colp10">
				<label style="width:85px;text-align:right">商品编码：</label>
				<input id="qgcode" class="easyui-textbox" style="width:290px;height:28px;"/>
			</div>
			<div class="time_col time_colp10">
				<label style="width:85px;text-align:right">商品名称：</label>
				<input id="qgname" class="easyui-textbox" style="width:290px;height:28px;"/>
			</div>
			<div class="time_col time_colp10">
				<label style="width:85px;text-align:right">合同状态：</label>
				<select id="qstatus" class="easyui-combobox" data-options="panelHeight:'auto'" 
					editable="false" style="width:290px;height:28px;">
					<option value="-1">全部</option>
					<option value="1">已保存</option>
					<option value="2">已发布</option>
					<option value="3">已下架</option>
				</select>
			</div>
			<p>
				<a class="ui-btn save_input" onclick="clearParams()">清除</a>
				<a class="ui-btn save_input" onclick="reloadData()">确定</a>
				<a class="ui-btn cancel_input" onclick="closeCx()">取消</a>
			</p>
		</div>
		<!-- 查询对话框end -->
		
		<div id="dataGrid" class="grid-wrap">
			<table id="grid"></table>
		</div>
		
<script>

function add() {
	$('#cbDialog').dialog('open').dialog('center').dialog('setTitle', '订单详情');

}
</script>

		<div id="cbDialog" class="easyui-dialog"
			style="width: 830px; height: 530px; padding-top: 10px; font-size: 14px;"
			data-options="closed:true,buttons:'#dlg-buttons'" modal=true>

			<div class="shift">
				<div style="margin-bottom: 20px;">
					<div style="width: 10%; display: inline-block">
						<div class="shop_pick">订单信息</div>
					</div>
					<div class="order">
						<label>订单编号：</label> <span>8808201800034 </span>
					</div>
					<div class="order">
						<label>加盟商：</label> <span>加盟商演示系统 </span>
					</div>
				</div>
				<div class="flow">
					<div class="flow_one">
						<div>
							<img src="../../images/shop_cj.png">
						</div>
						<div>订单创建</div>
						<div>
							<span>2018-08-07</span>&nbsp;<label>20:20</label>
						</div>
					</div>
					<div class="flow_main">
						<div>
							<img src="../../images/shop_qr.png">
						</div>
						<div>订单确认</div>
						<div>
							<span>2018-08-07</span>&nbsp;<label>20:20</label>
						</div>
						<div>
							<img class="flow_mian_img" src="../../images/shop_xq.png">
						</div>
					</div>
					<div class="flow_main">
						<div>
							<img src="../../images/shop_fh.png">
						</div>
						<div>商品发货</div>
						<div>
							<span>2018-08-07</span>&nbsp;<label>20:20</label>
						</div>
						<div>
							<img class="flow_mian_img" src="../../images/shop_xq.png">
						</div>
					</div>
					<div class="flow_main">
						<div>
							<img src="../../images/shop_sh.png">
						</div>
						<div>已收货</div>
						<div>
							<span>2018-08-07</span>&nbsp;<label>20:20</label>
						</div>
						<div>
							<img class="flow_mian_img" src="../../images/shop_xq.png">
						</div>
					</div>
				</div>
				<div class="icon">
					<div class="shop_pick">快递信息</div>
				</div>
				<div style="margin-left: 40px;">
					<div class="order">
						<label>物流公司：</label> <span>顺丰速运 </span>
					</div>
					<div class="order">
						<label>快递单号：</label> <span>8808201800034 </span>
					</div>
				</div>
				<div class="icon">
					<div class="shop_pick">收货信息</div>
				</div>
				<div style="margin-left: 40px;">
					汪家帆，17676872346，内蒙古自治区乌兰察布市四子王旗乌兰花镇供济堂后德义赛罕小区B单元502，110239	
				</div>
				<div class="icon">
					<div class="shop_pick">已选商品</div>
				</div>

				<div class=" pick_main">
					<div class="pick_left">
						<img src="../../images/shop_img1.png" />
					</div>
					<div class=" pick_right">
						<div>
							大账房定制水杯 &emsp;<span>三色可选</span>
						</div>
						<div class=" pick_cost">
							<div class=" pick_cost_A">
								<span>时尚</span><span>健康</span><span>耐用</span>
							</div>
							<div class="pick_cost_B">3</div>
							<div class="pick_cost_C">¥ 99.00</div>
						</div>
					</div>
				</div>
				<div class=" pick_main">
					<div class="pick_left">
						<img src="../../images/shop_img1.png" />
					</div>
					<div class=" pick_right">
						<div>
							大账房定制水杯 &emsp;<span>三色可选</span>
						</div>
						<div class=" pick_cost">
							<div class=" pick_cost_A">
								<span>时尚</span><span>健康</span><span>耐用</span>
							</div>
							<div class="pick_cost_B">3</div>
							<div class="pick_cost_C">¥ 99.00</div>
						</div>
					</div>
				</div>

			</div>
			<div class="goodsbuy_foot">
				<div class="shop_cut_left">
				    实付款：<span>¥ 300.00</span>
				</div>
				<div class="shop_cut_right">
					(包含返点<span>¥ 100.00</span>)
				</div>
			</div>
		</div>


	</div>
</body>
</html>