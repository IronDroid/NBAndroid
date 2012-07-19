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

import java.util.List;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Martin Adamek
 */
public abstract class AndroidManifestComponent extends AbstractDocumentComponent<AndroidManifestComponent> {

    public AndroidManifestComponent(AbstractDocumentModel model, Element e) {
        super(model, e);
    }

    @Override
    public AndroidManifestModel getModel() {
        return (AndroidManifestModel) super.getModel();
    }

    @Override
    protected void populateChildren(List<AndroidManifestComponent> children) {
        NodeList nodeList = getPeer().getChildNodes();
        if (nodeList != null){
            for (int i = 0; i < nodeList.getLength(); i++) {
                org.w3c.dom.Node node = nodeList.item(i);
                if (node instanceof Element) {
                    AndroidManifestModel model = getModel();
                    AndroidManifestComponent comp = model.getFactory().create((Element)node, this);
                    if (comp != null) {
                        children.add(comp);
                    }
                }
            }
        }
    }

    static public Element createElementNS(AbstractDocumentModel model, AndroidManifestQNames qname) {
        return model.getDocument().createElementNS(model.getRootComponent().getPeer().getNamespaceURI(), qname.getQualifiedName());
    }

    @Override
    protected Object getAttributeValueOf(Attribute attr, String stringValue) {
        return stringValue;
    }

    abstract void accept(AndroidManifestVisitor visitor);

}
