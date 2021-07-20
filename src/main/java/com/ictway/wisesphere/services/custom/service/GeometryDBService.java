package com.ictway.wisesphere.services.custom.service;

import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.naming.NamingException;
import javax.xml.bind.JAXBException;

import com.google.gson.JsonObject;

public interface GeometryDBService {
	
	//DB연결,sql 생성,sql 실행,연결 해제의 순세 제어
	public JsonObject process();
	//레이어명을 입력받아 DB와 연결
	public void initFeatureConnection(String featureId) throws NamingException, SQLException, MalformedURLException, JAXBException;
	//변경하는 컬럼 리스트 반환
	public ArrayList<String> getColumnList(String[] cList) throws Exception;
	//사용자가 컬럼을 보내지 않고 DB에서 연결을 수행
	public ArrayList<String> getColumnListFromDB(String layerId) throws Exception;
	//sql 생성 - 각각의 클래스에 맞게 오버로딩
	public String getSql() throws Exception;
	//EPSG 좌표계를 전송했을 경우 수행
	public int updateFeature(String sql) throws Exception;
	//DB와의 연결 해제
	public void closeConnection() throws SQLException;
	
}
