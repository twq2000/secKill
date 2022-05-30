package cn.twq.secKill.util;

import cn.twq.secKill.entity.User;
import cn.twq.secKill.vo.Result;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** 用户生成工具类 */
public final class UserUtils {

  public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException {
    createUser(5000);
  }

  private static void createUser(int size)
      throws SQLException, ClassNotFoundException, IOException {
    List<User> userList = new ArrayList<>(size);
    // 生成用户对象
    for (int i = 0; i < size; i++) {
      User user = new User();
      user.setId(13000000000L + i);
      user.setUsername("test" + i);
      user.setSalt("1a2b3c4d");
      user.setPassword(MD5Utils.encodePassword("123456", user.getSalt()));
      userList.add(user);
    }
     // 将对象数据批量插入数据库
//    batchInsert(userList);
    // 用户登录，并生成 cookie(loginTicket)
    userLogin(userList);
  }

  private static void userLogin(List<User> userList) throws IOException {
    File file = new File("C:\\Users\\twq31\\Desktop\\user.txt");
    if (file.exists()) {
      file.delete();
    }
    RandomAccessFile raf = new RandomAccessFile(file, "rw");
    file.createNewFile();
    raf.seek(0);
    StringBuilder strBuilder = new StringBuilder();
    for (User user : userList) {
      URL url = new URL("http://localhost:8080/login/doLogin");
      HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
      urlConnection.setRequestMethod("POST");
      urlConnection.setDoOutput(true);
      OutputStream outputStream = urlConnection.getOutputStream();
      String params =
          strBuilder
              .append("mobile=")
              .append(user.getId())
              .append("&password=")
              .append(MD5Utils.encodePassword2Backend("123456"))
              .toString();
      strBuilder.setLength(0);
      outputStream.write(params.getBytes(StandardCharsets.UTF_8));
      outputStream.flush();
      InputStream inputStream = urlConnection.getInputStream();
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      byte[] buffer = new byte[1024];
      int len;
      while ((len = inputStream.read(buffer)) >= 0) {
        byteArrayOutputStream.write(buffer, 0, len);
      }
      inputStream.close();
      byteArrayOutputStream.close();
      String response = byteArrayOutputStream.toString(StandardCharsets.UTF_8);
      ObjectMapper objectMapper = new ObjectMapper();
      Result result = objectMapper.readValue(response, Result.class);
      String loginTicket = ((String) result.getData());
      String row = strBuilder.append(user.getId()).append(',').append(loginTicket).toString();
      strBuilder.setLength(0);
      raf.seek(raf.length());
      raf.write(row.getBytes(StandardCharsets.UTF_8));
      raf.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }
    raf.close();
  }

  private static void batchInsert(List<User> userList) throws SQLException, ClassNotFoundException {
    Connection connection = getConnection();
    String sql = "insert into seckill.t_user(id, username, salt, password) values(?,?,?,?)";
    PreparedStatement statement = connection.prepareStatement(sql);
    for (User user : userList) {
      statement.setLong(1, user.getId());
      statement.setString(2, user.getUsername());
      statement.setString(3, user.getSalt());
      statement.setString(4, user.getPassword());
      statement.addBatch();
    }
    statement.executeBatch();
    statement.clearParameters();
    connection.close();
  }

  private static Connection getConnection() throws ClassNotFoundException, SQLException {
    String driver = "com.mysql.cj.jdbc.Driver";
    String url =
        "jdbc:mysql://47.102.197.62:3306/seckill?serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=utf8&useUnicode=true";
    String username = "root";
    String password = "Tang_1012";
    Class.forName(driver);
    return DriverManager.getConnection(url, username, password);
  }
}
