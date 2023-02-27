
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

public class alias_dbase {

  private Connection con=null;
  private String mac_id;
  private Statement stmt = null; 
  private btconfig.BTFrame parent=null;
  private ResultSet rs=null;

  private String INSERT_ALIAS_RID_1 = "insert into alias_rid (rid, alias, tg, alphatag) values (?, ?, ?, ?)";
  private String UPDATE_ALIAS_RID_1 = "update alias_rid set alias=?,tg=?,alphatag=? where rid=?";
  private String UPDATE_ALIAS_RID_1b = "update alias_rid set tg=?,alphatag=? where rid=?";

  private String INSERT_ALIAS_RID_2 = "insert into alias_rid (rid, alias, tg, alphatag) values (?, ?, ?, ?)";
  private String UPDATE_ALIAS_RID_2 = "update alias_rid set alias=? where rid=?"; 

  private String SELECT_ALIAS_RID_1 = "select alias from alias_rid where rid = ?"; 
  private String DEL = "delete from alias_rid where rid = ?"; 
  private String UPDATE_ALIAS_RID_3 = "update alias_rid set alias=?,tg=?,alphatag=? where rid = ?"; 
  private String SELECT_ALIAS_RID_O1 = "select rid, alias, tg, alphatag from alias_rid order by alias"; 
  private String SELECT_ALIAS_RID_O2 = "select rid, alias, tg, alphatag from alias_rid order by rid";
  private String DROP = "drop table alias_rid"; 
  private String SELECT_ALIAS_RID = "select count(*) from alias_rid"; 

  private PreparedStatement p1=null;
  private PreparedStatement p2=null;
  private PreparedStatement p3=null;
  private PreparedStatement p4=null;
  private PreparedStatement p5=null;
  private PreparedStatement p6=null;
  private PreparedStatement p7=null;
  private PreparedStatement p8=null;
  private PreparedStatement p9=null;
  private PreparedStatement p10=null;
  private PreparedStatement p11=null;
  private PreparedStatement p12=null;
  ////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////
  public alias_dbase(btconfig.BTFrame p) {
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

      if( !parent.enable_alias_dbase.isSelected()) return false;


      con = DriverManager.getConnection("jdbc:sqlite:btt_"+mac_id+"_aliasv002.db");
      con.setAutoCommit(false);

      stmt = con.createStatement();
      stmt.setQueryTimeout(30);  // set timeout to 30 sec.

      create_tables();

      int nrows = alias_get_rows();
      if(parent!=null) ((DefaultTableModel) parent.alias_table.getModel()).setRowCount(nrows+10);

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
      //stmt.executeUpdate("create table if not exists alias_rid (rid integer, alias string, tg integer, sys_id integer, wacn integer, unique (rid) )");
      stmt.executeUpdate("create table if not exists alias_rid (rid integer, alias string, tg integer, alphatag string, unique (rid) )");
    } catch(Exception e) {
      //e.printStackTrace();
    }
    finally {
    }
  }
  ////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////
  public int addRIDAlias(int rid, String alias, int tg, String alphatag) {
    int rows=0;

    if( !parent.enable_alias_dbase.isSelected()) return 0;


    try {
      if(rid==0) return rows;

      if(alphatag!=null) alphatag = alphatag.trim();

      if(p1==null) p1 = con.prepareStatement(INSERT_ALIAS_RID_1);
      p1.setInt(1,rid);
      p1.setString(2, alias);
      p1.setInt(3, tg);
      p1.setString(4, alphatag);
      rows = p1.executeUpdate();

    } catch(Exception e) {
      //e.printStackTrace();
    }
    finally {
      try {
        if(rows==0 && alias!=null && alias.length()>0) {
          if(p2==null) p2 = con.prepareStatement(UPDATE_ALIAS_RID_1);
          p2.setString(1, alias);
          p2.setInt(2, tg);
          p2.setString(3, alphatag);
          p2.setInt(4,rid);
          rows = p2.executeUpdate();
        }
        else {
          if(p3==null) p3 = con.prepareStatement(UPDATE_ALIAS_RID_1b);
          p3.setInt(1, tg);
          p3.setString(2, alphatag);
          p3.setInt(3,rid);
          rows = p3.executeUpdate();
        }
      } catch(Exception e2) {
      }
      return rows;
    }
  }

  ////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////
  public void addRID(int rid, int tg, String alpha) {
    try {

      if( !parent.enable_alias_dbase.isSelected()) return;


      if(rid==0) return;
      //if(tg==0) return;
      //if(sys_id==0) return;
      //if(wacn==0) return;
      if(alpha!=null) alpha = alpha.trim();

      if(p4==null) p4 = con.prepareStatement(INSERT_ALIAS_RID_2);
      p4.setInt(1,rid);
      p4.setString(2,"");
      p4.setInt(3,tg);
      p4.setString(4,alpha);
      int rows = p4.executeUpdate();

      if(rows>0) update_alias_table();
    } catch(Exception e) {
      //e.printStackTrace();
    }
    finally {
    }
  }

  ////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////
  public void addAlias(int rid, String alias) {
    try {
      if( !parent.enable_alias_dbase.isSelected()) return;

      //System.out.println("updating: "+UPDATE_ALIAS_RID+"  "+rid+" "+alias);


      if(rid==0) return;
      if(alias==null || alias.length()==0) return;

      if(p5==null) p5 = con.prepareStatement(UPDATE_ALIAS_RID_2);
      p5.setString(1,alias);
      p5.setInt(2,rid);
      int rows = p5.executeUpdate();

      if(rows>0) update_alias_table();
    } catch(Exception e) {
      //e.printStackTrace();
    }
    finally {
    }
  }
  ////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////
  public void alias_delete_rid(int rid) {

    try {


      if(p6==null) p6 = con.prepareStatement(DEL);
      p6.setInt(1,rid);
      p6.executeUpdate();

    } catch(Exception e) {
      //e.printStackTrace();
    }
    finally {
    }
  }
  ////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////
  public int drop_alias_table() {
    int rows = 0;

    try {


      if(p7==null) p7 = con.prepareStatement(DROP);
      rows = p7.executeUpdate();

    } catch(Exception e) {
      //e.printStackTrace();
    }
    finally {
      return rows;
    }
  }
  ////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////
  public int alias_get_rows() {
    int rows = 0;

    try {


      if(p8==null) p8 = con.prepareStatement(SELECT_ALIAS_RID);
      rs = p8.executeQuery();

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
  ////////////////////////////////////////////////////////////////////////////////
  public String getAliasStr(int rid) {
    String res=""; 

    try {


      if(rid==0) return "";

      //System.out.println("select: "+SELECT_ALIAS_RID+"  "+rid);

      if(p9==null) p9 = con.prepareStatement(SELECT_ALIAS_RID_1);
      p9.setInt(1,rid);
      rs = p9.executeQuery();

      if(rs!=null && rs.next()) {
        res = rs.getString("alias");
        //System.out.println("res: "+res+" :");
        //rs.close();
      }

    } catch(Exception e) {
      //e.printStackTrace();
      return "";
    }
    finally {
      return res;
    }
  }
  ////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////
  public void update_alias_table() {
    String res=""; 
    int rid=0;
    int tg=0;
    String alpha=""; 

    try {
      if( parent.alias_table.isEditing() ) return; //don't interrupt an edit
      if( !parent.enable_alias_dbase.isSelected()) return;


      try {
        if( parent.tabbed_pane.getTitleAt(parent.tabbed_pane.getSelectedIndex()).contains("Alias")) {
          //System.out.println("alias tab not selected. skipping table update");
        }
        //System.out.println("update alias table");
      } catch(Exception e) {
      }


      if(parent.sort_rid.isSelected()) { 
        if(p10==null) p10 = con.prepareStatement(SELECT_ALIAS_RID_O2);
        rs = p10.executeQuery();
      }
      else {
        if(p11==null) p11 = con.prepareStatement(SELECT_ALIAS_RID_O1);
        rs = p11.executeQuery();
      }

      int row=0;

      while(rs!=null && rs.next()) {
        res = rs.getString("alias");
        rid = rs.getInt("rid");
        alpha = rs.getString("alphatag");
        tg = rs.getInt("tg");


        parent.addAliasObject( new Integer(rid), row, 0); 
        parent.addAliasObject( res, row, 1); 
        parent.addAliasObject( new Integer(tg), row, 2); 
        parent.addAliasObject( alpha, row, 3); 
        row++;
      }

      for(int i=0;i<10;i++) {
        parent.addAliasObject( null, row, 0); 
        parent.addAliasObject( null, row, 1); 
        parent.addAliasObject( null, row, 2); 
        parent.addAliasObject( null, row, 3); 
        row++;
      }


      //rs.close();

    } catch(Exception e) {
      //e.printStackTrace();
    }
    finally {
      int nrows = alias_get_rows();
      if(parent!=null) ((DefaultTableModel) parent.alias_table.getModel()).setRowCount(nrows+10);
      parent.alias_edit_timeout=0;
    }
  }
  ////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////
  public void alias_update_from_table() {
    try {

      int nrows2 = ((DefaultTableModel) parent.alias_table.getModel()).getRowCount();


      for(int i=0;i<nrows2;i++) {

        Integer rid_s = (Integer) parent.getAliasObject(i, 0);
        String alias_s = (String) parent.getAliasObject(i, 1);
        Integer tg_i = (Integer) parent.getAliasObject(i, 2);
        String alpha_tag = (String) parent.getAliasObject(i, 3);


        int rid = 0;
        if( rid_s!=null) {
          rid = rid_s.intValue(); 
        }
        int tg=0;
        if(tg_i!=null) {
          tg = tg_i.intValue();
        }
        if(alias_s==null) alias_s="";
        if(alpha_tag==null) alpha_tag="";

        if( addRIDAlias(rid, alias_s, tg, alpha_tag) == 0) {
          if(rid!=0 && alias_s!=null) {
            if(p12==null) p12 = con.prepareStatement(UPDATE_ALIAS_RID_3);
            p12.setString(1,alias_s);
            p12.setInt(2,tg);
            p12.setString(3,alpha_tag);
            p12.setInt(4,rid);
            try {
              p12.executeUpdate();
              //System.out.println("update ok");
            } catch(Exception e) {
              e.printStackTrace();
              //System.out.println("update fail");
            }
          }
        }
        else {
          //System.out.println("insert ok");
        }

        parent.setStatus("updating record "+i);
      }

      con.commit();

      update_alias_table();

      int nrows = alias_get_rows();
      if(parent!=null) ((DefaultTableModel) parent.alias_table.getModel()).setRowCount(nrows+10);
    } catch(Exception e) {
      e.printStackTrace();
    }
    finally {
    }
  }
}
