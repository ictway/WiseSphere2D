package com.ictway.wisesphere.services.custom.service;

import static org.deegree.services.controller.OGCFrontController.getServiceWorkspace;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

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

public class InsertOracleService implements GeometryDBService {
	
	private static final long serialVersionUID = -901785500425786189L;
	
    private static final String CONFIG_NS = "http://www.deegree.org/datasource/feature/simplesql";

    private static final String CONFIG_JAXB_PACKAGE = "org.deegree.feature.persistence.simplesql.jaxb";

    private static final URL CONFIG_SCHEMA = SimpleSQLFeatureStoreProvider.class.getResource( "/META-INF/schemas/datasource/feature/simplesql/3.0.1/simplesql.xsd" );
	
	private ArrayList<String> values; // properties value들의 리스트
	private Connection conn;
	
	private static final Logger LOG = getLogger(InsertOracleService.class);

	public JsonObject process(String layerId,String tableName,String json,String srid,String geomName,String[] cList, String seqName, String seqColName) {
		JsonObject result = new JsonObject();
		try {
			//connection pool 초기화
			initFeatureConnection(layerId);
			//columnList 설정
			ArrayList<String> columnList = getColumnList(cList);
			if(!Optional.ofNullable(srid).isPresent()) {
				srid="4326";
			}
			String sql = getSql(tableName,json,geomName,columnList,seqName,seqColName,srid);
			//insert 실행
			int insertedRow = updateFeature(sql); //좌표계 입력이 없을 경우
			closeConnection();
			result.addProperty("message", "success");
			result.addProperty("tableName", tableName);
			result.addProperty("insertedRow", insertedRow);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result.addProperty("message", "fail");
			result.addProperty("tableName", tableName);
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
	
	private String getSql(String layerId,String json,String geomName,ArrayList<String> columnList,String seqName,String seqColName,String srid) throws Exception {
		// # 1. 변수 초기화
		String sql = "INSERT INTO " + layerId + "("; // 반환될 sql문이 저장되는 변수
		String valueStr="VALUES (";
		this.values = new ArrayList<String>();
		Gson gson = new Gson();
		JsonParser parser = new JsonParser();
		// # 2. String 인자를 Json으로 치환
		JsonElement temp = parser.parse(json);
		// # 3. geomName이 null이거나 비어 있으면 default로 geometry
		if(geomName == null || (geomName.isEmpty() || geomName.equals("null"))) {
			geomName="geometry";
		}
		// # 4. properties,geometry 추출
		JsonObject properties = temp.getAsJsonObject().get("properties").getAsJsonObject();
		JsonObject geometry = temp.getAsJsonObject().get("geometry").getAsJsonObject();
		// # 5. propeties를 Map으로 변환
		Map map = gson.fromJson(properties, Map.class);
		// map의 key부분을 이용하여 result 부분 완성 , values에 각각의 value를 추가
		ArrayList<String> incorrectColumns=new ArrayList<>(); //잘못된 컬럼을 저장
		//시퀸스를 사용할 때
		if(Optional.ofNullable(seqColName).isPresent() && Optional.ofNullable(seqName).isPresent()) {//sequence 존재 여부 확인 경우)
			sql += seqColName+","; // result에 sql문 일부 추가
			int val = selectSeqNextVal(seqName);
			if(val > 0) {
				valueStr += selectSeqNextVal(seqName) + ",";
			}else {
				LOG.error("error occured about sequence");
				return null;
			}
		}
		for (Object key : map.keySet()) {
			String k = (String) key;
			if(columnList.contains(k) && !k.equals(seqColName)){ //key 존재 여부 확인
				this.values.add(properties.get(k).getAsString()); // values에 각각의 value 추가
				sql += k+","; // result에 sql문 일부 추가
				valueStr += "?,";
			}else {
				incorrectColumns.add(k);
			}
		}
		// geom 추가
		sql += geomName+") ";
		valueStr += "SDO_CS.TRANSFORM(SDO_UTIL.FROM_GEOJSON(?)," + srid + "))";
		values.add(geometry.toString());
		return sql+valueStr;
	}	
	
	//2021-04-21
	//sequence 간접 사용
	private int selectSeqNextVal(String seqName) {
		try {
			String sql = "SELECT "+seqName+".NEXTVAL FROM DUAL";
			PreparedStatement sqlStatement = this.conn.prepareStatement(sql);
			ResultSet rs = sqlStatement.executeQuery();
			rs.next();
			return rs.getInt(1);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
	}

	//사용자가 입력한 데이터를 기반으로 컬럼리스트를 반환
	@Override
	public ArrayList<String> getColumnList(String[] cList) throws Exception {
		// TODO Auto-generated method stub
		//초기화
		ArrayList<String> columnList = new ArrayList<>(Arrays.asList(cList));
		return columnList;
	}

	//DB를 통해서 컬럼리스트를 반환
	@Override
	public ArrayList<String> getColumnListFromDB(String tableName) throws Exception {
		// TODO Auto-generated method stub
		//초기화
		ArrayList<String> arrayList = new ArrayList<>();
		//sql문 설정
		String sql = "SELECT * FROM COLS WHERE TABLE_NAME = '?'";
		//sql실행
		try {
			PreparedStatement pstmt = this.conn.prepareStatement(sql);
			pstmt.setString(1, tableName);
			//select 수행
			ResultSet resultSet=pstmt.executeQuery();
			//데이터 arrayList에 저장
			while(resultSet.next()) {
				arrayList.add(resultSet.getString("COLUMN_NAME"));
			}
			//에러 처리
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return arrayList;
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

	//좌표계가 설정되어 있지 않았을 경우 EPSG4326으로 설정
	@Override
	public int updateFeature(String sql) throws Exception {
		// TODO Auto-generated method stub
		// #1. prepareStatement 초기화
		PreparedStatement pstmt = this.conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		// #2. 파라미터 설정(setString)
		int i = 1;
		for (String value : this.values) {
			pstmt.setString(i++, value);
		}
		// #3. update SQL 실행 결과값 가져오기
		return pstmt.executeUpdate(); //updateRow : 수정된 로우의 개수
	}

}
