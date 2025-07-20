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
package top.mrxiaom.pluginbase.resolver.aether.internal.impl;

import java.util.Calendar;

import top.mrxiaom.pluginbase.resolver.aether.RepositorySystemSession;
import top.mrxiaom.pluginbase.resolver.aether.impl.UpdatePolicyAnalyzer;
import top.mrxiaom.pluginbase.resolver.aether.repository.RepositoryPolicy;

import static java.util.Objects.requireNonNull;

public class DefaultUpdatePolicyAnalyzer implements UpdatePolicyAnalyzer {

    public DefaultUpdatePolicyAnalyzer() {
        // enables default constructor
    }

    public String getEffectiveUpdatePolicy(RepositorySystemSession session, String policy1, String policy2) {
        requireNonNull(session, "session cannot be null");
        return ordinalOfUpdatePolicy(policy1) < ordinalOfUpdatePolicy(policy2) ? policy1 : policy2;
    }

    @SuppressWarnings({"checkstyle:magicnumber"})
    private int ordinalOfUpdatePolicy(String policy) {
        if (RepositoryPolicy.UPDATE_POLICY_DAILY.equals(policy)) {
            return 1440;
        } else if (RepositoryPolicy.UPDATE_POLICY_ALWAYS.equals(policy)) {
            return 0;
        } else if (policy != null && policy.startsWith(RepositoryPolicy.UPDATE_POLICY_INTERVAL)) {
            return getMinutes(policy);
        } else {
            // assume "never"
            return Integer.MAX_VALUE;
        }
    }

    public boolean isUpdatedRequired(RepositorySystemSession session, long lastModified, String policy) {
        requireNonNull(session, "session cannot be null");
        boolean checkForUpdates;

        if (policy == null) {
            policy = "";
        }

        if (RepositoryPolicy.UPDATE_POLICY_ALWAYS.equals(policy)) {
            checkForUpdates = true;
        } else if (RepositoryPolicy.UPDATE_POLICY_DAILY.equals(policy)) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            checkForUpdates = cal.getTimeInMillis() > lastModified;
        } else if (policy.startsWith(RepositoryPolicy.UPDATE_POLICY_INTERVAL)) {
            int minutes = getMinutes(policy);

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MINUTE, -minutes);

            checkForUpdates = cal.getTimeInMillis() > lastModified;
        } else {
            // assume "never"
            checkForUpdates = false;
        }

        return checkForUpdates;
    }

    @SuppressWarnings({"checkstyle:magicnumber"})
    private int getMinutes(String policy) {
        try {
            String s = policy.substring(RepositoryPolicy.UPDATE_POLICY_INTERVAL.length() + 1);
            return Integer.parseInt(s);
        } catch (RuntimeException e) {
            return  24 * 60;
        }
    }
}
