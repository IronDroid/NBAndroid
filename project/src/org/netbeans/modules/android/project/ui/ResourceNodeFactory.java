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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.android.project.AndroidProject;
import org.netbeans.modules.android.project.ui.customizer.CustomizerProviderImpl;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 */
public final class ResourceNodeFactory implements NodeFactory {
  public ResourceNodeFactory() {
  }

  @Override
  public NodeList createNodes(Project p) {
      AndroidProject project = p.getLookup().lookup(AndroidProject.class);
      assert project != null;
      return new SourcesNodeList(project);
  }

  private static enum ResFolder {
    ASSETS("assets.dir", "assets", "Assets"),
    RES("resource.dir", "res", "Resources");

    public final String propertyName;
    public final String folderName;
    public final String nodeName;

    private ResFolder(String propertyName, String folderName, String nodeName) {
      this.propertyName = propertyName;
      this.folderName = folderName;
      this.nodeName = nodeName;
    }
  }

  private static class NamedFileObject {
    public final FileObject fo;
    public final String name;

    public NamedFileObject(FileObject fo, String name) {
      this.fo = fo;
      this.name = name;
    }
  }

    private static class SourcesNodeList implements NodeList<NamedFileObject>, ChangeListener {

        private AndroidProject project;

        private final ChangeSupport changeSupport = new ChangeSupport(this);

        public SourcesNodeList(AndroidProject proj) {
            project = proj;
        }

        @Override
        public List<NamedFileObject> keys() {
            if (this.project.getProjectDirectory() == null || !this.project.getProjectDirectory().isValid()) {
                return Collections.emptyList();
            }

            return getResources();
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            changeSupport.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }

        @Override
        public Node node(NamedFileObject key) {
            try {
                DataObject folderDO = DataObject.find(key.fo);
                return new ResourcesFilterNode(folderDO.getNodeDelegate(), project, key.name);

            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        }

        @Override
        public void addNotify() {
            // getResources().addChangeListener(this);
        }

        @Override
        public void removeNotify() {
            // getResources().removeChangeListener(this);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            // setKeys(getKeys());
            // The caller holds ProjectManager.mutex() read lock
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    changeSupport.fireChange();
                }
            });
        }

        private List<NamedFileObject> getResources() {
          List<NamedFileObject> folders = Lists.newArrayList();
          for (ResFolder rf : ResFolder.values()) {

            String resDirName = project.evaluator().getProperty(rf.propertyName);
            if (resDirName == null) {
                resDirName = rf.folderName;
            }


            FileObject folder = project.getProjectDirectory();
            FileObject resDir = folder.getFileObject(resDirName);
            if (resDir != null && resDir.isValid()) {
              folders.add(new NamedFileObject(resDir, rf.nodeName));
            }
          }
          return folders;
        }
    }


    /** Yet another cool filter node just to add properties action
     */
    @VisibleForTesting static class ResourcesFilterNode extends FilterNode implements PathFinder {

        private String nodeName;
        private Project project;

        Action[] actions;

        public ResourcesFilterNode(Node delegate, Project project, String nodeName) {
            super(delegate);
            this.nodeName = nodeName;
            this.project = project;
        }

        @Override
        public String getDisplayName() {
            return nodeName;
        }

        @Override
        public Action[] getActions(boolean context) {
            if (!context) {
                if (actions == null) {
                    Action superActions[] = super.getActions(context);
                    actions = new Action[superActions.length + 2];
                    System.arraycopy(superActions, 0, actions, 0, superActions.length);
                    actions[superActions.length] = null;
                    actions[superActions.length + 1] = new PreselectPropertiesAction(project, nodeName);
                }
                return actions;
            } else {
                return super.getActions(context);
            }
        }

        // a copy from PhysicalView.PathFinder
        @Override
        public Node findPath( Node root, Object object ) {

            if ( !( object instanceof FileObject ) ) {
                return null;
            }

            FileObject fo = (FileObject)object;        
            FileObject resRoot = project.getProjectDirectory().getFileObject("res");
            if ( FileUtil.isParentOf( resRoot, fo ) /* && group.contains( fo ) */ ) {
                // The group contains the object

                String relPath = FileUtil.getRelativePath( resRoot, fo );

                ArrayList<String> path = new ArrayList<String>();
                StringTokenizer strtok = new StringTokenizer( relPath, "/" );
                while( strtok.hasMoreTokens() ) {
                   path.add( strtok.nextToken() );
                }

                if (path.size() > 0) {
                    path.remove(path.size() - 1);
                } else {
                    return null;
                }
                try {
                    //#75205
                    Node parent = NodeOp.findPath( root, Collections.enumeration( path ) );
                    if (parent != null) {
                        //not nice but there isn't a findNodes(name) method.
                        Node[] nds = parent.getChildren().getNodes(true);
                        for (int i = 0; i < nds.length; i++) {
                            DataObject dobj = nds[i].getLookup().lookup(DataObject.class);
                            if (dobj != null && fo.equals(dobj.getPrimaryFile())) {
                                return nds[i];
                            }
                        }
                        String name = fo.getName();
                        try {
                            DataObject dobj = DataObject.find( fo );
                            name = dobj.getNodeDelegate().getName();
                        } catch (DataObjectNotFoundException ex) {
                        }
                        return parent.getChildren().findChild(name);
                    }
                }
                catch ( NodeNotFoundException e ) {
                    return null;
                }
            }   
            else if ( resRoot.equals( fo ) ) {
                return root;
            }

            return null;
        }
    }


    /** The special properties action
     */
    static class PreselectPropertiesAction extends AbstractAction {

        private final Project project;
        private final String nodeName;
        private final String panelName;

        public PreselectPropertiesAction(Project project, String nodeName) {
            this(project, nodeName, null);
        }

        public PreselectPropertiesAction(Project project, String nodeName, String panelName) {
            super(NbBundle.getMessage(AndroidLogicalViewProvider.class, "LBL_Properties_Action"));
            this.project = project;
            this.nodeName = nodeName;
            this.panelName = panelName;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // J2SECustomizerProvider cp = (J2SECustomizerProvider) project.getLookup().lookup(J2SECustomizerProvider.class);
            CustomizerProviderImpl cp = project.getLookup().lookup(CustomizerProviderImpl.class);
            if (cp != null) {
                cp.showCustomizer(nodeName, panelName);
            }

        }
    }

}
