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

import java.io.File;
import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager.Result;
import org.netbeans.modules.android.core.sdk.DalvikPlatformManager;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectFactory2;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * Factory for simple Android projects.
 */
@ServiceProvider(service=ProjectFactory.class, position=/* after Maven, before autoproject */830)
public final class AndroidProjectType implements ProjectFactory2 {

  /** Property name fired from AndroidProjectInfo. */
  public static final String NEEDS_FIX_INFO = "needsFix";

  public static final String ANDROID_MANIFEST_XML = "AndroidManifest.xml";

    public @Override Result isProject2(FileObject projectDirectory) {
        if (isProject(projectDirectory)) {
            return new Result(ImageUtilities.loadImageIcon("org/netbeans/modules/android/project/ui/resources/androidProject.png", true));
        } else {
            return null;
        }
    }

    public @Override boolean isProject(FileObject projectDirectory) {
        // XXX perhaps also make sure src/ exists, so we do not pick up resources dir for a Maven-Android project
        return projectDirectory.getFileObject(ANDROID_MANIFEST_XML) != null;
    }

    public @Override Project loadProject(FileObject projectDirectory, ProjectState state) throws IOException {
        if (isProject(projectDirectory)) {
            File dirF = FileUtil.toFile(projectDirectory);
            if (dirF != null) {
                return new AndroidProjectImpl(
                    projectDirectory, dirF, DalvikPlatformManager.getDefault());
            }
        }
        return null;
    }

    public @Override void saveProject(Project project) throws IOException, ClassCastException {}

}
