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
import com.google.common.io.Closeables;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.android.project.ui.customizer.AndroidProjectProperties;
import org.openide.WizardDescriptor;
import org.openide.util.Enumerations;

public final class ExportPackageAliasVisualPanel extends SettingsPanel {

  private String errorMsg = null;
  private Exception exc;
  private boolean valid = true;
  private final ExportPackageAliasWizardPanel wizPanel;

  /** Creates new form ExportPackageVisualPanel2 */
  public ExportPackageAliasVisualPanel(ExportPackageAliasWizardPanel wizPanel) {
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
    jPasswordField.getDocument().addDocumentListener(documentListener);
  }

  @Override
  public String getName() {
    return "Alias selection";
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    buttonGroup1 = new javax.swing.ButtonGroup();
    jRadioButtonUseKey = new javax.swing.JRadioButton();
    jComboBoxAliases = new javax.swing.JComboBox();
    jLabelAliases = new javax.swing.JLabel();
    jLabelPasswd = new javax.swing.JLabel();
    jPasswordField = new javax.swing.JPasswordField();
    jRadioButtonAddKey = new javax.swing.JRadioButton();

    buttonGroup1.add(jRadioButtonUseKey);
    jRadioButtonUseKey.setSelected(true);
    org.openide.awt.Mnemonics.setLocalizedText(jRadioButtonUseKey, org.openide.util.NbBundle.getMessage(ExportPackageAliasVisualPanel.class, "ExportPackageAliasVisualPanel.jRadioButtonUseKey.text")); // NOI18N
    jRadioButtonUseKey.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jRadioButtonUseKeyActionPerformed(evt);
      }
    });

    jComboBoxAliases.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jComboBoxAliasesActionPerformed(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(jLabelAliases, org.openide.util.NbBundle.getMessage(ExportPackageAliasVisualPanel.class, "ExportPackageAliasVisualPanel.jLabelAliases.text")); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(jLabelPasswd, org.openide.util.NbBundle.getMessage(ExportPackageAliasVisualPanel.class, "ExportPackageAliasVisualPanel.jLabelPasswd.text")); // NOI18N

    jPasswordField.setText(org.openide.util.NbBundle.getMessage(ExportPackageAliasVisualPanel.class, "ExportPackageAliasVisualPanel.jPasswordField.text")); // NOI18N
    jPasswordField.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jPasswordFieldActionPerformed(evt);
      }
    });

    buttonGroup1.add(jRadioButtonAddKey);
    org.openide.awt.Mnemonics.setLocalizedText(jRadioButtonAddKey, org.openide.util.NbBundle.getMessage(ExportPackageAliasVisualPanel.class, "ExportPackageAliasVisualPanel.jRadioButtonAddKey.text")); // NOI18N
    jRadioButtonAddKey.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jRadioButtonAddKeyActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jRadioButtonUseKey)
          .addGroup(layout.createSequentialGroup()
            .addGap(12, 12, 12)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(layout.createSequentialGroup()
                .addComponent(jLabelPasswd)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPasswordField, javax.swing.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE))
              .addGroup(layout.createSequentialGroup()
                .addComponent(jLabelAliases)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 48, Short.MAX_VALUE)
                .addComponent(jComboBoxAliases, javax.swing.GroupLayout.PREFERRED_SIZE, 281, javax.swing.GroupLayout.PREFERRED_SIZE))))
          .addComponent(jRadioButtonAddKey))
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(jRadioButtonUseKey)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jComboBoxAliases, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabelAliases))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabelPasswd)
          .addComponent(jPasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jRadioButtonAddKey)
        .addContainerGap(170, Short.MAX_VALUE))
    );
  }// </editor-fold>//GEN-END:initComponents

  private void jComboBoxAliasesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxAliasesActionPerformed
    onUpdate();
  }//GEN-LAST:event_jComboBoxAliasesActionPerformed

  private void jPasswordFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPasswordFieldActionPerformed
    onUpdate();
  }//GEN-LAST:event_jPasswordFieldActionPerformed

  private void jRadioButtonAddKeyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonAddKeyActionPerformed
    onUpdate();
  }//GEN-LAST:event_jRadioButtonAddKeyActionPerformed

  private void jRadioButtonUseKeyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonUseKeyActionPerformed
    onUpdate();
  }//GEN-LAST:event_jRadioButtonUseKeyActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.ButtonGroup buttonGroup1;
  private javax.swing.JComboBox jComboBoxAliases;
  private javax.swing.JLabel jLabelAliases;
  private javax.swing.JLabel jLabelPasswd;
  private javax.swing.JPasswordField jPasswordField;
  private javax.swing.JRadioButton jRadioButtonAddKey;
  private javax.swing.JRadioButton jRadioButtonUseKey;
  // End of variables declaration//GEN-END:variables

  @Override
  void store(WizardDescriptor settings) {
    settings.putProperty(
        AndroidProjectProperties.PROP_KEY_ALIAS, jComboBoxAliases.getSelectedItem());
    settings.putProperty(
        AndroidProjectProperties.PROP_KEY_ALIAS_PASSWD, new String(jPasswordField.getPassword()));
    settings.putProperty(ExportPackageWizardIterator.PROP_USE_EXISTING_ALIAS, 
        Boolean.valueOf(jRadioButtonUseKey.isSelected()));
  }

  boolean useExistingAlias() {
    return jRadioButtonUseKey.isSelected();
  }

  @Override
  void read(WizardDescriptor settings) {
    FileInputStream fis = null;
    try {
      exc = null;
      // get the alias list (also used as a keystore password test)
      Enumeration<String> aliases = Enumerations.empty();
      if (Boolean.TRUE.equals(settings.getProperty(
              ExportPackageWizardIterator.PROP_USE_EXISTING_KEYSTORE))) {
        String keystore = (String) settings.getProperty(AndroidProjectProperties.PROP_KEY_STORE);
        String keystorePasswd = 
            (String) settings.getProperty(AndroidProjectProperties.PROP_KEY_STORE_PASSWD);
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        fis = new FileInputStream(keystore);
        keyStore.load(fis, keystorePasswd.toCharArray());

        aliases = keyStore.aliases();
      }

      boolean hasAliases = aliases.hasMoreElements();
      jRadioButtonUseKey.setSelected(hasAliases);
      jRadioButtonAddKey.setSelected(!hasAliases);
      jComboBoxAliases.setEnabled(hasAliases);
      jPasswordField.setEnabled(hasAliases);
      jComboBoxAliases.removeAllItems();
      while (aliases.hasMoreElements()) {
        jComboBoxAliases.addItem(aliases.nextElement());
      }
      if (hasAliases) {
        jComboBoxAliases.setSelectedIndex(0);
      }
      String alias = (String) settings.getProperty(AndroidProjectProperties.PROP_KEY_ALIAS);
      if (alias != null) {
        jComboBoxAliases.setSelectedItem(alias);
      }
      String aliasPasswd = 
          (String) settings.getProperty(AndroidProjectProperties.PROP_KEY_ALIAS_PASSWD);

      jPasswordField.setText(aliasPasswd);
      onUpdate();
    } catch (CertificateException ex) {
      onException(ex);
    } catch (NoSuchAlgorithmException ex) {
      onException(ex);
    } catch (KeyStoreException ex) {
      onException(ex);
    } catch (IOException ex) {
      onException(ex);
    } finally {
      Closeables.closeQuietly(fis);
    }
  }

  private void onUpdate() {
    wizPanel.onChange();
    jComboBoxAliases.setEnabled(jRadioButtonUseKey.isSelected());
    jPasswordField.setEnabled(jRadioButtonUseKey.isSelected());
  }

  @Override
  boolean valid(WizardDescriptor settings) {
    settings.putProperty("WizardPanel_errorMessage", !valid ? errorMsg : null);
    return this.valid;
  }

  @Override
  void validate(WizardDescriptor settings) {
    valid = false;

    if (exc != null) {
      errorMsg = exc.getLocalizedMessage();
      return;
    }
    if (jRadioButtonUseKey.isSelected()) {
        if (jComboBoxAliases.getSelectedIndex() == -1) {
            errorMsg = "Select a key alias.";
            return;
        }

        if (new String(jPasswordField.getPassword()).trim().length() == 0) {
            errorMsg = "Enter key password.";
            return;
        }
      }

    valid = true;
  }

  private void onException(Exception ex) {
    valid = false;
    errorMsg = ex.getLocalizedMessage();
    exc = ex;
    onUpdate();
  }

  boolean isFinishable() {
    return valid && jRadioButtonUseKey.isSelected();
  }
}
