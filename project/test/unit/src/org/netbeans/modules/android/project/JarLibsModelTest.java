/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.netbeans.modules.android.project;

import java.util.List;
import com.android.sdklib.SdkManager;
import com.android.sdklib.internal.project.ProjectCreator;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.android.core.sdk.DalvikPlatform;
import org.netbeans.modules.android.core.sdk.DalvikPlatformManager;
import org.netbeans.modules.android.core.sdk.SdkLogProvider;
import org.netbeans.modules.android.core.sdk.Utils;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import static org.junit.Assert.*;

/**
 *
 * @author radim, Baldur
 */
public class JarLibsModelTest {

  private static final String SDK_DIR = System.getProperty("test.all.android.sdks.home");

  private static DalvikPlatformManager platformManager;

  private static File tempFolder;



  @BeforeClass
  public static void setUpClass() throws Exception {
    platformManager = DalvikPlatformManager.getDefault();
    platformManager.setSdkLocation(SDK_DIR);
  }


  @After
  public void clear() {
    FileUtilities.recursiveDelete(tempFolder);
  }

  private static FileObject createProject(Utils.TestPlatform p, FileObject root, String name,
      boolean isLibrary, String ... usedLib) 
      throws IOException {
    // get platform manager & project creator for the current target
    DalvikPlatform platform = platformManager.findPlatformForTarget(p.getTarget());
    assertNotNull("missing platform for target " + p.getTarget(), platform);

    SdkManager sdkManager = platform.getSdkManager();
    assertNotNull("missing sdk manager for target " + p.getTarget(), sdkManager);

    ProjectCreator prjCreator = new ProjectCreator(
              sdkManager,
              sdkManager.getLocation(),
              ProjectCreator.OutputLevel.NORMAL,
              SdkLogProvider.createSdkLogger(true)
    );

    FileObject foProject   = root.createFolder(name);
    prjCreator.createProject(
              FileUtil.toFile(foProject).getAbsolutePath(),
              name,
              "com.android.test." + name,
              "MainActivity",
              platform.getAndroidTarget(),
              isLibrary,
              /*main project*/null
    );

    if (usedLib.length > 0) {

      // write project properties
      FileObject foProjectProps = foProject.getFileObject("default", "properties");
      Properties propProject = new Properties();
      InputStream in = foProjectProps.getInputStream();
      propProject.load(in);
      in.close();
      int i = 1;
      for (String lib : usedLib) {
        propProject.setProperty("android.library.reference." + String.valueOf(i), 
            ".." + File.separatorChar + lib);
      }
      OutputStream out = foProjectProps.getOutputStream();
      propProject.store(out, "");  
      out.close();
    }
    return foProject;
  }

  @Test
  public void testFindClassPathWithLib() throws IOException, InterruptedException {

    for (Utils.TestPlatform p : Utils.purePlatforms()) {
      tempFolder = File.createTempFile("junit", "");
      tempFolder.delete();
      tempFolder.mkdirs();

      // file object for the temp dir
      FileObject root = FileUtil.toFileObject(tempFolder);
      FileObject foProject = createProject(p, root, "TestProject", false);

      // create the project
      AndroidProject project = (AndroidProject)ProjectManager.getDefault().findProject(foProject);
      ClassPathProvider cpp = project.getLookup().lookup(ClassPathProvider.class);
      // expected paths
      FileObject foProjectSrc   = foProject.getFileObject("src");
      // library in main project
      FileObject libs = foProject.getFileObject("libs");
      FileObject myLib = libs.createData("myLib.jar");

      ClassPath classpath = cpp.findClassPath(foProjectSrc, ClassPath.COMPILE);
      assertNotNull(classpath);
      checkLib(classpath, true, myLib.getPath());

      FileObject libs2 = foProject.createFolder("libs2");
      FileObject myLib2 = libs2.createData("myLib.jar");
      JarLibsModel model = new JarLibsModel(project);
      model.setUseDefaultLocation(false);
      model.setLibsDir("libs2");
      assertTrue(model.apply(project));
      checkLib(classpath, false, myLib.getPath());
      checkLib(classpath, true, myLib2.getPath());

      model.setUseDefaultLocation(true);
      assertTrue(model.apply(project));
      checkLib(classpath, true, myLib.getPath());
      checkLib(classpath, false, myLib2.getPath());

    }
  }

  private void checkLib(ClassPath classpath, boolean contains, String path) throws InterruptedException {
      Thread.sleep(500L);
      List<String> cpEntries = Lists.newArrayList(
          Splitter.on(':').split(classpath.toString(ClassPath.PathConversionMode.PRINT)));
      System.err.println(cpEntries);
      assertEquals("Classpath " + classpath + " has sources " + path, contains, cpEntries.contains(path));
  }

}
