/*
 * Copyright 2015 Tagir Valeev
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
package javax.util.streamex;

import java.util.Arrays;
import java.util.OptionalDouble;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.DoubleStream;

import org.junit.Test;

import static org.junit.Assert.*;

public class DoubleStreamExTest {
    @Test
    public void testCreate() {
        assertArrayEquals(new double[] {}, DoubleStreamEx.empty().toArray(), 0.0);
        assertArrayEquals(new double[] {}, DoubleStreamEx.empty().toArray(), 0.0); // double check is intended
        assertArrayEquals(new double[] { 1 }, DoubleStreamEx.of(1).toArray(), 0.0);
        assertArrayEquals(new double[] { 1 }, DoubleStreamEx.of(OptionalDouble.of(1)).toArray(), 0.0);
        assertArrayEquals(new double[] {}, DoubleStreamEx.of(OptionalDouble.empty()).toArray(), 0.0);
        assertArrayEquals(new double[] { 1, 2, 3 }, DoubleStreamEx.of(1, 2, 3).toArray(), 0.0);
        assertArrayEquals(new double[] { 4, 6 }, DoubleStreamEx.of(new double[] {2, 4, 6, 8, 10}, 1, 3).toArray(), 0.0);
        assertArrayEquals(new double[] { 1, 2, 3 }, DoubleStreamEx.of(DoubleStream.of(1, 2, 3)).toArray(), 0.0);
        assertArrayEquals(new double[] { 1, 2, 3 }, DoubleStreamEx.of(Arrays.asList(1.0, 2.0, 3.0)).toArray(), 0.0);
        assertArrayEquals(new double[] { 1, 2, 4, 8, 16 }, DoubleStreamEx.iterate(1, x -> x*2).limit(5).toArray(), 0.0);
        assertArrayEquals(new double[] { 1, 1, 1, 1 }, DoubleStreamEx.generate(() -> 1).limit(4).toArray(), 0.0);
        assertArrayEquals(new double[] { 1, 1, 1, 1 }, DoubleStreamEx.constant(1.0, 4).toArray(), 0.0);
        assertEquals(10, DoubleStreamEx.of(new Random(), 10).count());
        assertTrue(DoubleStreamEx.of(new Random(), 100, 1, 10).allMatch(x -> x >= 1 && x < 10));
        assertArrayEquals(DoubleStreamEx.of(new Random(1), 100, 1, 10).toArray(), DoubleStreamEx.of(new Random(1), 1, 10).limit(100).toArray(), 0.0);

        DoubleStream stream = DoubleStreamEx.of(1, 2, 3);
        assertSame(stream, DoubleStreamEx.of(stream));
    }

    @Test
    public void testBasics() {
        assertFalse(DoubleStreamEx.of(1).isParallel());
        assertTrue(DoubleStreamEx.of(1).parallel().isParallel());
        assertFalse(DoubleStreamEx.of(1).parallel().sequential().isParallel());
        AtomicInteger i = new AtomicInteger();
        try (DoubleStreamEx s = DoubleStreamEx.of(1).onClose(() -> i.incrementAndGet())) {
            assertEquals(1, s.count());
        }
        assertEquals(1, i.get());
        assertEquals(6, IntStreamEx.range(0, 4).asDoubleStream().sum(), 0);
        assertEquals(3, IntStreamEx.range(0, 4).asDoubleStream().max().getAsDouble(), 0);
        assertEquals(0, IntStreamEx.range(0, 4).asDoubleStream().min().getAsDouble(), 0);
        assertEquals(1.5, IntStreamEx.range(0, 4).asDoubleStream().average().getAsDouble(), 0.000001);
        assertEquals(4, IntStreamEx.range(0, 4).asDoubleStream().summaryStatistics().getCount());
        assertArrayEquals(new double[] { 1, 2, 3 },
                IntStreamEx.range(0, 5).asDoubleStream().skip(1).limit(3).toArray(), 0.0);
        assertArrayEquals(new double[] { 1, 2, 3 }, DoubleStreamEx.of(3, 1, 2).sorted().toArray(), 0.0);
        assertArrayEquals(new double[] { 1, 2, 3 }, DoubleStreamEx.of(1, 2, 1, 3, 2).distinct().toArray(), 0.0);
        assertArrayEquals(new int[] { 2, 4, 6 }, IntStreamEx.range(1, 4).asDoubleStream().mapToInt(x -> (int) x * 2)
                .toArray());
        assertArrayEquals(new long[] { 2, 4, 6 }, IntStreamEx.range(1, 4).asDoubleStream().mapToLong(x -> (long) x * 2)
                .toArray());
        assertArrayEquals(new double[] { 2, 4, 6 }, IntStreamEx.range(1, 4).asDoubleStream().map(x -> x * 2).toArray(),
                0.0);
        assertArrayEquals(new double[] { 1, 3 }, IntStreamEx.range(0, 5).asDoubleStream().filter(x -> x % 2 == 1)
                .toArray(), 0.0);
        assertEquals(6.0, DoubleStreamEx.of(1.0, 2.0, 3.0).reduce(Double::sum).getAsDouble(), 0.0);
    }

    @Test
    public void testPrepend() {
        assertArrayEquals(new double[] { -1, 0, 1, 2, 3 }, DoubleStreamEx.of(1, 2, 3).prepend(-1, 0).toArray(), 0.0);
    }

    @Test
    public void testAppend() {
        assertArrayEquals(new double[] { 1, 2, 3, 4, 5 }, DoubleStreamEx.of(1, 2, 3).append(4, 5).toArray(), 0.0);
    }

    @Test
    public void testFind() {
        assertEquals(6.0, LongStreamEx.range(1, 10).asDoubleStream().findFirst(i -> i > 5).getAsDouble(), 0.0);
        assertFalse(LongStreamEx.range(1, 10).asDoubleStream().findAny(i -> i > 10).isPresent());
    }

    @Test
    public void testRemove() {
        assertArrayEquals(new double[] { 1, 2 }, DoubleStreamEx.of(1, 2, 3).remove(x -> x > 2).toArray(), 0.0);
    }

    @Test
    public void testSort() {
        assertArrayEquals(new double[] { 3, 2, 1 }, DoubleStreamEx.of(1, 2, 3).sortedByDouble(x -> -x).toArray(), 0.0);
        assertArrayEquals(
                new double[] { Double.POSITIVE_INFINITY, Double.MAX_VALUE, 1000, 1, Double.MIN_VALUE, 0, -0.0, -10,
                        -Double.MAX_VALUE, Double.NEGATIVE_INFINITY },
                DoubleStreamEx
                        .of(0, 1, 1000, -10, -Double.MAX_VALUE, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
                                Double.MAX_VALUE, -0.0, Double.MIN_VALUE).reverseSorted().toArray(), 0.0);
    }

    @Test
    public void testMinMax() {
        assertEquals(9, IntStreamEx.range(5, 12).asDoubleStream().max((a, b) -> String.valueOf(a).compareTo(String.valueOf(b))).getAsDouble(), 0.0);
        assertEquals(10, IntStreamEx.range(5, 12).asDoubleStream().min((a, b) -> String.valueOf(a).compareTo(String.valueOf(b))).getAsDouble(), 0.0);
        assertEquals(9, IntStreamEx.range(5, 12).asDoubleStream().maxBy(String::valueOf).getAsDouble(), 0.0);
        assertEquals(10, IntStreamEx.range(5, 12).asDoubleStream().minBy(String::valueOf).getAsDouble(), 0.0);
        assertEquals(5, IntStreamEx.range(5, 12).asDoubleStream().maxByDouble(x -> 1.0/x).getAsDouble(), 0.0);
        assertEquals(11, IntStreamEx.range(5, 12).asDoubleStream().minByDouble(x -> 1.0/x).getAsDouble(), 0.0);
        assertEquals(29.0, DoubleStreamEx.of(15, 8, 31, 47, 19, 29).maxByInt(x -> (int)(x % 10 * 10 + x / 10)).getAsDouble(), 0.0);
        assertEquals(31.0, DoubleStreamEx.of(15, 8, 31, 47, 19, 29).minByInt(x -> (int)(x % 10 * 10 + x / 10)).getAsDouble(), 0.0);
        assertEquals(29.0, DoubleStreamEx.of(15, 8, 31, 47, 19, 29).maxByLong(x -> (long)(x % 10 * 10 + x / 10)).getAsDouble(), 0.0);
        assertEquals(31.0, DoubleStreamEx.of(15, 8, 31, 47, 19, 29).minByLong(x -> (long)(x % 10 * 10 + x / 10)).getAsDouble(), 0.0);
    }
}
