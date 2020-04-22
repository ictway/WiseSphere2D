package com.ictway.wisesphere.commons.jdbc.driver;

import static java.sql.DriverManager.registerDriver;

import java.net.URL;
import java.sql.Driver;

import org.deegree.commons.config.AbstractResourceManager;
import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.config.ResourceManagerMetadata;
import org.deegree.commons.config.ResourceProvider;
import org.deegree.commons.jdbc.DriverWrapper;


@SuppressWarnings("rawtypes")
public class AltibaseDriverManager extends AbstractResourceManager implements ResourceProvider {
	public void init(){
		startup();
	}
	
	public void startup(){
		try{
    		Driver d = new Altibase.jdbc.driver.AltibaseDriver();
    		registerDriver( new DriverWrapper( d ) );
    		System.out.println("***** Altibase Driver Load...");
    	} catch(Exception e){}
	}
/*
	@Override
	public ResourceManagerMetadata getMetadata() {
		// TODO Auto-generated method stub
		return null;
	}
*/
	@Override
	public void initMetadata(DeegreeWorkspace arg0) {
		startup();		
	}

	@Override
	public Class<? extends ResourceManager>[] getDependencies() {
		return new Class[] {};
	}

	@Override
	public String getConfigNamespace() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URL getConfigSchema() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResourceManagerMetadata getMetadata() {
		// TODO Auto-generated method stub
		return null;
	}

}