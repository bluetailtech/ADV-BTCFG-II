
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

package rr_importer;

import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.*;

import javax.swing.filechooser.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.*;
import java.awt.*;
import java.util.*;


public class RR_import extends javax.swing.JFrame implements Runnable {


  public RR_import(btconfig.BTFrame p) {
    this.parent = p;
  }

  javax.swing.JTree import_tree;

    public static btconfig.BTFrame parent;
    public static String fs="";
    public static String root_path;
    public static String home_dir_str;

    public boolean do_rescan=false;

    private DefaultMutableTreeNode root;

    public DefaultTreeModel treeModel;


    private String value;
    private RandomAccessFile raf; 

    /**
     * Creates new form RR_import
     */
    public void run() {
        initComponents();
        setIconImage(new javax.swing.ImageIcon(getClass().getResource("/btconfig/images/console.png")).getImage()); // NOI18N
        import_table.setShowHorizontalLines(true);
        import_table.setShowVerticalLines(true);


        update_home_paths();



        
        // get first directory(a node), then creates a tree from the node.
        File fileRoot = new File(root_path);
        root = new DefaultMutableTreeNode(new FileNode(fileRoot));
        treeModel = new DefaultTreeModel(root);

        // goes from a generic tree node into a JTree which is usable with the Jframe
        // (UI)
        import_tree = new JTree(treeModel);
        import_tree.setShowsRootHandles(true);
        scroll_pane_tree.setViewportView(import_tree);



        // add listener for opening files
        import_tree.addTreeSelectionListener(new TreeSelectionListener() { // https://docs.oracle.com/javase/tutorial/uiswing/events/treeselectionlistener.html
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) import_tree.getLastSelectedPathComponent();
                // nothing is selected
                if (node == null)
                    return;

                // if node is a leaf and double click open
                if (node.isLeaf()) {

                    // get the path
                    TreePath treepath = e.getPath();

                    String filename = treepath.getLastPathComponent().toString();

                    try {
                      status_label.setText("Status: loading records"); 

                      setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                      System.out.println(root_path + fs + filename); 

                      if(filename.contains("ctid") || filename.contains("stid")) {

                        int ncols=0;
                        if(filename.contains("ctid")) ncols=9;
                        if(filename.contains("stid")) ncols=10;

                        raf = new RandomAccessFile( root_path+fs+filename, "r" ); 
                        int lines=0;
                        while(raf!=null && raf.readLine()!=null){
                          lines++;
                        }
                        lines--; //remove 1 for header

                        if(lines > 0 ) {
                              import_table.removeAll();
                              import_table.setModel(new javax.swing.table.DefaultTableModel(
                                  new Object[lines][10],
                                  new String [] {
                                      "Frequency","Agency/Category","County","Description","Alpha Tag","PL Tone","Mode","Class Station Code","Tag","Freq Test Result"
                                  }
                              ) {
                                  Class[] types = new Class [] {
                                      java.lang.String.class,java.lang.String.class,java.lang.String.class,java.lang.String.class,java.lang.String.class,java.lang.String.class,java.lang.String.class,java.lang.String.class,java.lang.String.class,java.lang.String.class
                                  };

                                  public Class getColumnClass(int columnIndex) {
                                      return types [columnIndex];
                                  }
                              });


                              raf.seek(0);
                              int idx=0;
                              String line = raf.readLine(); //skip the header

                              boolean type12 = false;
                              if(line.contains("PL Output Tone") && line.contains("PL Input Tone")) {
                                type12=true;
                              }


                              line = raf.readLine();

                              while(line!=null) {
                                  //System.out.println("cols: "+ncols+" "+line.trim());

                                  //String[] cols = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                                //String[] cols = line.split(",");
                                String[] cols = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                                for(int i=0;i<cols.length;i++) {
                                  try {
                                    cols[i] = strip_garbage(cols[i]).trim();
                                  } catch(Exception e2) {
                                    e2.printStackTrace();
                                  }
                                }

                                boolean skip=false;
                                if(cols.length!=10 && cols.length!=11 && !(type12 && cols.length==12) ) {
                                  System.out.println("mangled record: cols: "+cols.length+" "+line);
                                  skip=true;
                                }

                                if(!skip) {
                                  //System.out.println("");
                                  int off=0;
                                  int off2=0;
                                  for(int i=0;i<cols.length-2;i++) {
																		//System.out.println( "col[i+off]: "+cols[i+off] );

                                    if( (i+off == 8) && type12 ) {
																			//System.out.println( "SKIP: col[i+off]: "+cols[i+off] );
																			//System.out.println( "IMPORT: col[i+off]: "+cols[i+off] );
                                      import_table.getModel().setValueAt( cols[i+off], idx, 6); 
                                      //skip
                                      off2--;
                                    }
                                    else {
																			//System.out.println( "IMPORT: col[i+off]: "+cols[i+off] );
                                      import_table.getModel().setValueAt( cols[i+off], idx, i+off2); 
                                      off=2;
                                      if(ncols==9 && i==1) off2=1;
                                    }
                                  }
                                  idx++;
                                }

                                line = raf.readLine();
                              }


                         }

                      }
                            
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                    finally {
                      setCursor(Cursor.getDefaultCursor());
                      status_label.setText("Status: idle"); 
                    }

                }

            }
        });

        //setVisible(true);

        // Instantiates a CreateChildNode
        CreateChildNodes ccn = new CreateChildNodes(fileRoot, root, this);
        new Thread(ccn).start();

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        status_label = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        rescan = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        import_sel = new javax.swing.JButton();
        test_selected = new javax.swing.JButton();
        close = new javax.swing.JButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        import_table = new javax.swing.JTable();
        scroll_pane_tree = new javax.swing.JScrollPane();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();

        setTitle("RR State / County CSV FIle Import");

        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.Y_AXIS));

        jPanel6.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        status_label.setText("Status:");
        jPanel6.add(status_label);

        jPanel1.add(jPanel6);

        rescan.setText("ReScan Files");
        rescan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rescanActionPerformed(evt);
            }
        });
        jPanel5.add(rescan);

        jLabel1.setText("Use Mouse + CTRL + Shift Keys To Select Systems To Import");
        jPanel5.add(jLabel1);

        import_sel.setText("Import Selected Records");
        import_sel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                import_selActionPerformed(evt);
            }
        });
        jPanel5.add(import_sel);

        test_selected.setText("Test Selected Freqs");
        jPanel5.add(test_selected);

        close.setText("Close");
        close.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeActionPerformed(evt);
            }
        });
        jPanel5.add(close);

        jPanel1.add(jPanel5);

        getContentPane().add(jPanel1, java.awt.BorderLayout.PAGE_END);

        jSplitPane1.setDividerLocation(175);
        jSplitPane1.setDividerSize(15);

        import_table.setAutoCreateRowSorter(true);
        import_table.setModel(new javax.swing.table.DefaultTableModel(
            new Object[150][10],
            new String [] {
                "Frequency","Agency/Category","County","Description","Alpha Tag","PL Tone","Mode","Class Station Code","Tag","Freq Test Result"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class,java.lang.String.class,java.lang.String.class,java.lang.String.class,java.lang.String.class,java.lang.String.class,java.lang.String.class,java.lang.String.class,java.lang.String.class,java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane2.setViewportView(import_table);

        jSplitPane1.setRightComponent(jScrollPane2);
        jSplitPane1.setLeftComponent(scroll_pane_tree);

        getContentPane().add(jSplitPane1, java.awt.BorderLayout.CENTER);

        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.Y_AXIS));

        jLabel3.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jLabel3.setText("RR State / County CSV Channel Import  ( stid_xxx.csv, ctid_xxx.csv files)");
        jPanel3.add(jLabel3);

        jPanel2.add(jPanel3);

        jLabel2.setText("Add CSV Files To The 'Documents/p25rx/rr_imports' Folder And Press ReScan Files. Filenames Must Contain (stid or ctid)");
        jPanel4.add(jLabel2);

        jPanel2.add(jPanel4);

        getContentPane().add(jPanel2, java.awt.BorderLayout.NORTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public int import_records(boolean append) {
      int rec_cnt=0;

      try {


        int[] rows = import_table.getSelectedRows();
        String val="";

        if(rows.length>0) {

          //jTable1.getModel().setValueAt(null,jTable1.convertRowIndexToModel(rows[i]),0);
          /*
          for(int i=0;i<rows.length;i++) {
            for(int j=0;j<10;j++) {
              val = (String) import_table.getModel().getValueAt(rows[i],j);
              System.out.println( String.format("col: %d, val=%s", j, val) );
            }

          }
          */
          for(int i=0;i<rows.length;i++) {


            //create directory structure

            String freq = (String) import_table.getModel().getValueAt(import_table.convertRowIndexToModel(rows[i]),0);
            String agency = (String) import_table.getModel().getValueAt(import_table.convertRowIndexToModel(rows[i]),1);
            String county = (String) import_table.getModel().getValueAt(import_table.convertRowIndexToModel(rows[i]),2);
            String desc = (String) import_table.getModel().getValueAt(import_table.convertRowIndexToModel(rows[i]),3);
            String alpha_tag = (String) import_table.getModel().getValueAt(import_table.convertRowIndexToModel(rows[i]),4);
            String pl_tone = (String) import_table.getModel().getValueAt(import_table.convertRowIndexToModel(rows[i]),5);
            String mode = (String) import_table.getModel().getValueAt(import_table.convertRowIndexToModel(rows[i]),6);
            String class_station_code = (String) import_table.getModel().getValueAt(import_table.convertRowIndexToModel(rows[i]),7);
            String tag = (String) import_table.getModel().getValueAt(import_table.convertRowIndexToModel(rows[i]),8);

            rec_cnt++;

            add_new_record( freq, agency, county, desc, alpha_tag, pl_tone, mode, class_station_code, tag, false );



          }

        }

      } catch(Exception e) {
        e.printStackTrace();
      }
      finally {
         parent.browser_rescan();
      }
      return rec_cnt;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    static public void update_home_paths() {
      try {
        JFileChooser chooser = new JFileChooser();
        File file = chooser.getCurrentDirectory();  //better for windows to do it this way
        fs =  System.getProperty("file.separator");
        home_dir_str = file.getAbsolutePath()+fs;

        root_path = home_dir_str+"p25rx/rr_imports";

        Path path = Paths.get(new File(root_path).getAbsolutePath() );
        try {
          Files.createDirectories(path);
        } catch(Exception e) {
          e.printStackTrace();
        }
        System.out.println("path:"+path);


      } catch(Exception e) {
        e.printStackTrace();
      }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    static public void add_new_record( String freq, String agency, String county, String desc, String alpha_tag, 
                                String pl_tone, String mode, String class_station_code, String tag, boolean do_rescan) {

      try {
        update_home_paths();

        parent.update_systems_path(false);

        Path path = Paths.get(new File(parent.systems_path).getAbsolutePath() );

        freq = strip_garbage(freq); 
        agency =  strip_garbage(agency);
        county =  strip_garbage(county);
        desc =  strip_garbage(desc);
        alpha_tag =  strip_garbage(alpha_tag);
        pl_tone =  strip_garbage(pl_tone);
        mode =  strip_garbage(mode);
        class_station_code =  strip_garbage(class_station_code);
        tag =  strip_garbage(tag);

        agency = agency.trim();
        if(county==null) county="";
        county = county.trim();
        desc = desc.trim();
        freq = freq.trim();
        alpha_tag = alpha_tag.trim();

        pl_tone = pl_tone.trim();
        mode = mode.trim();
        tag = tag.trim();
        class_station_code = class_station_code.trim();


        try {
          double freq_d = Double.valueOf(freq);
          freq = String.format("%3.6f", freq_d);
        } catch(Exception e) {
        }


        Boolean is_simulcast=false;

        if( desc.toLowerCase().contains("simulcast") ) is_simulcast=true;

        path = Paths.get(new File(parent.systems_path+fs+agency+fs+desc+fs+freq).getAbsolutePath() );
        try {
          Files.createDirectories(path);
        } catch(Exception e) {
          e.printStackTrace();
        }



        File cfg_file;
        FileOutputStream fos;
        Properties props = new Properties();

        //create file
        try {
          cfg_file = new File( path+fs+"click_to_monitor" );
          fos = new FileOutputStream( cfg_file, false );

          props.setProperty("is_simulcast", is_simulcast.toString());
          props.setProperty("pl_tone", pl_tone);
          props.setProperty("mode", mode);
          props.setProperty("alpha_tag", alpha_tag);
          props.setProperty("tag", tag);
          props.setProperty("county", county);
          props.setProperty("class_station_code", class_station_code);
          props.setProperty("agency", agency);
          props.setProperty("desc", desc);
          props.setProperty("freq", freq);

          props.store(fos,"---machine generated. don't edit---");

          fos.close();

        } catch(Exception e) {
          e.printStackTrace();
        }


        //create file
        try {

          cfg_file = new File( path+fs+"channel_config" );

          if(cfg_file.exists() && cfg_file.length()>64 ) {
          }
          else {

            //intialize and show channel_config here
            btconfig.channel_config.initialize_from_rr( props, cfg_file );
          }

        } catch(Exception e) {
            e.printStackTrace();
        }
      } catch(Exception e) {
        e.printStackTrace();
      }
      finally {
         if(do_rescan) parent.browser_rescan();
      }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    static public String strip_garbage(String str) {
      int i;
      if(str==null) str="";

      byte[] b = str.getBytes();

      for(i=0;i<b.length;i++) {

        if( b[i]>=(byte) 0x00 && b[i]<=(byte)0x1f && b[i]!=(byte)0x0a && b[i]!=(byte)0x0d) {
          b[i] = (byte)' ';
        }
        if((byte)b[i]<0) {
          b[i] = (byte)' ';
        }
        if(b[i]==0xa0) b[i]=(byte)' ';

        if(b[i]=='\"') b[i]=(byte)' ';

        if(b[i]=='/') b[i]=(byte)'-';
        if(b[i]=='&') b[i]=(byte)'-';
        if(b[i]==':') b[i]=(byte)' ';
      }
      return new String(b).trim();
    }

    private void import_selActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_import_selActionPerformed
      status_label.setText("Status: importing"); 

      setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      int recs = 0;
      try {
        recs = import_records(true);
      } catch(Exception e) {
      }
      setCursor(Cursor.getDefaultCursor());

      status_label.setText("Status: imported "+recs+" records");
    }//GEN-LAST:event_import_selActionPerformed

    private void rescanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rescanActionPerformed
      do_rescan=true;
    }//GEN-LAST:event_rescanActionPerformed

    private void closeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeActionPerformed
      //setVisible(false);
      hide();
    }//GEN-LAST:event_closeActionPerformed

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
            java.util.logging.Logger.getLogger(RR_import.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(RR_import.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(RR_import.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(RR_import.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        //java.awt.EventQueue.invokeLater( new RR_import() );
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton close;
    private javax.swing.JButton import_sel;
    private javax.swing.JTable import_table;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JButton rescan;
    private javax.swing.JScrollPane scroll_pane_tree;
    private javax.swing.JLabel status_label;
    private javax.swing.JButton test_selected;
    // End of variables declaration//GEN-END:variables
}
