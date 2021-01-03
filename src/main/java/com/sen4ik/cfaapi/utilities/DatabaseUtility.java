package com.sen4ik.cfaapi.utilities;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.sql.*;

@Component
@Slf4j
public class DatabaseUtility {

    @Autowired
    private Environment env;

    private Connection getConn() throws ClassNotFoundException, SQLException {
        String myDriver = env.getProperty("spring.datasource.driver-class-name");
        String myUrl = env.getProperty("spring.datasource.url");
        Class.forName(myDriver);
        Connection conn = DriverManager.getConnection(
                myUrl,
                env.getProperty("spring.datasource.username"),
                env.getProperty("spring.datasource.password")
        );
        return conn;
    }

    public void executeUpdate(String query) throws SQLException, ClassNotFoundException {
        Connection conn = getConn();
        Statement st = conn.createStatement();
        st.executeUpdate(query);
        st.close();
        conn.close();
    }

}
