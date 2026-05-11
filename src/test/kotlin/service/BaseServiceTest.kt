package service

import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.After
import org.junit.Before
import tables

abstract class BaseServiceTest {
    private lateinit var database: Database

    @Before
    fun setup() {
        database = Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
        transaction {
            SchemaUtils.create(*tables)
        }
    }

    @After
    fun tearDown() {
        // H2 in-memory is closed when connection is closed, but since we use DB_CLOSE_DELAY=-1, we might want to drop tables
        transaction {
            SchemaUtils.drop(*tables)
        }
    }
}
