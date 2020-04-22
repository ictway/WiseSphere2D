/**
 * 
 */
package com.ictway.wisesphere.services.custom;

import static org.deegree.services.controller.OGCFrontController.getServiceWorkspace;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
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

/**
 * @author ksjang 2016. 11. 27. ResourceServlet.java
 */
public class ResourceServlet extends HttpServlet {

	private static final long serialVersionUID = -901785500425786189L;

	private static final Logger LOG = getLogger(ResourceServlet.class);

	private synchronized void updateResource(String rname)
			throws ResourceInitException, IOException, URISyntaxException {

		DeegreeWorkspace dworkspace = getServiceWorkspace();
		OWSMetadataProviderManager mgr2 = dworkspace.getSubsystemManager(OWSMetadataProviderManager.class);
		String configRoot = mgr2.getFiles().get(0).getParentFile().getParentFile().getPath();

		OGCFrontController.getServiceConfiguration().shutdown();

		File configFile = null;

		configFile = new File(configRoot + File.separator + "styles" + File.separator + rname + ".xml");
		StyleStoreManager ssm = dworkspace.getSubsystemManager(StyleStoreManager.class);
		ssm.manualActivate(rname, configFile);

		configFile = new File(configRoot + File.separator + "layers" + File.separator + rname + ".xml");
		LayerStoreManager lsm = dworkspace.getSubsystemManager(LayerStoreManager.class);
		lsm.manualActivate(rname, configFile);

		ThemeManager tm = dworkspace.getSubsystemManager(ThemeManager.class);
		tm.shutdown();
		tm.startup(dworkspace);

		OGCFrontController.getServiceConfiguration().startup(dworkspace);

	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		// TODO Auto-generated method stub
		req.setCharacterEncoding(Charset.defaultCharset().displayName());
		String output = "";
		res.setContentType("application/json; charset=UTF-8");
		PrintWriter writer = res.getWriter();

		try {
			
			boolean bIsEmpty = false;
			String rname = req.getParameter("rname");

			if ((rname == null) || (rname.isEmpty())) {
				output = "{" + "\"message\" : \"'rname' parameter is null or empty.\"" + "}";
				bIsEmpty = true;
			}
			
			if (!bIsEmpty) {
				updateResource(rname);
			}
			/*
			if (!bIsEmpty) {
				output = "{\"rtype\" : \"" + rname + "\"}";
				// DeegreeWorkspace workspace = getServiceWorkspace();
				// List<ResourceManager> list = workspace.getResourceManagers();

				OWSMetadataProviderManager mgr2 = getServiceWorkspace()
						.getSubsystemManager(OWSMetadataProviderManager.class);
				String configRoot = mgr2.getFiles().get(0).getParentFile().getParentFile().getPath();

				for (ResourceManager mgr : getServiceWorkspace().getResourceManagers()) {
					ResourceManagerMetadata2 md = ResourceManagerMetadata2.getMetadata(mgr);
					if (md != null) {

						String mdName = md.getName();
						String mdCategory = md.getCategory();
						System.out.println("name : " + mdName);
						System.out.println("category : " + mdCategory);
						if (mdName.equals("styles") || mdName.equals("layers")) {
							File configFile = new File(
									configRoot + File.separator + mdName + File.separator + rname + ".xml");
							md.getManager().manualActivate(rname, configFile);
						}
						// else if (mdName.equals("themes")) {
						// File configFile = new File(configRoot +
						// File.separator + mdName + File.separator +
						// "theme.xml");
						// md.getManager().manualActivate(rname, configFile);
						// }
						else if (mdName.equals("feature")) {
							File configFile = new File(configRoot + File.separator + "datasources" + File.separator
									+ mdName + File.separator + rname + ".xml");
							md.getManager().manualActivate(rname, configFile);
						}
						// else if (mdName.equals("services")) {
						// File configFile = new File(configRoot +
						// File.separator + "services" + File.separator +
						// "wms.xml");
						// md.getManager().manualActivate("wms", configFile);
						// }
					}
				}
				OGCFrontController.getInstance().reload();
			}
		 	*/
		} catch (Exception e) {
			e.printStackTrace();
			output = "{" + "\"error\" : \"" + e.toString() + "\"" + "}";
		} finally {
			writer.write(output);
			writer.close();
		}
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
