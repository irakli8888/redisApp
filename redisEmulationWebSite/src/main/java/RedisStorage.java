import org.redisson.Redisson;
import org.redisson.api.RKeys;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisConnectionException;
import org.redisson.config.Config;

import java.util.Date;

public class RedisStorage {

    // Объект для работы с Redis
    private RedissonClient redisson;

    // Объект для работы с ключами
    private RKeys rKeys;

    // Объект для работы с Sorted Set'ом
    private RScoredSortedSet<String> onlineUsers;

    private RScoredSortedSet<String> newUsers;

    private final static String KEY = "ONLINE_USERS";

    long getTs() {
        return System.currentTimeMillis();
    }

    // Пример вывода всех ключей
    public void listKeys() {
        Iterable<String> keys = rKeys.getKeys();
        for(String key: keys) {
            System.out.println("KEY: " + key + ", type:" + rKeys.getType(key));
        }
    }
    void deleteFirst(){
        newUsers.pollFirst();
    }

    void init() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        try {
            redisson = Redisson.create(config);
        } catch (RedisConnectionException Exc) {
            System.out.println("Не удалось подключиться к Redis");
            System.out.println(Exc.getMessage());
        }
        rKeys = redisson.getKeys();
        onlineUsers = redisson.getScoredSortedSet(KEY);
        newUsers = redisson.getScoredSortedSet(KEY);
        rKeys.delete(KEY);
    }

    void shutdown() {
        redisson.shutdown();
    }

    // Фиксирует посещение пользователем страницы
    void logPageVisit(int user_id)
    {
        //ZADD ONLINE_USERS
        newUsers.add(getTs(), String.valueOf(user_id));
    }

    Object getElement(int a){
        return newUsers.valueRange(a,a);
    }
    void add(String v){
        newUsers.addAndGetRevRank(getTs(),v);
    }

    RScoredSortedSet<String> getUser(){
        return newUsers;
    }

    void delete(Object o){
        newUsers.remove(o);
    }
    // Удаляет
    void deleteOldEntries(int secondsAgo)
    {
        //ZREVRANGEBYSCORE ONLINE_USERS 0 <time_5_seconds_ago>
        newUsers.removeRangeByScore(0, true, getTs() - secondsAgo, true);
    }
    int calculateUsersNumber()
    {
        //ZCOUNT ONLINE_USERS
        return newUsers.count(Double.NEGATIVE_INFINITY, true, Double.POSITIVE_INFINITY, true);
    }
}
