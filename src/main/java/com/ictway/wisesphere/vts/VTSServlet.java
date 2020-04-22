/**
 * 
 */
package com.ictway.wisesphere.vts;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import com.sun.security.ntlm.Client;

/**
 * @author ictway 2018. 3. 19. VTSServlet.java
 */
public class VTSServlet extends HttpServlet {
	private static final long serialVersionUID = 4549789857666973050L;
	
	private Client client;
	private Properties prop;

	private static final Logger LOG = getLogger(VTSServlet.class);

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String output = "";
		String layer = "";
		String z = "";
		String x = "";
		String y = "";
		
		String reqPath = req.getRequestURI();

		String[] temp = reqPath.split("/");
		
		for (int i=0; i<temp.length; i++) {
			if (temp[i].equals("vts") && ((i+3) <= temp.length)) {
				layer = temp[i+1];
				z = temp[i+2];
				x = temp[i+3];
				y = temp[i+4];
			}
		}
		
		req.setCharacterEncoding(Charset.defaultCharset().displayName());
		resp.setContentType("application/json; charset=UTF-8");
		PrintWriter writer = resp.getWriter();

		try {
			output = layer + "/" + z + "/" + x + "/" + y;
			
		} catch (Exception e) {
			e.printStackTrace();
			output = "{" + "\"error\" : \"" + e.toString() + "\"" + "}";

		} finally {
			writer.write(output);
			writer.close();
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// TODO Auto-generated method stub

		String output = "";
		resp.setContentType("application/json; charset=UTF-8");
		PrintWriter writer = resp.getWriter();

		try {

		} catch (Exception e) {
			e.printStackTrace();
			output = "{" + "\"error\" : \"" + e.toString() + "\"" + "}";

		} finally {
			writer.write(output);
			writer.close();
		}
		
	}
	// converting Stream to String
		public String convertStreamToString(InputStream is) throws IOException {

			if (is != null) {
				java.io.Writer writer = new java.io.StringWriter();
				char[] buffer = new char[1024];
				try {
					java.io.Reader reader = new java.io.BufferedReader(
							new InputStreamReader(is, "UTF-8"));
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
		InputStream is = null;
		prop = new Properties();
		URL url = VTSServlet.class.getResource("/META-INF/console/resourcemanager/geocoding");
		try {
		} catch (Exception e) {
			LOG.error("검색엔진 Elasticsearch 연결 초기화에 실패하였습니다. " + e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

}
