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
package org.netbeans.modules.android.core.sdk;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

import static org.junit.Assert.*;
import org.openide.filesystems.JarFileSystem;


public class DalvikPlatformTest {

  private static final String SDK_DIR = System.getProperty("test.all.android.sdks.home");

  private DalvikPlatformManager dpm;

  @Before
  public void setUp() {
    dpm = new DalvikPlatformManager();
  }

  /**
   * Test of findSdkManager method, of class SdkManagers.
   */
  @Test
  public void cleanPlatforms() throws Exception {
    dpm.setSdkLocation(SDK_DIR);
    for (Utils.TestPlatform p : Utils.purePlatforms()) {
      FileObject sdkDir = Utils.createSdkPlatform(p);

      DalvikPlatform platform = dpm.findPlatformForTarget(p.getTarget());
      assertNotNull(p.getPlatformDir(), platform);
      assertEquals(sdkDir, platform.getInstallFolder());
      assertEquals(sdkDir.getParent().getParent().getFileObject(p.getPlatformDir()), platform.getPlatformFolder());
      for (Tool t : Tool.values()) {
        assertNotNull("Tool " + t.getSystemName() + " in " + platform, platform.findTool(t.getSystemName()));
      }
      doTestClasspath(platform.getBootstrapLibraries(), "android.jar");
      doTestClasspathResource(platform.getBootstrapLibraries(), "android/app/Application.class");
      doTestClasspathResource(platform.getBootstrapLibraries(), "android/annotation/TargetApi.class");
      doTestSources(platform);
      doTestTools(platform);
      doTestJavadoc(platform, "android/app/Activity.html");
    }
  }

  @Test
  public void googleApiPlatforms() throws Exception {
    dpm.setSdkLocation(SDK_DIR);
    for (Utils.TestPlatform p : Utils.googlePlatforms()) {
      FileObject sdkDir = Utils.createSdkPlatform(p);

      DalvikPlatform platform = dpm.findPlatformForTarget(p.getTarget());
      assertNotNull(p.getPlatformDir(), platform);
      assertEquals(sdkDir, platform.getInstallFolder());
      assertEquals(sdkDir.getParent().getParent().getFileObject(p.getPlatformDir()), platform.getPlatformFolder());
      for (Tool t : Tool.values()) {
        assertNotNull("Tool " + t.getSystemName() + " in " + platform, platform.findTool(t.getSystemName()));
      }
      doTestClasspath(platform.getBootstrapLibraries(), "android.jar", "maps.jar");
      doTestClasspathResource(platform.getBootstrapLibraries(), "android/annotation/TargetApi.class");
      doTestSources(platform);
      // doTestClasspath(platform.getStandardLibraries());
      doTestTools(platform);
      doTestJavadoc(platform, "android/app/Activity.html");
      doTestJavadoc(platform, "com/google/android/maps/MapView.html");
    }
  }

  /** Tests bootclasspath */
  private void doTestClasspath(List<URL> cp, String ... jars) {
    assertTrue(cp != null && !cp.isEmpty());
    Set<String> jarNames = new HashSet<String>();
    Collections.addAll(jarNames, jars);
    for (URL cpEntry : cp) {
      if (FileUtil.getArchiveFile(cpEntry) != null) {
        jarNames.remove(URLMapper.findFileObject(FileUtil.getArchiveFile(cpEntry)).getNameExt());
      }
    }
    assertTrue("No missing libraries in " + jarNames, jarNames.isEmpty());
  }

  /** Tests bootclasspath */
  private void doTestClasspathResource(List<URL> cp, String classfileName) {
    assertTrue(cp != null && !cp.isEmpty());
    for (URL cpEntry : cp) {
      final URL archiveFile = FileUtil.getArchiveFile(cpEntry);
      if (archiveFile != null) {
        if (URLMapper.findFileObject(cpEntry).getFileObject(classfileName) != null) 
          return;
      }
    }
    fail("Cannot find " + classfileName + " in classpath " + cp);
  }

  private void doTestSources(DalvikPlatform platform) {
    int apiLevel = platform.getAndroidTarget().getVersion().getApiLevel();
    if (apiLevel >= 14) {
      FileObject javadocFo = platform.getSourceFolders().findResource("android/app/Activity.java");
      assertNotNull(javadocFo);
    }
  }
  
  private void doTestTools(DalvikPlatform platform) {
    for (Tool t : Util.sdkTools()) {
      FileObject toolFO = platform.findTool(t.getSystemName());
      assertNotNull(t.getSystemName() + " found", toolFO);
      assertTrue(t.getSystemName() + " valid", toolFO.isValid());
    }
  }

  private void doTestJavadoc(DalvikPlatform platform, String resource)
      throws MalformedURLException {
    List<URL> javadocs = platform.getJavadocFolders();
    assertNotNull(javadocs);
    boolean found = false;
    for (URL docRoot : javadocs) {
      FileObject documentationFO = URLMapper.findFileObject(new URL(docRoot, resource));
      if (documentationFO != null && documentationFO.isValid()) {
        found = true;
        break;
      }
    }
    assertTrue("Resource " + resource + " found in javadoc", found);
  }
}
