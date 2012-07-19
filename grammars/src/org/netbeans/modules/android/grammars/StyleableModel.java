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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A wrapper around XML files describing structure of Android XML files.
 *
 * @author Radim Kubacki
 */
class StyleableModel {

  private static final Logger LOG = Logger.getLogger(StyleableModel.class.getName());

  private final SortedMap<String, StyleableInfo> styleables = new TreeMap<String, StyleableInfo>();

  /** Lookup table of possible values for attribute names. */
  private final Map<String, AttributeInfo> attributeValues = new HashMap<String, AttributeInfo>();

  AttributeInfo getAttributeValue(String attrName) {
    return attributeValues.get(attrName);
  }

  void addAttribute(String name, AttributeInfo ai) {
    attributeValues.put(name, ai);
  }

  StyleableInfo createStyleable(String name, String description, Set<String> parents) {
    StyleableInfo s = new StyleableInfo(name, description, parents);
    LOG.log(Level.FINEST, "adding styleable {0}", s);
    styleables.put(name, s);
    return s;
  }

  public SortedMap<String, StyleableInfo> getStyleables() {
    return Collections.unmodifiableSortedMap(styleables);
  }

  @Override
  public String toString() {
    return "StyleableModel{" + "styleables=" + styleables + ", attributeValues=" + attributeValues + '}';
  }
}
