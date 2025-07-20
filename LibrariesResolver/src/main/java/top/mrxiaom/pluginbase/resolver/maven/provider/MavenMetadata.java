/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package top.mrxiaom.pluginbase.resolver.maven.provider;

import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.WriterFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import top.mrxiaom.pluginbase.resolver.aether.RepositoryException;
import top.mrxiaom.pluginbase.resolver.aether.metadata.AbstractMetadata;
import top.mrxiaom.pluginbase.resolver.aether.metadata.MergeableMetadata;
import top.mrxiaom.pluginbase.resolver.maven.artifact.repository.metadata.Metadata;
import top.mrxiaom.pluginbase.resolver.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import top.mrxiaom.pluginbase.resolver.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Writer;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

/**
 * @author Benjamin Bentmann
 */
abstract class MavenMetadata extends AbstractMetadata implements MergeableMetadata {

    static final String MAVEN_METADATA_XML = "maven-metadata.xml";

    protected Metadata metadata;

    private final File file;

    protected final Date timestamp;

    private boolean merged;

    protected MavenMetadata(Metadata metadata, File file, Date timestamp) {
        this.metadata = metadata;
        this.file = file;
        this.timestamp = timestamp;
    }

    @Override
    public String getType() {
        return MAVEN_METADATA_XML;
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public void merge(File existing, File result) throws RepositoryException {
        Metadata recessive = read(existing);

        merge(recessive);

        write(result, metadata);

        merged = true;
    }

    @Override
    public boolean isMerged() {
        return merged;
    }

    protected abstract void merge(Metadata recessive);

    static Metadata read(File metadataFile) throws RepositoryException {
        if (metadataFile.length() <= 0) {
            return new Metadata();
        }

        try (Reader reader = ReaderFactory.newXmlReader(metadataFile)) {
            return new MetadataXpp3Reader().read(reader, false);
        } catch (IOException e) {
            throw new RepositoryException("Could not read metadata " + metadataFile + ": " + e.getMessage(), e);
        } catch (XmlPullParserException e) {
            throw new RepositoryException("Could not parse metadata " + metadataFile + ": " + e.getMessage(), e);
        }
    }

    private void write(File metadataFile, Metadata metadata) throws RepositoryException {
        metadataFile.getParentFile().mkdirs();
        try (Writer writer = WriterFactory.newXmlWriter(metadataFile)) {
            new MetadataXpp3Writer().write(writer, metadata);
        } catch (IOException e) {
            throw new RepositoryException("Could not write metadata " + metadataFile + ": " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, String> getProperties() {
        return Collections.emptyMap();
    }

    @Override
    public top.mrxiaom.pluginbase.resolver.aether.metadata.Metadata setProperties(Map<String, String> properties) {
        return this;
    }
}
