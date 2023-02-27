
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


//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
class ccfg_MessageListener implements SerialPortMessageListener
{
   @Override
      //data written only works on Windows, so we don't use
   public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_RECEIVED | SerialPort.LISTENING_EVENT_DATA_WRITTEN; }
   //public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_RECEIVED ; }

   @Override
   //public byte[] getMessageDelimiter() { return new byte[] { (byte)0xa6, (byte)0x67, (byte)0x54, (byte)0xd3 }; }
   public byte[] getMessageDelimiter() { return new byte[] { (byte)0xd3, (byte)0x54, (byte)0x67, (byte)0xa6 }; }

   @Override
   public boolean delimiterIndicatesEndOfMessage() { return false; }

   @Override
   public void serialEvent(SerialPortEvent event)
   {
       if(event.getEventType() == SerialPort.LISTENING_EVENT_DATA_RECEIVED) {
          byte[] newData = event.getReceivedData();

         if(newData.length>=48) {
            System.out.print("\r\nDATA: ");
            for (int i = 0; i < 16; ++i)
               System.out.print(String.format("0x%02x, ", (byte)newData[i]));
               System.out.print("\r\n");

              CCFG.pdata = new byte[ newData.length-4];

              for (int i = 0; i < newData.length-4; ++i) {
                CCFG.pdata[i] = newData[i];
              }
              CCFG.have_data=1;
            System.out.println("RX_LEN: "+newData.length);
          }
         return;
       }
       if(event.getEventType() == SerialPort.LISTENING_EVENT_DATA_WRITTEN) {
         //System.out.println("data written event");
         return;
       }
   }
}


//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
class CCFG
{

java.util.Timer utimer;
SerialPort serial_port;
java.text.SimpleDateFormat formatter_date;
BTFrame parent;
public static volatile byte pdata[];
public static volatile int have_data=0;
public static volatile int rx_data=0;
int did_write=0;
int NRECS=512; 
ccfg_MessageListener listener=null;
//PacketListener listener=null;

//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
static byte[] get_cfg_array( channel_config cc) {

  //pad with 0xff for crc calcs / blank flash mem
  byte[] b = new byte[256];
  for(int i=0;i<256;i++) {
    b[i] = (byte) 0xff;
  }

  ByteBuffer bb = ByteBuffer.wrap(b);
  bb.order(ByteOrder.LITTLE_ENDIAN);

  //fill in the buffer
  cc.write_config_bb(bb);


  int crc = crc32.crc32_range(b, 256-4);
  bb.putInt(252,crc);

  return bb.array(); 
  //return b;
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
public CCFG(BTFrame parent) {
  this.parent = parent;
  pdata = new byte[16+256];
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
public void send_cc(SerialPort serial_port, channel_config[] ccfgs)
{
  this.serial_port = serial_port;

  if( listener==null ) {
    listener = new ccfg_MessageListener();
    this.serial_port.addDataListener(listener);
  }

  if(ccfgs==null) return;

  int config_length = 0;

  try {


    int state = -1; 

    int rec_idx=0;
    int nrecs= ccfgs.length;

    while(true) {


        if(state==-1) {
          if(serial_port!=null && serial_port.isOpen()) {
            state=0;
          } 
        }
        else {
          parent.setProgress(-1);
          parent.setStatus("\r\ncouldn't find device");
          this.serial_port.removeDataListener();
          listener=null;
          return;
        }


        if(state==0) {

            parent.setStatus(nrecs+" records.");

            //limit to max recs
            if(nrecs>NRECS) {
              nrecs=NRECS;
            }

            parent.setProgress(5);
            parent.setStatus("Writing channel config to P25RX device..."); 

            while(rec_idx<nrecs) {
              int did_write=0;
              while(true) {

                byte[] out_buffer = new byte[16+256]; //size of bl_op
                ByteBuffer bb = ByteBuffer.wrap(out_buffer);
                bb.order(ByteOrder.LITTLE_ENDIAN);
                //uint32_t magic;
                //uint32_t op;
                //uint32_t addr;
                //uint32_t len;
                //uint8_t  data[32]; 

                bb.putInt( (int) Long.parseLong("d35467A6", 16) );  //magic
                bb.putInt( (int) Long.parseLong("13", 10) ); //write flash cmd 2
                bb.putInt( (int) new Long((long) 0x08140000 + (rec_idx*256)).longValue() );
                bb.putInt( (int) Long.parseLong("256", 10) );  //data len

                byte[] cfg_b = CCFG.get_cfg_array( ccfgs[rec_idx] );

                System.out.println("");
                for(int i=0;i<256;i++) {
                  bb.put( cfg_b[i] ); 
                  System.out.print(String.format("0x%02x, ", (byte)cfg_b[i]));
                }
                have_data=0;
                serial_port.writeBytes( out_buffer, 16+256, 0);

                 // System.out.print("\r\n");
                  //for (int i = 0; i < 256; ++i) {
                   //  System.out.print(String.format("0x%02x, ", (byte)out_buffer[i]));
                  //}


                int count=0;

                if(rec_idx==0) {
                  parent.setStatus("clearing channel config flash area...");
                }


                if((rec_idx*256)%131072==0) SLEEP(1000);

                while(have_data==0) {
                  if(count++>500) {
                    System.out.println("timeout");
                    serial_port.writeBytes( out_buffer, 16+256, 0);
                    break;
                  }
                  SLEEP(1);
                }

                if(have_data==1) {
                  ByteBuffer bb_verify = ByteBuffer.wrap(pdata);
                  bb_verify.order(ByteOrder.LITTLE_ENDIAN);
                  if( bb_verify.getInt()== 0xa66754d3) {//magic
                    int op = bb_verify.getInt();  //op
                    //System.out.println("op "+op);
                    if( op==4 && bb_verify.getInt()==0x8140000+(rec_idx*256)) { //address
                      did_write=1;
                    }
                    else {
                      SLEEP(5);
                    }
                  }
                  have_data=0;
                }

                if(did_write==1) break;
              }

              rec_idx++;

              int pcomplete = (int)  (((float) nrecs/(float) rec_idx)*100.0);
              parent.setProgress((int) pcomplete);
            }


            //TODO: need to check for ack
            try {
              SLEEP(500);
            } catch(Exception e) {
            }

            parent.setStatus("\r\nCompleted sending channel config.");
            parent.setProgress(-1);
            this.serial_port.removeDataListener();
            listener=null;

            return;
        }

    } //while(true) 
  } catch (Exception e) {
    e.printStackTrace();
  }
  this.serial_port.removeDataListener();
  listener=null;
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

}
