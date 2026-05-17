package com.hospit;

import com.hospit.service.impl.StatisticsComputeServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ZScoreAlgorithmTest {

    @Autowired
    private StatisticsComputeServiceImpl statisticsService;

    @Test
    void testCalculateZScore() {
        BigDecimal value = new BigDecimal("7.0");
        BigDecimal mean = new BigDecimal("5.2");
        BigDecimal stdDev = new BigDecimal("0.8");
        
        BigDecimal zScore = statisticsService.calculateZScore(value, mean, stdDev);
        
        System.out.println("Z-Score: " + zScore);
        assertTrue(zScore.abs().compareTo(new BigDecimal("2.0")) > 0, "Z-Score should be > 2");
    }

    @Test
    void testIsAnomaly() {
        BigDecimal value = new BigDecimal("7.0");
        BigDecimal mean = new BigDecimal("5.2");
        BigDecimal stdDev = new BigDecimal("0.8");
        
        boolean isAnomaly = statisticsService.isAnomaly(value, mean, stdDev, 2.0);
        
        assertTrue(isAnomaly, "Value 7.0 with mean 5.2 and std 0.8 should be anomaly at threshold 2");
    }

    @Test
    void testNormalValueNotAnomaly() {
        BigDecimal value = new BigDecimal("5.5");
        BigDecimal mean = new BigDecimal("5.2");
        BigDecimal stdDev = new BigDecimal("0.8");
        
        boolean isAnomaly = statisticsService.isAnomaly(value, mean, stdDev, 2.0);
        
        assertFalse(isAnomaly, "Value 5.5 with mean 5.2 and std 0.8 should NOT be anomaly at threshold 2");
    }

    @Test
    void testZeroStdDev() {
        BigDecimal value = new BigDecimal("7.0");
        BigDecimal mean = new BigDecimal("5.2");
        BigDecimal stdDev = BigDecimal.ZERO;
        
        BigDecimal zScore = statisticsService.calculateZScore(value, mean, stdDev);
        
        assertEquals(BigDecimal.ZERO, zScore, "Z-Score should be 0 when stdDev is 0");
    }
}
