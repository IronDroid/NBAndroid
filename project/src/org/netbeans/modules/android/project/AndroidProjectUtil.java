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

import com.android.io.StreamException;
import com.android.sdklib.SdkManager;
import com.android.sdklib.internal.project.ProjectCreator;
import com.android.sdklib.xml.AndroidManifestParser;
import com.android.sdklib.xml.ManifestData;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.android.core.sdk.DalvikPlatform;
import org.netbeans.modules.android.core.sdk.DalvikPlatformManager;
import org.netbeans.modules.android.core.sdk.SdkLogProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.windows.InputOutput;
import org.openide.xml.EntityCatalog;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Miscellaneous utilities for the android module.
 */
public class AndroidProjectUtil {
    private static final Logger LOG = Logger.getLogger(AndroidProjectUtil.class.getName());
    private static final String ACTIVITY = "android.app.Activity";      //NOI18N

    private AndroidProjectUtil () {}

    public static Collection<ElementHandle<TypeElement>> getActivityClass (final FileObject file) {
        final Set<ElementHandle<TypeElement>> result = new HashSet<ElementHandle<TypeElement>>();
        try {
            JavaSource js = JavaSource.forFileObject(file);
            js.runUserActionTask(new Task<CompilationController>() {
                @Override
                public void run(CompilationController c) throws Exception {
                    c.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    final TypeElement activity = c.getElements().getTypeElement(ACTIVITY);
                    final List<? extends TypeElement> topElements = c.getTopLevelElements();
                    for (TypeElement e : topElements) {
                        if (e.getModifiers().contains(Modifier.PUBLIC) && isActivityClass(e, activity)) {
                            result.add(ElementHandle.create(e));
                            return;
                        }
                    }
                }
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return result;
    }

    public static boolean isActivityClass (final String className, ClassPath bootPath, ClassPath compilePath, ClassPath sourcePath) {
        final boolean[] result = new boolean[] {false};
        try {
            final ClasspathInfo cpInfo = ClasspathInfo.create(bootPath, compilePath, sourcePath);
            final JavaSource js = JavaSource.create(cpInfo);            
            js.runUserActionTask(new Task<CompilationController>() {

                @Override
                public void run(CompilationController c) throws Exception {
                    final TypeElement activity = c.getElements().getTypeElement(ACTIVITY);
                    if (activity == null) {
                        return;
                    }
                    TypeElement e = c.getElements().getTypeElement(className);
                    result[0] = isActivityClass(e, activity);
                }
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);            
        }
        return result[0];
    }

    private static boolean isActivityClass (TypeElement e, final TypeElement activity) {
        while (e != null) {
            if (e.equals(activity)) {
                return true;
            }
            e = (TypeElement)((DeclaredType)e.getSuperclass()).asElement();
       }
        return false;
    }


    public static Collection<ElementHandle<TypeElement>> getActivities (final FileObject[] sourceRoots) {
        final Set<ElementHandle<TypeElement>> result = new HashSet<ElementHandle<TypeElement>>();
        try {            
            final ClasspathInfo cpInfo = ClasspathInfo.create(sourceRoots[0]);
            final JavaSource js = JavaSource.create(cpInfo);
            js.runUserActionTask(new Task<CompilationController>() {

                @Override
                public void run(CompilationController c) throws Exception {
                    final TypeElement activity = c.getElements().getTypeElement(ACTIVITY);
                    if (activity == null) {
                        return;
                    }
                    final ElementHandle<TypeElement> activityHandle = ElementHandle.create(activity);
                    final Set<ElementHandle<TypeElement>> impls = c.getClasspathInfo().getClassIndex().getElements(activityHandle, EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS), EnumSet.of(ClassIndex.SearchScope.SOURCE));
                    for (ElementHandle<TypeElement> eh : impls) {
                        TypeElement e = eh.resolve(c);
                        if (isActivityClass(e, activity)) {
                            result.add(eh);
                        }
                    }
                }
            }, true);            
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return result;
    }

  /**
   * Creates new Android project using {@code ProjectCreator} from SDK.
   */
  public static void create(AndroidGeneralData data, String packageName, String activityEntry) {
    SdkManager sdkManager = data.getPlatform().getSdkManager();
    ProjectCreator prjCreator = new ProjectCreator(
        sdkManager, sdkManager.getLocation(),
        ProjectCreator.OutputLevel.NORMAL, SdkLogProvider.createSdkLogger(true));
    prjCreator.createProject(data.getProjectDirPath(),
        data.getProjectName(),
        packageName,
        activityEntry,
        data.getPlatform().getAndroidTarget(),
        /*library*/false,
        data.getMainProjectDirPath());
  }

  public static ManifestData parseProjectManifest(Project project) {
    FileObject androidManifest =
        project.getProjectDirectory().getFileObject(AndroidProjectType.ANDROID_MANIFEST_XML);
    if (androidManifest == null) {
      LOG.log(Level.WARNING, "No AndroidManifest.xml in {0}", project);
      return null;
    }
    InputStream manifestIS = null;
    try {
      manifestIS = androidManifest.getInputStream();
      return AndroidManifestParser.parse(manifestIS);
    } catch (ParserConfigurationException ex) {
      LOG.log(Level.WARNING, null, ex);
    } catch (StreamException ex) {
      LOG.log(Level.WARNING, null, ex);
    } catch (IOException ioe) {
      LOG.log(Level.WARNING, null, ioe);
    } catch (SAXException saxe) {
      LOG.log(Level.WARNING, null, saxe);
    } finally {
      try {
        if (manifestIS != null) {
          manifestIS.close();
        }
      } catch (IOException ex) {
        LOG.log(Level.INFO, null, ex);
      }
    }

    return null;
  }

  private static final String PLATFORM_STORAGE = "Services/Platforms/org-netbeans-api-java-Platform";

  /**
   * Check if this is an old project that was a fork of J2SE project and try to fix it into new one.
   * @param project 
   */
  public static void upgradeProject(AndroidProject project) {
    FileObject nbproject = project.getProjectDirectory().getFileObject("nbproject");
    if (nbproject == null) {
      return;
    }

    InputOutput io = AndroidIO.getDefaultIO();
    io.getOut().println(MessageFormat.format(
        "Upgrading old nbandroid project {0}", 
        project.getLookup().lookup(ProjectInformation.class).getName()));

    FileObject projectProps = nbproject.getFileObject("project.properties");
    if (projectProps == null) {
      LOG.log(Level.INFO, "Cannot upgrade old nbandroid project. project.properties file not found");
      return;
    }
    PropertyProvider props = PropertyUtils.propertiesFilePropertyProvider(
        new File(FileUtil.toFile(nbproject), "project.properties"));
    if (props == null || props.getProperties() == null) {
      LOG.log(Level.INFO, "Cannot upgrade old nbandroid project. platform definition not found in project.properties");
      return;
    }
    AndroidGeneralData data = findSDKAndTarget(project, props, nbproject);
    if (data == null) {
      io.getOut().println(MessageFormat.format(
          "Upgrading old nbandroid project {0} FAILED. See log file for details.", 
          project.getLookup().lookup(ProjectInformation.class).getName()));
      return;
    }
    // copy build.xml: it can contain some customizations
    io.getOut().println(MessageFormat.format(
        "Upgrading old nbandroid project {0}: move build.xml to unused_old_build.xml", 
        project.getLookup().lookup(ProjectInformation.class).getName()));
    FileObject buildXml = project.getProjectDirectory().getFileObject("build.xml");
    if (buildXml != null) {
      try {
        FileUtil.moveFile(buildXml, project.getProjectDirectory(), "unused_old_build");
      } catch (IOException ex) {
        Exceptions.printStackTrace(ex);
      }
    }
    // remove R.java generated into src tree
    FileObject outputs = findRFile(project.getProjectDirectory(), props);
    if (outputs != null) {
      FileUtilities.recursiveDelete(FileUtil.toFile(outputs));
    }
    // remove nbproject, build, dist
    io.getOut().println(MessageFormat.format(
        "Upgrading old nbandroid project {0}: delete nbproject, build and dist directories", 
        project.getLookup().lookup(ProjectInformation.class).getName()));
    FileUtilities.recursiveDelete(FileUtil.toFile(nbproject));
    outputs = project.getProjectDirectory().getFileObject("build");
    if (outputs != null) {
      FileUtilities.recursiveDelete(FileUtil.toFile(outputs));
    }
    outputs = project.getProjectDirectory().getFileObject("dist");
    if (outputs != null) {
      FileUtilities.recursiveDelete(FileUtil.toFile(outputs));
    }

    // recreate project file using SDK tools
    io.getOut().println(MessageFormat.format(
        "Upgrading old nbandroid project {0}: recreate project using Android SDK tools", 
        project.getLookup().lookup(ProjectInformation.class).getName()));
    project.update(data);
  }

  private static AndroidGeneralData findSDKAndTarget(
      AndroidProject project, PropertyProvider props, FileObject nbproject) {
    String platformName = props.getProperties().get("platform.active");
    if (platformName == null) {
      LOG.log(Level.INFO, "Cannot upgrade old nbandroid project. platform definition not found in project.properties");
      return null;
    }
    FileObject storage = FileUtil.getConfigFile(PLATFORM_STORAGE);
    String buildTargetPath = null;
    if (storage != null) {
      for (FileObject platformProvider : storage.getChildren()) {
        buildTargetPath = parseIfActivePlatorm(platformProvider, platformName);
        if (buildTargetPath != null) {
          break;
        }
      }
    }
    if (buildTargetPath == null) {
      LOG.log(Level.INFO, "Cannot upgrade old nbandroid project. platform {0} not found", platformName);
      return null;
    }
    DalvikPlatform platform = toDalvikPlatorm(buildTargetPath);
    if (platform == null) {
      LOG.log(Level.INFO, "Cannot upgrade old nbandroid project. platform {0} not found in Android SDK", platformName);
      return null;
    }
    AndroidGeneralData data = new AndroidGeneralData();
    data.setPlatform(platform);
    data.setProjectDirPath(project.getProjectDirectoryFile().getAbsolutePath());
    data.setProjectName(project.getLookup().lookup(ProjectInformation.class).getName());

    return data;
  }

  private static FileObject findRFile(FileObject projDir, PropertyProvider props) {
    String srcDir = props.getProperties().get("src.dir");
    String pkgName = props.getProperties().get("main.component");
    if (srcDir == null || pkgName == null) {
      LOG.log(Level.FINE, "Cannot find properties to locate R.java: src.dir = {0}, main.component = {1}", 
          new Object[] { srcDir, pkgName });
      return null;
    }
    String path = srcDir + File.separator + pkgName.replace('.', File.separatorChar) + File.separator + "R.java";
    FileObject rFile = projDir.getFileObject(path);
    LOG.log(Level.FINE, "Looking for {0} in {2}. Found {1}", new Object[] { path, rFile, projDir.getPath() });
    return rFile;
  }

  /*@VisibleForTesting*/ static DalvikPlatform toDalvikPlatorm(String targetDir) {
    DalvikPlatformManager dpm = DalvikPlatformManager.getDefault();
    try {
      URL targetDirURL = new URL(targetDir);
      FileObject targetDirFO = URLMapper.findFileObject(targetDirURL);
      if (targetDirFO == null || targetDirFO.getParent() == null || targetDirFO.getParent().getParent() == null) {
        return null;
      }
      FileObject sdkDirFO = targetDirFO.getParent().getParent();
      if (dpm.getSdkLocation() == null) {
        dpm.setSdkLocation(sdkDirFO.getPath());
      } else if (!sdkDirFO.getPath().equals(dpm.getSdkLocation())) {
        // SDK in old platform and new settings do not match
        return null;
      }
      for (DalvikPlatform p : dpm.getPlatforms()) {
        if (targetDirFO.equals(p.getInstallFolder())) {
          return p;
        }
      }
    } catch (MalformedURLException ex) {
      LOG.log(Level.FINE, null, ex);
    }
    return null;
  }

  /*@VisibleForTesting*/ static String parseIfActivePlatorm(FileObject platformProvider, String platformName) {
    InputStream is;
    try {
      is = platformProvider.getInputStream();
      class MyErrorHandler implements ErrorHandler {

        @Override
        public void warning(SAXParseException ex) throws SAXException {
          LOG.log(Level.FINE, null, ex);
        }

        @Override
        public void error(SAXParseException ex) throws SAXException {
          LOG.log(Level.INFO, null, ex);
        }

        @Override
        public void fatalError(SAXParseException ex) throws SAXException {
          LOG.log(Level.INFO, null, ex);
        }
      }
      MyErrorHandler handler = new MyErrorHandler();
      Document doc = XMLUtil.parse(new InputSource(is), true, false, handler, EntityCatalog.getDefault());
      XPathFactory factory = XPathFactory.newInstance();
      Object name = factory.newXPath().compile(
          "/platform/properties/property[@name='platform.ant.name']/@value").evaluate(doc, XPathConstants.STRING);
      LOG.log(Level.FINE, "Found platform {0} in {1}", new Object[] {name, platformProvider});
      if (!platformName.equals(name)) {
        return null;
      }
      Object url = factory.newXPath().compile("/platform/home/resource/text()")
          .evaluate(doc, XPathConstants.STRING);
      return url instanceof String ? (String) url : null;
    } catch (XPathExpressionException ex) {
      LOG.log(Level.WARNING, null, ex);
    } catch (SAXException ex) {
      LOG.log(Level.INFO, null, ex);
    } catch (IOException ioe) {
      LOG.log(Level.WARNING, null, ioe);
    }
    return null;
  }

  /**
   * Tries to find an opened project via it's package name.
   * @param packageName the projects package name
   * @return The first matching project or {@code null}, if nothing was found.
   */
  public static AndroidProject findProjectByPackageName(String packageName) {
    for(Project p : OpenProjects.getDefault().getOpenProjects()) {
      AndroidProject project = p.getLookup().lookup(AndroidProject.class);

      if (project != null) {
        ManifestData manifest = AndroidProjectUtil.parseProjectManifest(project);

        if (manifest != null && manifest.getPackage().equals(packageName)) {
          return project;
        }
      }
    }

    return null;
  }

  /**
   * Tries to convert a classpath with classes into a classpath containing java source code.
   * @param cp
   * @return 
   */
  public static ClassPath toSourcePath(final ClassPath cp) {
    final List<FileObject> resources = new ArrayList<FileObject>();
    for (ClassPath.Entry e : cp.entries()) {
      final FileObject[] srcRoots = SourceForBinaryQuery.findSourceRoots(e.getURL()).getRoots();
      resources.addAll(Arrays.asList(srcRoots));
    }
    return ClassPathSupport.createClassPath(resources.toArray(new FileObject[resources.size()]));
  }
}
