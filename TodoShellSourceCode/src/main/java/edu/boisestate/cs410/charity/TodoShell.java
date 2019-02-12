package edu.boisestate.cs410.charity;

import com.budhash.cliche.Command;
import com.budhash.cliche.ShellFactory;

import java.io.IOException;
import java.sql.*;

public class TodoShell {
    private final Connection db;

    public TodoShell(Connection cxn) {
        db = cxn;
    }


    //REQUIRED COMMANDS

    @Command
    public void active() throws SQLException {
        String query = "SELECT task_id, task_label, task_createdate, task_duedate FROM task WHERE task_isCompleted=0 AND task_isCancelled=0";
        try (PreparedStatement stmt = db.prepareStatement(query)) {
            try (ResultSet rs = stmt.executeQuery()) {
                System.out.format("%-33s%-30s%-30s%n", "Task", "Date Created", "Due Date");
                while (rs.next()) {
                    System.out.format("%d: %-30s%-30s%-30s%n",
                            rs.getInt("task_id"),
                            rs.getString("task_label"),
                            rs.getString("task_createdate"),
                            rs.getString("task_duedate"));
                }
            }
        }catch(SQLException e){
            throw e;
        }
    }

    @Command
    public void active(String tag) throws SQLException {
        String query = "SELECT DISTINCT task_id, task_label, task_createdate, task_duedate  FROM task JOIN tag USING (task_id) WHERE tag_name=? AND task_isCompleted=0 AND task_isCancelled=0";

        try (PreparedStatement stmt = db.prepareStatement(query)) {
            stmt.setString(1, tag);
            try (ResultSet rs = stmt.executeQuery()) {
                System.out.format("%-33s%-30s%-30s%n", "Task", "Date Created", "Due Date");
                while (rs.next()) {
                    System.out.format("%d: %-30s%-30s%-30s%n",
                            rs.getInt("task_id"),
                            rs.getString("task_label"),
                            rs.getString("task_createdate"),
                            rs.getString("task_duedate"));
                }

            }
        }catch(SQLException e){
            throw e;
        }
    }

    @Command
    public void add(String... args) throws SQLException {
        String insertTask = "INSERT INTO task (task_label) VALUES (?)";
        int taskId;
        db.setAutoCommit(false);
        try {
            try (PreparedStatement stmt = db.prepareStatement(insertTask, Statement.RETURN_GENERATED_KEYS)) {
                String label = new String();
                for(int i = 0; i < args.length; i++){
                    if(i > 0) {
                        label = label + " ";
                    }
                    label = label + args[i];
                }
                stmt.setString(1, label);
                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (!rs.next()) {
                        throw new RuntimeException("no generated keys");
                    }
                    taskId = rs.getInt(1);
                    System.out.format("Creating task %d%n", taskId);
                }
            }
            db.commit();
        } catch (SQLException | RuntimeException e) {
            db.rollback();
            throw e;
        } finally {
            db.setAutoCommit(true);
        }
    }

    @Command
    public void due(int id, String date) throws SQLException {
        String query = "UPDATE task SET task_duedate=? WHERE task_id=?";
        db.setAutoCommit(false);
        try {
            try (PreparedStatement stmt = db.prepareStatement(query)) {
                stmt.setString(1, date);
                stmt.setInt(2, id);
                System.out.format("Setting due date for task %d to %s%n", id, date);
                stmt.executeUpdate();
            }
            db.commit();
        } catch (SQLException | RuntimeException e) {
            db.rollback();
            throw e;
        } finally {
            db.setAutoCommit(true);
        }
    }


    @Command
    public void tag(int id, String ...args) throws SQLException {
        String query = "INSERT INTO tag (task_id, tag_name) VALUES (?, ?)";
        db.setAutoCommit(false);
        try {
            try (PreparedStatement stmt = db.prepareStatement(query)) {
                for (int i = 0; i < args.length; i++) {
                    String tag = args[i];
                    stmt.setInt(1, id);
                    stmt.setString(2, tag);
                    System.out.format("Associating tag %s with task %d%n", tag, id);
                    stmt.executeUpdate();
                }
            }
            db.commit();
        }catch (SQLException | RuntimeException e) {
            db.rollback();
            throw e;
        } finally {
            db.setAutoCommit(true);
        }
    }

    @Command
    public void finish(int id) throws SQLException{
        String query = "Update task SET task_isCompleted=TRUE WHERE task_id=?";
        db.setAutoCommit(false);
        try {
            try (PreparedStatement stmt = db.prepareStatement(query)) {
                stmt.setInt(1, id);
                System.out.format("Setting task %d to %s%n", id, "Completed");
                stmt.executeUpdate();
            }
            db.commit();
        }catch (SQLException | RuntimeException e) {
            db.rollback();
            throw e;
        } finally {
            db.setAutoCommit(true);
        }
    }

    @Command
    public void cancel(int id) throws SQLException{
        String cancelTask = "Update task SET task_isCancelled=TRUE WHERE task_id=?";
        db.setAutoCommit(false);
        try {
            try (PreparedStatement stmt = db.prepareStatement(cancelTask)) {
                stmt.setInt(1, id);
                System.out.format("Setting task %d to %s%n", id, "Cancelled");
                stmt.executeUpdate();
            }
            db.commit();
        }catch (SQLException | RuntimeException e) {
            db.rollback();
            throw e;
        } finally {
            db.setAutoCommit(true);
        }
    }

    @Command
    public void show(int id) throws SQLException {
        String query = "SELECT task_id, task_label, task_createdate, task_duedate, task_isCompleted, task_isCancelled FROM task WHERE task_id=?";
        String status = "active";
        try (PreparedStatement stmt = db.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    System.err.format("%d: task does not exist%n", id);
                    return;
                }
                if(rs.getBoolean("task_isCompleted")){
                    status="completed";
                }
                if(rs.getBoolean("task_isCancelled")){
                    status="cancelled";
                }
                System.out.format("%-33s%-30s%-30s%-30s%n", "Task", "Date Created", "Due Date", "Status");
                System.out.format("%d: %-30s%-30s%-30s%-30s%n",
                        rs.getInt("task_id"),
                        rs.getString("task_label"),
                        rs.getString("task_createdate"),
                        rs.getString("task_duedate"),
                        status);
            }
        }
    }

    @Command
    public void completed(String tag) throws SQLException{
        String query = "SELECT DISTINCT task_id, task_label, task_createdate FROM task JOIN tag USING (task_id) WHERE tag_name=? AND task_isCompleted=1";
        try (PreparedStatement stmt = db.prepareStatement(query)) {
            stmt.setString(1, tag);

            try(ResultSet rs = stmt.executeQuery()) {
                System.out.format("%-33s%-30s%n", "Task", "Date Created");
                while (rs.next()) {
                    System.out.format("%d: %-30s%-30s%n",
                            rs.getInt("task_id"),
                            rs.getString("task_label"),
                            rs.getString("task_createdate"));
                }

            }
        }
    }

    @Command
    public void overdue() throws SQLException{
        String query = "SELECT task_id, task_label, task_createdate, task_duedate FROM task WHERE task_isCompleted=0 AND task_duedate<CURDATE()";
        try (Statement stmt = db.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            System.out.format("%-33s%-30s%-30s%n", "Task", "Date Created", "Due Date");
            while (rs.next()) {
                System.out.format("%d: %-30s%-30s%-30s%n",
                        rs.getInt("task_id"),
                        rs.getString("task_label"),
                        rs.getString("task_createdate"),
                        rs.getString("task_duedate"));
            }
        }
    }

    @Command
    public void due(String when) throws SQLException{
        String query= new String();
        if(when.equals("today")){
            query = "SELECT task_id, task_label, task_createdate, task_duedate FROM task WHERE task_isCompleted=0 AND task_duedate=CURDATE()";
        }
        if(when.equals("soon")){
            query = "SELECT task_id, task_label, task_createdate, task_duedate FROM task WHERE task_isCompleted=0 AND (task_duedate BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 3 DAY))";
        }
        try (PreparedStatement stmt = db.prepareStatement(query)) {
            try(ResultSet rs = stmt.executeQuery()) {
                System.out.format("%-33s%-30s%-30s%n", "Task", "Date Created", "Due Date");
                while (rs.next()) {
                    System.out.format("%d: %-30s%-30s%-30s%n",
                            rs.getInt("task_id"),
                            rs.getString("task_label"),
                            rs.getString("task_createdate"),
                            rs.getString("task_duedate"));
                }
            }
        }
    }

    @Command
    public void rename(int id, String... args) throws SQLException {
        String renameTask = "UPDATE task SET task_label=? WHERE task_id=?";
        db.setAutoCommit(false);
        try {
            try (PreparedStatement stmt = db.prepareStatement(renameTask)) {
                String label = new String();
                for (int i = 0; i < args.length; i++) {
                    if (i > 0) {
                        label = label + " ";
                    }
                    label = label + args[i];
                }
                stmt.setString(1, label);
                stmt.setInt(2, id);
                System.out.format("Renaming task %d to %s%n", id, label);
                stmt.executeUpdate();
            }
            db.commit();
        } catch (SQLException | RuntimeException e) {
            db.rollback();
            throw e;
        } finally {
            db.setAutoCommit(true);
        }
    }

    @Command
    public void search(String keyword) throws SQLException {
        String query = "SELECT task_id, task_label, task_createdate, task_duedate, task_isCompleted, task_isCancelled FROM task WHERE task_label LIKE ?";
        keyword = "%" + keyword + "%";
        String status = "active";
        try (PreparedStatement stmt = db.prepareStatement(query)) {
            stmt.setString(1, keyword);

            try (ResultSet rs = stmt.executeQuery()) {
                System.out.format("%-33s%-30s%-30s%-30s%n", "Task", "Date Created", "Due Date", "Status");
                while (rs.next()) {
                    if (rs.getBoolean("task_isCompleted")) {
                        status = "completed";
                    }
                    if (rs.getBoolean("task_isCancelled")) {
                        status = "cancelled";
                    }
                    System.out.format("%d: %-30s%-30s%-30s%-30s%n",
                            rs.getInt("task_id"),
                            rs.getString("task_label"),
                            rs.getString("task_createdate"),
                            rs.getString("task_duedate"),
                            status);
                }
            }
        }
    }

//    @Command
//    public void donor(int id) throws SQLException {
//        String query = "SELECT donor_name, donor_address, donor_city, donor_state, donor_zip FROM donor WHERE donor_id = ?";
//        try (PreparedStatement stmt = db.prepareStatement(query)) {
//            stmt.setInt(1, id);
//            try (ResultSet rs = stmt.executeQuery()) {
//                if (!rs.next()) {
//                    System.err.format("%d: donor does not exist%n", id);
//                    return;
//                }
//                System.out.format("%s%n", rs.getString("donor_name"));
//                System.out.format("%s%n", rs.getString("donor_address"));
//                System.out.format("%s, %s %s%n",
//                                  rs.getString("donor_city"),
//                                  rs.getString("donor_state"),
//                                  rs.getString("donor_zip"));
//            }
//        }
//    }

//    @Command
//    public void renameDonor(int id, String name) throws SQLException {
//        String query = "UPDATE donor SET donor_name = ? WHERE donor_id = ?";
//        try (PreparedStatement stmt = db.prepareStatement(query)) {
//            stmt.setString(1, name);
//            stmt.setInt(2, id);
//            System.out.format("Renaming donor %d to %s%n", id, name);
//            int nrows = stmt.executeUpdate();
//            System.out.format("updated %d donors%n", nrows);
//        }
//    }

//    @Command
//    public void addGift(int donor, String date, String... allocs) throws SQLException {
//        String insertGift = "INSERT INTO gift (donor_id, gift_date) VALUES (?, ?)";
//        String allocate = "INSERT INTO gift_fund_allocation (gift_id, fund_id, amount) VALUES (?, ?, ?)";
//        int giftId;
//        db.setAutoCommit(false);
//        try {
//            try (PreparedStatement stmt = db.prepareStatement(insertGift, Statement.RETURN_GENERATED_KEYS)) {
//                stmt.setInt(1, donor);
//                stmt.setString(2, date);
//                stmt.executeUpdate();
//                // fetch the generated gift_id!
//                try (ResultSet rs = stmt.getGeneratedKeys()) {
//                    if (!rs.next()) {
//                        throw new RuntimeException("no generated keys???");
//                    }
//                    giftId = rs.getInt(1);
//                    System.out.format("Creating gift %d%n", giftId);
//                }
//            }
//            try (PreparedStatement stmt = db.prepareStatement(allocate)) {
//                for (int i = 0; i < allocs.length; i += 2) {
//                    stmt.setInt(1, giftId);
//                    stmt.setInt(2, Integer.parseInt(allocs[i]));
//                    stmt.setBigDecimal(3, new BigDecimal(allocs[i + 1]));
//                    stmt.executeUpdate();
//                }
//            }
//            db.commit();
//        } catch (SQLException | RuntimeException e) {
//            db.rollback();
//            throw e;
//        } finally {
//            db.setAutoCommit(true);
//        }
//    }


//    @Command
//    public void funds() throws SQLException {
//        String query = "SELECT fund_id, fund_name FROM fund";
//        try (Statement stmt = db.createStatement();
//             ResultSet rs = stmt.executeQuery(query)) {
//            System.out.format("Funds:%n");
//            while (rs.next()) {
//                System.out.format("%d: %s%n",
//                                  rs.getInt("fund_id"),
//                                  rs.getString("fund_name"));
//            }
//        }
//    }

    //COMMANDS FOR DEBUGGING

    @Command
    public void tasks() throws SQLException {
        String query = "SELECT task_id, task_label, task_createdate, task_duedate, task_isCompleted, task_isCancelled FROM task";
        String status = "active";
        try (Statement stmt = db.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            System.out.format("%-33s%-30s%-30s%-30s%n", "Task", "Date Created", "Due Date", "Status");
            while (rs.next()) {
                if(rs.getBoolean("task_isCompleted")){
                    status="completed";
                }
                if(rs.getBoolean("task_isCancelled")){
                    status="cancelled";
                }
                System.out.format("%d: %-30s%-30s%-30s%-30s%n",
                        rs.getInt("task_id"),
                        rs.getString("task_label"),
                        rs.getString("task_createdate"),
                        rs.getString("task_duedate"),
                        status);
            }
        }
    }

    @Command
    public void undoFinish(int id) throws SQLException{
        String query = "Update task SET task_isCompleted=FALSE WHERE task_id=?";
        try (PreparedStatement stmt = db.prepareStatement(query)) {
            stmt.setInt(1, id);
            System.out.format("Setting task %d to %s%n", id, "active");
            stmt.executeUpdate();
        }
    }

    @Command
    public void undoCancel(int id) throws SQLException{
        String query = "Update task SET task_isCancelled=FALSE WHERE task_id=?";
        try (PreparedStatement stmt = db.prepareStatement(query)) {
            stmt.setInt(1, id);
            System.out.format("Setting task %d to %s%n", id, "active");
            stmt.executeUpdate();
        }
    }

    @Command
    public void echo(String... args) {
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                System.out.print(' ');
            }
            System.out.print(args[i]);
        }
        System.out.println();
    }

    @Command
    public void exit(){
        System.out.println("Goodbye!");
        System.exit(1);
    }

    public static void main(String[] args) throws IOException, SQLException {
        String dbUrl = args[0];
        try (Connection cxn = DriverManager.getConnection("jdbc:" + dbUrl)) {
            TodoShell shell = new TodoShell(cxn);
            ShellFactory.createConsoleShell("todolist", "", shell)
                        .commandLoop();
        }
    }
}
