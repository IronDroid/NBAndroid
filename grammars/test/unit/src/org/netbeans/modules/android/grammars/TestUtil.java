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

import java.io.File;
import java.io.StringReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.xml.api.model.GrammarResult;
import org.netbeans.modules.xml.api.model.HintContext;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public final class TestUtil {

  private TestUtil() {
  }

  static Collection<URL> allSdks() throws MalformedURLException {
    URL androidSDKs = new File(System.getProperty("test.all.android.sdks.home")).toURI().toURL();
    Collection<URL> sdks = new ArrayList<URL>();
//    sdks.add(new URL(androidSDKs, "platforms/android-1.5/"));
    sdks.add(new URL(androidSDKs, "add-ons/addon_google_apis_google_inc_3/"));
//    sdks.add(new URL(androidSDKs, "platforms/android-1.6/"));
    sdks.add(new URL(androidSDKs, "add-ons/addon_google_apis_google_inc_4/"));
    sdks.add(new URL(androidSDKs, "platforms/android-7/"));
    sdks.add(new URL(androidSDKs, "add-ons/addon_google_apis_google_inc_7/"));
    sdks.add(new URL(androidSDKs, "platforms/android-8/"));
    sdks.add(new URL(androidSDKs, "add-ons/addon_google_apis_google_inc_8/"));
    return sdks;
  }

  private static HintContext createHintContext(final Node n, final String prefix) {
    Set<Class> interfaces = new HashSet<Class>();
    findAllInterfaces(n.getClass(), interfaces);
    interfaces.add(HintContext.class);
    class Handler implements InvocationHandler {

      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass().equals(HintContext.class)) {
          assert method.getName().equals("getCurrentPrefix");
          return prefix;
        } else {
          return method.invoke(n, args);
        }
      }
    }
    return (HintContext) Proxy.newProxyInstance(TestUtil.class.getClassLoader(), interfaces.toArray(new Class[interfaces.size()]), new Handler());
  }

  static void findAllInterfaces(Class c, Set<Class> interfaces) {
    if (c.isInterface()) {
      interfaces.add(c);
    }
    Class s = c.getSuperclass();
    if (s != null) {
      findAllInterfaces(s, interfaces);
    }
    Class[] is = c.getInterfaces();
    for (int i = 0; i < is.length; i++) {
      findAllInterfaces(is[i], interfaces);
    }
  }

  /**
   * Create a context for completing some XML.
   * The XML text must be a well-formed document.
   * It must contain exactly one element name, attribute name,
   * attribute value, or text node ending in the string <samp>HERE</samp>.
   * The context will be that node (Element, Attribute, or Text) with
   * the suffix stripped off and the prefix set to the text preceding that suffix.
   */
  public static HintContext createCompletion(String xml) throws Exception {
    Document doc = XMLUtil.parse(new InputSource(new StringReader(xml)), false, true, null, null);
    return findCompletion(doc.getDocumentElement(), doc);
  }

  private static HintContext findCompletion(Node n, Document doc) {
    switch (n.getNodeType()) {
      case Node.ELEMENT_NODE:
        Element el = (Element) n;
        String name = el.getTagName();
        if (name.endsWith("HERE")) {
          String prefix = name.substring(0, name.length() - 4);
          Node nue = doc.createElement(prefix);
          NodeList nl = el.getChildNodes();
          while (nl.getLength() > 0) {
            nue.appendChild(nl.item(0));
          }
          el.getParentNode().replaceChild(nue, el);
          return createHintContext(nue, prefix);
        }
        break;
      case Node.ATTRIBUTE_NODE:
        Attr attr = (Attr) n;
        name = attr.getName();
        if (name.endsWith("HERE")) {
          String prefix = name.substring(0, name.length() - 4);
          Attr nue = doc.createAttribute(prefix);
          Element owner = attr.getOwnerElement();
          owner.removeAttributeNode(attr);
          owner.setAttributeNodeNS(nue);
          return createHintContext(nue, prefix);
        } else {
          String value = attr.getNodeValue();
          if (value.endsWith("HERE")) {
            String prefix = value.substring(0, value.length() - 4);
            attr.setNodeValue(prefix);
            return createHintContext(attr, prefix);
          }
        }
        break;
      default:
        // ignore
        break;
    }
    // Didn't find it, check children.
    NodeList nl = n.getChildNodes();
    for (int i = 0; i < nl.getLength(); i++) {
      HintContext c = findCompletion(nl.item(i), doc);
      if (c != null) {
        return c;
      }
    }
    // Element's attr nodes are listed separately.
    NamedNodeMap nnm = n.getAttributes();
    if (nnm != null) {
      for (int i = 0; i < nnm.getLength(); i++) {
        HintContext c = findCompletion(nnm.item(i), doc);
        if (c != null) {
          return c;
        }
      }
    }
    return null;
  }

  /**
   * Given a list of XML nodes returned in GrammarResult's, return a list of their names.
   * For elements, you get the name; for attributes, the name;
   * for text nodes, the value.
   * (No namespaces returned.)
   */
  public static List<String> grammarResultValues(Enumeration<GrammarResult> e) {
    List<String> l = new ArrayList<String>();
    while (e.hasMoreElements()) {
      l.add(e.nextElement().toString());
    }
    return l;
  }
}
