package com.syos.adapter.in.cli.commands;

import com.syos.adapter.in.cli.io.ConsoleIO;
import com.syos.adapter.in.cli.session.SessionManager;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Displays simple WEB order history for current customer.
 */
public class OrderHistoryCommand implements Command {
    private final ConsoleIO console;
    private final SessionManager sessionManager;

    public OrderHistoryCommand(ConsoleIO console, SessionManager sessionManager) {
        this.console = console;
        this.sessionManager = sessionManager;
    }

    @Override
    public void execute() {
        if (!sessionManager.isLoggedIn() || !sessionManager.isCustomer()) {
            console.printError("Customer login required to view order history.");
            return;
        }
        long userId = sessionManager.getCurrentUserId();
        List<ViewCartCommand.WebOrder> orders = ViewCartCommand.getOrdersForUser(userId);
        console.println("\n\u2554\u2550\u2550 ORDER HISTORY \u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550");
        if (orders.isEmpty()) {
            console.println("You have no previous web orders.");
            console.println("\nPress Enter to continue...");
            console.readLine();
            return;
        }
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (ViewCartCommand.WebOrder o : orders) {
            console.println("\nOrder #" + o.orderNo + "  Date: " + fmt.format(o.dateTime) + "  Total: LKR " + String.format("%.2f", o.total));
            for (ViewCartCommand.OrderLine l : o.lines) {
                console.println("  - " + l.name + " x " + String.format("%.2f", l.qty) + " @ LKR " + String.format("%.2f", l.unit) +
                        " = LKR " + String.format("%.2f", l.lineTotal));
            }
        }
        console.println("\nPress Enter to continue...");
        console.readLine();
    }
}
