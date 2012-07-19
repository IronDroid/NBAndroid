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

package org.netbeans.modules.android.project;

import com.google.common.collect.ObjectArrays;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.modules.android.project.configs.AndroidConfigProvider;
import org.netbeans.modules.android.project.launch.LaunchConfiguration;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.ErrorManager;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Action provider of the Android project.
 */
class AndroidActionProvider implements ActionProvider {

  private static final Logger LOG = Logger.getLogger(AndroidActionProvider.class.getName());

  // Commands available from Android project
  private static final String[] supportedActions = {
      COMMAND_BUILD,
      COMMAND_CLEAN,
      COMMAND_REBUILD,
      COMMAND_RUN,
      COMMAND_DEBUG,
//        JavaProjectConstants.COMMAND_JAVADOC,
//      COMMAND_TEST,
//        COMMAND_TEST_SINGLE,
//        COMMAND_DEBUG_TEST_SINGLE,
      COMMAND_DELETE,
      COMMAND_COPY,
      COMMAND_MOVE,
      COMMAND_RENAME,
  };

  private AndroidProject project;

  /** Mapping between command and Ant target(s). */
  private enum CommandTarget {
    CLEAN(COMMAND_CLEAN, "clean"),
    RUN(COMMAND_RUN, "debug"),
    DEBUG(COMMAND_DEBUG, "debug"),
    BUILD(COMMAND_BUILD, "debug"),
//    TEST(COMMAND_TEST, "run-tests", "run-tests"),
    TEST(COMMAND_TEST, "debug"),
    REBUILD(COMMAND_REBUILD, new String[] { "clean", "debug" });

    private final String command;
    private final String[] prjCmds;

    private CommandTarget(String command, String prjTarget) {
      this.command = command;
      this.prjCmds = new String[] { prjTarget };
    }

    public String getCommand() {
      return command;
    }

    private CommandTarget(String command, String[] prjTargets) {
      this.command = command;
      this.prjCmds = prjTargets;
    }

    public String[] getTargets(String mode) {
      String [] targets = new String[prjCmds.length];
      System.arraycopy(prjCmds, 0, targets, 0, prjCmds.length);
      if (!LaunchConfiguration.MODE_DEBUG.equals(mode)) {
        for (int i = 0; i < targets.length; i++) {
          if (LaunchConfiguration.MODE_DEBUG.equals(targets[i])) {
            targets[i] = mode;
          }
        }
      }
      return targets;
    }
  }

    public AndroidActionProvider(AndroidProject project) {
        this.project = project;
    }

    private FileObject findBuildXml() {
        return project.getProjectDirectory().getFileObject("build.xml");
    }

    @Override
    public String[] getSupportedActions() {
      return project.info().isTest() ?
          ObjectArrays.concat(supportedActions, COMMAND_TEST) :
          supportedActions;
    }

    @Override
    public void invokeAction( final String command, final Lookup context ) throws IllegalArgumentException {
        if (COMMAND_DELETE.equals(command)) {
            DefaultProjectOperations.performDefaultDeleteOperation(project);
            return ;
        }

        if (COMMAND_COPY.equals(command)) {
            DefaultProjectOperations.performDefaultCopyOperation(project);
            return ;
        }

        if (COMMAND_MOVE.equals(command)) {
            DefaultProjectOperations.performDefaultMoveOperation(project);
            return ;
        }

        if (COMMAND_RENAME.equals(command)) {
            DefaultProjectOperations.performDefaultRenameOperation(project, null);
            return ;
        }

        // XXX ActionUtils.runTarget, or call android tool
        final Runnable action = new Runnable () {
            @Override
            public void run () {
                Properties p = new Properties();
                String[] targetNames;

                targetNames = getTargetNames(command, context, p);
                if (targetNames == null) {
                    return;
                }
                if (targetNames.length == 0) {
                    targetNames = null;
                }
                if (p.keySet().isEmpty()) {
                    p = null;
                }
                try {
                  FileObject buildFo = findBuildXml();
                  ExecutorTask task = ActionUtils.runTarget(buildFo, targetNames, p);

                  new LaunchExecutor(project).doLaunchAfterBuild(command, buildFo, task);
                }
                catch (IOException e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
        };

        // TODO(radim): handle bkgScanSensitiveActions
        action.run();
    }

    /**
     * @return array of targets or null to stop execution; can return empty array
     */
    /*private*/ String[] getTargetNames(String command, Lookup context, Properties p) throws IllegalArgumentException {
      for (CommandTarget t : CommandTarget.values()) {
        if (t.getCommand().equals(command)) {
          LaunchConfiguration launch =
              project.getLookup().lookup(AndroidConfigProvider.class).getActiveConfiguration().getLaunchConfiguration();
          return t.getTargets(launch.getMode());
        }
      }
      return new String[0];
    }

    @Override
    public boolean isActionEnabled( String command, Lookup context ) {
        FileObject buildXml = findBuildXml();
        LOG.log(Level.FINER, "action {0} on: {1}", new Object[] { command, buildXml });
        if (  buildXml == null || !buildXml.isValid()) {
            return false;
        }
        return true;
    }
}
