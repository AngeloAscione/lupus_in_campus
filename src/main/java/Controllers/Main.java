package Controllers;

import Model.Utils.ConPool;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws SQLException {
        ConPool cp = new ConPool();
        Connection connection = cp.getConnection();


        String createTableSQL = "CREATE TABLE prova(ID int not null auto_increment primary key)";

        Statement statement = connection.createStatement();
        statement.executeUpdate(createTableSQL);
        System.out.println("Tabella creata con successo!");

    }
}