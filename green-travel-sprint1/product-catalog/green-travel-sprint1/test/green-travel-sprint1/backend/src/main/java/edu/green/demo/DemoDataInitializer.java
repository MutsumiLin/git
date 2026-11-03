package edu.green.demo;

import edu.green.identity.UserProvisioningService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
@ConditionalOnProperty(name="app.seed-demo",havingValue="true")
public class DemoDataInitializer implements ApplicationRunner {
    private final JdbcTemplate jdbc;
    private final UserProvisioningService users;
    private final String password;
    private final Clock clock;
    public DemoDataInitializer(JdbcTemplate jdbc,UserProvisioningService users,@Value("${app.demo-password}") String password,Clock clock) {
        this.jdbc=jdbc;this.users=users;this.password=password;this.clock=clock;
    }
    @Override
    @Transactional(rollbackFor=Exception.class)
    public void run(ApplicationArguments args) {
        long merchant=merchant("青禾低碳生活馆");
        long other=merchant("城市公共服务站");
        users.createIfAbsent("citizen",password,"USER",null);
        users.createIfAbsent("citizen2",password,"USER",null);
        users.createIfAbsent("operator",password,"ADMIN",null);
        users.createIfAbsent("merchant",password,"MERCHANT",merchant);
        users.createIfAbsent("merchant2",password,"MERCHANT",other);
        product(merchant,"ECO-BAG","循环环保袋","一只可重复使用的帆布袋，让每一次出门都少一点负担。","日常好物",100,20,"PUBLISHED");
        product(merchant,"ECO-CUP","随行环保杯","带上自己的杯子，把绿色习惯留在日常。","日常好物",300,8,"PUBLISHED");
        product(other,"BIKE-VOUCHER","骑行体验券","城市骑行服务体验，用一段骑行认识你的城市。","城市服务",150,15,"PUBLISHED");
        product(merchant,"DRAFT-GIFT","待上架活动礼品","此商品用于验证用户端不会展示草稿商品。","运营测试",200,5,"DRAFT");
        // 重启只补充缺失演示记录，不清空行程、不覆盖余额或库存。
    }
    private long merchant(String name) {
        var existing=jdbc.query("SELECT id FROM merchant WHERE name=?",(rs,n)->rs.getLong(1),name);
        if(!existing.isEmpty())return existing.getFirst();
        jdbc.update("INSERT INTO merchant(name,status,created_at) VALUES(?,'ACTIVE',?)",name,now());
        return jdbc.queryForObject("SELECT id FROM merchant WHERE name=?",Long.class,name);
    }
    private void product(long merchant,String sku,String name,String description,String category,long points,int stock,String status) {
        if(jdbc.queryForObject("SELECT COUNT(*) FROM product WHERE sku=?",Long.class,sku)>0)return;
        jdbc.update("INSERT INTO product(merchant_id,sku,name,description,category,points_price,stock,status,created_at) VALUES(?,?,?,?,?,?,?,?,?)",
                merchant,sku,name,description,category,points,stock,status,now());
    }
    private LocalDateTime now(){return LocalDateTime.ofInstant(clock.instant(),ZoneOffset.UTC);}
}
