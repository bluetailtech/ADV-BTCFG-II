
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

import javax.swing.JColorChooser;
import javax.swing.*;
import javax.swing.filechooser.*;
import java.awt.Color;
import java.awt.Font;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;

import java.io.*;


public class displayframe_edit extends javax.swing.JFrame {

  JFontChooser jfc;
  JFileChooser chooser;
java.text.SimpleDateFormat formatter_date;
java.text.SimpleDateFormat formatter_time;
java.text.SimpleDateFormat formatter_date_time;


  helpFrame hf;
  public Boolean did_init=false;
  public colorSelect cs;
  public BTFrame parent;
  public Color col1;
  public Color col2;
  public Color col3;
  public Color col4;
  public Color col5;
  BigText bt1;
  BigText bt2;
  BigText bt3;
  BigText bt4;
  BigText bt5;

  int fsz1=288;
  int fsz2=144;
  int fsz3=72;
  int fsz4=72;
  int fsz5=72;

  int fstyle1 = Font.PLAIN;
  int fstyle2 = Font.PLAIN;
  int fstyle3 = Font.PLAIN;
  int fstyle4 = Font.PLAIN;
  int fstyle5 = Font.PLAIN;

  String fname1="SansSerif";
  String fname2="SansSerif";
  String fname3="SansSerif";
  String fname4="SansSerif";
  String fname5="SansSerif";

  String ts1 ="";
  String ts2 ="";
  String ts3 ="";
  String ts4 ="";
  String ts5 ="";

  float dw=1.0f;

  String prev_date_format;
  String prev_time_format;
  String prev_date_time_format;

    /**
     * Creates new form displayframe_edit
     */
    public displayframe_edit(BTFrame p, BigText b1, BigText b2, BigText b3, BigText b4, BigText b5) {
      initComponents();
      parent = p;

      update_prefs();

      hf = new helpFrame();

      cs = new colorSelect();

      jfc = new JFontChooser();
      jfc.setSize(1024,768);

      bt1 = b1;
      bt2 = b2;
      bt3 = b3;
      bt4 = b4;
      bt5 = b5;

      col1 = new Color(128,0,128);
      col2 = Color.white; 
      col3 = Color.red; 
      col4 = Color.cyan; 
      col5 = Color.yellow; 

      chooser = new JFileChooser();

      setSize(1215,550);

    }

  ///////////////////////////////////////////////////
  ///////////////////////////////////////////////////
  public void update_prefs() {
    try {
      if(parent.prefs!=null) date_format.setText( parent.prefs.get("date_format", "yyyy-MM-dd") );
      if(parent.prefs!=null) time_format.setText( parent.prefs.get("time_format", "HH:mm:ss") );
      if(parent.prefs!=null) date_time_format.setText( parent.prefs.get("date_time_format", "yyyy-MM-dd HH:mm:ss") );

      formatter_date = new java.text.SimpleDateFormat( date_format.getText() );
      formatter_time = new java.text.SimpleDateFormat( time_format.getText() ); 
      formatter_date_time = new java.text.SimpleDateFormat( date_time_format.getText() ); 

      prev_date_format = ""; 
      prev_time_format = ""; 
      prev_date_time_format = ""; 
    } catch(Exception e) {
    }
  }

  ///////////////////////////////////////////////////
  ///////////////////////////////////////////////////
  public String getDateTimeStr() {
    try {
      java.util.Date now = new java.util.Date();
      return formatter_date_time.format(now);
    } catch(Exception e) {
    }
    return "";
  }
  ///////////////////////////////////////////////////
  ///////////////////////////////////////////////////
  public String getDateStr() {
    try {
      java.util.Date now = new java.util.Date();
      return formatter_date.format(now);
    } catch(Exception e) {
    }
    return "";
  }
  ///////////////////////////////////////////////////
  ///////////////////////////////////////////////////
  public String getTimeStr() {
    try {
      java.util.Date now = new java.util.Date();
      return formatter_time.format(now);
    } catch(Exception e) {
    }
    return "";
  }

  ///////////////////////////////////////////////////
  ///////////////////////////////////////////////////
  String do_subs(String s1, boolean is_tg_log) {
    String s2 = s1; 

    try {


      try {
        if( !date_format.getText().equals( prev_date_format ) ) {
          formatter_date = new java.text.SimpleDateFormat( date_format.getText() );
          parent.prefs.put("date_format", date_format.getText() );
        }
        if( !time_format.getText().equals( prev_time_format ) ) {
          formatter_time = new java.text.SimpleDateFormat( time_format.getText() ); 
          parent.prefs.put("time_format", time_format.getText() );
        }
        if( !date_time_format.getText().equals( prev_date_time_format ) ) {
          formatter_date_time = new java.text.SimpleDateFormat( date_time_format.getText() ); 
          parent.prefs.put("date_time_format", date_time_format.getText() );
        }
      } catch(Exception e) {
      }

      java.util.Date now = new java.util.Date();
      String cdate = formatter_date.format(now);
      String ctime = formatter_time.format(now);

      s2 = s2.replaceAll(Matcher.quoteReplacement("$DATE$"), cdate.trim() );
      s2 = s2.replaceAll(Matcher.quoteReplacement("$TIME$"), ctime.trim() );
        if(s2==null) s2 = s1;

      String f = parent.freqval;

      String wacn = "";
      String sysid = "";
      String nac = ""; 
      String siteid = ""; 
      String cc_freq = ""; 
      String rfid = ""; 
      String rid = "";
      String rid_alias = "";
      String evm_p = "";



      if(parent.sysid.getText()!=null) sysid = parent.sysid.getText().trim();
      if(parent.nac.getText()!=null) nac = parent.nac.getText().trim();
      if(parent.siteid.getText()!=null) siteid = parent.siteid.getText().trim();
      if(parent.rfid.getText()!=null) rfid = parent.rfid.getText().trim();
      //if(parent.freq.getText()!=null) cc_freq = parent.freq.getText().trim();

      rid = parent.src_uid_str;

      if(rid==null) rid="";

      //if( sysid.contains("SYS_ID:") && sysid.length()>7) sysid = sysid.substring(7,sysid.length());
      if( nac.contains("NAC:") && nac.length()>4) nac = nac.substring(4,nac.length());
      if( rfid.contains("RFSS ID:") && rfid.length()>8) rfid = rfid.substring(8,rfid.length());
      if( siteid.contains("SITE ID:") && siteid.length()>8) siteid = siteid.substring(8,siteid.length());
      //if( cc_freq.contains("Freq:") && cc_freq.length()>5) cc_freq = cc_freq.substring(5,cc_freq.length());

      try {
        wacn = String.format("0x%05X", Integer.valueOf(parent.current_wacn_id).intValue());
      } catch(Exception e) {
      }
      try {
        sysid = String.format("0x%03X", Integer.valueOf(parent.current_sys_id).intValue());
      } catch(Exception e) {
      }

      s2 = s2.replaceAll(Matcher.quoteReplacement("$WACN$"), wacn.trim() );
        if(s2==null) s2 = s1;
      s2 = s2.replaceAll(Matcher.quoteReplacement("$SYS_ID$"), sysid.trim() );
        if(s2==null) s2 = s1;
      s2 = s2.replaceAll(Matcher.quoteReplacement("$NAC$"), nac.trim() );
        if(s2==null) s2 = s1;
      s2 = s2.replaceAll(Matcher.quoteReplacement("$SITE_ID$"), siteid.trim() );
        if(s2==null) s2 = s1;
      s2 = s2.replaceAll(Matcher.quoteReplacement("$RFSS_ID$"), rfid.trim() );
        if(s2==null) s2 = s1;
      s2 = s2.replaceAll(Matcher.quoteReplacement("$CC_FREQ$"), String.format("%3.6f", parent.cc_freq) );
        if(s2==null) s2 = s1;

      s2 = s2.replaceAll(Matcher.quoteReplacement("$SYNC_COUNT$"), String.format("%d", parent.global_sync_count) );
        if(s2==null) s2 = s1;

      s2 = s2.replaceAll(Matcher.quoteReplacement("$RESET_COUNT$"), String.format("%d", parent.reset_count) );
        if(s2==null) s2 = s1;


      try {
        if( (parent.src_uid!=0 || !is_tg_log) && (parent.mode_b==1 || parent.mode_b==(byte) 0x81 
          || parent.mode_b==5 || parent.mode_b==2 ) ) {

          rid_alias = parent.current_alias;

          if(rid_alias==null) rid_alias="";

          rid = String.format("%d", parent.src_uid);

          if(!rid.equals("0")) {
            s2 = s2.replaceAll(Matcher.quoteReplacement("$RID$"), rid.trim() );
              if(s2==null) s2 = s1;
          }
          else {
            rid = "";
            s2 = s2.replaceAll(Matcher.quoteReplacement("$RID$"), "" );
              if(s2==null) s2 = s1;
          }
        }
        else {
          rid = "";
          s2 = s2.replaceAll(Matcher.quoteReplacement("$RID$"), " RID: NA" );
            if(s2==null) s2 = s1;
        }
      } catch(Exception e) {
      }

      if( (parent.mode_b==1 || parent.mode_b==(byte) 0x81 || parent.mode_b==5 || parent.mode_b==2 ) ) {

        s2 = s2.replaceAll(Matcher.quoteReplacement("$RID_ALIAS$"), rid_alias.trim() );
          if(s2==null) s2 = s1;
      }
      else {
        s2 = s2.replaceAll(Matcher.quoteReplacement("$RID_ALIAS$"), " ALIAS: NA" );
          if(s2==null) s2 = s1;
      }

      try {
        s2 = s2.replaceAll(Matcher.quoteReplacement("$ERR_RATE$"), String.format("%3.3f", parent.erate) );
        if(s2==null) s2 = s1;
      } catch(Exception e) {
      }

      if(parent.tg_zone>0 && parent.tg_zone<=65535) {
        try {
          s2 = s2.replaceAll(Matcher.quoteReplacement("$ZONE$"), String.format("%d", parent.tg_zone) );
          if(s2==null) s2 = s1;
        } catch(Exception e) {
        }
        try {
          s2 = s2.replaceAll(Matcher.quoteReplacement("$ZONE_ALIAS$"), String.format("%s", parent.tg_zone_alias) );
          if(s2==null) s2 = s1;
        } catch(Exception e) {
        }
      }
      else {
        try {
          s2 = s2.replaceAll(Matcher.quoteReplacement("$ZONE$"), ""); 
          if(s2==null) s2 = s1;
        } catch(Exception e) {
        }
        try {
          s2 = s2.replaceAll(Matcher.quoteReplacement("$ZONE_ALIAS$"), ""); 
          if(s2==null) s2 = s1;
        } catch(Exception e) {
        }
      }

      try {
        if(parent.edit_alias1!=null) {
            parent.edit_alias1.setEnabled(true);
            parent.edit_alias.setEnabled(true);
          if(rid!=null && rid.length()>0 && Integer.valueOf(rid).intValue()!=0) {
            //parent.edit_alias1.setEnabled(true);
            //parent.edit_alias.setEnabled(true);
          }
          else {
            //parent.edit_alias1.setEnabled(false);
            //parent.edit_alias.setEnabled(false);
          }
        }
      } catch(Exception e) {
      }

      if( parent.demod_type==0) {
      s2 = s2.replaceAll(Matcher.quoteReplacement("$DEMOD$"), "LSM" );
        if(s2==null) s2 = s1;
      }
      else if( parent.demod_type==1) {
      s2 = s2.replaceAll(Matcher.quoteReplacement("$DEMOD$"), "CQPSK/C4FM" );
        if(s2==null) s2 = s1;
      }
      else if( parent.demod_type==3) {

        if( parent.mode_b==7 || parent.mode_b==8) {
          s2 = s2.replaceAll(Matcher.quoteReplacement("$DEMOD$"), "AM" );
            if(s2==null) s2 = s1;
        }
        else { 
          s2 = s2.replaceAll(Matcher.quoteReplacement("$DEMOD$"), "FM" );
            if(s2==null) s2 = s1;
        }
      }

//#define OP_MODE_P25 1
//#define OP_MODE_DMR 2
//#define OP_MODE_NXDN4800 3
//#define OP_MODE_FMNB 4
//#define OP_MODE_TDMA_CC 5
//#define OP_MODE_NXDN9600 6
//#define OP_MODE_AM 7
//#define OP_MODE_AM_AGC 8
      if( parent.mode_b==3) {
      s2 = s2.replaceAll(Matcher.quoteReplacement("$P25_MODE$"), "NXDN4800" );
        if(s2==null) s2 = s1;
      }
      else if( parent.mode_b==4) {
      s2 = s2.replaceAll(Matcher.quoteReplacement("$P25_MODE$"), "FMNB" );
        if(s2==null) s2 = s1;
      }
      else if( parent.mode_b==7 || parent.mode_b==8) {
      s2 = s2.replaceAll(Matcher.quoteReplacement("$P25_MODE$"), "AM" );
        if(s2==null) s2 = s1;
      }
      else if( parent.mode_b==2) {
      s2 = s2.replaceAll(Matcher.quoteReplacement("$P25_MODE$"), "DMR" );
        if(s2==null) s2 = s1;
      }
      else if(parent.mode_b==1) {
      s2 = s2.replaceAll(Matcher.quoteReplacement("$P25_MODE$"), "P25-P1" );
        if(s2==null) s2 = s1;
      }
      else if( parent.mode_b== (byte) 0x81) {
      s2 = s2.replaceAll(Matcher.quoteReplacement("$P25_MODE$"), "P25-P2" );
        if(s2==null) s2 = s1;
      }
      else if( parent.mode_b==5) {
      s2 = s2.replaceAll(Matcher.quoteReplacement("$P25_MODE$"), "TDMA CC" );
        if(s2==null) s2 = s1;
      }
      else if( parent.mode_b==6) {
      s2 = s2.replaceAll(Matcher.quoteReplacement("$P25_MODE$"), "NXDN9600" );
        if(s2==null) s2 = s1;
      }

      try {
        evm_p = String.format("%3.0f", parent.current_evm_percent);
      } catch(Exception e) {
        evm_p="";
      }

      s2 = s2.replaceAll(Matcher.quoteReplacement("$EVM_P$"), evm_p.trim() );
        if(s2==null) s2 = s1;

      String sysname = ""; 
      //String sysname = parent.system_alias.getText();
      if(sysname==null || sysname.length()==0) sysname="SYS_NAME";
      //s2 = s2.replaceAll(Matcher.quoteReplacement("$SYS_NAME$"), sysname.trim() );
      s2 = s2.replaceAll(Matcher.quoteReplacement("$SYS_NAME$"), parent.sys_name.trim() );
        if(s2==null) s2 = s1;

      s2 = s2.replaceAll(Matcher.quoteReplacement("$V_FREQ$"), String.format("%3.6f", parent.v_freq) );
        if(s2==null) s2 = s1;

      s2 = s2.replaceAll(Matcher.quoteReplacement("$EVENT$"), parent.prev_p25_evt );
        if(s2==null) s2 = s1;

      if(f!=null) {
        f = f.trim();
        if( f.contains("MHz") ) f = f.substring(0,f.length()-4);
        //System.out.println("freqval: "+f);
        f = f.replace(","," ");

        if(parent.on_control_freq==0) {
          s2 = s2.replaceAll(Matcher.quoteReplacement("$V_FREQ$"), f.trim() );
          s2 = s2.replaceAll(Matcher.quoteReplacement("$LCN$"), parent.rf_channel.trim() );
          if(s2==null) s2 = s1;
        }
        else {
          s2 = s2.replaceAll(Matcher.quoteReplacement("$V_FREQ$"), cc_freq.trim() );
          try {
            s2 = s2.replaceAll(Matcher.quoteReplacement("$LCN$"), new Integer(parent.cc_lcn).toString().trim() );
          } catch(Exception e) {
          }
          if(s2==null) s2 = s1;
        }
      }

      String frequency= String.format("%3.6f", parent.current_freq/1e6);
      s2 = s2.replaceAll(Matcher.quoteReplacement("$FREQ$"), frequency.trim() );
      if(s2==null) s2 = s1;


      String tone_a_freq = String.format("%3.2f", parent.tone_a_freq);
      s2 = s2.replaceAll(Matcher.quoteReplacement("$TONEA_FREQ$"), tone_a_freq.trim() );
      if(s2==null) s2 = s1;

      String tone_b_freq = String.format("%3.2f", parent.tone_b_freq);
      s2 = s2.replaceAll(Matcher.quoteReplacement("$TONEB_FREQ$"), tone_b_freq.trim() );
      if(s2==null) s2 = s1;

      String tone_a_idx = String.format("%d", parent.tone_a_idx);
      s2 = s2.replaceAll(Matcher.quoteReplacement("$TONEA_IDX$"), tone_a_idx.trim() );
      if(s2==null) s2 = s1;

      String tone_b_idx = String.format("%d", parent.tone_b_idx);
      s2 = s2.replaceAll(Matcher.quoteReplacement("$TONEB_IDX$"), tone_b_idx.trim() );
      if(s2==null) s2 = s1;

      try {
        s2 = s2.replaceAll(Matcher.quoteReplacement("$BLKS_SEC$"), new Integer(parent.blks_per_sec).toString().trim() );
      } catch(Exception e) {
      }

      if(parent.talkgroup_name!=null && f!=null && (parent.mode_b==1 || parent.mode_b==(byte) 0x81 || parent.mode_b==5 || parent.mode_b==2) ) {
        s2 = s2.replaceAll(Matcher.quoteReplacement("$TG_NAME$"), parent.talkgroup_name.trim() );
        if(s2==null) s2 = s1;
      }
      else {
        //s2 = s2.replaceAll(Matcher.quoteReplacement("$TG_NAME$"), " TG: NA" );
        s2 = s2.replaceAll(Matcher.quoteReplacement("$TG_NAME$"), " " );
        if(s2==null) s2 = s1;
      }

      if(parent.current_talkgroup!=null && f!=null && (parent.mode_b==1 || parent.mode_b==(byte) 0x81 || parent.mode_b==5 || parent.mode_b==2) ) {
        s2 = s2.replaceAll(Matcher.quoteReplacement("$TG_ID$"), parent.current_talkgroup.trim().trim() );
        if(s2==null) s2 = s1;
      }
      else {
        //s2 = s2.replaceAll(Matcher.quoteReplacement("$TG_ID$"), " ID: NA" );
        s2 = s2.replaceAll(Matcher.quoteReplacement("$TG_ID$"), " " );
        if(s2==null) s2 = s1;
      }

      if(parent.rssi!=null) {
        String rssi_d = String.format( "%-3d", Integer.valueOf(parent.rssi) );
        if(rssi_d.length()==3) rssi_d = rssi_d+" ";
        s2 = s2.replaceAll(Matcher.quoteReplacement("$RSSI$"), rssi_d.trim());
        if(s2==null) s2 = s1;
      }

      String tdma_slot_d = String.format( "%d", Integer.valueOf(parent.tdma_slot) );
      s2 = s2.replaceAll(Matcher.quoteReplacement("$TDMA_SLOT$"), tdma_slot_d.trim());
      if(s2==null) s2 = s1;



    } catch(Exception e) {
      e.printStackTrace();
    }

    if(s2==null) s2=s1;

    return s2;
  }
  ////////////////////////////////////////
  ////////////////////////////////////////
    public void update_colors() {

      Boolean is_analog=false;
      Boolean tg_active=false;
      Boolean trunked = false;

      if( parent.prefs!=null ) {

        String adv_init = parent.prefs.get("adv_p25rx_displayinit", "no");
        if( adv_init.equals("no") ) {
          reset_to_defaults();
          parent.prefs.put("adv_p25rx_displayinit", "yes");
        }


        col1 = new Color( parent.prefs.getInt("dfcol1", new Color(128,0,128).getRGB() ) );
        col2 = new Color( parent.prefs.getInt("dfcol2", Color.white.getRGB() ));
        col3 = new Color( parent.prefs.getInt("dfcol3", Color.red.getRGB() ));
        col4 = new Color( parent.prefs.getInt("dfcol4", Color.cyan.getRGB() ));
        col5 = new Color( parent.prefs.getInt("dfcol5", Color.yellow.getRGB() ));

        Boolean b = parent.prefs.getBoolean("dfen1",true); 
        if(!b) col1 = Color.black;
        en1.setSelected(b);

        b = parent.prefs.getBoolean("dfen2",true); 
        if(!b) col2 = Color.black;
        en2.setSelected(b);

        b = parent.prefs.getBoolean("dfen3",true); 
        if(!b) col3 = Color.black;
        en3.setSelected(b);

        b = parent.prefs.getBoolean("dfen4",true); 
        if(!b) col4 = Color.black;
        en4.setSelected(b);

        b = parent.prefs.getBoolean("dfen5",true); 
        if(!b) col5 = Color.black;
        en5.setSelected(b);


        fname1 = parent.prefs.get("df_font1", "Serif"); 
        fname2 = parent.prefs.get("df_font2", "Serif"); 
        fname3 = parent.prefs.get("df_font3", "Serif"); 
        fname4 = parent.prefs.get("df_font4", "Serif"); 
        fname5 = parent.prefs.get("df_font5", "Serif"); 

        fsz1 = parent.prefs.getInt("df_font_size1", 288);
        fsz2 = parent.prefs.getInt("df_font_size2", 144);
        fsz3 = parent.prefs.getInt("df_font_size3", 72);
        fsz4 = parent.prefs.getInt("df_font_size4", 72);
        fsz5 = parent.prefs.getInt("df_font_size5", 72);

        fstyle1 = parent.prefs.getInt("df_font_style1", Font.PLAIN);
        fstyle2 = parent.prefs.getInt("df_font_style2", Font.PLAIN);
        fstyle3 = parent.prefs.getInt("df_font_style3", Font.PLAIN);
        fstyle4 = parent.prefs.getInt("df_font_style4", Font.PLAIN);
        fstyle5 = parent.prefs.getInt("df_font_style5", Font.PLAIN);

        ts1 = parent.prefs.get("dftok1", "$TG_NAME$");
        ts2 = parent.prefs.get("dftok2", "TGID $TG_ID$");
        ts3 = parent.prefs.get("dftok3", "$FREQ$");
        ts4 = parent.prefs.get("dftok4", "$SYS_NAME$");
        ts5 = parent.prefs.get("dftok5", "WACN: $WACN$  SYS_ID: $SYS_ID$  NAC: $NAC$");

        clrnv1.setSelected( parent.prefs.getBoolean("clrnv1", false) );
        clrnv2.setSelected( parent.prefs.getBoolean("clrnv2", false) );
        clrnv3.setSelected( parent.prefs.getBoolean("clrnv3", false) );
        clrnv4.setSelected( parent.prefs.getBoolean("clrnv4", false) );
        clrnv5.setSelected( parent.prefs.getBoolean("clrnv5", false) );

        clr_analog1.setSelected( parent.prefs.getBoolean("clr_analog1", false) );
        clr_analog2.setSelected( parent.prefs.getBoolean("clr_analog2", false) );
        clr_analog3.setSelected( parent.prefs.getBoolean("clr_analog3", false) );
        clr_analog4.setSelected( parent.prefs.getBoolean("clr_analog4", false) );
        clr_analog5.setSelected( parent.prefs.getBoolean("clr_analog5", false) );


      }

      if( !did_init ) {
        did_init=true;

        tok1.setText(ts1);
        tok2.setText(ts2);
        tok3.setText(ts3);
        tok4.setText(ts4);
        tok5.setText(ts5);

        dw = parent.prefs.getFloat("dwidth", 1.0f);
        dwidth.setText( String.format("%3.2f", dw) );
      }

      String tts1 = do_subs(ts1,false);
      String tts2 = do_subs(ts2,false);
      String tts3 = do_subs(ts3,false);
      String tts4 = do_subs(ts4,false);
      String tts5 = do_subs(ts5,false);

      try {
        if(parent.mode_b==1 || parent.mode_b==(byte) 0x81 || parent.mode_b==2 || parent.mode_b==5) {
          trunked=true;
          is_analog=false;
        }
        else {
        //if( parent.mode_b==3 || parent.mode_b==4 || parent.mode_b==7 || parent.mode_b==8) {
          is_analog=true;
          trunked=false;
        }

        tg_active= parent.tg_active;

        if(  (clrnv1.isSelected() && ((trunked && parent.on_control_freq==1) || (is_analog && !tg_active))) ||
             (is_analog && clr_analog1.isSelected()) ||
             (clrnv1.isSelected() && trunked && (parent.c_tg==0 || parent.c_tg==65535) ) 
         ) {
          tts1=" ";
          col1 = Color.black;
          //System.out.println("got here "+parent.mode_b+" "+tg_active);
        }
        if(  (clrnv2.isSelected() && ((trunked && parent.on_control_freq==1) || (is_analog && !tg_active))) ||
             (is_analog && clr_analog2.isSelected()) ||
             (clrnv2.isSelected() && trunked && (parent.c_tg==0 || parent.c_tg==65535) ) 

         ) {
          tts2=" ";
          col2 = Color.black;
        }
        if(  (clrnv3.isSelected() && ((trunked && parent.on_control_freq==1) || (is_analog && !tg_active))) ||
             (is_analog && clr_analog3.isSelected()) ||
             (clrnv3.isSelected() && trunked && (parent.c_tg==0 || parent.c_tg==65535) ) 

         ) {
          tts3=" ";
          col3 = Color.black;
        }
        if(  (clrnv4.isSelected() && ((trunked && parent.on_control_freq==1) || (is_analog && !tg_active))) ||
             (is_analog && clr_analog4.isSelected()) ||
             (clrnv4.isSelected() && trunked && (parent.c_tg==0 || parent.c_tg==65535) ) 

         ) {
          tts4=" ";
          col4 = Color.black;
        }
        if(  (clrnv5.isSelected() && ((trunked && parent.on_control_freq==1) || (is_analog && !tg_active))) ||
             (is_analog && clr_analog5.isSelected()) ||
             (clrnv5.isSelected() && trunked && (parent.c_tg==0 || parent.c_tg==65535) ) 

         ) {
          tts5=" ";
          col5 = Color.black;
        }
      } catch(Exception e) {
      }

      bt1.setText(tts1);
      bt2.setText(tts2);
      bt3.setText(tts3);
      bt4.setText(tts4);
      bt5.setText(tts5);

      dw = Float.valueOf( dwidth.getText() );
      bt1.setDWidth(dw);
      bt2.setDWidth(dw);
      bt3.setDWidth(dw);
      bt4.setDWidth(dw);
      bt5.setDWidth(dw);

      bt1.setFont( fname1, fstyle1, fsz1);
      bt2.setFont( fname2, fstyle2, fsz2);
      bt3.setFont( fname3, fstyle3, fsz3);
      bt4.setFont( fname4, fstyle4, fsz4);
      bt5.setFont( fname5, fstyle5, fsz5);

      bt1.setColor(col1);
      bt2.setColor(col2);
      bt3.setColor(col3);
      bt4.setColor(col4);
      bt5.setColor(col5);

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
        jPanel2 = new javax.swing.JPanel();
        tok1 = new javax.swing.JTextField();
        selfont1 = new javax.swing.JButton();
        dvcol1 = new javax.swing.JButton();
        en1 = new javax.swing.JCheckBox();
        clrnv1 = new javax.swing.JCheckBox();
        clr_analog1 = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        tok2 = new javax.swing.JTextField();
        selfont2 = new javax.swing.JButton();
        dvcol2 = new javax.swing.JButton();
        en2 = new javax.swing.JCheckBox();
        clrnv2 = new javax.swing.JCheckBox();
        clr_analog2 = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        tok3 = new javax.swing.JTextField();
        selfont3 = new javax.swing.JButton();
        dvcol3 = new javax.swing.JButton();
        en3 = new javax.swing.JCheckBox();
        clrnv3 = new javax.swing.JCheckBox();
        clr_analog3 = new javax.swing.JCheckBox();
        jPanel5 = new javax.swing.JPanel();
        tok4 = new javax.swing.JTextField();
        selfont4 = new javax.swing.JButton();
        dvcol4 = new javax.swing.JButton();
        en4 = new javax.swing.JCheckBox();
        clrnv4 = new javax.swing.JCheckBox();
        clr_analog4 = new javax.swing.JCheckBox();
        jPanel6 = new javax.swing.JPanel();
        tok5 = new javax.swing.JTextField();
        selfont5 = new javax.swing.JButton();
        dvcol5 = new javax.swing.JButton();
        en5 = new javax.swing.JCheckBox();
        clrnv5 = new javax.swing.JCheckBox();
        clr_analog5 = new javax.swing.JCheckBox();
        jPanel9 = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        date_format = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        time_format = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        date_time_format = new javax.swing.JTextField();
        jPanel8 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        dwidth = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        close = new javax.swing.JButton();
        resettodefault = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        export_df = new javax.swing.JButton();
        import_df = new javax.swing.JButton();
        showkeyw = new javax.swing.JButton();
        saveconfig = new javax.swing.JButton();

        jPanel1.setLayout(new java.awt.GridLayout(7, 1));

        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        tok1.setColumns(60);
        tok1.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N
        tok1.setText("$TG_NAME$");
        tok1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                fs4KeyTyped(evt);
            }
        });
        jPanel2.add(tok1);

        selfont1.setText("Select Font");
        selfont1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selfont1ActionPerformed(evt);
            }
        });
        jPanel2.add(selfont1);

        dvcol1.setText("Edit Color");
        dvcol1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dvcol1ActionPerformed(evt);
            }
        });
        jPanel2.add(dvcol1);

        en1.setSelected(true);
        en1.setText("Enable");
        en1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                en1ActionPerformed(evt);
            }
        });
        jPanel2.add(en1);

        clrnv1.setText("Clear On No Voice");
        clrnv1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clrnv1ActionPerformed(evt);
            }
        });
        jPanel2.add(clrnv1);

        clr_analog1.setText("Clear On Analog");
        clr_analog1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clr_analog1ActionPerformed(evt);
            }
        });
        jPanel2.add(clr_analog1);

        jPanel1.add(jPanel2);

        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        tok2.setColumns(60);
        tok2.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N
        tok2.setText("$TG_ID$");
        tok2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                fs4KeyTyped(evt);
            }
        });
        jPanel3.add(tok2);

        selfont2.setText("Select Font");
        selfont2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selfont2ActionPerformed(evt);
            }
        });
        jPanel3.add(selfont2);

        dvcol2.setText("Edit Color");
        dvcol2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dvcol2ActionPerformed(evt);
            }
        });
        jPanel3.add(dvcol2);

        en2.setSelected(true);
        en2.setText("Enable");
        en2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                en2ActionPerformed(evt);
            }
        });
        jPanel3.add(en2);

        clrnv2.setText("Clear On No Voice");
        clrnv2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clrnv2ActionPerformed(evt);
            }
        });
        jPanel3.add(clrnv2);

        clr_analog2.setText("Clear On Analog");
        clr_analog2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clr_analog2ActionPerformed(evt);
            }
        });
        jPanel3.add(clr_analog2);

        jPanel1.add(jPanel3);

        jPanel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel4.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        tok3.setColumns(60);
        tok3.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N
        tok3.setText("$FREQ$");
        tok3.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                fs4KeyTyped(evt);
            }
        });
        jPanel4.add(tok3);

        selfont3.setText("Select Font");
        selfont3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selfont3ActionPerformed(evt);
            }
        });
        jPanel4.add(selfont3);

        dvcol3.setText("Edit Color");
        dvcol3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dvcol3ActionPerformed(evt);
            }
        });
        jPanel4.add(dvcol3);

        en3.setSelected(true);
        en3.setText("Enable");
        en3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                en3ActionPerformed(evt);
            }
        });
        jPanel4.add(en3);

        clrnv3.setText("Clear On No Voice");
        clrnv3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clrnv3ActionPerformed(evt);
            }
        });
        jPanel4.add(clrnv3);

        clr_analog3.setText("Clear On Analog");
        clr_analog3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clr_analog3ActionPerformed(evt);
            }
        });
        jPanel4.add(clr_analog3);

        jPanel1.add(jPanel4);

        jPanel5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel5.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        tok4.setColumns(60);
        tok4.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N
        tok4.setText("$SYS_NAME$");
        tok4.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                fs4KeyTyped(evt);
            }
        });
        jPanel5.add(tok4);

        selfont4.setText("Select Font");
        selfont4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selfont4ActionPerformed(evt);
            }
        });
        jPanel5.add(selfont4);

        dvcol4.setText("Edit Color");
        dvcol4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dvcol4ActionPerformed(evt);
            }
        });
        jPanel5.add(dvcol4);

        en4.setSelected(true);
        en4.setText("Enable");
        en4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                en4ActionPerformed(evt);
            }
        });
        jPanel5.add(en4);

        clrnv4.setText("Clear On No Voice");
        clrnv4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clrnv4ActionPerformed(evt);
            }
        });
        jPanel5.add(clrnv4);

        clr_analog4.setText("Clear On Analog");
        clr_analog4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clr_analog4ActionPerformed(evt);
            }
        });
        jPanel5.add(clr_analog4);

        jPanel1.add(jPanel5);

        jPanel6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel6.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        tok5.setColumns(60);
        tok5.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N
        tok5.setText("$WACN$ $SYS_ID$ $NAC$");
        tok5.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                fs4KeyTyped(evt);
            }
        });
        jPanel6.add(tok5);

        selfont5.setText("Select Font");
        selfont5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selfont5ActionPerformed(evt);
            }
        });
        jPanel6.add(selfont5);

        dvcol5.setText("Edit Color");
        dvcol5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dvcol5ActionPerformed(evt);
            }
        });
        jPanel6.add(dvcol5);

        en5.setSelected(true);
        en5.setText("Enable");
        en5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                en5ActionPerformed(evt);
            }
        });
        jPanel6.add(en5);

        clrnv5.setText("Clear On No Voice");
        clrnv5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clrnv5ActionPerformed(evt);
            }
        });
        jPanel6.add(clrnv5);

        clr_analog5.setText("Clear On Analog");
        clr_analog5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clr_analog5ActionPerformed(evt);
            }
        });
        jPanel6.add(clr_analog5);

        jPanel1.add(jPanel6);

        jLabel3.setText("Date Format");
        jPanel10.add(jLabel3);

        date_format.setColumns(12);
        date_format.setText("yyyy-MM-dd");
        date_format.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                date_formatActionPerformed(evt);
            }
        });
        jPanel10.add(date_format);

        jLabel4.setText("Time Format");
        jPanel10.add(jLabel4);

        time_format.setColumns(12);
        time_format.setText("HH:mm:ss");
        time_format.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                time_formatActionPerformed(evt);
            }
        });
        jPanel10.add(time_format);

        jPanel9.add(jPanel10);

        jLabel1.setText("Combined Date-Time Format");
        jPanel9.add(jLabel1);

        date_time_format.setColumns(20);
        date_time_format.setText("yyyy-MM-dd HH:mm:ss");
        date_time_format.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                date_time_formatActionPerformed(evt);
            }
        });
        jPanel9.add(date_time_format);

        jPanel1.add(jPanel9);

        jPanel8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel6.setText("Display Width Relative To Window");
        jPanel8.add(jLabel6);

        dwidth.setColumns(4);
        dwidth.setText("1.0");
        dwidth.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                dwidthKeyReleased(evt);
            }
        });
        jPanel8.add(dwidth);

        jSeparator1.setPreferredSize(new java.awt.Dimension(150, 0));
        jPanel8.add(jSeparator1);

        close.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        close.setText("Close");
        close.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeActionPerformed(evt);
            }
        });
        jPanel8.add(close);

        resettodefault.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        resettodefault.setText("Reset To Defaults");
        resettodefault.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resettodefaultActionPerformed(evt);
            }
        });
        jPanel8.add(resettodefault);

        jPanel1.add(jPanel8);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        jPanel7.setLayout(new java.awt.GridLayout(10, 1, 25, 25));

        export_df.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        export_df.setText("Export");
        export_df.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                export_dfActionPerformed(evt);
            }
        });
        jPanel7.add(export_df);

        import_df.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        import_df.setText("Import");
        import_df.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                import_dfActionPerformed(evt);
            }
        });
        jPanel7.add(import_df);

        showkeyw.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        showkeyw.setText("Show Key Words");
        showkeyw.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showkeywActionPerformed(evt);
            }
        });
        jPanel7.add(showkeyw);

        saveconfig.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        saveconfig.setText("Save Config");
        saveconfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveconfigActionPerformed(evt);
            }
        });
        jPanel7.add(saveconfig);

        getContentPane().add(jPanel7, java.awt.BorderLayout.EAST);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void export_dfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_export_dfActionPerformed
      try {

        FileNameExtensionFilter filter = new FileNameExtensionFilter( "displayview file", "dvp");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showDialog(parent, "Export Display View Profile .DVP file");

        ObjectOutputStream oos;

        if(returnVal == JFileChooser.APPROVE_OPTION) {
          File file = chooser.getSelectedFile();
          oos = new ObjectOutputStream( new FileOutputStream(file) );

          oos.writeInt((int) 100); //version

          oos.writeInt(col1.getRGB());
          oos.writeInt(col2.getRGB());
          oos.writeInt(col3.getRGB());
          oos.writeInt(col4.getRGB());
          oos.writeInt(col5.getRGB());


          Boolean b = parent.prefs.getBoolean("dfen1",true); 
          oos.writeBoolean(b);
          b = parent.prefs.getBoolean("dfen2",true); 
          oos.writeBoolean(b);
          b = parent.prefs.getBoolean("dfen3",true); 
          oos.writeBoolean(b);
          b = parent.prefs.getBoolean("dfen4",true); 
          oos.writeBoolean(b);
          b = parent.prefs.getBoolean("dfen5",true); 
          oos.writeBoolean(b);

          oos.writeUTF(fname1);
          oos.writeUTF(fname2);
          oos.writeUTF(fname3);
          oos.writeUTF(fname4);
          oos.writeUTF(fname5);

          oos.writeInt(fsz1);
          oos.writeInt(fsz2);
          oos.writeInt(fsz3);
          oos.writeInt(fsz4);
          oos.writeInt(fsz5);

          oos.writeInt(fstyle1);
          oos.writeInt(fstyle2);
          oos.writeInt(fstyle3);
          oos.writeInt(fstyle4);
          oos.writeInt(fstyle5);

          oos.writeUTF(ts1);
          oos.writeUTF(ts2);
          oos.writeUTF(ts3);
          oos.writeUTF(ts4);
          oos.writeUTF(ts5);

          b = parent.prefs.getBoolean("clrnv1", false);
          oos.writeBoolean(b);
          b = parent.prefs.getBoolean("clrnv2", false);
          oos.writeBoolean(b);
          b = parent.prefs.getBoolean("clrnv3", false);
          oos.writeBoolean(b);
          b = parent.prefs.getBoolean("clrnv4", false);
          oos.writeBoolean(b);
          b = parent.prefs.getBoolean("clrnv5", false);
          oos.writeBoolean(b);

          oos.writeFloat(dw);

          oos.flush();
          oos.close();

        }

      } catch(Exception e) {
        e.printStackTrace();
      }

    }//GEN-LAST:event_export_dfActionPerformed

    private void dvcol1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dvcol1ActionPerformed
      //cs.setVisible(true);
      Color color = JColorChooser.showDialog(parent, "Color1", col1); 
      if(color!=null) col1=color;
      if( parent.prefs!=null && color!=null) {
        parent.prefs.putInt("dfcol1",  color.getRGB() );
      }
      update_colors();
    }//GEN-LAST:event_dvcol1ActionPerformed

    private void dvcol2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dvcol2ActionPerformed
      //cs.setVisible(true);
      Color color = JColorChooser.showDialog(parent, "Color1", col2); 
      if(color!=null) col2=color;
      if( parent.prefs!=null && color!=null) {
        parent.prefs.putInt("dfcol2",  color.getRGB() );
      }
      update_colors();
    }//GEN-LAST:event_dvcol2ActionPerformed

    private void dvcol3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dvcol3ActionPerformed
      //cs.setVisible(true);
      Color color = JColorChooser.showDialog(parent, "Color1", col3); 
      if(color!=null) col3=color;
      if( parent.prefs!=null && color!=null) {
        parent.prefs.putInt("dfcol3",  color.getRGB() );
      }
      update_colors();
    }//GEN-LAST:event_dvcol3ActionPerformed

    private void dvcol4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dvcol4ActionPerformed
      //cs.setVisible(true);
      Color color = JColorChooser.showDialog(parent, "Color1", col4); 
      if(color!=null) col4=color;
      if( parent.prefs!=null && color!=null) {
        parent.prefs.putInt("dfcol4",  color.getRGB() );
      }
      update_colors();
    }//GEN-LAST:event_dvcol4ActionPerformed

    private void dvcol5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dvcol5ActionPerformed
      //cs.setVisible(true);
      Color color = JColorChooser.showDialog(parent, "Color1", col5); 
      if(color!=null) col5=color;
      if( parent.prefs!=null && color!=null) {
        parent.prefs.putInt("dfcol5",  color.getRGB() );
      }
      update_colors();
    }//GEN-LAST:event_dvcol5ActionPerformed

    private void closeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeActionPerformed
      setVisible(false);
    }//GEN-LAST:event_closeActionPerformed

      //col1 = new Color(128,0,128);
      //col2 = Color.white; 
      //col3 = Color.red; 
      //col4 = Color.cyan; 
      //col5 = Color.yellow; 
    private void en1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_en1ActionPerformed
        // TODO add your handling code here:
      if( parent.prefs!=null) {
        parent.prefs.putBoolean("dfen1",  en1.isSelected() );
      }
      update_colors();
    }//GEN-LAST:event_en1ActionPerformed

    private void en2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_en2ActionPerformed
        // TODO add your handling code here:
      if( parent.prefs!=null) {
        parent.prefs.putBoolean("dfen2",  en2.isSelected() );
      }
      update_colors();
    }//GEN-LAST:event_en2ActionPerformed

    private void en3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_en3ActionPerformed
        // TODO add your handling code here:
      if( parent.prefs!=null) {
        parent.prefs.putBoolean("dfen3",  en3.isSelected() );
      }
      update_colors();
    }//GEN-LAST:event_en3ActionPerformed

    private void en4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_en4ActionPerformed
        // TODO add your handling code here:
      if( parent.prefs!=null) {
        parent.prefs.putBoolean("dfen4",  en4.isSelected() );
      }
      update_colors();
    }//GEN-LAST:event_en4ActionPerformed

    private void en5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_en5ActionPerformed
        // TODO add your handling code here:
      if( parent.prefs!=null) {
        parent.prefs.putBoolean("dfen5",  en5.isSelected() );
      }
      update_colors();
    }//GEN-LAST:event_en5ActionPerformed

    private void saveconfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveconfigActionPerformed
      if(parent.prefs!=null) {

        parent.prefs.put("dftok1", tok1.getText() );
        parent.prefs.put("dftok2", tok2.getText() );
        parent.prefs.put("dftok3", tok3.getText() );
        parent.prefs.put("dftok4", tok4.getText() );
        parent.prefs.put("dftok5", tok5.getText() );

        parent.prefs.putFloat("dwidth", Float.valueOf( dwidth.getText()).floatValue() );

        parent.prefs.put("date_format", date_format.getText() );
        parent.prefs.put("time_format", time_format.getText() );
        parent.prefs.put("date_time_format", date_time_format.getText() );

      }

      update_colors();
      saveconfig.setEnabled(true);
      parent.setStatus("display view config saved.");
    }//GEN-LAST:event_saveconfigActionPerformed

    private void fs4KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fs4KeyTyped
      //System.out.println("evt:"+evt);
      saveconfig.setEnabled(true);
    }//GEN-LAST:event_fs4KeyTyped

    private void clrnv1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clrnv1ActionPerformed
      if(parent.prefs!=null) {
        parent.prefs.putBoolean("clrnv1", clrnv1.isSelected() );
      }
      update_colors();
    }//GEN-LAST:event_clrnv1ActionPerformed

    private void clrnv2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clrnv2ActionPerformed
      if(parent.prefs!=null) {
        parent.prefs.putBoolean("clrnv2", clrnv2.isSelected() );
      }
      update_colors();
    }//GEN-LAST:event_clrnv2ActionPerformed

    private void clrnv3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clrnv3ActionPerformed
      if(parent.prefs!=null) {
        parent.prefs.putBoolean("clrnv3", clrnv3.isSelected() );
      }
      update_colors();
    }//GEN-LAST:event_clrnv3ActionPerformed

    private void clrnv4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clrnv4ActionPerformed
      if(parent.prefs!=null) {
        parent.prefs.putBoolean("clrnv4", clrnv4.isSelected() );
      }
      update_colors();
    }//GEN-LAST:event_clrnv4ActionPerformed

    private void clrnv5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clrnv5ActionPerformed
      if(parent.prefs!=null) {
        parent.prefs.putBoolean("clrnv5", clrnv5.isSelected() );
      }
      update_colors();
    }//GEN-LAST:event_clrnv5ActionPerformed

    private void dwidthKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_dwidthKeyReleased
      saveconfig.setEnabled(true);
    }//GEN-LAST:event_dwidthKeyReleased

    public void show_help() {
      String kw="";

      kw = kw.concat("\n$BLKS_SEC$");
      kw = kw.concat("\n$CC_FREQ$");
      kw = kw.concat("\n$DATE$");
      kw = kw.concat("\n$DEMOD$");
      kw = kw.concat("\n$EVM_P$");
      kw = kw.concat("\n$ERR_RATE$");
      kw = kw.concat("\n$EVENT$");
      kw = kw.concat("\n$FREQ$");
      kw = kw.concat("\n$LCN$");
      kw = kw.concat("\n$NAC$");
      kw = kw.concat("\n$P25_MODE$");
      kw = kw.concat("\n$RFSS_ID$");
      kw = kw.concat("\n$RID$");
      kw = kw.concat("\n$RID_ALIAS$");
      kw = kw.concat("\n$RSSI$");
      kw = kw.concat("\n$SITE_ID$");
      kw = kw.concat("\n$SYNC_COUNT$");
      kw = kw.concat("\n$SYS_ID$");
      kw = kw.concat("\n$SYS_NAME$");
      kw = kw.concat("\n$TDMA_SLOT$");
      kw = kw.concat("\n$TG_ID$");
      kw = kw.concat("\n$TG_NAME$");
      kw = kw.concat("\n$TIME$");
      kw = kw.concat("\n$TONEA_FREQ$");
      kw = kw.concat("\n$TONEB_FREQ$");
      kw = kw.concat("\n$TONEA_IDX$");
      kw = kw.concat("\n$TONEB_IDX$");
      kw = kw.concat("\n$V_FREQ$");
      kw = kw.concat("\n$WACN$");
      kw = kw.concat("\n$ZONE$");
      kw = kw.concat("\n$ZONE_ALIAS$");
      hf.setText(kw);

      hf.setVisible(true);
    }

    private void showkeywActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showkeywActionPerformed
      show_help();
    }//GEN-LAST:event_showkeywActionPerformed

    private void import_dfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_import_dfActionPerformed
      try {

        FileNameExtensionFilter filter = new FileNameExtensionFilter( "displayview file", "dvp");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showDialog(parent, "Import Display View Profile .DVP file");


        if(returnVal == JFileChooser.APPROVE_OPTION) {
          File file = chooser.getSelectedFile();
          FileInputStream fis = new FileInputStream(file);
          ObjectInputStream ois = new ObjectInputStream(fis);
          do_import(ois);
        }

      } catch(Exception e) {
        e.printStackTrace();
      }
    }//GEN-LAST:event_import_dfActionPerformed

  ///////////////////////////////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////
  public void reset_to_defaults() {
    try {
      ObjectInputStream ois = new ObjectInputStream( getClass().getResourceAsStream("/btconfig/default.dvp") );
      do_import(ois);
    } catch(Exception e) {
    }
  }

  ///////////////////////////////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////
  public void do_import(ObjectInputStream ois) {
    try {


      int version = ois.readInt(); //version

      if(version==100 && parent.prefs!=null) {

        parent.prefs.putInt( "dfcol1", ois.readInt() );
        parent.prefs.putInt( "dfcol2", ois.readInt() );
        parent.prefs.putInt( "dfcol3", ois.readInt() );
        parent.prefs.putInt( "dfcol4", ois.readInt() );
        parent.prefs.putInt( "dfcol5", ois.readInt() );

        parent.prefs.putBoolean( "dfen1", ois.readBoolean() );
        parent.prefs.putBoolean( "dfen2", ois.readBoolean() );
        parent.prefs.putBoolean( "dfen3", ois.readBoolean() );
        parent.prefs.putBoolean( "dfen4", ois.readBoolean() );
        parent.prefs.putBoolean( "dfen5", ois.readBoolean() );

        parent.prefs.put("df_font1", ois.readUTF() );
        parent.prefs.put("df_font2", ois.readUTF() );
        parent.prefs.put("df_font3", ois.readUTF() );
        parent.prefs.put("df_font4", ois.readUTF() );
        parent.prefs.put("df_font5", ois.readUTF() );

        parent.prefs.putInt("df_font_size1", ois.readInt() );
        parent.prefs.putInt("df_font_size2", ois.readInt() );
        parent.prefs.putInt("df_font_size3", ois.readInt() );
        parent.prefs.putInt("df_font_size4", ois.readInt() );
        parent.prefs.putInt("df_font_size5", ois.readInt() );

        parent.prefs.putInt("df_font_style1", ois.readInt() );
        parent.prefs.putInt("df_font_style2", ois.readInt() );
        parent.prefs.putInt("df_font_style3", ois.readInt() );
        parent.prefs.putInt("df_font_style4", ois.readInt() );
        parent.prefs.putInt("df_font_style5", ois.readInt() );

        parent.prefs.put("dftok1", ois.readUTF() );
        parent.prefs.put("dftok2", ois.readUTF() );
        parent.prefs.put("dftok3", ois.readUTF() );
        parent.prefs.put("dftok4", ois.readUTF() );
        parent.prefs.put("dftok5", ois.readUTF() );

        parent.prefs.putBoolean("clrnv1", ois.readBoolean() );
        parent.prefs.putBoolean("clrnv2", ois.readBoolean() );
        parent.prefs.putBoolean("clrnv3", ois.readBoolean() );
        parent.prefs.putBoolean("clrnv4", ois.readBoolean() );
        parent.prefs.putBoolean("clrnv5", ois.readBoolean() );

        parent.prefs.putFloat("dwidth", ois.readFloat() );

        ois.close();

        did_init=false;
      }
      else {
        System.out.println("import display file: wrong version");
      }
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

    private void selfont1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selfont1ActionPerformed

      jfc.setSelectedFontFamily(fname1);
      jfc.setSelectedFontSize(fsz1);
      jfc.setSelectedFontStyle(fstyle1);


      int result = jfc.showDialog(this);
      if( result == JFontChooser.OK_OPTION ) {
        bt1.setFont( jfc.getSelectedFontFamily(), jfc.getSelectedFontStyle(), jfc.getSelectedFontSize() );
      }
      if(parent.prefs!=null) {
        parent.prefs.put("df_font1", jfc.getSelectedFontFamily() );
        parent.prefs.putInt("df_font_style1", jfc.getSelectedFontStyle() );
        parent.prefs.putInt("df_font_size1", jfc.getSelectedFontSize() );
      }
      update_colors();
    }//GEN-LAST:event_selfont1ActionPerformed

    private void selfont2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selfont2ActionPerformed
      jfc.setSelectedFontFamily(fname2);
      jfc.setSelectedFontSize(fsz2);
      jfc.setSelectedFontStyle(fstyle2);

      int result = jfc.showDialog(this);
      if( result == JFontChooser.OK_OPTION ) {
        bt2.setFont( jfc.getSelectedFontFamily(), jfc.getSelectedFontStyle(), jfc.getSelectedFontSize() );
      }
      if(parent.prefs!=null) {
        parent.prefs.put("df_font2", jfc.getSelectedFontFamily() );
        parent.prefs.putInt("df_font_style2", jfc.getSelectedFontStyle() );
        parent.prefs.putInt("df_font_size2", jfc.getSelectedFontSize() );
      }
      update_colors();
    }//GEN-LAST:event_selfont2ActionPerformed

    private void selfont3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selfont3ActionPerformed
      jfc.setSelectedFontFamily(fname3);
      jfc.setSelectedFontSize(fsz3);
      jfc.setSelectedFontStyle(fstyle3);

      int result = jfc.showDialog(this);
      if( result == JFontChooser.OK_OPTION ) {
        bt3.setFont( jfc.getSelectedFontFamily(), jfc.getSelectedFontStyle(), jfc.getSelectedFontSize() );
      }
      if(parent.prefs!=null) {
        parent.prefs.put("df_font3", jfc.getSelectedFontFamily() );
        parent.prefs.putInt("df_font_style3", jfc.getSelectedFontStyle() );
        parent.prefs.putInt("df_font_size3", jfc.getSelectedFontSize() );
      }
      update_colors();
    }//GEN-LAST:event_selfont3ActionPerformed

    private void selfont4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selfont4ActionPerformed
      jfc.setSelectedFontFamily(fname4);
      jfc.setSelectedFontSize(fsz4);
      jfc.setSelectedFontStyle(fstyle4);

      int result = jfc.showDialog(this);
      if( result == JFontChooser.OK_OPTION ) {
        bt4.setFont( jfc.getSelectedFontFamily(), jfc.getSelectedFontStyle(), jfc.getSelectedFontSize() );
      }
      if(parent.prefs!=null) {
        parent.prefs.put("df_font4", jfc.getSelectedFontFamily() );
        parent.prefs.putInt("df_font_style4", jfc.getSelectedFontStyle() );
        parent.prefs.putInt("df_font_size4", jfc.getSelectedFontSize() );
      }
      update_colors();
    }//GEN-LAST:event_selfont4ActionPerformed

    private void selfont5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selfont5ActionPerformed
      jfc.setSelectedFontFamily(fname5);
      jfc.setSelectedFontSize(fsz5);
      jfc.setSelectedFontStyle(fstyle5);

      int result = jfc.showDialog(this);
      if( result == JFontChooser.OK_OPTION ) {
        bt5.setFont( jfc.getSelectedFontFamily(), jfc.getSelectedFontStyle(), jfc.getSelectedFontSize() );
      }
      if(parent.prefs!=null) {
        parent.prefs.put("df_font5", jfc.getSelectedFontFamily() );
        parent.prefs.putInt("df_font_style5", jfc.getSelectedFontStyle() );
        parent.prefs.putInt("df_font_size5", jfc.getSelectedFontSize() );
      }
      update_colors();
    }//GEN-LAST:event_selfont5ActionPerformed

    private void resettodefaultActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resettodefaultActionPerformed
      reset_to_defaults();
    }//GEN-LAST:event_resettodefaultActionPerformed

    private void date_formatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_date_formatActionPerformed
        if( parent.prefs!=null) {
            parent.prefs.put("date_format", date_format.getText());
        }
    }//GEN-LAST:event_date_formatActionPerformed

    private void time_formatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_time_formatActionPerformed
        if( parent.prefs!=null) {
            parent.prefs.put("time_format", time_format.getText());
        }
    }//GEN-LAST:event_time_formatActionPerformed

    private void date_time_formatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_date_time_formatActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_date_time_formatActionPerformed

    private void clr_analog1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clr_analog1ActionPerformed
      if(parent.prefs!=null) {
        parent.prefs.putBoolean("clr_analog1", clr_analog1.isSelected() );
      }
      update_colors();
    }//GEN-LAST:event_clr_analog1ActionPerformed

    private void clr_analog2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clr_analog2ActionPerformed
      if(parent.prefs!=null) {
        parent.prefs.putBoolean("clr_analog2", clr_analog2.isSelected() );
      }
      update_colors();
    }//GEN-LAST:event_clr_analog2ActionPerformed

    private void clr_analog3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clr_analog3ActionPerformed
      if(parent.prefs!=null) {
        parent.prefs.putBoolean("clr_analog3", clr_analog3.isSelected() );
      }
      update_colors();
    }//GEN-LAST:event_clr_analog3ActionPerformed

    private void clr_analog4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clr_analog4ActionPerformed
      if(parent.prefs!=null) {
        parent.prefs.putBoolean("clr_analog4", clr_analog4.isSelected() );
      }
      update_colors();
    }//GEN-LAST:event_clr_analog4ActionPerformed

    private void clr_analog5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clr_analog5ActionPerformed
      if(parent.prefs!=null) {
        parent.prefs.putBoolean("clr_analog5", clr_analog5.isSelected() );
      }
      update_colors();
    }//GEN-LAST:event_clr_analog5ActionPerformed

  /*
    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(displayframe_edit.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(displayframe_edit.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(displayframe_edit.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(displayframe_edit.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new displayframe_edit().setVisible(true);
            }
        });
    }

  */
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton close;
    public javax.swing.JCheckBox clr_analog1;
    public javax.swing.JCheckBox clr_analog2;
    public javax.swing.JCheckBox clr_analog3;
    public javax.swing.JCheckBox clr_analog4;
    public javax.swing.JCheckBox clr_analog5;
    private javax.swing.JCheckBox clrnv1;
    private javax.swing.JCheckBox clrnv2;
    private javax.swing.JCheckBox clrnv3;
    private javax.swing.JCheckBox clrnv4;
    private javax.swing.JCheckBox clrnv5;
    public javax.swing.JTextField date_format;
    public javax.swing.JTextField date_time_format;
    private javax.swing.JButton dvcol1;
    private javax.swing.JButton dvcol2;
    private javax.swing.JButton dvcol3;
    private javax.swing.JButton dvcol4;
    private javax.swing.JButton dvcol5;
    private javax.swing.JTextField dwidth;
    public javax.swing.JCheckBox en1;
    public javax.swing.JCheckBox en2;
    public javax.swing.JCheckBox en3;
    public javax.swing.JCheckBox en4;
    public javax.swing.JCheckBox en5;
    private javax.swing.JButton export_df;
    private javax.swing.JButton import_df;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton resettodefault;
    private javax.swing.JButton saveconfig;
    private javax.swing.JButton selfont1;
    private javax.swing.JButton selfont2;
    private javax.swing.JButton selfont3;
    private javax.swing.JButton selfont4;
    private javax.swing.JButton selfont5;
    private javax.swing.JButton showkeyw;
    public javax.swing.JTextField time_format;
    private javax.swing.JTextField tok1;
    private javax.swing.JTextField tok2;
    private javax.swing.JTextField tok3;
    private javax.swing.JTextField tok4;
    private javax.swing.JTextField tok5;
    // End of variables declaration//GEN-END:variables
}
