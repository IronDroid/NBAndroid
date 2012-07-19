/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.netbeans.modules.android.grammars;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Object that can be included in Android XML file to describe view or view layout element.
 *
 * @author radim
 */
class AttributeInfo {

  /** An attribute format, e.g. string, reference, float, etc. */
  public enum Format {
    STRING,
    BOOLEAN,
    INTEGER,
    FLOAT,
    REFERENCE,
    COLOR,
    DIMENSION,
    FRACTION,
    ENUM,
    FLAG
  }

  private final String name;
  /*Nullable*/
  private final String description;
  private final Set<Format> formats = Sets.newHashSet();
  private final List<String> enums = Lists.newArrayList();

  public AttributeInfo(String name, String description) {
    this.name = Preconditions.checkNotNull(name);
    this.description = description;
  }

  void addEnum(String enumValue) {
    enums.add(enumValue);
  }

  void addFormat(Format format) {
    formats.add(format);
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public Iterable<Format> getFormats() {
    return Collections.unmodifiableSet(formats);
  }

  public Iterable<String> getEnumValues() {
    return Collections.unmodifiableList(enums);
  }

  @Override
  public String toString() {
    return "AttributeInfo{" +
        "name=" + name +
        // ", description=" + description +
        ", formats=" +
        formats +
        ", enums=" + enums + '}';
  }

}
