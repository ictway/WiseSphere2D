package com.ictway.wisesphere.services.custom.service;

import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.naming.NamingException;
import javax.xml.bind.JAXBException;

import com.google.gson.JsonObject;

public interface GeometryDBService {
	
	public JsonObject process();
	public void initFeatureConnection(String featureId) throws NamingException, SQLException, MalformedURLException, JAXBException;
	public ArrayList<String> getColumnList(String[] cList) throws Exception;
	public ArrayList<String> getColumnListFromDB(String layerId) throws Exception;
	public String getSql() throws Exception;
	public int updateFeature(String sql) throws Exception;
	public void closeConnection() throws SQLException;
	
}
