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

import com.android.sdklib.xml.ManifestData;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.project.Project;
import org.netbeans.modules.android.project.AndroidProjectUtil;
import org.openide.filesystems.FileObject;

/**
 *
 * @author radim
 */
class ProjectRefResolver implements ReferenceResolver {
  public static final Logger LOG = Logger.getLogger(ProjectRefResolver.class.getName());

  private final Project prj;

  public ProjectRefResolver(Project prj) {
    this.prj = Preconditions.checkNotNull(prj);
  }

  @Override
  public List<ResourceRef> getReferences() {
    final List<ResourceRef> results = Lists.newArrayList();
    ManifestData manifest = AndroidProjectUtil.parseProjectManifest(prj);
    final String pkg = manifest != null ? manifest.getPackage() : null;
    FileObject rFile = pkg != null ?
        prj.getProjectDirectory().getFileObject("gen/" + pkg.replace('.', '/') + "/R.java") :
        null;
    if (rFile == null) {
      LOG.log(Level.FINE, "no R.java");
      return Collections.emptyList();
    }
    ClasspathInfo cpInfo = ClasspathInfo.create(rFile);
    if (cpInfo == null) {
      LOG.log(Level.FINE, "no ClasspathInfo");
      return Collections.emptyList();
    }
    JavaSource javaSource = JavaSource.create(cpInfo, rFile);
    if (javaSource == null) {
      return Collections.emptyList();
    }

    try {
      javaSource.runUserActionTask(new Task<CompilationController>() {

        @Override
        public void run(CompilationController parameter) throws IOException {

          parameter.toPhase(Phase.ELEMENTS_RESOLVED);

          List<? extends TypeElement> topLevelElements = parameter.getTopLevelElements();
          for (TypeElement el : topLevelElements) {
            if (!el.getSimpleName().contentEquals("R")) continue;

            for (TypeElement category : ElementFilter.typesIn(el.getEnclosedElements())) {
              String catName = category.getSimpleName().toString();
              for (VariableElement resource : ElementFilter.fieldsIn(category.getEnclosedElements())) {
                String resName = resource.getSimpleName().toString();
                results.add(new ResourceRef(true, pkg, catName, resName));
              }
            }
          }
        }
      }, true);
    } catch (IOException e) {
      LOG.log(Level.SEVERE, e.getMessage(), e);
    }

    return results;
  }
}
