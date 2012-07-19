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

import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.Map;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.android.core.sdk.Tool;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.modules.android.core.sdk.DalvikPlatform;
import org.netbeans.modules.android.core.sdk.DalvikPlatformManager;
import org.netbeans.modules.android.core.sdk.Utils;
import org.openide.filesystems.URLMapper;
import static org.junit.Assert.*;

/**
 * Test functionality of AndroidGrammar.
 */
public class LayoutClassesParserTest {

  private static final String SDK_DIR = System.getProperty("test.all.android.sdks.home");

  @BeforeClass
  public static void classSetup() {
    DalvikPlatformManager.getDefault().setSdkLocation(SDK_DIR);
  }

  @Test
  public void testAndroidLayoutsModel() throws Exception {
    for (Utils.TestPlatform p : Utils.allPlatforms()) {
      DalvikPlatform platform = DalvikPlatformManager.getDefault().findPlatformForTarget(p.getTarget());
      assertNotNull(platform);
      FileObject attrsLayoutFO = platform.findTool(Tool.WIDGETS.getSystemName());
      LayoutClassesParser parser = new LayoutClassesParser(URLMapper.findURL(attrsLayoutFO, URLMapper.INTERNAL));
      WidgetData data = parser.load();

      UIClassDescriptor clz = Iterables.getOnlyElement(UIClassDescriptors.findBySimpleName(data, "LinearLayout"));
      assertNotNull("LinearLayout in " + p, clz);
      assertTrue(Iterables.isEmpty(
          UIClassDescriptors.findBySimpleName(data, LayoutElementType.LAYOUT_PARAM, "LinearLayout")));
      clz = Iterables.getOnlyElement(
          UIClassDescriptors.findBySimpleName(data, LayoutElementType.VIEW_GROUP, "LinearLayout"));
      assertNotNull(clz);
      assertEquals("LinearLayout in " + p, "android.widget.LinearLayout", clz.getFQClassName());
      assertEquals("LinearLayout in " + p, "android.view.ViewGroup", clz.getSuperclass());

      clz = UIClassDescriptors.findParamsForName(data, "android.widget.LinearLayout");
      assertEquals("LinearLayout params in " + p,
          "android.widget.LinearLayout.LayoutParams", clz.getFQClassName());
      assertEquals("LinearLayout params superclass in " + p, 
          "android.view.ViewGroup.MarginLayoutParams", clz.getSuperclass());

      UIClassDescriptor pClz = Iterables.getOnlyElement(
          UIClassDescriptors.findBySimpleName(data, LayoutElementType.LAYOUT_PARAM, "LinearLayout.LayoutParams"));
      assertNotNull(pClz);
      assertEquals("LinearLayout.LayoutParams in " + p,
          "android.widget.LinearLayout.LayoutParams", pClz.getFQClassName());
      pClz = UIClassDescriptors.findByFQName(data, pClz.getSuperclass());
      assertNotNull(pClz);
      assertEquals("MarginLayoutParams params in " + p,
          "android.view.ViewGroup.MarginLayoutParams", pClz.getFQClassName());

      // we can find descriptors by their names
      assertNotNull(UIClassDescriptors.findByFQName(data, "android.view.ViewGroup"));
    }
  }
}
