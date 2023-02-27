
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
import java.util.*;

public class channel_config {

  public String config_path;

  //general
  public boolean enable_pl_tone_filter;
  public float pl_tone_freq=0.0f;
  public boolean do_scan;
  public boolean use_on_power_up;
  public String name="";
  public String agency="";
  public double frequency=0.0;
  public int modulation_type;
  public boolean is_control; 
  public boolean is_conventional; 
  public int lna_gain;
  public int mgain;
  public int vga_gain;

  //scanning
  public boolean squelch_enable;
  public int squelch_level;


  //p25
  public boolean add_p25_traffic;
  public boolean add_p25_secondary;
  public boolean add_p25_neighbor;
  public int p25_wacn;
  public int p25_sysid;
  public int p25_nac;
  public boolean p25_p1;
  public boolean p25_p2;

  //dmr
  public int dmr_lcn;
  public int site_id;

  //analog decoders
  public boolean en_acars;
  public boolean en_flex32;
  public boolean en_pocsag12;

  public String tag="";
  public String alpha_tag="";
  public String county="";

  public boolean install_in_flash=false;

  public void init() {
    this.enable_pl_tone_filter = enable_pl_tone_filter;
    this.do_scan = false;
    this.use_on_power_up = false;
    this.name = ""; 
    this.modulation_type = 0;
    this.is_control = false;
    this.is_conventional = true;
    this.lna_gain = 0; 
    this.mgain = 0;
    this.vga_gain = 0;

    this.squelch_enable = true;
    this.squelch_level = -120;

    this.add_p25_traffic = false;
    this.add_p25_secondary = false;
    this.add_p25_neighbor = false;
    this.p25_wacn = 0;
    this.p25_sysid = 0;
    this.p25_nac = 0;
    this.p25_p1 = true;
    this.p25_p2 = false;

    this.dmr_lcn = 0;
    this.site_id = 1;

    this.en_acars=false;
    this.en_flex32=false;
    this.en_pocsag12=false;

    this.tag = "";
    this.alpha_tag = "";
    this.county = "";
    this.install_in_flash=false;
  } 

  public channel_config(String name, String f) {
    this.frequency = Double.valueOf(f);
    this.name = name;
    init();
  }
  public channel_config(String name, double f) {
    this.frequency = f; 
    this.name = name;
    init();
  }
  public channel_config(String name, Double f) {
    this.name = name;
    this.frequency = f.doubleValue(); 
    init();
  }

  /*
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
  */
  static public void initialize_from_rr( Properties rr_props, File file ) {
    try {


      FileOutputStream fos = new FileOutputStream(file);
      ObjectOutputStream oos = new ObjectOutputStream( fos );

      String f = rr_props.getProperty("freq");
      String name = rr_props.getProperty("desc");
      String agency = rr_props.getProperty("agency");
      System.out.println("name:" +name);
      System.out.println("freq:" +f);

      channel_config cc = new channel_config(name, f);

      cc.config_path = file.getAbsolutePath().toString(); 
      cc.name = name;
      cc.agency = agency;

      String tag = rr_props.getProperty("tag");
      String alpha_tag = rr_props.getProperty("alpha_tag");
      String county = rr_props.getProperty("county");

      cc.tag = tag;
      cc.alpha_tag = alpha_tag;
      cc.county = county;

      try {
        String d = name.trim();
        if(d.startsWith("Site")) {
          String[] s = d.split(" ");
          int site = Integer.valueOf(s[1]);
          if(site>0) cc.site_id = site;
        } 
      } catch(Exception e) {
      }

      String mode = rr_props.getProperty("mode");
      mode = mode.trim();
      String is_simulcast = rr_props.getProperty("is_simulcast");

      if( mode.equals("AM") ) {
          cc.modulation_type=8;
      }
      else if( mode.equals("FM") || mode.equals("FMN") ) {
        cc.modulation_type=6;
        try {
          String pl_tone = rr_props.getProperty("pl_tone").trim();
          String[] s = pl_tone.split(" ");
          cc.enable_pl_tone_filter=false; 
          if(s.length>0) {
            float tone_freq=0.0f;
            for(int i=0;i<s.length;i++) {
              try {
                tone_freq = Float.valueOf(s[i]);
                if( tone_freq > 60.0f && tone_freq < 500.0f) {
                  cc.enable_pl_tone_filter=true; 
                  cc.pl_tone_freq = tone_freq; 
                  break;
                }
              } catch(Exception e) {
              }
            }
          }
        } catch(Exception e) {
        }
      }
      else if( mode.equals("DMR") ) {
          cc.modulation_type=3;
      }
      else if( mode.equals("NXDN") ) {
          cc.modulation_type=4;
      }
      else if( mode.equals("P25") || mode.equals("Project 25") ) {
        if(is_simulcast.equals("true")) {
          cc.modulation_type=0;
        }
        else {
          cc.modulation_type=1;
        }
      }
      else {
        //we dont know. make it FM
        cc.modulation_type=6;
      }

      cc.write_config(oos);

      oos.close();
      fos.close();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
  static public channel_config get_cc_from_file(Properties rr_props, File file ) {
    channel_config cc=null;

    try {
      FileInputStream fis = new FileInputStream(file);
      ObjectInputStream ois = new ObjectInputStream(fis);

      if(rr_props!=null) {
        String f = rr_props.getProperty("freq");
        String name = rr_props.getProperty("desc");
        String agency = rr_props.getProperty("agency");
        cc = new channel_config(name, f);
      }
      else {
        cc = new channel_config("name", "915.0");
      }

      cc.read_config(ois);

      cc.config_path = file.getAbsolutePath().toString(); 

      fis.close();
      ois.close();

    } catch(Exception e) {
    }

    return cc;
  }

  public void read_config(ObjectInputStream ois) {
    try {
      int version = ois.readInt();

      enable_pl_tone_filter = ois.readBoolean();
      pl_tone_freq = ois.readFloat();
      do_scan = ois.readBoolean();
      use_on_power_up = ois.readBoolean();
      name = ois.readUTF();
      agency = ois.readUTF();
      frequency = ois.readDouble();
      modulation_type = ois.readInt();
      is_control = ois.readBoolean();
      is_conventional = ois.readBoolean();
      lna_gain = ois.readInt();
      mgain = ois.readInt();
      vga_gain = ois.readInt();

      squelch_enable = ois.readBoolean();
      squelch_level = ois.readInt();

      add_p25_traffic = ois.readBoolean();
      add_p25_secondary = ois.readBoolean();
      add_p25_neighbor = ois.readBoolean();
      p25_wacn = ois.readInt();
      p25_sysid = ois.readInt();
      p25_nac = ois.readInt();
      p25_p1 = ois.readBoolean();
      p25_p2 = ois.readBoolean();

      dmr_lcn = ois.readInt();
      site_id = ois.readInt();

      en_acars = ois.readBoolean();
      en_flex32 = ois.readBoolean();
      en_pocsag12 = ois.readBoolean();


      tag = ois.readUTF();
      alpha_tag = ois.readUTF();
      county = ois.readUTF();
      config_path = ois.readUTF();

      install_in_flash = ois.readBoolean();


    } catch(Exception e) {
      e.printStackTrace();
    }
  }


  /*
  uint8_t enable;
  float pl_tone;
  uint8_t do_scan;
  uint8_t use_on_power_up;
  uint8_t name[32];
  double  frequency;
  int32_t modulation_type;
  uint8_t is_control;
  uint8_t is_conventional;
  int32_t lna_gain; 
  int32_t mgain; 
  int32_t vga_gain; 

  uint8_t squelch_enable;
  int32_t squelch_level;

  uint8_t add_p25_traffic;
  uint8_t add_p25_secondary;
  uint8_t add_p25_neighbor;
  int32_t p25_wacn;
  int32_t p25_sysid;
  int32_t p25_nac;
  uint8_t p25_p1;
  uint8_t p25_p2;

  int32_t dmr_lcn;
  int32_t site_id;

  uint8_t en_acars;
  uint8_t en_flex32;
  uint8_t en_pocsag12;
  */
  public void write_config_bb(ByteBuffer bb) {
    try {
      if( enable_pl_tone_filter ) bb.put((byte)1);
        else bb.put((byte)0);

      bb.putFloat( pl_tone_freq );

      if(do_scan) bb.put((byte)1);
        else bb.put((byte)0);
      if(use_on_power_up) bb.put((byte)1);
        else bb.put((byte)0);

      byte[] b= new byte[32];
      byte[] name_b = name.getBytes();
      int idx=0;
      for(int i=0;i<32;i++) {
        if( idx<name_b.length) b[i]=name_b[idx++];
          else b[i]=(byte)0x00;
      }
      b[31]=0;
      for(int i=0;i<32;i++) {
        bb.put(b[i]);
      }

      b= new byte[32];
      name_b = agency.getBytes();
      idx=0;
      for(int i=0;i<32;i++) {
        if( idx<name_b.length) b[i]=name_b[idx++];
          else b[i]=(byte)0x00;
      }
      b[31]=0;
      for(int i=0;i<32;i++) {
        bb.put(b[i]);
      }

      bb.putDouble(frequency);

      bb.putInt(modulation_type);
      if(is_control) bb.put((byte)1);
          else bb.put((byte)0);
      if(is_conventional) bb.put((byte)1);
        else bb.put((byte)0);

      bb.putInt(lna_gain);
      bb.putInt(mgain);
      bb.putInt(vga_gain);

      if(squelch_enable) bb.put((byte)1);
        else bb.put((byte)0);

      bb.putInt(squelch_level);


      if(add_p25_traffic) bb.put((byte)1);
        else bb.put((byte)0);
      if(add_p25_secondary) bb.put((byte)1);
        else bb.put((byte)0);
      if(add_p25_neighbor) bb.put((byte)1);
        else bb.put((byte)0);

      bb.putInt(p25_wacn);
      bb.putInt(p25_sysid);
      bb.putInt(p25_nac);
      
      if(p25_p1) bb.put((byte)1);
        else bb.put((byte)0);
      if(p25_p2) bb.put((byte)1);
        else bb.put((byte)0);

      bb.putInt(dmr_lcn);
      bb.putInt(site_id);

      if(en_acars) bb.put((byte)1);
        else bb.put((byte)0);
      if(en_flex32) bb.put((byte)1);
        else bb.put((byte)0);
      if(en_pocsag12) bb.put((byte)1);
        else bb.put((byte)0);


    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  public void write_config(ObjectOutputStream oos) {
    try {
      oos.writeInt(100);

      oos.writeBoolean(enable_pl_tone_filter);
      oos.writeFloat( pl_tone_freq );
      oos.writeBoolean(do_scan);
      oos.writeBoolean(use_on_power_up);
      oos.writeUTF(name);
      oos.writeUTF(agency);
      oos.writeDouble(frequency);
      oos.writeInt(modulation_type);
      oos.writeBoolean(is_control);
      oos.writeBoolean(is_conventional);
      oos.writeInt(lna_gain);
      oos.writeInt(mgain);
      oos.writeInt(vga_gain);

      oos.writeBoolean(squelch_enable);
      oos.writeInt(squelch_level);

      oos.writeBoolean(add_p25_traffic);
      oos.writeBoolean(add_p25_secondary);
      oos.writeBoolean(add_p25_neighbor);
      oos.writeInt(p25_wacn);
      oos.writeInt(p25_sysid);
      oos.writeInt(p25_nac);
      oos.writeBoolean(p25_p1);
      oos.writeBoolean(p25_p2);

      oos.writeInt(dmr_lcn);
      oos.writeInt(site_id);

      oos.writeBoolean(en_acars);
      oos.writeBoolean(en_flex32);
      oos.writeBoolean(en_pocsag12);

      oos.writeUTF(tag);
      oos.writeUTF(alpha_tag);
      oos.writeUTF(county);

      oos.writeUTF(config_path);

      oos.writeBoolean(install_in_flash);
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  static public channel_config clone(channel_config p) {
    channel_config q = new channel_config(p.name, p.frequency);
    q.enable_pl_tone_filter = p.enable_pl_tone_filter;
    q.pl_tone_freq = p.pl_tone_freq;
    q.do_scan = p.do_scan;
    q.use_on_power_up = p.use_on_power_up;
    q.name = p.name;
    q.agency = p.agency;
    q.frequency = p.frequency;
    q.modulation_type = p.modulation_type;
    q.is_control = p.is_control;
    q.is_conventional = p.is_conventional;
    q.lna_gain = p.lna_gain;
    q.mgain = p.mgain;
    q.vga_gain = p.vga_gain;

    q.squelch_enable = p.squelch_enable;
    q.squelch_level = p.squelch_level;

    q.add_p25_traffic = p.add_p25_traffic;
    q.add_p25_secondary = p.add_p25_secondary;
    q.add_p25_neighbor = p.add_p25_neighbor;
    q.p25_wacn = p.p25_wacn;
    q.p25_sysid = p.p25_sysid;
    q.p25_nac = p.p25_nac;
    q.p25_p1 = p.p25_p1; 
    q.p25_p2 = p.p25_p2; 

    q.dmr_lcn = p.dmr_lcn;
    q.site_id = p.site_id;

    q.en_acars = p.en_acars;
    q.en_flex32 = p.en_flex32;
    q.en_pocsag12 = p.en_pocsag12;

    q.tag = p.tag;
    q.alpha_tag = p.alpha_tag;
    q.county = p.county;
    q.config_path = p.config_path;
    q.install_in_flash = p.install_in_flash;
    return q;
  }

  public String toString() {
    if(name==null || name.length()==0) return String.format("%3.5f", frequency);
    return name;
  }

  public String getFreq() {
    return String.format("%3.5f", frequency);
  }
}
