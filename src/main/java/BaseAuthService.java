
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class BaseAuthService implements IAuthService {
    private static final String DATABASE_NAME = "chat.db";
    private Connection connection;

    public BaseAuthService() {
    }

    public void start() {
        try {
            this.connect("chat.db");
        } catch (ClassNotFoundException var2) {
            var2.printStackTrace();
        } catch (SQLException var3) {
            var3.printStackTrace();
        }

    }

    public void stop() {
        this.disconnect();
    }

    public String addLoginPass(String login, String pass) {
        int count = 0;

        try {
            PreparedStatement ps = this.connection.prepareStatement("SELECT COUNT(*) FROM users WHERE login = ? LIMIT 1");
            ps.setString(1, login);

            for(ResultSet rs = ps.executeQuery(); rs.next(); count = rs.getInt(1)) {
            }

            if (count == 0) {
                ps = this.connection.prepareStatement("INSERT INTO users (login, pass) VALUES (?, ?)");
                ps.setString(1, login);
                ps.setString(2, this.stringToMd5(pass));
                ps.execute();
                return login;
            }
        } catch (SQLException var6) {
            var6.printStackTrace();
        }

        return null;
    }

    public void deleteByLogin(String login) {
        try {
            PreparedStatement ps = this.connection.prepareStatement("DELETE FROM users WHERE login = ?");
            ps.setString(1, login);
            ps.execute();
        } catch (SQLException var3) {
            var3.printStackTrace();
        }

    }

    public ArrayList<String> getLoginList() {
        ArrayList users = new ArrayList();

        try {
            ResultSet rs = this.connection.createStatement().executeQuery("SELECT login FROM users");

            while(rs.next()) {
                users.add(rs.getString("login"));
            }
        } catch (SQLException var3) {
            var3.printStackTrace();
        }

        return users;
    }

    public String getNickByLoginPass(String login, String pass) {
        try {
            PreparedStatement ps = this.connection.prepareStatement("SELECT login FROM users WHERE login = ? AND pass = ? LIMIT 1");
            ps.setString(1, login);
            ps.setString(2, this.stringToMd5(pass));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString(1);
            }
        } catch (SQLException var5) {
            var5.printStackTrace();
        }

        return null;
    }

    private void disconnect() {
        try {
            this.connection.close();
        } catch (Exception var2) {
            var2.printStackTrace();
        }

    }

    private void connect(String filename) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        String url = "jdbc:mysql://127.0.0.1:3306/?user=root" + filename;
        this.connection = DriverManager.getConnection(url);
        if (this.connection != null) {
            this.connection.createStatement().execute("CREATE TABLE IF NOT EXISTS users (id    INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT NULL,login VARCHAR(255) UNIQUE,pass  TEXT)");
        }

    }

    private String stringToMd5(String string) {
        String generatedPassword = null;

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(string.getBytes());
            byte[] bytes = md.digest();
            StringBuilder sb = new StringBuilder();

            for(int i = 0; i < bytes.length; ++i) {
                sb.append(Integer.toString((bytes[i] & 255) + 256, 16).substring(1));
            }

            generatedPassword = sb.toString();
        } catch (NoSuchAlgorithmException var7) {
            var7.printStackTrace();
        }

        return generatedPassword;
    }
}
