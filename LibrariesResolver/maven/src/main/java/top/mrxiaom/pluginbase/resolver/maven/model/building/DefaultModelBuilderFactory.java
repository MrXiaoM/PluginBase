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
package top.mrxiaom.pluginbase.resolver.maven.model.building;

import top.mrxiaom.pluginbase.resolver.maven.model.Model;
import top.mrxiaom.pluginbase.resolver.maven.model.composition.DefaultDependencyManagementImporter;
import top.mrxiaom.pluginbase.resolver.maven.model.composition.DependencyManagementImporter;
import top.mrxiaom.pluginbase.resolver.maven.model.inheritance.DefaultInheritanceAssembler;
import top.mrxiaom.pluginbase.resolver.maven.model.inheritance.InheritanceAssembler;
import top.mrxiaom.pluginbase.resolver.maven.model.interpolation.DefaultModelVersionProcessor;
import top.mrxiaom.pluginbase.resolver.maven.model.interpolation.ModelInterpolator;
import top.mrxiaom.pluginbase.resolver.maven.model.interpolation.ModelVersionProcessor;
import top.mrxiaom.pluginbase.resolver.maven.model.interpolation.StringVisitorModelInterpolator;
import top.mrxiaom.pluginbase.resolver.maven.model.io.DefaultModelReader;
import top.mrxiaom.pluginbase.resolver.maven.model.io.ModelReader;
import top.mrxiaom.pluginbase.resolver.maven.model.locator.DefaultModelLocator;
import top.mrxiaom.pluginbase.resolver.maven.model.locator.ModelLocator;
import top.mrxiaom.pluginbase.resolver.maven.model.management.DefaultDependencyManagementInjector;
import top.mrxiaom.pluginbase.resolver.maven.model.management.DefaultPluginManagementInjector;
import top.mrxiaom.pluginbase.resolver.maven.model.management.DependencyManagementInjector;
import top.mrxiaom.pluginbase.resolver.maven.model.management.PluginManagementInjector;
import top.mrxiaom.pluginbase.resolver.maven.model.normalization.DefaultModelNormalizer;
import top.mrxiaom.pluginbase.resolver.maven.model.normalization.ModelNormalizer;
import top.mrxiaom.pluginbase.resolver.maven.model.path.*;
import top.mrxiaom.pluginbase.resolver.maven.model.plugin.*;
import top.mrxiaom.pluginbase.resolver.maven.model.profile.DefaultProfileInjector;
import top.mrxiaom.pluginbase.resolver.maven.model.profile.DefaultProfileSelector;
import top.mrxiaom.pluginbase.resolver.maven.model.profile.ProfileInjector;
import top.mrxiaom.pluginbase.resolver.maven.model.profile.ProfileSelector;
import top.mrxiaom.pluginbase.resolver.maven.model.profile.activation.*;
import top.mrxiaom.pluginbase.resolver.maven.model.superpom.DefaultSuperPomProvider;
import top.mrxiaom.pluginbase.resolver.maven.model.superpom.SuperPomProvider;
import top.mrxiaom.pluginbase.resolver.maven.model.validation.DefaultModelValidator;
import top.mrxiaom.pluginbase.resolver.maven.model.validation.ModelValidator;

/**
 * A factory to create model builder instances when no dependency injection is available. <em>Note:</em> This class is
 * only meant as a utility for developers that want to employ the model builder outside of the Maven build system, Maven
 * plugins should always acquire model builder instances via dependency injection. Developers might want to subclass
 * this factory to provide custom implementations for some of the components used by the model builder.
 *
 * @author Benjamin Bentmann
 */
public class DefaultModelBuilderFactory {

    protected ModelProcessor newModelProcessor() {
        DefaultModelProcessor processor = new DefaultModelProcessor();
        processor.setModelLocator(newModelLocator());
        processor.setModelReader(newModelReader());
        return processor;
    }

    protected ModelLocator newModelLocator() {
        return new DefaultModelLocator();
    }

    protected ModelReader newModelReader() {
        return new DefaultModelReader();
    }

    protected ProfileSelector newProfileSelector() {
        DefaultProfileSelector profileSelector = new DefaultProfileSelector();

        for (ProfileActivator activator : newProfileActivators()) {
            profileSelector.addProfileActivator(activator);
        }

        return profileSelector;
    }

    protected ProfileActivator[] newProfileActivators() {
        return new ProfileActivator[] {
            new JdkVersionProfileActivator(),
            new OperatingSystemProfileActivator(),
            new PropertyProfileActivator(),
            new FileProfileActivator()
                    .setProfileActivationFilePathInterpolator(newProfileActivationFilePathInterpolator())
        };
    }

    protected ProfileActivationFilePathInterpolator newProfileActivationFilePathInterpolator() {
        return new ProfileActivationFilePathInterpolator().setPathTranslator(newPathTranslator());
    }

    protected UrlNormalizer newUrlNormalizer() {
        return new DefaultUrlNormalizer();
    }

    protected PathTranslator newPathTranslator() {
        return new DefaultPathTranslator();
    }

    protected ModelInterpolator newModelInterpolator() {
        UrlNormalizer normalizer = newUrlNormalizer();
        PathTranslator pathTranslator = newPathTranslator();
        return new StringVisitorModelInterpolator()
                .setPathTranslator(pathTranslator)
                .setUrlNormalizer(normalizer)
                .setVersionPropertiesProcessor(newModelVersionPropertiesProcessor());
    }

    protected ModelVersionProcessor newModelVersionPropertiesProcessor() {
        return new DefaultModelVersionProcessor();
    }

    protected ModelValidator newModelValidator() {
        return new DefaultModelValidator(newModelVersionPropertiesProcessor());
    }

    protected ModelNormalizer newModelNormalizer() {
        return new DefaultModelNormalizer();
    }

    protected ModelPathTranslator newModelPathTranslator() {
        return new DefaultModelPathTranslator().setPathTranslator(newPathTranslator());
    }

    protected ModelUrlNormalizer newModelUrlNormalizer() {
        return new DefaultModelUrlNormalizer().setUrlNormalizer(newUrlNormalizer());
    }

    protected InheritanceAssembler newInheritanceAssembler() {
        return new DefaultInheritanceAssembler();
    }

    protected ProfileInjector newProfileInjector() {
        return new DefaultProfileInjector();
    }

    protected SuperPomProvider newSuperPomProvider() {
        return new DefaultSuperPomProvider().setModelProcessor(newModelProcessor());
    }

    protected DependencyManagementImporter newDependencyManagementImporter() {
        return new DefaultDependencyManagementImporter();
    }

    protected DependencyManagementInjector newDependencyManagementInjector() {
        return new DefaultDependencyManagementInjector();
    }

    protected LifecycleBindingsInjector newLifecycleBindingsInjector() {
        return new StubLifecycleBindingsInjector();
    }

    protected PluginManagementInjector newPluginManagementInjector() {
        return new DefaultPluginManagementInjector();
    }

    protected PluginConfigurationExpander newPluginConfigurationExpander() {
        return new DefaultPluginConfigurationExpander();
    }

    protected ReportConfigurationExpander newReportConfigurationExpander() {
        return new DefaultReportConfigurationExpander();
    }

    protected ReportingConverter newReportingConverter() {
        return new DefaultReportingConverter();
    }

    /**
     * Creates a new model builder instance.
     *
     * @return The new model builder instance, never {@code null}.
     */
    public DefaultModelBuilder newInstance() {
        return new DefaultModelBuilder()
                .setModelProcessor(newModelProcessor())
                .setModelValidator(newModelValidator())
                .setModelNormalizer(newModelNormalizer())
                .setModelPathTranslator(newModelPathTranslator())
                .setModelUrlNormalizer(newModelUrlNormalizer())
                .setModelInterpolator(newModelInterpolator())
                .setInheritanceAssembler(newInheritanceAssembler())
                .setProfileInjector(newProfileInjector())
                .setProfileSelector(newProfileSelector())
                .setSuperPomProvider(newSuperPomProvider())
                .setDependencyManagementImporter(newDependencyManagementImporter())
                .setDependencyManagementInjector(newDependencyManagementInjector())
                .setLifecycleBindingsInjector(newLifecycleBindingsInjector())
                .setPluginManagementInjector(newPluginManagementInjector())
                .setPluginConfigurationExpander(newPluginConfigurationExpander())
                .setReportConfigurationExpander(newReportConfigurationExpander())
                .setReportingConverter(newReportingConverter())
                .setProfileActivationFilePathInterpolator(newProfileActivationFilePathInterpolator());
    }

    private static class StubLifecycleBindingsInjector implements LifecycleBindingsInjector {

        @Override
        public void injectLifecycleBindings(
                Model model, ModelBuildingRequest request, ModelProblemCollector problems) {}
    }
}
