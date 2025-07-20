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
package top.mrxiaom.pluginbase.resolver.maven.artifact.versioning;

import java.util.*;

/**
 * Construct a version range from a specification.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
public class VersionRange {
    private static final Map<String, VersionRange> CACHE_SPEC =
            Collections.synchronizedMap(new WeakHashMap<>());

    private final ArtifactVersion recommendedVersion;

    private final List<Restriction> restrictions;

    private VersionRange(ArtifactVersion recommendedVersion, List<Restriction> restrictions) {
        this.recommendedVersion = recommendedVersion;
        this.restrictions = restrictions;
    }

    /**
     * <p>
     * Create a version range from a string representation
     * </p>
     * Some spec examples are:
     * <ul>
     * <li><code>1.0</code> Version 1.0 as a recommended version</li>
     * <li><code>[1.0]</code> Version 1.0 explicitly only</li>
     * <li><code>[1.0,2.0)</code> Versions 1.0 (included) to 2.0 (not included)</li>
     * <li><code>[1.0,2.0]</code> Versions 1.0 to 2.0 (both included)</li>
     * <li><code>[1.5,)</code> Versions 1.5 and higher</li>
     * <li><code>(,1.0],[1.2,)</code> Versions up to 1.0 (included) and 1.2 or higher</li>
     * </ul>
     *
     * @param spec string representation of a version or version range
     * @return a new {@link VersionRange} object that represents the spec
     *
     */
    public static VersionRange createFromVersionSpec(String spec) throws InvalidVersionSpecificationException {
        if (spec == null) {
            return null;
        }

        VersionRange cached = CACHE_SPEC.get(spec);
        if (cached != null) {
            return cached;
        }

        List<Restriction> restrictions = new ArrayList<>();
        String process = spec;
        ArtifactVersion version = null;
        ArtifactVersion upperBound = null;
        ArtifactVersion lowerBound = null;

        while (process.startsWith("[") || process.startsWith("(")) {
            int index1 = process.indexOf(')');
            int index2 = process.indexOf(']');

            int index = index2;
            if (index2 < 0 || index1 < index2) {
                if (index1 >= 0) {
                    index = index1;
                }
            }

            if (index < 0) {
                throw new InvalidVersionSpecificationException("Unbounded range: " + spec);
            }

            Restriction restriction = parseRestriction(process.substring(0, index + 1));
            if (lowerBound == null) {
                lowerBound = restriction.getLowerBound();
            }
            if (upperBound != null) {
                if (restriction.getLowerBound() == null
                        || restriction.getLowerBound().compareTo(upperBound) < 0) {
                    throw new InvalidVersionSpecificationException("Ranges overlap: " + spec);
                }
            }
            restrictions.add(restriction);
            upperBound = restriction.getUpperBound();

            process = process.substring(index + 1).trim();

            if (process.startsWith(",")) {
                process = process.substring(1).trim();
            }
        }

        if (!process.isEmpty()) {
            if (!restrictions.isEmpty()) {
                throw new InvalidVersionSpecificationException(
                        "Only fully-qualified sets allowed in multiple set scenario: " + spec);
            } else {
                version = new DefaultArtifactVersion(process);
                restrictions.add(Restriction.EVERYTHING);
            }
        }

        cached = new VersionRange(version, restrictions);
        CACHE_SPEC.put(spec, cached);
        return cached;
    }

    private static Restriction parseRestriction(String spec) throws InvalidVersionSpecificationException {
        boolean lowerBoundInclusive = spec.startsWith("[");
        boolean upperBoundInclusive = spec.endsWith("]");

        String process = spec.substring(1, spec.length() - 1).trim();

        Restriction restriction;

        int index = process.indexOf(',');

        if (index < 0) {
            if (!lowerBoundInclusive || !upperBoundInclusive) {
                throw new InvalidVersionSpecificationException("Single version must be surrounded by []: " + spec);
            }

            ArtifactVersion version = new DefaultArtifactVersion(process);

            restriction = new Restriction(version, lowerBoundInclusive, version, upperBoundInclusive);
        } else {
            String lowerBound = process.substring(0, index).trim();
            String upperBound = process.substring(index + 1).trim();

            ArtifactVersion lowerVersion = null;
            if (!lowerBound.isEmpty()) {
                lowerVersion = new DefaultArtifactVersion(lowerBound);
            }
            ArtifactVersion upperVersion = null;
            if (!upperBound.isEmpty()) {
                upperVersion = new DefaultArtifactVersion(upperBound);
            }

            if (upperVersion != null && lowerVersion != null) {
                int result = upperVersion.compareTo(lowerVersion);
                if (result < 0 || (result == 0 && (!lowerBoundInclusive || !upperBoundInclusive))) {
                    throw new InvalidVersionSpecificationException("Range defies version ordering: " + spec);
                }
            }

            restriction = new Restriction(lowerVersion, lowerBoundInclusive, upperVersion, upperBoundInclusive);
        }

        return restriction;
    }

    public String toString() {
        if (recommendedVersion != null) {
            return recommendedVersion.toString();
        } else {
            StringBuilder buf = new StringBuilder();
            for (Iterator<Restriction> i = restrictions.iterator(); i.hasNext(); ) {
                Restriction r = i.next();

                buf.append(r.toString());

                if (i.hasNext()) {
                    buf.append(',');
                }
            }
            return buf.toString();
        }
    }

    public boolean containsVersion(ArtifactVersion version) {
        for (Restriction restriction : restrictions) {
            if (restriction.containsVersion(version)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasRestrictions() {
        return !restrictions.isEmpty() && recommendedVersion == null;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof VersionRange)) {
            return false;
        }
        VersionRange other = (VersionRange) obj;

        return Objects.equals(recommendedVersion, other.recommendedVersion)
                && Objects.equals(restrictions, other.restrictions);
    }

    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (recommendedVersion == null ? 0 : recommendedVersion.hashCode());
        hash = 31 * hash + (restrictions == null ? 0 : restrictions.hashCode());
        return hash;
    }
}
