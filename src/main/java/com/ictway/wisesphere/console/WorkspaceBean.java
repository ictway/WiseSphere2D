//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.deegree.client.core.utils.ActionParams.getParam1;
import static org.deegree.commons.utils.net.HttpUtils.STREAM;
import static org.deegree.services.controller.OGCFrontController.getModulesInfo;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

import org.apache.http.HttpResponse;
import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.modules.ModuleInfo;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.io.Zip;
import org.deegree.commons.utils.net.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSF Bean for controlling various global aspects of the {@link DeegreeWorkspace}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: markus $
 * 
 * @version $Revision: $, $Date: $
 */
@ManagedBean(name = "workspaceSp")
@ApplicationScoped
public class WorkspaceBean implements Serializable {

    private static Logger LOG = LoggerFactory.getLogger( WorkspaceBean.class );

    private static final long serialVersionUID = -2225303815897732019L;

    public static final String WS_MAIN_VIEW = "/console/workspace/workspace";

    public static final String WS_UPLOAD_VIEW = "/console/workspace/upload";

    private static final String DEFAULT_VERSION = "3.2-rc2";

    private static final String[] WS_LIST = { "spatium-workspace-test" };

    private final HashMap<String, String> workspaceLocations = new HashMap<String, String>();

    private String lastMessage = "Workspace initialized.";
    
    private String workspaceImportName;

    public String getWorkspaceImportName() {
        return workspaceImportName;
    }

    public void setWorkspaceImportName( String workspaceImportName ) {
        this.workspaceImportName = workspaceImportName;
    }

    public String getWorkspaceRoot() {
        return DeegreeWorkspace.getWorkspaceRoot();
    }
    
    public void downloadWorkspace() {
        String wsName = (String) getParam1();
        String location = workspaceLocations.get( wsName );
        InputStream in = null;
        try {
            setWorkspaceImportName( wsName );
            importWorkspace( location );
        } catch ( Throwable t ) {
//            FacesMessage fm = new FacesMessage( SEVERITY_ERROR, "Unable to download workspace: " + t.getMessage(), null );
            FacesMessage fm = new FacesMessage( SEVERITY_ERROR, "작업공간을 다운로드 할 수 없습니다(오류내용: " + t.getMessage() + ")", null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
        } finally {
            closeQuietly( in );
        }
    }

    private void importWorkspace( String location ) {
        InputStream in = null;
        try {
            URL url = new URL( location );
            Pair<InputStream, HttpResponse> p = HttpUtils.getFullResponse( STREAM, location, null, null, null, 10 );
            File root = new File( getWorkspaceRoot() );
            in = p.getFirst();
            if ( p.second.getStatusLine().getStatusCode() != 200 ) {
                throw new Exception( "Download of '" + location + "' failed. Server responded with HTTP status code "
                                     + p.second.getStatusLine().getStatusCode() );
            }
            String name = workspaceImportName;
            if ( name == null || name.isEmpty() ) {
                name = new File( url.getPath() ).getName();
                name = name.substring( 0, name.lastIndexOf( "." ) );
            }
            File target = new File( root, name );
            if ( target.exists() ) {
                lastMessage = "Workspace already exists!";
            } else {
                Zip.unzip( in, target );
                lastMessage = "Workspace has been imported.";
            }
        } catch ( Exception e ) {
            e.printStackTrace();
//            FacesMessage fm = new FacesMessage( SEVERITY_ERROR, "Unable to import workspace: " + e.getMessage(), null );
            FacesMessage fm = new FacesMessage( SEVERITY_ERROR, "작업공간을 가져올 수 없습니다(오류내용: " + e.getMessage() + ")", null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
        } finally {
            closeQuietly( in );
        }
    }

    public List<String> getRemoteWorkspaces() {
        workspaceLocations.clear();
        List<String> list = new ArrayList<String>();
        for ( String wsArtifactName : WS_LIST ) {
            addWorkspaceLocation( wsArtifactName, list );
        }
        return list;
    }

    private void addWorkspaceLocation( String wsArtifactName, List<String> list ) {
        String repo = getVersion().endsWith( "SNAPSHOT" ) ? "snapshots" : "releases";
        String version = getVersion().endsWith( "SNAPSHOT" ) ? "LATEST" : getVersion();
//        String url = "http://localhost:8080/webservices/spatium-workspace-test.zip";
        String url = "http://14.35.204.48:8080/ws/spatium-workspace-test.zip";
        workspaceLocations.put( wsArtifactName, url );
        list.add( wsArtifactName );
    }

    private String getVersion() {
        String version = null;
        Collection<ModuleInfo> modules = getModulesInfo();
        for ( ModuleInfo module : modules ) {
            if ( module.getArtifactId().equals( "deegree-core-commons" ) ) {
                version = module.getVersion();
                break;
            }
        }
        if ( version == null ) {
            LOG.warn( "No valid version information from Maven deegree modules available. Defaulting to "
                      + DEFAULT_VERSION );
            version = DEFAULT_VERSION;
        }
        return version;
    }

}