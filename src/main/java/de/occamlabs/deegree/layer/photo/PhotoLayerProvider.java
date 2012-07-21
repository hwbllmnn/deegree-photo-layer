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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.xml.jaxb.JAXBUtils;
import org.deegree.geometry.metadata.SpatialMetadata;
import org.deegree.geometry.metadata.SpatialMetadataConverter;
import org.deegree.layer.Layer;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.layer.persistence.LayerStore;
import org.deegree.layer.persistence.LayerStoreProvider;
import org.deegree.layer.persistence.MultipleLayerStore;
import org.deegree.protocol.ows.metadata.Description;
import org.deegree.protocol.ows.metadata.DescriptionConverter;

import de.occamlabs.deegree.layer.photo.jaxb.PhotoLayerType;
import de.occamlabs.deegree.layer.photo.jaxb.PhotoLayers;

/**
 * <code>PhotoLayerProvider</code>
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */

public class PhotoLayerProvider implements LayerStoreProvider {

    private static final URL CONFIG_SCHEMA = PhotoLayerProvider.class.getResource( "/META-INF/schemas/layers/photo/3.2.0/photo.xsd" );

    private DeegreeWorkspace workspace;

    @Override
    public void init( DeegreeWorkspace workspace ) {
        this.workspace = workspace;
    }

    @Override
    public LayerStore create( URL configUrl )
                            throws ResourceInitException {
        try {
            PhotoLayers cfg = (PhotoLayers) JAXBUtils.unmarshall( "de.occamlabs.deegree.layer.photo.jaxb",
                                                                  CONFIG_SCHEMA, configUrl, workspace );
            Map<String, Layer> map = new LinkedHashMap<String, Layer>();

            for ( PhotoLayerType l : cfg.getPhotoLayer() ) {
                Layer lay = createPhotoLayer( l, configUrl );
                map.put( lay.getMetadata().getName(), lay );
            }

            return new MultipleLayerStore( map );
        } catch ( ResourceInitException e ) {
            throw e;
        } catch ( Throwable e ) {
            throw new ResourceInitException( "Unable to create photo layer store: " + e.getLocalizedMessage(), e );
        }
    }

    private PhotoLayer createPhotoLayer( PhotoLayerType cfg, URL configUrl )
                            throws ResourceInitException, URISyntaxException {
        String dir = cfg.getDirectory().getValue();
        boolean recursive = cfg.getDirectory().isRecursive();
        int size = cfg.getImageSize();
        File dirFile = new File( configUrl.toURI().resolve( dir ) );
        File h2File = new File( dirFile, ".h2-index" );

        if ( !dirFile.exists() ) {
            throw new ResourceInitException( "Directory " + dir + " does not exist." );
        }

        Description desc = DescriptionConverter.fromJaxb( cfg.getTitle(), cfg.getAbstract(), cfg.getKeywords() );
        SpatialMetadata smd = SpatialMetadataConverter.fromJaxb( cfg.getEnvelope(), cfg.getCRS() );

        LayerMetadata md = new LayerMetadata( cfg.getName(), desc, smd );
        return new PhotoLayer( md, dirFile, h2File, recursive, size );
    }

    @Override
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[] {};
    }

    @Override
    public String getConfigNamespace() {
        return "http://www.occamlabs.de/deegree/layers/photo";
    }

    @Override
    public URL getConfigSchema() {
        return CONFIG_SCHEMA;
    }

}
