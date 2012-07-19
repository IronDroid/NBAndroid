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

package org.netbeans.modules.android.grammars.resources;

import org.junit.BeforeClass;
import org.netbeans.modules.android.grammars.*;
import java.util.List;
import org.junit.Test;
import org.netbeans.modules.android.core.sdk.DalvikPlatformManager;
import static org.junit.Assert.*;

/**
 * Test functionality of AndroidGrammar.
 */
public class ResourcesGrammarTest {

  private static final String SDK_DIR = System.getProperty("test.all.android.sdks.home");

  private static final String PRELUDE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
      + "<resources>";
  private static final String CODA = "</resources>";

  private static ResourcesGrammar grammar;

  @BeforeClass
  public static void setUp() throws Exception {
    DalvikPlatformManager.getDefault().setSdkLocation(SDK_DIR);
    grammar = new ResourcesGrammar();
  }

  @Test
  public void testTags() throws Exception {
    String p = PRELUDE + "  <sHERE/>\n" + CODA;
    List<String> l = TestUtil.grammarResultValues(grammar.queryElements(TestUtil.createCompletion(p)));
    assertTrue(grammar.toString(), l.contains("string"));
    assertTrue(grammar.toString(), l.contains("string-array"));
    assertFalse(grammar.toString(), l.contains("color"));
  }

  @Test
  public void testNestedTags() throws Exception {
    String p = PRELUDE + "  <integer-array name=\"ints\"><iHERE/>\n</integer-array>" + CODA;
    List<String> l = TestUtil.grammarResultValues(grammar.queryElements(TestUtil.createCompletion(p)));
    assertTrue(grammar.toString(), l.contains("item"));

    p = PRELUDE + "  <string-array name=\"texts\"><iHERE/>\n</string-array>" + CODA;
    l = TestUtil.grammarResultValues(grammar.queryElements(TestUtil.createCompletion(p)));
    assertTrue(grammar.toString(), l.contains("item"));
  }

  @Test
  public void testAttrInNonExistingTag() throws Exception {
    // there is no tag 'strings'
    String p = PRELUDE + "  <strings naHERE=\"\"></strings>\n" + CODA;
    List<String> l = TestUtil.grammarResultValues(grammar.queryAttributes(TestUtil.createCompletion(p)));
    assertTrue(l.isEmpty());
  }

  @Test
  public void testAttrName() throws Exception {
    // there is no tag 'strings'
    String p = PRELUDE + "  <string naHERE=\"\"></string>\n" + CODA;
    List<String> l = TestUtil.grammarResultValues(grammar.queryAttributes(TestUtil.createCompletion(p)));
    assertTrue(grammar.toString(), l.contains("name"));
    assertEquals(1, l.size());
  }

}
