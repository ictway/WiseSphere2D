package com.ictway.wisesphere.services.custom.service;

import static org.deegree.services.controller.OGCFrontController.getServiceWorkspace;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.naming.NamingException;
import javax.xml.bind.JAXBException;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.xml.jaxb.JAXBUtils;
import org.deegree.feature.persistence.simplesql.jaxb.SimpleSQLFeatureStoreConfig;
import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ictway.wisesphere.feature.persistence.simplesql.SimpleSQLFeatureStoreProvider;

public class DeleteOracleGeomService implements GeometryDBService {
	
	private static final long serialVersionUID = -901785500425786189L;
	
    private static final String CONFIG_NS = "http://www.deegree.org/datasource/feature/simplesql";

    private static final String CONFIG_JAXB_PACKAGE = "org.deegree.feature.persistence.simplesql.jaxb";

    private static final URL CONFIG_SCHEMA = SimpleSQLFeatureStoreProvider.class.getResource( "/META-INF/schemas/datasource/feature/simplesql/3.0.1/simplesql.xsd" );


	private ArrayList<String> values; // properties value들의 리스트
	private Connection conn;
	
	private static final Logger LOG = getLogger(DeleteOracleGeomService.class);
	
	public JsonObject process(String layerId,String json,String geomName) {
		JsonObject result = new JsonObject();
		try {
			//connection pool 초기화
			initFeatureConnection(layerId);
			String sql = getSql(layerId,json,geomName);
			//update 실행
			int deletedRow = updateFeature(sql);
			closeConnection();
			result.addProperty("message", "success");
			result.addProperty("tableName", layerId);
			result.addProperty("deletedRow", deletedRow);
		} catch (Exception  e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result.addProperty("message", "fail");
			result.addProperty("tableName", layerId);
			result.addProperty("error",e.toString() );
		}
		return result;
	}

	@Override
	public void initFeatureConnection(String featureId)
			throws NamingException, SQLException, MalformedURLException, JAXBException {
		// TODO Auto-generated method stub
		/*
		LOG.warn("---@@ jeus Connection Pool -------------------");
		InitialContext initCtx = new InitialContext();
		LOG.info("GeometryServlet : ConnectionPool connecting");
		String cpName = newId.substring(0, newId.indexOf("."));
		DataSource ds = (DataSource) initCtx.lookup(cpName);
		this.conn = ds.getConnection();
		LOG.info("GeometryServlet : ConnectionPool connected");
		*/
			
		DeegreeWorkspace workspace = getServiceWorkspace();
		
		File featureFile = new File(workspace.getLocation(), "/datasources/feature/" + featureId + ".xml");
		
		SimpleSQLFeatureStoreConfig config;
        config = (SimpleSQLFeatureStoreConfig) JAXBUtils.unmarshall( 
        		CONFIG_JAXB_PACKAGE, CONFIG_SCHEMA, featureFile.toURI().toURL(),workspace );	
        
        String connId = config.getConnectionPoolId();
        if ( connId == null ) {
            connId = config.getJDBCConnId();
        }		
		ConnectionManager mgr = workspace.getSubsystemManager( ConnectionManager.class );		
        this.conn = mgr.get(connId);

	}

	@Override
	public void closeConnection() throws SQLException {
		// TODO Auto-generated method stub
		this.conn.close();
	}
	
	private String getSql(String layerId,String json,String geomName) throws Exception {
		// # 1. 변수 초기화
		this.values = new ArrayList<String>();
		Gson gson = new Gson();
		JsonParser parser = new JsonParser();
		// # 2. String 인자를 Json으로 치환
		JsonElement temp = parser.parse(json);
		// # 3. geomName이 null이거나 비어 있으면 default로 geometry
		if(geomName == null || (geomName.isEmpty() || geomName.equals("null"))) {
			geomName="geometry";
		}
		JsonObject geometry = temp.getAsJsonObject().get(geomName).getAsJsonObject();
		String sql = "DELETE FROM "+layerId+" WHERE SDO_UTIL.TO_JSON_VARCHAR("+geomName
				+")=(SELECT SDO_UTIL.TO_JSON_VARCHAR(SDO_UTIL.FROM_GEOJSON(?)) FROM DUAL)";
		values.add(geometry.toString());
		return sql;
	}

	@Override
	public int updateFeature(String sql) throws Exception {
		// TODO Auto-generated method stub
		// #2. Connection Pool에서 connection 가져오기
		// #3. prepareStatement 초기화
		PreparedStatement pstmt = this.conn.prepareStatement(sql);
		pstmt.setString(1, this.values.get(0));
		// #5. update SQL 실행 결과값 가져오기
		return pstmt.executeUpdate(); //updateRow : 수정된 로우의 개수
		// #6. conection 종료하기
		// #7. 예외처리하기
	}

	@Override
	public ArrayList<String> getColumnList(String[] cList) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<String> getColumnListFromDB(String layerId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JsonObject process() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSql() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
