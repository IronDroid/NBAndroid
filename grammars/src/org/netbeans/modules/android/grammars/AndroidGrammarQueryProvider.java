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

import java.beans.FeatureDescriptor;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.android.core.sdk.DalvikPlatform;
import org.netbeans.modules.android.grammars.resources.ResourcesGrammar;
import org.netbeans.modules.android.project.api.AndroidProjects;
import org.netbeans.modules.xml.api.model.GrammarEnvironment;
import org.netbeans.modules.xml.api.model.GrammarQuery;
import org.netbeans.modules.xml.api.model.GrammarQueryManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Enumerations;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

/**
 * Provides the Android grammar for any documents whose root elements matches
 * a standard pattern.
 *
 * @author Radim Kubacki
 */
public final class AndroidGrammarQueryProvider extends GrammarQueryManager {

  private static final Logger LOG = Logger.getLogger(AndroidGrammarQueryProvider.class.getName());

  @Override
  public Enumeration enabled(GrammarEnvironment ctx) {
    Enumeration en = ctx.getDocumentChildren();
    while (en.hasMoreElements()) {
      Node next = (Node) en.nextElement();
      if (useManifestGrammar(next)) {
        return Enumerations.singleton(next);
      }
    }
    if (useLayoutGrammar(ctx)) {
      return en;
    }
    if (useResourcesGrammar(ctx)) {
      return en;
    }
    LOG.log(Level.FINE, "enabled -> null");
    return null;
  }

  private boolean useManifestGrammar(Node node) {
    if (node.getNodeType() == node.ELEMENT_NODE) {
      Element root = (Element) node;
      /* AndroidManifest.xml starts with something like
         <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                   package="org.foo.simpleandroidapp">
       */
      if ("manifest".equals(root.getNodeName()) 
          && (root.getAttributeNode("xmlns:android") != null
              || root.getAttributeNode("package") != null)) {
        LOG.log(Level.FINE, "enabled -> {0}", node);
        return true;
      }
    }
    return false;
  }

  private boolean useLayoutGrammar(GrammarEnvironment ctx) {
    FileObject fo = ctx.getFileObject();
    if (fo == null) {
      return false;
    }
    Project p = FileOwnerQuery.getOwner(fo);
    if (!AndroidProjects.isAndroidProject(p)) {
      return false;
    }
    FileObject layoutsDir = p.getProjectDirectory().getFileObject("res/layout");
    if (layoutsDir == null) {
      return false;
    }
    return FileUtil.isParentOf(layoutsDir, fo) && "xml".equals(fo.getExt());
  }

  private boolean useResourcesGrammar(GrammarEnvironment ctx) {
    FileObject fo = ctx.getFileObject();
    if (fo == null) {
      return false;
    }
    Project p = FileOwnerQuery.getOwner(fo);
    if (!AndroidProjects.isAndroidProject(p)) {
      return false;
    }
    FileObject layoutsDir = p.getProjectDirectory().getFileObject("res/values");
    if (layoutsDir == null) {
      return false;
    }
    return FileUtil.isParentOf(layoutsDir, fo) && "xml".equals(fo.getExt());
  }

  @Override
  public FeatureDescriptor getDescriptor() {
    return new FeatureDescriptor();
  }

  @Override
  public GrammarQuery getGrammar(GrammarEnvironment env) {
    // XXX(radim): fallback to default android platform?
    FileObject fo = env.getFileObject();
    Project prj = fo != null ? FileOwnerQuery.getOwner(fo) : null;
    DalvikPlatform prjPlatform = prj != null ? AndroidProjects.projectPlatform(prj) : null;
    LOG.log(Level.FINE, "android SDK for {0} is {1}", new Object[] {fo, prjPlatform});
    if (useResourcesGrammar(env)) {
      return new ResourcesGrammar();
    }
    if (prjPlatform == null) {
      return null;
    } else if (useLayoutGrammar(env)) {
      return AndroidLayoutGrammar.create(prjPlatform, new ProjectRefResolver(prj));
    } else {
      return new AndroidManifestGrammar(prjPlatform);
    }
  }
}
