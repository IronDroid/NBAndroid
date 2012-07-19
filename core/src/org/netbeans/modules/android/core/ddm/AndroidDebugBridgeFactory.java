/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.netbeans.modules.android.core.ddm;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.ClientData;
import com.android.ddmlib.DdmPreferences;
import com.android.ddmlib.DebugPortManager;
import com.android.ddmlib.Log;
import com.android.ddmlib.Log.LogLevel;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.android.core.sdk.DalvikPlatform;
import org.netbeans.modules.android.core.sdk.DalvikPlatformManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author tom
 */
public final class AndroidDebugBridgeFactory {
  public static final Logger LOG = Logger.getLogger(AndroidDebugBridgeFactory.class.getName());

  /*
   * Get adb location form DalvikPlatformManager (SdkConstants.OS_SDK_TOOLS_FOLDER + SdkConstants.FN_ADB)
   * store it in prefs for fast access
   * add listener to DPM to get changes and restart adb
   * 
   * make context object wrapping access to the bridge and its listeners.
   */

    private static DalvikPlatform usedPlatform;
    private static AndroidDebugBridge bridge;
    private static final String ADB_TOOL = "adb";    //NOI18N

    private static final boolean DEBUG = Boolean.getBoolean("AndroidDebugBridgeFactory.debug");   //NOI18N

    private AndroidDebugBridgeFactory () {
    }

    /** Singleton accessor. */
  public static synchronized AndroidDebugBridge getDefault() {
    if (usedPlatform != null) {
      return bridge;
    } else {
      for (DalvikPlatform androidPlatform : DalvikPlatformManager.getDefault().getPlatforms()) {
        try {
          AndroidDebugBridge adb = forSDK(androidPlatform);
          if (adb != null) {
            return adb;
          }
        } catch (IllegalArgumentException ex) {
          LOG.log(Level.INFO, "Cannot create debug bridge for this platform, will try different one.", ex);
        }
      }
    }
    LOG.log(Level.INFO, "Cannot create debug bridge for any platform");
    return null;
  }

    /**
     * Currently only one active SDK supported :-(
     * @return AndroidDebugBridge
     */
    /*public (private until it will work for more sdks)*/
    private static synchronized AndroidDebugBridge forSDK (final DalvikPlatform androidPlatform) {

        if (DEBUG) {
            Log.setLogOutput(new Log.ILogOutput() {

                @Override
                public void printLog(LogLevel arg0, String arg1, String arg2) {
                    LOG.log(
                        Level.WARNING, "{0} {1}", new Object[]{arg1, arg2});
                }

                @Override
                public void printAndPromptLog(LogLevel arg0, String arg1, String arg2) {
                    Logger.getLogger(AndroidDebugBridgeFactory.class.getName()).log(
                        Level.WARNING, "{0} {1}", new Object[]{arg1, arg2});
                }
            });
            DdmPreferences.setLogLevel("verbose");
        }

        if (bridge == null) {
            final FileObject path = androidPlatform.findTool(ADB_TOOL);
            if (path == null) {
                throw new IllegalArgumentException(
                        "Broken platform " + androidPlatform.getAndroidTarget().getFullName());         //NOI18N
            }
            usedPlatform = androidPlatform;
            ClientData.class.getClassLoader().clearAssertionStatus();      //qattern
            DebugPortManager.setProvider(DebugPortProvider.getDefault());
            AndroidDebugBridge.init(true);
            bridge = AndroidDebugBridge.createBridge(FileUtil.toFile(path).getAbsolutePath(), true);
            return bridge;
        }
        else if (usedPlatform.equals(androidPlatform)) {
            return bridge;
        }
        else {
            throw new UnsupportedOperationException(
                    "Already created for SDK " + usedPlatform.getAndroidTarget().getFullName());    //NOI18N
        }
    }

}
