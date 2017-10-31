var grid;

$(window).resize(function(){ 
	$('#grid').datagrid('resize',{ 
		height : Public.setGrid().h,
		width : 'auto'
	});
});

$(function() {
	initQryPeroid();
	load();
});

/**
 * 数据表格初始化
 */
function load(){
	$('#grid').datagrid({
		url : DZF.contextPath + "/report/financedealstaterep!query.action",
		queryParams:{
			'period' : $('#qryperiod').textbox('getValue'),//查询期间
		},
		striped : true,
		title : '',
		rownumbers : true,
		height : Public.setGrid().h,
		singleSelect : false,
		pagination : true, //显示分页
		pageSize : 20, //默认20行
		pageList : [ 20, 50, 100, 200 ],
		showRefresh : false,// 不显示分页的刷新按钮
		showFooter : true,
		border : true,
		remoteSort:false,
		//冻结在 左边的列 
		frozenColumns:[[
						{ field : 'pid',    title : '会计公司主键', hidden : true},
		                { field : 'provin',  title : '省份', width : 100,halign:'center',align:'left'}, 
		                { field : 'pname', title : '加盟商名称', width:260,halign:'center',align:'left'},
		]],
		columns : [ 
		            [ 
					 { field : 'custsmall', title : '小规模数量', width:120,halign:'center',align:'right',rowspan:2},
					 { field : 'custtaxpay', title : '一般纳税人数量', width:120,halign:'center',align:'right',rowspan:2},
		             
		             { field : 'cust', title : '客户占比(%)', halign:'center',align:'center',colspan:2},
		             { field : 'voucher', title : '凭证数量', halign:'center',align:'center',colspan:2},
		             ] ,
        [
            { field : 'custrates', title : '小规模', width : 150, formatter:formatMny, halign:'center',align:'right'}, 
            { field : 'custratet', title : '一般纳税人', width : 150, formatter:formatMny, halign:'center',align:'right'}, 
            { field : 'vouchernums', title : '小规模', width : 150, halign:'center',align:'right'}, 
            { field : 'vouchernumt', title : '一般纳税人', width : 150, halign:'center',align:'right'}, 
        ] ],
	});
}

/**
 * 查询
 */
function reloadData(){
	$('#grid').datagrid('options').url = DZF.contextPath + "/report/financedealstaterep!query.action";
	var queryParams = $('#grid').datagrid('options').queryParams;
	queryParams.period = $('#qryperiod').textbox('getValue');
	$('#grid').datagrid('options').queryParams = queryParams;
	$('#grid').datagrid('reload');
}

/**
 * 查询期间初始化
 */
function initQryPeroid(){
	$('#qryperiod').textbox({
		icons: [{
			iconCls:'foxdate',
			handler: function(e){
				click_icon(50, 90, function(val){
					if(!isEmpty(val)){
						$('#qryperiod').textbox('setValue', val);
					}
				})
			}
		}]
	});
}
