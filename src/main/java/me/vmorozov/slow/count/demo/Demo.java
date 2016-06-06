package me.vmorozov.slow.count.demo;

import com.hazelcast.config.*;
import com.hazelcast.core.*;
import com.hazelcast.mapreduce.aggregation.Aggregations;
import com.hazelcast.mapreduce.aggregation.Supplier;
import com.hazelcast.query.PagingPredicate;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author vmorozov
 */
public class Demo {

    public static void main(String[] args) {
        //generates values on demand
        MapLoader<Long, MyEntity> loader = new MapLoader<Long, MyEntity>() {
            @Override
            public MyEntity load(Long key) {
                return null;
            }

            @Override
            public Map<Long, MyEntity> loadAll(Collection<Long> keys) {
                Random random = new Random();
                return keys.stream().collect(Collectors.toMap(k -> k, k -> {
                    if (k % 2 == 0) {
                        return new MyEntity("one", random.nextInt(10_000));
                    } else {
                        return new MyEntity("two", random.nextInt(10_000));
                    }
                }));
            }

            @Override
            public Iterable loadAllKeys() {
                Long[] i = {0L};
                return Arrays.asList(Stream.generate(() -> i[0]++).limit(1_000_000L).toArray());
            }
        };

        final Config config = new Config("instance");
        final MapConfig mapConfig = config.getMapConfig("default");
        final MapStoreConfig mapStoreConfig = new MapStoreConfig();
        mapStoreConfig.setFactoryImplementation((MapStoreFactory<Long, MyEntity>) (mapName, properties) -> loader);
        mapConfig.setMapStoreConfig(mapStoreConfig);
        mapConfig.setInMemoryFormat(InMemoryFormat.OBJECT);

        HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config);
        IMap<String, MyEntity> map = hazelcastInstance.getMap( "default" );
        map.loadAll(true);

        Predicate predicate = Predicates.equal("filterField", "two");
        long start = System.currentTimeMillis();
        long count = map.values(predicate).size();
        System.out.println("query time: " + (System.currentTimeMillis() - start) + " result: " + count);

        start = System.currentTimeMillis();
        count = map.aggregate( Supplier.fromPredicate(predicate), Aggregations.count());
        System.out.println("count time: " + (System.currentTimeMillis() - start) + " result: " + count);

        PagingPredicate pagingPredicate = new PagingPredicate(predicate, new MyComparator(), 2000 );
        start = System.currentTimeMillis();
        map.values( pagingPredicate );
        pagingPredicate.nextPage();
        System.out.println("paging time: " + (System.currentTimeMillis() - start));
    }

    static class MyComparator implements Comparator<Map.Entry> {
        @Override
        public int compare(Map.Entry o1, Map.Entry o2) {
            Integer value1 = ((Map.Entry<Long, MyEntity>)o1).getValue().getSortField();
            Integer value2 = ((Map.Entry<Long, MyEntity>)o2).getValue().getSortField();
            return value1.compareTo(value2);
        }
    }

}
