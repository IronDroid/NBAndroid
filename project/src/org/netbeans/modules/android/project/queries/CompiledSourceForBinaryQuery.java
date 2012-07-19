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

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery.Result;
import org.netbeans.modules.android.project.AndroidProject;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Finds sources corresponding to binaries in an android project.
 */
public class CompiledSourceForBinaryQuery implements SourceForBinaryQueryImplementation {

    private final AndroidProject project;
    private final URL binRoot;
    private final URL libRoot;

    public CompiledSourceForBinaryQuery(AndroidProject project) {
        this.project = project;
        binRoot = FileUtil.urlForArchiveOrDir(new File(project.getProjectDirectoryFile(), "bin/classes"));
        libRoot = FileUtil.urlForArchiveOrDir(new File(project.getProjectDirectoryFile(), "bin/classes.jar"));
    }

    public @Override Result findSourceRoots(URL binaryRoot) {
        if (binaryRoot.equals(binRoot) || binaryRoot.equals(libRoot)) {
            return new Result() {
                public @Override FileObject[] getRoots() {
                    List<FileObject> roots = new ArrayList<FileObject>(2);
                    for (String dir : new String[] {"src", "gen"}) {
                        FileObject f = project.getProjectDirectory().getFileObject(dir);
                        if (f != null) {
                            roots.add(f);
                        }
                    }
                    return roots.toArray(new FileObject[roots.size()]);
                }
                public @Override void addChangeListener(ChangeListener l) {}
                public @Override void removeChangeListener(ChangeListener l) {}
            };
        } 
        return null;
    }

}
