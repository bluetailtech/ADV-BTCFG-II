
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

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.*;
import javax.swing.tree.*;

//Starts from the given file root and then generates the tree
public class CreateChildNodes implements Runnable {

    private DefaultMutableTreeNode root;

    private File fileRoot;
    private RR_import parent;

    public CreateChildNodes(File fileRoot, DefaultMutableTreeNode root, RR_import p) {
        this.fileRoot = fileRoot;
        this.root = root;
        this.parent = p;
    }

    @Override
    public void run() {
      while(true) {
        //recursion calls itself until it can't keep going deeper (files ==null)
        createChildren(fileRoot, root);

        if(parent.do_rescan) {
          parent.treeModel.reload();
          parent.do_rescan=false;
        }
        TreePath tree_path = parent.import_tree.getSelectionPath();
        parent.import_tree.expandPath(tree_path);


        while( !parent.do_rescan ) {
          try {
            //Thread.yield();
            Thread.sleep(100);
          } catch(Exception e) {
          }
        }

        root.removeAllChildren();

        System.out.println("rescan");
      }
    }

    private void createChildren(File fileRoot, DefaultMutableTreeNode node) {
                //lists the files at the current node
        File[] files = fileRoot.listFiles();

        if (files == null) return;
                //for each file that gets listed, add it to the tree and then if it is a directory, call this function with the node as the root
        for (File file : files) {
            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(new FileNode(file));
            node.add(childNode);
            if (file.isDirectory()) {
                createChildren(file, childNode);
            }
        }
    }

}
