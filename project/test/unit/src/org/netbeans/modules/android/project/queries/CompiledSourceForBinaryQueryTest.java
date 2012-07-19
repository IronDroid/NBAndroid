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

import org.junit.Before;
import java.io.File;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import java.net.URL;
import org.junit.Test;
import org.netbeans.api.java.queries.SourceForBinaryQuery.Result;
import org.netbeans.modules.android.project.AndroidProject;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.openide.filesystems.FileUtil;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CompiledSourceForBinaryQueryTest {

  private AndroidProject project;
  private FileSystem fs;
  private FileObject prjDirFo;
  private URL binRoot;

  @Before
  public void setUp() throws Exception {
    project = mock(AndroidProject.class);
    fs = FileUtil.createMemoryFileSystem();
    prjDirFo = fs.getRoot().createFolder("prjFolder");
    when(project.getProjectDirectory()).thenReturn(prjDirFo);
    when(project.getProjectDirectoryFile()).thenReturn(new File(prjDirFo.getPath()));
    binRoot = FileUtil.urlForArchiveOrDir(
        new File(project.getProjectDirectoryFile(), "bin/classes"));
  }

  @Test
  public void findSourceRoots() throws Exception {
    SourceForBinaryQueryImplementation s4bQ = new CompiledSourceForBinaryQuery(project);
    Result result = s4bQ.findSourceRoots(binRoot);
    assertEquals(0, result.getRoots().length);

    prjDirFo.createFolder("src");
    result = s4bQ.findSourceRoots(binRoot);
    assertEquals(1, result.getRoots().length);
    prjDirFo.createFolder("gen");
    result = s4bQ.findSourceRoots(binRoot);
    assertEquals(2, result.getRoots().length);
  }

  @Test
  public void findSourceRoots2() throws Exception {
    SourceForBinaryQueryImplementation s4bQ = new CompiledSourceForBinaryQuery(project);
    prjDirFo.createFolder("gen");
    Result result = s4bQ.findSourceRoots(binRoot);
    assertEquals(1, result.getRoots().length);
  }

  @Test
  public void findSourceRootsForCompiledLibrary() throws Exception {
    SourceForBinaryQueryImplementation s4bQ = new CompiledSourceForBinaryQuery(project);
    prjDirFo.createFolder("src");
    prjDirFo.createFolder("gen");
    URL libRoot = FileUtil.urlForArchiveOrDir(
        new File(project.getProjectDirectoryFile(), "bin/classes.jar"));
    Result result = s4bQ.findSourceRoots(libRoot);
    assertEquals(2, result.getRoots().length);
  }
}