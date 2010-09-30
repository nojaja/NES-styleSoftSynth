package com.github.nojaja.sound.softsynth.oscillator;
/**
 * IToneOscillator.java<br>
 * 波形生成ロジックインタフェース
 * @author nojaja
 * @version 0.1
 *
 */
public interface IToneOscillator {

	/**<PRE>
	 * 音色生成メソッド
	 * このメソッドにて音色データを生成してください。
	 * _dataは他のmakeToneにて生成したデータがセットされた状態で
	 * 渡されるので 生成したデータは_dataに対して加算をしながらセットしてください。
	 * 
	 * 
	 * _dataのフォーマットは
	 * SampleRate：44100
	 * Channels：1
	 * SampleSizeInBits：16
	 * で生成すること
	 * </PRE>
	 * @param _data 再生データ
	 * @param bufSize バッファサイズ
	 * @param tone 音色データ
	 * @return 生成したデータサイズ
	 * 
	 * 
	 */
	public int makeTone(byte[] _data,int bufSize,ToneBean tone);


	/**
	 * 発音時に再生ポインタを初期化せずに続きから再生するか？<br>
	 * （ループ音源はtrue推奨）
	 * @return true：続きから再生
	 */
	public boolean isToneSync();
	

	/**
	 * 音源データのループ開始ポイントを返す<br>
	 * （未設定の場合は0を返す）
	 */
	public int toneLoopPoint();
	

	/**
	 * 音源データのループ終了ポイントを返す<br>
	 * （未設定の場合は音源データのサイズを返す）
	 */
	public int toneEndPoint();
}