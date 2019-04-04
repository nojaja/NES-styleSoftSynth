package com.github.nojaja.sound.softsynth.oscillator.impl;

import com.github.nojaja.sound.softsynth.oscillator.AbstractTableWave;
import com.github.nojaja.sound.softsynth.oscillator.IToneOscillator;
import com.github.nojaja.sound.softsynth.oscillator.ToneBean;

/**
 * 矩形波Wave音源テーブルクラス
 *
 * 矩形波 音源テーブルを生成します。
 * @author nojaja
 * @version 0.1
 *
 */
public class RectWave extends AbstractTableWave {

	/*音源テーブル設定*/
	public int dutyLookups[][]= {//Waveテーブル
			{13,-13,-13,-13,-13,-13,-13,-13 },// Duty比12.5%
			{13, 13,-13,-13,-13,-13,-13,-13 },// Duty比25.0%
			{13, 13, 13, 13,-13,-13,-13,-13 },// Duty比50.0%
			{13, 13, 13, 13, 13, 13,-13,-13 },// Duty比75.0%
			
			{13,-13,-13,-13,-13,-13,-13,-13,  // Duty比12.5%
			 13, 13,-13,-13,-13,-13,-13,-13,  // Duty比25.0%
			 13, 13, 13, 13,-13,-13,-13,-13}  // Duty比50.0%
	};
	
	/**
	 * コンストラクタ
	 *
	 */
	public RectWave() {
		/*音源テーブル設定*/
		
		dutyLookup = new int[]{
				// 13, 13,  -13, -13// Duty比25%=75%
				//  2,3,3, 2,  -2, -3,-3,-2// Duty比25%=75%
				//  6,12,12,12, 12,11, -11, -12, -12,-12,-12,-6// Duty比25%=75%
				//  -2, -2,  -4, -4, -4, -3, -3, -3, -2, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0// Duty比25%=75%
				// -13, -13, -14,  -14, -14, -14, -14, -13,-13, -13, -12, -12, 0, 11, 11,11,11, 11, 11, 11, 0,0, 0, 0// Duty比25%=75%
				//-13, -14,  -14, -14, -14, -13, -13, -12, 0, 11, 11, 11, 11, 11, 0, 0// Duty比25%=75%

				 //13, 13,  -13, -13// Duty比50.0%

				 //13, 13,  13, -13// Duty比25.0%
				 //13, -13,  -13, -13// Duty比75.0%
				 
				 //13,-13,-13,-13,-13,-13,-13,-13// Duty比12.5%
				 13, 13, 13, 13, 13, 13,-13,-13// Duty比25.0%
				 //13, 13, 13, 13,-13,-13,-13,-13// Duty比50.0%
				 //13, 13,-13,-13,-13,-13,-13,-13// Duty比75.0%
				
/*
				 13,-13,-13,-13,-13,-13,-13,-13,// Duty比12.5%
				 13, 13, 13, 13, 13, 13,-13,-13,// Duty比25.0%
				 13, 13, 13, 13,-13,-13,-13,-13 // Duty比50.0%
*/				 
				 
/*
				 13, 13, 13, 13,-13,-13,-13,-13,// Duty比50.0%
				 13,-13,-13,-13,-13,-13,-13,-13// Duty比12.5%
*/				 
				 //Duty比 87.5% ,75.0% , 50.0% , 25.0%, 12.5%
		};

		/*音量調整*/
		for (int i = 0; i < dutyLookup.length; i++) {
			/* byte→int だと音量が小さくなるので32倍する*/
			dutyLookup[i]= -dutyLookup[i]*24;
			/*音量のピーク値を取得しておく*/
			dutyMax = Math.max(dutyMax, dutyLookup[i]);
		}
		for (int i = 0; i < dutyLookups.length; i++) {
			for (int j = 0; j < dutyLookups[i].length; j++) {
				/* byte→int だと音量が小さくなるので32倍する*/
				dutyLookups[i][j]= -dutyLookups[i][j]*24;
				/*音量のピーク値を取得しておく*/
				dutyMax = Math.max(dutyMax, dutyLookups[i][j]);
			}
		}
		
	}

	public int makeTone(byte[] _data,int sampleRate,ToneBean tone) {
		dutyLookup = dutyLookups[tone.getVariation()];
		return super.makeTone(_data, sampleRate, tone);
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
		return dutyLookup.length-1;
	}

}
