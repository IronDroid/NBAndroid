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

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.android.core.sdk.DalvikPlatformManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Tests for AndroidActionProvider
 */
public class AndroidInfoImplTest {

  private static final String SDK_DIR = System.getProperty("test.all.android.sdks.home");

  private static File tempFolder;
  private static FileObject projdir;
  private static FileObject testprojdir;
  private static FileObject mainprojdir;
  private static FileObject libprojdir;

  private static Project srcPrj;
  private static Project testPrj;
  private static Project mainPrj;
  private static Project libPrj;
  private static FileObject someSource1;
  private static FileObject someTest1;

  @Before
  public void setUp() throws Exception {
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

    mainprojdir = scratch.createFolder("TicTacToeMain");
    FileUtilities.recursiveCopy(sdkDirFo.getFileObject("samples/android-8/TicTacToeMain"), mainprojdir);
    libprojdir = scratch.createFolder("TicTacToeLib");
    FileUtilities.recursiveCopy(sdkDirFo.getFileObject("samples/android-8/TicTacToeLib"), libprojdir);

    mainPrj = ProjectManager.getDefault().findProject(mainprojdir);
    libPrj = ProjectManager.getDefault().findProject(libprojdir);
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
    AndroidProjectInfo aInfo = proj.info();
    assertFalse(aInfo.isTest());

    // This project is not updated yet so it hard to tell if it is test.
    // update it first and check the result
    AndroidProject tProj = (AndroidProject) ProjectManager.getDefault().findProject(testprojdir);
    data = AndroidGeneralData.fromProject(tProj);
    data.setPlatform(DalvikPlatformManager.getDefault().findPlatformForTarget("android-8"));
    data.setMainProjectDirPath("../Spinner");
    tProj.update(data);
    aInfo = tProj.info();
    assertTrue(tProj + " is test project", aInfo.isTest());
  }

  @Test
  public void isLibrary() throws Exception {
    AndroidProject proj = (AndroidProject) ProjectManager.getDefault().findProject(libprojdir);
    AndroidGeneralData data = AndroidGeneralData.fromProject(proj);
    data.setPlatform(DalvikPlatformManager.getDefault().findPlatformForTarget("android-8"));
    proj.update(data);
    AndroidProjectInfo aInfo = proj.info();
    assertTrue(aInfo.isLibrary());
    assertFalse(aInfo.isTest());

    // Turn off library flag.
    data = AndroidGeneralData.fromProject(proj);
    data.setPlatform(DalvikPlatformManager.getDefault().findPlatformForTarget("android-8"));
    data.setLibrary(false);
    proj.update(data);
    aInfo = proj.info();
    assertFalse(proj + " is not library", aInfo.isLibrary());

    // Turn on library flag.
    data = AndroidGeneralData.fromProject(proj);
    data.setPlatform(DalvikPlatformManager.getDefault().findPlatformForTarget("android-8"));
    data.setLibrary(true);
    proj.update(data);
    aInfo = proj.info();
    assertTrue(proj + " is not library", aInfo.isLibrary());
  }

  @Test
  public void newTestProject() throws Exception {
    AndroidGeneralData data = new AndroidGeneralData();
    File prjDir = new File(tempFolder, "project");
    prjDir.mkdir();
    data.setProjectDirPath(prjDir.getAbsolutePath());
    data.setProjectName("project");
    data.setPlatform(DalvikPlatformManager.getDefault().findPlatformForTarget("android-7"));
    String pkgName = "com.foo.bar";
    String activity = "MainActivity";

    AndroidProjectUtil.create(data, pkgName, activity);
    Project prj = ProjectManager.getDefault().findProject(FileUtil.toFileObject(prjDir));
    assertNotNull(prj);
    AndroidProjectInfo aPrj = prj.getLookup().lookup(AndroidProjectInfo.class);
    assertNotNull(aPrj);
    assertFalse(aPrj.isTest());

    File testPrjDir = new File(tempFolder, "projectTest");
    testPrjDir.mkdir();
    data.setProjectDirPath(testPrjDir.getAbsolutePath());
    data.setMainProjectDirPath("../project");
    AndroidProjectUtil.create(data, pkgName, activity);

    Project tPrj = ProjectManager.getDefault().findProject(FileUtil.toFileObject(testPrjDir));
    assertNotNull(tPrj);
    aPrj = tPrj.getLookup().lookup(AndroidProjectInfo.class);
    assertNotNull(aPrj);
    assertTrue(aPrj.isTest());
  }

  // test that old projects can be updated to tools r14+
  @Test
  public void upgradePreR14Tools() throws Exception {
    FileObject scratch = FileUtil.toFileObject(tempFolder);
    FileObject sdkDirFo = FileUtil.toFileObject(new File(SDK_DIR));

    FileObject snakePrjDir = scratch.createFolder("Snake");
    FileUtilities.recursiveCopy(sdkDirFo.getFileObject("samples/android-8/Snake"), snakePrjDir);

    FileUtil.copy(this.getClass().getResourceAsStream("resources/build.xml"),
        snakePrjDir.createData("build.xml").getOutputStream());
    FileUtil.copy(this.getClass().getResourceAsStream("resources/build.properties"),
        snakePrjDir.createData("build.properties").getOutputStream());
    FileUtil.copy(this.getClass().getResourceAsStream("resources/default.properties"),
        snakePrjDir.createData("default.properties").getOutputStream());
    FileObject localProps = snakePrjDir.createData("local.properties");
    FileUtil.copy(this.getClass().getResourceAsStream("resources/local.properties"),
        localProps.getOutputStream());
    Files.append("sdk.dir=" + SDK_DIR, FileUtil.toFile(localProps), Charsets.UTF_8);
    FileUtil.copy(this.getClass().getResourceAsStream("resources/proguard.cfg"),
        snakePrjDir.createData("proguard.cfg").getOutputStream());

    AndroidProject proj = (AndroidProject) ProjectManager.getDefault().findProject(snakePrjDir);
    AndroidGeneralData data = AndroidGeneralData.fromProject(proj);
    AndroidProjectInfo aInfo = proj.info();
    assertTrue(aInfo.isNeedsFix());
    proj.update(data);

    data = AndroidGeneralData.fromProject(proj);
    aInfo = proj.info();
    assertFalse(aInfo.isNeedsFix());
    assertNotNull(data.getPlatform());
    
    assertFalse(aInfo.isTest());
    assertFalse(aInfo.isLibrary());
  }
}
