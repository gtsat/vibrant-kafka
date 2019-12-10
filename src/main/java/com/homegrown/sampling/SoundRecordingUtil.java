package com.homegrown.sampling;

import org.apache.log4j.Logger;
import java.io.File;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;


class SoundRecordingUtil {
	private static final Logger logger = Logger.getLogger (SoundRecordingUtil.class);

	private static final int BUFFER_SIZE = 4096;
	private ByteArrayOutputStream recordBytes;
	private TargetDataLine audioLine;
	private AudioFormat format;
 
	private boolean isRunning;

	AudioFormat getAudioFormat(float sampleRate) {
		int channels = 1;
		boolean signed = true;
		boolean bigEndian = false;
		return new AudioFormat(sampleRate,8,channels,signed,bigEndian);
	}
 
	/**
	 * Start recording sound.
	 * @throws LineUnavailableException if the system does not support the specified
	 * audio format nor open the audio data line.
	 */
	public void start (float sampleRate) throws LineUnavailableException {
		format = getAudioFormat (sampleRate);
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
 
		// checks if system supports the data line
		if (!AudioSystem.isLineSupported(info)) {
			throw new LineUnavailableException("%% The system does not support the specified format.");
		}
 
		audioLine = AudioSystem.getTargetDataLine(format);
		audioLine.open(format);
		audioLine.start();
 
		byte[] buffer = new byte[BUFFER_SIZE];
		recordBytes = new ByteArrayOutputStream();
		isRunning = true;
 
		while (isRunning) {
			int bytesRead = audioLine.read(buffer, 0, buffer.length);
			recordBytes.write(buffer, 0, bytesRead);
		}
	}
 
	/**
	 * Stop recording sound.
	 * @throws IOException if any I/O error occurs.
	 */
	public void stop() throws IOException {
		isRunning = false;
		if (audioLine != null) {
			audioLine.drain();
			audioLine.close();
		}
	}
 
	/**
	 * Save recorded sound data into a .wav file format.
	 * @param wavFile The file to be saved.
	 * @throws IOException if any I/O error occurs.
	 */
	public void save(File wavFile) throws IOException {
		byte[] audioData = recordBytes.toByteArray();
		System.err.println ("%% Now dumping "+audioData.length+" bytes into "+wavFile.getAbsoluteFile()+"...");
		ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
		AudioInputStream audioInputStream = 
				new AudioInputStream(bais,format,audioData.length/format.getFrameSize());
		AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, wavFile);
		audioInputStream.close();
		recordBytes.close();
	}

	public byte[] getBytes () throws IOException {
		byte[] audioData = recordBytes.toByteArray();
		recordBytes.close();
		return audioData;
	}

	public int[] extractInt () throws IOException {
		int counter = 0;
		byte[] audioData = recordBytes.toByteArray();
		int[] output = new int [audioData.length];
		for (int i=0; i<audioData.length; ++i) {
			output [counter++] = audioData[i];
		}
		recordBytes.close();
		return output;
	}
}

