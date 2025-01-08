package pl.gpiwosz.wordpdfconverter.repositories;

import org.apache.camel.spi.IdempotentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.gpiwosz.wordpdfconverter.enums.FileStatusEnum;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.params.SetParams;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;

public class RedisIdempotentRepository implements IdempotentRepository {

  private static final Logger LOG = LoggerFactory.getLogger(RedisIdempotentRepository.class);
  private static final String DEFAULT_KEY_PREFIX = "camel.idempotent.";
  private final String keyPrefix;
  private final JedisPool jedisPool;
  private final Duration expiryTime;
  private final String fileStatus;

  public RedisIdempotentRepository(String redisUrl, String keyPrefix, FileStatusEnum fileStatus, Duration expiryTime) throws URISyntaxException {
    this.keyPrefix = (keyPrefix == null || keyPrefix.isEmpty()) ? DEFAULT_KEY_PREFIX : keyPrefix;
    this.expiryTime = expiryTime;
    this.fileStatus = fileStatus.toString();

    URI redisUri = new URI(redisUrl);
    JedisPoolConfig poolConfig = new JedisPoolConfig();
    // Configure pool as needed (e.g., maxTotal, maxIdle, minIdle)
    this.jedisPool = new JedisPool(poolConfig, redisUri);
  }

  public static RedisIdempotentRepository redisIdempotentRepository(String redisUrl, String keyPrefix, FileStatusEnum fileStatus, Duration expiryTime) throws URISyntaxException {
    return new RedisIdempotentRepository(redisUrl, keyPrefix, fileStatus, expiryTime);
  }


  @Override
  public boolean add(String messageId) {
    return addValue(messageId, fileStatus, true);
  }

  public boolean add(String messageId, String value) {
    return addValue(messageId, value, false);
  }

  private boolean addValue(String messageId, String value, boolean nx) {
    try (Jedis jedis = jedisPool.getResource()) {
      String key = keyPrefix + messageId;
      if (nx) {
        return jedis.set(key, value, SetParams.setParams().nx().ex(expiryTime.toSeconds())) != null;
      } else {
        return jedis.set(key, value, SetParams.setParams().ex(expiryTime.toSeconds())) != null;
      }
    } catch (Exception e) {
      LOG.error("Error interacting with Redis: {}", e.getMessage(), e);
      return false; // Important: Return false on error to prevent message loss
    }
  }

  @Override
  public boolean contains(String messageId) {
    try (Jedis jedis = jedisPool.getResource()) {
      String key = keyPrefix + messageId;
      return jedis.exists(key);
    } catch (Exception e) {
      LOG.error("Error interacting with Redis: {}", e.getMessage(), e);
      return false; // Return false on error
    }
  }

  public boolean containsValue(String messageId, String value) {
    try (Jedis jedis = jedisPool.getResource()) {
      String key = keyPrefix + messageId;
      return jedis.exists(key) && value.equals(jedis.get(key));
    } catch (Exception e) {
      LOG.error("Error interacting with Redis: {}", e.getMessage(), e);
      return false; // Return false on error
    }
  }

  public FileStatusEnum get(String messageId) {
    try (Jedis jedis = jedisPool.getResource()) {
      String key = keyPrefix + messageId;
      return FileStatusEnum.valueOf(jedis.get(key));
    } catch (Exception e) {
      LOG.error("Error interacting with Redis: {}", e.getMessage(), e);
     throw new RuntimeException(e);
    }
  }

  @Override
  public boolean remove(String messageId) {
    try (Jedis jedis = jedisPool.getResource()) {
      String key = keyPrefix + messageId;
      return jedis.del(key) == 1;
    } catch (Exception e) {
      LOG.error("Error interacting with Redis: {}", e.getMessage(), e);
      return false; // Return false on error
    }
  }

  @Override
  public boolean confirm(String messageId) {
    // No-op for Redis as add already confirms (using SETNX or SET with NX)
    return true;
  }

  @Override
  public void clear() {
    jedisPool.clear();
  }

  @Override
  public void start() {
    // JedisPool is initialized in the constructor
    LOG.info("Redis Idempotent Repository started.");
  }

  @Override
  public void stop() {
    if (jedisPool != null) {
      jedisPool.close();
      jedisPool.destroy();
    }
    LOG.info("Redis Idempotent Repository stopped.");
  }
}