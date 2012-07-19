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

import com.android.sdklib.xml.ManifestData;
import org.openide.filesystems.FileObject;

/**
 * Container to hold data needed for application launching.
 *
 * @author radim
 */
public class LaunchInfo {
  // TODO(radim) add install retry mode, ...

  public final FileObject packageFile;
  public final boolean reinstall;
  public final boolean debug;
  public final LaunchConfiguration launchConfig;
  public final ManifestData manifestData;

  public LaunchInfo(FileObject packageFile, boolean reinstall, boolean debug,
      LaunchConfiguration launchConfig, ManifestData manifestData) {
    this.packageFile = packageFile;
    this.reinstall = reinstall;
    this.debug = debug;
    this.launchConfig = launchConfig;
    this.manifestData = manifestData;
  }
}
