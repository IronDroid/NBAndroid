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
package org.netbeans.modules.android.maven;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.project.Project;
import org.netbeans.modules.android.core.sdk.DalvikPlatform;
import org.netbeans.modules.android.core.sdk.DalvikPlatformManager;
import org.netbeans.modules.android.project.spi.DalvikPlatformResolver;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.spi.project.ProjectServiceProvider;

/**
 *
 * @author radim
 */
@ProjectServiceProvider(service = DalvikPlatformResolver.class,
    projectType = "org-netbeans-modules-maven")
public class DalvikPlatformForMavenProvider implements DalvikPlatformResolver {
  private static final Logger LOG = Logger.getLogger(DalvikPlatformForMavenProvider.class.getName());
  private static final String ANDROID_SDK_GROUP_ID = "com.google.android";
  private static final String ANDROID_SDK_ARTIFACT = "android";
  
  private final Project p;

  public DalvikPlatformForMavenProvider(Project p) {
    this.p = p;
  }

  @Override
  public DalvikPlatform findDalvikPlatform() {
    NbMavenProject mvnApiPrj = p.getLookup().lookup(NbMavenProject.class);
    if (mvnApiPrj == null) {
      // weird but OK
      return null;
    }
    MavenProject mavenProject = mvnApiPrj.getMavenProject();
    if (mavenProject == null) {
      LOG.log(Level.FINER, "No MavenProject for {0}", p);
      return null;
    }
    List<Dependency> deps = mavenProject.getDependencies();
    deps = deps != null ? deps : Collections.<Dependency>emptyList();
    for (Dependency d : deps) {
      LOG.log(Level.FINE, "dependency {0}", d);
    }
    Dependency androidDep = Iterables.find(deps, new Predicate<Dependency>() {

          @Override
          public boolean apply(Dependency d) {
            return ANDROID_SDK_ARTIFACT.equals(d.getArtifactId()) &&
                ANDROID_SDK_GROUP_ID.equals(d.getGroupId()) && 
                Artifact.SCOPE_PROVIDED.equals(d.getScope());
          }
        }, null);
    if (androidDep == null) {
      return null;
    }
    String version = androidDep.getVersion();
    LOG.log(Level.FINE, "{0} depends on android version {1}", new Object[] {p, version});
    if (version == null) {
      return null;
    }
    for (DalvikPlatform dp : DalvikPlatformManager.getDefault().getPlatforms()) {
      if (dp.getAndroidTarget().getVersionName().equals(version)) {
        LOG.log(Level.FINE, "{0} matches to {1}", new Object[] {p, dp});
        return dp;
      }
    }
    return null;
  }
}
