/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.android.project.ui.customizer;

import java.util.List;
import com.android.sdklib.AndroidVersion;
import com.android.sdklib.IAndroidTarget;
import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author radim
 */
public class AndroidTargetTableModelTest {

  private AndroidTargetTableModel model;
  private IAndroidTarget t1;
  private IAndroidTarget t2;

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() {
    t1 = mock(IAndroidTarget.class);
    when(t1.getName()).thenReturn("Android 1.1");
    when(t1.getVendor()).thenReturn("Android Open Source Project");
    when(t1.getVersionName()).thenReturn("1.1");
    when(t1.getVersion()).thenReturn(new AndroidVersion(2, null));

    t2 = mock(IAndroidTarget.class);
    when(t2.getName()).thenReturn("Google APIs");
    when(t2.getVendor()).thenReturn("Google Inc.");
    when(t2.getVersionName()).thenReturn("1.5");
    when(t2.getVersion()).thenReturn(new AndroidVersion(3, null));

    List<IAndroidTarget> targets = new ArrayList<IAndroidTarget>();
    targets.add(t1);
    targets.add(t2);
    model = new AndroidTargetTableModel(targets);
  }

  @After
  public void tearDown() {
  }

  @Test
  public void testSomeMethod() {
    assertEquals(4, model.getColumnCount());
    assertEquals(2, model.getRowCount());
    assertEquals("Android 1.1", model.getValueAt(0, AndroidTargetTableModel.COLUMN_TARGET));
    assertEquals("Android Open Source Project", model.getValueAt(0, AndroidTargetTableModel.COLUMN_VENDOR));
    assertEquals("1.1", model.getValueAt(0, AndroidTargetTableModel.COLUMN_PLATFORM));
    assertEquals("2", model.getValueAt(0, AndroidTargetTableModel.COLUMN_API_LEVEL));
  }

}