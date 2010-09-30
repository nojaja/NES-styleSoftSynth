package com.github.nojaja.sound.softsynth;

import java.util.Stack;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import com.github.nojaja.sound.softsynth.oscillator.AbstractToneChannel;
import com.github.nojaja.sound.softsynth.oscillator.IToneOscillator;
import com.github.nojaja.sound.softsynth.oscillator.ToneBean;

/**<PRE>
 * Midi Receiverとして各音源を制御する機能を提供します。
 * 詳しい使い方についてはサンプルのMidiEngine.javaを参照してください
 * </PRE>
 * @author nojaja
 * @version 0.1
 *
 * */
public class ToneChannel extends AbstractToneChannel implements Receiver {

	/**発声中の音色を管理するためのテーブル*/
	public ToneBean[] tones = new ToneBean[128]; //
	/**利用可能な音色をプールするためのクラス*/
	public Stack<ToneBean> tonePool = new Stack<ToneBean>();
	/**このインスタンスで処理するMIDIチャンネル*/
	public int channel = 0; //

	private int NRPN = 0;
	private float pitch_sc = 2;



	/**<PRE>
	 * 新しいToneChannelを生成します。
	 *</PRE>
	 * @param toneCnt 同時発音数 1～128
	 * @param toneOscillator 音源クラス 
	 * @param channel MIDIチャンネル 0～15ch
	 **/
	public ToneChannel (int toneCnt,IToneOscillator toneOscillator,int channel) {
		super(1);
		this.channel = channel;

		toneOsc = toneOscillator;
		//		switch (type) {
		//		case 0:
		//			toneOsc = new RectWave();
		//			break;
		//		case 1:
		//			toneOsc = new RectWaveSim();
		//			break;
		//		case 2:
		//			toneOsc = new TryWave();
		//			break;
		//		case 3:
		//			toneOsc = new SinWave();
		//			break;
		//		case 4:
		//			toneOsc = new NoisWave();
		//			break;
		//		case 5:
		//			toneOsc = new TryWaveSim();
		//			break;
		//		case 6:
		//			//toneOsc = new DataClipWave("midi/Hit08_A3.wav");
		//			//toneOsc = new DataClipWave("midi/Guitar1_11_6_A4.wav");
		//			//toneOsc = new DataClipWave("midi/ccopedia.wav");
		//			//toneOsc = new DataClipWave("midi/Choir01.wav");
		//			
		//			toneOsc = new DataClipWave("midi/ら.wav");
		//
		//			break;
		//		case 7:
		//			toneOsc = new DataClipWave("midi/お.wav");
		//			break;
		//		case 8:
		//			toneOsc = new DataClipWave("midi/た.wav");
		//			break;
		//		case 9:
		//			//toneOsc = new DataClipWave("midi/^Tom01.wav");
		//			toneOsc = new DataClipWave("midi/^hatC.wav");
		//			//toneOsc = new DataClipWave("midi/^Kick.wav");
		//			
		//			break;
		//		case 10:
		//			toneOsc = new DataClipWave("midi/ccopedia.wav");
		//			
		//			break;
		//		default:
		//			toneOsc = new RectWaveSim();
		//		}

		/*音階データ生成*/
		for (int i = 0; i < toneCnt; i++) {
			try {
				ToneBean rw = new ToneBean();
				rw.setFrequency(523.3);//523.3
				rw.setVelocity(0);
				tonePool.add(rw);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		start();
	}

	/**
	 * <PRE>
	 * アプリケーションによるレシーバの使用が終了し、レシーバが要求する限られたリソースを解放または使用可能にできることを示します。 
	 * ただし、このクラスではスタブとする
	 *</PRE>
	 * @see javax.sound.midi.Receiver#close
	 **/
	public void close() {
	}


	/**<PRE>
	 * MIDI メッセージおよび時刻表示をこのレシーバに送信します。
	 * このレシーバが時刻表示をサポートしていない場合は、時刻表示値は -1 になります。
	 *</PRE>
	 * @param message 送信する MIDI メッセージ
	 * @param timeStamp メッセージの時刻表示、マイクロ秒単位
	 * @throws IllegalStateException レシーバがクローズしている場合
	 * @see javax.sound.midi.Receiver#send
	 **/
	public void send(MidiMessage message, long timeStamp) {
		// ここで message の中身を解析すればよい
		//System.out.println(message.toString());
		if ( message instanceof ShortMessage ) {

			ShortMessage sm = (ShortMessage)message;

			//if(channel != sm.getChannel()) return;
			int channel = sm.getChannel();
			if(channel > tones.length)return;
			boolean eflag = false;
			if(channel == this.channel){
				eflag = true;
			}
			if(!eflag)return;
			switch ( sm.getCommand() ) {
			case ShortMessage.NOTE_ON:
				try {
					int note = sm.getData1();
					int velocity = sm.getData2();
					ToneBean tone = null;
					if(velocity > 0){
						if(!tonePool.empty()&&tones[note]==null){
							ToneBean t = tonePool.pop();
							t.setNote(note);
							//t.setFrequency(523.3);//523.3
							t.setVelocity(velocity);
							//tone.activeTone[channel].add(tone);
							toneList.add(t);
							tones[note]=t;
						}
					}else{
						tone = tones[note];
						if(tone!=null){
							tone.setVelocity(0);
							toneList.remove(tone);
							tonePool.push(tone);
							tones[note] = null;
							if(!toneOsc.isToneSync()) tone.tablePointer = 0;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case ShortMessage.NOTE_OFF:
			{
				int note = sm.getData1();
				ToneBean tone = tones[note];
				if(tone!=null){
					tone.setVelocity(0);
					toneList.remove(tone);
					tonePool.push(tone);
					tones[note] = null;
					if(!toneOsc.isToneSync()) tone.tablePointer = 0;
				}
			}
			break;
			case ShortMessage.PITCH_BEND:
			{
				for (int i = 0; i < toneList.size(); i++) {
					ToneBean tone = toneList.get(i);
					if(tone==null)continue;

					//-8192 ～ +8191 
					int pitch =(sm.getData2()  <<  7)+(sm.getData1() & 0x7f)-8192;
					//tone.setPitch((float)((float)(pitch/16382) * pitch_sc));//16382で半音あがる
					tone.setPitch((float)(pitch*pitch_sc)/8191);//16382で半音あがる
					//tone.setPitch((float)(pitch)/32764);//16382で半音あがる
					//System.out.println("PITCH_BEND="+((float)(pitch)/8191)+"  pitch= "+pitch);

				}
			}
			break;
			case ShortMessage.CONTROL_CHANGE:

				switch ( sm.getData1() ) {
				case 0x00://
					break;
				case 0x01://モジュレーション・デプス
					break;

				case 0x05://Portamento Time
					break;
				case 0x06://データエントリ（ＲＰＮ／ＮＲＰＮで指定したパラメータの値を設定）
					//System.out.println("NRPN "+NRPN +" " + sm.getData2() +" ");
					switch (NRPN){
					case 0x00://ピッチベンド・センシティビティ
					{
						//System.out.println("ピッチベンド・センシティビティ "+NRPN +" " + sm.getData2() +" ");
						pitch_sc = sm.getData2();///2;
					}
					break;
					case 0x08://ビブラート・レイト（Vibrato Rate）
					{
						for (int i = 0; i < toneList.size(); i++) {
							ToneBean tone = toneList.get(i);
							if(tone==null)continue;
							tone.setVibratoRate((sm.getData2()-64)/64);
						}
					}
					break;
					case 0x09://ビブラート・デプス（Vibrato Depth）
					{
						for (int i = 0; i < toneList.size(); i++) {
							ToneBean tone = toneList.get(i);
							if(tone==null)continue;
							tone.setVibratoDepth(sm.getData2()-64);
						}
					}
					break;
					case 0x0A://ビブラート・ディレイ（Vibrato Delay）
					{
						for (int i = 0; i < toneList.size(); i++) {
							ToneBean tone = toneList.get(i);
							if(tone==null)continue;
							tone.setVibratoDelay(sm.getData2()-64);
						}
					}
					break;
					}
					break;
				case 0x07://メインボリューム(チャンネルの音量を設定）
					//float vol = ((float)sm.getData2()/150);//127
					float vol = ((float)sm.getData2()/200);//127
					{
						for (int i = 0; i < toneList.size(); i++) {
							ToneBean tone = toneList.get(i);
							if(tone==null)continue;
							this.setVolume(vol);
						}
					}
					break;
				case 0x0A://PAN
					this.setPan(sm.getData2());
					break;
				case 0x0B://Expression
				{
					for (int i = 0; i < toneList.size(); i++) {
						ToneBean tone = toneList.get(i);
						if(tone==null)continue;
						tone.setExpression(sm.getData2());
					}
					for (int i = 0; i < tonePool.size(); i++) {
						ToneBean tone = tonePool.get(i);
						if(tone==null)continue;
						tone.setExpression(sm.getData2());
					}
				}
				break;
				case 0x61://Portamento
					break;
				case 0x62://NRPN
					NRPN = sm.getData2();
					break;
				case 0x65://101
					NRPN = 0x00;
					break;
				case 0x79://リセット オール コントローラー
				{
					//System.out.println("リセット オール コントローラー");
					pitch_sc = 2;
					for (int i = 0; i < toneList.size(); i++) {
						ToneBean tone = toneList.get(i);
						if(tone==null)continue;
						tone.reset();
					}
					for (int i = 0; i < tonePool.size(); i++) {
						ToneBean tone = tonePool.get(i);
						if(tone==null)continue;
						tone.reset();
					}
					this.setVolume(1);
				}
				break;
				case 0x7B://All Note Off
				{
					pitch_sc = 2;
					for (int i = 0; i < toneList.size(); i++) {
						ToneBean tone = toneList.get(i);
						if(tone==null)continue;
						tone.reset();
					}
				}
				break;
				default:
					//System.out.println("CONTROL_CHANGE "+sm.getData1() +" " + sm.getData2() +" ");
				}
				break;
			case ShortMessage.PROGRAM_CHANGE:
				this.setVolume(1);
				break;
			case 0x20://バンク・セレクト LSB
				break;
			default:
				System.out.println(" ShortMessage"+sm.getCommand());
			}
		}
	}


}
