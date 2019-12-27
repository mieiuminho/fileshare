package util;

import org.junit.*;

public class HashPriorityBufferTest {
    private static RequestsBuffer<String> requests = new HashPriorityBuffer(10);

    @BeforeClass
    public static void setUpBeforeAll() {
        try {
            for (int i = 0; i < 2; i++) {
                requests.add("1 download blah blah blah");
                requests.add("1 download blah blah blah");
                requests.add("2 download blah blah blah");
                requests.add("3 download blah blah blah");
                requests.add("4 download blah blah blah");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void add() {
    }

    @Test
    public void get() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            System.out.println(requests.get());
        }
    }

    @Test
    public void getSessionID() {
        Assert.assertEquals("1", HashPriorityBuffer.getSessionID("1 download blah blah blah"));
    }
}
