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

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.android.project.AndroidProject;
import org.netbeans.modules.android.project.ui.customizer.CustomizerProviderImpl;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 *
 */
public final class SourceNodeFactory implements NodeFactory {
    public SourceNodeFactory() {
    }

    public NodeList createNodes(Project p) {
        AndroidProject project = p.getLookup().lookup(AndroidProject.class);
        assert project != null;
        return new SourcesNodeList(project);
    }

    private static class SourcesNodeList implements NodeList<SourceGroupKey>, ChangeListener {

        private AndroidProject project;

        private final ChangeSupport changeSupport = new ChangeSupport(this);

        public SourcesNodeList(AndroidProject proj) {
            project = proj;
        }

        public List<SourceGroupKey> keys() {
            if (this.project.getProjectDirectory() == null || !this.project.getProjectDirectory().isValid()) {
                return Collections.emptyList();
            }
            Sources sources = getSources();
            SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);

            List<SourceGroupKey> result =  new ArrayList<SourceGroupKey>(groups.length);
            for(SourceGroup sg : groups) {
                result.add(new SourceGroupKey(sg));
            }
            return result;
        }

        public void addChangeListener(ChangeListener l) {
            changeSupport.addChangeListener(l);
        }

        public void removeChangeListener(ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }

        public Node node(SourceGroupKey key) {
            return new PackageViewFilterNode(key.group, project);
        }

        public void addNotify() {
            getSources().addChangeListener(this);
        }

        public void removeNotify() {
            getSources().removeChangeListener(this);
        }

        public void stateChanged(ChangeEvent e) {
            // setKeys(getKeys());
            // The caller holds ProjectManager.mutex() read lock
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    changeSupport.fireChange();
                }
            });
        }

        private Sources getSources() {
            return ProjectUtils.getSources(project);
        }

    }

    private static class SourceGroupKey {

        public final SourceGroup group;
        public final FileObject fileObject;

        SourceGroupKey(SourceGroup group) {
            this.group = group;
            this.fileObject = group.getRootFolder();
        }

        @Override
        public int hashCode() {
            int hash = 5;
            String disp = this.group.getDisplayName();
            hash = 79 * hash + (fileObject != null ? fileObject.hashCode() : 0);
            hash = 79 * hash + (disp != null ? disp.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof SourceGroupKey)) {
                return false;
            } else {
                SourceGroupKey otherKey = (SourceGroupKey) obj;

                if (fileObject != otherKey.fileObject && (fileObject == null || !fileObject.equals(otherKey.fileObject))) {
                    return false;
                }
                String thisDisplayName = this.group.getDisplayName();
                String otherDisplayName = otherKey.group.getDisplayName();
                boolean oneNull = thisDisplayName == null;
                boolean twoNull = otherDisplayName == null;
                if (oneNull != twoNull || !thisDisplayName.equals(otherDisplayName)) {
                    return false;
                }
                return true;
            }
        }


    }

    /** Yet another cool filter node just to add properties action
     */
    private static class PackageViewFilterNode extends FilterNode {

        private String nodeName;
        private Project project;

        Action[] actions;

        public PackageViewFilterNode(SourceGroup sourceGroup, Project project) {
            super(PackageView.createPackageView(sourceGroup));
            this.project = project;
            this.nodeName = "Sources";
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

        public void actionPerformed(ActionEvent e) {
            // J2SECustomizerProvider cp = (J2SECustomizerProvider) project.getLookup().lookup(J2SECustomizerProvider.class);
            CustomizerProviderImpl cp = project.getLookup().lookup(CustomizerProviderImpl.class);
            if (cp != null) {
                cp.showCustomizer(nodeName, panelName);
            }

        }
    }

}
