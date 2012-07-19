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
package org.netbeans.modules.android.grammars;

import java.util.List;
import java.util.Set;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.modules.android.core.sdk.DalvikPlatform;
import org.netbeans.modules.android.core.sdk.DalvikPlatformManager;
import org.netbeans.modules.android.core.sdk.Utils;
import static org.junit.Assert.*;

/**
 * Test functionality of AndroidGrammar.
 */
public class StyleableModelsTest {

  private static final String SDK_DIR = System.getProperty("test.all.android.sdks.home");

  @BeforeClass
  public static void classSetup() {
    DalvikPlatformManager.getDefault().setSdkLocation(SDK_DIR);
  }

  @Test
  public void testAndroidManifestModel() throws Exception {
    for (Utils.TestPlatform p : Utils.allPlatforms()) {
      DalvikPlatform platform = DalvikPlatformManager.getDefault().findPlatformForTarget(p.getTarget());
      assertNotNull(platform);
      StyleableModel manifestModel = StyleableModels.getAndroidManifestModel(platform);
      assertNotNull(manifestModel);
      Set<String> names = manifestModel.getStyleables().keySet();
      assertTrue(names.contains("application"));
      assertNotNull(manifestModel.getStyleables().get("application").getDescription());
      assertTrue(names.contains("activity"));
      assertTrue(names.contains("intent-filter"));

      // name in protected-broadcast has generic attribute description
      // but it is missing in old versions
      StyleableInfo style = manifestModel.getStyleables().get("protected-broadcast");
      if (style != null) {
        List<AttributeInfo> attrs = manifestModel.getStyleables().get("protected-broadcast").getAttributeNames();
        assertTrue(attrs.size() > 0);
        AttributeInfo attr = attrs.get(0);
        assertEquals("android:name", attr.getName());
        assertTrue(attr + " has description", attr.getDescription().contains("A unique name for the given item"));
      }

      // name in uses-permission has its own description
      List<AttributeInfo> attrs2 = manifestModel.getStyleables().get("uses-permission").getAttributeNames();
      assertTrue(attrs2.size() > 0);
      AttributeInfo attr2 = attrs2.get(0);
      assertEquals("android:name", attr2.getName());
      assertTrue(attr2 + " has description", attr2.getDescription().contains("name of the permission you use"));
    }
  }

  @Test
  public void testAndroidLayoutsModel() throws Exception {
    for (Utils.TestPlatform p : Utils.allPlatforms()) {
      DalvikPlatform platform = DalvikPlatformManager.getDefault().findPlatformForTarget(p.getTarget());
      assertNotNull(platform);

      StyleableModel manifestModel = AndroidLayoutGrammar.create(platform, new NullRefResolver()).getStyleableModel();
      assertNotNull(manifestModel);
      Set<String> names = manifestModel.getStyleables().keySet();
      assertTrue(!names.isEmpty());
      assertNotNull("There are styleables in " + platform, 
          manifestModel.getStyleables());
      assertNotNull("Theme is regonized " + platform, 
          manifestModel.getStyleables().get("Theme"));
      assertNotNull("Theme has description in " + platform, 
          manifestModel.getStyleables().get("Theme").getDescription());
      assertNotSame("Descriptions in " + platform + " are not the same", 
          manifestModel.getStyleables().get("Theme").getDescription(),
          manifestModel.getStyleables().get("Window").getDescription());
    }
  }
}
