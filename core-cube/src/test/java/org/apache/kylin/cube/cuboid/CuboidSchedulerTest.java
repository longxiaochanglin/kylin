/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.apache.kylin.cube.cuboid;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.kylin.common.util.LocalFileMetadataTestCase;
import org.apache.kylin.cube.CubeDescManager;
import org.apache.kylin.cube.model.CubeDesc;
import org.apache.kylin.metadata.MetadataManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;
import com.google.common.primitives.Longs;

/**
 * @author George Song (ysong1)
 * 
 */
public class CuboidSchedulerTest extends LocalFileMetadataTestCase {
    @Before
    public void setUp() throws Exception {
        this.createTestMetadata();
        MetadataManager.clearCache();
    }

    @After
    public void after() throws Exception {
        this.cleanupTestMetadata();
    }

    static long toLong(String bin) {
        return Long.parseLong(bin, 2);
    }

    static String toString(Collection<Long> cuboids) {
        StringBuilder buf = new StringBuilder();
        buf.append("[");
        for (Long l : cuboids) {
            if (buf.length() > 1)
                buf.append(",");
            buf.append(l).append("(").append(Long.toBinaryString(l)).append(")");
        }
        buf.append("]");
        return buf.toString();
    }

    private CubeDesc getTestKylinCubeWithoutSeller() {
        return getCubeDescManager().getCubeDesc("test_kylin_cube_without_slr_desc");
    }

    private CubeDesc getTestKylinCubeWithSeller() {
        return getCubeDescManager().getCubeDesc("test_kylin_cube_with_slr_desc");
    }

    private CubeDesc getTestKylinCubeWithSellerLeft() {
        return getCubeDescManager().getCubeDesc("test_kylin_cube_with_slr_left_join_desc");
    }

    private CubeDesc getTestKylinCubeWithoutSellerLeftJoin() {
        return getCubeDescManager().getCubeDesc("test_kylin_cube_without_slr_left_join_desc");
    }

    private CubeDesc getStreamingCubeDesc() {
        return getCubeDescManager().getCubeDesc("test_streaming_table_cube_desc");
    }

    private CubeDesc getFiftyDimCubeDesc() {
        return getCubeDescManager().getCubeDesc("fifty_dim");
    }

    private CubeDesc getTwentyDimCubeDesc() {
        return getCubeDescManager().getCubeDesc("twenty_dim");
    }

    private CubeDesc getSSBCubeDesc() {
        return getCubeDescManager().getCubeDesc("ssb");
    }

    private CubeDesc getCIInnerJoinCube() {
        return getCubeDescManager().getCubeDesc("ci_inner_join_cube");
    }

    private void testSpanningAndGetParent(CuboidScheduler scheduler, CubeDesc cube, long[] cuboidIds, long[] expectChildren) {
        Set<Long> totalSpanning = Sets.newHashSet();
        for (long cuboidId : cuboidIds) {
            List<Long> spannings = scheduler.getSpanningCuboid(cuboidId);
            totalSpanning.addAll(spannings);
            System.out.println("Spanning result for " + cuboidId + "(" + Long.toBinaryString(cuboidId) + "): " + toString(spannings));

            for (long child : spannings) {
                assertTrue(Cuboid.isValid(cube, child));
            }
        }

        long[] spanningsArray = Longs.toArray(totalSpanning);
        Arrays.sort(spanningsArray);
        Arrays.sort(expectChildren);
        assertArrayEquals(expectChildren, spanningsArray);
    }

    @Test
    public void testGetSpanningCuboid2() {
        CubeDesc cube = getTestKylinCubeWithSeller();
        CuboidScheduler scheduler = new CuboidScheduler(cube);

        // generate 8d
        System.out.println("Spanning for 8D Cuboids");
        testSpanningAndGetParent(scheduler, cube, new long[] { 511 }, new long[] { 504, 447, 503, 383 });
        // generate 7d
        System.out.println("Spanning for 7D Cuboids");
        testSpanningAndGetParent(scheduler, cube, new long[] { 504, 447, 503, 383 }, new long[] { 440, 496, 376, 439, 487, 319, 375 });
        // generate 6d
        System.out.println("Spanning for 6D Cuboids");
        testSpanningAndGetParent(scheduler, cube, new long[] { 440, 496, 376, 439, 487, 319, 375 }, new long[] { 432, 480, 312, 368, 423, 455, 311, 359 });
        // generate 5d
        System.out.println("Spanning for 5D Cuboids");
        testSpanningAndGetParent(scheduler, cube, new long[] { 432, 480, 312, 368, 423, 455, 311, 359 }, new long[] { 416, 448, 304, 352, 391, 295, 327 });
        // generate 4d
        System.out.println("Spanning for 4D Cuboids");
        testSpanningAndGetParent(scheduler, cube, new long[] { 416, 448, 304, 352, 391, 295, 327 }, new long[] { 384, 288, 320, 263 });
        // generate 3d
        System.out.println("Spanning for 3D Cuboids");
        testSpanningAndGetParent(scheduler, cube, new long[] { 384, 288, 320, 263 }, new long[0]);
        // generate 2d
        // generate 1d
        // generate 0d
    }

    @Test
    public void testGetSpanningCuboid1() {
        CubeDesc cube = getTestKylinCubeWithoutSeller();
        CuboidScheduler scheduler = new CuboidScheduler(cube);

        // generate 7d
        System.out.println("Spanning for 7D Cuboids");
        testSpanningAndGetParent(scheduler, cube, new long[] { 255 }, new long[] { 135, 251, 253, 254 });
        // generate 6d
        System.out.println("Spanning for 6D Cuboids");
        testSpanningAndGetParent(scheduler, cube, new long[] { 135, 251, 253, 254 }, new long[] { 131, 133, 134, 249, 250, 252 });
        // generate 5d
        System.out.println("Spanning for 5D Cuboids");
        testSpanningAndGetParent(scheduler, cube, new long[] { 131, 133, 134, 249, 250, 252 }, new long[] { 129, 130, 132, 248 });
        // generate 4d
        System.out.println("Spanning for 4D Cuboids");
        testSpanningAndGetParent(scheduler, cube, new long[] { 129, 130, 132, 248 }, new long[] { 184, 240 });
        // generate 3d
        System.out.println("Spanning for 3D Cuboids");
        testSpanningAndGetParent(scheduler, cube, new long[] { 184, 240 }, new long[] { 176, 224 });
        // generate 2d
        System.out.println("Spanning for 2D Cuboids");
        testSpanningAndGetParent(scheduler, cube, new long[] { 176, 224 }, new long[] { 160, 192 });
        // generate 1d
        System.out.println("Spanning for 1D Cuboids");
        testSpanningAndGetParent(scheduler, cube, new long[] { 160, 192 }, new long[0]);
        // generate 0d
    }

    @Test
    public void testGetCardinality() {
        CubeDesc cube = getTestKylinCubeWithSeller();
        CuboidScheduler scheduler = new CuboidScheduler(cube);

        assertEquals(0, scheduler.getCardinality(0));
        assertEquals(7, scheduler.getCardinality(127));
        assertEquals(1, scheduler.getCardinality(1));
        assertEquals(1, scheduler.getCardinality(8));
        assertEquals(6, scheduler.getCardinality(126));
    }

    @Test
    public void testCuboidGeneration1() {

        CubeDesc cube = getTestKylinCubeWithoutSeller();
        CuboidCLI.simulateCuboidGeneration(cube, true);
    }

    @Test
    public void testCuboidGeneration2() {
        CubeDesc cube = getTestKylinCubeWithSeller();
        CuboidCLI.simulateCuboidGeneration(cube, true);
    }

    @Test
    public void testCuboidGeneration3() {
        CubeDesc cube = getTestKylinCubeWithoutSellerLeftJoin();
        CuboidCLI.simulateCuboidGeneration(cube, true);
    }

    @Test
    public void testCuboidGeneration4() {
        CubeDesc cube = getTestKylinCubeWithSellerLeft();
        CuboidCLI.simulateCuboidGeneration(cube, true);
    }

    @Test
    public void testCuboidGeneration5() {
        CubeDesc cube = getStreamingCubeDesc();
        CuboidCLI.simulateCuboidGeneration(cube, true);
    }

    @Test
    public void testCuboidGeneration6() {
        CubeDesc cube = getSSBCubeDesc();
        CuboidCLI.simulateCuboidGeneration(cube, true);
    }

    @Test
    public void testCuboidGeneration7() {
        CubeDesc cube = getCIInnerJoinCube();
        CuboidCLI.simulateCuboidGeneration(cube, true);
    }

    @Test
    public void testCuboidCounts1() {
        CubeDesc cube = getTestKylinCubeWithoutSeller();
        CuboidScheduler cuboidScheduler = new CuboidScheduler(cube);
        int[] counts = CuboidCLI.calculateAllLevelCount(cube);
        printCount(counts);
        int sum = 0;
        for (Integer x : counts) {
            sum += x;
        }
        assertEquals(cuboidScheduler.getCuboidCount(), sum);
    }

    @Test
    public void testCuboidCounts2() {
        CubeDesc cube = getTestKylinCubeWithoutSellerLeftJoin();
        CuboidScheduler cuboidScheduler = new CuboidScheduler(cube);
        int[] counts = CuboidCLI.calculateAllLevelCount(cube);
        printCount(counts);
        int sum = 0;
        for (Integer x : counts) {
            sum += x;
        }
        assertEquals(cuboidScheduler.getCuboidCount(), sum);
    }

    @Test
    public void testCuboidCounts3() {
        CubeDesc cube = getTestKylinCubeWithSeller();
        CuboidScheduler cuboidScheduler = new CuboidScheduler(cube);
        int[] counts = CuboidCLI.calculateAllLevelCount(cube);
        printCount(counts);
        int sum = 0;
        for (Integer x : counts) {
            sum += x;
        }
        assertEquals(cuboidScheduler.getCuboidCount(), sum);
    }

    @Test
    public void testCuboidCounts4() {
        CubeDesc cube = getTestKylinCubeWithSellerLeft();
        CuboidScheduler cuboidScheduler = new CuboidScheduler(cube);
        int[] counts = CuboidCLI.calculateAllLevelCount(cube);
        printCount(counts);
        int sum = 0;
        for (Integer x : counts) {
            sum += x;
        }
        assertEquals(cuboidScheduler.getCuboidCount(), sum);
    }

    @Test
    public void testCuboidCounts5() {
        CubeDesc cube = getStreamingCubeDesc();
        CuboidScheduler cuboidScheduler = new CuboidScheduler(cube);
        int[] counts = CuboidCLI.calculateAllLevelCount(cube);
        printCount(counts);
        int sum = 0;
        for (Integer x : counts) {
            sum += x;
        }
        assertEquals(cuboidScheduler.getCuboidCount(), sum);
    }

    @Test
    public void testCuboidCounts6() {
        CubeDesc cube = getCIInnerJoinCube();
        CuboidScheduler cuboidScheduler = new CuboidScheduler(cube);
        int[] counts = CuboidCLI.calculateAllLevelCount(cube);
        printCount(counts);
        int sum = 0;
        for (Integer x : counts) {
            sum += x;
        }
        assertEquals(cuboidScheduler.getCuboidCount(), sum);
    }

    @Test
    public void testLargeCube() {
        CubeDesc cube = getFiftyDimCubeDesc();
        CuboidScheduler cuboidScheduler = new CuboidScheduler(cube);
        long start = System.currentTimeMillis();
        System.out.println(cuboidScheduler.getCuboidCount());
        System.out.println("build tree takes: " + (System.currentTimeMillis() - start) + "ms");
    }

    @Test(expected=RuntimeException.class)
    public void testTooManyCombination() {
        File twentyFile = new File(new File(LocalFileMetadataTestCase.LOCALMETA_TEMP_DATA, "cube_desc"), "twenty_dim");
        twentyFile.renameTo(new File(twentyFile.getPath().substring(0, twentyFile.getPath().length() - 4)));
        CubeDesc cube = getTwentyDimCubeDesc();
        CuboidScheduler cuboidScheduler = new CuboidScheduler(cube);
        cuboidScheduler.getCuboidCount();
        twentyFile.renameTo(new File(twentyFile.getPath() + ".bad"));
    }

    @Test
    public void testCuboid_onlyBaseCuboid() {
        for (File f : new File(LocalFileMetadataTestCase.LOCALMETA_TEMP_DATA, "cube_desc").listFiles()) {
            if (f.getName().endsWith(".bad")) {
                String path = f.getPath();
                f.renameTo(new File(path.substring(0, path.length() - 4)));
            }
        }
        CubeDescManager.clearCache();
        CubeDesc cube = getCubeDescManager().getCubeDesc("ut_large_dimension_number");
        CuboidScheduler scheduler = new CuboidScheduler(cube);

        Cuboid baseCuboid = Cuboid.getBaseCuboid(cube);
        assertTrue(Cuboid.isValid(cube, baseCuboid.getId()));

        List<Long> spanningChild = scheduler.getSpanningCuboid(baseCuboid.getId());
        assertTrue(spanningChild.size() > 0);
    }

    public CubeDescManager getCubeDescManager() {
        return CubeDescManager.getInstance(getTestConfig());
    }

    private void printCount(int[] counts) {
        int sum = 0;
        for (int c : counts)
            sum += c;
        System.out.println(sum + " = " + Arrays.toString(counts));
    }
}