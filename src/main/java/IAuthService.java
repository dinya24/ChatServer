import java.util.ArrayList;

public interface IAuthService {
    void start();

    void stop();

    ArrayList<String> getLoginList();

    String addLoginPass(String var1, String var2);

    void deleteByLogin(String var1);

    String getNickByLoginPass(String var1, String var2);
}
