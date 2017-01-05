package com.zld.utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pay.AlipayUtil;

import com.zld.AjaxUtil;
import com.zld.CustomDefind;
import com.zld.TableField;

/**
 * �ӿ��ϴ����ݹ���
 * @author Gecko
 *
 */
public class ZldUploadUtils {

	/**
	 * http�ύ����תΪMap
	 * 
	 * @param params
	 * @return Map<String, String>
	 */
	public static Map<String, String> stringToMap(String params) {
		Map<String, String> map = new HashMap<String, String>();
		if (params != null) {
			String param[] = params.split("&");
			for (int i = 0; i < param.length; i++) {
				String pString =  AjaxUtil.decodeUTF8(param[i]);
				String p[] = pString.split("=");
				if (p != null && p.length == 2) {
					String key = getFieldName(p[0]);//������ת���ݿ��ֶ���
					if(key!=null&&!"".equals(key)){
						if(key.equals("gps")&&p[1].indexOf(",")!=-1){//�ϴ��ľ�γ����һ���ֶΣ��ö��Ÿ���
							String vs []= p[1].split(",");
							if(vs.length==2){
								map.put("longitude",vs[0]);
								map.put("latitude", vs[1]);
							}
						}else {
							map.put(key.toLowerCase(), p[1]);
						}
					}
				}
			}
		}
		return map;
	}

	/**
	 * ��֤ǩ��
	 * @param paramsMap
	 * @return
	 */
	public static boolean validateSign(String params){
		Map<String, String> paramsMap = new HashMap<String, String>();
		if (params != null) {
			String param[] = params.split("&");
			for (int i = 0; i < param.length; i++) {
				String pString = param[i];
				String p[] = pString.split("=");
				if (p != null && p.length == 2) {
						paramsMap.put(p[0],p[1]);//AjaxUtil.decodeUTF8(p[1]));
					}
				}
			}
		String sign = paramsMap.get("sign");
		if(sign==null||"".equals(sign))
			return false;
		paramsMap.remove("sign");
		String channelId = paramsMap.get("chanid");
		String key =CustomDefind.getValue("RSA"+channelId);//"yangzhou_guomai_201602";
		String pamString = AlipayUtil.createLinkString(paramsMap);
		System.out.println(pamString);
		try {
			String newSign = StringUtils.MD5(pamString+"key="+key).toUpperCase();
			System.out.println("sign:"+sign+",mysign:"+newSign);
			if(sign.equals(newSign))
				return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}

	public static void main(String[] args) {
		String pString = "parking_total=20&company_id=1009&parkingNo=35rf788&timeStamp=1457693090&city_merchants_id=321000&parkingName=asd&state=0&sign=30FE309B987AE1662524206DBB452447&address=%E6%B5%8B%E8%AF%95%E6%B5%8B%E8%AF%95&token=29f5bf9e4134492d5cd8d937e4870c9e&parkingType=1&operate=0&create_time=1457693090&gps=119.91753%2C93.1778040&chanid=1008";
		validateSign(pString);
	}
	
	/**
	 * �����ϴ�����
	 * @param paramMap
	 * @param tableName ���ݱ���
	 * @param type �������� 0ע�ᣬ1���� ��2ɾ��
	 * @return map {sql=string,params=object[],errmesg=string}
	 */
	public static Map<String, Object> getData(Map<String, String> paramMap,String tableName,int type ) {
		List<TableField> companyFields = getFields(tableName);
		String keyName ="uuid";
		if(tableName.equals("com_info_tb"))
			keyName = "park_uuid";
		if(tableName.equals("com_park_tb")){
			keyName = "cid";
			paramMap.put("create_time", System.currentTimeMillis()/1000+"");
		}
		String keyValue =null;
		Map<String, Object> preUpdateMap = new HashMap<String, Object>();
		String errmesg = "";
		String sql = "";
		List<Object> values = new ArrayList<Object>();
		if(type==0){//ע��
			sql = "insert into "+tableName+" (";
		}else if(type==2){//����
			sql = "update "+ tableName+ " set ";
		}else if(type==1){//ɾ��
			sql ="delete from "+tableName+" where ";
		}
		//����ÿ���ֶ�ֵ���жϺϷ���
		for(TableField field: companyFields){
			String fieldName  = field.getName();
			String fieldvalue = paramMap.get(fieldName);
			int fieldType = field.getFieldType();
			int length = field.getFieldLength();
			boolean isInsetNull = field.isInsertNull();
			boolean isUpdateNull= field.isUpdateNull();
			Object value = getValues(fieldType, fieldvalue, length);
			if(fieldName.equals(keyName)&&value!=null)
				keyValue = value.toString();
			if(fieldType==12){//��֤�ַ�������
				if(value!=null){
					if(value.toString().getBytes().length>length){
						errmesg += fieldName+"="+value+",���Ȳ��Ϸ�,����Ϊ"+length+",ʵ��Ϊ"+value.toString().getBytes().length+";";
					}
				}
			}
			if(type==0){//ע��
				if(isInsetNull){//����Ϊ��
					if(value==null){
						continue;
					}else {
						sql +=fieldName+",";
						values.add(value);
					}
				}else {//������Ϊ��
					if(value==null){
						errmesg +=fieldName+"Ϊ�ջ򲻺Ϸ�;";
					}else {
						sql +=fieldName+",";
						values.add(value);
					}
				}
			}else if(type==1){//����
				if(isUpdateNull){
					if(value==null){
						continue;
					}else{
						sql +=fieldName+"=?,";
						values.add(value);
					}
				}else{//������Ϊ��
					if(value==null){
						errmesg +=fieldName+"Ϊ�ջ򲻺Ϸ�;";
					}else {
						sql +=fieldName+"=?,";
						values.add(value);
					}
				}
			}
		}
		
		if(!values.isEmpty()){
			if(sql.endsWith(",")){
				sql = sql.substring(0,sql.length()-1);
			}
			if(type==0){
				sql +=") values(";
				for(int i=0;i<values.size();i++){
					sql +="?,";
				}
				if(sql.endsWith(","))
					sql =sql.substring(0,sql.length()-1);
				sql +=")";
			}else if(type==1){
				sql +=" where "+keyName+"=? ";
				values.add(keyValue);
			}
		}else {//�ֶζ�Ϊ��
			if(type==2){//ɾ��
				if(keyValue!=null){
					sql +="  "+keyName+"=? ";
					values.add(keyValue);
				}else {
					errmesg +="ɾ��ʧ��,uuid����Ϊ��";
				}
			}else {//ע������ʱ��û�п�������
				errmesg +="ע������ʧ��,"+keyName+"û�п��õ�����";
			}
		}
		
		preUpdateMap.put("errmesg",errmesg);
		if(errmesg.equals("")&&!values.isEmpty()){//������������
			preUpdateMap.put("sql", sql);
			Object[] params = new Object[values.size()];
			int index =0;
			for(Object v : values){
				params[index]=v;
				index++;
			}
			preUpdateMap.put("params", params);
		}
		return preUpdateMap;
	}
	
	/**
	 * ȡ�ϴ�����ֵ
	 * @param fieldType �ֶ����� 2ö�� 3���� 4���� 5������ 12�ַ���
	 * @param value 
	 * @param length �������ĳ���
	 * @return
	 */
	private static Object getValues(int fieldType,String value,int length){
		if(value==null)
			return null;
		if(fieldType==2){//ö��
			if(Check.isNumber(value)){
				Integer num = Integer.valueOf(value);
				return num;
			}
		}else if(fieldType==3){//����
			if(Check.isDouble(value)&&length>0){
				return formatDouble(value, length);
			}
		}else if(fieldType==4){//����
			if(Check.isNumber(value)){
				return Integer.valueOf(value);
			}
		}else if(fieldType==5){//������
			if(Check.isLong(value)){
				return Long.valueOf(value);
			}
		}else if(fieldType==12){
			value = AjaxUtil.decodeUTF8(value);
			//���������ַ�
			value = value.replaceAll("\"", "").replaceAll("'", "").replaceAll(",", "��").replace("[", "��").replace("]", "��")
					.replace("{", "��").replace("}", "��").replaceAll(";", "��");
			return value;
		}
		return null;
	}
	/**
	 * ��ʽ��������
	 * @param value
	 * @param length �������ľ���
	 * @return
	 */
	public static Double formatDouble(Object value,int length){
		String len = "#.";
		for(int i=0;i<length;i++){
			len +="0";
		}
		if(Check.isDouble(value+"")){
			DecimalFormat df=new DecimalFormat(len); 
			String dv = df.format(Double.valueOf(value+""));
			if(Check.isDouble(dv))
				return Double.valueOf(dv);
		}
		return 0.0d;
	}
	/**
	 * ���ݱ���ȡ���ֶ�����
	 * @param tablaName
	 * @return
	 */
	private static List<TableField> getFields(String tablaName){
		if(tablaName.equals("company_tb")){
			return companyFliedType();
		}else if(tablaName.equals("com_info_tb")){
			return comInfoTbFliedType();
		}else if(tablaName.equals("com_berthsecs_tb")){
			return comBerthsesTbFliedType();
		}else if(tablaName.equals("park_daypay_tb")){
			return parkDaypayTbFieldType();
		}else if(tablaName.equals("park_dayuse_tb")){
			return parkDayuseTbFieldType();
		}else if(tablaName.equals("com_hd_tb")){
			return comHdTbFieldType();
		}else if(tablaName.equals("dici_tb")){
			return diciTbFieldType();
		}else if(tablaName.equals("com_camera_tb")){
			return comCameraTbFieldType();
		}else if(tablaName.equals("com_led_tb")){
			return comLedTbFieldType();
		}else if(tablaName.equals("com_brake_tb")){
			return comBrakeTbFieldType();
		}else if(tablaName.equals("com_park_tb")){
			return comParkTbFliedType();
		}else if(tablaName.equals("user_info_tb")){
			return userInfoTbFliedType();
		}else if(tablaName.equals("com_etc_tb")){
			return comEtcTbFieldType();
		}else if(tablaName.equals("order_tb")){
			return orderTbFieldType();
		}else if(tablaName.equals("com_parkuser_check")){
			return comParkuserCheckFieldType();
		}else if(tablaName.equals("work_group_tb")){
			return workGroupTbFieldType();
		}else if(tablaName.equals("zld_black_tb")){
			return comBlackTbFieldType();
		}else if(tablaName.equals("com_parkstatus_tb")){
			return comParkStatusTbFieldType();
		}else if(tablaName.equals("sites_tb")){
			return sitesTbFieldType();
		}
		return null;
	}
	
	private static List<TableField> comParkStatusTbFieldType() {
		List<TableField> tableFields = new ArrayList<TableField>();
		tableFields.add(new TableField("comid", 5,0, false, true));
		tableFields.add(new TableField("total", 4,0, false, true));
		tableFields.add(new TableField("empty", 4,0, false, true));
		tableFields.add(new TableField("sheduled", 4,0, true, true));
		tableFields.add(new TableField("rscheduled", 4,0, true, true));
		tableFields.add(new TableField("rinternal", 4,0, true, true));
		tableFields.add(new TableField("internal", 4,0, true, true));
		tableFields.add(new TableField("ctime", 5,0, false, true));
		tableFields.add(new TableField("chanid", 5,0, false, true));
		tableFields.add(new TableField("cityid", 5,0, false, true));
		return tableFields;
	}

	//�������ֶ�
	private static List<TableField> comBlackTbFieldType(){
		List<TableField> tableFields = new ArrayList<TableField>();
		tableFields.add(new TableField("uuid", 12, 50, false, false));
		tableFields.add(new TableField("ctime", 5,0, false, true));
		tableFields.add(new TableField("car_number", 12, 50, false, false));
		tableFields.add(new TableField("remark", 12, 50, true, false));
		tableFields.add(new TableField("car_type", 2,0, true, true));//�������нӿ��ж���
		tableFields.add(new TableField("chanid", 5, 0, false, true));
		
		return tableFields;
	}
	
	//�շ�Ա���¸��ֶ�
	private static List<TableField> comParkuserCheckFieldType(){
		List<TableField> tableFields = new ArrayList<TableField>();
		tableFields.add(new TableField("uuid", 12, 50, false, false));
		tableFields.add(new TableField("uid", 5,0, false, true));
		tableFields.add(new TableField("comid", 5, 0, false, true));
		tableFields.add(new TableField("company_id", 5, 0, false, true));
		tableFields.add(new TableField("check_in_time", 5, 0, true, true));
		tableFields.add(new TableField("check_out_time", 5, 0, true, true));
		tableFields.add(new TableField("device_code", 2,0, true, true));
		tableFields.add(new TableField("berthsec_id", 5, 0, true, true));
		tableFields.add(new TableField("chanid", 5, 0, false, true));

		return tableFields;
	}
	//��������ֶ�
	private static List<TableField> workGroupTbFieldType(){
		List<TableField> tableFields = new ArrayList<TableField>();
		tableFields.add(new TableField("uuid", 12, 50, false, false));
		tableFields.add(new TableField("workgroup_name", 12, 10, false, true));
		tableFields.add(new TableField("company_id", 5, 0, false, true));
		tableFields.add(new TableField("create_time", 5,0, false, true));
		tableFields.add(new TableField("update_time", 5,0, false, true));
		tableFields.add(new TableField("is_active", 2,0, true, true));
		tableFields.add(new TableField("chanid", 5,0, false, true));
		
		return tableFields;
	}
	//�����ֶ�
	private static List<TableField> orderTbFieldType(){
		List<TableField> tableFields = new ArrayList<TableField>();
		//���ӻ��޸�ʱ���ֶ�
		tableFields.add(new TableField("id", 5, 0, false, true));
		tableFields.add(new TableField("order_uuid", 12, 50, false, true));
		tableFields.add(new TableField("create_time", 5, 0, false, true));
		tableFields.add(new TableField("car_number", 12, 50, false, true));
		tableFields.add(new TableField("comid", 5, 0, false, true));
		//tableFields.add(new TableField("car_type", 2,0, false, true));
		//tableFields.add(new TableField("uid", 5,0, false, true));
		//tableFields.add(new TableField("out_uid", 5,0, false, true));
		//tableFields.add(new TableField("prepaid", 3, 2, false, true));
		//tableFields.add(new TableField("in_equipment", 12, 50, false, true));
		//tableFields.add(new TableField("car_intime", 5, 0, false, true));
		tableFields.add(new TableField("pay_type", 2,0, false, true));
		//tableFields.add(new TableField("prepaid_pay_time", 5, 0, false, true));
		//tableFields.add(new TableField("berthnumber", 12, 50, false, true));
		tableFields.add(new TableField("chanid", 5,0, false, true));
		//����ʱҪ�ӵ��ֶ�
		tableFields.add(new TableField("end_time", 5, 0, true, true));
		//tableFields.add(new TableField("out_paytime", 2,0, false, false));
		//tableFields.add(new TableField("out_equipmen", 5,0, true, false));
		tableFields.add(new TableField("total", 3, 2, true, true));
		tableFields.add(new TableField("state", 2, 0, false, true));
		tableFields.add(new TableField("c_type", 2, 0, false, true));
		//tableFields.add(new TableField("pay_total", 3, 2, true, false));
		tableFields.add(new TableField("uin", 5, 0, false, true));
		return tableFields;
	}
	//���ӱ�ǩ�ֶ�
	private static List<TableField> comEtcTbFieldType(){
		List<TableField> tableFields = new ArrayList<TableField>();
		tableFields.add(new TableField("uuid", 12, 50, false, false));
		tableFields.add(new TableField("car_number", 12, 10, false, true));
		tableFields.add(new TableField("card_id", 12, 50, false, true));
		tableFields.add(new TableField("name", 12, 50, true, true));
		tableFields.add(new TableField("mobile", 12,15, true, true));
		tableFields.add(new TableField("balance", 3, 2, true, true));
		return tableFields;
	}
	//�շ�Ա�ֶ�
	private static List<TableField> userInfoTbFliedType(){
		List<TableField> tableFields = new ArrayList<TableField>();
		tableFields.add(new TableField("uuid", 12, 50, false, false));
		tableFields.add(new TableField("chanid", 4, 0, false, true));
		tableFields.add(new TableField("company_id", 4,0 , false, true));
		tableFields.add(new TableField("reg_time", 5, 0, false, true));
		tableFields.add(new TableField("mobile", 12, 15, false, true));
		tableFields.add(new TableField("sex", 2,0, true, true));
		tableFields.add(new TableField("nickname", 12, 50, true, true));
		tableFields.add(new TableField("email", 12, 50, true, true));
		tableFields.add(new TableField("md5pass", 12, 50, true, false));
		tableFields.add(new TableField("strid", 12, 50, false, false));
		return tableFields;
	}
	//��˾���ֶ�
	private static List<TableField> companyFliedType(){
		List<TableField> tableFields = new ArrayList<TableField>();
		tableFields.add(new TableField("uuid", 12, 50, false, false));
		tableFields.add(new TableField("company_name", 12, 100, false, true));
		tableFields.add(new TableField("corporation", 12, 20, false, true));
		tableFields.add(new TableField("phone", 12, 50, false, true));
		tableFields.add(new TableField("address", 12, 100, false, true));
		tableFields.add(new TableField("chanid", 4, 0, false, true));
		tableFields.add(new TableField("city_merchants_id", 5, 0, false, true));
		tableFields.add(new TableField("update_time", 5, 0, true, false));
		tableFields.add(new TableField("lon", 3, 6, true, true));
		tableFields.add(new TableField("create_time", 5, 0, false, true));
		return tableFields;
	}
	//ͣ�������ֶ�
	private static List<TableField> comInfoTbFliedType(){
		List<TableField> tableFields = new ArrayList<TableField>();
		tableFields.add(new TableField("park_uuid", 12, 50, false, false));
		tableFields.add(new TableField("company_name", 12, 100, false, true));
		tableFields.add(new TableField("phone", 12, 50, true, true));
		tableFields.add(new TableField("fax", 12, 50, true, true));
		tableFields.add(new TableField("address", 12, 100, true, true));
		tableFields.add(new TableField("zipcode", 12, 10, true, true));
		tableFields.add(new TableField("homepage", 12, 50, true, true));
		tableFields.add(new TableField("remarks", 12, 1000, true, true));
		tableFields.add(new TableField("create_time", 5, 0, false, true));
		tableFields.add(new TableField("longitude", 3, 6, false, true));
		tableFields.add(new TableField("latitude", 3, 6, false, true));
		tableFields.add(new TableField("parking_type", 4, 0, false, true));
		tableFields.add(new TableField("parking_total", 4, 0, false, true));
		tableFields.add(new TableField("share_number", 4, 0, true, true));
		tableFields.add(new TableField("mobile", 12, 15, true, true));
		tableFields.add(new TableField("mcompany", 12, 100, true, true));
		tableFields.add(new TableField("type", 2, 0, true, true));
		tableFields.add(new TableField("stop_type", 2, 0, true, true));
		tableFields.add(new TableField("state", 2, 0, true, true));
		tableFields.add(new TableField("city", 4, 0, true, true));
		tableFields.add(new TableField("record_number", 12, 100, true, true));
		tableFields.add(new TableField("company_uuid", 12, 50, true, true));
		tableFields.add(new TableField("chanid", 4, 0, false, true));
		//tableFields.add(new TableField("city_merchants_id", 5, 0, false, true));
		tableFields.add(new TableField("update_time", 5, 0, true, false));
		return tableFields;
	}
	//��λ�α�
	private static List<TableField> comBerthsesTbFliedType(){
		List<TableField> tableFields = new ArrayList<TableField>();
		tableFields.add(new TableField("uuid", 12, 50, false, false));
		tableFields.add(new TableField("berthsec_name", 12, 100, false, true));
		tableFields.add(new TableField("park_uuid", 12, 20, false, true));
		tableFields.add(new TableField("create_time", 5, 0, false, true));
		tableFields.add(new TableField("is_active", 12, 50, false, true));
		tableFields.add(new TableField("address", 12, 100, false, true));
		tableFields.add(new TableField("longitude", 3, 6, false, true));
		tableFields.add(new TableField("latitude", 3, 6, false, true));
		return tableFields;
	}
	//��λ��
	private static List<TableField> comParkTbFliedType(){
		List<TableField> tableFields = new ArrayList<TableField>();
		tableFields.add(new TableField("comid", 5, 0, false, true));
		tableFields.add(new TableField("cid", 12, 50, true, true));
		tableFields.add(new TableField("create_time", 5, 0, false, true));
		return tableFields;
	}
	//ͣ����ÿ���շ�
	private static List<TableField> parkDaypayTbFieldType(){
		List<TableField> tableFields = new ArrayList<TableField>();
		tableFields.add(new TableField("comid", 5, 0, false, true));
		tableFields.add(new TableField("internalIC".toLowerCase(), 5, 0, false, true));
		tableFields.add(new TableField("internalLabel".toLowerCase(), 5, 0, false, true));
		tableFields.add(new TableField("cashPay".toLowerCase(), 5, 0, false, true));
		tableFields.add(new TableField("labelPay".toLowerCase(), 5, 0, false, true));
		tableFields.add(new TableField("cardPay".toLowerCase(),5, 0, false, true));
		tableFields.add(new TableField("onlinePay".toLowerCase(),5, 0, false, true));
		tableFields.add(new TableField("unpaid".toLowerCase(), 5, 0, false, true));
		tableFields.add(new TableField("hbTime".toLowerCase(), 5, 0, false, true));
		tableFields.add(new TableField("cityElecAcctPay".toLowerCase(), 5, 0, false, true));
		tableFields.add(new TableField("escArrsAmt".toLowerCase(), 5, 0, false, true));
		tableFields.add(new TableField("parkingFee".toLowerCase(),12, 30, false, true));
		tableFields.add(new TableField("chanid", 4, 0, false, true));
		return tableFields;
	}
	//ͣ����ÿ��ͣ����
	private static List<TableField> parkDayuseTbFieldType(){
		List<TableField> tableFields = new ArrayList<TableField>();
		tableFields.add(new TableField("comid", 5, 0, false, true));
		tableFields.add(new TableField("count".toLowerCase(), 5, 0, false, true));
		tableFields.add(new TableField("hbTime".toLowerCase(), 5, 0, false, true));
		tableFields.add(new TableField("chanid", 4, 0, false, true));
		return tableFields;
	}
	
	//�豸
	private static List<TableField> comHdTbFieldType(){
		List<TableField> tableFields = new ArrayList<TableField>();
		tableFields.add(new TableField("comid", 5, 0, false, true));
		tableFields.add(new TableField("no", 12, 50, false, false));
		tableFields.add(new TableField("type", 2, 0, false, true));
		tableFields.add(new TableField("operate_time", 5, 0, false, true));
		tableFields.add(new TableField("state", 4, 0, false, true));
		return tableFields;
	}
	//�ش�
	private static List<TableField> diciTbFieldType(){
		List<TableField> tableFields = new ArrayList<TableField>();
		tableFields.add(new TableField("comid", 5, 0, false, true));
		tableFields.add(new TableField("operate_time", 5, 0, false, true));
		tableFields.add(new TableField("state", 4, 0, false, true));
		return tableFields;
	}
	
	//����ͷ
	private static List<TableField> comCameraTbFieldType(){
		List<TableField> tableFields = new ArrayList<TableField>();
		tableFields.add(new TableField("passid", 5, 0, false, true));
		tableFields.add(new TableField("upload_time", 5, 0, false, true));
		tableFields.add(new TableField("state", 4, 0, false, true));
		tableFields.add(new TableField("comid", 5, 0, false, true));
		return tableFields;
	}
	
	//�յ���
	private static List<TableField> comLedTbFieldType(){
		List<TableField> tableFields = new ArrayList<TableField>();
		tableFields.add(new TableField("passid", 5, 0, false, true));
		tableFields.add(new TableField("upload_time", 5, 0, false, true));
		tableFields.add(new TableField("state", 4, 0, false, true));
		tableFields.add(new TableField("comid", 5, 0, false, true));
		return tableFields;
	}
	
	//��բ
	private static List<TableField> comBrakeTbFieldType(){
		List<TableField> tableFields = new ArrayList<TableField>();
		tableFields.add(new TableField("passid", 5, 0, false, true));
		tableFields.add(new TableField("upload_time", 5, 0, false, true));
		tableFields.add(new TableField("state", 4, 0, false, true));
		tableFields.add(new TableField("comid", 5, 0, false, true));
		return tableFields;
	}
	//��բ
	private static List<TableField> sitesTbFieldType(){
		List<TableField> tableFields = new ArrayList<TableField>();
		tableFields.add(new TableField("uuid", 5, 0, false, true));
		tableFields.add(new TableField("comid", 5, 0, false, true));
		tableFields.add(new TableField("cityid", 5, 0, false, true));
		tableFields.add(new TableField("state", 4, 0, false, true));
		return tableFields;
	}
	//�ֶ�����ת��
	private static String getFieldName(String paramName){
		Map<String, String> pfMap = new HashMap<String, String>();
		pfMap.put("parkingNo", "park_uuid");
		pfMap.put("parkingName", "company_name");
		pfMap.put("parkingType", "parking_type");
		pfMap.put("berth", "cid");
		pfMap.put("plot_id", "cp_id");
		pfMap.put("operTime", "operate_time");
		pfMap.put("arriveTime", "create_time");
		pfMap.put("license", "car_number");
		pfMap.put("hbTime", "ctime");
		pfMap.put("city_merchants_id", "cityid");
		pfMap.put("status", "state");
		pfMap.put("out_paytime", "end_time");
		if(pfMap.get(paramName)!=null)
			return pfMap.get(paramName);
		return paramName;
	}
	
}