package ca.crim.nlp.pacte;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import org.junit.Ignore;

import ca.crim.nlp.pacte.client.Admin;

public class DemoTest {

    @Test
    @Ignore
    public void runDemo() {
        QuickConfig loCfg = new QuickConfig();
        Admin loAdmin = new Admin(loCfg);

        loAdmin.createUser("menardpa@crim.ca", "demo1testing", "pa", "menard");

        assertNotNull(loAdmin.checkUser("menardpa@crim.ca", "demo1testing"));
        // Demo loDemo = new Demo(loCfg);
        // loDemo.giveRessources(true, true, true);

    }
}
