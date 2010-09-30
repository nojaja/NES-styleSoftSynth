package com.github.nojaja.sound.softsynth.oscillator;


import java.awt.Component;
import java.util.Arrays;
import java.util.Vector;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * AbstractToneChannel.java<br>
 * 管理しているオシレータの音声生成ロジック呼び出しとAudioへの出力を行う
 * @author nojaja
 * @version 0.1
 *
 */
public abstract class AbstractToneChannel extends Thread {

	//static int SAMPLE_RATE = 11025; // サンプルレート
	//static int SAMPLE_RATE = 22050; // サンプルレート
	//static int SAMPLE_RATE = 44100; // サンプルレート
	public static final float SAMPLE_RATE = 44100.0f;

	public static final boolean Sample16Bits = true;
	public static final int   SAMPLE_SIZE = (Sample16Bits)?16:8;
	public static final int   CHANNEL     = 2;
	public static final int   FrameSize     = CHANNEL *(SAMPLE_SIZE/8);


	static AudioFormat FORMAT = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE, CHANNEL, true, true); // 再生形式


	// 1フレームで再生するバイト数を計算する
	//static int BUF_SIZE = SAMPLE_RATE / 50; // バッファサイズ(少ないほうが反応が良いが音飛びしやすい)
	//static int BUF_SIZE = SAMPLE_RATE / 90; // バッファサイズ(少ないほうが反応が良いが音飛びしやすい)
	public static int BUF_SIZE = ((int)SAMPLE_RATE /50 * FrameSize) ;
	public byte[] _waveData = null;
	//public float flameSize = 0;
	private static long last;

	public Component app = null;
	private int printWait = 0;

	//public byte[] data = new byte[BUF_SIZE]; // 波形データ

	public SourceDataLine line = null;	// 出力ライン

	public boolean isRunning;			// 動作中なら真

	/*オシレータ	 */
	public IToneOscillator toneOsc ;

	/*音を管理するリスト	 */
	public Vector<ToneBean> toneList = new Vector<ToneBean>();

	/**
	 * コンストラクタ
	 */
	public AbstractToneChannel () {
		this(1);
	}

	/**
	 * コンストラクタ<br>
	 * 
	 * @param _frequency　初期周波数（適当でOK）
	 */
	public AbstractToneChannel (float _frequency) {
		// ライン情報取得
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, FORMAT, BUF_SIZE);
		//DataLine.Info info = new DataLine.Info(SourceDataLine.class, FORMAT, AudioSystem.NOT_SPECIFIED);
		if(!AudioSystem.isLineSupported(info)){
			System.out.println("Line type not supported: "+info);
		}
		try {
			line = (SourceDataLine)AudioSystem.getLine(info); // 出力ライン取得
			line.open();
			line.flush();
		} catch(LineUnavailableException e) {
			e.printStackTrace();
			return;
		}
		this.setPriority(MAX_PRIORITY);
	}

	/**
	 * 音声バッファ生成処理<br>
	 * toneListに登録されているすべての音色データに対してmakeToneを実行する<br>
	 * @param _data 音声バッファ
	 * @param bufSize 必要なバッファサイズ
	 * @return 生成したバッファのサイズ
	 */
	public synchronized int makeTone(byte[] _data,int bufSize){			// 波形変更/作成 作成したデータの長さを返す
		int length = Math.min(bufSize, _data.length);
		/*出力バッファの初期化*/
		Arrays.fill(_data, (byte)0x00);

		for (int i = 0; i < toneList.size(); i++) {
			try{
				ToneBean tone = (ToneBean)toneList.get(i);
				toneOsc.makeTone(_data,length,tone);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return length;
	}


	/**
	 * 1フレームで再生するバイト数を計算する予定
	 * @param milliseconds 1フレームの時間
	 */
	public int calculateSampleRate(long milliseconds) {
		//		System.out.println("      Channels  : " + format.getChannels());
		//		System.out.println("      SampleRate: " + format.getSampleRate());
		//		System.out.println("SampleSizeInBits: " + format.getSampleSizeInBits());
		int ret = (int)(  milliseconds / 1000  *  BUF_SIZE );
		//System.out.println("      frame time: " + milliseconds / 1000);
		//System.out.println("      SampleRate: " + ret);
		return ret;
		//		System.out.println("      SampleRate: " + sampleRate);
		//		System.out.println();
	}

	/**
	 * バッファ再生処理
	 */
	public void run() {
		//long current = System.currentTimeMillis();
		isRunning = true;
		line.start();
		byte[] waveData = new byte[BUF_SIZE]; // 波形データ
		_waveData = new byte[BUF_SIZE]; // 波形データ

		int sampleRate = BUF_SIZE ;
		//System.out.println("BUF_SIZE:"+BUF_SIZE);
		while (isRunning) {
			try
			{
				//current = System.currentTimeMillis();
				// 前回の呼び出しからの経過時刻
				////long difference = (long)(current - last);
				// サンプルレートを計算する
				//int sampleRate = calculateSampleRate(difference);
				//System.out.println(sampleRate);//2940
				//int sampleRate = BUF_SIZE/90 ;
				//Thread.sleep(10);
				int length = makeTone(waveData,sampleRate);			// 再生するたびにデータが壊れるので作り直す
				//System.out.println(length);
				if(printWait%2==0){
					repaintBuf(waveData);
					printWait=0;
				}
				printWait++;

				line.write(waveData, 0, length);

				//last = current;
			}catch (Exception ex){
				ex.printStackTrace();	
			}
		}
		line.close();
	}

	protected void off() {				// 再生終了
		isRunning = false;
	}



	/**
	 * ボリュームの設定
	 * @param volume　0 から 1 までの小数
	 */
	public void setVolume(float volume) {	// 音量設定 v は 0 から 1 までの小数
		// linearScalar = pow(10.0, gainDB/20.0) なのでややこしい
		FloatControl control = (FloatControl)line.getControl(FloatControl.Type.MASTER_GAIN);
		double max = Math.pow(10.0, control.getMaximum()/20.0);
		double min = Math.pow(10.0, control.getMinimum()/20.0);
		double newValue = (max - min) * (volume * volume) + min;
		newValue = 20 * Math.log(newValue) / Math.log(10);
		control.setValue((float) newValue);
	}

	/**
	 * pan設定処理
	 * @param pan 設定する pan 0～127
	 */
	public void setPan(int pan) {
		try {
			// 0 ~ 64 ~ 127
			//-1 ~  0 ~ 1

			FloatControl control = (FloatControl)line.getControl(FloatControl.Type.PAN);

			float _pan = ((float)pan - 64)/127;

			//System.out.println("PAN "+_pan +" " + pan +" ");
			control.setValue(_pan);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * リバーブ設定処理
	 * @param reverb リバーブの設定をします 0～127
	 * 
	 */
	public void setReverb_ret(int reverb) {
		try {
			// 0 ~ 64 ~ 127
			//-1 ~  0 ~ 1

			FloatControl control = (FloatControl)line.getControl(FloatControl.Type.REVERB_RETURN);

			float _reverb = ((float)reverb - 64)/127;

			//System.out.println("REVERB_RETURN "+_reverb +" " + reverb +" ");
			control.setValue(_reverb);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * 表示用バッファ更新処理<br>
	 * 生成したバッファを表示用バッファにコピーし、<br>
	 * appに対してrepaintを実行させる。
	 * @param _data
	 */
	public void repaintBuf(byte[] _data) {
		if(app == null)return;
		try{
			System.arraycopy(_data, 0, _waveData, 0, _data.length);
			app.repaint();
		}catch(Exception e){

		}
	}

}


