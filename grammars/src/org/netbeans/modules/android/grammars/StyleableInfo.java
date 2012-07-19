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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Object that can be included in Android XML file to describe view or view layout element.
 *
 * @author radim
 */
class StyleableInfo {
  private final String name;
  /*Nullable*/
  private final String description;
  private final Set<String> parents = new HashSet<String>();
  private final List<AttributeInfo> attributeNames = Lists.newArrayList();

  public StyleableInfo(String name, String description, Set<String> parents) {
    this.name = Preconditions.checkNotNull(name);
    this.description = description;
    this.parents.addAll(parents);
  }

  void addAttribute(AttributeInfo attr) {
    attributeNames.add(attr);
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  /** Names of parent tags that can contain this styleable. Applicable to tag styleables. */
  public Set<String> getParentNames() {
    return Collections.unmodifiableSet(parents);
  }

  public List<AttributeInfo> getAttributeNames() {
    return Collections.unmodifiableList(attributeNames);
  }

  @Override
  public String toString() {
    return "Styleable{" + "name=" + name + ", parents=" + parents + ", attributeNames=" + attributeNames + '}';
  }

}
