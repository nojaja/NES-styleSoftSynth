package com.github.nojaja.sound;


import java.awt.Component;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Stack;
import javax.sound.midi.ControllerEventListener;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;

import com.github.nojaja.sound.app.util.Helper;
import com.github.nojaja.sound.softsynth.ToneChannel;
import com.github.nojaja.sound.softsynth.oscillator.impl.*;


/**
 * MIDI再生クラス<br>
 * ファミシンセ音源にて再生を行う<br>
 * ループ設定ありのMIDはループ再生する<br>
 * MIDIシーケンスは256曲までをキャッシュしておき、タイトル名で再生出来る<br>
 * 曲切り替え時に前曲を停止しない場合はスタックして、次曲終了時にポップする(フィールド→戦闘→フィールド、シーン１→アイキャッチ→シーン１)<br>
 * 
 * 
 * @author nojaja
 *
 */
public class MidiEngine {
	// 登録できるMIDIファイルの最大数
	private static final int MAX_SEQUENCE = 256;

	// MIDIシーケンス
	private static Sequence[] sequences = new Sequence[MAX_SEQUENCE];
	// MIDIシーケンサ
	private static Sequencer sequencer;


	// 登録されたMIDIファイルの数
	private static int counter = 0;

	// 再生中のMIDIシーケンスの登録番号
	private static int playSequenceNo = -1;

	// MIDIシーケンスの開始地点
	private static long startTick = -1;


	// シーケンスリスト
	private static HashMap<String,Integer> sequenceIndex = new HashMap<String,Integer>();

	private static Stack<Integer> playListStack = new Stack<Integer>(); 


	//呼び出し親
	public static Component app = null;

	//音源リスト
	private static LinkedHashMap<String,ToneChannel> toneChannelList = new LinkedHashMap<String,ToneChannel>();

	static{
		/*音源と音色設定*/
		//		toneChannelList.put("CH01", new ToneChannel(64,new DataClipWave("wav/ら.wav"),0));
		//		toneChannelList.put("CH02", new ToneChannel(64,new DataClipWave("wav/ら.wav"),1)); 
		//		toneChannelList.put("CH03", new ToneChannel(64,new DataClipWave("wav/ら.wav"),2)); 
		toneChannelList.put("CH01", new ToneChannel(64,new RectWave(),0));
		toneChannelList.put("CH02", new ToneChannel(64,new RectWave(),1)); 
		toneChannelList.put("CH03", new ToneChannel(64,new TryWave(),2)); 
		//toneChannelList.put("CH03", new ToneChannel(64,new TryWaveSim(),2));
		toneChannelList.put("CH10", new ToneChannel(64,new NoisWave(),9));
		//		toneChannelList.put("CH10", new ToneChannel(64,new DataClipWave("wav/^Kick.wav"),9));

		toneChannelList.put("CH04", new ToneChannel(64,new RectWave(),3)); 
		toneChannelList.put("CH05", new ToneChannel(64,new RectWave(),4)); 
		toneChannelList.put("CH06", new ToneChannel(64,new RectWave(),5)); 
		toneChannelList.put("CH07", new ToneChannel(64,new RectWave(),6)); 
		toneChannelList.put("CH08", new ToneChannel(64,new RectWave(),7)); 
		toneChannelList.put("CH09", new ToneChannel(64,new RectWave(),8)); 


		toneChannelList.put("CH11", new ToneChannel(64,new RectWave(),10)); 
		toneChannelList.put("CH12", new ToneChannel(64,new RectWave(),11)); 
		toneChannelList.put("CH13", new ToneChannel(64,new RectWave(),12)); 
		toneChannelList.put("CH14", new ToneChannel(64,new RectWave(),13)); 
		toneChannelList.put("CH15", new ToneChannel(64,new RectWave(),14)); 
		toneChannelList.put("CH16", new ToneChannel(64,new RectWave(),15)); 

		/*音源登録*/
		openDevice();
	}



	/**
	 * 音源登録処理<br>
	 * toneChannelListに登録した音源をレシーバに登録する
	 */
	private static void openDevice() {

		try {
			if (sequencer == null) {
				sequencer = MidiSystem.getSequencer(false);

				/*toneChannelListに登録した音源をレシーバーに登録する*/
				try{
					String[] keyList = (String[])toneChannelList.keySet().toArray(new String[0]);
					for (int i = 0; i < keyList.length; i++) {
						String key = keyList[i];
						ToneChannel toneChannel = (ToneChannel) toneChannelList.get(key);
						sequencer.getTransmitter().setReceiver(toneChannel); 
					}	
				}catch (Exception e) {
					e.printStackTrace();
				}

				// シーケンサを開く
				sequencer.open();

				// メタイベントリスナーを登録
				sequencer.addMetaEventListener(new EndOfTrackEventListener());

				int[] controllers = {111};//CC#111はループポイントを表す
				sequencer.addControllerEventListener(new LoopingEventListener(), controllers);
			}
		}catch(MidiUnavailableException e){
		}
	}

	/**
	 * 音源リセット<br>
	 * toneChannelListに登録した音源に対してリセット（CC#0x79,0x00）を送信する
	 */
	private static void resetDevice() {
		try {
			ShortMessage resetMessage1 = new ShortMessage();
			String[] keyList = (String[])toneChannelList.keySet().toArray(new String[0]);
			for (int i = 0; i < keyList.length; i++) {
				String key = keyList[i];
				ToneChannel toneChannel = (ToneChannel) toneChannelList.get(key);
				resetMessage1.setMessage(ShortMessage.CONTROL_CHANGE, i,0x79,0);
				toneChannel.send(resetMessage1, -1);
			}
			Thread.sleep(500);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

	/**
	 * MIDIファイルをロード
	 * @param url MIDIファイルのURL
	 * @throws IOException 
	 * @throws InvalidMidiDataException 
	 */
	private static void load(String title,URL url) throws InvalidMidiDataException, IOException {
		// MIDIシーケンスを登録
		sequences[counter] = MidiSystem.getSequence(url);
		sequenceIndex.put(title, counter);
		counter++;
	}

	/**
	 * MIDIファイルをロード
	 * @param filename MIDIファイル名
	 * @throws IOException 
	 * @throws InvalidMidiDataException 
	 */
	public static void load(String title,String filename) throws InvalidMidiDataException, IOException  {
		URL url = Helper.getURLFromFileName(filename);
		System.out.println(url.toString()); 	
		load(title,url);
	}



	/**
	 * 再生開始
	 * @param no 登録番号
	 */
	public static void play(int no) {
		// 登録されてなければ何もしない
		if (sequences[no] == null) {
			return;
		}

		// 現在再生中のMIDIファイルと同じ場合は何もしない
		if ((sequencer.isRunning())&&(playSequenceNo == no)) {
			return;
		}

		// 別のMIDIシーケンスを再生する場合は
		// 現在再生中のシーケンスを停止する
		if (sequencer.isRunning()) {
			//再生中だった曲をスタックにつむ
			playListStack.add(playSequenceNo);
			sequencer.stop();
		}

		//音源リセット
		resetDevice();


		try {
			// シーケンサにMIDIシーケンスをセット
			sequencer.setSequence(sequences[no]);
			// 登録番号を記憶
			playSequenceNo = no;
			// MIDIシーケンサのスタート地点を変更（ループしないように）
			startTick = -1;
			// 再生開始
			sequencer.start();
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}

	public static void play(String title) {
		if(!sequenceIndex.containsKey(title)) return ;
		play(sequenceIndex.get(title));
	}

	/**
	 * 停止
	 */
	public static boolean stop() {
		try {
			if (sequencer.isRunning()) {
				sequencer.stop();
			}

			/*スタックがいる場合はポップして再生する*/
			if(playListStack.size()>0){
				play(playListStack.pop());
			}

			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static  LinkedHashMap<String,ToneChannel> getToneChannelList() {
		return  toneChannelList;
	}

	/**
	 * 再生終了時に発生するメタイベントリスナークラス
	 */
	private static class EndOfTrackEventListener implements MetaEventListener {

		// MIDIメタイベント
		private static final int END_OF_TRACK_MESSAGE = 47;

		/**
		 * ループポイント（CC#111）が指定されている場合は
		 * 指定したTickに再生位置を移動させます。
		 *  @param meta the meta-message that the sequencer encountered 
		 */
		public void meta(MetaMessage meta) {
			if (meta.getType() == END_OF_TRACK_MESSAGE) {
				if (sequencer != null && sequencer.isOpen() ) {
					if(startTick != -1){
						// MIDIシーケンス再生位置をstartTickに戻す
						sequencer.setMicrosecondPosition(startTick);
						// startTickから再生
						sequencer.start();
						System.out.println("midi loop "+startTick);
					}else{
						System.out.println("midi stop ");
						stop();
					}
				}
			}
		} 
	}


	/**
	 * コントロールチェンジ時に発生するメタイベントリスナークラス
	 */
	private static class LoopingEventListener implements ControllerEventListener {
		/**
		 * CC#111の場合はループポイントを登録します。
		 * @see MidiChannel#controlChange(int, int)
		 * 
		 * @param event the control-change event that the sequencer encountered in
		 * the sequence it is processing
		 */
		public void controlChange(ShortMessage event) {
			long nowPosition = sequencer.getMicrosecondPosition();
			if ((event.getData1() == 111) && (startTick == -1)) {
				startTick = nowPosition;
				System.out.println("midi setLoopPoint "+nowPosition);
			}
		}

	}

}