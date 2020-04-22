//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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

package com.ictway.wisesphere.services.wms.controller;

import static org.deegree.services.i18n.Messages.get;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.ows.metadata.ServiceIdentification;
import org.deegree.commons.ows.metadata.ServiceProvider;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.protocol.wms.Utils;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.metadata.OWSMetadataProvider;
import org.deegree.services.ows.PreOWSExceptionReportSerializer;

import com.ictway.wisesphere.services.wms.MapService;
import com.ictway.wisesphere.services.wms.controller.capabilities.Capabilities130XMLAdapter;

/**
 * <code>WMSController130</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WMSController130 extends WMSControllerBase {

    /**
     * 
     */
    public WMSController130() {
        EXCEPTION_DEFAULT = "XML";
        EXCEPTION_BLANK = "BLANK";
        EXCEPTION_INIMAGE = "INIMAGE";
        exceptionSerializer = new PreOWSExceptionReportSerializer( EXCEPTION_MIME );
    }

    @Override
    public void sendException( OWSException ex, HttpResponseBuffer response, WMSController controller )
                            throws ServletException {
        controller.sendException( null, exceptionSerializer, ex, response );
    }

    @Override
    public void throwSRSException( String name )
                            throws OWSException {
        throw new OWSException( get( "WMS.INVALID_SRS", name ), OWSException.INVALID_CRS );
    }

    /**
     * @param crs
     * @return a new CRS
     */
    public static ICRS getCRS( String crs ) {
        return CRSManager.getCRSRef( crs );
    }

    /**
     * @param crs
     * @param bbox
     * @return a new CRS
     */
    public static Envelope getCRSAndEnvelope( String crs, double[] bbox ) {
        if ( crs.startsWith( "AUTO2:" ) ) {
            String[] cs = crs.split( ":" )[1].split( "," );
            int id = Integer.parseInt( cs[0] );
            // this is not supported
            double factor = Double.parseDouble( cs[1] );
            double lon0 = Double.parseDouble( cs[2] );
            double lat0 = Double.parseDouble( cs[3] );

            return new GeometryFactory().createEnvelope( factor * bbox[0], factor * bbox[1], factor * bbox[2],
                                                         factor * bbox[3], Utils.getAutoCRS( id, lon0, lat0 ) );
        }
        return new GeometryFactory().createEnvelope( bbox[0], bbox[1], bbox[2], bbox[3], CRSManager.getCRSRef( crs ) );
    }

    @Override
    protected void exportCapas( String getUrl, String postUrl, MapService service, HttpResponseBuffer response,
                                ServiceIdentification identification, ServiceProvider provider,
                                WMSController controller, OWSMetadataProvider metadata )
                            throws IOException {
        response.setContentType( "text/xml" );
        try {
            XMLStreamWriter xmlWriter = response.getXMLWriter();
            new Capabilities130XMLAdapter( identification, provider, metadata, getUrl, postUrl, service, controller ).export( xmlWriter );
        } catch ( XMLStreamException e ) {
            throw new IOException( e );
        }
    }

}
