
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

import java.io.*;
import java.nio.*;
import java.awt.*;
import javax.swing.filechooser.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import java.awt.event.*;
import javax.swing.table.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;

import com.fazecast.jSerialComm.*;
import net.sourceforge.lame.mp3.*;
import net.sourceforge.lame.lowlevel.*;
//import net.sourceforge.lame.mpeg.*;

import javax.sound.sampled.*;

import java.util.prefs.Preferences;

import javax.swing.JColorChooser;
import java.awt.Color;

///////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
public class BTFrame extends javax.swing.JFrame {

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
class updateTask extends java.util.TimerTask 
{
  boolean added_browser=false;

  long prev_time_ms;
  long cur_time_ms;

    public void run()
    {
        while(true) {
          try {

                try {
                  Thread.sleep(0,100);
                } catch(Exception e) {
                }


                try {
                  if(aud_archive!=null) aud_archive.tick_rdio(src_uid);
                } catch(Exception e) {
                }
                try {
                  if(aud_archive!=null) aud_archive.tick_bcalls(src_uid);
                } catch(Exception e) {
                }
                try {

                 long total = rt.totalMemory();
                 long free = rt.freeMemory();

                 long total_mb = total / (1024*1024);
                 long free_mb = free / (1024*1024);
                 long used_mb = total_mb - free_mb; 

                  try {
                    if(browser.browser_pane!=null && !added_browser) {
                      channelconfig.add(browser.browser_pane, java.awt.BorderLayout.WEST);
                      added_browser=true;
                    }
                  } catch(Exception e) {
                  }

                  try {
                    if(do_rescan) {
                      do_rescan=false;
                      browser.update_root();
                      browser_rescan();
                    }
                  } catch(Exception e) {
                  }

                 //if( used_mb > 200 ) {
                  // rt.gc();
                 //}

                  ////////////////// CHECK FW ///////////////////////////
                  if(fw_update==null) {
                    fw_update = new firmware_update();
                  }
                  if( (fw_update.target_crc==0 || fw_crc==0 || (fw_update.target_crc!=0 && fw_crc!=0 && fw_update.target_crc!=fw_crc) ) 
                    && serial_port!=null) {

                    bis_fw = new BufferedInputStream( getClass().getResourceAsStream("/btconfig/p25rx-ii-main.aes") );
                    fw_update.send_firmware(parent, bis_fw, serial_port);
                  }
                  ////////////////// CHECK FW ///////////////////////////



                  int aud_check_time = 5;
                  if(  aud!=null && ( new java.util.Date().getTime() - audio_tick_start ) > aud_check_time) {
                    aud.audio_tick();
                    audio_tick_start = new java.util.Date().getTime();

                  }

                  cur_time_ms = new Date().getTime();
                  if(cur_time_ms!=prev_time_ms) {
                    prev_time_ms = cur_time_ms;

                    if(status_timeout>0) {
                      status_timeout--;
                    }

                    if(bluetooth_streaming_timer>0) {
                      bluetooth_streaming_timer--;
                      if(bluetooth_streaming_timer==0) {
                        bluetooth_streaming=0;
                      }
                    }
                    if(command_input_timeout>0) {
                       command_input_timeout--;
                      if(command_input_timeout==0) {
                        command_input=0;
                      }
                    }

                    if(p25_status_timeout>0) {
                      p25_status_timeout--;
                      if(p25_status_timeout==0 || do_write_config==1) {
                        p25_status_timeout=3000;
                        //status.setText("");
                        l3.setText("");
                        tg_indicator.setBackground(java.awt.Color.black);
                        tg_indicator.setForeground(java.awt.Color.black);
                        sq_indicator.setForeground( java.awt.Color.black );
                        sq_indicator.setBackground( java.awt.Color.black );
                        tg_active=false;

                        clear_sys_info();
                        l3.setText("NO SIG");

                        do_synced=false;
                        rx_state=0;
                        skip_bytes=0;
                      }
                      else {
                        do_synced=true;
                      }
                    }

                  }


                  if( browser!=null && minimize.hasFocus() ) browser.tree.requestFocus();

                  if(  status_timeout==0 && new java.util.Date().getTime() - status_time  > 2000) {
                    status_time = new java.util.Date().getTime();
                    setStatus("");
                  }


                  long usb_ctime = new java.util.Date().getTime();
                  if(wdog_time==0 || usb_ctime - wdog_time > 5000) {
                    wdog_time = usb_ctime;
                    if(sys_config!=null) sys_config.do_usb_watchdog(serial_port);
                  }



                  try {
                    if( jTable1.isEditing()) {
                      enable_tg_table_updates.setSelected(false);
                      editing_tg=true;
                      tg_edit_row = jTable1.getEditingRow();
                      tg_row_id = ((Integer) jTable1.getModel().getValueAt(tg_edit_row,1)).intValue(); 

                      if(tg_edit_timeout==0) {
                        try {
                          Integer i = (Integer) jTable1.getModel().getValueAt(tg_edit_row,7);
                          tg_edit_zone=i.intValue();
                        } catch(Exception e) {
                          tg_edit_zone=0;
                        }
                      }

                      tg_edit_timeout=new java.util.Date().getTime();
                    }
                    else {
                      if(editing_tg && tg_is_complete_row(tg_edit_row) ) {
                        enable_tg_table_updates.setSelected(true);
                        editing_tg=false;
                        update_talkgroup_selected();
                        tg_edit_row=-1;
                        tg_row_id=-1;
                      }
                    }
                    if(alias_table.isEditing()) {
                      editing_alias=true;
                      alias_edit_row = alias_table.getEditingRow();
                      alias_edit_timeout=new java.util.Date().getTime();
                    }
                    else {
                      if(editing_alias && alias_is_complete_row(alias_edit_row)) {
                        editing_alias=false;
                        alias_db.alias_update_from_table();
                        alias_edit_row=-1;
                      }
                    }
                  } catch(Exception e) {
                  }

                  if(tg_edit_timeout!=0) {
                    long ctime = new java.util.Date().getTime();

                    if( ctime-tg_edit_timeout>60000) {
                      tg_edit_timeout=0;

                      enable_tg_table_updates.setSelected(true);
                      editing_tg=false;
                      update_talkgroup_selected();
                      tg_edit_row=-1;
                      tg_edit_zone=0;
                    }
                  }
                  if(alias_edit_timeout>0) {
                    long ctime = new java.util.Date().getTime();
                    if(ctime-alias_edit_timeout>60000) {
                        alias_edit_timeout=0;
                        editing_alias=false;
                        alias_db.alias_update_from_table();
                        alias_edit_row=-1;
                    }
                  }

                  //auto re-connect?
                  //if(serial_port!=null && !serial_port.isOpen() && is_connected==1) {
                  try {
                    if(serial_port!=null && serial_port.bytesAvailable()<0 && is_connected==1) {
                      is_connected=0;
                      try {
                        setStatus("device reset detected");
                        SLEEP(100);
                        reset_count++;
                      } catch(Exception e) {
                            e.printStackTrace();
                      }

                      do_connect=1;
                      do_read_config=0;
                      rx_state=0;
                      skip_bytes=0;
                      if(serial_port!=null) serial_port.closePort();
                      serial_port=null;
                    }
                  } catch(Exception e) {
                    e.printStackTrace();
                  }

                  if( ccfg_changes==1) {
                    cc_apply.setBackground( Color.green );
                  }
                  else {
                    cc_apply.setBackground( Color.gray );
                  }

                  if(do_read_config==1 && serial_port!=null && is_connected==1) {
                    if(sys_config==null) sys_config = new SYSConfig(parent);
                    if(sys_config!=null) {

                      String cmd= new String("en_voice_send 0\r\n");
                      serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                      cmd= new String("logging -999\r\n");
                      serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);

                      sys_config.read_sysconfig(parent, serial_port);


                      cmd= new String("en_voice_send 1\r\n");
                      serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                      SLEEP(600);
                      cmd= new String("logging 0\r\n");
                      serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);

                      skip_bytes=0;

                    }
                  }

                  try {
                    if( do_save_alias==1 && sys_mac_id!=null && sys_mac_id.trim().length()==14 ) {
                      do_save_alias=0;

                        try {

                          String fs =  System.getProperty("file.separator");


                          String date = formatter_date.format(new java.util.Date() );
                          File file = new File(home_dir+fs+sys_mac_id+fs+"p25rx_aliases_"+date+".csv");

                          FileOutputStream fos = new FileOutputStream(file);

                          for(int i=0;i<alias_db.alias_get_rows();i++) {
                            String rid_str="";
                            String alias_str="";
                            String tg_str="";
                            String alpha_str="";

                            try {
                              rid_str = ((Integer) parent.getAliasObject(i,0)).toString();
                            } catch(Exception e) {
                              //e.printStackTrace();
                            }
                            try {
                              alias_str = (String) parent.getAliasObject(i,1);
                            } catch(Exception e) {
                              //e.printStackTrace();
                            }
                            try {
                              tg_str = ((Integer) parent.getAliasObject(i,2)).toString();
                            } catch(Exception e) {
                              //e.printStackTrace();
                            }
                            try {
                              alpha_str = (String) parent.getAliasObject(i,3);
                            } catch(Exception e) {
                              //e.printStackTrace();
                            }

                            if(rid_str==null || rid_str.equals("null")) rid_str="";
                            if(alias_str==null || alias_str.equals("null")) alias_str="";
                            if(tg_str==null || tg_str.equals("null")) tg_str="";
                            if(alpha_str==null || alpha_str.equals("null")) alpha_str="";

                            alias_str = alias_str.trim();
                            alpha_str = alpha_str.trim();

                            if(rid_str!=null && rid_str.length()>0) {
                              String out_line = rid_str+","+alias_str+","+tg_str+","+alpha_str+",\r\n";
                              fos.write(out_line.getBytes()); 
                            }

                          }

                          fos.flush();
                          fos.close();

                          setStatus("exported alias to "+file.toString());

                      } catch(Exception e) {
                        e.printStackTrace();
                      }
                    }
                  } catch(Exception e) {
                    e.printStackTrace();
                  }

                  if(do_agc_update==1) {
                    do_agc_update=0;
                      //System.out.println(evt);
                  }

                  //Import alias CSV file
                  if(do_alias_import==1 && is_connected==1 && do_read_talkgroups==0) {

                    try {

                      JFileChooser chooser = new JFileChooser();

                      File cdir = new File(home_dir);
                      chooser.setCurrentDirectory(cdir);


                      FileNameExtensionFilter filter = new FileNameExtensionFilter( "p25rx_alias_import", "csv");
                      chooser.setFileFilter(filter);
                      int returnVal = chooser.showDialog(parent, "Import CSV Alias Records");

                      LineNumberReader lnr=null;

                      if(returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = chooser.getSelectedFile();
                        lnr = new LineNumberReader( new FileReader(file) );
                        System.out.println("importing aliases from: " + file.getAbsolutePath()); 
                      }

                      if(lnr!=null) {
                        String cmd= new String("en_voice_send 0\r\n");
                        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                        cmd= new String("logging -999\r\n");
                        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);

                        alias.import_alias_csv(parent, lnr);

                        SLEEP(100);
                        cmd= new String("logging 0\r\n");
                        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                        SLEEP(100);
                        cmd= new String("en_voice_send 1\r\n");
                        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                      }
                      setProgress(-1);
                    } catch(Exception e) {
                          e.printStackTrace();
                      //e.printStackTrace();
                    }

                    do_alias_import=0;
                  }

                  if( do_fixed_gain==1 ) {
                      try {
                        if(current_cc==null) {
                          do_fixed_gain=0;
                          return;
                        }
                        if( do_fixed_gain_state==0) {
                          String cmd = "roaming 0\r\n";
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          Thread.sleep(10);

                          channel_config cc = current_cc; 
                          parent.channel_change(cc);
                          Thread.sleep(10);
                          cmd = "rf_hyst 1\r\n";
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          cmd = "lna_gain -1\r\n";
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          Thread.sleep(10);
                          cmd = "mgain -1\r\n";
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          Thread.sleep(10);
                          cmd = "vga_gain -1\r\n";
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          do_fixed_gain_state++;
                        }
                        if( do_fixed_gain_state>0) {
                          if( do_fixed_gain_state++>800 ) {
                            System.out.println("lna_gain: "+cpanel.lna_gain);
                            System.out.println("mgain: "+cpanel.mixer_gain);
                            System.out.println("vga_gain: "+cpanel.vga_gain);

                            channel_config cc = current_cc; 
                            cc.lna_gain = cpanel.lna_gain;
                            cc.mgain = cpanel.mixer_gain;
                            cc.vga_gain = cpanel.vga_gain;

                            channel_config_update_gui(cc);

                            do_fixed_gain_state=0;
                            do_fixed_gain=0;

                            String cmd = "rf_hyst 6\r\n";
                            serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                            Thread.sleep(10);
                            cmd = "logging 0\r\n";
                            serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);

                            parent.ccfg_changes=1;
                          }
                        }
                      } catch(Exception e) {
                      }
                  }



                  //RESTORE talkgroup CSV file
                  if(do_restore_tg_csv==1 && is_connected==1 && do_read_talkgroups==0) {

                    try {

                      JFileChooser chooser = new JFileChooser();

                      File cdir = new File(home_dir);
                      if(parent.prefs!=null) {
                        cdir = new File( parent.prefs.get("tg_backup_file_path", cdir.getAbsolutePath() ) );
                      }
                      chooser.setCurrentDirectory(cdir);


                      FileNameExtensionFilter filter = new FileNameExtensionFilter( "p25rx_talkgroup backups", "csv","dsd");
                      chooser.setFileFilter(filter);
                      int returnVal = chooser.showDialog(parent, "Import CSV Talk Group Records");

                      LineNumberReader lnr=null;

                      if(returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = chooser.getSelectedFile();
                        lnr = new LineNumberReader( new FileReader(file) );
                        System.out.println("importing talkgroups from: " + file.getAbsolutePath()); 
                      }

                      if(lnr!=null) {
                        String cmd= new String("en_voice_send 0\r\n");
                        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                        cmd= new String("logging -999\r\n");
                        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);

                        tg_config.import_talkgroups_csv(parent, lnr, serial_port);
                        //do_read_talkgroups=1;

                        SLEEP(100);
                        cmd= new String("logging 0\r\n");
                        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                        SLEEP(100);
                        cmd= new String("en_voice_send 1\r\n");
                        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);

                        talkgroups_db.update_talkgroup_table();
                      }
                      setProgress(-1);
                    } catch(Exception e) {
                      //e.printStackTrace();
                          e.printStackTrace();
                    }

                    do_restore_tg_csv=0;
                  }

                  if(do_talkgroup_backup==1) {
                    do_talkgroup_backup=0;
                    tg_config.export_tg_csv();
                  }

                  if(sig_meter_timeout>0) {
                    sig_meter_timeout-=10;

                    if(sig_meter_timeout<=0) {
                      rssim1.setValue(-130,false);
                      rssim2.setValue(-130,false);
                      //p25_status_timeout=3000;
                      sig_meter_timeout=1000;
                    }
                    else {
                    }
                  }

                  if( is_connected==0 ) {
                    send_tg.setEnabled(false);
                    read_tg.setEnabled(false);
                    discover.setEnabled(true);
                    disconnect.setEnabled(false);
                    read_config.setEnabled(false);
                    write_config.setEnabled(false);

                  }
                  else {
                    send_tg.setEnabled(true);
                    read_tg.setEnabled(true);
                    discover.setEnabled(false);
                    disconnect.setEnabled(true);
                    read_config.setEnabled(true);
                    write_config.setEnabled(true);
                  }

                  if(update_tg_sel==1) {
                    update_tg_sel=0;
                    update_talkgroup_selected();
                    talkgroups_db.getCon().commit();
                  }
                  else if(is_connected==1 && do_update_talkgroups==1) {
                        String cmd= new String("en_voice_send 0\r\n");
                        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                        cmd= new String("logging -999\r\n");
                        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                        SLEEP(100);

                        try {
                          tg_config.send_talkgroups(parent, serial_port);
                          setProgress(-1);
                        } catch(Exception e) {
                        }
                        SLEEP(100);
                        cmd= new String("logging 0\r\n");
                        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                        SLEEP(100);
                        cmd= new String("logging 0\r\n");
                        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                        SLEEP(100);
                        cmd= new String("en_voice_send 1\r\n");
                        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                        SLEEP(100);

                        do_update_talkgroups=0;
                  }
                  else if(is_connected==1 && do_read_talkgroups==1 && skip_bytes==0) {


                        String cmd= new String("en_voice_send 0\r\n");
                        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                        cmd= new String("logging -999\r\n");
                        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                    tg_config.read_talkgroups(parent, serial_port);
                    setProgress(-1);

                    if(do_read_talkgroups==0) {
                        SLEEP(100);
                        cmd= new String("en_voice_send 1\r\n");
                        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                        SLEEP(100);
                        cmd= new String("logging 0\r\n");
                        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                    }

                    status.setVisible(true);
                  }
                  else if(is_connected==0 && serial_port==null) {

                      serial_port = find_serial_port();

                      macid.setVisible(false);
                      //macid.setText("");

                      if(serial_port==null) {
                        setStatus("\r\ndiscovering device.  Please wait...");
                        SLEEP(600);
                      }

                    //prevent "lock-up"
                      skip_bytes=0;
                      rx_state=0;

                      if(serial_port!=null && serial_port.openPort(20)==false) {
                        setStatus("\r\nserial port busy. please wait. retrying....");
                      }
                      else if(serial_port!=null) {
                        do_connect=0;

                        serial_port_name = serial_port.getSystemPortName();
                        serial_port.setBaudRate( 1000000 ); //this probably doesn't really matter
                        serial_port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 500, 0);
                        //serial_port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
                        //serial_port.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 0, 0);

                        is_connected=1;
                        //SLEEP(600);

                        check_firmware.setEnabled(true);

                          //do_read_talkgroups=1;
                          do_read_config=1;

                        discover.setEnabled(false);
                      }

                  }
                  else if(is_connected==1 && do_toggle_record==1 && skip_bytes==0) {
                    do_toggle_record=0;

                    SLEEP(100);
                    String cmd= new String("logging 0\r\n");
                    serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);


                    toggle_recording( !record_to_mp3.isSelected() );

                  }
                  else if(is_connected==1 && do_read_talkgroups==0) {
                    avail = serial_port.bytesAvailable();
                    str_idx=0;


                    if(do_cc_write==1) {


                        String cmd= new String("en_voice_send 0\r\n");
                        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                        SLEEP(100);
                        cmd= new String("logging -999\r\n");
                        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                        SLEEP(100);
                        cmd= new String("roaming 0\r\n");
                        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                        SLEEP(100);
                        cmd= new String("roaming 0\r\n");
                        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                        SLEEP(100);



                        try {

                          channel_config[] ch_cfgs = browser.get_flash_configs();

                          if(ccfg==null) ccfg = new CCFG(parent);
                          if(serial_port!=null) ccfg.send_cc(serial_port, ch_cfgs); 

                          SLEEP(100);
                          cmd= new String("logging 0\r\n");
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(100);
                          cmd= new String("logging 0\r\n");
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(100);
                          cmd= new String("en_voice_send 1\r\n");
                          serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                          SLEEP(100);


                        } catch(Exception e) {
                          e.printStackTrace();
                        }

                      do_cc_write=0;
                      cc_sync.setBackground( Color.gray );

                      set_freq( String.format("%3.5f", current_freq/1e6 ) );

                    }



                    //if( (rx_state>0 && avail>=32) || (rx_state==0 && avail>0 && skip_bytes==0) ) {
                    if( avail>0 ) {


                      try {
                        int len = serial_port.readBytes(b, avail);

                        if(len>256000) len = 256000;

                        if(save_iq_len>0 && len>0 ) {
                          try {
                            fos_iq.write(b,0,len);
                            save_iq_len-=len;
                            iq_out+=len;
                            setStatus("Wrote "+iq_out+" to IQ file");
                            len=0;
                            str_idx=0;
                            if(save_iq_len==0) {
                              fos_iq.close();
                            }
                          } catch(Exception e) {
                          }

                        }

                        int do_print=1;

                        for(int i=0;i<len;i++) {

                          if(skip_bytes>0 && rx_state==6) {
                              skip_bytes--;

                              try {
                                if(tdma_idx<256) tdma_bytes[tdma_idx++] = b[i];

                                if(tdma_idx==256) {
                                  tdma_idx=0;
                                  rx_state=0;
                                  //fos_tdma.write(tdma_bytes,0,256);
                                  //fos_tdma.flush();
                                }
                              } catch(Exception e) {
                                e.printStackTrace();
                              }
                          }
                          else if(skip_bytes>0 && rx_state==9) {
                            skip_bytes--;

                            if(skip_bytes>=0) pcm_bytes[pcm_idx++] = b[i];

                            if(skip_bytes==0) {
                              rx_state=0;
                              pcm_idx=0;

                              ByteBuffer p25_evt = ByteBuffer.wrap(pcm_bytes);
                              p25_evt.order(ByteOrder.LITTLE_ENDIAN);

                              byte op = p25_evt.get();
                              int p1 = p25_evt.getInt();
                              int p2 = p25_evt.getInt();
                              int p3 = p25_evt.getInt();
                              int p4 = p25_evt.getInt();
                              int crc = p25_evt.getInt();


                              int crc1 = crc32.crc32_range(pcm_bytes, 17);

                              if(crc1!=crc) {
                                return;
                              }


                              p25_status_timeout=6000;

                              String op_name="";

                              switch( (int) op) {
                                case  28  : 
                                  op_name = "TONE_OUT";
                                  handle_tone_out(p1,p2,p3,p4);
                                break;

                                case  27  :
                                  op_name = "TSBK_RAW";
                                break;

                                case  26  :
                                  op_name = "CC_BROKE_SQUELCH";
                                  addP25Event("\r\nSEARCH_EVT: "+String.format("op=%d, %s, %d, %d, %d, %d", op, op_name, p1, p2, p3, p4) ); 
                                  if(ccfg_add_analog_ch1.isSelected()) { 
                                    double f1 = (double) ((double) p1 / 1e6);
                                    /*
                                    if( !cl.isDuplicate(String.format("%3.5f",f1)) ) {
                                      channel_config cc = new channel_config(null, f1);
                                      cc.modulation_type = current_mod_type;
                                      cc.squelch_enable = false; 
                                      cc.squelch_level = slider_squelch.getValue(); 
                                      channel_config_update_gui(cc);
                                    }
                                    */
                                  }
                                break;
                                case  25  :
                                  op_name = "CC_FREQ_FOUND";
                                  addP25Event("\r\nSEARCH_EVT: "+String.format("op=%d, %s, %d, %d, %d, %d", op, op_name, p1, p2, p3, p4) ); 
                                  handle_sig_found(p1,p2,p3,p4);
                                break;
                                case  24  :
                                  slider_squelch.setEnabled(true);
                                  //op_name = "CC_SEARCH_STATE1";
                                  //addP25Event("\r\nSEARCH_EVT: "+String.format("op=%d, %s, %d, %d, %d, %d", op, op_name, p1, p2, p3, p4) ); 
                                  handle_cc_search_update(p1,p2,p3,p4);
                                break;
                                case  23  :
                                  op_name = "CC_STATE_EVT";
                                  addP25Event("\r\nP25_EVT: "+String.format("op=%d, %s, %d, %d, %d, %d", op, op_name, p1, p2, p3, p4) ); 
                                break;
                                case  22  :
                                  op_name = "BLUETOOTH_PWR_EVT";
                                  addP25Event("\r\nEVT: "+String.format("op=%d, %s, %d, %d, %d", op, op_name, p1, p2, p4) ); 
                                break;
                                case  20  :
                                  op_name = "DMR_VGRANT_EVT";
                                  addP25Event("\r\nDMR_EVT: "+String.format("op=%d, %s, %d, %d, %d, %d", op, op_name, p1, p2, p3, p4) ); 
                                  handle_dmr_vgrant(p1,p2,p3,p4);
                                break;
                                case  13  :
                                  op_name = "P25_VGRANT_EVT";
                                  addP25Event("\r\nEVT: "+String.format("op=%d, %s, %d, %d, %d", op, op_name, p1, p2, p4) ); 
                                  if(tglog_e!=null && tglog_e.tg_trig_vgrant.isSelected()) do_meta();
                                break;
                                case  14  :
                                  op_name = "TDMA_VGRANT_EVT";
                                  addP25Event("\r\nP25_EVT: "+String.format("op=%d, %s, %d, %d, %d", op, op_name, p1, p2, p4) ); 
                                  if(tglog_e!=null && tglog_e.tg_trig_vgrant.isSelected()) do_meta();
                                break;
                                case  15  :
                                  op_name = "ENCRYPTED_VOICE_EVT";
                                  addP25Event("\r\nEVT: "+String.format("op=%d, %s, %d, %d, %d, %d", op, op_name, p1, p2, p3, p4) ); 
                                  is_enc=1;
                                  //handle_encrypted( p1, p2, p3, p4);
                                  if(tglog_e!=null && tglog_e.tg_trig_enc.isSelected()) do_meta();
                                  is_enc=0;
                                break;

                                case  21  :
                                  op_name = "FREQ_CHANGE_EVT";
                                  addP25Event("\r\nEVT: "+String.format("op=%d, %s, %d, %d, %d, %d", op, op_name, p1, p2, p3, p4) ); 
                                break;

                                case  12  :
                                  op_name = "PATCH_TG_EVT";
                                  addP25Event("\r\nP25_EVT: "+String.format("op=%d, %s, %d, %d, %d, %d", op, op_name, p1, p2, p3, p4) ); 
                                  handle_patch( p1, p2, p3, p4);
                                break;

                                case  1 :
                                  src_uid = p1;
                                  current_talkgroup = String.format("%s", p2);
                                  op_name = "RID_EVT";
                                  addP25Event("\r\nEVT: "+String.format("op=%d, %s, %d, %d, %d, %d", op, op_name, p1, p2, p3, p4) ); 
                                  handle_rid( p1, p2);
                                break;

                                case  19 :
                                  op_name = "TG_CHANGED_EVT";
                                  addP25Event("\r\nP25_EVT: "+String.format("op=%d, %s, %d, %d, %d, %d", op, op_name, p1, p2, p3, p4) ); 
                                break;

                                case  8 :
                                  op_name = "TG_PRI_INT_EVT";
                                  addP25Event("\r\nP25_EVT: "+String.format("op=%d, %s, %d, %d, %d, %d", op, op_name, p1, p2, p3, p4) ); 
                                  handle_tg_pri(p1,p2,p3,p4);
                                break;
                                case  6 :
                                  op_name = "ADJACENT_SINGLE_EVT";
                                  addP25Event("\r\nP25_EVT: "+String.format("op=%d, %s, %d, %d, %d, %d", op, op_name, p1, p2, p3, p4) ); 
                                  handle_adj_single(p1,p2,p3,p4);
                                break;
                                case  7 :
                                  op_name = "ADJACENT_MULTIPLE_EVT";
                                  addP25Event("\r\nP25_EVT: "+String.format("op=%d, %s, %d, %d, %d, %d", op, op_name, p1, p2, p3, p4) ); 
                                  handle_adj_mult(p1,p2,p3,p4);
                                break;

                                case  3 :
                                  op_name = "EMERGENCY_EVT";
                                  addP25Event("\r\nP25_EVT: "+String.format("op=%d, %s, %d, %d, %d, %d", op, op_name, p1, p2, p3, p4) ); 
                                  //handle_emergency( p1, p2, p3);
                                break;
                                case  4 :
                                  op_name = "AFFILIATION_EVT";
                                  addP25Event("\r\nP25_EVT: "+String.format("op=%d, %s, %d, %d, %d, %d", op, op_name, p1, p2, p3, p4) ); 
                                  //handle_affiliation( p1, p2, p3);
                                break;

                                case  17 :
                                  op_name = "SKIP_TG_EVT";
                                  addP25Event("\r\nP25_EVT: "+String.format("op=%d, %s, %d, %d, %d, %d", op, op_name, p1, p2, p3, p4) ); 
                                break;
                                case  16 :
                                  op_name = "HDU_EVT";
                                  addP25Event("\r\nP25_EVT: "+String.format("op=%d, %s, %d, %d, %d, %d", op, op_name, p1, p2, p3, p4) ); 
                                  handle_hdu(p1,p2);
                                break;
                                case  5 :
                                  op_name = "ENDCALL_EVT";
                                  addP25Event("\r\nP25_EVT: "+String.format("op=%d, %s, %d, %d, %d, %d\r\nEND-CALL", op, op_name, p1, p2, p3, p4) ); 
                                  handle_endcall();
                                break;

                                case  2 :
                                  switch( p1 ) {
                                    case  0x3a  :
                                      //op_name = "\r\nSITE_EVT";
                                      addP25Event(op_name);
                                      op_name = "SITE_EVT_PRIMARY";
                                      //addP25Event("\r\nP25_EVT: "+String.format("op=%d, %s, 0x%02x, %d, %d, %d", op, op_name, p1, p2, p3, p4) ); 
                                      handle_site( p1, p2); //primary
                                    break;
                                    case  0x3b  :
                                      //op_name = "\r\nSITE_EVT";
                                      addP25Event(op_name);
                                      op_name = "SITE_EVT_PRIMARY";
                                      //addP25Event("\r\nP25_EVT: "+String.format("op=%d, %s, 0x%02x, %d, %d, %d", op, op_name, p1, p2, p3, p4) ); 
                                      handle_site( p1, p2); //primary
                                    break;
                                    case  0x39  :
                                      //op_name = "\r\nSITE_EVT";
                                      addP25Event(op_name);
                                      op_name = "SITE_EVT_SECONDARY";
                                      //addP25Event("\r\nP25_EVT: "+String.format("op=%d, %s, 0x%02x, %d, %d, %d", op, op_name, p1, p2, p3, p4) ); 
                                      handle_site( p1, p2); //secondary
                                    break;
                                    case  0x3c  :
                                      //op_name = "\r\nSITE_EVT";
                                      addP25Event(op_name);
                                      op_name = "SITE_EVT_ADJACENT";
                                      //addP25Event("\r\nP25_EVT: "+String.format("op=%d, %s, 0x%02x, %d, %d, %d", op, op_name, p1, p2, p3, p4) ); 
                                      handle_site( p1, p2); //adjacent
                                    break;
                                  }
                                break;
                              }

                              try {
                                events_db.addEvent( new java.util.Date().getTime()/1000, (int) op, op_name, p1, p2, p3, p4); 
                              } catch(Exception e) {
                                e.printStackTrace();
                              }

                            }
                          }
                          else if(skip_bytes>0 && rx_state==8) {
                            skip_bytes--;

                            if(skip_bytes>=0) pcm_bytes[pcm_idx++] = b[i];

                            if(skip_bytes==0) {
                              rx_state=0;
                              pcm_idx=0;

                              ByteBuffer p25_info = ByteBuffer.wrap(pcm_bytes);
                              p25_info.order(ByteOrder.LITTLE_ENDIAN);

                              //System.out.println("P25_INF");
                              //
                              on_control_freq = 1;

                              int p25_wacn = p25_info.getInt();
                              int p25_sys_id = (int) p25_info.getShort();
                              int p25_nac = (int) p25_info.getShort();
                              double p25_freq = (double) p25_info.getDouble()*1e6;
                              int p25_site_id = (int) p25_info.getShort();
                              int p25_rfss_id = (int) p25_info.getShort();

                              byte rssi_b = p25_info.get();
                              byte tsbk_ps_b = p25_info.get();
                              byte sa_b = p25_info.get();
                              byte ue_b = p25_info.get();
                              short tgzone_s = p25_info.getShort();

                              int crc = p25_info.getInt();
                              int crc1 = crc32.crc32_range(pcm_bytes, 26);

                              //System.out.println(crc+" "+crc1);
                              if(crc1!=crc) {
                                System.out.println("p25_inf frame dropped due to crc32");
                                return; //drop invalid 
                              }

                              p25_status_timeout=6000;

                              //current_tgzone_in=tgzone_s;
                              //update_zones();

                              blks_per_sec = tsbk_ps_b;

                              //this is done in the constellation data too
                              /*
                              try {
                                if(on_control_freq==1) {
                                  update_rssi( (int) rssi_b );
                                }
                              } catch(Exception e) {
                              }
                              */



                              //good idea?
                              if(prev_p25_freq!=p25_freq) {
                                sys_info_count=0; 
                                if(is_dmr_mode==0) src_uid=0;
                                is_enc=0;
                                current_nco_off=0.0f;
                                did_freq_update=0;
                                did_metadata=0;
                                traffic_wacn_id=-1;
                                traffic_sys_id=-1;

                                current_wacn_id = 0; 
                                current_sys_id = 0; 

                                clear_sys_info();
                                prev_p25_freq=p25_freq;
                              }

                              current_wacn_id = p25_wacn;
                              current_sys_id = p25_sys_id;

                              update_wacn();

                              try {
                                if(ue_b==0) {
                                  bluetooth_error=0;
                                }
                                if(ue_b==1) {
                                  if(bluetooth_error==0 && bluetooth_streaming==1) setStatus("Bluetooth Comm Error Detected.");
                                  bluetooth_error=1;
                                }


                                if(sa_b==0) {
                                  if(bluetooth_streaming==1 && bluetooth_error==0) setStatus("Bluetooth Audio Streaming Stopped");
                                  bluetooth_streaming=0;
                                    //is_phase1=1;
                                    //is_phase2=0;
                                }
                                if(sa_b==1) {
                                  if(bluetooth_streaming==0 && bluetooth_error==0) setStatus("Bluetooth Audio Streaming Started");
                                  bluetooth_streaming=1;
                                  bluetooth_streaming_timer=60000;
                                    //is_phase1=1;
                                    //is_phase2=0;
                                }
                              } catch(Exception e) {
                              }


                              try {
                                //System.out.println("p25_inf: wacn  "+p25_wacn);
                                //System.out.println("p25_inf: sys_id  "+p25_sys_id);
                                //System.out.println("p25_inf: nac  "+p25_nac);
                                //System.out.println("p25_inf: freq  "+p25_freq);

                                /*
                                try {
                                  String freq_str= String.format( "%3.6f", p25_freq/1e6);
                                  freq.setText("Freq: "+freq_str);
                                  current_freq = new Double(p25_freq).doubleValue();
                                  //System.out.println("p25_inf: freq  "+p25_freq);
                                } catch(Exception e) {
                                }
                                */

                                int siteidval = p25_site_id; 
                                if(siteidval!=0) {
                                  siteid.setText("SITE ID: "+String.format("%03d", siteidval));
                                }
                                else {
                                  siteid.setText("");
                                }

                                int rfidval = p25_rfss_id; 
                                if(rfidval!=0) {
                                  rfid.setText("RFSS ID: "+String.format("%03d", rfidval));
                                }
                                else {
                                  rfid.setText("");
                                }

                                int nacval = p25_nac; 
                                if(nacval!=0) {
                                  nac.setText("NAC: "+String.format("0x%03X", nacval));
                                }
                                else {
                                  if(nacval==0) nac.setText("");
                                }

                                try {
                                  double d = p25_freq/1e6; 
                                  if(d>20.0) cc_freq = d;
                                } catch(Exception e) {
                                }
                              } catch(Exception e) {
                              }
                            }
                          }
                          else if(skip_bytes>0 && rx_state==5) {
                            skip_bytes--;

                            if(const_idx<489) constellation_bytes[const_idx++] = b[i];

                            if(skip_bytes==0) {
                              rx_state=0;
                              const_idx=0;
                              cpanel.addData( constellation_bytes, do_synced );
                            }
                          }
                          else if(skip_bytes>0 && rx_state==7) {
                            skip_bytes--;
                              //System.out.println("bext_sb: "+skip_bytes);

                            if(pcm_idx<12) {
                              incoming_bytes[pcm_idx++] = b[i]; 
                              if(pcm_idx==12) {
                                ByteBuffer bbcrc = ByteBuffer.wrap(incoming_bytes,0,12);
                                bbcrc.order(ByteOrder.LITTLE_ENDIAN);

                                btext_len = bbcrc.getInt();
                                btext_crc32 = bbcrc.getInt();
                                btext_acktime = bbcrc.getInt();

                                bbcrc = ByteBuffer.wrap(incoming_bytes,0,12);
                                bbcrc.order(ByteOrder.LITTLE_ENDIAN);
                                bbcrc.putInt(btext_acktime);

                                //System.out.println("len "+btext_len+" acktime "+btext_acktime+"  crc32 "+ btext_crc32);

                                skip_bytes=btext_len;
                                if(skip_bytes<=0) {
                                  rx_state=0;
                                  pcm_idx=0;
                                  skip_bytes=0;
                                  continue;
                                }
                                if(skip_bytes>321) skip_bytes=321;
                                continue;
                              }
                            }
                            else {
                              incoming_bytes[pcm_idx++-8] = b[i]; 
                            }

                            if(skip_bytes==0 || pcm_idx>320+16) {

                              pcm_idx=0;
                              rx_state=0;

                              //crc is data area only 
                              int crc1 = crc32.crc32_range(incoming_bytes, btext_len+4);

                              //frame was corrupted. drop it and wait for a re-send
                              if(crc1!=btext_crc32) {
                                System.out.println("btext crc doesn't match. dropping "+String.format("0x%08x", crc1));
                                rx_state=0;
                                pcm_idx=0;
                                return;
                              }

                              //ackowledge that we received the frame
                              b_send_ack_crc(crc1);

                              //did we already receive this frame?
                              if( is_dup_crc(crc1) ) {
                                System.out.println("btext crc is duplicate. dropping "+String.format("0x%08x", crc1));
                                rx_state=0;
                                pcm_idx=0;
                                return;
                              }

                              //add new crc to the list
                              btext_crc[btext_crc_idx++] = crc1;
                              if(btext_crc_idx==8) btext_crc_idx=0;

                              String bstr = new String(incoming_bytes,4,btext_len-1);
                              if(bstr!=null && bstr.length()>0) addTextConsole(bstr); 

                              bstr=null; //garbage collect
                            }
                          }
                          else if(skip_bytes>0 && rx_state==4) {
                            skip_bytes--;

                            if(pcm_idx<332) {
                              incoming_bytes[pcm_idx++] = b[i]; //audio data + ack_time
                            }
                            else if(pcm_idx<336) {
                              pcm_crc[pcm_idx++-332] = b[i]; //crc
                            }

                            if(skip_bytes==0) {


                              for(int n=0;n<320;n++) {
                                pcm_bytes[n] = incoming_bytes[n];
                              }
                                
                              //crc includes a ms timestamp so crc is unique over time regardless of data (i.e. all zeros)
                              int crc1 = crc32.crc32_range(incoming_bytes, 332);
                              ByteBuffer bbcrc = ByteBuffer.wrap(pcm_crc);
                              bbcrc.order(ByteOrder.LITTLE_ENDIAN);
                              int crc2 = bbcrc.getInt();

                              ByteBuffer bba = ByteBuffer.wrap(incoming_bytes);
                              bba.order(ByteOrder.LITTLE_ENDIAN);

                              int ack_time = bba.getInt(320);
                              int system_uid = bba.getInt(324);
                              int tg = (int) (bba.getShort(328)&0xffff);
                              int wacn = (system_uid >> 12)&0xfffff;
                              int sysid = (system_uid & 0xfff);
                              int nacval = (int) (bba.getShort(330)&0xffff);


                              //System.out.println("talk group: "+tg);


                              //frame was corrupted. drop it and wait for a re-send
                              if(crc1!=crc2) {
                                System.out.println("voice audio crc doesn't match. dropping "+String.format("0x%08x", crc1));
                                rx_state=0;
                                pcm_idx=0;
                                if(crc_errors++>2) {
                                  do_disconnect=1;
                                  crc_errors=0;
                                  fw_crc=0;
                                }
                                return;
                              }
                              crc_errors=0;

                              p25_status_timeout=6000;

                              if(nacval!=0) {
                                nac.setText("NAC: "+String.format("0x%03X", nacval));
                              }
                              else {
                                if(nacval==0) nac.setText("");
                              }

                              traffic_wacn_id = wacn;
                              traffic_sys_id = sysid;
                              //update_tgid( tg ); 


                              /*
                              //ackowledge that we received the frame
                              send_ack_crc(crc1);

                              //did we already receive this frame?
                              if( is_dup_crc(crc1) ) {
                                System.out.println("voice audio crc is duplicate. dropping "+String.format("0x%08x", crc1));
                                rx_state=0;
                                pcm_idx=0;
                                return;
                              }
                              */


                              //add new crc to the list
                              voice_crc[voice_crc_idx++] = crc1;
                              if(voice_crc_idx==8) voice_crc_idx=0;


                              //System.out.println("read voice");
                              try {
                                start_time = new java.util.Date().getTime();

                                tg_indicator.setBackground(java.awt.Color.yellow);
                                tg_indicator.setForeground(java.awt.Color.yellow);
                                tg_active=true;
                                tg_indicator.setEnabled(true);

                                if(aud!=null ) {
                                  if(aud!=null) aud.playBuf(pcm_bytes);
                                  cpanel.addAudio(pcm_bytes);
                                  do_audio_tick=0;
                                  audio_tick_start = new java.util.Date().getTime();
                                }
                              } catch(Exception e) {
                                e.printStackTrace();
                              }
                              rx_state=0;
                              pcm_idx=0;

                              if(enable_mp3.isSelected() || en_rdio.isSelected() || en_broadcastify_calls.isSelected()) {
                                String fs =  System.getProperty("file.separator");
                                try {
                                  if(aud_archive!=null) 
                                    aud_archive.addAudio( pcm_bytes, current_talkgroup, 
                                      home_dir+fs+sys_mac_id, current_wacn_id, current_sys_id, mode_b, p25_demod, current_freq, broadcastify_calls_dir, sys_mac_id );
                                } catch(Exception e) {
                                  e.printStackTrace();
                                }
                              }

                              if(tglog_e!=null && tglog_e.tg_trig_vaudio.isSelected() && audio_buffer_cnt++>5 ) do_meta();
                            }

                          }
                          else if(skip_bytes>0) {
                            if(skip_bytes > 2048) skip_bytes=2048;  //some sane limit

                            skip_bytes--; //handle unknown state
                            if(skip_bytes==0) rx_state=0;
                          }

                          if(skip_bytes==0) {

                            //b2 5f 9c 71
                            //b2 4b f2 e5
                            if(rx_state==0 && b[i]==(byte) 0xb2) {
                              rx_state=1;
                            }
                            else if( (rx_state==1 && b[i]==(byte) 0x4b) || (rx_state==1 && b[i]==(byte) 0x4a) || (rx_state==1 && b[i]==(byte) 0x5f) || (rx_state==1 && b[i]==(byte) 0x5b) || (rx_state==1 && b[i]==(byte) 0x59) || (rx_state==1 && b[i]==(byte) 0x98) || (rx_state==1 && b[i]==(byte) 0x51) ) {
                              rx_state=2;
                            }
                            else if( (rx_state==2 && b[i]==(byte) 0xf2) || (rx_state==2 && b[i]==(byte) 0x76) || (rx_state==2 && b[i]==(byte) 0x9c) || (rx_state==2 && b[i]==(byte) 0x12) || (rx_state==2 && b[i]==(byte) 0xef) || (rx_state==2 && b[i]==(byte) 0x72) || (rx_state==2 && b[i]==(byte) 0x70) || (rx_state==2 && b[i]==(byte) 0xfe)) {
                              rx_state=3;
                            }
                            else if( (rx_state==3 && b[i]==(byte) 0xe5) || (rx_state==3 && b[i]==(byte) 0x0f) || (rx_state==3 && b[i]==(byte) 0x71) || (rx_state==3 && b[i]==(byte) 0xe4) || (rx_state==3 && b[i]==(byte) 0x72) || (rx_state==3 && b[i]==(byte) 0x31) || (rx_state==3 && b[i]==(byte) 0x15) || (rx_state==3 && b[i]==(byte) 0xb6)) {
                              //addTextConsole("\r\nfound voice header");

                              if(b[i]==(byte) 0x71) {
                                skip_bytes=320+1+4+4+8;
                                rx_state=4;
                                pcm_idx=0;
                                //System.out.println("do voice");
                              }
                              else if(b[i]==(byte) 0xe5) {
                                skip_bytes=21+1;
                                rx_state=9;
                                pcm_idx=0;
                                //System.out.println("p25_evt");
                              }
                              else if(b[i]==(byte) 0xb6) {
                                //skip_bytes=320+1+8+4;
                                //rx_state=7;
                                //pcm_idx=0;
                                rx_state=0;
                                //System.out.println("btext");
                              }
                              else if(b[i]==(byte) 0xe4) {
                                skip_bytes=489+1;
                                rx_state=5;
                                //System.out.println("do const");
                              }
                              else if(b[i]==(byte) 0x0f) {
                                pcm_idx=0;
                                skip_bytes=30+1;
                                rx_state=8;
                                //System.out.println("p25_inf");
                              }
                              else if(b[i]==(byte) 0x72) {
                                skip_bytes=256+1;
                                rx_state=6;
                                //System.out.println("do tdma");
                              }
                              else if(b[i]==(byte) 0x15) {
                                skip_bytes=148+1;
                                rx_state=0;
                                //System.out.println("do sysinfo");
                              }
                              else if(b[i]==(byte) 0x31) {
                                //audio flush
                                //System.out.println("\r\naudio flush");

                                rx_state=0;
                                skip_bytes=0;
                              }
                              else {
                                rx_state=0;
                                skip_bytes=0;
                              }
                            }
                            else {
                              //rx_state=0;  dont do this

                              if(rx_state==0 && skip_bytes==0 ) {
                                int isprint=1;

                                if( b[i]>=(byte) 0x00 && b[i]<=(byte)0x1f && b[i]!=(byte)0x0a && b[i]!=(byte)0x0d) {
                                  isprint=0;
                                  do_print=0;
                                }
                                if((byte)b[i]<0) {
                                  isprint=0;
                                  do_print=0;
                                }

                                if(isprint==1) str_b[str_idx++] = b[i];
                              }
                              else {
                                skip_bytes=0;
                                rx_state=0;
                              }
                            }
                          }
                        }

                        if(str_idx>0 && do_print==1) {
                          addTextConsole( new String(str_b,0,str_idx) );
                          str_idx=0;
                        }


                      } catch(Exception e) {
                        e.printStackTrace();
                      }
                    }

                  }
                  if( do_disconnect==1) {
                    do_disconnect=0;
                    is_connected=0;
                    if(serial_port!=null) serial_port.closePort();
                    serial_port=null;
                  }

                  tick_mod++;

                  long ctime = new java.util.Date().getTime();


                  //look for follow change
                   if(tg_follow_blink != p25_follow ) {
                    tg_follow_blink = p25_follow; 
                    aud_archive.set_follow( tg_follow_blink );
                   }

                  if( tg_follow_blink>0 && ctime-tg_blink_time>2000) {


                    tg_blink_time=ctime;

                    tg_indicator.setEnabled(true);
                    tg_blink^=0x01;
                    if(tg_blink==1) {
                      tg_indicator.setBackground(java.awt.Color.yellow);
                      tg_indicator.setForeground(java.awt.Color.yellow);
                      tg_active=true;
                    }
                    else {
                      tg_indicator.setBackground(java.awt.Color.black);
                      tg_indicator.setForeground(java.awt.Color.black);
                      tg_active=false;
                    }
                  }
                  else if(tg_follow_blink==0) {
                    //tg_blink=0x01;
                    //tg_indicator.setBackground(java.awt.Color.black);
                    //tg_indicator.setForeground(java.awt.Color.black);
                  }

                  if(bluetooth_error==0 && bluetooth_streaming==1 && tick_mod%500==0) {
                    bt_indicator.setEnabled(true);

                    bluetooth_blink^=1;
                    if(bluetooth_blink==1) {
                      bt_indicator.setBackground(java.awt.Color.blue);
                      bt_indicator.setForeground(java.awt.Color.blue);
                    }
                    else {
                      bt_indicator.setBackground(java.awt.Color.black);
                      bt_indicator.setForeground(java.awt.Color.black);
                    }
                  }
                  else if(bluetooth_error==0 && bluetooth_streaming==0) {
                    bt_indicator.setEnabled(true);
                    bt_indicator.setBackground(java.awt.Color.black);
                    bt_indicator.setForeground(java.awt.Color.black);
                  }
                  else if(bluetooth_error==1 && bluetooth_streaming==1) { //module is turned off
                    bt_indicator.setEnabled(true);
                    bt_indicator.setBackground(java.awt.Color.black);
                    bt_indicator.setForeground(java.awt.Color.black);
                  }


                } catch(Exception e) {
                  e.printStackTrace(System.out);
                }

                long time = new java.util.Date().getTime();
                Boolean isWindows = System.getProperty("os.name").startsWith("Windows");
                int stop_time=50;
                //if(isWindows ) stop_time=50;
                  //else stop_time=500; 
                stop_time=500; 
                if(time-start_time>stop_time) {
                  //if(aud!=null) aud.playStop();
                  if(tg_follow_blink==0) {
                    tg_indicator.setBackground(java.awt.Color.black);
                    tg_indicator.setForeground(java.awt.Color.black);
                      tg_active=false;
                  }
                }

                /////////////////////////////////////////////////////////////////////////////////////////////////
                //got the mac_id, init dbase, etc
                //STARTUP, this is currently working very well. Changing this may result in stability issues
                //be careful and do lots of testing after changing
                /////////////////////////////////////////////////////////////////////////////////////////////////
                if(parent.sys_mac_id.length()!=0 && !db_init) {

                      try {
                        tg_config = new TGConfig(parent);

                        System.out.println("mac:"+parent.sys_mac_id);
                        update_prefs();

                        dframe.did_init=false;
                        dframe.update_colors();


                        //open alias database
                        alias_db = new dbase.alias_dbase(parent);
                        if( !alias_db.init(parent.sys_mac_id) ) {
                          //JOptionPane.showMessageDialog(parent, "Couldn't open database", "ok", JOptionPane.OK_OPTION);
                          //System.exit(0);
                        }
                        //open alias database
                        talkgroups_db = new dbase.talkgroups_dbase(parent);
                        if( !talkgroups_db.init(parent.sys_mac_id) ) {
                          JOptionPane.showMessageDialog(parent, "Couldn't open database", "ok", JOptionPane.OK_OPTION);
                          System.exit(0);
                        }
                        //open events database
                        events_db = new dbase.events_dbase(parent);
                        if( !events_db.init(parent.sys_mac_id) ) {
                          //JOptionPane.showMessageDialog(parent, "Couldn't open database", "ok", JOptionPane.OK_OPTION);
                          //System.exit(0);
                        }

                      } catch(Exception e) {
                        e.printStackTrace();
                      }

                      try {
                        update_systems_path(false);
                        browser = new filesystem.FileBrowser(parent);
                        SwingUtilities.invokeLater(browser);
                      } catch(Exception e) {
                        e.printStackTrace();
                      }

                  //DONT DO THIS
                      //try {
                        //rr_importer.RR_import.update_home_paths();
                      //} catch(Exception e) {
                      //}
                      try {
                        if( rr_import != null ) {
                          rr_import = null; 
                        }
                        rr_import = new rr_importer.RR_import(parent);
                        java.awt.EventQueue.invokeLater( rr_import );
                      } catch(Exception e) {
                      }

                      try {
                        open_audio_output_files();
                        macid.setVisible(true);
                        //macid.setText("MAC: "+sys_mac_id);
                      } catch(Exception e) {
                      }


                      try {
                        edit_display_view.setEnabled(true);
                        if(alias==null) alias = new Alias(parent, parent.sys_mac_id, document_dir);
                      } catch(Exception e) {
                      }

                      try {
                        alias_db.update_alias_table();
                      } catch(Exception e) {
                      }

                    db_init=true;
                    do_read_config=1;
                }

            } catch(Exception e) {
              e.printStackTrace();
            }
          }
    }
}
boolean editing_tg=false;
boolean editing_alias=false;

int update_tg_sel=0;
boolean db_init=false;
java.lang.Runtime rt;
public String systems_path="";
int c_tg=0;
int audio_buffer_cnt=0;
int do_audio_tick=0;
int mode_b=0;
int p25_demod=0;
int is_control=1;
Boolean isWindows=true;
java.util.Timer utimer;
public int do_fixed_gain=0;
int tg_row_id=-1;
int next_row_id=-1;
int tg_edit_row=-1;
int alias_edit_row=-1;
int do_fixed_gain_state=0;
int do_disconnect;
int do_update_talkgroups;
int do_update_roaming=0;
int do_read_talkgroups;
rr_importer.RR_import rr_import;
static BTFrame parent;
public SerialPort serial_port=null;
int is_connected=0;
float current_evm_percent;
int do_connect=0;
String serial_port_name="";
char keydata[];
int sleep_factor=0;
int keyindex;
int user_squelch_level=-130;
int command_input=0;
int status_timeout=1;
rssimeter rssim1;
rssimeter rssim2;
String console_line;
int did_read_cc=0;
int sig_meter_timeout=1;
javax.swing.JLabel l1;
javax.swing.JLabel l2;
javax.swing.JLabel l3;
int did_tg_backup=1;  //don't do backup on startup
int bluetooth_streaming=0;
int bluetooth_error=0;
int bluetooth_blink;
int tg_follow_blink=0;
int tg_blink=0;
int wd_count=0;
int tick_mod;
int rx_state=0;
int skip_bytes=0;
byte[] incoming_bytes;
byte[] pcm_bytes;
byte[] pcm_crc;
byte[] constellation_bytes;
byte[] tdma_bytes;
int pcm_idx=0;
int const_idx=0;
int tdma_idx=0;
byte is_roaming=0;
LameEncoder encoder=null;
byte[] mp3_buffer;
String current_date=null;
String home_dir=null;
public String broadcastify_calls_dir=null;
FileOutputStream fos_mp3;
FileOutputStream fos_meta;
FileOutputStream fos_conlog;
FileOutputStream fos_tdma;
File mp3_file=null;
File meta_file=null;
File conlog_file=null;
File tdma_file=null;
java.text.SimpleDateFormat formatter_date;
java.text.SimpleDateFormat time_format;
float current_nco_off=0.0f;
int current_sys_id = 0;
int current_wacn_id = 0; 
int traffic_sys_id = -1;
int traffic_wacn_id = -1; 
int do_toggle_record=1;
int did_metadata=0;
int meta_count=0;
int skip_header=1;
private Dimension parentSize;
int do_read_config=1;
int do_write_config=0;
audio aud = null; 
long start_time;
String freq_str="";
SYSConfig sys_config;
Preferences prefs;
int do_agc_update=0;
int system_crc=-1;
int do_talkgroup_backup=0;
public TGConfig tg_config=null;
int do_test_freqs=0;
int do_roam_freq=0;
int do_roaming_backup=0;
int tg_update_pending=0;
int do_erase_roaming=0;
int do_backup_roaming=0;
int do_restore_roaming=0;
int do_append_roaming=0;
int do_console_output=0;
int do_write_roaming_flash_only=0;
int did_read_talkgroups=0;
int is_mac_osx=0;
int is_linux=0;
int is_windows=0;
int is_dmr_mode=0;
int tsbk_ps_i=0;
int bluetooth_streaming_timer=0;
int p25_status_timeout=1;
Hashtable rid_hash;
Hashtable lat_lon_hash1;
Hashtable lat_lon_hash2;
Hashtable supergroup_hash;
Hashtable no_loc_freqs;
Boolean do_tdma_messages=false;
ConstPlotPanel cpanel;
Boolean do_mini_const=false;
boolean do_synced;
public double current_freq=0.0;
double prev_current_freq=0.0;
long audio_tick_start=0;
int command_input_timeout=0;
long tg_blink_time=0;
String current_talkgroup="";
int reset_session=0;
java.text.SimpleDateFormat mp3_time_format = new java.text.SimpleDateFormat( "HH:mm:ss" );
String mp3_time ="";
int tg_pri=0;
int do_select_home_dir=0;
public String sys_mac_id="";
long wdog_time=0;
int did_freq_tests=0;
int sys_info_count=0;
int src_uid=0;
int prev_uid=0;
int is_enc=0;
Alias alias;
String current_alias;
int do_alias_import=0;
int do_alias_export=0;
byte[] b;
byte[] str_b;
int str_idx=0;
int avail=0;
int is_phase1=1;
int is_phase2=0;
int is_tdma_cc=0;
String tsbk_ps="";

int is_fast_mode=0;
double v_freq=0.0f;
double cc_freq=0.0f;
long status_time;
int bl_p1=0;
int current_tgzone=0;
int current_tgzone_in=0;
int global_sync_count;
audio_archive aud_archive;
String document_dir="";
logger logger_out;
sysinfo si;
String talkgroup_name;
String freqval="";
String rssi="";

BigText bt1;
BigText bt2;
BigText bt3;
BigText bt4;
BigText bt5;

displayframe_edit dframe;

String src_uid_str="";
String rf_channel="";

aliasEntry alias_dialog;

int sel_freq=0;
public int crc_errors=0;

int[] voice_crc;
int voice_crc_idx=0;
int[] btext_crc;
int btext_crc_idx=0;
displayframe_popout dvout;

si_frame si_popout;
console_frame con_popout;

String con_str="";

public int ccfg_changes=0;

JFontChooser jfc;
int tg_font_size=14;
int tg_font_style = Font.PLAIN;
String tg_font_name="Monospaced";
public Color tg_font_color;

int con_font_size=14;
int con_font_style = Font.PLAIN;
String con_font_name="Monospaced";
public Color con_font_color;


int btext_len = 0; 
int btext_acktime = 0; 
int btext_crc32 = 0; 
int cc_lcn=0;
int tdma_slot=0;
float erate=0.0f;

boolean tg_active=false;

int save_iq_len=0;
int iq_out=0;

int fw_completed=0;
int blks_per_sec=0;

int current_rssi;

int demod_type=0;
int do_cc_write=0;
int tg_zone=0;
String tg_zone_alias="";

tglog_editor tglog_e;

FileOutputStream fos_iq;
JFileChooser chooser;
freqConfiguration button_config;

double prev_p25_freq=0.0;
int on_control_freq=1;
int did_freq_update=0;
int send_const_count=0;
int roaming_toggle=0;
String prev_p25_evt="";
byte p25_follow=0;
String sys_name="";

int do_save_alias=0;

int read_channel_config=0;
CCFG ccfg=null;
int current_mod_type=0;
select_device sel_dev;
filesystem.FileBrowser browser;
public channel_config current_cc;
public int reset_count=0;
int freq_to=0;
boolean do_rescan=false;

BufferedInputStream bis_fw = null; 
firmware_update fw_update=null;

public long tg_edit_timeout=0;
public long alias_edit_timeout=0;
int tg_edit_zone=0;
//////////////////////////////////////////////////////////////
public int fw_crc=0;
//////////////////////////////////////////////////////////////

public dbase.alias_dbase alias_db=null;
public dbase.talkgroups_dbase talkgroups_db=null;
public dbase.events_dbase events_db=null;
public int do_restore_tg_csv=0;

public float tone_a_freq = 0; 
public float tone_b_freq = 0; 
public int tone_a_idx = 0; 
public int tone_b_idx = 0; 

  ///////////////////////////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////
  public boolean tg_is_complete_row(int row) {
    try {
      int cols=0;
      for(int i=0;i<9;i++) {
        Object o = jTable1.getModel().getValueAt(row,i);

        if(i==0 && o!=null) cols++;
        if(i==2 && o!=null) cols++;
        if(i==3 && o!=null) cols++;
        if(i==4 && o!=null) cols++;
        if(i==6 && o!=null) cols++;
        if(i==7 && o!=null) cols++;
        if(i==8 && o!=null) cols++;
      }

      if(cols>=7) return true;  //Description can be blank

    } catch(Exception e) {
    }
    return false;
  }
  ///////////////////////////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////
  public boolean alias_is_complete_row(int row) {
    try {
      int cols=0;
      for(int i=0;i<4;i++) {
        Object o = alias_table.getModel().getValueAt(row,i);
        if(i==0 && o!=null) cols++;
        if(i==1 && o!=null) cols++;
      }

      if(cols==2) return true;

    } catch(Exception e) {
    }
    return false;
  }

  ///////////////////////////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////
  public void update_wacn() {
    int wacnval = current_wacn_id; 
    if(wacnval!=0) {
      wacn.setText("WACN: "+String.format("0x%05X", wacnval));
    }
    else {
      wacn.setText("");
    }
    int sys_id = current_sys_id; 
    if(sys_id!=0) {
      sysid.setText("SYS_ID: "+String.format("0x%03X", sys_id));
    }
    else {
      sysid.setText("");
    }
  }

  public void browser_rescan() {
    try {
      browser.rescan();

    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  ///////////////////////////////////////////////////////////////////
    public BTFrame(String[] args) {


      initComponents();



      enable_tg_table_updates.setEnabled(false);
      update_selected_tg.setEnabled(false);
      update_alias_table.setEnabled(false);

      enable_tg_table_updates.setVisible(false);
      update_selected_tg.setVisible(false);
      update_alias_table.setVisible(false);


      //search
        InputMap inputMap_search = tabbed_pane.getInputMap();
        ActionMap actionMap_search = tabbed_pane.getActionMap();
        String searchAction = "search";
        KeyStroke key_search = KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK);
        inputMap_search.put(key_search, searchAction);
        actionMap_search.put(searchAction, new AbstractAction()
        {
          public void actionPerformed(java.awt.event.ActionEvent searchEvent)
          {
              browser.do_search_action();
          }
        });

      //search
        InputMap inputMap_search2 = jTabbedPane2.getInputMap();
        ActionMap actionMap_search2 = jTabbedPane2.getActionMap();
        String searchAction2 = "search";
        KeyStroke key_search2 = KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK);
        inputMap_search2.put(key_search, searchAction2);
        actionMap_search2.put(searchAction, new AbstractAction()
        {
          public void actionPerformed(java.awt.event.ActionEvent searchEvent)
          {
              browser.do_search_action();
          }
        });


      top_label.setText("");

      freemem.setVisible(false);

      rt = Runtime.getRuntime();

      button_config = new freqConfiguration(this);
      Rectangle r = button_config.getBounds();
      r.x = 200;
      r.y = 200;
      button_config.setBounds(r);

      chooser = new JFileChooser();

      DefaultCaret caret = (DefaultCaret) log_ta.getCaret();
      caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

      jfc = new JFontChooser();
      jfc.setSize(1024,825);

      voice_crc = new int[8];
      btext_crc = new int[8];

      edit_display_view.setEnabled(false);

      bt1 = new BigText(" ", 192, new Color(128,0,128) );
      bt2 = new BigText(" ", 128, Color.white);
      bt3 = new BigText(" ", 128, Color.red);
      bt4 = new BigText(" ", 128, Color.cyan);
      bt5 = new BigText(" ",128, Color.yellow);

      dframe = new displayframe_edit(this, bt1,bt2,bt3,bt4,bt5);

      display_frame.add(bt1);
      display_frame.add(bt2);
      display_frame.add(bt3);
      display_frame.add(bt4);
      display_frame.add(bt5);

      si = new sysinfo();

      logger_out = new logger(this);
      aud_archive = new audio_archive(this);

      autoscale_const.setVisible(false);
      nsymbols.setVisible(false);
      jLabel9.setVisible(false);
      if_slide.setVisible(false);

      audio_prog.setBackground( Color.black );
      audio_prog.setForeground( Color.green );


      formatter_date = new java.text.SimpleDateFormat( "yyyy-MM-dd" );
      String ndate = formatter_date.format(new java.util.Date() );
      String fdate=new String(ndate);  //date changed
      String exe_path = getClass().getProtectionDomain().getCodeSource().getLocation().getPath().toString();
      exe_path = exe_path.replace("BTConfig.exe", "");


      System.out.println("log file path: "+exe_path+"p25rx_conlog_"+fdate+".txt");

      try {
        //conlog_file = new File(exe_path+"p25rx_conlog_"+fdate+".txt");
        //fos_conlog = new FileOutputStream( conlog_file, true ); 
      } catch(Exception e) {
        e.printStackTrace();
      }


      b = new byte[256000];
      str_b = new byte[256000];

      supergroup_hash = new Hashtable();
      rid_hash = new Hashtable();


      //jPanel25.remove(const_panel);
      cpanel = new ConstPlotPanel(this);
      const_panel.add(cpanel, java.awt.BorderLayout.CENTER);
      //jPanel25.add(cpanel);
      //


      if(args.length>0) {
        for(int i=0;i<args.length;i++) {
          if(args[i].equals("-fastmode")) {
            System.out.println("enable fast mode");
            is_fast_mode=1;
          }
          if(args[i].equals("-console")) {
            do_console_output=1;
            System.out.println("enable console output");
          }
          if(args[i].equals("-tdma")) {
            do_tdma_messages=true;
            System.out.println("enable tdma / phase 2 messages");
          }
          if(args[i].equals("-mac")) {
            is_mac_osx=1;
            System.out.println("\r\nenabling MAC_OSX option");
          }
          if(args[i].equals("-miniconst")) {
            do_mini_const=true;
          }
          if(args[i].equals("-bl_p1")) {
            System.out.println("enable p25rx-1 bootloader support");
            bl_p1=1;
          }
        }
      }


      macid.setVisible(false);

      //agc_gain.setVisible(false); //hide agc slider related
      //jLabel3.setVisible(false);
      //agc_level_lb.setVisible(false);

      fw_ver.setVisible(false);
      fw_installed.setVisible(false);

      pcm_bytes = new byte[320];
      incoming_bytes = new byte[512];
      pcm_crc = new byte[4];
      constellation_bytes = new byte[489];
      tdma_bytes = new byte[256];

      write_config.setEnabled(false);
      disconnect.setEnabled(false);

      isWindows=false;

      if( System.getProperty("os.name").startsWith("Windows") ) {
        isWindows=true;
          System.out.println("\r\nenabling Windows option");
          os_string.setText("OS: Windows");
        is_windows=1;
      }

      //Mac OSX
      if( System.getProperty("os.name").toLowerCase().contains("mac os x") ) {
          is_mac_osx=1;
          System.out.println("\r\nenabling MAC_OSX option");
          os_string.setText("OS: Mac OSX");
      }

      if( System.getProperty("os.name").toLowerCase().contains("linux") ) {
          is_linux=1;
          System.out.println("\r\nenabling Linux option");
          os_string.setText("OS: Linux");
      }


      read_config.setVisible(false);  //read config button


      check_firmware.setEnabled(false);
      check_firmware.setVisible(false);

      record_to_mp3.setEnabled(false);
      record_to_mp3.setVisible(false);

      //macid.setText("");
      wacn.setText("");
      sysid.setText("");
      nac.setText("");
      freq.setText("");
      rfid.setText("");
      siteid.setText("");


      record_to_mp3.setSelected(true);


      formatter_date = new java.text.SimpleDateFormat( "yyyy-MM-dd" );
      time_format = new java.text.SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );



      fw_ver.setText("Latest Avail: FW Date: 202301190134");
      release_date.setText("Release: 2023-06-11 08:33");
      fw_installed.setText("   Installed FW: ");

      setProgress(-1);

      jTable1.setShowHorizontalLines(true);
      jTable1.setShowVerticalLines(true);

      alias_table.setShowHorizontalLines(true);
      alias_table.setShowVerticalLines(true);

      keydata = new char[4096];
      keyindex=0;
      jTextArea1.getCaret().setVisible(true);
      jTextArea1.getCaret().setBlinkRate(250);

      setIconImage(new javax.swing.ImageIcon(getClass().getResource("/btconfig/images/iconsmall.gif")).getImage()); // NOI18N
      setTitle("BlueTail Technologies BTT-Advanced-db 2022");

      rssim1 = new rssimeter();
      rssim2 = new rssimeter();
      //rssim1.setValue(-90,true);
      //rssim2.setValue(-20,false);

      l1 = new javax.swing.JLabel();
      l2 = new javax.swing.JLabel();
      l3 = new javax.swing.JLabel();
      l1.setText("RF Sig Level");
      l2.setText("Sig Quality");
      l1.setForeground(java.awt.Color.white);
      l2.setForeground(java.awt.Color.white);
      l3.setForeground(java.awt.Color.white);
      l3.setFont(new java.awt.Font("Monospaced", 0, 18)); // NOI18N

      l3.setText("NO SIG");
      desc_panel.add(l3);


      level_panel.add( l1 ); 
      level_panel.add(rssim1); 

      //level_panel.add( l2 ); 
      //level_panel.add(rssim2); 

      l1.setVisible(true);
      l2.setVisible(true);
      l3.setVisible(true);
      rssim1.setVisible(true);
      rssim2.setVisible(true);
      rssim1.setValue(-130,false);
      rssim2.setValue(-130,false);
      p25_status_timeout=1;
      l3.setText("");


      DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
      rightRenderer.setHorizontalAlignment(JLabel.LEFT);

      jTable1.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
      jTable1.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
      jTable1.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
      jTable1.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);
      jTable1.getColumnModel().getColumn(7).setCellRenderer(rightRenderer);

      alias_table.getColumnModel().getColumn(0).setCellRenderer(rightRenderer);
      alias_table.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
      alias_table.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
      alias_table.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);

      /*
      jTable1.setAutoCreateRowSorter(true);
      jTable1.getRowSorter().addRowSorterListener(new RowSorterListener() {
       public void sorterChanged(RowSorterEvent rse) {
        if (rse.getType() == RowSorterEvent.Type.SORT_ORDER_CHANGED) {
          if(tg_config!=null) {
            //System.out.println(rse);
            //tg_config.postSort(parent);
            java.util.List<? extends RowSorter.SortKey> sortKeys = jTable1.getRowSorter().getSortKeys();
            if(sortKeys.get(0).getSortOrder() == SortOrder.ASCENDING) {
              //jTable1.getRowSorter().setSortKeys(null);
              jTable1.getRowSorter().toggleSortOrder(0);
            }
          }
        }
       }
      });
      */

      level_panel.remove(sq_lb);
      level_panel.remove(tg_lb);
      level_panel.remove(bt_lb);

      level_panel.remove(bt_indicator);
      level_panel.remove(bt_indicator);
      level_panel.remove(tg_indicator);
      level_panel.remove(sq_indicator);

      sq_lb.setText("   SIG");
      level_panel.add(sq_lb);
      level_panel.add(sq_indicator);
      level_panel.add(tg_lb);
      level_panel.add(tg_indicator);
      level_panel.add(bt_lb);
      level_panel.add(bt_indicator);

      sq_indicator.setBackground(java.awt.Color.black);
      sq_indicator.setForeground(java.awt.Color.black);
      bt_indicator.setBackground(java.awt.Color.black);
      bt_indicator.setForeground(java.awt.Color.black);
      tg_indicator.setBackground(java.awt.Color.black);
      tg_indicator.setForeground(java.awt.Color.black);
            tg_active=false;


      //parentSize = Toolkit.getDefaultToolkit().getScreenSize();
      //setSize(new Dimension((int) (parentSize.width * 0.75), (int) (parentSize.height * 0.8)));

        //jTabbedPane1.remove( buttong_config);

      InputMap inputMap = jTable1.getInputMap(javax.swing.JComponent.WHEN_FOCUSED);
      ActionMap actionMap = jTable1.getActionMap();
      String deleteAction = "delete";
      inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0),
                deleteAction);
      actionMap.put(deleteAction, new AbstractAction()
        {
          public void actionPerformed(java.awt.event.ActionEvent deleteEvent)
          {
              delete_talkgroup_rows();
          }
        });

      InputMap inputMap2 = alias_table.getInputMap(javax.swing.JComponent.WHEN_FOCUSED);
      ActionMap actionMap2 = alias_table.getActionMap();
      String deleteAction2 = "delete";
      inputMap2.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0), deleteAction2);
      actionMap2.put(deleteAction2, new AbstractAction()
        {
          public void actionPerformed(java.awt.event.ActionEvent deleteEvent)
          {
              delete_alias_rows();
          }
        });


      InputMap inputMap3 = sig_cmd.getInputMap(javax.swing.JComponent.WHEN_FOCUSED);
      ActionMap actionMap3 = sig_cmd.getActionMap();
      String deleteAction3 = "cmd";
      inputMap3.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0),
                deleteAction3);
      actionMap3.put(deleteAction3, new AbstractAction()
        {
          public void actionPerformed(java.awt.event.ActionEvent deleteEvent)
          {
            String cmd= sig_cmd.getText().trim()+"\r\n"; 
            serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
            System.out.println(cmd);
          }
        });

      do_connect();


      //do this last
			utimer = new java.util.Timer(); 
      utimer.schedule( new updateTask(), 100, 1);
      setSize(1200,825);
    }
  //////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////
  void update_talkgroup_selected() {

    try {

      next_row_id=0;

      int nrows2 = ((DefaultTableModel) parent.jTable1.getModel()).getRowCount();
      if(nrows2>0) {
        for(int i=0;i<nrows2;i++) {
          try {

            Boolean enable = (Boolean) jTable1.getModel().getValueAt(i,0);
            Integer rowid_i = new Integer( (String) jTable1.getModel().getValueAt(i,1) );
            String sysid_s = (String) jTable1.getModel().getValueAt(i,2);
            Integer pri_i = (Integer) jTable1.getModel().getValueAt(i,3);
            Integer tg_i = (Integer) jTable1.getModel().getValueAt(i,4);
            String alpha_s = (String) jTable1.getModel().getValueAt(i,5);
            String desc_s = (String) jTable1.getModel().getValueAt(i,6);
            String wacn_s = (String) jTable1.getModel().getValueAt(i,7);
            Integer zone_i = (Integer) jTable1.getModel().getValueAt(i,8);

            int tg = tg_i.intValue();
            if( wacn_s!=null && wacn_s.startsWith("0x") ) wacn_s = wacn_s.substring(2,wacn_s.length()); 
            if( wacn_s!=null && wacn_s.startsWith("0X") ) wacn_s = wacn_s.substring(2,wacn_s.length()); 

            if( sysid_s!=null && sysid_s.startsWith("0x") ) sysid_s = sysid_s.substring(2,sysid_s.length()); 
            if( sysid_s!=null && sysid_s.startsWith("0X") ) sysid_s = sysid_s.substring(2,sysid_s.length()); 

            int wacn = Integer.valueOf(wacn_s,16).intValue(); 
            int sys_id = Integer.valueOf(sysid_s,16).intValue(); 

            int en = 0;
            if(enable) en=1;

            int pri = pri_i.intValue();
            int zone = zone_i.intValue();

            int row_id = rowid_i.intValue();
            if(row_id > next_row_id) next_row_id=row_id+1;


            if(tg_edit_zone==9999) {
              talkgroups_db.tg_insert_or_update_nodup(en, sys_id, pri, tg, alpha_s, desc_s, wacn, zone, next_row_id); 
            }
            else {
              talkgroups_db.tg_insert_or_update_nodup(en, sys_id, pri, tg, alpha_s, desc_s, wacn, zone, row_id); 
            }

            setStatus("updating from row "+i);

          } catch(Exception e) {
            e.printStackTrace();
          }
        } 

      }

      talkgroups_db.update_talkgroup_table();
      setStatus("updated "+nrows2+" records from talkgroups table");
    } catch(Exception e) {
    }
    tg_edit_timeout=0;

  }

  //////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////
  void duplicate_tg_row() {
      int[] rows = jTable1.getSelectedRows();

    Connection db_con = null; 

  try {
    db_con = parent.talkgroups_db.getCon();
    db_con.setAutoCommit(false);
      if(rows.length>0) {
        for(int i=0;i<1;i++) {
          try {
            int row_sel = jTable1.convertRowIndexToModel(rows[0]);

            Boolean enable = (Boolean) jTable1.getModel().getValueAt(row_sel,0);
            Integer rowid_i = new Integer( (String) jTable1.getModel().getValueAt(row_sel,1) );
            String sysid_s = (String) jTable1.getModel().getValueAt(row_sel,2);
            Integer pri_i = (Integer) jTable1.getModel().getValueAt(row_sel,3);
            Integer tg_i = (Integer) jTable1.getModel().getValueAt(row_sel,4);
            String alpha_s = (String) jTable1.getModel().getValueAt(row_sel,5);
            String desc_s = (String) jTable1.getModel().getValueAt(row_sel,6);
            String wacn_s = (String) jTable1.getModel().getValueAt(row_sel,7);
            Integer zone_i = (Integer) jTable1.getModel().getValueAt(row_sel,8);

            int tg = tg_i.intValue();
            if( wacn_s!=null && wacn_s.startsWith("0x") ) wacn_s = wacn_s.substring(2,wacn_s.length()); 
            if( wacn_s!=null && wacn_s.startsWith("0X") ) wacn_s = wacn_s.substring(2,wacn_s.length()); 

            if( sysid_s!=null && sysid_s.startsWith("0x") ) sysid_s = sysid_s.substring(2,sysid_s.length()); 
            if( sysid_s!=null && sysid_s.startsWith("0X") ) sysid_s = sysid_s.substring(2,sysid_s.length()); 

            int wacn = Integer.valueOf(wacn_s,16).intValue(); 
            int sys_id = Integer.valueOf(sysid_s,16).intValue(); 

            int en = 0;
            if(enable) en=1;

            int pri = pri_i.intValue();
            int zone = zone_i.intValue();
            zone=9999;

            int max_row = parent.talkgroups_db.tg_get_max_row();

            talkgroups_db.tg_insert_or_update_nodup(en, sys_id, pri, tg, alpha_s, desc_s, wacn, zone, max_row+1);

          } catch(Exception e) {
            e.printStackTrace();
          }
        }
      }
    db_con.commit();
    //db_con.setAutoCommit(true);
  } catch(Exception e) {
    try {
      db_con.commit();
    } catch(Exception e2) {
    }
  }

    talkgroups_db.update_talkgroup_table();
    setStatus("duplicated record from talkgroups table");

    try {
      db_con.commit();
    } catch(Exception e3) {
    }
  }
  //////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////
  void delete_talkgroup_rows() {
      int[] rows = jTable1.getSelectedRows();

    Connection db_con = null; 

  try {
    db_con = parent.talkgroups_db.getCon();
    db_con.setAutoCommit(false);
      if(rows.length>0) {
        for(int i=0;i<rows.length;i++) {
          try {
            int rowid = new Integer( (String) jTable1.getModel().getValueAt(jTable1.convertRowIndexToModel(rows[i]),1) );

            talkgroups_db.talkgroup_delete_tg(rowid);
            setStatus("deleting record "+i);
          } catch(Exception e) {
            e.printStackTrace();
          }
        } 
      }
    db_con.commit();
    //db_con.setAutoCommit(true);
  } catch(Exception e) {
    try {
      db_con.commit();
    } catch(Exception e2) {
    }
  }

    talkgroups_db.update_talkgroup_table();
    setStatus("deleted "+rows.length+" records from talkgroups table");

    try {
      db_con.commit();
    } catch(Exception e3) {
    }
  }

  public TGConfig getTGConfig() {
    return tg_config;
  }

  //////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////
  void delete_alias_rows() {
    int[] rows = alias_table.getSelectedRows();
    if(rows.length==0) return;

    Connection db_con = null; 
    try {
      db_con = parent.alias_db.getCon();
      db_con.setAutoCommit(false);
      if(rows.length>0) {
        for(int i=0;i<rows.length;i++) {
          try {
              Integer rid_i = (Integer) alias_table.getModel().getValueAt(alias_table.convertRowIndexToModel(rows[i]),0);
              int rid = Integer.valueOf(rid_i);
              alias_db.alias_delete_rid(rid);
            } catch(Exception e) {
              e.printStackTrace();
            }
          setStatus("delete record "+i);
        }
      }
      db_con.commit();
      //db_con.setAutoCommit(true);
    } catch(Exception e) {
      try {
        db_con.commit();
      } catch(Exception e2) {
      }
    }
    alias_db.update_alias_table();
    setStatus("deleted "+rows.length+" records from alias table");
    try {
      db_con.commit();
    } catch(Exception e3) {
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////
  public SerialPort find_serial_port()
  {

    int n=0;

    SerialPort[] ports = SerialPort.getCommPorts();
    String[] _devs = new String[ports.length];
    SerialPort[] _port = new SerialPort[ports.length]; 
    int fcnt=0;

    for(int i=0; i<ports.length; i++) {
        String isopen="";
        if( ports[i].isOpen() ) isopen=" open";
          else isopen="  closed";


        System.out.println("\r\n["+i+"]  found device on : "+
          ports[i].getSystemPortName()+"  "+
          ports[i].getDescriptivePortName()+"  "+
          ports[i].getPortDescription()+"  "+
          ports[i].toString()+isopen);

          //String bl_search_str = "BlueTail-P25RX-II";
          if( ports[i].toString().startsWith("BlueTail-P25RX-II") && !ports[i].isOpen() ) {
            _port[fcnt] = ports[i];
            _devs[fcnt++] = ports[i].toString()+" - "+ports[i].getSystemPortName()+" - "+isopen;

          }
    }

    String devs[] = new String[ fcnt ];
    for(int i=0;i<fcnt;i++) {
      devs[i] = _devs[i]; 
    }



    String dev_to_use="";

    try {
      /*
      if(devs.length>0) {
        sel_dev = new select_device(this);
        new Thread(sel_dev).start();
        sel_dev.set_devs( devs );
        //sel_dev.show();

        //dev_to_use = sel_dev.get_dev();
        int idx = sel_dev.get_dev_idx();

        //if(dev_to_use!=null) {
        if(idx>=0) {

                int i = idx;
              //for(int i=0; i<ports.length; i++) {

                //if( ports[i].toString().equals( dev_to_use ) ) {

                  if(!_port[i].isOpen()) {
                    if( _port[i].openPort(200) ) {
                      _port[i].closePort();
                      System.out.println("using ["+i+"]  "+_port[i]);

                      if( _port[i].openPort(200) ) {
                        String fastmode_str="";
                        if(is_fast_mode==1) fastmode_str="  FAST MODE ON";
                        ser_dev.setText("PORT: "+_port[i].getSystemPortName()+fastmode_str);
                        return _port[i];
                      }
                    }
                    else {
                        System.out.println("attempting to close locked _port "+_port[i]);
                        _port[i].closePort();
                        if( _port[i].openPort(200) ) {
                          System.out.println("using ["+i+"]  "+_port[i]);
                          return _port[i];
                        }
                    }
                  }
                //}
              //}

        }

        //String dev_to_use = sel_dev.getDevice();  
      }
      */
    } catch(Exception e) {
    }


    for(int i=0; i<ports.length; i++) {
      //setStatus("\r\nport: "+ports[i]+" on " + ports[i].getSystemPortName());

      //String bl_search_str = "BlueTail-P25RX-II";
      //if(bl_p1==1) bl_search_str="BlueTail-P";  //allow p25rx bootloader for first proto

      if( ports[i].toString().startsWith("BlueTail") ) {

        if(!ports[i].isOpen()) {
          if( ports[i].openPort(20) ) {
            ports[i].closePort();
            System.out.println("using ["+i+"]  "+ports[i]);

            String fastmode_str="";
            if(is_fast_mode==1) fastmode_str="  FAST MODE ON";
            ser_dev.setText("PORT: "+ports[i].getSystemPortName()+fastmode_str);
            return ports[i];
          }
          else {
              System.out.println("attempting to close locked port "+ports[i]);
              ports[i].closePort();
              if( ports[i].openPort(20) ) {
                System.out.println("using ["+i+"]  "+ports[i]);
                return ports[i];
              }
          }
        }
      }
    }

    return null;
  }

/////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////
void update_rssi(int rssi_b) {

  try {
    if(freq_to>0) freq_to--;

    current_rssi = rssi_b;

    rssi = String.format("%03d", rssi_b);
    p25_status_timeout=6000;

    if( rssim1.getValue()<-127 && blks_per_sec==0) {
      l3.setText("NO SIG");
    }
    else {

      if(on_control_freq==1) {
        //FORMAT ON CC
        String fmt = status_format_cc.getText(); 
        String cc_str = dframe.do_subs(fmt,false);
        l3.setText(cc_str);
      }
      else {
        //FORMAT ON VOICE
        String fmt = status_format_voice.getText(); 
        String voice_str = dframe.do_subs(fmt,false);
        l3.setText(voice_str);
      }


      reset_session=1;
    }


    rssim1.setValue( Integer.valueOf(rssi_b).intValue(),true );
    sig_meter_timeout=20000;

    try {
      //if( did_freq_update==0 ) {
        dframe.update_colors();
       // did_freq_update=1;
      //}
    } catch(Exception e) {
    }
  } catch(Exception e) {
  }
}


    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////
    public byte[] encode_mp3(byte[] pcm) {

      int len=0;
      byte[] b=null;


      try {

        if(encoder==null) {
          //AudioFormat inputFormat = new AudioFormat( new AudioFormat.Encoding("PCM_SIGNED"), 8000.0f, 16, 1, 160, 50, false);
          AudioFormat inputFormat = new AudioFormat( 8000.0f, 16, 1, true, false);  //booleans are signed, big-endian
          //encoder = new LameEncoder(inputFormat, 256, MPEGMode.MONO, Lame.QUALITY_LOWEST, false);
          encoder = new LameEncoder(inputFormat, 32, MPEGMode.MONO, Lame.QUALITY_LOWEST, true);
          //ByteArrayOutputStream mp3 = new ByteArrayOutputStream();
          mp3_buffer = new byte[encoder.getPCMBufferSize()];

          //int bytesToTransfer = Math.min(buffer.length, pcm.length);
          //int bytesWritten;
          //int currentPcmPosition = 0;
          //while (0 < (bytesWritten = encoder.encodeBuffer(pcm, currentPcmPosition, bytesToTransfer, buffer))) {
           // currentPcmPosition += bytesToTransfer;
            //bytesToTransfer = Math.min(buffer.length, pcm.length - currentPcmPosition);

            //mp3.write(buffer, 0, bytesWritten);
          //}

          //encoder.close();
          //return mp3.toByteArray();
        }
        else {
          len = encoder.encodeBuffer(pcm, 0, 320, mp3_buffer);
          //addTextConsole("\r\nencoder: "+len);
        }

        if(len==0) return null;

        b = new byte[len];

        if(len>0) {
          for(int i=0;i<len;i++) {
            b[i] = mp3_buffer[i];
          }
        }
      } catch(Exception e) {
        //e.printStackTrace();
        e.printStackTrace();
      }

      return b;
    }

    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////
    public void addTableObject(Object obj, int row, int col) {
      jTable1.getModel().setValueAt(obj,row,col);
    }
    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////
    public Object getTableObject(int row, int col) {
      return jTable1.getModel().getValueAt(row,col);
    }
    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////
    public void addAliasObject(Object obj, int row, int col) {
      alias_table.getModel().setValueAt(obj,row,col);
    }
    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////
    public Object getAliasObject(int row, int col) {
      return alias_table.getModel().getValueAt(row,col);
    }

    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////
    void addP25Event(String str) {
      try {

        String date = dframe.getDateTimeStr(); 

        if( str.equals(prev_p25_evt) ) {
          //if( en_evt_output.isSelected() &&  !str.contains("SITE_EVT")) {
            //addTextConsole(".");
          //}
        } 
        else {
          if( en_evt_output.isSelected() ) {
            addTextConsole("\r\n"+date+","+str.trim());
          }
          prev_p25_evt=str;
        }


        if(document_dir==null || document_dir.length()==0) return;

        if( en_evt_output.isSelected() ) {
          try {
            String fs =  System.getProperty("file.separator");
            String wacn_out = String.format("%05X", current_wacn_id);
            String sysid_out = String.format("%03X", current_sys_id);
            String hdir = document_dir+fs+sys_mac_id+fs+"p25rx_eventlog_"+current_date+".txt";
            String header = "\r\n#EVT: op, p1, p2, p3, p4";
            if( logger_out!=null ) logger_out.write_log( "\r\n"+date+","+str.trim(), hdir, header );
            console_line = "";
          } catch(Exception e) {
          }
        }
      } catch(Exception e) {
      }
    }

    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////
    void addTextConsole(String str) {

      if(command_input==1) return;

   try {

       /*
      if( (!con_str.contains("\r\n") && !con_str.equals(".") && command_input_timeout==0)) {
        return;
      }
       */

      if(str!=null && do_console_output==1) System.out.println(str.trim());


      String talkgroup="";
      //freqval="";
      tsbk_ps="";

      if(console_line==null) console_line = new String("");
      console_line = console_line.concat(str);


       /////////////////////////////////////////////////////////////////////////////////////////////////////////
       //         EVENT SECTION
       /////////////////////////////////////////////////////////////////////////////////////////////////////////





      if(console_line.contains("P25_PII_CC") ) {
        is_tdma_cc=1;
        p25_status_timeout=6000;
      }

      if(console_line.contains("$TDMA") ) {
        p25_status_timeout=6000;
      }


      if(console_line.contains("VGA_CAL:") && console_line.contains("$") ) {
        StringTokenizer st = new StringTokenizer(console_line,"\r\n$");
        String st1 = ""; 
        int cnt=0;
        while(st.hasMoreTokens() && cnt++<25) {
          st1 = st.nextToken();
          if(st1!=null && st1.contains("VGA_CAL:")) {
            try {
              setStatus(st1);
            } catch(Exception e) {
              e.printStackTrace();
            }
          }
        }
      }


       try {
          console_line = new String("");
        } catch(Exception e) {
            e.printStackTrace();
          console_line = new String("");
        }

      if( jTextArea1.getText().length() > 128000 ) {
        jTextArea1.replaceRange("",0,64000);
      }

        if(str.length()>0 ) {

          //String date = time_format.format(new java.util.Date() );
          String date = dframe.getDateTimeStr(); 
          //str = str.replaceAll("DATA_SYNC",date+" "+"DATA_SYNC");
          //str = str.replaceAll("found DMR_BS_VOICE_SYNC",date+" "+"found DMR_BS_VOICE_SYNC");

          jTextArea1.append(str);
          jTextArea1.setCaretPosition(jTextArea1.getText().length());

          jTextArea1.getCaret().setVisible(true);
          jTextArea1.getCaret().setBlinkRate(250);
        }
     } catch(Exception e) {
       e.printStackTrace();
     }
    }


/////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////
public void handle_tone_out(int p1, int p2, int p3, int p4) {
  try {
    String date = dframe.getDateTimeStr(); 
    String rec = ""; 

    tone_a_freq = (float) p1 / 100.0f;
    tone_b_freq = (float) p3 / 100.0f;
    tone_a_idx = p2;
    tone_b_idx = p4;

    int op = 28; //TONE_OUT EVT
    String op_name = "TONE_OUT_EVT:";

    rec = String.format("\r\n%s, op=%d, %s, %3.2f, %d, %3.2f, %d", date, op, op_name, tone_a_freq, tone_a_idx, tone_b_freq, tone_b_idx); 
    addP25Event(rec);

    String fs =  System.getProperty("file.separator");
    String wacn_out = String.format("%05X", current_wacn_id);
    String sysid_out = String.format("%03X", current_sys_id);
    String hdir = document_dir+fs+sys_mac_id+fs+"toneouts-"+current_date+".txt";
    String header = "\r\n#tone-out, date, op, op_name, tone_a_freq_hz, tone_a_idx, tone_b_freq_hz, tone_b_idx";
    if( logger_out!=null ) logger_out.write_log( rec, hdir, header );


  } catch(Exception e) {
  }

}
/////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////
public void handle_cc_search_update(int p1, int p2, int p3, int p4) {
  try {

    double st_freq = (double) p2 / (double)1e6;
    double c_freq = (double) p1 / (double)1e6;
    double bw = (double) p3 / (double)1e6;

    //System.out.println(st_freq+" "+bw+" "+c_freq);

    double complete = ((c_freq-st_freq)/bw)*100.0;

    //System.out.println("complete "+complete);

    if(complete>98) complete=100.0;
    ccfg_prog.setValue( (int) complete );

    String iteration = new Integer(p4).toString();
    ccfg_iteration_lb.setText( "Iterations left: "+iteration );

  } catch(Exception e) {
  }
}
/////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////
public void handle_sig_found(int p1, int p2, int p3, int p4) {
  try {
      String date = dframe.getDateTimeStr(); 
      String rec = ""; 

      String protocol="";
      if(p1==1) protocol="P25";
      if(p1==2) protocol="DMR DATA";
      if(p1==3) protocol="DMR VOICE";
      if(p1==4) protocol="NXDN";
      if(p1==5) protocol="ANALOG";

      double f1 = (double) ((double) p2 / 1e6);

      rec = String.format("\r\n%s, %s, %3.5f, %d", date, protocol, f1, p3);

      try {
        String txt = ccfg_ta.getText();
        txt = txt.concat("\r\n"+rec.trim());
        ccfg_ta.setText(txt);

        /*
        if( ccfg_add_digital_ch.isSelected() && !cl.isDuplicate(String.format("%3.5f",f1)) ) {
          channel_config cc = new channel_config(null, f1);
          cc.modulation_type = current_mod_type;
          cc.squelch_enable = false; 
          cc.squelch_level = slider_squelch.getValue(); 
        }
        */
      } catch(Exception e) {
      }

      String fs =  System.getProperty("file.separator");
      String wacn_out = String.format("%05X", current_wacn_id);
      String sysid_out = String.format("%03X", current_sys_id);
      String hdir = document_dir+fs+sys_mac_id+fs+"p25rx_cc_scan_"+current_date+".txt";
      String header = "\r\n#cc_search output";
      if( logger_out!=null ) logger_out.write_log( rec, hdir, header );
    } catch(Exception e) {
    }
}
/////////////////////////////////////////////////////////////////////////////////////////////////////////
//         TG PRI EVENT 
/////////////////////////////////////////////////////////////////////////////////////////////////////////
public void handle_tg_pri(int p1, int p2, int p3, int p4) {

  String tg_id = Integer.valueOf(p1).toString(); 

  if(tg_id!=null) {
    current_talkgroup = tg_id;
  }
  tg_pri=1;
  src_uid=0;
}
/////////////////////////////////////////////////////////////////////////////////////////////////////////
//         ADJACENT MULT GRPS EVENT 
/////////////////////////////////////////////////////////////////////////////////////////////////////////
public void handle_adj_mult(int p1, int p2, int p3, int p4) {
  String active_tg= String.format("Adjacent Active Talk Groups: %d, %d", p2, p4);
  setStatus(active_tg);
}
/////////////////////////////////////////////////////////////////////////////////////////////////////////
//         ADJACENT SINGLE GRPS EVENT 
/////////////////////////////////////////////////////////////////////////////////////////////////////////
public void handle_adj_single(int p1, int p2, int p3, int p4) {
  String active_tg= String.format("Adjacent Active Talk Groups: %d", p1);
  setStatus(active_tg);
}
/////////////////////////////////////////////////////////////////////////////////////////////////////////
//         P25 RID EVENT 
  //case  0x3a  :
  //  handle_site( op, p2); //primary
  //break;
  //case  0x3b  :
  //  handle_site( op, p2); //primary
  //break;
  //case  0x39  :
  //  handle_site( op, p2); //secondary
  //break;
  //case  0x3c  :
  //  handle_site( op, p2); //adjacent
  //break;
/////////////////////////////////////////////////////////////////////////////////////////////////////////
public void handle_site( int op, int freq_hz) {

  /*
    String rec=", ";

    double f = Double.valueOf( ((double) freq_hz) / 1e6 );
    switch(op) {
      case  0x3a  :
        roaming_tests.addPrimary( String.format("%3.5f", f) );
        rec = rec.concat("PRIMARY,");
      break;
      case  0x3b  :
        roaming_tests.addPrimary( String.format("%3.5f", f) );
        rec = rec.concat("PRIMARY,");
      break;
      case  0x39  :
        roaming_tests.addSecondary( String.format("%3.5f", f) );
        rec = rec.concat("SECONDARY,");
      break;
      case  0x3c  :
        roaming_tests.addAdjacent( String.format("%3.5f", f) );
        rec = rec.concat("NEIGHBOR,");
      break;
    }
    rec = rec.concat( String.format("%3.6f", f) );

    try {
      String date = dframe.getDateTimeStr(); 
      String fs =  System.getProperty("file.separator");
      String wacn_out = String.format("%05X", current_wacn_id);
      String sysid_out = String.format("%03X", current_sys_id);
      String hdir = document_dir+fs+sys_mac_id+fs+"p25rx_eventlog_"+current_date+".txt";
      String header = "\r\n#EVT: op, p1, p2, p3, p4";
      if( logger_out!=null ) logger_out.write_log( rec, hdir, header );
      console_line = "";
    } catch(Exception e) {
    }
  */
}
/////////////////////////////////////////////////////////////////////////////////////////////////////////
//         P25 HDU EVENT 
/////////////////////////////////////////////////////////////////////////////////////////////////////////
public void handle_hdu(int p1, int p2 ) {
  try {
  } catch(Exception e) {
  }
}
/////////////////////////////////////////////////////////////////////////////////////////////////////////
//         P25 ENDCALL EVENT 
/////////////////////////////////////////////////////////////////////////////////////////////////////////
public void handle_endcall( ) {
  try {
    int silent_time = new Integer( parent.end_call_silence.getText() ).intValue();
    if(aud_archive!=null && silent_time>0) {
      String fs =  System.getProperty("file.separator");
      aud_archive.addSilence( silent_time, current_talkgroup, home_dir+fs+sys_mac_id, current_wacn_id, current_sys_id );

      try {
        aud_archive.close_all_rdio();
      } catch(Exception e) {
      }

      try {
        //aud_archive.close_all_bcalls(0);
      } catch(Exception e) {
      }
    }
  } catch(Exception e) {
  }
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////
//         P25 EMERGENCY EVENT 
/////////////////////////////////////////////////////////////////////////////////////////////////////////
public void handle_emergency( int p1, int p2, int p3) {

  try {
    double fr = Double.valueOf((double) p1 / 1e6);
    int ga = Integer.valueOf(p2);
    int sa = Integer.valueOf(p3);

    String date = dframe.getDateTimeStr(); 
    String rec = String.format("\r\nEMRG_RESP(0x27),%s,%3.6f,%d,%d", date, fr, ga, sa);

    String fs =  System.getProperty("file.separator");
    String wacn_out = String.format("%05X", current_wacn_id);
    String sysid_out = String.format("%03X", current_sys_id);
    String hdir = document_dir+fs+sys_mac_id+fs+"p25rx_emergency_"+current_date+"-"+wacn_out+"-"+sysid_out+".txt";
    String header = "OPCODE,TIME,Frequency,TGroup,RADIO_ID";
    if( logger_out!=null && current_wacn_id!=0 && current_sys_id!=0) logger_out.write_log( rec, hdir, header );
  } catch(Exception e) {
  }
}
/////////////////////////////////////////////////////////////////////////////////////////////////////////
//         P25 AFFILIATION EVENT 
/////////////////////////////////////////////////////////////////////////////////////////////////////////
public void handle_affiliation( int p1, int p2, int p3) {
  try {
    double fr = Double.valueOf((double) p1 / 1e6);
    int ga = Integer.valueOf(p2);
    int sa = Integer.valueOf(p3);

    String date = dframe.getDateTimeStr(); 
    String rec = String.format("\r\nGRP_AFF_RESP(0x28),%s,%3.6f,%d,%d", date, fr, ga, sa);

    String fs =  System.getProperty("file.separator");
    String wacn_out = String.format("%05X", current_wacn_id);
    String sysid_out = String.format("%03X", current_sys_id);
    String hdir = document_dir+fs+sys_mac_id+fs+"p25rx_affiliation_"+current_date+"-"+wacn_out+"-"+sysid_out+".txt";
    String header = "OPCODE,TIME,Frequency,TGroup,RADIO_ID";
    if( logger_out!=null && current_wacn_id!=0 && current_sys_id!=0) logger_out.write_log( rec, hdir, header );
  } catch(Exception e) {
  }
}
/////////////////////////////////////////////////////////////////////////////////////////////////////////
//         P25 PATCH EVENT 
/////////////////////////////////////////////////////////////////////////////////////////////////////////
public void handle_patch(int p1, int p2, int p3, int p4) {
}
/////////////////////////////////////////////////////////////////////////////////////////////////////////
//         P25 ENC EVENT 
/////////////////////////////////////////////////////////////////////////////////////////////////////////
public void handle_encryption(int p1, int p2, int p3, int p4) {
}
/////////////////////////////////////////////////////////////////////////////////////////////////////////
//         DMR VGRANT EVENT 
  //         add_event_rec(P25_EVT_DMR_VGRANT,dmr_current_talkgroup,freq_l,vg_radio_id,goto_l);
/////////////////////////////////////////////////////////////////////////////////////////////////////////
public void handle_dmr_vgrant(int grp, int freq1, int src, int goto_lcn) {

    cc_freq = (double) (freq1/1e6);

    rf_channel = String.format("%d", goto_lcn);

    String ridstr = Integer.valueOf(src).toString();

    if(src!=0 && src != prev_uid) {
      did_metadata=0;
    }
    src_uid = src;

    try {
      if(alias!=null && src!=0) {
        try {
          Connection con = alias_db.getCon();
          alias_db.addRIDAlias(src, "", grp, talkgroup_name);
          con.commit();
        } catch(Exception e) {
        }
      }

      try {
          dframe.update_colors();
      } catch(Exception e) {
      }

    } catch(Exception e) {
    }


    if(ridstr!=null && ridstr.length()>0 && current_alias!=null) {
      status.setText("RID: "+ridstr+", "+current_alias);
    }
    else if(ridstr!=null && ridstr.length()>0 ) {
      status.setText("RID: "+ridstr);
    }

    try {

      if(tglog_e!=null && tglog_e.tg_trig_nzrid.isSelected() && src_uid!=0) do_meta();
      if(src_uid!=0) src_uid_str = new Integer(src_uid).toString();

      if(tglog_e!=null && tglog_e.tg_trig_nzrid.isSelected() && src_uid!=0) do_meta();
        else if(tglog_e!=null && tglog_e.tg_trig_anyrid.isSelected() ) do_meta();

    } catch(Exception e) {
      e.printStackTrace();
    }



}
/////////////////////////////////////////////////////////////////////////////////////////////////////////
//         P25 RID EVENT 
/////////////////////////////////////////////////////////////////////////////////////////////////////////
public void handle_rid( int src, int grp) {

    try {
        //if(parent.current_alias==null || parent.current_alias.equals("")) {
          parent.current_alias = parent.alias.getAlias(parent.src_uid);
        //}
    } catch(Exception e) {
      e.printStackTrace();
    }
    try {
      if(tglog_e!=null && tglog_e.tg_trig_nzrid.isSelected() && src_uid!=0) do_meta();
      if(src_uid!=0) src_uid_str = new Integer(src_uid).toString();

      if(tglog_e!=null && tglog_e.tg_trig_nzrid.isSelected() && src_uid!=0) do_meta();
        else if(tglog_e!=null && tglog_e.tg_trig_anyrid.isSelected() ) do_meta();

    } catch(Exception e) {
      e.printStackTrace();
    }

    String ridstr = Integer.valueOf(src).toString();

    if(src!=0 && src != prev_uid) {
      did_metadata=0;
    }
    src_uid = src;

    try {
      if(alias!=null && src!=0) {
        try {
          Connection con = alias_db.getCon();
          alias_db.addRIDAlias(src, "", grp, talkgroup_name);
          con.commit();
        } catch(Exception e) {
        }
      }

      try {
          dframe.update_colors();
      } catch(Exception e) {
      }

    } catch(Exception e) {
    }


    if(ridstr!=null && ridstr.length()>0 && current_alias!=null) {
      status.setText("RID: "+ridstr+", "+current_alias);
    }
    else if(ridstr!=null && ridstr.length()>0 ) {
      status.setText("RID: "+ridstr);
    }

}
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
void update_pause(byte b) {
  if( b!=0 && !ccfg_search_pause.isSelected() ) {
    ccfg_search_pause.setSelected(true);
    ccfg_search_pause.setBackground( Color.green );
    ccfg_search_pause.setText("Resume");
  }
  else if( b==0 && ccfg_search_pause.isSelected() ) {
    ccfg_search_pause.setSelected(false);
    ccfg_search_pause.setBackground( Color.gray );
    ccfg_search_pause.setText("Pause");
  }
}
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
void update_follow(byte b) {
  if( b!=0 && !hold1.isSelected() ) {
    hold1.setSelected(true);
    hold1.setBackground( Color.green );
  }
  else if( b==0 && hold1.isSelected() ) {
    hold1.setSelected(false);
    hold1.setBackground( Color.gray );
  }
}
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
void update_roaming(byte b) {
  is_roaming = b;

  if( b!=0  ) {
    pause_roaming.setSelected(true);
    pause_roaming.setBackground( Color.green );
    next_freq.setEnabled(false);
    prev_freq.setEnabled(false);
    //freq_ch_lb.setEnabled(false);
  }
  else if( b==0  ) {
    pause_roaming.setSelected(false);
    pause_roaming.setBackground( Color.gray );
    next_freq.setEnabled(true);
    prev_freq.setEnabled(true);
    //freq_ch_lb.setEnabled(true);
  }
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
void update_tgid(int tg_id) {

  if(current_wacn_id==0 || current_wacn_id!=traffic_wacn_id) {
    return;
  }
  if(current_sys_id==0 || current_sys_id!=traffic_sys_id) {
    return;
  }


    if(tg_id==0) return;
    if(current_wacn_id==0) return;
    if(current_sys_id==0) return;

    //if( on_control_freq==0 && current_sys_id!=0 && current_wacn_id!=0 && send_const_count>3) {
    if(send_const_count>3) {
      tg_config.addUknownTG(parent, Integer.valueOf(tg_id).toString(), Integer.valueOf(current_sys_id).toString(), Integer.valueOf(current_wacn_id).toString() ); 
    }

    tg_zone = tg_config.find_tg_zone(this, Integer.valueOf(tg_id).toString() ); 
    if(tg_zone>0 && tg_zone<=16) {
      if(tg_zone==1 && prefs!=null) tg_zone_alias = prefs.get("zone1_alias","");
      if(tg_zone==2 && prefs!=null) tg_zone_alias = prefs.get("zone2_alias","");
      if(tg_zone==3 && prefs!=null) tg_zone_alias = prefs.get("zone3_alias","");
      if(tg_zone==4 && prefs!=null) tg_zone_alias = prefs.get("zone4_alias","");
      if(tg_zone==5 && prefs!=null) tg_zone_alias = prefs.get("zone5_alias","");
      if(tg_zone==6 && prefs!=null) tg_zone_alias = prefs.get("zone6_alias","");
      if(tg_zone==7 && prefs!=null) tg_zone_alias = prefs.get("zone7_alias","");
      if(tg_zone==8 && prefs!=null) tg_zone_alias = prefs.get("zone8_alias","");
      if(tg_zone==9 && prefs!=null) tg_zone_alias = prefs.get("zone9_alias","");
      if(tg_zone==10 && prefs!=null) tg_zone_alias = prefs.get("zone10_alias","");
      if(tg_zone==11 && prefs!=null) tg_zone_alias = prefs.get("zone11_alias","");
      if(tg_zone==12 && prefs!=null) tg_zone_alias = prefs.get("zone12_alias","");
      if(tg_zone==13 && prefs!=null) tg_zone_alias = prefs.get("zone13_alias","");
      if(tg_zone==14 && prefs!=null) tg_zone_alias = prefs.get("zone14_alias","");
      if(tg_zone==15 && prefs!=null) tg_zone_alias = prefs.get("zone15_alias","");
      if(tg_zone==16 && prefs!=null) tg_zone_alias = prefs.get("zone16_alias","");
    }
}



    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////
    void setAlias(String alias) {
      current_alias=alias;
    }
    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////
    void setProgress(int pcomplete) {
      if(pcomplete<0) {
        progbar.setVisible(false);
        progbar.setValue(0);
        progress_label.setVisible(false);
      }
      else {
        progbar.setVisible(true);
        progress_label.setVisible(true);
        progbar.setValue(pcomplete);
      }
      repaint();
    }

    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////
    public void update_current_freq() {
      try {
        if( current_freq == prev_current_freq) return;

        prev_current_freq = current_freq;

        String f = String.format( "%3.5f", (double)current_freq/(double)1e6);
        byte[] b = f.getBytes();

          //System.out.println("len: "+b.length);
          //len=8  e.g. 27 MHz
          //len=9  e.g. 100 MHz
          //len=10  e.g. 1000 MHz

          int off=0;

          if(b.length==10) {
            freq_9.setVisible(true);
            freq_9.setText( Integer.toString( (int) (b[0]-0x30) ) );
            off++;
          }
          else {
            freq_9.setVisible(false);
            freq_9.setText( Integer.toString( (int) (0) ) );
          }
          if(b.length>=9) {
            freq_8.setVisible(true);
            freq_8.setText( Integer.toString( (int) (b[0+off]-0x30) ) );
            off++;
          }
          else {
            freq_8.setVisible(false);
            freq_8.setText( Integer.toString( (int) (0) ) );
          }

          freq_7.setText( Integer.toString( (int) (b[0+off]-0x30) ) );
          freq_6.setText( Integer.toString( (int) (b[1+off]-0x30) ) );

          freq_5.setText( Integer.toString( (int) (b[3+off]-0x30) ) );
          freq_4.setText( Integer.toString( (int) (b[4+off]-0x30) ) );
          freq_3.setText( Integer.toString( (int) (b[5+off]-0x30) ) );
          freq_2.setText( Integer.toString( (int) (b[6+off]-0x30) ) );
          freq_1.setText( Integer.toString( (int) (b[7+off]-0x30) ) );


      } catch(Exception e) {
        e.printStackTrace();
      }
    }

    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////
    public void setStatus(String str) {
      if(str==null) return;
      status.setVisible(true);
      status.setText("Status: "+str);
      status_timeout=1600;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        buttonGroup4 = new javax.swing.ButtonGroup();
        buttonGroup5 = new javax.swing.ButtonGroup();
        buttonGroup6 = new javax.swing.ButtonGroup();
        buttonGroup7 = new javax.swing.ButtonGroup();
        buttonGroup8 = new javax.swing.ButtonGroup();
        buttonGroup9 = new javax.swing.ButtonGroup();
        buttonGroup10 = new javax.swing.ButtonGroup();
        buttonGroup11 = new javax.swing.ButtonGroup();
        buttonGroup12 = new javax.swing.ButtonGroup();
        buttonGroup13 = new javax.swing.ButtonGroup();
        buttonGroup14 = new javax.swing.ButtonGroup();
        buttonGroup15 = new javax.swing.ButtonGroup();
        buttonGroup16 = new javax.swing.ButtonGroup();
        buttonGroup17 = new javax.swing.ButtonGroup();
        buttonGroup18 = new javax.swing.ButtonGroup();
        buttonGroup19 = new javax.swing.ButtonGroup();
        buttonGroup20 = new javax.swing.ButtonGroup();
        buttonGroup21 = new javax.swing.ButtonGroup();
        buttonGroup22 = new javax.swing.ButtonGroup();
        buttonGroup23 = new javax.swing.ButtonGroup();
        buttonGroup24 = new javax.swing.ButtonGroup();
        buttonGroup25 = new javax.swing.ButtonGroup();
        southpanel = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        status_panel = new javax.swing.JPanel();
        tiny_const = new javax.swing.JPanel();
        jPanel57 = new javax.swing.JPanel();
        jLabel23 = new javax.swing.JLabel();
        z1 = new javax.swing.JToggleButton();
        z2 = new javax.swing.JToggleButton();
        z3 = new javax.swing.JToggleButton();
        z4 = new javax.swing.JToggleButton();
        z5 = new javax.swing.JToggleButton();
        z6 = new javax.swing.JToggleButton();
        z7 = new javax.swing.JToggleButton();
        z8 = new javax.swing.JToggleButton();
        z9 = new javax.swing.JToggleButton();
        z10 = new javax.swing.JToggleButton();
        z11 = new javax.swing.JToggleButton();
        z12 = new javax.swing.JToggleButton();
        z13 = new javax.swing.JToggleButton();
        z14 = new javax.swing.JToggleButton();
        z15 = new javax.swing.JToggleButton();
        z16 = new javax.swing.JToggleButton();
        hold1 = new javax.swing.JButton();
        skip1 = new javax.swing.JButton();
        mute = new javax.swing.JToggleButton();
        edit_alias1 = new javax.swing.JButton();
        jPanel10 = new javax.swing.JPanel();
        jPanel52 = new javax.swing.JPanel();
        status = new javax.swing.JLabel();
        jPanel44 = new javax.swing.JPanel();
        ch_step_lb = new javax.swing.JLabel();
        stepdown = new javax.swing.JToggleButton();
        jSeparator8 = new javax.swing.JSeparator();
        stepup = new javax.swing.JToggleButton();
        jSeparator7 = new javax.swing.JSeparator();
        freq_9 = new javax.swing.JToggleButton();
        freq_8 = new javax.swing.JToggleButton();
        freq_7 = new javax.swing.JToggleButton();
        freq_6 = new javax.swing.JToggleButton();
        freq_nan = new javax.swing.JToggleButton();
        freq_5 = new javax.swing.JToggleButton();
        freq_4 = new javax.swing.JToggleButton();
        freq_3 = new javax.swing.JToggleButton();
        freq_2 = new javax.swing.JToggleButton();
        freq_1 = new javax.swing.JToggleButton();
        jLabel46 = new javax.swing.JLabel();
        popout_all = new javax.swing.JButton();
        jPanel70 = new javax.swing.JPanel();
        cc_ch = new javax.swing.JButton();
        traffic_ch = new javax.swing.JButton();
        freq_ch_lb1 = new javax.swing.JLabel();
        mode_p25_lsm = new javax.swing.JToggleButton();
        mode_p25_cqpsk = new javax.swing.JToggleButton();
        mode_tdmacc = new javax.swing.JToggleButton();
        mode_dmr = new javax.swing.JToggleButton();
        mode_nxdn48 = new javax.swing.JToggleButton();
        mode_nxdn96 = new javax.swing.JToggleButton();
        mode_fm = new javax.swing.JToggleButton();
        mode_am = new javax.swing.JToggleButton();
        mode_am_agc = new javax.swing.JToggleButton();
        freq_ch_lb2 = new javax.swing.JLabel();
        prev_freq = new javax.swing.JButton();
        next_freq = new javax.swing.JButton();
        pause_roaming = new javax.swing.JButton();
        add_rid = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        progress_label = new javax.swing.JLabel();
        progbar = new javax.swing.JProgressBar();
        jSeparator2 = new javax.swing.JSeparator();
        meter_panel = new javax.swing.JPanel();
        desc_panel = new javax.swing.JPanel();
        level_panel = new javax.swing.JPanel();
        sq_lb = new javax.swing.JLabel();
        sq_indicator = new javax.swing.JToggleButton();
        tg_lb = new javax.swing.JLabel();
        tg_indicator = new javax.swing.JToggleButton();
        bt_lb = new javax.swing.JLabel();
        bt_indicator = new javax.swing.JToggleButton();
        jPanel50 = new javax.swing.JPanel();
        audio_prog = new javax.swing.JProgressBar();
        tabbed_pane = new javax.swing.JTabbedPane();

        p25rxconfigpanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jPanel12 = new javax.swing.JPanel();
        en_bluetooth_cb = new javax.swing.JCheckBox();
        enable_leds = new javax.swing.JCheckBox();
        os_string = new javax.swing.JLabel();
        jPanel60 = new javax.swing.JPanel();
        status_format_cc = new javax.swing.JTextField();
        jPanel66 = new javax.swing.JPanel();
        status_format_voice = new javax.swing.JTextField();
        show_keywords = new javax.swing.JButton();
        jPanel67 = new javax.swing.JPanel();
        jLabel58 = new javax.swing.JLabel();
        audio_agc_max = new javax.swing.JTextField();
        jLabel59 = new javax.swing.JLabel();
        jPanel71 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        lineout_vol_slider = new javax.swing.JSlider();
        volume_label = new javax.swing.JLabel();
        auto_lsm = new javax.swing.JCheckBox();
        disconnect = new javax.swing.JButton();
        discover = new javax.swing.JButton();
        check_firmware = new javax.swing.JButton();
        write_config = new javax.swing.JButton();
        read_config = new javax.swing.JButton();
        fw_ver = new javax.swing.JLabel();
        fw_installed = new javax.swing.JLabel();
        audiopanel = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        enable_mp3 = new javax.swing.JCheckBox();
        enable_audio = new javax.swing.JCheckBox();
        mp3_separate_files = new javax.swing.JCheckBox();
        jScrollPane3 = new javax.swing.JScrollPane();
        audio_dev_list = new javax.swing.JList<>();
        jLabel3 = new javax.swing.JLabel();
        select_home = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        home_dir_label = new javax.swing.JLabel();
        do_mp3 = new javax.swing.JRadioButton();
        do_wav = new javax.swing.JRadioButton();
        audio_hiq = new javax.swing.JRadioButton();
        audio_lowq = new javax.swing.JRadioButton();
        jLabel28 = new javax.swing.JLabel();
        jPanel59 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        end_call_silence = new javax.swing.JTextField();
        jLabel32 = new javax.swing.JLabel();
        separate_rid = new javax.swing.JCheckBox();
        en_rdio = new javax.swing.JCheckBox();
        rdio_mask = new javax.swing.JTextField();
        jLabel51 = new javax.swing.JLabel();
        en_broadcastify_calls = new javax.swing.JCheckBox();
        jPanel13 = new javax.swing.JPanel();
        talkgroup_panel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel22 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        read_tg = new javax.swing.JButton();
        send_tg = new javax.swing.JButton();
        enable_table_rows = new javax.swing.JButton();
        disable_table_rows = new javax.swing.JButton();
        tg_edit_del = new javax.swing.JButton();
        set_zones = new javax.swing.JButton();
        tg_duplicate = new javax.swing.JButton();
        tg_sort = new javax.swing.JComboBox<>();
        jPanel23 = new javax.swing.JPanel();
        import_csv = new javax.swing.JButton();
        backup_tg = new javax.swing.JButton();
        auto_flash_tg = new javax.swing.JCheckBox();
        disable_encrypted = new javax.swing.JCheckBox();
        auto_pop_table = new javax.swing.JCheckBox();
        enable_tg_table_updates = new javax.swing.JCheckBox();
        update_selected_tg = new javax.swing.JButton();
        jPanel72 = new javax.swing.JPanel();
        jPanel73 = new javax.swing.JPanel();
        jLabel34 = new javax.swing.JLabel();
        vtimeout = new javax.swing.JComboBox<>();
        allow_unknown_tg_cb = new javax.swing.JCheckBox();
        allow_tg_pri_int = new javax.swing.JCheckBox();
        en_tg_int_tone = new javax.swing.JCheckBox();
        write_config_tg = new javax.swing.JButton();
        channelconfig = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        browser_view_mode = new javax.swing.JComboBox<>();
        jSeparator6 = new javax.swing.JSeparator();
        select_systems = new javax.swing.JButton();
        systems_path_str = new javax.swing.JLabel();
        jPanel15 = new javax.swing.JPanel();
        jPanel16 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        reset_to_defaults = new javax.swing.JButton();
        refresh_treeview = new javax.swing.JButton();
        jPanel18 = new javax.swing.JPanel();
        jPanel19 = new javax.swing.JPanel();
        jPanel17 = new javax.swing.JPanel();
        jSeparator3 = new javax.swing.JSeparator();
        cc_apply = new javax.swing.JButton();
        cc_sync = new javax.swing.JButton();
        cc_import = new javax.swing.JButton();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        chconfig_general = new javax.swing.JPanel();
        jPanel20 = new javax.swing.JPanel();
        jPanel31 = new javax.swing.JPanel();
        cc_do_scan = new javax.swing.JCheckBox();
        cc_use_on_powerup = new javax.swing.JCheckBox();
        cc_install_to_flash = new javax.swing.JCheckBox();
        jPanel92 = new javax.swing.JPanel();
        jLabel48 = new javax.swing.JLabel();
        cc_agency = new javax.swing.JTextField();
        jPanel30 = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        cc_name = new javax.swing.JTextField();
        jPanel93 = new javax.swing.JPanel();
        jLabel43 = new javax.swing.JLabel();
        ccfg_site_id = new javax.swing.JTextField();
        county_lb = new javax.swing.JLabel();
        jPanel25 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        cc_frequency = new javax.swing.JTextField();
        jPanel26 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        cc_modulation = new javax.swing.JComboBox<>();
        jPanel29 = new javax.swing.JPanel();
        cc_control = new javax.swing.JRadioButton();
        cc_conventional = new javax.swing.JRadioButton();
        jPanel100 = new javax.swing.JPanel();
        jPanel101 = new javax.swing.JPanel();
        jPanel32 = new javax.swing.JPanel();
        jPanel41 = new javax.swing.JPanel();
        ccfg_analog_en = new javax.swing.JCheckBox();
        jLabel36 = new javax.swing.JLabel();
        ccfg_squelch_level = new javax.swing.JTextField();
        jLabel37 = new javax.swing.JLabel();
        jPanel46 = new javax.swing.JPanel();
        pl_tone_filter = new javax.swing.JCheckBox();
        pl_tone_freq = new javax.swing.JTextField();
        jPanel38 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        ccfg_dmr_lcn = new javax.swing.JTextField();
        jPanel62 = new javax.swing.JPanel();
        jPanel69 = new javax.swing.JPanel();
        jPanel74 = new javax.swing.JPanel();
        jPanel77 = new javax.swing.JPanel();
        jPanel79 = new javax.swing.JPanel();
        jPanel80 = new javax.swing.JPanel();
        jPanel47 = new javax.swing.JPanel();
        jPanel33 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        cc_rfgain = new javax.swing.JComboBox<>();
        jSeparator9 = new javax.swing.JSeparator();
        find_fixed_gains = new javax.swing.JButton();
        jPanel34 = new javax.swing.JPanel();
        jLabel24 = new javax.swing.JLabel();
        cc_mgain = new javax.swing.JComboBox<>();
        jSeparator10 = new javax.swing.JSeparator();
        set_gains_auto = new javax.swing.JButton();
        jPanel94 = new javax.swing.JPanel();
        jLabel25 = new javax.swing.JLabel();
        cc_vga_gain = new javax.swing.JComboBox<>();
        jPanel95 = new javax.swing.JPanel();
        jPanel96 = new javax.swing.JPanel();
        jPanel97 = new javax.swing.JPanel();
        jPanel98 = new javax.swing.JPanel();
        jPanel99 = new javax.swing.JPanel();
        p25config = new javax.swing.JPanel();
        jPanel37 = new javax.swing.JPanel();
        auto_add_p25_traffic_ch = new javax.swing.JCheckBox();
        auto_add_p25_secondaries = new javax.swing.JCheckBox();
        auto_add_p25_neighbors = new javax.swing.JCheckBox();
        jPanel42 = new javax.swing.JPanel();
        jLabel40 = new javax.swing.JLabel();
        ccfg_p25_wacn = new javax.swing.JTextField();
        jLabel41 = new javax.swing.JLabel();
        ccfg_p25_sysid = new javax.swing.JTextField();
        jLabel42 = new javax.swing.JLabel();
        ccfg_p25_nac = new javax.swing.JTextField();
        jLabel45 = new javax.swing.JLabel();
        jPanel43 = new javax.swing.JPanel();
        ccfg_p25_p1 = new javax.swing.JRadioButton();
        ccfg_p25_p2 = new javax.swing.JRadioButton();
        jPanel36 = new javax.swing.JPanel();
        allow_unknown_tg = new javax.swing.JCheckBox();
        jPanel81 = new javax.swing.JPanel();
        jPanel82 = new javax.swing.JPanel();
        en_acars = new javax.swing.JCheckBox();
        en_flex32 = new javax.swing.JCheckBox();
        en_pocsag12 = new javax.swing.JCheckBox();
        console_dec_no_audio = new javax.swing.JCheckBox();
        jPanel83 = new javax.swing.JPanel();
        jPanel84 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        trunk_no_voice_timeout = new javax.swing.JTextField();
        jLabel47 = new javax.swing.JLabel();
        jPanel85 = new javax.swing.JPanel();
        jPanel86 = new javax.swing.JPanel();
        jPanel87 = new javax.swing.JPanel();
        jPanel88 = new javax.swing.JPanel();
        jPanel89 = new javax.swing.JPanel();
        jPanel90 = new javax.swing.JPanel();
        write_config_global = new javax.swing.JButton();
        jPanel91 = new javax.swing.JPanel();
        channel_search = new javax.swing.JPanel();
        profile_search1 = new javax.swing.JPanel();
        jLabel26 = new javax.swing.JLabel();
        ccfg_ch_band = new javax.swing.JComboBox<>();
        jLabel92 = new javax.swing.JLabel();
        ccfg_ch_sp = new javax.swing.JComboBox<>();
        profile_search2 = new javax.swing.JPanel();
        jSeparator5 = new javax.swing.JSeparator();
        jLabel16 = new javax.swing.JLabel();
        ccfg_iterations = new javax.swing.JTextField();
        jSeparator4 = new javax.swing.JSeparator();
        ccfg_add_digital_ch = new javax.swing.JCheckBox();
        ccfg_add_analog_ch1 = new javax.swing.JCheckBox();
        ccfg_start_discover = new javax.swing.JButton();
        ccfg_search_pause = new javax.swing.JButton();
        ccfg_search_abort = new javax.swing.JButton();
        ccfg_prog = new javax.swing.JProgressBar();
        ccfg_iteration_lb = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        ccfg_ta = new javax.swing.JTextArea();
        jPanel14 = new javax.swing.JPanel();
        ccfg_p1control = new javax.swing.JCheckBox();
        ccfg_p1voice = new javax.swing.JCheckBox();
        ccfg_dmr_control = new javax.swing.JCheckBox();
        ccfg_dmr_voice = new javax.swing.JCheckBox();
        ccfg_nxdn_voice = new javax.swing.JCheckBox();
        jPanel39 = new javax.swing.JPanel();
        jPanel40 = new javax.swing.JPanel();
        jLabel89 = new javax.swing.JLabel();
        ccfg_st_freq = new javax.swing.JTextField();
        jLabel90 = new javax.swing.JLabel();
        ccfg_end_freq = new javax.swing.JTextField();
        jLabel44 = new javax.swing.JLabel();
        ccfg_ch_step = new javax.swing.JTextField();
        jLabel38 = new javax.swing.JLabel();
        ccfg_step_dwell = new javax.swing.JComboBox<>();
        jLabel39 = new javax.swing.JLabel();
        ccfg_max_analyze_time = new javax.swing.JComboBox<>();
        advancedpanel = new javax.swing.JPanel();
        adv_write_config = new javax.swing.JButton();
        en_encout = new javax.swing.JCheckBox();
        en_p2_tones = new javax.swing.JCheckBox();
        p25_tone_vol = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        en_zero_rid = new javax.swing.JCheckBox();
        enc_mode = new javax.swing.JCheckBox();
        reset_defaults = new javax.swing.JButton();
        jPanel63 = new javax.swing.JPanel();
        jPanel65 = new javax.swing.JPanel();
        jLabel50 = new javax.swing.JLabel();
        p2_sync_thresh = new javax.swing.JTextField();
        jLabel66 = new javax.swing.JLabel();
        jPanel64 = new javax.swing.JPanel();
        jLabel57 = new javax.swing.JLabel();
        p1_sync_thresh = new javax.swing.JTextField();
        jLabel65 = new javax.swing.JLabel();
        jPanel56 = new javax.swing.JPanel();
        jLabel52 = new javax.swing.JLabel();
        enc_timeout = new javax.swing.JTextField();
        jLabel54 = new javax.swing.JLabel();
        enc_count = new javax.swing.JTextField();
        jLabel55 = new javax.swing.JLabel();
        enable_alias_dbase = new javax.swing.JCheckBox();
        jPanel7 = new javax.swing.JPanel();
        jPanel53 = new javax.swing.JPanel();
        jLabel31 = new javax.swing.JLabel();
        rfgain = new javax.swing.JComboBox<>();
        jSeparator42 = new javax.swing.JSeparator();
        jLabel35 = new javax.swing.JLabel();
        mixgain = new javax.swing.JComboBox<>();
        jSeparator43 = new javax.swing.JSeparator();
        jLabel29 = new javax.swing.JLabel();
        vga_gain = new javax.swing.JComboBox<>();
        jSeparator46 = new javax.swing.JSeparator();
        jLabel56 = new javax.swing.JLabel();
        rf_hyst = new javax.swing.JComboBox<>();
        jPanel75 = new javax.swing.JPanel();
        jLabel30 = new javax.swing.JLabel();
        vga_target = new javax.swing.JTextField();
        jLabel53 = new javax.swing.JLabel();
        jSeparator44 = new javax.swing.JSeparator();
        jLabel62 = new javax.swing.JLabel();
        vga_step = new javax.swing.JTextField();
        jLabel63 = new javax.swing.JLabel();
        jSeparator45 = new javax.swing.JSeparator();
        jSeparator47 = new javax.swing.JSeparator();
        jLabel8 = new javax.swing.JLabel();
        vga_gain_offset = new javax.swing.JComboBox<>();
        jPanel76 = new javax.swing.JPanel();
        jLabel60 = new javax.swing.JLabel();
        jSeparator48 = new javax.swing.JSeparator();
        jLabel49 = new javax.swing.JLabel();
        auto_gain_profile = new javax.swing.JComboBox<>();
        save_iq = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        popout_ontop = new javax.swing.JCheckBox();
        enable_event_dbase = new javax.swing.JCheckBox();
        consolePanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jPanel6 = new javax.swing.JPanel();
        jPanel51 = new javax.swing.JPanel();
        console_color = new javax.swing.JButton();
        console_font = new javax.swing.JButton();
        en_evt_output = new javax.swing.JCheckBox();
        jCheckBox1 = new javax.swing.JCheckBox();
        consolepop = new javax.swing.JButton();
        logpanel = new javax.swing.JPanel();
        tg_scroll_pane = new javax.swing.JScrollPane();
        log_ta = new javax.swing.JTextArea();
        tgfontpanel = new javax.swing.JPanel();
        tglog_font = new javax.swing.JButton();
        tglog_color = new javax.swing.JButton();
        tglog_edit = new javax.swing.JButton();
        buttong_config = new javax.swing.JPanel();
        jPanel21 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        button_write_config = new javax.swing.JButton();
        jPanel27 = new javax.swing.JPanel();
        single_click_opt1 = new javax.swing.JRadioButton();
        single_click_opt2 = new javax.swing.JRadioButton();
        single_click_opt3 = new javax.swing.JRadioButton();
        single_click_opt4 = new javax.swing.JRadioButton();
        single_click_opt5 = new javax.swing.JRadioButton();
        single_click_opt6 = new javax.swing.JRadioButton();
        jPanel28 = new javax.swing.JPanel();
        double_click_opt1 = new javax.swing.JRadioButton();
        double_click_opt2 = new javax.swing.JRadioButton();
        double_click_opt3 = new javax.swing.JRadioButton();
        double_click_opt4 = new javax.swing.JRadioButton();
        double_click_opt5 = new javax.swing.JRadioButton();
        double_click_opt6 = new javax.swing.JRadioButton();
        jPanel45 = new javax.swing.JPanel();
        triple_click_opt1 = new javax.swing.JRadioButton();
        triple_click_opt2 = new javax.swing.JRadioButton();
        triple_click_opt3 = new javax.swing.JRadioButton();
        triple_click_opt4 = new javax.swing.JRadioButton();
        triple_click_opt5 = new javax.swing.JRadioButton();
        triple_click_opt6 = new javax.swing.JRadioButton();
        jPanel48 = new javax.swing.JPanel();
        jLabel21 = new javax.swing.JLabel();
        skip_tg_to = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        jSeparator40 = new javax.swing.JSeparator();
        roaming_ret_to_cc = new javax.swing.JCheckBox();
        jLabel33 = new javax.swing.JLabel();
        jPanel49 = new javax.swing.JPanel();
        quad_click_opt1 = new javax.swing.JRadioButton();
        quad_click_opt2 = new javax.swing.JRadioButton();
        quad_click_opt3 = new javax.swing.JRadioButton();
        quad_click_opt4 = new javax.swing.JRadioButton();
        quad_click_opt5 = new javax.swing.JRadioButton();
        quad_click_opt6 = new javax.swing.JRadioButton();
        alias_panel = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        alias_table = new javax.swing.JTable();
        jPanel5 = new javax.swing.JPanel();
        sort_alias = new javax.swing.JRadioButton();
        sort_rid = new javax.swing.JRadioButton();
        import_alias = new javax.swing.JButton();
        export_alias = new javax.swing.JButton();
        update_alias_table = new javax.swing.JButton();
        del_alias = new javax.swing.JButton();
        jLabel61 = new javax.swing.JLabel();
        signalinsightpanel = new javax.swing.JPanel();
        const_panel = new javax.swing.JPanel();
        jPanel78 = new javax.swing.JPanel();
        jPanel55 = new javax.swing.JPanel();
        jPanel24 = new javax.swing.JPanel();
        autoscale_const = new javax.swing.JCheckBox();
        nsymbols = new javax.swing.JComboBox<>();
        jSeparator41 = new javax.swing.JSeparator();
        jPanel68 = new javax.swing.JPanel();
        si_cpu_high = new javax.swing.JRadioButton();
        si_cpu_normal = new javax.swing.JRadioButton();
        si_cpu_low = new javax.swing.JRadioButton();
        si_cpu_battery_saving = new javax.swing.JRadioButton();
        si_cpu_off = new javax.swing.JRadioButton();
        write_cfg_si = new javax.swing.JButton();
        freemem = new javax.swing.JButton();
        jPanel58 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        if_slide = new javax.swing.JSlider();
        jLabel27 = new javax.swing.JLabel();
        sig_cmd = new javax.swing.JTextField();
        sipopout = new javax.swing.JButton();
        displayviewmain_border = new javax.swing.JPanel();
        display_frame = new javax.swing.JPanel();
        jPanel61 = new javax.swing.JPanel();
        hold = new javax.swing.JButton();
        skip = new javax.swing.JButton();
        edit_alias = new javax.swing.JButton();
        edit_display_view = new javax.swing.JButton();
        dvpopout = new javax.swing.JButton();
        northpanel = new javax.swing.JPanel();
        logo_panel = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        jButton1 = new javax.swing.JButton();
        top_label = new javax.swing.JLabel();
        wacn = new javax.swing.JLabel();
        sysid = new javax.swing.JLabel();
        nac = new javax.swing.JLabel();
        freq = new javax.swing.JLabel();
        siteid = new javax.swing.JLabel();
        rfid = new javax.swing.JLabel();
        macid = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        minimize = new javax.swing.JToggleButton();
        record_to_mp3 = new javax.swing.JToggleButton();
        release_date = new javax.swing.JLabel();
        ser_dev = new javax.swing.JLabel();
        eastpanel = new javax.swing.JPanel();
        slider_squelch = new javax.swing.JSlider();
        slider_val = new javax.swing.JLabel();
        squelch_set = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
            public void componentMoved(java.awt.event.ComponentEvent evt) {
                formComponentMoved(evt);
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                formKeyTyped(evt);
            }
        });

        southpanel.setLayout(new javax.swing.BoxLayout(southpanel, javax.swing.BoxLayout.Y_AXIS));

        jPanel9.setLayout(new java.awt.BorderLayout());

        status_panel.setBackground(new java.awt.Color(0, 0, 0));
        status_panel.setMinimumSize(new java.awt.Dimension(99, 33));
        status_panel.setPreferredSize(new java.awt.Dimension(1004, 33));
        status_panel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 7));
        jPanel9.add(status_panel, java.awt.BorderLayout.CENTER);

        tiny_const.setBackground(new java.awt.Color(0, 0, 0));
        tiny_const.setPreferredSize(new java.awt.Dimension(33, 33));
        tiny_const.setRequestFocusEnabled(false);
        jPanel9.add(tiny_const, java.awt.BorderLayout.EAST);

        jPanel57.setBackground(new java.awt.Color(0, 0, 0));

        jLabel23.setForeground(new java.awt.Color(255, 255, 255));
        jLabel23.setText("Zones");
        jPanel57.add(jLabel23);

        z1.setBackground(new java.awt.Color(204, 204, 204));
        z1.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        z1.setText("1");
        z1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                z1ActionPerformed(evt);
            }
        });
        jPanel57.add(z1);

        z2.setBackground(new java.awt.Color(204, 204, 204));
        z2.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        z2.setText("2");
        z2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                z2ActionPerformed(evt);
            }
        });
        jPanel57.add(z2);

        z3.setBackground(new java.awt.Color(204, 204, 204));
        z3.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        z3.setText("3");
        z3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                z3ActionPerformed(evt);
            }
        });
        jPanel57.add(z3);

        z4.setBackground(new java.awt.Color(204, 204, 204));
        z4.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        z4.setText("4");
        z4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                z4ActionPerformed(evt);
            }
        });
        jPanel57.add(z4);

        z5.setBackground(new java.awt.Color(204, 204, 204));
        z5.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        z5.setText("5");
        z5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                z5ActionPerformed(evt);
            }
        });
        jPanel57.add(z5);

        z6.setBackground(new java.awt.Color(204, 204, 204));
        z6.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        z6.setText("6");
        z6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                z6ActionPerformed(evt);
            }
        });
        jPanel57.add(z6);

        z7.setBackground(new java.awt.Color(204, 204, 204));
        z7.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        z7.setText("7");
        z7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                z7ActionPerformed(evt);
            }
        });
        jPanel57.add(z7);

        z8.setBackground(new java.awt.Color(204, 204, 204));
        z8.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        z8.setText("8");
        z8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                z8ActionPerformed(evt);
            }
        });
        jPanel57.add(z8);

        z9.setBackground(new java.awt.Color(204, 204, 204));
        z9.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        z9.setText("9");
        z9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                z9ActionPerformed(evt);
            }
        });
        jPanel57.add(z9);

        z10.setBackground(new java.awt.Color(204, 204, 204));
        z10.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        z10.setText("10");
        z10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                z10ActionPerformed(evt);
            }
        });
        jPanel57.add(z10);

        z11.setBackground(new java.awt.Color(204, 204, 204));
        z11.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        z11.setText("11");
        z11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                z11ActionPerformed(evt);
            }
        });
        jPanel57.add(z11);

        z12.setBackground(new java.awt.Color(204, 204, 204));
        z12.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        z12.setText("12");
        z12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                z12ActionPerformed(evt);
            }
        });
        jPanel57.add(z12);

        z13.setBackground(new java.awt.Color(204, 204, 204));
        z13.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        z13.setText("13");
        z13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                z13ActionPerformed(evt);
            }
        });
        jPanel57.add(z13);

        z14.setBackground(new java.awt.Color(204, 204, 204));
        z14.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        z14.setText("14");
        z14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                z14ActionPerformed(evt);
            }
        });
        jPanel57.add(z14);

        z15.setBackground(new java.awt.Color(204, 204, 204));
        z15.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        z15.setText("15");
        z15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                z15ActionPerformed(evt);
            }
        });
        jPanel57.add(z15);

        z16.setBackground(new java.awt.Color(204, 204, 204));
        z16.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        z16.setText("16");
        z16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                z16ActionPerformed(evt);
            }
        });
        jPanel57.add(z16);

        hold1.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        hold1.setText("H");
        hold1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hold1ActionPerformed(evt);
            }
        });
        jPanel57.add(hold1);

        skip1.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        skip1.setText("S");
        skip1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                skip1ActionPerformed(evt);
            }
        });
        jPanel57.add(skip1);

        mute.setBackground(new java.awt.Color(204, 204, 204));
        mute.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        mute.setText("MUTE");
        mute.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                muteActionPerformed(evt);
            }
        });
        jPanel57.add(mute);

        edit_alias1.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        edit_alias1.setText("Edit Alias");
        edit_alias1.setEnabled(false);
        edit_alias1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                edit_alias1ActionPerformed(evt);
            }
        });
        jPanel57.add(edit_alias1);

        jPanel9.add(jPanel57, java.awt.BorderLayout.EAST);

        jPanel10.setBackground(new java.awt.Color(0, 0, 0));
        jPanel10.setForeground(new java.awt.Color(255, 255, 255));
        jPanel10.setLayout(new java.awt.BorderLayout());

        jPanel52.setBackground(new java.awt.Color(0, 0, 0));
        jPanel52.setForeground(new java.awt.Color(255, 255, 255));
        jPanel52.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        status.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        status.setForeground(new java.awt.Color(255, 255, 255));
        status.setText("Status: Idle");
        status.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jPanel52.add(status);

        jPanel10.add(jPanel52, java.awt.BorderLayout.WEST);

        jPanel44.setBackground(new java.awt.Color(0, 0, 0));
        jPanel44.setForeground(new java.awt.Color(255, 255, 255));
        jPanel44.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        ch_step_lb.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        ch_step_lb.setForeground(new java.awt.Color(255, 255, 255));
        ch_step_lb.setText("CH STEP  12.5 kHz ");
        ch_step_lb.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                ch_step_lbMouseWheelMoved(evt);
            }
        });
        jPanel44.add(ch_step_lb);

        stepdown.setBackground(new java.awt.Color(0, 0, 0));
        stepdown.setFont(new java.awt.Font("Dialog", 1, 24)); // NOI18N
        stepdown.setForeground(new java.awt.Color(255, 255, 255));
        stepdown.setText("<");
        stepdown.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        stepdown.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                stepdownfreq_5MouseWheelMoved(evt);
            }
        });
        stepdown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stepdownActionPerformed(evt);
            }
        });
        jPanel44.add(stepdown);

        jSeparator8.setPreferredSize(new java.awt.Dimension(10, 0));
        jPanel44.add(jSeparator8);

        stepup.setBackground(new java.awt.Color(0, 0, 0));
        stepup.setFont(new java.awt.Font("Dialog", 1, 24)); // NOI18N
        stepup.setForeground(new java.awt.Color(255, 255, 255));
        stepup.setText(">");
        stepup.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        stepup.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                stepupfreq_5MouseWheelMoved(evt);
            }
        });
        stepup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stepupActionPerformed(evt);
            }
        });
        jPanel44.add(stepup);

        jSeparator7.setMinimumSize(new java.awt.Dimension(50, 0));
        jSeparator7.setPreferredSize(new java.awt.Dimension(50, 0));
        jPanel44.add(jSeparator7);

        freq_9.setBackground(new java.awt.Color(0, 0, 0));
        freq_9.setFont(new java.awt.Font("Dialog", 1, 24)); // NOI18N
        freq_9.setForeground(new java.awt.Color(255, 255, 255));
        freq_9.setText("0");
        freq_9.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        freq_9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                freq_9ActionPerformed(evt);
            }
        });
        jPanel44.add(freq_9);

        freq_8.setBackground(new java.awt.Color(0, 0, 0));
        freq_8.setFont(new java.awt.Font("Dialog", 1, 24)); // NOI18N
        freq_8.setForeground(new java.awt.Color(255, 255, 255));
        freq_8.setText("0");
        freq_8.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        freq_8.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                freq_8MouseWheelMoved(evt);
            }
        });
        freq_8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                freq_8ActionPerformed(evt);
            }
        });
        jPanel44.add(freq_8);

        freq_7.setBackground(new java.awt.Color(0, 0, 0));
        freq_7.setFont(new java.awt.Font("Dialog", 1, 24)); // NOI18N
        freq_7.setForeground(new java.awt.Color(255, 255, 255));
        freq_7.setText("0");
        freq_7.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        freq_7.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                freq_7MouseWheelMoved(evt);
            }
        });
        freq_7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                freq_7ActionPerformed(evt);
            }
        });
        jPanel44.add(freq_7);

        freq_6.setBackground(new java.awt.Color(0, 0, 0));
        freq_6.setFont(new java.awt.Font("Dialog", 1, 24)); // NOI18N
        freq_6.setForeground(new java.awt.Color(255, 255, 255));
        freq_6.setText("0");
        freq_6.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        freq_6.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                freq_6MouseWheelMoved(evt);
            }
        });
        freq_6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                freq_6ActionPerformed(evt);
            }
        });
        jPanel44.add(freq_6);

        freq_nan.setBackground(new java.awt.Color(0, 0, 0));
        freq_nan.setFont(new java.awt.Font("Dialog", 1, 24)); // NOI18N
        freq_nan.setForeground(new java.awt.Color(255, 255, 255));
        freq_nan.setText(".");
        freq_nan.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel44.add(freq_nan);

        freq_5.setBackground(new java.awt.Color(0, 0, 0));
        freq_5.setFont(new java.awt.Font("Dialog", 1, 24)); // NOI18N
        freq_5.setForeground(new java.awt.Color(255, 255, 255));
        freq_5.setText("0");
        freq_5.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        freq_5.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                freq_5MouseWheelMoved(evt);
            }
        });
        freq_5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                freq_5ActionPerformed(evt);
            }
        });
        jPanel44.add(freq_5);

        freq_4.setBackground(new java.awt.Color(0, 0, 0));
        freq_4.setFont(new java.awt.Font("Dialog", 1, 24)); // NOI18N
        freq_4.setForeground(new java.awt.Color(255, 255, 255));
        freq_4.setText("0");
        freq_4.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        freq_4.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                freq_4MouseWheelMoved(evt);
            }
        });
        freq_4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                freq_4ActionPerformed(evt);
            }
        });
        jPanel44.add(freq_4);

        freq_3.setBackground(new java.awt.Color(0, 0, 0));
        freq_3.setFont(new java.awt.Font("Dialog", 1, 24)); // NOI18N
        freq_3.setForeground(new java.awt.Color(255, 255, 255));
        freq_3.setText("0");
        freq_3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        freq_3.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                freq_3MouseWheelMoved(evt);
            }
        });
        freq_3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                freq_3ActionPerformed(evt);
            }
        });
        jPanel44.add(freq_3);

        freq_2.setBackground(new java.awt.Color(0, 0, 0));
        freq_2.setFont(new java.awt.Font("Dialog", 1, 24)); // NOI18N
        freq_2.setForeground(new java.awt.Color(255, 255, 255));
        freq_2.setText("0");
        freq_2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        freq_2.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                freq_2MouseWheelMoved(evt);
            }
        });
        freq_2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                freq_2ActionPerformed(evt);
            }
        });
        jPanel44.add(freq_2);

        freq_1.setBackground(new java.awt.Color(0, 0, 0));
        freq_1.setFont(new java.awt.Font("Dialog", 1, 24)); // NOI18N
        freq_1.setForeground(new java.awt.Color(255, 255, 255));
        freq_1.setText("0");
        freq_1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        freq_1.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                freq_1MouseWheelMoved(evt);
            }
        });
        freq_1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                freq_1ActionPerformed(evt);
            }
        });
        jPanel44.add(freq_1);

        jLabel46.setBackground(new java.awt.Color(0, 0, 0));
        jLabel46.setFont(new java.awt.Font("Dialog", 1, 24)); // NOI18N
        jLabel46.setForeground(new java.awt.Color(255, 255, 255));
        jLabel46.setText("MHz");
        jPanel44.add(jLabel46);

        popout_all.setIcon(new javax.swing.ImageIcon(getClass().getResource("/btconfig/images/rightarrow.png"))); // NOI18N
        popout_all.setText(" ");
        popout_all.setToolTipText("Popout All Windows");
        popout_all.setBorderPainted(false);
        popout_all.setContentAreaFilled(false);
        popout_all.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popout_allActionPerformed(evt);
            }
        });
        jPanel44.add(popout_all);

        jPanel10.add(jPanel44, java.awt.BorderLayout.EAST);

        jPanel9.add(jPanel10, java.awt.BorderLayout.PAGE_START);

        southpanel.add(jPanel9);

        jPanel70.setBackground(new java.awt.Color(0, 0, 0));
        jPanel70.setForeground(new java.awt.Color(255, 255, 255));
        jPanel70.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        cc_ch.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        cc_ch.setText("Control Ch");
        cc_ch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cc_chActionPerformed(evt);
            }
        });
        jPanel70.add(cc_ch);

        traffic_ch.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        traffic_ch.setText("Traffic Ch");
        traffic_ch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                traffic_chActionPerformed(evt);
            }
        });
        jPanel70.add(traffic_ch);

        freq_ch_lb1.setForeground(new java.awt.Color(255, 255, 255));
        freq_ch_lb1.setText("DEMOD");
        jPanel70.add(freq_ch_lb1);

        mode_p25_lsm.setBackground(new java.awt.Color(204, 204, 204));
        mode_p25_lsm.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        mode_p25_lsm.setText("P25-LSM");
        mode_p25_lsm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mode_p25_lsmActionPerformed(evt);
            }
        });
        jPanel70.add(mode_p25_lsm);

        mode_p25_cqpsk.setBackground(new java.awt.Color(204, 204, 204));
        mode_p25_cqpsk.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        mode_p25_cqpsk.setText("P25-CQPSK");
        mode_p25_cqpsk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mode_p25_cqpskActionPerformed(evt);
            }
        });
        jPanel70.add(mode_p25_cqpsk);

        mode_tdmacc.setBackground(new java.awt.Color(204, 204, 204));
        mode_tdmacc.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        mode_tdmacc.setText("TDMA CC");
        mode_tdmacc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mode_tdmaccActionPerformed(evt);
            }
        });
        jPanel70.add(mode_tdmacc);

        mode_dmr.setBackground(new java.awt.Color(204, 204, 204));
        mode_dmr.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        mode_dmr.setText("DMR");
        mode_dmr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mode_dmrActionPerformed(evt);
            }
        });
        jPanel70.add(mode_dmr);

        mode_nxdn48.setBackground(new java.awt.Color(204, 204, 204));
        mode_nxdn48.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        mode_nxdn48.setText("NXDN48");
        mode_nxdn48.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mode_nxdn48ActionPerformed(evt);
            }
        });
        jPanel70.add(mode_nxdn48);

        mode_nxdn96.setBackground(new java.awt.Color(204, 204, 204));
        mode_nxdn96.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        mode_nxdn96.setText("NXDN96");
        mode_nxdn96.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mode_nxdn96ActionPerformed(evt);
            }
        });
        jPanel70.add(mode_nxdn96);

        mode_fm.setBackground(new java.awt.Color(204, 204, 204));
        mode_fm.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        mode_fm.setText("NBFM");
        mode_fm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mode_fmActionPerformed(evt);
            }
        });
        jPanel70.add(mode_fm);

        mode_am.setBackground(new java.awt.Color(204, 204, 204));
        mode_am.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        mode_am.setText("AM");
        mode_am.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mode_amActionPerformed(evt);
            }
        });
        jPanel70.add(mode_am);

        mode_am_agc.setBackground(new java.awt.Color(204, 204, 204));
        mode_am_agc.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        mode_am_agc.setText("AM+AAGC");
        mode_am_agc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mode_am_agcActionPerformed(evt);
            }
        });
        jPanel70.add(mode_am_agc);

        freq_ch_lb2.setForeground(new java.awt.Color(255, 255, 255));
        freq_ch_lb2.setText("FREQ");
        jPanel70.add(freq_ch_lb2);

        prev_freq.setText("<");
        prev_freq.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prev_freqActionPerformed(evt);
            }
        });
        jPanel70.add(prev_freq);

        next_freq.setText(">");
        next_freq.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                next_freqActionPerformed(evt);
            }
        });
        jPanel70.add(next_freq);

        pause_roaming.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        pause_roaming.setText("Scan");
        pause_roaming.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pause_roamingActionPerformed(evt);
            }
        });
        jPanel70.add(pause_roaming);

        add_rid.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        add_rid.setText("Add RID");
        add_rid.setEnabled(false);
        add_rid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                add_ridActionPerformed(evt);
            }
        });
        jPanel70.add(add_rid);

        southpanel.add(jPanel70);

        jPanel2.setBackground(new java.awt.Color(0, 0, 0));
        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        progress_label.setForeground(new java.awt.Color(255, 255, 255));
        progress_label.setText("Working...");
        jPanel2.add(progress_label);

        progbar.setBackground(new java.awt.Color(204, 204, 204));
        progbar.setForeground(new java.awt.Color(102, 102, 102));
        progbar.setToolTipText("");
        progbar.setDoubleBuffered(true);
        progbar.setPreferredSize(new java.awt.Dimension(320, 14));
        progbar.setStringPainted(true);
        jPanel2.add(progbar);

        jSeparator2.setForeground(new java.awt.Color(255, 255, 255));
        jSeparator2.setEnabled(false);
        jSeparator2.setPreferredSize(new java.awt.Dimension(250, 0));
        jPanel2.add(jSeparator2);

        southpanel.add(jPanel2);

        meter_panel.setBackground(new java.awt.Color(0, 0, 0));
        meter_panel.setForeground(new java.awt.Color(255, 255, 255));
        meter_panel.setLayout(new javax.swing.BoxLayout(meter_panel, javax.swing.BoxLayout.LINE_AXIS));

        desc_panel.setBackground(new java.awt.Color(0, 0, 0));
        desc_panel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        meter_panel.add(desc_panel);

        level_panel.setBackground(new java.awt.Color(0, 0, 0));
        level_panel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        sq_lb.setForeground(new java.awt.Color(255, 255, 255));
        sq_lb.setText("SIG");
        level_panel.add(sq_lb);

        sq_indicator.setFont(new java.awt.Font("Dialog", 1, 8)); // NOI18N
        sq_indicator.setText("SQ");
        sq_indicator.setToolTipText("");
        sq_indicator.setBorderPainted(false);
        sq_indicator.setFocusPainted(false);
        level_panel.add(sq_indicator);

        tg_lb.setForeground(new java.awt.Color(255, 255, 255));
        tg_lb.setText("TG");
        level_panel.add(tg_lb);

        tg_indicator.setFont(new java.awt.Font("Dialog", 1, 8)); // NOI18N
        tg_indicator.setText("TG");
        tg_indicator.setToolTipText("");
        tg_indicator.setBorderPainted(false);
        tg_indicator.setFocusPainted(false);
        level_panel.add(tg_indicator);

        bt_lb.setForeground(new java.awt.Color(255, 255, 255));
        bt_lb.setText("BT");
        level_panel.add(bt_lb);

        bt_indicator.setFont(new java.awt.Font("Dialog", 1, 8)); // NOI18N
        bt_indicator.setText("BT");
        bt_indicator.setToolTipText("");
        bt_indicator.setBorderPainted(false);
        bt_indicator.setFocusPainted(false);
        level_panel.add(bt_indicator);

        meter_panel.add(level_panel);

        southpanel.add(meter_panel);

        jPanel50.setBackground(new java.awt.Color(0, 0, 0));
        jPanel50.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        audio_prog.setBackground(new java.awt.Color(0, 0, 0));
        audio_prog.setForeground(new java.awt.Color(0, 255, 0));
        audio_prog.setPreferredSize(new java.awt.Dimension(1100, 10));
        jPanel50.add(audio_prog);

        southpanel.add(jPanel50);

        getContentPane().add(southpanel, java.awt.BorderLayout.SOUTH);

        tabbed_pane.setPreferredSize(new java.awt.Dimension(1115, 659));
        tabbed_pane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabbed_paneStateChanged(evt);
            }
        });
        tabbed_pane.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tabbed_paneMouseClicked(evt);
            }
        });

        p25rxconfigpanel.setMinimumSize(new java.awt.Dimension(1110, 554));
        p25rxconfigpanel.setPreferredSize(new java.awt.Dimension(1110, 554));
        p25rxconfigpanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(204, 204, 204));
        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.Y_AXIS));
        jPanel1.add(jPanel12);

        p25rxconfigpanel.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 444, 1110, 70));

        en_bluetooth_cb.setSelected(true);
        en_bluetooth_cb.setText("Enable Bluetooth On Power-Up");
        en_bluetooth_cb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                en_bluetooth_cbActionPerformed(evt);
            }
        });
        p25rxconfigpanel.add(en_bluetooth_cb, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 220, -1, -1));

        enable_leds.setSelected(true);
        enable_leds.setText("Enable Status LEDS");
        p25rxconfigpanel.add(enable_leds, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 360, -1, -1));

        os_string.setText("OS: ");
        p25rxconfigpanel.add(os_string, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, -1));

        jPanel60.setBorder(javax.swing.BorderFactory.createTitledBorder("Status Format On CC"));

        status_format_cc.setColumns(50);
        status_format_cc.setText("CC $P25_MODE$ B/SEC $BLKS_SEC$  $WACN$-$SYS_ID$-$NAC$, $FREQ$ MHz");
        status_format_cc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                status_format_ccActionPerformed(evt);
            }
        });
        jPanel60.add(status_format_cc);

        p25rxconfigpanel.add(jPanel60, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 120, 610, 80));

        jPanel66.setBorder(javax.swing.BorderFactory.createTitledBorder("Status Format Voice"));

        status_format_voice.setColumns(50);
        status_format_voice.setText("$P25_MODE$ $V_FREQ$ MHz, TG $TG_ID$, $TG_NAME$");
        jPanel66.add(status_format_voice);

        p25rxconfigpanel.add(jPanel66, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 220, 610, 80));

        show_keywords.setText("Show KeyWords");
        show_keywords.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                show_keywordsActionPerformed(evt);
            }
        });
        p25rxconfigpanel.add(show_keywords, new org.netbeans.lib.awtextra.AbsoluteConstraints(950, 310, -1, -1));

        jPanel67.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel58.setText("Audio AGC Max Gain");
        jPanel67.add(jLabel58);

        audio_agc_max.setColumns(5);
        audio_agc_max.setText("0.7");
        jPanel67.add(audio_agc_max);

        jLabel59.setText("(default 0.7)");
        jPanel67.add(jLabel59);

        p25rxconfigpanel.add(jPanel67, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 290, 380, 40));

        jPanel71.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel5.setText("Line Out Volume");
        jPanel71.add(jLabel5);

        lineout_vol_slider.setPaintLabels(true);
        lineout_vol_slider.setPaintTicks(true);
        lineout_vol_slider.setToolTipText("This option control the audio line-out level for driving powered speakers or line-in on an external device.");
        lineout_vol_slider.setValue(100);
        lineout_vol_slider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                lineout_vol_sliderStateChanged(evt);
            }
        });
        jPanel71.add(lineout_vol_slider);

        volume_label.setText("1.0 (Def 0.5)");
        jPanel71.add(volume_label);

        p25rxconfigpanel.add(jPanel71, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 130, 460, 40));

        auto_lsm.setText("Auto Detect P25 Modulation (LSM / CQPSK)");
        auto_lsm.setEnabled(false);
        p25rxconfigpanel.add(auto_lsm, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 80, 410, -1));

        disconnect.setText("Disconnect");
        disconnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                disconnectActionPerformed(evt);
            }
        });
        p25rxconfigpanel.add(disconnect, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 530, -1, -1));

        discover.setText("Connect To P25RX");
        discover.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                discoverActionPerformed(evt);
            }
        });
        p25rxconfigpanel.add(discover, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 530, -1, -1));

        check_firmware.setText("Install Latest Firmware");
        check_firmware.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                check_firmwareActionPerformed(evt);
            }
        });
        p25rxconfigpanel.add(check_firmware, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 530, -1, -1));

        write_config.setText("Write Config To P25RX");
        write_config.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                write_configActionPerformed(evt);
            }
        });
        p25rxconfigpanel.add(write_config, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 360, -1, -1));

        read_config.setText("Read Config From P25RX");
        read_config.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                read_configActionPerformed(evt);
            }
        });
        p25rxconfigpanel.add(read_config, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 530, -1, -1));

        fw_ver.setText("FW Date: 2020-03-04 16:00 ");
        p25rxconfigpanel.add(fw_ver, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 370, -1, -1));

        fw_installed.setText("FW currently installed:");
        p25rxconfigpanel.add(fw_installed, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 370, -1, -1));

        tabbed_pane.addTab("P25RX Config", p25rxconfigpanel);

        audiopanel.setLayout(new java.awt.BorderLayout());

        jPanel11.setEnabled(false);
        jPanel11.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        enable_mp3.setSelected(true);
        enable_mp3.setText("Enable audio file generation");
        enable_mp3.setToolTipText("This option will generate mp3 files in the p25rx directory located in the user home directory.  ~/p25rx on Linux and Documents/p25rx on Windows.");
        enable_mp3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enable_mp3ActionPerformed(evt);
            }
        });
        jPanel11.add(enable_mp3, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 100, -1, -1));

        enable_audio.setSelected(true);
        enable_audio.setText("Enable PC Audio Output (PC Speakers)");
        enable_audio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enable_audioActionPerformed(evt);
            }
        });
        jPanel11.add(enable_audio, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 320, -1, -1));

        mp3_separate_files.setText("Separate files by talk group");
        mp3_separate_files.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mp3_separate_filesActionPerformed(evt);
            }
        });
        jPanel11.add(mp3_separate_files, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 190, -1, -1));

        jScrollPane3.setAutoscrolls(true);

        audio_dev_list.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Default" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        audio_dev_list.setSelectedIndex(0);
        audio_dev_list.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                audio_dev_listValueChanged(evt);
            }
        });
        jScrollPane3.setViewportView(audio_dev_list);

        jPanel11.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 40, 510, 250));

        jLabel3.setText("PC Output Audio Device Selection");
        jPanel11.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 20, -1, -1));

        select_home.setText("Select");
        select_home.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                select_homeActionPerformed(evt);
            }
        });
        jPanel11.add(select_home, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 10, -1, -1));

        jLabel10.setText("Audio Output Dir:");
        jPanel11.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 40, -1, -1));

        home_dir_label.setText("/home/p25rx");
        jPanel11.add(home_dir_label, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 60, -1, -1));

        buttonGroup16.add(do_mp3);
        do_mp3.setSelected(true);
        do_mp3.setText("MP3");
        do_mp3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                do_mp3ActionPerformed(evt);
            }
        });
        jPanel11.add(do_mp3, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 130, -1, -1));

        buttonGroup16.add(do_wav);
        do_wav.setText("WAV");
        do_wav.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                do_wavActionPerformed(evt);
            }
        });
        jPanel11.add(do_wav, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 130, -1, -1));

        buttonGroup18.add(audio_hiq);
        audio_hiq.setSelected(true);
        audio_hiq.setText("High");
        audio_hiq.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                audio_hiqActionPerformed(evt);
            }
        });
        jPanel11.add(audio_hiq, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 160, -1, -1));

        buttonGroup18.add(audio_lowq);
        audio_lowq.setText("VBR (variable bit rate)");
        audio_lowq.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                audio_lowqActionPerformed(evt);
            }
        });
        jPanel11.add(audio_lowq, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 160, -1, -1));

        jLabel28.setText("MP3 Quality");
        jPanel11.add(jLabel28, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 160, -1, 20));

        jLabel4.setText("End-Of-Call Silence");
        jPanel59.add(jLabel4);

        end_call_silence.setColumns(5);
        end_call_silence.setText("0");
        jPanel59.add(end_call_silence);

        jLabel32.setText("ms");
        jPanel59.add(jLabel32);

        jPanel11.add(jPanel59, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 270, 230, 40));

        separate_rid.setText("Separate files by RID");
        separate_rid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                separate_ridActionPerformed(evt);
            }
        });
        jPanel11.add(separate_rid, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 230, -1, -1));

        en_rdio.setText("Rdio-Scanner support (DirWatch)");
        en_rdio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                en_rdioActionPerformed(evt);
            }
        });
        jPanel11.add(en_rdio, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 370, -1, -1));

        rdio_mask.setText("TG_#TG_#DATE_#TIME_#SYS_#HZ_#UNIT");
        rdio_mask.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdio_maskActionPerformed(evt);
            }
        });
        jPanel11.add(rdio_mask, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 380, 460, -1));

        jLabel51.setText("Cut/Paste This Mask To DirWatch Config");
        jPanel11.add(jLabel51, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 400, -1, -1));

        en_broadcastify_calls.setText("Enable Broadcastify Calls Output");
        en_broadcastify_calls.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                en_broadcastify_callsActionPerformed(evt);
            }
        });
        jPanel11.add(en_broadcastify_calls, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 320, -1, -1));

        audiopanel.add(jPanel11, java.awt.BorderLayout.CENTER);

        jPanel13.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        audiopanel.add(jPanel13, java.awt.BorderLayout.PAGE_START);

        tabbed_pane.addTab("PC Audio", audiopanel);

        talkgroup_panel.setLayout(new java.awt.BorderLayout());

        jTable1.setDoubleBuffered(true);
        jTable1.setEditingColumn(1);
        jTable1.setEditingRow(1);
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object[6554][9],
            new String [] {
                "Enabled", "Row", "SYS_ID(HEX)", "Priority", "TGRP", "AlphaTag", "Description", "WACN(HEX)", "Zone"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Boolean.class,java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jTable1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTable1KeyTyped(evt);
            }
        });
        jScrollPane2.setViewportView(jTable1);

        talkgroup_panel.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        jPanel22.setLayout(new javax.swing.BoxLayout(jPanel22, javax.swing.BoxLayout.Y_AXIS));

        read_tg.setText("Import TG From DEV");
        read_tg.setToolTipText("reads talk groups from P25RX Device");
        read_tg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                read_tgActionPerformed(evt);
            }
        });
        jPanel3.add(read_tg);

        send_tg.setText("Write TG To DEV");
        send_tg.setToolTipText("writes talkgroups to P25RX device");
        send_tg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                send_tgActionPerformed(evt);
            }
        });
        jPanel3.add(send_tg);

        enable_table_rows.setText("Enable Selected");
        enable_table_rows.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enable_table_rowsActionPerformed(evt);
            }
        });
        jPanel3.add(enable_table_rows);

        disable_table_rows.setText("Disable Selected");
        disable_table_rows.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                disable_table_rowsActionPerformed(evt);
            }
        });
        jPanel3.add(disable_table_rows);

        tg_edit_del.setText("DEL Selected");
        tg_edit_del.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tg_edit_delActionPerformed(evt);
            }
        });
        jPanel3.add(tg_edit_del);

        set_zones.setText("Set Selected Zones");
        set_zones.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                set_zonesActionPerformed(evt);
            }
        });
        jPanel3.add(set_zones);

        tg_duplicate.setText("Duplicate");
        tg_duplicate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tg_duplicateActionPerformed(evt);
            }
        });
        jPanel3.add(tg_duplicate);

        tg_sort.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "ROWID", "TGID", "SYSID", "WACN", "PRIORITY", "AlphaTag", "Description", "Zone" }));
        tg_sort.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tg_sortActionPerformed(evt);
            }
        });
        jPanel3.add(tg_sort);

        jPanel22.add(jPanel3);

        import_csv.setText("Import DSD/CSV");
        import_csv.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                import_csvActionPerformed(evt);
            }
        });
        jPanel23.add(import_csv);

        backup_tg.setText("Export CSV");
        backup_tg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backup_tgActionPerformed(evt);
            }
        });
        jPanel23.add(backup_tg);

        auto_flash_tg.setText("AUTO FLASH");
        auto_flash_tg.setEnabled(false);
        auto_flash_tg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                auto_flash_tgActionPerformed(evt);
            }
        });
        jPanel23.add(auto_flash_tg);

        disable_encrypted.setSelected(true);
        disable_encrypted.setText("AUTO DISABLE ENCRYPTED");
        disable_encrypted.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                disable_encryptedActionPerformed(evt);
            }
        });
        jPanel23.add(disable_encrypted);

        auto_pop_table.setSelected(true);
        auto_pop_table.setText("AUTO POPULATE TABLE");
        auto_pop_table.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                auto_pop_tableActionPerformed(evt);
            }
        });
        jPanel23.add(auto_pop_table);

        enable_tg_table_updates.setSelected(true);
        enable_tg_table_updates.setText("Enable Table Updates");
        enable_tg_table_updates.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enable_tg_table_updatesActionPerformed(evt);
            }
        });
        jPanel23.add(enable_tg_table_updates);

        update_selected_tg.setText("Update DB");
        update_selected_tg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                update_selected_tgActionPerformed(evt);
            }
        });
        jPanel23.add(update_selected_tg);

        jPanel22.add(jPanel23);

        jPanel72.setForeground(new java.awt.Color(255, 255, 255));
        jPanel72.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jPanel73.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 15, 5));

        jLabel34.setText("Talk Group Timeout");
        jPanel73.add(jLabel34);

        vtimeout.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "500ms", "1sec", "1.5sec", "2sec", "3sec", "5sec", "10sec", "30sec" }));
        vtimeout.setToolTipText("The time since the last activity on a talk group before the receiver will follow a different talk group.");
        jPanel73.add(vtimeout);

        allow_unknown_tg_cb.setSelected(true);
        allow_unknown_tg_cb.setText("Allow Unknown Talkgroups");
        jPanel73.add(allow_unknown_tg_cb);

        allow_tg_pri_int.setSelected(true);
        allow_tg_pri_int.setText("Enable TG Priority Interrupts");
        jPanel73.add(allow_tg_pri_int);

        en_tg_int_tone.setSelected(true);
        en_tg_int_tone.setText("Enable 440 Hz TG Int Tone");
        jPanel73.add(en_tg_int_tone);

        jPanel72.add(jPanel73);

        write_config_tg.setText("Write Config");
        write_config_tg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                write_config_tgActionPerformed(evt);
            }
        });
        jPanel72.add(write_config_tg);

        jPanel22.add(jPanel72);

        talkgroup_panel.add(jPanel22, java.awt.BorderLayout.SOUTH);

        tabbed_pane.addTab("TG Editor", talkgroup_panel);

        channelconfig.setLayout(new java.awt.BorderLayout());

        jPanel8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel2.setText("Channel Browser  View Mode:");
        jPanel8.add(jLabel2);

        browser_view_mode.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All Channels", "Flash-based Channels", "P25-only", "FM-only", "DMR-only", "NXDN-only", "AM-only" }));
        browser_view_mode.setEnabled(false);
        browser_view_mode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browser_view_modeActionPerformed(evt);
            }
        });
        jPanel8.add(browser_view_mode);

        jSeparator6.setPreferredSize(new java.awt.Dimension(50, 0));
        jPanel8.add(jSeparator6);

        select_systems.setText("Select System Path");
        select_systems.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                select_systemsActionPerformed(evt);
            }
        });
        jPanel8.add(select_systems);

        systems_path_str.setText("Path:");
        jPanel8.add(systems_path_str);

        channelconfig.add(jPanel8, java.awt.BorderLayout.PAGE_START);

        jPanel15.setLayout(new javax.swing.BoxLayout(jPanel15, javax.swing.BoxLayout.Y_AXIS));

        jPanel16.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 15, 5));

        jLabel6.setText("Click Right Mouse Button On Tree View For Options");
        jPanel16.add(jLabel6);

        reset_to_defaults.setText("Reset Channel To RR Defaults");
        reset_to_defaults.setEnabled(false);
        reset_to_defaults.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reset_to_defaultsActionPerformed(evt);
            }
        });
        jPanel16.add(reset_to_defaults);

        refresh_treeview.setText("Refresh Tree View");
        refresh_treeview.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refresh_treeviewActionPerformed(evt);
            }
        });
        jPanel16.add(refresh_treeview);

        jPanel15.add(jPanel16);

        jPanel18.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        jPanel18.add(jPanel19);

        jPanel15.add(jPanel18);

        jPanel17.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jSeparator3.setPreferredSize(new java.awt.Dimension(50, 0));
        jPanel17.add(jSeparator3);

        cc_apply.setText("Apply Changes");
        cc_apply.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cc_applyActionPerformed(evt);
            }
        });
        jPanel17.add(cc_apply);

        cc_sync.setText("Sync Channel Config To Receiver");
        cc_sync.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cc_syncActionPerformed(evt);
            }
        });
        jPanel17.add(cc_sync);

        cc_import.setText("Import RR CSV");
        cc_import.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cc_importActionPerformed(evt);
            }
        });
        jPanel17.add(cc_import);

        jPanel15.add(jPanel17);

        channelconfig.add(jPanel15, java.awt.BorderLayout.SOUTH);

        chconfig_general.setLayout(new javax.swing.BoxLayout(chconfig_general, javax.swing.BoxLayout.LINE_AXIS));

        jPanel20.setLayout(new javax.swing.BoxLayout(jPanel20, javax.swing.BoxLayout.Y_AXIS));

        jPanel31.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        cc_do_scan.setText("Include In Scan");
        cc_do_scan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cc_do_scanActionPerformed(evt);
            }
        });
        jPanel31.add(cc_do_scan);

        cc_use_on_powerup.setText("Use On Power-Up");
        cc_use_on_powerup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cc_use_on_powerupActionPerformed(evt);
            }
        });
        jPanel31.add(cc_use_on_powerup);

        cc_install_to_flash.setText("Install To Flash");
        cc_install_to_flash.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cc_install_to_flashActionPerformed(evt);
            }
        });
        jPanel31.add(cc_install_to_flash);

        jPanel20.add(jPanel31);

        jPanel92.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel48.setText("Agency");
        jPanel92.add(jLabel48);

        cc_agency.setColumns(24);
        cc_agency.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cc_agencyActionPerformed(evt);
            }
        });
        cc_agency.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                cc_agencyKeyTyped(evt);
            }
        });
        jPanel92.add(cc_agency);

        jPanel20.add(jPanel92);

        jPanel30.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel14.setText("Desc");
        jPanel30.add(jLabel14);

        cc_name.setColumns(24);
        cc_name.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cc_nameActionPerformed(evt);
            }
        });
        cc_name.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                cc_nameKeyTyped(evt);
            }
        });
        jPanel30.add(cc_name);

        jPanel20.add(jPanel30);

        jPanel93.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel43.setText("Site ID");
        jPanel93.add(jLabel43);

        ccfg_site_id.setColumns(5);
        jPanel93.add(ccfg_site_id);

        county_lb.setText("County:");
        jPanel93.add(county_lb);

        jPanel20.add(jPanel93);

        jPanel25.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel11.setText("Frequency");
        jPanel25.add(jLabel11);

        cc_frequency.setColumns(12);
        cc_frequency.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cc_frequencyActionPerformed(evt);
            }
        });
        cc_frequency.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                cc_frequencyKeyTyped(evt);
            }
        });
        jPanel25.add(cc_frequency);

        jPanel20.add(jPanel25);

        jPanel26.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel13.setText("Modulation");
        jPanel26.add(jLabel13);

        cc_modulation.setMaximumRowCount(15);
        cc_modulation.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "P25 LSM", "P25 CQPSK", "TDMA CC", "DMR", "NXDN48", "NXDN96", "NBFM", "AM", "AM+AGC" }));
        cc_modulation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cc_modulationActionPerformed(evt);
            }
        });
        jPanel26.add(cc_modulation);

        jPanel20.add(jPanel26);

        jPanel29.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        buttonGroup23.add(cc_control);
        cc_control.setSelected(true);
        cc_control.setText("Control Channel");
        cc_control.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cc_controlActionPerformed(evt);
            }
        });
        jPanel29.add(cc_control);

        buttonGroup23.add(cc_conventional);
        cc_conventional.setText("Conventional");
        cc_conventional.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cc_conventionalActionPerformed(evt);
            }
        });
        jPanel29.add(cc_conventional);

        jPanel20.add(jPanel29);
        jPanel20.add(jPanel100);
        jPanel20.add(jPanel101);

        chconfig_general.add(jPanel20);

        jPanel32.setPreferredSize(new java.awt.Dimension(400, 157));
        jPanel32.setLayout(new javax.swing.BoxLayout(jPanel32, javax.swing.BoxLayout.Y_AXIS));

        jPanel41.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        ccfg_analog_en.setSelected(true);
        ccfg_analog_en.setText("Enable_Squelch");
        ccfg_analog_en.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ccfg_analog_enActionPerformed(evt);
            }
        });
        jPanel41.add(ccfg_analog_en);
        jPanel41.add(jLabel36);

        ccfg_squelch_level.setColumns(4);
        ccfg_squelch_level.setText("-120");
        ccfg_squelch_level.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ccfg_squelch_levelActionPerformed(evt);
            }
        });
        jPanel41.add(ccfg_squelch_level);

        jLabel37.setText("dBm");
        jPanel41.add(jLabel37);

        jPanel32.add(jPanel41);

        jPanel46.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        pl_tone_filter.setText("Enable CTCSS/PL");
        pl_tone_filter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pl_tone_filterActionPerformed(evt);
            }
        });
        jPanel46.add(pl_tone_filter);

        pl_tone_freq.setColumns(8);
        jPanel46.add(pl_tone_freq);

        jPanel32.add(jPanel46);

        jPanel38.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 15, 5));

        jLabel15.setText("DMR LCN");
        jPanel38.add(jLabel15);

        ccfg_dmr_lcn.setColumns(3);
        ccfg_dmr_lcn.setText("1");
        jPanel38.add(ccfg_dmr_lcn);

        jPanel32.add(jPanel38);
        jPanel32.add(jPanel62);
        jPanel32.add(jPanel69);
        jPanel32.add(jPanel74);
        jPanel32.add(jPanel77);
        jPanel32.add(jPanel79);

        jPanel80.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        jPanel32.add(jPanel80);

        chconfig_general.add(jPanel32);

        jTabbedPane2.addTab("General", chconfig_general);

        jPanel47.setLayout(new javax.swing.BoxLayout(jPanel47, javax.swing.BoxLayout.Y_AXIS));

        jPanel33.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel17.setText("LNA Gain");
        jPanel33.add(jLabel17);

        cc_rfgain.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "AUTO", "2 dB", "4 dB", "6 dB", "8 dB", "10 dB", "12 dB", "14 dB", "16 dB", "18 dB", "20 dB", "22 dB", "24 dB", "26 dB", "28 dB", "30 dB", "32 dB" }));
        cc_rfgain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cc_rfgainActionPerformed(evt);
            }
        });
        jPanel33.add(cc_rfgain);

        jSeparator9.setMinimumSize(new java.awt.Dimension(50, 0));
        jSeparator9.setPreferredSize(new java.awt.Dimension(50, 0));
        jPanel33.add(jSeparator9);

        find_fixed_gains.setText("Find Optimal Fixed Gains");
        find_fixed_gains.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                find_fixed_gainsActionPerformed(evt);
            }
        });
        jPanel33.add(find_fixed_gains);

        jPanel47.add(jPanel33);

        jPanel34.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel24.setText("Mixer Gain");
        jPanel34.add(jLabel24);

        cc_mgain.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "AUTO", "1.125 dB", "2.25 dB", "3.375 dB", "4.5 dB", "5.625 dB", "6.75 dB", "7.875 dB", "9 dB", "10.125 dB", "11.25 dB", "12.375 dB", "13.5 dB", "14.625 dB", "15.75 dB", "16.875", "18 dB" }));
        cc_mgain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cc_mgainActionPerformed(evt);
            }
        });
        jPanel34.add(cc_mgain);

        jSeparator10.setMinimumSize(new java.awt.Dimension(50, 0));
        jSeparator10.setPreferredSize(new java.awt.Dimension(50, 0));
        jPanel34.add(jSeparator10);

        set_gains_auto.setText("Set Auto");
        set_gains_auto.setEnabled(false);
        set_gains_auto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                set_gains_autoActionPerformed(evt);
            }
        });
        jPanel34.add(set_gains_auto);

        jPanel47.add(jPanel34);

        jPanel94.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel25.setText("VGA Gain");
        jPanel94.add(jLabel25);

        cc_vga_gain.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "AUTO", "0 dB", "3 dB", "6 dB", "9 dB", "12 dB", "15 dB", "18 dB", "21 dB", "24 dB", "27 dB", "30 dB", "33 dB", "36 dB", "39 dB", "42 dB", "45 dB" }));
        cc_vga_gain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cc_vga_gainActionPerformed(evt);
            }
        });
        jPanel94.add(cc_vga_gain);

        jPanel47.add(jPanel94);
        jPanel47.add(jPanel95);
        jPanel47.add(jPanel96);
        jPanel47.add(jPanel97);
        jPanel47.add(jPanel98);
        jPanel47.add(jPanel99);

        jTabbedPane2.addTab("Gains", jPanel47);

        p25config.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel37.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        auto_add_p25_traffic_ch.setText("Auto Add P25 Traffic Ch");
        auto_add_p25_traffic_ch.setEnabled(false);
        auto_add_p25_traffic_ch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                auto_add_p25_traffic_chActionPerformed(evt);
            }
        });
        jPanel37.add(auto_add_p25_traffic_ch);

        auto_add_p25_secondaries.setText("Auto Add P25 Secondaries");
        auto_add_p25_secondaries.setEnabled(false);
        auto_add_p25_secondaries.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                auto_add_p25_secondariesActionPerformed(evt);
            }
        });
        jPanel37.add(auto_add_p25_secondaries);

        auto_add_p25_neighbors.setText("Auto Add P25 Neighbors");
        auto_add_p25_neighbors.setEnabled(false);
        auto_add_p25_neighbors.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                auto_add_p25_neighborsActionPerformed(evt);
            }
        });
        jPanel37.add(auto_add_p25_neighbors);

        p25config.add(jPanel37, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, 890, 50));

        jPanel42.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel40.setText("P25 WACN");
        jLabel40.setEnabled(false);
        jPanel42.add(jLabel40);

        ccfg_p25_wacn.setColumns(15);
        ccfg_p25_wacn.setEnabled(false);
        ccfg_p25_wacn.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                ccfg_p25_sysidKeyTyped(evt);
            }
        });
        jPanel42.add(ccfg_p25_wacn);

        jLabel41.setText("P25 SYS_ID");
        jLabel41.setEnabled(false);
        jPanel42.add(jLabel41);

        ccfg_p25_sysid.setColumns(15);
        ccfg_p25_sysid.setEnabled(false);
        ccfg_p25_sysid.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                ccfg_p25_sysidKeyTyped(evt);
            }
        });
        jPanel42.add(ccfg_p25_sysid);

        jLabel42.setText("P25 NAC");
        jLabel42.setEnabled(false);
        jPanel42.add(jLabel42);

        ccfg_p25_nac.setColumns(15);
        ccfg_p25_nac.setEnabled(false);
        ccfg_p25_nac.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                ccfg_p25_sysidKeyTyped(evt);
            }
        });
        jPanel42.add(ccfg_p25_nac);

        jLabel45.setText("<- all inputs are in hex");
        jLabel45.setEnabled(false);
        jPanel42.add(jLabel45);

        p25config.add(jPanel42, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 80, 920, 50));

        jPanel43.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        buttonGroup24.add(ccfg_p25_p1);
        ccfg_p25_p1.setSelected(true);
        ccfg_p25_p1.setText("P25 P1");
        ccfg_p25_p1.setEnabled(false);
        ccfg_p25_p1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ccfg_p25_p1ActionPerformed(evt);
            }
        });
        jPanel43.add(ccfg_p25_p1);

        buttonGroup24.add(ccfg_p25_p2);
        ccfg_p25_p2.setText("P25 P2");
        ccfg_p25_p2.setEnabled(false);
        ccfg_p25_p2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ccfg_p25_p2ActionPerformed(evt);
            }
        });
        jPanel43.add(ccfg_p25_p2);

        p25config.add(jPanel43, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 140, 910, 40));

        jPanel36.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        allow_unknown_tg.setText("Allow Unknow Talk Groups");
        allow_unknown_tg.setEnabled(false);
        jPanel36.add(allow_unknown_tg);

        p25config.add(jPanel36, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 190, 520, 60));

        jTabbedPane2.addTab("P25 Config", p25config);

        jPanel81.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel82.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        en_acars.setText("ACARS");
        en_acars.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                en_acarsActionPerformed(evt);
            }
        });
        jPanel82.add(en_acars);

        en_flex32.setText("FLEX-3200");
        en_flex32.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                en_flex32ActionPerformed(evt);
            }
        });
        jPanel82.add(en_flex32);

        en_pocsag12.setText("POCSAG-1200");
        en_pocsag12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                en_pocsag12ActionPerformed(evt);
            }
        });
        jPanel82.add(en_pocsag12);

        console_dec_no_audio.setText("Disable Audio Output");
        console_dec_no_audio.setEnabled(false);
        console_dec_no_audio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                console_dec_no_audioActionPerformed(evt);
            }
        });
        jPanel82.add(console_dec_no_audio);

        jPanel81.add(jPanel82, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 30, 780, 50));

        jTabbedPane2.addTab("Console Decoders", jPanel81);

        jPanel83.setLayout(new javax.swing.BoxLayout(jPanel83, javax.swing.BoxLayout.Y_AXIS));

        jPanel84.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel1.setText("Trunked No Voice Timeout");
        jPanel84.add(jLabel1);

        trunk_no_voice_timeout.setColumns(5);
        trunk_no_voice_timeout.setText("0");
        jPanel84.add(trunk_no_voice_timeout);

        jLabel47.setText("Set this to 0 for constant scanning of channels. Set to a high number to \"Stick on CC until signal lost\".");
        jPanel84.add(jLabel47);

        jPanel83.add(jPanel84);
        jPanel83.add(jPanel85);
        jPanel83.add(jPanel86);
        jPanel83.add(jPanel87);
        jPanel83.add(jPanel88);
        jPanel83.add(jPanel89);

        write_config_global.setText("Write Config");
        write_config_global.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                write_config_globalActionPerformed(evt);
            }
        });
        jPanel90.add(write_config_global);

        jPanel83.add(jPanel90);
        jPanel83.add(jPanel91);

        jTabbedPane2.addTab("Global Settings", jPanel83);

        channel_search.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        profile_search1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel26.setText("Channel Bands");
        profile_search1.add(jLabel26);

        ccfg_ch_band.setMaximumRowCount(35);
        ccfg_ch_band.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Frequency Range  Mode  Step (kHz)  Band", "25.0000 to 26.9600 AM 5 Petroleum", "26.9650 to 27.4050 AM 5 CB Class D", "27.4100 to 27.9950 AM 5 Business & Forest", "28.0000 to 29.6800 NFM 20 10 Meter Amateur Band", "29.7000 to 49.9900 NFM 10 VHF Low Band", "50.0000 to 53.9800 NFM 20 6 Meter Amateur Band", "72.0000 to 75.9500 FM 5 Intersystem & Astronomy", "108.0000 to 136.9916 AM 8.33 Commercial Aircraft", "137.0000 to 143.9875 NFM 12.5 Military Land Mobile", "144.0000 to 147.9950 NFM 5 2 Meter Amateur Band", "148.0000 to 150.7875 NFM 12.5 Military Land Mobile", "150.8000 to 161.9950 NFM 5 VHF High Band", "162.0000 to 173.9875 NFM 12.5 Federal Government", "216.0000 to 224.9800 NFM 20 1.25 Meter Amateur", "225.0000 to 379.9750 AM 25 UHF Aircraft Band", "380.0000 to 399.9875 NFM 12.5 Trunked Military", "400.0000 to 405.9875 NFM 12.5 Miscellaneous", "406.0000 to 419.9875 NFM 12.5 Federal Government", "420.0000 to 449.9875 NFM 12.5 70 cm Amateur Band", "450.0000 to 469.9875 NFM 12.5 UHF Standard Band", "470.0000 to 512.0000 NFM 12.5 UHF-T Band", "758.0000 to 787.99375 NFM 6.25 Public Service Band", "788.0000 to 805.99375 NFM 6.25 Public Service Band", "806.0000 to 823.9875 NFM 12.5 Public Service Band", "849.0125 to 868.9875 NFM 12.5 Public Service Band", "894.0125 to 960.0000 NFM 12.5 Public Service Band" }));
        ccfg_ch_band.setSelectedIndex(25);
        ccfg_ch_band.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ccfg_ch_bandActionPerformed(evt);
            }
        });
        profile_search1.add(ccfg_ch_band);

        jLabel92.setText("CH SP");
        profile_search1.add(jLabel92);

        ccfg_ch_sp.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "5 kHz", "6.25 kHz", "8.33 kHz", "10 kHz", "12.5 kHz", "20 kHz", "25 kHz" }));
        ccfg_ch_sp.setSelectedIndex(4);
        ccfg_ch_sp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ccfg_ch_spActionPerformed(evt);
            }
        });
        profile_search1.add(ccfg_ch_sp);

        channel_search.add(profile_search1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 90, 940, -1));

        profile_search2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        profile_search2.add(jSeparator5);

        jLabel16.setText("Search Iterations");
        profile_search2.add(jLabel16);

        ccfg_iterations.setColumns(4);
        ccfg_iterations.setText("1");
        profile_search2.add(ccfg_iterations);

        jSeparator4.setMinimumSize(new java.awt.Dimension(50, 0));
        profile_search2.add(jSeparator4);

        ccfg_add_digital_ch.setSelected(true);
        ccfg_add_digital_ch.setText("Add Digital Ch");
        ccfg_add_digital_ch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ccfg_add_digital_chActionPerformed(evt);
            }
        });
        profile_search2.add(ccfg_add_digital_ch);

        ccfg_add_analog_ch1.setSelected(true);
        ccfg_add_analog_ch1.setText("Add Analog Ch");
        ccfg_add_analog_ch1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ccfg_add_analog_ch1ActionPerformed(evt);
            }
        });
        profile_search2.add(ccfg_add_analog_ch1);

        ccfg_start_discover.setText("Start");
        ccfg_start_discover.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ccfg_start_discoverActionPerformed(evt);
            }
        });
        profile_search2.add(ccfg_start_discover);

        ccfg_search_pause.setText("Pause");
        ccfg_search_pause.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ccfg_search_pauseActionPerformed(evt);
            }
        });
        profile_search2.add(ccfg_search_pause);

        ccfg_search_abort.setText("Abort");
        ccfg_search_abort.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ccfg_search_abortActionPerformed(evt);
            }
        });
        profile_search2.add(ccfg_search_abort);

        channel_search.add(profile_search2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 170, 820, -1));

        ccfg_prog.setStringPainted(true);
        channel_search.add(ccfg_prog, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 210, 870, -1));

        ccfg_iteration_lb.setText("Iteration: 0");
        channel_search.add(ccfg_iteration_lb, new org.netbeans.lib.awtextra.AbsoluteConstraints(850, 180, -1, -1));

        ccfg_ta.setBackground(new java.awt.Color(0, 0, 0));
        ccfg_ta.setColumns(20);
        ccfg_ta.setForeground(new java.awt.Color(255, 255, 255));
        ccfg_ta.setRows(5);
        jScrollPane4.setViewportView(ccfg_ta);

        channel_search.add(jScrollPane4, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 240, 870, 130));

        jPanel14.setBorder(javax.swing.BorderFactory.createTitledBorder("Sync Word Search"));
        jPanel14.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        ccfg_p1control.setSelected(true);
        ccfg_p1control.setText("P25 P1 Control");
        ccfg_p1control.setEnabled(false);
        ccfg_p1control.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ccfg_p1controlActionPerformed(evt);
            }
        });
        jPanel14.add(ccfg_p1control);

        ccfg_p1voice.setSelected(true);
        ccfg_p1voice.setText("P25 Voice");
        ccfg_p1voice.setEnabled(false);
        ccfg_p1voice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ccfg_p1voiceActionPerformed(evt);
            }
        });
        jPanel14.add(ccfg_p1voice);

        ccfg_dmr_control.setSelected(true);
        ccfg_dmr_control.setText("DMR Control");
        ccfg_dmr_control.setEnabled(false);
        ccfg_dmr_control.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ccfg_dmr_controlActionPerformed(evt);
            }
        });
        jPanel14.add(ccfg_dmr_control);

        ccfg_dmr_voice.setSelected(true);
        ccfg_dmr_voice.setText("DMR Voice");
        ccfg_dmr_voice.setEnabled(false);
        ccfg_dmr_voice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ccfg_dmr_voiceActionPerformed(evt);
            }
        });
        jPanel14.add(ccfg_dmr_voice);

        ccfg_nxdn_voice.setSelected(true);
        ccfg_nxdn_voice.setText("NXDN Voice");
        ccfg_nxdn_voice.setEnabled(false);
        ccfg_nxdn_voice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ccfg_nxdn_voiceActionPerformed(evt);
            }
        });
        jPanel14.add(ccfg_nxdn_voice);

        channel_search.add(jPanel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, 580, -1));
        channel_search.add(jPanel39, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 70, -1, -1));

        jPanel40.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel89.setText("St Freq");
        jPanel40.add(jLabel89);

        ccfg_st_freq.setColumns(10);
        ccfg_st_freq.setText("849.0125");
        jPanel40.add(ccfg_st_freq);

        jLabel90.setText("End Freq");
        jPanel40.add(jLabel90);

        ccfg_end_freq.setColumns(10);
        ccfg_end_freq.setText("868.9875");
        jPanel40.add(ccfg_end_freq);

        jLabel44.setText("CH Step");
        jPanel40.add(jLabel44);

        ccfg_ch_step.setColumns(5);
        ccfg_ch_step.setText("12.5");
        jPanel40.add(ccfg_ch_step);

        jLabel38.setText("Step Dwell");
        jPanel40.add(jLabel38);

        ccfg_step_dwell.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "25 ms", "50 ms", "100 ms", "250 ms", "500 ms", "1000 ms", "2000 ms", "5000 ms" }));
        ccfg_step_dwell.setSelectedIndex(2);
        jPanel40.add(ccfg_step_dwell);

        jLabel39.setText("Max Analyze Time");
        jPanel40.add(jLabel39);

        ccfg_max_analyze_time.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "250 ms", "500 ms", "1 sec", "3 sec", "10 sec", "100 sec" }));
        ccfg_max_analyze_time.setSelectedIndex(3);
        jPanel40.add(ccfg_max_analyze_time);

        channel_search.add(jPanel40, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 130, 880, 30));

        jTabbedPane2.addTab("Signal Search", channel_search);

        channelconfig.add(jTabbedPane2, java.awt.BorderLayout.CENTER);

        tabbed_pane.addTab("Channel Config", channelconfig);

        advancedpanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        adv_write_config.setText("Write Config");
        adv_write_config.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                adv_write_configActionPerformed(evt);
            }
        });
        advancedpanel.add(adv_write_config, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 440, -1, -1));

        en_encout.setText("Enable Encrypted Audio Output");
        advancedpanel.add(en_encout, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 220, -1, -1));

        en_p2_tones.setSelected(true);
        en_p2_tones.setText("Enable Phase II Tone Output");
        advancedpanel.add(en_p2_tones, new org.netbeans.lib.awtextra.AbsoluteConstraints(850, 230, -1, -1));

        p25_tone_vol.setColumns(5);
        p25_tone_vol.setText("1.0");
        p25_tone_vol.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                p25_tone_volActionPerformed(evt);
            }
        });
        advancedpanel.add(p25_tone_vol, new org.netbeans.lib.awtextra.AbsoluteConstraints(1040, 260, -1, -1));

        jLabel12.setText("P25 Phase II Tone Volume");
        advancedpanel.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(850, 260, -1, -1));

        en_zero_rid.setSelected(true);
        en_zero_rid.setText("Allow Logging Of RID = 0 (some transmissions report SRC ID of 0)");
        en_zero_rid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                en_zero_ridActionPerformed(evt);
            }
        });
        advancedpanel.add(en_zero_rid, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 180, -1, -1));

        enc_mode.setText("Return To Control Control Channel And Skip TG for 30 Sec If Encrypted");
        enc_mode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enc_modeActionPerformed(evt);
            }
        });
        advancedpanel.add(enc_mode, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 260, -1, -1));

        reset_defaults.setText("Reset To Defaults");
        reset_defaults.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reset_defaultsActionPerformed(evt);
            }
        });
        advancedpanel.add(reset_defaults, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 390, -1, -1));

        jPanel63.setLayout(new java.awt.GridLayout(2, 1));

        jPanel65.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel50.setText("P2 Sync Thresh");
        jPanel65.add(jLabel50);

        p2_sync_thresh.setColumns(3);
        p2_sync_thresh.setText("0");
        jPanel65.add(p2_sync_thresh);

        jLabel66.setText("default=0 (max err allowed)");
        jPanel65.add(jLabel66);

        jPanel63.add(jPanel65);

        jPanel64.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel57.setText("P1 Sync Thresh");
        jPanel64.add(jLabel57);

        p1_sync_thresh.setColumns(3);
        p1_sync_thresh.setText("2");
        jPanel64.add(p1_sync_thresh);

        jLabel65.setText("default=2, (max err allowed)");
        jPanel64.add(jLabel65);

        jPanel63.add(jPanel64);

        advancedpanel.add(jPanel63, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 180, 220, 100));

        jPanel56.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel52.setText("ENC Timeout ms");
        jPanel56.add(jLabel52);

        enc_timeout.setColumns(9);
        enc_timeout.setText("30000");
        jPanel56.add(enc_timeout);

        jLabel54.setText("ENC Verify Count");
        jPanel56.add(jLabel54);

        enc_count.setColumns(3);
        enc_count.setText("2");
        jPanel56.add(enc_count);

        jLabel55.setText("(0 to 10)");
        jPanel56.add(jLabel55);

        advancedpanel.add(jPanel56, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 290, 480, 30));

        enable_alias_dbase.setText("Enable ALIAS DBASE File Generation");
        enable_alias_dbase.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enable_alias_dbaseActionPerformed(evt);
            }
        });
        advancedpanel.add(enable_alias_dbase, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 330, -1, 30));

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder("P25RX-II RF Gains Config"));
        jPanel7.setLayout(new javax.swing.BoxLayout(jPanel7, javax.swing.BoxLayout.Y_AXIS));

        jPanel53.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel31.setText("LNA Gain");
        jPanel53.add(jLabel31);

        rfgain.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "AUTO", "2 dB", "4 dB", "6 dB", "8 dB", "10 dB", "12 dB", "14 dB", "16 dB", "18 dB", "20 dB", "22 dB", "24 dB", "26 dB", "28 dB", "30 dB", "32 dB" }));
        jPanel53.add(rfgain);

        jSeparator42.setPreferredSize(new java.awt.Dimension(20, 0));
        jPanel53.add(jSeparator42);

        jLabel35.setText("Mixer Gain");
        jPanel53.add(jLabel35);

        mixgain.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "AUTO", "1.125 dB", "2.25 dB", "3.375 dB", "4.5 dB", "5.625 dB", "6.75 dB", "7.875 dB", "9 dB", "10.125 dB", "11.25 dB", "12.375 dB", "13.5 dB", "14.625 dB", "15.75 dB", "16.875", "18 dB" }));
        jPanel53.add(mixgain);

        jSeparator43.setPreferredSize(new java.awt.Dimension(20, 0));
        jPanel53.add(jSeparator43);

        jLabel29.setText("BB VGA Gain");
        jPanel53.add(jLabel29);

        vga_gain.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "AUTO", "0 dB", "3 dB", "6 dB", "9 dB", "12 dB", "15 dB", "18 dB", "21 dB", "24 dB", "27 dB", "30 dB", "33 dB", "36 dB", "39 dB", "42 dB", "45 dB" }));
        jPanel53.add(vga_gain);

        jSeparator46.setPreferredSize(new java.awt.Dimension(20, 0));
        jPanel53.add(jSeparator46);

        jLabel56.setText("Hysteresis");
        jPanel53.add(jLabel56);

        rf_hyst.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "3 dB", "6 dB", "10 dB" }));
        rf_hyst.setSelectedIndex(2);
        jPanel53.add(rf_hyst);

        jPanel7.add(jPanel53);

        jPanel75.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel30.setText("AGC Target");
        jPanel75.add(jLabel30);

        vga_target.setColumns(3);
        vga_target.setText("-10");
        vga_target.setToolTipText("Higher values = More Linear, Lower values=More Sensitive");
        vga_target.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vga_targetActionPerformed(evt);
            }
        });
        jPanel75.add(vga_target);

        jLabel53.setText("dB (default -10 dB)");
        jPanel75.add(jLabel53);

        jSeparator44.setPreferredSize(new java.awt.Dimension(10, 0));
        jPanel75.add(jSeparator44);

        jLabel62.setText("  AGC Step Time");
        jPanel75.add(jLabel62);

        vga_step.setColumns(3);
        vga_step.setText("25");
        vga_step.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vga_stepActionPerformed(evt);
            }
        });
        jPanel75.add(vga_step);

        jLabel63.setText("ms");
        jPanel75.add(jLabel63);

        jSeparator45.setPreferredSize(new java.awt.Dimension(10, 0));
        jPanel75.add(jSeparator45);

        jSeparator47.setPreferredSize(new java.awt.Dimension(10, 0));
        jPanel75.add(jSeparator47);

        jLabel8.setText("VGA Gain Offset");
        jPanel75.add(jLabel8);

        vga_gain_offset.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "-9 dB", "-6 dB", "-3 dB", "0 dB", "+3 dB", "+6 dB", "+9 dB" }));
        vga_gain_offset.setSelectedIndex(3);
        vga_gain_offset.setEnabled(false);
        jPanel75.add(vga_gain_offset);

        jPanel7.add(jPanel75);

        jPanel76.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel60.setText("Suggest 0 to -20 dB AGC Target");
        jPanel76.add(jLabel60);

        jSeparator48.setPreferredSize(new java.awt.Dimension(50, 0));
        jPanel76.add(jSeparator48);

        jLabel49.setText("AGC AUTO-GAIN Profile");
        jPanel76.add(jLabel49);

        auto_gain_profile.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Maximum Sensitivity", "Maximum Linearity" }));
        jPanel76.add(auto_gain_profile);

        jPanel7.add(jPanel76);

        advancedpanel.add(jPanel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 10, 770, 140));

        save_iq.setText("Save I/Q File");
        save_iq.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                save_iqActionPerformed(evt);
            }
        });
        advancedpanel.add(save_iq, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 340, -1, -1));

        jLabel7.setText("Diagnostics:");
        advancedpanel.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 350, -1, -1));

        popout_ontop.setSelected(true);
        popout_ontop.setText("Pop-out Windows Always On Top");
        popout_ontop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popout_ontopActionPerformed(evt);
            }
        });
        advancedpanel.add(popout_ontop, new org.netbeans.lib.awtextra.AbsoluteConstraints(850, 30, -1, -1));

        enable_event_dbase.setText("Enable EVENT DBASE File Generation");
        enable_event_dbase.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enable_event_dbaseActionPerformed(evt);
            }
        });
        advancedpanel.add(enable_event_dbase, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 290, -1, 30));

        tabbed_pane.addTab("Advanced", advancedpanel);

        consolePanel.setPreferredSize(new java.awt.Dimension(963, 500));
        consolePanel.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                consolePanelFocusGained(evt);
            }
        });
        consolePanel.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jTextArea1.setEditable(false);
        jTextArea1.setBackground(new java.awt.Color(0, 0, 0));
        jTextArea1.setColumns(120);
        jTextArea1.setFont(new java.awt.Font("Monospaced", 0, 14)); // NOI18N
        jTextArea1.setForeground(new java.awt.Color(255, 255, 255));
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(40);
        jTextArea1.setText("\nBlueTail Technologies P25RX Console\n\n$ ");
        jTextArea1.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        jTextArea1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextArea1FocusGained(evt);
            }
        });
        jTextArea1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextArea1KeyTyped(evt);
            }
        });
        jScrollPane1.setViewportView(jTextArea1);

        consolePanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        console_color.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        console_color.setText("Color");
        console_color.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                console_colorActionPerformed(evt);
            }
        });
        jPanel51.add(console_color);

        console_font.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        console_font.setText("Font");
        console_font.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                console_fontActionPerformed(evt);
            }
        });
        jPanel51.add(console_font);

        en_evt_output.setText("Display And Log Events");
        en_evt_output.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                en_evt_outputActionPerformed(evt);
            }
        });
        jPanel51.add(en_evt_output);

        jPanel6.add(jPanel51);

        jCheckBox1.setSelected(true);
        jCheckBox1.setText("Enable Debugging Messages");
        jCheckBox1.setEnabled(false);
        jPanel6.add(jCheckBox1);

        consolepop.setIcon(new javax.swing.ImageIcon(getClass().getResource("/btconfig/images/rightarrow.png"))); // NOI18N
        consolepop.setText(" ");
        consolepop.setBorderPainted(false);
        consolepop.setContentAreaFilled(false);
        consolepop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                consolepopActionPerformed(evt);
            }
        });
        jPanel6.add(consolepop);

        consolePanel.add(jPanel6, java.awt.BorderLayout.PAGE_END);

        tabbed_pane.addTab("Console", consolePanel);

        logpanel.setBackground(new java.awt.Color(0, 0, 0));
        logpanel.setPreferredSize(new java.awt.Dimension(963, 500));
        logpanel.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                logpanelFocusGained(evt);
            }
        });
        logpanel.setLayout(new java.awt.BorderLayout());

        log_ta.setEditable(false);
        log_ta.setBackground(new java.awt.Color(0, 0, 0));
        log_ta.setColumns(120);
        log_ta.setFont(new java.awt.Font("Monospaced", 0, 14)); // NOI18N
        log_ta.setForeground(new java.awt.Color(255, 255, 255));
        log_ta.setRows(40);
        log_ta.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        log_ta.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                log_taFocusGained(evt);
            }
        });
        log_ta.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                log_taKeyTyped(evt);
            }
        });
        tg_scroll_pane.setViewportView(log_ta);

        logpanel.add(tg_scroll_pane, java.awt.BorderLayout.CENTER);

        tgfontpanel.setBackground(new java.awt.Color(0, 0, 0));
        tgfontpanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        tglog_font.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        tglog_font.setText("Font");
        tglog_font.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglog_fontActionPerformed(evt);
            }
        });
        tgfontpanel.add(tglog_font);

        tglog_color.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        tglog_color.setText("Color");
        tglog_color.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglog_colorActionPerformed(evt);
            }
        });
        tgfontpanel.add(tglog_color);

        tglog_edit.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        tglog_edit.setText("Edit");
        tglog_edit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tglog_editActionPerformed(evt);
            }
        });
        tgfontpanel.add(tglog_edit);

        logpanel.add(tgfontpanel, java.awt.BorderLayout.PAGE_END);

        tabbed_pane.addTab("TG Log", logpanel);

        buttong_config.setLayout(new java.awt.BorderLayout());

        jPanel21.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel18.setText("Single Click");
        jPanel21.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 70, -1, -1));

        jLabel19.setText("Double Click");
        jPanel21.add(jLabel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 140, -1, 20));

        jLabel20.setText("Triple Click");
        jPanel21.add(jLabel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 220, -1, -1));

        button_write_config.setText("Write Config");
        button_write_config.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_write_configActionPerformed(evt);
            }
        });
        jPanel21.add(button_write_config, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 390, -1, -1));

        jPanel27.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        buttonGroup11.add(single_click_opt1);
        single_click_opt1.setSelected(true);
        single_click_opt1.setText("Follow TG");
        jPanel27.add(single_click_opt1);

        buttonGroup11.add(single_click_opt2);
        single_click_opt2.setText("Bluetooth Pairing");
        jPanel27.add(single_click_opt2);

        buttonGroup11.add(single_click_opt3);
        single_click_opt3.setText("Enable/Disable Status Leds");
        jPanel27.add(single_click_opt3);

        buttonGroup11.add(single_click_opt4);
        single_click_opt4.setText("Skip TG");
        jPanel27.add(single_click_opt4);

        buttonGroup11.add(single_click_opt5);
        single_click_opt5.setText("Enable/Disable Unknown TG");
        jPanel27.add(single_click_opt5);

        buttonGroup11.add(single_click_opt6);
        single_click_opt6.setText("Enable/Disable Scanning");
        jPanel27.add(single_click_opt6);

        jPanel21.add(jPanel27, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 80, 1010, 40));

        jPanel28.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        buttonGroup10.add(double_click_opt1);
        double_click_opt1.setText("Follow TG");
        jPanel28.add(double_click_opt1);

        buttonGroup10.add(double_click_opt2);
        double_click_opt2.setSelected(true);
        double_click_opt2.setText("Bluetooth Pairing");
        jPanel28.add(double_click_opt2);

        buttonGroup10.add(double_click_opt3);
        double_click_opt3.setText("Enable/Disable Status Leds");
        jPanel28.add(double_click_opt3);

        buttonGroup10.add(double_click_opt4);
        double_click_opt4.setText("Skip TG");
        jPanel28.add(double_click_opt4);

        buttonGroup10.add(double_click_opt5);
        double_click_opt5.setText("Enable/Disable Unknown TG");
        jPanel28.add(double_click_opt5);

        buttonGroup10.add(double_click_opt6);
        double_click_opt6.setText("Enable/Disable Scanning");
        jPanel28.add(double_click_opt6);

        jPanel21.add(jPanel28, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 153, -1, 70));

        jPanel45.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        buttonGroup9.add(triple_click_opt1);
        triple_click_opt1.setText("Follow TG");
        jPanel45.add(triple_click_opt1);

        buttonGroup9.add(triple_click_opt2);
        triple_click_opt2.setText("Bluetooth Pairing");
        jPanel45.add(triple_click_opt2);

        buttonGroup9.add(triple_click_opt3);
        triple_click_opt3.setSelected(true);
        triple_click_opt3.setText("Enable/Disable Status Leds");
        jPanel45.add(triple_click_opt3);

        buttonGroup9.add(triple_click_opt4);
        triple_click_opt4.setText("Skip TG");
        triple_click_opt4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                triple_click_opt4ActionPerformed(evt);
            }
        });
        jPanel45.add(triple_click_opt4);

        buttonGroup9.add(triple_click_opt5);
        triple_click_opt5.setText("Enable/Disable Unknown TG");
        jPanel45.add(triple_click_opt5);

        buttonGroup9.add(triple_click_opt6);
        triple_click_opt6.setText("Enable/Disable Scanning");
        jPanel45.add(triple_click_opt6);

        jPanel21.add(jPanel45, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 230, 1040, 40));

        jLabel21.setText("Skip TG Timeout ");
        jPanel48.add(jLabel21);

        skip_tg_to.setColumns(5);
        skip_tg_to.setText("60");
        jPanel48.add(skip_tg_to);

        jLabel22.setText("Minutes");
        jPanel48.add(jLabel22);

        jSeparator40.setMinimumSize(new java.awt.Dimension(75, 10));
        jSeparator40.setPreferredSize(new java.awt.Dimension(75, 0));
        jPanel48.add(jSeparator40);

        roaming_ret_to_cc.setSelected(true);
        roaming_ret_to_cc.setText("Roaming Return To Primary On Disable");
        jPanel48.add(roaming_ret_to_cc);

        jPanel21.add(jPanel48, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 350, 870, 50));

        jLabel33.setText("Quad Click");
        jPanel21.add(jLabel33, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 290, -1, -1));

        jPanel49.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        buttonGroup13.add(quad_click_opt1);
        quad_click_opt1.setText("Follow TG");
        jPanel49.add(quad_click_opt1);

        buttonGroup13.add(quad_click_opt2);
        quad_click_opt2.setText("Bluetooth Pairing");
        jPanel49.add(quad_click_opt2);

        buttonGroup13.add(quad_click_opt3);
        quad_click_opt3.setText("Enable/Disable Status Leds");
        jPanel49.add(quad_click_opt3);

        buttonGroup13.add(quad_click_opt4);
        quad_click_opt4.setText("Skip TG");
        quad_click_opt4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quad_click_opt4ActionPerformed(evt);
            }
        });
        jPanel49.add(quad_click_opt4);

        buttonGroup13.add(quad_click_opt5);
        quad_click_opt5.setText("Enable/Disable Unknown TG");
        jPanel49.add(quad_click_opt5);

        buttonGroup13.add(quad_click_opt6);
        quad_click_opt6.setSelected(true);
        quad_click_opt6.setText("Enable/Disable Scanning");
        jPanel49.add(quad_click_opt6);

        jPanel21.add(jPanel49, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 300, 1030, -1));

        buttong_config.add(jPanel21, java.awt.BorderLayout.CENTER);

        tabbed_pane.addTab("Button CFG", buttong_config);

        alias_panel.setLayout(new java.awt.BorderLayout());

        alias_table.setModel(new javax.swing.table.DefaultTableModel(
            new Object[16000][4],
            new String [] {
                "Radio ID", "Alias_And_Comments", "TG", "AlphaTag"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane5.setViewportView(alias_table);

        alias_panel.add(jScrollPane5, java.awt.BorderLayout.CENTER);

        buttonGroup25.add(sort_alias);
        sort_alias.setSelected(true);
        sort_alias.setText("Sort by Alias");
        sort_alias.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sort_aliasActionPerformed(evt);
            }
        });
        jPanel5.add(sort_alias);

        buttonGroup25.add(sort_rid);
        sort_rid.setText("Sort By RID");
        sort_rid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sort_ridActionPerformed(evt);
            }
        });
        jPanel5.add(sort_rid);

        import_alias.setText("Import CSV");
        import_alias.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                import_aliasActionPerformed(evt);
            }
        });
        jPanel5.add(import_alias);

        export_alias.setText("Export CSV");
        export_alias.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                export_aliasActionPerformed(evt);
            }
        });
        jPanel5.add(export_alias);

        update_alias_table.setText("Update DB");
        update_alias_table.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                update_alias_tableActionPerformed(evt);
            }
        });
        jPanel5.add(update_alias_table);

        del_alias.setText("DEL Selected");
        del_alias.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                del_aliasActionPerformed(evt);
            }
        });
        jPanel5.add(del_alias);

        jLabel61.setText("Use DEL Key To Delete Records");
        jPanel5.add(jLabel61);

        alias_panel.add(jPanel5, java.awt.BorderLayout.SOUTH);

        tabbed_pane.addTab("Alias", alias_panel);

        signalinsightpanel.setLayout(new java.awt.BorderLayout());

        const_panel.setBackground(new java.awt.Color(0, 0, 0));
        const_panel.setMaximumSize(new java.awt.Dimension(1512, 1512));
        const_panel.setMinimumSize(new java.awt.Dimension(512, 512));
        const_panel.setPreferredSize(new java.awt.Dimension(512, 512));
        const_panel.setLayout(new java.awt.BorderLayout());

        jPanel78.setBackground(new java.awt.Color(0, 0, 0));
        const_panel.add(jPanel78, java.awt.BorderLayout.EAST);

        signalinsightpanel.add(const_panel, java.awt.BorderLayout.CENTER);

        jPanel55.setLayout(new javax.swing.BoxLayout(jPanel55, javax.swing.BoxLayout.Y_AXIS));

        jPanel24.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        autoscale_const.setSelected(true);
        autoscale_const.setText("Auto Scale");
        autoscale_const.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoscale_constActionPerformed(evt);
            }
        });
        jPanel24.add(autoscale_const);

        nsymbols.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "256 Symbols", "512 Symbols", "1024 Symbols", "2048 Symbols", "4096 Symbols", "8192 Symbols" }));
        nsymbols.setEnabled(false);
        nsymbols.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nsymbolsActionPerformed(evt);
            }
        });
        jPanel24.add(nsymbols);
        jPanel24.add(jSeparator41);

        jPanel68.setBorder(javax.swing.BorderFactory.createTitledBorder("CPU Usage (Sig Insights / DView Update Rate)"));

        buttonGroup20.add(si_cpu_high);
        si_cpu_high.setText("High");
        si_cpu_high.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                si_cpu_highActionPerformed(evt);
            }
        });
        jPanel68.add(si_cpu_high);

        buttonGroup20.add(si_cpu_normal);
        si_cpu_normal.setSelected(true);
        si_cpu_normal.setText("Normal");
        si_cpu_normal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                si_cpu_normalActionPerformed(evt);
            }
        });
        jPanel68.add(si_cpu_normal);

        buttonGroup20.add(si_cpu_low);
        si_cpu_low.setText("Low");
        si_cpu_low.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                si_cpu_lowActionPerformed(evt);
            }
        });
        jPanel68.add(si_cpu_low);

        buttonGroup20.add(si_cpu_battery_saving);
        si_cpu_battery_saving.setText("Battery Saving");
        si_cpu_battery_saving.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                si_cpu_battery_savingActionPerformed(evt);
            }
        });
        jPanel68.add(si_cpu_battery_saving);

        buttonGroup20.add(si_cpu_off);
        si_cpu_off.setText("Off");
        si_cpu_off.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                si_cpu_offActionPerformed(evt);
            }
        });
        jPanel68.add(si_cpu_off);

        jPanel24.add(jPanel68);

        write_cfg_si.setText("Write Config");
        write_cfg_si.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                write_cfg_siActionPerformed(evt);
            }
        });
        jPanel24.add(write_cfg_si);

        freemem.setText("Free Memory");
        freemem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                freememActionPerformed(evt);
            }
        });
        jPanel24.add(freemem);

        jPanel55.add(jPanel24);

        jPanel58.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel9.setText("IF FREQ TEST");
        jLabel9.setEnabled(false);
        jPanel58.add(jLabel9);

        if_slide.setMaximum(459000);
        if_slide.setMinimum(454000);
        if_slide.setEnabled(false);
        if_slide.setPreferredSize(new java.awt.Dimension(400, 16));
        if_slide.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                if_slideStateChanged(evt);
            }
        });
        jPanel58.add(if_slide);

        jLabel27.setText("CMD");
        jPanel58.add(jLabel27);

        sig_cmd.setColumns(40);
        sig_cmd.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                sig_cmdKeyReleased(evt);
            }
        });
        jPanel58.add(sig_cmd);

        sipopout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/btconfig/images/rightarrow.png"))); // NOI18N
        sipopout.setText(" ");
        sipopout.setBorderPainted(false);
        sipopout.setContentAreaFilled(false);
        sipopout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sipopoutActionPerformed(evt);
            }
        });
        jPanel58.add(sipopout);

        jPanel55.add(jPanel58);

        signalinsightpanel.add(jPanel55, java.awt.BorderLayout.NORTH);

        tabbed_pane.addTab("Signal Insights", signalinsightpanel);

        displayviewmain_border.setBackground(new java.awt.Color(0, 0, 0));
        displayviewmain_border.setLayout(new java.awt.BorderLayout());

        display_frame.setBackground(new java.awt.Color(0, 0, 0));
        display_frame.setLayout(new java.awt.GridLayout(5, 1));
        displayviewmain_border.add(display_frame, java.awt.BorderLayout.CENTER);

        jPanel61.setBackground(new java.awt.Color(0, 0, 0));
        jPanel61.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        hold.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        hold.setText("H");
        hold.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                holdActionPerformed(evt);
            }
        });
        jPanel61.add(hold);

        skip.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        skip.setText("S");
        skip.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                skipActionPerformed(evt);
            }
        });
        jPanel61.add(skip);

        edit_alias.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        edit_alias.setText("Edit Alias");
        edit_alias.setEnabled(false);
        edit_alias.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                edit_aliasActionPerformed(evt);
            }
        });
        jPanel61.add(edit_alias);

        edit_display_view.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        edit_display_view.setText("EDIT Display");
        edit_display_view.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                edit_display_viewActionPerformed(evt);
            }
        });
        jPanel61.add(edit_display_view);

        dvpopout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/btconfig/images/rightarrow.png"))); // NOI18N
        dvpopout.setText(" ");
        dvpopout.setBorderPainted(false);
        dvpopout.setContentAreaFilled(false);
        dvpopout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dvpopoutActionPerformed(evt);
            }
        });
        jPanel61.add(dvpopout);

        displayviewmain_border.add(jPanel61, java.awt.BorderLayout.SOUTH);

        tabbed_pane.addTab("Display View", displayviewmain_border);

        tabbed_pane.setSelectedIndex(3);

        getContentPane().add(tabbed_pane, java.awt.BorderLayout.CENTER);

        northpanel.setBackground(new java.awt.Color(255, 255, 255));
        northpanel.setLayout(new java.awt.BorderLayout());

        logo_panel.setBackground(new java.awt.Color(255, 255, 255));
        logo_panel.setMinimumSize(new java.awt.Dimension(877, 10));
        logo_panel.setName(""); // NOI18N
        logo_panel.setPreferredSize(new java.awt.Dimension(1150, 80));
        logo_panel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jSeparator1.setMaximumSize(new java.awt.Dimension(32767, 0));
        jSeparator1.setMinimumSize(new java.awt.Dimension(40, 0));
        jSeparator1.setPreferredSize(new java.awt.Dimension(40, 0));
        logo_panel.add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(5, 50, -1, -1));

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/btconfig/images/btlogo_small.gif"))); // NOI18N
        jButton1.setBorderPainted(false);
        jButton1.setContentAreaFilled(false);
        jButton1.setFocusPainted(false);
        jButton1.setFocusable(false);
        jButton1.setRequestFocusEnabled(false);
        jButton1.setRolloverEnabled(false);
        jButton1.setVerifyInputWhenFocusTarget(false);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        logo_panel.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 5, -1, -1));

        top_label.setText("ADV-BTT-CFG");
        logo_panel.add(top_label, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 10, -1, -1));

        wacn.setText("WACN:");
        logo_panel.add(wacn, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 40, -1, -1));

        sysid.setText("SYS_ID:");
        logo_panel.add(sysid, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 40, -1, -1));

        nac.setText("NAC:");
        logo_panel.add(nac, new org.netbeans.lib.awtextra.AbsoluteConstraints(800, 40, -1, -1));

        freq.setText("Freq:");
        logo_panel.add(freq, new org.netbeans.lib.awtextra.AbsoluteConstraints(890, 40, -1, -1));

        siteid.setText("SITE ID:");
        logo_panel.add(siteid, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 60, -1, -1));

        rfid.setText("RFSS ID:");
        logo_panel.add(rfid, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 60, -1, -1));

        macid.setText("MAC ID:");
        logo_panel.add(macid, new org.netbeans.lib.awtextra.AbsoluteConstraints(800, 60, -1, -1));

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        minimize.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        minimize.setText("MON");
        minimize.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                minimizeActionPerformed(evt);
            }
        });
        jPanel4.add(minimize);

        record_to_mp3.setText("REC");
        record_to_mp3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                record_to_mp3ActionPerformed(evt);
            }
        });
        jPanel4.add(record_to_mp3);

        release_date.setText("V: ");
        jPanel4.add(release_date);

        logo_panel.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(790, 0, 320, -1));

        ser_dev.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        ser_dev.setText("PORT:");
        logo_panel.add(ser_dev, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, -1, -1));

        northpanel.add(logo_panel, java.awt.BorderLayout.CENTER);

        getContentPane().add(northpanel, java.awt.BorderLayout.NORTH);

        eastpanel.setBackground(new java.awt.Color(0, 0, 0));
        eastpanel.setForeground(new java.awt.Color(255, 255, 255));
        eastpanel.setPreferredSize(new java.awt.Dimension(50, 100));
        eastpanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        slider_squelch.setBackground(new java.awt.Color(0, 0, 0));
        slider_squelch.setMaximum(-30);
        slider_squelch.setMinimum(-140);
        slider_squelch.setOrientation(javax.swing.JSlider.VERTICAL);
        slider_squelch.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                slider_squelchStateChanged(evt);
            }
        });
        slider_squelch.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                slider_squelchMouseWheelMoved(evt);
            }
        });
        eastpanel.add(slider_squelch, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 160, 40, 260));

        slider_val.setBackground(new java.awt.Color(0, 0, 0));
        slider_val.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        slider_val.setForeground(new java.awt.Color(255, 255, 255));
        slider_val.setText("-120");
        eastpanel.add(slider_val, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 420, 50, -1));

        squelch_set.setBackground(new java.awt.Color(0, 0, 0));
        squelch_set.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        squelch_set.setForeground(new java.awt.Color(255, 255, 255));
        squelch_set.setText("SQ");
        squelch_set.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        squelch_set.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                squelch_setActionPerformed(evt);
            }
        });
        eastpanel.add(squelch_set, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 110, 50, 30));

        getContentPane().add(eastpanel, java.awt.BorderLayout.EAST);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void discoverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_discoverActionPerformed
      do_connect();

      //if(serial_port!=null) serial_port.closePort();
      //is_connected=0;
      //discover.setEnabled(true);
    }//GEN-LAST:event_discoverActionPerformed

    public void do_connect() {
      do_connect=1;
      do_read_config=1;
    }

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jTextArea1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextArea1KeyTyped
    // TODO add your handling code here:
      char c = evt.getKeyChar();

      //System.out.println("got key");

      //if(skip_bytes==0 && do_update_firmware==0) {
        if(c=='\n') {
          String str = String.copyValueOf(keydata,0,keyindex);
          str = str.trim()+"\r\n";
            //handle str 
            byte b[] = new byte[4096];
            for(int i=0;i<keyindex+2;i++) {
              //b[i] = (byte) keydata[i];
              b = str.getBytes();
            }
            if(serial_port!=null) serial_port.writeBytes(b,keyindex+2);
          keyindex=0;
          command_input=0;
        } else if(c=='\b') {
          if(keyindex>0) {
            keyindex--;
            String str = jTextArea1.getText();
            if(str.length()>0) jTextArea1.setText( str.substring(0, str.length()-1) );
          }
        } else {
          if(command_input==0) addTextConsole("\r\n$ ");
          command_input=1;
          command_input_timeout=5000;
          keydata[keyindex++] = c;
          jTextArea1.append( new Character(c).toString() );
        }
    }//GEN-LAST:event_jTextArea1KeyTyped

    private void jTextArea1FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextArea1FocusGained
      if( parent.tabbed_pane.getTitleAt(tabbed_pane.getSelectedIndex()).contains("Console")) {
        jTextArea1.requestFocus();
      }
    }//GEN-LAST:event_jTextArea1FocusGained

    private void consolePanelFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_consolePanelFocusGained
      if( parent.tabbed_pane.getTitleAt(tabbed_pane.getSelectedIndex()).contains("Console")) {
        jTextArea1.requestFocus();
      }
    }//GEN-LAST:event_consolePanelFocusGained

    private void tabbed_paneStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabbed_paneStateChanged
      try {
        if( parent.tabbed_pane.getTitleAt(tabbed_pane.getSelectedIndex()).contains("Console")) {
          jTextArea1.requestFocus();
        }
        if( parent.tabbed_pane.getTitleAt(tabbed_pane.getSelectedIndex()).contains("TG Editor")) {
          talkgroups_db.update_talkgroup_table(); 
        }
        if( parent.tabbed_pane.getTitleAt(tabbed_pane.getSelectedIndex()).contains("Alias")) {
          alias_db.update_alias_table(); 
        }
      } catch(Exception e) {
      }
    }//GEN-LAST:event_tabbed_paneStateChanged

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
      resizeColumns();
      resizeColumns3();
      //if(minimize.isSelected()) {
       // setSize(1054,192);
      //}
      save_position();
    }//GEN-LAST:event_formComponentResized

    private void disconnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disconnectActionPerformed
      do_disconnect=1;
    }//GEN-LAST:event_disconnectActionPerformed

    private void check_firmwareActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_check_firmwareActionPerformed
    }//GEN-LAST:event_check_firmwareActionPerformed

    private void minimizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minimizeActionPerformed
      if(minimize.isSelected()) {
        if(isWindows ) {
          setSize(1200,200+18+37+27+15+12);
        }
        else  {
          setSize(1200,185+18+37+27+15+12);  //linux and Mac
        }
      }
      else {
        setSize(1200,800+10+27+10);
        //parentSize = Toolkit.getDefaultToolkit().getScreenSize();
        //setSize(new Dimension((int) (parentSize.width * 0.75), (int) (parentSize.height * 0.8)));
      }
    }//GEN-LAST:event_minimizeActionPerformed

    private void record_to_mp3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_record_to_mp3ActionPerformed
      do_toggle_record=1;
    }//GEN-LAST:event_record_to_mp3ActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
      if(serial_port!=null) {
          SLEEP(100);
        String cmd= new String("en_voice_send 0\r\n");
        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
          SLEEP(100);
        cmd= new String("logging 0\r\n");
        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      }

      try {
        if(serial_port!=null) serial_port.closePort();
      } catch(Exception e) {
      }

      try {
        prefs.put("end_call_silence", end_call_silence.getText() ); 
      } catch(Exception e) {
        e.printStackTrace();
      }

      try {
        parent.prefs.put("status_format_cc", status_format_cc.getText() ); 
      } catch(Exception e) {
        e.printStackTrace();
      }

      try {
        parent.prefs.put("status_format_voice", status_format_voice.getText() ); 
      } catch(Exception e) {
        e.printStackTrace();
      }

      save_position();
      try {
        con_popout.dispose();
        si_popout.dispose();
        dvout.dispose();
      } catch(Exception e) {
        e.printStackTrace();
      }


         try {
           parent.alias_db.getCon().commit();
           parent.alias_db.getCon().close();
         } catch(Exception e) {
         }

         try {
           parent.talkgroups_db.getCon().commit();
           parent.talkgroups_db.getCon().close();
         } catch(Exception e) {
         }

         try {
           parent.events_db.getCon().commit();
           parent.events_db.getCon().close();
         } catch(Exception e) {
         }

    }//GEN-LAST:event_formWindowClosing

    public void clear_sys_info() {
      wacn.setText("");
      sysid.setText("");
      nac.setText("");
      nac.setText("");
      rfid.setText("");
      siteid.setText("");

    }

    private void en_bluetooth_cbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_en_bluetooth_cbActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_en_bluetooth_cbActionPerformed

    private void read_configActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_read_configActionPerformed
      do_read_config=1;
    }//GEN-LAST:event_read_configActionPerformed

    private void write_configActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_write_configActionPerformed
      do_read_config=1;
      do_write_config=1;


      //current_sys_id = 0;
      //current_wacn_id = 0; 

      clear_sys_info();

      try {
        if(prefs!=null) parent.prefs.put("status_format_cc", status_format_cc.getText() ); 
      } catch(Exception e) {
        e.printStackTrace();
      }

      try {
        if(prefs!=null) parent.prefs.put("status_format_voice", status_format_voice.getText() ); 
      } catch(Exception e) {
        e.printStackTrace();
      }

      try {
        save_position();
      } catch(Exception e) {
      }

    }//GEN-LAST:event_write_configActionPerformed

    private void lineout_vol_sliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_lineout_vol_sliderStateChanged
      volume_label.setText( String.format( "%3.2f", (float) lineout_vol_slider.getValue() / 100.0f ) );
    }//GEN-LAST:event_lineout_vol_sliderStateChanged

    private void log_taFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_log_taFocusGained
        // TODO add your handling code here:
    }//GEN-LAST:event_log_taFocusGained

    private void log_taKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_log_taKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_log_taKeyTyped

    private void logpanelFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_logpanelFocusGained
        // TODO add your handling code here:
    }//GEN-LAST:event_logpanelFocusGained

    private void jTable1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTable1KeyTyped
    }//GEN-LAST:event_jTable1KeyTyped

    private void tabbed_paneMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabbed_paneMouseClicked
      //System.out.println("evt tab");
      if( parent.tabbed_pane.getTitleAt(tabbed_pane.getSelectedIndex()).contains("Console")) {
        //System.out.println("evt tab");
        jTextArea1.requestFocus();
      }
    }//GEN-LAST:event_tabbed_paneMouseClicked

    private void enable_mp3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enable_mp3ActionPerformed
      if(prefs!=null) prefs.putBoolean("enable_mp3", enable_mp3.isSelected());
    }//GEN-LAST:event_enable_mp3ActionPerformed

    private void enable_audioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enable_audioActionPerformed
      if(prefs!=null) prefs.putBoolean("enable_audio", enable_audio.isSelected());
    }//GEN-LAST:event_enable_audioActionPerformed

    private void button_write_configActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_write_configActionPerformed
      do_read_config=1;
      do_write_config=1;

      //current_sys_id = 0;
      //current_wacn_id = 0; 
      wacn.setText("");
      sysid.setText("");
      nac.setText("");
    }//GEN-LAST:event_button_write_configActionPerformed


    private void autoscale_constActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoscale_constActionPerformed
      if(prefs!=null) prefs.putBoolean("autoscale_const", autoscale_const.isSelected());
    }//GEN-LAST:event_autoscale_constActionPerformed

    private void nsymbolsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nsymbolsActionPerformed
       if(prefs!=null) prefs.putInt("nsymbols", nsymbols.getSelectedIndex());
    }//GEN-LAST:event_nsymbolsActionPerformed


    private void mp3_separate_filesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mp3_separate_filesActionPerformed
     if(prefs!=null) prefs.putBoolean("mp3_separate_files", mp3_separate_files.isSelected());
    }//GEN-LAST:event_mp3_separate_filesActionPerformed

    private void audio_dev_listValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_audio_dev_listValueChanged
      try {
        String str = (String) audio_dev_list.getSelectedValue();
        if(str!=null) {
          if(prefs!=null) prefs.put("audio_output_device", str); 
          if(aud!=null) aud.dev_changed();
        }
      } catch(Exception e) {
        e.printStackTrace();
      }
    }//GEN-LAST:event_audio_dev_listValueChanged

    private void select_homeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_select_homeActionPerformed
      get_home_dir();
    }//GEN-LAST:event_select_homeActionPerformed

    private void formComponentMoved(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentMoved
      save_position();
    }//GEN-LAST:event_formComponentMoved

    private void import_aliasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_import_aliasActionPerformed
      do_alias_import=1;
    }//GEN-LAST:event_import_aliasActionPerformed

    private void triple_click_opt4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_triple_click_opt4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_triple_click_opt4ActionPerformed

    private void quad_click_opt4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quad_click_opt4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_quad_click_opt4ActionPerformed


    ///////////////////////////////////////
    ///////////////////////////////////////
    public void do_follow() {
      try {
        String cmd= new String("f\r\n");
        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      } catch(Exception e) {
        e.printStackTrace();
      }
    }
    ///////////////////////////////////////
    ///////////////////////////////////////
    public void do_skip() {
      try {
        String cmd= new String("s \r\n");
        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      } catch(Exception e) {
        e.printStackTrace();
      }
    }

    private void muteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_muteActionPerformed
      if(mute.isSelected()) {
        mute.setBackground(java.awt.Color.green);
      }
      else {
        mute.setBackground(java.awt.Color.gray);
      }
    }//GEN-LAST:event_muteActionPerformed


    private void if_slideStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_if_slideStateChanged
      String cmd= new String("if_freq "+if_slide.getValue()+"\r\n");
      serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      System.out.println(cmd);
    }//GEN-LAST:event_if_slideStateChanged

    private void sig_cmdKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_sig_cmdKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_sig_cmdKeyReleased

    private void do_mp3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_do_mp3ActionPerformed
        // TODO add your handling code here:
     if(prefs!=null) prefs.putBoolean("do_mp3", do_mp3.isSelected());
     if(prefs!=null) prefs.putBoolean("do_wav", !do_mp3.isSelected());
    }//GEN-LAST:event_do_mp3ActionPerformed

    private void do_wavActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_do_wavActionPerformed
        // TODO add your handling code here:
     if(prefs!=null) prefs.putBoolean("do_wav", do_wav.isSelected());
     if(prefs!=null) prefs.putBoolean("do_mp3", !do_wav.isSelected());
    }//GEN-LAST:event_do_wavActionPerformed

    private void audio_hiqActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_audio_hiqActionPerformed
        // TODO add your handling code here:
     if(prefs!=null) prefs.putBoolean("audio_hiq", audio_hiq.isSelected());
     if(prefs!=null) prefs.putBoolean("audio_lowq", !audio_hiq.isSelected());
    }//GEN-LAST:event_audio_hiqActionPerformed

    private void audio_lowqActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_audio_lowqActionPerformed
        // TODO add your handling code here:
     if(prefs!=null) prefs.putBoolean("audio_lowq", audio_lowq.isSelected());
     if(prefs!=null) prefs.putBoolean("audio_hiq", !audio_lowq.isSelected());
    }//GEN-LAST:event_audio_lowqActionPerformed

    private void edit_display_viewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_edit_display_viewActionPerformed
      dframe.setVisible(true);
    }//GEN-LAST:event_edit_display_viewActionPerformed

    private void edit_aliasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_edit_aliasActionPerformed
      edit_alias();
    }//GEN-LAST:event_edit_aliasActionPerformed

    private void holdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_holdActionPerformed
      do_follow();
    }//GEN-LAST:event_holdActionPerformed

    private void skipActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_skipActionPerformed
      do_skip();
    }//GEN-LAST:event_skipActionPerformed

    private void dvpopoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dvpopoutActionPerformed

      do_dv_popout();

    }//GEN-LAST:event_dvpopoutActionPerformed



    private void tglog_colorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglog_colorActionPerformed
      Color color = JColorChooser.showDialog(parent, "TG Font Color", tg_font_color); 
      if(color!=null) tg_font_color=color;
      if( parent.prefs!=null && color!=null) {
        parent.prefs.putInt("tg_font_color",  tg_font_color.getRGB() );
        log_ta.setForeground(color);
      }
    }//GEN-LAST:event_tglog_colorActionPerformed

    private void tglog_fontActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglog_fontActionPerformed

      jfc.setSelectedFontFamily(tg_font_name);
      jfc.setSelectedFontSize(tg_font_size);
      jfc.setSelectedFontStyle(tg_font_style);


      int result = jfc.showDialog(this);


      if( result == JFontChooser.OK_OPTION ) {
        tg_font_name = jfc.getSelectedFontFamily();
        tg_font_style = jfc.getSelectedFontStyle();
        tg_font_size = jfc.getSelectedFontSize();
        log_ta.setFont(new java.awt.Font(tg_font_name, tg_font_style, tg_font_size)); 
      }
      if(parent.prefs!=null) {
        parent.prefs.put("tg_font_name", jfc.getSelectedFontFamily() );
        parent.prefs.putInt("tg_font_style", jfc.getSelectedFontStyle() );
        parent.prefs.putInt("tg_font_size", jfc.getSelectedFontSize() );
      }


    }//GEN-LAST:event_tglog_fontActionPerformed

    private void tglog_editActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tglog_editActionPerformed
      tglog_e.setVisible(true);
    }//GEN-LAST:event_tglog_editActionPerformed

    private void hold1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hold1ActionPerformed
      do_follow();
    }//GEN-LAST:event_hold1ActionPerformed

    private void skip1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_skip1ActionPerformed
      do_skip();
      setStatus("skipping current TG");
    }//GEN-LAST:event_skip1ActionPerformed

    private void show_keywordsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_show_keywordsActionPerformed
        // TODO add your handling code here:
      dframe.show_help();
    }//GEN-LAST:event_show_keywordsActionPerformed

    private void edit_alias1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_edit_alias1ActionPerformed
      edit_alias();
    }//GEN-LAST:event_edit_alias1ActionPerformed

    private void si_cpu_highActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_si_cpu_highActionPerformed
        if(prefs!=null) prefs.put("si_cpu", "high");
    }//GEN-LAST:event_si_cpu_highActionPerformed

    private void si_cpu_normalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_si_cpu_normalActionPerformed
        if(prefs!=null) prefs.put("si_cpu", "normal");
    }//GEN-LAST:event_si_cpu_normalActionPerformed

    private void si_cpu_lowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_si_cpu_lowActionPerformed
        if(prefs!=null) prefs.put("si_cpu", "low");
    }//GEN-LAST:event_si_cpu_lowActionPerformed

    private void si_cpu_battery_savingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_si_cpu_battery_savingActionPerformed
        if(prefs!=null) prefs.put("si_cpu", "battery");
    }//GEN-LAST:event_si_cpu_battery_savingActionPerformed

    private void si_cpu_offActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_si_cpu_offActionPerformed
        if(prefs!=null) prefs.put("si_cpu", "off");
    }//GEN-LAST:event_si_cpu_offActionPerformed



//#define OP_MODE_P25 1
//#define OP_MODE_DMR 2
//#define OP_MODE_NXDN4800 3
//#define OP_MODE_FMNB 4
//#define OP_MODE_TDMA_CC 5
//#define OP_MODE_NXDN9600 6
//#define OP_MODE_AM 7
//#define OP_MODE_AM_AGC 8
    public void update_modes() {

      if( on_control_freq==1 ) {
        cc_ch.setBackground(java.awt.Color.green);
        traffic_ch.setBackground(java.awt.Color.gray);

        //need to make sure we are on this channel before doing this
        //if(!cc_control.isSelected()) cc_control.setSelected(true);
      }
      else {
        traffic_ch.setBackground(java.awt.Color.green);
        cc_ch.setBackground(java.awt.Color.gray);
      }

      if( mode_b ==1 || mode_b == 129) {
        if(p25_demod==0) current_mod_type = 0;
        if(p25_demod==1) current_mod_type = 1;
      }
      if(mode_b==2) current_mod_type=3;
      if(mode_b==3) current_mod_type=4;
      if(mode_b==4) current_mod_type=6;
      if(mode_b==5) current_mod_type=2;
      if(mode_b==6) current_mod_type=5;
      if(mode_b==7) current_mod_type=7;
      if(mode_b==8) current_mod_type=8;

      if( (mode_b&0x7f)==0x01) {
        if( p25_demod==0 ) {
          if( mode_b == 1 || mode_b == 129) mode_p25_lsm.setSelected(true);
            else mode_p25_lsm.setSelected(false);
          if(mode_p25_lsm.isSelected()) mode_p25_lsm.setBackground(java.awt.Color.green);
            else mode_p25_lsm.setBackground(java.awt.Color.gray);

          mode_p25_cqpsk.setBackground(java.awt.Color.gray);
          mode_p25_cqpsk.setSelected(false);
        }
        if( p25_demod==1 ) {
          if( mode_b == 1 || mode_b == 129) mode_p25_cqpsk.setSelected(true);
            else mode_p25_cqpsk.setSelected(false);
          if(mode_p25_cqpsk.isSelected()) mode_p25_cqpsk.setBackground(java.awt.Color.green);
            else mode_p25_cqpsk.setBackground(java.awt.Color.gray);

          mode_p25_lsm.setBackground(java.awt.Color.gray);
          mode_p25_lsm.setSelected(false);
        }
      }
      else {
        mode_p25_cqpsk.setBackground(java.awt.Color.gray);
        mode_p25_lsm.setBackground(java.awt.Color.gray);
      }

      if( mode_b == 2 ) mode_dmr.setSelected(true);
        else mode_dmr.setSelected(false);
      if(mode_dmr.isSelected()) mode_dmr.setBackground(java.awt.Color.green);
        else mode_dmr.setBackground(java.awt.Color.gray);

      if( mode_b == 3 ) mode_nxdn48.setSelected(true);
        else mode_nxdn48.setSelected(false);
      if(mode_nxdn48.isSelected()) mode_nxdn48.setBackground(java.awt.Color.green);
        else mode_nxdn48.setBackground(java.awt.Color.gray);

      if( mode_b == 4 ) mode_fm.setSelected(true);
        else mode_fm.setSelected(false);
      if(mode_fm.isSelected()) mode_fm.setBackground(java.awt.Color.green);
        else mode_fm.setBackground(java.awt.Color.gray);

      if( mode_b == 5 ) mode_tdmacc.setSelected(true);
        else mode_tdmacc.setSelected(false);
      if(mode_tdmacc.isSelected()) mode_tdmacc.setBackground(java.awt.Color.green);
        else mode_tdmacc.setBackground(java.awt.Color.gray);

      if( mode_b == 6 ) mode_nxdn96.setSelected(true);
        else mode_nxdn96.setSelected(false);
      if(mode_nxdn96.isSelected()) mode_nxdn96.setBackground(java.awt.Color.green);
        else mode_nxdn96.setBackground(java.awt.Color.gray);

      if( mode_b == 7 ) mode_am.setSelected(true);
        else mode_am.setSelected(false);
      if(mode_am.isSelected()) mode_am.setBackground(java.awt.Color.green);
        else mode_am.setBackground(java.awt.Color.gray);

      if( mode_b == 8 ) mode_am_agc.setSelected(true);
        else mode_am_agc.setSelected(false);
      if(mode_am_agc.isSelected()) mode_am_agc.setBackground(java.awt.Color.green);
        else mode_am_agc.setBackground(java.awt.Color.gray);
    }
    
    public void update_zones() {
      current_tgzone = current_tgzone_in;

      if( (current_tgzone_in&0x01)>0) z1.setSelected(true);
        else z1.setSelected(false);
      if( (current_tgzone_in&0x02)>0) z2.setSelected(true);
        else z2.setSelected(false);
      if( (current_tgzone_in&0x04)>0) z3.setSelected(true);
        else z3.setSelected(false);
      if( (current_tgzone_in&0x08)>0) z4.setSelected(true);
        else z4.setSelected(false);
      if( (current_tgzone_in&0x10)>0) z5.setSelected(true);
        else z5.setSelected(false);
      if( (current_tgzone_in&0x20)>0) z6.setSelected(true);
        else z6.setSelected(false);
      if( (current_tgzone_in&0x40)>0) z7.setSelected(true);
        else z7.setSelected(false);
      if( (current_tgzone_in&0x80)>0) z8.setSelected(true);
        else z8.setSelected(false);

      if( (current_tgzone_in&0x100)>0) z9.setSelected(true);
        else z9.setSelected(false);
      if( (current_tgzone_in&0x200)>0) z10.setSelected(true);
        else z10.setSelected(false);
      if( (current_tgzone_in&0x400)>0) z11.setSelected(true);
        else z11.setSelected(false);
      if( (current_tgzone_in&0x800)>0) z12.setSelected(true);
        else z12.setSelected(false);

      if( (current_tgzone_in&0x1000)>0) z13.setSelected(true);
        else z13.setSelected(false);
      if( (current_tgzone_in&0x2000)>0) z14.setSelected(true);
        else z14.setSelected(false);
      if( (current_tgzone_in&0x4000)>0) z15.setSelected(true);
        else z15.setSelected(false);
      if( (current_tgzone_in&0x8000)>0) z16.setSelected(true);
        else z16.setSelected(false);

      if(z1.isSelected()) {
        z1.setBackground(java.awt.Color.green);
      }
      else {
        z1.setBackground(java.awt.Color.gray);
      }
      if(z2.isSelected()) {
        z2.setBackground(java.awt.Color.green);
      }
      else {
        z2.setBackground(java.awt.Color.gray);
      }
      if(z3.isSelected()) {
        z3.setBackground(java.awt.Color.green);
      }
      else {
        z3.setBackground(java.awt.Color.gray);
      }
      if(z4.isSelected()) {
        z4.setBackground(java.awt.Color.green);
      }
      else {
        z4.setBackground(java.awt.Color.gray);
      }
      if(z5.isSelected()) {
        z5.setBackground(java.awt.Color.green);
      }
      else {
        z5.setBackground(java.awt.Color.gray);
      }
      if(z6.isSelected()) {
        z6.setBackground(java.awt.Color.green);
      }
      else {
        z6.setBackground(java.awt.Color.gray);
      }
      if(z7.isSelected()) {
        z7.setBackground(java.awt.Color.green);
      }
      else {
        z7.setBackground(java.awt.Color.gray);
      }
      if(z8.isSelected()) {
        z8.setBackground(java.awt.Color.green);
      }
      else {
        z8.setBackground(java.awt.Color.gray);
      }
      if(z9.isSelected()) {
        z9.setBackground(java.awt.Color.green);
      }
      else {
        z9.setBackground(java.awt.Color.gray);
      }
      if(z10.isSelected()) {
        z10.setBackground(java.awt.Color.green);
      }
      else {
        z10.setBackground(java.awt.Color.gray);
      }
      if(z11.isSelected()) {
        z11.setBackground(java.awt.Color.green);
      }
      else {
        z11.setBackground(java.awt.Color.gray);
      }
      if(z12.isSelected()) {
        z12.setBackground(java.awt.Color.green);
      }
      else {
        z12.setBackground(java.awt.Color.gray);
      }

      if(z13.isSelected()) {
        z13.setBackground(java.awt.Color.green);
      }
      else {
        z13.setBackground(java.awt.Color.gray);
      }
      if(z14.isSelected()) {
        z14.setBackground(java.awt.Color.green);
      }
      else {
        z14.setBackground(java.awt.Color.gray);
      }
      if(z15.isSelected()) {
        z15.setBackground(java.awt.Color.green);
      }
      else {
        z15.setBackground(java.awt.Color.gray);
      }
      if(z16.isSelected()) {
        z16.setBackground(java.awt.Color.green);
      }
      else {
        z16.setBackground(java.awt.Color.gray);
      }


    }

    private void z1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_z1ActionPerformed

      if(evt.getModifiers() == (InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK)) {
        button_config.setButton(1);
        button_config.setLabels("Zone Alias", "");
        button_config.setVisible(true);

        String zone_alias = button_config.getInput();
        if(prefs!=null && zone_alias!=null) prefs.put("zone1_alias", zone_alias); 
        z1.setToolTipText(zone_alias);
        return;
      }

      if(z1.isSelected()) current_tgzone |= 0x01;
        else current_tgzone &= (~0x01)&0xffff; 

      String cmd= "tgzone "+current_tgzone+"\r\n"; 
      serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);

      //update_zones();
    }//GEN-LAST:event_z1ActionPerformed

    private void z2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_z2ActionPerformed
      if(evt.getModifiers() == (InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK)) {
        button_config.setButton(2);
        button_config.setLabels("Zone Alias", "");
        button_config.setVisible(true);

        String zone_alias = button_config.getInput();
        if(prefs!=null && zone_alias!=null) prefs.put("zone2_alias", zone_alias); 
        z2.setToolTipText(zone_alias);
        return;
      }
      if(z2.isSelected()) current_tgzone |= 0x02;
        else current_tgzone &= (~0x02)&0xffff; 

      String cmd= "tgzone "+current_tgzone+"\r\n"; 
      serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      //update_zones();
    }//GEN-LAST:event_z2ActionPerformed

    private void z3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_z3ActionPerformed
      if(evt.getModifiers() == (InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK)) {
        button_config.setButton(3);
        button_config.setLabels("Zone Alias", "");
        button_config.setVisible(true);

        String zone_alias = button_config.getInput();
        if(prefs!=null && zone_alias!=null) prefs.put("zone3_alias", zone_alias); 
        z3.setToolTipText(zone_alias);
        return;
      }
      if(z3.isSelected()) current_tgzone |= 0x04;
        else current_tgzone &= (~0x04)&0xffff; 
      String cmd= "tgzone "+current_tgzone+"\r\n"; 
      serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      //update_zones();
    }//GEN-LAST:event_z3ActionPerformed

    private void z4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_z4ActionPerformed
      if(evt.getModifiers() == (InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK)) {
        button_config.setButton(4);
        button_config.setLabels("Zone Alias", "");
        button_config.setVisible(true);

        String zone_alias = button_config.getInput();
        if(prefs!=null && zone_alias!=null) prefs.put("zone4_alias", zone_alias); 
        z4.setToolTipText(zone_alias);
        return;
      }
      if(z4.isSelected()) current_tgzone |= 0x08;
        else current_tgzone &= (~0x08)&0xffff; 
      String cmd= "tgzone "+current_tgzone+"\r\n"; 
      serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      //update_zones();
    }//GEN-LAST:event_z4ActionPerformed

    private void z5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_z5ActionPerformed
      if(evt.getModifiers() == (InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK)) {
        button_config.setButton(5);
        button_config.setLabels("Zone Alias", "");
        button_config.setVisible(true);

        String zone_alias = button_config.getInput();
        if(prefs!=null && zone_alias!=null) prefs.put("zone5_alias", zone_alias); 
        z5.setToolTipText(zone_alias);
        return;
      }
      if(z5.isSelected()) current_tgzone |= 0x10;
        else current_tgzone &= (~0x10)&0xffff; 
      String cmd= "tgzone "+current_tgzone+"\r\n"; 
      serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      //update_zones();
    }//GEN-LAST:event_z5ActionPerformed

    private void z6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_z6ActionPerformed
      if(evt.getModifiers() == (InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK)) {
        button_config.setButton(6);
        button_config.setLabels("Zone Alias", "");
        button_config.setVisible(true);

        String zone_alias = button_config.getInput();
        if(prefs!=null && zone_alias!=null) prefs.put("zone6_alias", zone_alias); 
        z6.setToolTipText(zone_alias);
        return;
      }
      if(z6.isSelected()) current_tgzone |= 0x20;
        else current_tgzone &= (~0x20)&0xffff; 
      String cmd= "tgzone "+current_tgzone+"\r\n"; 
      serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      //update_zones();
    }//GEN-LAST:event_z6ActionPerformed

    private void z7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_z7ActionPerformed
      if(evt.getModifiers() == (InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK)) {
        button_config.setButton(7);
        button_config.setLabels("Zone Alias", "");
        button_config.setVisible(true);

        String zone_alias = button_config.getInput();
        if(prefs!=null && zone_alias!=null) prefs.put("zone7_alias", zone_alias); 
        z7.setToolTipText(zone_alias);
        return;
      }
      if(z7.isSelected()) current_tgzone |= 0x40;
        else current_tgzone &= (~0x40)&0xffff; 
      String cmd= "tgzone "+current_tgzone+"\r\n"; 
      serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      //update_zones();
    }//GEN-LAST:event_z7ActionPerformed

    private void z8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_z8ActionPerformed
      if(evt.getModifiers() == (InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK)) {
        button_config.setButton(8);
        button_config.setLabels("Zone Alias", "");
        button_config.setVisible(true);

        String zone_alias = button_config.getInput();
        if(prefs!=null && zone_alias!=null) prefs.put("zone8_alias", zone_alias); 
        z8.setToolTipText(zone_alias);
        return;
      }
      if(z8.isSelected()) current_tgzone |= 0x80;
        else current_tgzone &= (~0x80)&0xffff; 
      String cmd= "tgzone "+current_tgzone+"\r\n"; 
      serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      //update_zones();
    }//GEN-LAST:event_z8ActionPerformed

    private void z9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_z9ActionPerformed
      if(evt.getModifiers() == (InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK)) {
        button_config.setButton(9);
        button_config.setLabels("Zone Alias", "");
        button_config.setVisible(true);

        String zone_alias = button_config.getInput();
        if(prefs!=null && zone_alias!=null) prefs.put("zone9_alias", zone_alias); 
        z9.setToolTipText(zone_alias);
        return;
      }
      if(z9.isSelected()) current_tgzone |= 0x100;
        else current_tgzone &= (~0x100)&0xffff; 
      String cmd= "tgzone "+current_tgzone+"\r\n"; 
      serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      //update_zones();
    }//GEN-LAST:event_z9ActionPerformed

    private void z10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_z10ActionPerformed
      if(evt.getModifiers() == (InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK)) {
        button_config.setButton(10);
        button_config.setLabels("Zone Alias", "");
        button_config.setVisible(true);

        String zone_alias = button_config.getInput();
        if(prefs!=null && zone_alias!=null) prefs.put("zone10_alias", zone_alias); 
        z10.setToolTipText(zone_alias);
        return;
      }
      if(z10.isSelected()) current_tgzone |= 0x200;
        else current_tgzone &= (~0x200)&0xffff; 
      String cmd= "tgzone "+current_tgzone+"\r\n"; 
      serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      //update_zones();
    }//GEN-LAST:event_z10ActionPerformed

    private void z11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_z11ActionPerformed
      if(evt.getModifiers() == (InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK)) {
        button_config.setButton(11);
        button_config.setLabels("Zone Alias", "");
        button_config.setVisible(true);

        String zone_alias = button_config.getInput();
        if(prefs!=null && zone_alias!=null) prefs.put("zone11_alias", zone_alias); 
        z11.setToolTipText(zone_alias);
        return;
      }
      if(z11.isSelected()) current_tgzone |= 0x400;
        else current_tgzone &= (~0x400)&0xffff; 
      String cmd= "tgzone "+current_tgzone+"\r\n"; 
      serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      //update_zones();
    }//GEN-LAST:event_z11ActionPerformed

    private void z12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_z12ActionPerformed
      if(evt.getModifiers() == (InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK)) {
        button_config.setButton(12);
        button_config.setLabels("Zone Alias", "");
        button_config.setVisible(true);

        String zone_alias = button_config.getInput();
        if(prefs!=null && zone_alias!=null) prefs.put("zone12_alias", zone_alias); 
        z12.setToolTipText(zone_alias);
        return;
      }
      if(z12.isSelected()) current_tgzone |= 0x800;
        else current_tgzone &= (~0x800)&0xffff; 
      String cmd= "tgzone "+current_tgzone+"\r\n"; 
      serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      //update_zones();
    }//GEN-LAST:event_z12ActionPerformed

    private void status_format_ccActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_status_format_ccActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_status_format_ccActionPerformed

    private void freememActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_freememActionPerformed
      if(rt!=null) rt.gc();
    }//GEN-LAST:event_freememActionPerformed

    private void separate_ridActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_separate_ridActionPerformed
     if(prefs!=null) prefs.putBoolean("separate_rid", separate_rid.isSelected());
    }//GEN-LAST:event_separate_ridActionPerformed

    public void channel_change(channel_config cc) {

       int cc_mode = cc.modulation_type; 
       //set_freq now sets mode in firmware
       String mode = "";
       if(cc_mode==0) mode = "p25_2";
       if(cc_mode==1) mode = "p25";
       if(cc_mode==2) mode = "tdma_cc";
       if(cc_mode==3) mode = "dmr";
       if(cc_mode==4) mode = "nxdn4800";
       if(cc_mode==5) mode = "nxdn9600";
       if(cc_mode==6) mode = "fm";
       if(cc_mode==7) mode = "am";
       if(cc_mode==8) mode = "am_agc";

       set_mode(mode);

       if( cc.is_control ) set_control("1");
          else set_control("0");
       
       String freq = cc.getFreq();
       set_freq(freq);
       top_label.setText(cc.name);

       if( cc.squelch_enable ) {
         set_squelch( String.format("%d", cc.squelch_level)  );
       }

    }
    public void set_freq(String f) {
      try {
        String cmd= new String("freq "+f+"\r\n");
        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
        Thread.sleep(5);
        //byte[] result=new byte[64];
        //int rlen=serial_port.readBytes( result, 64);
        //Thread.sleep(10);
      } catch(Exception e) {
        e.printStackTrace();
      }
    }
    public void set_mode(String m) {
      try {
        String cmd= new String("mode "+m+"\r\n");
        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
        Thread.sleep(5);
        //byte[] result=new byte[64];
        //int rlen=serial_port.readBytes( result, 64);
        //Thread.sleep(50);
      } catch(Exception e) {
        e.printStackTrace();
      }
    }
    public void set_squelch(String c) {
      try {
        String cmd= new String("squelch "+c+"\r\n");
        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
        Thread.sleep(5);
        //byte[] result=new byte[64];
        //int rlen=serial_port.readBytes( result, 64);
        //Thread.sleep(50);
      } catch(Exception e) {
        e.printStackTrace();
      }
    }
    public void set_control(String c) {
      try {
        String cmd= new String("is_control "+c+"\r\n");
        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
        Thread.sleep(5);
        //byte[] result=new byte[64];
        //int rlen=serial_port.readBytes( result, 64);
        //Thread.sleep(50);
      } catch(Exception e) {
        e.printStackTrace();
      }
    }

    private void prev_freqActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prev_freqActionPerformed
      if(browser!=null && freq_to==0) {
        browser.prev_freq();
        freq_to=2;
      }
    }//GEN-LAST:event_prev_freqActionPerformed

    private void next_freqActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_next_freqActionPerformed
      if(browser!=null && freq_to==0) {
        browser.next_freq();
        freq_to=2;
      }
    }//GEN-LAST:event_next_freqActionPerformed


    private void add_ridActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_add_ridActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_add_ridActionPerformed

    private void pause_roamingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pause_roamingActionPerformed
      try {
        int state = 2;
        if( pause_roaming.isSelected() ) state=0;

        String cmd= new String("roaming "+state+"\r\n");
        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
        Thread.sleep(10);
        //cmd= new String("squelch -120\r\n");
        //serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);


        if(state!=0) {
          setStatus("roaming on");
          pause_roaming.setBackground( Color.green );
        }
        if(state==0) {
          setStatus("roaming off");
          pause_roaming.setBackground( Color.gray );
        }
      } catch(Exception e) {
        e.printStackTrace();
      }
    }//GEN-LAST:event_pause_roamingActionPerformed

    private void en_evt_outputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_en_evt_outputActionPerformed
     if(prefs!=null) prefs.putBoolean("en_evt_output", en_evt_output.isSelected());
    }//GEN-LAST:event_en_evt_outputActionPerformed

    private void mode_p25_lsmActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mode_p25_lsmActionPerformed
      try {
        String cmd= new String("mode p25\r\n");
        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
        Thread.sleep(10);
        cmd= new String("demod 0\r\n");
        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      } catch(Exception e) {
        e.printStackTrace();
      }
    }//GEN-LAST:event_mode_p25_lsmActionPerformed

    private void mode_dmrActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mode_dmrActionPerformed
      try {
        String cmd= new String("mode dmr\r\n");
        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      } catch(Exception e) {
        e.printStackTrace();
      }
    }//GEN-LAST:event_mode_dmrActionPerformed

    private void mode_nxdn48ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mode_nxdn48ActionPerformed
      try {
        String cmd= new String("mode nxdn4800\r\n");
        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      } catch(Exception e) {
        e.printStackTrace();
      }
    }//GEN-LAST:event_mode_nxdn48ActionPerformed

    private void mode_nxdn96ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mode_nxdn96ActionPerformed
      try {
        String cmd= new String("mode nxdn9600\r\n");
        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      } catch(Exception e) {
        e.printStackTrace();
      }
    }//GEN-LAST:event_mode_nxdn96ActionPerformed

    private void mode_tdmaccActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mode_tdmaccActionPerformed
      try {
        String cmd= new String("mode tdma_cc\r\n");
        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      } catch(Exception e) {
        e.printStackTrace();
      }
    }//GEN-LAST:event_mode_tdmaccActionPerformed

    private void mode_fmActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mode_fmActionPerformed
      try {
        String cmd= new String("mode fm\r\n");
        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      } catch(Exception e) {
        e.printStackTrace();
      }
    }//GEN-LAST:event_mode_fmActionPerformed

    private void mode_amActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mode_amActionPerformed
      try {
        String cmd= new String("mode am\r\n");
        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
        //Thread.sleep(10);
        //cmd= new String("squelch -140\r\n");
        //serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      } catch(Exception e) {
        e.printStackTrace();
      }
    }//GEN-LAST:event_mode_amActionPerformed

    private void mode_am_agcActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mode_am_agcActionPerformed
      try {
        String cmd= new String("mode am_agc\r\n");
        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
        //Thread.sleep(10);
        //cmd= new String("squelch -140\r\n");
        //serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      } catch(Exception e) {
        e.printStackTrace();
      }
    }//GEN-LAST:event_mode_am_agcActionPerformed

    private void mode_p25_cqpskActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mode_p25_cqpskActionPerformed
      try {
        String cmd= new String("mode p25\r\n");
        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
        Thread.sleep(10);
        cmd= new String("demod 1\r\n");
        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      } catch(Exception e) {
        e.printStackTrace();
      }
    }//GEN-LAST:event_mode_p25_cqpskActionPerformed

    private void z13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_z13ActionPerformed
      if(evt.getModifiers() == (InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK)) {
        button_config.setButton(13);
        button_config.setLabels("Zone Alias", "");
        button_config.setVisible(true);

        String zone_alias = button_config.getInput();
        if(prefs!=null && zone_alias!=null) prefs.put("zone13_alias", zone_alias); 
        z13.setToolTipText(zone_alias);
        return;
      }
      if(z13.isSelected()) current_tgzone |= 0x1000;
        else current_tgzone &= (~0x1000)&0xffff; 
      String cmd= "tgzone "+current_tgzone+"\r\n"; 
      serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      //update_zones();
    }//GEN-LAST:event_z13ActionPerformed

    private void z14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_z14ActionPerformed
      if(evt.getModifiers() == (InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK)) {
        button_config.setButton(14);
        button_config.setLabels("Zone Alias", "");
        button_config.setVisible(true);

        String zone_alias = button_config.getInput();
        if(prefs!=null && zone_alias!=null) prefs.put("zone14_alias", zone_alias); 
        z14.setToolTipText(zone_alias);
        return;
      }
      if(z14.isSelected()) current_tgzone |= 0x2000;
        else current_tgzone &= (~0x2000)&0xffff; 
      String cmd= "tgzone "+current_tgzone+"\r\n"; 
      serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      //update_zones();
    }//GEN-LAST:event_z14ActionPerformed

    private void z15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_z15ActionPerformed
      if(evt.getModifiers() == (InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK)) {
        button_config.setButton(15);
        button_config.setLabels("Zone Alias", "");
        button_config.setVisible(true);

        String zone_alias = button_config.getInput();
        if(prefs!=null && zone_alias!=null) prefs.put("zone15_alias", zone_alias); 
        z15.setToolTipText(zone_alias);
        return;
      }
      if(z15.isSelected()) current_tgzone |= 0x4000;
        else current_tgzone &= (~0x4000)&0xffff; 
      String cmd= "tgzone "+current_tgzone+"\r\n"; 
      serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      //update_zones();
    }//GEN-LAST:event_z15ActionPerformed

    private void z16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_z16ActionPerformed
      if(evt.getModifiers() == (InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK)) {
        button_config.setButton(16);
        button_config.setLabels("Zone Alias", "");
        button_config.setVisible(true);

        String zone_alias = button_config.getInput();
        if(prefs!=null && zone_alias!=null) prefs.put("zone16_alias", zone_alias); 
        z16.setToolTipText(zone_alias);
        return;
      }
      if(z16.isSelected()) current_tgzone |= 0x8000;
        else current_tgzone &= (~0x8000)&0xffff; 
      String cmd= "tgzone "+current_tgzone+"\r\n"; 
      serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      //update_zones();
    }//GEN-LAST:event_z16ActionPerformed

    private void adv_write_configActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_adv_write_configActionPerformed
        do_read_config=1;
        do_write_config=1;

        current_sys_id = 0;
        current_wacn_id = 0;
        wacn.setText("");
        sysid.setText("");
        nac.setText("");
    }//GEN-LAST:event_adv_write_configActionPerformed

    private void p25_tone_volActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_p25_tone_volActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_p25_tone_volActionPerformed

    private void en_zero_ridActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_en_zero_ridActionPerformed
        if(prefs!=null) prefs.putBoolean("en_zero_rid", en_zero_rid.isSelected());
    }//GEN-LAST:event_en_zero_ridActionPerformed

    private void enc_modeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enc_modeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_enc_modeActionPerformed

    private void reset_defaultsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reset_defaultsActionPerformed
        String cmd= new String("factory\r\n");
        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
        setStatus("Reset config to factory defaults");
    }//GEN-LAST:event_reset_defaultsActionPerformed

    private void vga_targetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vga_targetActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_vga_targetActionPerformed

    private void vga_stepActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vga_stepActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_vga_stepActionPerformed

    private void save_iqActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_save_iqActionPerformed
        try {

            FileNameExtensionFilter filter = new FileNameExtensionFilter( "BTT IQ file", "biq");
            chooser.setFileFilter(filter);
            int returnVal = chooser.showDialog(parent, "Save BTT .iq file");

            if(returnVal == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                fos_iq = new FileOutputStream(file);
                save_iq_len = 5242880;
                iq_out=0;

                int avail = serial_port.bytesAvailable();
                byte[] b = new byte[avail];
                int len = serial_port.readBytes(b, avail);

                String cmd= new String("send_iq 30\r\n");
                serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                setStatus("Saving IQ File For 30 Seconds");
            }

        } catch(Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_save_iqActionPerformed

    private void enable_table_rowsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enable_table_rowsActionPerformed
        int[] rows = jTable1.getSelectedRows();
        if(rows.length>0) {
            for(int i=0;i<rows.length;i++) {
                jTable1.getModel().setValueAt(true,jTable1.convertRowIndexToModel(rows[i]),0);
                System.out.println("row "+i);
            }
        }
        try {
          update_talkgroup_selected();
        } catch(Exception e) {
        }
    }//GEN-LAST:event_enable_table_rowsActionPerformed

    private void disable_table_rowsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disable_table_rowsActionPerformed
        int[] rows = jTable1.getSelectedRows();
        if(rows.length>0) {
            for(int i=0;i<rows.length;i++) {
                jTable1.getModel().setValueAt(false,jTable1.convertRowIndexToModel(rows[i]),0);
                System.out.println("row "+i);
            }
        }
        try {
          update_talkgroup_selected();
        } catch(Exception e) {
        }
    }//GEN-LAST:event_disable_table_rowsActionPerformed

    private void read_tgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_read_tgActionPerformed
        do_read_talkgroups=1;
    }//GEN-LAST:event_read_tgActionPerformed

    private void send_tgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_send_tgActionPerformed
        do_update_talkgroups=1;
    }//GEN-LAST:event_send_tgActionPerformed

    private void backup_tgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backup_tgActionPerformed
        do_talkgroup_backup=1;
    }//GEN-LAST:event_backup_tgActionPerformed

    private void tg_edit_delActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tg_edit_delActionPerformed
        delete_talkgroup_rows();
    }//GEN-LAST:event_tg_edit_delActionPerformed

    private void set_zonesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_set_zonesActionPerformed
        int zone = Integer.parseInt( JOptionPane.showInputDialog((JFrame) this,
            "Zone # (1-8)",
            "[Zone Number?]",
            JOptionPane.INFORMATION_MESSAGE) );

        int[] rows = jTable1.getSelectedRows();
        if(rows.length>0) {
            for(int i=0;i<rows.length;i++) {
                jTable1.getModel().setValueAt(new Integer(zone),jTable1.convertRowIndexToModel(rows[i]),8);
            }
        }
        try {
          update_talkgroup_selected();
        } catch(Exception e) {
        }
    }//GEN-LAST:event_set_zonesActionPerformed

    private void import_csvActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_import_csvActionPerformed
        if(is_connected==0) do_connect();
        do_restore_tg_csv=1;
    }//GEN-LAST:event_import_csvActionPerformed

    private void auto_flash_tgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_auto_flash_tgActionPerformed
        if(prefs!=null) prefs.putBoolean( "tg_auto_flash", auto_flash_tg.isSelected());
    }//GEN-LAST:event_auto_flash_tgActionPerformed

    private void disable_encryptedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disable_encryptedActionPerformed
        if(prefs!=null) prefs.putBoolean( "enc_auto_flash", disable_encrypted.isSelected());
    }//GEN-LAST:event_disable_encryptedActionPerformed

    private void auto_pop_tableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_auto_pop_tableActionPerformed
        if(prefs!=null) prefs.putBoolean( "tg_auto_pop_table", auto_pop_table.isSelected());
    }//GEN-LAST:event_auto_pop_tableActionPerformed

    private void write_cfg_siActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_write_cfg_siActionPerformed
      do_read_config=1;
      do_write_config=1;


      //current_sys_id = 0;
      //current_wacn_id = 0; 

      clear_sys_info();

      try {
        if(prefs!=null) parent.prefs.put("status_format_cc", status_format_cc.getText() ); 
      } catch(Exception e) {
        e.printStackTrace();
      }

      try {
        if(prefs!=null) parent.prefs.put("status_format_voice", status_format_voice.getText() ); 
      } catch(Exception e) {
        e.printStackTrace();
      }

      try {
        save_position();
      } catch(Exception e) {
      }        // TODO add your handling code here:
    }//GEN-LAST:event_write_cfg_siActionPerformed

    private void cc_chActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cc_chActionPerformed
      String cmd= new String("is_control 1\r\n");
      serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
    }//GEN-LAST:event_cc_chActionPerformed

    private void traffic_chActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_traffic_chActionPerformed
      String cmd= new String("is_control 0\r\n");
      serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
    }//GEN-LAST:event_traffic_chActionPerformed

    private void write_config_tgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_write_config_tgActionPerformed
      do_read_config=1;
      do_write_config=1;


      //current_sys_id = 0;
      //current_wacn_id = 0; 

      clear_sys_info();

      try {
        if(prefs!=null) parent.prefs.put("status_format_cc", status_format_cc.getText() ); 
      } catch(Exception e) {
        e.printStackTrace();
      }

      try {
        if(prefs!=null) parent.prefs.put("status_format_voice", status_format_voice.getText() ); 
      } catch(Exception e) {
        e.printStackTrace();
      }

      try {
        save_position();
      } catch(Exception e) {
      }
    }//GEN-LAST:event_write_config_tgActionPerformed

    private void ccfg_add_digital_chActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ccfg_add_digital_chActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ccfg_add_digital_chActionPerformed

    private void ccfg_p1controlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ccfg_p1controlActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ccfg_p1controlActionPerformed

    private void ccfg_p1voiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ccfg_p1voiceActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ccfg_p1voiceActionPerformed

    private void ccfg_dmr_controlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ccfg_dmr_controlActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ccfg_dmr_controlActionPerformed

    private void ccfg_dmr_voiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ccfg_dmr_voiceActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ccfg_dmr_voiceActionPerformed

    private void ccfg_nxdn_voiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ccfg_nxdn_voiceActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ccfg_nxdn_voiceActionPerformed

    private void ccfg_analog_enActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ccfg_analog_enActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ccfg_analog_enActionPerformed

    private void ccfg_start_discoverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ccfg_start_discoverActionPerformed
      start_discover();
    }//GEN-LAST:event_ccfg_start_discoverActionPerformed

    private void ccfg_add_analog_ch1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ccfg_add_analog_ch1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ccfg_add_analog_ch1ActionPerformed

    private void ccfg_search_abortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ccfg_search_abortActionPerformed
     try {
      ccfg_prog.setValue(0);
      String ch_search = "cc_search_abort\r\n";
      serial_port.writeBytes( ch_search.getBytes(), ch_search.length(), 0);
     } catch(Exception e) {
       e.printStackTrace();
     }
    }//GEN-LAST:event_ccfg_search_abortActionPerformed

    private void ccfg_ch_bandActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ccfg_ch_bandActionPerformed
      update_discover_bands();
    }//GEN-LAST:event_ccfg_ch_bandActionPerformed

    private void ccfg_ch_spActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ccfg_ch_spActionPerformed
      try {
        String sp = (String) ccfg_ch_sp.getSelectedItem();
        StringTokenizer st = new StringTokenizer(sp, " ");
        String chsp = st.nextToken();
        ccfg_ch_step.setText(chsp);
        ch_step_lb.setText("CH STEP "+chsp+" kHz  ");
      } catch(Exception e) {
      }
    }//GEN-LAST:event_ccfg_ch_spActionPerformed

    private void slider_squelchStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_slider_squelchStateChanged

      Integer val = new Integer(slider_squelch.getValue());

      if(slider_squelch.isEnabled()) {
        setStatus( String.format( "Squelch: %d", val.intValue() )); 
        String cmd = "squelch "+val.toString()+"\r\n";
        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      }

      slider_val.setText(val.toString());
    }//GEN-LAST:event_slider_squelchStateChanged

    private void freq_9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_freq_9ActionPerformed
    }//GEN-LAST:event_freq_9ActionPerformed

    private void freq_8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_freq_8ActionPerformed
      try {
        System.out.println(evt);
      } catch(Exception e) {
      }
    }//GEN-LAST:event_freq_8ActionPerformed

    private void freq_7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_freq_7ActionPerformed
    }//GEN-LAST:event_freq_7ActionPerformed

    private void freq_6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_freq_6ActionPerformed
    }//GEN-LAST:event_freq_6ActionPerformed

    private void freq_5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_freq_5ActionPerformed
    }//GEN-LAST:event_freq_5ActionPerformed

    private void freq_4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_freq_4ActionPerformed
    }//GEN-LAST:event_freq_4ActionPerformed

    private void freq_3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_freq_3ActionPerformed
    }//GEN-LAST:event_freq_3ActionPerformed

    private void freq_2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_freq_2ActionPerformed
    }//GEN-LAST:event_freq_2ActionPerformed

    private void freq_1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_freq_1ActionPerformed
    }//GEN-LAST:event_freq_1ActionPerformed

    ////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////
    public String freq_next_down(String f, double step) {
      try {
        double val = Double.valueOf(f);
        if( sys_config.is_valid_freq(val) ) return f; //done

        while(val >= 25.0) {
          val -= step;
          if( sys_config.is_valid_freq(val) ) return String.format("%3.5f", val);
        }


      } catch(Exception e) {
      }

      return "25.0";
    }

    ////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////
    public String freq_next_up(String f, double step) {
      try {
        double val = Double.valueOf(f);
        if( sys_config.is_valid_freq(val) ) return f; //done

        while(val <= 1300.0) {
          val += step;
          if( sys_config.is_valid_freq(val) ) return String.format("%3.5f", val);
        }


      } catch(Exception e) {
      }
      return "1300.0";
    }

    public void step_down() {
      try {
        String step_val = ccfg_ch_step.getText(); 
        double step = Double.valueOf( step_val ) /1e3;
        String fval = String.format("%3.5f", current_freq/1e6-step);
        fval = freq_next_down(fval, step);
        String cmd = "freq "+fval+"\r\n";
        //System.out.println(cmd);
        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      } catch(Exception e) {
      }
    }
    private void stepdownfreq_5MouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_stepdownfreq_5MouseWheelMoved
      double val = evt.getPreciseWheelRotation();
      if(val>0.0) step_down();
      if(val<0.0) step_up();
    }//GEN-LAST:event_stepdownfreq_5MouseWheelMoved

    private void stepdownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stepdownActionPerformed
      step_down();
    }//GEN-LAST:event_stepdownActionPerformed

    public void step_up() {
      try {
        String step_val = ccfg_ch_step.getText(); 
        double step = Double.valueOf( step_val ) /1e3;
        String fval = String.format("%3.5f", current_freq/1e6+step);
        fval = freq_next_up(fval, step);
        String cmd = "freq "+fval+"\r\n";
        //System.out.println(cmd);
        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      } catch(Exception e) {
      }
    }
    private void stepupfreq_5MouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_stepupfreq_5MouseWheelMoved
      double val = evt.getPreciseWheelRotation();
      if(val>0.0) step_down();
      if(val<0.0) step_up();
    }//GEN-LAST:event_stepupfreq_5MouseWheelMoved

    private void stepupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stepupActionPerformed
      step_up();
    }//GEN-LAST:event_stepupActionPerformed

    private void freq_8MouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_freq_8MouseWheelMoved
      try {
        //System.out.println(evt);
        double val = evt.getPreciseWheelRotation();
        String fval="";
        if(val<0.0) fval = String.format("%3.5f", (current_freq+100e6)/1e6);
        if(val>0.0) fval = String.format("%3.5f", (current_freq-100e6)/1e6);

        String step_val = ccfg_ch_step.getText(); 
        double step = Double.valueOf( step_val ) /1e3;
        if(val<0.0) fval = freq_next_up(fval, step);
        if(val>0.0) fval = freq_next_down(fval, step);

        String cmd = "freq "+fval+"\r\n";
        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      } catch(Exception e) {
      }
    }//GEN-LAST:event_freq_8MouseWheelMoved

    private void freq_7MouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_freq_7MouseWheelMoved
      try {
        //System.out.println(evt);
        double val = evt.getPreciseWheelRotation();
        String fval="";
        if(val<0.0) fval = String.format("%3.5f", (current_freq+10e6)/1e6);
        if(val>0.0) fval = String.format("%3.5f", (current_freq-10e6)/1e6);

        String step_val = ccfg_ch_step.getText(); 
        double step = Double.valueOf( step_val ) /1e3;
        if(val<0.0) fval = freq_next_up(fval, step);
        if(val>0.0) fval = freq_next_down(fval, step);

        String cmd = "freq "+fval+"\r\n";
        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      } catch(Exception e) {
      }
    }//GEN-LAST:event_freq_7MouseWheelMoved

    private void freq_6MouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_freq_6MouseWheelMoved
      try {
        //System.out.println(evt);
        double val = evt.getPreciseWheelRotation();
        String fval="";
        if(val<0.0) fval = String.format("%3.5f", (current_freq+1e6)/1e6);
        if(val>0.0) fval = String.format("%3.5f", (current_freq-1e6)/1e6);

        String step_val = ccfg_ch_step.getText(); 
        double step = Double.valueOf( step_val ) /1e3;
        if(val<0.0) fval = freq_next_up(fval, step);
        if(val>0.0) fval = freq_next_down(fval, step);

        String cmd = "freq "+fval+"\r\n";
        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      } catch(Exception e) {
      }
    }//GEN-LAST:event_freq_6MouseWheelMoved

    private void freq_5MouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_freq_5MouseWheelMoved
      try {
        //System.out.println(evt);
        double val = evt.getPreciseWheelRotation();
        String fval="";
        if(val<0.0) fval = String.format("%3.5f", (current_freq+0.1e6)/1e6);
        if(val>0.0) fval = String.format("%3.5f", (current_freq-0.1e6)/1e6);

        String step_val = ccfg_ch_step.getText(); 
        double step = Double.valueOf( step_val ) /1e3;
        if(val<0.0) fval = freq_next_up(fval, step);
        if(val>0.0) fval = freq_next_down(fval, step);

        String cmd = "freq "+fval+"\r\n";
        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      } catch(Exception e) {
      }
    }//GEN-LAST:event_freq_5MouseWheelMoved

    private void freq_4MouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_freq_4MouseWheelMoved
      try {
        //System.out.println(evt);
        double val = evt.getPreciseWheelRotation();
        String fval="";
        if(val<0.0) fval = String.format("%3.5f", (current_freq+0.01e6)/1e6);
        if(val>0.0) fval = String.format("%3.5f", (current_freq-0.01e6)/1e6);

        String step_val = ccfg_ch_step.getText(); 
        double step = Double.valueOf( step_val ) /1e3;
        if(val<0.0) fval = freq_next_up(fval, step);
        if(val>0.0) fval = freq_next_down(fval, step);

        String cmd = "freq "+fval+"\r\n";
        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      } catch(Exception e) {
      }
    }//GEN-LAST:event_freq_4MouseWheelMoved

    private void freq_3MouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_freq_3MouseWheelMoved
      try {
        //System.out.println(evt);
        double val = evt.getPreciseWheelRotation();
        String fval="";
        if(val<0.0) fval = String.format("%3.5f", (current_freq+0.001e6)/1e6);
        if(val>0.0) fval = String.format("%3.5f", (current_freq-0.001e6)/1e6);

        String step_val = ccfg_ch_step.getText(); 
        double step = Double.valueOf( step_val ) /1e3;
        if(val<0.0) fval = freq_next_up(fval, step);
        if(val>0.0) fval = freq_next_down(fval, step);

        String cmd = "freq "+fval+"\r\n";
        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      } catch(Exception e) {
      }
    }//GEN-LAST:event_freq_3MouseWheelMoved

    private void freq_2MouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_freq_2MouseWheelMoved
      try {
        //System.out.println(evt);
        double val = evt.getPreciseWheelRotation();
        String fval="";
        if(val<0.0) fval = String.format("%3.5f", (current_freq+0.0001e6)/1e6);
        if(val>0.0) fval = String.format("%3.5f", (current_freq-0.0001e6)/1e6);

        String step_val = ccfg_ch_step.getText(); 
        double step = Double.valueOf( step_val ) /1e3;
        if(val<0.0) fval = freq_next_up(fval, step);
        if(val>0.0) fval = freq_next_down(fval, step);

        String cmd = "freq "+fval+"\r\n";
        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      } catch(Exception e) {
      }
    }//GEN-LAST:event_freq_2MouseWheelMoved

    private void freq_1MouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_freq_1MouseWheelMoved
      try {
        //System.out.println(evt);
        double val = evt.getPreciseWheelRotation();
        String fval="";
        if(val<0.0) fval = String.format("%3.5f", (current_freq+0.00001e6)/1e6);
        if(val>0.0) fval = String.format("%3.5f", (current_freq-0.00001e6)/1e6);

        String step_val = ccfg_ch_step.getText(); 
        double step = Double.valueOf( step_val ) /1e3;
        if(val<0.0) fval = freq_next_up(fval, step);
        if(val>0.0) fval = freq_next_down(fval, step);

        String cmd = "freq "+fval+"\r\n";
        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      } catch(Exception e) {
      }
    }//GEN-LAST:event_freq_1MouseWheelMoved

    private void squelch_setActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_squelch_setActionPerformed
      slider_squelch.setValue(current_rssi+3);
    }//GEN-LAST:event_squelch_setActionPerformed

    private void slider_squelchMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_slider_squelchMouseWheelMoved
      double val = evt.getPreciseWheelRotation();
      String fval="";
      int cval = slider_squelch.getValue();
      if(val<0.0) slider_squelch.setValue(cval+1);
      if(val>0.0) slider_squelch.setValue(cval-1);

      user_squelch_level = slider_squelch.getValue();
    }//GEN-LAST:event_slider_squelchMouseWheelMoved

    private void ccfg_search_pauseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ccfg_search_pauseActionPerformed
        String cmd = "cc_pause\r\n";
        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
    }//GEN-LAST:event_ccfg_search_pauseActionPerformed

    private void ch_step_lbMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_ch_step_lbMouseWheelMoved
      //if( chsp_d == 0.00500 ) ccfg_ch_sp.setSelectedIndex(0);
      //if( chsp_d == 0.00625 ) ccfg_ch_sp.setSelectedIndex(1);
      //if( chsp_d == 0.00833 ) ccfg_ch_sp.setSelectedIndex(2);
      //if( chsp_d == 0.01000 ) ccfg_ch_sp.setSelectedIndex(3);
      //if( chsp_d == 0.01250 ) ccfg_ch_sp.setSelectedIndex(4);
      //if( chsp_d == 0.02000 ) ccfg_ch_sp.setSelectedIndex(5);
      //if( chsp_d == 0.02500 ) ccfg_ch_sp.setSelectedIndex(6);
      try {
        int idx = ccfg_ch_sp.getSelectedIndex();
        double val = evt.getPreciseWheelRotation();
        if(val<0.0 && idx<6) idx++;
        if(val>0.0 && idx>0) idx--;
        ccfg_ch_sp.setSelectedIndex(idx);
      } catch (Exception e) {
      }


    }//GEN-LAST:event_ch_step_lbMouseWheelMoved

    public void add_freq(String f) {
        double f1 = Double.valueOf(f); 
      /*
        if( !cl.isDuplicate(String.format("%3.5f",f1)) ) {
          channel_config cc = new channel_config(null, f1);
          int m_type = 0;
          if( mode_p25_lsm.isSelected() ) m_type = 0;
          if( mode_p25_cqpsk.isSelected() ) m_type = 1;
          if( mode_tdmacc.isSelected() ) m_type = 2;
          if( mode_dmr.isSelected() ) m_type = 3;
          if( mode_nxdn48.isSelected() ) m_type = 4;
          if( mode_nxdn96.isSelected() ) m_type = 5;
          if( mode_fm.isSelected() ) m_type = 6;
          if( mode_am.isSelected() ) m_type = 7;
          if( mode_am_agc.isSelected() ) m_type = 8;
          cc.modulation_type = m_type;
        }
      */
    }


    //////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////
    public void channel_config_update_from_gui(channel_config cc) {
      try {
        cc.enable_pl_tone_filter = pl_tone_filter.isSelected(); 
        try {
          cc.pl_tone_freq = Float.valueOf( pl_tone_freq.getText() ); 
        } catch(Exception e) {
          cc.pl_tone_freq=0.0f;
        }
        cc.do_scan = cc_do_scan.isSelected(); 
        cc.use_on_power_up = cc_use_on_powerup.isSelected(); 
        cc.name = cc_name.getText();
        cc.agency = cc_agency.getText();
        cc.frequency = Double.valueOf( cc_frequency.getText() );
        cc.modulation_type = cc_modulation.getSelectedIndex();
        cc.is_control = cc_control.isSelected(); 
        cc.is_conventional = cc_conventional.isSelected(); 
        cc.lna_gain = cc_rfgain.getSelectedIndex(); 
        cc.mgain = cc_mgain.getSelectedIndex(); 
        cc.vga_gain = cc_vga_gain.getSelectedIndex(); 

        cc.squelch_enable = ccfg_analog_en.isSelected(); 
        try {
          cc.squelch_level = Integer.valueOf( ccfg_squelch_level.getText() ); 
        } catch(Exception e) {
        }

        cc.add_p25_traffic = auto_add_p25_traffic_ch.isSelected(); 
        cc.add_p25_secondary = auto_add_p25_secondaries.isSelected(); 
        cc.add_p25_neighbor = auto_add_p25_neighbors.isSelected(); 
        try {
          String hex = ccfg_p25_wacn.getText().trim();
          if(hex.startsWith("0x")) hex = hex.substring(2,hex.length());
          cc.p25_wacn = Integer.valueOf( hex, 16 );
        } catch(Exception e) {
          cc.p25_wacn=0;
        }
        try {
          String hex = ccfg_p25_sysid.getText().trim();
          if(hex.startsWith("0x")) hex = hex.substring(2,hex.length());
          cc.p25_sysid = Integer.valueOf( hex, 16 );
        } catch(Exception e) {
          cc.p25_sysid=0;
        }
        try {
          String hex = ccfg_p25_nac.getText().trim();
          if(hex.startsWith("0x")) hex = hex.substring(2,hex.length());
          cc.p25_nac = Integer.valueOf( hex, 16 );
        } catch(Exception e) {
          cc.p25_nac=0;
        }
        cc.p25_p1 = ccfg_p25_p1.isSelected(); 
        cc.p25_p2 = ccfg_p25_p2.isSelected(); 

        try {
          cc.dmr_lcn = Integer.valueOf( ccfg_dmr_lcn.getText() ); 
        } catch(Exception e) {
        }
        try {
          cc.site_id = Integer.valueOf( ccfg_site_id.getText() ); 
        } catch(Exception e) {
        }

        cc.en_acars = en_acars.isSelected();
        cc.en_flex32 = en_flex32.isSelected();
        cc.en_pocsag12 = en_pocsag12.isSelected();

        cc.install_in_flash = cc_install_to_flash.isSelected();

      } catch(Exception e) {
        e.printStackTrace();
      }
    }
    //////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////
    public void channel_config_update_gui(channel_config cc) {
      try {

        //config_path.setText( "path: "+cc.config_path );

        if( cc.county!=null && cc.county.length()>0) {
          county_lb.setText("County: "+cc.county);
        }

        pl_tone_filter.setSelected(cc.enable_pl_tone_filter);
        pl_tone_freq.setText( String.format("%3.1f", cc.pl_tone_freq) );

        cc_do_scan.setSelected(cc.do_scan); 
        cc_use_on_powerup.setSelected(cc.use_on_power_up); 

        cc_name.setText( cc.name );
        cc_agency.setText( cc.agency );
        cc_frequency.setText( cc.getFreq() );
        cc_modulation.setSelectedIndex(cc.modulation_type);

        cc_control.setSelected(cc.is_control); 
        cc_conventional.setSelected(cc.is_conventional); 
        cc_rfgain.setSelectedIndex(cc.lna_gain); 
        cc_mgain.setSelectedIndex(cc.mgain); 
        cc_vga_gain.setSelectedIndex(cc.vga_gain); 

        ccfg_analog_en.setSelected(cc.squelch_enable); 

        if(ccfg_analog_en.isSelected()) {
          //slider_squelch.setEnabled(false);
          //slider_squelch.setValue( cc.squelch_level ); 
        }
        else {
          //slider_squelch.setEnabled(true);
          //slider_squelch.setValue( user_squelch_level ); 
        }
        try {
          ccfg_squelch_level.setText( String.format("%d", cc.squelch_level) );
        } catch(Exception e) {
        }

        auto_add_p25_traffic_ch.setSelected(cc.add_p25_traffic); 
        auto_add_p25_secondaries.setSelected(cc.add_p25_secondary); 
        auto_add_p25_neighbors.setSelected(cc.add_p25_neighbor); 
        try {
          ccfg_p25_wacn.setText(String.format("0x%05X", cc.p25_wacn) );
        } catch(Exception e) {
        }
        try {
          ccfg_p25_sysid.setText(String.format("0x%03X", cc.p25_sysid) );
        } catch(Exception e) {
        }
        try {
          ccfg_p25_nac.setText(String.format("0x%03X", cc.p25_nac) );
        } catch(Exception e) {
        }
        ccfg_p25_p1.setSelected(cc.p25_p1); 
        ccfg_p25_p2.setSelected(cc.p25_p2); 

        try {
          ccfg_dmr_lcn.setText( String.format("%d", cc.dmr_lcn) );
        } catch(Exception e) {
        }
        try {
          ccfg_site_id.setText( String.format("%d", cc.site_id) );
        } catch(Exception e) {
        }

        en_acars.setSelected(cc.en_acars);
        en_flex32.setSelected(cc.en_flex32);
        en_pocsag12.setSelected(cc.en_pocsag12);

        cc_install_to_flash.setSelected(cc.install_in_flash);

        current_cc = cc;

        ccfg_changes=0;

      } catch(Exception e) {
        e.printStackTrace();
        current_cc = null;
      }
    }

    public void read_channel_configs(boolean do_def_squelch) {
      try {

        /*
        String fs =  System.getProperty("file.separator");
        String dir = document_dir+fs+sys_mac_id+fs+"channel_config";
        ObjectInputStream ois = new ObjectInputStream( new FileInputStream(dir) );

        //}

        while(ois.available()>0) { 
          channel_config cc = new channel_config(null,0.0); 
          cc.read_config(ois);

          if(do_def_squelch) {
            cc.squelch_enable = true; 
            cc.squelch_level = -120; 
          }

          cl.addElement(cc); 
        }

        ois.close();

        ccfg_changes=0;
        */


      } catch(Exception e) {
        e.printStackTrace();
      }
    }

    public void write_channel_configs() {
      try {
        /*
        Object[] o = ((DefaultListModel) ccfg_freq_list.getModel()).toArray();
        if(o==null) return;

        String fs =  System.getProperty("file.separator");
        String dir = document_dir+fs+sys_mac_id+fs+"channel_config";
        ObjectOutputStream oos = new ObjectOutputStream( new FileOutputStream(dir) );

        for(int i=0;i<o.length;i++) {
          channel_config cc = (channel_config) o[i];
          cc.write_config(oos);
        }

        oos.close();


        setStatus("Channel Config Saved To Local File");
        */
      } catch(Exception e) {
        e.printStackTrace();
      }
    }

    private void cc_syncActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cc_syncActionPerformed
      do_cc_write=1;
    }//GEN-LAST:event_cc_syncActionPerformed

    private void cc_importActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cc_importActionPerformed
      /*
      try {
        if( rr_import != null ) {
          rr_import = null; 
        }
        rr_import = new rr_importer.RR_import(this);
        java.awt.EventQueue.invokeLater( rr_import );
      } catch(Exception e) {
      }
      */
      if(rr_import!=null) rr_import.setVisible(true);
    }//GEN-LAST:event_cc_importActionPerformed

    private void cc_applyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cc_applyActionPerformed
      try {

        if( current_cc!=null && freq_to==0) {
          cc_sync.setBackground( Color.green );
          channel_config_update_from_gui(current_cc);
          ObjectOutputStream oos = new ObjectOutputStream( new FileOutputStream(current_cc.config_path) );
          current_cc.write_config(oos);
          oos.close();
          channel_change(current_cc);
          ccfg_changes=0;
          freq_to=2;
        }


      } catch(Exception e) {
        e.printStackTrace();
      }
    }//GEN-LAST:event_cc_applyActionPerformed

    private void cc_vga_gainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cc_vga_gainActionPerformed
      ccfg_changes=1;
    }//GEN-LAST:event_cc_vga_gainActionPerformed

    private void cc_mgainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cc_mgainActionPerformed
      ccfg_changes=1;
    }//GEN-LAST:event_cc_mgainActionPerformed

    private void cc_rfgainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cc_rfgainActionPerformed
      ccfg_changes=1;
    }//GEN-LAST:event_cc_rfgainActionPerformed

    private void cc_conventionalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cc_conventionalActionPerformed
      ccfg_changes=1;
    }//GEN-LAST:event_cc_conventionalActionPerformed

    private void cc_controlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cc_controlActionPerformed
      ccfg_changes=1;
    }//GEN-LAST:event_cc_controlActionPerformed

    private void cc_modulationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cc_modulationActionPerformed
      ccfg_changes=1;
    }//GEN-LAST:event_cc_modulationActionPerformed

    private void cc_frequencyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cc_frequencyActionPerformed
      ccfg_changes=1;
    }//GEN-LAST:event_cc_frequencyActionPerformed

    private void cc_nameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cc_nameActionPerformed
      ccfg_changes=1;
    }//GEN-LAST:event_cc_nameActionPerformed

    private void cc_use_on_powerupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cc_use_on_powerupActionPerformed
      try {
        /*
        int idx = ccfg_freq_list.getSelectedIndex();

        Object[] o = ((DefaultListModel) ccfg_freq_list.getModel()).toArray();
        if(o==null || o.length==0) return;

        for(int i=0;i<o.length;i++) {
          channel_config cc = (channel_config) o[i];
          if(i==idx) cc.use_on_power_up=true;
            else cc.use_on_power_up=false;
        }

        ccfg_changes=1;
        */
      } catch(Exception e) {
        e.printStackTrace();
      }
    }//GEN-LAST:event_cc_use_on_powerupActionPerformed

    private void cc_do_scanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cc_do_scanActionPerformed
      ccfg_changes=1;
      cc_install_to_flash.setSelected(true);
    }//GEN-LAST:event_cc_do_scanActionPerformed

    private void ccfg_squelch_levelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ccfg_squelch_levelActionPerformed
      ccfg_changes=1;
    }//GEN-LAST:event_ccfg_squelch_levelActionPerformed

    private void en_acarsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_en_acarsActionPerformed
      ccfg_changes=1;
    }//GEN-LAST:event_en_acarsActionPerformed

    private void en_flex32ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_en_flex32ActionPerformed
      ccfg_changes=1;
    }//GEN-LAST:event_en_flex32ActionPerformed

    private void en_pocsag12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_en_pocsag12ActionPerformed
      ccfg_changes=1;
    }//GEN-LAST:event_en_pocsag12ActionPerformed

    private void sipopoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sipopoutActionPerformed
      do_si_popout();
    }//GEN-LAST:event_sipopoutActionPerformed

    private void consolepopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_consolepopActionPerformed
      do_con_popout();
    }//GEN-LAST:event_consolepopActionPerformed


    public void do_si_popout() {
      try {
        if(do_read_talkgroups==1) return;
        tabbed_pane.remove(signalinsightpanel);
        si_popout.add(signalinsightpanel, java.awt.BorderLayout.CENTER);
        si_popout.setVisible(true);
      } catch(Exception e) {
        e.printStackTrace();
      }
    }
    public void do_con_popout() {
      try {
        if(do_read_talkgroups==1) return;
        tabbed_pane.remove(consolePanel);
        con_popout.add(consolePanel, java.awt.BorderLayout.CENTER);
        con_popout.setVisible(true);
      } catch(Exception e) {
        e.printStackTrace();
      }
    }
    public void do_dv_popout() {
      try {
        if(do_read_talkgroups==1) return;

        displayviewmain_border.remove(display_frame);
        dvout.split_top.add(display_frame, java.awt.BorderLayout.CENTER);
        displayviewmain_border.repaint();

        logpanel.remove(tg_scroll_pane);
        dvout.split_bottom.add(tg_scroll_pane, java.awt.BorderLayout.CENTER);

        dvout.setVisible(true);
      } catch(Exception e) {
        e.printStackTrace();
      }
    }

    private void ccfg_p25_p1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ccfg_p25_p1ActionPerformed
      ccfg_changes=1;
    }//GEN-LAST:event_ccfg_p25_p1ActionPerformed

    private void ccfg_p25_sysidKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ccfg_p25_sysidKeyTyped
      ccfg_changes=1;
    }//GEN-LAST:event_ccfg_p25_sysidKeyTyped

    private void ccfg_p25_p2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ccfg_p25_p2ActionPerformed
      ccfg_changes=1;
    }//GEN-LAST:event_ccfg_p25_p2ActionPerformed

    private void cc_nameKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cc_nameKeyTyped
      ccfg_changes=1;
    }//GEN-LAST:event_cc_nameKeyTyped

    private void cc_frequencyKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cc_frequencyKeyTyped
      ccfg_changes=1;
    }//GEN-LAST:event_cc_frequencyKeyTyped

    private void auto_add_p25_traffic_chActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_auto_add_p25_traffic_chActionPerformed
      ccfg_changes=1;
    }//GEN-LAST:event_auto_add_p25_traffic_chActionPerformed

    private void auto_add_p25_secondariesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_auto_add_p25_secondariesActionPerformed
      ccfg_changes=1;
    }//GEN-LAST:event_auto_add_p25_secondariesActionPerformed

    private void auto_add_p25_neighborsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_auto_add_p25_neighborsActionPerformed
      ccfg_changes=1;
    }//GEN-LAST:event_auto_add_p25_neighborsActionPerformed

    private void write_config_globalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_write_config_globalActionPerformed
      do_read_config=1;
      do_write_config=1;


      //current_sys_id = 0;
      //current_wacn_id = 0; 

      clear_sys_info();

      try {
        if(prefs!=null) parent.prefs.put("status_format_cc", status_format_cc.getText() ); 
      } catch(Exception e) {
        e.printStackTrace();
      }

      try {
        if(prefs!=null) parent.prefs.put("status_format_voice", status_format_voice.getText() ); 
      } catch(Exception e) {
        e.printStackTrace();
      }

      try {
        save_position();
      } catch(Exception e) {
      }
    }//GEN-LAST:event_write_config_globalActionPerformed

    private void find_fixed_gainsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_find_fixed_gainsActionPerformed
      do_fixed_gain=1;
      do_fixed_gain_state=0;
    }//GEN-LAST:event_find_fixed_gainsActionPerformed

    private void popout_allActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_popout_allActionPerformed
      do_dv_popout();
      do_si_popout();
      do_con_popout();
    }//GEN-LAST:event_popout_allActionPerformed

    private void set_gains_autoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_set_gains_autoActionPerformed
      /*
      int idx = ccfg_freq_list.getSelectedIndex();
      channel_config cc = (channel_config) ((DefaultListModel) ccfg_freq_list.getModel()).getElementAt(idx);
      cc.lna_gain = 0; 
      cc.mgain = 0; 
      cc.vga_gain = 0; 

      channel_config_update_gui(cc);
      */
    }//GEN-LAST:event_set_gains_autoActionPerformed

    private void console_dec_no_audioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_console_dec_no_audioActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_console_dec_no_audioActionPerformed

    private void console_fontActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_console_fontActionPerformed

      jfc.setSelectedFontFamily(con_font_name);
      jfc.setSelectedFontSize(con_font_size);
      jfc.setSelectedFontStyle(con_font_style);

      int result = jfc.showDialog(this);

      if( result == JFontChooser.OK_OPTION ) {
        con_font_name = jfc.getSelectedFontFamily();
        con_font_style = jfc.getSelectedFontStyle();
        con_font_size = jfc.getSelectedFontSize();
        jTextArea1.setFont(new java.awt.Font(con_font_name, con_font_style, con_font_size)); 
      }
      if(parent.prefs!=null) {
        parent.prefs.put("con_font_name", jfc.getSelectedFontFamily() );
        parent.prefs.putInt("con_font_style", jfc.getSelectedFontStyle() );
        parent.prefs.putInt("con_font_size", jfc.getSelectedFontSize() );
      }

      jfc.setSelectedFontFamily(con_font_name);
      jfc.setSelectedFontSize(con_font_size);
      jfc.setSelectedFontStyle(con_font_style);

    }//GEN-LAST:event_console_fontActionPerformed

    private void console_colorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_console_colorActionPerformed

      Color color = JColorChooser.showDialog(parent, "Console Font Color", con_font_color); 
      if(color!=null) con_font_color=color;
      if( parent.prefs!=null && color!=null) {
        parent.prefs.putInt("con_font_color",  con_font_color.getRGB() );
        jTextArea1.setForeground(color);
      }

    }//GEN-LAST:event_console_colorActionPerformed

    private void pl_tone_filterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pl_tone_filterActionPerformed
      ccfg_changes=1;
    }//GEN-LAST:event_pl_tone_filterActionPerformed

    private void cc_agencyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cc_agencyActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cc_agencyActionPerformed

    private void cc_agencyKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cc_agencyKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_cc_agencyKeyTyped

    private void browser_view_modeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browser_view_modeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_browser_view_modeActionPerformed

    private void reset_to_defaultsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reset_to_defaultsActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_reset_to_defaultsActionPerformed

    private void cc_install_to_flashActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cc_install_to_flashActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cc_install_to_flashActionPerformed

    private void formKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyTyped
      System.out.println(evt);
    }//GEN-LAST:event_formKeyTyped

    private void en_rdioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_en_rdioActionPerformed
      try {
        prefs.putBoolean("en_rdio", en_rdio.isSelected());
      } catch(Exception e) {
        e.printStackTrace();
      }
    }//GEN-LAST:event_en_rdioActionPerformed

    private void rdio_maskActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdio_maskActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_rdio_maskActionPerformed

    private void select_systemsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_select_systemsActionPerformed
        update_systems_path(true);
    }//GEN-LAST:event_select_systemsActionPerformed

    private void refresh_treeviewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refresh_treeviewActionPerformed
      do_rescan=true;
    }//GEN-LAST:event_refresh_treeviewActionPerformed

    private void update_alias_tableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_update_alias_tableActionPerformed
      try {
        alias_db.alias_update_from_table();
      } catch(Exception e) {
      }
    }//GEN-LAST:event_update_alias_tableActionPerformed

    private void export_aliasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_export_aliasActionPerformed
      do_save_alias=1;
    }//GEN-LAST:event_export_aliasActionPerformed

    private void sort_aliasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sort_aliasActionPerformed
      try {
        alias_db.update_alias_table();
      } catch(Exception e) {
      }
    }//GEN-LAST:event_sort_aliasActionPerformed

    private void sort_ridActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sort_ridActionPerformed
      try {
        alias_db.update_alias_table();
      } catch(Exception e) {
      }
    }//GEN-LAST:event_sort_ridActionPerformed

    private void update_selected_tgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_update_selected_tgActionPerformed
      update_tg_sel=1;
    }//GEN-LAST:event_update_selected_tgActionPerformed


    private void enable_tg_table_updatesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enable_tg_table_updatesActionPerformed
      try {
        talkgroups_db.update_talkgroup_table();
      } catch(Exception e) {
      }
    }//GEN-LAST:event_enable_tg_table_updatesActionPerformed

    private void tg_sortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tg_sortActionPerformed
        talkgroups_db.update_talkgroup_table(); 
    }//GEN-LAST:event_tg_sortActionPerformed

    private void tg_duplicateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tg_duplicateActionPerformed
      duplicate_tg_row();
    }//GEN-LAST:event_tg_duplicateActionPerformed

    private void en_broadcastify_callsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_en_broadcastify_callsActionPerformed
      try {
        prefs.putBoolean("en_bcalls", en_broadcastify_calls.isSelected());
      } catch(Exception e) {
        e.printStackTrace();
      }
    }//GEN-LAST:event_en_broadcastify_callsActionPerformed

    private void popout_ontopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_popout_ontopActionPerformed
      try {
        parent.prefs.putBoolean("popout_ontop", popout_ontop.isSelected() );
        update_ontop();
      } catch(Exception e) {
        e.printStackTrace();
      }
    }//GEN-LAST:event_popout_ontopActionPerformed

    private void del_aliasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_del_aliasActionPerformed
        delete_alias_rows();
    }//GEN-LAST:event_del_aliasActionPerformed

    private void enable_event_dbaseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enable_event_dbaseActionPerformed
      try {
        parent.prefs.putBoolean("enable_event_db", enable_event_dbase.isSelected() );
      } catch(Exception e) {
        e.printStackTrace();
      }
    }//GEN-LAST:event_enable_event_dbaseActionPerformed

    private void enable_alias_dbaseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enable_alias_dbaseActionPerformed
      try {
        parent.prefs.putBoolean("enable_alias_db", enable_alias_dbase.isSelected() );
      } catch(Exception e) {
        e.printStackTrace();
      }
    }//GEN-LAST:event_enable_alias_dbaseActionPerformed


///////////////////////////////////////////////////
///////////////////////////////////////////////////
void update_discover_bands() {

  try {
    int idx = ccfg_ch_band.getSelectedIndex();
    if(idx==0) return;

    String band = (String) ccfg_ch_band.getSelectedItem();
    if(band==null) return;

    StringTokenizer st = new StringTokenizer(band," ");

    String st_freq = st.nextToken();
    st.nextToken();
    String end_freq = st.nextToken();

    String mode = st.nextToken();
    String chsp = st.nextToken();

    double chsp_d = Double.valueOf(chsp) / 1e3;
    double f_st = Double.valueOf(st_freq);
    double f_end = Double.valueOf(end_freq);
    double span = f_end - f_st;

    ccfg_st_freq.setText( String.format( "%3.6f", f_st ) );
    ccfg_end_freq.setText( String.format( "%3.6f", f_end ) );
    ccfg_ch_step.setText( String.format( "%3.3f", (chsp_d*1e3) ) );


    //"5 kHz", "6.25 kHz", "8.33 kHz", "10 kHz", "12.5 kHz", "20 kHz", "25 kHz" }));
    if( chsp_d == 0.00500 ) ccfg_ch_sp.setSelectedIndex(0);
    if( chsp_d == 0.00625 ) ccfg_ch_sp.setSelectedIndex(1);
    if( chsp_d == 0.00833 ) ccfg_ch_sp.setSelectedIndex(2);
    if( chsp_d == 0.01000 ) ccfg_ch_sp.setSelectedIndex(3);
    if( chsp_d == 0.01250 ) ccfg_ch_sp.setSelectedIndex(4);
    if( chsp_d == 0.02000 ) ccfg_ch_sp.setSelectedIndex(5);
    if( chsp_d == 0.02500 ) ccfg_ch_sp.setSelectedIndex(6);

  } catch(Exception e) {
  }

}
///////////////////////////////////////////////////
///////////////////////////////////////////////////
void start_discover() {
 try {
  ccfg_prog.setValue(0);


  double chsp_d = Double.valueOf(ccfg_ch_step.getText()) / 1e3;
  double f_st = Double.valueOf(ccfg_st_freq.getText());
  double f_end = Double.valueOf(ccfg_end_freq.getText());

  int span_int = (int) ((f_end - f_st)/chsp_d);
  span_int++;

  double span = (double) ((int) span_int * chsp_d);


  String start = String.format("%3.5f", f_st);
  String ch_span = String.format("%3.5f", chsp_d);
  String span_str = String.format("%3.5f", span);

  int iterations = Integer.valueOf( ccfg_iterations.getText() ); 
  iterations--;

  if(iterations<0) iterations=0;

  String dwell_time = "25";
  int dwell_idx = ccfg_step_dwell.getSelectedIndex();
  if(dwell_idx==0) dwell_time="25";
  if(dwell_idx==1) dwell_time="50";
  if(dwell_idx==2) dwell_time="100";
  if(dwell_idx==3) dwell_time="250";
  if(dwell_idx==4) dwell_time="500";
  if(dwell_idx==5) dwell_time="1000";
  if(dwell_idx==6) dwell_time="2000";
  if(dwell_idx==7) dwell_time="5000";

  String max_analyze_time = "250";
  int max_analyze_idx = ccfg_max_analyze_time.getSelectedIndex();
  if(max_analyze_idx==0) max_analyze_time="250";
  if(max_analyze_idx==1) max_analyze_time="500";
  if(max_analyze_idx==2) max_analyze_time="1000";
  if(max_analyze_idx==3) max_analyze_time="3000";
  if(max_analyze_idx==4) max_analyze_time="10000";
  if(max_analyze_idx==5) max_analyze_time="100000";

  String ch_search = "cc_search "+start+" "+ch_span+" "+span_str+" "+dwell_time+" "+iterations+" "+max_analyze_time+"\r\n";

  System.out.println(":"+ch_search+":");

  serial_port.writeBytes( ch_search.getBytes(), ch_search.length(), 0);

 } catch(Exception e) {
   e.printStackTrace();
 }
}

///////////////////////////////////////////////////
///////////////////////////////////////////////////
public void edit_alias() {

  if(alias==null) alias = new Alias(parent, parent.sys_mac_id, document_dir);
  if(alias_dialog==null) alias_dialog = new aliasEntry(alias);

  if(alias_dialog!=null && src_uid!=0) {
    alias_dialog.setRID( src_uid );
    alias_dialog.setVisible(true);
  }
}

//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
public void do_meta() {

  if(is_roaming!=2 || mode_b==0 || mode_b==1) {

    if(current_talkgroup==null || Integer.valueOf(current_talkgroup)==0) {
      //did_metadata=1;
      return;
    }
  }

  if( is_roaming!=2 && is_control==1 && mode_b==1) { 
    if(current_wacn_id==0 || current_wacn_id!=traffic_wacn_id) {
      //did_metadata=1;
      return;
    }
    if(current_sys_id==0 || current_sys_id!=traffic_sys_id) {
      //did_metadata=1;
      return;
    }
  }

  if(is_enc==0) {

    if(did_metadata==1) return;

    if( is_dmr_mode==0 && src_uid==0 && !en_zero_rid.isSelected() ) {
      did_metadata=1;
      return;
    }
  }

  //is_enc==1 always gets here
  //String enc="";
  //if(is_enc==1) enc="ENCRYPTED,";
  is_enc=0;

    try {

        String log_format = tglog_e.getFormat();

        //String log_str = "\r\n"+dframe.do_subs(log_format,true)+","+enc;
      String log_str = "\r\n"+dframe.do_subs(log_format,true);

        String date = formatter_date.format(new java.util.Date() );
        current_date=new String(date);  //date changed

        try {
          if(fos_meta!=null) fos_meta.close();
        } catch(Exception e) {
          e.printStackTrace();
        }

        try {
          String fs =  System.getProperty("file.separator");
          meta_file = new File(document_dir+fs+sys_mac_id+fs+"p25rx_recmeta_"+current_date+".txt");
          fos_meta = new FileOutputStream( meta_file, true ); 
        } catch(Exception e) {
          e.printStackTrace();
        }


      try {
        //fos_meta.write(log_str.getBytes(),0,log_str.length());  //write int num records
        log_str = log_str.trim()+"\r\n";
        fos_meta.write(log_str.getBytes(),0,log_str.length());  //write int num records
        fos_meta.flush();
        fos_meta.close();
      } catch(Exception e) {
        e.printStackTrace();
      }

      log_str = "\r\n"+log_str.trim();
      String text = log_ta.getText().trim();

      log_ta.setText(text.concat( new String(log_str.getBytes()) ).trim()+"\n");

      /*
      if( log_ta.getText().length() > 16000 ) {
        String new_text = text.substring(8000,text.length()-1);
        log_ta.setText(new_text.trim()+"\n");
      }
      */
      if( log_ta.getText().length() > 64000 ) {
        log_ta.replaceRange("",0,32000);
      }


      log_ta.setCaretPosition(log_ta.getText().length());
      log_ta.getCaret().setVisible(true);
      log_ta.getCaret().setBlinkRate(250);

      tg_scroll_pane.getHorizontalScrollBar().setValue(0);

      did_metadata=1;
      tg_pri=0;

      prev_uid = src_uid;
      if(is_dmr_mode==0) src_uid=0;
    } catch(Exception e) {
      e.printStackTrace();
    }
}

//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
public void open_audio_output_files() {
  try {
    String fs =  System.getProperty("file.separator");

    Path path = Paths.get(home_dir+fs+sys_mac_id);
    Files.createDirectories(path);

    home_dir_label.setText(home_dir);

    String date = formatter_date.format(new java.util.Date() );
    current_date=new String(date);  //date changed

    meta_file = new File(document_dir+fs+sys_mac_id+fs+"p25rx_recmeta_"+current_date+".txt");
    String exe_path = getClass().getProtectionDomain().getCodeSource().getLocation().getPath().toString();
    exe_path = exe_path.replace("BTConfig.exe", "");
    System.out.println("log file path: "+exe_path+"p25rx_conlog_"+current_date+".txt");
    fos_meta = new FileOutputStream( meta_file, true ); 

  } catch(Exception e) {
    e.printStackTrace();
    StringWriter sw = new StringWriter();
    e.printStackTrace(new PrintWriter(sw));
    JOptionPane.showMessageDialog(this, sw.toString(), "ok", JOptionPane.OK_OPTION);
  }


  try {
    if(aud==null && parent!=null) {
      aud = new audio(parent);
    }
    if(aud!=null) aud.dev_changed();
    if(aud!=null) aud.audio_tick();
  } catch(Exception e) {
    e.printStackTrace();
    StringWriter sw = new StringWriter();
    e.printStackTrace(new PrintWriter(sw));
    JOptionPane.showMessageDialog(this, sw.toString(), "ok", JOptionPane.OK_OPTION);
  }
}
//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
public void update_systems_path(boolean show) {
  try {
      JFileChooser chooser_systems = new JFileChooser();
      chooser_systems.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

      if(show) {
        String cur_dir=null;
        if(prefs!=null) cur_dir = prefs.get("systems_home_dir", "");
        if(cur_dir!=null && cur_dir.length()>0) chooser_systems.setCurrentDirectory( new File(cur_dir) ); 
        int returnVal = chooser_systems.showDialog(parent, "Select Systems Folder (/p25rx/systems/ will be added to the end)");
        if(returnVal != JFileChooser.APPROVE_OPTION) return; 
      }


      if( prefs!=null ) {
        if(show) {
          File file = chooser_systems.getSelectedFile();  //better for windows to do it this way
          String fs =  System.getProperty("file.separator");
          String home_dir_str = file.getAbsolutePath();
            System.out.println("home dir: "+home_dir_str);

          prefs.put("systems_home_dir", home_dir_str);

          String default_systems_path = home_dir_str+fs+"p25rx"+fs+"systems";

          prefs.put("systems_path", default_systems_path);
          systems_path = default_systems_path;
            System.out.println("systems_path: "+systems_path);
        }
        else {
          File file = chooser_systems.getCurrentDirectory();  //better for windows to do it this way
          String fs =  System.getProperty("file.separator");
          String home_dir_str = file.getAbsolutePath();
            System.out.println("home dir: "+home_dir_str);
          String default_systems_path = home_dir_str+fs+"p25rx"+fs+"systems";

          systems_path = prefs.get("systems_path", default_systems_path);
            System.out.println("systems_path: "+systems_path);
          //if(!systems_path.contains("p25rx"+fs+"systems")) {
           // systems_path = default_systems_path; 
          //}
        }
      }

      Path path = Paths.get(new File(systems_path).getAbsolutePath() );
      try {
        Files.createDirectories(path);
      } catch(Exception e) {
        e.printStackTrace();
      }
      systems_path_str.setText( path.toString() );

      if(browser!=null && show) {
        //do_rescan=true;
        do_rescan=true;
      }
      //System.out.println("path:"+path);
  } catch(Exception e) {
  }

}
//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
public void update_prefs() {

  try {
    //if(prefs==null) prefs = Preferences.userRoot().node(this.getClass().getName()+"_"+sys_mac_id);
    System.out.println("sys_mac_id: "+sys_mac_id);
    if(prefs==null) prefs = Preferences.userRoot().node("adv"+sys_mac_id);



    if( !prefs.getBoolean("did_new_agc1", false) ) {
      prefs.putInt("agc_gain", 50);
      prefs.putBoolean("did_new_agc1", true);
    }
    //agc_gain.setValue(65);
    do_agc_update=1;

    if(prefs!=null) {
      int i = prefs.getInt("audio_buffer_system",1);

      try {
        if(dframe!=null) dframe.update_colors();
      } catch(Exception e) {
      }
      try {
        tglog_e = new tglog_editor(this);
      } catch(Exception e) {
      }

      en_evt_output.setSelected( prefs.getBoolean("en_evt_output", true) );
      auto_flash_tg.setSelected( prefs.getBoolean("tg_auto_flash", false) );
      auto_pop_table.setSelected( prefs.getBoolean("tg_auto_pop_table", true) );
      disable_encrypted.setSelected( prefs.getBoolean("enc_auto_flash", false) );
      autoscale_const.setSelected( prefs.getBoolean("autoscale_const", true) );
      nsymbols.setSelectedIndex( prefs.getInt("nsymbols", 0) );

      enable_alias_dbase.setSelected( prefs.getBoolean("enable_alias_db", true) );
      enable_event_dbase.setSelected( prefs.getBoolean("enable_event_db", true) );

      en_zero_rid.setSelected( prefs.getBoolean("en_zero_rid", true) );


      enable_mp3.setSelected( prefs.getBoolean("enable_mp3", true) ); 
      enable_audio.setSelected( prefs.getBoolean("enable_audio", true) ); 
      mp3_separate_files.setSelected( prefs.getBoolean("mp3_separate_files", false) );
      separate_rid.setSelected( prefs.getBoolean("separate_rid", false) );

      do_mp3.setSelected( prefs.getBoolean("do_mp3", true) );
      do_wav.setSelected( prefs.getBoolean("do_wav", false) );
      audio_hiq.setSelected( prefs.getBoolean("audio_hiq", false) );
      audio_lowq.setSelected( prefs.getBoolean("audio_lowq", true) );

      end_call_silence.setText( prefs.get("end_call_silence", "0") );

      int constellation = prefs.getInt("const_select", 1);
    }

      JFileChooser chooser = new JFileChooser();
      File file = chooser.getCurrentDirectory();  //better for windows to do it this way
      String fs =  System.getProperty("file.separator");
      String home_dir_str = file.getAbsolutePath()+fs;

      document_dir = home_dir_str+"p25rx";

      home_dir = prefs.get("p25rx_home_dir", home_dir_str+"p25rx");
      home_dir_label.setText(home_dir);
      System.out.println("home_dir: "+home_dir);

        try {
          Path path = Paths.get(new File(home_dir+fs+sys_mac_id).getAbsolutePath() );
          Files.createDirectories(path);
        } catch(Exception e) {
          e.printStackTrace();
        }

        try {
          Path path = Paths.get(new File(home_dir+fs+"broadcastify_calls").getAbsolutePath() );
          Files.createDirectories(path);
          broadcastify_calls_dir = path.toString();
        } catch(Exception e) {
          e.printStackTrace();
        }

        try {
          Path path = Paths.get(new File(document_dir+fs+sys_mac_id).getAbsolutePath() );
          Files.createDirectories(path);
        } catch(Exception e) {
          e.printStackTrace();
        }


    try {
      con_font_color = new Color( parent.prefs.getInt("con_font_color", new Color(255,255,255).getRGB() ) );
      jTextArea1.setForeground( con_font_color ); 

      con_font_name = parent.prefs.get("con_font_name", "Monospaced" );
      con_font_style = parent.prefs.getInt("con_font_style", Font.PLAIN );
      con_font_size = parent.prefs.getInt("con_font_size", 14 );
      jTextArea1.setFont(new java.awt.Font(con_font_name, con_font_style, con_font_size)); 
    } catch(Exception e) {
    }

    try {
      tg_font_color = new Color( parent.prefs.getInt("tg_font_color", new Color(255,255,255).getRGB() ) );
      log_ta.setForeground( tg_font_color ); 

      tg_font_name = parent.prefs.get("tg_font_name", "Monospaced" );
      tg_font_style = parent.prefs.getInt("tg_font_style", Font.PLAIN );
      tg_font_size = parent.prefs.getInt("tg_font_size", 14 );
      log_ta.setFont(new java.awt.Font(tg_font_name, tg_font_style, tg_font_size)); 
    } catch(Exception e) {
    }

      status_format_cc.setText( parent.prefs.get("status_format_cc", "CC $P25_MODE$ B/SEC $BLKS_SEC$  $WACN$-$SYS_ID$-$NAC$, $FREQ$ MHz") );
      status_format_voice.setText( parent.prefs.get("status_format_voice", "$P25_MODE$  $TG_NAME$ ($TG_ID$)  $RID_ALIAS$ [$RID$] $V_FREQ$ MHz") );

      if(prefs!=null) {
        String sicpu = prefs.get("si_cpu", "normal");
        if(sicpu.equals("normal")) si_cpu_normal.setSelected(true);
        if(sicpu.equals("high")) si_cpu_high.setSelected(true);
        if(sicpu.equals("low")) si_cpu_low.setSelected(true);
        if(sicpu.equals("battery")) si_cpu_battery_saving.setSelected(true);
        if(sicpu.equals("off")) si_cpu_off.setSelected(true);
      }

      dframe.update_prefs();

      z1.setToolTipText( prefs.get("zone1_alias","Press Shift While Pressing Button To Set Zone Alias") );
      z2.setToolTipText( prefs.get("zone2_alias","Press Shift While Pressing Button To Set Zone Alias") );
      z3.setToolTipText( prefs.get("zone3_alias","Press Shift While Pressing Button To Set Zone Alias") );
      z4.setToolTipText( prefs.get("zone4_alias","Press Shift While Pressing Button To Set Zone Alias") );
      z5.setToolTipText( prefs.get("zone5_alias","Press Shift While Pressing Button To Set Zone Alias") );
      z6.setToolTipText( prefs.get("zone6_alias","Press Shift While Pressing Button To Set Zone Alias") );
      z7.setToolTipText( prefs.get("zone7_alias","Press Shift While Pressing Button To Set Zone Alias") );
      z8.setToolTipText( prefs.get("zone8_alias","Press Shift While Pressing Button To Set Zone Alias") );
      z9.setToolTipText( prefs.get("zone9_alias","Press Shift While Pressing Button To Set Zone Alias") );
      z10.setToolTipText( prefs.get("zone10_alias","Press Shift While Pressing Button To Set Zone Alias") );
      z11.setToolTipText( prefs.get("zone11_alias","Press Shift While Pressing Button To Set Zone Alias") );
      z12.setToolTipText( prefs.get("zone12_alias","Press Shift While Pressing Button To Set Zone Alias") );
      z13.setToolTipText( prefs.get("zone13_alias","Press Shift While Pressing Button To Set Zone Alias") );
      z14.setToolTipText( prefs.get("zone14_alias","Press Shift While Pressing Button To Set Zone Alias") );
      z15.setToolTipText( prefs.get("zone15_alias","Press Shift While Pressing Button To Set Zone Alias") );
      z16.setToolTipText( prefs.get("zone16_alias","Press Shift While Pressing Button To Set Zone Alias") );


    try {
      if(si_popout==null) {
        si_popout = new si_frame(this, prefs);
        si_popout.setIconImage(new javax.swing.ImageIcon(getClass().getResource("/btconfig/images/siginsight.png")).getImage()); // NOI18N
        si_popout.setTitle("Signal Insights");
      }

      if(con_popout==null) {
        con_popout = new console_frame(this, prefs);
        con_popout.setIconImage(new javax.swing.ImageIcon(getClass().getResource("/btconfig/images/console.png")).getImage()); // NOI18N
        con_popout.setTitle("Console");
      }

      if(dvout==null) {
        dvout = new displayframe_popout(this, prefs);
        dvout.setTitle("Display View");
        dvout.setIconImage(new javax.swing.ImageIcon(getClass().getResource("/btconfig/images/dispview.png")).getImage()); // NOI18N
      }

    } catch(Exception e) {
      e.printStackTrace();
    }

    try {
      restore_position();
    } catch(Exception e) {
      e.printStackTrace();
    }

    try {
      en_rdio.setSelected( prefs.getBoolean("en_rdio", false) );
    } catch(Exception e) {
      e.printStackTrace();
    }
    try {
      en_broadcastify_calls.setSelected( prefs.getBoolean("en_bcalls", false) );
    } catch(Exception e) {
      e.printStackTrace();
    }

    try {
      Boolean do_def_squelch = parent.prefs.getBoolean("do_def_squelch", true );

      if(did_read_cc==0) read_channel_configs(do_def_squelch);
      did_read_cc=1;

      if(do_def_squelch) {
        try {
          parent.prefs.putBoolean("do_def_squelch", false );
          do_def_squelch = false;
        } catch(Exception e) {
        }
      } 

      prev_current_freq=0;
      update_current_freq();
    } catch(Exception e) {
      e.printStackTrace();
    }

    try {
      Boolean ontop = parent.prefs.getBoolean("popout_ontop", true );
      popout_ontop.setSelected(ontop);
      update_ontop();
    } catch(Exception e) {
      e.printStackTrace();
    }


  } catch(Exception e) {
    e.printStackTrace();
  }
}

//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
public void update_ontop() {
    try {
      Boolean ontop = popout_ontop.isSelected();
      con_popout.setAlwaysOnTop(ontop);
      si_popout.setAlwaysOnTop(ontop);
      dvout.setAlwaysOnTop(ontop);
    } catch(Exception e) {
      e.printStackTrace();
    }

}

//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
String get_home_dir() {
  JFileChooser chooser = new JFileChooser();

  chooser.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);


  int returnVal = chooser.showDialog(parent, "Select Home Directory/Folder");

  if(returnVal == JFileChooser.APPROVE_OPTION) {

    String fs =  System.getProperty("file.separator");
    File file = chooser.getSelectedFile();  //better for windows to do it this way
    System.out.println("file:"+file);


    Path path = Paths.get(new File(home_dir+fs+sys_mac_id).getAbsolutePath() );
    try {
      Files.createDirectories(path);
    } catch(Exception e) {
      e.printStackTrace();
    }
    System.out.println("path:"+path);

    path = Paths.get(file.getAbsolutePath());
    home_dir_label.setText(path.toString());
    home_dir = path.toString(); 
    prefs.put("p25rx_home_dir", home_dir); 

    alias = new Alias(parent, parent.sys_mac_id, document_dir);
  }

  return home_dir_label.getText();
}

//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
public void save_position() {
  try {
    if(prefs==null) return;

    Rectangle r = getBounds();

    prefs.putInt("form_x", r.x);
    prefs.putInt("form_y", r.y);
    prefs.putInt("form_width", r.width);
    prefs.putInt("form_height", r.height);

    Boolean b = minimize.isSelected();
    prefs.putBoolean("form_min", b);

  } catch(Exception e) {
    e.printStackTrace();
  }
}
//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
public void restore_position() {
  try {
    if(prefs==null) return;

    Boolean b = prefs.getBoolean("form_min", false);


    int x = prefs.getInt("form_x",50);
    int y = prefs.getInt("form_y",50);
    int width = prefs.getInt("form_width", 1200);
    int height = prefs.getInt("form_height",770);

    if(height < 100) height = 770;
    if(width < 800) width=1200; 

    if(x > 1600) x = 50;
    if(y > 1600) y = 50;

      //setSize(1054,750);
    Rectangle r = new Rectangle(x,y,width,height);
    setBounds(r);

    if(height==200) b = true;
    if(height==185) b = true;
    if(height>=750) b = false;

    minimize.setSelected(b);

  } catch(Exception e) {
    e.printStackTrace();
  }
      try {
        if(prefs==null) return;

        int x = prefs.getInt("si_form_x",50);
        int y = prefs.getInt("si_form_y",50);
        int width = prefs.getInt("si_form_width", 1200);
        int height = prefs.getInt("si_form_height",750+10+27);
        if(height < 100) height = 770;
        if(width < 100) width=800; 

        if(x > 3200) x = 50;
        if(y > 3200) y = 50;
          //setSize(1054,750);
        Rectangle r = new Rectangle(x,y,width,height);
        si_popout.setBounds(r);
        System.out.println(String.format("si_popout: got here %d,%d,%d,%d", x,y,width,height));

      } catch(Exception e) {
        e.printStackTrace();
      }
      try {
        if(prefs==null) return;

        int x = prefs.getInt("console_form_x",50);
        int y = prefs.getInt("console_form_y",50);
        int width = prefs.getInt("console_form_width", 1200);
        int height = prefs.getInt("console_form_height",750+10+27);
        if(height < 100) height = 770;
        if(width < 100) width=800; 

        if(x > 3200) x = 50;
        if(y > 3200) y = 50;
          //setSize(1054,750);
        Rectangle r = new Rectangle(x,y,width,height);
        con_popout.setBounds(r);
        System.out.println("con_popout: got here");

      } catch(Exception e) {
        e.printStackTrace();
      }

      try {
        if(prefs==null) return;

        int x = prefs.getInt("dvout_form_x",50);
        int y = prefs.getInt("dvout_form_y",50);
        int width = prefs.getInt("dvout_form_width", 1200);
        int height = prefs.getInt("dvout_form_height",750+10+27);
        if(height < 100) height = 770;
        if(width < 100) width=800; 

        if(x > 3200) x = 50;
        if(y > 3200) y = 50;
          //setSize(1054,750);
        Rectangle r = new Rectangle(x,y,width,height);
        dvout.setBounds(r);

      } catch(Exception e) {
        e.printStackTrace();
      }
}

//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
void toggle_recording(Boolean isrec) {

  boolean is_recording=!isrec;

  String recnow="";
  if(is_recording) recnow="1";  //toggle
  if(!is_recording) recnow="0"; //toggle

  //enable binary voice output for mp3 recording
  for(int i=0;i<99;i++) {
    serial_port.writeBytes( new String("en_voice_send "+recnow+"\r\n").getBytes(), 17, 0);
    try {
      SLEEP(50);
      if(serial_port.bytesAvailable()>29) break;
    } catch(Exception e) {
      //e.printStackTrace();
      e.printStackTrace();
    }
    byte[] b = new byte[32];
    int len = serial_port.readBytes(b, 30);
    if(len>0) {
      String s = new String(b);
      if(s.contains("en_voice_send "+recnow)) {
        is_recording = !isrec; 
        break;
      };
    }
  }

  if(is_recording) {
    record_to_mp3.setSelected(true);
    record_to_mp3.setBackground(java.awt.Color.red);
    record_to_mp3.setForeground(java.awt.Color.black);
  }
  else {
    record_to_mp3.setSelected(false);
    record_to_mp3.setBackground(java.awt.Color.white);
    record_to_mp3.setForeground(java.awt.Color.black);
  }
}
    
//SUMS 1
float[] columnWidthPercentage = {.075f, .05f, .10f, .075f, .075f, .25f, .275f, 0.1f, 0.05f };
private void resizeColumns() {
  // Use TableColumnModel.getTotalColumnWidth() if your table is included in a JScrollPane
  //int tW = jTable1.getWidth();
  int tW = jTable1.getColumnModel().getTotalColumnWidth();
  TableColumn column;
  TableColumnModel jTableColumnModel = jTable1.getColumnModel();
  int cantCols = jTableColumnModel.getColumnCount();
  for (int i = 0; i < cantCols; i++) {
    column = jTableColumnModel.getColumn(i);
    int pWidth = Math.round(columnWidthPercentage[i] * tW);
    column.setPreferredWidth(pWidth);
  }
}

                  /*
//SUMS 1
float[] columnWidthPercentage2 = {0.08f, 0.2f, .05f, 0.12f, 0.05f, 0.05f, 0.058f, 0.1f, 0.1f, 0.035f, 0.13f };
private void resizeColumns2() {
  int tW = freq_table.getColumnModel().getTotalColumnWidth();
  TableColumn column;
  TableColumnModel jTableColumnModel = freq_table.getColumnModel();
  int cantCols = jTableColumnModel.getColumnCount();
  for (int i = 0; i < cantCols; i++) {
    column = jTableColumnModel.getColumn(i);
    int pWidth = Math.round(columnWidthPercentage2[i] * tW);
    column.setPreferredWidth(pWidth);
  }
}
                  */

//SUMS 1
float[] columnWidthPercentage3 = {0.1f, 0.4f, 0.1f, 0.4f};
private void resizeColumns3() {
  int tW = alias_table.getColumnModel().getTotalColumnWidth();
  TableColumn column;
  TableColumnModel jTableColumnModel = alias_table.getColumnModel();
  int cantCols = jTableColumnModel.getColumnCount();
  for (int i = 0; i < cantCols; i++) {
    column = jTableColumnModel.getColumn(i);
    int pWidth = Math.round(columnWidthPercentage3[i] * tW);
    column.setPreferredWidth(pWidth);
  }
}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
public void SLEEP(long val) {
  try {

    long start_ms = new java.util.Date().getTime(); 

     while (true) {
       long end_ms = new java.util.Date().getTime(); 
       if(end_ms-start_ms >= val) return;
     }

  } catch(Exception e) {
    e.printStackTrace();
  }
}


  ////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////
  public Boolean is_dup_crc(int crc_test) {
    for(int i=0;i<8;i++) {
      if(voice_crc[i]==crc_test) return true;
    }
    for(int i=0;i<8;i++) {
      if(btext_crc[i]==crc_test) return true;
    }
    return false;
  } 

  ////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////
  public void b_send_ack_crc(int crc) {
    if(true) return;

    byte[] out_buffer = new byte[48]; //size of bl_op
    ByteBuffer bb = ByteBuffer.wrap(out_buffer);
    bb.order(ByteOrder.LITTLE_ENDIAN);
    //uint32_t magic;
    //uint32_t op;
    //uint32_t addr;
    //uint32_t len;
    //uint8_t  data[32]; 

    bb.putInt( (int) Long.parseLong("d35467A6", 16) );  //magic
    bb.putInt( (int) Long.parseLong("12", 10) ); //ack crc op
    bb.putInt( crc ); //crc
    bb.putInt( (int) Long.parseLong("0", 10) ); //len

    for(int i=0;i<32;i++) {
      bb.put((byte)0);
    }

    serial_port.writeBytes( out_buffer, 48, 0);
   }
  ////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////
  public void send_ack_crc(int crc) {
    if(true) return;

    byte[] out_buffer = new byte[48]; //size of bl_op
    ByteBuffer bb = ByteBuffer.wrap(out_buffer);
    bb.order(ByteOrder.LITTLE_ENDIAN);
    //uint32_t magic;
    //uint32_t op;
    //uint32_t addr;
    //uint32_t len;
    //uint8_t  data[32]; 

    bb.putInt( (int) Long.parseLong("d35467A6", 16) );  //magic
    bb.putInt( (int) Long.parseLong("11", 10) ); //ack crc op
    bb.putInt( crc ); //crc
    bb.putInt( (int) Long.parseLong("0", 10) ); //len

    for(int i=0;i<32;i++) {
      bb.put((byte)0);
    }

    serial_port.writeBytes( out_buffer, 48, 0);
   }

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
            java.util.logging.Logger.getLogger(BTFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(BTFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(BTFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(BTFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
              parent = new BTFrame(args);
              parent.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton add_rid;
    private javax.swing.JButton adv_write_config;
    private javax.swing.JPanel advancedpanel;
    private javax.swing.JPanel alias_panel;
    public javax.swing.JTable alias_table;
    public javax.swing.JCheckBox allow_tg_pri_int;
    private javax.swing.JCheckBox allow_unknown_tg;
    public javax.swing.JCheckBox allow_unknown_tg_cb;
    public javax.swing.JTextField audio_agc_max;
    public javax.swing.JList<String> audio_dev_list;
    public javax.swing.JRadioButton audio_hiq;
    public javax.swing.JRadioButton audio_lowq;
    public javax.swing.JProgressBar audio_prog;
    private javax.swing.JPanel audiopanel;
    private javax.swing.JCheckBox auto_add_p25_neighbors;
    private javax.swing.JCheckBox auto_add_p25_secondaries;
    private javax.swing.JCheckBox auto_add_p25_traffic_ch;
    public javax.swing.JCheckBox auto_flash_tg;
    public javax.swing.JComboBox<String> auto_gain_profile;
    public javax.swing.JCheckBox auto_lsm;
    public javax.swing.JCheckBox auto_pop_table;
    public javax.swing.JCheckBox autoscale_const;
    private javax.swing.JButton backup_tg;
    private javax.swing.JComboBox<String> browser_view_mode;
    private javax.swing.JToggleButton bt_indicator;
    private javax.swing.JLabel bt_lb;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup10;
    private javax.swing.ButtonGroup buttonGroup11;
    private javax.swing.ButtonGroup buttonGroup12;
    private javax.swing.ButtonGroup buttonGroup13;
    private javax.swing.ButtonGroup buttonGroup14;
    private javax.swing.ButtonGroup buttonGroup15;
    private javax.swing.ButtonGroup buttonGroup16;
    private javax.swing.ButtonGroup buttonGroup17;
    private javax.swing.ButtonGroup buttonGroup18;
    private javax.swing.ButtonGroup buttonGroup19;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup20;
    private javax.swing.ButtonGroup buttonGroup21;
    private javax.swing.ButtonGroup buttonGroup22;
    private javax.swing.ButtonGroup buttonGroup23;
    private javax.swing.ButtonGroup buttonGroup24;
    private javax.swing.ButtonGroup buttonGroup25;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.ButtonGroup buttonGroup4;
    private javax.swing.ButtonGroup buttonGroup5;
    private javax.swing.ButtonGroup buttonGroup6;
    private javax.swing.ButtonGroup buttonGroup7;
    private javax.swing.ButtonGroup buttonGroup8;
    private javax.swing.ButtonGroup buttonGroup9;
    private javax.swing.JButton button_write_config;
    private javax.swing.JPanel buttong_config;
    public javax.swing.JTextField cc_agency;
    public javax.swing.JButton cc_apply;
    private javax.swing.JButton cc_ch;
    public javax.swing.JRadioButton cc_control;
    public javax.swing.JRadioButton cc_conventional;
    public javax.swing.JCheckBox cc_do_scan;
    public javax.swing.JTextField cc_frequency;
    private javax.swing.JButton cc_import;
    private javax.swing.JCheckBox cc_install_to_flash;
    public javax.swing.JComboBox<String> cc_mgain;
    public javax.swing.JComboBox<String> cc_modulation;
    public javax.swing.JTextField cc_name;
    public javax.swing.JComboBox<String> cc_rfgain;
    public javax.swing.JButton cc_sync;
    public javax.swing.JCheckBox cc_use_on_powerup;
    public javax.swing.JComboBox<String> cc_vga_gain;
    public javax.swing.JCheckBox ccfg_add_analog_ch1;
    public javax.swing.JCheckBox ccfg_add_digital_ch;
    public javax.swing.JCheckBox ccfg_analog_en;
    public javax.swing.JComboBox<String> ccfg_ch_band;
    public javax.swing.JComboBox<String> ccfg_ch_sp;
    private javax.swing.JTextField ccfg_ch_step;
    public javax.swing.JCheckBox ccfg_dmr_control;
    private javax.swing.JTextField ccfg_dmr_lcn;
    public javax.swing.JCheckBox ccfg_dmr_voice;
    public javax.swing.JTextField ccfg_end_freq;
    public javax.swing.JLabel ccfg_iteration_lb;
    private javax.swing.JTextField ccfg_iterations;
    public javax.swing.JComboBox<String> ccfg_max_analyze_time;
    public javax.swing.JCheckBox ccfg_nxdn_voice;
    public javax.swing.JCheckBox ccfg_p1control;
    public javax.swing.JCheckBox ccfg_p1voice;
    private javax.swing.JTextField ccfg_p25_nac;
    private javax.swing.JRadioButton ccfg_p25_p1;
    private javax.swing.JRadioButton ccfg_p25_p2;
    private javax.swing.JTextField ccfg_p25_sysid;
    private javax.swing.JTextField ccfg_p25_wacn;
    public javax.swing.JProgressBar ccfg_prog;
    public javax.swing.JButton ccfg_search_abort;
    public javax.swing.JButton ccfg_search_pause;
    private javax.swing.JTextField ccfg_site_id;
    public javax.swing.JTextField ccfg_squelch_level;
    public javax.swing.JTextField ccfg_st_freq;
    public javax.swing.JButton ccfg_start_discover;
    public javax.swing.JComboBox<String> ccfg_step_dwell;
    public javax.swing.JTextArea ccfg_ta;
    private javax.swing.JLabel ch_step_lb;
    private javax.swing.JPanel channel_search;
    private javax.swing.JPanel channelconfig;
    private javax.swing.JPanel chconfig_general;
    private javax.swing.JButton check_firmware;
    public javax.swing.JPanel consolePanel;
    private javax.swing.JButton console_color;
    private javax.swing.JCheckBox console_dec_no_audio;
    private javax.swing.JButton console_font;
    private javax.swing.JButton consolepop;
    private javax.swing.JPanel const_panel;
    private javax.swing.JLabel county_lb;
    private javax.swing.JButton del_alias;
    private javax.swing.JPanel desc_panel;
    public javax.swing.JCheckBox disable_encrypted;
    private javax.swing.JButton disable_table_rows;
    private javax.swing.JButton disconnect;
    private javax.swing.JButton discover;
    public javax.swing.JPanel display_frame;
    public javax.swing.JPanel displayviewmain_border;
    public javax.swing.JRadioButton do_mp3;
    public javax.swing.JRadioButton do_wav;
    public javax.swing.JRadioButton double_click_opt1;
    public javax.swing.JRadioButton double_click_opt2;
    public javax.swing.JRadioButton double_click_opt3;
    public javax.swing.JRadioButton double_click_opt4;
    public javax.swing.JRadioButton double_click_opt5;
    public javax.swing.JRadioButton double_click_opt6;
    private javax.swing.JButton dvpopout;
    private javax.swing.JPanel eastpanel;
    public javax.swing.JButton edit_alias;
    public javax.swing.JButton edit_alias1;
    private javax.swing.JButton edit_display_view;
    private javax.swing.JCheckBox en_acars;
    public javax.swing.JCheckBox en_bluetooth_cb;
    public javax.swing.JCheckBox en_broadcastify_calls;
    public javax.swing.JCheckBox en_encout;
    public javax.swing.JCheckBox en_evt_output;
    private javax.swing.JCheckBox en_flex32;
    public javax.swing.JCheckBox en_p2_tones;
    private javax.swing.JCheckBox en_pocsag12;
    public javax.swing.JCheckBox en_rdio;
    public javax.swing.JCheckBox en_tg_int_tone;
    public javax.swing.JCheckBox en_zero_rid;
    public javax.swing.JCheckBox enable_alias_dbase;
    public javax.swing.JCheckBox enable_audio;
    public javax.swing.JCheckBox enable_event_dbase;
    public javax.swing.JCheckBox enable_leds;
    public javax.swing.JCheckBox enable_mp3;
    private javax.swing.JButton enable_table_rows;
    public javax.swing.JCheckBox enable_tg_table_updates;
    public javax.swing.JTextField enc_count;
    public javax.swing.JCheckBox enc_mode;
    public javax.swing.JTextField enc_timeout;
    public javax.swing.JTextField end_call_silence;
    private javax.swing.JButton export_alias;
    private javax.swing.JButton find_fixed_gains;
    private javax.swing.JButton freemem;
    public javax.swing.JLabel freq;
    public javax.swing.JToggleButton freq_1;
    public javax.swing.JToggleButton freq_2;
    public javax.swing.JToggleButton freq_3;
    public javax.swing.JToggleButton freq_4;
    public javax.swing.JToggleButton freq_5;
    public javax.swing.JToggleButton freq_6;
    public javax.swing.JToggleButton freq_7;
    public javax.swing.JToggleButton freq_8;
    public javax.swing.JToggleButton freq_9;
    private javax.swing.JLabel freq_ch_lb1;
    private javax.swing.JLabel freq_ch_lb2;
    public javax.swing.JToggleButton freq_nan;
    public javax.swing.JLabel fw_installed;
    public javax.swing.JLabel fw_ver;
    private javax.swing.JButton hold;
    private javax.swing.JButton hold1;
    public javax.swing.JLabel home_dir_label;
    private javax.swing.JSlider if_slide;
    private javax.swing.JButton import_alias;
    private javax.swing.JButton import_csv;
    private javax.swing.JButton jButton1;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JLabel jLabel54;
    private javax.swing.JLabel jLabel55;
    private javax.swing.JLabel jLabel56;
    private javax.swing.JLabel jLabel57;
    private javax.swing.JLabel jLabel58;
    private javax.swing.JLabel jLabel59;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel60;
    private javax.swing.JLabel jLabel61;
    private javax.swing.JLabel jLabel62;
    private javax.swing.JLabel jLabel63;
    private javax.swing.JLabel jLabel65;
    private javax.swing.JLabel jLabel66;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel89;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabel90;
    private javax.swing.JLabel jLabel92;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel100;
    private javax.swing.JPanel jPanel101;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JPanel jPanel23;
    public javax.swing.JPanel jPanel24;
    private javax.swing.JPanel jPanel25;
    private javax.swing.JPanel jPanel26;
    private javax.swing.JPanel jPanel27;
    private javax.swing.JPanel jPanel28;
    private javax.swing.JPanel jPanel29;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel30;
    private javax.swing.JPanel jPanel31;
    private javax.swing.JPanel jPanel32;
    private javax.swing.JPanel jPanel33;
    private javax.swing.JPanel jPanel34;
    private javax.swing.JPanel jPanel36;
    private javax.swing.JPanel jPanel37;
    private javax.swing.JPanel jPanel38;
    private javax.swing.JPanel jPanel39;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel40;
    private javax.swing.JPanel jPanel41;
    private javax.swing.JPanel jPanel42;
    private javax.swing.JPanel jPanel43;
    private javax.swing.JPanel jPanel44;
    private javax.swing.JPanel jPanel45;
    private javax.swing.JPanel jPanel46;
    private javax.swing.JPanel jPanel47;
    private javax.swing.JPanel jPanel48;
    private javax.swing.JPanel jPanel49;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel50;
    private javax.swing.JPanel jPanel51;
    private javax.swing.JPanel jPanel52;
    private javax.swing.JPanel jPanel53;
    private javax.swing.JPanel jPanel55;
    private javax.swing.JPanel jPanel56;
    private javax.swing.JPanel jPanel57;
    private javax.swing.JPanel jPanel58;
    private javax.swing.JPanel jPanel59;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel60;
    private javax.swing.JPanel jPanel61;
    private javax.swing.JPanel jPanel62;
    private javax.swing.JPanel jPanel63;
    private javax.swing.JPanel jPanel64;
    private javax.swing.JPanel jPanel65;
    private javax.swing.JPanel jPanel66;
    private javax.swing.JPanel jPanel67;
    private javax.swing.JPanel jPanel68;
    private javax.swing.JPanel jPanel69;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel70;
    private javax.swing.JPanel jPanel71;
    private javax.swing.JPanel jPanel72;
    private javax.swing.JPanel jPanel73;
    private javax.swing.JPanel jPanel74;
    private javax.swing.JPanel jPanel75;
    private javax.swing.JPanel jPanel76;
    private javax.swing.JPanel jPanel77;
    private javax.swing.JPanel jPanel78;
    private javax.swing.JPanel jPanel79;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel80;
    private javax.swing.JPanel jPanel81;
    private javax.swing.JPanel jPanel82;
    private javax.swing.JPanel jPanel83;
    private javax.swing.JPanel jPanel84;
    private javax.swing.JPanel jPanel85;
    private javax.swing.JPanel jPanel86;
    private javax.swing.JPanel jPanel87;
    private javax.swing.JPanel jPanel88;
    private javax.swing.JPanel jPanel89;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPanel jPanel90;
    private javax.swing.JPanel jPanel91;
    private javax.swing.JPanel jPanel92;
    private javax.swing.JPanel jPanel93;
    private javax.swing.JPanel jPanel94;
    private javax.swing.JPanel jPanel95;
    private javax.swing.JPanel jPanel96;
    private javax.swing.JPanel jPanel97;
    private javax.swing.JPanel jPanel98;
    private javax.swing.JPanel jPanel99;
    private javax.swing.JScrollPane jScrollPane1;
    public javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator10;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator40;
    private javax.swing.JSeparator jSeparator41;
    private javax.swing.JSeparator jSeparator42;
    private javax.swing.JSeparator jSeparator43;
    private javax.swing.JSeparator jSeparator44;
    private javax.swing.JSeparator jSeparator45;
    private javax.swing.JSeparator jSeparator46;
    private javax.swing.JSeparator jSeparator47;
    private javax.swing.JSeparator jSeparator48;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JSeparator jSeparator9;
    private javax.swing.JTabbedPane jTabbedPane2;
    public javax.swing.JTable jTable1;
    public javax.swing.JTextArea jTextArea1;
    private javax.swing.JPanel level_panel;
    public javax.swing.JSlider lineout_vol_slider;
    private javax.swing.JTextArea log_ta;
    private javax.swing.JPanel logo_panel;
    public javax.swing.JPanel logpanel;
    public javax.swing.JLabel macid;
    private javax.swing.JPanel meter_panel;
    private javax.swing.JToggleButton minimize;
    public javax.swing.JComboBox<String> mixgain;
    public javax.swing.JToggleButton mode_am;
    public javax.swing.JToggleButton mode_am_agc;
    public javax.swing.JToggleButton mode_dmr;
    public javax.swing.JToggleButton mode_fm;
    public javax.swing.JToggleButton mode_nxdn48;
    public javax.swing.JToggleButton mode_nxdn96;
    public javax.swing.JToggleButton mode_p25_cqpsk;
    public javax.swing.JToggleButton mode_p25_lsm;
    public javax.swing.JToggleButton mode_tdmacc;
    public javax.swing.JCheckBox mp3_separate_files;
    public javax.swing.JToggleButton mute;
    public javax.swing.JLabel nac;
    private javax.swing.JButton next_freq;
    private javax.swing.JPanel northpanel;
    public javax.swing.JComboBox<String> nsymbols;
    private javax.swing.JLabel os_string;
    public javax.swing.JTextField p1_sync_thresh;
    public javax.swing.JTextField p25_tone_vol;
    private javax.swing.JPanel p25config;
    private javax.swing.JPanel p25rxconfigpanel;
    public javax.swing.JTextField p2_sync_thresh;
    private javax.swing.JButton pause_roaming;
    private javax.swing.JCheckBox pl_tone_filter;
    public javax.swing.JTextField pl_tone_freq;
    private javax.swing.JButton popout_all;
    public javax.swing.JCheckBox popout_ontop;
    private javax.swing.JButton prev_freq;
    private javax.swing.JPanel profile_search1;
    private javax.swing.JPanel profile_search2;
    private javax.swing.JProgressBar progbar;
    private javax.swing.JLabel progress_label;
    public javax.swing.JRadioButton quad_click_opt1;
    public javax.swing.JRadioButton quad_click_opt2;
    public javax.swing.JRadioButton quad_click_opt3;
    public javax.swing.JRadioButton quad_click_opt4;
    public javax.swing.JRadioButton quad_click_opt5;
    public javax.swing.JRadioButton quad_click_opt6;
    public javax.swing.JTextField rdio_mask;
    private javax.swing.JButton read_config;
    private javax.swing.JButton read_tg;
    private javax.swing.JToggleButton record_to_mp3;
    private javax.swing.JButton refresh_treeview;
    private javax.swing.JLabel release_date;
    private javax.swing.JButton reset_defaults;
    private javax.swing.JButton reset_to_defaults;
    public javax.swing.JComboBox<String> rf_hyst;
    public javax.swing.JComboBox<String> rfgain;
    public javax.swing.JLabel rfid;
    public javax.swing.JCheckBox roaming_ret_to_cc;
    private javax.swing.JButton save_iq;
    private javax.swing.JButton select_home;
    private javax.swing.JButton select_systems;
    private javax.swing.JButton send_tg;
    public javax.swing.JCheckBox separate_rid;
    private javax.swing.JLabel ser_dev;
    private javax.swing.JButton set_gains_auto;
    private javax.swing.JButton set_zones;
    private javax.swing.JButton show_keywords;
    public javax.swing.JRadioButton si_cpu_battery_saving;
    public javax.swing.JRadioButton si_cpu_high;
    public javax.swing.JRadioButton si_cpu_low;
    public javax.swing.JRadioButton si_cpu_normal;
    public javax.swing.JRadioButton si_cpu_off;
    private javax.swing.JTextField sig_cmd;
    public javax.swing.JPanel signalinsightpanel;
    public javax.swing.JRadioButton single_click_opt1;
    public javax.swing.JRadioButton single_click_opt2;
    public javax.swing.JRadioButton single_click_opt3;
    public javax.swing.JRadioButton single_click_opt4;
    public javax.swing.JRadioButton single_click_opt5;
    public javax.swing.JRadioButton single_click_opt6;
    private javax.swing.JButton sipopout;
    public javax.swing.JLabel siteid;
    private javax.swing.JButton skip;
    private javax.swing.JButton skip1;
    public javax.swing.JTextField skip_tg_to;
    public javax.swing.JSlider slider_squelch;
    public javax.swing.JLabel slider_val;
    public javax.swing.JRadioButton sort_alias;
    public javax.swing.JRadioButton sort_rid;
    private javax.swing.JPanel southpanel;
    public javax.swing.JToggleButton sq_indicator;
    private javax.swing.JLabel sq_lb;
    private javax.swing.JButton squelch_set;
    private javax.swing.JLabel status;
    private javax.swing.JTextField status_format_cc;
    private javax.swing.JTextField status_format_voice;
    private javax.swing.JPanel status_panel;
    public javax.swing.JToggleButton stepdown;
    public javax.swing.JToggleButton stepup;
    public javax.swing.JLabel sysid;
    public javax.swing.JLabel systems_path_str;
    public javax.swing.JTabbedPane tabbed_pane;
    private javax.swing.JPanel talkgroup_panel;
    private javax.swing.JButton tg_duplicate;
    private javax.swing.JButton tg_edit_del;
    public javax.swing.JToggleButton tg_indicator;
    private javax.swing.JLabel tg_lb;
    public javax.swing.JScrollPane tg_scroll_pane;
    public javax.swing.JComboBox<String> tg_sort;
    private javax.swing.JPanel tgfontpanel;
    private javax.swing.JButton tglog_color;
    private javax.swing.JButton tglog_edit;
    private javax.swing.JButton tglog_font;
    private javax.swing.JPanel tiny_const;
    public javax.swing.JLabel top_label;
    private javax.swing.JButton traffic_ch;
    public javax.swing.JRadioButton triple_click_opt1;
    public javax.swing.JRadioButton triple_click_opt2;
    public javax.swing.JRadioButton triple_click_opt3;
    public javax.swing.JRadioButton triple_click_opt4;
    public javax.swing.JRadioButton triple_click_opt5;
    public javax.swing.JRadioButton triple_click_opt6;
    public javax.swing.JTextField trunk_no_voice_timeout;
    private javax.swing.JButton update_alias_table;
    private javax.swing.JButton update_selected_tg;
    public javax.swing.JComboBox<String> vga_gain;
    public javax.swing.JComboBox<String> vga_gain_offset;
    public javax.swing.JTextField vga_step;
    public javax.swing.JTextField vga_target;
    public javax.swing.JLabel volume_label;
    public javax.swing.JComboBox<String> vtimeout;
    public javax.swing.JLabel wacn;
    private javax.swing.JButton write_cfg_si;
    private javax.swing.JButton write_config;
    private javax.swing.JButton write_config_global;
    private javax.swing.JButton write_config_tg;
    public javax.swing.JToggleButton z1;
    public javax.swing.JToggleButton z10;
    public javax.swing.JToggleButton z11;
    public javax.swing.JToggleButton z12;
    public javax.swing.JToggleButton z13;
    public javax.swing.JToggleButton z14;
    public javax.swing.JToggleButton z15;
    public javax.swing.JToggleButton z16;
    public javax.swing.JToggleButton z2;
    public javax.swing.JToggleButton z3;
    public javax.swing.JToggleButton z4;
    public javax.swing.JToggleButton z5;
    public javax.swing.JToggleButton z6;
    public javax.swing.JToggleButton z7;
    public javax.swing.JToggleButton z8;
    public javax.swing.JToggleButton z9;
    // End of variables declaration//GEN-END:variables
}
