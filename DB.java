package com.form;

import java.sql.*;

public class DB {
    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/research_platform","root","");
    }
    
}
