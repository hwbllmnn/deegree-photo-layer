//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/

package de.occamlabs.deegree.layer.photo;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.deegree.commons.utils.Triple;
import org.deegree.feature.FeatureCollection;
import org.deegree.geometry.primitive.Point;
import org.deegree.layer.LayerData;
import org.deegree.rendering.r2d.Renderer;
import org.deegree.rendering.r2d.context.RenderContext;
import org.deegree.style.styling.PointStyling;

/**
 * <code>PhotoLayerData</code>
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */

public class PhotoLayerData implements LayerData {

    private List<Triple<Point, File, Integer>> points;

    private int size;

    public PhotoLayerData( List<Triple<Point, File, Integer>> points, int size ) {
        this.points = points;
        this.size = size;
    }

    @Override
    public void render( RenderContext context ) {
        Renderer renderer = context.getVectorRenderer();
        PointStyling sty = new PointStyling();
        sty.graphic.size = size;
        for ( Triple<Point, File, Integer> p : points ) {
            try {
                sty.graphic.image = ImageIO.read( p.second );
                sty.graphic.rotation = p.third;
                renderer.render( sty, p.first );
            } catch ( IOException e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public FeatureCollection info() {
        // TODO Auto-generated method stub
        return null;
    }

}
