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

import com.android.sdklib.SdkManager;
import com.android.sdklib.internal.project.ProjectCreator;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import org.netbeans.modules.android.core.sdk.DalvikPlatform;
import org.netbeans.modules.android.core.sdk.DalvikPlatformManager;
import org.netbeans.modules.android.core.sdk.SdkLogProvider;
import org.netbeans.modules.android.core.sdk.Utils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import static org.junit.Assert.*;

/**
 *
 * @author radim
 */
public class TestUtils {

  /**
   * Creates an android project.
   * Expects that DalvikPlatformManager is already configured.
   *
   * @return file object of a project root directory
   */
  public static FileObject createProject(Utils.TestPlatform p, FileObject root, String name,
      boolean isLibrary, String ... usedLib) 
      throws IOException {
    return createProject(p, root, name, null, isLibrary, usedLib);
  }
  
  public static FileObject createProject(Utils.TestPlatform p, FileObject root, String name,
      String mainProject, boolean isLibrary, String ... usedLib) 
      throws IOException {
    DalvikPlatformManager platformManager = DalvikPlatformManager.getDefault();
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
              mainProject
    );

    if (usedLib.length > 0) {

      // write project properties
      FileObject foProjectProps = foProject.getFileObject("project", "properties");
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
    foProject.refresh();
    if (isLibrary) {
      // TODO extract this enable test of built/not built lib dependency
      // create files for build output
      FileObject binFo = foProject.getFileObject("bin");
      if (binFo == null) {
        binFo = foProject.createFolder("bin");
      }
      binFo.createData("classes.jar");
    }
    return foProject;
  }
}
