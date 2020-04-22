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
package org.deegree.rendering.r2d;

import static java.awt.BasicStroke.CAP_BUTT;
import static java.awt.BasicStroke.JOIN_ROUND;
import static java.awt.geom.AffineTransform.getTranslateInstance;
import static java.lang.Math.toRadians;
import static org.deegree.commons.utils.math.MathUtils.isZero;
import static org.deegree.commons.utils.math.MathUtils.round;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D.Double;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.Point;
import org.deegree.rendering.r2d.strokes.HaloStroke;
import org.deegree.rendering.r2d.strokes.OffsetStroke;
import org.deegree.rendering.r2d.strokes.TextStroke;
import org.deegree.style.styling.TextStyling;

import java.util.BitSet;

/**
 * Responsible for rendering a single label.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
class LabelRenderer {

	private Java2DRenderer renderer;

	private RendererContext context;
	
	private BitSet[] bitMatrix;
	private int imgWidth;
	private int imgHeight;
	private Boolean isAvoidLblCollision;

	LabelRenderer(Java2DRenderer renderer) {
		this.renderer = renderer;
		this.context = renderer.rendererContext;
	}

	/**
	 * Title: Label 충돌 방지 소스 수정 + parameter 추가
	 * Author: Jumgmin 
	 * Date: 2016.02.12
	 * Version: Wise Shpere 1.0 
	 * Content: width, height 파라미터 추가한 오버로딩 생성자 + 라벨 충돌 여부 체크 parameter 추가
	 * 
	 * @param renderer
	 * @param width
	 * @param height
	 */
	public LabelRenderer(Java2DRenderer renderer, int width, int height, Boolean isAvoidLblCollision, BitSet[] bitMatrix) {
		// TODO Auto-generated constructor stub
		this.renderer = renderer;
		this.context = renderer.rendererContext;
		// this.criteriaLabelAvoid = false;
		// this.labelBndList = new ArrayList<double[]>();
//		this.bitMatrix = new BitSet[width];
//		for (int i = 0; i < width; i++)
//			bitMatrix[i] = new BitSet(height);
		this.bitMatrix = bitMatrix;
		this.imgWidth = width;
		this.imgHeight = height;
		this.isAvoidLblCollision = isAvoidLblCollision;
	}
	/**
	 * Title: Label 충돌 방지 소스 수정 
	 * Author: Jumgmin 
	 * Date: 2016.02.02
	 * Version: Wise Shpere 1.0 
	 * Content: TextLayout을 먼저 생성 후 라벨 충돌 체크, rotation 및 draw 수행
	 */
	void render(TextStyling styling, Font font, String text, Point p) {
		Point2D.Double pt = (Point2D.Double) renderer.worldToScreen.transform(
				new Point2D.Double(p.get0(), p.get1()), null);
		double x = pt.x
				+ context.uomCalculator.considerUOM(styling.displacementX,
						styling.uom);
		double y = pt.y
				- context.uomCalculator.considerUOM(styling.displacementY,
						styling.uom);
		renderer.graphics.setFont(font);
		//TextLayout 먼저 생성
		TextLayout layout;
		synchronized (FontRenderContext.class) {
			// apparently getting the font render context is not threadsafe
			// (despite having different graphics here)
			// so do this globally synchronized to fix:
			// http://tracker.deegree.org/deegree-core/ticket/200
			FontRenderContext frc = renderer.graphics.getFontRenderContext();
			layout = new TextLayout(text, font, frc);
		}
		double width = layout.getBounds().getWidth();
		double height = layout.getBounds().getHeight();
		double px = x - styling.anchorPointX * width;
		double py = y + styling.anchorPointY * height;
		//충돌 라벨 제거한다면...
		if(isAvoidLblCollision != null){
			//라벨 충돌 여부 확인 x좌표, y좌표, 레이아웃 너비, 높이, buffer 값(default: 5)
			if(isLabelOverlap(px, py, width, height, styling.rotation, 5)){
				return;
			}
		}
		//기존 소스 수정. TextLayout을 통해 먼저 라벨 충돌 여부 확인 후 rotation. 
		AffineTransform transform = renderer.graphics.getTransform();
		renderer.graphics.rotate(toRadians(styling.rotation), x, y);

		if (styling.halo != null) {
			context.fillRenderer.applyFill(styling.halo.fill, styling.uom);

			BasicStroke stroke = new BasicStroke(
					round(2 * context.uomCalculator.considerUOM(
							styling.halo.radius, styling.uom)), CAP_BUTT,
					JOIN_ROUND);
			renderer.graphics.setStroke(stroke);
			renderer.graphics.draw(layout.getOutline(getTranslateInstance(px,
					py)));
		}

		renderer.graphics.setStroke(new BasicStroke());

		context.fillRenderer.applyFill(styling.fill, styling.uom);
		layout.draw(renderer.graphics, (float) px, (float) py);

		renderer.graphics.setTransform(transform);
	}
	/**
	 * Title: Spline 라벨 처리 수정
	 * Author: Jumgmin 
	 * Date: 2016.02.02 
	 * Version: Wise Shpere 1.0 
	 * Content: Spline의 라벨에서 halo 적용이 안되는 문제 해결
	 */
	void render(TextStyling styling, Font font, String text, Curve c) {

		java.awt.Stroke stroke = new TextStroke(text, font,
				styling.linePlacement);
		if (isZero(((TextStroke) stroke).getLineHeight())) {
			return;
		}

		stroke = applyOffset(styling, stroke);
		Double line = context.geomHelper.fromCurve(c, false);
		//halo 일 경우
		if (styling.halo != null) {
			//HaloStroke 객체 생성
			java.awt.Stroke haloStroke = new HaloStroke(text, font,
					styling.linePlacement, styling.halo, styling.uom,
					renderer.rendererContext.uomCalculator);
			haloStroke = applyOffset(styling, haloStroke);
			renderer.rendererContext.fillRenderer.applyFill(styling.halo.fill,
					styling.uom);
			renderer.graphics.setStroke(haloStroke);
			renderer.graphics.draw(line);
		}
		renderer.rendererContext.fillRenderer.applyFill(styling.fill,
				styling.uom);
		renderer.graphics.setStroke(stroke);
		renderer.graphics.draw(line);
	}


	/**
	 * Title: Label 충돌 방지 소스 수정 
	 * Author: Jumgmin 
	 * Date: 2016.02.01 
	 * Version: Wise Shpere 1.0 
	 * Content: Label 충돌 방지를 위해 호출. 
	 * @param px
	 * @param py
	 * @param width
	 * @param height
	 * @param rotation 
	 * @param i
	 * @return
	 */
	private boolean isLabelOverlap(double px, double py, double width,
			double height, double rotation, int buffer) {
		// TODO Auto-generated method stub
		// 영역 기준이 되는 x, y 좌표를 정수형으로 변환. 최소값은 내림, 최대값은 올림
		// 해당 evelope 직사각형 크기를 buffer 값을 통해 적당 크기를 잡음.(단위 픽셀)
		//min 값은 1이상이어야 하고, max는 이미지 크기만큼 설정
		int criMinX = (int) Math.floor(px) - buffer <= 1 ? 1 : (int) Math.floor(px) - buffer;
		int criMinY = (int) Math.floor(py) - buffer <= 1 ? 1 : (int) Math.floor(py) - buffer;
		int criMaxX = (int) Math.ceil(px + width) + buffer >= imgWidth ? imgWidth : (int) Math.ceil(px + width) + buffer;
		int criMaxY = (int) Math.ceil(py + height) + buffer >= bitMatrix[0].size() ?  bitMatrix[0].size() : (int) Math.ceil(py + height) + buffer; //bitmatrix size 크기만큼
//		int criMaxY = (int) Math.ceil(py + height) + buffer >= imgHeight ? imgHeight : (int) Math.ceil(py + height) + buffer;
		try {
			//회전 없는 경우
			if (rotation == 0) {
				for (int i = criMinX - 1; i < criMaxX; i++) {
					for (int j = criMinY - 1; j < criMaxY; j++) {
						if (bitMatrix[i].get(j)) {
							return true;
						}
					}
				}
				for (int i = criMinX - 1; i < criMaxX; i++) {
					for (int j = criMinY - 1; j < criMaxY; j++) {
						bitMatrix[i].set(j);
					}
				}
				return false;
			} 
			//회전 있는 경우
			else {
				double dSetDegree = Math.toRadians(rotation);
				double cosq = Math.cos(dSetDegree);
				double sinq = Math.sin(dSetDegree);
								
				for (int i = criMinX - 1; i < criMaxX; i++) {
					for (int j = criMinY - 1; j < criMaxY; j++) {
						double sx = i - criMinX;
						double sy = j - criMinY;
						double rx = (sx * cosq - sy * sinq) + criMinX; // 결과 좌표
																		// x
						double ry = (sx * sinq + sy * cosq) + criMinY; // 결과 좌표
																		// y
						int iResultX = (int) Math.round(rx);
						int iResultY = (int) Math.round(ry);
						
						if(iResultX < 0) iResultX = 0;
						if(iResultY < 0) iResultY = 0;
						if(iResultX > imgWidth - 1) iResultX = imgWidth - 1;
						if(iResultY > bitMatrix[0].size() - 1) iResultY = bitMatrix[0].size() - 1; //bitmatrix size 크기만큼
//						if(iResultY > imgHeight - 1) iResultY = imgHeight - 1;
						
						if (bitMatrix[iResultX].get(iResultY)) {
							return true;
						}
					}
				}
				for (int i = criMinX - 1; i < criMaxX; i++) {
					for (int j = criMinY - 1; j < criMaxY; j++) {
						double sx = i - criMinX;
						double sy = j - criMinY;
						double rx = (sx * cosq - sy * sinq) + criMinX; // 결과 좌표
																		// x
						double ry = (sx * sinq + sy * cosq) + criMinY; // 결과 좌표
																		// y
						int iResultX = (int) Math.round(rx);
						int iResultY = (int) Math.round(ry);
						
						if(iResultX < 0) iResultX = 0;
						if(iResultY < 0) iResultY = 0;
						if(iResultX > imgWidth - 1) iResultX = imgWidth - 1;
						if(iResultY > bitMatrix[0].size() - 1) iResultY = bitMatrix[0].size() - 1;//bitmatrix size 크기만큼
//						if(iResultY > imgHeight - 1) iResultY = imgHeight - 1;
						
						bitMatrix[iResultX].set(iResultY);
					}
				}
				return false;

			}
			
		}catch(Exception e){
			System.out.println("Error in cal overlap: " + e.getStackTrace());
//			log.error("Error in cal overlap: " + e.getStackTrace());
			return false;
		}
		
	}
	private java.awt.Stroke applyOffset(TextStyling styling,
			java.awt.Stroke stroke) {
		if (!isZero(styling.linePlacement.perpendicularOffset)) {
			stroke = new OffsetStroke(
					styling.linePlacement.perpendicularOffset, stroke,
					styling.linePlacement.perpendicularOffsetType);
		}
		return stroke;
	}

}
