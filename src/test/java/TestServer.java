import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertTrue;

public class TestServer {
    static final String[] params = {"8081"};
    @Test
    public void Test() {
        try {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Server.main(params);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            t.start();

            sleep(1000);

            Process p;

            p = Runtime.getRuntime().exec("nmap -p 8081 localhost");
            p.waitFor();

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));

            boolean portOpen = false;
            String line = "";
            while ((line = reader.readLine())!= null) {
                System.out.println(line);
                if (line.contains("open")) {
                    portOpen = true;
                }
            }

            assertTrue("Server startup failed", portOpen);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
