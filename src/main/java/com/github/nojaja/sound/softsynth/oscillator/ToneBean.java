package com.github.nojaja.sound.softsynth.oscillator;

/**
 * 音色クラス<br>
 * 発音させる音色の各種パラメータを管理するクラスです。
 * 
 * @author nojaja
 *
 */
public class ToneBean {

	public double frequency;				// 周波数
	public double amplitude;				// 波長(バイト数)


	private float concert_pitch = 440;
	private int note = 1;
	private float pitch = 0;
	private float vibrato = 0;
	
	//private float vibratoDepth = 2; 
	private float vibratoDepth = 0; 
	private double vibratoRate = 0.5; 
	private double vibratoDelay = 60; 
	private double vibratoCount = 0; 
	

	public int overallLevel = 0;			// (ベロシティ値 ÷ 8) × (エクスプレッション値 ÷ 127)
	public int velocity = 60;				// ベロシティ
	public int expression = 127;			//エクスプレッション値

	public int toneStep = 0;

	public double tablePointer = 0;//Waveテーブルのポインタ
	public double stapTimingCounter = 0;//Waveテーブルのポインタを進めるタイミングのカウンター

	/*オシレータ	 */
	public  IToneOscillator toneOsc ;
	
	/**
	 * コンストラクタ
	 */
	public ToneBean() {
	}

	/**
	 *  周波数を設定する
	 * @param _frequency
	 */
	public void setFrequency(double _frequency) { 
		frequency = _frequency;
		if (frequency <=0) return;
		amplitude = AbstractToneChannel.SAMPLE_RATE / frequency; // 振幅
	}

	/**
	 *  チッピを取得
	 */
	public float getPitch() {
		return pitch;
	}

	/**
	 * チッピを設定する
	 * @param pitch 設定する pitch
	 */
	public void setPitch(float pitch) {
		this.pitch = pitch;
		setFrequency(note2freq(note+pitch));
	}
	
	/**
	 * MIDIノート番号から周波数への変換
	 * @param note ノート番号
	 * @return 周波数
	 */
	private double note2freq(double note) {
		return (double) Math.floor(concert_pitch * Math.pow( 2 ,((double)note - 69)/12));
	}
	
	/**
	 * ノート番号を指定して周波数をセットする
	 * @param note
	 */
	public void setNote(int note) { // 周波数を設定する
		this.pitch = 0;
		vibratoCount = 0;
		this.note = note;
		toneStep = 0;
		setFrequency(note2freq(note));
	}

	/** 
	 * ベロシティーを取得する
	 */
	public int getVelocity() {
		return velocity;
	}

	/**
	 * ベロシティーを設定する
	 */
	public void setVelocity(int velocity) {
		this.velocity = velocity;
		setOverallLevel();
	}

	/** 
	 * expressionを取得する
	 */
	public int getExpression() {
		return expression;
	}

	/**
	 *  expressionを設定する
	 */
	public void setExpression(int expression) {
		this.expression = expression;
		setOverallLevel();
	}
	

	/** 
	 * velocityとexpressionから実際の音量を設定する
	 */
	public void setOverallLevel() {
		double _velocity = velocity;
		double _expression = expression;
		overallLevel = (int)((_velocity / 8 )*(_expression/127));
		//System.out.println("overallLevel "+overallLevel +" ");
	}


	/* (non-Javadoc)
	 */
	public double getVibratoDelay() {
		return vibratoDelay;
	}

	/* (non-Javadoc)
	 */
	public void setVibratoDelay(double vibratoDelay) {
		this.vibratoDelay = vibratoDelay;
	}

	/* (non-Javadoc)
	 */
	public float getVibratoDepth() {
		return vibratoDepth;
	}

	/* (non-Javadoc)
	 */
	public void setVibratoDepth(float vibratoDepth) {
		this.vibratoDepth = vibratoDepth;
	}

	/* (non-Javadoc)
	 */
	public double getVibratoRate() {
		return vibratoRate;
	}

	/* (non-Javadoc)
	 */
	public void setVibratoRate(double vibratoRate) {
		this.vibratoRate = vibratoRate;
	}
	
	/**
	 * リセット処理<br>
	 * すべてのパラメータを初期値に戻す
	 */
	public void reset() { //
		setVelocity(127);
		
		concert_pitch = 440;
		note = 1;
		pitch = 0;
		vibrato = 0;
		expression = 127;			//エクスプレッション値
		
		vibratoDepth = 3; 
		vibratoRate = 0.5; 
		vibratoDelay = 60; 
		vibratoCount = 0; 
	}


	/**
	 * ノート番号を取得する
	 * @return note
	 */
	public int getNote() {
		return note;
	}
}