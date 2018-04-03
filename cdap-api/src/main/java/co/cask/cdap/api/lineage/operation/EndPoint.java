/*
 * Copyright © 2018 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package co.cask.cdap.api.lineage.operation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;

/**
 * EndPoint in the operation represents the data source or data sink along
 * with the properties associated with it.
 */
public class EndPoint {
  private final String name;
  private final String namespace;
  private final Map<String, String> properties;

  private EndPoint(String name, @Nullable String namespace, Map<String, String> properties) {
    this.name = name;
    this.namespace = namespace;
    this.properties = Collections.unmodifiableMap(new HashMap<>(properties));
  }

  /**
   * @return the name of the {@link EndPoint}
   */
  public String getName() {
    return name;
  }

  /**
   * @return the properties associated with the {@link EndPoint} for the lineage purpose
   */
  public Map<String, String> getProperties() {
    return properties;
  }

  /**
   * @return the namespace name if it is explicitly provided while creating this EndPoint,
   * otherwise {@code null} is returned
   */
  @Nullable
  public String getNamespace() {
    return namespace;
  }

  /**
   * Return the EndPoint as defined by the provided name.
   * @param name the name of the EndPoint
   * @return the EndPoint
   */
  public static EndPoint of(String name) {
    return of(name, null, Collections.emptyMap());
  }

  /**
   * Return the EndPoint as defined by the provided name.
   * @param name the name of the EndPoint
   * @param namespace the name of the namespace. EndPoint is associated with the
   *                  namespace when it is non {@code null}, otherwise
   *                  the namespace in which program runs is considered as the
   *                  namespace for the EndPoint
   * @return the EndPoint
   */
  public static EndPoint of(String name, @Nullable String namespace) {
    return of(name, namespace, Collections.emptyMap());
  }

  /**
   * Return the EndPoint as defined by the provided name.
   * @param name the name of the EndPoint
   * @param properties the properties to be associated with the EndPoint for lineage purpose
   */
  public static EndPoint of(String name, Map<String, String> properties) {
    return of(name, null, properties);
  }

  /**
   * Return the EndPoint as defined by the provided name.
   * @param name the name of the EndPoint
   * @param namespace the name of the namespace. EndPoint is associated with the
   *                  namespace when it is non {@code null}, otherwise
   *                  the namespace in which program runs is considered as the
   *                  namespace for the EndPoint
   * @param properties the properties to be associated with the EndPoint for lineage purpose
   * @return the EndPoint
   */
  public static EndPoint of(String name, @Nullable String namespace, Map<String, String> properties) {
    return new EndPoint(name, namespace, properties);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    EndPoint that = (EndPoint) o;

    return Objects.equals(name, that.name)
      && Objects.equals(namespace, that.namespace)
      && Objects.equals(properties, that.properties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, namespace, properties);
  }
}
