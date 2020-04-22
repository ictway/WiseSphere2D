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
package org.deegree.services.wfs.format.gml.request;

import static java.math.BigInteger.ZERO;
import static org.deegree.commons.ows.exception.OWSException.NO_APPLICABLE_CODE;
import static org.deegree.commons.ows.exception.OWSException.OPERATION_PROCESSING_FAILED;
import static org.deegree.commons.ows.exception.OWSException.OPTION_NOT_SUPPORTED;
import static org.deegree.commons.tom.datetime.ISO8601Converter.formatDateTime;
import static org.deegree.commons.xml.CommonNamespaces.GML3_2_NS;
import static org.deegree.commons.xml.CommonNamespaces.GMLNS;
import static org.deegree.commons.xml.stax.XMLStreamUtils.writeNamespaceIfNotBound;
import static org.deegree.gml.GMLOutputFactory.createGMLStreamWriter;
import static org.deegree.gml.GMLVersion.GEOJSON_1;
import static org.deegree.gml.GMLVersion.GML_2;
import static org.deegree.gml.GMLVersion.GML_32;
import static org.deegree.gml.GMLVersion.PBF_2;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_100;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_110;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_200;
import static org.deegree.protocol.wfs.WFSConstants.WFS_100_BASIC_SCHEMA_URL;
import static org.deegree.protocol.wfs.WFSConstants.WFS_110_SCHEMA_URL;
import static org.deegree.protocol.wfs.WFSConstants.WFS_200_NS;
import static org.deegree.protocol.wfs.WFSConstants.WFS_200_SCHEMA_URL;
import static org.deegree.protocol.wfs.WFSConstants.WFS_NS;
import static org.deegree.services.wfs.query.StoredQueryHandler.GET_FEATURE_BY_ID;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.tom.ResolveParams;
import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.datetime.DateTime;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.GenericFeatureCollection;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.lock.Lock;
import org.deegree.feature.persistence.lock.LockManager;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.persistence.simplesql.SimpleSQLFeatureStore;
import org.deegree.feature.property.GenericProperty;
import org.deegree.feature.property.SimpleProperty;
import org.deegree.feature.stream.FeatureInputStream;
import org.deegree.feature.types.FeatureType;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.projection.ProjectionClause;
import org.deegree.filter.projection.PropertyName;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.standard.multi.DefaultMultiLineString;
import org.deegree.geometry.standard.multi.DefaultMultiPoint;
import org.deegree.geometry.standard.multi.DefaultMultiPolygon;
import org.deegree.geometry.standard.primitive.DefaultPoint;
import org.deegree.geometry.standard.primitive.DefaultPolygon;
import org.deegree.gml.GMLStreamWriter;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.reference.GmlXlinkOptions;
import org.deegree.protocol.wfs.getfeature.GetFeature;
import org.deegree.protocol.wfs.getfeaturewithlock.GetFeatureWithLock;
import org.deegree.protocol.wfs.query.StandardPresentationParams;
import org.deegree.protocol.wfs.query.StoredQuery;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.i18n.Messages;
import org.deegree.services.wfs.WebFeatureService;
import org.deegree.services.wfs.WfsFeatureStoreManager;
import org.deegree.services.wfs.format.gml.BufferableXMLStreamWriter;
import org.deegree.services.wfs.format.gml.GmlFormat;
import org.deegree.services.wfs.query.QueryAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.io.geojson.GeoJsonWriter;

import no.ecc.vectortile.VectorTileEncoder;


/**
 * Handles {@link GetFeature} and {@link GetFeatureWithLock} requests for the
 * {@link GmlFormat}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GmlGetFeatureHandler extends AbstractGmlRequestHandler {

	private static final Logger LOG = LoggerFactory.getLogger(GmlGetFeatureHandler.class);

	/**
	 * Creates a new {@link GmlGetFeatureHandler} instance.
	 * 
	 * @param gmlFormat
	 *            never <code>null</code>
	 */
	public GmlGetFeatureHandler(GmlFormat format) {
		super(format);
	}

	/**
	 * Performs the given {@link GetFeature} request.
	 * 
	 * @param request
	 *            request to be handled, never <code>null</code>
	 * @param response
	 *            response that is used to write the result, never
	 *            <code>null</code>
	 */
	public void doGetFeatureResults(GetFeature request, HttpResponseBuffer response) throws Exception {
		///@@
		LOG.info(">>>>>> Wise Sphere Start : doGetFeatureResults ");
		LOG.debug("Performing GetFeature (results) request.");

		StandardPresentationParams spp = request.getPresentationParams();
		String outputFormat = spp.getOutputFormat();

		GMLVersion gmlVersion = options.getGmlVersion();

		QueryAnalyzer analyzer = new QueryAnalyzer(request.getQueries(), format.getMaster(),
				format.getMaster().getStoreManager(), options.isCheckAreaOfUse());
		Lock lock = acquireLock(request, analyzer);

		String schemaLocation = getSchemaLocation(request.getVersion(), analyzer.getFeatureTypes());

		int traverseXLinkDepth = 0;
		BigInteger resolveTimeout = null;
		String xLinkTemplate = getObjectXlinkTemplate(request.getVersion(), gmlVersion);

		if (VERSION_110.equals(request.getVersion()) || VERSION_200.equals(request.getVersion())) {
			if (request.getResolveParams().getDepth() != null) {
				if ("*".equals(request.getResolveParams().getDepth())) {
					traverseXLinkDepth = -1;
				} else {
					try {
						traverseXLinkDepth = Integer.parseInt(request.getResolveParams().getDepth());
					} catch (NumberFormatException e) {
						String msg = Messages.get("WFS_TRAVERSEXLINKDEPTH_INVALID",
								request.getResolveParams().getDepth());
						throw new OWSException(new InvalidParameterValueException(msg));
					}
				}
			}
			if (request.getResolveParams().getTimeout() != null) {
				resolveTimeout = request.getResolveParams().getTimeout();
				// needed for CITE 1.1.0 compliance
				// (wfs:GetFeature-traverseXlinkExpiry)
				if (resolveTimeout == null || resolveTimeout.equals(ZERO)) {
					String msg = Messages.get("WFS_TRAVERSEXLINKEXPIRY_ZERO", resolveTimeout);
					throw new OWSException(new InvalidParameterValueException(msg));
				}
			}
		}

		if ((outputFormat!=null)&&(outputFormat.equals("pbf"))) {
			response.setContentType("application/pbf");
			ServletOutputStream sos = response.getOutputStream();

			writeFeatureMembersStreamPbf(request.getVersion(), sos, analyzer);
		} else if ((outputFormat!=null)&&(outputFormat.equals("json"))) {
			response.setContentType("application/json");
			ServletOutputStream sos = response.getOutputStream();

			writeFeatureMembersStreamJson(request.getVersion(), sos, analyzer);
		} else {
			// quick check if local references in the output can be ruled out
			boolean localReferencesPossible = localReferencesPossible(analyzer, traverseXLinkDepth);

			String contentType = options.getMimeType();
			XMLStreamWriter xmlStream = WebFeatureService.getXMLResponseWriter(response, contentType, schemaLocation);
			xmlStream = new BufferableXMLStreamWriter(xmlStream, xLinkTemplate);

			QName memberElementName = determineFeatureMemberElement(request.getVersion());

			QName responseContainerEl = options.getResponseContainerEl();

			boolean isGetFeatureById = isGetFeatureByIdRequest(request);

			// open "wfs:FeatureCollection" element
			if (request.getVersion().equals(VERSION_100)) {
				if (responseContainerEl != null) {
					xmlStream.setPrefix(responseContainerEl.getPrefix(), responseContainerEl.getNamespaceURI());
					xmlStream.writeStartElement(responseContainerEl.getNamespaceURI(),
							responseContainerEl.getLocalPart());
					xmlStream.writeNamespace(responseContainerEl.getPrefix(), responseContainerEl.getNamespaceURI());
				} else {
					xmlStream.setPrefix("wfs", WFS_NS);
					xmlStream.writeStartElement(WFS_NS, "FeatureCollection");
					xmlStream.writeNamespace("wfs", WFS_NS);
					if (lock != null) {
						xmlStream.writeAttribute("lockId", lock.getId());
					}
				}
			} else if (request.getVersion().equals(VERSION_110)) {
				if (responseContainerEl != null) {
					xmlStream.setPrefix(responseContainerEl.getPrefix(), responseContainerEl.getNamespaceURI());
					xmlStream.writeStartElement(responseContainerEl.getNamespaceURI(),
							responseContainerEl.getLocalPart());
					xmlStream.writeNamespace(responseContainerEl.getPrefix(), responseContainerEl.getNamespaceURI());
				} else {
					xmlStream.setPrefix("wfs", WFS_NS);
					xmlStream.writeStartElement(WFS_NS, "FeatureCollection");
					xmlStream.writeNamespace("wfs", WFS_NS);
					if (lock != null) {
						xmlStream.writeAttribute("lockId", lock.getId());
					}
					xmlStream.writeAttribute("timeStamp", getTimestamp());
				}
			} else if (request.getVersion().equals(VERSION_200) && (!isGetFeatureById)) {
				xmlStream.setPrefix("wfs", WFS_200_NS);
				xmlStream.writeStartElement(WFS_200_NS, "FeatureCollection");
				xmlStream.writeNamespace("wfs", WFS_200_NS);
				xmlStream.writeAttribute("timeStamp", getTimestamp());
				if (lock != null) {
					xmlStream.writeAttribute("lockId", lock.getId());
				}
			}

			if (!isGetFeatureById) {
				// ensure that namespace for feature member elements is bound
				writeNamespaceIfNotBound(xmlStream, memberElementName.getPrefix(), memberElementName.getNamespaceURI());

				// ensure that namespace for gml (e.g. geometry elements) is
				// bound
				writeNamespaceIfNotBound(xmlStream, "gml", gmlVersion.getNamespace());

				if (GML_32 == gmlVersion && !request.getVersion().equals(VERSION_200)) {
					xmlStream.writeAttribute("gml", GML3_2_NS, "id", "WFS_RESPONSE");
				}
			}

			int returnMaxFeatures = options.getQueryMaxFeatures();
			if (request.getPresentationParams().getCount() != null && (options.getQueryMaxFeatures() < 1
					|| request.getPresentationParams().getCount().intValue() < options.getQueryMaxFeatures())) {
				returnMaxFeatures = request.getPresentationParams().getCount().intValue();
			}

			int startIndex = 0;
			if (request.getPresentationParams().getStartIndex() != null) {
				startIndex = request.getPresentationParams().getStartIndex().intValue();
			}

			GMLStreamWriter gmlStream = createGMLStreamWriter(gmlVersion, xmlStream);
			gmlStream.setProjections(analyzer.getProjections());
			gmlStream.setOutputCrs(analyzer.getRequestedCRS());
			gmlStream.setCoordinateFormatter(options.getFormatter());
			gmlStream.setGenerateBoundedByForFeatures(options.isGenerateBoundedByForFeatures());
			Map<String, String> prefixToNs = new HashMap<String, String>(
					format.getMaster().getStoreManager().getPrefixToNs());
			prefixToNs.putAll(getFeatureTypeNsPrefixes(analyzer.getFeatureTypes()));
			gmlStream.setNamespaceBindings(prefixToNs);
			GmlXlinkOptions resolveOptions = new GmlXlinkOptions(request.getResolveParams());
			WfsXlinkStrategy additionalObjects = new WfsXlinkStrategy((BufferableXMLStreamWriter) xmlStream,
					localReferencesPossible, xLinkTemplate, resolveOptions);
			gmlStream.setReferenceResolveStrategy(additionalObjects);

			if (isGetFeatureById) {
				writeSingleFeatureMember(gmlStream, analyzer, resolveOptions);
			} else if (options.isDisableStreaming()) {
				writeFeatureMembersCached(request.getVersion(), gmlStream, analyzer, gmlVersion, returnMaxFeatures,
						startIndex, memberElementName, lock);
			} else {
				writeFeatureMembersStream(request.getVersion(), gmlStream, analyzer, gmlVersion, returnMaxFeatures,
						startIndex, memberElementName, lock);
			}

			if (!isGetFeatureById) {
				writeAdditionalObjects(gmlStream, additionalObjects, memberElementName, request.getVersion());

				// close container element
				xmlStream.writeEndElement();
			}
			xmlStream.flush();

			// append buffered parts of the stream
			if (((BufferableXMLStreamWriter) xmlStream).hasBuffered()) {
				((BufferableXMLStreamWriter) xmlStream).appendBufferedXML(gmlStream);
			}
		}
		LOG.info(">>>>>> Wise Sphere End : doGetFeatureResults ");
		
	}

	public void doGetFeatureHits(GetFeature request, HttpResponseBuffer response)
			throws OWSException, XMLStreamException, IOException, FeatureStoreException, FilterEvaluationException {

		LOG.debug("Performing GetFeature (hits) request.");

		QueryAnalyzer analyzer = new QueryAnalyzer(request.getQueries(), format.getMaster(),
				format.getMaster().getStoreManager(), options.isCheckAreaOfUse());
		Lock lock = acquireLock(request, analyzer);
		String schemaLocation = null;
		if (VERSION_100.equals(request.getVersion())) {
			schemaLocation = WFS_NS + " " + WFS_100_BASIC_SCHEMA_URL;
		} else if (VERSION_110.equals(request.getVersion())) {
			schemaLocation = WFS_NS + " " + WFS_110_SCHEMA_URL;
		} else if (VERSION_200.equals(request.getVersion())) {
			schemaLocation = WFS_200_NS + " " + WFS_200_SCHEMA_URL;
		}

		String contentType = options.getMimeType();
		XMLStreamWriter xmlStream = WebFeatureService.getXMLResponseWriter(response, contentType, schemaLocation);

		Map<org.deegree.protocol.wfs.query.Query, Integer> wfsQueryToIndex = new HashMap<org.deegree.protocol.wfs.query.Query, Integer>();
		int i = 0;
		for (org.deegree.protocol.wfs.query.Query query : request.getQueries()) {
			wfsQueryToIndex.put(query, i++);
		}

		int hitsTotal = 0;
		int[] queryHits = new int[wfsQueryToIndex.size()];
		DateTime[] queryTimeStamps = new DateTime[queryHits.length];

		for (Map.Entry<FeatureStore, List<Query>> fsToQueries : analyzer.getQueries().entrySet()) {
			FeatureStore fs = fsToQueries.getKey();
			Query[] queries = fsToQueries.getValue().toArray(new Query[fsToQueries.getValue().size()]);
			int[] hits = fs.queryHits(queries);

			// map the hits from the feature store back to the original query
			// sequence
			for (int j = 0; j < hits.length; j++) {
				Query query = queries[j];
				int singleHits = hits[j];
				org.deegree.protocol.wfs.query.Query wfsQuery = analyzer.getQuery(query);
				int index = wfsQueryToIndex.get(wfsQuery);
				hitsTotal += singleHits;
				queryHits[index] = queryHits[index] + singleHits;
				queryTimeStamps[index] = getCurrentDateTimeWithoutMilliseconds();
			}
		}

		// open "wfs:FeatureCollection" element
		if (request.getVersion().equals(VERSION_100)) {
			xmlStream.setPrefix("wfs", WFS_NS);
			xmlStream.writeStartElement(WFS_NS, "FeatureCollection");
			xmlStream.writeNamespace("wfs", WFS_NS);
			if (lock != null) {
				xmlStream.writeAttribute("lockId", lock.getId());
			}
			xmlStream.writeAttribute("numberOfFeatures", "" + hitsTotal);
		} else if (request.getVersion().equals(VERSION_110)) {
			xmlStream.setPrefix("wfs", WFS_NS);
			xmlStream.writeStartElement(WFS_NS, "FeatureCollection");
			xmlStream.writeNamespace("wfs", WFS_NS);
			if (lock != null) {
				xmlStream.writeAttribute("lockId", lock.getId());
			}
			xmlStream.writeAttribute("timeStamp", getTimestamp());
			xmlStream.writeAttribute("numberOfFeatures", "" + hitsTotal);
		} else if (request.getVersion().equals(VERSION_200)) {
			xmlStream.setPrefix("wfs", WFS_200_NS);
			xmlStream.writeStartElement(WFS_200_NS, "FeatureCollection");
			xmlStream.writeNamespace("wfs", WFS_200_NS);
			xmlStream.writeAttribute("timeStamp", getTimestamp());
			xmlStream.writeAttribute("numberMatched", "" + hitsTotal);
			xmlStream.writeAttribute("numberReturned", "0");
			if (queryHits.length > 1) {
				for (int j = 0; j < queryHits.length; j++) {
					xmlStream.writeStartElement("wfs", "member", WFS_200_NS);
					xmlStream.writeEmptyElement("wfs", "FeatureCollection", WFS_200_NS);
					xmlStream.writeAttribute("timeStamp", formatDateTime(queryTimeStamps[j]));
					xmlStream.writeAttribute("numberMatched", "" + queryHits[j]);
					xmlStream.writeAttribute("numberReturned", "0");
					xmlStream.writeEndElement();
				}
			}
		}

		// "gml:boundedBy" is necessary for GML 2 schema compliance
		if (options.getGmlVersion().equals(GMLVersion.GML_2)) {
			xmlStream.writeStartElement("gml", "boundedBy", GMLNS);
			xmlStream.writeStartElement(GMLNS, "null");
			xmlStream.writeCharacters("unknown");
			xmlStream.writeEndElement();
			xmlStream.writeEndElement();
		}

		// close "wfs:FeatureCollection"
		xmlStream.writeEndElement();
		xmlStream.flush();
	}

	private void writeFeatureMembersStream(Version wfsVersion, GMLStreamWriter gmlStream, QueryAnalyzer analyzer,
			GMLVersion outputFormat, int maxFeatures, int startIndex, QName featureMemberEl, Lock lock)
			throws XMLStreamException, UnknownCRSException, TransformationException, FeatureStoreException,
			FilterEvaluationException, FactoryConfigurationError {
		//@@ ictway		
		LOG.info(">>>>> Wise Sphere : Start writeFeatureMembersStream@GmlGetFeatureHandler");		
		
		XMLStreamWriter xmlStream = gmlStream.getXMLStream();

		if (wfsVersion.equals(VERSION_200)) {
			xmlStream.writeAttribute("numberMatched", "unknown");
			xmlStream.writeAttribute("numberReturned", "0");
			xmlStream.writeComment(
					"NOTE: numberReturned attribute should be 'unknown' as well, but this would not validate against the current version of the WFS 2.0 schema (change upcoming). See change request (CR 144): https://portal.opengeospatial.org/files?artifact_id=43925.");
		}

		if (outputFormat == GML_2) {
			// "gml:boundedBy" is necessary for GML 2 schema compliance
			xmlStream.writeStartElement("gml", "boundedBy", GMLNS);
			xmlStream.writeStartElement(GMLNS, "null");
			xmlStream.writeCharacters("unknown");
			xmlStream.writeEndElement();
			xmlStream.writeEndElement();
		}
		// @@ ictway 2018.06.05
		else if (outputFormat == PBF_2) {
			xmlStream.writeStartElement("pbf", "boundedBy", "pbf");
			// xmlStream.writeStartElement( GMLNS, "null" );
			xmlStream.writeCharacters("unknown");
			xmlStream.writeEndElement();
			xmlStream.writeEndElement();
		}
		// @@ ictway 2019.03.27
		else if (outputFormat == GEOJSON_1) {
			xmlStream.writeStartElement("json", "boundedBy", "json");
			// xmlStream.writeStartElement( GMLNS, "null" );
			xmlStream.writeCharacters("unknown");
			xmlStream.writeEndElement();
			xmlStream.writeEndElement();
		}

		// retrieve and write result features
		int featuresAdded = 0;
		int featuresSkipped = 0;
//@@ ictway		
//LOG.info(">>>>> Wise Sphere Start gml writing");		
		GmlXlinkOptions resolveState = gmlStream.getReferenceResolveStrategy().getResolveOptions();
		for (Map.Entry<FeatureStore, List<Query>> fsToQueries : analyzer.getQueries().entrySet()) {
			FeatureStore fs = fsToQueries.getKey();
			Query[] queries = fsToQueries.getValue().toArray(new Query[fsToQueries.getValue().size()]);
			FeatureInputStream rs = fs.query(queries);

			// @@ ksjang Test 
			FeatureCollection fc = rs.toCollection();

			List<Property> list = fc.getProperties();
			for (int i = 0; i < list.size(); i++) {
				Property prop = list.get(i);
				//LOG.debug(prop.getName().toString() + ", " + prop.toString());
			}
			// @@

			try {
				for (Feature member : rs) {
					if (lock != null && !lock.isLocked(member.getId())) {
						continue;
					}
					if (featuresAdded == maxFeatures) {
						// limit the number of features written to maxfeatures
						break;
					}
					if (featuresSkipped < startIndex) {
						featuresSkipped++;
					} else {
						writeMemberFeature(member, gmlStream, xmlStream, resolveState, featureMemberEl);
						featuresAdded++;
					}
				}
			} finally {
	//@@ ictway		
	LOG.info(">>>>> Wise Sphere : End writeFeatureMembersStream@GmlGetFeatureHandler");		
				
				LOG.debug("Closing FeatureResultSet (stream)");
				rs.close();
			}
		}
//LOG.info(">>>>> Wise Sphere End gml writing");		
	}

	private void writeFeatureMembersStreamPbf(Version wfsVersion, ServletOutputStream sos, QueryAnalyzer analyzer)
			throws XMLStreamException, UnknownCRSException, TransformationException, FeatureStoreException,
			FilterEvaluationException, FactoryConfigurationError {
		Envelope env = null;

		VectorTileEncoder vte = new VectorTileEncoder();

		for (Map.Entry<FeatureStore, List<Query>> fsToQueries : analyzer.getQueries().entrySet()) {
			FeatureStore fs = fsToQueries.getKey();
			Query[] queries = fsToQueries.getValue().toArray(new Query[fsToQueries.getValue().size()]);
			FeatureInputStream rs = fs.query(queries);
			
			FeatureType ft = analyzer.getFeatureTypes().iterator().next();
			QName qn = ft.getName();
			env = fs.getEnvelope(qn);

			Coordinate p1;
			Coordinate p2;
			if (env == null) {
				// 'POLYGON ((13868378.000000 3916910.000000,14577581.000000
				// 3916910.000000,14577581.000000 4669832.000000,13868378.000000
				// 4669832.000000,13868378.000000 3916910.000000))'
				p1 = new Coordinate(13868378.000000, 3916910.000000);
				p2 = new Coordinate(14577581.000000, 4669832.000000);
			} else {
				p1 = new Coordinate(env.getMin().get0(), env.getMin().get1());
				p2 = new Coordinate(env.getMax().get0(), env.getMax().get1());
			}

			com.vividsolutions.jts.geom.Envelope jenv = new com.vividsolutions.jts.geom.Envelope(p1, p2);
			com.vividsolutions.jts.geom.GeometryFactory gf = new com.vividsolutions.jts.geom.GeometryFactory();
			com.vividsolutions.jts.geom.Geometry eg = gf.toGeometry(jenv);

			vte.setClipGeometry(eg);

			// @@ ksjang Test
			FeatureCollection fc = rs.toCollection();

			SimpleSQLFeatureStore sfs = (SimpleSQLFeatureStore) fs;
			String layerName = sfs.getFeatureType().getName().getLocalPart();

			Map<String, ?> map = new HashMap<String, Object>();
			Map<String, String> attrs = new HashMap<String, String>();

			com.vividsolutions.jts.geom.Geometry geom = null;

			List<Property> list = fc.getProperties();
			for (int i = 0; i < list.size(); i++) {
				Property prop = list.get(i);
				TypedObjectNode oNode = prop.getValue();

				if (oNode instanceof Feature) {
					Feature feature = (Feature) oNode;
					List<Property> property = feature.getProperties();
					for (int j = 0; j < property.size(); j++) {
						String sName = "";
						String value = "";

						if (property.get(j) instanceof GenericProperty) {
							GenericProperty p = (GenericProperty) property.get(j);
							TypedObjectNode ton = p.getValue();
							if (ton instanceof DefaultMultiPoint) {
								DefaultMultiPoint dmp = (DefaultMultiPoint) ton;
								geom = dmp.getJTSGeometry();
							}
							else if (ton instanceof DefaultPoint) {
								DefaultPoint dmp = (DefaultPoint) ton;
								geom = dmp.getJTSGeometry();
							}
							else if (ton instanceof DefaultMultiLineString) {
								DefaultMultiLineString dmp = (DefaultMultiLineString) ton;
								geom = dmp.getJTSGeometry();
							}
							else if (ton instanceof DefaultMultiPolygon) {
								DefaultMultiPolygon dmp = (DefaultMultiPolygon) ton;
								geom = dmp.getJTSGeometry();
							}
							else if (ton instanceof DefaultPolygon) {
								DefaultPolygon dmp = (DefaultPolygon) ton;
								geom = dmp.getJTSGeometry();
							}

						} else if (property.get(j) instanceof SimpleProperty) {
							SimpleProperty p = (SimpleProperty) property.get(j);
							sName = p.getName().getLocalPart();
							PrimitiveValue pv = p.getValue();
							value = pv.getAsText();
							attrs.put(sName, value);
						}
					}
				}
				@SuppressWarnings("unchecked")
				Map<String,?> newMap = (HashMap<String,String>)(Map)attrs;  
				vte.addFeature(layerName, newMap, geom);
			}
		}

		try {
			byte[] res = vte.encode();
			sos.write(res);
			sos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeFeatureMembersStreamJson(Version wfsVersion, ServletOutputStream sos, QueryAnalyzer analyzer)
			throws XMLStreamException, UnknownCRSException, TransformationException, FeatureStoreException,
			FilterEvaluationException, FactoryConfigurationError, IOException {
		Envelope env = null;
		String bbox = "";
		GeoJsonWriter gw = null;
		String geomString = "";
		StringBuilder propString = new StringBuilder();
		int rowIdx = 0;
		
		String sCRS = analyzer.getRequestedCRS().getAlias();
		if (sCRS.indexOf(":")>0) {
			sCRS = sCRS.split(":")[1];
		}
		
		for (Map.Entry<FeatureStore, List<Query>> fsToQueries : analyzer.getQueries().entrySet()) {
			FeatureStore fs = fsToQueries.getKey();
			Query[] queries = fsToQueries.getValue().toArray(new Query[fsToQueries.getValue().size()]);
			FeatureInputStream rs = fs.query(queries);
			
			FeatureType ft = analyzer.getFeatureTypes().iterator().next();
			QName qn = ft.getName();
			env = fs.getEnvelope(qn);
			
			Coordinate p1;
			Coordinate p2;
			if (env == null) {
				p1 = new Coordinate(13868378.000000, 3916910.000000);
				p2 = new Coordinate(14577581.000000, 4669832.000000);
			} else {
				p1 = new Coordinate(env.getMin().get0(), env.getMin().get1());
				p2 = new Coordinate(env.getMax().get0(), env.getMax().get1());
			}
			
			bbox = p1.x + "," + p1.y + "," + p2.x + "," + p2.y;

			String header = "{\"type\": \"FeatureCollection\",\"crs\": {\"type\": \"epsg\",\"properties\": {\"code\": \""+ sCRS+"\"}},\n" + 
					"\"bbox\": [" + bbox + "],\n" +  
					"\"features\": [";

			sos.write(header.getBytes());

			Iterator<Feature> itr = rs.iterator();
			while (itr.hasNext()) {
				Feature feature = itr.next();
				
				List<Property> property = feature.getProperties();
				com.vividsolutions.jts.geom.Geometry geom = null;
				
				propString.delete(0, propString.length());
				for (int j = 0; j < property.size(); j++) {
					String sName = "";
					String value = "";
					if (property.get(j) instanceof GenericProperty) {
						GenericProperty p = (GenericProperty) property.get(j);
						TypedObjectNode ton = p.getValue();
						
						if (ton instanceof DefaultMultiPoint) {
							DefaultMultiPoint dmp = (DefaultMultiPoint) ton;
							geom = dmp.getJTSGeometry();
						}
						else if (ton instanceof DefaultPoint) {
							DefaultPoint dmp = (DefaultPoint) ton;
							geom = dmp.getJTSGeometry();
						}
						else if (ton instanceof DefaultMultiLineString) {
							DefaultMultiLineString dmp = (DefaultMultiLineString) ton;
							geom = dmp.getJTSGeometry();
						}
						else if (ton instanceof DefaultPolygon) {
							DefaultPolygon dmp = (DefaultPolygon) ton;
							geom = dmp.getJTSGeometry();
						}
						else if (ton instanceof DefaultMultiPolygon) {
							DefaultMultiPolygon dmp = (DefaultMultiPolygon) ton;
							geom = dmp.getJTSGeometry();
						}
						
					} else if (property.get(j) instanceof SimpleProperty) {
						SimpleProperty p = (SimpleProperty) property.get(j);
						sName = p.getName().getLocalPart();
						PrimitiveValue pv = p.getValue();
						value = pv.getAsText();
						//propString += ",\"" + sName + "\":\"" + value + "\""; 
						propString.append(",\"");
						propString.append(sName);
						propString.append("\":\"");
						propString.append(value);
						propString.append("\"");
					}
				}
				//propString = propString.substring(1);
				propString.deleteCharAt(0);

				gw = new GeoJsonWriter();
				gw.setEncodeCRS(false);
				geomString = gw.write(geom);
				
				//flist.append(",{\"type\": \"Feature\",\"id\": \""+idx+"\",\"geometry\":" + geomString + ", \"properties\":{"+ propString+"}}");
				switch(rowIdx) {
				case 0:
					sos.write("{\"type\": \"Feature\",\"id\": \"".getBytes());
					break;
				default:
					sos.write(",{\"type\": \"Feature\",\"id\": \"".getBytes());
					break;
				}
				//sos.write(",{\"type\": \"Feature\",\"id\": \"".getBytes());
				sos.write(Integer.toString(rowIdx).getBytes());
				sos.write("\",\"geometry\":".getBytes());
				sos.write(geomString.getBytes());
				sos.write(", \"properties\":{".getBytes());
				sos.write(propString.toString().getBytes());
				sos.write("}}".getBytes());
				
				rowIdx++;
			}
			
			
			sos.flush();
			try {
			rs.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		try {
			//String res = "{\"type\": \"FeatureCollection\",\"crs\": {\"type\": \"epsg\",\"properties\": {\"code\": \""+ sCRS+"\"}},\n" + 
			//		"\"bbox\": [" + bbox + "],\n" +  
			//		"\"features\": [" + flist.substring(1) + "]}";
			String tail = "]}";
			sos.write(tail.getBytes());
			sos.flush();
			sos.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}	

	private void writeFeatureMembersCached(Version wfsVersion, GMLStreamWriter gmlStream, QueryAnalyzer analyzer,
			GMLVersion outputFormat, int maxFeatures, int startIndex, QName featureMemberEl, Lock lock)
			throws XMLStreamException, UnknownCRSException, TransformationException, FeatureStoreException,
			FilterEvaluationException, FactoryConfigurationError {

//LOG.info(">>>> Wise Sphere writeFeatureMembersCached #01");		
		FeatureCollection allFeatures = new GenericFeatureCollection();
		Set<String> fids = new HashSet<String>();

		// retrieve maxfeatures features
		int featuresAdded = 0;
		int featuresSkipped = 0;
		//LOG.info(">>>> Wise Sphere writeFeatureMembersCached #02");		
		for (Map.Entry<FeatureStore, List<Query>> fsToQueries : analyzer.getQueries().entrySet()) {
			FeatureStore fs = fsToQueries.getKey();
			Query[] queries = fsToQueries.getValue().toArray(new Query[fsToQueries.getValue().size()]);
			FeatureInputStream rs = fs.query(queries);
			try {
				//for (Feature feature : rs) {
				Iterator<Feature> itr = rs.iterator();

				//int idx = 0;
//LOG.info(">>>> Wise Sphere writeFeatureMembersCached #03-0");
//LOG.info(">>>> Wise Sphere writeFeatureMembersCached Count : " + Integer.toString(rs.count()));
				while (itr.hasNext()) {
					//LOG.info(">>>> Wise Sphere writeFeatureMembersCached #03-2->>" + Integer.toString(idx++));
					Feature feature = itr.next();

					if (lock != null && !lock.isLocked(feature.getId())) {
						continue;
					}
					if (featuresAdded == maxFeatures) {
						break;
					}
					if (featuresSkipped < startIndex) {
						featuresSkipped++;
					} else if (!fids.contains(feature.getId())) {
						allFeatures.add(feature);
						fids.add(feature.getId());
						featuresAdded++;
					}
				}
				//LOG.info(">>>> Wise Sphere writeFeatureMembersCached #04");				
			} finally {
				LOG.debug("Closing FeatureResultSet (cached)");
				rs.close();
			}
		}

		XMLStreamWriter xmlStream = gmlStream.getXMLStream();
		if (wfsVersion.equals(VERSION_200)) {
			xmlStream.writeAttribute("numberMatched", "" + allFeatures.size());
			xmlStream.writeAttribute("numberReturned", "" + allFeatures.size());
		} else if (!wfsVersion.equals(VERSION_100) && options.getResponseContainerEl() == null) {
			xmlStream.writeAttribute("numberOfFeatures", "" + allFeatures.size());
		}

		if (outputFormat == GML_2 || allFeatures.getEnvelope() != null) {
			writeBoundedBy(wfsVersion, gmlStream, outputFormat, allFeatures.getEnvelope());
		}

		// retrieve and write result features
		GmlXlinkOptions resolveState = gmlStream.getReferenceResolveStrategy().getResolveOptions();
		for (Feature member : allFeatures) {
			writeMemberFeature(member, gmlStream, xmlStream, resolveState, featureMemberEl);
		}
	}

	private void writeBoundedBy(Version wfsVersion, GMLStreamWriter gmlStream, GMLVersion outputFormat, Envelope env)
			throws XMLStreamException, UnknownCRSException, TransformationException {

		XMLStreamWriter xmlStream = gmlStream.getXMLStream();
		switch (outputFormat) {
		case GML_2: {
			xmlStream.writeStartElement("gml", "boundedBy", GMLNS);
			if (env == null) {
				xmlStream.writeStartElement("gml", "null", GMLNS);
				xmlStream.writeCharacters("inapplicable");
				xmlStream.writeEndElement();
			} else {
				gmlStream.write(env);
			}
			xmlStream.writeEndElement();
			break;
		}
		case GML_30:
		case GML_31: {
			xmlStream.writeStartElement("gml", "boundedBy", GMLNS);
			if (env == null) {
				xmlStream.writeStartElement("gml", "Null", GMLNS);
				xmlStream.writeCharacters("inapplicable");
				xmlStream.writeEndElement();
			} else {
				gmlStream.write(env);
			}
			xmlStream.writeEndElement();
			break;
		}
		case GML_32: {
			if (wfsVersion.equals(VERSION_200)) {
				xmlStream.writeStartElement("wfs", "boundedBy", GML3_2_NS);
				if (env == null) {
					xmlStream.writeStartElement("gml", "Null", GML3_2_NS);
					xmlStream.writeCharacters("inapplicable");
					xmlStream.writeEndElement();
				} else {
					gmlStream.write(env);
				}
				xmlStream.writeEndElement();
			} else {
				xmlStream.writeStartElement("gml", "boundedBy", GML3_2_NS);
				if (env == null) {
					xmlStream.writeStartElement("gml", "Null", GML3_2_NS);
					xmlStream.writeCharacters("inapplicable");
					xmlStream.writeEndElement();
				} else {
					gmlStream.write(env);
				}
				xmlStream.writeEndElement();
			}
			break;
		}
		}
	}

	private Lock acquireLock(GetFeature request, QueryAnalyzer analyzer) throws OWSException {

		Lock lock = null;

		if (request instanceof GetFeatureWithLock) {
			GetFeatureWithLock gfLock = (GetFeatureWithLock) request;

			// CITE 1.1.0 compliance (wfs:GetFeatureWithLock-Xlink)
			if (analyzer.getProjections() != null) {
				for (ProjectionClause clause : analyzer.getProjections()) {
					if (clause instanceof PropertyName) {
						PropertyName propName = (PropertyName) clause;
						ResolveParams resolveParams = propName.getResolveParams();
						if (resolveParams.getDepth() != null || resolveParams.getMode() != null
								|| resolveParams.getTimeout() != null) {
							throw new OWSException("GetFeatureWithLock does not support XlinkPropertyName",
									OPTION_NOT_SUPPORTED);
						}
					}
				}
			}

			// default: lock all
			boolean lockAll = true;
			if (gfLock.getLockAll() != null) {
				lockAll = gfLock.getLockAll();
			}

			// default: 5 minutes
			long expiryInMilliseconds = 5 * 60 * 1000;
			if (gfLock.getExpiryInSeconds() != null) {
				expiryInMilliseconds = gfLock.getExpiryInSeconds().longValue() * 1000;
			}

			LockManager manager = null;
			try {
				// TODO strategy for multiple LockManagers / feature stores
				WfsFeatureStoreManager storeManager = format.getMaster().getStoreManager();
				manager = storeManager.getStores()[0].getLockManager();
				List<Query> queries = analyzer.getQueries().get(storeManager.getStores()[0]);
				lock = manager.acquireLock(queries, lockAll, expiryInMilliseconds);
			} catch (OWSException e) {
				throw new OWSException(e.getMessage(), "CannotLockAllFeatures");
			} catch (FeatureStoreException e) {
				throw new OWSException("Cannot acquire lock: " + e.getMessage(), NO_APPLICABLE_CODE);
			}
		}
		return lock;
	}

	private boolean isGetFeatureByIdRequest(GetFeature request) {
		if (request.getQueries().size() == 1) {
			if (request.getQueries().get(0) instanceof StoredQuery) {
				StoredQuery getFeatureByIdQuery = (StoredQuery) request.getQueries().get(0);
				if (getFeatureByIdQuery.getId().equals(GET_FEATURE_BY_ID)) {
					LOG.debug("processing " + GET_FEATURE_BY_ID + " request");
					return true;
				}
			}
		}
		return false;
	}

	private void writeSingleFeatureMember(GMLStreamWriter gmlStream, QueryAnalyzer analyzer,
			GmlXlinkOptions resolveState) throws XMLStreamException, UnknownCRSException, TransformationException,
			FeatureStoreException, FilterEvaluationException, OWSException {

		FeatureCollection allFeatures = new GenericFeatureCollection();
		Set<String> fids = new HashSet<String>();

		for (Map.Entry<FeatureStore, List<Query>> fsToQueries : analyzer.getQueries().entrySet()) {
			FeatureStore fs = fsToQueries.getKey();
			Query[] queries = fsToQueries.getValue().toArray(new Query[fsToQueries.getValue().size()]);
			FeatureInputStream rs = fs.query(queries);
			try {
				for (Feature feature : rs) {
					if (!fids.contains(feature.getId())) {
						allFeatures.add(feature);
						fids.add(feature.getId());
						break;
					}
				}
			} catch (RuntimeException e) {
				throw new OWSException(e.getLocalizedMessage(), OPERATION_PROCESSING_FAILED);
			} finally {
				LOG.debug("Closing FeatureResultSet (cached)");
				rs.close();
			}
		}
		if (fids.isEmpty()) {
			throw new OWSException("feature not found", OPERATION_PROCESSING_FAILED);
		}
		for (Feature member : allFeatures) {
			gmlStream.getFeatureWriter().export(member, resolveState);
			break;
		}
	}
}
