
//MIT License
//
//Copyright (c) 2023 bluetailtech
//
//Permission is hereby granted, free of charge, to any person obtaining a copy
//of this software and associated documentation files (the "Software"), to deal
//in the Software without restriction, including without limitation the rights
//to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//copies of the Software, and to permit persons to whom the Software is
//furnished to do so, subject to the following conditions:
//
//The above copyright notice and this permission notice shall be included in all
//copies or substantial portions of the Software.
//
//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
//SOFTWARE.
//
//

package btconfig;
import java.util.prefs.Preferences;
  import java.awt.*;
/**
 *
 * @author radioactive
 */
public class displayframe_popout extends javax.swing.JFrame {

BTFrame parent;
Preferences prefs;
    /**
     * Creates new form displayframe_popout
     */
    /**
     * Creates new form displayframe_popout
     */
    public displayframe_popout(BTFrame parent, Preferences prefs) {
        this.parent = parent;
        this.prefs = prefs;
        initComponents();
        setSize(1024,768);
        split_top.setBackground(new java.awt.Color(0, 0, 0));
        split_bottom.setBackground(new java.awt.Color(0, 0, 0));
      setAlwaysOnTop(parent.popout_ontop.isEnabled());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        split_top = new javax.swing.JPanel();
        split_bottom = new javax.swing.JPanel();

        setTitle("Display View Popout");
        setBackground(new java.awt.Color(0, 0, 0));
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentHidden(java.awt.event.ComponentEvent evt) {
                formComponentHidden(evt);
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        jSplitPane1.setDividerLocation(500);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        split_top.setLayout(new java.awt.BorderLayout());
        jSplitPane1.setTopComponent(split_top);

        split_bottom.setLayout(new java.awt.BorderLayout());
        jSplitPane1.setRightComponent(split_bottom);

        getContentPane().add(jSplitPane1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
    }//GEN-LAST:event_formWindowClosed

    private void formComponentHidden(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentHidden
    }//GEN-LAST:event_formComponentHidden

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing

        Rectangle r = parent.dvout.getBounds();
        prefs.putInt("dvout_form_x", r.x);
        prefs.putInt("dvout_form_y", r.y);
        prefs.putInt("dvout_form_width", r.width);
        prefs.putInt("dvout_form_height", r.height);

      this.remove(parent.display_frame);
      parent.displayviewmain_border.add(parent.display_frame, java.awt.BorderLayout.CENTER);

      this.remove(parent.tg_scroll_pane);
      parent.logpanel.add(parent.tg_scroll_pane, java.awt.BorderLayout.CENTER);

    }//GEN-LAST:event_formWindowClosing

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(displayframe_popout.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(displayframe_popout.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(displayframe_popout.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(displayframe_popout.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

              /*
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new displayframe_popout().setVisible(true);
            }
        });
              */
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSplitPane jSplitPane1;
    public javax.swing.JPanel split_bottom;
    public javax.swing.JPanel split_top;
    // End of variables declaration//GEN-END:variables
}
