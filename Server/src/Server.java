
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Server {
	private static ServerSocket soct;
	private static ServerSocket socs;
	List<Map<String, Object>> studentlists = new ArrayList<Map<String, Object>>();
	List<Map<String, Object>> teacherlists = new ArrayList<Map<String, Object>>();

	public Server() {
		try {
			soct = new ServerSocket(509);// for teacher
			socs = new ServerSocket(599);// for student
			System.out.println("Server is start1.");
			addteacher adt = new addteacher();
			adt.start();
			addstudent ads = new addstudent();
			ads.start();
		} catch (IOException e) {
			System.out.println("Socket ERROR");
		}
	}

	public static void main(String[] args) {
		new Server();
	}

	class addteacher extends Thread {// 監聽用
		public void run() {
			try {
				while (true) {
					Socket socket = soct.accept();
					System.out.println("get t");
					inteacher inteach = new inteacher(socket);
					inteach.start();
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	class addstudent extends Thread {// 監聽用
		public void run() {
			try {
				while (true) {
					Socket socket = socs.accept();
					System.out.println("get s");
					instudent instudents = new instudent(socket);
					instudents.start();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	class inteacher extends Thread {// 加入list
		private Socket soct;
		private BufferedReader brt;
		private BufferedWriter bwt;
		private String function, pinforaccess;
		int flag = 1;

		public inteacher(Socket s) {
			try {
				soct = s;
				bwt = new BufferedWriter(new OutputStreamWriter(soct.getOutputStream(), "UTF-8"));
				brt = new BufferedReader(new InputStreamReader(soct.getInputStream(), "UTF-8"));
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("name", brt.readLine());
				map.put("socket", soct);
				map.put("function", function = brt.readLine());
				map.put("pinforaccess", pinforaccess = brt.readLine());
				Iterator<Map<String, Object>> Iterator = teacherlists.iterator();
				while (Iterator.hasNext()) {// 檢查是否有重複名稱的老師(怕跳出再登入 記錄重複
					Map<String, Object> x = Iterator.next();
					if (x.get("name").equals(map.get("name"))) {
						System.out.println("!!!!!" + teacherlists.size());
						Iterator.remove();
						System.out.println("!!!!!" + teacherlists.size());
					}
				}
				teacherlists.add(map);
				System.out.println("!!!!!" + teacherlists.size());
				System.out
						.println("create 1 teacher" + map.get("name") + map.get("pinforaccess") + map.get("function"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void run() {
			System.out.println("starts");
			try {
				updatepeople up = new updatepeople();
				up.start();
				if (brt.readLine().contains("start")) 
				{// 收到老師按開始健
					flag = 0;
					System.out.println(studentlists);
					Iterator<Map<String, Object>> sListIterator = studentlists.iterator();
					while (sListIterator.hasNext()) {
						Map<String, Object> x = sListIterator.next();
						if (x.get("pinforaccess").equals(pinforaccess)) {
							System.out.println("AAA");
							Socket temps = (Socket) x.get("socket");
							BufferedWriter bw = new BufferedWriter(
									new OutputStreamWriter(temps.getOutputStream(), "UTF-8"));
							bw.write("startt\n");// fort
							bw.flush();
							temps.close();//第一種考試 傳完權限之後就斷開socket 並清除stlist資料
							sListIterator.remove();
							System.out.println("刪掉S:"+temps.isClosed());
						}
					}System.out.println(studentlists);
				
					Iterator<Map<String, Object>> tIterator = teacherlists.iterator();
					while (tIterator.hasNext()) {
						Map<String, Object> xx = tIterator.next();
						if (xx.get("socket").equals(soct)) {
							soct.close();
							tIterator.remove();
							System.out.println("刪掉T:"+soct.isClosed());
						}
					}System.out.println(teacherlists);
					

				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		class updatepeople extends Thread {
			public void run() {
				int count = 0;
				Iterator<Map<String, Object>> sListIterator;
				Map<String, Object> x;
				Socket temps;
				while (flag != 0) {//老師按開始FLAG就會=0
					count = 0;
					sListIterator = studentlists.iterator();
					while (sListIterator.hasNext()) {
						x = sListIterator.next();
						temps = (Socket) x.get("socket");
						if ((x.get("pinforaccess").equals(pinforaccess))) {
							try {
								temps.setOOBInline(false);
								temps.sendUrgentData(0xFF);
								count++;
							} catch (Exception ex) {

								sListIterator.remove();
								System.out.println("有人斷開" + studentlists.size());
							}

						}
					}
					System.out.println("!" + count);
					try {
						bwt.write(count + "\n");
						bwt.flush();
						sleep(3000);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
		}
	}

	class instudent extends Thread {
		private Socket soct;
		private BufferedReader brs;
		private BufferedWriter bws;

		public instudent(Socket s) {
			String pinforaccess;
			try {
				System.out.println("instu");
				soct = s;
				bws = new BufferedWriter(new OutputStreamWriter(soct.getOutputStream(), "UTF-8"));
				brs = new BufferedReader(new InputStreamReader(soct.getInputStream(), "UTF-8"));
				pinforaccess = brs.readLine();
				System.out.println(pinforaccess);
				System.out.println(pinforaccess);
				Iterator<Map<String, Object>> tIterator = teacherlists.iterator();
				Boolean canin = false;
				while (tIterator.hasNext()) {// 檢查是否有相同pfa的老師在開房
					Map<String, Object> xx = tIterator.next();
					if (xx.get("pinforaccess").equals(pinforaccess)) {
						canin = true;
					}
				}
				if (canin != true) {
					bws.write("no\n");// 學生利用SOC 查詢是否有此房間
					bws.flush();
					System.out.println("!!!!!NONO");
				} else {
					System.out.println("!!!!!GOGO");
					bws.write("hr\n");// has room
					bws.flush();
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("name", brs.readLine());
					map.put("socket", soct);
					map.put("function", brs.readLine());
					map.put("pinforaccess", pinforaccess);
					Iterator<Map<String, Object>> Iterator = studentlists.iterator();
					while (Iterator.hasNext()) {
						Map<String, Object> x = Iterator.next();
						if (x.get("name").equals(map.get("name"))) {
							System.out.println("!!!!!" + studentlists.size());
							Iterator.remove();
						}
					}
					studentlists.add(map);
					System.out.println("!!!!!" + studentlists.size());
					System.out.println("create 1 student" + map.get("name"));
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		public void run() {

		}
	}

}