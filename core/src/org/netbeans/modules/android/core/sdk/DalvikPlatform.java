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

import com.android.sdklib.IAndroidTarget;
import com.android.sdklib.SdkConstants;
import com.android.sdklib.SdkManager;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

public class DalvikPlatform {
    public static final String PLATFORM_JAR  = "android.jar";   //NOI18N

    private static final Logger LOG = Logger.getLogger(DalvikPlatform.class.getName());

    private static URL findPlatformJar(IAndroidTarget androidTarget)
        throws MalformedURLException {
      IAndroidTarget platformTarget = androidTarget;
      while (platformTarget != null && !platformTarget.isPlatform()) {
        platformTarget = platformTarget.getParent();
      }
      if (platformTarget == null) {
        throw new IllegalStateException("Cannot find platform.jar for " + androidTarget);
      }
      File platformJar = FileUtil.normalizeFile(new File(platformTarget.getPath(IAndroidTarget.ANDROID_JAR)));
      URL archiveRoot = FileUtil.getArchiveRoot(platformJar.toURI().toURL());
      return archiveRoot;
    }

    private static URL findAnnotationsLib(SdkManager sdkManager)
        throws MalformedURLException {
      File annotationsJar = FileUtil.normalizeFile(new File(sdkManager.getLocation(), SdkConstants.FD_TOOLS +
                    File.separator + SdkConstants.FD_SUPPORT +
                    File.separator + SdkConstants.FN_ANNOTATIONS_JAR));
      URL archiveRoot = FileUtil.getArchiveRoot(annotationsJar.toURI().toURL());
      return archiveRoot;
    }

    private static List<URL> findTargetLibraries(IAndroidTarget target) {
      if (target == null) {
        return Collections.emptyList();
      }
      IAndroidTarget.IOptionalLibrary [] libs = target.getOptionalLibraries();
      if (libs == null) {
        return Collections.emptyList();
      }
      List<URL> libUrls = new ArrayList<URL>();
      for (IAndroidTarget.IOptionalLibrary lib : libs) {
        try {
          LOG.log(Level.FINER, "Adding standard lib {0} to AndroidTarget@{1}",
              new Object [] { lib.getJarPath(), target.getLocation() });
          libUrls.add(FileUtil.getArchiveRoot(new File(lib.getJarPath()).toURI().toURL()));
        } catch (MalformedURLException ex) {
          Exceptions.printStackTrace(ex);
        }
      }
      return libUrls;
    }

    private static List<URL> findAllLibraries(SdkManager sdkManager, IAndroidTarget target) throws MalformedURLException {
      List<URL> libUrls = new ArrayList<URL>();
      libUrls.add(findPlatformJar(target));
      libUrls.addAll(findTargetLibraries(target));
      if (target.getVersion().getApiLevel() <= 15) {
        libUrls.add(findAnnotationsLib(sdkManager));
      }
      return libUrls;
    }

    private final FileObject installFolder;
    private final List<URL> bootLibs;

    private final SdkManager sdkManager;
    private final IAndroidTarget androidTarget;

    //@GuardedBy (this)
    private ClassPath sources;

    DalvikPlatform (SdkManager sdkManager, IAndroidTarget androidTarget) throws IOException {
      Preconditions.checkNotNull(androidTarget);
      this.installFolder = FileUtil.toFileObject(new File(androidTarget.getLocation()));

      this.sdkManager = sdkManager;
      this.androidTarget = androidTarget;

      this.bootLibs = findAllLibraries(sdkManager, androidTarget);

      LOG.log(Level.CONFIG, "DalvikPlatform created: install folder = {0}, installTarget = {1}, null = {2}",
          new Object[] { installFolder, androidTarget, null });
    }

    public IAndroidTarget getAndroidTarget() {
      return androidTarget;
    }

    public SdkManager getSdkManager() {
      return sdkManager;
    }

    public List<URL> getBootstrapLibraries() {
        return this.bootLibs;
    }

    public FileObject getInstallFolder() {
      return installFolder;
    }

    public FileObject getPlatformFolder() {
      IAndroidTarget platformTarget = androidTarget;
      while (platformTarget != null && !platformTarget.isPlatform()) {
        platformTarget = platformTarget.getParent();
      }
      FileObject platformDir = 
          platformTarget == null ? null : FileUtil.toFileObject(new File(platformTarget.getLocation()));
      return platformDir;
    }

    public FileObject findTool(String toolName) {
        return Util.findTool(toolName, getPlatformFolder());
    }

    public synchronized ClassPath getSourceFolders() {
        if (this.sources == null) {
          List<URL> srcURLs = Lists.newArrayList();
          fillSourceFolders(sdkManager, androidTarget, srcURLs);
          this.sources = ClassPathSupport.createClassPath(srcURLs.toArray(new URL[0]));
        }
        return this.sources;
    }
    
    private static void fillSourceFolders(SdkManager sdkManager, IAndroidTarget androidTarget, List<URL> srcURLs) {
      if (androidTarget != null) {
        String srcs = androidTarget.getPath(IAndroidTarget.SOURCES);
        if (srcs != null) {
          LOG.log(Level.FINE, "Found sources {0} for target {1}", new Object[] {srcs, androidTarget});
          srcURLs.add(FileUtil.urlForArchiveOrDir(new File(srcs)));
        }
        String name = new File(androidTarget.getLocation()).getName();
        File altSourceFolder = new File(sdkManager.getLocation(), "sources" + File.separatorChar + name);
        if (altSourceFolder.isDirectory()) {
          LOG.log(Level.FINE, "Found alternative source folder {0} for target {1}", new Object[] {altSourceFolder, androidTarget});
          srcURLs.add(FileUtil.urlForArchiveOrDir(altSourceFolder));
        }
        fillSourceFolders(sdkManager, androidTarget.getParent(), srcURLs);
      }
      
    }

    public synchronized List<URL> getJavadocFolders() {
      List<URL> javadocs = new ArrayList<URL>();
      try {
        javadocs.add(new File(sdkManager.getLocation() + "/docs/reference").toURI().toURL());
        if (androidTarget != null) { // XXX(radim): when does this happen?
          String docs = androidTarget.getPath(IAndroidTarget.DOCS);
          if (docs != null) {
            final File docsFolder = new File(docs);
            if (docsFolder.exists()) {
              javadocs.add(docsFolder.toURI().toURL());
            } else {
              File docsParent = docsFolder.getParentFile();
              for (File child : docsParent.listFiles()) {
                if (!child.isDirectory()) {
                  continue;
                }
                javadocs.add(child.toURI().toURL());
              }
              
            }
          }
        }
      } catch (MalformedURLException ex) {
        Exceptions.printStackTrace(ex);
      }
      return javadocs;
    }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final DalvikPlatform other = (DalvikPlatform) obj;
    if (this.sdkManager != other.sdkManager && (this.sdkManager == null || !this.sdkManager.equals(other.sdkManager))) {
      return false;
    }
    if (this.androidTarget != other.androidTarget && (this.androidTarget == null || !this.androidTarget.equals(other.androidTarget))) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 37 * hash + (this.sdkManager != null ? this.sdkManager.hashCode() : 0);
    hash = 37 * hash + (this.androidTarget != null ? this.androidTarget.hashCode() : 0);
    return hash;
  }

  @Override
  public String toString() {
    return "DalvikPlatform{" + "installFolder=" + installFolder + ", androidTarget=" + androidTarget + '}';
  }
}
