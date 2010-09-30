package com.github.nojaja.sound.softsynth.oscillator.impl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.github.nojaja.sound.softsynth.oscillator.AbstractTableWave;


/**
 * Wave音源テーブルクラス
 * 
 * Waveデータを取り込んで音源テーブルを生成します。
 * @author nojaja
 * @version 0.1
 *
 */
public class DataClipWave extends AbstractTableWave {


	/**
	 * インナーコンストラクタ
	 */
	private DataClipWave() {
		dutyLookup = new int[]{};
		dutyMax = 0;
	}

	/**
	 * コンストラクタ
	 * @param fileName 音源ファイルのパス
	 */
	public DataClipWave(String fileName) {
		this();
		try {
			load("a",fileName);
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}


	/**
	 * 音源WAVEファイルをロード
	 * @param title 未使用
	 * @param url 音源WAVEファイルのURL
	 */
	private  void load(String title,URL url) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		// オーディオストリームを開く
		AudioInputStream ais = AudioSystem.getAudioInputStream(url);
		// WAVEファイルのフォーマットを取得
		AudioFormat format = ais.getFormat();

		/* ファイル情報表示 */
		System.out.println("getFrameLength="+ais.getFrameLength());
		System.out.println("getFrameSize="+format.getFrameSize());
		System.out.println("getSampleRate="+format.getSampleRate());
		System.out.println("getSampleSizeInBits="+format.getSampleSizeInBits());
		System.out.println("getEncoding="+format.getEncoding().toString());
		System.out.println("isBigEndian="+format.isBigEndian());

		// WAVEファイルの大きさを求める
		int length = (int)(ais.getFrameLength() * format.getFrameSize());
		// 処理用のbyte配列を用意
		byte[] _dutyLookup = new byte[length];
		/*16bitか判断用フラグ*/
		boolean is16bit = (format.getSampleSizeInBits()==16)?true:false;

		if(is16bit){
			/*2byteをintに変換するためサイズは半分になる*/
			dutyLookup = new int[length/2];
		}else{
			dutyLookup = new int[length];
		}

		// 一時領域にWAVEデータを格納する
		ais.read(_dutyLookup);

		dutyMax = 0;
		int i = 0;
		int j = 0;
		while( i < dutyLookup.length){
			if(is16bit){
				if(format.isBigEndian()){
					dutyLookup[i] =((_dutyLookup[j++] << 8) | (_dutyLookup[j++] & 0xff))/8;
				}else{
					dutyLookup[i] =((_dutyLookup[j++] & 0xff) | (_dutyLookup[j++] << 8))/8;
				}
			}else{
				/* 8bitのデータはUNSIGNなので-127だけオフセット調整する
				 * 更にbyte→int だと音量が小さくなるので32倍する*/
				dutyLookup[i] = (((_dutyLookup[j++]& 0xff) -127) *32);
			}
			/*音量のピーク値を取得しておく*/
			dutyMax = Math.max(dutyMax, dutyLookup[i]);
			i++;
		}
		System.out.println("dutyMax="+dutyMax);   
	}

	/**
	 * 音源WAVEファイルをロード
	 * @param title 未使用
	 * @param filename 音源WAVEファイルのパス
	 */
	public  void load(String title,String filename) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		//URL url = WaveEngine.class.getResource(filename);
		URL url = getURLFromFileName(filename);
		System.out.println(url.toString());
		load(title,url);
	}

	/**
	 * ファイルパスからURLを生成します。
	 * @param filename ファイルパス
	 * @return URL
	 */
	private URL getURLFromFileName(String filename) {
		try {
			char sep = File.separator.charAt(0);
			String file = filename.replace(sep, '/');
			if ((file.charAt(0) != '/')||(file.charAt(1) != ':')) {
				String dir = System.getProperty("user.dir");
				dir = dir.replace(sep, '/') + '/';
				if (dir.charAt(0) != '/') {
					dir = "/" + dir;
				}
				file = dir + file;
			}
			return (new URL("file", "", file));
		} catch (MalformedURLException e) {
			throw (new InternalError("can't convert from filename"));
		}
	}

	/**(non-Javadoc)
	 * @see com.github.nojaja.sound.softsynth.oscillator.IToneOscillator#isToneSync()
	 */
	public boolean isToneSync() {
		return false;
	}

	/**(non-Javadoc)
	 * @see com.github.nojaja.sound.softsynth.oscillator.IToneOscillator#toneLoopPoint()
	 */
	public int toneLoopPoint() {
		//return 15500;
		return 4630;
	}

	/**(non-Javadoc)
	 * @see com.github.nojaja.sound.softsynth.oscillator.IToneOscillator#toneEndPoint()
	 */
	public int toneEndPoint() {
		//return 16280;
		return 5689;
	}


}
