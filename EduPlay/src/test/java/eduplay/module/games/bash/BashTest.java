package eduplay.module.games.bash;

import eduplay.connection.Callable;
import eduplay.connection.Coordinator;
import eduplay.gui.ApplicationWindow;
import org.junit.Before;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BashTest {

    Bash tester;
    Logger logger;
    @Before
    public void initialize() {
        tester = new Bash();
        logger = LoggerFactory.getLogger("testing");
        Coordinator.appWindow = mock(ApplicationWindow.class);
        Coordinator.player = mock(Callable.class);
    }

    @Test
    public void whatIsStronger() throws Exception {

        assertEquals("66 is not stronger than 66, expected return value: 1",1,tester.whatIsStronger
                (66,21));

        assertEquals("66 is stronger than 65, expected return value: 0",0,tester.whatIsStronger
                (66,65));

        assertEquals("66 is stronger than 65, expected return value: 0",2,tester.whatIsStronger
                (32,32));

        logger.debug("Executed test case:"+Thread.currentThread().getStackTrace()[1]);
    }

    @Test
    public void isValidThrow() throws Exception {


        Method method = tester.getClass().getDeclaredMethod("isValidThrowValue", int.class);
        method.setAccessible(true);
        assertTrue("The throw is valid",(boolean)method.invoke(tester,41));
        assertFalse("The throw is not valid",(boolean)method.invoke(tester,23));

        logger.debug("Executed test case:"+Thread.currentThread().getStackTrace()[1]);
    }

    @Test
    public void isGeneratedThrowValid() throws Exception {

        Method method = tester.getClass().getDeclaredMethod("isValidThrowValue", int.class);
        method.setAccessible(true);

        Method randThrow = tester.getClass().getDeclaredMethod("generateRandomThrow");
        randThrow.setAccessible(true);

        for (int i=0;i<5;i++) {
            assertTrue("The throw is valid",(boolean)method.invoke(tester,randThrow.invoke(tester)));
        }

        logger.debug("Executed test case:"+Thread.currentThread().getStackTrace()[1]);
    }

    @Test
    public void mockMethodsOfBash() throws Exception {
        Bash mockedBash = mock(Bash.class);

        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            System.out.println("called with arguments: " + args[0]);
            return null;
        }).when(mockedBash).print(anyString());

        when(mockedBash.getName()).thenReturn("Not Real Name");

        mockedBash.print("Hello World");

        assertEquals("Expected: Not Real Name","Not Real Name",mockedBash.getName());

        logger.debug("Executed test case:"+Thread.currentThread().getStackTrace()[1]);
    }



}