This directory is created to provide the OpenCV Java bindings, which will then leverage the native OpenCV system libraries.

Java libraries within this directory are managed by Maven, e.g.:

    mvn deploy:deploy-file -Durl=file:///home/user/Projects/docidx/lib/ -Dfile=/usr/local/Cellar/opencv/3.2.0/share/OpenCV/java/opencv-320.jar -DgroupId=org.opencv -DartifactId=opencv -Dpackaging=jar -Dversion=3.2.0

The OpenCV Java bindings are built and distributed within this directory. This is governed by licensing independent of docidx project itself.

OpenCV documentation and source is available at https://opencv.org
