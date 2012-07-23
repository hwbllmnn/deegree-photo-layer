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
import java.util.ArrayList;
import java.util.List;

import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.tiff.TiffField;
import org.apache.sanselan.formats.tiff.TiffImageMetadata.GPSInfo;
import org.apache.sanselan.formats.tiff.constants.TiffTagConstants;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.Triple;
import org.deegree.commons.utils.fam.FileAlterationListener;
import org.deegree.commons.utils.fam.FileAlterationMonitor;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.primitive.Point;
import org.deegree.layer.Layer;
import org.deegree.layer.LayerData;
import org.deegree.layer.LayerQuery;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.protocol.ows.exception.OWSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>PhotoLayer</code>
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */

public class PhotoLayer implements Layer, FileAlterationListener {

    private static final Logger LOG = LoggerFactory.getLogger( PhotoLayer.class );

    private GeometryFactory factory = new GeometryFactory();

    private LayerMetadata metadata;

    private File dir, index;

    private boolean recursive;

    private int size;

    private List<Triple<Point, File, Integer>> points = new ArrayList<Triple<Point, File, Integer>>();

    public PhotoLayer( LayerMetadata metadata, File dir, File index, boolean recursive, int size ) {
        this.metadata = metadata;
        this.dir = dir;
        this.index = index;
        this.recursive = recursive;
        this.size = size;
        FileAlterationMonitor monitor = new FileAlterationMonitor( dir, 1000, recursive, null );
        monitor.registerListener( this );
        monitor.start();
    }

    @Override
    public LayerMetadata getMetadata() {
        return metadata;
    }

    @Override
    public LayerData mapQuery( LayerQuery query, List<String> headers )
                            throws OWSException {
        return new PhotoLayerData( points, size );
    }

    @Override
    public LayerData infoQuery( LayerQuery query, List<String> headers )
                            throws OWSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void newFile( File file ) {
        try {
            IImageMetadata md = Sanselan.getMetadata( file );
            if ( !( md instanceof JpegImageMetadata ) ) {
                LOG.warn( "File {} has metadata in an unsupported format.", file );
                return;
            }
            System.out.println("yuh?");
            JpegImageMetadata tmd = (JpegImageMetadata) md;
            System.out.println("rerer");
            TiffField orientation = tmd.findEXIFValue( TiffTagConstants.TIFF_TAG_ORIENTATION );
            System.out.println("resr");
            int rotation = 0;
            System.out.println(orientation);
            if ( orientation != null ) {
                int o = orientation.getIntValue();
                if ( o == 2 ) {
                    rotation = 180;
                }
                if ( o == 5 ) {
                    rotation = 270;
                }
                if ( o == 7||o ==6 ) {
                    rotation = 90;
                }
                System.out.println(o);
                System.out.println(rotation);
            }
            GPSInfo info = tmd.getExif().getGPS();
            if ( info == null ) {
                LOG.warn( "File {} has no GPS info.", file );
                return;
            }

            double x = info.getLongitudeAsDegreesEast();
            double y = info.getLatitudeAsDegreesNorth();
            Point p = factory.createPoint( null, x, y, CRSManager.getCRSRef( "CRS:84" ) );
            points.add( new Triple<Point, File, Integer>( p, file, rotation ) );
        } catch ( ImageReadException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void fileChanged( File file ) {
        // TODO Auto-generated method stub
        System.out.println( "changed" );

    }

    @Override
    public void fileDeleted( File file ) {
        // TODO Auto-generated method stub
        System.out.println( "deleted" );

    }

}
