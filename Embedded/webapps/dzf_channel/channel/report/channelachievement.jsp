<%@page language="java" pageEncoding="UTF-8"%>
<%@page import="com.dzf.pub.UpdateGradeVersion"%>
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
</head>
<body>
	<div class="wrapper">
	 <div class="sel_time">
				<div class="time_col"> 
			
				<label style="text-align:right;width: 80px;">维度：</label> 
				<select id="qsgtype" name="qsgtype" class="easyui-combobox" data-options="editable:false,required:true,panelHeight:80" 
					style="width: 100px; height: 28px;">
					<option value="-1">全部</option>
					<option value="0">已签订合同</option>
					<option value="1">未签订合同</option>
				</select> 
		
					<label style="text-align: right;">期间：</label> 
					<font> 
						<input name="begindate" type="text" id="begindate" class="easyui-datebox" data-options="width:100,height:28" /> 
					</font> 
					<font>  
						<input name="enddate" type="text" id="enddate" class="easyui-datebox" data-options="width:100,height:28" />
					</font>
			
				
					<font>-</font>
					<font> 
						<input name="begindate" type="text" id="begindate" class="easyui-datebox" data-options="width:100,height:28" /> 
					</font> 
				
					<font>  
						<input name="enddate" type="text" id="enddate" class="easyui-datebox" data-options="width:100,height:28" />
					</font>
					
			    <a href="javascript:void(0)" class="ui-btn ui-btn-xz" style="margin-bottom: 0px; " onclick=''>查询</a>
				<a href="javascript:void(0)" class="ui-btn ui-btn-xz" style="margin-bottom: 0px; " onclick=''>清除</a>
				<div style="float: right;">
					<a href="javascript:void(0)" class="ui-btn ui-btn-xz"  onclick="">刷新</a>
					<a href="javascript:void(0)" class="ui-btn ui-btn-xz"  onclick="">导出</a> 
					<a href="javascript:void(0)" class="ui-btn ui-btn-xz"  onclick="">打印</a>	
					
				</div>
					</div>
			</div>
			
        <div id="main" style="width: 100%;height:400px;"></div>
	</div>
</body>
  <script type="text/javascript">
        // 基于准备好的dom，初始化echarts实例
        var myChart = echarts.init(document.getElementById('main'));
        var option = {
    title: {
        text: '业绩环比'
    },
    tooltip: {
        trigger: 'axis'
    },
    legend: {
        data:['扣款金额','合同金额']
    },
    grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
    },
    toolbox: {
        feature: {
            saveAsImage: {}
        }
    },
    xAxis: {
        type: 'category',
        boundaryGap: false,
        data: ['2018-01','2018-02','2018-03','2018-04','2018-05','2018-06']
    },
    yAxis: {
        type: 'value'
    },
    series: [
        {
            name:'扣款金额',
            type:'line',
            stack: '总量',
            data:[120, 132, 101, 134, 90, 230, 210]
        },
        {
            name:'合同金额',
            type:'line',
            stack: '总量',
            data:[220, 182, 191, 234, 290, 330, 310]
        }
        
    ]
};


                // 使用刚指定的配置项和数据显示图表。
                myChart.setOption(option);

    </script>
</html>