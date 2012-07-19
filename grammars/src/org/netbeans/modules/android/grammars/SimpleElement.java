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

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author radim
 */
public class SimpleElement extends AbstractResultNode implements Element {
  String name;

  public SimpleElement(String name, String desc) {
    super(desc);
    this.name = name;
  }

  @Override
  public short getNodeType() {
    return Node.ELEMENT_NODE;
  }

  @Override
  public String getNodeName() {
    return name;
  }

  @Override
  public String getTagName() {
    return name;
  }

  @Override
  public String toString() {
    return name;
  }

}
