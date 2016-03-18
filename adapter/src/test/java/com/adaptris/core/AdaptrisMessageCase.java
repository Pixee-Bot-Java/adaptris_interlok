/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.adaptris.core.stubs.MockEncoder;
import com.adaptris.util.stream.StreamUtil;

@SuppressWarnings("deprecation")
public abstract class AdaptrisMessageCase {

  protected static final String PAYLOAD = "Glib jocks quiz nymph to vex dwarf";
  protected static final String PAYLOAD2 = "Pack my box with five dozen liquor jugs";

  protected static final String VAL2 = "val2";
  protected static final String KEY2 = "key2";
  protected static final String VAL1 = "val1";
  protected static final String KEY1 = "key1";

  private AdaptrisMessage createMessage() throws UnsupportedEncodingException {
    return createMessage(null);
  }

  private AdaptrisMessage createMessage(String charEncoding) throws UnsupportedEncodingException {
    return getMessageFactory().newMessage(PAYLOAD, charEncoding, createMetadata());
  }

  private Set<MetadataElement> createMetadata() {
    Set<MetadataElement> metadata = new HashSet(Arrays.asList(new MetadataElement[] {
        new MetadataElement(KEY1, VAL1), new MetadataElement(KEY2, VAL2)
    }));
    return metadata;

  }
  protected abstract AdaptrisMessageFactory getMessageFactory();

  @Test
  public void testGetMessageFactory() throws Exception {
    AdaptrisMessageFactory mf = getMessageFactory();
    AdaptrisMessage msg = mf.newMessage();
    assertEquals(mf, msg.getFactory());
  }

  @Test
  public void testSetNextServiceId() throws Exception {
    String nextServiceId = "NEXT";
    AdaptrisMessage msg1 = createMessage();
    msg1.setNextServiceId(nextServiceId);
    assertEquals(nextServiceId, msg1.getNextServiceId());
    try {
      msg1.setNextServiceId(null);
      fail();
    }
    catch (IllegalArgumentException e) {

    }
    assertEquals(nextServiceId, msg1.getNextServiceId());
  }

  @Test
  public void testToString() throws Exception {
    AdaptrisMessage msg1 = createMessage();
    assertNotNull(msg1.toString());
    assertNotNull(msg1.toString(true));
    assertNotNull(msg1.toString(false));
    assertNotNull(msg1.toString(true, true));
    assertNotNull(msg1.toString(true, false));
    assertNotNull(msg1.toString(false, true));
    assertNotNull(msg1.toString(false, false));
  }

  @Test
  public void testGetPayload() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    assertEquals(PAYLOAD, new String(msg1.getPayload()));
  }

  @Test
  public void testSetPayload() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    msg1.setPayload(PAYLOAD2.getBytes());
    assertTrue(Arrays.equals(PAYLOAD2.getBytes(), msg1.getPayload()));
  }

  @Test
  public void testGetStringPayload() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    assertEquals(PAYLOAD, msg1.getContent());
  }

  @Test
  public void testSetStringPayload() throws Exception {
    AdaptrisMessage msg1 = createMessage();
    msg1.setContent(PAYLOAD2, msg1.getContentEncoding());
    assertEquals(PAYLOAD2, msg1.getContent());
    // with spec. char enc.
    String enc = "ISO-8859-1";
    String payload2 = new String(PAYLOAD2.getBytes(), enc);

    msg1.setContent(payload2, enc);
    assertEquals(payload2, msg1.getContent());
  }

  @Test
  public void testSetStringPayload_RemovesEncoding() throws Exception {
    AdaptrisMessage msg1 = createMessage();
    msg1.setContentEncoding("ISO-8859-1");
    assertEquals("ISO-8859-1", msg1.getContentEncoding());
    msg1.setStringPayload(PAYLOAD2);
    assertEquals(PAYLOAD2, msg1.getContent());
    assertNull(msg1.getContentEncoding());
  }

  @Test
  public void testGetMetadataValue() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    assertTrue(msg1.getMetadataValue("key1").equals("val1"));
    assertTrue(msg1.getMetadataValue("key3") == null);
    assertNull(msg1.getMetadataValue(null));
  }
  
  @Test
  public void testGetReferencedMetadata() throws Exception {
    AdaptrisMessage msg1 = createMessage();
    
    msg1.addMessageHeader("RefKey", "key1");

    assertTrue(msg1.getMetadataValue("RefKey").equals("key1"));
    assertTrue(msg1.getMetadataValue("$$RefKey").equals("val1"));
  }

  @Test
  public void testGetMetadata() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    assertEquals(createMetadata(), msg1.getMetadata());
  }

  @Test
  public void testEncoder() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    MockEncoder m = new MockEncoder();
    byte[] bytes = msg1.encode(m);
    assertTrue(Arrays.equals(PAYLOAD.getBytes(), bytes));
    byte[] b2 = msg1.encode(null);
    assertTrue(Arrays.equals(PAYLOAD.getBytes(), b2));
  }

  @Test
  public void testContainsKey() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    assertTrue(msg1.headersContainsKey("key1"));
    assertTrue(!msg1.headersContainsKey("key3"));
  }
  
  @Test
  public void testContainsReferencedKey() throws Exception {
    AdaptrisMessage msg1 = createMessage();
    
    msg1.addMessageHeader("RefKey1", "key1");

    assertTrue(msg1.headersContainsKey("RefKey1"));
    assertTrue(msg1.headersContainsKey("$$RefKey1")); // tests for "key1"
  }

  @Test
  public void testAddMetadata() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    msg1.addMetadata("key4", "val4");
    assertTrue(msg1.getMetadataValue("key4").equals("val4"));

    msg1.addMetadata(new MetadataElement("key5", "val5"));
    assertTrue(msg1.getMetadataValue("key5").equals("val5"));
  }
  
  @Test
  public void testAddReferencedMetadata() throws Exception {
    AdaptrisMessage msg1 = createMessage();
    
    msg1.addMessageHeader("RefKey", "key999");

    msg1.addMetadata("$$RefKey", "val999");
    assertTrue(msg1.getMetadataValue("key999").equals("val999"));
    assertTrue(msg1.getMetadataValue("$$RefKey").equals("val999"));
  }

  @Test
  public void testGetMetadataElement() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    MetadataElement me = new MetadataElement(KEY1, VAL1);
    assertEquals(me, msg1.getMetadata(KEY1));
    assertNull(msg1.getMetadata(null));
    assertNull(msg1.getMetadata("something"));
  }

  @Test
  public void testSetMetadata() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    MetadataElement mez = new MetadataElement("key6", "val6");
    Set newMetadata = new HashSet();
    newMetadata.add(mez);

    msg1.clearMetadata();
    msg1.setMetadata(newMetadata);

    assertTrue(newMetadata.equals(msg1.getMetadata()));
  }

  @Test
  public void testClearMetadata() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    msg1.clearMetadata();
    assertEquals(new HashSet(), msg1.getMetadata());
  }

  @Test
  public void testGetSize() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    assertEquals(PAYLOAD.length(), msg1.getSize());
  }

  @Test
  public void testSetCharEncoding() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    msg1.setContentEncoding("ISO-8859-1");

    assertTrue(msg1.getContentEncoding().equals("ISO-8859-1"));
  }

  @Test
  public void testGetUniqueId() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    assertTrue(msg1.getUniqueId() != null && !msg1.getUniqueId().equals(""));
  }

  @Test
  public void testSetUniqueId() throws Exception {
    AdaptrisMessage msg1 = createMessage();
    msg1.setUniqueId("uuid");

    assertTrue(msg1.getUniqueId().equals("uuid"));
  }

  @Test
  public void testAddMessageEvent() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    msg1.addEvent(new StandaloneProducer(), true);
    msg1.addEvent(new StandaloneProducer(), true);
    msg1.addEvent(new StandaloneProducer(), true);
    assertEquals(3, msg1.getMessageLifecycleEvent().getMleMarkers().size());
    msg1.addMetadata(CoreConstants.MLE_SEQUENCE_KEY, "FRED");
    msg1.addEvent(new StandaloneProducer(), true);
    assertEquals(4, msg1.getMessageLifecycleEvent().getMleMarkers().size());
    // Should have been reset by the "non-int" mle sequence number
    assertEquals("1", msg1.getMetadataValue(CoreConstants.MLE_SEQUENCE_KEY));
  }

  @Test
  public void testObjectMetadata() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    Object metadata2 = new Object();
    msg1.addObjectHeader("key", metadata2);

    java.util.Map<?,?> objectMetadata = msg1.getObjectHeaders();

    assertTrue(objectMetadata.keySet().size() == 1);
    assertTrue(metadata2.equals(objectMetadata.get("key")));
  }

  @Test
  public void testCloneAdaptrisMessage() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    msg1.addEvent(new StandaloneProducer(), true);
    AdaptrisMessage msg2 = (AdaptrisMessage) msg1.clone();

    assertTrue(msg2.getPayload() != msg1.getPayload());
    assertTrue(msg2.getMetadata() != msg1.getMetadata());
    assertTrue(msg2.getMessageLifecycleEvent() != msg1.getMessageLifecycleEvent());
    assertTrue(msg2.getStringPayload().equals(msg1.getStringPayload()));
    assertTrue(msg2.getMetadata().equals(msg1.getMetadata()));
    MessageLifecycleEvent event1 = msg1.getMessageLifecycleEvent();
    MessageLifecycleEvent event2 = msg2.getMessageLifecycleEvent();
    assertEquals(event1.getCreationTime(), event2.getCreationTime());
    assertEquals(event1.getMessageUniqueId(), event2.getMessageUniqueId());
    assertEquals(event1.getMleMarkers().size(), event2.getMleMarkers().size());
  }

  @Test
  public void testEquivalentForTracking() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    AdaptrisMessage msg2 = (AdaptrisMessage) msg1.clone();
    assertTrue(msg2.equivalentForTracking(msg1));
    msg2.setContentEncoding(null);
    msg1.setContentEncoding(null);
    assertTrue(msg2.equivalentForTracking(msg1));
    msg2.setContentEncoding(null);
    msg1.setContentEncoding("UTF-8");
    assertFalse(msg2.equivalentForTracking(msg1));

    msg2.setContentEncoding("ISO-8859-1");
    assertFalse(msg2.equivalentForTracking(msg1));
    msg1.setContentEncoding(null);
    assertFalse(msg2.equivalentForTracking(msg1));

    msg2.setContentEncoding("UTF-8");
    msg1.setContentEncoding("UTF-8");
    assertTrue(msg2.equivalentForTracking(msg1));

  }

  @Test
  public void testGetMetadataValueIgnoreKeyCaseExactMatch() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    msg1.addMetadata("AAA", "1");
    msg1.addMetadata("aaa", "2");
    msg1.addMetadata("Aaa", "3");
    msg1.addMetadata("aAa", "4");
    msg1.addMetadata("aaA", "5");

    String result1 = msg1.getMetadataValueIgnoreKeyCase("AAA");
    assertTrue("1".equals(result1));

    String result2 = msg1.getMetadataValueIgnoreKeyCase("aaa");
    assertTrue("2".equals(result2));

    String result3 = msg1.getMetadataValueIgnoreKeyCase("Aaa");
    assertTrue("3".equals(result3));

    String result4 = msg1.getMetadataValueIgnoreKeyCase("aAa");
    assertTrue("4".equals(result4));

    String result5 = msg1.getMetadataValueIgnoreKeyCase("aaA");
    assertTrue("5".equals(result5));

    String result6 = msg1.getMetadataValueIgnoreKeyCase("aaaa");
    assertTrue(result6 == null);
  }

  @Test
  public void testGetMetadataValueIgnoreKeyCase() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    msg1.addMetadata("AAA", "1");

    String result1 = msg1.getMetadataValueIgnoreKeyCase("aaa");
    assertTrue("1".equals(result1));

    String result2 = msg1.getMetadataValueIgnoreKeyCase("Aaa");
    assertTrue("1".equals(result2));

    String result3 = msg1.getMetadataValueIgnoreKeyCase("aAa");
    assertTrue("1".equals(result3));

    String result4 = msg1.getMetadataValueIgnoreKeyCase("aaA");
    assertTrue("1".equals(result4));

    String result5 = msg1.getMetadataValueIgnoreKeyCase("aaaa");
    assertTrue(result5 == null);
  }

  @Test
  public void testInputStream() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    InputStream in = msg1.getInputStream();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    StreamUtil.copyStream(in, out);
    assertEquals(PAYLOAD, out.toString());
  }

  @Test
  public void testOutputStream() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    PrintStream out = new PrintStream(msg1.getOutputStream());
    out.print(PAYLOAD2);
    // w/o closing the output stream, it's not going to be equal
    assertNotSame(PAYLOAD2, msg1.getStringPayload());
    out.close();
    assertEquals(PAYLOAD2, msg1.getStringPayload());
  }

  @Test
  public void testReader() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    Reader in = msg1.getReader();
    StringWriter out = new StringWriter();
    IOUtils.copy(in, out);
    out.flush();
    assertEquals(PAYLOAD, out.toString());
  }

  @Test
  public void testReaderWithCharEncoding() throws Exception {
    AdaptrisMessage msg1 = createMessage("ISO-8859-1");
    Reader in = msg1.getReader();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    IOUtils.copy(in, out, "ISO-8859-1");
    out.flush();
    assertTrue(Arrays.equals(msg1.getPayload(), out.toByteArray()));
  }

  @Test
  public void testWriter() throws Exception {
    AdaptrisMessage msg1 = createMessage();

    PrintWriter out = new PrintWriter(msg1.getWriter());
    out.print(PAYLOAD2);
    // w/o closing the output stream, it's not going to be equal
    assertNotSame(PAYLOAD2, msg1.getStringPayload());
    out.close();
    assertEquals(PAYLOAD2, msg1.getStringPayload());
  }

  @Test
  public void testWriter_UnchangedCharEncoding() throws Exception {
    AdaptrisMessage msg1 = createMessage("ISO-8859-1");

    PrintWriter out = new PrintWriter(msg1.getWriter());
    out.print(PAYLOAD2);
    // w/o closing the output stream, it's not going to be equal
    assertNotSame(PAYLOAD2, msg1.getStringPayload());
    out.close();
    assertTrue(Arrays.equals(PAYLOAD2.getBytes("ISO-8859-1"), msg1.getPayload()));
  }

  @Test
  public void testWriter_ChangeCharEncoding() throws Exception {
    StringBuilder sb = new StringBuilder(PAYLOAD2);
    Charset iso8859 = Charset.forName("ISO-8859-1");

    ByteBuffer inputBuffer = ByteBuffer.wrap(new byte[]
    {
      (byte) 0xFC // a u with an umlaut in ISO-8859-1
    });

    CharBuffer d1 = iso8859.decode(inputBuffer);
    sb.append(d1.toString());
    String payload = sb.toString();
    AdaptrisMessage msg1 = createMessage("ISO-8859-1");

    PrintWriter out = new PrintWriter(msg1.getWriter("UTF-8"));
    out.print(payload);
    out.close();
    assertEquals("UTF-8", msg1.getCharEncoding());
    assertFalse(Arrays.equals(payload.getBytes("ISO-8859-1"), msg1.getPayload()));
  }

  @Test
  public void testWriter_ChangedCharEncodingNull() throws Exception {
    AdaptrisMessage msg1 = createMessage("ISO-8859-1");

    PrintWriter out = new PrintWriter(msg1.getWriter(null));
    out.print(PAYLOAD2);
    out.close();
    assertEquals("ISO-8859-1", msg1.getCharEncoding());
    assertTrue(Arrays.equals(PAYLOAD2.getBytes("ISO-8859-1"), msg1.getPayload()));
  }


}
