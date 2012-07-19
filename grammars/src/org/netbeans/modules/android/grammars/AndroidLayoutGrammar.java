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

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.SortedMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.android.core.sdk.DalvikPlatform;
import org.netbeans.modules.android.core.sdk.Tool;
import org.netbeans.modules.xml.api.model.GrammarResult;
import org.netbeans.modules.xml.api.model.HintContext;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Enumerations;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Query implementation based on Android metadata for completion in layout XML descriptors.
 *
 * @author Radim Kubacki
 */
class AndroidLayoutGrammar extends AndroidGrammar {

  private static final Logger LOG = Logger.getLogger(AndroidLayoutGrammar.class.getName());

  private static class ParentTagResolver implements Function<String, String> {
    private final WidgetData classData;

    public ParentTagResolver(WidgetData classData) {
      this.classData = classData;
    }

    @Override
    public String apply(String input) {
      if (input.endsWith("Layout") && input.contains("_")) {
        Iterable<UIClassDescriptor> clzs = UIClassDescriptors.findBySimpleName(
            classData, LayoutElementType.LAYOUT_PARAM, input.replace('_', '.') + "Params");
        if (!Iterables.isEmpty(clzs)) {
          UIClassDescriptor superClz = UIClassDescriptors.findByFQName(
              classData, LayoutElementType.LAYOUT_PARAM, Iterables.get(clzs, 0).getSuperclass());
          if (superClz != null) {
            String superLayoutParam = superClz.getSimpleName().replace('.', '_');
            if (superLayoutParam.endsWith("Params")) {
              superLayoutParam = superLayoutParam.substring(0, superLayoutParam.length() - "Params".length());
            }
            return superLayoutParam;
          }
        }
      }
      return null;
    }

  }

  public static AndroidLayoutGrammar create(DalvikPlatform platform, ReferenceResolver rr) {
    FileObject attrsLayoutFO = platform.findTool(Tool.WIDGETS.getSystemName());
    LayoutClassesParser parser = new LayoutClassesParser(URLMapper.findURL(attrsLayoutFO, URLMapper.INTERNAL));
    return new AndroidLayoutGrammar(platform, parser.load(), rr);
  }

  private final WidgetData classData;

  private AndroidLayoutGrammar(
      DalvikPlatform platform, WidgetData classData, ReferenceResolver rr) {
    super(platform, StyleableModels.getAndroidLayoutsModel(
        platform, new ParentTagResolver(classData)), rr);
    this.classData = Preconditions.checkNotNull(classData);
  }

  @Override
  protected Enumeration<GrammarResult> doQueryAttributes(Element ownerElement, String prefix) {

    String parentTagName = ownerElement.getParentNode() != null ? ownerElement.getParentNode().getNodeName() : null;
    LOG.log(Level.FINE, "queryAttributes(tag={0} parent={1} prefix={2})", 
        new Object[] {ownerElement, parentTagName, prefix});
    List<GrammarResult> list = new ArrayList<GrammarResult>();
    NamedNodeMap existingAttributes = ownerElement.getAttributes();

    for (UIClassDescriptor clazz : UIClassDescriptors.findBySimpleName(classData, ownerElement.getTagName())) {
      for (UIClassDescriptor clz = clazz; clz != null; clz = UIClassDescriptors.findByFQName(classData, clz.getSuperclass())) {
        addAttrNames(clz.getSimpleName(), prefix, existingAttributes, list);
      }
    }
    for (UIClassDescriptor clazz : Iterables.concat(
        UIClassDescriptors.findBySimpleName(classData, LayoutElementType.VIEW_GROUP, parentTagName),
        UIClassDescriptors.findBySimpleName(classData, LayoutElementType.VIEW_GROUP, ownerElement.getTagName()))) {
      UIClassDescriptor params = UIClassDescriptors.findParamsForName(classData, clazz.getFQClassName());
      for (UIClassDescriptor paramClz = params;
          paramClz != null;
          paramClz = UIClassDescriptors.findByFQName(classData, paramClz.getSuperclass())) {
        addAttrNames(layoutParamsStyleableName(paramClz.getSimpleName()),
            prefix, existingAttributes, list);
      }
    }

    LOG.log(Level.FINE, "queryAttributes({0}) -> {1}", new Object[]{prefix, list});
    return Collections.enumeration(list);
  }

  /** Convert ViewGroup.LayoutParams -> ViewGroup_Layout */
  private String layoutParamsStyleableName(String params) {
    String styleableSuffix = 
        params.endsWith("Params") ? params.substring(0, params.length() - "Params".length()) : params;
    return styleableSuffix.replace('.', '_');
  }

  private void addAttrNames(String name, String prefix, NamedNodeMap existingAttributes, List<GrammarResult> list) {
    SortedMap<String, StyleableInfo> styleables = model.getStyleables();
    StyleableInfo elementData = styleables != null ? styleables.get(name) : null;
    List<AttributeInfo> possibleAttributes =
        elementData != null ? elementData.getAttributeNames() : Lists.<AttributeInfo>newArrayList();

    for (AttributeInfo attribute : possibleAttributes) {
      final String attrName = attribute.getName();
      if (attribute.getName().startsWith(prefix) &&
          existingAttributes.getNamedItem(attribute.getName()) == null &&
          !Iterables.any(list, new Predicate<GrammarResult>() {

            @Override
            public boolean apply(GrammarResult input) {
              return input.getNodeName().equals(attrName);
            }
          })) {
        list.add(new SimpleAttr(attrName, attribute.getDescription()));
      }
    }
  }

  @Override
  public Enumeration<GrammarResult> queryElements(HintContext ctx) {
    LOG.log(Level.FINE, "queryElements({0})", ctx.getCurrentPrefix());

    String prefix = ctx.getCurrentPrefix();

    List<GrammarResult> list = new ArrayList<GrammarResult>();
    for(StyleableInfo s : model.getStyleables().values()) {
      if (s.getName().startsWith(prefix) 
          && !s.getName().contains("_") 
          && !s.getName().startsWith("android:")) {
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
    Node parentNode = ownerElement.getParentNode();
    String parentTagName = null;
    parentTagName = parentNode != null ? parentNode.getNodeName() : null;

    List<GrammarResult> choices = new ArrayList<GrammarResult>();
    for (AttributeInfo attr : attributeInfos(ownerElement.getTagName(), parentTagName)) {
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
    return "AndroidLayoutGrammar for SDK " + platform + " [" + super.toString() + "]";
  }

  private Iterable<AttributeInfo> attributeInfos(String tagName, String parentTagName) {
    List<AttributeInfo> infos = Lists.newArrayList();
    for (UIClassDescriptor clazz : UIClassDescriptors.findBySimpleName(classData, tagName)) {
      for (UIClassDescriptor clz = clazz; clz != null; clz = UIClassDescriptors.findByFQName(classData, clz.getSuperclass())) {
        addAttrInfos(clz.getSimpleName(), infos);
      }
    }
    if (parentTagName != null) {
      for (UIClassDescriptor clazz : UIClassDescriptors.findBySimpleName(classData, LayoutElementType.VIEW_GROUP, parentTagName)) {
        UIClassDescriptor params = UIClassDescriptors.findParamsForName(classData, clazz.getFQClassName());
        for (UIClassDescriptor paramClz = params;
            paramClz != null;
            paramClz = UIClassDescriptors.findByFQName(classData, paramClz.getSuperclass())) {
          // TODO need to iterate class hierarchy to get full spec of attr infos
          addAttrInfos(layoutParamsStyleableName(paramClz.getSimpleName()), infos);
        }
      }
    }
    return infos;
  }

  private void addAttrInfos(String styleableName, List<AttributeInfo> list) {
    SortedMap<String, StyleableInfo> styleables = model.getStyleables();
    StyleableInfo elementData = styleables != null ? styleables.get(styleableName) : null;
    List<AttributeInfo> possibleAttributes =
        elementData != null ? elementData.getAttributeNames() : Lists.<AttributeInfo>newArrayList();
    list.addAll(possibleAttributes);
  }
}
