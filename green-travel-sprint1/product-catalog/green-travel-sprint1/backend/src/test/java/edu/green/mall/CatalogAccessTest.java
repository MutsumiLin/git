package edu.green.mall;

import edu.green.TestSupport;
import org.junit.jupiter.api.Test;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

class CatalogAccessTest extends TestSupport {
    @Test void usersOnlySeePublishedProducts() throws Exception {
        var session=login("citizen");
        var result=mvc.perform(get("/api/products").session(session.http())).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(3)).andReturn();
        for(var item:json.readTree(result.getResponse().getContentAsString()).path("data").path("items")) {
            assertEquals("PUBLISHED",item.path("status").asText());
        }
        long draft=jdbc.queryForObject("SELECT id FROM product WHERE sku='DRAFT-GIFT'",Long.class);
        mvc.perform(get("/api/products/"+draft).session(session.http())).andExpect(status().isNotFound());
    }
    @Test void merchantDataIsScopedBySession() throws Exception {
        var session=login("merchant");
        long merchant=users.findByUsername("merchant").orElseThrow().merchantId();
        var result=mvc.perform(get("/api/merchant/products").session(session.http()).param("merchantId","999"))
                .andExpect(status().isOk()).andReturn();
        var rows=json.readTree(result.getResponse().getContentAsString()).path("data");
        assertEquals(3,rows.size());
        for(var item:rows)assertEquals(merchant,item.path("merchantId").asLong());
        mvc.perform(get("/api/admin/products").session(session.http())).andExpect(status().isForbidden());
    }
    @Test void adminCanReadAllButNotMutateCatalogInSprintOne() throws Exception {
        var session=login("operator");
        mvc.perform(get("/api/admin/products").session(session.http())).andExpect(status().isOk()).andExpect(jsonPath("$.data.length()").value(4));
        mvc.perform(post("/api/admin/products").session(session.http()).header(session.header(),session.token()).contentType("application/json").content("{}"))
                .andExpect(status().isMethodNotAllowed());
    }
    @Test void paginationRejectsInvalidValues() throws Exception {
        var session=login("citizen");
        mvc.perform(get("/api/products?size=101").session(session.http())).andExpect(status().isBadRequest());
        mvc.perform(get("/api/products?page=-1").session(session.http())).andExpect(status().isBadRequest());
    }
}
