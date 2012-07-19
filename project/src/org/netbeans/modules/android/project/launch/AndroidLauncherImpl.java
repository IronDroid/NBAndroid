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

package org.netbeans.modules.android.project.launch;

import com.android.ddmlib.*;
import com.android.prefs.AndroidLocation.AndroidLocationException;
import com.android.sdklib.IAndroidTarget;
import com.android.sdklib.ISdkLog;
import com.android.sdklib.SdkManager;
import com.android.sdklib.internal.avd.AvdManager;
import com.android.sdklib.internal.avd.AvdInfo;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.android.core.ddm.AndroidDebugBridgeFactory;
import org.netbeans.modules.android.core.sdk.DalvikPlatform;
import org.netbeans.modules.android.core.sdk.DalvikPlatformManager;
import org.netbeans.modules.android.core.sdk.SdkLogProvider;
import org.netbeans.modules.android.project.AndroidIO;
import org.netbeans.modules.android.project.AndroidProject;
import org.netbeans.modules.android.project.AvdSelector;
import org.netbeans.modules.android.project.ui.customizer.DeviceChooserImpl;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.windows.InputOutput;

/**
 *
 * @author radim
 */
public class AndroidLauncherImpl implements AndroidLauncher {

  private static final Logger LOG = Logger.getLogger(AndroidLauncherImpl.class.getName());
  private static final RequestProcessor RP = new RequestProcessor(AndroidLauncherImpl.class);

  private static InputOutput io;

  public AndroidLauncherImpl() {
  }

  private static void initIO() {
    if (io == null) {
      io = AndroidIO.getDefaultIO();
    }
  }

  @Override
  public Future<Client> launch(DalvikPlatform platform, Lookup context, String mode) {
    initIO();
    LaunchConfiguration launchCfg = context.lookup(LaunchConfiguration.class);
    AvdSelector.LaunchData launchData = context.lookup(AvdSelector.LaunchData.class);
    if (launchData == null) {
      launchData = configAvd(platform.getSdkManager(), platform.getAndroidTarget(), launchCfg);
    }
    if (launchData == null) {
      LOG.log(Level.INFO, "No target AVD selected or found. Cancelling launch");
      return null;
    }
    // TODO check if we need to start emulator
    if (launchData.getDevice() == null) {
      LOG.log(Level.INFO, "Device not running. Launch emulator");
      // TODO det Future<int> and stop launch asynchronously(?) if the command fails
      Future<IDevice> toBeDevice = new EmulatorLauncher(DalvikPlatformManager.getDefault())
          .launchEmulator(launchData.getAvdInfo(), launchCfg);
      try {
        launchData = new AvdSelector.LaunchData(launchData.getAvdInfo(), toBeDevice.get());
      } catch (InterruptedException ex) {
        LOG.log(Level.INFO, null, ex);
        return null;
      } catch (ExecutionException ex) {
        LOG.log(Level.INFO, null, ex);
        return null;
      }
    }
    LaunchInfo info = context.lookup(LaunchInfo.class);
    if (!simpleLaunch(info, launchData.getDevice())) {
      return null;
    }
    LaunchAction launchAction = Preconditions.checkNotNull(context.lookup(LaunchAction.class));
    AndroidProject project = Preconditions.checkNotNull(context.lookup(AndroidProject.class));
    if (!launchAction.doLaunch(info, launchData.getDevice(), project)) {
      return null;
    }
    final RunnableFuture<Client> futureClient = new ClientFuture (launchData.getDevice(), info);
    RP.execute(futureClient);
    return futureClient;
  }

  /**
   * Evaluates target settings to find what AVD should be used and makes sure
   * that {@link AndroidProjectProperties.PROP_TARGET_PREFFERED_AVD is set for
   * Ant execution.
   * @return device or emulator description or {@code null}
   */
  @Override
  public AvdSelector.LaunchData configAvd(
      SdkManager sdkManager, IAndroidTarget target, LaunchConfiguration launchCfg) {
    try {
      LaunchConfiguration.TargetMode mode = launchCfg.getTargetMode();
      String targetAvd = null;
      // targetAvd = this.project.evaluator().getProperty(AndroidProjectProperties.PROP_TARGET_PREFFERED_AVD);

      final ISdkLog sdkLog = SdkLogProvider.createSdkLogger(true);
      final AvdManager avdMgr = new AvdManager(sdkManager, sdkLog);
      AvdSelector.AvdManagerMock avdMgrMock = new AvdSelector.AvdManagerMock() {

        @Override
        public void reloadAvds() throws AndroidLocationException {
          avdMgr.reloadAvds(sdkLog);
        }

        @Override
        public AvdInfo getAvd(String name, boolean validAvdOnly) {
          return avdMgr.getAvd(name, validAvdOnly);
        }

        @Override
        public AvdInfo[] getValidAvds() {
          return avdMgr.getValidAvds();
        }

        @Override
        public AvdManager getAvdManager() {
          return avdMgr;
        }
      };
      AndroidDebugBridge adb = AndroidDebugBridgeFactory.getDefault();
      if (adb == null) {
        LOG.log(Level.WARNING, "Android Debug Bridge is not configured. Cannot launch.");
        return null;
      }
      AvdSelector selector = new AvdSelector(
          mode,
          targetAvd,
          avdMgrMock,
          target,
          adb.getDevices());
      AvdSelector.LaunchData launch = selector.selectDevice(new DeviceChooserImpl());
      if (launch == null) {
        LOG.log(Level.INFO, "Launch cancelled");
        return null;
      }
      // TODO check if we need to start emulator
      if (launch.getDevice() == null) {
        LOG.log(Level.INFO, "Device not running. Launch emulator");
        // TODO det Future<int> and stop launch asynchronously(?) if the command fails
        Future<IDevice> toBeDevice = new EmulatorLauncher(DalvikPlatformManager.getDefault())
            .launchEmulator(launch.getAvdInfo(), launchCfg);
        try {
          launch = new AvdSelector.LaunchData(launch.getAvdInfo(), toBeDevice.get());
        } catch (InterruptedException ex) {
          LOG.log(Level.INFO, null, ex);
          return null;
        } catch (ExecutionException ex) {
          LOG.log(Level.INFO, null, ex);
          return null;
        }
      }
      return launch;
    } catch (AndroidLocationException ex) {
      Exceptions.printStackTrace(ex);
    }
    return null;
  }

  @Override
  public boolean simpleLaunch(LaunchInfo launchInfo, IDevice device) {
    // API level check
    if (!checkBuildInfo(launchInfo, device)) {
      LOG.log(Level.INFO, "Launch cancelled! Build info does not pass the check");
      stopLaunch(launchInfo);
      return false;
    }

    // sync the app
    if (!syncApp(launchInfo, device)) {
      LOG.log(Level.INFO, "Launch cancelled! Cannot sync the app");
      stopLaunch(launchInfo);
      return false;
    }

    return true;
  }

  private boolean checkBuildInfo(LaunchInfo launchInfo, IDevice device) {
    // TODO(radim): check IDevice.PROP_BUILD_* to find if we can deploy in this device
    return true;
  }

  private void stopLaunch(LaunchInfo launchInfo) {
    // seems no-op at the moment
  }

  private boolean syncApp(LaunchInfo launchInfo, IDevice device) {
    if (needsInstall(launchInfo, device)) {
      return doSyncApp(launchInfo, device);
    }
    return true;
  }

  private boolean needsInstall(LaunchInfo launchInfo, IDevice device) {
    // TODO(radim): check if it is already installed (or if we want to replace it)
    return true;
  }

  private boolean doSyncApp(LaunchInfo launchInfo, IDevice device) {
    try {
      // copy (upload) package to device
      if (device == null || launchInfo.packageFile == null) {
        LOG.log(Level.WARNING,
            "Cannot deploy the application. device = {0}, packageFile = {1}",
            new Object[] {device, launchInfo.packageFile});
        return false;
      }
      String remotePackagePath = device.syncPackageToDevice(launchInfo.packageFile.getPath());
      // install app
      boolean installResult = installPackage(launchInfo, remotePackagePath, device);
      // remove uploaded package
      device.removeRemotePackage(remotePackagePath);

      return installResult;
    } catch (InstallException e) {
      logEx(e, launchInfo, device);
    } catch (SyncException e) {
      logEx(e, launchInfo, device);
    } catch (AdbCommandRejectedException e) {
      logEx(e, launchInfo, device);
    } catch (TimeoutException e) {
      logEx(e, launchInfo, device);
    } catch (IOException e) {
      logEx(e, launchInfo, device);
    }
    return false;
  }

  private static void logEx(Exception e, LaunchInfo launchInfo, IDevice device) {
      LOG.log(Level.WARNING,
          "Failed to upload " + (launchInfo != null ? launchInfo.packageFile : "<no file>") +
          " on device " + (device != null ? device.getSerialNumber() : "<no device>"),
          e);
  }

  boolean installPackage(LaunchInfo launchInfo, String remotePackagePath, IDevice device) {
    initIO();
    try {
      String out = device.installRemotePackage(remotePackagePath, launchInfo.reinstall);
      if (out == null) {
        // success
        io.getOut().println("Package " + launchInfo.packageFile.getNameExt() + " deployed");
        return true;
      } else {
        io.getOut().println("Package deployment failed with: " + out);
        return false;
      }
    } catch (Exception ex) {
      if (launchInfo.reinstall) {
        try {
          // maybe it is not installed. try again clean install
          String out2 = device.installRemotePackage(remotePackagePath, false);
          if (out2 == null) {
            // success
            io.getOut().println("Package " + launchInfo.packageFile.getNameExt() + " deployed");
            return true;
          } else {
            io.getOut().println("Package deployment failed with: " + out2);
            return false;
          }
        } catch (Exception ex1) {
          Exceptions.printStackTrace(ex1);
        }
      }
      Exceptions.printStackTrace(ex);
      return false;
    }
  }

  private static final Pattern sAmErrorType = Pattern.compile("Error type (\\d+)");
  /**
   * Output receiver for am process (Activity Manager)
   *
   * Monitors adb output for am errors, and retries launch as appropriate.
   */
  public static class AMReceiver extends MultiLineReceiver {

    private final LaunchInfo launchInfo;
    private final IDevice device;

    /**
     * Basic constructor.
     *
     * @param launchInfo the {@link DelayedLaunchInfo} associated with the am process.
     * @param device the Android device on which the launch is done.
     * @param launchController the {@link ILaunchController} that is managing the launch
     */
    public AMReceiver(LaunchInfo launchInfo, IDevice device) {
      this.launchInfo = launchInfo;
      this.device = device;
    }

    /**
     * Monitors the am process for error messages. If an error occurs, will reattempt launch up to
     * <code>MAX_ATTEMPT_COUNT</code> times.
     *
     * @param lines a portion of the am output
     *
     * @see MultiLineReceiver#processNewLines(String[])
     */
    @Override
    public void processNewLines(String[] lines) {
      // first we check if one starts with error
      List<String> array = new ArrayList<String>();
      boolean error = false;
      boolean warning = false;
      for (String s : lines) {
        // ignore empty lines.
        if (s.length() == 0) {
          continue;
        }

        // check for errors that output an error type, if the attempt count is still
        // valid. If not the whole text will be output in the console
//        if (launchInfo.isCancelled() == false) {
          Matcher m = sAmErrorType.matcher(s);
          if (m.matches()) {
            // get the error type
            int type = Integer.parseInt(m.group(1));

            final int waitTime = 3;
            String msg;

            switch (type) {
              case 1:
              /* Intended fall through */
              case 2:
                msg = String.format(
                    "Device not ready in %d.",
                    waitTime);
                break;
              case 3:
                msg = String.format(
                    "New package not yet registered with the system  in %d.",
                    waitTime);
                break;
              default:
                msg = String.format(
                    "Device not ready  in %1d (%2$d).",
                    waitTime, type);
                break;

            }

            io.getOut().println(msg);

            // no need to parse the rest
            return;
          }
//        }

        // check for error if needed
        if (!error && s.startsWith("Error:")) {
          error = true;
        }
        if (!warning && s.startsWith("Warning:")) {
          warning = true;
        }

        // add the line to the list
        array.add("ActivityManager: " + s);
      }

      // then we display them in the console
      if (warning || error) {
        for (String l : array) {
          io.getErr().println(l);
        }
      } else {
        for (String l : array) {
          io.getOut().println(l);
        }
      }

      // if error then we cancel the launch, and remove the delayed info
//      if (error) {
//        mLaunchController.stopLaunch(mLaunchInfo);
//      }
    }
      /**
     * Returns true if launch has been cancelled
     */
    @Override
    public boolean isCancelled() {
      return false;
      // TODO implement cancelling
//        return launchInfo.isCancelled();
    }
}

  private static class ClientFuture implements RunnableFuture<Client> {

    private final AtomicBoolean canceled = new AtomicBoolean();
    private final AtomicReference<Client> result = new AtomicReference<Client>();
    private final CountDownLatch done = new CountDownLatch(1);
    private final IDevice device;
    private final LaunchInfo launchInfo;

    private ClientFuture(final IDevice device, final LaunchInfo launchInfo) {
      assert device != null;
      assert launchInfo != null;
      this.device = device;
      this.launchInfo = launchInfo;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
      canceled.set(true);
      return true;
    }

    @Override
    public boolean isCancelled() {
      return canceled.get();
    }

    @Override
    public boolean isDone() {
      return done.getCount() == 0;
    }

    @Override
    public Client get() throws InterruptedException, ExecutionException {
      if (canceled.get()) {
        throw new CancellationException();
      }
      done.await();
      return result.get();
    }

    @Override
    public Client get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, java.util.concurrent.TimeoutException {
      if (canceled.get()) {
        throw new CancellationException();
      }
      done.await(timeout, unit);
      return result.get();
    }

    @Override
    public void run() {
      while (!isCancelled()) {
        final Client client = device.getClient(launchInfo.manifestData.getPackage());
        if (client != null) {
            result.set(client);
            done.countDown();
            break;
        }
        //TODO: Busy waiting, replace by listener + Latch
        try {
          Thread.sleep(1000);
        } catch (InterruptedException ex) {
          Exceptions.printStackTrace(ex);
        }
      }
    }

  }
}
