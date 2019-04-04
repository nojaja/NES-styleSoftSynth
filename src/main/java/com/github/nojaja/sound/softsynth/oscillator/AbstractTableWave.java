package com.github.nojaja.sound.softsynth.oscillator;


/**
 * Wave音源テーブル抽象クラス
 * 
 * 音源テーブルにセットされた波形データを出力(繰り返して再生)します
 * @author nojaja
 * @version 0.1
 *
 */
public abstract class AbstractTableWave implements IToneOscillator {

	public int[] dutyLookup;//Waveテーブル
	public int dutyMax;//Waveテーブル


	public AbstractTableWave() {
		dutyLookup = new int[]{
		};
	}

	/* (non-Javadoc)
	 * 波形変更/作成 作成したデータの長さを返す
	 * @see game.system.sound.softsynth.oscillator.IToneOscillator#makeTone(byte[])
	 */
	public int makeTone(byte[] _data,int sampleRate,ToneBean tone) {

		int length = sampleRate;//ネイティブに変数ロード
		byte _overallLevel = (byte)(tone.overallLevel);//ネイティブに変数ロード

		/*音量0の場合は即終了*/
		if(_overallLevel==0)return length;

		double stapTimingCounter = tone.stapTimingCounter;//ネイティブに変数ロード
		double tablePointer = tone.tablePointer;//ネイティブに変数ロード

		double _amplitude = tone.amplitude;//ネイティブに変数ロード
		int y = 0;//生成データ一時格納変数（音量）

		/*音源テーブルの再生速度設定　タイミング管理*/
		double stapTiming = _amplitude / (double)(dutyLookup.length-toneLoopPoint())-1;
		/*音源テーブルの再生速度設定2　ステップ管理*/
		double step = 1;

		/*音源テーブルが１周期以上のテーブルの場合*/
		if(0>stapTiming){
			/* 間引くためのステップ数を計算する　ここでは音源テーブルの元の音階はド(554Hz)固定とする*/
			step = (AbstractToneChannel.SAMPLE_RATE / 554.3652619537441)/_amplitude;
			//step = (AbstractToneChannel.SAMPLE_RATE / 200.45454545454547)/_amplitude;
			//step = (AbstractToneChannel.SAMPLE_RATE / 220)/_amplitude;
		}

		int i = 0;
		while(i < length){
			//Ｗａｖｅテーブルのポインタ
			if(stapTimingCounter >= stapTiming){
				//tone.tablePointer = (tone.tablePointer+step<=dutyLookup.length-1)?tone.tablePointer+step:0;
				//tone.tablePointer = (tone.tablePointer<dutyLookup.length-1)?tone.tablePointer+1:0;
				if(tablePointer+step<=toneEndPoint()){
					tablePointer = tablePointer+step;
				}else{
					int looppoint=toneLoopPoint();
					if(looppoint>=0){
						tablePointer = looppoint;
					}
				}

				stapTimingCounter=stapTimingCounter-stapTiming;//端数を繰り越しつつ初期化
				//tone.stapTimingCounter=0;//端数を切り捨てると音痴になるので廃止

			}else{
				stapTimingCounter ++;
			}
			//波形を書き込む　（ノーマライズしながら書き込み）
			y =  (int)( ( (dutyLookup[(int)tablePointer]*(_overallLevel)) )/2 );
			//y =  (int)( ( (dutyLookup[(int)tone.tablePointer]*(_overallLevel))/dutyMax )/2 );

			int _data_L = (_data[i] << 8) | (_data[i+1] & 0xff);
			int _data_R = (_data[i+2] << 8) | (_data[i+3] & 0xff);


			//y =  (byte)( (_data[i] + (dutyLookup[(int)tone.tablePointer]) )/2 );
			//_data[i] = (byte)(dutyLookup[tone.tablePointer]*_overallLevel);
			//			_data[i] = y;
			//			_data[i+1] = y;

			// int -> byte
			// Lch 分
			if(AbstractToneChannel.Sample16Bits){ 
				_data[i] = (byte) ((y+_data_L) >> 8);i++;
			}
			_data[i] = (byte) ((y+_data_L) & 0xff);i++;
			// Rch 分
			if(AbstractToneChannel.Sample16Bits){ 
				_data[i] = (byte) ((y+_data_R) >> 8);i++;
			}
			_data[i] = (byte) ((y+_data_R) & 0xff);i++;
		}

		tone.stapTimingCounter = stapTimingCounter;
		tone.tablePointer = tablePointer;
		return length;
	}






}
