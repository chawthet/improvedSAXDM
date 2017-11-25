package test.java.net.seninp.jmotif.sax.datastructures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import java.util.Collection;
import org.junit.Test;
import net.seninp.jmotif.sax.datastructure.SAXRecord;

/**
 * Test data structures used in the SAX implementation.
 * 
 * @author Pavel Senin.
 * 
 */
public class TestSaxRecord {

  private static final Integer iNum1 = 7;
  private static final Integer iNum2 = 3;
  private static final String str11 = "abggfecbb";

  private static final String str1 = "aaabbaa";
  private static final String str2 = "aaabbba";
  private static final Integer ONE = 1;
  private static final Integer ZERO = 0;

  /**
   * Test the SAX frequency structure.
   */
  @Test
  public void testSAXFrequencyEntry() {
    SAXRecord se = new SAXRecord(str11.toCharArray(), iNum2);

    assertTrue("Testing SAXRecord", str11.equalsIgnoreCase(String.valueOf(se.getPayload())));

    Collection<Integer> freqs = se.getIndexes();
    assertEquals("Testing SAXRecord", 1, freqs.size());
    assertTrue("Testing SAXRecord", freqs.contains(iNum2));

    se.addIndex(iNum1);
    Collection<Integer> freqs1 = se.getIndexes();
    assertEquals("Testing SAXRecord", 2, freqs1.size());
    assertTrue("Testing SAXRecord", freqs.contains(iNum2));
    assertTrue("Testing SAXRecord", freqs.contains(iNum1));

    se.addIndex(iNum2);
    Collection<Integer> freqs2 = se.getIndexes();
    assertEquals("Testing SAXRecord", 2, freqs2.size());
    assertTrue("Testing SAXRecord", freqs.contains(iNum2));
    assertTrue("Testing SAXRecord", freqs.contains(iNum1));
  }

  /**
   * Test constructor and setters/getters.
   * 
   */
  @Test
  public void setUp() {
    SAXRecord sfe1 = new SAXRecord(str1.toCharArray(), 0);
    assertTrue("Testing constructor", String.valueOf(sfe1.getPayload()).equalsIgnoreCase(str1));
    assertFalse("Testing constructor", String.valueOf(sfe1.getPayload()).equalsIgnoreCase(str2));
    assertEquals("Testing constructor", (Integer) sfe1.getIndexes().size(), ONE);
    assertTrue("Testing constructor", sfe1.getIndexes().contains(ZERO));

    sfe1.addIndex(15);
    assertTrue("Testing setter", sfe1.getIndexes().contains(15));
    assertFalse("Testing setter", sfe1.getIndexes().contains(11));
  }

  /**
   * Test comparison.
   * 
   */
  @Test
  public void testCompare() {
    SAXRecord sfe1 = new SAXRecord(str1.toCharArray(), 0);
    SAXRecord sfe2 = new SAXRecord(str1.toCharArray(), 0);
    SAXRecord sfe3 = new SAXRecord(str2.toCharArray(), 0);
    assertTrue("testing equals", sfe1.equals(sfe2));
    assertEquals("testing hashCode", sfe1.hashCode(), sfe2.hashCode());
    assertSame("testing comparison", sfe1.compareTo(sfe1), 0);
    assertTrue("testing comparison", sfe1.compareTo(sfe3) == 0);

    sfe2.addIndex(11);
    assertFalse("testing equals", sfe1.equals(sfe2));
    assertNotSame("testing hashCode", sfe1.hashCode(), sfe2.hashCode());

    assertTrue("testing comparison", sfe1.compareTo(sfe2) < 0);
  }

}
