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

import org.netbeans.modules.xml.xam.AbstractModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;

/**
 *
 * @author Martin Adamek
 */
public class AndroidManifestModelFactory extends AbstractModelFactory<AndroidManifestModel> {

    private static AndroidManifestModelFactory modelFactory = null;
    private static final Object instanceSyncPoint = new Object();

    private AndroidManifestModelFactory() {
    }

    public static AndroidManifestModelFactory getInstance() {
        synchronized (instanceSyncPoint) {
            AndroidManifestModelFactory _modelFactory = modelFactory;
            if (_modelFactory == null) {
                modelFactory = new AndroidManifestModelFactory();
            }
        }
        return modelFactory;
    }

    @Override
    protected AndroidManifestModel createModel(ModelSource source) {
        return new AndroidManifestModel(source);
    }

    @Override
    public AndroidManifestModel getModel(ModelSource source) {
        return super.getModel(source);
    }
}
