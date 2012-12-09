deegree-photo-layer
===================

Well Hanko, here it is:

A layer implementation that displays georeferenced images in a deegree map. Make sure you use at least a deegree webservices 3.2-pre12.

An example configuration looks like this:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<PhotoLayers xmlns="http://www.occamlabs.de/deegree/layers/photo" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://www.occamlabs.de/deegree/layers/photo photo.xsd" configVersion="3.2.0"
  xmlns:l="http://www.deegree.org/layers/base" xmlns:d="http://www.deegree.org/metadata/description" xmlns:s="http://www.deegree.org/metadata/spatial">

  <PhotoLayer>
    <l:Name>hankos_vacation</l:Name>
    <d:Title>Where the hell was Hanko</d:Title>
    <s:CRS>EPSG:31466 EPSG:4326</s:CRS>
    <Directory recursive="true">/data/pics</Directory>
    <ImageSize>32</ImageSize>
  </PhotoLayer>

</PhotoLayers>
 
```

Take note to include the Apache Sanselan library in your workspace:

http://repo1.maven.org/maven2/org/apache/sanselan/sanselan/0.97-incubator/sanselan-0.97-incubator.jar

As usual, you may include an arbitrary number of (in this case) PhotoLayer elements, each handles a single directory. You may leave off the recursive attribute, true is the default.

I think most of the default layer stuff is configurable, probably only the standard metadata stuff (abstract and so on) is actually used by the implementation.

A feature info is usable, and uses a radius to include the configured image size (which is optional, and set to 32 pixels by default). It will yield a file URL to the actual image the layer displayed. Using a feature info template to reference the image remotely might be nice, but is not possible yet, so the main use is to display the images locally (tested with Chrome, FF will probably work as well).

Well, there you have it. Let me know if there's anything else which would be nice.

