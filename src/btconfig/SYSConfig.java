
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
import com.fazecast.jSerialComm.*;
import javax.swing.filechooser.*;
import javax.swing.*;
import javax.swing.*;




//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
class SYSConfig
{

  int read_serial_delay = 5;
  int write_serial_delay = 5;

java.util.Timer utimer;
BTFrame parent;
SerialPort serial_port;
java.text.SimpleDateFormat formatter_date;

int did_warning=0;
int did_crc_reset=0;
int prev_op_mode=-1;

  String cmd="";
  int rlen=0;

///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
void do_usb_watchdog(SerialPort sp) {

  /*
  try {
    byte[] out_buffer = new byte[16+32]; //size of bl_op
    ByteBuffer bb = ByteBuffer.wrap(out_buffer);
    bb.order(ByteOrder.LITTLE_ENDIAN);

    bb.putInt( (int) Long.parseLong("d35467A6", 16) );  //magic
    bb.putInt( (int) Long.parseLong("9", 10) ); //usb watchdog reset
    bb.putInt( (int) new Long((long) 0x00000000 ).longValue() );  //address to return
    bb.putInt( (int) Long.parseLong("0", 10) );  //data len  to return

    if(sp!=null) sp.writeBytes( out_buffer, 48); //16 + data len=0

  } catch(Exception e) {
    e.printStackTrace();
  }
  */
}


//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
Boolean is_valid_freq(double freq) {
  int band = 0;

  if(freq >= 25.0 && freq <= 512.0) band= 1;  //band 1
  if(freq >= 758.0 && freq <= 824.0) band= 2;  //band 2
  if(freq >= 849.0 && freq <= 869.0) band= 3;  //band 3
  if(freq >= 894.0 && freq <= 960.0) band= 5;  //band 4
  if(freq >= 1240.0 && freq <= 1300.0) band= 6;  //band 5


  if(band!=0) return true;

  return false; 
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
public SYSConfig(BTFrame parent) {
  this.parent = parent;
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
String write_cmd(String cmd) {
  try {
    byte[] result=new byte[64];

    System.out.println("cmd:"+cmd+":");

    for(int i=0;i<5;i++) {
      serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
      SLEEP(write_serial_delay);
      rlen=serial_port.readBytes( result, 64);
      String res = new String(result).trim();
      System.out.println("res:"+res+":");
      if(res.length()>0) {
        return res;
      }
      else {
        int avail = serial_port.bytesAvailable();
        if(avail>0) {
          byte[] flush = new byte[avail]; 
          serial_port.readBytes( flush, avail);
        }
      }
    }

    SLEEP(read_serial_delay);
  } catch(Exception e) {
  }
  return "";
}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
private void SLEEP(long val) {
  try {
    parent.SLEEP(val);
  } catch(Exception e) {
    e.printStackTrace();
  }
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
public void read_sysconfig(BTFrame parent, SerialPort serial_port)
{
  this.serial_port = serial_port;

  byte[] image_buffer = new byte[128 * 1024 * 6];

  for( int i=0; i< 128 * 1024 *6; i++) {
    image_buffer[i] = (byte) 0xff;
  }

  int config_length = 0;
  int CONFIG_SIZE=1024;

  try {


    if(parent.fw_completed==0) {
      parent.do_read_talkgroups=0;
      parent.did_read_talkgroups=1;
      parent.is_connected=1;
      parent.do_read_config=0;
      return;
    }


    int state = -1; 

    while(true) {


        if(state==-1) {
          if(serial_port!=null && serial_port.isOpen()) {
            state=0;
          } 
        }
        else {
          parent.setProgress(-1);
          parent.setStatus("\r\ncouldn't find device");
          return;
        }

        try {
          serial_port.removeDataListener();
        } catch(Exception e) {
        }

      //get the number of recs
        if(state==0) {

          parent.setProgress(5);
          parent.setStatus("Reading sys_config from P25RX device..."); 


          int offset = 0;
          //while(offset<config_length) {


          int nrecs=0;
          int timeout=0;
          while(true) {

              if(timeout++>10) break;

              byte[] out_buffer = new byte[16+32]; //size of bl_op
              ByteBuffer bb = ByteBuffer.wrap(out_buffer);
              bb.order(ByteOrder.LITTLE_ENDIAN);

              bb.putInt( (int) Long.parseLong("d35467A6", 16) );  //magic
              bb.putInt( (int) Long.parseLong("6", 10) ); //read cfg flash
              bb.putInt( (int) new Long((long) 0x08100000 + offset).longValue() );  //address to return
              bb.putInt( (int) Long.parseLong("32", 10) );  //data len  to return



              byte[] input_buffer = new byte[48];
              rlen=0;
              while(rlen!=48) {
                serial_port.writeBytes( out_buffer, 48, 0); //16 + data len=0

                  try {
                    int count=0;
                    while(serial_port.bytesAvailable()<48) {
                      SLEEP(1);
                      if(count++>50) break;
                    }
                  } catch(Exception e) {
                    e.printStackTrace();
                  }

                rlen=serial_port.readBytes( input_buffer, 48 );
                if(rlen==48) {
                  break;
                }
                //else {
                 // System.out.println("rlen<>48");
                //}
              }

              ByteBuffer bb2 = ByteBuffer.wrap(input_buffer);
              bb2.order(ByteOrder.LITTLE_ENDIAN);


              if( bb2.getInt()== 0xd35467A6) {//magic
                bb2.getInt();  //op
                bb2.getInt();  //address
                bb2.getInt();  //len
                nrecs = bb2.getInt();
                if(nrecs>0 && nrecs<1280000) break;
              }
              else {
                //flush the input buffers
                byte[] b = new byte[ serial_port.bytesAvailable()+1 ];
                if(b.length>0)serial_port.readBytes( b, b.length-1 );  //flush buffer
              }
          }

          if(nrecs>0) {
            parent.setStatus("\r\nCompleted reading sys_config. nrecs: "+nrecs);
          }
          else {
            parent.setStatus("\r\nNo talkgroup records found.");
          }
          parent.setProgress(10);



          offset = 0; //skip the nrecs int

          while(true) {

              byte[] out_buffer = new byte[16+32]; //size of bl_op
              ByteBuffer bb = ByteBuffer.wrap(out_buffer);
              bb.order(ByteOrder.LITTLE_ENDIAN);

              bb.putInt( (int) Long.parseLong("d35467A6", 16) );  //magic
              bb.putInt( (int) Long.parseLong("6", 10) ); //read cfg flash
              bb.putInt( (int) new Long((long) 0x08100000 + offset).longValue() );  //address to return
              bb.putInt( (int) Long.parseLong("32", 10) );  //data len  to return


              byte[] input_buffer = new byte[32000];
              rlen=0;
              while(rlen!=48) {
                serial_port.writeBytes( out_buffer, 48, 0); //16 + data len=0

                  try {
                    int count=0;
                    while(serial_port.bytesAvailable()<48) {
                      SLEEP(1);
                      if(count++>50) break;
                    }
                  } catch(Exception e) {
                    e.printStackTrace();
                  }

                rlen=serial_port.readBytes( input_buffer, 48 );
                if(rlen==48) {
                  break;
                }
                else {
                  serial_port.readBytes( input_buffer, serial_port.bytesAvailable() );
                }
              }

              ByteBuffer bb2 = ByteBuffer.wrap(input_buffer);
              bb2.order(ByteOrder.LITTLE_ENDIAN);


              if( bb2.getInt()== 0xd35467A6) {//magic
                bb2.getInt();  //op
                int raddress = (bb2.getInt()-0x08100000) ;  //address
                bb2.getInt();  //len

                if(raddress>=0) {
                  for(int i=0;i<32;i++) {
                    image_buffer[i+raddress] = bb2.get();
                  }

                  offset+=32;
                  //if(offset >= 552+32) { //finished?
                  if(offset >= CONFIG_SIZE+32) { //finished?

                    ByteBuffer bb3 = ByteBuffer.wrap(image_buffer);
                    bb3.order(ByteOrder.LITTLE_ENDIAN);
                    int crc = crc32.crc32_range(image_buffer, CONFIG_SIZE-4);
                    parent.system_crc=crc;
                    System.out.println(String.format("config crc 0x%08x", crc));

                    int config_crc = bb3.getInt(CONFIG_SIZE-4);  //1024-4

                      if(crc==0 || config_crc == 0xffffffff) {
                        if(parent.fw_completed==0) {
                        }
                        parent.do_read_talkgroups=0;
                        parent.did_read_talkgroups=1;
                        parent.is_connected=1;

                        parent.do_read_config=0;
                          //int result2 = JOptionPane.showConfirmDialog(parent, "Would you like to erase talk group and roaming frequency flash?", "Erase Config Areas?", JOptionPane.YES_NO_OPTION);
                          //if(result2==JOptionPane.YES_OPTION) {
                           // String cmd = "clear_configs\r\n";
                            //serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);
                            //SLEEP(3000);
                          //}
                            SLEEP(3000);
                        return;
                      }


                    if(crc == config_crc) {

                      parent.system_crc=crc;

                      parent.setStatus("sys_config crc ok."); 
                      parent.do_disconnect=0;
                      parent.crc_errors=0;
                      parent.cpanel.bad_crc=0;
                      parent.setProgress(-1);

                      /*
                      System.out.println( String.format("\r\nfrequency: %3.6f",bb3.getDouble()) );
                      System.out.println( String.format("\r\nis_control: %d",bb3.getInt(36)) );
                      System.out.println( String.format("\r\nvolume: %3.2f",bb3.getFloat(12)) );
                      System.out.println( String.format("\r\nbluetooth: %d",bb3.getInt(88)) );
                      System.out.println( String.format("\r\nbluetooth reset: %d",bb3.getInt(260)/5) );
                      System.out.println( String.format("\r\nbt_gain: %3.2f",bb3.getFloat(176)) );
                      System.out.println( String.format("\r\nled_mode: %d",bb3.getInt(196)) );
                      System.out.println( String.format("\r\nallow unknown tg: %d",bb3.getShort(130)) );
                      System.out.println( String.format("\r\nenable_roaming %d",bb3.getInt(68)) );
                      System.out.println( String.format("\r\nno_voice_roam_sec",bb3.getInt(280)) );

                      System.out.println( String.format("\r\nconfig verson: %d",bb3.getInt(544)) );
                      System.out.println( String.format("\r\nconfig crc: 0x%08x",config_crc) );
                      */


                      try {
                        String fw_ver = "";
                        byte[] fw_version = new byte[12];
                        for(int c=0;c<12;c++) {
                          fw_version[c] = (byte) bb3.get(264+c);
                        } 

                        fw_ver = new String( fw_version );
                        parent.fw_installed.setText("   Installed FW: "+fw_ver);

                        if(parent.fw_ver.getText().contains(fw_ver)) {
                          parent.fw_ver.setVisible(false);
                          parent.fw_installed.setVisible(false);
                        }
                        else {
                          parent.fw_ver.setVisible(true);
                          parent.fw_installed.setVisible(true);
                        }

                        parent.do_read_config=0;
                      } catch(Exception e) {
                      }

                      if( parent.do_write_config==0) {
                        try {

                          try {
                            int trunk_no_voice_timeout = bb3.getInt(280);
                            parent.trunk_no_voice_timeout.setText( Integer.valueOf(trunk_no_voice_timeout).toString() );
                          } catch(Exception e) {
                          }

                          int squelch = (int) bb3.getFloat(132);
                          parent.slider_squelch.setValue(squelch);

                          int vga_step = bb3.getInt(740);
                          parent.vga_step.setText( String.format("%d", vga_step) );

                          int vga_target = bb3.getInt(732);
                          int vga_evmth = bb3.getInt(736);

                          try {
                            parent.vga_target.setText( Integer.valueOf(vga_target).toString() );
                          } catch(Exception e) {
                          }

                          int tgzone = bb3.getInt(772);
                          try {
                            parent.current_tgzone = tgzone;

                            parent.update_zones();

                          } catch(Exception e) {
                          }

                          int rf_max_gain = bb3.getInt(296);
                          if(rf_max_gain==11) parent.auto_gain_profile.setSelectedIndex(0);
                            else parent.auto_gain_profile.setSelectedIndex(1);


                          int auto_lsm = bb3.getInt(816);
                          if(auto_lsm==1) parent.auto_lsm.setSelected(true);
                            else parent.auto_lsm.setSelected(false);

                          int skip_tg_to = bb3.getInt(552);
                          parent.skip_tg_to.setText( Integer.toString(skip_tg_to/1000/60) );

                          int enc_mode = bb3.getInt(564);

                          int en_tg_pri_int = bb3.getInt(568);
                          if(en_tg_pri_int==1) parent.allow_tg_pri_int.setSelected(true);
                            else parent.allow_tg_pri_int.setSelected(false);

                          int en_tg_int_tone = bb3.getInt(576);
                          if(en_tg_int_tone==1) parent.en_tg_int_tone.setSelected(true);
                            else parent.en_tg_int_tone.setSelected(false);

                          if(enc_mode==1) parent.enc_mode.setSelected(true);
                            else parent.enc_mode.setSelected(false);

                          /*
                          int ch_flt = bb3.getInt(788);
                          if(ch_flt<0) ch_flt=0; 
                          if(ch_flt>3) ch_flt=3; 
                          parent.ch_flt.setSelectedIndex(ch_flt);
                          */

                          int rf_hyst = bb3.getInt(628);
                          if(rf_hyst==3) parent.rf_hyst.setSelectedIndex(0);
                          else if(rf_hyst==6) parent.rf_hyst.setSelectedIndex(1);
                          else if(rf_hyst==10) parent.rf_hyst.setSelectedIndex(2);
                          else if(rf_hyst==16) parent.rf_hyst.setSelectedIndex(3);
                          else if(rf_hyst==22) parent.rf_hyst.setSelectedIndex(4);
                          else parent.rf_hyst.setSelectedIndex(1);

                          int enctimeout = bb3.getInt(760);
                          parent.enc_timeout.setText( new Integer(enctimeout).toString() );

                          int enccount = bb3.getInt(764);
                          parent.enc_count.setText( new Integer(enccount).toString() );


                          float agc_max_gain = bb3.getFloat(168);
                          try {
                            parent.audio_agc_max.setText( String.format("%3.2f", agc_max_gain) );
                          } catch(Exception e) {
                          }


                          int but1_cfg = bb3.getInt(540);
                          int but2_cfg = bb3.getInt(544);
                          int but3_cfg = bb3.getInt(548);
                          int but4_cfg = bb3.getInt(556);

                          int vga_gain = bb3.getInt(636);

                          int p1_ssync = bb3.getInt(708);
                          int p2_ssync = bb3.getInt(712);

                          try {
                            parent.p1_sync_thresh.setText( new Integer(p1_ssync).toString() );
                          } catch(Exception e) {
                          }
                          try {
                            parent.p2_sync_thresh.setText( new Integer(p2_ssync).toString() );
                          } catch(Exception e) {
                          }


                          int agc_mode = bb3.getInt(136);
                          if(agc_mode < 1) agc_mode=1;
                          if(agc_mode > 6) agc_mode=6;


                          int lna_gain = bb3.getInt(616);
                          parent.rfgain.setSelectedIndex(lna_gain+1);

                          int mgain = bb3.getInt(728);
                          parent.mixgain.setSelectedIndex(mgain+1);


                          //if(vga_gain < 0) vga_gain=0;
                          //if(vga_gain > 15) vga_gain=15;
                          parent.vga_gain.setSelectedIndex(vga_gain+1);

                          int roam_ret_to_cc = bb3.getInt(560);

                          if( roam_ret_to_cc == 1) parent.roaming_ret_to_cc.setSelected(true);
                            else parent.roaming_ret_to_cc.setSelected(false); 

                          if( but1_cfg == 0 ) parent.single_click_opt1.setSelected(true);
                          else if( but1_cfg == 1 ) parent.single_click_opt2.setSelected(true);
                          else if( but1_cfg == 2 ) parent.single_click_opt3.setSelected(true);
                          else if( but1_cfg == 3 ) parent.single_click_opt4.setSelected(true);
                          else if( but1_cfg == 4 ) parent.single_click_opt5.setSelected(true);
                          else if( but1_cfg == 5 ) parent.single_click_opt6.setSelected(true);
                          else parent.single_click_opt1.setSelected(true); //default

                          if( but2_cfg == 0 ) parent.double_click_opt1.setSelected(true);
                          else if( but2_cfg == 1 ) parent.double_click_opt2.setSelected(true);
                          else if( but2_cfg == 2 ) parent.double_click_opt3.setSelected(true);
                          else if( but2_cfg == 3 ) parent.double_click_opt4.setSelected(true);
                          else if( but2_cfg == 4 ) parent.double_click_opt5.setSelected(true);
                          else if( but2_cfg == 5 ) parent.double_click_opt6.setSelected(true);
                          else parent.double_click_opt2.setSelected(true); //default

                          if( but3_cfg == 0 ) parent.triple_click_opt1.setSelected(true);
                          else if( but3_cfg == 1 ) parent.triple_click_opt2.setSelected(true);
                          else if( but3_cfg == 2 ) parent.triple_click_opt3.setSelected(true);
                          else if( but3_cfg == 3 ) parent.triple_click_opt4.setSelected(true);
                          else if( but3_cfg == 4 ) parent.triple_click_opt5.setSelected(true);
                          else if( but3_cfg == 5 ) parent.triple_click_opt6.setSelected(true);
                          else parent.triple_click_opt3.setSelected(true); //default

                          if( but4_cfg == 0 ) parent.quad_click_opt1.setSelected(true);
                          else if( but4_cfg == 1 ) parent.quad_click_opt2.setSelected(true);
                          else if( but4_cfg == 2 ) parent.quad_click_opt3.setSelected(true);
                          else if( but4_cfg == 3 ) parent.quad_click_opt4.setSelected(true);
                          else if( but4_cfg == 4 ) parent.quad_click_opt5.setSelected(true);
                          else if( but4_cfg == 5 ) parent.quad_click_opt6.setSelected(true);
                          else parent.quad_click_opt6.setSelected(true); //default



                          int is_analog = bb3.getInt(52);


                          float vol = bb3.getFloat(12);
                          vol *= 100.0f;
                          parent.lineout_vol_slider.setValue( (int) vol );
                          parent.volume_label.setText( String.format("%3.2f", vol/100.0f) );

                          float p25_tone_vol = bb3.getFloat(244);
                          parent.p25_tone_vol.setText( String.format("%3.2f", p25_tone_vol) );

                          Boolean b = true;
                          if(bb3.getInt(88)==1) b=true;
                              else b=false;
                          parent.en_bluetooth_cb.setSelected(b); 

                          b = false;
                          if(bb3.getInt(236)==1) b=true;  //en_encout
                              else b=false;
                          parent.en_encout.setSelected(b); 


                          b = false;
                          if(bb3.getInt(216)==1) b=true;  //en_p2_tones
                              else b=false;
                          parent.en_p2_tones.setSelected(b); 

                          int tgtimeout = bb3.getInt(372);
                          switch(tgtimeout) {
                            case  500  :
                              parent.vtimeout.setSelectedIndex(0);
                            break;
                            case  1000  :
                              parent.vtimeout.setSelectedIndex(1);
                            break;
                            case  1500  :
                              parent.vtimeout.setSelectedIndex(2);
                            break;
                            case  2000  :
                              parent.vtimeout.setSelectedIndex(3);
                            break;
                            case  3000  :
                              parent.vtimeout.setSelectedIndex(4);
                            break;
                            case  5000  :
                              parent.vtimeout.setSelectedIndex(5);
                            break;
                            case  10000  :
                              parent.vtimeout.setSelectedIndex(6);
                            break;
                            case  30000  :
                              parent.vtimeout.setSelectedIndex(7);
                            break;
                            default :
                              parent.vtimeout.setSelectedIndex(3);
                            break;
                          }


                          //int rfmg = bb3.getInt(296)-4;
                          //if(rfmg<0) rfmg=0;
                          //parent.rfmaxgain.setSelectedIndex( rfmg ); 

                          if(bb3.getShort(130)==1) b=true;
                              else b=false;
                          parent.allow_unknown_tg_cb.setSelected(b); 

                          if(bb3.getInt(196)==1) b=true;
                              else b=false;
                          parent.enable_leds.setSelected(b); 

                        } catch(Exception e) {
                          e.printStackTrace();
                        }

                      }
                      else {
                        try {

                          cmd = ""; 
                          parent.setStatus("writing configuration to flash..."); 


                          cmd = "logging -999"+"\r\n";
                          write_cmd(cmd);

                          int reset_on_save=0;


                          int vgastep = Integer.valueOf( parent.vga_step.getText() );
                          cmd = "vga_step "+vgastep+"\r\n";
                          write_cmd(cmd);


                          int tgzone = 0;
                          try {
                            if( parent.z1.isSelected() ) tgzone |= 0x01;
                            if( parent.z2.isSelected() ) tgzone |= 0x02;
                            if( parent.z3.isSelected() ) tgzone |= 0x04;
                            if( parent.z4.isSelected() ) tgzone |= 0x08;
                            if( parent.z5.isSelected() ) tgzone |= 0x10;
                            if( parent.z6.isSelected() ) tgzone |= 0x20;
                            if( parent.z7.isSelected() ) tgzone |= 0x40;
                            if( parent.z8.isSelected() ) tgzone |= 0x80;
                            if( parent.z9.isSelected() ) tgzone |= 0x100;
                            if( parent.z10.isSelected() ) tgzone |= 0x200;
                            if( parent.z11.isSelected() ) tgzone |= 0x400;
                            if( parent.z12.isSelected() ) tgzone |= 0x800;
                            if( parent.z13.isSelected() ) tgzone |= 0x1000;
                            if( parent.z14.isSelected() ) tgzone |= 0x2000;
                            if( parent.z15.isSelected() ) tgzone |= 0x4000;
                            if( parent.z16.isSelected() ) tgzone |= 0x8000;
                          } catch(Exception e) {
                          }
                          cmd = "tgzone "+tgzone+"\r\n";
                          write_cmd(cmd);



                          int rfmaxgain = parent.auto_gain_profile.getSelectedIndex();
                          if(rfmaxgain==0) rfmaxgain=11;
                            else rfmaxgain = 6;
                          cmd = "rf_max_gain "+rfmaxgain+"\r\n";
                          write_cmd(cmd);

                          int vga = parent.vga_gain.getSelectedIndex();
                          cmd = "vga_gain "+(vga-1)+"\r\n";
                          write_cmd(cmd);


                          cmd = "enc_timeout "+parent.enc_timeout.getText()+"\r\n";
                          write_cmd(cmd);

                          cmd = "agc_max_gain "+parent.audio_agc_max.getText()+"\r\n";
                          write_cmd(cmd);

                          cmd = "enc_count "+parent.enc_count.getText()+"\r\n";
                          write_cmd(cmd);

                          cmd = "vga_target "+parent.vga_target.getText()+"\r\n";
                          write_cmd(cmd);


                          cmd = "vol "+(float) parent.lineout_vol_slider.getValue()/100.0f+"\r\n";
                          write_cmd(cmd);

                          try {
                            cmd = "no_voice_roam_sec "+(int) Integer.valueOf( parent.trunk_no_voice_timeout.getText() )+"\r\n";
                            write_cmd(cmd);
                          } catch(Exception e) {
                            e.printStackTrace();
                          }


                          try {
                            cmd = "p25_tone_vol "+(float) Float.valueOf( parent.p25_tone_vol.getText() )+"\r\n";
                            write_cmd(cmd);
                          } catch(Exception e) {
                            e.printStackTrace();
                          }



                          int vt = parent.vtimeout.getSelectedIndex();
                          int vto = 2000;
                          switch(vt) {
                            case  0  :
                              vto = 500;
                            break;
                            case  1  :
                              vto = 1000;
                            break;
                            case  2  :
                              vto = 1500;
                            break;
                            case  3  :
                              vto = 2000;
                            break;
                            case  4  :
                              vto = 3000;
                            break;
                            case  5  :
                              vto = 5000;
                            break;
                            case  6  :
                              vto = 10000;
                            break;
                            case  7  :
                              vto = 30000;
                            break;
                            default :
                              vto = 2000;
                            break;
                          }
                          cmd = "tgtimeout "+vto+"\r\n";  
                          write_cmd(cmd);

                          //cmd = "bt_reset "+parent.bluetooth_reset.getText()+"\r\n";
                          cmd = "bt_reset 0"+"\r\n";  //always disabled for now
                          write_cmd(cmd);


                          boolean b = parent.auto_lsm.isSelected();
                          if(b) cmd = "auto_lsm 1\r\n";
                            else cmd = "auto_lsm 0\r\n"; 
                          write_cmd(cmd);


                          int rfhyst = parent.rf_hyst.getSelectedIndex(); 
                          int hyst = 1;
                          if(rfhyst==0) hyst=3;
                          else if(rfhyst==1) hyst=6;
                          else if(rfhyst==2) hyst=10;
                          else if(rfhyst==3) hyst=16;
                          else if(rfhyst==4) hyst=22;
                          else hyst=6;

                          cmd = "rf_hyst "+hyst+"\r\n";
                          write_cmd(cmd);


                          int p1sync = new Integer( parent.p1_sync_thresh.getText() ).intValue();
                          int p2sync = new Integer( parent.p2_sync_thresh.getText() ).intValue();

                          cmd = "p1_ssync "+p1sync+"\r\n";
                          write_cmd(cmd);

                          cmd = "p2_ssync "+p2sync+"\r\n";
                          write_cmd(cmd);



                          b = parent.en_bluetooth_cb.isSelected();
                          if(b) cmd = "bluetooth 1\r\n";
                            else cmd = "bluetooth 0\r\n"; 
                          write_cmd(cmd);


                          b = parent.allow_unknown_tg_cb.isSelected();
                          if(b) cmd = "en_unknown_tg 1\r\n";
                            else cmd = "en_unknown_tg 0\r\n"; 
                          write_cmd(cmd);

                          b = parent.en_encout.isSelected();
                          if(b) cmd = "en_encout 1\r\n";
                            else cmd = "en_encout 0\r\n"; 
                          write_cmd(cmd);


                          b = parent.en_p2_tones.isSelected();
                          if(b) cmd = "en_p2_tones 1\r\n";
                            else cmd = "en_p2_tones 0\r\n"; 
                          write_cmd(cmd);

                          b = parent.enc_mode.isSelected();
                          if(b) cmd = "enc_mode 1\r\n";
                            else cmd = "enc_mode 0\r\n"; 
                          write_cmd(cmd);


                          b = parent.allow_tg_pri_int.isSelected();
                          if(b) cmd = "en_tg_pri_int 1\r\n";
                            else cmd = "en_tg_pri_int 0\r\n"; 
                          write_cmd(cmd);

                          b = parent.en_tg_int_tone.isSelected();
                          if(b) cmd = "en_tg_int_tone 1\r\n";
                            else cmd = "en_tg_int_tone 0\r\n"; 
                          write_cmd(cmd);

                          b = parent.enable_leds.isSelected();
                          if(b) cmd = "led_mode 1\r\n";
                            else cmd = "led_mode 0\r\n"; 
                          write_cmd(cmd);

                          int roam_ret_to_cc = 0;
                          if( parent.roaming_ret_to_cc.isSelected() ) roam_ret_to_cc = 1;

                          cmd = "roam_ret_to_cc "+roam_ret_to_cc+"\r\n"; 
                          write_cmd(cmd);


                          int optb1 = 0;
                          if( parent.single_click_opt1.isSelected() ) optb1 = 0;
                          else if( parent.single_click_opt2.isSelected() ) optb1 = 1;
                          else if( parent.single_click_opt3.isSelected() ) optb1 = 2;
                          else if( parent.single_click_opt4.isSelected() ) optb1 = 3;
                          else if( parent.single_click_opt5.isSelected() ) optb1 = 4;
                          else if( parent.single_click_opt6.isSelected() ) optb1 = 5;

                          int optb2 = 0;
                          if( parent.double_click_opt1.isSelected() ) optb2 = 0;
                          else if( parent.double_click_opt2.isSelected() ) optb2 = 1;
                          else if( parent.double_click_opt3.isSelected() ) optb2 = 2;
                          else if( parent.double_click_opt4.isSelected() ) optb2 = 3;
                          else if( parent.double_click_opt5.isSelected() ) optb2 = 4;
                          else if( parent.double_click_opt6.isSelected() ) optb2 = 5;

                          int optb3 = 0;
                          if( parent.triple_click_opt1.isSelected() ) optb3 = 0;
                          else if( parent.triple_click_opt2.isSelected() ) optb3 = 1;
                          else if( parent.triple_click_opt3.isSelected() ) optb3 = 2;
                          else if( parent.triple_click_opt4.isSelected() ) optb3 = 3;
                          else if( parent.triple_click_opt5.isSelected() ) optb3 = 4;
                          else if( parent.triple_click_opt6.isSelected() ) optb3 = 5;

                          int optb4 = 0;
                          if( parent.quad_click_opt1.isSelected() ) optb4 = 0;
                          else if( parent.quad_click_opt2.isSelected() ) optb4 = 1;
                          else if( parent.quad_click_opt3.isSelected() ) optb4 = 2;
                          else if( parent.quad_click_opt4.isSelected() ) optb4 = 3;
                          else if( parent.quad_click_opt5.isSelected() ) optb4 = 4;
                          else if( parent.quad_click_opt6.isSelected() ) optb4 = 5;

                          int mixgain = parent.mixgain.getSelectedIndex();

                          cmd = "mgain "+(mixgain-1)+"\r\n"; 
                          write_cmd(cmd);

                          int rfg = parent.rfgain.getSelectedIndex();

                          cmd = "lna_gain "+(rfg-1)+"\r\n"; 
                          write_cmd(cmd);


                          cmd = "but1_cfg "+optb1+"\r\n"; 
                          write_cmd(cmd);

                          cmd = "but2_cfg "+optb2+"\r\n"; 
                          write_cmd(cmd);

                          cmd = "but3_cfg "+optb3+"\r\n"; 
                          write_cmd(cmd);

                          cmd = "but4_cfg "+optb4+"\r\n"; 
                          write_cmd(cmd);

                          int skip_tg_to = 60; 
                          try {
                            skip_tg_to = Integer.valueOf( parent.skip_tg_to.getText() );
                          } catch(Exception e) {
                          }
                          cmd = "skip_tg_to "+skip_tg_to+"\r\n"; 
                          write_cmd(cmd);


                          if(parent.is_fast_mode==1) {
                            cmd = "fast_mode\r\n";
                            write_cmd(cmd);
                          }





                          cmd = "save\r\n";
                          write_cmd(cmd);

                          SLEEP(3000);

                          cmd = "logging 0"+"\r\n";
                          write_cmd(cmd);

                          parent.setStatus("sys_config update ok."); 

                          parent.do_write_config=0;

                          parent.do_read_config=1;
                        } catch(Exception e) {
                          e.printStackTrace();
                        }
                      }


                    }
                    else {
                      /*
                      parent.setStatus("sys_config crc not ok."); 
                      System.out.println(String.format("sys_config crc NOT OK. Resetting device.  0x%08x, 0x%08x", crc, config_crc));

                        try {
                          SLEEP(5000);
                        } catch(Exception e) {
                        }
                        parent.setStatus("\r\nresetting device");
                        cmd = "system_reset\r\n";
                        serial_port.writeBytes( cmd.getBytes(), cmd.length(), 0);

                        try {
                          SLEEP(5000);
                        } catch(Exception e) {
                        }


                      parent.is_connected=0;
                      parent.do_connect=1;
                      parent.serial_port=null;
                      */
                    }


                    parent.setProgress(-1); 

                    return; 
                  }

                  //parent.setStatus("read "+offset+" bytes");
                  parent.setStatus("read sys_config."); 
                  //parent.setProgress( (int) ((float)offset/552.0f * 100.0) );
                  parent.setProgress( (int) ((float)offset/1024.0f * 100.0) );
                }
              }
              else {
                //flush buffers
                byte[] b = new byte[ serial_port.bytesAvailable()+1 ];
                if(b.length>0)serial_port.readBytes( b, b.length-1 );  //flush buffer
              }
          }


        }

    } //while(true) 
  } catch (Exception e) {
    e.printStackTrace();
  }
}


}
