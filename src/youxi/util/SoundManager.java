package youxi.util;

import javax.sound.sampled.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 游戏音效管理器 — 合成简短 PCM 音效，无需外部文件。
 */
public class SoundManager {

    private static final float SAMPLE_RATE = 22050;
    private static final int BITS = 16;
    private static final int CHANNELS = 1;
    private static final AudioFormat FORMAT =
            new AudioFormat(SAMPLE_RATE, BITS, CHANNELS, true, false);

    private static Clip clickClip, correctClip, wrongClip, comboClip, timeoutClip, victoryClip;
    private static boolean muted;
    private static float volume = 0.7f;

    public static void init() {
        clickClip   = makeClip(clickSamples());
        correctClip = makeClip(correctSamples());
        wrongClip   = makeClip(wrongSamples());
        comboClip   = makeClip(comboSamples());
        timeoutClip = makeClip(timeoutSamples());
        victoryClip = makeClip(victorySamples());
    }

    public static void setMuted(boolean m)  { muted = m; }
    public static boolean isMuted()         { return muted; }
    public static void setVolume(float v)   { volume = Math.max(0, Math.min(1, v)); }

    public static void click()   { play(clickClip); }
    public static void correct() { play(correctClip); }
    public static void wrong()   { play(wrongClip); }
    public static void combo()   { play(comboClip); }
    public static void timeout() { play(timeoutClip); }
    public static void victory() { play(victoryClip); }

    // ── 内部实现 ──

    private static void play(Clip clip) {
        if (muted || clip == null) return;
        clip.setFramePosition(0);
        FloatControl fc = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        float dB = 20f * (float) Math.log10(volume);
        fc.setValue(Math.max(fc.getMinimum(), Math.min(fc.getMaximum(), dB)));
        clip.start();
    }

    private static Clip makeClip(byte[] samples) {
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(FORMAT, samples, 0, samples.length);
            return clip;
        } catch (Exception e) {
            System.err.println("[Sound] 初始化失败: " + e.getMessage());
            return null;
        }
    }

    // ═══════════════════════════════════════
    // 合成音效
    // ═══════════════════════════════════════

    private static byte[] clickSamples() {
        // 极短白噪声脉冲，50ms
        int n = (int) (SAMPLE_RATE * 0.05);
        byte[] buf = new byte[n * 2];
        ByteBuffer bb = ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < n; i++) {
            double env = Math.exp(-i / (n * 0.15));
            short val = (short) (Short.MAX_VALUE * 0.6 * env * (Math.random() * 2 - 1));
            bb.putShort(val);
        }
        return buf;
    }

    private static byte[] correctSamples() {
        // C5→E5 双音升调，200ms
        int n = (int) (SAMPLE_RATE * 0.20);
        byte[] buf = new byte[n * 2];
        ByteBuffer bb = ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN);
        double f1 = 523.25, f2 = 659.25;
        for (int i = 0; i < n; i++) {
            double t = i / SAMPLE_RATE;
            double env = Math.exp(-t / 0.12);
            double freq = t < 0.08 ? f1 : f2;
            short val = (short) (Short.MAX_VALUE * 0.45 * env * Math.sin(2 * Math.PI * freq * t));
            bb.putShort(val);
        }
        return buf;
    }

    private static byte[] wrongSamples() {
        // 低音嗡嗡，300ms
        int n = (int) (SAMPLE_RATE * 0.30);
        byte[] buf = new byte[n * 2];
        ByteBuffer bb = ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < n; i++) {
            double t = i / SAMPLE_RATE;
            double env = Math.exp(-t / 0.20);
            double tone = Math.sin(2 * Math.PI * 180 * t) * 0.5
                        + Math.sin(2 * Math.PI * 220 * t) * 0.3
                        + Math.sin(2 * Math.PI * 150 * t) * 0.2;
            short val = (short) (Short.MAX_VALUE * 0.5 * env * tone);
            bb.putShort(val);
        }
        return buf;
    }

    private static byte[] comboSamples() {
        // 快速琶音 C5→E5→G5，250ms
        int n = (int) (SAMPLE_RATE * 0.25);
        byte[] buf = new byte[n * 2];
        ByteBuffer bb = ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN);
        double[] freqs = {523.25, 659.25, 783.99};
        for (int i = 0; i < n; i++) {
            double t = i / SAMPLE_RATE;
            double env = Math.exp(-t / 0.18);
            int seg = Math.min(2, (int) (t / 0.07));
            double freq = freqs[seg];
            short val = (short) (Short.MAX_VALUE * 0.4 * env * Math.sin(2 * Math.PI * freq * t));
            bb.putShort(val);
        }
        return buf;
    }

    private static byte[] timeoutSamples() {
        // 两下短促嘀嗒，400ms
        int n = (int) (SAMPLE_RATE * 0.40);
        byte[] buf = new byte[n * 2];
        ByteBuffer bb = ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < n; i++) {
            double t = i / SAMPLE_RATE;
            double env = 0;
            if (t < 0.06) env = Math.exp(-t / 0.02);
            else if (t > 0.18 && t < 0.24) env = Math.exp(-(t - 0.18) / 0.02);
            else if (t > 0.30 && t < 0.36) env = Math.exp(-(t - 0.30) / 0.02);
            short val = (short) (Short.MAX_VALUE * 0.5 * env * Math.sin(2 * Math.PI * 880 * t));
            bb.putShort(val);
        }
        return buf;
    }

    private static byte[] victorySamples() {
        // 大三和弦琶音 + 延音，600ms
        int n = (int) (SAMPLE_RATE * 0.60);
        byte[] buf = new byte[n * 2];
        ByteBuffer bb = ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN);
        double[] notes = {523.25, 659.25, 783.99, 1046.5};
        for (int i = 0; i < n; i++) {
            double t = i / SAMPLE_RATE;
            double env = Math.exp(-t / 0.35);
            int seg = Math.min(3, (int) (t / 0.10));
            double val = 0;
            for (int j = 0; j <= seg && j < notes.length; j++) {
                val += Math.sin(2 * Math.PI * notes[j] * t) * (0.25 / (j + 1));
            }
            short s = (short) (Short.MAX_VALUE * 0.5 * env * val);
            bb.putShort(s);
        }
        return buf;
    }
}
