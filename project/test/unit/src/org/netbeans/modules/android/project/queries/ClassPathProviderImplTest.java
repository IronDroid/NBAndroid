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
package org.netbeans.modules.android.project.queries;

import org.netbeans.modules.android.project.TestUtils;
import java.util.List;
import java.net.URL;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.android.core.sdk.DalvikPlatformManager;
import org.netbeans.modules.android.core.sdk.Utils;
import org.netbeans.modules.android.project.AndroidProject;
import org.netbeans.modules.android.project.FileUtilities;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import static org.junit.Assert.*;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.util.EditableProperties;
import org.openide.util.Mutex;

/**
 *
 * @author radim, Baldur
 */
public class ClassPathProviderImplTest {

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

  @Test
  public void testNestedLibrariesClassPathProvider() throws IOException {
    for (Utils.TestPlatform p : Utils.purePlatforms()) {
      // create new 
      tempFolder = File.createTempFile("junit", "");
      tempFolder.delete();
      tempFolder.mkdirs();

      // file object for the temp dir
      FileObject root = FileUtil.toFileObject(tempFolder);

      FileObject foProject = TestUtils.createProject(p, root, "TestProject", false, "TestLibrary");
      FileObject foFirstLib = TestUtils.createProject(p, root, "TestLibrary", true, "NestedLibrary");
      FileObject foNestedLib = TestUtils.createProject(p, root, "NestedLibrary", true);

      // create the project
      AndroidProject project = (AndroidProject)ProjectManager.getDefault().findProject(foProject);
      ClassPathProvider cpp = project.getLookup().lookup(ClassPathProvider.class);
      assertNotNull(cpp);

      // expected paths
      FileObject foProjectSrc = foProject.getFileObject("src");
      FileObject foFirstLibSrc = foFirstLib.getFileObject("src");
      FileObject foNestedLibSrc = foNestedLib.getFileObject("src");

      // get the classpath
      ClassPath classpath = cpp.findClassPath(foProjectSrc, ClassPath.SOURCE);
      assertNotNull("classpath for path " + foProjectSrc.getPath() + " not found.", classpath);

      boolean foundProjectPath = false;
      boolean foundFirstLibPath = false;
      boolean foundNestedLibPath = false;

      for(FileObject classPathRoot : classpath.getRoots()) {
        if (classPathRoot.equals(foProjectSrc)) {
          foundProjectPath = true;
        }

        if (classPathRoot.equals(foFirstLibSrc)) {
          foundFirstLibPath = true;
        }

        if (classPathRoot.equals(foNestedLibSrc)) {
          foundNestedLibPath = true;
        }
      }

      assertTrue("missing project path in classpath " + classpath, foundProjectPath);
      assertFalse("missing first library path in classpath " + classpath, foundFirstLibPath);
      assertFalse("missing nested library path in classpath " + classpath, foundNestedLibPath);

      ClassPath compileClasspath = cpp.findClassPath(foProjectSrc, ClassPath.COMPILE);
      assertNotNull("classpath for path " + foProjectSrc.getPath() + " not found.", classpath);
      List<String> compileCpEntries = Lists.newArrayList(
          Splitter.on(':').split(compileClasspath.toString(ClassPath.PathConversionMode.PRINT)));
      assertFalse("Classpath " + classpath + " has sources", compileCpEntries.contains(foProjectSrc.getPath()));
      assertTrue("Classpath " + classpath + " has lib sources",
          compileCpEntries.contains(foFirstLib.getFileObject("bin/classes.jar").getPath()));
      assertTrue("Classpath " + classpath + " has lib sources",
          compileCpEntries.contains(foNestedLib.getFileObject("bin/classes.jar").getPath()));

      // clean up
      FileUtilities.recursiveDelete(tempFolder);
    }
  }

  @Test
  public void testFindClassPathWithLib() throws IOException {

    for (Utils.TestPlatform p : Utils.purePlatforms()) {
      tempFolder = File.createTempFile("junit", "");
      tempFolder.delete();
      tempFolder.mkdirs();

      // file object for the temp dir
      FileObject root = FileUtil.toFileObject(tempFolder);

      FileObject foProject = TestUtils.createProject(p, root, "TestProject", false);

      // create the project
      AndroidProject project = (AndroidProject)ProjectManager.getDefault().findProject(foProject);
      ClassPathProvider cpp = project.getLookup().lookup(ClassPathProvider.class);
      assertNotNull(cpp);

      // expected paths
      FileObject foProjectSrc   = foProject.getFileObject("src");

      // library in main project
      FileObject libs = foProject.getFileObject("libs");
      FileObject myLib = libs.createData("myLib.jar");

      // get the classpath
      ClassPath classpath = cpp.findClassPath(foProjectSrc, ClassPath.SOURCE);
      assertNotNull("classpath for path " + foProjectSrc.getPath() + " not found.", classpath);

      FileObject androidJar = FileUtil.toFileObject(
          new File(SDK_DIR, p.getPlatformDir() + "/android.jar"));

      ClassPath result = cpp.findClassPath(foProjectSrc, ClassPath.BOOT);
      assertNotNull(result);
      boolean found = false;
      for (ClassPath.Entry entry : result.entries()) {
        FileObject jarFile = FileUtil.getArchiveFile(entry.getRoot());
        if (androidJar.equals(jarFile)) {
          found = true;
          break;
        }
      }
      assertTrue("Classpath " + result + " has android.jar", found);

      result = cpp.findClassPath(foProjectSrc, ClassPath.COMPILE);
      assertNotNull(result);
      found = false;
      URL libURL = FileUtil.getArchiveRoot(myLib.getURL());
      for (ClassPath.Entry entry : result.entries()) {
        if (libURL.equals(entry.getURL())) {
          found = true;
          break;
        }
      }
      assertTrue("Classpath " + result + " has myLib.jar", found);
    }
  }

  @Test
  public void testFindClassPathWithLibraryProjectContaingJARLib() throws IOException {

    for (Utils.TestPlatform p : Utils.purePlatforms()) {
      tempFolder = File.createTempFile("junit", "");
      tempFolder.delete();
      tempFolder.mkdirs();

      // file object for the temp dir
      FileObject root = FileUtil.toFileObject(tempFolder);

      FileObject foProject = TestUtils.createProject(p, root, "TestProject", false, "TestLibrary");
      FileObject foFirstLib = TestUtils.createProject(p, root, "TestLibrary", true);
      // library in lib-project
      FileObject libs = foFirstLib.getFileObject("libs");
      FileObject myLib = libs.createData("myLib.jar");

      // create the project
      AndroidProject project = (AndroidProject)ProjectManager.getDefault().findProject(foProject);
      ClassPathProvider cpp = project.getLookup().lookup(ClassPathProvider.class);
      assertNotNull(cpp);

      // expected paths
      FileObject foProjectSrc = foProject.getFileObject("src");
      FileObject foFirstLibSrc = foFirstLib.getFileObject("src"); // no longer used
      FileObject foFirstLibJar = foFirstLib.getFileObject("bin/classes.jar"); // no longer used

      // get the classpath
      ClassPath classpath = cpp.findClassPath(foProjectSrc, ClassPath.SOURCE);
      assertNotNull("classpath for path " + foProjectSrc.getPath() + " not found.", classpath);

      List<String> cpEntries = Lists.newArrayList(
          Splitter.on(':').split(classpath.toString(ClassPath.PathConversionMode.PRINT)));
      assertTrue("Classpath " + classpath + " has sources", cpEntries.contains(foProjectSrc.getPath()));
      assertFalse("Classpath " + classpath + " has not lib sources", cpEntries.contains(foFirstLibSrc.getPath()));

      ClassPath compileClasspath = cpp.findClassPath(foProjectSrc, ClassPath.COMPILE);
      assertNotNull("classpath for path " + foProjectSrc.getPath() + " not found.", compileClasspath);
      List<String> compileCpEntries = Lists.newArrayList(
          Splitter.on(':').split(compileClasspath.toString(ClassPath.PathConversionMode.PRINT)));
      assertFalse("Classpath " + classpath + " has sources", compileCpEntries.contains(foProjectSrc.getPath()));
      assertTrue("Classpath " + classpath + " has lib sources", compileCpEntries.contains(foFirstLibJar.getPath()));
      
      boolean found = false;
      URL libURL = FileUtil.getArchiveRoot(myLib.getURL());
      for (ClassPath.Entry entry : compileClasspath.entries()) {
        if (libURL.equals(entry.getURL())) {
          found = true;
          break;
        }
      }
      assertTrue("Classpath " + compileClasspath + " has myLib.jar", found);
    }
  }

  @Test
  public void testFindClassPathWithLibraryProject() throws IOException {

    for (Utils.TestPlatform p : Utils.purePlatforms()) {
      tempFolder = File.createTempFile("junit", "");
      tempFolder.delete();
      tempFolder.mkdirs();

      // file object for the temp dir
      FileObject root = FileUtil.toFileObject(tempFolder);

      FileObject foProject = TestUtils.createProject(p, root, "TestProject", false, "TestLibrary");
      FileObject foFirstLib = TestUtils.createProject(p, root, "TestLibrary", true);

      // create the project
      AndroidProject project = (AndroidProject)ProjectManager.getDefault().findProject(foProject);
      ClassPathProvider cpp = project.getLookup().lookup(ClassPathProvider.class);
      assertNotNull(cpp);

      // expected paths
      FileObject foProjectSrc = foProject.getFileObject("src");
      FileObject foFirstLibSrc = foFirstLib.getFileObject("src"); // no longer used
      FileObject foFirstLibJar = foFirstLib.getFileObject("bin/classes.jar"); // no longer used

      // get the classpath
      ClassPath classpath = cpp.findClassPath(foProjectSrc, ClassPath.SOURCE);
      assertNotNull("classpath for path " + foProjectSrc.getPath() + " not found.", classpath);

      List<String> cpEntries = Lists.newArrayList(
          Splitter.on(':').split(classpath.toString(ClassPath.PathConversionMode.PRINT)));
      assertTrue("Classpath " + classpath + " has sources", cpEntries.contains(foProjectSrc.getPath()));
      assertFalse("Classpath " + classpath + " has not lib sources", cpEntries.contains(foFirstLibSrc.getPath()));

      ClassPath compileClasspath = cpp.findClassPath(foProjectSrc, ClassPath.COMPILE);
      assertNotNull("classpath for path " + foProjectSrc.getPath() + " not found.", compileClasspath);
      List<String> compileCpEntries = Lists.newArrayList(
          Splitter.on(':').split(compileClasspath.toString(ClassPath.PathConversionMode.PRINT)));
      assertFalse("Classpath " + classpath + " has sources", compileCpEntries.contains(foProjectSrc.getPath()));
      assertTrue("Classpath " + classpath + " has lib sources", compileCpEntries.contains(foFirstLibJar.getPath()));
    }
  }

  @Test
  public void testTestProjectWithLibsClassPath() throws Exception {

    for (Utils.TestPlatform p : Utils.purePlatforms()) {
      tempFolder = File.createTempFile("junit", "");
      tempFolder.delete();
      tempFolder.mkdirs();

      // file object for the temp dir
      FileObject root = FileUtil.toFileObject(tempFolder);

      FileObject foMainProject = TestUtils.createProject(p, root, "MainProject", false);
      File fileMainProject = FileUtil.toFile(foMainProject);
      FileObject foTestProject = TestUtils.createProject(p, root, "TestProject", fileMainProject.getAbsolutePath(), false);

      // create the project
      AndroidProject testProject = (AndroidProject)ProjectManager.getDefault().findProject(foTestProject);
      ClassPathProvider cpp = testProject.getLookup().lookup(ClassPathProvider.class);
      assertNotNull(cpp);

      // expected paths
      FileObject foTestProjectSrc   = foTestProject.getFileObject("src");

      // library in main project
      FileObject mainLibs = foMainProject.getFileObject("libs");
      FileObject mainLib = mainLibs.createData("mainLib.jar");
      
      // library in test project
      FileObject testLibs = foTestProject.getFileObject("libs");
      FileObject testLib = testLibs.createData("testLib.jar");

      // get the classpath
      ClassPath classpath = cpp.findClassPath(foTestProjectSrc, ClassPath.SOURCE);
      assertNotNull("classpath for path " + foTestProjectSrc.getPath() + " not found.", classpath);

      FileObject androidJar = FileUtil.toFileObject(
          new File(SDK_DIR, p.getPlatformDir() + "/android.jar"));

      ClassPath result = cpp.findClassPath(foTestProjectSrc, ClassPath.BOOT);
      assertNotNull(result);
      boolean found = false;
      for (ClassPath.Entry entry : result.entries()) {
        FileObject jarFile = FileUtil.getArchiveFile(entry.getRoot());
        if (androidJar.equals(jarFile)) {
          found = true;
          break;
        }
      }
      assertTrue("Classpath " + result + " has android.jar", found);

      assertClassPathContain(cpp.findClassPath(foTestProjectSrc, ClassPath.COMPILE),
                             testLib.getPath(),
                             mainLib.getPath(),
                             new File(fileMainProject, "bin/classes").getAbsolutePath());

      //verify current/libs file listener:
      testLibs.delete();
      
      assertClassPathDoesNotContain(cpp.findClassPath(foTestProjectSrc, ClassPath.COMPILE),
                                    testLib.getPath());
      assertClassPathContain(cpp.findClassPath(foTestProjectSrc, ClassPath.COMPILE),
                             mainLib.getPath(),
                             new File(fileMainProject, "bin/classes").getAbsolutePath());
      
      // library in test project
      testLibs = FileUtil.createFolder(foTestProject, "libs");
      testLib = testLibs.createData("testLib.jar");
      
      assertClassPathContain(cpp.findClassPath(foTestProjectSrc, ClassPath.COMPILE),
                             testLib.getPath(),
                             mainLib.getPath(),
                             new File(fileMainProject, "bin/classes").getAbsolutePath());
      
      //verify main/libs file listener:
      mainLib.delete();
      
      assertClassPathDoesNotContain(cpp.findClassPath(foTestProjectSrc, ClassPath.COMPILE),
                                    mainLib.getPath());
      assertClassPathContain(cpp.findClassPath(foTestProjectSrc, ClassPath.COMPILE),
                             testLib.getPath(),
                             new File(fileMainProject, "bin/classes").getAbsolutePath());
      
      // library in main project
      mainLib = mainLibs.createData("mainLib.jar");
      
      assertClassPathContain(cpp.findClassPath(foTestProjectSrc, ClassPath.COMPILE),
                             testLib.getPath(),
                             mainLib.getPath(),
                             new File(fileMainProject, "bin/classes").getAbsolutePath());
      
      FileObject nueTestLib = FileUtil.createData(root, "libs/c.jar");
    
      setJarLibsDirProperty(testProject, "../libs");
      
      assertClassPathContain(cpp.findClassPath(foTestProjectSrc, ClassPath.COMPILE),
                             nueTestLib.getPath(),
                             mainLib.getPath(),
                             new File(fileMainProject, "bin/classes").getAbsolutePath());
      
      FileObject nueTestLib2 = FileUtil.createData(root, "libs2/c.jar");
      
      setJarLibsDirProperty(testProject, FileUtil.toFile(nueTestLib2.getParent()).getAbsolutePath());
      
      assertClassPathContain(cpp.findClassPath(foTestProjectSrc, ClassPath.COMPILE),
                             nueTestLib2.getPath(),
                             mainLib.getPath(),
                             new File(fileMainProject, "bin/classes").getAbsolutePath());
      
    }
  }

  private void setJarLibsDirProperty(AndroidProject testProject, String reference) throws Exception {
    EditableProperties ep = new EditableProperties(false);
    FileObject properties = testProject.getProjectDirectory().getFileObject("project.properties");
    final CountDownLatch cdl = new CountDownLatch(1);
    
    testProject.evaluator().addPropertyChangeListener(new PropertyChangeListener() {
      @Override public void propertyChange(PropertyChangeEvent evt) {
        cdl.countDown();
      }
    });
    
    assertNotNull(properties);
    
    InputStream in = properties.getInputStream();
    
    ep.load(in);
    in.close();
    
    ep.put("jar.libs.dir", reference);
    
    OutputStream out = properties.getOutputStream();
    
    ep.store(out);
    out.close();
    
    assertTrue(cdl.await(2, TimeUnit.SECONDS));
  }
  
  private static void assertClassPathContain(ClassPath toTest, String... shouldContain) {
      assertNotNull(toTest);
      
      List<String> cpEntries = Lists.newArrayList(
          Splitter.on(':').split(toTest.toString(ClassPath.PathConversionMode.PRINT)));
      
      for (String path : shouldContain) {
        assertTrue("Classpath " + toTest + " should contain: " + path, cpEntries.contains(path));
      }
  }
  
  private static void assertClassPathDoesNotContain(ClassPath toTest, String... shouldContain) {
      assertNotNull(toTest);
      
      List<String> cpEntries = Lists.newArrayList(
          Splitter.on(':').split(toTest.toString(ClassPath.PathConversionMode.PRINT)));
      
      for (String path : shouldContain) {
        assertFalse("Classpath " + toTest + " should not contain: " + path, cpEntries.contains(path));
      }
  }
}
