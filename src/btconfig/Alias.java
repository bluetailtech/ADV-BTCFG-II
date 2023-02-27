
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

import java.util.*;
import java.io.*;
import java.nio.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.fazecast.jSerialComm.*;
import javax.swing.filechooser.*;
import javax.swing.*;
import javax.swing.table.*;
import java.util.prefs.Preferences;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
class Alias
{

java.util.Hashtable alias_hash;
private int NRECS=16000;
private BTFrame parent; 
Preferences prefs;
int[] recent_rows;
int recent_idx=0;
int previous_rid;
String home_dir;
String sys_mac_id;
TableRowSorter trs;
java.util.Timer utimer;


public Alias(BTFrame parent, String sys_mac_id, String home_dir) {
  this.parent = parent;
  this.sys_mac_id=sys_mac_id;
  this.home_dir=home_dir;
}

private void read_alias() {
  try {

  } catch(Exception e) {
    e.printStackTrace();
  }
}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
public void setAlias(aliasEntry ae, int rid_i, String alias_str) {
  try {

    if(parent.alias_db!=null) {
      Connection con = parent.alias_db.getCon();
      parent.alias_db.addAlias(rid_i, alias_str);
      con.commit();
    }

  } catch(Exception e) {
  }
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
public String getAlias(int rid_i) {
  try {
    if( parent.alias_db!=null ) return parent.alias_db.getAliasStr(rid_i);
  } catch(Exception e) {
  }
  return null;
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
public void findAlias(aliasEntry ae, int rid_i) {
  try {
    String alias_str="";
    System.out.println("finding alias for "+rid_i);

    alias_str = parent.alias_db.getAliasStr(rid_i);

    ae.setAlias(alias_str);
  } catch(Exception e) {
    e.printStackTrace();
  }
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
public void import_alias_csv(BTFrame parent, LineNumberReader lnr)
{
  Connection db_con = null; 
  try {

    int number_of_records=0;

    if(parent!=null) ((DefaultTableModel) parent.alias_table.getModel()).setRowCount(64000);

    String in_line="";
    String[] strs = null;

    System.out.println("import aliases");

    parent.alias_db.drop_alias_table();
    parent.alias_db.create_tables();

    db_con = parent.alias_db.getCon();
    db_con.setAutoCommit(false);

    //while(number_of_records<NRECS) {
    while(number_of_records<64000) {

      in_line = lnr.readLine();
      
      if(in_line!=null && in_line.length()>1) {
        in_line = in_line.trim();

        strs = in_line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");

        if(strs!=null && strs.length>=1) { 

            String str1="";
            String str2=null;
            String str3=null;
            String str4=null;

            if(strs[0]!=null) str1 = strs[0];  
            if(strs.length>1 && strs[1]!=null) str2 = strs[1]; 
            if(strs.length>2 && strs[2]!=null) str3 = strs[2]; 
            if(strs.length>3 && strs[3]!=null) str4 = strs[3]; 

            try {
              //if( str1!=null ) parent.addAliasObject(new Integer(str1), number_of_records,0);
            } catch(Exception e) {
              e.printStackTrace();
            }
            try {
              //if( str2!=null ) parent.addAliasObject(str2, number_of_records,1);
               // else parent.addAliasObject(null, number_of_records,1);
            } catch(Exception e) {
              e.printStackTrace();
            }

            int tg_i = 0;
            try {
              tg_i = Integer.valueOf(str3);
            } catch(Exception e) {
            }

            try {
                //parent.addAliasObject(tg_i, number_of_records,2);
                //parent.addAliasObject(str4, number_of_records,3);
            } catch(Exception e) {
              e.printStackTrace();
            }

            int rid = Integer.valueOf(str1);
            parent.alias_db.addRIDAlias(rid, str2, tg_i, str4);

        }
      }
      else {
        break;
      }

      parent.setStatus("import record "+number_of_records);

      number_of_records++;
    }

    db_con.commit();
    //db_con.setAutoCommit(true);

    NRECS=number_of_records;

    if(parent!=null) ((DefaultTableModel) parent.alias_table.getModel()).setRowCount(NRECS);

    System.out.println(number_of_records+" alias records");

    lnr.close();

    parent.alias_db.update_alias_table();

  } catch (Exception e) {
    try {
      db_con.commit();
    } catch(Exception e2) {
    }
    e.printStackTrace();
  }
}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
public void addRID(BTFrame parent, String rid) {
  int first_empty_row=0;


  if(parent.alias_db!=null) {
    try {
      Connection con = parent.alias_db.getCon();
      parent.alias_db.addRID( parent.src_uid, parent.c_tg, parent.talkgroup_name);
      con.commit();
    } catch(Exception e) {
    }
  }

}

}
