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

import top.mrxiaom.pluginbase.resolver.plexus.util.StringUtils;
import top.mrxiaom.pluginbase.resolver.maven.model.Activation;
import top.mrxiaom.pluginbase.resolver.maven.model.ActivationProperty;
import top.mrxiaom.pluginbase.resolver.maven.model.Profile;
import top.mrxiaom.pluginbase.resolver.maven.model.building.ModelProblem;
import top.mrxiaom.pluginbase.resolver.maven.model.building.ModelProblemCollector;
import top.mrxiaom.pluginbase.resolver.maven.model.building.ModelProblemCollectorRequest;
import top.mrxiaom.pluginbase.resolver.maven.model.profile.ProfileActivationContext;

/**
 * Determines profile activation based on the existence or value of some execution property.
 *
 * @author Benjamin Bentmann
 * @see ActivationProperty
 */
public class PropertyProfileActivator implements ProfileActivator {

    @Override
    public boolean isActive(Profile profile, ProfileActivationContext context, ModelProblemCollector problems) {
        Activation activation = profile.getActivation();

        if (activation == null) {
            return false;
        }

        ActivationProperty property = activation.getProperty();

        if (property == null) {
            return false;
        }

        String name = property.getName();
        boolean reverseName = false;

        if (name != null && name.startsWith("!")) {
            reverseName = true;
            name = name.substring(1);
        }

        if (name == null || name.isEmpty()) {
            problems.add(new ModelProblemCollectorRequest(ModelProblem.Severity.ERROR, ModelProblem.Version.BASE)
                    .setMessage("The property name is required to activate the profile " + profile.getId())
                    .setLocation(property.getLocation("")));
            return false;
        }

        String sysValue = context.getUserProperties().get(name);
        if (sysValue == null) {
            sysValue = context.getSystemProperties().get(name);
        }

        String propValue = property.getValue();
        if (StringUtils.isNotEmpty(propValue)) {
            boolean reverseValue = false;
            if (propValue.startsWith("!")) {
                reverseValue = true;
                propValue = propValue.substring(1);
            }

            // we have a value, so it has to match the system value...
            boolean result = propValue.equals(sysValue);

            return reverseValue != result;
        } else {
            boolean result = StringUtils.isNotEmpty(sysValue);

            return reverseName != result;
        }
    }

    @Override
    public boolean presentInConfig(Profile profile, ProfileActivationContext context, ModelProblemCollector problems) {
        Activation activation = profile.getActivation();

        if (activation == null) {
            return false;
        }

        ActivationProperty property = activation.getProperty();

        return property != null;
    }
}
