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
package top.mrxiaom.pluginbase.resolver.maven.model.profile.activation;

import top.mrxiaom.pluginbase.resolver.plexus.interpolation.InterpolationException;
import top.mrxiaom.pluginbase.resolver.plexus.util.StringUtils;
import top.mrxiaom.pluginbase.resolver.maven.model.Activation;
import top.mrxiaom.pluginbase.resolver.maven.model.ActivationFile;
import top.mrxiaom.pluginbase.resolver.maven.model.Profile;
import top.mrxiaom.pluginbase.resolver.maven.model.building.ModelProblem;
import top.mrxiaom.pluginbase.resolver.maven.model.building.ModelProblemCollector;
import top.mrxiaom.pluginbase.resolver.maven.model.building.ModelProblemCollectorRequest;
import top.mrxiaom.pluginbase.resolver.maven.model.path.ProfileActivationFilePathInterpolator;
import top.mrxiaom.pluginbase.resolver.maven.model.profile.ProfileActivationContext;
import top.mrxiaom.pluginbase.resolver.maven.model.validation.DefaultModelValidator;

import java.io.File;

/**
 * Determines profile activation based on the existence/absence of some file.
 * File name interpolation support is limited to <code>${project.basedir}</code>
 * system properties and user properties.
 *
 * @author Benjamin Bentmann
 * @see ActivationFile
 * @see DefaultModelValidator#validateRawModel
 */
public class FileProfileActivator implements ProfileActivator {

    private ProfileActivationFilePathInterpolator profileActivationFilePathInterpolator;

    public FileProfileActivator setProfileActivationFilePathInterpolator(
            ProfileActivationFilePathInterpolator profileActivationFilePathInterpolator) {
        this.profileActivationFilePathInterpolator = profileActivationFilePathInterpolator;
        return this;
    }

    @Override
    public boolean isActive(Profile profile, ProfileActivationContext context, ModelProblemCollector problems) {
        Activation activation = profile.getActivation();

        if (activation == null) {
            return false;
        }

        ActivationFile file = activation.getFile();

        if (file == null) {
            return false;
        }

        String path;
        boolean missing;

        if (StringUtils.isNotEmpty(file.getExists())) {
            path = file.getExists();
            missing = false;
        } else if (StringUtils.isNotEmpty(file.getMissing())) {
            path = file.getMissing();
            missing = true;
        } else {
            return false;
        }

        try {
            path = profileActivationFilePathInterpolator.interpolate(path, context);
        } catch (InterpolationException e) {
            problems.add(new ModelProblemCollectorRequest(ModelProblem.Severity.ERROR, ModelProblem.Version.BASE)
                    .setMessage("Failed to interpolate file location " + path + " for profile " + profile.getId() + ": "
                            + e.getMessage())
                    .setLocation(file.getLocation(missing ? "missing" : "exists"))
                    .setException(e));
            return false;
        }

        if (path == null) {
            return false;
        }

        File f = new File(path);

        if (!f.isAbsolute()) {
            return false;
        }

        boolean fileExists = f.exists();

        return missing != fileExists;
    }

    @Override
    public boolean presentInConfig(Profile profile, ProfileActivationContext context, ModelProblemCollector problems) {
        Activation activation = profile.getActivation();

        if (activation == null) {
            return false;
        }

        ActivationFile file = activation.getFile();

        return file != null;
    }
}
