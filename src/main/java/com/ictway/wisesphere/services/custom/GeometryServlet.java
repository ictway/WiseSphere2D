package com.ictway.wisesphere.services.custom;

import static org.deegree.services.controller.OGCFrontController.getServiceWorkspace;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.xml.jaxb.JAXBUtils;
import org.deegree.feature.persistence.simplesql.jaxb.SimpleSQLFeatureStoreConfig;
import org.deegree.layer.persistence.LayerStoreManager;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.metadata.OWSMetadataProviderManager;
import org.deegree.style.persistence.StyleStoreManager;
import org.deegree.theme.persistence.ThemeManager;
import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ictway.wisesphere.feature.persistence.simplesql.SimpleSQLFeatureStoreProvider;

public class GeometryServlet extends HttpServlet {

	private static final long serialVersionUID = -901785500425786189L;
	
    private static final String CONFIG_NS = "http://www.deegree.org/datasource/feature/simplesql";

    private static final String CONFIG_JAXB_PACKAGE = "org.deegree.feature.persistence.simplesql.jaxb";

    private static final URL CONFIG_SCHEMA = SimpleSQLFeatureStoreProvider.class.getResource( "/META-INF/schemas/datasource/feature/simplesql/3.0.1/simplesql.xsd" );


	private ArrayList<String> values; // properties value들의 리스트
	private Connection conn;
	private JsonObject result;

	private static final Logger LOG = getLogger(GeometryServlet.class);
	
	//Post method
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String layerId = req.getParameter("layerId"); //테이블 명
		String wClause = req.getParameter("wClause"); //where절
		String json = req.getParameter("json"); //json 데이터
		String geomName = req.getParameter("geomName"); //geom 이름
		//a1. ArrayList<String> vs JsonArray 선언 및 컬럼 리스트 받기
		String[] columnList=req.getParameterValues("columnList[]");
		res.setContentType("application/json;charset=UTF-8");
		res.setCharacterEncoding("UTF-8");
		PrintWriter out = res.getWriter();
		updateDBService(layerId,wClause,json,geomName,columnList);
		if (this.result.get("message").getAsString().equals("fail")) {
			//실패시
			res.setStatus(406);
		}
		out.print(this.result);
		out.close();
	}
	
	//로직 부분 분리
	private void updateDBService(String layerId,String wClause,String json,String geomName,String[] cList) {
		//result 객체 초기화
		this.result=new JsonObject();
		try {
			//connection pool 초기화
			initFeatureConnection(layerId);
			LOG.debug("connected");
			//columnList 설정
			ArrayList<String> columnList = getColumnList(cList);
			LOG.debug("created columnList");
			//ArrayList<String> columnList = getColumnListFromDB(layerId);
			//sql문 생성
			String sql = getUpadteSQL(layerId,wClause,json,geomName,columnList);
			LOG.debug("created sql");
			//update 실행
			updateFeature(sql);
			LOG.debug("updated db");
			closeConnectionPool();
			LOG.debug("closed");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//컬럼들의 리스트 가져오기
	private ArrayList<String> getColumnList(String[] cList) {
		//초기화
		ArrayList<String> columnList = new ArrayList<>(Arrays.asList(cList));
		return columnList;
	}
	
	//컬럼들의 리스트 가져오기
	private ArrayList<String> getColumnListFromDB(String layerId) {
		//초기화
		ArrayList<String> arrayList = new ArrayList<>();
		//sql문 설정
		String sql = "SELECT * FROM COLS WHERE TABLE_NAME = '?'";
		//sql실행
		try {
			PreparedStatement pstmt = this.conn.prepareStatement(sql);
			pstmt.setString(1, layerId);
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
	
	// sql set 부분 만드는 메소드
	//
	private String getUpadteSQL(String layerId,String wClause,String json,String geomName,ArrayList<String> columnList) throws Exception {
		// # 1. 변수 초기화
		String sql = "update " + layerId + " set "; // 반환될 sql문이 저장되는 변수
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
		JsonObject geometry = temp.getAsJsonObject().get(geomName).getAsJsonObject();
		// # 5. propeties를 Map으로 변환
		Map map = gson.fromJson(properties, Map.class);
		// map의 key부분을 이용하여 result 부분 완성 , values에 각각의 value를 추가
		ArrayList<String> incorrectColumns=new ArrayList<>(); //잘못된 컬럼을 저장
		for (Object key : map.keySet()) {
			String k = (String) key;
			if(columnList.contains(k)){ //key 존재 여부 확인
				this.values.add(properties.get(k).getAsString()); // values에 각각의 value 추가
				sql += k + " = ?,"; // result에 sql문 일부 추가
			}else {
				incorrectColumns.add(k);
			}
		}
		
		// geom 추가
		sql += geomName + "= SDO_UTIL.FROM_GEOJSON(?) ";
		values.add(geometry.toString());
		sql+="where "+wClause;
		return sql;
	}

	// db row update메소드 DB에 직접접근 수정 필요,postgresql
	// @layerId : table명
	// @where : where clause
	// @json : String(json format)
	public void updateFeature(String updateSql){
		try {
			// #2. Connection Pool에서 connection 가져오기
			// #3. prepareStatement 초기화
			PreparedStatement pstmt = this.conn.prepareStatement(updateSql, Statement.RETURN_GENERATED_KEYS);
			// #4. 파라미터 설정(setString)
			int i = 1;
			for (String value : this.values) {
				pstmt.setString(i++, value);
			}
			// #5. update SQL 실행 결과값 가져오기
			int updatedRow = pstmt.executeUpdate(); //updateRow : 수정된 로우의 개수
			result.addProperty("message", "success");
			result.addProperty("updated", updatedRow);
			// #6. conection 종료하기
			// #7. 예외처리하기
		} catch (Exception e) {
			this.result.addProperty("message", "fail");
			this.result.addProperty("error", e.toString());
			e.printStackTrace();
		}
		// try {
		// updateSql = "UPDATE "+layerId+" set GEOMETRY="+
		// "ST_SetSRID(ST_GeomFromGeoJSON('"+convertStringToJson(json)+"'),4326)"
		// "SDO_UTIL.FROM_GEOJSON(?)"
		// +" WHERE "+where;
		// initConnectionPool("oraclePool.CP");
		// PreparedStatement pstmt = this.conn.prepareStatement(updateSql,
		// Statement.RETURN_GENERATED_KEYS);
		// LOG.info("updateSql : "+pstmt);
		// int updatedRow = pstmt.executeUpdate();
		// LOG.info("updated");
		// result.addProperty("message", "success");
		// result.addProperty("updated", updatedRow);
		// closeConnectionPool();
		// LOG.info("Connection Pool closed");
		// } catch (Exception e) {
		// TODO Auto-generated catch block
		// e.printStackTrace();
		// result.addProperty("message", "fail");
		/// result.addProperty("error", e.toString());
		// }
		// try {
		// Connection conn = DriverManager.getConnection(connectionUrl);
		// Class.forName("org.postgresql.Driver");
		// Connection
		// conn=DriverManager.getConnection("jdbc:postgresql://192.168.0.146:5432/gisdb","postgres","ictway001");

		// PreparedStatement prepsUpdateProduct = conn.prepareStatement(updateSql,
		// Statement.RETURN_GENERATED_KEYS);
		// LOG.info("updateSql : "+updateSql);
		// int r=prepsUpdateProduct.executeUpdate();
		// LOG.info("r="+r);
		// result.addProperty("message", "success");
		// } catch (SQLException | ClassNotFoundException e) {
		// TODO Auto-generated catch block
		// e.printStackTrace();
		// result.addProperty("message", "fail");
		// result.addProperty("error", e.toString());
		// }

	}
	
	//db connection pool 연결
	private void initFeatureConnection(String featureId) throws NamingException, SQLException, MalformedURLException, JAXBException {
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

	private void closeConnectionPool() throws SQLException {
		this.conn.close();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		// TODO Auto-generated method stub
		req.setCharacterEncoding(Charset.defaultCharset().displayName());

		res.setContentType("application/json; charset=UTF-8");
		PrintWriter writer = res.getWriter();

		String layerId = req.getParameter("layerId");
		String w_clause = req.getParameter("w_clause");
		String json = req.getParameter("json");
		String action = req.getParameter("action");

		if (action.equals("update")) {
			doUpdateStyle(writer, layerId, w_clause, json);
		} else if (action.equals("select")) {
			// doGetStyle(writer, layerId);
		}
	}

	private void doUpdateStyle(PrintWriter writer, String layerId, String w_clause, String json) {
		String output = "";

		try {
			boolean bIsEmpty = false;

			if ((layerId == null) || (layerId.isEmpty())) {
				output = "{" + "\"message\" : \"'layerId' parameter is null or empty.\"" + "}";
				bIsEmpty = true;
			}
			if ((w_clause == null) || (w_clause.isEmpty())) {
				output = "{" + "\"message\" : \"'w_clause' parameter is null or empty.\"" + "}";
				bIsEmpty = true;
			}
			if ((w_clause == null) || (w_clause.isEmpty())) {
				output = "{" + "\"message\" : \"'json' parameter is null or empty.\"" + "}";
				bIsEmpty = true;
			}
			if (!bIsEmpty) {
				output = updateStyle(layerId, w_clause, json);
			}
		} catch (Exception e) {
			e.printStackTrace();
			output = "{" + "\"error\" : \"" + e.toString() + "\"" + "}";
		} finally {
			writer.write(output);
			writer.close();
		}

	}

	private synchronized String updateStyle(String layerId, String w_clause, String json)
			throws ResourceInitException, IOException, URISyntaxException {
		String res = "";
		res = "{" + "\"error\" : \"" + layerId + " is not exists.\"" + "}";
		DeegreeWorkspace dworkspace = getServiceWorkspace();
		OWSMetadataProviderManager mgr2 = dworkspace.getSubsystemManager(OWSMetadataProviderManager.class);
		String configRoot = mgr2.getFiles().get(0).getParentFile().getParentFile().getPath();

		OGCFrontController.getServiceConfiguration().shutdown();

		File configFile = null;

		configFile = new File(configRoot + File.separator + "styles" + File.separator + layerId + ".xml");

		if (configFile.exists()) {
			BufferedWriter bufwriter = new BufferedWriter(new FileWriter(configFile));
			bufwriter.write(w_clause);
			bufwriter.close();

			StyleStoreManager ssm = dworkspace.getSubsystemManager(StyleStoreManager.class);
			ssm.manualActivate(layerId, configFile);

			configFile = new File(configRoot + File.separator + "layers" + File.separator + layerId + ".xml");
			LayerStoreManager lsm = dworkspace.getSubsystemManager(LayerStoreManager.class);
			lsm.manualActivate(layerId, configFile);

			ThemeManager tm = dworkspace.getSubsystemManager(ThemeManager.class);
			tm.shutdown();
			tm.startup(dworkspace);

			OGCFrontController.getServiceConfiguration().startup(dworkspace);
			res = "{" + "\"success\" : \"" + layerId + " updated.\"" + "}";
		}

		return res;
	}

	@Override
	public void init() throws ServletException {
		// TODO Auto-generated method stub
		// 프로퍼티 파일 위치
		try {
			System.out.println("log...");
		} catch (Exception e) {
			LOG.error("Custom Resource 초기화에 실패하였습니다. " + e.getMessage(), e);
		} finally {
		}
	}

	@Override
	public void destroy() {
		LOG.info("destroy() is Running...");
	}
}
