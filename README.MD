[![Build Status](https://travis-ci.org/barakb/versions-maven-plugin.svg?branch=master)](https://travis-ci.org/barakb/versions-maven-plugin) 

Versions maven plugin
==========================

## DESCRIPTION
Update pom version in project, can be used with projects with interdenendency.

Maven usage:

```xml

    <repositories>
        <repository>
            <id>sonatype-repo</id>
            <name>sonatype</name>
            <url>https://oss.sonatype.org/content/groups/public</url>
        </repository>
    </repositories>
    <build>
        <plugins>
            <plugin>
                <groupId>com.github.barakb</groupId>
                <artifactId>versions-maven-plugin</artifactId>
                <version>1.2</version>
                <configuration>
                    <includes>
                        <include>org.xap:*</include>
                        <include>com.gigaspaces:*</include>
                        <include>com.xap:*</include>
                        <include>org.openspaces:*</include>
                        <include>com.gs.webui:*</include>
                        <include>*:xap-maven-plugin:*</include>
                        <include>com.gigaspaces.http-session:*</include>
                        <include>com.gigaspaces.http-session.qa:*</include>
                        <include>com.gigaspaces.mongo-datasource-itests:*</include>
                        <include>com.gigaspaces.petclinic:*</include>
                        <include>com.gigaspaces.quality:*</include>
                        <include>com.gigaspaces.quality.sgtest:*</include>
                        <include>com.gigaspaces.quality.sgtest.apps:*</include>
                        <include>com.gigaspaces.quality.sgtest.apps.abstract-bean:*</include>
                        <include>com.gigaspaces.quality.sgtest.apps.blobstore-example:*</include>
                        <include>com.gigaspaces.quality.sgtest.apps.cassandraEDS:*</include>
                        <include>com.gigaspaces.quality.sgtest.apps.data:*</include>
                        <include>com.gigaspaces.quality.sgtest.apps.durable-notify:*</include>
                        <include>com.gigaspaces.quality.sgtest.apps.gateway:*</include>
                        <include>com.gigaspaces.quality.sgtest.apps.gateway.hardcoded:*</include>
                        <include>com.gigaspaces.quality.sgtest.apps.gatewayPUs:*</include>
                        <include>com.gigaspaces.quality.sgtest.apps.gateway.security:*</include>
                        <include>com.gigaspaces.quality.sgtest.apps.initial-load-query:*</include>
                        <include>com.gigaspaces.quality.sgtest.apps.listeners:*</include>
                        <include>com.gigaspaces.quality.sgtest.apps.local-view:*</include>
                        <include>com.gigaspaces.quality.sgtest.apps.manifest-classpath:*</include>
                        <include>com.gigaspaces.quality.sgtest.apps.many-listeners:*</include>
                        <include>com.gigaspaces.quality.sgtest.apps.MHEDS:*</include>
                        <include>com.gigaspaces.quality.sgtest.apps.MHEDS-v2:*</include>
                        <include>com.gigaspaces.quality.sgtest.apps.MHEDS-zk:*</include>
                        <include>com.gigaspaces.quality.sgtest.apps.processorAndMirror:*</include>
                        <include>com.gigaspaces.quality.sgtest.apps.rocksdb-example:*</include>
                        <include>com.gigaspaces.quality.sgtest.apps.rocksdb-zookeeper:*</include>
                        <include>com.gigaspaces.quality.sgtest.apps.rocksdb-zookeeper-leader-selector-zones:*</include>
                        <include>com.gigaspaces.quality.sgtest.apps.rocksdb-zookeeper-zones:*</include>
                        <include>com.gigaspaces.quality.sgtest.apps.security:*</include>
                        <include>com.gigaspaces.quality.sgtest.apps.simpledata:*</include>
                        <include>com.gigaspaces.quality.sgtest.apps.stateless:*</include>
                        <include>com.gigaspaces.quality.sgtest.apps.support:*</include>
                        <include>com.gigaspaces.quality.sgtest.apps.systemexitdata:*</include>
                        <include>com.gigaspaces.quality.sgtest.apps.user-defined-sql-function:*</include>
                        <include>com.gigaspaces.quality.sgtest.apps.wan:*</include>
                        <include>com.gigaspaces.quality.sgtest.apps.zones:*</include>
                        <include>com.gigaspaces.quality.sgtest.apps.zookeeper-leader-selector:*</include>
                        <include>org.openspaces.example.data:*</include>
                        <include>org.openspaces.example.helloworld:*</include>
                        <include>org.openspaces.example.web:*</include>
                    </includes>
                    <excludes>
                        <exclude>com.gigaspaces.quality:tgrid:pom:*</exclude>
                        <exclude>com.gigaspaces.quality:metric-core:*</exclude>
                        <exclude>com.gigaspaces.quality:benchmark:*</exclude>
                    </excludes>
                </configuration>
            </plugin>

```
