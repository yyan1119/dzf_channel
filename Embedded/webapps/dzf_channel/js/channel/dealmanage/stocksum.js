
var contextPath = DZF.contextPath;
var editIndex;
var status = "brows";

$(function(){
	initQry();
	initCombobox();
	load();
	reloadData();
});

/**
 * 查询初始化
 */
function initQry(){
	// 下拉按钮的事件
	$("#jqj").on("mouseover", function() {
		$("#qrydialog").show();
		$("#qrydialog").css("visibility", "visible");
	});
	queryBoxChange('#begdate','#enddate');
	$("#begdate").datebox("setValue", parent.SYSTEM.PreDate);
	$("#enddate").datebox("setValue",parent.SYSTEM.LoginDate);
	$("#jqj").html(parent.SYSTEM.PreDate+" 至  "+parent.SYSTEM.LoginDate);
	
}

function initCombobox(){
	$("#goodsname").combobox({
		onShowPanel: function () {
			initType();
        }
    })
}

/**
 * 查询商品下拉
 */
function initType(){
	$.ajax({
		type : 'POST',
		async : false,
	    url : DZF.contextPath + '/dealmanage/stockoutin!queryComboBox.action',
		dataTye : 'json',
		success : function(result) {
			var result = eval('(' + result+ ')');
			if (result.success) {
				$('#goodsname').combobox('loadData',result.rows);
			} else {
				Public.tips({content : result.msg,type : 2});
			}
		}
	});
};

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
		    singleSelect : true,
		    checkOnSelect : false,
//		    pagination : true,// 分页工具栏显示
//		    pageSize : DZF.pageSize,
//		    pageList : DZF.pageList,
		    remoteSort : false,
		    showFooter: true,
		    idField : 'sid',
		columns : [ 
		            [     
		                 {   width : '100',
		                	 title : '商品id',
		                	 align : 'left',
		                	 halign : 'center',
		                	 field : 'gid',
		                	 rowspan:2,
		                	 hidden : true,
	            	     }, {   
	            	    	 width : '100',
		                	 title : '规格型号id',
		                	 align : 'left',
		                	 halign : 'center',
		                	 field : 'pk_goodsspec',
		                	 rowspan:2,
		                	 hidden : true,
	            	     },
		            	  {
		            	      width : '100',
		            	      title : '商品编码',
		            	      align : 'left',
		            	      halign : 'center',
		            	      field : 'gcode',
		            	      rowspan:2,
		            	      
		            	    }, {
		            	      width : '150',
		            	      title : '商品',
		            	      field : 'gname',
		            	      halign : 'center',
		            	      align : 'left',
		            	      rowspan:2,
		            	      formatter : function(value, row, index) {
		            	    	  if(!isEmpty(row.gid)){
		            	    		  return '<a href="javascript:void(0)"  style="color:blue" onclick="toDetail(\''+row.gid+'\',\''+row.pk_goodsspec+'\')">' + value + '</a>';
			            	      }else{
			            	          return ""; 
			            	      }
		                      }
		            	    }, {
		            	      width : '100',
		            	      title : '规格',
		            	      field : 'spec',
		            	      halign : 'center',
		            	      align : 'center',
		            	      rowspan:2,
		            	    }, {
			            	    width : '100',
			            	    title : '型号',
			            	    field : 'type',
			            	    halign : 'center',
			            	    align : 'center',
			            	    rowspan:2,
			            	 }, {
			                   width : '100',
			                   title : '期初余额',
			                   field : 'balancestart',
			                   halign : 'center',
			                   align : 'center',
			                   colspan:3,
			              },{
			                   width : '100',
			                   title : '本期入库',
			                   field : 'instock',
			                   halign : 'center',
			                   align : 'center',
			                   colspan:2,
			              },{
			                   width : '100',
			                   title : '本期卖出',
			                   field : 'outstock',
			                   halign : 'center',
			                   align : 'center',
			                   colspan:2,
			              },{
			                   width : '100',
			                   title : '期末余额',
			                   field : 'balanceend',
			                   halign : 'center',
			                   align : 'center',
			                   colspan:3,
			              },
		            	    
		             ] ,
        [
            { field : 'numstart', title : '数量', width : 100, halign:'center',align:'right'},
            { field : 'pricestart', title : '单价', width : 100, halign:'center',align:'right',
            	formatter : function(value, row, index) {
            		if(!isEmpty(row.gid)){
            			if(value == null || value=='0'){
                			return "0.0000";
                		}else{
                			return value.toFixed(4);
                		}
            		}
            	}
            },
            { field : 'moneystart', title : '金额', width : 100, halign:'center',align:'right',formatter : formFourMny},
            { field : 'numin', title : '数量', width : 100, halign:'center',align:'right'},
            { field : 'moneyin', title : '金额', width : 100,halign:'center',align:'right',formatter : formFourMny},
            { field : 'numout', title : '数量', width : 100, halign:'center',align:'right'},
            { field : 'moneyout', title : '金额', width : 100,halign:'center',align:'right',formatter : formFourMny},            
            { field : 'numend', title : '数量', width : 100, halign:'center',align:'right'},
            { field : 'priceend', title : '单价', width : 100, halign:'center',align:'right',
            	formatter : function(value, row, index) {
            		if(!isEmpty(row.gid)){
            			if(value == null || value=='0'){
                			return "0.0000";
                		}else{
                			return value.toFixed(4);
                		}
            		}
            	}
            },
            { field : 'moneyend', title : '金额', width : 100, halign:'center',align:'right',formatter : formFourMny},
            
        ] ],
        
        onLoadSuccess : function(data) {
        	var rows = $('#grid').datagrid('getRows');
        	calFooter();
        	var numstart = 0;	
        	var pricestart = 0;	
        	var moneystart = 0;	
        	
        	var numin = 0;	
        	var moneyin = 0;
        	
        	var numout = 0;	
        	var moneyout = 0;
        	
        	var numend = 0;	
        	var priceend = 0;	
        	var moneyend = 0;	
        	
        	for (var i = 0; i < rows.length; i++) {
        		if(rows[i].numstart != undefined && rows[i].numstart != null){
        			numstart += parseFloat(rows[i].numstart);
        		}
        		if(rows[i].pricestart != undefined && rows[i].pricestart != null){
        			pricestart += parseFloat(rows[i].pricestart);
        		}
        		if(rows[i].moneystart != undefined && rows[i].moneystart != null){
        			moneystart += parseFloat(rows[i].moneystart);
        		}
        		
        		if(rows[i].numin != undefined && rows[i].numin != null){
        			numin += parseFloat(rows[i].numin);
        		}
        		if(rows[i].moneyin != undefined && rows[i].moneyin != null){
        			moneyin += parseFloat(rows[i].moneyin);
        		}
        		
        		if(rows[i].numout != undefined && rows[i].numout != null){
        			numout += parseFloat(rows[i].numout);
        		}
        		if(rows[i].moneyout != undefined && rows[i].moneyout != null){
        			moneyout += parseFloat(rows[i].moneyout);
        		}
        		
        		if(rows[i].numend != undefined && rows[i].numend != null){
        			numend += parseFloat(rows[i].numend);
        		}
        		if(rows[i].priceend != undefined && rows[i].priceend != null){
        			priceend += parseFloat(rows[i].priceend);
        		}
        		if(rows[i].moneyend != undefined && rows[i].moneyend != null){
        			moneyend += parseFloat(rows[i].moneyend);
        		}
        	}
        },
	});
}


//金额，保留小数点后4位
function formFourMny(value) {
	if(value == null || value=='0'){
		return "0.0000";
	}else{
		return value.toFixed(4);
	}
}

/**
 * 计算合计
 */
function calFooter(){
	var rows = $('#grid').datagrid('getRows');
	var footerData = new Object();
	//期初余额
	var numstart = 0;	
	//var pricestart = 0;
	var moneystart = 0;
	
	//本期入库
	var numin = 0;	
	var moneyin = 0;
	
	//本期出库
	var numout = 0;	
	var moneyout = 0;
	
	//期末余额
	var numend = 0;	
	//var priceend = 0;
	var moneyend = 0;
	
	for (var i = 0; i < rows.length; i++) {
		numstart += getFloatValue(rows[i].numstart);
		//pricestart += getFloatValue(rows[i].pricestart);
		moneystart += getFloatValue(rows[i].moneystart);
		
		numin += getFloatValue(rows[i].numin);
		moneyin += getFloatValue(rows[i].moneyin);
		
		numout += getFloatValue(rows[i].numout);
		moneyout += getFloatValue(rows[i].moneyout);
		
		numend += getFloatValue(rows[i].numend);
		//priceend += getFloatValue(rows[i].priceend);
		moneyend += getFloatValue(rows[i].moneyend);
	  
	}

	 footerData['gcode'] = "合计";
	 footerData['numstart'] = numstart;
	// footerData['pricestart'] = pricestart;
	 footerData['moneystart'] = moneystart;
	 
	 footerData['numin'] = numin;
	 footerData['moneyin'] = moneyin;
	 
	 footerData['numout'] = numout;
	 footerData['moneyout'] = moneyout;
	 
	 footerData['numend'] = numend;
	// footerData['priceend'] = priceend;
	 footerData['moneyend'] = moneyend;
    var fs=new Array(1);
    fs[0] = footerData;
    $('#grid').datagrid('reloadFooter',fs);
}

String.prototype.startWith=function(str){
	var reg=new RegExp("^"+str);
	return reg.test(this);
	}


/**
 * 查询数据
 */
function reloadData(){
	var url = DZF.contextPath + '/dealmanage/stocksum!query.action';
	$('#grid').datagrid('options').url = url;
	var gids=$('#goodsname').combobox('getValues');
	var strgids="";
	for(i=0;i<gids.length;i++){
		strgids+=","+gids[i];
	}
	strgids=strgids.substring(1);
	
	$('#grid').datagrid('load', {
		'begdate' : $("#begdate").datebox('getValue'),
		'enddate' : $("#enddate").datebox('getValue'),
		'gid' :  strgids,
	});
	$('#grid').datagrid('clearSelections');
	$('#grid').datagrid('clearChecked');
	$('#qrydialog').hide();
}

/**
 * 清除查询条件
 */
function clearParams(){
	$("#qvcode").textbox('setValue',null);
	$("#goodsname").combobox('clear');
}


/**
 * 取消
 */
function closeCx(){
	$("#qrydialog").hide();
}

/**
 * 跳转到出入库明细表页面
 */
function toDetail(gid,pk_goodsspec){
	var begdate=$("#begdate").datebox('getValue');
	var enddate=$("#enddate").datebox('getValue');
	
	var url = 'channel/dealmanage/stockoutin.jsp?operate=toDetail&gid='+gid+'&pk_goodsspec='+pk_goodsspec+'&begdate='+begdate+'&enddate='+enddate;
	parent.addTabNew('出入库明细表', url);
}

