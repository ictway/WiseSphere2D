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
import com.ictway.wisesphere.services.custom.service.DeleteOracleGeomService;
import com.ictway.wisesphere.services.custom.service.DeleteOracleService;
import com.ictway.wisesphere.services.custom.service.GeometryDBService;
import com.ictway.wisesphere.services.custom.service.InsertOracleService;
import com.ictway.wisesphere.services.custom.service.UpdateOracleService;

public class GeometryServlet extends HttpServlet {

	private static final long serialVersionUID = -901785500425786189L;

	private static final Logger LOG = getLogger(GeometryServlet.class);
	
	//Post method
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//a1. ArrayList<String> vs JsonArray 선언 및 컬럼 리스트 받기
		res.setContentType("application/json;charset=UTF-8");
		res.setCharacterEncoding("UTF-8");
		PrintWriter out = res.getWriter();
		JsonObject result = discriminationAction(req);
		
		if (result.get("message").getAsString().equals("fail")) {
			//실패시
			res.setStatus(406);
		}
		out.print(result);
		out.close();
	}
	
	private JsonObject discriminationAction(HttpServletRequest req) {
		JsonObject jsonObject;
		String action = req.getParameter("action");
		String layerId = req.getParameter("layerId"); //테이블 명
		String wClause = req.getParameter("wClause"); //where절
		String json = req.getParameter("json"); //json 데이터
		String srid =req.getParameter("srid");
		String seqName =req.getParameter("seqName");
		String seqColName =req.getParameter("seqColName");
		String geomName = req.getParameter("geomName"); //geom 이름
		String tableName = req.getParameter("tableName"); //geom 이름
		String[] columnList=req.getParameterValues("columnList[]");
		
		switch(action) {
		case "update":
			UpdateOracleService updateOracleService = new UpdateOracleService();
			jsonObject = updateOracleService.process(layerId, tableName,wClause, json, srid, geomName, columnList);
			break;
		case "insert":
			InsertOracleService insertOracleService = new InsertOracleService();
			jsonObject = insertOracleService.process(layerId, tableName, json, srid, geomName, columnList, seqName,seqColName);
			break;
		case "delete":
			DeleteOracleService deleteOracleService = new DeleteOracleService();
			jsonObject = deleteOracleService.process(layerId,tableName, wClause);
			break;
		case "deleteGeom":
			DeleteOracleGeomService deleteOracleGeomService = new DeleteOracleGeomService();
			jsonObject = deleteOracleGeomService.process(layerId, json, geomName);
			break;
		default:
			jsonObject = new JsonObject();
			jsonObject.addProperty("message", "fail");
			jsonObject.addProperty("tableName", layerId);
			jsonObject.addProperty("error", "Action을 다시 입력하시오.");
			break;
		}
		return jsonObject;
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
