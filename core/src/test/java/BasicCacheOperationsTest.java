/*
 * #%L
 * cache2k core package
 * %%
 * Copyright (C) 2000 - 2016 headissue GmbH, Munich
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
import org.cache2k.Cache;
import org.cache2k.CacheBuilder;
import org.cache2k.CacheEntry;
import org.cache2k.CacheException;
import org.cache2k.impl.InternalCache;
import org.cache2k.junit.FastTests;
import org.junit.*;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.*;

/**
 * Test basic cache operations on a shared cache in a simple configuration.
 * The cache may hold 1000 entries and has no expiry.
 */
@Category(FastTests.class)
public class BasicCacheOperationsTest {

  final static Integer KEY = 1;
  final static Integer OTHER_KEY = 2;
  final static Integer VALUE = 1;
  final static Integer OTHER_VALUE = 2;

  static Cache<Integer, Integer> staticCache;

  @BeforeClass
  public static void setUp() {
    staticCache = CacheBuilder
            .newCache(Integer.class, Integer.class)
            .name(BasicCacheOperationsTest.class)
            .eternal(true)
            .entryCapacity(1000)
            .build();
  }

  /**
   * Used cache is a class field. We may subclass this class and run the tests with a different
   * configuration.
   */
  Cache<Integer, Integer> cache;

  @Before
  public void initCache() {
    cache = staticCache;
  }

  @After
  public void cleanupCache() {
    assertTrue("Tests are not allowed to create private caches", staticCache == cache);
    ((InternalCache) cache).checkIntegrity();
    cache.clear();
  }

  @AfterClass
  public static void tearDown() {
    staticCache.close();
  }

  /*
   * initial: Tests on the initial state of the cache.
   */

  @Test
  public void initial_Iterator() {
    assertFalse(cache.iterator().hasNext());
  }

  @Test
  public void initial_Peek() {
    assertNull(cache.peek(KEY));
    assertNull(cache.peek(OTHER_KEY));
  }

  @Test
  public void initial_Contains() {
    assertFalse(cache.contains(KEY));
    assertFalse(cache.contains(OTHER_KEY));
  }

  /**
   * Yields "org.cache2k.PropagatedCacheException: (expiry=none) org.cache2k.impl.CacheUsageExcpetion: source not set".
   * This is intentional, but maybe we change it in the future. At least check that we are consistent for now.
   */
  @Test(expected = CacheException.class)
  public void initial_Get() {
    cache.get(KEY);
  }

  /*
   * put
   */

  @Test
  public void put() {
    cache.put(KEY, VALUE);
    assertTrue(cache.contains(KEY));
    assertEquals(VALUE, cache.get(KEY));
    assertEquals(VALUE, cache.peek(KEY));
  }

  @Test
  public void put_Null() {
    cache.put(KEY, null);
    assertTrue(cache.contains(KEY));
    assertEquals(null, cache.peek(KEY));
    assertEquals(null, cache.get(KEY));
  }

  @Test(expected = NullPointerException.class)
  public void put_NullKey() {
    cache.put(null, VALUE);
  }

  /*
   * peekAndPut
   */
  @Test
  public void peekAndPut() {
    Integer v = cache.peekAndPut(KEY, VALUE);
    assertNull(v);
    v = cache.peekAndPut(KEY, VALUE);
    assertNotNull(v);
    assertEquals(VALUE, v);
  }

  @Test(expected = NullPointerException.class)
  public void peekAndPut_NullKey() {
    cache.peekAndPut(null, VALUE);
  }

  @Test
  public void peekAndPut_Null() {
    Integer v = cache.peekAndPut(KEY, null);
    assertNull(v);
    assertTrue(cache.contains(KEY));
    v = cache.peekAndPut(KEY, VALUE);
    assertNull(v);
    assertTrue(cache.contains(KEY));
    v = cache.peekAndPut(KEY, null);
    assertNotNull(v);
    assertEquals(VALUE, v);
    v = cache.peekAndPut(KEY, null);
    assertNull(v);
  }

  /*
   * peekAndRemove
   */

  @Test
  public void peekAndRemove() {
    Integer v = cache.peekAndRemove(KEY);
    assertNull(v);
    assertFalse(cache.contains(KEY));
    cache.put(KEY, VALUE);
    assertTrue(cache.contains(KEY));
    v = cache.peekAndRemove(KEY);
    assertNotNull(v);
    assertFalse(cache.contains(KEY));
  }

  @Test
  public void peekAndRemove_Null() {
    cache.put(KEY, null);
    assertTrue(cache.contains(KEY));
    Integer v = cache.peekAndRemove(KEY);
    assertNull(v);
    assertFalse(cache.contains(KEY));
  }

  @Test(expected = NullPointerException.class)
  public void peekAndRemove_NullKey() {
    cache.peekAndRemove(null);
  }

  /*
   * peekAndReplace
   */

  @Test
  public void peekAndReplace() {
    Integer v = cache.peekAndReplace(KEY, VALUE);
    assertNull(v);
    assertFalse(cache.contains(KEY));
    cache.put(KEY, VALUE);
    v = cache.peekAndReplace(KEY, OTHER_VALUE);
    assertNotNull(v);
    assertTrue(cache.contains(KEY));
    assertEquals(VALUE, v);
    assertEquals(OTHER_VALUE, cache.peek(KEY));
  }

  @Test
  public void peekAndReplace_Null() {
    Integer v = cache.peekAndReplace(KEY, null);
    assertNull(v);
    assertFalse(cache.contains(KEY));
    cache.put(KEY, VALUE);
    v = cache.peekAndReplace(KEY, null);
    assertNotNull(v);
    assertTrue(cache.contains(KEY));
    assertEquals(VALUE, v);
    assertNull(cache.peek(KEY));
  }

  @Test(expected = NullPointerException.class)
  public void peekAndReplace_NullKey() {
    cache.peekAndReplace(null, VALUE);
  }

  /*
   * peekEntry
   */

  @Test
  public void peekEntry() {
    long t0 = System.currentTimeMillis();
    CacheEntry<Integer, Integer> e = cache.peekEntry(KEY);
    assertNull(e);
    cache.put(KEY, VALUE);
    e = cache.peekEntry(KEY);
    assertEquals(KEY, e.getKey());
    assertEquals(VALUE, e.getValue());
    assertTrue(e.getLastModification() >= t0);
  }

  @Test
  public void peekEntry_Null() {
    long t0 = System.currentTimeMillis();
    CacheEntry<Integer, Integer> e = cache.peekEntry(KEY);
    assertNull(e);
    cache.put(KEY, null);
    e = cache.peekEntry(KEY);
    assertEquals(KEY, e.getKey());
    assertNull(e.getValue());
    assertTrue(e.getLastModification() >= t0);
  }

  @Test(expected = NullPointerException.class)
  public void peekEntry_NullKey() {
    cache.peekEntry(null);
  }

  /*
   * remove
   *
   * TODO: test remove
   */

  /*
   * replace_3arg
   *
   * TODO: null value
   * TODO: null key
   */

  @Test
  public void replace_3arg_Missing() {
    assertFalse(cache.replace(KEY, VALUE, OTHER_VALUE));
    assertFalse(cache.contains(KEY));
  }

  @Test
  public void replace_3arg() throws Exception {
    cache.put(KEY, VALUE);
    assertTrue(cache.replace(KEY, VALUE, OTHER_VALUE));
    assertEquals(OTHER_VALUE, cache.peek(KEY));
  }

  @Test
  public void replace_3arg_Different() {
    cache.put(KEY, VALUE);
    assertEquals(VALUE, cache.peek(KEY));
    assertFalse(cache.replace(KEY, OTHER_VALUE, OTHER_VALUE));
    assertEquals(VALUE, cache.peek(KEY));
  }

  @Test
  public void replace_3arg_NoMap() {
    cache.put(KEY, VALUE);
    assertFalse(cache.replace(OTHER_KEY, OTHER_VALUE, OTHER_VALUE));
    assertEquals(VALUE, cache.peek(KEY));
    assertNull(cache.peek(OTHER_KEY));
    assertFalse(cache.contains(OTHER_KEY));
  }

  /*
   * replace_2arg
   *
   * TODO: null value
   * TODO: null key
   */

  @Test
  public void replace_2arg() {
    cache.put(KEY, VALUE);
    assertTrue(cache.replace(KEY, OTHER_VALUE));
    assertEquals(OTHER_VALUE, cache.peek(KEY));
  }

  @Test
  public void replace_2arg_NoMap() {
    cache.put(KEY, VALUE);
    assertFalse(cache.replace(OTHER_KEY, OTHER_VALUE));
    assertEquals(VALUE, cache.peek(KEY));
    assertNull(cache.peek(OTHER_KEY));
    assertFalse(cache.contains(OTHER_KEY));
  }

}