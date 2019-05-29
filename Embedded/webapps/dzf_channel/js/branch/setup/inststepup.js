var contextPath = DZF.contextPath;

$(function(){
	initInstGrid();
	initCorpGrid();
	initListColumn(0);
	queryCorpInfo();
});

/**
 * 初始化机构名称
 */
function initCombobox(){
	$("#pk_bset").combobox({
		onShowPanel : function() {
			initListColumn(0);
		}
	})
}



function initInstGrid(){
	$("#instgrid").datagrid({
		fit:true,
		rownumbers : true,
		singleSelect : true,
		idField : 'pk_bset',
		columns : [ [
		{field : 'operater',title : '操作',width : 101,
			formatter: function(value,row,index){
        		return '<a href="javascript:void(0)" style="margin-bottom:0px;margin-left:10px;color:blue;" onclick="edit(' + index + ')">编辑</a>';
        	}	
		} ,
		{field : 'name',title : '机构名称',width : 102,halign : 'center',align : 'center'} ,
		{field : 'pk_bset',title : '机构主键',width : 100,hidden : true} 
		] ],onLoadSuccess:function(data){
            $("#instgrid").datagrid("selectRow",0);
            $('#instgrid').datagrid("scrollTo",0);
		},
		onClickRow:function(index,row){
			$("#corpgrid").datagrid("loadData",{total:0,rows:[]});
			var row = $("#instgrid").datagrid("getSelected");
			initListColumn(1,row.pk_bset);
		},
	});
}



function initCorpGrid(){
	$("#corpgrid").datagrid({
		rownumbers : true,
		fit:true,
		singleSelect : false,
        checkbox : true,
	    idField:'pk_bcorp',
		columns  : [ [
			{field:'ck',checkbox:true},   
			{field:'operater',title:'操作',width:100,align:'center',halign:'center',
				formatter: function(value,row,index){
					if(row.isseal=='Y'){
						return '<span style="margin-bottom:0px;" onclick="seal(this)">封存</span>'  
		        		 +'  '+'<a href="javascript:void(0)" style="margin-bottom:0px;margin-left:5px;color:blue;" onclick="seal(\''+row.pk_bcorp+'\')">启用</a>'
		        		 +'  '+'<a href="javascript:void(0)" style="margin-bottom:0px;margin-left:5px;color:blue;" onclick="del(\''+row.pk_bcorp+'\')">删除</a>';
					}else{
						return '<a href="javascript:void(0)" style="margin-bottom:0px;color:blue;" onclick="seal(\''+row.pk_bcorp+'\')">封存</a>'
		        		 +'  '+'<span style="margin-bottom:0px;margin-left:5px;" onclick="seal(this)">启用</span>'
		        		 +'  '+'<a href="javascript:void(0)" style="margin-bottom:0px;margin-left:5px;color:blue;" onclick="del(\''+row.pk_bcorp+'\')">删除</a>';
					}
	        		
	        	}
			},
			{field : 'name',title : '企业识别号',width : 100,halign : 'center',align : 'left'} ,
			{field : 'uname',title : '公司名称',width : 134,halign : 'center',align : 'center'} ,
			{field : 'lman',title : '联系人',width : 100,halign : 'center',align : 'center'},
			{field : 'phone',title : '联系方式',width : 100,halign : 'center',align : 'center'},
			{field : 'isseal',title : '是否封存',width : 100,halign : 'center',align : 'center',
				formatter : function(value,row) {
     	           if(!isEmpty(row.pk_bcorp)){
     	             if (value == 'Y')
     	               return '是';
     	             return '否';
     	           }
     	         }	
			},
			{field : 'memo',title : '备注',width : 194,halign : 'center',align : 'center'},
			{field : 'pk_bcorp',title : '主键',width : 100,hidden : true}
			
		] ],
		onLoadSuccess:function(){
			$("#corpgrid").datagrid("clearSelections");
    		$('#corpgrid').datagrid("scrollTo",0);
		},
	});
}


function initListColumn(qtype,pk_id){
	$.ajax({
		type : "POST",
		url : contextPath+'/branch/setup!query.action',
		data:{qrytype:qtype,pk_currency:pk_id},
        async: false, 
        dataTye:'json',
        success:function(result){
        	var result = eval('(' + result + ')');
        	if(result.success){
        		if(result.rows!=undefined){
        			$("#instgrid").datagrid("loadData",result.rows);
        			$('#pk_bset').combobox('loadData', result.rows);
        		}
                $("#corpgrid").datagrid("loadData",result.data);
        	}else{
        		$("#instgrid").datagrid("loadData",{total:0,rows:[]});
        		$("#corpgrid").datagrid("loadData",{total:0,rows:[] });
        	}
        },
	});
}


function addInst(){
	$('#addInstDialog').dialog('open').dialog('center').dialog('setTitle', '新增机构');
	$('#addInstDialog').form("clear");
}

function onSave(){
	if ($("#inst_add").form('validate')) {
		$('#inst_add').form('submit', {
			url : DZF.contextPath + '/branch/setup!saveInst.action',
			success : function(result) {
				var result = eval('(' + result + ')');
				if (result.success) {
					$('#addInstDialog').dialog('close');
					initListColumn(0);
					Public.tips({
						content : result.msg,
					});
				} else {
					Public.tips({
						content : result.msg,
						type : 2
					});
				}
			}
		});
	} else {
		Public.tips({
			content : "机构名称为空或输入不正确",
			type : 2
		});
		return; 
	}
}


function updateInst(){
	var rows = $('#corpgrid').datagrid('getChecked');
	if (rows == null || rows.length == 0) {
		Public.tips({
			content : '请选择需要处理的数据',
			type : 2
		});
		return;
	}
	$('#updateInstDialog').dialog('open').dialog('center').dialog('setTitle', '更换机构');
}



function editInst(){
	var ids = "";
	var row = $('#instgrid').datagrid('getSelected');
	var rows = $('#corpgrid').datagrid('getChecked');
	for(var i=0;i<rows.length;i++){
		ids = ids +","+rows[i].pk_bcorp;
	}
	ids = ids.substring(1);
	
	$.ajax({
		type : "post",
		dataType : "json",
		traditional : true,
		async : false,
		url : contextPath + '/branch/setup!editInst.action',
		data : {
			"ids" : ids,
			"pk_bset" : $('#pk_bset').combobox('getValue')
		  
		},
		success : function(data) {
			if (!data.success) {
				Public.tips({
					content :  data.msg,
					type : 2
				});	
				return;
			} else {
				$('#updateInstDialog').dialog('close');
				initListColumn(1,row.pk_bset);
			}
		},
	});
}


function onSaveCorp(){
	
	var row = $('#instgrid').datagrid('getSelected');
	$('#bset').val(row.pk_bset);
	if ($("#corp_add").form('validate')) {
		$('#corp_add').form('submit', {
			url : DZF.contextPath + '/branch/setup!saveCorp.action',
			success : function(result) {
				var result = eval('(' + result + ')');
				if (result.success) {
					$('#addCorpDialog').dialog('close');
					initListColumn(1,row.pk_bset);
					Public.tips({
						content : result.msg,
					});
				} else {
					Public.tips({
						content : result.msg,
						type : 2
					});
				}
			}
		});
	} else {
		Public.tips({
			content : "必输信息为空或输入不正确",
			type : 2
		});
		return; 
	}
}

function edit(index){
	$('#addInstDialog').dialog('open').dialog('center').dialog('setTitle', '修改机构');
	var erow = $('#instgrid').datagrid('getData').rows[index];
	var row = queryByID(erow.pk_bset,0);
	if(isEmpty(row)){
		return;
	}
	$('#addInstDialog').form('load', row);
	
}

function queryByID(pk_id,type){
	var row;
	$.ajax({
		type : "post",
		dataType : "json",
		traditional : true,
		async : false,
		url : contextPath + '/branch/setup!queryById.action',
		data : {
			"id" : pk_id,
		    "type" : type,//0-机构设置  1-公司设置
		},
		success : function(data) {
			if (!data.success) {
				Public.tips({
					content :  data.msg,
					type : 1
				});	
				return;
			} else {
				row = data.rows;
			}
		},
	});
	return row;
}


function seal(pk_id){
	
	var row = queryByID(pk_id,1);
	//var erow = = $('#corpgrid').datagrid('getSelected');
	var tip ="确定封存？";
	if(row.isseal=='Y'){
		tip = "确定启用？";
	}
	$.messager.confirm("提示", tip, function(flag) {
		if (flag) {
			$.ajax({
				type : "post",
				dataType : "json",
				url : contextPath + '/branch/setup!editSseal.action',
				data : {
					"pk_bcorp" : row.pk_bcorp,
					"name" : row.name,
					"uname" : row.uname,
					"lman" : row.lman,
					"phone" : row.phone,
					"isseal" : row.isseal,
					"memo" : row.memo,
					"updatets" : row.updatets,
				},
				traditional : true,
				async : false,
				success : function(data) {
					if (!data.success) {
						Public.tips({
							content : data.msg,
							type : 2
						});
					} else {
						initListColumn(1,row.pk_bset);
						Public.tips({
							content : data.msg,
						});
					}
				},
			});
		} else {
			return null;
		}
	});
}



function del(pk_id){
	var row = queryByID(pk_id,1);
	$.messager.confirm("提示", "确定删除？", function(flag) {
		if (flag) {
			$.ajax({
				type : "post",
				dataType : "json",
				url : contextPath + '/branch/setup!deleteCorpById.action',
				data : {
					"pk_bcorp" : row.pk_bcorp,
					"name" : row.name,
					"uname" : row.uname,
					"lman" : row.lman,
					"phone" : row.phone,
					"isseal" : row.isseal,
					"memo" : row.memo,
					"updatets" : row.updatets,
				},
				traditional : true,
				async : false,
				success : function(data) {
					if (!data.success) {
						Public.tips({
							content : data.msg,
							type : 2
						});
					} else {
						initListColumn(1,row.pk_bset);
						Public.tips({
							content : data.msg,
						});
					}
				},
			});
		} else {
			return null;
		}
	});
}


/**
 * 按enter自动带出公司信息
 */
function queryCorpInfo(){
	$('#entname').textbox('textbox').keydown(function (e) {
		if (e.keyCode == 13) {
			var entname = $("#entname").textbox('getValue'); 
	        if (entname != "") { 
			$.ajax({
				type : "post",
				dataType : "json",
				traditional : true,
				async : false,
				url : contextPath + '/branch/setup!queryCorpInfo.action',
				data : {
					"name" : entname
				},
				success : function(data) {
					if (!data.success) {
						Public.tips({
							content :  data.msg,
							type : 1
						});	
						return;
					} else {
						$('#cname').textbox('setValue',data.rows.uname);
						$('#linkman').textbox('setValue',data.rows.lman);
						$('#phone').textbox('setValue',data.rows.phone);
					}
				},
			});
	        }
		}
		
	});
}


function onCancel(){
	$('#addInstDialog').dialog('close');
	$('#addCorpDialog').dialog('close');
	$('#updateInstDialog').dialog('close');
}


function addCorp(){
	$('#addCorpDialog').dialog('open').dialog('center').dialog('setTitle', '新增公司');
	$('#addCorpDialog').form("clear");
}


