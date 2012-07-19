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

package org.netbeans.modules.android.project.launch;

import org.netbeans.modules.android.project.AndroidProject;
import org.netbeans.modules.android.project.AndroidProjectInfo;
import org.netbeans.modules.android.project.configs.AndroidConfigProvider;
import org.netbeans.modules.android.project.launch.LaunchConfiguration.Action;

/**
 * Utilities related to {@link LaunchConfiguration}.
 *
 * @author radim
 */
public class Launches {

  public static LaunchAction actionForProject(AndroidProject p) {

    AndroidConfigProvider cfgProvider = p.getLookup().lookup(AndroidConfigProvider.class);
    Action launchAction = cfgProvider.getActiveConfiguration().getLaunchConfiguration().getLaunchAction();
    if (Action.DO_NOTHING == launchAction) {
      return new EmptyLaunchAction();
    }
    return defaultActionForProject(p);
  }

  private static LaunchAction defaultActionForProject(AndroidProject p) {
    AndroidProjectInfo info = p.info();
    // TODO fix to run tests
    return info.isTest() ? new TestLaunchAction() : new ActivityLaunchAction();
  }
}
