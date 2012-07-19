/*
 *  Copyright 2009 radim.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.netbeans.modules.android.core.sdk;

import java.io.File;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author radim
 */
public class DalvikPlatformManagerTest {

  private static final String SDK_DIR = System.getProperty("test.all.android.sdks.home");

  private DalvikPlatformManager dpm;

  @Before
  public void setUp() {
    dpm = new DalvikPlatformManager();
  }

  @Test
  public void emptyManager() {
    assertFalse(dpm.getPlatforms().iterator().hasNext());
  }

  @Test
  public void wrongManager() throws IOException {
    File tmpFile = File.createTempFile("android", null);
    DalvikPlatformManager dpm2 = new DalvikPlatformManager();
    dpm2.setSdkLocation(tmpFile.getAbsolutePath());
    assertFalse(dpm2.getPlatforms().iterator().hasNext());
    assertNull(dpm2.getSdkLocation());
    tmpFile.delete();
  }

  @Test
  public void nonEmptyManager() {
    dpm.setSdkLocation(SDK_DIR);
    for (DalvikPlatform p : dpm.getPlatforms()) {
      System.err.println(p.getInstallFolder());
    }
    assertTrue(dpm.getPlatforms().iterator().hasNext());
  }

  // TODO set location and check new content
}