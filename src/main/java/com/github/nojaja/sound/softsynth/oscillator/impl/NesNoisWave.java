package com.github.nojaja.sound.softsynth.oscillator.impl;

import com.github.nojaja.sound.softsynth.oscillator.IToneOscillator;
import com.github.nojaja.sound.softsynth.oscillator.ToneBean;


/**
 * ノイズオシレータクラス
 * 
 * ノイズを生成します。
 * @author nojaja
 * @version 0.1
 *
 */
public class NesNoisWave implements IToneOscillator {

	//レジスタの初期値 （間違ってるかもしれない）
	int reg = 0x4000;
	boolean shortFreq = false;
	static final int [] noisePeriods = {
			0x004, 0x008, 0x010, 0x020, 0x040, 0x060, 0x080, 0x0A0,
			0x0CA, 0x0FE, 0x17C, 0x1FC, 0x2FA, 0x3F8, 0x7F2, 0xFE4
	};
	
	public NesNoisWave(boolean shortFreq) {
		super();
		this.shortFreq = shortFreq;
	}
	/* (non-Javadoc)
	 * 波形変更/作成 作成したデータの長さを返す
	 * @see game.system.sound.softsynth.oscillator.IToneOscillator#makeTone(byte[])
	 */
	public int makeTone(byte[] _data,int sampleRate,ToneBean tone) {

		int length = sampleRate;//ネイティブに変数ロード
		int toneStep = tone.toneStep;//ネイティブに変数ロード

		double f = 0;//生成データ一時格納変数（音量）

		/* ノイズ周期の設定（短いほど高音になる）*/
		//int _amplitude = (int) 50-tone.getNote();
		//int _amplitude = (int) tone.getNote()-30;
		//int _amplitude = (int) (50-tone.getNote());

		int _amplitude = noisePeriods [tone.getNote() % 16];

		/*ベロシティー設定*/
		double _velocity = (byte)((tone.velocity+tone.expression)/15);

		byte y = 0;
		for(int i = 0; i < length;  i =i+2 ){
			toneStep++;

			/*toneStepが_amplitudeを超えたら音量を変更する*/
			if(toneStep > _amplitude){
				/*音量データ生成*/
				//f = (- _velocity/2+(Math.random()* _velocity));

				//以下の3行を回す。
				//　shortFreqは、短周期フラグ。1にすると有効になる。
				//　これで得られるoutputの値が、ノイズチャンネルの波形。
				reg >>= 1;
				reg |= ((reg ^ (reg >> (shortFreq ? 6 : 1))) & 1) << 14;
				f = (- _velocity/2+((reg & 1)* _velocity));
				
				toneStep = 0;
			}

			y = (byte)(_data[i] + f);
			/* Lch 分*/
			_data[i] =  y;
			/* Rch 分*/
			_data[i+1] =  y;

		}
		tone.toneStep = toneStep;
		return length;
	}



	/**(non-Javadoc)
	 * @see com.github.nojaja.sound.softsynth.oscillator.IToneOscillator#isToneSync()
	 */
	public boolean isToneSync() {
		return true;
	}

	/**(non-Javadoc)
	 * @see com.github.nojaja.sound.softsynth.oscillator.IToneOscillator#toneLoopPoint()
	 */
	public int toneLoopPoint() {
		return 0;
	}

	/**(non-Javadoc)
	 * @see com.github.nojaja.sound.softsynth.oscillator.IToneOscillator#toneEndPoint()
	 */
	public int toneEndPoint() {
		return -1;
	}


}
