package eduplay.module.games.bash;

import org.junit.Test;

import static org.junit.Assert.*;

public class BashTest {
    @Test
    public void whatIsStronger() throws Exception {
        Bash tester = new Bash();
        assertEquals("11 is stronger than 65, expected return value: 0",1,tester.whatIsStronger
                (66,21));
    }

}