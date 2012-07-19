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
import org.netbeans.modules.xml.xam.ComponentUpdater.Operation;

/**
 *
 * @author Martin Adamek
 */
public class SyncUpdateVisitor extends AndroidManifestVisitor.Default implements ComponentUpdater<AndroidManifestComponent> {

    private AndroidManifestComponent target;
    private Operation operation;
    private int index;

    public void update(AndroidManifestComponent target, AndroidManifestComponent child, Operation operation) {
        update(target, child, -1 , operation);
    }

    public void update(AndroidManifestComponent target, AndroidManifestComponent child, int index, Operation operation) {
        assert target != null;
        assert child != null;
        this.target = target;
        this.index = index;
        this.operation = operation;
        child.accept(this);
    }

}
