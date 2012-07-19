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

import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.w3c.dom.Element;

/**
 *
 * @author Martin Adamek
 */
public class AndroidManifestModel extends AbstractDocumentModel<AndroidManifestComponent> {

    private final AndroidManifestCompontentFactory componentFactory;
    private Manifest manifest;

    AndroidManifestModel(ModelSource source) {
        super(source);
        componentFactory = new AndroidManifestCompontentFactory(this);
    }

    @Override
    public AndroidManifestComponent createRootComponent(Element root) {
        Manifest newManifest = (Manifest) getFactory().create(root, null);
        if (newManifest != null) {
            manifest = newManifest;
        }
        return newManifest;
    }

    @Override
    protected ComponentUpdater<AndroidManifestComponent> getComponentUpdater() {
        return new SyncUpdateVisitor();
    }

    public Manifest getRootComponent() {
        return manifest;
    }

    public AndroidManifestComponent createComponent(AndroidManifestComponent parent, Element element) {
        return getFactory().create(element, parent);
    }

    public AndroidManifestCompontentFactory getFactory() {
        return componentFactory;
    }

}
