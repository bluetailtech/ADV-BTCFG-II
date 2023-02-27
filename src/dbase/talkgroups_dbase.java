
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

package dbase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import javax.swing.*;
import javax.swing.table.*;

public class talkgroups_dbase {

  private Connection con=null;
  private String mac_id;
  private Statement stmt = null; 
  private btconfig.BTFrame parent=null;
  private ResultSet rs=null;
  private PreparedStatement p=null;

  private String INSERT_TALKGROUPS_DSD = "insert into talkgroups (Decimal, AlphaTag, Description, SYS_ID, WACN, ZONE, PRIORITY, ENABLE) values (?, ?, ?, ?, ?, ?, ?, ?)";
  private String INSERT_TALKGROUPS_1 = "insert into talkgroups (Decimal, AlphaTag, Description, SYS_ID, WACN, ZONE, PRIORITY, ENABLE) values (?, ?, ?, ?, ?, ?, ?, ?)";

  private String UPDATE_TALKGROUPS_1 = "update talkgroups set AlphaTag=?, Description=? where Decimal = ? and wacn=? and sys_id=? and ZONE=?"; 
  private String SELECT_TALKGROUPS_1 = "select count(*) from talkgroups"; 
  private String DEL = "delete from talkgroups where rowid=?"; 

  public String order_by = "rowid";
  private int order_by_idx=0;
  private int prev_order_by_idx=0;

  private String SELECT_TALKGROUPS_2 = "select ENABLE, rowid as Row, SYS_ID, PRIORITY, Decimal, AlphaTag, Description, WACN, ZONE, rowid from talkgroups order by "+order_by; 

  private String SELECT_TALKGROUPS_3 = "select Decimal from talkgroups where WACN=? and SYS_ID=? and Decimal=?"; 
  private String UPDATE_TALKGROUPS_4 = "insert or replace into talkgroups (AlphaTag,Description,ZONE,PRIORITY,ENABLE,Decimal,SYS_ID,WACN,rowid) values (?,?,?,?,?,?,?,?,?)"; 

  private String MAX_ROWID = "select max(rowid) from talkgroups";

  private String DROP = "drop table talkgroups"; 
  private PreparedStatement p_dsd;
  private PreparedStatement p1;
  private PreparedStatement p2;
  private PreparedStatement p3;
  private PreparedStatement p4;
  private PreparedStatement p5;
  private PreparedStatement p6;
  private PreparedStatement p7;
  private PreparedStatement p8;
  private PreparedStatement p9;
  private PreparedStatement p10;
  private PreparedStatement p11;
  private PreparedStatement p12;


  ////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////
  public talkgroups_dbase(btconfig.BTFrame p) {
    this.parent = p;
  }

  ////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////
  public Connection getCon() {
    return con;
  }

  ////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////
  public boolean init(String mac_id) {
    boolean err=true;
    try {
      con = DriverManager.getConnection("jdbc:sqlite:btt_"+mac_id+"_talkgroupv3.db");
      con.setAutoCommit(false);

      stmt = con.createStatement();
      stmt.setQueryTimeout(30);  // set timeout to 30 sec.

      create_tables();

      p_dsd = con.prepareStatement(INSERT_TALKGROUPS_DSD);

      int nrows = tg_get_rows();

      update_talkgroup_table();

      con.commit();

    } catch(Exception e) {
      err=false;
    }
    finally {
      return err;
    }
  }

  ////////////////////////////////////////////////////////////////////////////////
  //TEXT
  //NUMERIC
  //INTEGER
  //REAL
  //BLOB
  ////////////////////////////////////////////////////////////////////////////////
  public void create_tables() {
    try {
      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS talkgroups ( Decimal	INTEGER, AlphaTag	TEXT, Description	TEXT, SYS_ID INTEGER, WACN INTEGER, ZONE INTEGER, PRIORITY INTEGER, ENABLE INTEGER )");
    } catch(Exception e) {
      //e.printStackTrace();
    }
    finally {
    }
  }

  ////////////////////////////////////////////////////////////////////////////////
  public void addRecordCSV(int en, Integer sys_id, int priority, int talkgroup, String alpha, String desc, Integer wacn, int zone) {
    int rows=0;
    try {
      if(wacn==null) return;
      if(sys_id==null) return;
      if(alpha==null) return;
      if(desc==null) return;
      if(talkgroup==0) return;

      int wacn_d = wacn.intValue();
      int sys_id_d = sys_id.intValue();


      if(p1==null) p1 = con.prepareStatement(INSERT_TALKGROUPS_1);

      p1.setInt(1,talkgroup);
      p1.setString(2,alpha);
      p1.setString(3,desc);
      p1.setInt(4,sys_id_d);
      p1.setInt(5,wacn_d);
      p1.setInt(6,zone); //zone
      p1.setInt(7,priority); //pri
      p1.setInt(8,en); //enable

      rows = p1.executeUpdate();


    } catch(Exception e) {
      //e.printStackTrace();
      if(rows==0) {
        update_tg_record_short(talkgroup, wacn, sys_id, alpha, desc, zone);
      }
    }
    finally {
    }
  }
  ////////////////////////////////////////////////////////////////////////////////
  //private String INSERT_TALKGROUPS_DSD = "insert into talkgroups (Decimal, AlphaTag, Description, 
  //SYS_ID, WACN, ZONE, PRIORITY, ENABLE) values (?, ?, ?, ?, ?, ?, ?, ?)";
  ////////////////////////////////////////////////////////////////////////////////
  public int addRecordUnknown(int talkgroup, Integer wacn, Integer sys_id, String alpha, String desc) {
    int rows=0;
    try {
      if(wacn==null) return 0;
      if(sys_id==null) return 0;
      if(alpha==null) return 0;
      if(desc==null) return 0;
      if(talkgroup==0) return 0;

      int wacn_d = wacn.intValue();
      int sys_id_d = sys_id.intValue();

      if(p9==null) p9 = con.prepareStatement(SELECT_TALKGROUPS_3);
      p9.setInt(1,wacn_d);
      p9.setInt(2,sys_id_d);
      p9.setInt(3,talkgroup);
      rs = p9.executeQuery();
      if(rs!=null && rs.next()) return 0; //already exists


      p_dsd.setInt(1,talkgroup);
      p_dsd.setString(2,alpha);
      p_dsd.setString(3,desc);
      p_dsd.setInt(4,sys_id_d);
      p_dsd.setInt(5,wacn_d);
      p_dsd.setInt(6,1); //zone
      p_dsd.setInt(7,1); //pri
      p_dsd.setInt(8,1); //enable

      rows = p_dsd.executeUpdate();


    } catch(Exception e) {
      //e.printStackTrace();
    }
    finally {
      return rows;
    }
  }
  ////////////////////////////////////////////////////////////////////////////////
  //parent.talkgroups_db.addRecordDSD( talkgroup, wacn, sys_id, atag, dtag );
  //Decimal	INTEGER, 
  //AlphaTag	TEXT, 
  //Description	TEXT, 
  //SYS_ID INTEGER, 
  //WACN INTEGER, 
  //ZONE INTEGER, 
  //PRIORITY INTEGER, 
  //ENABLE INTEGER, 
  //UNIQUE(Decimal, SYS_ID, WACN) )");
  ////////////////////////////////////////////////////////////////////////////////
  public int addRecordDSD(int talkgroup, Integer wacn, Integer sys_id, String alpha, String desc) {
    int rows=0;
    try {
      if(wacn==null) return 0;
      if(sys_id==null) return 0;
      if(alpha==null) return 0;
      if(desc==null) return 0;
      if(talkgroup==0) return 0;

      int wacn_d = wacn.intValue();
      int sys_id_d = sys_id.intValue();


      p_dsd.setInt(1,talkgroup);
      p_dsd.setString(2,alpha);
      p_dsd.setString(3,desc);
      p_dsd.setInt(4,sys_id_d);
      p_dsd.setInt(5,wacn_d);
      p_dsd.setInt(6,1); //zone
      p_dsd.setInt(7,1); //pri
      p_dsd.setInt(8,1); //enable

      rows = p_dsd.executeUpdate();


    } catch(Exception e) {
      //e.printStackTrace();
      if(rows==0) {
        update_tg_record_short(talkgroup, wacn, sys_id, alpha, desc, 1);
      }
    }
    finally {
      return rows;
    }
  }

  ////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////
  public void update_tg_record_short(int talkgroup, Integer wacn, Integer sys_id, String alpha, String desc, int zone) {
    try {

      if(talkgroup==0) return;
      if(wacn==null) return;
      if(sys_id==null) return;

      int w = Integer.valueOf(wacn); 
      int s = Integer.valueOf(sys_id); 

      if(p2==null) p2 = con.prepareStatement(UPDATE_TALKGROUPS_1);

      p2.setString(1,alpha);
      p2.setString(2,desc);
      p2.setInt(3,talkgroup);
      p2.setInt(4,w);
      p2.setInt(5,s);
      p2.setInt(6,zone);

      int rows = p2.executeUpdate();

      //don't do this. too much CPU
      //if(rows>0) update_talkgroup_table();
    } catch(Exception e) {
      //e.printStackTrace();
    }
    finally {
    }
  }
  ////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////
  public void talkgroup_delete_tg(int rowid) {

    try {


      if(p3==null) p3 = con.prepareStatement(DEL);
      p3.setInt(1,rowid);
      p3.executeUpdate();

    } catch(Exception e) {
      //e.printStackTrace();
    }
    finally {
    }
  }
  ////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////
  public int drop_talkgroup_table() {
    int rows = 0;

    try {


      if(p4==null) p4 = con.prepareStatement(DROP);
      rows = p4.executeUpdate();

    } catch(Exception e) {
      //e.printStackTrace();
    }
    finally {
      return rows;
    }
  }
  ////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////
  public int tg_get_max_row() {
    int max_row = 0;

    try {


      if(p12==null) p12 = con.prepareStatement(MAX_ROWID);
      rs = p12.executeQuery();

      if(rs!=null && rs.next()) {
        max_row = rs.getInt(1);
      }


    } catch(Exception e) {
      //e.printStackTrace();
    }
    finally {
      return max_row;
    }
  }
  ////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////
  public int tg_get_rows() {
    int rows = 0;

    try {


      if(p5==null) p5 = con.prepareStatement(SELECT_TALKGROUPS_1);
      rs = p5.executeQuery();

      if(rs!=null && rs.next()) {
        rows = rs.getInt(1);
      }


    } catch(Exception e) {
      //e.printStackTrace();
    }
    finally {
      return rows;
    }
  }
  ////////////////////////////////////////////////////////////////////////////////
        //parent.addTableObject( true, idx, 0);
        //parent.addTableObject( new String(sys_id_hex), idx, 1);
        //parent.addTableObject( new Integer(1), idx, 2);
        //parent.addTableObject( new Integer(talkgroup), idx, 3);
        //parent.addTableObject( new String(talkgroup+"_unknown"), idx, 4);
        //parent.addTableObject( "", idx, 5);
        //parent.addTableObject( new String(wacn_hex), idx, 6);
        //parent.addTableObject( new Integer("1"), idx, 7);
  ////////////////////////////////////////////////////////////////////////////////
  public void update_talkgroup_table() {

    try {
      if( parent.jTable1.isEditing() ) return; //don't interrupt an edit
      if( !parent.enable_tg_table_updates.isSelected()) return;

      try {
        if( parent.tabbed_pane.getTitleAt(parent.tabbed_pane.getSelectedIndex()).contains("TG Editor")) {
          //System.out.println("TG Editor tab not selected. skipping table update");
        } 
      } catch(Exception e) {
      }

      int nrows = tg_get_rows();
      if(nrows<30) nrows=30;
      if(parent!=null && parent.enable_tg_table_updates.isSelected()) {
        ((DefaultTableModel) parent.jTable1.getModel()).setRowCount(nrows+10);
        parent.getTGConfig().setNROWS(nrows);
      }
        for(int i=0; i<nrows+10; i++) {
          try {
            parent.jTable1.getModel().setValueAt(null, i,0);
            parent.jTable1.getModel().setValueAt(null, i,1);
            parent.jTable1.getModel().setValueAt(null, i,2);
            parent.jTable1.getModel().setValueAt(null, i,3);
            parent.jTable1.getModel().setValueAt(null, i,4);
            parent.jTable1.getModel().setValueAt(null, i,5);
            parent.jTable1.getModel().setValueAt(null, i,6);
            parent.jTable1.getModel().setValueAt(null, i,7);
            parent.jTable1.getModel().setValueAt(null, i,8);
          } catch(Exception e) {
            e.printStackTrace();
          }
        }

      order_by_idx = parent.tg_sort.getSelectedIndex();

      if(order_by_idx!=prev_order_by_idx) {
        if(order_by_idx==0) order_by = "rowid";
        if(order_by_idx==1) order_by = "Decimal";
        if(order_by_idx==2) order_by = "SYS_ID";
        if(order_by_idx==3) order_by = "WACN";
        if(order_by_idx==4) order_by = "PRIORITY";
        if(order_by_idx==5) order_by = "AlphaTag";
        if(order_by_idx==6) order_by = "Description";
        if(order_by_idx==7) order_by = "ZONE";
        SELECT_TALKGROUPS_2 = "select ENABLE, rowid as Row, SYS_ID, PRIORITY, Decimal, AlphaTag, Description, WACN, ZONE from talkgroups order by "+order_by; 
        prev_order_by_idx=order_by_idx;
        p6 = con.prepareStatement(SELECT_TALKGROUPS_2);
      }

      int max_row = parent.talkgroups_db.tg_get_max_row();

      if(p6==null) p6 = con.prepareStatement(SELECT_TALKGROUPS_2);
      rs = p6.executeQuery();

      int idx=0;
      while(rs!=null && rs.next()) {

        boolean b=false;
        if( rs.getInt("ENABLE")!=0 ) b=true;

        parent.addTableObject( b, idx, 0);
        int row = rs.getInt("Row");
        parent.addTableObject( String.format("%d", rs.getInt("Row")), idx, 1);
        parent.addTableObject( String.format("0x%03x", rs.getInt("SYS_ID")), idx, 2);
        parent.addTableObject( new Integer(rs.getInt("PRIORITY")), idx, 3);
        parent.addTableObject( new Integer(rs.getInt("Decimal")), idx, 4);
        parent.addTableObject( rs.getString("AlphaTag"), idx, 5 );
        parent.addTableObject( rs.getString("Description"), idx, 6);
        parent.addTableObject( String.format("0x%05x", rs.getInt("WACN")), idx, 7 );
        parent.addTableObject( new Integer(rs.getInt("ZONE")), idx, 8);

        idx++;

      }

        max_row++;
        for(int i=0; i<10; i++) {
          try {
            parent.jTable1.getModel().setValueAt(null, idx,0);
            parent.jTable1.getModel().setValueAt(String.format("%d", max_row++), idx,1);
            parent.jTable1.getModel().setValueAt(null, idx,2);
            parent.jTable1.getModel().setValueAt(null, idx,3);
            parent.jTable1.getModel().setValueAt(null, idx,4);
            parent.jTable1.getModel().setValueAt(null, idx,5);
            parent.jTable1.getModel().setValueAt(null, idx,6);
            parent.jTable1.getModel().setValueAt(null, idx,7);
            parent.jTable1.getModel().setValueAt(null, idx,8);
            idx++;
          } catch(Exception e) {
            e.printStackTrace();
          }
        }

    } catch(Exception e) {
      //e.printStackTrace();
    }
    finally {
    }
  }
  ////////////////////////////////////////////////////////////////////////////////
  //private String UPDATE_TALKGROUPS_4 = "replace into talkgroups (AlphaTag,Description,
  //ZONE,PRIORITY,ENABLE,Decimal,SYS_ID,WACN,rowid) values (?,?,?,?,?,?,?,?,?)"; 
  ////////////////////////////////////////////////////////////////////////////////
  public int tg_insert_or_update_nodup(int en, int sys_id, int pri, int tg, String alpha, String desc, int wacn, int zone, int tg_row_id) {
    int rows=0;

    try {
      if(p10==null) p10 = con.prepareStatement(UPDATE_TALKGROUPS_4);

      p10.setString(1,alpha);
      p10.setString(2,desc);
      p10.setInt(3,zone);
      p10.setInt(4,pri);
      p10.setInt(5,en);
      p10.setInt(6,tg);
      p10.setInt(7,sys_id);
      p10.setInt(8,wacn);
      p10.setInt(9,tg_row_id);

      rows = p10.executeUpdate();
    } catch(Exception e2) {
      e2.printStackTrace();
    }
    finally {
      return rows;
    }
  }
}
