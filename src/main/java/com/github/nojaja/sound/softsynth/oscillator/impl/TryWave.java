package com.github.nojaja.sound.softsynth.oscillator.impl;

import com.github.nojaja.sound.softsynth.oscillator.AbstractTableWave;
import com.github.nojaja.sound.softsynth.oscillator.IToneOscillator;



/**
 * 三角波Wave音源テーブルクラス
 * 
 * 三角波 音源テーブルを生成します。
 * @author nojaja
 * @version 0.1
 *
 */
public class TryWave extends AbstractTableWave {

	/**
	 * コンストラクタ
	 * 
	 */
	public TryWave() {
		/*音源テーブル設定*/
		dutyLookup = new int[]{
				//   0,  1,    2,   3,   4,   5,   6,   7,   8,   9,  10,  11,  12,  13,  14,  15,  
				// -64,  64,  64,  64,  64, -64, -64, -64
				// -64, -48, -32, -16,   0,  16,  32,  48,  64,  48,  32,  16,   0, -16, -32, -48//16
				// -64, -64, -64, -64, -64, -64, -64, -64,  64,  64,  64,  64,  64,  64,  64,  64//16
				//   -64, -56, -48, -40, -32, -24, -16,  -8,   0,   8,  16,  24,  32,  40,  48,  56
				//   -1, -1, -1, -1, -1, -1, -1, -1,  1,  1,  1,  1,  1,  1,  1,  1//16
				//  -1,  1//16
				//   -1,  -1,  -1,  -1, 1,  1,  1,1//16

				-8, -7, -6, -5, -4, -3, -2,  -1,   0,   1,  2,  3,  4,  5,  6,  7, 8 ,7 ,6 ,5 ,4 ,3 ,2 ,1 ,0 ,-1 ,-2 ,-3 ,-4 ,-5 ,-6 ,-7
		};
		/*音量調整*/
		for (int i = 0; i < dutyLookup.length; i++) {
			/* byte→int だと音量が小さくなるので32倍する*/
			//dutyLookup[i]= dutyLookup[i]*32;
			dutyLookup[i]= dutyLookup[i]*64;
			/*音量のピーク値を取得しておく*/
			dutyMax = Math.max(dutyMax, dutyLookup[i]);
		}
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
