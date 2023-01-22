package jd.core.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class TryResources {

    List<String> readFile(Path path) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8); Scanner sc = new Scanner(br)) {
            while (sc.hasNextLine()) {
                lines.add(sc.nextLine());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lines;
    }

    List<String> readFile(File file) {
        List<String> lines = new ArrayList<>();
        try (InputStream in = Files.newInputStream(file.toPath());
                InputStreamReader isr = new InputStreamReader(in);
                Scanner sc = new Scanner(isr)) {
            while (sc.hasNextLine()) {
                lines.add(sc.nextLine());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lines;
    }
    
    public static Map.Entry<Integer, String> get(Connection connection, int employeeId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM employee WHERE employee_id = ?")) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String employeeName = rs.getString("employee_name");
                    return new SimpleEntry<>(employeeId, employeeName);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            throw new SQLException(e);
        }
        return null;
    }
}
