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

import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.w3c.dom.Element;

/**
 *
 * @author Martin Adamek
 */
public class Manifest extends AndroidManifestComponent {

    public Manifest(AbstractDocumentModel model, Element e) {
        super(model, e);
    }

    // just for initial testing
    public String getPackage() {
        return getPeer().getAttribute("package");
    }

    public void setApplication(Application application) {
        try {
            getModel().startTransaction();
            appendChild(AndroidManifestQNames.APPLICATION.getLocalName(), application);
        } finally {
            if (getModel().isIntransaction()) {
                getModel().endTransaction();
            }
        }
    }

    @Override
    void accept(AndroidManifestVisitor visitor) {
        visitor.visit(this);
    }
}
