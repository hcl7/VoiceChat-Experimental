package serverx;

import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;


public class Serverx {
    ServerSocket serverSocket;
    Socket clientSocket = null;
    InputStream input;
    TargetDataLine targetDataLine;
    OutputStream out;
    AudioFormat audioFormat;
    SourceDataLine sourceDataLine;
    int Size = 10000;
    byte tempBuffer[] = new byte[Size];
    static Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();

    Serverx() throws LineUnavailableException, HeadlessException, UnknownHostException {
        JFrame.setDefaultLookAndFeelDecorated(true);
        JFrame frame = new JFrame("Network Phone Server");
        JLabel label = new JLabel("Server ip: "+InetAddress.getLocalHost().getHostAddress(), JLabel.CENTER );
        frame.getContentPane().add( label );
    
        JLabel lblNewLabel = new JLabel("");
        lblNewLabel.setIcon(new ImageIcon("voip.png"));
        frame.getContentPane().add(lblNewLabel, BorderLayout.EAST);
    
        frame.setSize(600,400);
        frame.setVisible(true);
        ImageIcon img = new ImageIcon("icon.png");
        frame.setIconImage(img.getImage());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        try {  
            audioFormat = getAudioFormat();
            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
            sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
            sourceDataLine.open(audioFormat);
            sourceDataLine.start();
            serverSocket = new ServerSocket(500);
            clientSocket = serverSocket.accept();
            captureAudio();
            input = new BufferedInputStream(clientSocket.getInputStream());
            out = new BufferedOutputStream(clientSocket.getOutputStream());
            while (input.read(tempBuffer) != -1) {
                sourceDataLine.write(tempBuffer, 0, Size);
            }
            serverSocket.close();
            clientSocket.close();
        } catch (IOException e) {
            System.out.println("Server Exception: " + e);
        }
    }

    private AudioFormat getAudioFormat() {
        float sampleRate = 8000;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false;
        return new AudioFormat(sampleRate,sampleSizeInBits,channels,signed,bigEndian);
    }

    public static void main(String s[]) throws LineUnavailableException, UnknownHostException {
        Serverx s2 = new Serverx();
        s2.notify();
    }

    private void captureAudio() {
        try {
            audioFormat = getAudioFormat();
            DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
            Mixer mixer;
            System.out.println("Server Ip Address "+InetAddress.getLocalHost().getHostAddress());
            System.out.println("Available Hardware Devices from server:");
            for (int cnt = 0; cnt < mixerInfo.length; cnt++) {
                mixer = AudioSystem.getMixer(mixerInfo[3]);      
                if (mixer.isLineSupported(dataLineInfo)) {
                    System.out.println(cnt+":"+mixerInfo[cnt].getName());
                    targetDataLine = (TargetDataLine) mixer.getLine(dataLineInfo);
                }
            }
            targetDataLine.open(audioFormat);
            targetDataLine.start();

            Thread captureThread = new CaptureThread();
            captureThread.start();
        } catch (UnknownHostException | LineUnavailableException e) {
            System.out.println("captureAudio Server Exception: " + e);
            System.exit(0);
        }
    }

    class CaptureThread extends Thread {

        byte tempBuffer[] = new byte[Size];

        @Override
        public void run() {
            try {
                while (true) {
                    int cnt = targetDataLine.read(tempBuffer, 0, tempBuffer.length);
                    out.write(tempBuffer);
                    out.flush();
                    System.out.println(cnt);
                }
            } catch (IOException e) {
                System.out.println("CaptureThread Server Exception: " + e);
                System.exit(0);
            }
        }
    }
}