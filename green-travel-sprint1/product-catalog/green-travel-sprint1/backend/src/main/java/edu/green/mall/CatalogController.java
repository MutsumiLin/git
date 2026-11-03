package edu.green.mall;

import edu.green.common.ApiResponse;
import edu.green.common.BusinessException;
import edu.green.common.PageResult;
import edu.green.identity.DomainPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class CatalogController {
    private final CatalogRepository products;
    public CatalogController(CatalogRepository products) {this.products=products;}
    @GetMapping("/api/products")
    ApiResponse<PageResult<ProductView>> published(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {
        PageResult.validate(page,size);return ApiResponse.ok(new PageResult<>(products.published(page,size),page,size,products.publishedCount()));
    }
    @GetMapping("/api/products/{id}")
    ApiResponse<ProductView> detail(@PathVariable long id) {
        return ApiResponse.ok(products.publishedDetail(id).orElseThrow(()->BusinessException.notFound("商品不存在或已下架")));
    }
    @GetMapping("/api/admin/products")
    ApiResponse<List<ProductView>> admin() {return ApiResponse.ok(products.all());}
    @GetMapping("/api/merchant/products")
    ApiResponse<List<ProductView>> merchant(@AuthenticationPrincipal DomainPrincipal user) {return ApiResponse.ok(products.merchant(user.merchantId()));}
}
