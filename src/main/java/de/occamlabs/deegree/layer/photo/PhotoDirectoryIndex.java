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

import static org.apache.sanselan.formats.tiff.constants.TiffTagConstants.TIFF_TAG_ORIENTATION;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.tiff.TiffField;
import org.apache.sanselan.formats.tiff.TiffImageMetadata.GPSInfo;
import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.utils.JDBCUtils;
import org.deegree.commons.utils.Triple;
import org.deegree.commons.utils.fam.FileAlterationListener;
import org.deegree.commons.utils.fam.FileAlterationMonitor;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.geometry.primitive.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>PhotoDirectoryIndex</code>
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */

public class PhotoDirectoryIndex implements FileAlterationListener {

    private static final Logger LOG = LoggerFactory.getLogger( PhotoDirectoryIndex.class );

    private DeegreeWorkspace workspace;

    private String connid;

    public PhotoDirectoryIndex( DeegreeWorkspace workspace, File directory, File index, boolean recursive ) {
        this.workspace = workspace;
        FileAlterationMonitor monitor = new FileAlterationMonitor( directory, 1000, recursive, null );
        monitor.registerListener( this );

        ConnectionManager.addConnection( connid = UUID.randomUUID().toString(), "jdbc:h2:" + index.toString(), "SA",
                                         "", 0, 5 );
        initDatabase();
        monitor.start();
    }

    private void initDatabase() {
        ConnectionManager mgr = workspace.getSubsystemManager( ConnectionManager.class );
        Connection conn = mgr.get( connid );
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            DatabaseMetaData md = conn.getMetaData();
            rs = md.getTables( null, null, "PHOTODIRECTORYINDEX", null );
            if ( !rs.next() ) {
                String sql = "create table photodirectoryindex (x double, y double, rotation integer, file varchar, timestamp bigint)";
                stmt = conn.prepareStatement( sql );
                stmt.executeUpdate();
                stmt.close();
                stmt = conn.prepareStatement( "create index x_index on photodirectoryindex (x)" );
                stmt.executeUpdate();
                stmt.close();
                stmt = conn.prepareStatement( "create index y_index on photodirectoryindex (y)" );
                stmt.executeUpdate();
                stmt.close();
                conn.commit();
            }
            rs.close();
        } catch ( SQLException e ) {
            LOG.error( "Could not create photo directory index: {}", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        } finally {
            JDBCUtils.close( rs, stmt, conn, LOG );
        }
    }

    @Override
    public void newFile( File file ) {
        long timestamp = file.lastModified();
        ConnectionManager mgr = workspace.getSubsystemManager( ConnectionManager.class );
        Connection conn = mgr.get( connid );
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement( "select * from photodirectoryindex where file = ? and timestamp = ?" );
            stmt.setString( 1, file.toString() );
            stmt.setLong( 2, timestamp );
            rs = stmt.executeQuery();
            if ( rs.next() ) {
                return;
            }
            rs.close();
            stmt.close();

            IImageMetadata md = Sanselan.getMetadata( file );
            if ( !( md instanceof JpegImageMetadata ) ) {
                LOG.warn( "File {} has metadata in an unsupported format.", file );
                return;
            }
            JpegImageMetadata tmd = (JpegImageMetadata) md;
            TiffField orientation = tmd.findEXIFValue( TIFF_TAG_ORIENTATION );
            int rotation = 0;
            if ( orientation != null ) {
                int o = orientation.getIntValue();
                if ( o == 2 ) {
                    rotation = 180;
                }
                if ( o == 5 ) {
                    rotation = 270;
                }
                if ( o == 7 || o == 6 ) {
                    rotation = 90;
                }
            }
            GPSInfo info = tmd.getExif().getGPS();
            if ( info == null ) {
                LOG.warn( "File {} has no GPS info.", file );
                return;
            }

            double x = info.getLongitudeAsDegreesEast();
            double y = info.getLatitudeAsDegreesNorth();
            stmt = conn.prepareStatement( "insert into photodirectoryindex values (?,?,?,?,?)" );
            stmt.setDouble( 1, x );
            stmt.setDouble( 2, y );
            stmt.setInt( 3, rotation );
            stmt.setString( 4, file.toString() );
            stmt.setLong( 5, timestamp );
            stmt.executeUpdate();
            conn.commit();
        } catch ( Throwable e ) {
            LOG.warn( "Could not update index with file {}: {}", file, e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        } finally {
            JDBCUtils.close( rs, stmt, conn, LOG );
        }
    }

    @Override
    public void fileChanged( File file ) {
        fileDeleted( file );
        newFile( file );
    }

    @Override
    public void fileDeleted( File file ) {
        ConnectionManager mgr = workspace.getSubsystemManager( ConnectionManager.class );
        Connection conn = mgr.get( connid );
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement( "delete from photodirectoryindex where file = ?" );
            stmt.executeUpdate();
            stmt.close();
            conn.commit();
        } catch ( Throwable e ) {
            LOG.warn( "Could not update index with file {}: {}", file, e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        } finally {
            JDBCUtils.close( null, stmt, conn, LOG );
        }
    }

    public List<Triple<Point, File, Integer>> query( Envelope envelope ) {
        List<Triple<Point, File, Integer>> list = new ArrayList<Triple<Point, File, Integer>>();
        GeometryFactory fac = new GeometryFactory();

        ConnectionManager mgr = workspace.getSubsystemManager( ConnectionManager.class );
        Connection conn = mgr.get( connid );
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            envelope = new GeometryTransformer( CRSManager.getCRSRef( "CRS:84" ) ).transform( envelope );
            stmt = conn.prepareStatement( "select x, y, rotation, file from photodirectoryindex where x >= ? and x <= ? and y >= ? and y <= ?" );
            stmt.setDouble( 1, envelope.getMin().get0() );
            stmt.setDouble( 2, envelope.getMax().get0() );
            stmt.setDouble( 3, envelope.getMin().get1() );
            stmt.setDouble( 4, envelope.getMax().get1() );
            rs = stmt.executeQuery();

            while ( rs.next() ) {
                double x = rs.getDouble( "x" );
                double y = rs.getDouble( "y" );
                int rotation = rs.getInt( "rotation" );
                String file = rs.getString( "file" );
                Point p = fac.createPoint( null, x, y, CRSManager.getCRSRef( "CRS:84" ) );
                list.add( new Triple<Point, File, Integer>( p, new File( file ), rotation ) );
            }
        } catch ( Throwable e ) {
            LOG.warn( "Could not query index: {}", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        } finally {
            JDBCUtils.close( rs, stmt, conn, LOG );
        }
        LOG.debug( "Found {} images.", list.size() );
        return list;
    }

}
