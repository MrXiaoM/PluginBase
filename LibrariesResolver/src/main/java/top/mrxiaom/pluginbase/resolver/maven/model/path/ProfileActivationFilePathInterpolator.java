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
package top.mrxiaom.pluginbase.resolver.maven.model.path;

import top.mrxiaom.pluginbase.resolver.plexus.interpolation.AbstractValueSource;
import top.mrxiaom.pluginbase.resolver.plexus.interpolation.InterpolationException;
import top.mrxiaom.pluginbase.resolver.plexus.interpolation.MapBasedValueSource;
import top.mrxiaom.pluginbase.resolver.plexus.interpolation.RegexBasedInterpolator;
import top.mrxiaom.pluginbase.resolver.maven.model.ActivationFile;
import top.mrxiaom.pluginbase.resolver.maven.model.profile.ProfileActivationContext;

import java.io.File;

/**
 * Finds an absolute path for {@link ActivationFile#getExists()} or {@link ActivationFile#getMissing()}
 *
 * @author Ravil Galeyev
 */
public class ProfileActivationFilePathInterpolator {
    private PathTranslator pathTranslator;

    public ProfileActivationFilePathInterpolator setPathTranslator(PathTranslator pathTranslator) {
        this.pathTranslator = pathTranslator;
        return this;
    }

    /**
     * Interpolates given {@code path}.
     *
     * @return absolute path or {@code null} if the input was {@code null}
     */
    public String interpolate(String path, ProfileActivationContext context) throws InterpolationException {
        if (path == null) {
            return null;
        }

        RegexBasedInterpolator interpolator = new RegexBasedInterpolator();

        final File basedir = context.getProjectDirectory();

        if (basedir != null) {
            interpolator.addValueSource(new AbstractValueSource(false) {
                @Override
                public Object getValue(String expression) {
                    if ("basedir".equals(expression) || "project.basedir".equals(expression)) {
                        return basedir.getAbsolutePath();
                    }
                    return null;
                }
            });
        } else if (path.contains("${basedir}")) {
            return null;
        }

        interpolator.addValueSource(new MapBasedValueSource(context.getProjectProperties()));

        interpolator.addValueSource(new MapBasedValueSource(context.getUserProperties()));

        interpolator.addValueSource(new MapBasedValueSource(context.getSystemProperties()));

        String absolutePath = interpolator.interpolate(path, "");

        return pathTranslator.alignToBaseDirectory(absolutePath, basedir);
    }
}
