package com.pbshop.springshop.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class FlywayMigrationTest {

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void appliesInitialMigrationAndCreatesCoreTables() {
        assertThat(flyway.info().current()).isNotNull();
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("10");

        List<String> tables = jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'PUBLIC'",
                String.class
        );

        assertThat(tables)
                .contains(
                        "USERS",
                        "CATEGORIES",
                        "SELLERS",
                        "PRODUCTS",
                        "PRODUCT_SPECS",
                        "PRICE_ENTRIES",
                        "SYSTEM_SETTINGS",
                        "AUTH_VERIFICATION_CODES",
                        "AUTH_ACCESS_TOKENS",
                        "AUTH_REFRESH_TOKENS",
                        "AUTH_PASSWORD_RESET_REQUESTS",
                        "SOCIAL_ACCOUNTS",
                        "SPEC_DEFINITIONS",
                        "PRICE_ALERTS",
                        "ADDRESSES",
                        "CART_ITEMS",
                        "ORDERS",
                        "ORDER_ITEMS",
                        "PAYMENTS",
                        "REVIEWS",
                        "WISHLIST_ITEMS",
                        "POINT_TRANSACTIONS",
                        "BOARDS",
                        "POSTS",
                        "POST_LIKES",
                        "POST_COMMENTS",
                        "PRODUCT_INQUIRIES",
                        "SUPPORT_TICKETS",
                        "SUPPORT_TICKET_REPLIES",
                        "RECENT_PRODUCT_VIEWS",
                        "SEARCH_HISTORIES",
                        "CHAT_ROOMS",
                        "CHAT_ROOM_MEMBERS",
                        "CHAT_MESSAGES",
                        "PUSH_SUBSCRIPTIONS",
                        "PUSH_PREFERENCES",
                        "DEALS",
                        "RECOMMENDATIONS"
                );
    }
}
