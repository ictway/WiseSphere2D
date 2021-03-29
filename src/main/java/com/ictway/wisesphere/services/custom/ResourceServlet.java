/**
 * 
 */
package com.ictway.wisesphere.services.custom;

import static org.deegree.services.controller.OGCFrontController.getServiceWorkspace;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.layer.persistence.LayerStoreManager;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.metadata.OWSMetadataProviderManager;
import org.deegree.style.persistence.StyleStoreManager;
import org.deegree.theme.persistence.ThemeManager;
import org.slf4j.Logger;

public class ResourceServlet extends HttpServlet {

	private static final long serialVersionUID = -901785500425786189L;

	private static final Logger LOG = getLogger(ResourceServlet.class);

	private synchronized void updateResource(String layerId)
			throws ResourceInitException, IOException, URISyntaxException {

		DeegreeWorkspace dworkspace = getServiceWorkspace();
		OWSMetadataProviderManager mgr2 = dworkspace.getSubsystemManager(OWSMetadataProviderManager.class);
		String configRoot = mgr2.getFiles().get(0).getParentFile().getParentFile().getPath();

		OGCFrontController.getServiceConfiguration().shutdown();

		File configFile = null;

		configFile = new File(configRoot + File.separator + "styles" + File.separator + layerId + ".xml");
		StyleStoreManager ssm = dworkspace.getSubsystemManager(StyleStoreManager.class);
		ssm.manualActivate(layerId, configFile);

		configFile = new File(configRoot + File.separator + "layers" + File.separator + layerId + ".xml");
		LayerStoreManager lsm = dworkspace.getSubsystemManager(LayerStoreManager.class);
		lsm.manualActivate(layerId, configFile);

		ThemeManager tm = dworkspace.getSubsystemManager(ThemeManager.class);
		tm.shutdown();
		tm.startup(dworkspace);

		OGCFrontController.getServiceConfiguration().startup(dworkspace);

	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		// TODO Auto-generated method stub
		req.setCharacterEncoding(Charset.defaultCharset().displayName());
		
		res.setContentType("application/json; charset=UTF-8");
		PrintWriter writer = res.getWriter();
		
		String layerId = req.getParameter("layerId");
		String sld = req.getParameter("sld");
		String action = req.getParameter("action");
		
		if (action.equals("update")) {
			doUpdateStyle(writer, layerId, sld);			
		}
		else if (action.equals("select")) {
			doGetStyle(writer, layerId);
		} 
	}

	private void doGetStyle(PrintWriter writer, String layerId) {
		String output = "";
		try {
			boolean bIsEmpty = false;

			if ((layerId == null) || (layerId.isEmpty()))  {
				output = "{" + "\"message\" : \"'rname' parameter is null or empty.\"" + "}";
				bIsEmpty = true;
			}
			
			if (!bIsEmpty) {
				output = getStyle(layerId);
			}

			output = "{" + "\"sld\" : \'" + output + "\'" + "}";

		} catch (Exception e) {
			e.printStackTrace();
			output = "{" + "\"error\" : \"" + e.toString() + "\"" + "}";
		} finally {
			writer.write(output);
			writer.close();
		}
	}
	
	private void doUpdateStyle(PrintWriter writer, String layerId, String sld) {
		String output = "";

		try {
			boolean bIsEmpty = false;

			if ((layerId == null) || (layerId.isEmpty()))  {
				output = "{" + "\"message\" : \"'rname' parameter is null or empty.\"" + "}";
				bIsEmpty = true;
			}
			if ((sld == null) || (sld.isEmpty()))  {
				output = "{" + "\"message\" : \"'sld' parameter is null or empty.\"" + "}";
				bIsEmpty = true;
			}
			
			if (!bIsEmpty) {
				output = updateStyle(layerId, sld);
			}
		} catch (Exception e) {
			e.printStackTrace();
			output = "{" + "\"error\" : \"" + e.toString() + "\"" + "}";
		} finally {
			writer.write(output);
			writer.close();
		}
		
	}

	private synchronized String updateStyle(String layerId, String sld)
			throws ResourceInitException, IOException, URISyntaxException {
		String res = "";
		 res = "{" + "\"error\" : \"" +layerId + " is not exists.\"" + "}";
		DeegreeWorkspace dworkspace = getServiceWorkspace();
		OWSMetadataProviderManager mgr2 = dworkspace.getSubsystemManager(OWSMetadataProviderManager.class);
		String configRoot = mgr2.getFiles().get(0).getParentFile().getParentFile().getPath();

		OGCFrontController.getServiceConfiguration().shutdown();

		File configFile = null;

		configFile = new File(configRoot + File.separator + "styles" + File.separator + layerId + ".xml");
		
		if (configFile.exists()) {
	        BufferedWriter bufwriter = new BufferedWriter(new FileWriter(configFile));
	        bufwriter.write(sld);
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
			res = "{" + "\"success\" : \"" +layerId + " updated.\"" + "}";
		}
		
		return res;
	}
	
	private synchronized String getStyle(String layerId)
			throws ResourceInitException, IOException, URISyntaxException {
		String res = "";

		DeegreeWorkspace dworkspace = getServiceWorkspace();
		OWSMetadataProviderManager mgr2 = dworkspace.getSubsystemManager(OWSMetadataProviderManager.class);
		String configRoot = mgr2.getFiles().get(0).getParentFile().getParentFile().getPath();

		File configFile = null;

		configFile = new File(configRoot + File.separator + "styles" + File.separator + layerId + ".xml");
		
		//BufferedReader bufreader = new BufferedReader(new FileReader(configFile));
		BufferedReader bufreader = new BufferedReader(new InputStreamReader(new FileInputStream(configFile),"UTF8"));

		String line = null;
	    String message = new String();
	    final StringBuffer buffer = new StringBuffer(2048);
	    while ((line = bufreader.readLine()) != null) {
	        // buffer.append(line);
	        res += line;
	    }

		return res;
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(req, res);
	}

	// converting Stream to String
	public String convertStreamToString(InputStream is) throws IOException {

		if (is != null) {
			java.io.Writer writer = new java.io.StringWriter();
			char[] buffer = new char[1024];
			try {
				java.io.Reader reader = new java.io.BufferedReader(new InputStreamReader(is, "UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		} else {
			return "";
		}

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

}
