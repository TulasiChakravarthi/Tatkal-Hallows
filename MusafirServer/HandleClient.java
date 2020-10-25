package MusafirServer;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

import Classes.*;

public class HandleClient implements Runnable {
    private Conn c1 = new Conn();
    private Socket socket;

    public HandleClient(Socket socket) {
        this.socket = socket;
    }

    private String Login(LoginInfo loginInfo) {
        try {
            String query = "SELECT * FROM `user_info` WHERE email='" + loginInfo.getUsername() + "' AND password='"
                    + String.valueOf(loginInfo.getPassword()) + "'";

            ResultSet rs = c1.s.executeQuery(query);
            if (rs.next()) {
                return rs.getString("name");
            }
            query = "SELECT * FROM `user_info` WHERE phone='" + loginInfo.getUsername() + "' AND password='"
                    + String.valueOf(loginInfo.getPassword()) + "'";
            rs = c1.s.executeQuery(query);
            if (rs.next()) {
                return rs.getString("name");
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        return "";
    }

    private Boolean Signup(UserInfo userInfo) {

        String d = userInfo.getMm() + "-" + userInfo.getDd() + "-" + userInfo.getYy();
        String query = "INSERT INTO `user_info` ( `name`, `dob`, `gender`, `email`, `phone`, `password`) VALUES ( '"
                + userInfo.getName() + "', STR_TO_DATE('" + d + "','%m-%d-%Y'), '" + userInfo.getGender() + "', '"
                + userInfo.getEmail() + "', '" + userInfo.getPhone() + "', '" + String.valueOf(userInfo.getPassword())
                + "')";
        try {
            c1.s.executeUpdate(query);
        } catch (Exception e) {
            e.printStackTrace();

        }

        return true;
    }

    private String[][] GetCities() {
        String[][] cities = new String[2][];
        String query = "SELECT COUNT(*) FROM Cities";

        try {
            ResultSet rs = c1.s.executeQuery(query);
            rs.next();
            int n = rs.getInt(1);
            cities[0] = new String[n];
            cities[1] = new String[n];
            query = "SELECT * FROM Cities";
            rs = c1.s.executeQuery(query);
            for (int i = 0; i < n; i++) {
                rs.next();
                cities[0][i] = (String) rs.getString("stations");
                cities[1][i] = (String) rs.getString("short");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return cities;
    }

    private String AdminLogin(LoginInfo loginInfo) {
        try {
            String query = "SELECT * FROM `admin_info` WHERE email='" + loginInfo.getUsername() + "' AND password='"
                    + String.valueOf(loginInfo.getPassword()) + "'";

            ResultSet rs = c1.s.executeQuery(query);
            if (rs.next()) {
                return rs.getString("name");
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        return "";
    }

    private Vector<AvailabilityInfo> ScheduleEnq(ScheduleEnq scheduleEnq) {
        Vector<AvailabilityInfo> availabilityInfo = new Vector<AvailabilityInfo>();
        AvailabilityInfo temp;
        int train;
        String query = "SELECT * FROM `month` WHERE `date` = '" + scheduleEnq.getDate() + "'", query2;
        String st1,st2;
        String source = scheduleEnq.getSource();
        String dest = scheduleEnq.getDest();
        System.out.println("in the function");
        try {
            ResultSet rs1 = c1.s.executeQuery(query), rs2, rs3;
            while (rs1.next()) {
                train = (int) rs1.getInt("train");
                query2 = "SELECT * FROM `src_dest_table` WHERE `train_no` = " + train + " ORDER BY `station_no` ASC";
                Conn c2 = new Conn();
                rs2 = c2.s.executeQuery(query2);

                while (rs2.next()) {
                    
                    st1=(String)rs2.getString("station");
                    System.out.println(st1);
                    if (source.equals(st1)) {
                        rs3 = rs2;
                        System.out.println("equals");
                        while (rs3.next()) {
                            System.out.println("rs3");
                            st2=(String)rs3.getString("station");
                            if (dest.equals(st2)) {
                                temp = new AvailabilityInfo(true, train, rs1.getInt("Avail_S"), rs1.getInt("Avail_AC"),
                                        rs2.getTimestamp("departure"), rs3.getTimestamp("arrival"));
                                        
                                availabilityInfo.add(temp);
                                System.out.println("added");
                            }
                        }
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return availabilityInfo;
    }

    @Override
    public void run() {
        while (true) {
            try {

                ObjectInputStream oi = new ObjectInputStream(socket.getInputStream());

                ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
                int choice = (int) oi.readInt();
                switch (choice) {
                    case 1:
                        LoginInfo loginInfo = (LoginInfo) oi.readObject();
                        String s = Login(loginInfo);
                        os.writeUTF(s);
                        os.flush();
                        break;

                    case 2:
                        UserInfo userInfo = (UserInfo) oi.readObject();
                        Boolean b = Signup(userInfo);
                        os.writeBoolean(b);
                        os.flush();
                        break;

                    case 3:
                        String[][] cities = GetCities();
                        os.writeObject(cities);
                        os.flush();
                        break;

                    case 4:
                        LoginInfo adminLogin = (LoginInfo) oi.readObject();
                        String s1 = AdminLogin(adminLogin);
                        os.writeUTF(s1);
                        os.flush();
                        break;
                    case 5:
                        ScheduleEnq scheduleEnq = (ScheduleEnq) oi.readObject();
                        Vector<AvailabilityInfo> availabilityInfo = ScheduleEnq(scheduleEnq);
                        //os.writeObject(availabilityInfo);
                        os.flush();
                        System.out.println(availabilityInfo.get(0).getTrain());

                        break;

                }

            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }

    }
}
