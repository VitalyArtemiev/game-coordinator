import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import static org.junit.Assert.assertEquals;

public class TestServerResponse {
    static final String register = "{\n" +
            "  \"message\": {\n" +
            "    \"request\" : \"register\",\n" +
            "    \"name\" : \"newplayer1\"\n" +
            "  }\n" +
            "}";

    static final String roomList = "{\n" +
            "  \"message\": {\n" +
            "    \"request\" : \"roomList\",\n" +
            "    \"id\": 0\n" +
            "  }\n" +
            "}";

    static final String enterRoom = "{\n" +
            "  \"message\": {\n" +
            "    \"request\" : \"enterRoom\",\n" +
            "    \"id\": 0,\n" +
            "    \"roomId\" : 2\n" +
            "  }\n" +
            "}";

    static final String exitRoom = "{\n" +
            "  \"message\": {\n" +
            "    \"request\" : \"exitRoom\",\n" +
            "    \"id\": 0,\n" +
            "    \"roomId\" : 2\n" +
            "  }\n" +
            "}";

    static final String newRoom = "{\n" +
            "  \"message\": {\n" +
            "    \"request\" : \"newRoom\",\n" +
            "    \"roomName\" : \"firstroom\",\n" +
            "    \"playerLimit\":  8,\n" +
            "    \"id\" : 0\n" +
            "  }\n" +
            "}";

    static final String shutdown = "{\n" +
            "  \"message\": {\n" +
            "    \"request\" : \"shutdown\"\n" +
            "  }\n" +
            "}";

    @Test
    public void test() {
        // Create a new instance of the html unit driver
        // Notice that the remainder of the code relies on the interface,
        // not the implementation.
        WebDriver driver = new HtmlUnitDriver();

        // And now use this to visit Google
        driver.get("http://80.240.212.66:8081/");

        // Find the text input element by its name
        WebElement input = driver.findElement(By.name("message"));

        WebElement button = driver.findElement(By.name("button"));

        WebElement response = driver.findElement(By.id("responseText"));

        String s = response.getAttribute("value");
        System.out.println(s);
        //assertEquals("Failed to respond","Web Socket opened!", s);

        response.clear();

        try {
            input.sendKeys(register);
        }
        catch (Exception e) {

        }

        button.click();
        s = response.getAttribute("value");
        System.out.println(s);
        assertEquals("Failed to respond","", s);

        response.clear();
        try {
        input.sendKeys(roomList);
        }
        catch (Exception e) {}
        button.click();
        s = response.getAttribute("value");
        System.out.println(s);
        assertEquals("Failed to respond","", s);

        response.clear();
        try {
        input.sendKeys(enterRoom);
        }
        catch (Exception e) {}
        button.click();
        s = response.getAttribute("value");
        System.out.println(s);
        assertEquals("Failed to respond","", s);

        response.clear();
        try {
        input.sendKeys(exitRoom);
        }
        catch (Exception e) {}
        button.click();
        s = response.getAttribute("value");
        System.out.println(s);
        assertEquals("Failed to respond","", s);

        response.clear();
        try {
        input.sendKeys(newRoom);
        }
        catch (Exception e) {}
        button.click();
        s = response.getAttribute("value");
        System.out.println(s);
        assertEquals("Failed to respond","", s);

        response.clear();
        try {
        input.sendKeys(shutdown);
        }
        catch (Exception e) {}
        button.click();
        s = response.getAttribute("value");
        System.out.println(s);
        assertEquals("Failed to respond","", s);
    }

}
