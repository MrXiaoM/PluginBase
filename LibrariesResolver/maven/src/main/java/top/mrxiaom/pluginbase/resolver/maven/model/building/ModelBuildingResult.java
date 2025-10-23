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
import top.mrxiaom.pluginbase.resolver.maven.model.Profile;

import java.util.List;

/**
 * Collects the output of the model builder.
 *
 * @author Benjamin Bentmann
 */
public interface ModelBuildingResult {

    /**
     * Gets the sequence of model identifiers that denote the lineage of models from which the effective model was
     * constructed. Model identifiers have the form {@code <groupId>:<artifactId>:<version>}. The first identifier from
     * the list denotes the model on which the model builder was originally invoked. The last identifier will always be
     * an empty string that by definition denotes the super POM.
     *
     * @return The model identifiers from the lineage of models, never {@code null}.
     */
    List<String> getModelIds();

    /**
     * Gets the assembled model.
     *
     * @return The assembled model, never {@code null}.
     */
    Model getEffectiveModel();

    /**
     * Gets the raw model as it was read from the input model source. Apart from basic validation, the raw model has not
     * undergone any updates by the model builder, e.g. reflects neither inheritance nor interpolation.
     *
     * @return The raw model, never {@code null}.
     */
    Model getRawModel();

    /**
     * Gets the external profiles that were active during model building. External profiles are those that were
     * contributed by {@link ModelBuildingRequest#getProfiles()}.
     *
     * @return The active external profiles or an empty list if none, never {@code null}.
     */
    List<Profile> getActiveExternalProfiles();

    /**
     * Gets the problems that were encountered during the model building. Note that only problems of severity
     * {@link ModelProblem.Severity#WARNING} and below are reported here. Problems with a higher severity level cause
     * the model builder to fail with a {@link ModelBuildingException}.
     *
     * @return The problems that were encountered during the model building, can be empty but never {@code null}.
     */
    List<ModelProblem> getProblems();
}
