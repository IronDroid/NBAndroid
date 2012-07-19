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

package org.netbeans.modules.android.project.ui;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Children;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.ErrorManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.android.core.sdk.DalvikPlatform;
import org.netbeans.modules.android.project.AndroidProject;
import org.netbeans.modules.android.project.api.AndroidProjects;
import org.netbeans.modules.android.project.queries.AndroidClassPath;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.SystemAction;

/**
 * PlatformNode represents Java platform in the logical view.
 * Listens on the {@link PropertyEvaluator} for change of
 * the ant property holding the platform name.
 * It displays the content of boot classpath.
 * @see JavaPlatform
 */
class PlatformNode extends AbstractNode {

  private static final String PLATFORM_ICON = "org/netbeans/modules/android/project/ui/resources/platform.gif";    //NOI18N
  private static final String ARCHIVE_ICON = "org/netbeans/modules/android/project/ui/resources/jar.gif"; //NOI18N
  private final AndroidProject project;
  private final PropertyEvaluator eval;

  PlatformNode(AndroidProject pp, PropertyEvaluator eval) {
    super(new PlatformContentChildren() /*, Lookups.singleton (new JavadocProvider(pp))*/);
    this.project = pp;
    this.eval = eval;
    setIconBaseWithExtension(PLATFORM_ICON);
  }

  @Override
  public String getName() {
    return this.getDisplayName();
  }

  @Override
  public String getDisplayName() {
    DalvikPlatform plat = AndroidProjects.projectPlatform(project);
    String name;
    if (plat != null) {
      // TODO
      name = plat.getAndroidTarget().isPlatform() ?
          plat.getAndroidTarget().getFullName() :
          NbBundle.getMessage(PlatformNode.class, "FMT_PlatformDisplayName", 
              plat.getAndroidTarget().getName(), plat.getAndroidTarget().getVersionName());
    } else {
      name = NbBundle.getMessage(PlatformNode.class, "TXT_BrokenPlatform");
    }
    return name;
  }

//    @Override
//    public String getHtmlDisplayName () {
//        if (project.getPlatform() == null) {
//            String displayName = this.getDisplayName();
//            try {
//                displayName = XMLUtil.toElementContent(displayName);
//            } catch (CharConversionException ex) {
//                // OK, no annotation in this case
//                return null;
//            }
//            return "<font color=\"#A40000\">" + displayName + "</font>"; //NOI18N
//        }
//        else {
//            return null;
//        }                                
//    }

  @Override
  public boolean canCopy() {
    return false;
  }

  @Override
  public Action[] getActions(boolean context) {
    return new Action[]{
          SystemAction.get(ShowJavadocAction.class)
        };
  }

  private static class PlatformContentChildren extends Children.Keys<SourceGroup> implements PropertyChangeListener {

    PlatformContentChildren() {
    }

    @Override
    protected void addNotify() {
      AndroidClassPath cpProvider = 
          ((PlatformNode) getNode()).project.getLookup().lookup(AndroidClassPath.class);
      if (cpProvider != null) {
        cpProvider.getClassPath(ClassPath.BOOT).addPropertyChangeListener(this);
      }
      this.setKeys(this.getKeys());
    }

    @Override
    protected void removeNotify() {
      AndroidClassPath cpProvider = 
          ((PlatformNode) getNode()).project.getLookup().lookup(AndroidClassPath.class);
      if (cpProvider != null) {
        cpProvider.getClassPath(ClassPath.BOOT).removePropertyChangeListener(this);
      }
      this.setKeys(Collections.<SourceGroup>emptySet());
    }

    @Override
    protected Node[] createNodes(SourceGroup sg) {
      return new Node[] { PackageView.createPackageView(sg) };
    }

    private List<SourceGroup> getKeys() {
      AndroidClassPath cpProvider = 
          ((PlatformNode) getNode()).project.getLookup().lookup(AndroidClassPath.class);
      if (cpProvider == null) {
        return Collections.emptyList();
      }
      //Todo: Should listen on returned classpath, but now the bootstrap libraries are read only
      FileObject[] roots = cpProvider.getClassPath(ClassPath.BOOT).getRoots();
      List<SourceGroup> result = new ArrayList<SourceGroup>(roots.length);
      for (int i = 0; i < roots.length; i++) {
        try {
          FileObject file;
          Icon icon;
          Icon openedIcon;
          if ("jar".equals(roots[i].getURL().getProtocol())) { //NOI18N
            file = FileUtil.getArchiveFile(roots[i]);
            icon = openedIcon = new ImageIcon(ImageUtilities.loadImage(ARCHIVE_ICON));
          } else {
            file = roots[i];
            icon = null;
            openedIcon = null;
          }

          if (file.isValid()) {
            result.add(new LibrariesSourceGroup(roots[i], file.getNameExt(), icon, openedIcon));
          }
        } catch (FileStateInvalidException e) {
          ErrorManager.getDefault().notify(e);
        }
      }
      return result;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      // change from boot classpath: rebuild the node
      SwingUtilities.invokeLater(new Runnable() {

        @Override
        public void run() {
          PlatformContentChildren.this.setKeys(PlatformContentChildren.this.getKeys());
          ((PlatformNode) getNode()).fireNameChange(null, null);
          ((PlatformNode) getNode()).fireDisplayNameChange(null, null);
        }
      });
    }
  }
}
