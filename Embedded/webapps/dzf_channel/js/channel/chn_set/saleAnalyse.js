var contextPath = DZF.contextPath;
var loadrows;

$(window).resize(function(){ 
	$('#grid').datagrid('resize',{ 
		height : Public.setGrid().h,
		width : 'auto'
	});
});

$(function() {
	initArea();
	initProvince();
	initDate();
	load();
	quickfiltet();
});

function initArea(){
	$.ajax({
		type : 'POST',
		async : false,
		url : DZF.contextPath + '/report/manager!queryArea.action',
		dataTye : 'json',
		success : function(result) {
			var result = eval('(' + result + ')');
			if (result.success) {
			    $('#aname').combobox('loadData',result.rows);
			} else {
				Public.tips({content : result.msg,type : 2});
			}
		}
	});
}

function initProvince(){
	$.ajax({
		type : 'POST',
		async : false,
		url : DZF.contextPath + '/sys/sys_area!queryComboxArea.action',
		data : {
			parenter_id : 1,
		},
		dataTye : 'json',
		success : function(result) {
			var result = eval('(' + result + ')');
			if (result.success) {
			    $('#ovince').combobox('loadData',result.rows);
			} else {
				Public.tips({content : result.msg,type : 2});
			}
		}
	});
}

function initDate(){
	$("#bdate").datebox('setValue',parent.SYSTEM.PreDate);
	$("#edate").datebox('setValue',parent.SYSTEM.LoginDate)
}

function load() {
	var bdate=$("#bdate").datebox('getValue');
	var edate=$("#edate").datebox('getValue')
	var queryData={
			"bdate" : bdate,
			"edate" : edate,
		};
	var aname=$("#aname").combobox('getValue');
	if(!isEmpty(aname)){
		queryData['aname']=aname;
	}
	var ovince=$("#ovince").combobox('getValue');
	if(!isEmpty(ovince)){
		queryData['ovince']=ovince;
	}
	// 列表显示的字段
	$('#grid').datagrid({
		url : DZF.contextPath + '/chn_set/saleAnalyse!query.action',
		rownumbers : true,
		striped : true,
		singleSelect : true,
		fitColumns:false,
		height : Public.setGrid().h,
		queryParams : queryData,
		remoteSort : false,
		showFooter:true,
//		sortName:"visnum",
//		sortOrder:"desc",
		columns : [ [	
		             	{width : '130',title : '大区',field : 'aname',halign: 'center',align:'left'}, 
		            	{width : '120',title : '省（市）',field : 'provname',halign: 'center',align:'left'}, 
		            	{width : '250',title : '加盟商',field : 'corpnm',halign: 'center',align:'left'},
		            	{ field : 'visnum', title : '拜访数',width :'80',halign: 'center',align:'center',sortable:true },
			            { field : 'viscustnum', title : '拜访客户数',width :'80',halign: 'center',align:'center',sortable:true },
			            { field : 'signum', title : '签约客户数',width :'80',halign: 'center',align:'center',sortable:true}, 
			            { field : 'agentnum', title : '代账合同数',width :'80',halign: 'center',align:'center',sortable:true} ,
			            { field : 'increnum', title : '增值合同数',width :'80',halign: 'center',align:'center',sortable:true} ,
			            { field : 'contmny', title : '合同金额',width :'110',halign: 'center',align:'right',
			            	  sortable:true,formatter : formatMny} ,
			            { field : 'pricemny', title : '客单件',width :'100',halign: 'center',align:'right',
			            	  sortable:true,formatter : formatMny}, 
		          ] ],
		onLoadSuccess : function(data) {
			if(data.rows && loadrows == null){
				loadrows = data.rows;
			}
			$.messager.progress('close');
			calFooter();
		}
	});
}

/**
 * 计算合计
 */
function calFooter(){
	var rows = $('#grid').datagrid('getRows');
	var footerData = new Object();
	var visnum = 0;
	var viscustnum = 0;
	var signum = 0;
	var agentnum = 0;
	var increnum = 0;
	var contmny = 0;
	var pricemny = 0;
	for (var i = 0; i < rows.length; i++) {
		if(rows[i].visnum != undefined && rows[i].visnum != null){
			visnum += parseFloat(rows[i].visnum);
		}
		if(rows[i].viscustnum != undefined && rows[i].viscustnum != null){
			viscustnum += parseFloat(rows[i].viscustnum);
		}
		if(rows[i].signum != undefined && rows[i].signum != null){
			signum += parseFloat(rows[i].signum);
		}
		if(rows[i].agentnum != undefined && rows[i].agentnum != null){
			agentnum += parseFloat(rows[i].agentnum);
		}
		
		if(rows[i].increnum != undefined && rows[i].increnum != null){
			increnum += parseFloat(rows[i].increnum);
		}
		if(rows[i].contmny != undefined && rows[i].contmny != null){
			contmny += parseFloat(rows[i].contmny);
		}
		if(rows[i].pricemny != undefined && rows[i].pricemny != null){
			pricemny += parseFloat(rows[i].pricemny);
		}
	}
	footerData['corpnm'] = '合计';
	footerData['visnum'] = visnum;
	footerData['signum'] = signum;
	footerData['agentnum'] = agentnum;
	footerData['increnum'] = increnum;
	footerData['contmny'] = contmny;
	footerData['pricemny'] = pricemny;
	var fs=new Array(1);
	fs[0] = footerData;
	$('#grid').datagrid('reloadFooter',fs);
}

function quickfiltet(){
	$('#quname').textbox('textbox').keydown(function (e) {
        if (e.keyCode == 13) {
        	$('#grid').datagrid('unselectAll');
 		   var filtername = $("#quname").val(); 
		   if (filtername) {
				var jsonStrArr = [];
				if(loadrows){
					for(var i=0;i<loadrows.length;i++){
						var row = loadrows[i];
						if(row.corpnm.indexOf(filtername) >= 0){
							jsonStrArr.push(row);
						} 
					}
					$('#grid').datagrid('loadData',jsonStrArr);   
				}
			}else{
				load();
			} 
        }
    });
}

/**
 * 导出
 */
function doExport(){
	var datarows = $('#grid').datagrid("getRows");
	if(datarows == null || datarows.length == 0){
		Public.tips({content:'当前界面数据为空',type:2});
		return;
	}
	var columns = $('#grid').datagrid("options").columns[0];
	Business.getFile(contextPath+ '/chn_set/saleAnalyse!exportExcel.action',{'strlist':JSON.stringify(datarows)}, true, true);
}
