
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

package filesystem;

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
import javax.swing.tree.*;
import javax.swing.*;

import javax.swing.filechooser.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;

import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.io.IOException;

public class FileBrowser implements Runnable  {

    private String fs="";
    private String root_path = ""; 
    private String home_dir_str;
    private DefaultMutableTreeNode root;
    private DefaultTreeModel treeModel;
    public JTree tree;
    public JScrollPane browser_pane;
    private String value;
    private btconfig.BTFrame parent;
    private File fileRoot;
    private int current_channel_index=0;

    private DefaultMutableTreeNode current_selection;
    CreateChildNodes ccn=null;

    javax.swing.JPopupMenu popup;
    javax.swing.JMenuItem menuitem1;

    ///////////////////////////////////////////////////
    ///////////////////////////////////////////////////
    class config_thread implements Runnable {
      String op=null;
      Boolean b=null;
      int level = -120;
      boolean do_rescan=true;
      FileBrowser fb=null;

      public config_thread(String op, Boolean b, FileBrowser fb) {
        this.op = op;
        this.b = b;
        this.fb = fb;
      }
      public void run() {


          if( op.contains("set_squelch") ) {
            try {
              String str = JOptionPane.showInputDialog(parent,"squelch level dBm?");
              level = Integer.valueOf(str);
            } catch(Exception e) {
              e.printStackTrace();
              return;
            }
          }

        TreePath[] tp = tree.getSelectionPaths();
        if(tp==null) return;

        for(int i=0;i<tp.length;i++) {


          Object[] p = tp[i].getPath();
          int path_len = p.length; 

          String pathString = ""; 
          if(p.length==0) return; 
          if(p.length==1) return; 
          if(p.length==2) pathString = root_path+fs+p[1];
          if(p.length==3) pathString = root_path+fs+p[1]+fs+p[2];
          if(p.length==4) pathString = root_path+fs+p[1]+fs+p[2]+fs+p[3];
          if(p.length==5) return; 


          //System.out.println("path:"+pathString);


          try {
            Files.walkFileTree(Paths.get(pathString),new HashSet<>(), 4, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                        throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if( file.toString().contains("channel_config") ) {
                      System.out.println(file);

                      try {
                        File f = new File(file.toString());
                        if( f.exists() ) { 
                          btconfig.channel_config cc = new btconfig.channel_config("name","915.0"); 
                          FileInputStream fis = new FileInputStream(f);
                          ObjectInputStream ois = new ObjectInputStream(fis);
                          cc.read_config(ois);
                          ois.close();
                          fis.close();

                          if( cc!=null && op.contains("include_in_scan") ) {
                            cc.do_scan = b;
                            if(b) cc.install_in_flash = b;
                            FileOutputStream fos = new FileOutputStream(f);
                            ObjectOutputStream oos = new ObjectOutputStream(fos);
                            cc.write_config(oos);
                            oos.close();
                            fos.close();
                            parent.cc_apply.setBackground( Color.gray );
                            parent.cc_sync.setBackground( Color.green );
                          }
                          else if( cc!=null && op.contains("install_to_flash") ) {
                            cc.install_in_flash = b;
                            FileOutputStream fos = new FileOutputStream(f);
                            ObjectOutputStream oos = new ObjectOutputStream(fos);
                            cc.write_config(oos);
                            oos.close();
                            fos.close();
                            parent.cc_apply.setBackground( Color.gray );
                            parent.cc_sync.setBackground( Color.green );
                          }
                          else if( cc!=null && op.contains("is_control") ) {
                            cc.is_control = b;
                            cc.is_conventional = !b;
                            FileOutputStream fos = new FileOutputStream(f);
                            ObjectOutputStream oos = new ObjectOutputStream(fos);
                            cc.write_config(oos);
                            oos.close();
                            fos.close();
                            parent.cc_apply.setBackground( Color.gray );
                            parent.cc_sync.setBackground( Color.green );
                          }
                          else if( cc!=null && op.contains("cfg3") ) {
                            cc.do_scan = b;
                            cc.install_in_flash = b;
                            cc.is_control = b;
                            cc.is_conventional = !b;

                            FileOutputStream fos = new FileOutputStream(f);
                            ObjectOutputStream oos = new ObjectOutputStream(fos);
                            cc.write_config(oos);
                            oos.close();
                            fos.close();
                            parent.cc_apply.setBackground( Color.gray );
                            parent.cc_sync.setBackground( Color.green );
                          }
                          else if( cc!=null && op.contains("set_gain_optimal") ) {
                            parent.setStatus("Finding gains for "+cc.name);
                            parent.current_cc = cc; 
                            parent.do_fixed_gain=1;
                            while(true) {
                              if(parent.do_fixed_gain==0) break;
                              Thread.sleep(100);
                            }

                            FileOutputStream fos = new FileOutputStream(f);
                            ObjectOutputStream oos = new ObjectOutputStream(fos);
                            cc.write_config(oos);
                            oos.close();
                            fos.close();

                            parent.ccfg_changes=0;
                            parent.cc_apply.setBackground( Color.gray );
                            parent.cc_sync.setBackground( Color.green );
                          }
                          else if( cc!=null && op.contains("set_gain_auto") ) {
                            cc.lna_gain = 0;
                            cc.mgain = 0;
                            cc.vga_gain = 0;

                            FileOutputStream fos = new FileOutputStream(f);
                            ObjectOutputStream oos = new ObjectOutputStream(fos);
                            cc.write_config(oos);
                            oos.close();
                            fos.close();
                            parent.cc_apply.setBackground( Color.gray );
                            parent.cc_sync.setBackground( Color.green );
                          }
                          else if( cc!=null && op.contains("set_squelch") ) {
                            cc.squelch_enable = true;
                            cc.squelch_level = level; 

                            FileOutputStream fos = new FileOutputStream(f);
                            ObjectOutputStream oos = new ObjectOutputStream(fos);
                            cc.write_config(oos);
                            oos.close();
                            fos.close();
                            parent.cc_apply.setBackground( Color.gray );
                            parent.cc_sync.setBackground( Color.green );
                          }
                          else if( cc!=null && op.contains("highlight_flash") ) {
                            String freq = String.format("%3.6f", cc.frequency);

                            ccn = new CreateChildNodes(fileRoot, root, fb);
                            ccn.search(root,freq,1);
                            TreePath[] tps = ccn.tps;

                            if(tps!=null) {
                              for(int i=0;i<tps.length;i++) {
                                tree.expandPath(tps[i]);
                                System.out.println( tps[i].toString() );

                                if(i==0) {
                                  tree.scrollRectToVisible( tree.getPathBounds(tps[i]) );
                                }
                              }
                              tree.setSelectionPaths(tps);

                              do_rescan=false;
                            }
                          }
                        }
                      } catch(Exception e) {
                        e.printStackTrace();
                      }
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });
          } catch(Exception exp) {
            exp.printStackTrace();
          }
        }

        try {
          if(do_rescan) rescan();
        } catch(Exception e) {
          e.printStackTrace();
        }
      }
    }

    ///////////////////////////////////////////////////
    ///////////////////////////////////////////////////
    class PopupListener extends MouseAdapter 
      {  PopupListener(JPopupMenu m) 
         {  menu = m; 
         } 
       
         public void mousePressed(MouseEvent e) {  
           maybeShowPopup(e); 
         } 
       
         public void mouseReleased(MouseEvent e) {  
           maybeShowPopup(e); 
         } 
       
         private void maybeShowPopup(MouseEvent e) 
         {  if (e.isPopupTrigger()) 
              menu.show(e.getComponent(), e.getX(), e.getY()); 
         } 
       
         private JPopupMenu menu; 
      }

      ///////////////////////////////////////////////////
      ///////////////////////////////////////////////////
      public void setSystemsPath(String path) {
      }
      ///////////////////////////////////////////////////
      ///////////////////////////////////////////////////
      public void handle_config_change(String op, boolean b) {
        //update_root();

        Thread ct = new Thread( new config_thread(op,b,this) );
        ct.start();
      }

      ///////////////////////////////////////////////////
      ///////////////////////////////////////////////////
      public void handle_remove_selected() {

        int a=JOptionPane.showConfirmDialog(parent,"About to permanently delete systems. Are you sure?");
        if(a!=JOptionPane.YES_OPTION) return;

        if(current_selection.toString().startsWith("systems")) return;

        TreePath[] tp = tree.getSelectionPaths();
        for(int i=0;i<tp.length;i++) {


          Object[] p = tp[i].getPath();
          int path_len = p.length; 

          String pathString = ""; 
          if(p.length==0) return; 
          if(p.length==1) return; 
          if(p.length==2) pathString = root_path+fs+p[1];
          if(p.length==3) pathString = root_path+fs+p[1]+fs+p[2];
          if(p.length==4) pathString = root_path+fs+p[1]+fs+p[2]+fs+p[3];
          if(p.length==5) return; 

          System.out.println("path:"+pathString);

          try {
            Files.walkFileTree(Paths.get(pathString),new HashSet<>(), 4, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                        throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    System.out.println("delete_file " + file);
                    try {
                      File f = new File(file.toString());
                      f.delete();
                    } catch(Exception e) {
                      e.printStackTrace();
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    System.out.println("delete_dir " + dir);
                    try {
                      File f = new File(dir.toString());
                      f.delete();
                    } catch(Exception e) {
                      e.printStackTrace();
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
          } catch(Exception exp) {
            exp.printStackTrace();
          }
        }

        try {
          rescan();
        } catch(Exception e) {
          e.printStackTrace();
        }
      }


    ///////////////////////////////////////////////////
    ///////////////////////////////////////////////////
    private void test_popup_action(java.awt.event.ActionEvent evt) {
      //System.out.println("got here: "+evt.getActionCommand());
      String cmd = evt.getActionCommand();
      if(cmd.startsWith("Remove Selected") && current_selection!=null) {
        handle_remove_selected();
      }


      if(cmd.startsWith("Search Systems") && tree!=null) {
        do_search_action();
      }

      if(cmd.startsWith("Find Currently Monitored") && tree!=null) {
        String search_str = String.format("%3.6f", (double)parent.current_freq/(double)1e6);
        //System.out.println("search_str: "+search_str);
        if(search_str==null) return;
        search_str = search_str.substring(0,search_str.length()-4);
        do_search(search_str,1);
      }

      if(cmd.startsWith("Add New Channel") && tree!=null) {
        add_new_channel();
      }

      if(cmd.startsWith("Include Selected In Scan") && tree!=null) {
        handle_config_change("include_in_scan", true);
      }
      if(cmd.startsWith("Exclude Selected From Scan") && tree!=null) {
        handle_config_change("include_in_scan", false);
      }
      if(cmd.startsWith("Mark Selected As Install To Flash") && tree!=null) {
        handle_config_change("install_to_flash", true);
      }
      if(cmd.startsWith("Set Selected As Control") && tree!=null) {
        handle_config_change("is_control", true);
      }
      if(cmd.startsWith("Enable Scan/Flash/Control For Selected") && tree!=null) {
        handle_config_change("cfg3", true);
      }
      if(cmd.startsWith("Set Optimal Gains For Selected") && tree!=null) {
        handle_config_change("set_gain_optimal", true);
      }
      if(cmd.startsWith("Set Auto Gains For Selected") && tree!=null) {
        handle_config_change("set_gain_auto", true);
      }
      if(cmd.startsWith("Set Squelch For Selected") && tree!=null) {
        handle_config_change("set_squelch", true);
      }
      if(cmd.startsWith("Highlight Install-To-Flash Systems") && tree!=null) {
        handle_config_change("highlight_flash", true);
      }
    }

    ///////////////////////////////////////////////////
    ///////////////////////////////////////////////////
    public void add_new_channel() {
      try {
        //public void add_new_record( String freq, String agency, String county, String desc, String alpha_tag, 
         //                           String pl_tone, String mode, String class_station_code, String tag, boolean do_rescan) {
        String freq =JOptionPane.showInputDialog(parent,"Frequency (required)");
        if(freq==null || freq.length()==0) return;

        try {
          double freq_d = Double.valueOf(freq);
          freq = String.format("%3.6f", freq_d);
        } catch(Exception e) {
        }

        String agency =JOptionPane.showInputDialog(parent,"Agency (required)");
        if(agency==null || agency.length()==0) return;

        String county =JOptionPane.showInputDialog(parent,"County (can be blank)");
        if(county==null) county=""; 

        String desc =JOptionPane.showInputDialog(parent,"Description (required, add 'simulcast' if applicable)");
        if(desc==null || desc.length()==0) return; 

        String alpha_tag =JOptionPane.showInputDialog(parent,"Alpha Tag (required)");
        if(alpha_tag==null || alpha_tag.length()==0) return; 

        String mode =JOptionPane.showInputDialog(parent,"Mode (P25,DMR,FM,AM,NXDN)");
        if(mode==null || mode.length()==0) return; 

        rr_importer.RR_import rr = new rr_importer.RR_import(parent);
        rr.add_new_record(freq, agency, county, desc, alpha_tag, "", mode, "", alpha_tag, false);
        rescan();

        tree.requestFocus();

        Thread.sleep(100);

        do_search(freq,1);

      } catch(Exception e) {
        e.printStackTrace();
      }
    }

    public void do_search_action() {
        String search_str =JOptionPane.showInputDialog(parent,"Enter Word(s)/Frequency To Search For");
        if(search_str==null) return;
        do_search(search_str,0);
    }

    ///////////////////////////////////////////////////
    ///////////////////////////////////////////////////
    public void do_search(String search_str, int exact) {
      try {

        ccn = new CreateChildNodes(fileRoot, root, this);
        ccn.search(root,search_str,exact);
        TreePath[] tps = ccn.tps;

        if(tps!=null) {

          int row = tree.getRowCount() - 1;
          while (row > 0) { //collapses only child nodes of root node
            tree.collapseRow(row);
            row--;
          }

          for(int i=0;i<tps.length;i++) {
            tree.expandPath(tps[i]);
            System.out.println( tps[i].toString() );

            if(i==0) {
              tree.scrollRectToVisible( tree.getPathBounds(tps[i]) );
            }
          }
          tree.setSelectionPaths(tps);
        }
        else {
          //JOptionPane.showMessageDialog(parent,"Nothing Found.");
        }
      } catch(Exception e) {
        e.printStackTrace();
        //JOptionPane.showMessageDialog(parent,"Nothing Found.");
      }
    }

    ///////////////////////////////////////////////////
    ///////////////////////////////////////////////////
    private javax.swing.JMenuItem create_menu_item(String s) {
      javax.swing.JMenuItem menuitem1 = new javax.swing.JMenuItem();
      menuitem1.setText(s);
      menuitem1.setLabel(s);
      //menuitem1.setToolTipText(s);

      menuitem1.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
          test_popup_action(evt);
        } 
      });

      return menuitem1;
    }

    ///////////////////////////////////////////////////
    ///////////////////////////////////////////////////
    private void init_menu( javax.swing.JTree tree) {
        //menu related
        popup = new javax.swing.JPopupMenu();

        javax.swing.JMenuItem menu0 = create_menu_item("Search Systems CTRL-f");
        javax.swing.JMenuItem menu_add = create_menu_item("Add New Channel");
        javax.swing.JMenuItem menu1 = create_menu_item("Remove Selected From List");
        javax.swing.JMenuItem menu2 = create_menu_item("Include Selected In Scan");
        javax.swing.JMenuItem menu3 = create_menu_item("Exclude Selected From Scan");
        javax.swing.JMenuItem menu4 = create_menu_item("Mark Selected As Install To Flash");
        javax.swing.JMenuItem menu5 = create_menu_item("Set Squelch For Selected");
        javax.swing.JMenuItem menu6 = create_menu_item("Set Optimal Gains For Selected");
        javax.swing.JMenuItem menu6a = create_menu_item("Set Auto Gains For Selected");
        javax.swing.JMenuItem menu7 = create_menu_item("Import System Talk Groups");
        javax.swing.JMenuItem menu8 = create_menu_item("Find Currently Monitored Frequency");
        javax.swing.JMenuItem menu9 = create_menu_item("Highlight Install-To-Flash Systems");
        javax.swing.JMenuItem menu10 = create_menu_item("Set Selected As Control Channels");
        javax.swing.JMenuItem menu11 = create_menu_item("Enable Scan/Flash/Control For Selected");

        popup.add(menu0);
        popup.add(menu_add);
        popup.add(menu1);
        popup.add(menu2);
        popup.add(menu3);
        popup.add(menu4);
        popup.add(menu5);
        popup.add(menu6);
        popup.add(menu6a);
        popup.add(menu7);
        popup.add(menu8);
        popup.add(menu9);
        popup.add(menu10);
        popup.add(menu11);

        //menu0.setEnabled(false);
        //menu_add.setEnabled(false);
        //menu1.setEnabled(false);
        //menu2.setEnabled(false);
        //menu3.setEnabled(false);
        //menu4.setEnabled(false);
        //menu5.setEnabled(false);
        //menu6.setEnabled(false);
        menu7.setEnabled(false);
        //menu8.setEnabled(false);
        //menu9.setEnabled(false);
        //menu11.setEnabled(false);


        java.awt.event.MouseListener popupListener = new PopupListener(popup);
        tree.addMouseListener(popupListener);

        tree.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
              System.out.println(evt);
            }
        });


        InputMap inputMap = tree.getInputMap();
        ActionMap actionMap = tree.getActionMap();
        String searchAction = "search";
        KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK);
        inputMap.put(key, searchAction);
        actionMap.put(searchAction, new AbstractAction()
        {
          public void actionPerformed(java.awt.event.ActionEvent searchEvent)
          {
              do_search_action();
          }
        });

    }


    ///////////////////////////////////////////////////
    ///////////////////////////////////////////////////
    public FileBrowser(btconfig.BTFrame p) {
      this.parent = p;
      update_root();
      try {
        for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
          if ("Nimbus".equals(info.getName())) {
              javax.swing.UIManager.setLookAndFeel(info.getClassName());
              break;
          }
        }


      } catch(Exception e) {
      }
    }

    ///////////////////////////////////////////////////
    ///////////////////////////////////////////////////
    public void rescan_done() {
      try {
        treeModel.reload();
        //for (int i = 0; i < tree.getRowCount(); i++) {
         // tree.expandRow(i);
        //}
        tree.expandRow(0);
        tree.setPreferredSize( new Dimension(350,75000) );

      } catch(Exception e) {
        e.printStackTrace();
      }
      System.out.println("rescan done");
    }

    ///////////////////////////////////////////////////
    ///////////////////////////////////////////////////
    public void rescan() {
      try {
        System.out.println("starting rescan");
        root.removeAllChildren();
        treeModel.reload();
        ccn = new CreateChildNodes(fileRoot, root, this);
        new Thread(ccn).start();
        System.out.println("rescan running");

      } catch(Exception e) {
        e.printStackTrace();
      }
    }

    ///////////////////////////////////////////////////
    ///////////////////////////////////////////////////
    public void next_freq() {
      try {
        btconfig.channel_config[] cfgs = get_flash_configs();
        if(cfgs!=null && cfgs.length>0) {
          current_channel_index++;
          if(current_channel_index>=cfgs.length) current_channel_index=0;
          String freq = String.format("%3.6f", cfgs[current_channel_index].frequency);

          if(freq==null) return;
          freq = freq.substring(0,freq.length()-4);

          do_search(freq, 1);
          parent.channel_change(cfgs[current_channel_index]);
        }
      } catch(Exception e) {
        e.printStackTrace();
      }
    }
    ///////////////////////////////////////////////////
    ///////////////////////////////////////////////////
    public void prev_freq() {
      try {
        btconfig.channel_config[] cfgs = get_flash_configs();
        if(cfgs!=null && cfgs.length>0) {
          current_channel_index--;
          if(current_channel_index<0) current_channel_index=cfgs.length-1;
          String freq = String.format("%3.4f", cfgs[current_channel_index].frequency);

          if(freq==null) return;
          freq = freq.substring(0,freq.length()-4);


          do_search(freq,1);
          parent.channel_change(cfgs[current_channel_index]);
        }
      } catch(Exception e) {
        e.printStackTrace();
      }
    }

    ///////////////////////////////////////////////////
    ///////////////////////////////////////////////////
    public btconfig.channel_config[] get_flash_configs() {
      btconfig.channel_config[] cfgs=null;
      try {

        ccn = new CreateChildNodes(fileRoot, root, this);
        ccn.update_cc_ht(fileRoot,root);

        Hashtable ht = ccn.get_config_nodes(); 
        Enumeration e = ht.elements();

        System.out.println("cc_ht_sz: "+ht.size());

        int cnt_flash=0;
        while( e!=null && e.hasMoreElements()) {
          File f = (File) e.nextElement();
          btconfig.channel_config cc = btconfig.channel_config.get_cc_from_file(null, f); 
          if(cc!=null && cc.install_in_flash) cnt_flash++;
        }

        if(ht!=null) cfgs = new btconfig.channel_config[cnt_flash];

        e = ht.elements();
        int idx=0;
        while( e!=null && e.hasMoreElements() && cfgs!=null && cnt_flash>0) {
          File f = (File) e.nextElement();
          btconfig.channel_config cc = btconfig.channel_config.get_cc_from_file(null, f); 
          if(cc!=null && cc.install_in_flash) {
            System.out.println("install_to_flash file: "+f.getAbsolutePath());
            cfgs[idx++] = cc;
          }
        }
      } catch(Exception e) {
        e.printStackTrace();
      }
      return cfgs;
    }


    ///////////////////////////////////////////////////
    ///////////////////////////////////////////////////
    public void update_root() {
      try {
        parent.update_systems_path(false);

        fs =  System.getProperty("file.separator");

        root_path = parent.systems_path;
        fileRoot = new File(root_path);

        if(treeModel!=null) {
          root.setUserObject( new FileNode(fileRoot) );
          treeModel.setRoot(root);
          treeModel.reload();
          tree.expandRow(0);
          tree.setPreferredSize( new Dimension(350,75000) );
        }
        //rescan();
      } catch(Exception e) {
        e.printStackTrace();
      }
    }
    ///////////////////////////////////////////////////
    ///////////////////////////////////////////////////
    public void run() {

        update_root();

        root = new DefaultMutableTreeNode(new FileNode(fileRoot));
        if(treeModel==null) treeModel = new DefaultTreeModel(root);

        tree = new JTree(treeModel);

        tree.setShowsRootHandles(true);

        tree.setPreferredSize( new Dimension(350,75000) );

        init_menu(tree);

        //DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) tree.getCellRenderer();
        //Icon closedIcon = new ImageIcon("closed.png");
        //Icon openIcon = new ImageIcon("open.png");
        //renderer.setClosedIcon(closedIcon);
        //renderer.setOpenIcon(openIcon);
        //renderer.setLeafIcon( new javax.swing.ImageIcon(getClass().getResource("/filesystem/images/audio.png")));



        tree.addTreeSelectionListener(new TreeSelectionListener() { // https://docs.oracle.com/javase/tutorial/uiswing/events/treeselectionlistener.html
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                current_selection = node;

                // nothing is selected
                if (node == null)
                    return;


              // get the path
              TreePath treepath = e.getPath();

              try {
                 System.out.println(root_path + fs + treepath.getLastPathComponent());
                 System.out.println("treepath: "+treepath.toString()+"  is_leaf: "+node.isLeaf()+" len: "+ treepath.getPath().length );

                  Object[] p = treepath.getPath();
                  int path_len = p.length; 

                  //System.out.println("p[4] = "+p[4]+"  len: "+path_len);

                  //user clicked on "monitor" this channel 
                  if(path_len==5 && p[4].toString().contains("click_to_monitor") ) {

                    String path = root_path+fs+p[1]+fs+p[2]+fs+p[3];

                    File cfg_file;
                    FileInputStream fis;
                    Properties props = new Properties();

                    cfg_file = new File( path+fs+p[4] );
                    System.out.println("cfg_file: "+cfg_file);

                    if(cfg_file.exists()) {
                      fis = new FileInputStream( cfg_file );
                      props.load(fis);

                      String is_simulcast = props.getProperty("is_simulcast");
                      String pl_tone = props.getProperty("pl_tone");
                      String mode = props.getProperty("mode");
                      String alpha_tag = props.getProperty("alpha_tag");
                      String tag = props.getProperty("tag");
                      String county = props.getProperty("county");
                      String class_station_code = props.getProperty("class_station_code");
                      String agency = props.getProperty("agency");
                      String desc = props.getProperty("desc");
                      String freq = props.getProperty("freq");

                      fis.close();

                      System.out.println("props: "+props.toString());

                      //monitor
                      if( freq.length()>0) {
                        cfg_file = new File( path+fs+"channel_config" );
                        btconfig.channel_config cc = btconfig.channel_config.get_cc_from_file(props, cfg_file);
                        parent.channel_change(cc);
                        parent.ccfg_changes=0;
                      }


                      cfg_file = new File( path+fs+"channel_config" );

                      if(cfg_file.exists() && cfg_file.length()>64 ) {

                        //show channel_config here
                        //System.out.println("show channel_config here");

                        btconfig.channel_config cc = btconfig.channel_config.get_cc_from_file(props, cfg_file);
                        parent.channel_config_update_gui( cc );


                      }
                      else {

                        //intialize and show channel_config here
                        //System.out.println("show and init channel_config here");
                        btconfig.channel_config.initialize_from_rr( props, cfg_file );

                        btconfig.channel_config cc = btconfig.channel_config.get_cc_from_file(props, cfg_file);
                        parent.channel_config_update_gui( cc );
                      }


                    }
                  }

                  //user clicked on "frequency" for this channel 
                  if(path_len==4 || (path_len==5 && p[4].toString().contains("channel_config")) ) {

                    String path = root_path+fs+p[1]+fs+p[2]+fs+p[3];

                    File cfg_file;
                    FileInputStream fis;
                    
                    cfg_file = new File( path+fs+"click_to_monitor" );
                    fis = new FileInputStream( cfg_file );
                    Properties def_props = new Properties();
                    def_props.load(fis);
                    fis.close();


                    cfg_file = new File( path+fs+"channel_config" );

                    if(cfg_file.exists() && cfg_file.length()>64 ) {

                      //show channel_config here
                      System.out.println("show channel_config here");

                      btconfig.channel_config cc = btconfig.channel_config.get_cc_from_file(def_props, cfg_file);
                      parent.channel_config_update_gui( cc );


                    }
                    else {

                      //intialize and show channel_config here
                      System.out.println("show and init channel_config here");
                      btconfig.channel_config.initialize_from_rr( def_props, cfg_file );

                      btconfig.channel_config cc = btconfig.channel_config.get_cc_from_file(def_props, cfg_file);
                      parent.channel_config_update_gui( cc );
                    }
                  }


                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        });
        browser_pane = new JScrollPane(tree);


        rescan();

    }

    //public static void main(String[] args) {
     //   SwingUtilities.invokeLater(new FileBrowser());
    //}

}
