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

package org.netbeans.modules.android.project.queries;

import java.net.URL;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.android.project.*;
import java.io.File;
import org.junit.*;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.android.core.sdk.DalvikPlatformManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import static org.junit.Assert.*;

/**
 * Tests for AndroidActionProvider
 */
public class ClassPathProviderImpl2Test {

  private static final String SDK_DIR = System.getProperty("test.all.android.sdks.home");

  private static File tempFolder;
  private static FileObject projdir;
  private static FileObject testprojdir;

  private static Project srcPrj;
  private static Project testPrj;
  private static FileObject someSource1;
  private static FileObject someTest1;

  @Before
  public void setUpClass() throws Exception {
    MockServices.setServices();
    DalvikPlatformManager.getDefault().setSdkLocation(SDK_DIR);
    tempFolder = File.createTempFile("junit", "");
    tempFolder.delete();
    tempFolder.mkdir();

    FileObject scratch = FileUtil.toFileObject(tempFolder);
    FileObject sdkDirFo = FileUtil.toFileObject(new File(SDK_DIR));

    projdir = scratch.createFolder("Spinner");
    FileUtilities.recursiveCopy(sdkDirFo.getFileObject("samples/android-8/Spinner"), projdir);
    testprojdir = scratch.createFolder("SpinnerTest");
    FileUtilities.recursiveCopy(sdkDirFo.getFileObject("samples/android-8/SpinnerTest"), testprojdir);

    srcPrj = ProjectManager.getDefault().findProject(projdir);
    someSource1 = projdir.getFileObject("src/com/android/example/spinner/SpinnerActivity.java");
    testPrj = ProjectManager.getDefault().findProject(testprojdir);
    someTest1 = testprojdir.getFileObject("src/com/android/example/spinner/test/SpinnerActivityTest.java");
  }

  @After
  public void delete() {
    FileUtilities.recursiveDelete(tempFolder);
  }

  @Test
  public void isTest() throws Exception {
    AndroidProject proj = (AndroidProject) ProjectManager.getDefault().findProject(projdir);
    assertNotNull(someSource1);
    AndroidGeneralData data = AndroidGeneralData.fromProject(proj);
    data.setPlatform(DalvikPlatformManager.getDefault().findPlatformForTarget("android-8"));
    proj.update(data);

    // This project is not updated yet so it hard to tell if it is test.
    // update it first and check the result
    final AndroidProject tProj = (AndroidProject) ProjectManager.getDefault().findProject(testprojdir);
    final AndroidGeneralData data2 = AndroidGeneralData.fromProject(tProj);
    data2.setPlatform(DalvikPlatformManager.getDefault().findPlatformForTarget("android-8"));
    data2.setMainProjectDirPath("../Spinner");
    ProjectManager.mutex().readAccess(new Runnable() {
      @Override public void run() {
        tProj.update(data2);
      }
    });
    assertTrue(tProj.info().isTest());

    ClassPathProviderImpl instance = new ClassPathProviderImpl(tProj);
    ClassPath result = instance.findClassPath(someTest1, ClassPath.COMPILE);
    assertNotNull(result);
    URL mainPrjClassesURL = FileUtil.urlForArchiveOrDir(new File(FileUtil.toFile(projdir), "bin/classes"));
    boolean found = false;
    for (ClassPath.Entry entry : result.entries()) {
      System.err.println("compile cp entry " + entry.getURL());
      if (mainPrjClassesURL.equals(entry.getURL())) {
        found = true;
        break;
      }
    }
    assertTrue("Classpath " + result + " has bin/classes of " + testprojdir, found);
  }
}
