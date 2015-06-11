import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;

// Generated code
import loadbalancer.*;
import shared.*;

import java.io.*;

public class LoadBalancerHandler implements LoadBalancer.Iface {

  public void offLoad() {
    System.out.println("offLoad()");
    System.out.println(Server.fileName);
    System.out.println(Server.portNum);
    String str = tail2(new File(Server.fileName), 10);
    System.out.println(str);

    sendToBackupServer(str);
  }

  public void load(String str) {
    System.out.println("load()");
    System.out.println(Server.fileName);
    System.out.println(Server.portNum);

    System.out.println(str);
    // prepend to b.txt
    // write to new file
    PrintWriter pout = null;
    try {
      pout = new PrintWriter("temp.txt");
      pout.println(str);
    } catch(IOException e) {
      e.printStackTrace();
    } finally {
      pout.close();
    }

    // append from b.txt to temp.txt and delete b.txt
    try {
		String source = Server.fileName;
		String dest = "temp.txt";
 
		File fin = new File(source);
		FileInputStream fis = new FileInputStream(fin);
		BufferedReader in = new BufferedReader(new InputStreamReader(fis));
 
		FileWriter fstream = new FileWriter(dest, true);
		BufferedWriter out = new BufferedWriter(fstream);
 
		String aLine = null;
		while ((aLine = in.readLine()) != null) {
			//Process each line and add output to Dest.txt file
			out.write(aLine);
			out.newLine();
		}

                // delete b.txt
                fin.delete();
 
		// do not forget to close the buffer reader
		in.close();
 
		// close buffer writer
		out.close();

                // rename temp.txt to b.txt
                new File("temp.txt").renameTo(new File(Server.fileName)); 
    } catch(IOException e) {
      e.printStackTrace();
    }
  }

  // client to server connection to invoke load() method
  private void sendToBackupServer(String str) {
    try {
      TTransport transport;
      transport = new TSocket("localhost", Server.peerPortNum);
      transport.open();

      TProtocol protocol = new  TBinaryProtocol(transport);
      LoadBalancer.Client client = new LoadBalancer.Client(protocol);

      client.load(str);

      transport.close();
    } catch (TException x) {
      x.printStackTrace();
    }
  }
 
  public SharedStruct getStruct(int key) {
    System.out.println("getStruct(" + key + ")");
    return null;
  }

public static String tail2( File file, int lines) {
    java.io.RandomAccessFile fileHandler = null;
    try {
        fileHandler = 
            new java.io.RandomAccessFile( file, "r" );
        long fileLength = fileHandler.length() - 1;
        StringBuilder sb = new StringBuilder();
        int line = 0;

        for(long filePointer = fileLength; filePointer != -1; filePointer--){
            fileHandler.seek( filePointer );
            int readByte = fileHandler.readByte();

             if( readByte == 0xA ) {
                if (filePointer < fileLength) {
                    line = line + 1;
                }
            } else if( readByte == 0xD ) {
                if (filePointer < fileLength-1) {
                    line = line + 1;
                }
            }
            if (line >= lines) {
                break;
            }
            sb.append( ( char ) readByte );
        }

        String lastLine = sb.reverse().toString();
        return lastLine;
    } catch( java.io.FileNotFoundException e ) {
        e.printStackTrace();
        return null;
    } catch( java.io.IOException e ) {
        e.printStackTrace();
        return null;
    }
    finally {
        if (fileHandler != null )
            try {
                fileHandler.close();
            } catch (IOException e) {
            }
    }
}
}

