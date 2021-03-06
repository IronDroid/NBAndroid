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

import com.google.common.base.Preconditions;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.android.project.ui.customizer.AndroidProjectProperties;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

public final class ExportPackageKeystoreVisualPanel extends SettingsPanel {

  private String errorMsg = null;
  private boolean valid = true;
  private final ExportPackageKeystoreWizardPanel wizPanel;

  /** Creates new form ExportPackageVisualPanel1 */
  public ExportPackageKeystoreVisualPanel(ExportPackageKeystoreWizardPanel wizPanel) {
    this.wizPanel = Preconditions.checkNotNull(wizPanel);
    initComponents();
    final DocumentListener documentListener = new DocumentListener() {

      @Override
      public void insertUpdate(DocumentEvent e) {
        onUpdate();
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        onUpdate();
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        onUpdate();
      }
    };
    jTextFieldKeystoreLocation.getDocument().addDocumentListener(documentListener);
    jPasswordFieldKeystorePasswd.getDocument().addDocumentListener(documentListener);
    jPasswordFieldPasswdConfirm.getDocument().addDocumentListener(documentListener);
  }

  @Override
  public String getName() {
    return "Keystore selection";
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    buttonGroup = new javax.swing.ButtonGroup();
    jRadioButtonNewKeystore = new javax.swing.JRadioButton();
    jRadioButtonExistingKeystore = new javax.swing.JRadioButton();
    jLabelKeystoreLocation = new javax.swing.JLabel();
    jTextFieldKeystoreLocation = new javax.swing.JTextField();
    jButtonKeystoreLocationBrowse = new javax.swing.JButton();
    jLabelKeystorePasswd = new javax.swing.JLabel();
    jPasswordFieldKeystorePasswd = new javax.swing.JPasswordField();
    jLabelPasswdConfirm = new javax.swing.JLabel();
    jPasswordFieldPasswdConfirm = new javax.swing.JPasswordField();

    buttonGroup.add(jRadioButtonNewKeystore);
    org.openide.awt.Mnemonics.setLocalizedText(jRadioButtonNewKeystore, org.openide.util.NbBundle.getMessage(ExportPackageKeystoreVisualPanel.class, "ExportPackageKeystoreVisualPanel.jRadioButtonNewKeystore.text")); // NOI18N
    jRadioButtonNewKeystore.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jRadioButtonNewKeystoreActionPerformed(evt);
      }
    });

    buttonGroup.add(jRadioButtonExistingKeystore);
    org.openide.awt.Mnemonics.setLocalizedText(jRadioButtonExistingKeystore, org.openide.util.NbBundle.getMessage(ExportPackageKeystoreVisualPanel.class, "ExportPackageKeystoreVisualPanel.jRadioButtonExistingKeystore.text")); // NOI18N
    jRadioButtonExistingKeystore.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jRadioButtonExistingKeystoreActionPerformed(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(jLabelKeystoreLocation, org.openide.util.NbBundle.getMessage(ExportPackageKeystoreVisualPanel.class, "ExportPackageKeystoreVisualPanel.jLabelKeystoreLocation.text")); // NOI18N

    jTextFieldKeystoreLocation.setText(org.openide.util.NbBundle.getMessage(ExportPackageKeystoreVisualPanel.class, "ExportPackageKeystoreVisualPanel.jTextFieldKeystoreLocation.text")); // NOI18N
    jTextFieldKeystoreLocation.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jTextFieldKeystoreLocationActionPerformed(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(jButtonKeystoreLocationBrowse, org.openide.util.NbBundle.getMessage(ExportPackageKeystoreVisualPanel.class, "ExportPackageKeystoreVisualPanel.jButtonKeystoreLocationBrowse.text")); // NOI18N
    jButtonKeystoreLocationBrowse.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButtonKeystoreLocationBrowseActionPerformed(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(jLabelKeystorePasswd, org.openide.util.NbBundle.getMessage(ExportPackageKeystoreVisualPanel.class, "ExportPackageKeystoreVisualPanel.jLabelKeystorePasswd.text")); // NOI18N

    jPasswordFieldKeystorePasswd.setText(org.openide.util.NbBundle.getMessage(ExportPackageKeystoreVisualPanel.class, "ExportPackageKeystoreVisualPanel.jPasswordFieldKeystorePasswd.text")); // NOI18N
    jPasswordFieldKeystorePasswd.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jPasswordFieldKeystorePasswdActionPerformed(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(jLabelPasswdConfirm, org.openide.util.NbBundle.getMessage(ExportPackageKeystoreVisualPanel.class, "ExportPackageKeystoreVisualPanel.jLabelPasswdConfirm.text")); // NOI18N

    jPasswordFieldPasswdConfirm.setText(org.openide.util.NbBundle.getMessage(ExportPackageKeystoreVisualPanel.class, "ExportPackageKeystoreVisualPanel.jPasswordFieldPasswdConfirm.text")); // NOI18N
    jPasswordFieldPasswdConfirm.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jPasswordFieldPasswdConfirmActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jRadioButtonNewKeystore)
          .addComponent(jRadioButtonExistingKeystore)
          .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(jLabelKeystoreLocation)
              .addComponent(jLabelKeystorePasswd)
              .addComponent(jLabelPasswdConfirm))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(jPasswordFieldPasswdConfirm, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
              .addComponent(jPasswordFieldKeystorePasswd, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
              .addComponent(jTextFieldKeystoreLocation, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jButtonKeystoreLocationBrowse)))
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(jRadioButtonNewKeystore)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jRadioButtonExistingKeystore)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabelKeystoreLocation)
          .addComponent(jTextFieldKeystoreLocation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jButtonKeystoreLocationBrowse))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabelKeystorePasswd)
          .addComponent(jPasswordFieldKeystorePasswd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabelPasswdConfirm)
          .addComponent(jPasswordFieldPasswdConfirm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addContainerGap(134, Short.MAX_VALUE))
    );
  }// </editor-fold>//GEN-END:initComponents

  private void jRadioButtonNewKeystoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonNewKeystoreActionPerformed
    onUpdate();
  }//GEN-LAST:event_jRadioButtonNewKeystoreActionPerformed

  private void jRadioButtonExistingKeystoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonExistingKeystoreActionPerformed
    onUpdate();
  }//GEN-LAST:event_jRadioButtonExistingKeystoreActionPerformed

  private void jTextFieldKeystoreLocationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldKeystoreLocationActionPerformed
    onUpdate();
  }//GEN-LAST:event_jTextFieldKeystoreLocationActionPerformed

  private void jPasswordFieldKeystorePasswdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPasswordFieldKeystorePasswdActionPerformed
    onUpdate();
  }//GEN-LAST:event_jPasswordFieldKeystorePasswdActionPerformed

  private void jPasswordFieldPasswdConfirmActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPasswordFieldPasswdConfirmActionPerformed
    onUpdate();
  }//GEN-LAST:event_jPasswordFieldPasswdConfirmActionPerformed

  private void jButtonKeystoreLocationBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonKeystoreLocationBrowseActionPerformed
    JFileChooser chooser = new JFileChooser();
    FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
    chooser.setDialogTitle(NbBundle.getMessage(PanelProjectLocationVisual.class, "LBL_SelectKeystoreLocation"));
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    String path = jTextFieldKeystoreLocation.getText();
    if (path.length() > 0) {
      File f = new File(path);
      if (f.exists()) {
        chooser.setSelectedFile(f);
      }
    }
    if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) { //NOI18N
      File projectDir = chooser.getSelectedFile();
      jTextFieldKeystoreLocation.setText(FileUtil.normalizeFile(projectDir).getAbsolutePath());
    }
  }//GEN-LAST:event_jButtonKeystoreLocationBrowseActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.ButtonGroup buttonGroup;
  private javax.swing.JButton jButtonKeystoreLocationBrowse;
  private javax.swing.JLabel jLabelKeystoreLocation;
  private javax.swing.JLabel jLabelKeystorePasswd;
  private javax.swing.JLabel jLabelPasswdConfirm;
  private javax.swing.JPasswordField jPasswordFieldKeystorePasswd;
  private javax.swing.JPasswordField jPasswordFieldPasswdConfirm;
  private javax.swing.JRadioButton jRadioButtonExistingKeystore;
  private javax.swing.JRadioButton jRadioButtonNewKeystore;
  private javax.swing.JTextField jTextFieldKeystoreLocation;
  // End of variables declaration//GEN-END:variables

  @Override
  void store(WizardDescriptor settings) {
    settings.putProperty(
        AndroidProjectProperties.PROP_KEY_STORE, jTextFieldKeystoreLocation.getText());
    settings.putProperty(AndroidProjectProperties.PROP_KEY_STORE_PASSWD, 
        new String(jPasswordFieldKeystorePasswd.getPassword()));
    settings.putProperty(ExportPackageWizardIterator.PROP_USE_EXISTING_KEYSTORE, 
        Boolean.valueOf(jRadioButtonExistingKeystore.isSelected()));
  }

  @Override
  void read(WizardDescriptor settings) {
    jTextFieldKeystoreLocation.setText(
        (String) settings.getProperty(AndroidProjectProperties.PROP_KEY_STORE));
    jPasswordFieldKeystorePasswd.setText(
        (String) settings.getProperty(AndroidProjectProperties.PROP_KEY_STORE_PASSWD));
    jRadioButtonExistingKeystore.setSelected(
        (Boolean) settings.getProperty(ExportPackageWizardIterator.PROP_USE_EXISTING_KEYSTORE));
    jRadioButtonNewKeystore.setSelected(
        !((Boolean) settings.getProperty(ExportPackageWizardIterator.PROP_USE_EXISTING_KEYSTORE)).booleanValue());
    onUpdate();
  }

  private void onUpdate() {
    wizPanel.onChange();
    jPasswordFieldPasswdConfirm.setEnabled(jRadioButtonNewKeystore.isSelected());
  }

  @Override
  boolean valid(WizardDescriptor settings) {
    settings.putProperty("WizardPanel_errorMessage", !valid ? errorMsg : null);
    return this.valid;
  }

  @Override
  void validate(WizardDescriptor settings) {
    valid = false;

    boolean createStore = !jRadioButtonExistingKeystore.isSelected();

    // checks the keystore path is non null.
    String keystore = jTextFieldKeystoreLocation.getText().trim();
    if (keystore.length() == 0) {
      errorMsg = "Enter path to keystore.";
      return;
    } else {
      File f = new File(keystore);
      if (f.exists() == false) {
        if (createStore == false) {
          errorMsg = "Keystore does not exist.";
          return;
        }
      } else if (f.isDirectory()) {
        errorMsg = "Keystore path is a directory.";
        return;
      } else if (f.isFile()) {
        if (createStore) {
          errorMsg = "File already exists.";
          return;
        }
      }
    }

    String value = new String(jPasswordFieldKeystorePasswd.getPassword());
    if (value.length() == 0) {
      errorMsg = "Enter keystore password.";
      return;
    } else if (createStore && value.length() < 6) {
      errorMsg = "Keystore password is too short - must be at least 6 characters.";
      return;
    }

    String value2 = new String(jPasswordFieldPasswdConfirm.getPassword());
    if (createStore) {
      if (value2.length() == 0) {
        errorMsg = "Confirm keystore password.";
        return;
      }

      if (!value.equals(value2)) {
        errorMsg = "Keystore passwords do not match.";
        return;
      }
    }

    valid = true;
  }
}
