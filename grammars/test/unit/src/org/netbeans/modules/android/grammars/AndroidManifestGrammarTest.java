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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.android.core.sdk.DalvikPlatform;
import org.netbeans.modules.android.core.sdk.DalvikPlatformManager;
import org.netbeans.modules.android.core.sdk.Utils;

/**
 * Test functionality of AndroidGrammar.
 */
public class AndroidManifestGrammarTest extends NbTestCase {

  private static final String SDK_DIR = System.getProperty("test.all.android.sdks.home");

  private static final String PRELUDE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
      "    package=\"org.radim.simpleandroid\">\n";

  public AndroidManifestGrammarTest(String name) {
      super(name);
  }

  private List<AndroidManifestGrammar> grammars;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    DalvikPlatformManager.getDefault().setSdkLocation(SDK_DIR);
    grammars = new ArrayList<AndroidManifestGrammar>();
    for (Utils.TestPlatform p : Utils.allPlatforms()) {
      DalvikPlatform platform = DalvikPlatformManager.getDefault().findPlatformForTarget(p.getTarget());
      assertNotNull(platform);
      grammars.add(new AndroidManifestGrammar(platform));
    }
  }

  public void testTags() throws Exception {
    String p = PRELUDE + "  <application><aHERE/>\n" +
        "  </application>\n</manifest>\n";
    for (AndroidManifestGrammar g : grammars) {
      List<String> l = TestUtil.grammarResultValues(g.queryElements(TestUtil.createCompletion(p)));
      assertTrue(g.toString(), l.contains("activity"));
      assertFalse(g.toString(), l.contains("application"));
    }
  }

  public void testFirstLevelTags() throws Exception {
    String p = PRELUDE + "  <aHERE/>\n</manifest>\n";
    for (AndroidManifestGrammar g : grammars) {
      List<String> l = TestUtil.grammarResultValues(g.queryElements(TestUtil.createCompletion(p)));
      assertTrue(g.toString(), l.contains("application"));
      assertFalse(g.toString(), l.contains("intent-filter"));
    }
  }

  public void testTagWithMoreParents() throws Exception {
    String p = PRELUDE + "  <application><activity android:name=\"foo\" android:label=\"a\"><iHERE/>\n" +
        "  </activity></application>\n</manifest>\n";
    for (AndroidManifestGrammar g : grammars) {
      List<String> l = TestUtil.grammarResultValues(g.queryElements(TestUtil.createCompletion(p)));
      assertTrue(g.toString(), l.contains("intent-filter"));
      assertFalse(g.toString(), l.contains("application"));
    }
  }

  public void testAttributes() throws Exception {
    String p = PRELUDE + "  <application><activity andHERE=\"\"/>\n" +
        "  </application>\n</manifest>\n";
    for (AndroidManifestGrammar g : grammars) {
      List<String> l = TestUtil.grammarResultValues(g.queryAttributes(TestUtil.createCompletion(p)));
      assertTrue(g.toString(), l.contains("android:label"));
      assertTrue(g.toString(), l.contains("android:name"));
    }
  }

  public void testAttributes2() throws Exception {
    String p = PRELUDE + "  <application><activity android:name=\"foo\" andHERE=\"\"/>\n" +
        "  </application>\n</manifest>\n";
    for (AndroidManifestGrammar g : grammars) {
      List<String> l = TestUtil.grammarResultValues(g.queryAttributes(TestUtil.createCompletion(p)));
      assertTrue(g.toString(), l.contains("android:label"));
      assertFalse(g.toString(), l.contains("android:name"));
    }
  }

  public void testAttributes3() throws Exception {
    String p = PRELUDE + "  <application><activity android:name=\"foo\" android:eHERE=\"\"/>\n" +
        "  </application>\n</manifest>\n";
    for (AndroidManifestGrammar g : grammars) {
      List<String> l = TestUtil.grammarResultValues(g.queryAttributes(TestUtil.createCompletion(p)));
      assertTrue(g.toString(), l.contains("android:enabled"));
      assertFalse(g.toString(), l.contains("android:name"));
    }
  }

  public void testAttrBooleanValues() throws Exception {
    String p = PRELUDE + "  <application android:debuggable=\"HERE\">\n" +
        "  </application>\n</manifest>\n";
    for (AndroidManifestGrammar g : grammars) {
      List<String> l = TestUtil.grammarResultValues(g.queryValues(TestUtil.createCompletion(p)));
      assertTrue(g.toString(), l.contains("true"));
      assertTrue(g.toString(), l.contains("false"));
    }
  }

  public void testAttrValues() throws Exception {
    String p = PRELUDE + "  <permission android:protectionLevel=\"sigHERE\">\n" +
        "  </permission>\n</manifest>\n";
    for (AndroidManifestGrammar g : grammars) {
      List<String> l = TestUtil.grammarResultValues(g.queryValues(TestUtil.createCompletion(p)));
      assertTrue(g.toString(), l.contains("signature"));
      assertTrue(g.toString(), l.contains("signatureOrSystem"));
    }
  }
}
