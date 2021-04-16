package com.ictway.wisesphere.services.wfs;

import static org.deegree.commons.xml.jaxb.JAXBUtils.unmarshall;
import static org.deegree.services.controller.OGCFrontController.getServiceWorkspace;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.layer.persistence.LayerStore;
import org.deegree.layer.persistence.LayerStoreManager;
import org.deegree.theme.persistence.standard.StandardThemeProvider;
import org.deegree.theme.persistence.standard.jaxb.Themes;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class SpWFSUpdateXml {
	
	private static final Logger LOG = getLogger(SpWFSUpdateXml.class);
	
	private static final URL SCHEMA_URL = StandardThemeProvider.class.getResource( "/META-INF/schemas/themes/3.2.0/themes.xsd" );
	
	public SpWFSUpdateXml() {
		
	}
	
	public void updateFeatureStore() throws ParserConfigurationException, SAXException, IOException, JAXBException, TransformerException {
		
		String pkg = "org.deegree.theme.persistence.standard.jaxb";
		DeegreeWorkspace workspace = getServiceWorkspace();
		URL configUrl = new File(workspace.getLocation(), "/themes/theme.xml").toURL(); //theme.xml url 얻기
		//configUrl = new File("C:\\Users\\22cun\\.wisesphere\\default-workspace\\themes\\theme").toURL();
		//wfs.xml 열기
		File wfsXml = new File(workspace.getLocation(), "/services/wfs.xml");	
		
		//xml 읽기
		Document doc = readXml(wfsXml);
		
		//backup
		writeXml(doc,new File(workspace.getLocation() + "/services/backup"+new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())+".xml.backup"));
		
		//노드 가져오기
		Element root = doc.getDocumentElement();
		
		//FeatureStoreId 초기화
		while(root.getElementsByTagName("FeatureStoreId").getLength() != 0 ) {
			root.removeChild(root.getElementsByTagName("FeatureStoreId").item(0));
		}
		
		//theme.xml 내용에서 레이어 목록 읽어오기
		Themes cfg = (Themes) unmarshall( pkg, SCHEMA_URL, configUrl, workspace );
		List<String> layers = cfg.getLayerStoreId();
		
		//유효성 검사를 위한 코드
		LayerStoreManager mgr = workspace.getSubsystemManager( LayerStoreManager.class );
		
		//xml에 featureStoreId 추가
		//21.0.16 유효성 검사 추가
		for(String layer : layers) {
			LayerStore store = mgr.get(layer);
			if(store != null) {
				Element feature = doc.createElement("FeatureStoreId");
				feature.setTextContent(layer);
				root.insertBefore(feature, root.getElementsByTagName("EnableTransactions").item(0)); //EnableTransactions 앞부분에 추가
			}else {
				LOG.warn( "Layer store with LayerStoreId {} is not available.", layer );
				continue;
			}
		}
		
		//xml 쓰기
		writeXml(doc,wfsXml);
	}
	
	//xml 파일 읽기
	private Document readXml(File wfsXml) throws ParserConfigurationException, SAXException, IOException {
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		builder = factory.newDocumentBuilder();
		Document doc = builder.parse(wfsXml);
		doc.normalize();
		return doc;
	}
	
	//xml파일 쓰기
	private void writeXml(Document doc, File wfsXml) throws TransformerException {
		doc.normalize();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource DOMsource = new DOMSource(doc);
        StreamResult result =  new StreamResult(wfsXml);
        transformer.transform(DOMsource, result);
	}
	
}
