package clientx;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;

/**
 *
 * @author seven
 */
public class Audio {
    
    //private class variable;
    final private Mixer.Info [] mixersInfo = AudioSystem.getMixerInfo();
    
    //display mixer information based on name, description and vendor of hardware device of machine;
    public void displayMixerInfo()
    {
        for (int i = 0; i < mixersInfo.length; i++){
            System.out.println(i+": " + mixersInfo[i].getName()+ ": " + mixersInfo[i].getDescription() + ": " + mixersInfo[i].getVendor()+")");
        }
    }
    
    //get microphone index that is different in every machine that is using onboard microphone;
    public int getMicrophoneIndex(){
        int ind = 0;
        for (int cnt = 0; cnt < mixersInfo.length; cnt++) {
            String mname = mixersInfo[cnt].getName().substring(0, 10);
            if (mname.equals("Microphone")){
                ind = cnt;
            }
        }
        return ind;
    }
    
    //get system audio mixer based on microphone index using getMicrophoneIndex() function;
    public Mixer getMicrophone(){
        return AudioSystem.getMixer(mixersInfo[getMicrophoneIndex()]);
    }
    
    //this function returns all mixer information of audio system of machine;
    public Mixer.Info [] getMixerInfo(){
        return mixersInfo;
    } 
    
    //set audio format (sampleRate=8000, sampleSizeInBits=16, channels=1) 
    public void setAudioFormat(float _sr, int _ss, int _chl) {
	this.sampleRate = _sr;
	this.sampleSizeInBits = _ss;
	this.channels = _chl;
    }
    
    //get audio format using for communication between devices;
    public AudioFormat getAudioFormat(){
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }
    
    //private class variables;
    private float sampleRate;
    private int sampleSizeInBits;
    private int channels;
    final private boolean signed = true;
    final private boolean bigEndian = false;
}
