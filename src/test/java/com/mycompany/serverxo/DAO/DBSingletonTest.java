package com.mycompany.serverxo.DAO;

import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

class DBSingletonTest {

    @Test
    void DBSingletonTest(){
        Connection con=DBSingleton.getConnection();
        assertNotNull(con);
    }


}