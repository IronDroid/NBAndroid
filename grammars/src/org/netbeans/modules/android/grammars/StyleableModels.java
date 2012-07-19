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
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.android.core.sdk.DalvikPlatform;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.xml.XMLUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DefaultHandler2;

import org.netbeans.modules.android.core.sdk.Tool;

/**
 * @author Radim Kubacki
 */
class StyleableModels {

  private static final Logger LOG = Logger.getLogger(StyleableModels.class.getName());

  /** A tag declaring styleables. */
  private static final String DECLARE_STYLEABLE = "declare-styleable";

  /** Tag {@code name} for possible attribute values. */
  private static final String ENUM = "enum";

  /** Name of {@code name} tag or attribute. */
  private static final String NAME = "name";

  /** Name of {@code attr} attribute. */
  private static final String ATTR = "attr";

  /** Name of {code parent} attribute. */
  private static final String PARENT = "parent";

  /** Name of {code parent} attribute. */
  private static final String EAT_COMMENT = "eat-comment";

  /** Prefix of all style-able from a AndroidManifest. */
  private static final String ANDROID_MANIFEST = "AndroidManifest";

  private static Map<FileObject, StyleableModel> models = new HashMap<FileObject, StyleableModel>();

  public static synchronized StyleableModel getAndroidManifestModel(DalvikPlatform platform) {
    FileObject attrsManifestFO = platform.findTool(Tool.ATTRS_MANIFEST.getSystemName());
    StyleableModel manifestModel = models.get(attrsManifestFO);
    if (manifestModel == null) {
      manifestModel = StyleableModels.getModel(attrsManifestFO, true, new Function<String, String>() {

        @Override public String apply(String input) {
          return null;
        }
      });
    }
    return manifestModel;
  }

  public static synchronized StyleableModel getAndroidLayoutsModel(DalvikPlatform platform,
      Function<String, String> parentTagSupplier) {
    FileObject attrsLayoutFO = platform.findTool(Tool.ATTRS_LAYOUT.getSystemName());
    StyleableModel manifestModel = models.get(attrsLayoutFO);
    if (manifestModel == null) {
      manifestModel = StyleableModels.getModel(attrsLayoutFO, false, parentTagSupplier);
    }
    return manifestModel;
  }

  /**
   * Converts attribute value to a tag name like: AndroidManifestIntentFilter -&gt; intent-filter
   */
  private static String toTagName(String name) {
    if (name == null) {
      return null;
    }
    if (name.equals(ANDROID_MANIFEST)) {
      return "manifest";
    }
    if (name.startsWith(ANDROID_MANIFEST) && name.length() > ANDROID_MANIFEST.length()) {
      StringBuilder tagName = new StringBuilder();
      tagName.append(Character.toLowerCase(name.charAt(ANDROID_MANIFEST.length())));
      for (int i = ANDROID_MANIFEST.length() + 1; i < name.length(); i++) {
        char c = name.charAt(i);
        if (Character.isLowerCase(c)) {
          tagName.append(c);
        } else {
          tagName.append('-').append(Character.toLowerCase(c));
        }
      }
      return tagName.toString();
    }
    return null;
  }

  private static StyleableModel getModel(FileObject xmlDefaultValues, final boolean isManifest,
      final Function<String, String> superTagNameSupplier) {
    try {
      XMLReader reader = XMLUtil.createXMLReader();
      final StyleableModel model = new StyleableModel();

      DefaultHandler2 handler = new DefaultHandler2() {

        StyleableInfo currentStyleableTag;
        AttributeInfo currentAttribute;
        StringBuilder lastComment = new StringBuilder();

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attrs)
            throws SAXException {
          if (DECLARE_STYLEABLE.equals(qName)) {
            createStyleable(attrs);
          } else if (EAT_COMMENT.equals(qName)) {
            lastComment.setLength(0);
          } else if (ATTR.equals(qName)) {
            String name = attrs.getValue(NAME);
            if (name != null) {

              AttributeInfo genericAttribute = findDefaultAttrInfo(
                  model, currentStyleableTag != null ? currentStyleableTag.getName() : null, name);
              currentAttribute = buildAttribute("android:" + name, genericAttribute);
              if (currentStyleableTag != null) {
                currentStyleableTag.addAttribute(currentAttribute);
              } else {
                model.addAttribute(name, currentAttribute);
              }

              // TODO format is | separated list of formats
              String format = attrs.getValue("format");
              if ("boolean".equals(format)) {
                currentAttribute.addFormat(AttributeInfo.Format.BOOLEAN);
              } else if ("dimension".equals(format)) {
                currentAttribute.addFormat(AttributeInfo.Format.DIMENSION);
              } else if ("reference".equals(format)) {
                currentAttribute.addFormat(AttributeInfo.Format.REFERENCE);
              }
            }
            lastComment.setLength(0);
          } else if (ENUM.equals(qName)) {
            if (currentAttribute != null) {
              currentAttribute.addFormat(AttributeInfo.Format.ENUM);
              currentAttribute.addEnum(attrs.getValue(NAME));
            }
            lastComment.setLength(0);
          }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            lastComment.setLength(0);
            if (ATTR.equals(qName)) {
              currentAttribute = null;
            } else if (DECLARE_STYLEABLE.equals(qName)) {
              currentStyleableTag = null;
            }
        }

        @Override
        public void comment(char[] ch, int start, int length) throws SAXException {
          lastComment.append(ch, start, length);
        }

        private void createStyleable(Attributes attrs) {
          String name = isManifest ? toTagName(attrs.getValue(NAME)) : attrs.getValue(NAME);
          Set<String> parents = new HashSet<String>();
          String parent = attrs.getValue(PARENT);
          if (parent != null) {
            StringTokenizer strTok = new StringTokenizer(parent);
            while (strTok.hasMoreTokens()) {
              parents.add(isManifest ? toTagName(strTok.nextToken()) : strTok.nextToken());
            }
          }
          if (name != null) {
            currentStyleableTag = buildStyleable(name, parents);
          }
        }

        private StyleableInfo buildStyleable(String name, Set<String> parents) {
          StyleableInfo s = model.createStyleable(
              name, lastComment.length() > 0 ? lastComment.toString().trim() : null, parents);
          lastComment.setLength(0);
          return s;
        }

        private AttributeInfo buildAttribute(String name, AttributeInfo defaultAttrInfo) {
          AttributeInfo a = new AttributeInfo(
              name, 
              lastComment.length() > 0 ? lastComment.toString().trim() : 
                  defaultAttrInfo != null ? defaultAttrInfo.getDescription() : null);
          if (defaultAttrInfo != null) {
            for (AttributeInfo.Format f : defaultAttrInfo.getFormats()) {
              a.addFormat(f);
            }
            for (String s : defaultAttrInfo.getEnumValues()) {
              a.addEnum(s);
            }
          }
          lastComment.setLength(0);
          return a;
        }

        private AttributeInfo findDefaultAttrInfo(StyleableModel model, String tagName, String attrName) {
          if (tagName == null) {
            return model.getAttributeValue(attrName);
          }
          String superTagName = superTagNameSupplier.apply(tagName);
          if (superTagName != null) {
            StyleableInfo styleable = model.getStyleables().get(superTagName);
            for (AttributeInfo attrInfo : styleable.getAttributeNames()) {
              if (attrInfo.getName().equals("android:" + attrName)) {
                return attrInfo;
              }
            }
          }
          return findDefaultAttrInfo(model, superTagName, attrName);
        }
      };
      reader.setContentHandler(handler);

      if (xmlDefaultValues != null) {
        InputStream is = xmlDefaultValues.getInputStream();
        reader.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
        reader.parse(new InputSource(is));
        is.close();
      } else {
        LOG.log(Level.WARNING, "Missing XML default values file.");
      }

      return model;
    } catch (SAXException ex) {
      Exceptions.printStackTrace(ex);
    } catch (IOException ex) {
      Exceptions.printStackTrace(ex);
    }
    return null;
  }
}
