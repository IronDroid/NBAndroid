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

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.android.ddmlib.testrunner.ITestRunListener;
import com.android.ddmlib.testrunner.RemoteAndroidTestRunner;
import com.android.ddmlib.testrunner.TestIdentifier;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.android.project.AndroidIO;
import org.netbeans.modules.android.project.AndroidProject;
import org.netbeans.modules.android.project.api.TestOutputConsumer;
import org.netbeans.modules.android.project.ui.customizer.AndroidProjectProperties;
import org.openide.windows.InputOutput;

/**
 * Launch strategy to run test(s).
 *
 * @author radim
 */
class TestLaunchAction implements LaunchAction {
  private static final Logger LOG = Logger.getLogger(TestLaunchAction.class.getName());

  @Override
  public boolean doLaunch(LaunchInfo launchInfo, IDevice device, AndroidProject project) {
    Preconditions.checkNotNull(project);
    InputOutput io = AndroidIO.getDefaultIO();
    RemoteAndroidTestRunner runner = new RemoteAndroidTestRunner(launchInfo.manifestData.getPackage(),
            AndroidProjectProperties.INSTR_RUNNER_DEFAULT, device);

//    if (mLaunchInfo.getTestClass() != null) {
//        if (mLaunchInfo.getTestMethod() != null) {
//            runner.setMethodName(mLaunchInfo.getTestClass(), mLaunchInfo.getTestMethod());
//        } else {
//            runner.setClassName(mLaunchInfo.getTestClass());
//        }
//    }
//
//    if (mLaunchInfo.getTestPackage() != null) {
//        runner.setTestPackageName(mLaunchInfo.getTestPackage());
//    }
    runner.setDebug(launchInfo.debug);

    try {
      // now we actually launch the app.
      io.getOut().println("Starting tests on device " + device);
      runner.run(new TestRunListener(project));

    } catch (TimeoutException ex) {
      io.getErr().println("Launch error: timeout");
      LOG.log(Level.INFO, null, ex);
    } catch (AdbCommandRejectedException ex) {
      io.getErr().println("Launch error: adb rejected command: " + ex.getMessage());
      LOG.log(Level.INFO, null, ex);
    } catch (ShellCommandUnresponsiveException ex) {
      io.getErr().println(MessageFormat.format("Unresponsive shell when executing tests: {0}",
          ex.getMessage()));
      LOG.log(Level.INFO, null, ex);
    } catch (IOException ex) {
      io.getErr().println("Test launch error: " + ex.getMessage());
      LOG.log(Level.INFO, null, ex);
    }
    return true;
  }

  private static class TestRunListener implements ITestRunListener {
    private Iterable<ITestRunListener> delegates;
    private final ITestRunListener simpleLsnr = new SimpleTestRunListener();

    public TestRunListener(final AndroidProject project) {
      Preconditions.checkNotNull(project);
      delegates = Lists.newArrayList(Iterables.concat(
               Collections.singleton(simpleLsnr), 
               Iterables.transform(
                   project.getLookup().lookupAll(TestOutputConsumer.class),
                   new Function<TestOutputConsumer, ITestRunListener>() {

                     @Override
                     public ITestRunListener apply(TestOutputConsumer input) {
                       return input.createTestListener(project);
                     }
                   })));
      LOG.log(Level.FINE, "Sending test output to {0}", Iterables.toString(delegates));
    }
    @Override
    public void testRunStarted(String string, int testCount) {
      for (ITestRunListener lsnr : delegates) {
        lsnr.testRunStarted(string, testCount);
      }
    }

    @Override
    public void testStarted(TestIdentifier ti) {
      for (ITestRunListener lsnr : delegates) {
        lsnr.testStarted(ti);
      }
    }

    @Override
    public void testFailed(TestFailure tf, TestIdentifier ti, String trace) {
      for (ITestRunListener lsnr : delegates) {
        lsnr.testFailed(tf, ti, trace);
      }
    }

    @Override
    public void testEnded(TestIdentifier ti, Map<String, String> map) {
      for (ITestRunListener lsnr : delegates) {
        lsnr.testEnded(ti, map);
      }
    }

    @Override
    public void testRunFailed(String string) {
      for (ITestRunListener lsnr : delegates) {
        lsnr.testRunFailed(string);
      }
    }

    @Override
    public void testRunStopped(long timeElapsed) {
      for (ITestRunListener lsnr : delegates) {
        lsnr.testRunStopped(timeElapsed);
      }
    }

    @Override
    public void testRunEnded(long timeElapsed, Map<String, String> map) {
      for (ITestRunListener lsnr : delegates) {
        lsnr.testRunEnded(timeElapsed, map);
      }
    }
  }

  private static class SimpleTestRunListener implements ITestRunListener {
    InputOutput io = AndroidIO.getDefaultIO();

    @Override
    public void testRunStarted(String string, int testCount) {
      io.getOut().println("testRunStarted " + string + ", " + testCount);
    }

    @Override
    public void testStarted(TestIdentifier ti) {
      io.getOut().println("testStarted " + ti);
    }

    @Override
    public void testFailed(TestFailure tf, TestIdentifier ti, String trace) {
      io.getErr().println("testFailed " + tf + ", " + ti + ", " + trace);
    }

    @Override
    public void testEnded(TestIdentifier ti, Map<String, String> map) {
      io.getOut().println("testEnded " + ti + ", " + map);
    }

    @Override
    public void testRunFailed(String string) {
      io.getErr().println("testRunFailed " + string);
    }

    @Override
    public void testRunStopped(long timeElapsed) {
      io.getErr().println("testRunStopped " + timeElapsed);
    }

    @Override
    public void testRunEnded(long timeElapsed, Map<String, String> map) {
      io.getOut().println("testRunEnded " + timeElapsed + ", " + map);
    }

  }
}
