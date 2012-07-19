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
package org.netbeans.modules.android.grammars.resources;

import java.util.Collections;

/**
 * Description of an element used in XML files under {@code res/values}.
 * @author radim
 */
public class ResElementDescriptor {

  private final String name;
  private final String description;
  private final Iterable<ResElementDescriptor> children;
  private final Iterable<ResAttrDescriptor> attrs;

  public ResElementDescriptor(String name, String description,
      Iterable<ResAttrDescriptor> attrs, Iterable<ResElementDescriptor> children) {
    this.name = name;
    this.description = description;
    this.children = children != null ? children : Collections.<ResElementDescriptor>emptyList();
    this.attrs = attrs != null ? attrs : Collections.<ResAttrDescriptor>emptyList();
  }

  public String getName() {
    return name;
  }

  public Iterable<ResAttrDescriptor> getAttrs() {
    return attrs;
  }

  public Iterable<ResElementDescriptor> getChildren() {
    return children;
  }

  public String getDescription() {
    return description;
  }

  @Override
  public String toString() {
    return "ResElementDescriptor{" + "name=" + name + '}';
  }
}
