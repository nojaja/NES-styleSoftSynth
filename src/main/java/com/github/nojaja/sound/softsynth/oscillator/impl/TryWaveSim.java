package com.github.nojaja.sound.softsynth.oscillator.impl;

import com.github.nojaja.sound.softsynth.oscillator.IToneOscillator;
import com.github.nojaja.sound.softsynth.oscillator.ToneBean;


/**
 * OrgRectWave.java
 * サイン波を出力(繰り返して再生)
 * @author nojaja
 * @version 0.1
 *
 */

public class TryWaveSim implements IToneOscillator {


	/* (non-Javadoc)
	 * 波形変更/作成 作成したデータの長さを返す
	 * @see game.system.sound.softsynth.oscillator.IToneOscillator#makeTone(byte[])
	 */
	public int makeTone(byte[] _data,int sampleRate,ToneBean tone) {


		int length = sampleRate;//ネイティブに変数ロード
		int toneStep = tone.toneStep;//ネイティブに変数ロード
		byte _overallLevel = (byte)(tone.overallLevel);//ネイティブに変数ロード

		byte y = 0;//生成データ一時格納変数（音量）

		/* 周期の設定（短いほど高音になる）*/
		int _amplitude = (int) tone.amplitude;

		for(int i = 0; i < length;  i =i+2 ){
			toneStep++;

			double f = (1.0 * toneStep / _amplitude) - (toneStep / _amplitude);

			if (f < 0.5) {
				y =  (byte)( (_data[i] +( - _overallLevel + (_overallLevel*(f/1)*4)) ) );
			} else {
				y =  (byte)( (_data[i] + ( _overallLevel*3 - (_overallLevel*(f/1)*4)) ) );
			}
			//			if (f > 0.5) {
			//				y =  (byte)( (_data[i] +(-_overallLevel + (_overallLevel*(f/1)*4)) )/2 );
			//			} else {
			//				y =  (byte)( (_data[i] + (_overallLevel - (_overallLevel*(f/1)*4)) )/2 );
			//			}

			/* Lch 分*/
			_data[i] =  y;
			/* Rch 分*/
			_data[i+1] =  y;

		}
		tone.toneStep = toneStep;
		return length;
	}

	/**(non-Javadoc)
	 * @see IToneOscillator
	 */
	public boolean isToneSync() {
		return true;
	}

	/**(non-Javadoc)
	 * @see IToneOscillator
	 */
	public int toneLoopPoint() {
		return 0;
	}

	/**(non-Javadoc)
	 * @see IToneOscillator
	 */
	public int toneEndPoint() {
		return -1;
	}

}
