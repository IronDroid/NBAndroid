/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.android.maven;

import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.android.core.sdk.DalvikPlatform;
import org.netbeans.modules.android.core.sdk.DalvikPlatformManager;
import org.netbeans.modules.android.project.spi.DalvikPlatformResolver;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import static org.junit.Assert.*;

/**
 *
 * @author radim
 */
public class DalvikPlatformForMavenProviderTest {
  private static final Logger LOG = Logger.getLogger(DalvikPlatformForMavenProviderTest.class.getName());
  
  public DalvikPlatformForMavenProviderTest() {
  }

  private static final String SDK_DIR = System.getProperty("test.all.android.sdks.home");
  private static final String MAVEN_DIR = System.getProperty("test.all.maven.home");
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
    
//    InstalledFileLocator ifl = Lookup.getDefault().lookup(InstalledFileLocator.class);
//    File mvn = ifl.locate("maven/bin/mvn", null, false);
    File mvn = new File(MAVEN_DIR, "bin/mvn"); // TODO add .exe on Windows
    System.err.println("tmp " + tempFolder.getAbsolutePath());
    ProcessBuilder pb = new ProcessBuilder(Lists.newArrayList(
            mvn.getAbsolutePath(),
            "--batch-mode",
            "archetype:generate",
            "-DarchetypeArtifactId=android-quickstart",
            "-DarchetypeGroupId=de.akquinet.android.archetypes",
            "-DarchetypeVersion=1.0.8",
            "-DgroupId=your.company",
            "-DartifactId=my-android-application",
            "-Dplatform=9")).
        directory(tempFolder).redirectErrorStream(true);
    pb.environment().put("JAVA_HOME", System.getProperty("java.home"));
    final Process p = pb.start();
    Executors.newCachedThreadPool().submit(new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        ByteStreams.copy(p.getInputStream(), System.out);
        return null;
      }
    }).get(60, TimeUnit.SECONDS);
    p.waitFor();
    

    projdir = scratch.getFileObject("my-android-application");
    pp = ProjectManager.getDefault().findProject(projdir);
  }

  @AfterClass
  public static void delete() {
//    FileUtilities.recursiveDelete(tempFolder);
  }

  /**
   * Test of findDalvikPlatform method, of class DalvikPlatformForMavenProvider.
   */
  @Test
  public void testFindDalvikPlatform() {
    assertNotNull(pp);
    DalvikPlatformResolver platformProvider = pp.getLookup().lookup(DalvikPlatformResolver.class);
    assertNotNull(platformProvider);
    DalvikPlatform platform = platformProvider.findDalvikPlatform();
    assertNotNull(platform);
  }
}
