var contextPath = DZF.contextPath;
var id='#gridh';

$(function() {
	initQry();
	load();
	reloadData();
	quickfiltet();
	initDetailGrid();
	initWshGrid();
	initYbhGrid();
	initTabs();
});

//初始化
function initQry(){
	// 下拉按钮的事件
	$("#jqj").on("mouseover", function() {
		$("#qrydialog").show();
		$("#qrydialog").css("visibility", "visible");
	});
	queryBoxChange('#bdate','#edate');
	$("#bdate").datebox("setValue", parent.SYSTEM.LoginDate.substring(0,7)+"-01");
	$("#edate").datebox("setValue",parent.SYSTEM.LoginDate);
	$("#jqj").html(parent.SYSTEM.LoginDate.substring(0,7)+"-01"+" 至  "+parent.SYSTEM.LoginDate);
	changeProvince();
	initProvince({"qtype" :1});
	initManager({"qtype" :1});
}

function changeProvince(){
	 $("#ovince").combobox({
		onChange : function(n, o) {
			var queryData={"qtype" :1};
			if(!isEmpty(n)){
				queryData={'ovince':n,"qtype" :1};
				$('#cuid').combobox('setValue',null);
			}
			initManager(queryData);
		}
	});
}

//查询框关闭事件
function closeCx() {
	$("#qrydialog").css("visibility", "hidden");
}

// 清空查询条件
function clearCondition(){
	$('#ovince').combobox('select',null);
	$('#cuid').combobox('select',null);

	$('#isncust').combobox('setValue',"N");
	$('#comptype').combobox('setValue','-1');
	$('#chantype').combobox('setValue','-1');
}

// 重新加载数据
function reloadData(filtername) {
	var queryParams =new Array();

	var ovince=$('#ovince').combobox('getValue');''
	if(!isEmpty(ovince)){
		queryParams['ovince'] = ovince;
	}
	var isncust = $('#isncust').combobox('getValue');
	if(!isEmpty(isncust)){
		queryParams['isncust'] = isncust;
	}
	if(!isEmpty(filtername)){
		queryParams['corpnm'] = filtername;
	}
	queryParams['cuid'] =$('#cuid').combobox('getValue');
	queryParams['bdate'] = $('#bdate').datebox('getValue');
	queryParams['edate'] = $('#edate').datebox('getValue');
	queryParams['type'] = 2;

	queryParams['comptype'] = $('#comptype').combobox('getValue');
	queryParams['chantype'] = $('#chantype').combobox('getValue');
	
	$('#grid').datagrid('options').queryParams = queryParams;
	$('#grid').datagrid('options').url = DZF.contextPath +'/report/regionalman!query.action';
	$("#grid").datagrid('reload');
	$('#grid').datagrid('unselectAll');
	$("#qrydialog").css("visibility", "hidden");
	status = "brows";
}

function load() {
	// 列表显示的字段
	$('#grid').datagrid({
		fit : false,
		rownumbers : true,
		height : Public.setGrid().h,
		width:'100%',
		singleSelect : true,
		queryParams: {
			bdate: $('#bdate').datebox('getValue'),
			edate: $('#edate').datebox('getValue'),
			"type":2
		},
		showFooter:true,
		columns : [ [ 
		    {width : '140',title : '省（市）',field : 'provname',align:'left',rowspan:2}, 
		    {width : '130',title : '渠道经理',field : 'cuname',align:'left',rowspan:2}, 
			{width : '250',title : '加盟商',field : 'corpnm',align:'left',rowspan:2,
				formatter : function(value, row, index) {
						if(value == undefined){
							return;
						}else if(value=="合计"){
							return "合计";
						}else{
							value= "<a href='javascript:void(0)' style='color:blue' onclick=\"qryDetail('"+row.corpid+"','"+row.corpnm+"')\">" + value + "</a>";
			            	if (!isEmpty(row.dreldate)) {
			            		return "<div style='position: relative;'>" + value + "<img style='right: 0; position: absolute;' src='../../images/rescission.png' /></div>"
			            	}else{
			            		return value
			            	}
						}
			}}, 
			{width : '60',title : '小规模',field : 'xgmNum',align:'right',rowspan:2}, 
			{width : '60',title : '一般人',field : 'ybrNum',align:'right',rowspan:2}, 
		  	{width : '100',title : '保证金',field : 'bondmny',align:'right',rowspan:2,
		    	formatter : function(value,row,index){
		    		if(value == 0)return "0.00";
		    		return formatMny(value);
			}},  
			{width : '100',title : '预存款余额',field : 'outmny',align:'right',rowspan:2,
		    	formatter : function(value,row,index){
		    		if(value == 0)return "0.00";
		    		return formatMny(value);
			}},
			{width : '100',title : '返点余额',field : 'retmny',align:'right',rowspan:2,
		    	formatter : function(value,row,index){
		    		if(isEmpty(value))return "0.00";
		    		return formatMny(value);
			}},
	  	  	{width : '100',title : '本期预付款',field : 'predeposit',align:'right',rowspan:2,
		    	formatter : function(value,row,index){
		    		if(value == 0)return "0.00";
		    		return formatMny(value);
			}},
			{width : '80',title : '提单量',field : 'num',align:'right',colspan:2}, 
		    {width : '100',title : '合同代账费',field : 'ntlmny',align:'right',colspan:2},
		 	{width : '100',title : '客单价',field : 'uprice',align:'right',rowspan:2,
		    	formatter : function(value,row,index){
		    		if(value == 0)return "0.00";
		    		return formatMny(value);
			}},
		    {width : '100',title : '预付款扣款',field : 'ndemny',align:'right',rowspan:2,
		    	formatter : function(value,row,index){
		    		if(value == 0)return "0.00";
		    		return formatMny(value);
			}},
			{width : '100',title : '返点扣款',field : 'nderebmny',align:'right',rowspan:2,
		    	formatter : function(value,row,index){
		    		if(value == 0)return "0.00";
		    		return formatMny(value);
				}}],[
				 	{width : '60',title : '续费',field : 'rnum',align:'right'}, 
				  	{width : '60',title : '新增',field : 'anum',align:'right'}, 
				 	{width : '100',title : '续费',field : 'rntlmny',align:'right',
				    	formatter : function(value,row,index){
				    		if(value == 0)return "0.00";
				    		return formatMny(value);
					}},
				 	{width : '100',title : '新增',field : 'antlmny',align:'right',
				    	formatter : function(value,row,index){
				    		if(value == 0)return "0.00";
				    		return formatMny(value);
					}},
				    ]
				],
		onLoadSuccess : function(data) {
			setFooter();
		}
	});
}

/**
 * 快速过滤
 */
function quickfiltet(){
	$('#filter_value').textbox('textbox').keydown(function (e) {
		 if (e.keyCode == 13) {
	           var filtername = $("#filter_value").val(); 
	           reloadData(filtername)
        }
  });
}
