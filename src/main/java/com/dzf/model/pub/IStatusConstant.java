package com.dzf.model.pub;

public interface IStatusConstant {

	/** 渠道付款单操作类型 ( 2-取消确认；3-收款确认) */
	public static int ICHNOPRATETYPE_2 = 2;
	public static int ICHNOPRATETYPE_3 = 3; 
	
	/** 合同确认操作类型 ( 1-确认成功；2-确认失败；3-取消确认；) */
	public static int ICONTRACTCONFRIM_1 = 1;
	public static int ICONTRACTCONFRIM_2 = 2;
	public static int ICONTRACTCONFRIM_3 = 3;
	
	/** 合同扣款状态(  1：待确认；2：待扣款；3：已扣款；) */
	public static int IDEDUCTSTATUS_1 = 1;
	public static int IDEDUCTSTATUS_2 = 2;
	public static int IDEDUCTSTATUS_3 = 3;

}
