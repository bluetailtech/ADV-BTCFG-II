
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

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Hashtable;
import java.util.*;
import javax.swing.tree.*;
import javax.swing.*;

public class CreateChildNodes implements Runnable {

    private DefaultMutableTreeNode root;

    private File fileRoot;

    private FileBrowser parent;

    private Hashtable cc_ht; 
    public TreePath[] tps=null;
    public Vector tps_ht;

    public CreateChildNodes(File fileRoot, DefaultMutableTreeNode root, FileBrowser p) {
        this.fileRoot = fileRoot;
        this.parent = p;
        this.root = root;
        this.cc_ht = new Hashtable();
        tps_ht = new Vector();
    }

    @Override
    public void run() {
        //recursion calls itself until it can't keep going deeper (files ==null)
        createChildren(fileRoot, root);
        try {
          parent.rescan_done();
        } catch(Exception e) {
        }
    }

    public Hashtable get_config_nodes() {
      return cc_ht;
    }

    //populate tree with directory structure using recursion
    private void createChildren(File fileRoot, DefaultMutableTreeNode node) {
        File[] files = fileRoot.listFiles();

        if (files == null) {
          try {
            parent.rescan_done();
          } catch(Exception e) {
          }
          return;
        }
        for (File file : files) {
            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(new FileNode(file));

            if(!childNode.toString().contains("channel_config")) node.add(childNode); //add to tree

            if (file.isDirectory()) {
                createChildren(file, childNode);
            }
        }
    }

    //make a hashtable of paths for the directory structure
    public void update_cc_ht(File fileRoot, DefaultMutableTreeNode node) {
        File[] files = fileRoot.listFiles();

        if (files == null) {
          return;
        }
        for (File file : files) {
            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(new FileNode(file));

            if(childNode.toString().contains("channel_config")) {
              System.out.println(file.toString());
              cc_ht.put(file.toString(), file); 
            }

            if (file.isDirectory()) {
                update_cc_ht(file,childNode);
            }
        }
    }

    //create a vector of paths containing search string
    public void search(TreeNode root, String search_str, int exact) {
      try {
        Enumeration children = root.children();

        if(children!=null) {
          while (children.hasMoreElements()) {
            TreeNode tn = (TreeNode) children.nextElement();

              if(tn.toString().toLowerCase().contains(search_str.toLowerCase())) {
                tps_ht.addElement(tn); 
              }

            search(tn, search_str,exact);
          }
        }

        if( tps_ht!=null && tps_ht.size()>0) {
          int n = tps_ht.size();
          tps = new TreePath[n]; 
          for(int i=0;i<n;i++) {
            tps[i] = new TreePath( ((DefaultMutableTreeNode) tps_ht.elementAt(i)).getPath() ); 
          }
        }


      } catch(Exception e) {
        e.printStackTrace();
      }

    }

}
