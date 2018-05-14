<%@page language="java" pageEncoding="UTF-8"%>
<%@page import="com.dzf.pub.UpdateGradeVersion"%>
<%@page import="com.dzf.pub.DzfUtil"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.util.Date"%>
<%@page import="java.text.SimpleDateFormat"%>
<%
	Calendar e = Calendar.getInstance();
	String eym = new SimpleDateFormat("yyyy-MM").format(e.getTime());//当前年-月
	String year = new SimpleDateFormat("yyyy").format(e.getTime());//当前年
	String month = new SimpleDateFormat("MM").format(e.getTime());//当前月
	
	Integer imonth = Integer.parseInt(month);
	Integer jd = 1;
	if(1 <= imonth && imonth <= 3){
		jd = 1;
	}else if(4 <= imonth && imonth <= 6){
		jd = 2;
	}else if(7 <= imonth && imonth <= 9){
		jd = 3;
	}else if(10 <= imonth && imonth <= 12){
		jd = 4;
	}

	e.add(Calendar.MONTH, -3);
	String bym = new SimpleDateFormat("yyyy-MM").format(e.getTime());//当前日期（向前退3个月）年-月
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<title>省业绩分析 </title>
<jsp:include page="../../inc/easyui.jsp"></jsp:include>
<link
	href=<%UpdateGradeVersion.outversion(out, "../../css/index.css");%>
	rel="stylesheet">
<script
	src=<%UpdateGradeVersion.outversion(out, "../../js/easyuiext.js");%>
	charset="UTF-8" type="text/javascript"></script>
<script
	src=<%UpdateGradeVersion.outversion(out, "../../js/channel/report/channelachievement.js");%>
	charset="UTF-8" type="text/javascript"></script>
	<script type="text/javascript" src="../../js/echarts.common.min.js" ></script>
<style type="text/css">
.panel-body {
	overflow: auto
}
</style>
</head>
<body>
	<div class="wrapper" style="overflow:auto;height:100%;">
		<input type="hidden" value="<%=year%>" id="year" >
		<input type="hidden" value="<%=month%>" id="mth" >
		<input type="hidden" value="<%=jd%>" id="jd" >
		<div style="margin-bottom:20px;border:1px solid #ccc;background:#FFF">
			<div class="sel_time">
				<div class="time_col"> 
					<label style="text-align:right;width:80px;">维度：</label> 
					<select class="easyui-combobox" data-options="editable:false,required:true,panelHeight:'auto'" 
						style="width:70px; height: 28px;" id="qrytype" name="qrytype">
						<option value="1" selected>月度</option>
						<option value="2">季度</option>
						<option value="3">年度</option>
					</select> 
					<label style="text-align: right;">期间：</label> 
					
					<!-- 月度查询条件 begin -->
					<div id = "month" style="display:inline">
						<input id="bperiod" name="bperiod" type="text"  class="easyui-datebox" 
							data-options="editable:false" style="width:100px;height:28px;" value=<%=bym %> /> -
						<input id="eperiod" name="eperiod" type="text" class="easyui-datebox" 
							data-options="editable:false" style="width:100px;height:28px;" value=<%=eym %>/> 
					</div>
					<!-- 月度查询条件 begin -->
					<!-- 季度查询条件 begin -->
					<div id = "quarter" style="display:inline">
						<select id="byear" name="byear" class="easyui-combobox" data-options="editable:false"  
							style="width:70px;height:28px;">
							<% DzfUtil.WriteYearOption(out);%>
						</select> 
						<select id="bjd" name="bjd" class="easyui-combobox" data-options="editable:false,panelHeight:100" 
							style="width:100px;height:28px;text-align:left">
							<option value="1">第一季度</option>
							<option value="2">第二季度</option>
							<option value="3">第三季度</option>
							<option value="4">第四季度</option>	
						</select> - 
						<select id="eyear" name="eyear" class="easyui-combobox" data-options="editable:false"  
							style="width:70px;height:28px;">
							<% DzfUtil.WriteYearOption(out);%>
						</select> 
						<select id="ejd" name="ejd" class="easyui-combobox" data-options="editable:false,panelHeight:100" 
							style="width:100px;height:28px;text-align:left">
							<option value="1">第一季度</option>
							<option value="2">第二季度</option>
							<option value="3">第三季度</option>
							<option value="4">第四季度</option>	
						</select>	
					</div>
					<!-- 季度查询条件 end -->
					<!-- 年度查询条件 begin -->
					<div id = "qyear" style="display:inline">
						<select id="bqyear" name="bqyear" class="easyui-combobox" data-options="editable:false"  
								style="width:70px;height:28px;">
							<% DzfUtil.WriteYearOption(out);%>
						</select> - 
						<select id="eqyear" name="eqyear" class="easyui-combobox" data-options="editable:false"  
								style="width:70px;height:28px;">
							<% DzfUtil.WriteYearOption(out);%>
						</select> 
					</div>
					<!-- 年度查询条件 end -->
					<div style="margin-left:20px;display:inline">
					    <a href="javascript:void(0)" class="ui-btn ui-btn-xz" style="margin-bottom: 0px; " onclick='lineQry()'>查询</a>
					</div>
					<!-- <div style="float: right;">
						<a href="javascript:void(0)" class="ui-btn ui-btn-xz"  onclick="">刷新</a>
					</div> -->
				</div>
			</div>
		    <div id="main" style="width: 100%;height:300px;"></div>
	    </div>
        
        <div style="margin-bottom:30px;border:1px solid #ccc;background:#FFF">
	        <div class="sel_time">
		       	<div class="time_col"> 
					<label style="text-align:right;width:80px;">维度：</label> 
					<select class="easyui-combobox" data-options="editable:false,required:true,panelHeight:'auto'" 
						style="width:70px; height: 28px;" id="tqrytype" name="tqrytype">
						<option value="1" selected>月度</option>
						<option value="2">季度</option>
						<option value="3">年度</option>
					</select> 
					<label style="text-align: right;">期间：</label> 
					
					<!-- 月度查询条件 begin -->
					<div id = "tmonth" style="display:inline">
						<select id="tmyear" name="tmyear" class="easyui-combobox" data-options="editable:false"  
							style="width:70px;height:28px;">
							<% DzfUtil.WriteYearOption(out);%>
						</select>
						<select id="tbmonth" name="tbmonth" class="easyui-combobox"  data-options="editable:false" 
							style="width:50px;height:27px;">
							<% DzfUtil.WriteMonthOption(out);%>
						</select> -
						<select id="temonth" name="temonth" class="easyui-combobox"  data-options="editable:false" 
							style="width:50px;height:27px;">
							<% DzfUtil.WriteMonthOption(out);%>
						</select>
					</div>
					<!-- 月度查询条件 begin -->
					<!-- 季度查询条件 begin -->
					<div id = "tquarter" style="display:inline">
						<select id="tbyear" name="tbyear" class="easyui-combobox" data-options="editable:false"  
							style="width:70px;height:28px;">
							<% DzfUtil.WriteYearOption(out);%>
						</select> 
						<select id="tbjd" name="tbjd" class="easyui-combobox" data-options="editable:false,panelHeight:100" 
							style="width:100px;height:28px;text-align:left">
							<option value="1">第一季度</option>
							<option value="2">第二季度</option>
							<option value="3">第三季度</option>
							<option value="4">第四季度</option>	
						</select> -
						<select id="tejd" name="tejd" class="easyui-combobox" data-options="editable:false,panelHeight:100" 
							style="width:100px;height:28px;text-align:left">
							<option value="1">第一季度</option>
							<option value="2">第二季度</option>
							<option value="3">第三季度</option>
							<option value="4">第四季度</option>	
						</select> 
					</div>
					<!-- 季度查询条件 end -->
					<!-- 年度查询条件 begin -->
					<div id = "tqyear" style="display:inline">
						<select id="tbqyear" name="tbqyear" class="easyui-combobox" data-options="editable:false"  
								style="width:70px;height:28px;">
							<% DzfUtil.WriteYearOption(out);%>
						</select> 
					</div>
					<!-- 年度查询条件 end -->
					<div style="margin-left:20px;display:inline">
					    <a href="javascript:void(0)" class="ui-btn ui-btn-xz" style="margin-bottom: 0px; " onclick='chartQry()'>查询</a>
					</div>
					<!-- <div style="float: right;">
						<a href="javascript:void(0)" class="ui-btn ui-btn-xz"  onclick="">刷新</a>
					</div> -->
				</div>
			</div>
			<div style="background: #FFF;">
				<div style="text-align:left;width:8%;font-size:14px;float:left;padding-left:6px; 
					padding-top:4px;font-size: 18px;font-weight: bold;">业绩同比(%)</div>
					<div style="position:relative;padding-right:20px;padding-top:4px;">
						<select class="easyui-combobox" data-options="editable:false,required:true,panelHeight:'auto'"
							style="width:100px; height:28px; text-align:left" id="tshowtype" name="tshowtype">
							<option value="1">扣款金额</option>
							<option value="2">合同金额</option>
						</select>
					</div>
			</div>
	        <div id="man" style="width: 100%;height:300px;"></div>
        </div>
	</div>
</body>
</html>
