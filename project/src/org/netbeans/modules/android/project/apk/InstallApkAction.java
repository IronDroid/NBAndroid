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
package org.netbeans.modules.android.project.apk;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import org.netbeans.modules.android.core.sdk.DalvikPlatformManager;
import org.netbeans.modules.android.project.AvdSelector;
import org.netbeans.modules.android.project.configs.ConfigBuilder;
import org.netbeans.modules.android.project.launch.AndroidLauncher;
import org.netbeans.modules.android.project.launch.AndroidLauncherImpl;
import org.netbeans.modules.android.project.launch.LaunchConfiguration;
import org.netbeans.modules.android.project.launch.LaunchConfiguration.TargetMode;
import org.netbeans.modules.android.project.launch.LaunchInfo;
import org.openide.util.RequestProcessor;

public final class InstallApkAction implements ActionListener {

  private static final RequestProcessor RP = new RequestProcessor("APK deployer", 1);
  private final Deployable context;

  public InstallApkAction(Deployable context) {
    this.context = context;
  }

  @Override
  public void actionPerformed(ActionEvent ev) {
    RP.post(new Runnable() {

      @Override
      public void run() {
        AndroidLauncher launcher = new AndroidLauncherImpl();
        LaunchConfiguration cfg = ConfigBuilder.builder()
            .withName("dummy").withTargetMode(TargetMode.AUTO).config().getLaunchConfiguration();
        AvdSelector.LaunchData launchData = launcher.configAvd(
            DalvikPlatformManager.getDefault().getSdkManager(), null, cfg);
        if (launchData == null || launchData.getDevice() == null) {
          return;
        }
        launcher.simpleLaunch(new LaunchInfo(context.getDeployableFile(),
            /*reinstall*/true, /*debug*/false, null, null), launchData.getDevice());
      }
    });
  }
}
