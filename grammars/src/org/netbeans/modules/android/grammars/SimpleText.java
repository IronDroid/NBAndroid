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

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 *
 * @author radim
 */
class SimpleText extends AbstractResultNode implements Text {
  String data;

  SimpleText(String data, String description) {
    super(description);
    this.data = data;
  }

  @Override
  public short getNodeType() {
    return Node.TEXT_NODE;
  }

  @Override
  public String getNodeValue() {
    return data;
  }

  @Override
  public String getData() throws DOMException {
    return data;
  }

  @Override
  public int getLength() {
    return data == null ? -1 : data.length();
  }

  @Override
  public String toString() {
    return data;
  }

  @Override
  public String getDisplayName() {
    return data; // #113804
  }

}
