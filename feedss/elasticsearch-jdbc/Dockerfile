FROM centos:centos6

# install dev tools
RUN yum clean all; \
    rpm --rebuilddb; \
    yum install -y unzip
    
# java
RUN curl -LO 'http://feedss.com/jdk-8u121-linux-x64.rpm' && \
    rpm -i jdk-8u121-linux-x64.rpm && \
    rm jdk-8u121-linux-x64.rpm
ENV JAVA_HOME /usr/java/default
ENV PATH $PATH:$JAVA_HOME/bin

# elasticsearch-jdbc
ENV IMPORTER_VERSION 2.3.4.1
RUN curl -LO http://xbib.org/repository/org/xbib/elasticsearch/importer/elasticsearch-jdbc/${IMPORTER_VERSION}/elasticsearch-jdbc-${IMPORTER_VERSION}-dist.zip && \
	unzip elasticsearch-jdbc-${IMPORTER_VERSION}-dist.zip && \
	rm ./elasticsearch-jdbc-${IMPORTER_VERSION}-dist.zip && \
	mv ./elasticsearch-jdbc-${IMPORTER_VERSION} /usr/local
ENV PATH /usr/local/elasticsearch-jdbc-${IMPORTER_VERSION}/bin:$PATH

COPY live-stream.sh /usr/local/elasticsearch-jdbc-${IMPORTER_VERSION}/bin/live-stream.sh
COPY live-profile.sh /usr/local/elasticsearch-jdbc-${IMPORTER_VERSION}/bin/live-profile.sh
COPY ./docker-entrypoint.sh /

RUN chmod 755 \ 
	/docker-entrypoint.sh \
    /usr/local/elasticsearch-jdbc-${IMPORTER_VERSION}/bin/live-stream.sh \
    /usr/local/elasticsearch-jdbc-${IMPORTER_VERSION}/bin/live-profile.sh

ENTRYPOINT ["/docker-entrypoint.sh"]
CMD []