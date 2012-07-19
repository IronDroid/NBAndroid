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

package org.netbeans.modules.android.grammars;

import com.google.common.base.Splitter;
import org.netbeans.modules.android.project.AndroidGeneralData;
import org.netbeans.modules.android.project.AndroidProject;
import org.netbeans.modules.android.project.FileUtilities;
import java.io.File;
import java.io.OutputStream;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.android.core.sdk.DalvikPlatformManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import static org.junit.Assert.*;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.java.source.indexing.JavaCustomIndexer;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.java.source.parsing.JavacParserFactory;
import org.netbeans.modules.parsing.impl.indexing.CacheFolder;
import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
import org.openide.filesystems.MIMEResolver;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

/**
 * Tests for AndroidActionProvider
 */
public class ProjectRefResolverTest {

  private static final String SDK_DIR = System.getProperty("test.all.android.sdks.home");

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
    FileObject cacheDir = scratch.createFolder("cache");
    CacheFolder.setCacheFolder(cacheDir);
    FileObject sdkDirFo = FileUtil.toFileObject(new File(SDK_DIR));

    projdir = scratch.createFolder("Snake");
    FileUtilities.recursiveCopy(sdkDirFo.getFileObject("samples/android-8/Snake"), projdir);

    FileObject dir = projdir;
    for (String dirName : Splitter.on('/').split("gen/com/example/android/snake")) {
      dir = dir.createFolder(dirName);
    }
    OutputStream os = dir.createData("R.java").getOutputStream();
    FileUtil.copy(
        ProjectRefResolverTest.class.getResourceAsStream("Snake_R_java.txt"), os);
    os.close();

    pp = ProjectManager.getDefault().findProject(projdir);
  }


  @AfterClass
  public static void delete() {
    FileUtilities.recursiveDelete(tempFolder);
  }

  @Test
  public void refsInProject() throws Exception {
    final AndroidProject proj = (AndroidProject) ProjectManager.getDefault().findProject(projdir);
    AndroidGeneralData data = AndroidGeneralData.fromProject(proj);
    data.setPlatform(DalvikPlatformManager.getDefault().findPlatformForTarget("android-8"));
    proj.update(data);

    ReferenceResolver rr = new ProjectRefResolver(pp);
    List<ResourceRef> refs = rr.getReferences();
    assertTrue(!refs.isEmpty());
    assertTrue(refs.contains(new ResourceRef(true, "com.example.android.snake", "id", "snake")));
    assertEquals(14, refs.size());
  }

  @ServiceProvider(service = MimeDataProvider.class)
  public static final class JavacParserProvider implements MimeDataProvider {

    private Lookup javaLookup = Lookups.fixed(new JavacParserFactory(), new JavaCustomIndexer.Factory());

    @Override
    public Lookup getLookup(MimePath mimePath) {
      if (mimePath.getPath().endsWith(JavacParser.MIME_TYPE)) {
        return javaLookup;
      }

      return Lookup.EMPTY;
    }
  }

  @ServiceProvider(service = MIMEResolver.class)
  public static final class JavaMimeResolver extends MIMEResolver {

    public JavaMimeResolver() {
      super(JavacParser.MIME_TYPE);
    }

    @Override
    public String findMIMEType(FileObject fo) {
      if ("java".equals(fo.getExt())) {
        return JavacParser.MIME_TYPE;
      }

      return null;
    }
  }
}
