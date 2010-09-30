package com.github.nojaja.sound.app;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Transmitter;
import javax.swing.*;

import com.github.nojaja.sound.app.content.JWavePanel;
import com.github.nojaja.sound.softsynth.ToneChannel;
import com.github.nojaja.sound.softsynth.oscillator.impl.*;

/**
 * ファミコンシンセのデモ用MIDIピアノ
 * */
public class MidiKeyDemo extends JFrame{

	//シリアルバージョン
	private static final long serialVersionUID = 1L;

	private  ArrayList<MidiDevice> devices = getDevices();

	private String msg="CH01";

	private ToneChannel toneChannel = null;

	protected JWavePanel wavePanel; 

	private Choice synChoice = new Choice();

	/**
	 * 使用可能なMIDIデバイスを取得する処理
	 * 
	 * @return デバイス一覧
	 */
	public static ArrayList<MidiDevice> getDevices() {
		ArrayList<MidiDevice> devices = new ArrayList<MidiDevice>();

		MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
		for (int i = 0; i < infos.length; i++) {
			MidiDevice.Info info = infos[i];
			MidiDevice dev = null;
			try {
				dev = MidiSystem.getMidiDevice(info);
				devices.add(dev);
			} catch (SecurityException e) {
				System.err.println(e.getMessage());
			} catch (MidiUnavailableException e) {
				System.err.println(e.getMessage());
			}
		}
		return devices;
	}

	/**
	 * 使用可能なMIDIデバイス一覧を標準出力に表示する処理
	 */
	public static void dumpDeviceInfo() {
		ArrayList<MidiDevice> devices = getDevices();

		for (int i = 0; i < devices.size(); i++) {
			MidiDevice device = devices.get(i);
			MidiDevice.Info info = device.getDeviceInfo();
			System.out.println("[" + i + "] devinfo: " + info.toString());
			System.out.println("  name:"        + info.getName());
			System.out.println("  vendor:"      + info.getVendor());
			System.out.println("  version:"     + info.getVersion());
			System.out.println("  description:" + info.getDescription());
			if (device instanceof Synthesizer) {
				System.out.println("  SYNTHESIZER");
			}
			if (device instanceof Sequencer) {
				System.out.println("  SEQUENCER");
			}
			System.out.println("");
		}
	}

	/**
	 * コンストラクタ、各種アクションの設定
	 */
	public MidiKeyDemo(){
		super("Sound Demo");
		// ルックアンドフィールの変更
		try{
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		}catch(Exception e){
			System.err.println(e);
		}

		// ウインドウリスナの追加
		WindowListener listener = new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				System.exit(0);
			}
		};
		addWindowListener(listener);

		/*デバイス一覧を標準出力に表示する*/
		dumpDeviceInfo();

		/*プルダウンリストに利用可能なデバイスを登録する*/
		for (int i = 0; i < devices.size(); i++) {
			synChoice.add(devices.get(i).getDeviceInfo().getName());
		}

		// デバイスオープンボタンの定義
		JButton okButton = new JButton("デバイスオープン");
		ActionListener listener_okButton = new ActionListener(){
			public void actionPerformed(ActionEvent e){
				try { 
					/*MIDI INデバイスのセット～オープン*/
					int synNum = synChoice.getSelectedIndex();
					MidiDevice device_input  = devices.get(synNum);
					if (!device_input.isOpen()) {
						device_input.open();
					}

					Transmitter trans = device_input.getTransmitter();
					/*音源と音色の設定*/
					//toneChannel = new ToneChannel(64,new DataClipWave("wav/ら.wav"),0);
					toneChannel = new ToneChannel(64,new RectWave(),0);
					/*MIDI INデバイスのレシーバにセット*/
					trans.setReceiver(toneChannel);
					/*波形パネルの設定*/
					toneChannel.app = wavePanel;
					wavePanel.toneChannel = toneChannel;

					System.out.println("[OK]");
					/*波形パネルのメッセージをセット*/
					msg = "CH01";
					wavePanel.panelName = msg;

				} catch (MidiUnavailableException ex) {
					System.err.println(ex.getMessage());
					msg= ex.getMessage();
					wavePanel.panelName = msg;
					wavePanel.repaint();
				}catch(Exception me){
					System.err.println(me);
					msg= me.getMessage();
					wavePanel.panelName = msg;
					wavePanel.repaint();
				}
			}
		};
		okButton.addActionListener(listener_okButton);


		// ボタンの追加
		getContentPane().setLayout(new FlowLayout());
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());
		panel.setPreferredSize(new Dimension(512, 25));
		getContentPane().add(panel);
		panel.add(synChoice);
		panel.add(okButton);

		wavePanel = new JWavePanel(msg,toneChannel); 
		wavePanel.setBounds(0,0,100,100);  
		getContentPane().add(wavePanel);

		pack();
		this.setBounds(0,0,512,384);  //512x384　にこにこサイズ
		setVisible(true);
	}


	/**
	 * エラーダイアログ表示処理
	 * @param message 表示するエラーメッセージ
	 * @param title ダイアログのタイトル
	 */
	public void ShowErrorMessage(String message,String title)
	{
		JOptionPane.showMessageDialog(this,message,title,JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * メイン
	 * @param args
	 */
	public static void main(String args[]){
		new MidiKeyDemo();
	}
}
