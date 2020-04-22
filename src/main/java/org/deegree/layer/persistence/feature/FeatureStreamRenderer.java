//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.layer.persistence.feature;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.utils.Triple;
import org.deegree.feature.Feature;
import org.deegree.feature.stream.FeatureInputStream;
import org.deegree.feature.types.FeatureType;
import org.deegree.filter.XPathEvaluator;
import org.deegree.geometry.Geometry;
import org.deegree.rendering.r2d.Renderer;
import org.deegree.rendering.r2d.TextRenderer;
import org.deegree.rendering.r2d.context.RenderContext;
import org.deegree.style.se.unevaluated.Style;
import org.deegree.style.styling.Styling;
import org.deegree.style.styling.TextStyling;
import org.slf4j.Logger;

/**
 * Responsible for using a renderer to evaluate and render a feature stream.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
class FeatureStreamRenderer {

	private static final Logger LOG = getLogger(FeatureStreamRenderer.class);

	private RenderContext context;

	private int maxFeatures;

	private XPathEvaluator<?> evaluator;

	FeatureStreamRenderer(RenderContext context, int maxFeatures,
			XPathEvaluator<?> evaluator) {
		this.context = context;
		this.maxFeatures = maxFeatures;
		this.evaluator = evaluator;
	}
    private static String formatDuration(long duration) {
        long hours = TimeUnit.MILLISECONDS.toHours(duration);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60;
        long milliseconds = duration % 1000;
        return String.format("%02d:%02d:%02d,%03d", hours, minutes, seconds, milliseconds);
    }

	void renderFeatureStream(FeatureInputStream features, Style style)
			throws InterruptedException {
		// TODO Auto-generated method stub
		int cnt = 0;
		int txtCnt = 0;
		
		Renderer renderer = context.getVectorRenderer();
		TextRenderer textRenderer = context.getTextRenderer();
		/**
		 * Title: draw 순서 조정 
		 * Author: Jumgmin 
		 * Date: 2016.02.02
		 * Version: Wise Shpere 1.0 
		 * Content: rendering 시 먼저 심볼 rendering 후 text redering. 소축척 시 라벨이 거의 보이지 않음.
		 */
		// Text 렌더링을 위한 List
        // Date startTime = new Date();
		
		ArrayList<Triple<Styling, LinkedList<Geometry>, String>> txtEvalds = new ArrayList<>();
		try {
			for (Feature f : features) {
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}

				LinkedList<Triple<Styling, LinkedList<Geometry>, String>> evalds = style
						.evaluate(f, (XPathEvaluator<Feature>) evaluator);
				for (Triple<Styling, LinkedList<Geometry>, String> evald : evalds) {
					if (evald.first instanceof TextStyling) {
						//TextStyling을 나중에 렌더링하기 위해 list에 추가
						txtEvalds.add(evald);
					} else {
						renderer.render(evald.first, evald.second);
					}
				}

				if (maxFeatures > 0 && ++cnt == maxFeatures) {
					LOG.debug(
							"Reached max features of {} for layer '{}', stopping.",
							maxFeatures, this);
					break;
				}
			}
			LOG.debug("TextStyle rendering size: " + txtEvalds.size());
			for (Triple<Styling, LinkedList<Geometry>, String> evald : txtEvalds) {
				textRenderer.render((TextStyling) evald.first, evald.third,
						evald.second);
				if (maxFeatures > 0 && ++txtCnt == maxFeatures) {
					LOG.debug(
							"Reached max features of {} for layer '{}', stopping.",
							maxFeatures, this);
					break;
				}
			}
	        // Date endTime = new Date();
	        // long diff = endTime.getTime() - startTime.getTime();
	        // String hrDateText = formatDuration(diff);
	        // System.out.println("renderFeatureStream : " + hrDateText);

		} catch (Throwable e) {
			LOG.warn("Unable to render feature, probably a curve had multiple/non-linear segments.");
			LOG.warn("Error message was: {}", e.getLocalizedMessage());
			LOG.trace("Stack trace:", e);
		}
	}
	
	void renderFeatureStream(FeatureInputStream features, Style style, HashMap<String, String> hm)
			throws InterruptedException {
		// TODO Auto-generated method stub
		int cnt = 0;
		int txtCnt = 0;

		Renderer renderer = context.getVectorRenderer();
		TextRenderer textRenderer = context.getTextRenderer();
		/**
		 * Title: draw 순서 조정 
		 * Author: Jumgmin 
		 * Date: 2016.02.02
		 * Version: Wise Shpere 1.0 
		 * Content: rendering 시 먼저 심볼 rendering 후 text redering. 소축척 시 라벨이 거의 보이지 않음.
		 */
		// Text 렌더링을 위한 List
		ArrayList<Triple<Styling, LinkedList<Geometry>, String>> txtEvalds = new ArrayList<>();
		try {
			for (Feature f : features) {
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}

				LinkedList<Triple<Styling, LinkedList<Geometry>, String>> evalds = style
						.evaluate(f, (XPathEvaluator<Feature>) evaluator);
				for (Triple<Styling, LinkedList<Geometry>, String> evald : evalds) {
					if (evald.first instanceof TextStyling) {
						//TextStyling을 나중에 렌더링하기 위해 list에 추가
						txtEvalds.add(evald);
					} else {
						FeatureType ft = f.getType();
						ArrayList<PropertyType> pds = (ArrayList<PropertyType>)ft.getPropertyDeclarations();
						
						//ft.getPropertyDeclaration(pds.get(0).getName().getNamespaceURI());
						
						String layerName = ft.getName().getLocalPart();
						String bufferFieldName = null;
						String buff = null;

						for (int i=0; i<pds.size(); i++) {
							for (int j=0; j<hm.size(); j++) {
								if (!hm.get(layerName).isEmpty()) {
									bufferFieldName = hm.get(layerName).toString();
									break;
								}
							}
							if ((bufferFieldName == null) || bufferFieldName.equals("null") || bufferFieldName.isEmpty()) {
								buff = "0";
								break;
							}
							
							for (int k=0; k<pds.size()-1; k++) {
								QName fieldName = pds.get(k).getName();
								
								if (fieldName.getLocalPart().equals(bufferFieldName)) {
									Property p = f.getProperties(fieldName).get(0);
									buff = p.getValue().toString();
									break;
								}
							}
						}
						
						if (buff.equals("0"))
							renderer.render(evald.first, evald.second);
						else
							renderer.render(evald.first, evald.second, buff);
					}
				}

				if (maxFeatures > 0 && ++cnt == maxFeatures) {
					LOG.debug(
							"Reached max features of {} for layer '{}', stopping.",
							maxFeatures, this);
					break;
				}
			}
			LOG.debug("TextStyle rendering size: " + txtEvalds.size());
			for (Triple<Styling, LinkedList<Geometry>, String> evald : txtEvalds) {
				textRenderer.render((TextStyling) evald.first, evald.third,
						evald.second);
				if (maxFeatures > 0 && ++txtCnt == maxFeatures) {
					LOG.debug(
							"Reached max features of {} for layer '{}', stopping.",
							maxFeatures, this);
					break;
				}
			}

		} catch (Throwable e) {
			LOG.warn("Unable to render feature, probably a curve had multiple/non-linear segments.");
			LOG.warn("Error message was: {}", e.getLocalizedMessage());
			LOG.trace("Stack trace:", e);
		}
	}
}
