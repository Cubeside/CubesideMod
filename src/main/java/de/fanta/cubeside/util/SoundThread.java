package de.fanta.cubeside.util;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

public class SoundThread extends Thread {

    private final long millis;
    private final SoundEvent sound;
    private final Player player;
    private boolean running;
    private final boolean soundPlaying;
    private boolean force;

    public SoundThread(long millis, SoundEvent sound, Player player) {
        super();
        this.millis = millis;
        this.sound = sound;
        this.player = player;
        this.soundPlaying = true;
    }

    public static synchronized SoundThread of(long millis, SoundEvent sound, Player player){
        return new SoundThread(millis, sound, player);
    }

    @Override
    public void run() {
        while (running) {
            if (soundPlaying) {
                player.playNotifySound(sound, SoundSource.PLAYERS, 100.0f, 1.0f);
                force = false;
            }
            try {
                if (!running) break;
                if (!force) {
                    Thread.sleep(millis);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public synchronized void start() {
        this.running = true;
        super.start();
    }

    public synchronized void stopThread() {
        this.running = false;
    }

    public boolean isRunning() {
        return running;
    }
}
