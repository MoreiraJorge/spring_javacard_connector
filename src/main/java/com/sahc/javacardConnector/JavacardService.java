package com.sahc.javacardConnector;

import com.sun.javacard.apduio.Apdu;
import com.sun.javacard.apduio.CadClientInterface;
import com.sun.javacard.apduio.CadDevice;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

@Service
public class JavacardService {

    private CadClientInterface cad;
    private Socket sock;
    private InputStream is;
    private OutputStream os;
    private Apdu apdu;

    public JavacardService() {
        System.out.println("JavacardService service created!");
    }

    public void establishConnectionToSimulator() {
        try {

            sock = new Socket("localhost", 9025);
            os = sock.getOutputStream();
            is = sock.getInputStream();
            cad = CadDevice.getCadClientInstance(CadDevice.PROTOCOL_T1, is, os);

            pwrUp();
            //select
            //0x01 0x23 0x45 0x67 0x89 0x11 0x7F;
            apdu = new Apdu();
            byte[] cmnds = {(byte)0x00, (byte)0xA4, (byte) 0x04, (byte)0x00};
            System.out.println("----------APDU-----------");
            setTheAPDUCommands(cmnds);
            setTheDataLength((byte)6);
            byte[] data = {(byte)0x01, (byte)0x23, (byte)0x45, (byte)0x67, (byte)0x89, (byte)0x11};
            setTheDataIn(data);
            setExpctdByteLength((byte) 127);
            exchangeTheAPDUWithSimulator();
            decodeStatus();
            System.out.println("-------------------------");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void closeConnection() {
        try {
            sock.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void pwrUp() {
        try {
            if (cad != null) {
                //to power up the card
                cad.powerUp();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void pwrDown() {
        try {
            if (cad != null) {
                //power down the card
                cad.powerDown();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setTheAPDUCommands(byte[] cmnds) {
        if (cmnds.length > 4 || cmnds.length == 0) {
            System.err.println("inavlid commands");
        } else {
            //set the APDU header
            apdu.command = cmnds;
            System.out.println("CLA: " + atrToHex(cmnds[0]));
            System.out.println("INS: " + atrToHex(cmnds[1]));
            System.out.println("P1: " + atrToHex(cmnds[2]));
            System.out.println("P2: " + atrToHex(cmnds[3]));
        }
    }

    public void setTheDataLength(byte ln) {
        //set the length of the data command
        apdu.Lc = ln;
        System.out.println("Lc: " + atrToHex(ln));
    }

    public void setTheDataIn(byte[] data) {
        if (data.length != apdu.Lc) {
            System.err.println("The number of data in the array are more than expected");
        } else {
            //set the data to be sent to the applets
            apdu.dataIn = data;
            for (int dataIndx = 0; dataIndx < data.length; dataIndx++) {
                System.out.println("dataIn" + dataIndx + ": " + atrToHex(data[dataIndx]));
            }

        }
    }

    public void setExpctdByteLength(byte ln) {
        //expected length of the data in the response APDU
        apdu.Le = ln;
        System.out.println("Le: " + atrToHex(ln));
    }

    public void exchangeTheAPDUWithSimulator() {

        try {
            //Exchange the APDUs
            apdu.setDataIn(apdu.dataIn, apdu.Lc);
            cad.exchangeApdu(apdu);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public byte[] decodeDataOut() {

        byte[] dOut = apdu.dataOut;
        for (int dataIndx = 0; dataIndx < dOut.length; dataIndx++) {
            System.out.println("dataOut" + dataIndx + ": " + atrToHex(dOut[dataIndx]));
        }
        return dOut;

    }

    public byte[] decodeStatus() {
        byte[] statByte = apdu.getSw1Sw2();
        System.out.println("SW1: " + atrToHex(statByte[0]));
        System.out.println("SW2: " + atrToHex(statByte[1]));
        return statByte;
    }

    /*
    Convert to Hex
     */
    private String atrToHex(byte atCode) {
        char hex[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        String str2 = "";
        int num = atCode & 0xff;
        int rem;
        while (num > 0) {
            rem = num % 16;
            str2 = hex[rem] + str2;
            num = num / 16;
        }
        if (str2 != "") {
            return str2;
        } else {
            return "0";
        }
    }

    public Apdu getApdu() {
        return apdu;
    }

    public void setApdu() {
        this.apdu = new Apdu();
    }


    //TODO: Finalizar leitura de CAP
    public void readAndSendCAP(){
        try{

            File file = new File("C:\\Users\\vasco\\ideaProjects\\outros\\spring_javacard_connector\\src\\main\\java\\com\\sahc\\javacardConnector\\cap-sahc-script.txt");
            if(!file.exists()){
                System.out.println("Failed to read file.");
            }else{
                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                String st;
                while((st = bufferedReader.readLine()) != null){
                    if(!st.startsWith("/") && !st.isEmpty()){
                        Apdu apdu = new Apdu();
                        ArrayList<Byte> cmnds = new ArrayList<>();
                        ArrayList<Byte> data = new ArrayList<>();
                        int byteCounter = 0;
                        byte lc = (byte)0;
                        byte le = (byte)0;
                        for(int i=0; i<st.length() -1; i+=5){ //-1 que é o ";" no final de cada APDU

                            //byte currentByte = Byte.valueOf(st.substring(i+2, i+4), 16);      //"i+2, i+4" vai buscar apenas os valores que estão depois do "0x"
                            //byte currentByte = Byte.parseByte(st.substring(i+2, i+4), 16);    //tirar o "+2" se for preciso o "0x" para o parse também
                            byte currentByte = (byte) Integer.parseInt(st.substring(i+2, i+4), 16);
                            System.out.print(currentByte + " ");
                            if(byteCounter < 4){                    //4 primeiros bytes
                                cmnds.add(currentByte);
                            }else if(byteCounter == 4){             //5º byte = LC
                                lc = currentByte;
                            }else if(byteCounter < st.length() -5){ //todos os bytes restantes menos o final
                                data.add(currentByte);
                            }else{                                  //ultimo byte = LE
                                le = currentByte;
                            }
                            byteCounter++;
                            System.out.println();
                        }
                        byte[] cmndsArr = new byte[cmnds.size()];
                        byte[] dataArr = new byte[data.size()];
                        cmndsArr = cmnds.toArray(cmndsArr);         //como converter de ArrayList<Byte> para byte[]?
                        dataArr = data.toArray(dataArr);

                        apdu.command = cmndsArr;
                        apdu.Lc = lc;
                        apdu.dataIn = dataArr;
                        apdu.Le = le;
                        apdu.setDataIn(apdu.dataIn, apdu.Lc);

                    }
                }


            }


        }catch (Exception e){
            e.printStackTrace();
        }


    }
}
