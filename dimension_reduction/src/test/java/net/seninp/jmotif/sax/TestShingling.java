package test.java.net.seninp.jmotif.sax;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;

import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;

import org.junit.Test;

/**
 * Test SAX factory methods.
 * 
 * @author Pavel Senin
 * 
 */
public class TestShingling {

  /**
   * Testing the permutation production.
   */
  @Test
  public void testPermutations() throws NumberFormatException, IOException, SAXException {

    String[] arr = { "a", "b", "c" };

    String[] perm2 = SAXProcessor.getAllPermutations(arr, 2);
    assertEquals("Testing the resulting array's length.", 9, perm2.length);

    String asString2 = Arrays.toString(perm2);
    assertTrue("Testing the specific word is present.", asString2.contains("ca"));

    String[] perm5 = SAXProcessor.getAllPermutations(arr, 5);
    assertEquals("Testing the resulting array's length.", 3 * 3 * 3 * 3 * 3, perm5.length);

    String asString5 = Arrays.toString(perm5);
    assertTrue("Testing the specific word is present.", asString5.contains("caaca"));

  }

}
