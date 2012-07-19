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

import com.android.ddmlib.IDevice;
import com.android.ddmlib.InstallException;
import java.io.IOException;
import org.junit.Test;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author radim
 */
public class AndroidLauncherImplTest {

  /**
   * Test of installPackage method, of class AndroidLauncherImpl.
   */
  @Test
  public void testInstallPackage() throws Exception {
    FileSystem fs = FileUtil.createMemoryFileSystem();
    FileObject apk = FileUtil.createData(fs.getRoot(), "foo/bin/foo-debug.apk");

    LaunchInfo launchInfo = new LaunchInfo(apk, true, false, null, null);
    String remotePackagePath = "/tmp/xyz";
    IDevice device = mock(IDevice.class);
    when(device.installRemotePackage(remotePackagePath, true)).thenReturn(null);

    AndroidLauncherImpl instance = new AndroidLauncherImpl();
    boolean result = instance.installPackage(launchInfo, remotePackagePath, device);
    assertTrue(result);
  }

  /**
   * Test of installPackage method, of class AndroidLauncherImpl.
   */
  @Test
  public void testInstallPackageWhenNotDeployed() throws Exception {
    FileSystem fs = FileUtil.createMemoryFileSystem();
    FileObject apk = FileUtil.createData(fs.getRoot(), "foo/bin/foo-debug.apk");

    LaunchInfo launchInfo = new LaunchInfo(apk, true, false, null, null);
    String remotePackagePath = "/tmp/xyz";
    IDevice device = mock(IDevice.class);
    when(device.installRemotePackage(remotePackagePath, true)).thenThrow(
        new InstallException(new IOException()));
    when(device.installRemotePackage(remotePackagePath, false)).thenReturn(null);

    AndroidLauncherImpl instance = new AndroidLauncherImpl();
    boolean result = instance.installPackage(launchInfo, remotePackagePath, device);
    assertTrue(result);
  }

}