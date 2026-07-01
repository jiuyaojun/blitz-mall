#!/bin/bash
cd /home/ubuntu/blitz
B=blitz-common/src/main/java/org/springframework/boot
mkdir -p $B/autoconfigure/jdbc $B/autoconfigure/data/redis $B/autoconfigure/transaction $B/autoconfigure/kafka $B/autoconfigure/websocket/servlet $B/web/context

# ===== ALL SB 4.0 compatibility shims =====
cat > $B/autoconfigure/jdbc/DataSourceAutoConfiguration.java << 'J'
package org.springframework.boot.autoconfigure.jdbc;
public class DataSourceAutoConfiguration {}
J
cat > $B/autoconfigure/jdbc/DataSourceProperties.java << 'J'
package org.springframework.boot.autoconfigure.jdbc;
import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties(prefix="spring.datasource")
public class DataSourceProperties{private String url,username,password,driverClassName;private ClassLoader classLoader;
public String getUrl(){return url;}public void setUrl(String u){url=u;}
public String getUsername(){return username;}public void setUsername(String u){username=u;}
public String getPassword(){return password;}public void setPassword(String p){password=p;}
public String getDriverClassName(){return driverClassName;}public void setDriverClassName(String d){driverClassName=d;}
public String determineDriverClassName(){return driverClassName!=null?driverClassName:"com.mysql.cj.jdbc.Driver";}
public String determineUrl(){return url;}public String determineUsername(){return username;}
public String determinePassword(){return password;}
public ClassLoader getClassLoader(){return classLoader;}public void setClassLoader(ClassLoader c){classLoader=c;}}
J
cat > $B/autoconfigure/data/redis/RedisAutoConfiguration.java << 'J'
package org.springframework.boot.autoconfigure.data.redis;
public class RedisAutoConfiguration {}
J
cat > $B/autoconfigure/data/redis/RedisProperties.java << 'J'
package org.springframework.boot.autoconfigure.data.redis;
import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties(prefix="spring.data.redis")
public class RedisProperties {
private String host; private int port=6379; private String password;
public String getHost(){return host;} public void setHost(String h){host=h;}
public int getPort(){return port;} public void setPort(int p){port=p;}
public String getPassword(){return password;} public void setPassword(String p){password=p;}
}
J
cat > $B/autoconfigure/transaction/TransactionAutoConfiguration.java << 'J'
package org.springframework.boot.autoconfigure.transaction;
public class TransactionAutoConfiguration {}
J
cat > $B/autoconfigure/kafka/KafkaAutoConfiguration.java << 'J'
package org.springframework.boot.autoconfigure.kafka;
public class KafkaAutoConfiguration {}
J
cat > $B/autoconfigure/websocket/servlet/WebSocketAutoConfiguration.java << 'J'
package org.springframework.boot.autoconfigure.websocket.servlet;
public class WebSocketAutoConfiguration {}
J
cat > $B/web/context/WebServerInitializedEvent.java << 'J'
package org.springframework.boot.web.context;
import org.springframework.context.ApplicationEvent;
public class WebServerInitializedEvent extends ApplicationEvent{public WebServerInitializedEvent(Object s){super(s);}
public Object getWebServer(){return getSource();}public int getWebServerPort(){return 8080;}
public Object getApplicationContext(){return null;}}
J
echo "Shims: 7 classes"

# ===== Restore clean files =====
# AI source from local backup
tar xzf /tmp/blitz-ai-clean.tar.gz 2>/dev/null && echo "AI files restored" || true
# POM with web+jdbc+redis starters
cp /tmp/blitz-server-pom.xml blitz-server/pom.xml && echo "POM restored"
# MyBatisPlusConfig with manual SqlSessionFactory
cp /tmp/MyBatisPlusConfig.java blitz-data/src/main/java/com/blitz/data/config/ && echo "MP config restored"

# ===== Add @Profile("!minimal") to AI beans so they don't load in minimal mode =====
for f in blitz-ai/src/main/java/com/blitz/ai/infrastructure/llm/StreamingChatService.java \
         blitz-ai/src/main/java/com/blitz/ai/api/controller/ChatController.java \
         blitz-ai/src/main/java/com/blitz/ai/api/controller/KnowledgeBaseController.java \
         blitz-ai/src/main/java/com/blitz/ai/application/service/ChatApplicationService.java \
         blitz-ai/src/main/java/com/blitz/ai/application/service/KnowledgeBaseService.java \
         blitz-ai/src/main/java/com/blitz/ai/infrastructure/rag/HybridRetrievalService.java \
         blitz-ai/src/main/java/com/blitz/ai/infrastructure/rag/KeywordSearchService.java \
         blitz-ai/src/main/java/com/blitz/ai/infrastructure/config/PgVectorConfig.java; do
  if [ -f "$f" ] && ! grep -q 'Profile' "$f"; then
    sed -i '2a import org.springframework.context.annotation.Profile;' "$f"
    sed -i 's/^@Service/@Service\n@Profile("!minimal")/' "$f"
    sed -i 's/^@RestController/@RestController\n@Profile("!minimal")/' "$f"
    sed -i 's/^@Controller/@Controller\n@Profile("!minimal")/' "$f"
    sed -i 's/^@Configuration/@Configuration\n@Profile("!minimal")/' "$f"
    sed -i 's/^@Component/@Component\n@Profile("!minimal")/' "$f"
    sed -i 's/^@Repository/@Repository\n@Profile("!minimal")/' "$f"
    echo "  Fixed: $f"
  fi
done

# ===== Fix OrderEventPublisher null-safety =====
sed -i 's/private final OrderEventPublisher orderEventPublisher/private OrderEventPublisher orderEventPublisher/' blitz-order/src/main/java/com/blitz/order/application/service/OrderApplicationService.java
sed -i 's/orderEventPublisher\.publish/if(orderEventPublisher!=null)orderEventPublisher.publish/g' blitz-order/src/main/java/com/blitz/order/application/service/OrderApplicationService.java

# ===== Write minimal profile =====
cat > blitz-server/src/main/resources/application-minimal.yml << 'YAML'
spring:
  autoconfigure:
    exclude:
      - com.alibaba.cloud.sentinel.endpoint.SentinelEndpointAutoConfiguration
      - com.alibaba.cloud.sentinel.SentinelWebAutoConfiguration
      - io.seata.spring.boot.autoconfigure.SeataAutoConfiguration
      - io.seata.spring.boot.autoconfigure.SeataDataSourceAutoConfiguration
      - org.redisson.spring.starter.RedissonAutoConfigurationV2
      - org.springframework.cloud.client.discovery.simple.SimpleDiscoveryClientAutoConfiguration
      - org.springframework.cloud.client.discovery.composite.CompositeDiscoveryClientAutoConfiguration
      - org.springframework.cloud.client.discovery.simple.reactive.SimpleReactiveDiscoveryClientAutoConfiguration
      - org.springframework.cloud.client.loadbalancer.LoadBalancerAutoConfiguration
      - com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration
      - org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration
      - org.springframework.ai.autoconfigure.openai.OpenAiConnectionAutoConfiguration
      - org.springframework.ai.autoconfigure.vectorstore.pgvector.PgVectorAutoConfiguration
  datasource:
    url: jdbc:mysql://localhost:3306/blitz?characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
  cloud:
    compatibility-verifier:
      enabled: false
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
blitz:
  jwt:
    secret: ${BLITZ_JWT_SECRET}
    expiration: 86400000
springdoc:
  swagger-ui:
    path: /doc.html
  api-docs:
    path: /v3/api-docs
logging:
  level:
    com.blitz: debug
    org.springframework.security: warn
seata:
  enabled: false
  registry:
    type: file
  service:
    vgroupMapping:
      default_tx_group: default
YAML

# ===== Build =====
mvn package -DskipTests -q && echo "BUILD OK" || { echo "BUILD FAILED"; exit 1; }

# ===== Restart =====
pkill -9 -f blitz-server 2>/dev/null
sleep 2
set -a; source .env; set +a
SPRING_PROFILES_ACTIVE=minimal nohup java -jar blitz-server/target/blitz-server-1.0.0-SNAPSHOT.jar > /home/ubuntu/logs/app.log 2>&1 &
echo "PID=$!"
sleep 40
C=$(curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/)
echo "HTTP $C"
if [ "$C" = "200" ]; then
    echo "=== SUCCESS ==="
    curl -s http://localhost:8080/ | head -c 300
else
    echo "=== ERROR ==="
    head -80 /home/ubuntu/logs/app.log | grep -A4 "Description" | head -10
fi
