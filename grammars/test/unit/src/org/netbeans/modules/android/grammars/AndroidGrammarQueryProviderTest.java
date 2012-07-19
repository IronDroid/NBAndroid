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

import org.netbeans.modules.android.grammars.resources.ResourcesGrammar;
import org.netbeans.modules.android.project.AndroidGeneralData;
import org.netbeans.modules.xml.api.model.GrammarQuery;
import org.netbeans.modules.android.project.AndroidProject;
import org.netbeans.modules.android.project.FileUtilities;
import java.io.File;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.android.core.sdk.DalvikPlatformManager;
import org.netbeans.modules.xml.api.model.GrammarEnvironment;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Enumerations;
import org.xml.sax.InputSource;
import static org.junit.Assert.*;

/**
 * Tests for AndroidActionProvider
 */
public class AndroidGrammarQueryProviderTest {

  private static final String SDK_DIR = System.getProperty("test.all.android.sdks.home");

  private static File tempFolder;
  private static FileObject projdir;

  private static Project pp;

  @BeforeClass
  public static void setUpClass() throws Exception {
    MockServices.setServices();
    DalvikPlatformManager.getDefault().setSdkLocation(SDK_DIR);
    tempFolder = File.createTempFile("junit", "");
    tempFolder.delete();
    tempFolder.mkdir();

    FileObject scratch = FileUtil.toFileObject(tempFolder);
    FileObject sdkDirFo = FileUtil.toFileObject(new File(SDK_DIR));

    projdir = scratch.createFolder("Snake");
    FileUtilities.recursiveCopy(sdkDirFo.getFileObject("samples/android-8/Snake"), projdir);

    pp = ProjectManager.getDefault().findProject(projdir);
  }


  @AfterClass
  public static void delete() {
    FileUtilities.recursiveDelete(tempFolder);
  }

  @Test
  public void manifestGrammar() throws Exception {
    final AndroidProject proj = (AndroidProject) ProjectManager.getDefault().findProject(projdir);
    AndroidGeneralData data = AndroidGeneralData.fromProject(proj);
    data.setPlatform(DalvikPlatformManager.getDefault().findPlatformForTarget("android-8"));
    proj.update(data);
    FileObject manifest = projdir.getFileObject("AndroidManifest.xml");
    assertNotNull(manifest);

    GrammarEnvironment env = new GrammarEnvironment(Enumerations.empty(), new InputSource(), manifest);
    AndroidGrammarQueryProvider grammarQueryProvider = new AndroidGrammarQueryProvider();
    GrammarQuery query = grammarQueryProvider.getGrammar(env);
    assertTrue(query instanceof AndroidManifestGrammar);
    assertNotNull(((AndroidManifestGrammar) query).getStyleableModel());
  }

  @Test
  public void resourcesGrammar() throws Exception {
    final AndroidProject proj = (AndroidProject) ProjectManager.getDefault().findProject(projdir);
    AndroidGeneralData data = AndroidGeneralData.fromProject(proj);
    data.setPlatform(DalvikPlatformManager.getDefault().findPlatformForTarget("android-8"));
    proj.update(data);
    FileObject strings = projdir.getFileObject("res/values/strings.xml");
    assertNotNull(strings);

    GrammarEnvironment env = new GrammarEnvironment(Enumerations.empty(), new InputSource(), strings);
    AndroidGrammarQueryProvider grammarQueryProvider = new AndroidGrammarQueryProvider();
    GrammarQuery query = grammarQueryProvider.getGrammar(env);
    assertTrue(query instanceof ResourcesGrammar);
  }

}
