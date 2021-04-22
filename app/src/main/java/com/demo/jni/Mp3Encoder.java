package com.demo.jni;

public class Mp3Encoder {
    static {
        System.loadLibrary("mp3encoder");
    }

   public native int init(
           String pcmPath,
           int channel,
           int bitRate,
           int sampleRate,
           String mp3Path);

    public native void encode();

    public native void destroy();
}
