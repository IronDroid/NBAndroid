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

import org.netbeans.spi.project.support.ant.PropertyUtils;
import java.io.File;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.android.core.sdk.DalvikPlatformManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import static org.junit.Assert.*;

/**
 * Tests for AndroidActionProvider
 */
public class PropertiesHelperTest {

  private static final String SDK_DIR = System.getProperty("test.all.android.sdks.home");

  private static File tempFolder;
  private static FileObject projdir;

  private static Project pp;

  @BeforeClass
  public static void setUpClass() throws Exception {
    DalvikPlatformManager.getDefault().setSdkLocation(SDK_DIR);
    tempFolder = File.createTempFile("junit", "");
    tempFolder.delete();
    tempFolder.mkdir();

    FileObject scratch = FileUtil.toFileObject(tempFolder);
    FileObject sdkDirFo = FileUtil.toFileObject(new File(SDK_DIR));

    projdir = scratch.createFolder("TicTacToeMain");
    FileUtilities.recursiveCopy(sdkDirFo.getFileObject("samples/android-8/TicTacToeMain"), projdir);

    pp = ProjectManager.getDefault().findProject(projdir);
  }


  @AfterClass
  public static void delete() {
    FileUtilities.recursiveDelete(tempFolder);
  }

  @Test
  public void fixLocalProperties() {
    FileObject localProps = projdir.getFileObject("local.properties");
    assertNull("samples are shipped without local.properties", localProps);
    PropertiesHelper helper = new PropertiesHelper((AndroidProject) pp);
    helper.updateProperties(DalvikPlatformManager.getDefault());

    localProps = projdir.getFileObject("local.properties");
    assertNotNull("local.properties is created now", localProps);
    assertEquals(SDK_DIR, PropertyUtils.propertiesFilePropertyProvider(
            FileUtil.toFile(localProps)).getProperties().get("sdk.dir"));
  }

  @Test
  public void hasCommonProperties() {
    PropertiesHelper helper = new PropertiesHelper((AndroidProject) pp);
    assertEquals("TicTacToeMain", helper.evaluator().getProperty("project.name"));
    assertEquals("TicTacToeMain", helper.evaluator().getProperty("project.displayName"));

  }
}
