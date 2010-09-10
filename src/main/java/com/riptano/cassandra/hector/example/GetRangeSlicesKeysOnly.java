package com.riptano.cassandra.hector.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.prettyprint.cassandra.model.HectorException;
import me.prettyprint.cassandra.model.KeyspaceOperator;
import me.prettyprint.cassandra.model.Mutator;
import me.prettyprint.cassandra.model.OrderedRows;
import me.prettyprint.cassandra.model.RangeSlicesQuery;
import me.prettyprint.cassandra.model.Result;
import me.prettyprint.cassandra.model.Row;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.CassandraClient;
import me.prettyprint.cassandra.service.CassandraClientPool;
import me.prettyprint.cassandra.service.CassandraClientPoolFactory;
import me.prettyprint.cassandra.service.Cluster;
import me.prettyprint.cassandra.service.Keyspace;
import me.prettyprint.cassandra.utils.StringUtils;
import me.prettyprint.hector.api.factory.HFactory;

import org.apache.cassandra.thrift.Column;
import org.apache.cassandra.thrift.ColumnParent;
import org.apache.cassandra.thrift.ColumnPath;
import org.apache.cassandra.thrift.KeyRange;
import org.apache.cassandra.thrift.SlicePredicate;
import org.apache.cassandra.thrift.SliceRange;

/**
 * Use get_range_slices to retrieve the keys without deserializing the columns.
 * For clear results, it's best to run this on an empty ColumnFamily.
 * 
 * To run this example from maven:
 * mvn -e exec:java -Dexec.mainClass="com.riptano.cassandra.hector.example.GetRangeSlicesKeysOnly"
 * 
 * @author zznate
 *
 */
public class GetRangeSlicesKeysOnly {

    private static StringSerializer stringSerializer = StringSerializer.get();
    
    public static void main(String[] args) throws Exception {
        
        Cluster cluster = HFactory.getOrCreateCluster("TestCluster", "localhost:9160");

        KeyspaceOperator keyspaceOperator = HFactory.createKeyspaceOperator("Keyspace1", cluster);
                
        try {
            Mutator<String> mutator = HFactory.createMutator(keyspaceOperator, stringSerializer);

            for (int i = 0; i < 5; i++) {            
                mutator.addInsertion("fake_key_" + i, "Standard1", HFactory.createStringColumn("fake_column_0", "fake_value_0_" + i))
                .addInsertion("fake_key_" + i, "Standard1", HFactory.createStringColumn("fake_column_1", "fake_value_1_" + i))
                .addInsertion("fake_key_" + i, "Standard1", HFactory.createStringColumn("fake_column_2", "fake_value_2_" + i));            
            }
            mutator.execute();
            
            RangeSlicesQuery<String, String, String> rangeSlicesQuery = 
                HFactory.createRangeSlicesQuery(keyspaceOperator, stringSerializer, stringSerializer, stringSerializer);
            rangeSlicesQuery.setColumnFamily("Standard1");            
            rangeSlicesQuery.setKeys("fake_key_", "");
            rangeSlicesQuery.setReturnKeysOnly();
            
            rangeSlicesQuery.setRowCount(5);
            Result<OrderedRows<String, String, String>> result = rangeSlicesQuery.execute();
            OrderedRows<String, String, String> orderedRows = result.get();            
            
            Row<String,String,String> lastRow = orderedRows.peekLast();

            System.out.println("Contents of rows: \n");                       
            for (Row<String, String, String> r : orderedRows) {
                System.out.println("   " + r);
            }
            
            
        } catch (HectorException he) {
            he.printStackTrace();
        }
    }
        
}
