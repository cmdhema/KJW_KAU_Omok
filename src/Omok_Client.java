import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.StringTokenizer;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Omok_Client extends Frame implements Runnable, ActionListener {
	private TextArea msgView = new TextArea("", 1, 1, 1);
	private TextField sendBox = new TextField("");
	private TextField nameBox = new TextField();
	private TextField roomBox = new TextField("0");
	private Panel p = new Panel();
	private Panel p2 = new Panel();
	private Panel p2_1 = new Panel();
	private Panel p3 = new Panel();
	private Label pInfo = new Label("대기실:  명");
	private java.awt.List pList = new java.awt.List();
	private Button startButton = new Button("대국 시작");
	private Button stopButton = new Button("기권");
	private Button enterButton = new Button("입장 하기");
	private Button exitButton = new Button("대기실로");
	private Label infoView = new Label("< 김재욱 & 김동근 & 김승중 & 이근희 >", 1);
	private Omok_Board board = new Omok_Board(15, 30);
	private JFrame aa = new JFrame();
	private BufferedReader reader;
	private PrintWriter writer;
	private Socket socket;
	private int roomNumber = -1;
	private String userName = null;

	public Omok_Client(String title) {

		super(title);
		setLayout(null);
		aa.setSize(800, 560);
		aa.setLayout(null);
		msgView.setEditable(false);
		infoView.setBounds(10, 650, 250, 30);
		infoView.setBackground(new Color(200, 200, 255));
		add(infoView);
		p.setBackground(new Color(200, 255, 255));
		p.setLayout(new GridLayout(3, 3));
		p.add(new Label("이     름:", 2));
		p.add(nameBox);
		p.add(new Label("방 번호:", 2));
		p.add(roomBox);
		p.add(enterButton);
		p.add(exitButton);
		enterButton.setEnabled(false);
		p.setBounds(10, 30, 250, 70);

		p2.setBackground(new Color(255, 255, 100));
		p2.setLayout(new BorderLayout());
		p2_1.add(startButton);
		p2_1.add(stopButton);
		p2.add(pInfo, "North");
		p2.add(pList, "Center");
		p2.add(p2_1, "South");
		startButton.setEnabled(false);
		stopButton.setEnabled(false);
		p2.setBounds(10, 110, 250, 180);

		p3.setLayout(new BorderLayout());
		p3.add(msgView, "Center");
		p3.add(sendBox, "South");
		p3.setBounds(10, 300, 250, 350);

		add(p);
		add(p2);
		add(p3);
		sendBox.addActionListener(this);
		enterButton.addActionListener(this);
		exitButton.addActionListener(this);
		startButton.addActionListener(this);
		stopButton.addActionListener(this);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				System.exit(0);
			}
		});
	}

	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == sendBox) {
			String msg = sendBox.getText();
			if (msg.length() == 0)
				return;
			if (msg.length() >= 30)
				msg = msg.substring(0, 30);
			try {
				writer.println("[MSG]" + msg);
				sendBox.setText("");
			} catch (Exception ie) {
			}
		} else if (ae.getSource() == enterButton) {
			try {

				if (Integer.parseInt(roomBox.getText()) < 1) {
					infoView.setText("방번호가 잘못되었습니다. 1이상");
					return;
				}
				writer.println("[ROOM]" + Integer.parseInt(roomBox.getText()));
				msgView.setText("");
			} catch (Exception ie) {
				infoView.setText("입력하신 사항에 오류가 았습니다.");
			}
		} else if (ae.getSource() == exitButton) {
			try {
				goToWaitRoom();
				startButton.setEnabled(false);
				stopButton.setEnabled(false);
			} catch (Exception e) {
			}
		} else if (ae.getSource() == startButton) {
			try {
				writer.println("[START]");
				infoView.setText("상대의 결정을 기다립니다.");
				startButton.setEnabled(false);
			} catch (Exception e) {
			}
		} else if (ae.getSource() == stopButton) {
			try {
				writer.println("[DROPGAME]");
				endGame("기권하였습니다.");
			} catch (Exception e) {
			}
		}
	}

	void goToWaitRoom() {
		if (userName == null) {
			String name = nameBox.getText().trim();
			if (name.length() <= 2 || name.length() > 10) {
				infoView.setText("이름이 잘못되었습니다. 3~10자");
				nameBox.requestFocus();
				return;
			}
			userName = name;
			writer.println("[NAME]" + userName);
			nameBox.setText(userName);
			nameBox.setEditable(false);
		}
		msgView.setText("");
		writer.println("[ROOM]0");
		infoView.setText("대기실에 입장하셨습니다.");
		roomBox.setText("0");
		enterButton.setEnabled(true);
		exitButton.setEnabled(false);
	}

	public void run() {
		String msg;
		try {
			while ((msg = reader.readLine()) != null) {
				if (msg.startsWith("[STONE]")) {
					String temp = msg.substring(7);
					int x = Integer.parseInt(temp.substring(0,
							temp.indexOf(" ")));
					int y = Integer
							.parseInt(temp.substring(temp.indexOf(" ") + 1));
					board.putOpponent(x, y);
					board.setEnable(true);
				} else if (msg.startsWith("[ROOM]")) {
					if (!msg.equals("[ROOM]0")) {
						enterButton.setEnabled(false);
						exitButton.setEnabled(true);
						infoView.setText(msg.substring(6) + "번 방에 입장하셨습니다.");
					} else {
						setVisible(true);
						aa.setVisible(false);
						p.setBounds(10, 30, 250, 70);
						p2.setBounds(10, 110, 250, 180);
						p3.setBounds(10, 300, 250, 350);
						add(p);
						add(p2);
						add(p3);
						infoView.setText("대기실에 입장하셨습니다.");
					}
					roomNumber = Integer.parseInt(msg.substring(6));
					if (board.isRunning()) {
						board.stopGame();
					}
				} else if (msg.startsWith("[FULL]")) {
					infoView.setText("방이 차서 입장할 수 없습니다.");
				} else if (msg.startsWith("[PLAYERS]")) {
					nameList(msg.substring(9));
				} else if (msg.startsWith("[ENTER]")) {
					pList.add(msg.substring(7));
					playersInfo();
					msgView.append("[" + msg.substring(7) + "]님이 입장하였습니다.\n");
				} else if (msg.startsWith("[EXIT]")) {
					pList.remove(msg.substring(6));
					playersInfo();
					msgView.append("[" + msg.substring(6)
							+ "]님이 다른 방으로 입장하였습니다.\n");
					if (roomNumber != 0) {
						endGame("상대가 나갔습니다.");
					}
				} else if (msg.startsWith("[DISCONNECT]")) {
					pList.remove(msg.substring(12));
					playersInfo();
					msgView.append("[" + msg.substring(12) + "]님이 접속을 끊었습니다.\n");
					if (roomNumber != 0) {
						endGame("상대가 나갔습니다.");
					}
				} else if (msg.startsWith("[COLOR]")) {
					String color = msg.substring(7);
					aa.add(board).setBounds(10, 10, 500, 500);
					p.setBounds(520, 10, 250, 70);
					p2.setBounds(520, 90, 250, 180);
					p3.setBounds(520, 280, 250, 250);
					aa.add(p);
					aa.add(p2);
					aa.add(p3);
					aa.setVisible(true);
					setVisible(false);
					board.startGame(color);
					if (color.equals("BLACK"))
						infoView.setText("흑돌을 잡았습니다.");
					else
						infoView.setText("백돌을 잡았습니다.");
					stopButton.setEnabled(true);
				} else if (msg.startsWith("[DROPGAME]")) {
					// aa.setVisible(false);
					endGame("상대가 기권하였습니다.");
				} else if (msg.startsWith("[WIN]")) {
					// aa.setVisible(false);
					JOptionPane.showMessageDialog(null, "당신이 이겼습니다.");
					endGame("이겼습니다.");
				} else if (msg.startsWith("[LOSE]")) {
					JOptionPane.showMessageDialog(null, "당신이 졌습니다.");
					endGame("졌습니다.");
				} else
					msgView.append(msg + "\n");
			}
		} catch (IOException ie) {
			msgView.append(ie + "\n");
		}
		msgView.append("접속이 끊겼습니다.");
	}

	private void endGame(String msg) {
		infoView.setText(msg);
		startButton.setEnabled(false);
		stopButton.setEnabled(false);
		try {
			Thread.sleep(2000);
		} catch (Exception e) {
		}
		if (board.isRunning())
			board.stopGame();
		if (pList.getItemCount() == 2)
			startButton.setEnabled(true);
	}

	private void playersInfo() {
		int count = pList.getItemCount();
		if (roomNumber == 0)
			pInfo.setText("대기실: " + count + "명");
		else
			pInfo.setText(roomNumber + " 번 방: " + count + "명");
		if (count == 2 && roomNumber != 0)
			startButton.setEnabled(true);
		else
			startButton.setEnabled(false);
	}

	private void nameList(String msg) {
		pList.removeAll();
		StringTokenizer st = new StringTokenizer(msg, "\t");
		while (st.hasMoreElements())
			pList.add(st.nextToken());
		playersInfo();
	}

	private void connect(String ip) {
		try {
			msgView.append("서버에 연결을 요청합니다.\n");
			socket = new Socket(ip, 7777);
			msgView.append("---연결 성공--.\n");
			msgView.append("이름을 입력하고 대기실로 입장하세요.\n");
			reader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			writer = new PrintWriter(socket.getOutputStream(), true);
			new Thread(this).start();
			board.setWriter(writer);
		} catch (Exception e) {
			msgView.append(e + "\n\n연결 실패..\n");
			String reIp = JOptionPane.showInputDialog("IP를 다시입력해주세요");
			connect(reIp);
		}
	}

	public static void main(String[] args) {
		Omok_Client client = new Omok_Client("네트워크 오목 게임");
		client.setSize(260, 700);
		client.setVisible(true);
		String ip = JOptionPane.showInputDialog("IP를 입력하세요");
		client.connect(ip);
	}
}
