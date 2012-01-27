package webtech.artistcollector.data;

import webtech.artistcollector.gui.Main;

import java.sql.*;
import java.util.Collection;

/**
 * Zugriff auf die Datenbank zum Speichern der Namen
 */
public class Database {

    final String DB_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

    Connection connection;

    public Database(String url, String user, String password) {
        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            return;
        }

        try {
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Database() {
        this("jdbc:sqlserver://draphony.de\\SQLEXPRESS;" + "\n" +
                "\t\t\t\"databaseName=dphWeb.Artists", "dphWeb.sql_artists", "QvOtv&u.ünW3cl3T");
    }

    public boolean isReady() {
        return connection != null;
    }

    public void InsertNames(Collection<CollectionAndArtist> names) {
        String sql = "INSERT INTO dbo.Artists (lname,fname,collection,crawler,verified,url) VALUES ";
        StringBuilder sb = new StringBuilder(sql);
        int size = names.size();
        int inserted = 0;

        if (size == 0) {
            System.out.println("No Rows were inserted, because none were supplied");
            return;
        }

        Main.getInstance().reportStatus(0, "Inserting " + size + " names into Database");

        int i = 0;
        for (CollectionAndArtist item : names) {

            try {
                i++;
                PreparedStatement stmt = connection.prepareStatement(
                        "INSERT INTO dbo.Artists (lname,fname,[collection],crawler,verified,url) " +
                        "VALUES (?,?,?,?,?,?)");

                stmt.setString(1, item.getFirstName());
                stmt.setString(2, item.getLastName());
                stmt.setString(3, item.getCollection());
                stmt.setString(4, item.getCrawler());
                stmt.setInt(5, item.verified);
                stmt.setString(6, item.getURL());

                int rows = stmt.executeUpdate();
                inserted += rows;

                Main.getInstance().reportStatus(((float)i / (float)size) * 100, "Entry " + i + " / " + size);
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }

        System.out.println(inserted + " Rows have been inserted");
    }

    String escape(String string) {
        if (string == null) return "";
        return string.replaceAll("['\"\\\\]", "\\\\$0");
    }
}
