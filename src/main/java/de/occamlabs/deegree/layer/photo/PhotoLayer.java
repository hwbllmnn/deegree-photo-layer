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
import java.util.List;

import org.deegree.commons.utils.fam.FileAlterationListener;
import org.deegree.commons.utils.fam.FileAlterationMonitor;
import org.deegree.layer.Layer;
import org.deegree.layer.LayerData;
import org.deegree.layer.LayerQuery;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.protocol.ows.exception.OWSException;

/**
 * <code>PhotoLayer</code>
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */

public class PhotoLayer implements Layer, FileAlterationListener {

    private LayerMetadata metadata;

    private File dir, index;

    private boolean recursive;

    private int size;

    public PhotoLayer( LayerMetadata metadata, File dir, File index, boolean recursive, int size ) {
        this.metadata = metadata;
        this.dir = dir;
        this.index = index;
        this.recursive = recursive;
        this.size = size;
        FileAlterationMonitor monitor = new FileAlterationMonitor(dir, 1000, recursive, null);
        monitor.registerListener(this );
        monitor.start();
    }

    @Override
    public LayerMetadata getMetadata() {
        return metadata;
    }

    @Override
    public LayerData mapQuery( LayerQuery query, List<String> headers )
                            throws OWSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LayerData infoQuery( LayerQuery query, List<String> headers )
                            throws OWSException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.deegree.commons.utils.fam.FileAlterationListener#newFile(java.io.File)
     */
    @Override
    public void newFile( File file ) {
        // TODO Auto-generated method stub
        System.out.println("new");
    }

    /* (non-Javadoc)
     * @see org.deegree.commons.utils.fam.FileAlterationListener#fileChanged(java.io.File)
     */
    @Override
    public void fileChanged( File file ) {
        // TODO Auto-generated method stub
        System.out.println("changed");
        
    }

    /* (non-Javadoc)
     * @see org.deegree.commons.utils.fam.FileAlterationListener#fileDeleted(java.io.File)
     */
    @Override
    public void fileDeleted( File file ) {
        // TODO Auto-generated method stub
        System.out.println("deleted");
        
    }

}
