# DocIndex

DocIndex is the batch process used to feed DocMag, a front-end to Elasticsearch
that allows server-side document searching to be simple.


## Requirements

DocIndex can be run directly on an OS, however it is recommended to be run within
a Docker container. The container is composed within the DocMag docker-compose.yml.

Usually you won't want to build and run docidx locally, instead it is best to
run the docker container published at: https://hub.docker.com/r/deckerego/docidx/


## Building and Testing Locally

Since docidx relies heavily on computer vision and image processing, bindings to
native libraries are heavily used. Packaged Java distributions with native libraries
are a giant pain in the butt - hence leveraging Docker containers to ship things
by default. If you just want to get docidx up and running Docker will be the easiest
way to go, but if you would like to tweak the code and run it locally you will
need to jump through some hoops to install the native libs.

docidx uses bindings for OpenCV and Tesseract native libraries. The OpenCV
libraries are especially version-sensitive. To install the native Tessearact libriaries
in MacOS you can use Homebrew, as in:

    brew install tesseract

Unfortunately OpenCV 3.2 does not build properly under Homebrew. For MacOS, 
OpenCV needs to be built from source. This can be done with:

    wget https://github.com/opencv/opencv/archive/3.2.0.tar.gz
    tar xzf 3.2.0.tar.gz
    mkdir opencv-3.2.0/build
    cd opencv-3.2.0/build
    cmake .. -DBUILD_opencv_java=ON
    make
    make install

Linux distributions often ship with Tesseract and OpenCV 3.2, such as with
Ubuntu (Bionic):

    apt-get install tesseract-ocr libopencv3.2-jni

After the native libraries are installed, building and testing can be performed
locally with Maven and Spring Boot:

    mvn spring-boot:run

If you would also like to spin up a local Elasticsearch and Kibana instance for
testing, you can deploy both with Docker configs in the `tests/` directory:

    cd tests
    docker-compose up -d


## Searching and Querying Documents

To search within your documents, use DocMag available at https://github.com/deckerego/docmag

You could also query Elasticsearch directly using the API or Kibana's dev tools. A query sent over the API might be:

    GET /docidx/_search
    {"query": { "simple_query_string" :
      { "query": "water bill" }
    }}
