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

package org.netbeans.modules.android.project.ui;

import org.netbeans.modules.android.project.*;
import java.io.File;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.android.core.sdk.DalvikPlatformManager;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import static org.junit.Assert.*;

public class AndroidLogicalViewProviderTest {

  private static final String SDK_DIR = System.getProperty("test.all.android.sdks.home");

  private static File tempFolder;
  private static FileObject projdir;

  private static Project pp;
  private static FileObject someSource1;

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
    someSource1 = projdir.getFileObject("src/com/example/android/snake/Snake.java");
    }


  @AfterClass
  public static void delete() {
    FileUtilities.recursiveDelete(tempFolder);
  }

  @Test
  public void findPath() throws Exception {
    final AndroidProject proj = (AndroidProject) ProjectManager.getDefault().findProject(projdir);
    assertNotNull(someSource1);

    LogicalViewProvider viewProvider = proj.getLookup().lookup(LogicalViewProvider.class);
    assertNotNull(viewProvider);
    Node rootNode = viewProvider.createLogicalView();
    assertNotNull(rootNode);
    Node subNode = viewProvider.findPath(rootNode, someSource1);
    assertNotNull(subNode);
    FileObject layout = projdir.getFileObject("res/layout/snake_layout.xml");
    assertNotNull(layout);
    subNode = viewProvider.findPath(rootNode, layout);
    assertNotNull(subNode);
  }

}
