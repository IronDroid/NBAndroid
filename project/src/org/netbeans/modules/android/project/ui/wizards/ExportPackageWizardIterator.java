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
package org.netbeans.modules.android.project.ui.wizards;

import java.awt.Component;
import java.util.NoSuchElementException;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;

public final class ExportPackageWizardIterator implements WizardDescriptor.Iterator {
  /** Name of Boolean property use in wizard. */
  public static final String PROP_USE_EXISTING_KEYSTORE = "useExistingKeystore";
  public static final String PROP_USE_EXISTING_ALIAS = "useExistingAlias";
  public static final String PROP_KEY_ALIAS_DNAME = "dName";
  public static final String PROP_KEY_ALIAS_VALIDITY = "validity";

  // To invoke this wizard, copy-paste and run the following code, e.g. from
  // SomeAction.performAction():
    /*
  WizardDescriptor.Iterator iterator = new ExportPackageWizardIterator();
  WizardDescriptor wizardDescriptor = new WizardDescriptor(iterator);
  // {0} will be replaced by WizardDescriptor.Panel.getComponent().getName()
  // {1} will be replaced by WizardDescriptor.Iterator.name()
  wizardDescriptor.setTitleFormat(new MessageFormat("{0} ({1})"));
  wizardDescriptor.setTitle("Your wizard dialog title here");
  Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
  dialog.setVisible(true);
  dialog.toFront();
  boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
  if (!cancelled) {
  // do something
  }
   */
  private int index;
  private WizardDescriptor.Panel[] allPanels;

  /**
   * Initialize panels representing individual wizard's steps and sets
   * various properties for them influencing wizard appearance.
   */
  private WizardDescriptor.Panel[] getPanels() {
    if (allPanels == null) {
      allPanels = new WizardDescriptor.Panel[]{
        new ExportPackageKeystoreWizardPanel(),
        new ExportPackageAliasWizardPanel(),
        new ExportPackageCreateKeyWizardPanel(),
        new ExportPackageConfirmWizardPanel()
      };
      String[] steps = new String[allPanels.length];
      for (int i = 0; i < allPanels.length; i++) {
        Component c = allPanels[i].getComponent();
        // Default step name to component name of panel.
        steps[i] = c.getName();
        if (c instanceof JComponent) { // assume Swing components
          JComponent jc = (JComponent) c;
          // Sets step number of a component
          // TODO if using org.openide.dialogs >= 7.8, can use WizardDescriptor.PROP_*:
          jc.putClientProperty("WizardPanel_contentSelectedIndex", Integer.valueOf(i));
          // Sets steps names for a panel
          jc.putClientProperty("WizardPanel_contentData", steps);
          // Turn on subtitle creation on each step
          jc.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE);
          // Show steps on the left side with the image on the background
          jc.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE);
          // Turn on numbering of all steps
          jc.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE);
        }
      }
    }
    return allPanels;
  }

  public WizardDescriptor.Panel current() {
    return getPanels()[index];
  }

  public String name() {
    return index + 1 + ". from " + getPanels().length;
  }

  public boolean hasNext() {
    return index < getPanels().length - 1;
  }

  public boolean hasPrevious() {
    return index > 0;
  }

  public void nextPanel() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    index++;
    boolean useExistingKey = 
        ((ExportPackageAliasWizardPanel) allPanels[1]).getComponent().useExistingAlias();
    if (index == 2 && useExistingKey) {
      index++;
    }
  }

  public void previousPanel() {
    if (!hasPrevious()) {
      throw new NoSuchElementException();
    }
    index--;
    boolean useExistingKey = 
        ((ExportPackageAliasWizardPanel) allPanels[1]).getComponent().useExistingAlias();
    if (index == 3 && useExistingKey) {
      index--;
    }
  }

  // If nothing unusual changes in the middle of the wizard, simply:
  public void addChangeListener(ChangeListener l) {
  }

  public void removeChangeListener(ChangeListener l) {
  }
  // If something changes dynamically (besides moving between panels), e.g.
  // the number of panels changes in response to user input, then uncomment
  // the following and call when needed: fireChangeEvent();
    /*
  private Set<ChangeListener> listeners = new HashSet<ChangeListener>(1); // or can use ChangeSupport in NB 6.0
  public final void addChangeListener(ChangeListener l) {
  synchronized (listeners) {
  listeners.add(l);
  }
  }
  public final void removeChangeListener(ChangeListener l) {
  synchronized (listeners) {
  listeners.remove(l);
  }
  }
  protected final void fireChangeEvent() {
  Iterator<ChangeListener> it;
  synchronized (listeners) {
  it = new HashSet<ChangeListener>(listeners).iterator();
  }
  ChangeEvent ev = new ChangeEvent(this);
  while (it.hasNext()) {
  it.next().stateChanged(ev);
  }
  }
   */
}
