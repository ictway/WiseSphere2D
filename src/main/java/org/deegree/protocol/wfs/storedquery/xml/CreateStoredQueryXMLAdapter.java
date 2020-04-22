//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.protocol.wfs.storedquery.xml;

import static org.deegree.protocol.wfs.WFSConstants.VERSION_200;

import java.util.ArrayList;
import java.util.List;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.xml.XPath;
import org.deegree.protocol.i18n.Messages;
import org.deegree.protocol.wfs.AbstractWFSRequestXMLAdapter;
import org.deegree.protocol.wfs.storedquery.CreateStoredQuery;
import org.deegree.protocol.wfs.storedquery.StoredQueryDefinition;

/**
 * Adapter between XML <code>CreateStoredQuery</code> requests and {@link CreateStoredQuery} objects.
 * <p>
 * Supported WFS versions:
 * <ul>
 * <li>2.0.0</li>
 * </ul>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class CreateStoredQueryXMLAdapter extends AbstractWFSRequestXMLAdapter {

    /**
     * Parses a WFS <code>CreateStoredQuery</code> document into a {@link CreateStoredQuery} request.
     * 
     * @return parsed {@link CreateStoredQuery} request, never <code>null</code>
     * @throws InvalidParameterValueException
     *             if a parameter contains a syntax error
     */
    public CreateStoredQuery parse()
                            throws InvalidParameterValueException {

        // <xsd:attribute name="version" type="xsd:string" use="required" fixed="2.0.0"/>
        Version version = Version.parseVersion( getRequiredNodeAsString( rootElement, new XPath( "@version", nsContext ) ) );
        if ( !( VERSION_200.equals( version ) ) ) {
            String msg = Messages.get( "UNSUPPORTED_VERSION", version, Version.getVersionsString( VERSION_200 ) );
            throw new InvalidParameterValueException( msg );
        }

        // <xsd:attribute name="handle" type="xsd:string"/>
        String handle = getNodeAsString( rootElement, new XPath( "@handle", nsContext ), null );

        // <xsd:element name="StoredQueryDefinition" type="wfs:StoredQueryDescriptionType" minOccurs="0"
        // maxOccurs="unbounded"/>
        List<OMElement> els = getElements( rootElement, new XPath( "wfs200:StoredQueryDefinition", nsContext ) );
        List<StoredQueryDefinition> queryDefinitions = new ArrayList<StoredQueryDefinition>();
        for ( OMElement el : els ) {
            StoredQueryDefinitionXMLAdapter queryDefAdapter = new StoredQueryDefinitionXMLAdapter();
            queryDefAdapter.setRootElement( el );
            queryDefinitions.add( queryDefAdapter.parse() );
        }

        return new CreateStoredQuery( version, handle, queryDefinitions );
    }
}
