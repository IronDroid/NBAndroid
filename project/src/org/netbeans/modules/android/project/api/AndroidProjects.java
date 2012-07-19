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

package org.netbeans.modules.android.project.api;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.android.core.sdk.DalvikPlatform;
import org.netbeans.modules.android.core.sdk.DalvikPlatformManager;
import org.netbeans.modules.android.project.AndroidProject;
import org.netbeans.modules.android.project.PropertyName;
import org.netbeans.modules.android.project.spi.DalvikPlatformResolver;

public class AndroidProjects {
  private static final Logger LOG = Logger.getLogger(AndroidProjects.class.getName());

    private AndroidProjects() {}

    /**
     * Finds the Android Platform corresponding to an Android project.
     * @param project a project
     * @return the corresponding platform, or null if the project was null, not an Android project, or otherwise had a problem
     */
    public static File sdkOf(Project project) {
        if (project == null) {
            return null;
        }
        AndroidProject aproject = project.getLookup().lookup(AndroidProject.class);
        if (aproject == null) {
            return null;
        }
        String sdkDir = aproject.evaluator().getProperty(PropertyName.SDK_DIR.getName());
        if (sdkDir == null) {
            return null;
        }
        // XXX follow logic used in ClassPathProviderImpl.createBoot
        return new File(sdkDir);
    }

    public static DalvikPlatform projectPlatform(Project project) {
      AndroidProject aProject = project.getLookup().lookup(AndroidProject.class);
      if (aProject != null) {
        String target = aProject.evaluator().getProperty(PropertyName.TARGET.getName());
        if (target != null) {
          DalvikPlatform platform = DalvikPlatformManager.getDefault().findPlatformForTarget(target);
          return platform;
        }
      }
      DalvikPlatformResolver dpr = project.getLookup().lookup(DalvikPlatformResolver.class);
      if (dpr != null) {
        DalvikPlatform platform = dpr.findDalvikPlatform();
        LOG.log(Level.FINE, "project {0} has DalvikPlatformResolver and resolves to {1}", 
            new Object[] {project, platform});
        if (platform != null) {
          return platform;
        }
      }
      LOG.log(Level.INFO, "could not find platform for {0}", project);
      return null;
    }

  public static boolean isAndroidProject(Project p) {
    if (p != null && p.getLookup().lookup(AndroidProject.class) != null) {
      return true;
    }
    if (isAndroidMavenProject(p)) {
      return true;
    }
    return false;
  }
  
  private static boolean isAndroidMavenProject(Project p) {
    DalvikPlatformResolver dpr = p != null ? p.getLookup().lookup(DalvikPlatformResolver.class) : null;
    if (dpr == null) {
      return false;
    }
    return dpr.findDalvikPlatform() != null;
  }
}
