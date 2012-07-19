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

import org.netbeans.modules.android.project.launch.LaunchConfiguration;
import com.android.ddmlib.IDevice;
import com.android.prefs.AndroidLocation.AndroidLocationException;
import com.android.sdklib.IAndroidTarget;
import com.android.sdklib.SdkManager;
import com.android.sdklib.internal.avd.AvdManager;
import com.android.sdklib.internal.avd.AvdInfo;
import junit.framework.TestCase;
import org.netbeans.modules.android.project.AvdSelector.AvdManagerMock;
import org.netbeans.modules.android.project.AvdSelector.LaunchData;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link AvdSelector}
 *
 * @author Radim
 */
public class AvdSelectorTest extends TestCase {

  private static final String MY_AVD = "simple_avd";
  private static final String BAD_AVD = "bad_avd";

  public AvdSelectorTest(String testName) {
    super(testName);
  }

  // no devices reported by bridge

  public void testAutoWithSelectedNameAvailable() {
    final IAndroidTarget target = mock(IAndroidTarget.class);
    AvdSelector.AvdManagerMock avdManager = new TestAvdManager(
        target, new AvdInfo(MY_AVD, null, "path", "targetHash", target, null, null));
    when(target.canRunOn(target)).thenReturn(Boolean.TRUE);
    AvdSelector s = new AvdSelector(LaunchConfiguration.TargetMode.AUTO, MY_AVD, avdManager, target, null);
    LaunchData launch = s.selectAvdName();
    assertEquals(MY_AVD, launch.getAvdInfo().getName());
  }

  public void testAutoWithoutSelectedAndOneExactTargetMatch() {
    final IAndroidTarget target = mock(IAndroidTarget.class);
    AvdSelector.AvdManagerMock avdManager = new TestAvdManager(
        target, new AvdInfo(MY_AVD, null, "path", "targetHash", target, null, null));
    when(target.canRunOn(target)).thenReturn(Boolean.TRUE);
    AvdSelector s = new AvdSelector(LaunchConfiguration.TargetMode.AUTO, null, avdManager, target, null);
    LaunchData launch = s.selectAvdName();
    assertEquals(MY_AVD, launch.getAvdInfo().getName());
  }

  public void testAutoWithoutSelectedFindExactTargetMatch() {
    final IAndroidTarget target = mock(IAndroidTarget.class);
    final IAndroidTarget incompatibleTarget = mock(IAndroidTarget.class);
    AvdSelector.AvdManagerMock avdManager = new TestAvdManager(
        target,
        new AvdInfo(BAD_AVD, null, "path", "targetHash", incompatibleTarget, null, null),
        new AvdInfo(MY_AVD, null, "path", "targetHash", target, null, null));
    when(target.canRunOn(target)).thenReturn(Boolean.TRUE);
    when(target.canRunOn(incompatibleTarget)).thenReturn(Boolean.FALSE);
    AvdSelector s = new AvdSelector(LaunchConfiguration.TargetMode.AUTO, null, avdManager, target, null);
    LaunchData launch = s.selectAvdName();
    assertEquals(MY_AVD, launch.getAvdInfo().getName());
  }

  public void testAutoWithoutSelectedFindCompatibleTargetMatch() {
    final IAndroidTarget target = mock(IAndroidTarget.class);
    final IAndroidTarget compatibleTarget = mock(IAndroidTarget.class);
    final IAndroidTarget incompatibleTarget = mock(IAndroidTarget.class);
    AvdSelector.AvdManagerMock avdManager = new TestAvdManager(
        target,
        new AvdInfo(MY_AVD, null, "path", "targetHash", compatibleTarget, null, null),
        new AvdInfo(BAD_AVD, null, "path", "targetHash", incompatibleTarget, null, null));
    when(target.canRunOn(compatibleTarget)).thenReturn(Boolean.TRUE);
    when(target.canRunOn(incompatibleTarget)).thenReturn(Boolean.FALSE);
    AvdSelector s = new AvdSelector(LaunchConfiguration.TargetMode.AUTO, null, avdManager, target, null);
    LaunchData launch = s.selectAvdName();
    assertEquals(MY_AVD, launch.getAvdInfo().getName());
  }

  public void testAutoWithoutSelectedNoAvds() {
    final IAndroidTarget target = mock(IAndroidTarget.class);
    AvdSelector.AvdManagerMock avdManager = new TestAvdManager(
        target);
    AvdSelector s = new AvdSelector(LaunchConfiguration.TargetMode.AUTO, null, avdManager, target, null);

    LaunchData launch = s.selectAvdName();
    assertNull(launch);
  }

  // emulator already running and phone attached

  public void testAutoAvdTwoAdbDevices() {
    final IAndroidTarget target = mock(IAndroidTarget.class);
    IDevice phone = mock(IDevice.class);
    when(phone.getAvdName()).thenReturn(null);
    when(phone.getProperty(SdkManager.PROP_VERSION_SDK)).thenReturn("1.5");
    IDevice emulator = mock(IDevice.class);
    when(emulator.getAvdName()).thenReturn(MY_AVD);
    AvdSelector.AvdManagerMock avdManager = new TestAvdManager(
        target, new AvdInfo(MY_AVD, null, "path", "targetHash", target, null, null));
    when(target.canRunOn(target)).thenReturn(Boolean.TRUE);
    AvdSelector s = new AvdSelector(LaunchConfiguration.TargetMode.AUTO, MY_AVD, avdManager, target,
        new IDevice[] { phone, emulator });

    LaunchData launch = s.selectAvdName();
    assertEquals(MY_AVD, launch.getAvdInfo().getName());
    assertEquals(emulator, launch.getDevice());
  }

  public void testAutoTwoAdbDevicesButNeedsAddOn() {
    // if we need add-on we need to pick emulator that has it
    final IAndroidTarget target = mock(IAndroidTarget.class);
    when(target.isPlatform()).thenReturn(Boolean.FALSE);
    IDevice phone = mock(IDevice.class);
    when(phone.getAvdName()).thenReturn(null);
    when(phone.getProperty(SdkManager.PROP_VERSION_SDK)).thenReturn("1.5");
    IDevice emulator = mock(IDevice.class);
    when(emulator.getAvdName()).thenReturn(MY_AVD);
    AvdSelector.AvdManagerMock avdManager = new TestAvdManager(
        target, new AvdInfo(MY_AVD, null, "path", "targetHash", target, null, null));
    when(target.canRunOn(target)).thenReturn(Boolean.TRUE);
    AvdSelector s = new AvdSelector(LaunchConfiguration.TargetMode.AUTO, null, avdManager, target,
        new IDevice[] { phone, emulator });

    LaunchData launch = s.selectAvdName();
    assertNull(launch); // though there is a compatible emulator we are not sure about phone
    // assertEquals(MY_AVD, launch.getAvdInfo().getName());
    // assertEquals(emulator, launch.getDevice());
  }

  // test - when I have avd selected and emulator is not started but i have phone attached it is used though it is not guaranteed that it is google_apis
  // and build script will not start the emulator

  private static class TestAvdManager implements AvdManagerMock {

    private final IAndroidTarget target;
    private final AvdInfo[] infos;

    public TestAvdManager(IAndroidTarget target, AvdInfo ... infos) {
      this.target = target;
      this.infos = infos;
    }

    public void reloadAvds() throws AndroidLocationException {
    }

    public AvdInfo getAvd(String name, boolean validAvdOnly) {
      return MY_AVD.equals(name) ? infos[0] : null;
    }

    public AvdInfo[] getValidAvds() {
      return infos;
    }

    public AvdManager getAvdManager() {
      throw new UnsupportedOperationException("Not supported yet."); // should be used by UI only
    }
  }
}
