package com.einvik.cordova.plugin.azurespeech;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import com.microsoft.cognitiveservices.speech.audio.PullAudioInputStreamCallback;
import com.microsoft.cognitiveservices.speech.audio.AudioStreamFormat;

public class MicrophoneStream extends PullAudioInputStreamCallback {
    private final static int SAMPLE_RATE = 16000;
    private final AudioStreamFormat format;
    private AudioRecord recorder;
    

    public MicrophoneStream() {
        this.format = AudioStreamFormat.getWaveFormatPCM(SAMPLE_RATE, (short)16, (short)1);
        this.initMic();
    }

    public AudioStreamFormat getFormat() {
        return this.format;
    }

    @Override
    public int read(byte[] bytes) {
        long ret = this.recorder.read(bytes, 0, bytes.length);
        return (int)ret;
    }

    @Override
    public void close() {
        this.recorder.release();
        this.recorder = null;
    }

    private void initMic() {
        AudioFormat af = new AudioFormat.Builder()
                .setSampleRate(SAMPLE_RATE)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                .build();
        this.recorder = new AudioRecord.Builder()
                .setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
                .setAudioFormat(af)
                .build();
        this.recorder.startRecording();
    }
}