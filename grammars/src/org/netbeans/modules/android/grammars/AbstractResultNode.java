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

import javax.swing.Icon;
import org.netbeans.modules.xml.api.model.GrammarResult;
import org.netbeans.modules.xml.spi.dom.AbstractNode;

/**
 *
 * @author radim
 */
public abstract class AbstractResultNode extends AbstractNode implements GrammarResult {

  private String description;

  protected AbstractResultNode() {
    this(null);
  }

  public AbstractResultNode(String description) {
    this.description = description;
  }

  @Override
  public Icon getIcon(int kind) {
    return null;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public String getDisplayName() {
    return null;
  }

  // TODO(radim): implement appropriately
  @Override
  public boolean isEmptyElement() {
    return false;
  }

}
