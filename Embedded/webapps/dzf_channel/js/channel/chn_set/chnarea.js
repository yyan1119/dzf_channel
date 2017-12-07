var contextPath = DZF.contextPath;
var status="brows";
var editIndex = undefined;

$(function() {
	load();
	initCard();
	initManger();
});

function load() {
	// 列表显示的字段
	$('#grid').datagrid({
		url : DZF.contextPath + '/chn_set/chnarea!query.action',
		idField : 'pk_chnarea',
//		pageNumber : 1,
//		pageSize : DZF.pageSize,
//		pageList : DZF.pageList,
//		pagination : true,
		rownumbers : true,
		singleSelect : true,
		height : Public.setGrid().h,
		columns : [ [ {
			width : '80',
			title : '编码',
			field : 'acode',
		},{
			width : '130',
			title : '区域',
			field : 'aname'
		}, {
			width : '140',
			title : '区域负责人',
			field : 'uname'
		},{
			width : '260',
			title : '所属省市',
			field : 'provnames',
			formatter : function(value) {
	  			if(value!=undefined){
	  				return "<span title='" + value + "'>" + value + "</span>";
	  			}}
		},{
			width : '280',
			title : '备注',
			field : 'vmemo',
	  		formatter : function(value) {
	  			if(value!=undefined){
	  				return "<span title='" + value + "'>" + value + "</span>";
	  			}}
		},{
			width :'250',
			title : '操作',
			field : 'cz',
			align : 'center',
			formatter: 
				function(value,row,index){
					 return '<a class="ui-btn ui-btn-xz" style="margin-bottom:0px;" onclick="edit(\''+row.pk_area+'\',\''+1+'\');" plain="true" href="javascript:void(0);">查看</a>'+
					 	'<a class="ui-btn ui-btn-xz" style="margin-bottom:0px;" onclick="edit(\''+row.pk_area+'\',\''+0+'\');" plain="true" href="javascript:void(0);">修改</a>'+
						'<a class="ui-btn ui-btn-xz" style="margin-bottom:0px;" onclick="del(\''+row.pk_area+'\');" plain="true" href="javascript:void(0);">删除</a>'
						;
      	  		}
		},{
			title : '主键',
			field : 'pk_area',
			hidden: true
		},{
			title : '用户主键',
			field : 'uid',
			hidden: true
		}
		] ],
		onLoadSuccess : function(data) {
			parent.$.messager.progress('close');
			$('#grid').datagrid("selectRow", 0);  	
		}
	});
}


function add(){
    $('#cardDialog').dialog({
		modal:true,
	});
    $('#cardDialog').dialog('open').dialog('center').dialog('setTitle','渠道区域划分');
    status="add";
    setItemEdit(false);
    $('#chnarea').form("clear");
    $('#cardGrid').datagrid('loadData', { total : 0, rows : [] });// 清楚缓存数据
    for(var i=0;i<5;i++){
    	$('#cardGrid').datagrid('appendRow', {});
    }
//	editIndex = $('#cardGrid').datagrid('getRows').length - 1;
	editIndex =0;
	$('#cardGrid').datagrid('beginEdit',editIndex);
};


/**
 * 编辑
 */
function edit(id,style) {
	if(style==0){//修改
		status='edit';
	}else{//查看
		status='brows';
	}
    jQuery.ajax({
    	url : DZF.contextPath + '/chn_set/chnarea!queryByPrimaryKey.action',
    	data : {
    		'pk_area' : id,
    	},
    	type : 'post',
    	dataType : 'json',
    	success: function(result){
    		if (result.success){
    			$('#cardDialog').dialog({
    				modal:true,
    			});
    			$('#cardDialog').dialog('open').dialog('center').dialog('setTitle','渠道区域');
    			if(status=="brows"){
    				setItemEdit(true);
    			}else{
    				setItemEdit(false);
    			}
    			$('#chnarea').form("clear");
    			$('#chnarea').form('load', result.rows[0]);
    			var row =result.rows[0];
    			$("#cardGrid").datagrid("loadData",row.children);
    			editIndex = $('#cardGrid').datagrid('getRows').length - 1;
    		} else {
    			Public.tips({content:result.msg,type:1});
    		} 
    	}
    });
}

//删除
function del(id){
	var rows = $('#grid').datagrid('getRows');
	var index=$('#grid').datagrid('getRowIndex');
	var row=rows[index];
	$.messager.confirm("提示", "你是否确认要删除"+row.aname+"？", function(flag) {
		if (flag) {
			$.ajax({
				url : DZF.contextPath + '/chn_set/chnarea!delete.action',
				data : {
					'pk_area': id,
				},
				type : 'post',
				dataType : 'json',
				success : function(result) {
					if (result.success) {
						$("#grid").datagrid("reload");
						Public.tips({
							content : result.msg
						});
					} else {
						Public.tips({
							content : result.msg,
							type : 1
						});
					}
				}
			});
		} else {
			return null;
		}
	});
}

/**
 *  校验保存
 */
function checkSave(){
	//校验
	var flag = $('#chnarea').form('validate');
	if(flag == false){
		Public.tips({content:"必输信息为空或格式不正确",type:2});
		return;
	}
	var reg = new RegExp("[\\u4E00-\\u9FFF]+","g");
	if(reg.test($('#acode').textbox('getValue'))){ 
		Public.tips({content:"大区编码不能包含汉字",type:2});
		$('#acode').textbox('setValue',"");
		return;
	}
	endBodyEdit();
	// 校验子表
//	var bodys=JSON.parse(JSON.stringify( $("#cardGrid").datagrid("getRows")));
	var bodys= $("#cardGrid").datagrid("getRows");
	var length=JSON.parse(JSON.stringify(bodys.length));
	var u=0;
	for (var i = 0; i <length; i++) {
		if(!bodys[u].ovince&&!bodys[u].uid&&!bodys[u].vmemo){
			$('#cardGrid').datagrid('deleteRow',u);
			continue;
		}
		$('#cardGrid').datagrid('beginEdit',u);
		flg = $("#cardGrid").datagrid("validateRow", u);
		if (!flg){
			Public.tips({
				content : "必输信息为空或格式不正确",
				type : 2
			});
			return ;
		}
		u++;
	}
	save();
}

/**
 *  保存（新增和修改）
 */
function save() {
	var childBody1 = "";
	var rows = $("#cardGrid").datagrid('getRows');
	for (var i = 0; i < rows.length; i++) {
		childBody1 = childBody1 + JSON.stringify(rows[i]);
	}
	var childBody1d="";
	var rows = $("#cardGrid").datagrid('getChanges','deleted');
	for (var i = 0; i< rows.length; i++) {
		childBody1d = childBody1d + JSON.stringify(rows[i]);
	}
	var postdata = new Object();
	postdata["head"] = JSON.stringify(serializeObject($('#chnarea')));
	postdata["body1"] = childBody1;
	postdata["body1d"] = childBody1d;
	$.ajax({
		type : 'POST',
		url :DZF.contextPath + '/chn_set/chnarea!save.action',
		data : postdata,
		dataType : 'json',
		success : function(result) {
			$.messager.progress('close');
			if (result.success) {
				$('#cardDialog').dialog('close');
				Public.tips({
					content : result.msg,
					type : 0
				});
				load();
			} else {
				Public.tips({
					content : result.msg,
					type : 2
				});
			}
		}
	});
}


function cancel(){
	if(status == "add" || status == "edit"){
		$.messager.confirm("提示", "确定取消吗？", function(flag) {
			if (flag) {
				$('#cardDialog').dialog('close');
			} else {
				return null;
			}
		});
	}else{
		$('#cardDialog').dialog('close');
	}
}

/**
 * 字段编辑
 */
function setItemEdit(isEdit){
	$('#acode').textbox('readonly',isEdit);
	$('#aname').textbox('readonly',isEdit);
	$('#uname').textbox('readonly',isEdit);
	$('#vmemo').textbox("readonly",isEdit);
	updateBtnState();
}

/**
 * 卡片界面的按钮显示及隐藏
 */
function updateBtnState(){
	if("add"==status||"edit"==status){
		$('#save').show();
		$('#cancel').show();
	}else if("brows"==status){
		$('#save').hide();
		$('#cancel').hide();
	}	
}


function initChnUser(){
	$("#userdialog").dialog({
		width : 520,
		height : 530,
		readonly : true,
		title : '选择用户',
		cache : false,
		modal : true,
		href : contextPath + '/ref/chnuser_select.jsp',
		queryParams:{
			dblClickRowCallback : 'selectChnUser',
		},
		buttons : [ {
			text : '确认',
			handler : function() {
				var row = $('#userTable').datagrid('getSelected');
				if(row){
					selectChnUser(row);
				}else{
					Public.tips({
						content : "请选择需要处理的数据",
						type : 2
					});
				}
			}
		}, {
			text : '取消',
			handler : function() {
				$("#userdialog").dialog('close');
			}
		} ]
	});
}

/**
 * 渠道经理选择事件
 */
function selectChnUser(row){
	var uid = $('#cardGrid').datagrid('getEditor', {index:editIndex,field:'uid'});
	var uname = $('#cardGrid').datagrid('getEditor', {index:editIndex,field:'uname'});
	$(uid.target).textbox('setValue', row.uid);
	$(uname.target).textbox('setValue', row.uname);
	$('#userdialog').dialog('close');
}

/**
 * 大区总经理参照初始化
 */
function initManger(){
	$('#uname').searchbox({
		editable:false,
		prompt:'选择用户',
	    searcher:function(){
	    	$('#userdialog').dialog({
	    		width : 520,
	    		height : 530,
	    		readonly : true,
	    		close:true,
	    		title : '选择用户',
	    		modal : true,
	    		href : DZF.contextPath+'/ref/chnuser_select.jsp',
	    		queryParams:{
	    			dblClickRowCallback : 'selectManager',
	    		},
	    		buttons : [ {
	    			text : '确认',
	    			handler : function() {
	    				var row = $('#userTable').datagrid('getSelected');
	    				if(row){
	    					selectManager(row);
	    				}else{
	    					Public.tips({
	    						content : "请选择一行数据",
	    						type : 2
	    					});
	    				}
	    			}
	    		}, {
	    			text : '取消',
	    			handler : function() {
	    				$('#userdialog').dialog('close');
	    			}
	    		}]
	    	});
	    }
	});
}

function selectManager(row){
	$('#uname').textbox('setValue',row.uname);
	$('#uid').val(row.uid);
	$('#userdialog').dialog('close');
}


/**
 * 卡片grid初始化
 */
function initCard(){
	var areas;
	//地区初始化
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
				areas=result.rows;
			} else {
				Public.tips({content : result.msg,type : 2});
			}
		}
	});
	
	$('#cardGrid').datagrid({
		striped : true,
		rownumbers : true,
		idField : 'pk_areab',
		height : 220,
		singleSelect : true,
		columns : [ [ 
        {
        	width : '80',
			field : 'button',
			title : '操作',
        	formatter : coperatorLink
		}, {
			width : '200',
			field : 'provname', 
			title : '负责地区',
			editor : {
				type: 'combobox',
                options: {
                	height: 35,
                	panelHeight: 100,
                	showItemIcon: true,
                	valueField: "name",
                	editable: false,
                	required : true,
                	textField: "name",
                	data: areas,
                	onSelect: function (rec) { 
                		var ovince = $('#cardGrid').datagrid('getEditor', {index:editIndex,field:'ovince'});
                		$(ovince.target).textbox('setValue', rec.id);
                	}
                }
            }
		}, {
			width : '200',
			field : 'uname',
			title : '渠道经理',
			editor : {
				type : 'textbox',
				options : {
					height:31,
					editable:false,
					icons: [{
						iconCls:'icon-search',
						handler: function(){
							initChnUser();
						}
					}]
				}
			}
		},{
			width : '450',
			field : 'vmemo',
			title : '备注',
			editor : {
				type : 'textbox',
				options : {
					height:31,
					validType:['length[0,20]'],
        			invalidMessage:"备注最大长度不能超过20",
				}
			}
		},{
			width : '100',
			field : 'pk_area',
			title : '主表主键',
			hidden : true,
		},{
			width : '100',
			field : 'uid',
			title : '渠道经理主键',
			hidden : true,
			editor : {
				type : 'textbox',
			}
		},{
			width : '100',
			field : 'ovince',
			title : '地区',
			hidden : true,
			editor : {
				type : 'textbox',
			}
		}
		] ],
		onClickRow :  function(index, row){
			if(status == "brows"){
				return;
			}
			endBodyEdit('#cardGrid');
			if($('#cardGrid').datagrid('validateRow', editIndex)){
				if (index != undefined) {
					$('#cardGrid').datagrid('beginEdit', index);
					editIndex = index;
				}           		
			}else{
				Public.tips({
					content : "请先编辑必输项",
					type : 2
				});
			}
		} ,
	});
}

function coperatorLink(val,row,index){  
	var add = '<div><a href="javascript:void(0)" id="addBut" onclick="addRow()"><img title="增行" style="margin:0px 20% 0px 20%;" src="../../images/add.png" /></a>';
	var del = '<a href="javascript:void(0)" id="delBut" onclick="delRow(this)"><img title="删行" src="../../images/del.png" /></a></div>';
    return add + del;  
}

/**
 * 增行
 */
function addRow(){
	endBodyEdit();
	if(status == 'brows') {
		return ;
	}
	if(isCanAdd()){
		$('#cardGrid').datagrid('appendRow',{});
		editIndex = $('#cardGrid').datagrid('getRows').length - 1;
		$('#cardGrid').datagrid('beginEdit',editIndex);
	}else{
		Public.tips({
			content : "请先录入必输项",
			type : 2
		});
		return;
	}
}

/**
 * 删行
 */
function delRow(ths) {
	endBodyEdit();
	if(status == 'brows') {
		return ;
	}
	var tindex = $(ths).parents("tr").attr("datagrid-row-index");
	if(tindex == editIndex){
		var rows = $('#cardGrid').datagrid('getRows');
		if(rows && rows.length > 1){
			$('#cardGrid').datagrid('deleteRow', Number(tindex));   //将索引转为int型，否则，删行后，剩余行的索引不重新排列
		}
	}else{
		if(isCanAdd()){
			var rows = $('#cardGrid').datagrid('getRows');
			if(rows && rows.length > 1){
				$('#cardGrid').datagrid('deleteRow', Number(tindex));   //将索引转为int型，否则，删行后，剩余行的索引不重新排列
			}
		}else{
			Public.tips({
				content : "请先录入必输项",
				type : 2
			});
			return;
		}
	}
}

/**
 * 行编辑结束事件
 */
function endBodyEdit(){
    var rows = $("#cardGrid").datagrid('getRows');
 	for ( var i = 0; i < rows.length; i++) {
 		$("#cardGrid").datagrid('endEdit', i);
 	}
};

/**
 * 能否增行
 * @returns {Boolean}
 */
function isCanAdd() {
    if (editIndex == undefined) {
        return true;
    }
    if ($('#cardGrid').datagrid('validateRow', editIndex)) {
        $('#cardGrid').datagrid('endEdit', editIndex);
        editIndex = undefined;
        return true;
    } else {
        return false;
    }
}

