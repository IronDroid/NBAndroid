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

import javax.xml.namespace.QName;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.w3c.dom.Element;

/**
 *
 * @author Martin Adamek
 */
public class AndroidManifestCompontentFactory {

    private final AndroidManifestModel model;

    public AndroidManifestCompontentFactory(AndroidManifestModel model) {
        this.model = model;
    }

    public AndroidManifestComponent create(Element element, AndroidManifestComponent context) {
        AndroidManifestComponent configComponent = null;
        if (context == null){
            configComponent = new Manifest(model, element);
        } else {
            configComponent = new CreateVisitor().create(element, context);
        }
        return configComponent;
    }

    public static boolean areSameQName(AndroidManifestQNames jsfqname,Element element) {
        QName qname = AbstractDocumentComponent.getQName(element);
        return jsfqname.getLocalName().equals(qname.getLocalPart());
    }

    public Application createApplication() {
        return new Application(model);
    }

    public static class CreateVisitor implements AndroidManifestVisitor {

        Element element;
        AndroidManifestComponent created;

        AndroidManifestComponent create(Element element, AndroidManifestComponent context) {
            this.element = element;
            context.accept(this);
            return created;
        }

        private boolean isElementQName(AndroidManifestQNames jsfqname) {
            return areSameQName(jsfqname, element);
        }

        public void visit(Manifest context) {
            if (isElementQName(AndroidManifestQNames.MANIFEST)) {
                created = new Manifest(context.getModel(), element);
            }
        }

        public void visit(Application application) {
            if (isElementQName(AndroidManifestQNames.APPLICATION)) {
                created = new Application(application.getModel(), element);
            }
        }

    }

}
