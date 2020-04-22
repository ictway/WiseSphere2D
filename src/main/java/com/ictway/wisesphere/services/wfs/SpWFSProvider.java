package com.ictway.wisesphere.services.wfs;

import java.net.URL;

import org.deegree.protocol.wfs.WFSRequestType;
import org.deegree.services.OWS;
import org.deegree.services.controller.ImplementationMetadata;
import org.deegree.services.wfs.WFSProvider;

public class SpWFSProvider extends WFSProvider {
 
    protected static final ImplementationMetadata<WFSRequestType> IMPLEMENTATION_METADATA = WFSProvider.IMPLEMENTATION_METADATA;
    
    @Override
    public OWS create( URL configURL ) {
        return new SpWebFeatureService( configURL, getImplementationMetadata() );
    }    

}