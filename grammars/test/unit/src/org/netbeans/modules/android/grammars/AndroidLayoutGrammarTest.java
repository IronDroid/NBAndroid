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

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.modules.android.core.sdk.DalvikPlatform;
import org.netbeans.modules.android.core.sdk.DalvikPlatformManager;
import org.netbeans.modules.android.core.sdk.Utils;
import static org.junit.Assert.*;

/**
 * Test functionality of AndroidGrammar.
 */
public class AndroidLayoutGrammarTest {

  private static final String SDK_DIR = System.getProperty("test.all.android.sdks.home");

  private static final String PRELUDE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
  private static final String XMLNS = " xmlns:android=\"http://schemas.android.com/apk/res/android\"\n";
  private static final String LAYOUT_ATTRS = "    android:orientation=\"vertical\""
      + "    android:layout_width=\"fill_parent\""
      + "    android:layout_height=\"fill_parent\">";

  private static List<AndroidLayoutGrammar> grammars;

  @BeforeClass
  public static void setUp() throws Exception {
    DalvikPlatformManager.getDefault().setSdkLocation(SDK_DIR);
    ReferenceResolver rr = new ReferenceResolver() {

      @Override
      public List<ResourceRef> getReferences() {
        return Lists.newArrayList(
            new ResourceRef(true, "com.example.android.snake", "id", "snake"),
            new ResourceRef(true, "com.example.android.snake", "id", "text"),
            new ResourceRef(true, "com.example.android.snake", "string", "mode_lose_prefix"),
            new ResourceRef(true, "com.example.android.snake", "string", "mode_lose_suffix"),
            new ResourceRef(true, "com.example.android.snake", "string", "mode_pause"));
      }
    };

    grammars = Lists.newArrayList();
    for (Utils.TestPlatform p : Utils.allPlatforms()) {
      DalvikPlatform platform = DalvikPlatformManager.getDefault().findPlatformForTarget(p.getTarget());
      assertNotNull(platform);
      grammars.add(AndroidLayoutGrammar.create(platform, rr));
    }
  }

  @Test
  public void testTags() throws Exception {
    String p = PRELUDE + "  <LinearLayout " + XMLNS + LAYOUT_ATTRS + "<THERE/>\n" +
        "  </LinearLayout>\n";
    for (AndroidLayoutGrammar g : grammars) {
      List<String> l = TestUtil.grammarResultValues(g.queryElements(TestUtil.createCompletion(p)));
      assertTrue(g.toString(), l.contains("TextView"));
      assertFalse(g.toString(), l.contains("Button"));
    }
  }

  @Test
  public void testLinearLayoutWidth() throws Exception {
    // to get layout_width  for LinearLayout we need to look at it layout attrs of its superclass
    // ViewGroup_Layout -> ViewGroup.LayoutParams
    // to get gravity for LinearLayout we need to look at its layout params
    // LinearLayout_Layout -> LinearLayout.LayoutParams
    String p = Resources.toString(AndroidLayoutGrammarTest.class.getResource("linLayoutAttrs.xml"), Charsets.UTF_8);
    for (AndroidLayoutGrammar g : grammars) {
      List<String> l = TestUtil.grammarResultValues(g.queryAttributes(TestUtil.createCompletion(p)));
      assertTrue(g.toString(), l.contains("android:layout_width"));
      assertTrue(g.toString(), l.contains("android:gravity"));
    }
  }

  @Test
  public void testLinearLayoutWidthInChild() throws Exception {
    // to get layout_width  for a child in a LinearLayout we need to look at it layout attrs of a superclass of this
    // layout ViewGroup_Layout -> ViewGroup.LayoutParams
    String p = Resources.toString(AndroidLayoutGrammarTest.class.getResource("linLayoutAttrs1.xml"), Charsets.UTF_8);
    for (AndroidLayoutGrammar g : grammars) {
      List<String> l = TestUtil.grammarResultValues(g.queryAttributes(TestUtil.createCompletion(p)));
      assertTrue(g.toString(), l.contains("android:layout_width"));
      assertEquals("just one result " + l, 1, l.size());
    }
  }

  @Test
  public void testTagsDoNotHaveParamStyleables() throws Exception {
    String p = PRELUDE + "  <LinearLayout " + XMLNS + LAYOUT_ATTRS + "<LinearLHERE/>\n" +
        "  </LinearLayout>\n";
    for (AndroidLayoutGrammar g : grammars) {
      List<String> l = TestUtil.grammarResultValues(g.queryElements(TestUtil.createCompletion(p)));
      assertTrue(g.toString(), l.contains("LinearLayout"));
      assertFalse(g.toString(), l.contains("LinearLayout_Layout"));
      assertFalse(g.toString(), l.contains("android:bottom"));
    }
  }

  @Test
  public void testTagsDoNotHaveParamStyleables2() throws Exception {
    String p = PRELUDE + "  <LinearLayout " + XMLNS + LAYOUT_ATTRS + "<aHERE/>\n" +
        "  </LinearLayout>\n";
    for (AndroidLayoutGrammar g : grammars) {
      List<String> l = TestUtil.grammarResultValues(g.queryElements(TestUtil.createCompletion(p)));
      assertFalse(g.toString(), l.contains("android:bottom"));
    }
  }

  @Test
  public void testAttributes() throws Exception {
    String p = PRELUDE + "  <LinearLayout " + XMLNS + LAYOUT_ATTRS + "<TextView android:HERE=\"foo\"/>\n" +
        "  </LinearLayout>\n";
    for (AndroidLayoutGrammar g : grammars) {
      List<String> l = TestUtil.grammarResultValues(g.queryAttributes(TestUtil.createCompletion(p)));
      assertTrue(g.toString(), l.contains("android:id"));
      assertTrue(g.toString(), l.contains("android:layout_height"));
    }
  }

  @Test
  public void testAttributeValues() throws Exception {
    String p = PRELUDE + "  <LinearLayout " + XMLNS + LAYOUT_ATTRS + "<TextView android:layout_width=\"HERE\"/>\n" +
        "  </LinearLayout>\n";
    for (AndroidLayoutGrammar g : grammars) {
      List<String> l = TestUtil.grammarResultValues(g.queryValues(TestUtil.createCompletion(p)));
      assertTrue(g.toString(), l.contains("fill_parent")); // or match_parent that deprecates fill_parent since API level 8
      assertTrue(g.toString(), l.contains("wrap_content"));
      System.err.println("choices results " + l);
      assertTrue(l.size() <= 3); // 2 or 3
    }
  }

  @Test
  public void testAttributeValueEnum() throws Exception {
    String p = PRELUDE + "  <LinearLayout " + XMLNS + LAYOUT_ATTRS + "<TextView android:bufferType=\"HERE\"/>\n" +
        "  </LinearLayout>\n";
    for (AndroidLayoutGrammar g : grammars) {
      List<String> l = TestUtil.grammarResultValues(g.queryValues(TestUtil.createCompletion(p)));
      assertTrue(g.toString(), l.contains("normal"));
      assertTrue(g.toString(), l.contains("editable"));
      assertTrue(g.toString(), l.contains("spannable"));
    }
  }

  @Test
  public void testAttributeValuesDimensions() throws Exception {
    String p = PRELUDE + "  <LinearLayout " + XMLNS + LAYOUT_ATTRS + "<TextView android:layout_width=\"200HERE\"/>\n" +
        "  </LinearLayout>\n";
    for (AndroidLayoutGrammar g : grammars) {
      List<String> l = TestUtil.grammarResultValues(g.queryValues(TestUtil.createCompletion(p)));
      for (String unit : new String[] {"dp", "sp", "pt", "mm", "in", "px"})
      assertTrue(g.toString(), l.contains("200" + unit));
    }
  }

  @Test
  public void testAttributeValuesDimensions2() throws Exception {
    String p = PRELUDE + "  <LinearLayout " + XMLNS + LAYOUT_ATTRS + "<TextView android:textSize=\"15pHERE\"/>\n" +
        "  </LinearLayout>\n";
    for (AndroidLayoutGrammar g : grammars) {
      List<String> l = TestUtil.grammarResultValues(g.queryValues(TestUtil.createCompletion(p)));
      for (String unit : new String[] {"pt", "px"})
      assertTrue(g.toString(), l.contains("15" + unit));
    }
  }

  @Test
  public void references() throws Exception {
    String p = Resources.toString(AndroidLayoutGrammarTest.class.getResource("linLayoutAttrs2.xml"), Charsets.UTF_8);
    p = p.replace("HERE", "@string/moHERE");
    for (AndroidLayoutGrammar g : grammars) {
      List<String> l = TestUtil.grammarResultValues(g.queryValues(TestUtil.createCompletion(p)));
      assertTrue(g.toString(), l.contains("@string/mode_lose_prefix"));
      assertTrue(g.toString(), l.contains("@string/mode_lose_suffix"));
      assertTrue(g.toString(), l.contains("@string/mode_pause"));
      assertEquals(3, l.size());
    }
  }

  @Test
  public void referenceTypes() throws Exception {
    String p = Resources.toString(AndroidLayoutGrammarTest.class.getResource("linLayoutAttrs2.xml"), Charsets.UTF_8);
    p = p.replace("HERE", "@sHERE");
    for (AndroidLayoutGrammar g : grammars) {
      List<String> l = TestUtil.grammarResultValues(g.queryValues(TestUtil.createCompletion(p)));
      assertTrue(g.toString(), l.contains("@string/"));
      assertEquals(1, l.size());
    }
  }

  @Test
  public void referenceTypesWithPlus() throws Exception {
    String p = Resources.toString(AndroidLayoutGrammarTest.class.getResource("linLayoutAttrs2.xml"), Charsets.UTF_8);
    p = p.replace("HERE", "@+iHERE");
    for (AndroidLayoutGrammar g : grammars) {
      List<String> l = TestUtil.grammarResultValues(g.queryValues(TestUtil.createCompletion(p)));
      assertTrue(g.toString(), l.contains("@+id/"));
      assertEquals(1, l.size());
    }
  }
}
