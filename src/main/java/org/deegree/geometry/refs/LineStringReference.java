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

package org.deegree.geometry.refs;

import org.deegree.commons.tom.gml.GMLReferenceResolver;
import org.deegree.geometry.primitive.LineString;

/**
 * The <code></code> class TODO add class documentation here.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class LineStringReference extends CurveReference<LineString> implements LineString {

    /**
     * Creates a new {@link LineStringReference} instance.
     * 
     * @param resolver
     *            used for resolving the reference, must not be <code>null</code>
     * @param uri
     *            the geometry's uri, must not be <code>null</code>
     * @param baseURL
     *            base URL for resolving the uri, may be <code>null</code> (no resolving of relative URLs)
     */
    public LineStringReference( GMLReferenceResolver resolver, String uri, String baseURL ) {
        super( resolver, uri, baseURL );
    }

    @Override
    public CurveType getCurveType() {
        return CurveType.LineString;
    }
}
