
import java.util.Random;

public class RedisTest {
    // Запуск докер-контейнера:
    // docker run --rm --name skill-redis -p 127.0.0.1:6379:6379/tcp -d redis

    public static void main(String[] args) throws InterruptedException {
        RedisStorage redis = new RedisStorage();
        redis.init();
        for (int i = 0; i < 20; i++) {
            redis.logPageVisit(i);
            Thread.sleep(10);
        }
        int a = 0;
        int size = redis.getUser().size();
        for (int x = 0; ; x++) {
            if (a == size) a = 0;
            Object user = redis.getUser().first();
            System.out.println("-пользователь " + user);
            redis.add((String) user);
            if (x % 10 == 0 && x != 0) {
                int user_id = new Random().ints(0, 20).findFirst().getAsInt();
//                redis.delete(String.valueOf(user_id));
                redis.add(String.valueOf(user_id));
                System.out.println(">пользователю " + user_id + " доступна платная услуга \n-пользователь " + user_id);
                Thread.sleep(100);
            }
            a++;
            Thread.sleep(100);
        }
    }
}


