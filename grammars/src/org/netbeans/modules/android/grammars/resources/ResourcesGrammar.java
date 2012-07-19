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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.android.grammars.AbstractResultNode;
import org.netbeans.modules.android.grammars.SimpleAttr;
import org.netbeans.modules.android.grammars.SimpleElement;
import org.netbeans.modules.android.grammars.SimpleEntityReference;
import org.netbeans.modules.xml.api.model.GrammarQuery;
import org.netbeans.modules.xml.api.model.GrammarResult;
import org.netbeans.modules.xml.api.model.HintContext;
import org.openide.util.Enumerations;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * Query implementation for resource XML files -- strings, attributes, colors and similar.
 *
 * @author Radim Kubacki
 */
public class ResourcesGrammar implements GrammarQuery {

  private static final Logger LOG = Logger.getLogger(ResourcesGrammar.class.getName());

  private final ResourcesModel model;

  public ResourcesGrammar() {
    model = new ResourcesModel();
  }

  /**
   * Allow to get names of <b>parsed general entities</b>.
   * @return list of <code>CompletionResult</code>s (ENTITY_REFERENCE_NODEs)
   */
  @Override
  public Enumeration<GrammarResult> queryEntities(String prefix) {
    List<GrammarResult> list = new ArrayList<GrammarResult>();

    // XXX(radim): add well-know build-in entity names, can we do better?

    if ("lt".startsWith(prefix)) {
      list.add(new SimpleEntityReference("lt"));
    }
    if ("gt".startsWith(prefix)) {
      list.add(new SimpleEntityReference("gt"));
    }
    if ("apos".startsWith(prefix)) {
      list.add(new SimpleEntityReference("apos"));
    }
    if ("quot".startsWith(prefix)) {
      list.add(new SimpleEntityReference("quot"));
    }
    if ("amp".startsWith(prefix)) {
      list.add(new SimpleEntityReference("amp"));
    }

    LOG.log(Level.FINE, "queryEntities({0}) -> {1}", new Object[] { prefix, list });
    return Collections.enumeration(list);
  }

  @Override
  public Enumeration<GrammarResult> queryAttributes(HintContext ctx) {

    Element ownerElement = null;
    // Support both versions of GrammarQuery contract
    if (ctx.getNodeType() == Node.ATTRIBUTE_NODE) {
      ownerElement = ((Attr) ctx).getOwnerElement();
    } else if (ctx.getNodeType() == Node.ELEMENT_NODE) {
      ownerElement = (Element) ctx;
    }
    if (ownerElement == null) {
      return Enumerations.empty();
    }
    ResElementDescriptor resElem = findElement(ownerElement);
    LOG.log(Level.FINE, "queryAttributes(tag={0} element={1} prefix={2})",
        new Object[] {ownerElement, resElem, ctx.getCurrentPrefix()});
    List<GrammarResult> list = new ArrayList<GrammarResult>();
    String prefix = ctx.getCurrentPrefix();
    // TODO(radim): filter existing attrs
//    NamedNodeMap existingAttributes = ownerElement.getAttributes();
    if (resElem != null) {
      for (ResAttrDescriptor resElemAttr : resElem.getAttrs()) {
        if (resElemAttr.getName().startsWith(prefix)) {
          list.add(new SimpleAttr(resElemAttr.getName(), resElemAttr.getDescription()));
        }
      }
    }

    LOG.log(Level.FINE, "queryAttributes({0}) -> {1}", new Object[]{prefix, list});
    return Collections.enumeration(list);
  }

  private ResElementDescriptor findElement(Node n) {
    List<String> parentTags = Lists.newArrayList();
    String parentTagName = null;
    Node parent = n;
    while (parent != null && parent.getNodeType() == Node.ELEMENT_NODE) {
      parentTagName = ((Element) parent).getTagName();
      parentTags.add(parentTagName);
      parent = parent.getParentNode();
    }
    ResElementDescriptor resElem = model.getRootElement();
    if (!parentTags.isEmpty()) {
      boolean isFirst = true;
      while (!parentTags.isEmpty()) {
        final String tag = parentTags.remove(parentTags.size() - 1);
        if (!isFirst) {
          resElem = Iterables.find(
              resElem.getChildren(),
              new Predicate<ResElementDescriptor>() {
                @Override public boolean apply(ResElementDescriptor input) {
                  return input.getName().equals(tag);
                }
              },
              null);
        } else {
          isFirst = false;
          if (!resElem.getName().equals(tag)) {
            // something wrong with the hierarchy
            resElem = null;
          }
        }
        if (resElem == null) {
          break;
        }
      }
    }
    LOG.log(Level.FINE, "find element({0}) > {1}", new Object[] {n, resElem});
    return resElem;
  }

  @Override
  public Enumeration<GrammarResult> queryElements(HintContext ctx) {
    LOG.log(Level.FINE, "queryElements({0})", ctx.getCurrentPrefix());

    ResElementDescriptor resElem = findElement(((Node) ctx).getParentNode());
    String prefix = ctx.getCurrentPrefix();

    List<GrammarResult> list = new ArrayList<GrammarResult>();
    if (resElem != null) {
      for (ResElementDescriptor resElemTag : resElem.getChildren()) {
        if (resElemTag.getName().startsWith(prefix)) {
          list.add(new SimpleElement(resElemTag.getName(), resElemTag.getDescription()));
        }
      }
    }

    LOG.log(Level.FINE, "queryElements({0}) -> {1}", new Object[] { prefix, list });
    return Collections.enumeration(list);
  }

  @Override
  public Enumeration<GrammarResult> queryNotations(String prefix) {
    LOG.log(Level.FINE, "queryNotatios({0})", prefix);
    return Enumerations.empty();
  }

  @Override
  public Enumeration<GrammarResult> queryValues(HintContext ctx) {
    LOG.log(Level.FINE, "queryValues({0})", ctx.getCurrentPrefix());
//    Attr ownerAttr;
//    if (ctx.getNodeType() == Node.ATTRIBUTE_NODE) {
//        ownerAttr = (Attr)ctx;
//    } else {
//        LOG.fine("...unknown node type");
//        return Enumerations.empty();
//    }
//    String attrName = ownerAttr.getName();
//    if (attrName.startsWith("android:")) {
//      attrName = attrName.substring("android:".length());
//    }
    List<GrammarResult> choices = new ArrayList<GrammarResult>();
//    for (String value : model.getAttributeValues(attrName)) {
//      if (value.startsWith(ctx.getCurrentPrefix())) {
//        choices.add(new MyText(value));
//      }
//    }
    return Collections.enumeration(choices);
  }

  // return defaults, no way to query them
  @Override
  public GrammarResult queryDefault(final HintContext ctx) {
    LOG.log(Level.FINE, "queryDefault({0})", ctx.getCurrentPrefix());
    return null;
  }

  // it is not yet implemented
  @Override
  public boolean isAllowed(Enumeration<GrammarResult> en) {
    return true;
  }

  // customizers section ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  @Override
  public java.awt.Component getCustomizer(HintContext ctx) {
    return null;
  }

  @Override
  public boolean hasCustomizer(HintContext ctx) {
    return false;
  }

  @Override
  public org.openide.nodes.Node.Property[] getProperties(HintContext ctx) {
    return null;
  }

  @Override
  public String toString() {
    return "ResourceGrammar [" + super.toString() + "]";
  }


  // TODO(radim) pull out to share with AndroidGrammar
  private static class MyText extends AbstractResultNode implements Text {

    private String data;

    MyText(String data) {
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
}
