package dk.jlo.arqdemo.poc;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

@RunWith(Arquillian.class)
public class GrapheneTeEXCLUDEst {
    @Drone
    WebDriver driver;

    @Before
    public void beforeEachTest() {
        driver.manage().deleteAllCookies();
    }

    @Test
    public void testHappyPath() {
        driver.get("http://www.google.com");
        String pageTitle = driver.getTitle();
        Assert.assertEquals(pageTitle, "Google");
    }
}
