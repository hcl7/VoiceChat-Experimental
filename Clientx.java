package clientx;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**`
 *
 * @author seven
 */
public class Clientx {

    boolean stopCapture = false;
    ByteArrayOutputStream byteArrayOutputStream;
    AudioFormat audioFormat;
    TargetDataLine tdl;
    AudioInputStream audioInputStream;
    BufferedOutputStream out = null;
    BufferedInputStream in = null;
    Socket sock = null;
    private Socket socket = null;
    SourceDataLine sdl;
    int Size = 10000;
    /**
     * Launch the application.
     * @param args
     */
    
    public static void main(String[] args) {
        JFrame.setDefaultLookAndFeelDecorated(true);
        JFrame frame = new JFrame("Network Phone Client");
        JPanel pnl = new JPanel();
        JLabel lip = new JLabel("IP:");
        JTextField txtip = new JTextField(20);
        txtip.setText("127.0.0.1");
        JButton btnCall = new JButton("Call Server");
        btnCall.setForeground(Color.green);
        frame.setSize(600,80);
        frame.setVisible(true);
        ImageIcon img = new ImageIcon("icon.png");
        frame.setIconImage(img.getImage());
        pnl.add(lip);
        pnl.add(txtip);
        pnl.add(btnCall);
        frame.getContentPane().add(pnl);
        
        btnCall.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Clientx tx = new Clientx();
                tx.captureAudio(txtip.getText(), 500);
                btnCall.setText("Close");
                btnCall.setForeground(Color.red);
                if (e.getActionCommand().equals("Close")){
                    System.exit(0);
                }
            }
        });
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
    
    public Socket getSocket(String ip, int port) throws IOException{
        try{
            socket = new Socket(ip, port);
        }
        catch (SocketException e){
            System.out.println("getSocket IOException: " + e);
	    System.exit(0);
        }
        return socket;
    }
    
    public void captureAudio(String ip, int port) {
        System.out.println("Available Hardware devices:");
        Audio aobj = new Audio();
        aobj.displayMixerInfo();
        try {
            sock = getSocket(ip, port);
            out = new BufferedOutputStream(sock.getOutputStream());
            in = new BufferedInputStream(sock.getInputStream());
            aobj.setAudioFormat(8000, 16, 1);
            audioFormat = aobj.getAudioFormat();
            DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
            Mixer mixer = aobj.getMicrophone();    
            tdl = (TargetDataLine) mixer.getLine(dataLineInfo);
            tdl.open(audioFormat);
            tdl.start();
            Thread captureThread = new CaptureThread();
            captureThread.start();
            DataLine.Info dataLineInfo1 = new DataLine.Info(SourceDataLine.class, audioFormat);
	    sdl = (SourceDataLine) AudioSystem.getLine(dataLineInfo1);
	    sdl.open(audioFormat);
	    sdl.start();
            Thread playThread = new PlayThread();
            playThread.start();
	} catch (IOException | LineUnavailableException e) {
            System.out.println("captureAudio Exception: " + e);
	    System.exit(0);
        }
    }
	class CaptureThread extends Thread {
	    byte tempBuffer[] = new byte[Size];
	    @Override
	    public void run() {
	        byteArrayOutputStream = new ByteArrayOutputStream();
	        stopCapture = false;
	        try {
	            while (!stopCapture) {
	                int cnt = tdl.read(tempBuffer, 0, tempBuffer.length);
	                out.write(tempBuffer);
	                if (cnt > 0) {
	                    byteArrayOutputStream.write(tempBuffer, 0, cnt);
	                }
	            }
	            byteArrayOutputStream.close();
	        } catch (IOException e) {
                    System.out.println("CaptureThread Exception: " + e);
	            System.exit(0);
	        }
	    }
	}
	
	class PlayThread extends Thread {
	    byte tempBuffer[] = new byte[Size];
	    @Override
	    public void run() {
	        try {
	            while (in.read(tempBuffer) != -1) {
	                sdl.write(tempBuffer, 0, Size);
	            }
	            sdl.drain();
	            sdl.close();
	        } catch (IOException e) {
                    System.out.println("PlayThread Exception" + e);
	        }
	    }
	}
}
