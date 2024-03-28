package com.andorangutan.snake;

import android.content.Context;
import android.os.Build;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.IOException;

public class Audio {

    private SoundPool soundPool;

    public Audio() {
        // Initialize the SoundPool
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            soundPool = new SoundPool.Builder()
                    .setMaxStreams(5)
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else {
            soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        }
    }

    public int load(Context context, String file) {
        try {
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;

            // Prepare the sounds in memory
            descriptor = assetManager.openFd(file);
            return this.soundPool.load(descriptor, 0);
        } catch (IOException e) {
            // Error
            Log.e("LoadSound", "Error loading sound file: " + file, e);
            return -1;
        }
    }

    public void play(int id) {
        this.soundPool.play(id, 1, 1, 0, 0, 1);
    }
}

