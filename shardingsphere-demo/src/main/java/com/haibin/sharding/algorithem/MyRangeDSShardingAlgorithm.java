package com.haibin.sharding.algorithem;

import org.apache.shardingsphere.api.sharding.standard.RangeShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingValue;

import java.util.Arrays;
import java.util.Collection;

public class MyRangeDSShardingAlgorithm implements RangeShardingAlgorithm<Long> {
    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames, RangeShardingValue<Long> shardingValue) {
        //select * from course where cid between 1 and 100;
        Long upperVal = shardingValue.getValueRange().upperEndpoint();
        Long lowerVal = shardingValue.getValueRange().lowerEndpoint();
        String logicTableName = shardingValue.getLogicTableName();
        return Arrays.asList("m1","m2");
    }
}
