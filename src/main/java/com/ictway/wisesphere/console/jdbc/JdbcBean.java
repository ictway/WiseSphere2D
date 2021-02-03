package com.ictway.wisesphere.console.jdbc;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static javax.faces.application.FacesMessage.SEVERITY_INFO;
import static org.deegree.client.core.utils.ActionParams.getParam1;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceState;
import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.jdbc.jaxb.JDBCConnection;
import org.deegree.commons.jdbc.param.JDBCParamsManager;
import org.deegree.console.Config;
import org.deegree.console.ConfigManager;
import org.deegree.console.ResourceManagerMetadata2;
import org.deegree.console.WorkspaceBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ictway.wisesphere.commons.jdbc.driver.AltibaseDriverManager;
import com.ictway.wisesphere.commons.jdbc.driver.TiberoDriverManager;

@ManagedBean(name = "spJdbcBean")
@SessionScoped
public class JdbcBean {
	private static final Logger LOG = LoggerFactory.getLogger( JdbcBean.class );
    private String dbType = "mssql";

    private String dbPort = "1433";

    private String dbHost;

    private String dbName;

    private String dbConn;

    private String dbUser;

    private String dbPwd;

    private Config config;
    
    public String getDbType() {
        return dbType;
    }

    public String getDbPort() {
    	String res = "";
    	String strPort = "";
    	List<String>portList = Arrays.asList(dbPort.split(","));
    	
    	if (portList.size()>0) {
	        for (int i=0; i<portList.size(); i++) {
	        	strPort = portList.get(i);
	
	        	if (strPort.contains(":"))
	        		strPort = strPort.substring(strPort.indexOf(":")+1, strPort.length());
	        	
	        	res += strPort + ",";
	        }
	        
	        this.dbPort = res.substring(0, res.length()-1);
    	} else {
    	    this.dbPort = dbPort;
    	}
    	
        return dbPort;
    }

    public String getDbHost() {
    	String res = "";
    	String strHost = "";
    	if (dbHost != null) {
	    	List<String>hostList = Arrays.asList(dbHost.split(","));
	    	
	    	if (hostList.size()>0) {
		        for (int i=0; i<hostList.size(); i++) {
		        	strHost = hostList.get(i);
		
		        	if (strHost.contains(":"))
		        		strHost = strHost.substring(0, strHost.indexOf(":"));
		        	
		        	res += strHost + ",";
		        }
		        
		        this.dbHost = res.substring(0, res.length()-1);
	    	} else {
	    	    this.dbHost = dbHost;
	    	}
    	}
        return dbHost;
    }

    public String getDbName() {
        return dbName;
    }

    public String getDbConn() {
        return dbConn;
    }

    public String getDbUser() {
        return dbUser;
    }

    public String getDbPwd() {
        return dbPwd;
    }

    public void setDbType( String dbType ) {
        this.dbType = dbType;
        if ( dbType.equals( "mssql" ) ) {
            dbPort = "1433";
            dbConn = "jdbc:sqlserver://" + dbHost + ":" + dbPort + ";databaseName=" + dbName;
            return;
        }
        if ( dbType.equals( "oracle" ) ) {
            dbPort = "1521";
            dbConn = "jdbc:oracle:thin:@" + dbHost + ":" + dbPort + ";" + dbName;
            return;
        }
        if ( dbType.equals( "oracleService" ) ) {
            dbPort = "1521";
            //dbConn = "jdbc:oracle:thin:@" + dbHost + ":" + dbPort + ";" + dbName;
            dbConn = "jdbc:oracle:thin:@//" + dbHost + ":" + dbPort + "/" + dbName;
            return;
        }
        if ( dbType.equals( "jeusCP" ) ) {
            dbPort = "0";
            dbHost = "NA";
            dbConn = "NA";
            return;
        }
        if ( dbType.equals( "postgis" ) ) {
            dbPort = "5432";
            dbConn = "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName;
            return;
        }
        if ( dbType.equals( "altibase" ) ) {
            dbPort = "20300";
            dbConn = "jdbc:Altibase://" + dbHost + ":" + dbPort + "/" + dbName;
            return;
        }
        if ( dbType.equals( "tibero" ) ) {
            dbPort = "8629";
            dbConn = "jdbc:tibero:thin:@" + dbHost + ":" + dbPort + ":" + dbName;
            //dbConn = "jdbc:tibero:thin:@(description=(failover=on)(load_balance=on)(address_list=(address=(host=localhost)(port=8629))(address=(host=localhost)(port=8629)))(DATABASE_NAME=tibero))";
            return;
        }
    }

    public void setDbPort( String dbPort ) {
    	String res = "";
    	String strPort = "";
    	List<String>portList = Arrays.asList(dbPort.split(","));
    	
    	if (portList.size()>0) {
	        for (int i=0; i<portList.size(); i++) {
	        	strPort = portList.get(i);
	
	        	if (strPort.contains(":"))
	        		strPort = strPort.substring(strPort.indexOf(":")+1, strPort.length());
	        	
	        	res += strPort + ",";
	        }
	        
	        this.dbPort = res.substring(0, res.length()-1);
    	} else {
    	    this.dbPort = dbPort;
    	}

       	update();
    }

    public void setDbHost( String dbHost ) {
    	String res = "";
    	String strHost = "";
    	List<String>hostList = Arrays.asList(dbHost.split(","));
    	
    	if (hostList.size()>0) {
	        for (int i=0; i<hostList.size(); i++) {
	        	strHost = hostList.get(i);
	
	        	if (strHost.contains(":"))
	        		strHost = strHost.substring(0, strHost.indexOf(":"));
	        	
	        	res += strHost + ",";
	        }
	        
	        this.dbHost = res.substring(0, res.length()-1);
    	} else {
    	    this.dbHost = dbHost;
    	}

        update();
    }

    public void setDbName( String dbName ) {
        this.dbName = dbName;
        update();
    }

    public void setDbUser( String dbUser ) {
        this.dbUser = dbUser;
        update();
    }

    public void setDbPwd( String dbPwd ) {
        this.dbPwd = dbPwd;
        update();
    }

    public void setDbConn( String dbConn ) {
        this.dbConn = dbConn;
        update();
    }

    public void update() {
        if ( dbType.equals( "mssql" ) ) {
            if ( dbPort == null || dbPort.isEmpty() ) {
                dbPort = "1433";
            }
            dbConn = "jdbc:sqlserver://" + dbHost + ":" + dbPort + ";databaseName=" + dbName;
            return;
        }

        if ( dbType.equals( "oracle" ) ) {
            if ( dbPort == null || dbPort.isEmpty() ) {
                dbPort = "1521";
            }
            dbConn = "jdbc:oracle:thin:@" + dbHost + ":" + dbPort + ":" + dbName;
            return;
        }

        if ( dbType.equals( "oracleService" ) ) {
            if ( dbPort == null || dbPort.isEmpty() ) {
                dbPort = "1521";
            }

            dbConn = "jdbc:oracle:thin:@//" + dbHost + ":" + dbPort + "/" + dbName;
            return;
        }

        if ( dbType.equals( "jeusCP" ) ) {
            dbConn = "NA";
            return;
        }

        if ( dbType.equals( "postgis" ) ) {
            if ( dbPort == null || dbPort.isEmpty() ) {
                dbPort = "5432";
            }

            List<String> hostList = null;
            List<String> portList = null;
            
            int hostCount = dbHost.split(",").length;
            int portCount = dbPort.split(",").length;
            
        	String haConn = "";

            if ((hostCount > 1)&&(portCount > 1)&&(hostCount == portCount)&&(haConn.length()<1)) {
            	hostList = Arrays.asList(dbHost.split(","));
            	portList = Arrays.asList(dbPort.split(","));

                for (int i=0; i<hostList.size(); i++) {
                	String strHost = hostList.get(i);
                	String strPort = portList.get(i);

                	if (strHost.contains(":"))
                		strHost = strHost.substring(0, strHost.indexOf(":"));

                	if (strPort.contains(":"))
                		strPort = strPort.substring(strPort.indexOf(":")+1, strPort.length());
                	
                	haConn += (strHost + ":" + strPort + ",");
                }
                haConn = haConn.substring(0, haConn.length()-1);

                dbConn = "jdbc:postgresql://" + haConn + "/" + dbName;
            } else {
            	dbConn = "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName;	
            }

            return;
        }

        if ( dbType.equals( "altibase" ) ) {
        	if ( dbPort == null || dbPort.isEmpty() ) {
                dbPort = "20300";
            }
        	dbConn = "jdbc:Altibase://" + dbHost + ":" + dbPort + "/" + dbName;
            return;
        }
        if ( dbType.equals( "tibero" ) ) {
        	/*
        	if ( dbPort == null || dbPort.isEmpty() ) {
        		dbPort = "8629";
        	}
            dbConn = "jdbc:tibero:thin:@" + dbHost + ":" + dbPort + ":" + dbName;
            return;
            */
        	if ( dbPort == null || dbPort.isEmpty() ) {
                dbPort = "8629";
            }

            List<String> hostList = null;
            List<String> portList = null;
            
            int hostCount = dbHost.split(",").length;
            int portCount = dbPort.split(",").length;
            
        	String haConn = "";

            if ((hostCount > 1)&&(portCount > 1)&&(hostCount == portCount)&&(haConn.length()<1)) {
            	hostList = Arrays.asList(dbHost.split(","));
            	portList = Arrays.asList(dbPort.split(","));

                for (int i=0; i<hostList.size(); i++) {
                	String strHost = hostList.get(i);
                	String strPort = portList.get(i);

                	if (strHost.contains(":"))
                		strHost = strHost.substring(0, strHost.indexOf(":"));

                	if (strPort.contains(":"))
                		strPort = strPort.substring(strPort.indexOf(":")+1, strPort.length());
                	
                	haConn += (strHost + ":" + strPort + ",");
                }
                haConn = haConn.substring(0, haConn.length()-1);

                dbConn = "jdbc:tibero:thin:@" + haConn + ":" + dbName;
            } else {
            	dbConn = "jdbc:tibero:thin:@" + dbHost + ":" + dbPort + ":" + dbName;	
            }
            
            //dbConn = "jdbc:tibero:thin:@(description=(failover=on)(load_balance=on)(address_list=(address=(host=localhost)(port=8629))(address=(host=localhost)(port=8629)))(DATABASE_NAME=tibero))";

            return;        	
        }
    }

    public String editAsXml()
                            throws IOException {
        create();
        if ( config != null ) {
            return config.edit();
        }
       
        return null;
    }
    
    
    public String editJdbcWizard() throws IOException {
    	Config param = (Config) getParam1();
    	File location = param.getLocation();

    	if ( !location.exists() ) {
    		return "/console/jdbc/index";
    	}
    	
		try {
			String filePath = location.getAbsolutePath();
			readDbInfoFromFile(filePath);
			
			FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put( "newConfigId", param.getId() );
			FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put( "jdbcBean", this );
			FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put( "config", param );
			
        } catch ( Exception e ) {
        	throw new IllegalArgumentException(e.getMessage());
        }
		
		return "/console/jdbc/edit?faces-redirect=true";
	}
    
    public String edit() throws IOException {
    	ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
    	WorkspaceBean wb = (WorkspaceBean) ctx.getApplicationMap().get( "workspace" );
        DeegreeWorkspace ws = wb.getActiveWorkspace();
        JDBCParamsManager mgr = ws.getSubsystemManager( JDBCParamsManager.class );
        Map<String, Object> sMap = ctx.getSessionMap();
        String newId = (String) sMap.get( "newConfigId" );
        mgr.deleteResource(newId);
        
		create();
		clearFields();
		
		return "/console/jdbc/index?faces-redirect=true";
	}

    public void testConnection() throws NamingException {
        ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, Object> sMap = ctx.getSessionMap();
        String newId = (String) sMap.get( "newConfigId" );
        
        Connection conn = null;
        try {
        	if(dbType.contains("jeusCP")){
            	LOG.warn("---@@ jeus Connection Pool -------------------");
    			InitialContext initCtx = new InitialContext();
    			String cpName = newId.substring(0, newId.indexOf("."));
    			DataSource ds = (DataSource)initCtx.lookup(cpName);
				conn = ds.getConnection();
            }
        	else if(dbConn.startsWith("jdbc:Altibase:")){
        		Class.forName("Altibase5.jdbc.driver.AltibaseDriver");
        		Class.forName("Altibase.jdbc.driver.AltibaseDriver");
            	conn = DriverManager.getConnection( dbConn, dbUser, dbPwd );
        	}
        	else if(dbConn.startsWith("jdbc:tibero:")){
        		LOG.warn("---@@ tibero Driver -------------------");
        		Class.forName("com.tmax.tibero.jdbc.TbDriver");
        		Class.forName("com.tmax.tibero.jdbc.ext.TbConnectionPoolDataSource");
            	conn = DriverManager.getConnection( dbConn, dbUser, dbPwd );
        	}
        	
            FacesMessage fm = new FacesMessage( SEVERITY_INFO, "'" + newId + "' 연결 성공", null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
        } catch ( SQLException e ) {
            FacesMessage fm = new FacesMessage( SEVERITY_ERROR, "'" + newId + "' 연결을 사용할 수 없습니다(오류내용: " + e.getMessage() + ")", null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
        } catch ( ClassNotFoundException e ) {
            FacesMessage fm = new FacesMessage( SEVERITY_ERROR, "'" + newId + "' 연결을 사용할 수 없습니다(오류내용: " + e.getMessage() + ")", null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
		} finally {
            if ( conn != null ) {
                try {
                    conn.close();
                } catch ( SQLException e ) {
                    // nothing to do
                }
            }
        }
    }

    public void cancel() {
        ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
        DeegreeWorkspace ws = ( (WorkspaceBean) ctx.getApplicationMap().get( "workspace" ) ).getActiveWorkspace();
        JDBCParamsManager mgr = ws.getSubsystemManager( JDBCParamsManager.class );
        Map<String, Object> sMap = ctx.getSessionMap();
        String newId = (String) sMap.get( "newConfigId" );
        mgr.deleteResource( newId );
        clearFields();
    }

    private void clearFields() {
        dbType = "mssql";
        dbPort = "1433";
        dbHost = null;
        dbName = null;
        dbConn = null;
        dbUser = null;
        dbPwd = null;
    }

    public String save() {
        create();
        clearFields();
        return "/console/jdbc/index";
    }

    private void create() {
        ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
        DeegreeWorkspace ws = ( (WorkspaceBean) ctx.getApplicationMap().get( "workspace" ) ).getActiveWorkspace();
        JDBCParamsManager mgr = ws.getSubsystemManager( JDBCParamsManager.class );
        
        StringBuffer sb = new StringBuffer();
        sb.append( "<?xml version='1.0' encoding='UTF-8'?>\n" );
        sb.append( "<JDBCConnection configVersion='3.0.0'  xmlns='http://www.deegree.org/jdbc' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.deegree.org/jdbc http://schemas.deegree.org/jdbc/3.0.0/jdbc.xsd'>\n" );
        sb.append( "  <Url>" + dbConn + "</Url>\n" );
        sb.append( "  <User>" + dbUser + "</User>\n" );
        sb.append( "  <Password>" + dbPwd + "</Password>\n" );
        sb.append( "  <ReadOnly>false</ReadOnly>\n" );
        sb.append( "</JDBCConnection>\n" );
        ResourceState rs = null;
        InputStream is = null;
        try {
            is = new ByteArrayInputStream( sb.toString().getBytes( "UTF-8" ) );
            Map<String, Object> sMap = ctx.getSessionMap();
            String newId = (String) sMap.get( "newConfigId" );
            rs = mgr.createResource( newId, is );
            rs = mgr.activate( rs.getId() );
            ResourceManagerMetadata2 rsMetadata = (ResourceManagerMetadata2) sMap.get( "resourceManagerMetadata" );
            this.config = new Config( rs, (ConfigManager) sMap.get( "configManager" ), mgr, rsMetadata.getStartView(), false );
            
            ConnectionManager poolMgr = ws.getSubsystemManager( ConnectionManager.class );
            poolMgr.shutdown();
            
            // poolMgr.shutdown();를 실행하면서 모든 sql Driver를 반환해버림. 다시 로드...
            mgr.startup(ws);
            try{
            	new AltibaseDriverManager().startup();
            }catch(Exception ee){}
            try{
            	new TiberoDriverManager().startup();
            }catch(Exception ee){}
            
            poolMgr.startup( ws );
        } catch ( Throwable t ) {
//            FacesMessage fm = new FacesMessage( SEVERITY_ERROR, "Unable to create config: " + t.getMessage(), null );
        	FacesMessage fm = new FacesMessage( SEVERITY_ERROR, "config를 생성할 수 없습니다(오류내용: " + t.getMessage() + ")", null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
        }
    }
    
    private void readDbInfoFromFile(String filePath){
    	try{
	    	File file = new File(filePath);
			JAXBContext jaxbContext = JAXBContext.newInstance(JDBCConnection.class);
	 
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			JDBCConnection ts = (JDBCConnection) jaxbUnmarshaller.unmarshal(file);
			
			String url = ts.getUrl();
			String db_driver = "";
			String db_type = "";
			String db_host = "";
			String db_port = "";
			String db_name = "";
			
			if (url.contains(":") && url.length()>5) {
				db_driver = url.substring(url.indexOf("jdbc:")+5, url.indexOf(":",url.indexOf("jdbc:")+5));
			} else {
				db_driver = "jeusCP";
			}
			
			if (db_driver.equals("oracle")&&(url.indexOf("thin:@//") >0)) {
				db_driver = "oracleService";
			}
			if("sqlserver".equals(db_driver)){
				db_type = "mssql";
				db_host = url.substring(url.indexOf("//")+2, url.lastIndexOf(":"));
				db_name = url.substring(url.lastIndexOf("=")+1);
				db_port = url.substring(url.indexOf(":",url.indexOf(db_host))+1, url.indexOf(";"));
			} else if("oracle".equals(db_driver)){
				db_type = "oracle";
				// dbConn = "jdbc:oracle:thin:@" + dbHost + ":" + dbPort + ":" + dbName;
				db_host = url.substring(url.indexOf("@")+1, url.indexOf(":", url.indexOf("@")+1));
				db_name = url.substring(url.lastIndexOf(":")+1);
				db_port = url.substring(url.indexOf(":",url.indexOf(db_host))+1, url.lastIndexOf(":"));
			} else if("oracleService".equals(db_driver)){
				db_type = "oracleService";
				//jdbc:oracle:thin:@//106.244.149.68:1521/infodb
				db_host = url.substring(url.indexOf("//")+2, url.indexOf(":", url.indexOf("//")+1));
				db_name = url.substring(url.lastIndexOf("/")+1);
				db_port = url.substring(url.indexOf(":",url.indexOf(db_host))+1, url.lastIndexOf("/"));
			} else if("jeusCP".equals(db_driver)){
				db_type = "jeusCP";
				db_name = "NA";
				db_host = "NA";
				db_port = "0";
			} else if("postgresql".equals(db_driver)){
				db_type = "postgis";
				db_host = url.substring(url.indexOf("//")+2, url.lastIndexOf(":"));
				db_name = url.substring(url.lastIndexOf("/")+1);
				db_port = url.substring(url.indexOf(":",url.indexOf(db_host))+1, url.indexOf("/",url.indexOf(db_host)));
			} else if("Altibase".equals(db_driver)){
				db_type = "altibase";
				db_host = url.substring(url.indexOf("//")+2, url.lastIndexOf(":"));
				db_name = url.substring(url.lastIndexOf("/")+1);
				db_port = url.substring(url.indexOf(":",url.indexOf(db_host))+1, url.indexOf("/",url.indexOf(db_host)));
			} else if("tibero".equals(db_driver)){
				db_type = "tibero";
				db_host = url.substring(url.indexOf("@")+1, url.indexOf(":", url.indexOf("@")+1));
				db_name = url.substring(url.lastIndexOf(":")+1);
				db_port = url.substring(url.indexOf(":",url.indexOf(db_host))+1, url.lastIndexOf(":"));	
			}
			
			this.dbType = db_type;
			this.dbHost = db_host;
			this.dbPort = db_port;
			this.dbName = db_name;
			this.dbUser = ts.getUser();
			this.dbPwd = ts.getPassword();
			update();
			
    	} catch ( Exception e ) {
        	throw new IllegalArgumentException(e.getMessage());
    	}
    }

}
