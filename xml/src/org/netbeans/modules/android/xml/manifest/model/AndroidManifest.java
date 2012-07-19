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

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Adamek
 */
public class AndroidManifest {

    // We can override equals() and hashcode() methods here for accepting 2 keys in HashMap
    // However due to the performance issue and clear codes, use 2 HashMap here will be better
    private static WeakHashMap<FileObject, WeakReference<AndroidManifestModel>> configModelsEditable = new WeakHashMap<FileObject, WeakReference<AndroidManifestModel>>();
    private static WeakHashMap<FileObject, WeakReference<AndroidManifestModel>> configModelsNonEditable = new WeakHashMap<FileObject, WeakReference<AndroidManifestModel>>();

    public static synchronized AndroidManifestModel getModel(FileObject confFile, boolean editable) {
        AndroidManifestModel configModel = null;
        if (confFile != null && confFile.isValid()) {
            Map<FileObject,WeakReference<AndroidManifestModel>> configModelsRef = editable ? configModelsEditable : configModelsNonEditable;
            WeakReference<AndroidManifestModel> configModelRef = configModelsRef.get(confFile);
            if (configModelRef != null) {
                configModel = configModelRef.get();
                if (configModel != null) {
                    return configModel;
                }

                configModelsRef.remove(confFile);
            }

            try {
                ModelSource modelSource = Utilities.createModelSource(confFile,editable);
                configModel = AndroidManifestModelFactory.getInstance().getModel(modelSource);
                configModelsRef.put(confFile, new WeakReference<AndroidManifestModel>(configModel));
            } catch (CatalogModelException ex) {
                java.util.logging.Logger.getLogger(AndroidManifest.class.getName()).
                    log(java.util.logging.Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return configModel;
    }

}
