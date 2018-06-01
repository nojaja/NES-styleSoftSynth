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

public class SinWave implements IToneOscillator {



	/* (non-Javadoc)
	 * 波形変更/作成 作成したデータの長さを返す
	 * @see game.system.sound.softsynth.oscillator.IToneOscillator#makeTone(byte[])
	 */
	public int makeTone(byte[] _data,int sampleRate,ToneBean tone) {			// 波形変更/作成 作成したデータの長さを返す
		int _amplitude = (int) tone.amplitude;
		//int length = _data.length;
		int length = sampleRate;
		//		System.out.println(length);
		for(int i = 0; i < length; i++){
			tone.toneStep++;
			double f = (1.0 * tone.toneStep / _amplitude) - (tone.toneStep / _amplitude);
			//↑間違ってる
			//ノコギリの波形が出ている

			//_data[i] += (byte)(Math.sin(f)*overallLevel);
			_data[i] += (byte)(Math.sin(f)*tone.overallLevel);

		}
		return length;
	}

	public boolean isToneSync() {
		// TODO Auto-generated method stub
		return true;
	}
	public int toneLoopPoint() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int toneEndPoint() {
		// TODO Auto-generated method stub
		return -1;
	}
}
