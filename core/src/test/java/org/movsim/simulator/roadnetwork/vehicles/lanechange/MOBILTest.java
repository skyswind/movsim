/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 *                                   <movsim.org@gmail.com>
 * -----------------------------------------------------------------------------------------
 * 
 * This file is part of
 * 
 * MovSim - the multi-model open-source vehicular-traffic simulator.
 * 
 * MovSim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MovSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MovSim. If not, see <http://www.gnu.org/licenses/>
 * or <http://www.movsim.org>.
 * 
 * -----------------------------------------------------------------------------------------
 */
package org.movsim.simulator.roadnetwork.vehicles.lanechange;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.movsim.simulator.MovsimConstants;
import org.movsim.simulator.roadnetwork.Lane;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.lanechange.LaneChangeModel;
import org.movsim.simulator.vehicles.lanechange.MOBIL;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.IDM;

/**
 * Test module for the MOBIL class.
 * 
 */
public class MOBILTest {
    private static final double delta = 0.00001;

    private Vehicle newVehicle(double rearPosition, double speed, int lane, double length) {
        final IDM idm = new IDM(33.0, 0.5, 3.0, 1.5, 2.0, 5.0);
        final Vehicle vehicle = new Vehicle(rearPosition, speed, lane, length, 2.5);
        vehicle.setLongitudinalModel(idm);
        vehicle.setSpeedlimit(80.0 / 3.6); // 80 km/h
        return vehicle;
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link org.movsim.simulator.vehicles.lanechange.MOBIL#MOBIL(org.movsim.simulator.vehicles.Vehicle)}.
     */
    @Test
    public final void testMOBILVehicle() {
        //fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.movsim.simulator.vehicles.lanechange.MOBIL#MOBIL(org.movsim.simulator.vehicles.Vehicle, org.movsim.input.model.vehicle.lanechange.LaneChangeMobilData)}.
     */
    @Test
    public final void testMOBILVehicleLaneChangeMobilData() {
        //fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.movsim.simulator.vehicles.lanechange.MOBIL#MOBIL(org.movsim.simulator.vehicles.Vehicle, double, double, double, double, double)}.
     */
    @Test
    public final void testMOBILVehicleDoubleDoubleDoubleDoubleDouble() {
        //fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.movsim.simulator.vehicles.lanechange.MOBIL#calcAccelerationBalance(int, org.movsim.simulator.roadnetwork.RoadSegment)}.
     */
    @Test
    public final void testCalcAccelerationBalance() {
        final double lengthCar = 6.0;
        final double lengthTruck = 16.0;
        RoadSegment.resetNextId();
        Vehicle.resetNextId();
        final double roadLength = 1000.0;
        final int laneCount = 2;
        final RoadSegment roadSegment = new RoadSegment(roadLength, laneCount);
        final double minimumGap = 2.0;
        final double tooSmallGap = 1.0;
        final double safeDeceleration = 4.0;
        final double politeness = 0.1;
        final double thresholdAcceleration = 0.2;
        final double rightBiasAcceleration = 0.3;

        // set up a vehicle in lane 1, right lane
        final Vehicle v1 = newVehicle(900.0, 0.0, Lane.LANE1, lengthCar);
        final MOBIL m1 = new MOBIL(v1, minimumGap, safeDeceleration, politeness, thresholdAcceleration, rightBiasAcceleration);
        final LaneChangeModel lcm1 = new LaneChangeModel(v1, m1);
        v1.setLaneChangeModel(lcm1);
        roadSegment.addVehicle(v1);

        // set up a vehicle in lane 2, left lane
        final Vehicle v2 = newVehicle(900.0 - lengthCar - tooSmallGap, 0.0, Lane.LANE2, lengthCar);
        final MOBIL m2 = new MOBIL(v2, minimumGap, safeDeceleration, politeness, thresholdAcceleration, rightBiasAcceleration);
        final LaneChangeModel lcm2 = new LaneChangeModel(v2, m2);
        v2.setLaneChangeModel(lcm2);
        roadSegment.addVehicle(v2);

        // vehicles too close together, so acceleration balance should be large negative
        double balance = m1.calcAccelerationBalance(MovsimConstants.TO_LEFT, roadSegment);
        assertEquals(-Double.MAX_VALUE, balance, delta);
        balance = m2.calcAccelerationBalance(MovsimConstants.TO_RIGHT, roadSegment);
        assertEquals(-Double.MAX_VALUE, balance, delta);

        // now set up with sufficient gap between vehicles, but v2 needs to decelerate, so it is not
        // favourable to change lanes
        roadSegment.clearVehicles();
        v2.setFrontPosition(v1.getRearPosition() - 2 * minimumGap);
        v2.setSpeed(80.0 / 3.6); // 80 km/h
        roadSegment.addVehicle(v1);
        roadSegment.addVehicle(v2);
        balance = m2.calcAccelerationBalance(MovsimConstants.TO_RIGHT, roadSegment);
        assertTrue(balance < 0.0);

        // now set up with sufficient gap between vehicles, but v1 needs to brake heavily, so it is not
        // safe to change lanes
        roadSegment.clearVehicles();
        v2.setRearPosition(v1.getFrontPosition() + 2 * minimumGap);
        v2.setSpeed(80.0 / 3.6); // 80 km/h
        v1.setSpeed(120.0 / 3.6); // 120 km/h
        roadSegment.addVehicle(v1);
        roadSegment.addVehicle(v2);
        balance = m2.calcAccelerationBalance(MovsimConstants.TO_RIGHT, roadSegment);
        assertEquals(-Double.MAX_VALUE, balance, delta);
    }

    /**
     * Test method for {@link org.movsim.simulator.vehicles.lanechange.MOBIL#getMinimumGap()}.
     */
    @Test
    public final void testGetMinimumGap() {
        final double minimumGap = 2.1;
        final MOBIL mobil = new MOBIL(null, minimumGap, 0.0, 0.0, 0.0, 0.0);
        assertEquals(minimumGap, mobil.getMinimumGap(), delta);
    }

    /**
     * Test method for {@link org.movsim.simulator.vehicles.lanechange.MOBIL#getSafeDeceleration()}.
     */
    @Test
    public final void testGetSafeDeceleration() {
        final double safeDeceleration = 4.3;
        final MOBIL mobil = new MOBIL(null, 0.0, safeDeceleration, 0.0, 0.0, 0.0);
        assertEquals(safeDeceleration, mobil.getSafeDeceleration(), delta);
    }

}
