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

import com.android.sdklib.internal.avd.AvdInfo;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.android.core.sdk.DalvikPlatformManager;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.android.project.AvdSelector.LaunchData;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.lookup.Lookups;
import static org.junit.Assert.*;

/**
 * Tests for AndroidActionProvider
 */
public class AndroidActionProviderTest {

  private static final String SDK_DIR = System.getProperty("test.all.android.sdks.home");

  private static File tempFolder;
  private static FileObject projdir;
  private static FileObject libdir;

  private static Project pp;
  private static Project libp;
  private static AndroidActionProvider actionProvider;
  private static AndroidActionProvider libActionProvider;
  private static DataObject libSource1;
  private static DataObject someSource1;
  private static FileObject spinnerprojdir;
  private static FileObject spinnertestprojdir;
  private static Project spinnerTestPrj;
  private static FileObject spinnerTest1;
  private final LaunchData dummyLaunch = new LaunchData(new AvdInfo(
      "testingAVD", /*iniPath*/null, "path", "targetHash", null, null, null, AvdInfo.AvdStatus.OK),
      null);

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
    libdir = scratch.createFolder("TicTacToeLib");
    FileUtilities.recursiveCopy(sdkDirFo.getFileObject("samples/android-8/TicTacToeLib"), libdir);

    pp = ProjectManager.getDefault().findProject(projdir);
    libp = ProjectManager.getDefault().findProject(libdir);

    actionProvider = pp.getLookup().lookup(AndroidActionProvider.class);
    libActionProvider = libp.getLookup().lookup(AndroidActionProvider.class);
    someSource1 = DataObject.find(projdir.getFileObject(
        "src/com/example/android/tictactoe/MainActivity.java"));
    libSource1 = DataObject.find(libdir.getFileObject(
        "src/com/example/android/tictactoe/library/GameActivity.java"));

    spinnerprojdir = scratch.createFolder("Spinner");
    FileUtilities.recursiveCopy(sdkDirFo.getFileObject("samples/android-8/Spinner"), spinnerprojdir);
    spinnertestprojdir = scratch.createFolder("SpinnerTest");
    FileUtilities.recursiveCopy(sdkDirFo.getFileObject("samples/android-8/SpinnerTest"), spinnertestprojdir);

    spinnerTestPrj = ProjectManager.getDefault().findProject(spinnertestprojdir);
    spinnerTest1 = spinnertestprojdir.getFileObject("src/com/android/example/spinner/test/SpinnerActivityTest.java");
  }


  @AfterClass
  public static void delete() {
    FileUtilities.recursiveDelete(tempFolder);
  }

//  @Test
  public void getCompileSingle() throws Exception {
    Properties p;
    Lookup context;
    String[] targets;

    p = new Properties();
    context = Lookups.fixed();
    targets = actionProvider.getTargetNames(ActionProvider.COMMAND_COMPILE_SINGLE, context, p);
    assertNotNull("Must found some targets for COMMAND_COMPILE_SINGLE", targets);
    assertEquals("COMMAND_COMPILE_SINGLE is not supported", 0, targets.length);
  }

  // TODO(radim): may need proper UnitTestForSourceQ
  // @Test
  public void getTestSingle() throws Exception {
    Properties p;
    Lookup context;
    String[] targets;

    // test COMMAND_TEST_SINGLE

    p = new Properties();
    context = Lookups.fixed(someSource1);
    targets = actionProvider.getTargetNames(ActionProvider.COMMAND_TEST_SINGLE, context, p);
    assertNotNull("Must found some targets for COMMAND_TEST_SINGLE", targets);
    assertEquals("There must be one target for COMMAND_TEST_SINGLE", 1, targets.length);
    assertEquals("Unexpected target name", "test-single", targets[0]);
    assertEquals("There must be one target parameter", 2, p.keySet().size());
    assertEquals("There must be be target parameter", "foo/BarTest.java", p.getProperty("javac.includes"));
    assertEquals("There must be be target parameter", "foo/BarTest.java", p.getProperty("test.includes"));
    p = new Properties();
//    context = Lookups.fixed(someSource1,someSource2);
    targets = actionProvider.getTargetNames(ActionProvider.COMMAND_TEST_SINGLE, context, p);
    assertNotNull("Must found some targets for COMMAND_TEST_SINGLE", targets);
    assertEquals("There must be one target for COMMAND_TEST_SINGLE", 1, targets.length);
    assertEquals("Unexpected target name", "test-single", targets[0]);
    assertEquals("There must be one target parameter", 2, p.keySet().size());
    assertEquals("There must be be target parameter", "foo/BarTest.java,foo/MainTest.java", p.getProperty("javac.includes"));
    assertEquals("There must be be target parameter", "foo/BarTest.java,foo/MainTest.java", p.getProperty("test.includes"));

  }

  @Test
  public void test() throws Exception {
    Properties p;
    Lookup context;
    String[] targets;

    p = new Properties();
    context = Lookups.fixed(dummyLaunch);

    targets = spinnerTestPrj.getLookup().lookup(AndroidActionProvider.class)
        .getTargetNames(ActionProvider.COMMAND_TEST, context, p);
    assertNotNull("Must found some targets for COMMAND_TEST", targets);
    assertEquals("There must be one target for COMMAND_TEST", 1, targets.length);
    assertEquals("Unexpected target name", "debug", targets[0]);
    assertEquals("There must be no target parameter", Collections.emptyMap(), p);
  }

  @Test
  public void run() throws Exception {
    Properties p;
    Lookup context;
    String[] targets;

    p = new Properties();
    context = Lookups.fixed(dummyLaunch);
    targets = actionProvider.getTargetNames(ActionProvider.COMMAND_RUN, context, p);
    assertNotNull("Must found some targets for COMMAND_RUN", targets);
    assertEquals("There must be one target for COMMAND_RUN", 1, targets.length);
    assertEquals("Unexpected target name", "debug", targets[0]);
    assertEquals("There must be no target parameter", Collections.emptyMap(), p);
  }

  @Test
  public void build() throws Exception {
    Properties p;
    Lookup context;
    String[] targets;

    p = new Properties();
    context = Lookups.fixed(dummyLaunch);
    targets = actionProvider.getTargetNames(ActionProvider.COMMAND_BUILD, context, p);
    assertNotNull("Must found some targets for COMMAND_BUILD", targets);
    assertEquals("There must be one target for COMMAND_BUILD", 1, targets.length);
    assertEquals("Unexpected target name", "debug", targets[0]);
    assertEquals("There must be no target parameter", Collections.emptyMap(), p);

    targets = libActionProvider.getTargetNames(ActionProvider.COMMAND_BUILD, context, p);
    assertNotNull("Must found some targets for COMMAND_BUILD", targets);
    assertEquals("There must be one target for COMMAND_BUILD", 1, targets.length);
    assertEquals("Unexpected target name", "debug", targets[0]);
    assertEquals("There must be no target parameter", Collections.emptyMap(), p);
  }

  @Test
  public void debug() throws Exception {
    Properties p;
    Lookup context;
    String[] targets;

    // TODO(radim): same as run + debugging

    p = new Properties();
    context = Lookups.fixed(dummyLaunch);
    targets = actionProvider.getTargetNames(ActionProvider.COMMAND_DEBUG, context, p);
    assertNotNull("Must found some targets for COMMAND_DEBUG", targets);
    assertEquals("There must be one target for COMMAND_DEBUG", 1, targets.length);
    assertEquals("Unexpected target name", "debug", targets[0]);
  }

//  @Test
  public void configs() throws Exception {
    fail("need to implement support for debug/release and deployment params");
        final FileObject projdirFO = null; // scratch.createFolder("projectwithconfigs");
        // TODO we have a project created by setUp
//        AndroidProjectGenerator.createProject(
//            FileUtil.toFile(projdirFO), "projectwithconfigs", DEFAULT_PLATFORM_ID, "proj", "foo.Main", "manifest.mf");
        final AndroidProject proj = (AndroidProject) ProjectManager.getDefault().findProject(projdirFO);
        final ProjectConfigurationProvider<?> pcp = proj.getLookup().lookup(ProjectConfigurationProvider.class);
        ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
            @SuppressWarnings("unchecked") // due to ProjectConfiguration type
            public Void run() throws Exception {
                Properties props = new Properties();
                props.setProperty("main.class", "foo.Bar");
                props.setProperty("$target.build", "");
                props.setProperty("$target.run", "runtarget");
                props.setProperty("$target.debug", "debugtarget1 debugtarget2");
                write(props, projdirFO, "nbproject/configs/test.properties");
                props = new Properties();
                write(props, projdirFO, "nbproject/private/configs/test.properties");
                props = new Properties();
                props.setProperty("config", "test");
                write(props, projdirFO, "nbproject/private/config.properties");
                ProjectManager.getDefault().saveProject(proj);                
                return null;
            }
        });

        ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void> () {
            public Void run () throws Exception {
                setConfig1(pcp);
                return null;
            }
            <T extends ProjectConfiguration> void setConfig1(ProjectConfigurationProvider<T> pcp) throws Exception {
                pcp.setActiveConfiguration(new ArrayList<T>(pcp.getConfigurations()).get(1));
            }
        });        

        AndroidActionProvider ap = proj.getLookup().lookup(AndroidActionProvider.class);
        PropertyEvaluator eval = proj.evaluator();
        String config = eval.getProperty("config");
        assertEquals("Name of active config from Evaluator is test", "test", config);
        FileObject src = projdirFO.getFileObject("src");
        FileObject pkg = src.createFolder("foo");
        FileObject file = pkg.createData("Bar.java");
        DataObject srcDO = DataObject.find(file);
        Lookup context = Lookups.fixed( srcDO, dummyLaunch );
        // test of targets defined in config
        String[] targets = ap.getTargetNames(ActionProvider.COMMAND_DEBUG, context, new Properties());
        assertEquals("There must be two Debug targets in test config", 2, targets.length);
        assertEquals("First Debug target name is debugtarget1", "debugtarget1", targets[0]);
        assertEquals("Second Debug target name is debugtarget2", "debugtarget2", targets[1]);
        targets = ap.getTargetNames(ActionProvider.COMMAND_BUILD, context, new Properties());
        assertEquals("There must be 1 Build target in test config", 1, targets.length);
        // target is not in fact from the config, config contains empty string
        assertEquals("Build target name is jar", "jar", targets[0]);
        targets = ap.getTargetNames(ActionProvider.COMMAND_RUN, context, new Properties());
        assertEquals("There must be 1 Run target in test config", 1, targets.length);
        assertEquals("Run target name is runtarget", "runtarget", targets[0]);
        // test of targets not in config
        targets = ap.getTargetNames(ActionProvider.COMMAND_CLEAN, context, new Properties());
        assertEquals("There must be 1 Clean target", 1, targets.length);
        assertEquals("Clean target name is runtarget", "clean", targets[0]);
    }

    private void write(Properties p, FileObject d, String path) throws IOException {
        FileObject f = FileUtil.createData(d, path);
        OutputStream os = f.getOutputStream();
        p.store(os, null);
        os.close();
    }

}
