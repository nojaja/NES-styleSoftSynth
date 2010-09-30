package com.github.nojaja.sound.app;

import java.awt.*;
import java.awt.FlowLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.swing.*;

import com.github.nojaja.sound.MidiEngine;
import com.github.nojaja.sound.app.content.JWavePanel;
import com.github.nojaja.sound.softsynth.ToneChannel;



/**
 * ファミコンシンセのデモ用プレーヤー
 * */
public class MidiDemo extends JFrame{	

	//シリアルバージョン
	private static final long serialVersionUID = 1L;
	// ロードされたことを示すフラグ
	private boolean is_loaded  = false;

	private String ext=null;

	// 許容される拡張子
	private final String[] ext_list = {"mid"};

	/**
	 * コンストラクタ、各種アクションの設定
	 */
	public MidiDemo(){
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

		// ドロップターゲットの定義
		new DropTarget(this,DnDConstants.ACTION_REFERENCE,new DropTargetListener(){
			public void dragEnter(DropTargetDragEvent enter){}
			public void dragOver(DropTargetDragEvent over){}
			public void dropActionChanged(DropTargetDragEvent action){}
			public void dragExit(DropTargetEvent exit){}

			// ドロップされた時にコールされる
			public void drop(DropTargetDropEvent drop){
				try{
					Transferable transfer = drop.getTransferable();
					if(transfer.isDataFlavorSupported(DataFlavor.javaFileListFlavor)){
						// ドロップされたデータの取得
						drop.acceptDrop(DnDConstants.ACTION_REFERENCE);
						List file_list = (List)transfer.getTransferData(DataFlavor.javaFileListFlavor);
						if(file_list.size()>0){
							String file_name = file_list.get(0).toString();
							fileOpen(file_name);
						}
						drop.getDropTargetContext().dropComplete(true);
					}
				}catch(Exception e){
					System.err.println(e);
				}
			}
		});

		// ファイルを開くボタンの定義
		JButton open_file = new JButton("Open...");
		ActionListener listener_open_file = new ActionListener(){
			public void actionPerformed(ActionEvent e){
				try{
					// ファイルを開くダイアログの表示
					JFileChooser chooser = new JFileChooser();
					if(chooser.showOpenDialog(MidiDemo.this) == JFileChooser.APPROVE_OPTION){
						// サウンドファイルの取得
						File file = chooser.getSelectedFile();
						fileOpen(file.getName());
					}
				}catch(Exception me){
					System.err.println(me);
				}
			}
		};
		open_file.addActionListener(listener_open_file);

		// プレイボタンの定義
		JButton play = new JButton("Play");
		ActionListener listener_play = new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if(!is_loaded){
					ShowErrorMessage("Please open media file.","Not Loaded.");
					return;
				}
				MidiEngine.stop();
				if(ext==null){
					ShowErrorMessage("This file is invalid type","Invalid File!");
				}else if(ext=="mid"){
					MidiEngine.play("midi");
				}
			}
		};
		play.addActionListener(listener_play);

		// ストップボタンの定義
		JButton stop = new JButton("Stop");
		ActionListener listener_stop = new ActionListener(){
			public void actionPerformed(ActionEvent e){
				MidiEngine.stop();
			}
		};
		stop.addActionListener(listener_stop);



		// ボタンの追加
		getContentPane().setLayout(new FlowLayout());
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());
		panel.setPreferredSize(new Dimension(512, 25));
		getContentPane().add(panel);
		//panel.add(open_file);
		panel.add(play);
		panel.add(stop);

		/*波形パネルの追加
		 * 登録してある音源分だけ表示する*/
		LinkedHashMap<String,ToneChannel> toneChannelList = MidiEngine.getToneChannelList();
		String[] keyList = toneChannelList.keySet().toArray(new String[0]);
		System.out.println(keyList.length);
		for (int i = 0; i < keyList.length; i++) {
			String key = keyList[i];
			ToneChannel toneChannel = toneChannelList.get(key);
			JWavePanel wavePanel = new JWavePanel(key,toneChannel); 
			wavePanel.setBounds(0,0,100,100);  
			getContentPane().add(wavePanel);//TODO そのうち直す
			toneChannel.app = wavePanel;//TODO そのうち直す
		}
		pack();
		this.setBounds(0,0,512,384);  //512x384　にこにこサイズ
		setVisible(true);
	}

	/**
	 * 拡張子取得処理
	 * @param file_name 取得対象のファイル名
	 * @return 拡張子
	 */
	public void fileOpen(String fileName){
		try {
			MidiEngine.stop();
			ext = getFileExt(fileName);
			if(ext==null){
				ShowErrorMessage("This file is invalid type","Invalid File!");
			}else if(ext=="mid"){
				MidiEngine.load("midi",fileName);
				is_loaded = true;
				MidiEngine.play("midi");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 拡張子取得処理
	 * @param file_name 取得対象のファイル名
	 * @return 拡張子
	 */
	public String getFileExt(String file_name){
		int index = file_name.lastIndexOf('.');
		String ext = file_name.substring(index + 1,file_name.length());
		for(int i=0;i<ext_list.length;i++){
			if(ext.toLowerCase().compareTo(ext_list[i]) == 0){
				return(ext_list[i]);
			}
		}
		return(null);
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
		new MidiDemo();
	}
}
