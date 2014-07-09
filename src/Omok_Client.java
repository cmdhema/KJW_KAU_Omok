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
	private Label pInfo = new Label("����:  ��");
	private java.awt.List pList = new java.awt.List();
	private Button startButton = new Button("�뱹 ����");
	private Button stopButton = new Button("���");
	private Button enterButton = new Button("���� �ϱ�");
	private Button exitButton = new Button("���Ƿ�");
	private Label infoView = new Label("< ����� & �赿�� & ����� & �̱��� >", 1);
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
		p.add(new Label("��     ��:", 2));
		p.add(nameBox);
		p.add(new Label("�� ��ȣ:", 2));
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
					infoView.setText("���ȣ�� �߸��Ǿ����ϴ�. 1�̻�");
					return;
				}
				writer.println("[ROOM]" + Integer.parseInt(roomBox.getText()));
				msgView.setText("");
			} catch (Exception ie) {
				infoView.setText("�Է��Ͻ� ���׿� ������ �ҽ��ϴ�.");
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
				infoView.setText("����� ������ ��ٸ��ϴ�.");
				startButton.setEnabled(false);
			} catch (Exception e) {
			}
		} else if (ae.getSource() == stopButton) {
			try {
				writer.println("[DROPGAME]");
				endGame("����Ͽ����ϴ�.");
			} catch (Exception e) {
			}
		}
	}

	void goToWaitRoom() {
		if (userName == null) {
			String name = nameBox.getText().trim();
			if (name.length() <= 2 || name.length() > 10) {
				infoView.setText("�̸��� �߸��Ǿ����ϴ�. 3~10��");
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
		infoView.setText("���ǿ� �����ϼ̽��ϴ�.");
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
						infoView.setText(msg.substring(6) + "�� �濡 �����ϼ̽��ϴ�.");
					} else {
						setVisible(true);
						aa.setVisible(false);
						p.setBounds(10, 30, 250, 70);
						p2.setBounds(10, 110, 250, 180);
						p3.setBounds(10, 300, 250, 350);
						add(p);
						add(p2);
						add(p3);
						infoView.setText("���ǿ� �����ϼ̽��ϴ�.");
					}
					roomNumber = Integer.parseInt(msg.substring(6));
					if (board.isRunning()) {
						board.stopGame();
					}
				} else if (msg.startsWith("[FULL]")) {
					infoView.setText("���� ���� ������ �� �����ϴ�.");
				} else if (msg.startsWith("[PLAYERS]")) {
					nameList(msg.substring(9));
				} else if (msg.startsWith("[ENTER]")) {
					pList.add(msg.substring(7));
					playersInfo();
					msgView.append("[" + msg.substring(7) + "]���� �����Ͽ����ϴ�.\n");
				} else if (msg.startsWith("[EXIT]")) {
					pList.remove(msg.substring(6));
					playersInfo();
					msgView.append("[" + msg.substring(6)
							+ "]���� �ٸ� ������ �����Ͽ����ϴ�.\n");
					if (roomNumber != 0) {
						endGame("��밡 �������ϴ�.");
					}
				} else if (msg.startsWith("[DISCONNECT]")) {
					pList.remove(msg.substring(12));
					playersInfo();
					msgView.append("[" + msg.substring(12) + "]���� ������ �������ϴ�.\n");
					if (roomNumber != 0) {
						endGame("��밡 �������ϴ�.");
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
						infoView.setText("�浹�� ��ҽ��ϴ�.");
					else
						infoView.setText("�鵹�� ��ҽ��ϴ�.");
					stopButton.setEnabled(true);
				} else if (msg.startsWith("[DROPGAME]")) {
					// aa.setVisible(false);
					endGame("��밡 ����Ͽ����ϴ�.");
				} else if (msg.startsWith("[WIN]")) {
					// aa.setVisible(false);
					JOptionPane.showMessageDialog(null, "����� �̰���ϴ�.");
					endGame("�̰���ϴ�.");
				} else if (msg.startsWith("[LOSE]")) {
					JOptionPane.showMessageDialog(null, "����� �����ϴ�.");
					endGame("�����ϴ�.");
				} else
					msgView.append(msg + "\n");
			}
		} catch (IOException ie) {
			msgView.append(ie + "\n");
		}
		msgView.append("������ ������ϴ�.");
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
			pInfo.setText("����: " + count + "��");
		else
			pInfo.setText(roomNumber + " �� ��: " + count + "��");
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
			msgView.append("������ ������ ��û�մϴ�.\n");
			socket = new Socket(ip, 7777);
			msgView.append("---���� ����--.\n");
			msgView.append("�̸��� �Է��ϰ� ���Ƿ� �����ϼ���.\n");
			reader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			writer = new PrintWriter(socket.getOutputStream(), true);
			new Thread(this).start();
			board.setWriter(writer);
		} catch (Exception e) {
			msgView.append(e + "\n\n���� ����..\n");
			String reIp = JOptionPane.showInputDialog("IP�� �ٽ��Է����ּ���");
			connect(reIp);
		}
	}

	public static void main(String[] args) {
		Omok_Client client = new Omok_Client("��Ʈ��ũ ���� ����");
		client.setSize(260, 700);
		client.setVisible(true);
		String ip = JOptionPane.showInputDialog("IP�� �Է��ϼ���");
		client.connect(ip);
	}
}
