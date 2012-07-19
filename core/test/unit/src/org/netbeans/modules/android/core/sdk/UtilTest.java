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

import java.io.IOException;
import org.junit.Test;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import static org.junit.Assert.*;

/**
 *
 * @author radim
 */
public class UtilTest {

  @Test
  public void findToolIn() throws IOException {
    for (Utils.TestPlatform p : Utils.TestPlatform.values()) {
      FileObject sdkDir = Utils.createSdkPlatform(p);
      FileObject toolsParent = sdkDir.getParent().getParent();
      for (Tool t : Util.sdkTools()) {
        FileObject toolFO = Util.findTool(
            t.getSystemName(), sdkDir.getParent().getParent().getFileObject(p.getPlatformDir()));
        assertNotNull(t.getSystemName() + " found in " + p, toolFO);
        assertTrue(t.getSystemName() + " found in " + p,
            FileUtil.isParentOf(toolsParent, toolFO));
      }
    }
  }

  @Test
  public void findResourceIn() throws IOException {
    for (Utils.TestPlatform p : Utils.TestPlatform.values()) {
      FileObject sdkDir = Utils.createSdkPlatform(p);
      FileObject toolsParent = sdkDir.getParent().getParent();
      for (Tool t : Util.sdkResources()) {

        FileObject toolFO = Util.findTool(
            t.getSystemName(), sdkDir.getParent().getParent().getFileObject(p.getPlatformDir()));
        assertNotNull(t.getSystemName() + " found in " + p, toolFO);
        assertTrue(t.getSystemName() + " found in " + p,
            FileUtil.isParentOf(toolsParent, toolFO));
      }
    }
  }
}