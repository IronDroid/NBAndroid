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

import com.android.ddmlib.Client;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.debugger.jpda.DebuggerStartException;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.android.core.ui.ClientNode;
import org.netbeans.modules.android.project.AndroidProject;
import org.netbeans.modules.android.project.AndroidProjectUtil;
import org.netbeans.modules.android.project.queries.ClassPathProviderImpl;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * A action for android process nodes to attach a debugger to it.
 */
public class AttachDebuggerAction
      extends NodeAction
{

  @Override
  protected void performAction(Node[] activatedNodes) {
    assert activatedNodes.length == 1;
    final Client client = activatedNodes[0].getLookup().lookup(Client.class);
    assert client != null;

    try {
      String processName = client.getClientData().getClientDescription();

      // search for the project with the matching package name
      AndroidProject project = AndroidProjectUtil.findProjectByPackageName(processName);

      // if we didn't found any matching projects, we try the current main project
      if (project == null) {
        Project mainProject = OpenProjects.getDefault().getMainProject();

        if (mainProject instanceof AndroidProject) {
          project = (AndroidProject)mainProject;
        }
      }

      // maybe we could try to use any other opened project, if we still don't have
      // a valid android project for debugging?
      /*
      if (project == null) {
        for(Project p : OpenProjects.getDefault().getOpenProjects()) {
          AndroidProject ap = p.getLookup().lookup(AndroidProject.class);
          if (ap != null) {
            project = ap;
            break;
          }
        }
      }
      */

      if (project != null) {
        final Map properties = new HashMap();
        final ClassPathProviderImpl cpp = project.getLookup().lookup(ClassPathProviderImpl.class);
        final ClassPath sourcePath = cpp.getSourcePath();
        final ClassPath compilePath = cpp.getCompilePath();
        final ClassPath bootPath = cpp.getBootPath();
        properties.put("sourcepath", ClassPathSupport.createProxyClassPath(sourcePath, AndroidProjectUtil.toSourcePath(compilePath))); // NOI18N
        properties.put("name", ProjectUtils.getInformation(project).getDisplayName()); // NOI18N
        properties.put("jdksources", AndroidProjectUtil.toSourcePath(bootPath)); // NOI18N
        properties.put("baseDir", FileUtil.toFile(project.getProjectDirectory()));   //NOI18N

        JPDADebugger.attach(
                            "localhost",
                            client.getDebuggerListenPort(),
                            new Object[]{properties}
        );
      }
    }
    catch (DebuggerStartException e) {
      Exceptions.printStackTrace(e);
    }
  }

  @Override
  protected boolean enable(Node[] activatedNodes) {
    return activatedNodes.length == 1 && activatedNodes[0].getLookup().lookup(Client.class) != null;
  }

  @Override
  public String getName() {
    return NbBundle.getMessage(getClass(), "LBL_AttachDebuggerAction_Name");
  }

  @Override
  public HelpCtx getHelpCtx() {
    return new HelpCtx(ClientNode.class);
  }
}
