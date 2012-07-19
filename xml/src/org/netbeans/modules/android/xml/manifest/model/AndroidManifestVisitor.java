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

package org.netbeans.modules.android.xml.manifest.model;

/**
 *
 * @author Martin Adamek
 */
public interface AndroidManifestVisitor {

    void visit(Manifest context);

    void visit(Application application);

    public static class Default implements AndroidManifestVisitor {

        public void visit(Manifest component) {
            visitChild();
        }

        public void visit(Application application) {
            visitChild();
        }

        protected void visitChild() {
        }

    }

}
