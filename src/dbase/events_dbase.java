
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
import java.lang.*;

public class events_dbase {

  private Connection con=null;
  private String mac_id;
  private Statement stmt = null; 
  private btconfig.BTFrame parent=null;
  private ResultSet rs=null;
  private PreparedStatement p=null;

  private String INSERT_EVENT = "insert into events (TIME, OPCODE, OP_NAME, P1, P2, P3, P4) values (?, ?, ?, ?, ?, ?, ?)";
  private PreparedStatement p1;

  //private String SELECT_EVENT = "select datetime(time,\'unixepoch\',\'localtime\') as date_time,opcode,op_name,p1,p2,p3,p4 from events where opcode!=2 and datetime(time,\'unixepoch\',\'localtime\') > datetime(\'now\',\'localtime\',\'-1 minutes\') order by time";
  private String SELECT_EVENT = "select * from (select datetime(time,\'unixepoch\',\'localtime\') as date_time,opcode,op_name,p1,p2,p3,p4 from events where opcode!=2 order by time DESC LIMIT 64) order by date_time ASC";
  private PreparedStatement p2;

  //keep only the latest SITE_EVT records
  private String PRUNE = "delete from events where rowid not in (select max(rowid) from events group by opcode,p1,p2,p3,p4) and opcode=2";
  private PreparedStatement p3;

  private StringBuffer sb1; 
  private java.text.SimpleDateFormat formatter_date=null;

  ////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////
  public events_dbase(btconfig.BTFrame p) {
    this.parent = p;
    sb1 = new StringBuffer(512000);
    formatter_date = new java.text.SimpleDateFormat( "yyyy-MM-dd" );
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

    if( !parent.enable_event_dbase.isSelected()) return false;

    try {
      String date = formatter_date.format(new java.util.Date() );

      con = DriverManager.getConnection("jdbc:sqlite:btt_"+mac_id+"_"+date+"_"+"events.db");
      //con = DriverManager.getConnection("jdbc:sqlite:btt_"+mac_id+"_events.db");
      con.setAutoCommit(false);

      stmt = con.createStatement();
      stmt.setQueryTimeout(30);  // set timeout to 30 sec.

      create_tables();

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
      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS events (TIME DATETIME, OPCODE INTEGER, OP_NAME TEXT, P1 INTEGER, P2 INTEGER, P3 INTEGER, P4 INTEGER)");
    } catch(Exception e) {
      //e.printStackTrace();
    }
    finally {
    }
  }

  ////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////
  public void addEvent(long time, int op_code, String op_name, int parm1, int parm2, int parm3, int parm4) {

    if( !parent.enable_event_dbase.isSelected()) return;

    int rows=0;
    try {
      if(time==0 || time<0) return;
      if(op_name==null) op_name="";

      if(p1==null) p1 = con.prepareStatement(INSERT_EVENT);

      p1.setLong(1,time);
      p1.setInt(2,op_code);
      p1.setString(3,op_name);
      p1.setInt(4,parm1);
      p1.setInt(5,parm2); 
      p1.setInt(6,parm3);
      p1.setInt(7,parm4); 

      rows = p1.executeUpdate();

    } catch(Exception e) {
      //e.printStackTrace();
    }
    finally {
    }
  }
  ////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////
  public void do_prune() {
    try {
      //prune the SITE_EVT records
      if(p3==null) p3 = con.prepareStatement(PRUNE);
      p3.executeUpdate();
    } catch(Exception e) {
      //e.printStackTrace();
    }
  }
  ////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////
  public String getReport() {
    int rows=0;
    try {


      if(p2==null) p2 = con.prepareStatement(SELECT_EVENT);

      sb1.setLength(0);

      rs = p2.executeQuery();
      while(rs!=null && rs.next()) {
        sb1 = sb1.append(rs.getString("date_time")+","+rs.getInt("opcode")+","+rs.getString("op_name")+","+rs.getInt("p1")+","+rs.getInt("p2")+","+rs.getInt("p3")+","+rs.getInt("p4")+"\n");
      }

      return sb1.toString();

    } catch(Exception e) {
      //e.printStackTrace();
    }
    finally {
    }
    return "";
  }
}
