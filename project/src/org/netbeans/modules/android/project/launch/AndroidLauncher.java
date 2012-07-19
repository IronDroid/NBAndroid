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

import com.android.ddmlib.Client;
import com.android.ddmlib.IDevice;
import com.android.sdklib.IAndroidTarget;
import com.android.sdklib.SdkManager;
import java.util.concurrent.Future;
import org.netbeans.modules.android.core.sdk.DalvikPlatform;
import org.netbeans.modules.android.project.AvdSelector;
import org.openide.util.Lookup;

/**
 * Support for android application deployment and running or debugging on device or 
 * an emulator.
 * 
 * Cf. related functionality in Eclipse plugins located in 
 * {@code sdk/eclipse/plugins/com.android.ide.eclipse.adt/src/com/android/ide/eclipse/adt/internal/launch},
 * namely {@code LaunchConfigDelegate} and {@code AndroidLaunchController}.
 *
 * @author radim
 */
public interface AndroidLauncher {

  /**
   * Deploys an application on AVD (emulator or real device) and optionally
   * runs main or a selected activity.
   * 
   * @param platform
   * @param context must contain LaunchInfo, LaunchAction and AndroidProject.
   * optionally contains {@code AvdSelector.LaunchData}, {@code LaunchConfiguration.TargetMode}
   * @param mode run/debug
   * @return the {@link Future} reference to {@link Client} or null when execution
   * fails. The returned future's get method returns a {@link Client} when activity is started.
   * When no activity is started it returns null.
   */
  public Future<Client> launch(DalvikPlatform platform, Lookup context, String mode);

  public AvdSelector.LaunchData configAvd(
      SdkManager sdkManager, IAndroidTarget target, LaunchConfiguration launchCfg);

  public boolean simpleLaunch(LaunchInfo launchInfo, IDevice device);
}
