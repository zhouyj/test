# feedss
#
# docker build -t feedss/zplive .

FROM centos:centos6
MAINTAINER feedss

USER root

ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en
ENV LC_ALL en_US.UTF-8

# install dev tools
RUN yum clean all; \
    rpm --rebuilddb; \
    yum install -y curl which tar sudo openssh-server openssh-clients rsync vim

# update libselinux. see https://github.com/sequenceiq/hadoop-docker/issues/14
RUN yum update -y libselinux

# set timezone
RUN yum install -y ntpdate && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
# support chinese
RUN yum -y install kde-l10n-Chinese && \
    yum -y reinstall glibc-common && \
    sed -i 's/LANG="en_US.UTF-8"/LANG="zh_CN.UTF-8"/g' /etc/sysconfig/i18n && \
    echo 'SUPPORTED="zh_CN.UTF-8:zh_CN:zh:en_US.UTF-8:en_US:en"' >> /etc/sysconfig/i18n

# change password
RUN /bin/sh -c 'echo root:jklfds56 | chpasswd'

# passwordless ssh
RUN ssh-keygen -q -N "" -t dsa -f /etc/ssh/ssh_host_dsa_key
RUN ssh-keygen -q -N "" -t rsa -f /etc/ssh/ssh_host_rsa_key
RUN ssh-keygen -q -N "" -t rsa -f /root/.ssh/id_rsa
#RUN cp /root/.ssh/id_rsa.pub /root/.ssh/authorized_keys
ADD authorized_keys /root/.ssh/authorized_keys

RUN sed -ri 's/session    required     pam_loginuid.so/#session    required     pam_loginuid.so/g' /etc/pam.d/sshd

ADD startsshd.sh /startsshd.sh
RUN chmod 755 /startsshd.sh
EXPOSE 22
CMD ["/startsshd.sh"]

# auto start sshd
#RUN mkdir /var/run/sshd  
#EXPOSE 22  
#CMD ["/usr/sbin/sshd", "-D"]  

# java
RUN curl -LO 'http://feedss.com/jdk-8u121-linux-x64.rpm'
RUN rpm -i jdk-8u121-linux-x64.rpm
RUN rm jdk-8u121-linux-x64.rpm

ENV JAVA_HOME /usr/java/default
ENV PATH $PATH:$JAVA_HOME/bin

ADD target/feedss-2.0.jar /feedss.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/feedss.jar"]

