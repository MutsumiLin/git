package edu.green.mall;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class CatalogRepository {
    private final JdbcTemplate jdbc;
    public CatalogRepository(JdbcTemplate jdbc) {this.jdbc=jdbc;}
    private static final String SELECT="SELECT p.*,m.name AS merchant_name FROM product p JOIN merchant m ON m.id=p.merchant_id";
    private static final RowMapper<ProductView> MAPPER=(rs,n)->new ProductView(rs.getLong("id"),rs.getLong("merchant_id"),
            rs.getString("merchant_name"),rs.getString("sku"),rs.getString("name"),rs.getString("description"),
            rs.getString("category"),rs.getLong("points_price"),rs.getInt("stock"),rs.getString("status"));
    public List<ProductView> published(int page,int size) {return jdbc.query(SELECT+" WHERE p.status='PUBLISHED' ORDER BY p.id LIMIT ? OFFSET ?",MAPPER,size,(long)page*size);}
    public long publishedCount() {return jdbc.queryForObject("SELECT COUNT(*) FROM product WHERE status='PUBLISHED'",Long.class);}
    public Optional<ProductView> publishedDetail(long id) {return jdbc.query(SELECT+" WHERE p.id=? AND p.status='PUBLISHED'",MAPPER,id).stream().findFirst();}
    public List<ProductView> merchant(long merchantId) {return jdbc.query(SELECT+" WHERE p.merchant_id=? ORDER BY p.id",MAPPER,merchantId);}
    public List<ProductView> all() {return jdbc.query(SELECT+" ORDER BY p.id",MAPPER);}
}
