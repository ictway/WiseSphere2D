//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package com.ictway.wisesphere.services.wms.controller;

import static java.awt.Color.black;
import static java.awt.Color.decode;
import static java.awt.Color.white;
import static java.lang.Integer.parseInt;
import static java.util.Arrays.asList;
import static javax.xml.stream.XMLOutputFactory.IS_REPAIRING_NAMESPACES;
import static org.deegree.commons.utils.math.MathUtils.round;
import static org.deegree.services.i18n.Messages.get;
import static org.deegree.style.utils.ImageUtils.prepareImage;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;

import javax.servlet.ServletException;
import javax.xml.stream.XMLOutputFactory;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.ows.metadata.ServiceIdentification;
import org.deegree.commons.ows.metadata.ServiceProvider;
import org.deegree.protocol.wms.WMSConstants.WMSRequestType;
import org.deegree.services.controller.exception.serializer.XMLExceptionSerializer;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.metadata.OWSMetadataProvider;

import com.ictway.wisesphere.services.wms.MapService;
import com.ictway.wisesphere.services.wms.controller.WMSController.Controller;

/**
 * <code>WMSControllerBase</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public abstract class WMSControllerBase implements Controller {

    protected String EXCEPTION_DEFAULT, EXCEPTION_INIMAGE, EXCEPTION_BLANK;

    protected XMLExceptionSerializer exceptionSerializer;

    protected String EXCEPTION_MIME = "text/xml";

    @Override
    public void handleException( Map<String, String> map, WMSRequestType req, OWSException e,
                                 HttpResponseBuffer response, WMSController controller )
                            throws ServletException {
        String exceptions = map.get( "EXCEPTIONS" );
        switch ( req ) {
        case DescribeLayer:
        case GetCapabilities:
        case capabilities:
        case GetFeatureInfo:
        case GetFeatureInfoSchema:
        case GetStyles:
        case PutStyles:
            exceptions = "XML";
            break;
        case map:
        case GetMap:
        case GetLegendGraphic:
            exceptions = getExceptions( exceptions );
            break;
        case DTD:
            break;
        }

        checkParameters( map, e, response, exceptions, controller );
    }

    private String getExceptions( String exceptions ) {
        return exceptions == null ? EXCEPTION_DEFAULT : exceptions;
    }

    private void checkParameters( Map<String, String> map, OWSException e, HttpResponseBuffer response,
                                  String exceptions, WMSController controller )
                            throws ServletException {
        try {
            int width = Integer.parseInt( map.get( "WIDTH" ) );
            int height = Integer.parseInt( map.get( "HEIGHT" ) );
            boolean transparent = map.get( "TRANSPARENT" ) != null
                                  && map.get( "TRANSPARENT" ).equalsIgnoreCase( "true" );
            String format = map.get( "FORMAT" );
            Color color = map.get( "BGCOLOR" ) == null ? white : decode( map.get( "BGCOLOR" ) );
            sendException( e, response, exceptions, width, height, color, transparent, format, controller );
        } catch ( NumberFormatException _ ) {
            sendException( e, response, controller );
        }
    }

    private void sendException( OWSException ex, HttpResponseBuffer response, String type, int width, int height,
                                Color color, boolean transparent, String format, WMSController controller )
                            throws ServletException {
        if ( type.equalsIgnoreCase( EXCEPTION_DEFAULT ) ) {
            controller.sendException( null, exceptionSerializer, ex, response );
        } else if ( type.equalsIgnoreCase( EXCEPTION_INIMAGE ) ) {
            BufferedImage img = prepareImage( format, width, height, transparent, color );
            Graphics2D g = img.createGraphics();
            g.setColor( black );
            LinkedList<String> words = new LinkedList<String>( asList( ex.getMessage().split( "\\s" ) ) );
            String text = words.poll();
            TextLayout layout = new TextLayout( ex.getMessage(), g.getFont(), g.getFontRenderContext() );
            int pos = round( layout.getBounds().getHeight() );
            while ( words.size() > 0 ) {
                layout = new TextLayout( text + " " + words.peek(), g.getFont(), g.getFontRenderContext() );
                if ( layout.getBounds().getWidth() > width ) {
                    g.drawString( text, 0, pos );
                    text = words.poll();
                    pos += layout.getBounds().getHeight() * 1.5;
                    continue;
                }
                text += " " + words.poll();
            }
            g.drawString( text, 0, pos );
            g.dispose();
            try {
                controller.sendImage( img, response, format );
            } catch ( OWSException e ) {
                controller.sendException( null, exceptionSerializer, ex, response );
            } catch ( IOException e ) {
                controller.sendException( null, exceptionSerializer, ex, response );
            }
        } else if ( type.equalsIgnoreCase( EXCEPTION_BLANK ) ) {
            BufferedImage img = prepareImage( format, width, height, transparent, color );
            try {
                controller.sendImage( img, response, format );
            } catch ( OWSException e ) {
                controller.sendException( null, exceptionSerializer, ex, response );
            } catch ( IOException e ) {
                controller.sendException( null, exceptionSerializer, ex, response );
            }
        } else {
            controller.sendException( null, exceptionSerializer, ex, response );
        }
    }

    @Override
    public void getCapabilities( String getUrl, String postUrl, String updateSequence, MapService service,
                                 HttpResponseBuffer response, ServiceIdentification identification,
                                 ServiceProvider provider, Map<String, String> customParameters,
                                 WMSController controller, OWSMetadataProvider metadata )
                            throws OWSException, IOException {
        getUrl = getUrl.substring( 0, getUrl.length() - 1 );
// BY_JIN
//      postUrl = postUrl.substring( 0, getUrl.length() - 1 );
        if ( updateSequence != null && updateSequence.trim().length() > 0 ) {
            try {
                int seq = parseInt( updateSequence );
                if ( seq > service.updateSequence ) {
                    throw new OWSException( get( "WMS.INVALID_UPDATE_SEQUENCE", updateSequence ),
                                            OWSException.INVALID_UPDATE_SEQUENCE );
                }
                if ( seq == service.updateSequence ) {
                    throw new OWSException( get( "WMS.CURRENT_UPDATE_SEQUENCE" ), OWSException.CURRENT_UPDATE_SEQUENCE );
                }
            } catch ( NumberFormatException e ) {
                throw new OWSException( get( "WMS.INVALID_UPDATE_SEQUENCE", updateSequence ),
                                        OWSException.INVALID_UPDATE_SEQUENCE );
            }
        }

        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        factory.setProperty( IS_REPAIRING_NAMESPACES, true );
        exportCapas( getUrl, postUrl, service, response, identification, provider, controller, metadata );
    }

    protected abstract void exportCapas( String getUrl, String postUrl, MapService service,
                                         HttpResponseBuffer response, ServiceIdentification identification,
                                         ServiceProvider provider, WMSController controller,
                                         OWSMetadataProvider metadata )
                            throws IOException;

}
