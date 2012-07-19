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

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.io.ByteArrayInputStream;
import org.netbeans.modules.android.core.sdk.DalvikPlatform;
import java.io.InputStream;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.android.core.sdk.DalvikPlatformManager;
import org.openide.filesystems.FileObject;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests for AndroidActionProvider
 */
public class AndroidProjectUtilTest extends NbTestCase {

  private static final String SDK_DIR = System.getProperty("test.all.android.sdks.home");
  private static final String DUMMY_SDK_DIR = "/some/path/to/android-sdk-linux_x86";

  public AndroidProjectUtilTest(String name) {
    super(name);
  }

  public void testParseAndroidPlatform() throws Exception {
    FileObject platformFile = mock(FileObject.class);
    when(platformFile.getInputStream()).thenAnswer(new Answer<InputStream>() {

      @Override
      public InputStream answer(InvocationOnMock invocation) throws Throwable {
        String platformXml = Resources.toString(getClass().getResource("Android_2.2.xml"), Charsets.UTF_8);
        platformXml = platformXml.replace(DUMMY_SDK_DIR, SDK_DIR);
        return new ByteArrayInputStream(platformXml.getBytes());
      }

    });

    assertNull(AndroidProjectUtil.parseIfActivePlatorm(platformFile, "Android"));
    assertEquals("file:" + SDK_DIR + "/platforms/android-8/",
        AndroidProjectUtil.parseIfActivePlatorm(platformFile, "Android_2.2"));
  }

  public void testToPlatform() {
    DalvikPlatformManager dpm = DalvikPlatformManager.getDefault();
    // run w/o SDK to check if we can init it
    assertNull(dpm.getSdkLocation());

    DalvikPlatform platform = AndroidProjectUtil.toDalvikPlatorm(
        "file:" + SDK_DIR + "/platforms/android-8/");
    assertNotNull(platform);
    assertEquals(SDK_DIR, dpm.getSdkLocation());

    // another try with URL that uses same SDK
    platform = AndroidProjectUtil.toDalvikPlatorm(
        "file:" + SDK_DIR + "/platforms/android-7/");
    assertNotNull(platform);
    assertEquals(SDK_DIR, dpm.getSdkLocation());
    // another try with URL that uses different SDK should fail
    platform = AndroidProjectUtil.toDalvikPlatorm(
        "file:/another/path/android-sdk-linux_x86/platforms/android-7/");
    assertNull(platform);
    assertEquals(SDK_DIR, dpm.getSdkLocation());
  }
}
