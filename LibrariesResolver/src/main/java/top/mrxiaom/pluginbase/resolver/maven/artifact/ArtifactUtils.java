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
package top.mrxiaom.pluginbase.resolver.maven.artifact;

import java.util.regex.Pattern;

/**
 * ArtifactUtils
 */
public final class ArtifactUtils {
    public static final String SNAPSHOT_VERSION = "SNAPSHOT";
    public static final Pattern VERSION_FILE_PATTERN = Pattern.compile("^(.*)-(\\d{8}\\.\\d{6})-(\\d+)$");

    public static boolean isSnapshot(String version) {
        if (version != null) {
            if (version.regionMatches(
                    true,
                    version.length() - SNAPSHOT_VERSION.length(),
                    SNAPSHOT_VERSION,
                    0,
                    SNAPSHOT_VERSION.length())) {
                return true;
            } else {
                return VERSION_FILE_PATTERN.matcher(version).matches();
            }
        }
        return false;
    }
}
