//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package com.ictway.wisesphere.console;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.deegree.client.core.utils.ActionParams.getParam1;
import static org.deegree.client.core.utils.ActionParams.getParam2;
import static org.deegree.commons.config.ResourceState.StateType.init_error;
import static org.deegree.services.controller.OGCFrontController.getServiceWorkspace;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.xml.namespace.QName;

import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.config.ResourceProvider;
import org.deegree.commons.config.ResourceState;
import org.deegree.console.Config;
import org.deegree.console.ResourceManagerMetadata2;
import org.deegree.console.ResourceProviderMetadata;
import org.slf4j.Logger;

import com.ictway.wisesphere.feature.utils.DBUtils;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@ManagedBean(name = "spConfigManager")
@SessionScoped
public class ConfigManager extends org.deegree.console.ConfigManager implements Serializable {

    private static final long serialVersionUID = -8669393203479413121L;

    @SuppressWarnings("unused")
	private static final Logger LOG = getLogger( ConfigManager.class );
    
    private org.deegree.console.ConfigManager configManger;
    
    private String id;

    private String configType;

    private Config config;
    
    private String appColumn;
    
    private String filePath;
    
    private String sql;
    
    private String connId;
    
    private String styleName = "POINT";
    
    private ResourceManagerMetadata2 currentResourceManager;
    
    public ConfigManager() {
        super();
    }
    
    /**
     * 좌측메뉴 Data Stores > tile에 init_error 상태의 resource가 있는지 확인한다.
     * tile 중 id가 'tilematrixset' 인 것은 리스트에서 숨기기 위함
     * @return
     */
    public boolean getTileHasError() {
    	boolean result = false;
    	String category = "datastore";
        if ( getServiceWorkspace() == null ) {
            return result;
        }
        for ( ResourceManager mgr : getServiceWorkspace().getResourceManagers() ) {
        	ResourceManagerMetadata2 md = ResourceManagerMetadata2.getMetadata( mgr );
        	if ( md != null && category.equals( md.getCategory() ) && "tile".equals(md.getName()) ) {
	        	for ( ResourceState state : mgr.getStates() ) {
	            	// id가 'tilematrixset'로 시작하는 것들은 에러체크 하지 않는다.
	            	if(!state.getId().startsWith("tilematrixset")){ 
	            		if ( state.getType() == init_error ) {
	            			result = true;
	            			break;
	            		}
	            	}
	            }
        	}
        }
        
        return result;
    }
    
    public static String getDefaultCharEncoding(){
        byte [] bArray = {'w'};
        InputStream is = new ByteArrayInputStream(bArray);
        InputStreamReader reader = new InputStreamReader(is);
        String defaultCharacterEncoding = reader.getEncoding();
        return defaultCharacterEncoding;
    }
    
    public String saveOnlyFeature() throws IllegalArgumentException, IOException { //Feature만 수정할 경우 - style, theme 변화 없음
		this.configManger = (org.deegree.console.ConfigManager) getParam1();
		this.config = (Config) getParam2();
		
		this.id = config.getId();
		this.configType = this.configManger.getNewConfigType();
		this.currentResourceManager = configManger.getCurrentResourceManager();
    	
    	/*feature 등록*/
		this.config.setAutoActivate(false);
		String returnVal = this.config.save();

		return returnVal;
    }
    
    public String saveFeture() {
    	try {
    		this.configManger = (org.deegree.console.ConfigManager) getParam1();
    		this.config = (Config) getParam2();
    		
    		this.id = config.getId();
    		this.configType = this.configManger.getNewConfigType();
    		this.currentResourceManager = configManger.getCurrentResourceManager();
    		
    		String currentResourceName = this.currentResourceManager.getName();
	    	
	    	/*feature 등록*/
    		this.config.setAutoActivate(false);
    		String returnVal = this.config.save();
    		
    		String charEncoding = getDefaultCharEncoding();

    		String content = readFileToString( this.config.getLocation(), charEncoding);
    		//String content = readFileToString( this.config.getLocation(), "UTF-8" );
    		//String content = readFileToString( this.config.getLocation(), "ANSI" );
    		int up = 0;
    		/*
    		if(content.indexOf("<StorageCRS>EPSG:4326</StorageCRS>") > -1){
    			content = content.replaceAll("<StorageCRS>EPSG:4326</StorageCRS>", "<StorageCRS>EPSG:3857</StorageCRS>");
    			config.setContent(content);
    			up = 1;
    		}
    		*/
    		if(content.indexOf("<FeatureTypeName>MyFeature</FeatureTypeName>") > -1){
    			content = content.replaceAll("<FeatureTypeName>MyFeature</FeatureTypeName>", "<FeatureTypeName>"+ this.id +"</FeatureTypeName>");
    			config.setContent(content);
    			up = 1;
    		}
    		if(up == 1){
    			this.config.save(); // FeatureTypeName update
    		}
    		
    		if(content.indexOf("<JDBCConnId>") > -1){
    			this.connId = content.substring(content.indexOf("<JDBCConnId>")+12, content.indexOf("</JDBCConnId>"));
    		}
    		
    		if(content.indexOf("<SQLStatement>") > -1){ // 쿼리의 첫번째 컬럼 구하기
    			this.sql = content.substring(content.indexOf("<SQLStatement>")+14, content.indexOf("</SQLStatement>"));
    			content = content.toLowerCase();
    			content = content.substring(content.indexOf("select", content.indexOf("<SQLStatement>")+1)+6, content.indexOf("from", content.indexOf("<SQLStatement>")+1));
    			if(content.indexOf(",") > -1){
    				content = content.substring(0, content.indexOf(","));
    			}
    			
    			this.appColumn = content.trim();
    		}
    		if(content.indexOf("<File>") > -1){ // shapefile 경로 구하기
    			this.filePath = content.substring(content.indexOf("<File>")+6, content.indexOf("</File>"));
    		}
    		
	    	/**
	    	 * feature 등록인 경우 layer, map, theme를 자동생성 한다.
	    	 */
    		
    		// 이미 등록된 feature return...BY_JIN
//    		config.getState();
//    		String ext = config.getLocation().toString().substring( config.getLocation().toString().lastIndexOf( "." ) + 1 );
//    		if (config.getLocation().exists() && config.getState().equals("deactivated")){
//       		if (config.getLocation().exists() && config.getState().equals("deactivated")){
//    			return returnVal;
//    		}
    		// -- BY_JIN
    		
	    	if("feature".equals(currentResourceName)){
	    		List<ResourceManagerMetadata2> mapManagerList = configManger.getMapManagers();
	    		
	    		for(ResourceManagerMetadata2 mapManager : mapManagerList){
	    			String managerName = mapManager.getName();
	    			if("layers".equals(managerName)){
	    				/*layer 등록*/
	    				this.currentResourceManager = mapManager;
	    				this.configType = "Feature";
	    				this.save();
	    				
	    			} else if("styles".equals(managerName)){
	    				// geometry 컬럼의 타입을 알아낸다.
	    				String ftLocalName = "Feature";
	    				String ftNamespace = "http://www.deegree.org/app";
	    				String ftPrefix = "app";
	    		        QName ftName = new QName( ftNamespace, ftLocalName, ftPrefix );
	    		        if(this.connId != null && this.sql != null){
		    		        String geometryType = DBUtils.getGeometryType( ftName, this.connId, this.sql, getServiceWorkspace() );
		    		        if(geometryType != null && !"".equals(geometryType)){
		    		        	if("MULTI_POLYGON".equals(geometryType) || "POLYGON".equals(geometryType)){
		    		        		styleName = "POLYGON";
		    		        	} else if("MULTI_LINESTRING".equals(geometryType) || "LINESTRING".equals(geometryType)){
		    		        		styleName = "LINESTRING";
		    		        	}
		    		        }
	    		        }
	    		        
	    				/*styles 등록*/
	    				this.currentResourceManager = mapManager;
	    				this.configType = "SE";
	    				this.save();
	    				
	    			} else if("themes".equals(managerName)){
	    				/*themes 등록*/
	    				this.currentResourceManager = mapManager;
	    				this.id = "theme";
	    				this.configType = "SE";
	    				this.save();
	    			}
	    		}
	    	}
	    	
	    	return returnVal;

	    } catch ( Throwable t ) {
            
//	    	FacesMessage fm = new FacesMessage( SEVERITY_ERROR, "Unable to create config: " + t.getMessage(), null );
	    	FacesMessage fm = new FacesMessage( SEVERITY_ERROR, "config를 생성할 수 없습니다(오류내용: " + t.getMessage() + ")", null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
            return null;
            
        } finally{
        	
        	clearAll();
        	
        }
    	
    }
    
    
    private void save() throws IllegalArgumentException, IOException {

	    	ResourceManager manager = this.currentResourceManager.getManager();
	    	ResourceProvider provider = this.currentResourceManager.getProvider(this.configType);
	        if ( provider == null ) {
	            provider = this.currentResourceManager.getProviders().get( 0 );
	        }

	        ResourceProviderMetadata md = ResourceProviderMetadata.getMetadata( provider );
	    	String newConfigTypeTemplate = "";
	    	if (md != null && md.getExamples().size() > 0){
    			newConfigTypeTemplate = md.getExamples().keySet().iterator().next() ;
            }

	    	URL templateURL = md.getExamples().get( newConfigTypeTemplate ).getContentLocation();
	    	ResourceState<?> state = manager.getState(this.id);
	    	
	    	// BY_JIN --
//	    	if (state.getConfigLocation().exists()) {
//	    		return;
//	    	}
	    	// -- BY_JIN
	    	
	    	if(state == null) {
	    		state = manager.createResource( this.id, templateURL.openStream() );
    		}

	    	Config spconfig = new Config( state, this, currentResourceManager.getManager(), currentResourceManager.getStartView(), false );
	    	
			String content = "";
			StringBuffer bf = new StringBuffer();
			
	    	
			if("layers".equals(this.currentResourceManager.getName())){
				/*
				bf.append("<FeatureLayers xmlns=\"http://www.deegree.org/layers/feature\" xmlns:g=\"http://www.deegree.org/metadata/spatial\" xmlns:d=\"http://www.deegree.org/metadata/description\" xmlns:l=\"http://www.deegree.org/layers/base\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.deegree.org/layers/feature feature.xsd\" configVersion=\"3.2.0\"> \n");
				bf.append("	<FeatureStoreId>"+this.id+"</FeatureStoreId> \n");
				bf.append("    <FeatureLayer> \n");
				bf.append("      <l:Name>"+ this.id +"</l:Name> \n");
				bf.append("      <d:Title>"+ this.id +"</d:Title> \n");
				bf.append("      <l:StyleRef> \n");
				bf.append("      <l:StyleStoreId>"+ this.id +"</l:StyleStoreId> \n");
				bf.append("      </l:StyleRef> \n");
				//bf.append("<!--" \n);
				//bf.append("      <l:StyleRef> \n");
				//bf.append("      <l:StyleStoreId>LP_PA_CBND_BUBUN_LINE</l:StyleStoreId> \n");
				//bf.append("      </l:StyleRef> \n");
				//bf.append("--> \n");
				bf.append("    </FeatureLayer> \n");
				bf.append("</FeatureLayers>");
				*/

				bf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n");
				bf.append("<FeatureLayers xmlns=\"http://www.deegree.org/layers/feature\" xmlns:g=\"http://www.deegree.org/metadata/spatial\" xmlns:d=\"http://www.deegree.org/metadata/description\" xmlns:l=\"http://www.deegree.org/layers/base\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.deegree.org/layers/feature feature.xsd\" configVersion=\"3.2.0\"> \n");
				bf.append("	<AutoLayers> \n");
				bf.append("		<FeatureStoreId>"+ this.id +"</FeatureStoreId> \n");
				bf.append("		<StyleStoreId>"+ this.id +"</StyleStoreId> \n");
				bf.append("	</AutoLayers> \n");
				bf.append("</FeatureLayers> \n");
				content = bf.toString();
				
	    	}else if("styles".equals(this.currentResourceManager.getName())){
	    		if("POLYGON".equals(styleName)){
	    			if(this.appColumn == null || "".equals(this.appColumn)){
		    			bf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n");
		    			bf.append("<sld:PolygonSymbolizer xmlns:sld=\"http://www.opengis.net/se\"> \n");
		    			bf.append("  <sld:Fill> \n");
		    			bf.append("    <sld:SvgParameter name=\"fill\">#004000</sld:SvgParameter> \n");
		    			bf.append("    <sld:SvgParameter name=\"fill-opacity\">1.0</sld:SvgParameter> \n");
		    			bf.append("  </sld:Fill> \n");
		    			bf.append("  <sld:Stroke> \n");
		    			bf.append("    <sld:SvgParameter name=\"stroke\">#000000</sld:SvgParameter> \n");
		    			bf.append("    <sld:SvgParameter name=\"stroke-opacity\">1.0</sld:SvgParameter> \n");
		    			bf.append("    <sld:SvgParameter name=\"stroke-width\">1</sld:SvgParameter> \n");
		    			bf.append("    <sld:SvgParameter name=\"stroke-dasharray\">1</sld:SvgParameter> \n");
		    			bf.append("  </sld:Stroke> \n");
		    			bf.append("</sld:PolygonSymbolizer> \n");
		    		} else{
		    			bf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n");
		    			bf.append("<FeatureTypeStyle xmlns=\"http://www.opengis.net/se\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:app=\"http://www.deegree.org/app\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:deegreeogc=\"http://www.deegree.org/ogc\" xmlns:sed=\"http://www.deegree.org/se\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/se http://schemas.opengis.net/se/1.1.0/FeatureStyle.xsd http://www.deegree.org/se http://schemas.deegree.org/se/1.1.0/Symbolizer-deegree.xsd\">	\n");
		    			bf.append("  <Name>"+ this.id +"</Name>	\n");
		    			bf.append("  <Rule>	\n");
		    			bf.append("    <Name>"+ this.id +"</Name>	\n");
		    			bf.append("      <PolygonSymbolizer>	\n");
		    			bf.append("        <Fill>	\n");
		    			bf.append("          <SvgParameter name=\"fill\">#367FA9</SvgParameter>	\n");
		    			bf.append("          <SvgParameter name=\"fill-opacity\">0.6</SvgParameter>	\n");
		    			bf.append("        </Fill>	\n");
		    			bf.append("        <Stroke>	\n");
		    			bf.append("          <SvgParameter name=\"stroke\">#03341D</SvgParameter>	\n");
		    			bf.append("          <SvgParameter name=\"stroke-opacity\">1.0</SvgParameter>	\n");
		    			bf.append("          <SvgParameter name=\"stroke-width\">1</SvgParameter>	\n");
		    			bf.append("        </Stroke>	\n");
		    			bf.append("      </PolygonSymbolizer>	\n");
		    			bf.append("<!--  	\n");
		    			bf.append("    <TextSymbolizer>	\n");
		    			bf.append("        <Label>	\n");
		    			bf.append("          <ogc:PropertyName>app:"+ this.appColumn +"</ogc:PropertyName>	\n");
		    			bf.append("        </Label>	\n");
		    			bf.append("        <Font>	\n");
		    			bf.append("          <SvgParameter name=\"font-family\">Bitstream Vera Sans Mono</SvgParameter>	\n");
		    			bf.append("          <SvgParameter name=\"font-family\">Sans-Serif</SvgParameter>	\n");
		    			bf.append("          <SvgParameter name=\"font-weight\">bold</SvgParameter>	\n");
		    			bf.append("          <SvgParameter name=\"font-size\">12</SvgParameter>	\n");
		    			bf.append("       </Font>	\n");
		    			bf.append("       <Halo>	\n");
		    			bf.append("         <Radius>2</Radius>	\n");
		    			bf.append("         <Fill>	\n");
		    			bf.append("           <SvgParameter name=\"fill-opacity\">0.5</SvgParameter>	\n");
		    			bf.append("           <SvgParameter name=\"fill\">#000000</SvgParameter>	\n");
		    			bf.append("         </Fill>	\n");
		    			bf.append("       </Halo>	\n");
		    			bf.append("       <Fill>	\n");
		    			bf.append("         <SvgParameter name=\"fill\">#FFFFFF</SvgParameter>	\n");
		    			bf.append("       </Fill> 	\n");
		    			bf.append("      </TextSymbolizer>	\n");
		    			bf.append("-->  	\n");
		    			bf.append("  </Rule>	\n");
		    			bf.append("</FeatureTypeStyle>	\n");
						
		    		}
	    		} else if("LINESTRING".equals(styleName)){	
	    			if(this.appColumn == null || "".equals(this.appColumn)){
	    				bf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n");
	    				bf.append("<sld:LineSymbolizer xmlns:sld=\"http://www.opengis.net/se\"> \n");
	    				bf.append("	<sld:Stroke> \n");
	    				bf.append("    <sld:SvgParameter name=\"stroke\">#000000</sld:SvgParameter> \n");
	    				bf.append("    <sld:SvgParameter name=\"stroke-opacity\">1.0</sld:SvgParameter> \n");
	    				bf.append("    <sld:SvgParameter name=\"stroke-width\">1</sld:SvgParameter> \n");
	    				bf.append("    <sld:SvgParameter name=\"stroke-dasharray\">1</sld:SvgParameter> \n");
	    				bf.append("  </sld:Stroke> \n");
	    				bf.append("</sld:LineSymbolizer> \n");
	    			} else{
	    				bf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n");
	    				bf.append("<FeatureTypeStyle xmlns=\"http://www.opengis.net/se\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:app=\"http://www.deegree.org/app\" xmlns:deegreeogc=\"http://www.deegree.org/ogc\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:sed=\"http://www.deegree.org/se\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/se http://schemas.opengis.net/se/1.1.0/FeatureStyle.xsd http://www.deegree.org/se http://schemas.deegree.org/se/1.1.0/Symbolizer-deegree.xsd\"> \n");
	    				bf.append("		  <Name>subway</Name> \n");
	    				bf.append("		  <Rule> \n");
	    				bf.append("		    <Name>subway</Name> \n");
	    				bf.append("		      <LineSymbolizer> \n");
	    				bf.append("		         <Stroke> \n");
	    				bf.append("		           <CssParameter name=\"stroke\">#0000FF</CssParameter> \n");
	    				bf.append("		           <CssParameter name=\"stroke-width\">3</CssParameter> \n");
	    				bf.append("		           <CssParameter name=\"stroke-dasharray\">5 2</CssParameter> \n");
	    				bf.append("		         </Stroke> \n");
	    				bf.append("		       </LineSymbolizer> \n");
	    				bf.append("<!-- \n");
	    				bf.append("		    <TextSymbolizer> \n");
	    				bf.append("		        <Label> \n");
	    				bf.append("		          <ogc:PropertyName>app:sbw_nm</ogc:PropertyName> \n");
	    				bf.append("		        </Label> \n");
	    				bf.append("		        <Font> \n");
	    				bf.append("		          <SvgParameter name=\"font-family\">Bitstream Vera Sans Mono</SvgParameter> \n");
	    				bf.append("		          <SvgParameter name=\"font-family\">Sans-Serif</SvgParameter> \n");
	    				bf.append("		          <SvgParameter name=\"font-weight\">bold</SvgParameter> \n");
	    				bf.append("		          <SvgParameter name=\"font-size\">12</SvgParameter> \n");
	    				bf.append("		       </Font> \n");
	    				bf.append("		       <Halo> \n");
	    				bf.append("		         <Radius>2</Radius> \n");
	    				bf.append("		         <Fill> \n");
	    				bf.append("		           <SvgParameter name=\"fill-opacity\">0.5</SvgParameter> \n");
	    				bf.append("		           <SvgParameter name=\"fill\">#F39C12</SvgParameter> \n");
	    				bf.append("		         </Fill> \n");
	    				bf.append("		       </Halo> \n");
	    				bf.append("		       <Fill> \n");
	    				bf.append("		         <SvgParameter name=\"fill\">#FFFFFF</SvgParameter> \n");
	    				bf.append("		       </Fill> \n");
	    				bf.append("		      </TextSymbolizer> \n");
	    				bf.append("--> \n");
	    				bf.append("		  </Rule> \n");
	    				bf.append("		</FeatureTypeStyle> \n");
	    			}
	    		} else{ // "POINT".equals(styleName)
		    		if(this.appColumn == null || "".equals(this.appColumn)){
		    			bf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n");
		    			bf.append("<sld:PointSymbolizer xmlns:sld=\"http://www.opengis.net/se\"> \n");
		    			bf.append("  <sld:Graphic> \n");
		    			bf.append("    <sld:Mark> \n");
		    			bf.append("      <sld:WellKnownName>square</sld:WellKnownName> \n");
		    			bf.append("      <sld:Fill> \n");
		    			bf.append("        <sld:SvgParameter name=\"fill\">#00cc00</sld:SvgParameter> \n");
		    			bf.append("      </sld:Fill> \n");
		    			bf.append("    </sld:Mark> \n");
		    			bf.append("    <sld:Size>15</sld:Size> \n");
		    			bf.append("  </sld:Graphic> \n");
		    			bf.append("</sld:PointSymbolizer> \n");
		    		} else{
		    			bf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n");
		    			bf.append("<FeatureTypeStyle xmlns=\"http://www.opengis.net/se\" xmlns:app=\"http://www.deegree.org/app\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:deegreeogc=\"http://www.deegree.org/ogc\" xmlns:sed=\"http://www.deegree.org/se\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/se http://schemas.opengis.net/se/1.1.0/FeatureStyle.xsd http://www.deegree.org/se http://schemas.deegree.org/se/1.1.0/Symbolizer-deegree.xsd\">	\n");
		    			bf.append("		 <Name>"+ this.id +"</Name>	\n");
		    			bf.append("		 <Rule>	\n");
		    			bf.append("		    <Name>"+ this.id +"</Name>	\n");
		    			bf.append("		    <Description>	\n");
		    			bf.append("		      <Title>"+ this.id +"</Title>	\n");
		    			bf.append("		    </Description>	\n");
		    			bf.append("		      <PointSymbolizer>	\n");
		    			bf.append("		        <Graphic>	\n");
		    			bf.append("		          <Mark>	\n");
		    			bf.append("       			<WellKnownName>square</WellKnownName>	\n");
		    			bf.append("			        <Fill>	\n");
		    			bf.append("			          <SvgParameter name=\"fill\">#FF0000</SvgParameter>	\n");
		    			bf.append("			        </Fill>	\n");
		    			bf.append("			        <Stroke>	\n");
		    			bf.append("			          <SvgParameter name=\"stroke\">#000000</SvgParameter>	\n");
		    			bf.append("			          <SvgParameter name=\"stroke-width\">1</SvgParameter>	\n");
		    			bf.append("			       </Stroke>	\n");
		    			bf.append("			      </Mark>	\n");
		    			bf.append("			      <Size>13</Size>	\n");
		    			bf.append("			    </Graphic>	\n");
		    			bf.append("			  </PointSymbolizer>	\n");
		    			bf.append("<!--	\n");
		    			bf.append("			  <TextSymbolizer>	\n");
		    			bf.append("			       <Label>	\n");
		    			bf.append("			         <ogc:PropertyName>app:"+ this.appColumn +"</ogc:PropertyName>	\n");
		    			bf.append("			       </Label>	\n");
		    			bf.append("			       <Font>	\n");
		    			bf.append("			         <SvgParameter name=\"font-family\">Bitstream Vera Sans Mono</SvgParameter>	\n");
		    			bf.append("			         <SvgParameter name=\"font-family\">Sans-Serif</SvgParameter>	\n");
		    			bf.append("			         <SvgParameter name=\"font-weight\">bold</SvgParameter>	\n");
		    			bf.append("			         <SvgParameter name=\"font-size\">12</SvgParameter>	\n");
		    			bf.append("			      </Font>	\n");
		    			bf.append("				 <LabelPlacement>	\n");
		    			bf.append("			          <PointPlacement>	\n");
		    			bf.append("			            <AnchorPoint>	\n");
		    			bf.append("			              <AnchorPointX>0.5</AnchorPointX>	\n");
		    			bf.append("			              <AnchorPointY>1.0</AnchorPointY>	\n");
		    			bf.append("			            </AnchorPoint>	\n");
		    			bf.append("			          </PointPlacement>	\n");
		    			bf.append("			        </LabelPlacement>	\n");
		    			bf.append("			      <Halo>	\n");
		    			bf.append("			        <Radius>2</Radius>	\n");
		    			bf.append("			        <Fill>	\n");
		    			bf.append("			          <SvgParameter name=\"fill-opacity\">0.5</SvgParameter>	\n");
		    			bf.append("			          <SvgParameter name=\"fill\">#F39C12</SvgParameter>	\n");
		    			bf.append("			        </Fill>	\n");
		    			bf.append("			      </Halo>	\n");
		    			bf.append("			      <Fill>	\n");
		    			bf.append("			        <SvgParameter name=\"fill\">#FFFFFF</SvgParameter>	\n");
		    			bf.append("			      </Fill> 	\n");
		    			bf.append("			  </TextSymbolizer> 	\n");
		    			bf.append("-->	\n");
		    			bf.append("			 </Rule>	\n");
		    			bf.append("		</FeatureTypeStyle>		\n");
		    		}
	    		}
	    		
				content = bf.toString();
				
	    	}else if("themes".equals(this.currentResourceManager.getName())){
	    		if(!state.getConfigLocation().exists()){
	    			spconfig.edit();
	    		}
	    		List<String> commented = null;
	    		
	    		content = readFileToString( state.getConfigLocation(), "UTF-8" );
	    		// @@ictway
	    		commented = beforeChangeTheme(content);
	    		if (commented.size()>0) {
	    			content = commented.get(commented.size()-1);
	    		}
	    		
	    		String part1 = content.substring(0, content.lastIndexOf("</LayerStoreId>")+15);
	    		if(part1 != null){
	    			part1 = part1.trim() + "\n";
	    		}
	    		
	    		String part2 = "<LayerStoreId>"+ this.config.getId() +"</LayerStoreId>";
	    		if(content.indexOf(part2) > -1){
	    			part2 = "";
	    		} else {
	    			String padding = "";
	    			for (int i=0; i<commented.size()-1; i++) {
	    				if (commented.get(i).indexOf("/LayerStoreId>")>0) {
	    					padding += commented.get(i) + "\n";
	    				}
	    			}
	    			part2 = padding + "  "+ part2 + " \n";
	    		}
	    		
	    		String part3 = content.substring(content.indexOf("<Theme>",content.lastIndexOf("</LayerStoreId>")), content.lastIndexOf("</Theme>"));
	    		if(part3 != null){
	    			part3 = part3.trim() +" \n";
	    		}
	    		
	    		String part4 = "<Identifier>"+ this.config.getId() +"</Identifier>";
	    		if(content.indexOf(part4) > -1){
	    			part4 = "";
	    		} else {
	    			String padding = "";
	    			for (int i=0; i<commented.size()-1; i++) {
	    				if (commented.get(i).indexOf("/Identifier>")>0) {
	    					padding += commented.get(i) + "\n";
	    				}
	    			}
	    			part4 = padding + "    <Theme> \n"
	    				  + "      <Identifier>"+ this.config.getId() +"</Identifier> \n"
				    	  + "      <d:Title>"+ this.config.getId() +"</d:Title> \n"
				    	  + "      <Layer>"+ this.config.getId() +"</Layer> \n"
				    	  + "    </Theme> \n";
	    		}
	    		
	    		String part5 = "  </Theme> \n"
	    					 + "</Themes> \n";
	    		
	    		content = part1 + part2 + part3 + part4 + part5;
	    	}

	    	spconfig.setContent(content);
	    	spconfig.save();
	    	
    }

    // @@ictway 2018.03
	private List<String> beforeChangeTheme(String content) {
		List<String> res = new ArrayList<String>();
		String removedContent = "";
		
		Pattern p =  Pattern.compile("<!--(.*?)-->", Pattern.DOTALL);
		Matcher m = p.matcher(content);
		int start0 = 0;

		int start = -1;
		int end = -1;
		while(m.find()) {
			start = m.start();
			end = m.end();
			
			String temp = content.substring(start, end);
			res.add(temp);
			removedContent = removedContent + content.substring(start0, start);
			start0 = end;
        }
		
		if (removedContent.isEmpty()) {
			removedContent = content;
		} else {
			removedContent = removedContent + content.substring(end, content.length());
		}
		
		res.add(removedContent);

		return res;
	}

	private void clearAll(){
        this.configManger = null;
        this.id = null;
        this.configType = null;
        this.config = null;
        this.appColumn = null;
        this.filePath = null;
        this.currentResourceManager = null;
    }
    
}