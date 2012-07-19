/*
 *  Copyright 2009 radim.
 * 
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

package org.netbeans.modules.android.core.sdk;

import com.google.common.collect.Iterables;
import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author radim
 */
public class Utils {

  // XXX this is repeating something from SDK
  public enum TestPlatform {
    ANDROID_4_0_3("android-15", "platforms/android-15"),
    ANDROID_WITH_GOOGLE_API_4_0_3(
        "Google Inc.:Google APIs:15", "add-ons/addon-google_apis-google_inc_-15", "platforms/android-15"),
    ANDROID_4_0("android-14", "platforms/android-14"),
    ANDROID_WITH_GOOGLE_API_4_0(
        "Google Inc.:Google APIs:14", "add-ons/addon-google_apis-google_inc_-14", "platforms/android-14"),
    ANDROID_3_2("android-13", "platforms/android-13"),
    ANDROID_WITH_GOOGLE_API_3_2(
        "Google Inc.:Google APIs:13", "add-ons/addon_google_apis_google_inc_13", "platforms/android-13"),
    ANDROID_3_1("android-12", "platforms/android-12"),
    ANDROID_WITH_GOOGLE_API_3_1(
        "Google Inc.:Google APIs:12", "add-ons/addon_google_apis_google_inc_12", "platforms/android-12"),
    ANDROID_3_0("android-11", "platforms/android-11"),
    ANDROID_WITH_GOOGLE_API_3_0(
        "Google Inc.:Google APIs:11", "add-ons/addon_google_apis_google_inc_11", "platforms/android-11"),
    ANDROID_2_3_3("android-10", "platforms/android-10"),
    ANDROID_WITH_GOOGLE_API_2_3_3(
        "Google Inc.:Google APIs:10", "add-ons/addon_google_apis_google_inc_10", "platforms/android-10"),
    ANDROID_2_3_1("android-9", "platforms/android-9"),
    ANDROID_WITH_GOOGLE_API_2_3_1(
        "Google Inc.:Google APIs:9", "add-ons/addon_google_apis_google_inc_9", "platforms/android-9"),
    ANDROID_2_2("android-8", "platforms/android-8"),
    ANDROID_WITH_GOOGLE_API_2_2(
        "Google Inc.:Google APIs:8", "add-ons/addon_google_apis_google_inc_8", "platforms/android-8"),
    ANDROID_2_1u1("android-7", "platforms/android-7"),
    ANDROID_WITH_GOOGLE_API_2_1u1(
        "Google Inc.:Google APIs:7", "add-ons/addon_google_apis_google_inc_7", "platforms/android-7"),
    ANDROID_1_6("android-4", "platforms/android-4"),
    ANDROID_WITH_GOOGLE_API_1_6(
        "Google Inc.:Google APIs:4", "add-ons/addon_google_apis_google_inc_4", "platforms/android-4"),
    ANDROID_1_5("android-3", "platforms/android-3"),
    ANDROID_WITH_GOOGLE_API_1_5(
        "Google Inc.:Google APIs:3", "add-ons/addon_google_apis_google_inc_3", "platforms/android-3");

    private final String target;
    private final String installDir;
    private final String platformDir;

    private TestPlatform(String target, String installDir) {
      this(target, installDir, installDir);
    }

    private TestPlatform(String target, String installDir, String platformDir) {
      this.target = target;
      this.installDir = installDir;
      this.platformDir = platformDir;
    }

    public String getTarget() {
      return target;
    }

    public String getPlatformDir() {
      return platformDir;
    }

    public String getInstallDir() {
      return installDir;
    }

    @Override
    public String toString() {
      return "TestPlatform{" + "target=" + target + ", installDir=" + installDir + ", platformDir=" + platformDir + '}';
    }
  }

  private Utils() {}

  public static Iterable<TestPlatform> purePlatforms() {
    return EnumSet.of(TestPlatform.ANDROID_1_5,
        TestPlatform.ANDROID_1_6,
        TestPlatform.ANDROID_2_1u1,
        TestPlatform.ANDROID_2_2,
        TestPlatform.ANDROID_2_3_1,
        TestPlatform.ANDROID_2_3_3,
        TestPlatform.ANDROID_3_0,
        TestPlatform.ANDROID_3_1,
        TestPlatform.ANDROID_3_2,
        TestPlatform.ANDROID_4_0,
        TestPlatform.ANDROID_4_0_3);
  }

  public static Iterable<TestPlatform> googlePlatforms() {
    return EnumSet.of(TestPlatform.ANDROID_WITH_GOOGLE_API_1_5,
        TestPlatform.ANDROID_WITH_GOOGLE_API_1_6,
        TestPlatform.ANDROID_WITH_GOOGLE_API_2_1u1,
        TestPlatform.ANDROID_WITH_GOOGLE_API_2_2,
        TestPlatform.ANDROID_WITH_GOOGLE_API_2_3_1,
        TestPlatform.ANDROID_WITH_GOOGLE_API_2_3_3,
        TestPlatform.ANDROID_WITH_GOOGLE_API_3_0,
        TestPlatform.ANDROID_WITH_GOOGLE_API_3_1,
        TestPlatform.ANDROID_WITH_GOOGLE_API_3_2,
        TestPlatform.ANDROID_WITH_GOOGLE_API_4_0,
        TestPlatform.ANDROID_WITH_GOOGLE_API_4_0_3);
  }

  public static Iterable<TestPlatform> allPlatforms() {
    return Iterables.concat(purePlatforms(), googlePlatforms());
  }

  private static FileObject createSdk() throws IOException {
      return FileUtil.toFileObject(new File(System.getProperty("test.all.android.sdks.home")));
  }

  public static FileObject createSdkPlatform(TestPlatform p) throws IOException {
    return createSdk().getFileObject(p.getInstallDir());
  }

  public static FileObject createSdk15platform15() throws IOException {
    return createSdkPlatform(TestPlatform.ANDROID_1_5);
  }

  public static FileObject createSdk15platformGoogleAPI() throws IOException {
    return createSdkPlatform(TestPlatform.ANDROID_WITH_GOOGLE_API_1_5);
  }
}
