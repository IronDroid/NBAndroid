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

package org.netbeans.modules.android.xml.manifest;

import java.awt.Image;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.modules.xml.multiview.DesignMultiViewDesc;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Adamek
 */
public class OverviewDescription extends DesignMultiViewDesc {

    private final FileObject androidManifestXml;

    public OverviewDescription(ManifestDataObject dataObject, String name) {
//        super(dataObject, name);
        this.androidManifestXml = dataObject.getPrimaryFile();
    }

    @Override
    public MultiViewElement createElement() {
        return new OverviewPanel(androidManifestXml);
    }

    @Override
    public Image getIcon() {
        return null;//ImageUtilities.loadImage(ManifestDataObject.ICON_PATH);
    }

    @Override
    public String preferredID() {
        return getClass().getName();
    }

}
