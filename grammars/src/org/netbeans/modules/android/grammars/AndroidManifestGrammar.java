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

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.SortedMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.android.core.sdk.DalvikPlatform;
import org.netbeans.modules.xml.api.model.GrammarResult;
import org.netbeans.modules.xml.api.model.HintContext;
import org.openide.util.Enumerations;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Query implementation based on Android metadata.
 *
 * @author Radim Kubacki
 */
class AndroidManifestGrammar extends AndroidGrammar {

  private static final Logger LOG = Logger.getLogger(AndroidManifestGrammar.class.getName());

  public AndroidManifestGrammar(DalvikPlatform platform) {
    super(platform, StyleableModels.getAndroidManifestModel(platform), new NullRefResolver());
  }

  @Override
  protected Enumeration<GrammarResult> doQueryAttributes(Element ownerElement, String prefix) {
    NamedNodeMap existingAttributes = ownerElement.getAttributes();
    SortedMap<String, StyleableInfo> styleables = model.getStyleables();
    StyleableInfo elementData = styleables != null ? styleables.get(ownerElement.getTagName()) : null;
    List<AttributeInfo> possibleAttributes =
        elementData != null ? elementData.getAttributeNames() : Lists.<AttributeInfo>newArrayList();

    List<GrammarResult> list = new ArrayList<GrammarResult>();
    for (AttributeInfo attribute : possibleAttributes) {
      if (attribute.getName().startsWith(prefix)) {
        if (existingAttributes.getNamedItem(attribute.getName()) == null) {
          list.add(new SimpleAttr(attribute.getName(), attribute.getDescription()));
        }
      }
    }
    LOG.log(Level.FINE, "queryAttributes({0}) -> {1}", new Object[]{prefix, list});
    return Collections.enumeration(list);
  }

  @Override
  public Enumeration<GrammarResult> queryElements(HintContext ctx) {
    LOG.log(Level.FINE, "queryElements({0})", ctx.getCurrentPrefix());

    String parentTagName = null;
    Node parent = ((Node) ctx).getParentNode();
    if (parent != null && parent.getNodeType() == Node.ELEMENT_NODE) {
      parentTagName = ((Element) parent).getTagName();
    }

    String prefix = ctx.getCurrentPrefix();

    List<GrammarResult> list = new ArrayList<GrammarResult>();
    for(StyleableInfo s : model.getStyleables().values()) {
      if (s.getName().startsWith(prefix)
          && (parentTagName == null || s.getParentNames().contains(parentTagName))) {
        list.add(new SimpleElement(s.getName(), s.getDescription()));
      }
    }

    LOG.log(Level.FINE, "queryElements({0}) -> {1}", new Object[] { prefix, list });
    return Collections.enumeration(list);
  }

  @Override
  public Enumeration<GrammarResult> queryValues(HintContext ctx) {
    LOG.log(Level.FINE, "queryValues({0})", ctx.getCurrentPrefix());
    Attr ownerAttr;
    if (ctx.getNodeType() == Node.ATTRIBUTE_NODE) {
        ownerAttr = (Attr)ctx;
    } else {
        LOG.fine("...unknown node type");
        return Enumerations.empty();
    }
    String attrName = ownerAttr.getName();
    Element ownerElement = ownerAttr.getOwnerElement();
    List<GrammarResult> choices = new ArrayList<GrammarResult>();
    for (AttributeInfo attr : model.getStyleables().get(ownerElement.getNodeName()).getAttributeNames()) {
      if (attr.getName().equals(attrName)) {
        for (String choice : getChoices(attr, ctx.getCurrentPrefix())) {
          choices.add(new SimpleText(choice, attr.getDescription()));
        }
      }
    }
    return Collections.enumeration(choices);
  }

  // return defaults, no way to query them
  @Override
  public GrammarResult queryDefault(final HintContext ctx) {
    LOG.log(Level.FINE, "queryDefault({0})", ctx.getCurrentPrefix());
    return null;
  }

  @Override
  public String toString() {
    return "AndroidManifestGrammar for SDK " + platform + " [" + super.toString() + "]";
  }
}
