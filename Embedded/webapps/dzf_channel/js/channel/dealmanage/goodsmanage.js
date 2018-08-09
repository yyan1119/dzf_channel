var contextPath = DZF.contextPath;
var status;
var editIndex;
$(function(){
	load();
});

/**
 * 列表表格加载
 */
function load(){
	grid = $('#grid').datagrid({
		border : true,
		striped : true,
		rownumbers : true,
		fitColumns : false,
		height : Public.setGrid().h,
		singleSelect : false,
		pagination : true,// 分页工具栏显示
		pageSize : DZF.pageSize,
		pageList : DZF.pageList,
		showFooter:true,
		idField : 'gid',
		frozenColumns :[[ { field : 'ck', checkbox : true },
			              { field : 'operate', title : '操作列',width :'150',halign: 'center',align:'center',formatter:opermatter} ,
		               ]],
		columns : [ [ {
			width : '100',
			title : '主键',
			field : 'gid',
			hidden : true
		}, {
			width : '100',
			title : '商品编码',
			field : 'gcode',
			align : 'center',
            halign : 'center',
		}, {
			width : '200',
			title : '商品名称',
			field : 'gname',
			align : 'center',
            halign : 'center',
		}, {
			width : '100',
			title : '单价',
			align:'right',
            halign:'center',
			field : 'price',
			formatter : function(value,row,index){
				if(value == 0)return "0.00";
				return formatMny(value);
			},
		},{
			width : '160',
			title : '商品说明',
			field : 'note',
            halign : 'center',
			align : 'left',
			formatter : noteFormat
		}, {
			width : '100',
			title : '合同状态',
			field : 'status',
            halign : 'center',
			align : 'center',
			formatter : function(value) {
				if (value == '1')
					return '已保存';
				if (value == '2')
					return '已发布';
				if (value == '3')
					return '已下架';
			}
		}, {
			width : '100',
			title : '发布日期',
			field : 'pubdate',
            halign : 'center',
			align : 'center',
		}, {
			width : '100',
			title : '下架日期',
			field : 'dofdate',
            halign : 'center',
			align : 'center',
		}, {
			width : '100',
			title : '录入人',
			field : 'opername',
            halign : 'center',
			align : 'center',
		}, {
			width : '100',
			title : '录入日期',
			field : 'operdate',
            halign : 'center',
			align : 'center',
		} ] ],
		onLoadSuccess : function(data) {
            $('#grid').datagrid("scrollTo",0);
		},
	});
}

/**
 * 查询数据
 */
function reloadData(){
	var url = DZF.contextPath + '/dealmanage/goodsmanage!query.action';
	$('#grid').datagrid('options').url = url;
	$('#grid').datagrid('load', {
		'gcode' : $("#qgcode").val(),
		'gname' : $("#qgname").val(),
		'status' :  $('#qstatus').combobox('getValue'),
	});
	$('#grid').datagrid('clearSelections');
}

/**
 * 商品说明添加tips显示
 * @param value
 */
function noteFormat(value){
	if(value != undefined){
		return "<span title='" + value + "'>" + value + "</span>";
	}
}

/**
 * 列操作格式化
 * @param val
 * @param row
 * @param index
 * @returns {String}
 */
function opermatter(val, row, index) {
	return '<a href="#" class="ui-btn ui-btn-xz" style="margin-bottom:0px;" onclick="edit(' + index + ')">编辑</a>'+
	'<a href="#" class="ui-btn ui-btn-xz" style="margin-bottom:0px;" onclick="dele(' + index + ')">删除</a>';
}

/**
 * 修改
 * @param index
 */
function edit(index){
	var erow = $('#grid').datagrid('getData').rows[index];
	var row = queryByID(erow.gid);
	if(isEmpty(row)){
		return;
	}
	
	initFileEvent();
	$('#cbDialog').dialog('open').dialog('center').dialog('setTitle', '修改商品');
	$('#goods_add').form('clear');
	initMeas();
	initMeasSelect();
	$('#goods_add').form('load', row);
	
	$("#measid").combobox("setValue",row.measid);
	
	viewImageFiles(row);
	$('.filepath1').prop('disabled',false);
	$('.filepath2').prop('disabled',false);
	var htmlImg= '<div class="imgbox">'+
			'<div class="imgnum">'+
				'<input type="file" class="filepath1" name="imageFile" multiple="multiple"'+
					'accept="image/gif,image/jpeg,image/jpg,image/png"/>'+
				'<span class="close1"><img src="../../images/Dustbin.png"/></span>'+
				'<img src="../../images/wer_03.png" class="img1" /> ' +
				'<img src="" class="img2" />'+
			'</div>'+
		'</div>';
	$("#image1").append(htmlImg);
	initClick();
	initIdDelClick();
	status = "edit";
	editIndex = index;
}

/**
 * 展示商品图片
 * @param row
 */
function viewImageFiles(row){
	$.ajax({
		type : "POST",
		url : contextPath + "/dealmanage/goodsmanage!getAttaches.action",
  		dataType : 'json',
  		data : row,
  		processData : true,
  		async : false,//异步传输
  		success : function(result) {
			var rows = result.rows;
			arrachrows = result.rows;
			$("#image1").html('')
			$("#img12").attr("src",'');
			$("#span1").attr("data-id",'');
			$("#img11").show();
			if(rows && rows.length > 0){
				var ret = 0;
				for(var i = 0;i<rows.length;i++){
					if(rows[i].fpath){
						var url = getAttachImgUrl(rows[i]);
						var htmlImg= '<div class="imgbox">'+
										'<div class="imgnum">'+
										'<input type="file" class="filepath1" name="imageFile" multiple="multiple" accept="image/gif,image/jpeg,image/jpg,image/png"/>'+
										'<span class="close1" data-id="'+rows[i].doc_id+'"><img src="../../images/Dustbin.png"/></span>'+
										'<img src="'+url+'" class="img2" />'+
										'</div>'+
									 '</div>';
						$("#image1").append(htmlImg);
					}
				} 
				$('.filepath1').prop('disabled',true);
				$('.filepath2').prop('disabled',true);
			}
		}
	});
}

/**
 * 获取图片信息
 * @param attach
 * @returns {String}
 */
function getAttachImgUrl(row) {
	var ext = getFileExt(row['fpath']);
	if ("pdf" == ext.toLowerCase()) {
		return "../../images/typeicon/pdf.jpg";
	} else if ("txt" == ext.toLowerCase()) {
		return "../../images/typeicon/txt.jpg";
	}
	return DZF.contextPath
			+ '/dealmanage/goodsmanage!getAttachImage.action?&doc_id='
			+ row.doc_id;
}

/**
 * 获取附件扩展
 * @param filename
 * @returns
 */
function getFileExt(filename){
	var index1 = filename.lastIndexOf(".")+1;
	var index2 = filename.length;
	var ext = filename.substring(index1,index2);
	return ext;
}

/**
 * 通过主键查询商品信息
 * @param contractid
 * @returns
 */
function queryByID(gid){
	var row;
	$.ajax({
		type : "post",
		dataType : "json",
		traditional : true,
		async : false,
		url : contextPath + '/dealmanage/goodsmanage!queryByID.action',
		data : {
			"gid" : gid,
		},
		success : function(data, textStatus) {
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

/**
 * 删除
 * @param index
 */
function dele(index){
	var row = $('#grid').datagrid('getData').rows[index];
	if (row.status != 1) {
		Public.tips({
			content : '该记录不是已保存状态，不允许删除',
			type : 2
		});
		return;
	}
	$.messager.confirm("提示", "你确定删除吗？", function(flag) {
		if (flag) {
			$.ajax({
				type : "post",
				dataType : "json",
				url : contextPath + '/dealmanage/goodsmanage!delete.action',
				data : row,
				traditional : true,
				async : false,
				success : function(data, textStatus) {
					if (!data.success) {
						Public.tips({
							content : data.msg,
							type : 1
						});
					} else {
						$('#grid').datagrid('clearSelections');
						$('#grid').datagrid('deleteRow', index); 
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
 * 新增
 */
function add(){
	initFileEvent();
	$('#cbDialog').dialog('open').dialog('center').dialog('setTitle', '新增商品');
	$('#goods_add').form('clear');
	$("#image1").html('');
	var htmlImg = '<div class="imgbox">'+
					'<div class="imgnum">'+
						'<input type="file" class="filepath1" name="imageFile" multiple="multiple" accept="image/gif,image/jpeg,image/jpg,image/png"/>'+
						'<span class="close1"><img src="../../images/Dustbin.png"/></span>'+
						'<img src="../../images/wer_03.png" class="img1" /> ' +
						'<img src="" class="img2" />'+
					'</div>'+
				  '</div>';
	$("#image1").html(htmlImg);
	initMeas();
	initMeasSelect();
	
	initClick();
	initIdDelClick();
	status = "add";
}

/**
 * 计量单位选择事件
 */
function initMeasSelect(){
	$("#measid").combobox({
		onChange : function(n, o) {
			$('#mname').val($('#measid').combobox('getText'));
		}
	});
}

/**
 * 新增商品-初始化计量单位
 */
function initMeas(){
	$.ajax({
		type : 'POST',
		async : false,
		url : DZF.contextPath + '/dealmanage/goodsmanage!initMeasCombox.action',
		dataTye : 'json',
		success : function(result) {
			var result = eval('(' + result + ')');
			if (result.success) {
				$("#measid").combobox("loadData",result.rows);
			}
		}
	});
}

/**
 * 上传图片添加事件
 */
function initFileEvent(){
	$(".uploadImg").on("change",".filepath1",function(){//添加附件
		if(this.files.length <= 0){
			return;
		}
		var fname = this.files[0].name;
		var imageExt=fname.substr(fname.lastIndexOf(".")).toLowerCase();//获得文件后缀名
	    if(imageExt !='.jpg' && imageExt !='.png' && imageExt !='.jpeg'){
	        Public.tips({ content : "请上传后缀名为jpg、png、jpeg的图片", type : 2 });
	        return;
	    }
		var srcs = getObjectURL(this.files[0]);   //获取路径
		var imgsrc = $(this).nextAll(".img2").attr("src"); 
	    //this指的是input
	    $(this).nextAll(".img1").hide();   //this指的是input
	    $(this).nextAll(".img2").show();  //fireBUg查看第二次换图片不起做用
	    $(this).nextAll('.close1').show();   //this指的是input
	    $(this).nextAll(".img2").attr("src",srcs);    //this指的是input
	    if(imgsrc == null || imgsrc == ""){
	    	 var htmlImg='<div class="imgbox">'+
				     		'<div class="imgnum">'+
				     			'<input type="file" class="filepath1" name="imageFile" multiple="multiple"'+
				     				' accept="image/gif,image/jpeg,image/jpg,image/png"/>'+
				     			'<span class="close1"><img height="26" src="../../images/Dustbin.png"/></span>'+
						            '<img src="../../images/wer_03.png" class="img1" />'+
						            '<img src="" class="img2" />'+
						        '</div>'+
				         '</div>';
	    	 $(this).parent().parent().after(htmlImg);
	    }
	    initClick();
	    var data_id = $(this).nextAll('.close1').attr("data-id");
	   	if(data_id){
	   		deleteImageFile(data_id);
	   	}
	});
}

/**
 * 获取图片路径
 * @param file
 * @returns
 */
function getObjectURL(file) {
    var url = null;
	if (window.createObjectURL != undefined) {
		url = window.createObjectURL(file)
	} else if (window.URL != undefined) {
		url = window.URL.createObjectURL(file)
	} else if (window.webkitURL != undefined) {
		url = window.webkitURL.createObjectURL(file)
	}
    return url;
};

/**
 * 删除界面图片
 */
function initClick(){
	$(".close1").on("click",function() {
		var data_id = $(this).attr("data-id");
    	if(data_id){
    		deleteImageFile(data_id);
    	}
    	if($(this).nextAll(".img2").attr("src")){
    		$(this).hide();     //this指的是span
    		$(this).nextAll(".img2").hide();
    		$(this).nextAll(".img1").show();
    		if($('.imgbox').length>1){
    			$(this).parent().parent().remove();
    		}
    	}
    })
}

/**
 * 删除图片
 */
function initIdDelClick(){
    $(".Dlelete").on("click",function() {
    	if(status == "brows"){
    		return;
    	}
    	var data_id = $(this).attr("data-id");
    	if(data_id){
    		deleteImageFile(data_id);
    	}
        $(this).hide();     //this指的是span
        $(this).nextAll(".img22").hide();
        $(this).nextAll(".img11").show();
        $(this).nextAll(".img22").attr("src","")
        $(this).prev().val('');
    });
}

/**
 * 调用后台接口删除图片
 * @param data_id
 */
function deleteImageFile(data_id){
	$.ajax({
		url : contextPath + "/dealmanage/goodsmanage!deleteFile.action",
		traditional : true,
		async : false,
		dataType : 'json',
		data : {
			"doc_id" : data_id,
		},
	});
}

/**
 * 商品-新增保存
 */
function onSave(){
	if ($('.img2')[0].src.indexOf('goodsmanage.jsp') >= 0) {
		Public.tips({
			content : "商品图片不能为空",
			type : 2
		});
		return;
	}
	
	if ($("#goods_add").form('validate')) {
		$.messager.progress({
			text : '数据保存中，请稍后.....'
		});
		
		$('#goods_add').form('submit', {
			url : DZF.contextPath + '/dealmanage/goodsmanage!save.action',
			success : function(result) {
				var result = eval('(' + result + ')');
				$.messager.progress('close');
				if (result.success) {
					var row = result.rows;
					initMeas();
					$('#cbDialog').dialog('close');
					if(status == "edit"){
						$('#grid').datagrid('updateRow', {
							index : editIndex,
							row : row
						});
						editIndex = null;
					}else if(status == "add"){
						$('#grid').datagrid('appendRow',row);
					}
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
			content : "必输信息为空或格式不正确",
			type : 2
		});
		return; 
	}
}

/**
 * 商品-取消
 */
function onCancel(){
	$('#cbDialog').dialog('close');
	status = "brows";
	editIndex = null;
}

/**
 * 发布
 */
function publish(){
	
}

/**
 * 下架
 */
function off(){
	
}

/**
 * 添加单位
 */
function addMeas(){
	$('#jlDialog').dialog('open').dialog('center').dialog('setTitle', '商品计量单位');
	$('#meas_add').form('clear');
}

/**
 * 计量单位-保存
 */
function measSave(){
	if ($("#meas_add").form('validate')) {
		$.messager.progress({
			text : '数据保存中，请稍后.....'
		});
		
		$('#meas_add').form('submit', {
			url : DZF.contextPath + '/dealmanage/goodsmanage!saveMeas.action',
			success : function(result) {
				var result = eval('(' + result + ')');
				$.messager.progress('close');
				if (result.success) {
					initMeas();
					$('#jlDialog').dialog('close');
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
			content : "必输信息为空或格式不正确",
			type : 2
		});
		return; 
	}
}

/**
 * 计量单位-取消
 */
function measCancel(){
	$('#jlDialog').dialog('close');
}

