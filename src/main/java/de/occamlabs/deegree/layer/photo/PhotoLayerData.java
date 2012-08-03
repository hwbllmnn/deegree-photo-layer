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

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.Triple;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.GenericFeatureCollection;
import org.deegree.feature.property.SimpleProperty;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.GenericFeatureType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.geometry.primitive.Point;
import org.deegree.layer.LayerData;
import org.deegree.rendering.r2d.Renderer;
import org.deegree.rendering.r2d.context.RenderContext;
import org.deegree.style.styling.PointStyling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>PhotoLayerData</code>
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */

public class PhotoLayerData implements LayerData {

    private static final Logger LOG = LoggerFactory.getLogger( PhotoLayerData.class );

    private List<Triple<Point, BufferedImage, Integer>> points;

    private List<Pair<Point, File>> fiPoints;

    private int size;

    private static final FeatureType featureType;

    private static final SimplePropertyType linkType;

    static {
        QName name = new QName( "http://www.deegree.org/app", "photo" );
        List<PropertyType> props = new ArrayList<PropertyType>();
        QName linkName = new QName( "http://www.deegree.org/app", "link" );
        props.add( linkType = new SimplePropertyType( linkName, 1, 1, BaseType.STRING, null, null ) );
        featureType = new GenericFeatureType( name, props, false );
    }

    public PhotoLayerData( List<Triple<Point, BufferedImage, Integer>> points, int size ) {
        this.points = points;
        this.size = size;
    }

    public PhotoLayerData( List<Pair<Point, File>> points ) {
        this.fiPoints = points;
    }

    @Override
    public void render( RenderContext context ) {
        Renderer renderer = context.getVectorRenderer();
        PointStyling sty = new PointStyling();
        sty.graphic.size = size;
        for ( Triple<Point, BufferedImage, Integer> p : points ) {
            sty.graphic.image = p.second;
            sty.graphic.rotation = p.third;
            renderer.render( sty, p.first );
        }
    }

    @Override
    public FeatureCollection info() {
        GenericFeatureCollection col = new GenericFeatureCollection();
        for ( Pair<Point, File> p : fiPoints ) {
            List<Property> props = new ArrayList<Property>();
            try {
                props.add( new SimpleProperty( linkType, p.second.toURI().toURL().toExternalForm() ) );
                col.add( featureType.newFeature( "ID_" + p.second.toURI().toURL().toExternalForm(), props, null ) );
            } catch ( MalformedURLException e ) {
                LOG.error( "Creating file url failed?: {}", e.getLocalizedMessage() );
                LOG.trace( "Stack trace:", e );
            }
        }
        return col;
    }

}
