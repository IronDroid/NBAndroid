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
package org.netbeans.modules.android.project.configs;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.netbeans.modules.android.project.ui.customizer.AndroidProjectProperties;
import java.util.Collections;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.Map;
import org.netbeans.modules.android.project.launch.LaunchConfiguration;
import org.openide.util.lookup.Lookups;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.modules.android.project.AndroidProject;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.netbeans.modules.android.project.SavableAuxiliaryProperties;

/**
 *
 * @author radim
 */
public class AndroidConfigProviderTest {

  AndroidProject project;
  APImpl aprops;

  @Before
  public void init() {
    project = mock(AndroidProject.class);
    Map<String, String> props = Maps.newHashMap();
    aprops = new APImpl(props);
    when(project.getLookup()).thenReturn(Lookups.fixed(aprops));
  }

  @Test
  public void testHasDefaultConfig() {
    // start with nothing and there has to be config
    AndroidConfigProvider configs = new AndroidConfigProvider(project, aprops);
    Collection result = configs.getConfigurations();
    assertEquals(1, result.size());
    assertNotNull(configs.getActiveConfiguration());

    LaunchConfiguration launch = configs.getActiveConfiguration().getLaunchConfiguration();
    assertNotNull(launch);
    assertEquals(LaunchConfiguration.Action.MAIN, launch.getLaunchAction());

    // .. modify it and set. check that the change is applied
    configs.setConfigurations(Collections.singletonList(
        ConfigBuilder.builderForConfig(configs.getActiveConfiguration())
            .withLaunchAction(LaunchConfiguration.Action.DO_NOTHING)
            .config()));
    launch = configs.getActiveConfiguration().getLaunchConfiguration();
    assertNotNull(launch);
    assertEquals(LaunchConfiguration.Action.DO_NOTHING, launch.getLaunchAction());

    // .. another change
    configs.setConfigurations(Collections.singletonList(
        ConfigBuilder.builderForConfig(configs.getActiveConfiguration())
            .withLaunchAction(LaunchConfiguration.Action.ACTIVITY)
            .withActivityName("foo")
            .config()));
    launch = configs.getActiveConfiguration().getLaunchConfiguration();
    assertNotNull(launch);
    assertEquals(LaunchConfiguration.Action.ACTIVITY, launch.getLaunchAction());
    assertEquals("foo", launch.getActivityName());
  }

  @Test
  public void testForProject_NoAction() {
    aprops.props.put(AndroidProjectProperties.PROP_CONFIG_NAMES, AndroidConfigProvider.DEFAULT_CONFIG_NAME);
    aprops.props.put(AndroidProjectProperties.PROP_LAUNCH_ACTION + "." + AndroidConfigProvider.DEFAULT_CONFIG_NAME,
        AndroidProjectProperties.LAUNCH_ACTION_DO_NOTHING);

    AndroidConfigProvider configs = new AndroidConfigProvider(project, aprops);
    Collection result = configs.getConfigurations();
    assertEquals(1, result.size());
    assertNotNull(configs.getActiveConfiguration());

    LaunchConfiguration launch = configs.getActiveConfiguration().getLaunchConfiguration();
    assertNotNull(launch);
    assertEquals(LaunchConfiguration.Action.DO_NOTHING, launch.getLaunchAction());
  }

  @Test
  public void testForProject_Activity() {
    String activityName = "org.foo.app.SomeActivity";
    aprops.props.put(AndroidProjectProperties.PROP_CONFIG_NAMES, AndroidConfigProvider.DEFAULT_CONFIG_NAME);
    aprops.props.put(AndroidProjectProperties.PROP_LAUNCH_ACTION + "." + AndroidConfigProvider.DEFAULT_CONFIG_NAME,
        activityName);

    AndroidConfigProvider configs = new AndroidConfigProvider(project, aprops);
    Collection result = configs.getConfigurations();
    assertEquals(1, result.size());
    assertNotNull(configs.getActiveConfiguration());

    LaunchConfiguration launch = configs.getActiveConfiguration().getLaunchConfiguration();
    assertNotNull(launch);
    assertEquals(LaunchConfiguration.Action.ACTIVITY, launch.getLaunchAction());
    assertEquals(activityName, launch.getActivityName());
  }

  @Test
  public void buildModes() {
    AndroidConfigProvider configs = new AndroidConfigProvider(project, aprops);
    assertEquals(1, configs.getConfigurations().size());
    LaunchConfiguration launch = configs.getActiveConfiguration().getLaunchConfiguration();
    assertEquals(LaunchConfiguration.MODE_DEBUG, launch.getMode());
  }

  @Test
  public void buildModes2() {
    aprops.props.put(AndroidProjectProperties.PROP_CONFIG_NAMES, AndroidConfigProvider.DEFAULT_CONFIG_NAME);
    aprops.props.put(AndroidProjectProperties.PROP_LAUNCH_MODE + "." + AndroidConfigProvider.DEFAULT_CONFIG_NAME,
        LaunchConfiguration.MODE_DEBUG);

    AndroidConfigProvider configs = new AndroidConfigProvider(project, aprops);
    assertEquals(1, configs.getConfigurations().size());
    LaunchConfiguration launch = configs.getActiveConfiguration().getLaunchConfiguration();
    assertEquals(LaunchConfiguration.MODE_DEBUG, launch.getMode());
  }

  @Test
  public void buildModes3() {
    aprops.props.put(AndroidProjectProperties.PROP_CONFIG_NAMES, AndroidConfigProvider.DEFAULT_CONFIG_NAME);
    aprops.props.put(AndroidProjectProperties.PROP_LAUNCH_MODE + "." + AndroidConfigProvider.DEFAULT_CONFIG_NAME,
        LaunchConfiguration.MODE_RELEASE);

    AndroidConfigProvider configs = new AndroidConfigProvider(project, aprops);
    assertEquals(1, configs.getConfigurations().size());
    LaunchConfiguration launch = configs.getActiveConfiguration().getLaunchConfiguration();
    assertEquals(LaunchConfiguration.MODE_RELEASE, launch.getMode());
  }

  @Test
  public void targetModes() {
    aprops.props.put(AndroidProjectProperties.PROP_CONFIG_NAMES, AndroidConfigProvider.DEFAULT_CONFIG_NAME);
    aprops.props.put(AndroidProjectProperties.PROP_LAUNCH_TARGET_MODE + "." + AndroidConfigProvider.DEFAULT_CONFIG_NAME,
        LaunchConfiguration.TargetMode.AUTO.toString());
    AndroidConfigProvider configs = new AndroidConfigProvider(project, aprops);
    assertEquals(1, configs.getConfigurations().size());
    LaunchConfiguration launch = configs.getActiveConfiguration().getLaunchConfiguration();
    assertEquals(LaunchConfiguration.TargetMode.AUTO, launch.getTargetMode());
  }

  @Test
  public void targetModes2() {
    aprops.props.put(AndroidProjectProperties.PROP_CONFIG_NAMES, AndroidConfigProvider.DEFAULT_CONFIG_NAME);
    aprops.props.put(AndroidProjectProperties.PROP_LAUNCH_TARGET_MODE + "." + AndroidConfigProvider.DEFAULT_CONFIG_NAME,
        LaunchConfiguration.TargetMode.MANUAL.toString());
    AndroidConfigProvider configs = new AndroidConfigProvider(project, aprops);
    assertEquals(1, configs.getConfigurations().size());
    LaunchConfiguration launch = configs.getActiveConfiguration().getLaunchConfiguration();
    assertEquals(LaunchConfiguration.TargetMode.MANUAL, launch.getTargetMode());
  }

  @Test
  public void saveLaunchConfig() {
    AndroidConfigProvider configs = new AndroidConfigProvider(project, aprops);
    configs.setConfigurations(Collections.singletonList(
        ConfigBuilder.builderForConfig(configs.getActiveConfiguration())
            .withMode(LaunchConfiguration.MODE_RELEASE)
            .config()));
    assertEquals(LaunchConfiguration.MODE_RELEASE,
        configs.getActiveConfiguration().getLaunchConfiguration().getMode());
    configs.save();

    AndroidConfigProvider configs2 = new AndroidConfigProvider(project, aprops);
    assertEquals(LaunchConfiguration.MODE_RELEASE,
        configs2.getActiveConfiguration().getLaunchConfiguration().getMode());
  }

  @Test
  public void saveLaunchConfig_targetMode() {
    AndroidConfigProvider configs = new AndroidConfigProvider(project, aprops);
    configs.setConfigurations(Collections.singletonList(
        ConfigBuilder.builderForConfig(configs.getActiveConfiguration())
            .withTargetMode(LaunchConfiguration.TargetMode.MANUAL)
            .config()));
    assertEquals(LaunchConfiguration.TargetMode.MANUAL,
        configs.getActiveConfiguration().getLaunchConfiguration().getTargetMode());
    configs.save();

    AndroidConfigProvider configs2 = new AndroidConfigProvider(project, aprops);
    assertEquals(LaunchConfiguration.TargetMode.MANUAL,
        configs2.getActiveConfiguration().getLaunchConfiguration().getTargetMode());
  }

  @Test
  public void saveLaunchConfig_emulatorOptions() {
    final String OPTIONS = "--foo";
    AndroidConfigProvider configs = new AndroidConfigProvider(project, aprops);
    configs.setConfigurations(Collections.singletonList(
        ConfigBuilder.builderForConfig(configs.getActiveConfiguration())
            .withEmulatorOption(OPTIONS)
            .config()));
    assertEquals(OPTIONS,
        configs.getActiveConfiguration().getLaunchConfiguration().getEmulatorOptions());
    configs.save();

    AndroidConfigProvider configs2 = new AndroidConfigProvider(project, aprops);
    assertEquals(OPTIONS,
        configs2.getActiveConfiguration().getLaunchConfiguration().getEmulatorOptions());
  }

  @Test
  public void setActiveConfig() throws IOException {
    AndroidConfigProvider configs = new AndroidConfigProvider(project, aprops);
    AndroidConfigProvider.Config defaultCfg = ConfigBuilder.builderForConfig(configs.getActiveConfiguration())
            .config();
    AndroidConfigProvider.Config secondCfg = ConfigBuilder.builderForConfig(configs.getActiveConfiguration())
            .withName("newconfig")
            .config();
    configs.setConfigurations(Lists.newArrayList(defaultCfg, secondCfg));
    configs.setActiveConfiguration(secondCfg);
    assertEquals(secondCfg, configs.getActiveConfiguration());
    configs.setActiveConfiguration(defaultCfg);
    assertEquals(defaultCfg, configs.getActiveConfiguration());
  }

  private static class APImpl implements SavableAuxiliaryProperties {

    private Map<String, String> props;

    public APImpl(Map<String, String> props) {
      this.props = props;
    }

    @Override
    public String get(String key, boolean shared) {
      Preconditions.checkState(!shared, "only non-shared properties are expected");
      return props.get(key);
    }

    @Override
    public void put(String key, String value, boolean shared) {
      Preconditions.checkState(!shared, "only non-shared properties are expected");
      if (value != null) {
        props.put(key, value);
      } else {
        props.remove(key);
      }
    }

    @Override
    public Iterable<String> listKeys(boolean shared) {
      Preconditions.checkState(!shared, "only non-shared properties are expected");
      return props.keySet();
    }

    @Override
    public void save() {
      // no-op1
    }
  }
}
